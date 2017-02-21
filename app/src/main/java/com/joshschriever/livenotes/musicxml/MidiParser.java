package com.joshschriever.livenotes.musicxml;

import org.jfugue.Measure;
import org.jfugue.Note;
import org.jfugue.ParserListener;

import java.util.Arrays;

import jp.kshoji.javax.sound.midi.MidiMessage;
import jp.kshoji.javax.sound.midi.ShortMessage;
import jp.kshoji.javax.swing.event.EventListenerList;

import static java8.util.J8Arrays.stream;

// Forked from JFugue
public class MidiParser {

    private EventListenerList listenerList = new EventListenerList();

    private long[] tempNoteRegistry = new long[255];
    private long[] tempRestRegistry = new long[] {0L, 0L};
    private boolean[] tempNoteTieRegistry = new boolean[255];
    private boolean[] tempNoteChordRegistry = new boolean[255];
    private boolean[] tempRestChordRegistry = new boolean[] {false, false};

    private final long margin;
    private final long fullMeasureLength;
    private long currentMeasureStartTime;

    public MidiParser(int beatsPerMeasure, int tempo) {
        margin = marginInMillis(beatsPerMeasure, tempo);
        fullMeasureLength = measureLengthInMillis(beatsPerMeasure, tempo);

        for (int n = 0; n < 255; ++n) {
            tempNoteRegistry[n] = 0L;
            tempNoteTieRegistry[n] = false;
            tempNoteChordRegistry[n] = false;
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

    public void addParserListener(ParserListener listener) {
        listenerList.add(ParserListener.class, listener);
    }

    public void removeParserListener(ParserListener listener) {
        listenerList.remove(ParserListener.class, listener);
    }

    public void startWithRests() {
        long timeStamp = System.currentTimeMillis();
        currentMeasureStartTime = timeStamp;
        restOnEvent(timeStamp, true);
        restOnEvent(timeStamp, false);
    }

    public void startWithNote(MidiMessage message) {
        long timeStamp = System.currentTimeMillis();
        currentMeasureStartTime = timeStamp;
        if (message instanceof ShortMessage) {
            restOnEvent(timeStamp, ((ShortMessage) message).getData1() < 48);
            parseShortMessage(timeStamp, (ShortMessage) message);
        }
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

    public void parse(MidiMessage message) {
        if (message instanceof ShortMessage) {
            parseShortMessage(System.currentTimeMillis(), (ShortMessage) message);
        }
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

        doNoteOn(timeStamp, new Note((byte) noteValue, 0L));
    }

    private void noteOffEvent(long timeStamp, int noteValue) {
        doNoteOff(timeStamp, noteOffNoteFor(timeStamp, noteValue));

        tempNoteRegistry[noteValue] = 0L;
        tempNoteTieRegistry[noteValue] = false;
        tempNoteChordRegistry[noteValue] = false;

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
        note.setHasAccompanyingNotes(tempNoteChordRegistry[noteValue]);
        return note;
    }

    private void restOnEvent(long timeStamp, boolean trebleClef) {
        Note note = new Note((byte) (trebleClef ? 50 : 45), 0L);
        note.setRest(true);
        doNoteOn(timeStamp, note);
    }

    private void restOffEvent(long timeStamp, boolean trebleClef) {
        doNoteOff(timeStamp, restOffNoteFor(timeStamp, trebleClef));

        tempRestRegistry[trebleClef ? 1 : 0] = 0L;
        tempRestChordRegistry[trebleClef ? 1 : 0] = false;
    }

    private Note restOffNoteFor(long timeStamp, boolean trebleClef) {
        Note note = new Note((byte) (trebleClef ? 50 : 45),
                             timeStamp - tempRestRegistry[trebleClef ? 1 : 0]);
        note.setHasAccompanyingNotes(tempRestChordRegistry[trebleClef ? 1 : 0]);
        note.setRest(true);
        return note;
    }

    private void doNoteOn(long timeStamp, Note event) {
        if (timeStamp - currentMeasureStartTime + margin >= fullMeasureLength) {
            newMeasure(currentMeasureStartTime + fullMeasureLength);
        }

        //TODO - figure out if chord - including for rests - use note.setHasAccompanyingNotes()
        //TODO - store whether chord or not in temp registry

        if (event.isRest()) {
            tempRestRegistry[event.getValue() >= 48 ? 1 : 0] = timeStamp;
        } else {
            tempNoteRegistry[event.getValue()] = timeStamp;
        }
        fireNoteEvent(event);
    }

    private void doNoteOff(long timeStamp, Note event) {
        if (timeStamp - currentMeasureStartTime >= fullMeasureLength + margin) {
            event.setDuration(event.getDuration() - (currentMeasureStartTime + fullMeasureLength
                    - tempNoteRegistry[event.getValue()]));
            event.setEndOfTie(!event.isRest());
            newMeasure(currentMeasureStartTime + fullMeasureLength);
            doNoteOff(timeStamp, event);
        } else {
            fireNoteEvent(event);
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
            tempRestChordRegistry[0] = false;
            restOnEvent(timeStamp, false);
        }
        if (tempRestRegistry[1] != 0L) {
            tempRestChordRegistry[1] = false;
            restOnEvent(timeStamp, true);
        }

        for (int n = 0; n < 255; ++n) {
            if (tempNoteRegistry[n] != 0L) {
                tempNoteChordRegistry[n] = false;
                tempNoteTieRegistry[n] = true;
                tempNoteRegistry[n] = timeStamp;
                Note note = new Note((byte) n, 0L);
                note.setEndOfTie(true);
                doNoteOn(timeStamp, note);
            }
        }
    }

    private void fireNoteEvent(Note event) {
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ParserListener.class) {
                ((ParserListener) listeners[i + 1]).noteEvent(event);
            }
        }
    }

    private void fireMeasureEvent() {
        Measure measure = new Measure();
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ParserListener.class) {
                ((ParserListener) listeners[i + 1]).measureEvent(measure);
            }
        }
    }

}
