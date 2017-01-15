/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * Information about a key signature in the score
 */
public class KeySigItem extends TimedItem
{
	/**
	 * The MusicXML fifths value for the key signature
	 * 0 is no sharps or flats, +1 is 1 sharp, -1 is 1 flat etc.
	 */
	public final int fifths;
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		if (fifths > 0)
			sb.append(" ").append(fifths).append(" sharps");
		else if (fifths < 0)
			sb.append(" ").append(-fifths).append(" flats");
		else
			sb.append(" blank");
		return sb.toString();
	}

	private KeySigItem(int staff, int item_h, int start, int duration, int fifths) {
		super(ItemType_keysig, staff, item_h, start, duration);
		this.fifths = fifths;
	}
}