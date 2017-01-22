package com.joshschriever.livenotes.musicxml;

public class MusicXMLRenderer extends org.jfugue.MusicXmlRenderer {

    public MusicXMLRenderer() {
        doFirstMeasure(true);
    }

    @Override
    public final String getMusicXMLString() {
        String xml = super.getMusicXMLString();
        int divisionsEnd = xml.indexOf("</divisions>") + 12;
        int keyStart = xml.indexOf("<key>");
        int keyEnd = xml.indexOf("</key>") + 6;

        return xml.substring(0, divisionsEnd)
                + xml.substring(keyStart, keyEnd)
                + xml.substring(divisionsEnd, keyStart)
                + xml.substring(keyEnd);
    }

}
