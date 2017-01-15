/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */
package uk.co.dolphin_com.sscore;

/**
 * the Time Signature
 */
public class TimeSig
{
	/**
	 * number of beats in bar
	 */
	public final int numBeats;
	
	/**
	 * conventional beat type ie 4 is crotchet etc
	 */
	public final int beatType;

	public String toString()
	{
		return "TimeSig:" + " numBeats:" + numBeats + " beatType:" + beatType;
	}

	private TimeSig(int numbeats, int beattype) {
		this.numBeats = numbeats;
		this.beatType = beattype;
	}
}