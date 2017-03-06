package com.joshschriever.livenotes.musicxml;

public class KeySigHandler {

    public static final int[] STEP_INDICES = new int[] {0, 2, 4, 5, 7, 9, 11};

    public static final int[] FIFTHS = new int[]
            {-7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7};
    public static final String[] KEYS_MAJOR = new String[]
            {"Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#"};
    public static final String[] KEYS_MINOR = new String[]
            {"Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#", "G#", "D#", "A#"};

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

    public String defaultAccidentalForNoteValue(int value) {
        //TODO - actually use the key signature to get the correct default for the pitch
        return "natural";
    }

    public String octaveForNoteValue(int value) {
        return Integer.toString(value / 12);
    }

    private String pitchForNoteValue(int value) {
        //TODO - use sharps or flats depending on key
        return NOTES_SHARP[value % 12];
    }

}
