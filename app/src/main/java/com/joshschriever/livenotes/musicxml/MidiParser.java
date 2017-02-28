package com.joshschriever.livenotes.musicxml;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.kshoji.javax.sound.midi.ShortMessage;

import static java8.util.J8Arrays.stream;
import static java8.util.stream.StreamSupport.stream;

// Based on JFugue
public class MidiParser {

    private List<SimpleParserListener> listeners = new ArrayList<>();

    private long[] tempNoteRegistry = new long[255];
    private long[] tempRestRegistry = new long[] {0L, 0L};
    private boolean[] tempNoteTieRegistry = new boolean[255];

    private final DurationUtil durationUtil;
    private final long margin;
    private final long fullMeasureLength;
    private long currentMeasureStartTime;

    public MidiParser(DurationUtil durationUtil) {
        this.durationUtil = durationUtil;
        margin = durationUtil.shortestNoteLengthInMillis();
        fullMeasureLength = durationUtil.measureLengthInMillis();

        for (int n = 0; n < 255; ++n) {
            tempNoteRegistry[n] = 0L;
            tempNoteTieRegistry[n] = false;
        }
    }

    public void addParserListener(SimpleParserListener listener) {
        if (listeners.indexOf(listener) == -1) {
            listeners.add(listener);
        }
    }

    public void startWithRests() {
        long timeStamp = System.currentTimeMillis();
        currentMeasureStartTime = timeStamp;
        restOnEvent(timeStamp, true);
        restOnEvent(timeStamp, false);
    }

    public void startWithNote(ShortMessage message) {
        long timeStamp = System.currentTimeMillis();
        currentMeasureStartTime = timeStamp;
        restOnEvent(timeStamp, (message).getData1() < 48);
        parseShortMessage(timeStamp, message);
    }

    public void stop() {
        long timeStamp = System.currentTimeMillis();

        if (tempRestRegistry[0] != 0L) {
            restOffEvent(timeStamp, false);
        }
        if (tempRestRegistry[1] != 0L) {
            restOffEvent(timeStamp, true);
        }

        for (int n = 0; n < 255; ++n) {
            if (tempNoteRegistry[n] != 0L) {
                noteOffEvent(timeStamp, n);
            }
        }
    }

    public void parse(ShortMessage message) {
        parseShortMessage(System.currentTimeMillis(), message);
    }

    private void parseShortMessage(long timeStamp, ShortMessage message) {
        if (message.getCommand() == ShortMessage.NOTE_ON) {
            noteOnEvent(timeStamp, message.getData1());
        } else if (message.getCommand() == ShortMessage.NOTE_OFF) {
            noteOffEvent(timeStamp, message.getData1());
        }
    }

    private void noteOnEvent(long timeStamp, int noteValue) {
        newMeasureIfNeededForNoteOn(timeStamp);

        boolean trebleClef = noteValue >= 48;
        if (tempRestRegistry[trebleClef ? 1 : 0] != 0L) {
            restOffEvent(timeStamp, trebleClef);
        }

        tempNoteRegistry[noteValue] = timeStamp;
        fireNoteEvent(Note.newNote(timeStamp, 0L, noteValue).build());
    }

    private void noteOffEvent(long timeStamp, int noteValue) {
        doNoteOff(timeStamp, noteOffNoteFor(timeStamp, noteValue, false));

        tempNoteRegistry[noteValue] = 0L;
        tempNoteTieRegistry[noteValue] = false;

        boolean trebleClef = noteValue >= 48;
        if (stream(Arrays.copyOfRange(tempNoteRegistry,
                                      trebleClef ? 48 : 0,
                                      trebleClef ? tempNoteRegistry.length : 48))
                .allMatch(t -> t == 0L)) {
            restOnEvent(timeStamp, trebleClef);
        }
    }

    private Note noteOffNoteFor(long timeStamp, int noteValue, boolean startOfTie) {
        long startTime = tempNoteRegistry[noteValue];

        return Note.newNote(startTime, timeStamp - startTime, noteValue)
                   .withEndOfTie(tempNoteTieRegistry[noteValue])
                   .withStartOfTie(startOfTie)
                   .build();
    }

    private void restOnEvent(long timeStamp, boolean trebleClef) {
        newMeasureIfNeededForNoteOn(timeStamp);

        tempRestRegistry[trebleClef ? 1 : 0] = timeStamp;
        fireNoteEvent(Note.newRest(timeStamp, 0L, trebleClef).build());
    }

    private void restOffEvent(long timeStamp, boolean trebleClef) {
        doNoteOff(timeStamp, restOffNoteFor(timeStamp, trebleClef));

        tempRestRegistry[trebleClef ? 1 : 0] = 0L;
    }

    private Note restOffNoteFor(long timeStamp, boolean trebleClef) {
        long startTime = tempRestRegistry[trebleClef ? 1 : 0];

        return Note.newRest(startTime, timeStamp - startTime, trebleClef).build();
    }

    private void doNoteOff(long timeStamp, Note note) {
        if (timeStamp - currentMeasureStartTime >= fullMeasureLength + margin) {
            long newMeasureTime = currentMeasureStartTime + fullMeasureLength;
            Note newNote = Note.newNote(newMeasureTime,
                                        note.durationMillis
                                                - (newMeasureTime - tempNoteRegistry[note.value]),
                                        note.value)
                               .withEndOfTie(true)
                               .build();

            newMeasure(newMeasureTime);
            doNoteOff(timeStamp, newNote);
        } else {
            fireNoteEvent(note);
        }
    }

    private void newMeasureIfNeededForNoteOn(long timeStamp) {
        while (timeStamp - currentMeasureStartTime + margin >= fullMeasureLength) {
            newMeasure(currentMeasureStartTime + fullMeasureLength);
        }
    }

    private void newMeasure(long timeStamp) {
        stopCurrentNotesForMeasureBreak(timeStamp);
        fireMeasureEvent();
        currentMeasureStartTime = timeStamp;
        restartNotesAfterMeasureBreak(timeStamp);
    }

    private void stopCurrentNotesForMeasureBreak(long timeStamp) {
        if (tempRestRegistry[0] != 0L) {
            fireNoteEvent(restOffNoteFor(timeStamp, false));
        }
        if (tempRestRegistry[1] != 0L) {
            fireNoteEvent(restOffNoteFor(timeStamp, true));
        }

        for (int n = 0; n < 255; ++n) {
            if (tempNoteRegistry[n] != 0L) {
                fireNoteEvent(noteOffNoteFor(timeStamp, n, true));
            }
        }
    }

    private void restartNotesAfterMeasureBreak(long timeStamp) {
        if (tempRestRegistry[0] != 0L) {
            restOnEvent(timeStamp, false);
        }
        if (tempRestRegistry[1] != 0L) {
            restOnEvent(timeStamp, true);
        }

        for (int n = 0; n < 255; ++n) {
            if (tempNoteRegistry[n] != 0L) {
                tempNoteTieRegistry[n] = true;
                tempNoteRegistry[n] = timeStamp;
                fireNoteEvent(Note.newNote(timeStamp, 0L, n).withEndOfTie(true).build());
            }
        }
    }

    private void fireNoteEvent(Note note) {
        Pair<Note, List<Note>> pair = durationUtil.getNoteSequenceFromNote(note);
        stream(listeners).forEach(listener -> listener.noteEvent(pair.first, pair.second));
    }

    private void fireMeasureEvent() {
        stream(listeners).forEach(SimpleParserListener::measureEvent);
    }

}
