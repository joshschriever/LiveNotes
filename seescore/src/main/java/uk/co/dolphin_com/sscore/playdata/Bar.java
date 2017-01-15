package uk.co.dolphin_com.sscore.playdata;

/**
 * a Bar in the PlayData
 */
public class Bar {

    private static final int kMetronomePartIndex = -1;

    /**
     * the index of the bar
     */
    public final int index;

    /**
     * the duration of this bar accounting for time signature and tempo in force
     */
    public final int duration;

    /**
     * true if this is a count-in bar
     */
    public final boolean countIn;

    /**
     * the index of this bar in the total sequence allowing for any repeats
     * Corresponds to the index where there are no repeats before this bar
     */
    public final int sequenceIndex;

    /**
     * create a count-in bar copy of this bar with the count-in flag set and with a maximum of 4 beats
     * @return count-in bar
     */
    public native Bar createCountIn();

    /**
     * get a Part in the score
     *
     * @param partIndex the part index - 0 is the top part in the score
     * @return a Part which iterates all the notes in this bar for the part
     */
    public Part part(int partIndex)
    {
        return new Part(nativePointer, sequenceIndex, countIn, partIndex);
    }

    /**
     * get an artificial Part for a metronome in this bar
     *
     * @return a Part which iterates artificial metronome notes - 1 per beat in the bar
     */
    public Part metronome()
    {
        return new Part(nativePointer, sequenceIndex, countIn, kMetronomePartIndex);
    }

    public String toString()
    {
        return " bar:" + Integer.toString(index) + " duration:" + Integer.toString(duration) + "ms";
    }

    Bar(long nativePointer, int idx,  int dur, int seqi, boolean ci) {
        this.nativePointer = nativePointer;
        index = idx;
        duration = dur;
        sequenceIndex = seqi;
        countIn = ci;
    }
    private final long nativePointer;
}
