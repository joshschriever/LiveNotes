/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * Information about a time signature in the score
 */
public class TimeSigItem extends TimedItem
{
	/**
	 * Number of beats in bar
	 */
	public final int numBeats;
	
	/**
	 * Type of beat - 4 is crotchet etc
	 */
	public final int beatType;

	public String toString()
	{
		return super.toString() + " numBeats:" + numBeats + " beatType:" + beatType;
	}
	
	private TimeSigItem(int staff, int item_h, int start, int duration, int numbeats, int beattype) {
		super(ItemType_timesig, staff, item_h, start, duration);
		this.numBeats = numbeats;
		this.beatType = beattype;
	}
}