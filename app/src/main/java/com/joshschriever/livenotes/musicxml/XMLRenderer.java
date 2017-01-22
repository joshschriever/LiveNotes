package com.joshschriever.livenotes.musicxml;

import org.jfugue.MusicXmlRenderer;

public class XMLRenderer {

    private Callbacks callbacks;
    private MusicXmlRenderer renderer;

    public XMLRenderer(Callbacks callbacks) {
        this.callbacks = callbacks;
        renderer = new MusicXmlRenderer();
        renderer.doFirstMeasure(true);
    }

    public String getXML() {
        return renderer.getMusicXMLString();
    }

    public interface Callbacks {

        void onNewXML(String newXML);
    }

}
