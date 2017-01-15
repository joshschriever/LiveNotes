/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * a Sound item in the score
 */
public class SoundItem extends TimedItem
{
	public final Sound sound;

	public String toString()
	{
		return super.toString() + sound.toString();
	}
	
	private SoundItem(int staff, int item_h, int start, int duration, Sound sound) {
		super(ItemType_sound, staff, item_h, start, duration);
		this.sound = sound;
	}
}