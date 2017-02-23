package com.joshschriever.livenotes.musicxml;

public class Note {

    private Note(long timeStamp,
                 byte value,
                 int duration,
                 boolean isRest,
                 boolean isEndOfTie,
                 boolean isStartOfTie,
                 String type,
                 boolean isDotted) {
        this.timeStamp = timeStamp;
        this.value = value;
        this.duration = duration;
        this.isRest = isRest;
        this.isEndOfTie = isEndOfTie;
        this.isStartOfTie = isStartOfTie;
        this.type = type;
        this.isDotted = isDotted;
    }

    public final long timeStamp;
    public final byte value;
    public final int duration;
    public final boolean isRest;
    public final boolean isEndOfTie;
    public final boolean isStartOfTie;
    public final String type;
    public final boolean isDotted;

    public static Builder newNote(long timeStamp) {
        return new Builder(timeStamp, false);
    }

    public static Builder newRest(long timeStamp) {
        return new Builder(timeStamp, true);
    }

    public static class Builder {

        private long timeStamp = 0L;
        private byte value = 48;
        private int duration = 0;
        private boolean isRest = false;
        private boolean endOfTie = false;
        private boolean startOfTie = false;
        private String type = "64th";
        private boolean dotted = false;

        private Builder(long timeStamp, boolean isRest) {
            this.timeStamp = timeStamp;
            this.isRest = isRest;
        }

        public Builder withValue(byte value) {
            this.value = value;
            return this;
        }

        public Builder withDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder withEndOfTie() {
            endOfTie = true;
            return this;
        }

        public Builder withStartOfTie() {
            startOfTie = true;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withDotted() {
            dotted = true;
            return this;
        }

        public Note build() {
            return new Note(timeStamp, value, duration, isRest, endOfTie, startOfTie, type, dotted);
        }
    }
}
