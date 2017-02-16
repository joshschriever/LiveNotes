package com.joshschriever.livenotes.musicxml;

import org.jfugue.Note;
import org.jfugue.ParserListener;

import java.util.Arrays;

import jp.kshoji.javax.sound.midi.MidiMessage;
import jp.kshoji.javax.sound.midi.ShortMessage;
import jp.kshoji.javax.swing.event.EventListenerList;

import static java8.util.J8Arrays.stream;

// Forked from JFugue
public final class MidiParser {

    private EventListenerList listenerList = new EventListenerList();
    private long[] tempNoteRegistry = new long[255];
    private long[] tempRestRegistry = new long[] {0L, 0L};
    private long[] roughNoteRegistry = new long[255];
    private long[] roughRestRegistry = new long[] {0L, 0L};

    public MidiParser() {
        for (int n = 0; n < 255; ++n) {
            tempNoteRegistry[n] = 0L;
            roughNoteRegistry[n] = 0L;
        }
    }

    public void addParserListener(ParserListener listener) {
        listenerList.add(ParserListener.class, listener);
    }

    public void removeParserListener(ParserListener listener) {
        listenerList.remove(ParserListener.class, listener);
    }

    public void startWithRests() {
        restOnEvent(-1, true);
        restOnEvent(-1, false);
    }

    public void startWithNote(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage) {
            restOnEvent(timeStamp, ((ShortMessage) message).getData1() < 48);
        }
    }

    public void stop() {
        if (tempRestRegistry[0] != 0L) {
            restOffEvent(-1, false);
        }
        if (tempRestRegistry[1] != 0L) {
            restOffEvent(-1, true);
        }

        for (int n = 0; n < 255; ++n) {
            if (tempNoteRegistry[n] != 0L) {
                noteOffEvent(-1, n);
            }
        }
    }

    public void parse(MidiMessage message, long timestamp) {
        if (message instanceof ShortMessage) {
            parseShortMessage((ShortMessage) message, timestamp);
        }
    }

    private void parseShortMessage(ShortMessage message, long timestamp) {
        switch (message.getCommand()) {
            case ShortMessage.NOTE_OFF:
                noteOffEvent(timestamp, message.getData1());
                break;
            case ShortMessage.NOTE_ON:
                if (message.getData2() == 0) {
                    noteOffEvent(timestamp, message.getData1());
                } else {
                    noteOnEvent(timestamp, message.getData1());
                }
                break;
        }
    }

    private void noteOnEvent(long timestamp, int noteValue) {
        boolean trebleClef = noteValue >= 48;
        if (tempRestRegistry[trebleClef ? 1 : 0] != 0L) {
            restOffEvent(timestamp - 1, trebleClef);
        }

        tempNoteRegistry[noteValue] = timestamp;
        roughNoteRegistry[noteValue] = System.currentTimeMillis();
        Note note = new Note((byte) noteValue, 0L);
        fireNoteEvent(note);
    }

    private void noteOffEvent(long timestamp, int noteValue) {
        long duration = timestamp == -1
                        ? System.currentTimeMillis() - roughNoteRegistry[noteValue]
                        : timestamp - tempNoteRegistry[noteValue];
        Note note = new Note((byte) noteValue, duration);
        fireNoteEvent(note);
        tempNoteRegistry[noteValue] = 0L;
        roughNoteRegistry[noteValue] = 0L;

        boolean trebleClef = noteValue >= 48;
        if (stream(Arrays.copyOfRange(tempNoteRegistry,
                                      trebleClef ? 48 : 0,
                                      trebleClef ? tempNoteRegistry.length : 48))
                .allMatch(t -> t == 0L)) {
            restOnEvent(timestamp + 1, trebleClef);
        }
    }

    private void restOnEvent(long timeStamp, boolean trebleClef) {
        tempRestRegistry[trebleClef ? 1 : 0] = timeStamp;
        roughRestRegistry[trebleClef ? 1 : 0] = System.currentTimeMillis();
        Note note = new Note((byte) (trebleClef ? 50 : 45), 0L);
        note.setRest(true);
        fireNoteEvent(note);
    }

    private void restOffEvent(long timeStamp, boolean trebleClef) {
        long duration = tempRestRegistry[trebleClef ? 1 : 0] == -1 || timeStamp == -1
                        ? System.currentTimeMillis() - roughRestRegistry[trebleClef ? 1 : 0]
                        : timeStamp - tempRestRegistry[trebleClef ? 1 : 0];
        Note note = new Note((byte) (trebleClef ? 50 : 45), duration);
        note.setRest(true);
        fireNoteEvent(note);
        tempRestRegistry[trebleClef ? 1 : 0] = 0L;
        roughRestRegistry[trebleClef ? 1 : 0] = 0L;
    }

    private void fireNoteEvent(Note event) {
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ParserListener.class) {
                ((ParserListener) listeners[i + 1]).noteEvent(event);
            }
        }
    }

    private void fireParallelNoteEvent(Note event) { //TODO - handle chords - including for rests
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ParserListener.class) {
                ((ParserListener) listeners[i + 1]).parallelNoteEvent(event);
            }
        }
    }

}
