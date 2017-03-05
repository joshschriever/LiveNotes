package com.joshschriever.livenotes.musicxml;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import java8.lang.Integers;

public class DurationHandler {

    private static final int ONE_MINUTE = 60_000;
    private static final int MAX_BEAT_TYPE = 8;
    public static final int DIVISIONS_PER_BEAT = 4;

    final int beatsPerMeasure;
    final int beatType;
    final int markedTempo;
    private final int actualBeatsTempo;
    private final int precision;

    public DurationHandler(int beatsPerMeasure, int beatType, int tempo) {
        this.beatsPerMeasure = beatsPerMeasure;
        this.beatType = beatType;
        markedTempo = tempo;
        actualBeatsTempo = markedTempo * (isTimeSignatureCompound() ? 3 : 1);
        precision = 4;//TODO - precision options: 1, 2, 4, 8 - Minimum: MAX_BEAT_TYPE / beatType
    }

    //Note - this is the absolute shortest note, not accounting for the precision level
    public long shortestNoteLengthInMillis() {
        return ONE_MINUTE / markedTempo / (isTimeSignatureCompound() ? 3 : 1) / DIVISIONS_PER_BEAT;
    }

    public long measureLengthInMillis() {
        return beatsPerMeasure * ONE_MINUTE / markedTempo / (isTimeSignatureCompound() ? 3 : 1);
    }

    public boolean isTimeSignatureCompound() {
        return ((beatsPerMeasure % 3) == 0) && ((beatsPerMeasure / 3) > 1);
    }

    public Pair<Note, List<Note>> getNoteSequenceFromNote(final Note originalNote) {
        final int entireDuration = noteDurationForDurationMillis(originalNote.durationMillis,
                                                                 originalNote.isRest);
        int tiedDurationRemaining = noteExtraTiedDurationForDuration(entireDuration);
        int noteDuration = entireDuration - tiedDurationRemaining;

        Note baseNote = originalNote.newCopy()
                                    .withDuration(noteDuration)
                                    .withType(noteStringForDuration(noteDuration))
                                    .withDotted(noteDottedForDuration(noteDuration))
                                    .withStartOfTie(originalNote.isStartOfTie
                                                            || (tiedDurationRemaining > 0
                                            && !originalNote.isRest))
                                    .build();

        List<Note> tiedNotes = new ArrayList<>();
        long timeStamp = originalNote.timeStamp;
        int newTiedDurationRemaining;

        while (tiedDurationRemaining > 0) {
            timeStamp += durationMillisForNoteDuration(noteDuration);
            newTiedDurationRemaining = noteExtraTiedDurationForDuration(tiedDurationRemaining);
            noteDuration = tiedDurationRemaining - newTiedDurationRemaining;
            tiedDurationRemaining = newTiedDurationRemaining;

            tiedNotes.add(originalNote.copyWithNewTimeStamp(timeStamp)
                                      .withDuration(noteDuration)
                                      .withType(noteStringForDuration(noteDuration))
                                      .withDotted(noteDottedForDuration(noteDuration))
                                      .withEndOfTie(!originalNote.isRest)
                                      .withStartOfTie(originalNote.isStartOfTie
                                                              || (tiedDurationRemaining > 0
                                              && !originalNote.isRest))
                                      .build());
        }

        return Pair.create(baseNote, tiedNotes);
    }

    private int noteDurationForDurationMillis(long durationMillis, boolean isRest) {
        return adjustDurationForPrecision(
                (durationMillis == 0L ? 0
                                      : Integers.max(isRest ? 0 : 1,
                                                     Math.round((float) (
                                                             (double) (durationMillis
                                                                     * DIVISIONS_PER_BEAT
                                                                     * actualBeatsTempo)
                                                                     / ONE_MINUTE))))
                        * MAX_BEAT_TYPE / beatType);
    }

    private int durationMillisForNoteDuration(int duration) {
        return adjustDurationForPrecision(duration) * ONE_MINUTE / actualBeatsTempo
                * beatType / MAX_BEAT_TYPE / DIVISIONS_PER_BEAT;
    }

    private String noteStringForDuration(int duration) {
        return noteString(adjustDurationForPrecision(duration));
    }

    private boolean noteDottedForDuration(int duration) {
        return noteDotted(adjustDurationForPrecision(duration));
    }

    private int noteExtraTiedDurationForDuration(int duration) {
        return noteExtraTiedDuration(adjustDurationForPrecision(duration));
    }

    private int adjustDurationForPrecision(int duration) {
        final int remainder = duration % precision;
        final int lower = duration - remainder;
        final int higher = lower + precision;

        return Math.abs(duration - lower) <= Math.abs(duration - higher) ? lower : higher;
    }

    private static String noteString(int duration) {
        if (duration < 1) {
            return "64th";
        } else if (duration == 1) {
            return "32nd";
        } else if (duration <= 3) {
            return "16th";
        } else if (duration <= 7) {
            return "eighth";
        } else if (duration <= 15) {
            return "quarter";
        } else if (duration <= 31) {
            return "half";
        } else {
            return "whole";
        }
    }

    private static boolean noteDotted(int duration) {
        return (duration == 3) || (duration == 6) || (duration == 7)
                || (duration >= 12 && duration <= 15) || (duration >= 24 && duration <= 31);
    }

    private static int noteExtraTiedDuration(int duration) {
        if (duration > 32) {
            return duration - 32;
        }

        switch (duration) {
            case 5:
            case 7:
            case 9:
            case 13:
            case 17:
            case 25:
                return 1;
            case 10:
            case 14:
            case 18:
            case 26:
                return 2;
            case 11:
            case 15:
            case 19:
            case 27:
                return 3;
            case 20:
            case 28:
                return 4;
            case 21:
            case 29:
                return 5;
            case 22:
            case 30:
                return 6;
            case 23:
            case 31:
                return 7;
            default:
                return 0;
        }
    }

}
