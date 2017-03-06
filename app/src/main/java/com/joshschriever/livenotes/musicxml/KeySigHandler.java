package com.joshschriever.livenotes.musicxml;

import android.util.SparseArray;

public class KeySigHandler {

    public static final int[] FIFTHS = new int[]
            {-7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7};
    public static final String[] KEYS_MAJOR = new String[]
            {"Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#"};
    public static final String[] KEYS_MINOR = new String[]
            {"Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#", "G#", "D#", "A#"};

    private static SparseArray<String[]> NOTES = new SparseArray<>();
    private static SparseArray<int[]> STEP_INDICES = new SparseArray<>();

    static {
        NOTES.append(-7, new String[]
                {"C", "Db", "D", "Eb", "Fb", "F", "Gb", "G", "Ab", "A", "Bb", "Cb"});
        NOTES.append(-6, new String[]
                {"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "Cb"});
        for (int fifths = -5; fifths < 0; fifths++) {
            NOTES.append(fifths, new String[]
                    {"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"});
        }
        for (int fifths = 0; fifths < 6; fifths++) {
            NOTES.append(fifths, new String[]
                    {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"});
        }
        NOTES.append(6, new String[]
                {"C", "C#", "D", "D#", "E", "E#", "F#", "G", "G#", "A", "A#", "B"});
        NOTES.append(7, new String[]
                {"B#", "C#", "D", "D#", "E", "E#", "F#", "G", "G#", "A", "A#", "B"});

        for (int fifths : FIFTHS) {
            int first = (fifths < 0 ? (fifths * -5) : (fifths * 7)) % 12;
            STEP_INDICES.append(fifths, new int[]
                    {first, first + 2, first + 4, first + 5, first + 7, first + 9, first + 11});
        }
    }

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
        int alter = alterForNoteValue(value);
        return alter == 0 ? "natural" : alter == 1 ? "sharp" : "flat";
    }

    public String octaveForNoteValue(int value) {
        return Integer.toString(value / 12);
    }

    private String pitchForNoteValue(int value) {
        return NOTES.get(fifths)[value % 12];
    }

    public int[] stepIndices() {
        return STEP_INDICES.get(fifths);
    }
}
