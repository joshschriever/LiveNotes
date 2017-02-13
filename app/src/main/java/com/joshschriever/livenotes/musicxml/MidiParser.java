package com.joshschriever.livenotes.musicxml;

import jp.kshoji.javax.sound.midi.MetaMessage;
import jp.kshoji.javax.sound.midi.MidiEvent;
import jp.kshoji.javax.sound.midi.MidiMessage;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.ShortMessage;
import jp.kshoji.javax.sound.midi.SysexMessage;
import jp.kshoji.javax.sound.midi.Track;
import org.jfugue.ChannelPressure;
import org.jfugue.Controller;
import org.jfugue.Instrument;
import org.jfugue.Note;
import org.jfugue.Parser;
import org.jfugue.PitchBend;
import org.jfugue.PolyphonicPressure;
import org.jfugue.Tempo;
import org.jfugue.Time;
import org.jfugue.TimeFactor;
import org.jfugue.Voice;

// Forked from JFugue
public final class MidiParser extends Parser {
    long[][] tempNoteRegistry = new long[16][255];
    byte[][] tempNoteAttackRegistry = new byte[16][255];
    int tempo = 120;
    private static final int DEFAULT_TEMPO = 120;

    public MidiParser() {
        for(int m = 0; m < 16; ++m) {
            for(int n = 0; n < 255; ++n) {
                tempNoteRegistry[m][n] = 0L;
                tempNoteAttackRegistry[m][n] = 0;
            }
        }

    }

    public void parse(Sequence sequence) {
        tempo = 120;
        Track[] tracks = sequence.getTracks();
        long totalCount = 0L;
        long counter = 0L;

        for(byte t = 0; t < tracks.length; ++t) {
            totalCount += (long)tracks[t].size();
        }

        for(int var12 = 0; var12 < tracks.length; ++var12) {
            int trackSize = tracks[var12].size();
            if(trackSize > 0) {
                fireVoiceEvent(new Voice((byte)var12));

                for(int ev = 0; ev < trackSize; ++ev) {
                    ++counter;
                    fireProgressReported("Parsing MIDI...", counter, totalCount);
                    MidiEvent event = tracks[var12].get(ev);
                    MidiMessage message = event.getMessage();
                    trace(new Object[]{"Message received: ", message});
                    parse(message, event.getTick());
                }
            }
        }

    }

    public void parse(MidiMessage message, long timestamp) {
        if(message instanceof ShortMessage) {
            parseShortMessage((ShortMessage)message, timestamp);
        } else if(message instanceof SysexMessage) {
            parseSysexMessage((SysexMessage)message, timestamp);
        } else if(message instanceof MetaMessage) {
            parseMetaMessage((MetaMessage)message, timestamp);
        }

    }

    private void parseShortMessage(ShortMessage message, long timestamp) {
        int track = message.getChannel();
        switch(message.getCommand()) {
            case 128:
                noteOffEvent(timestamp, track, message.getData1(), message.getData2());
                break;
            case 144:
                if(message.getData2() == 0) {
                    noteOffEvent(timestamp, track, message.getData1(), message.getData2());
                } else {
                    noteOnEvent(timestamp, track, message.getData1(), message.getData2());
                }
                break;
            case 160:
                trace(new Object[]{"Poly pressure on key ", Integer.valueOf(message.getData1()), ", pressure = ", Integer.valueOf(message.getData2())});
                PolyphonicPressure poly = new PolyphonicPressure((byte)message.getData1(), (byte)message.getData2());
                fireTimeEvent(new Time(timestamp));
                fireVoiceEvent(new Voice((byte)track));
                firePolyphonicPressureEvent(poly);
                break;
            case 176:
                trace(new Object[]{"Controller change to ", Integer.valueOf(message.getData1()), ", value = ", Integer.valueOf(message.getData2())});
                Controller controller = new Controller((byte)message.getData1(), (byte)message.getData2());
                fireTimeEvent(new Time(timestamp));
                fireVoiceEvent(new Voice((byte)track));
                fireControllerEvent(controller);
                break;
            case 192:
                trace(new Object[]{"Program change to ", Integer.valueOf(message.getData1())});
                Instrument instrument = new Instrument((byte)message.getData1());
                fireTimeEvent(new Time(timestamp));
                fireVoiceEvent(new Voice((byte)track));
                fireInstrumentEvent(instrument);
                break;
            case 208:
                trace(new Object[]{"Channel pressure, pressure = ", Integer.valueOf(message.getData1())});
                ChannelPressure pressure = new ChannelPressure((byte)message.getData1());
                fireTimeEvent(new Time(timestamp));
                fireVoiceEvent(new Voice((byte)track));
                fireChannelPressureEvent(pressure);
                break;
            case 224:
                trace(new Object[]{"Pitch Bend, data1= ", Integer.valueOf(message.getData1()), ", data2= ", Integer.valueOf(message.getData2())});
                PitchBend bend = new PitchBend((byte)message.getData1(), (byte)message.getData2());
                fireTimeEvent(new Time(timestamp));
                fireVoiceEvent(new Voice((byte)track));
                firePitchBendEvent(bend);
                break;
            default:
                trace(new Object[]{"Unparsed message: ", Integer.valueOf(message.getCommand())});
        }

    }

    private void noteOnEvent(long timestamp, int track, int data1, int data2) {
        trace(new Object[]{"Note on ", Integer.valueOf(data1), " - attack is ", Integer.valueOf(data2)});
        tempNoteRegistry[track][data1] = timestamp;
        tempNoteAttackRegistry[track][data1] = (byte)data2;
        Note note = new Note((byte)data1, 0L);
        note.setDecimalDuration(0.0D);
        note.setAttackVelocity((byte)data2);
        fireNoteEvent(note);
    }

    private void noteOffEvent(long timestamp, int track, int data1, int data2) {
        long time = tempNoteRegistry[track][data1];
        trace(new Object[]{"Note off ", Integer.valueOf(data1), " - decay is ", Integer.valueOf(data2), ". Duration is ", Long.valueOf(timestamp - time)});
        fireTimeEvent(new Time(time));
        fireVoiceEvent(new Voice((byte)track));
        Note note = new Note((byte)data1, timestamp - time);
        note.setDecimalDuration((double)(timestamp - time) / ((double)tempo * 4.0D));
        note.setAttackVelocity(tempNoteAttackRegistry[track][data1]);
        note.setDecayVelocity((byte)data2);
        fireNoteEvent(note);
        tempNoteRegistry[track][data1] = 0L;
    }

    private void parseSysexMessage(SysexMessage message, long timestamp) {
        trace(new Object[]{"SysexMessage received but not parsed by JFugue (doesn\'t use them)"});
    }

    private void parseMetaMessage(MetaMessage message, long timestamp) {
        switch(message.getType()) {
            case 81:
                parseTempo(message, timestamp);
            case 89:
            default:
                trace(new Object[]{"MetaMessage received but not parsed by JFugue (doesn\'t use them)"});
        }
    }

    private void parseTempo(MetaMessage message, long timestamp) {
        int beatsPerMinute = TimeFactor.parseMicrosecondsPerBeat(message, timestamp);
        trace(new Object[]{"Tempo Event, bpm = ", Integer.valueOf(beatsPerMinute)});
        fireTimeEvent(new Time(timestamp));
        fireTempoEvent(new Tempo(beatsPerMinute));
        tempo = beatsPerMinute;
    }
}
