/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * a harmony item in the score
 */
public class HarmonyItem extends TimedItem
{
	/**
	 * the harmony
	 */
	public final Harmony harmony;
	
	public String toString()
	{
		return super.toString() + " " + harmony.toString();
	}

	private HarmonyItem(int staff, int item_h, int start, int duration, Harmony harmony) {
		super(ItemType_harmony, staff, item_h, start, duration);
		this.harmony = harmony;
	}
}