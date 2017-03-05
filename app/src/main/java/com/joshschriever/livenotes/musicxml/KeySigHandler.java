package com.joshschriever.livenotes.musicxml;

public class KeySigHandler {

    private static final String[] NOTES_SHARP = new String[]
            {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static final String[] NOTES_FLAT = new String[]
            {"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"};

    final int fifths;
    final boolean isMajor;

    public KeySigHandler(int fifths, boolean isMajor) {
        this.fifths = fifths;
        this.isMajor = isMajor;
    }

    public String stepForNoteValue(int value) {
        return pitchForNoteValue(value).substring(0, 1);
    }

    public int alterForNoteValue(int value) {
        String pitch = pitchForNoteValue(value);
        return pitch.length() > 1 ? pitch.contains("#") ? 1 : -1 : 0;
    }

    public String accidentalForNoteValue(int value) {
        //TODO - actually use the key signature to get the correct accidental
        int alter = alterForNoteValue(value);
        return alter == 0 ? "natural" : alter == 1 ? "sharp" : "flat";
    }

    public String octaveForNoteValue(int value) {
        return Integer.toString(value / 12);
    }

    private String pitchForNoteValue(int value) {
        //TODO - use sharps or flats depending on key
        return NOTES_SHARP[value % 12];
    }

}
