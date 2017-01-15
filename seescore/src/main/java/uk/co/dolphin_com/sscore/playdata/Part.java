package uk.co.dolphin_com.sscore.playdata;

/**
 * Part returns a NoteIterator for a single Bar
 */
public class Part implements Iterable<Note>
{
    /**
     * @return an iterator to the notes in the bar for this part
     */
    public native NoteIterator iterator();

    public String toString()
    {
        return " part:" + Integer.toString(partIndex);
    }

    Part(long nativePointer, int idx, boolean ci, int pindex) {
        assert(idx >= 0);
        this.partIndex = pindex;
        this.nativePointer = nativePointer;
        this.idx = idx;
        this.ci = ci;
    }
    private final int partIndex;
    private final long nativePointer;
    private final int idx;
    private final boolean ci;
}
