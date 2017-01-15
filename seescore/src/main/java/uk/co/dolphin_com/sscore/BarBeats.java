/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */
package uk.co.dolphin_com.sscore;

/**
 * Beats in a bar and ms timing of 1 beat.
 * <p>Returned from SScore.getBarBeats()
 */
public class BarBeats
{
	/**
	 * the number of beats in a particular bar
	 */
	public final int beatsInBar;
	
	/**
	 * the time of a single beat in ms
	 */
	public final int beatTime;
	
	public String toString()
	{
		return "BarBeats:" + " beatsInBar:" + beatsInBar + " beatTime:" + beatTime;
	}

	private BarBeats(int bib, int bt){
		this.beatsInBar = bib;
		this.beatTime = bt;
	}
}