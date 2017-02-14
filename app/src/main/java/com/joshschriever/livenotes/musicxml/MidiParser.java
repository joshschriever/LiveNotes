package com.joshschriever.livenotes.musicxml;

import org.jfugue.Measure;
import org.jfugue.Note;
import org.jfugue.ParserListener;

import jp.kshoji.javax.sound.midi.MidiMessage;
import jp.kshoji.javax.sound.midi.ShortMessage;
import jp.kshoji.javax.swing.event.EventListenerList;

// Forked from JFugue
public final class MidiParser {

    private EventListenerList listenerList = new EventListenerList();
    private long[] tempNoteRegistry = new long[255];
    private long[] tempRestRegistry = new long[] {0L, 0L};

    public MidiParser() {
        for (int n = 0; n < 255; ++n) {
            tempNoteRegistry[n] = 0L;
        }

        //TODO
    }

    public void addParserListener(ParserListener listener) {
        listenerList.add(ParserListener.class, listener);
    }

    public void removeParserListener(ParserListener listener) {
        listenerList.remove(ParserListener.class, listener);
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
        tempNoteRegistry[noteValue] = timestamp;
        Note note = new Note((byte) noteValue, 0L);
        fireNoteEvent(note);
    }

    private void noteOffEvent(long timestamp, int noteValue) {
        long time = tempNoteRegistry[noteValue];
        Note note = new Note((byte) noteValue, timestamp - time);
        fireNoteEvent(note);
        tempNoteRegistry[noteValue] = 0L;
    }

    private void fireNoteEvent(Note event) {
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ParserListener.class) {
                ((ParserListener) listeners[i + 1]).noteEvent(event);
            }
        }
    }

    private void fireParallelNoteEvent(Note event) {
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ParserListener.class) {
                ((ParserListener) listeners[i + 1]).parallelNoteEvent(event);
            }
        }
    }

    private void fireRestOnEvent(long timeStamp, boolean trebleClef) {
        tempRestRegistry[trebleClef ? 1 : 0] = timeStamp;
        Note note = new Note((byte) (trebleClef ? 80 : 40), 0L);
        note.setRest(true);
        fireNoteEvent(note);
    }

    private void fireRestOffEvent(long timeStamp, boolean trebleClef) {
        long time = tempRestRegistry[trebleClef ? 1 : 0];
        Note note = new Note((byte) (trebleClef ? 80 : 40), timeStamp - time);
        note.setRest(true);
        fireNoteEvent(note);
    }

    private void fireMeasureEvent() {
        Object[] listeners = this.listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ParserListener.class) {
                ((ParserListener) listeners[i + 1]).measureEvent(new Measure());
            }
        }

    }

}
