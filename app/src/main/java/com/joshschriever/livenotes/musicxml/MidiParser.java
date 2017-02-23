package com.joshschriever.livenotes.musicxml;

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

    private final long margin;
    private final long fullMeasureLength;
    private long currentMeasureStartTime;

    public MidiParser(int beatsPerMeasure, int tempo) {
        margin = marginInMillis(beatsPerMeasure, tempo);
        fullMeasureLength = measureLengthInMillis(beatsPerMeasure, tempo);

        for (int n = 0; n < 255; ++n) {
            tempNoteRegistry[n] = 0L;
            tempNoteTieRegistry[n] = false;
        }
    }

    private long marginInMillis(int beats, int tempo) {
        final boolean isCompound = (beats % 3 == 0) && (beats / 3 > 1);
        return 60_000 / tempo / (isCompound ? 3 : 1) / 4;
    }

    private long measureLengthInMillis(int beats, int tempo) {
        final boolean isCompound = (beats % 3 == 0) && (beats / 3 > 1);
        return beats * 60_000 / tempo / (isCompound ? 3 : 1);
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
        boolean trebleClef = noteValue >= 48;
        if (tempRestRegistry[trebleClef ? 1 : 0] != 0L) {
            restOffEvent(timeStamp, trebleClef);
        }

        Note note = new Note((byte) noteValue, 0L);

        newMeasureIfNeededForNoteOn(timeStamp);
        tempNoteRegistry[noteValue] = timeStamp;
        fireNoteEvent(note);
    }

    private void noteOffEvent(long timeStamp, int noteValue) {
        doNoteOff(noteOffNoteFor(timeStamp, noteValue));

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

    private Note noteOffNoteFor(long timeStamp, int noteValue) {
        Note note = new Note((byte) noteValue, timeStamp - tempNoteRegistry[noteValue]);
        note.setEndOfTie(tempNoteTieRegistry[noteValue]);
        return note;
    }

    private void restOnEvent(long timeStamp, boolean trebleClef) {
        Note note = new Note((byte) (trebleClef ? 50 : 45), 0L);
        note.setRest(true);

        newMeasureIfNeededForNoteOn(timeStamp);
        tempRestRegistry[trebleClef ? 1 : 0] = timeStamp;
        fireNoteEvent(note);
    }

    private void restOffEvent(long timeStamp, boolean trebleClef) {
        doNoteOff(restOffNoteFor(timeStamp, trebleClef));

        tempRestRegistry[trebleClef ? 1 : 0] = 0L;
    }

    private Note restOffNoteFor(long timeStamp, boolean trebleClef) {
        Note note = new Note((byte) (trebleClef ? 50 : 45),
                             timeStamp - tempRestRegistry[trebleClef ? 1 : 0]);
        note.setRest(true);
        return note;
    }

    private void doNoteOff(Note note) {
        if (note.timeStamp - currentMeasureStartTime >= fullMeasureLength + margin) {
            note.setDuration(note.getDuration() - (currentMeasureStartTime + fullMeasureLength
                    - tempNoteRegistry[note.value]));
            note.setEndOfTie(!note.isRest);
            newMeasure(currentMeasureStartTime + fullMeasureLength);
            doNoteOff(note);
        } else {
            fireNoteEvent(note);
        }
    }

    private void newMeasureIfNeededForNoteOn(long timeStamp) {
        if (timeStamp - currentMeasureStartTime + margin >= fullMeasureLength) {
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
                Note note = noteOffNoteFor(timeStamp, n);
                note.setStartOfTie(true);
                fireNoteEvent(note);
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
                Note note = new Note((byte) n, 0L);
                note.setEndOfTie(true);
                fireNoteEvent(note);
            }
        }
    }

    private void fireNoteEvent(Note note) {
        Note firstNote = note;//TODO
        List<Note> tiedNotes = new ArrayList<>();//TODO
        //TODO - convert the given note into the proper sequence of tied notes

        fireNoteEvent(firstNote, tiedNotes);
    }

    private void fireNoteEvent(Note note, List<Note> tiedNotes) {
        stream(listeners).forEach(listener -> listener.noteEvent(note, tiedNotes));
    }

    private void fireMeasureEvent() {
        stream(listeners).forEach(SimpleParserListener::measureEvent);
    }

}
