package com.joshschriever.livenotes.musicxml;

import org.jfugue.MidiMessageRecipient;
import org.jfugue.MidiParser;

import jp.kshoji.javax.sound.midi.MidiMessage;

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
        parser.parse(midiMessage, timeStamp);
        callbacks.onXMLUpdated();
    }

    public String getXML() {
        return renderer.getMusicXMLString();
    }

    public interface Callbacks {

        void onXMLUpdated();
    }

}
