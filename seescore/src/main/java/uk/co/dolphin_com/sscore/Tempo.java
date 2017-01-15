/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */
package uk.co.dolphin_com.sscore;

/**
 * the Tempo
 */
public class Tempo
{
	/**
	 * the beats-per-minute value for the given beat type
	 */
	public final int bpm;
	
	/**
	 * conventional beat type (crotchet = 4 etc) defined by note in metronome mark or always 4 for sound.tempo element
	 */
	public final int beatType;
	
	/**
	 * true if the metronome marking has a dot
	 */
	public final boolean dot;
	
	/**
	 * true if this is a beat type from a metronome mark and should be displayed, else it is a standard 4 for a sound tempo and is irrelevant to the beat
	 */
	public final boolean useBeatType;
	
	public String toString()
	{
		String rval = "Tempo:" + " bpm:" + bpm + " beatType:" + beatType;
		if (dot)
			rval += " dot";
		if (useBeatType)
			rval += " useBeatType";
		return rval;
	}

	private Tempo(int bpm, int bt, boolean d, boolean ubt){
		this.bpm = bpm;
		this.beatType = bt;
		this.dot = d;
		this.useBeatType = ubt;
	}
}