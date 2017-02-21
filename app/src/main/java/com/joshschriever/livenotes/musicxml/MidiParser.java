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
    private long currentMeasureLength;

    public MidiParser(int beatsPerMeasure, int tempo) {
        margin = marginInMillis(beatsPerMeasure, tempo);
        fullMeasureLength = measureLengthInMillis(beatsPerMeasure, tempo);
        currentMeasureLength = 0L;

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
        restOnEvent(timeStamp, true);
        restOnEvent(timeStamp, false);
    }

    public void startWithNote(MidiMessage message) {
        if (message instanceof ShortMessage) {
            restOnEvent(System.currentTimeMillis(), ((ShortMessage) message).getData1() < 48);
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
            parseShortMessage((ShortMessage) message, System.currentTimeMillis());
        }
    }

    private void parseShortMessage(ShortMessage message, long timestamp) {
        if (message.getCommand() == ShortMessage.NOTE_ON) {
            noteOnEvent(timestamp, message.getData1());
        } else if (message.getCommand() == ShortMessage.NOTE_OFF) {
            noteOffEvent(timestamp, message.getData1());
        }
    }

    private void noteOnEvent(long timestamp, int noteValue) {
        boolean trebleClef = noteValue >= 48;
        if (tempRestRegistry[trebleClef ? 1 : 0] != 0L) {
            restOffEvent(timestamp, trebleClef);
        }

        tempNoteRegistry[noteValue] = timestamp;
        Note note = new Note((byte) noteValue, 0L);
        doNoteOn(note);
    }

    private void noteOffEvent(long timestamp, int noteValue) {
        doNoteOff(noteOffNoteFor(timestamp, noteValue));

        tempNoteRegistry[noteValue] = 0L;
        tempNoteTieRegistry[noteValue] = false;
        tempNoteChordRegistry[noteValue] = false;

        boolean trebleClef = noteValue >= 48;
        if (stream(Arrays.copyOfRange(tempNoteRegistry,
                                      trebleClef ? 48 : 0,
                                      trebleClef ? tempNoteRegistry.length : 48))
                .allMatch(t -> t == 0L)) {
            restOnEvent(timestamp, trebleClef);
        }
    }

    private Note noteOffNoteFor(long timestamp, int noteValue) {
        Note note = new Note((byte) noteValue, timestamp - tempNoteRegistry[noteValue]);
        note.setEndOfTie(tempNoteTieRegistry[noteValue]);
        note.setHasAccompanyingNotes(tempNoteChordRegistry[noteValue]);
        return note;
    }

    private void restOnEvent(long timeStamp, boolean trebleClef) {
        tempRestRegistry[trebleClef ? 1 : 0] = timeStamp;
        Note note = new Note((byte) (trebleClef ? 50 : 45), 0L);
        note.setRest(true);
        doNoteOn(note);
    }

    private void restOffEvent(long timeStamp, boolean trebleClef) {
        doNoteOff(restOffNoteFor(timeStamp, trebleClef));

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

    //TODO - doNoteOn also needs to make a new measure if needed - compare the current time to when the last new measure was started
    private void doNoteOn(Note event) {
        //TODO - figure out if chord - including for rests - use note.setHasAccompanyingNotes()
        //TODO - store whether chord or not in temp registry
        fireNoteEvent(event);
    }

    private void doNoteOff(Note event) {
        if (currentMeasureLength + event.getDuration() >= fullMeasureLength + margin) {
            newMeasure(System.currentTimeMillis());//TODO
            event.setDuration(event.getDuration() - (fullMeasureLength - currentMeasureLength));
            event.setEndOfTie(!event.isRest());
            doNoteOff(event);
        } else {
            currentMeasureLength += event.getDuration();//TODO - chords will mess this up
            fireNoteEvent(event);
        }
    }

    private void newMeasure(long timeStamp) {
        stopCurrentNotesForMeasureBreak(timeStamp);
        fireMeasureEvent();
        currentMeasureLength = 0L;
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
                doNoteOn(note);
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
