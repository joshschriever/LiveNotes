package com.joshschriever.livenotes.musicxml;

public class Note {

    private Note(long timeStamp,
                 long durationMillis,
                 int value,
                 int duration,
                 boolean isRest,
                 boolean isEndOfTie,
                 boolean isStartOfTie,
                 String type,
                 boolean isDotted) {
        this.timeStamp = timeStamp;
        this.durationMillis = durationMillis;
        this.value = value;
        this.duration = duration;
        this.isRest = isRest;
        this.isEndOfTie = isEndOfTie;
        this.isStartOfTie = isStartOfTie;
        this.type = type;
        this.isDotted = isDotted;
    }

    public final long timeStamp;
    public final long durationMillis;
    public final int value;
    public final int duration;
    public final boolean isRest;
    public final boolean isEndOfTie;
    public final boolean isStartOfTie;
    public final String type;
    public final boolean isDotted;

    public Builder newCopy() {
        return new Builder(timeStamp, durationMillis, value, isRest)
                .withDuration(duration)
                .withEndOfTie(isEndOfTie)
                .withStartOfTie(isStartOfTie)
                .withType(type)
                .withDotted(isDotted);
    }

    public static Builder newNote(long timeStamp, long durationMillis, int value) {
        return new Builder(timeStamp, durationMillis, value, false);
    }

    public static Builder newRest(long timeStamp, long durationMillis, boolean trebleClef) {
        return new Builder(timeStamp, durationMillis, (trebleClef ? 50 : 45), true);
    }

    public static class Builder {

        private long timeStamp = 0L;
        private long durationMillis = 0L;
        private int value = 48;
        private int duration = 0;
        private boolean isRest = false;
        private boolean isEndOfTie = false;
        private boolean isStartOfTie = false;
        private String type = "64th";
        private boolean isDotted = false;

        private Builder(long timeStamp, long durationMillis, int value, boolean isRest) {
            this.timeStamp = timeStamp;
            this.durationMillis = durationMillis;
            this.value = value;
            this.isRest = isRest;
        }

        public Builder withDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder withEndOfTie(boolean isEndOfTie) {
            this.isEndOfTie = isEndOfTie;
            return this;
        }

        public Builder withStartOfTie(boolean isStartOfTie) {
            this.isStartOfTie = isStartOfTie;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withDotted(boolean isDotted) {
            this.isDotted = isDotted;
            return this;
        }

        public Note build() {
            return new Note(timeStamp,
                            durationMillis,
                            value,
                            duration,
                            isRest,
                            isEndOfTie,
                            isStartOfTie,
                            type,
                            isDotted);
        }
    }
}
