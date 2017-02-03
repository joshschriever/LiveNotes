package com.joshschriever.livenotes.musicxml;

import android.util.Log;

import org.jfugue.MidiMessageRecipient;
import org.jfugue.MidiParser;

import jp.kshoji.javax.sound.midi.MidiMessage;

public class MidiToXMLRenderer implements MidiMessageRecipient {

    private Callbacks callbacks;
    private MusicXmlRenderer renderer;
    private MidiParser parser;

    private boolean ready = false;
    private boolean recording = false;

    public MidiToXMLRenderer(Callbacks callbacks, int beats, int beatValue, int tempo) {
        this.callbacks = callbacks;
        renderer = new MusicXmlRenderer(tempo);Log.d("MidiToXML", "beats: " + beats + ", beatValue: " + beatValue);//TODO
        parser = new MidiParser();
        parser.addParserListener(renderer);
    }

    public void setReady() {
        ready = true;
    }

    public void startRecording() {
        if (ready && !recording) {
            recording = true;
            //TODO - start task or whatever
        }
    }

    public void stopRecording() {
        if (recording) {
            ready = false;
            recording = false;
            //TODO - stop task or whatever
        }
    }

    @Override
    public void messageReady(MidiMessage midiMessage, long timeStamp) {
        if (ready) {
            if (!recording) {
                recording = true;
                callbacks.onStartRecording();
            }

            parser.parse(midiMessage, timeStamp);
            callbacks.onXMLUpdated();
        }
    }

    public String getXML() {
        return renderer.getMusicXMLString();
    }

    public interface Callbacks {

        void onXMLUpdated();

        void onStartRecording();
    }

}
