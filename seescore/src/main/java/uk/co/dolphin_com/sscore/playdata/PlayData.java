/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore.playdata;

import uk.co.dolphin_com.sscore.SScore;
import uk.co.dolphin_com.sscore.ex.ScoreException;

/**
 * Access to the midi-style play information for the score.<p>
 * iterator returns a BarIterator which steps to each bar in the correct play sequence.
 * The Bar returns each Part in the bar and also a virtual metronome part.<p>
 * Part.iterator returns a NoteIterator which steps to each note in the bar in that part.
 * The note returns a midi pitch and start time and duration in ms.
 */
public class PlayData implements Iterable<Bar>
{
	public interface PlayControls
	{
		boolean getPartEnabled(int partIndex);
		boolean getPartStaffEnabled(int partIndex, int staffIndex);
		int getPartMIDIInstrument(int partIndex);
	}
	
	/**
	 * construct PlayData
	 * 
	 * @param score the score
	 * @param userTempo an implementation of the UserTempo interface allowing the user eg with a slider
	 * to define the tempo, or tempo scaling
	 * @throws ScoreException on error
	 */
	public PlayData(SScore score, UserTempo userTempo) throws ScoreException
	{
		this.score = score;
		this.numBars = score.numBars();
		this.userTempo = userTempo;
		this.nativePointer = getNativePointer(score, userTempo);
		this.loopStart = this.loopBack = -1;
		this.numRepeats = 0;
	}

	/**
	 * construct PlayData
	 *
	 * @param score the score
	 * @param userTempo an implementation of the UserTempo interface allowing the user eg with a slider
	 * to define the tempo, or tempo scaling
	 * @param loopStart the index of the first bar to play in each loop
	 * @param loopBack the index of the last bar to play in each loop
	 * @param numRepeats the number of times to repeat the loop
	 * @throws ScoreException on error
	 */
	public PlayData(SScore score, UserTempo userTempo, int loopStart, int loopBack, int numRepeats) throws ScoreException
	{
		this.score = score;
		this.numBars = score.numBars();
		this.userTempo = userTempo;
		this.nativePointer = getNativePointer(score, userTempo);
		this.loopStart = loopStart;
		this.loopBack = loopBack;
		this.numRepeats = numRepeats;
	}

	/**
	 * get an iterator to the set of bars in the score.
	 * The iterator will start at the first bar and sequence through all
	 * bars in playing order accounting for repeats, DC.DS etc
	 * 
	 * @return the iterator
	 */
	public native BarIterator iterator();

	/**
	 * get the number of playing parts in the score
	 * 
	 * @return the number of playing parts
	 */
	public native int numParts();

    /**
     * the number of bars in the score;
     */
    public final int numBars;

	/**
	 * get the maximum value of any sound dynamic in any bar. This allows note dynamic values to be scaled accordingly
	 * 
	 * @return the maximum dynamic
	 */
	public native float maxSoundDynamic();
	
	/**
	 * is the first bar an 'up-beat' or anacrusis partial bar?
	 * 
	 * @return true if the first bar is missing the first beat (anacrusis)
	 */
	public native boolean firstBarAnacrusis();
	
	/**
	 * generate a MIDI file from the play data
	 *
	 * @param midiFilePath the full pathname of the MIDI file to create
	 * @return false if failed
	 */
	public native boolean createMIDIFile(String midiFilePath);
	
	/**
	 * generate a MIDI file from the play data with control of enabled parts
	 *
	 * @param midiFilePath the full pathname of the MIDI file to create
	 * @param controls define which parts should be output to the file
	 * @return false if failed
	 */
	public native boolean createMIDIFileWithControls(String midiFilePath, PlayControls controls);
	
	/**
	 * scale the tempo in the MIDI file by writing tempo-defining bytes into it
	 * NB This assumes that the file was written by createMIDIFile and by the same version of the SeeScoreLib
     *
	 * @param midiFilePath the full pathname of the MIDI file
	 * @param tempoScaling the scaling (1.0 is unscaled)
	 */
	public static native void scaleMIDIFileTempo(String midiFilePath, float tempoScaling);

	protected native void finalize();
	private static native long getNativePointer(SScore score, UserTempo userTempo);
	private final SScore score;
	private final UserTempo userTempo;
	private final int loopStart;
	private final int loopBack;
	private final int numRepeats;
	private final long nativePointer;
}