package com.joshschriever.livenotes.musicxml;

public class MidiToXMLRenderer {

    private Callbacks callbacks;
    private MusicXMLRenderer renderer;

    public MidiToXMLRenderer(Callbacks callbacks) {
        this.callbacks = callbacks;
        renderer = new MusicXMLRenderer();
    }

    public String getXML() {
        return renderer.getMusicXMLString();
    }

    public interface Callbacks {

        void onNewXML(String newXML);
    }

}
