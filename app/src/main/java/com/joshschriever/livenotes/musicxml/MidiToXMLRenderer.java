package com.joshschriever.livenotes.musicxml;

import org.jfugue.MidiMessageRecipient;
import org.jfugue.MidiParser;

import jp.kshoji.javax.sound.midi.MidiMessage;
import jp.kshoji.javax.sound.midi.ShortMessage;

public class MidiToXMLRenderer implements MidiMessageRecipient {

    private Callbacks callbacks;
    private MusicXmlRenderer renderer;
    private MidiParser parser;

    public MidiToXMLRenderer(Callbacks callbacks) {
        this.callbacks = callbacks;
        renderer = new MusicXmlRenderer();
        renderer.doFirstMeasure(true);
        parser = new MidiParser();
        parser.addParserListener(renderer);
    }

    @Override
    public void messageReady(MidiMessage midiMessage, long timeStamp) {
        parser.removeParserListener(renderer);
        renderer = new MusicXmlRenderer();
        parser.addParserListener(renderer);
        //TODO - find a way around the above 3 lines
        parser.parse(midiMessage, timeStamp);

        if (midiMessage.getStatus() == ShortMessage.NOTE_OFF) {
            callbacks.onXMLUpdated();
        }
    }

    public String getXML() {
        return renderer.getMusicXMLString();
    }

    public interface Callbacks {

        void onXMLUpdated();
    }

}
