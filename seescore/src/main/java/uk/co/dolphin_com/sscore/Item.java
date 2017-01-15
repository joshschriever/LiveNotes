/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * Define an item in the score
 */
public class Item
{
	/**
	 * the type of an item in the score used in class Item
	 */
	public static final int ItemType_noitem = 0;
	public static final int ItemType_note = 1;
	public static final int ItemType_rest = 2;
	public static final int ItemType_direction = 3;
	public static final int ItemType_timesig = 4;
	public static final int ItemType_keysig = 5;
	public static final int ItemType_clef = 6;
	public static final int ItemType_harmony = 7;
	public static final int ItemType_sound = 8;
	public static final int ItemType_unknown = 9;
	public static final int ItemType_barline = 10;
	public static final int ItemType_chord = 11;

	/**
	 * The type of this item - ItemType_?
	 */
	public final int type;
	
	/**
	 * The index of the staff containing this item [0..]. 0 is the top staff
	 */
	public final int staff; 
	
	/**
	 * a unique identifier for this item in the score
	 */
	public final int item_h;
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		switch(type)
		{
		case ItemType_noitem:sb.append("no_item");break;
		case ItemType_note:sb.append("note");break;
		case ItemType_rest:sb.append("rest");break;
		case ItemType_direction:sb.append("direction");break;
		case ItemType_timesig:sb.append("timesig");break;
		case ItemType_keysig:sb.append("keysig");break;
		case ItemType_clef:sb.append("clef");break;
		case ItemType_harmony:sb.append("harmony");break;
		case ItemType_sound:sb.append("sound");break;
		default:
		case ItemType_unknown:sb.append("unknown");break;
		case ItemType_barline:sb.append("barline");break;
		}
		sb.append(" staff:").append(staff);
		return sb.toString();
	}
	
	Item(int type, int staff, int item_h) {
		this.type = type;
		this.staff = staff;
		this.item_h = item_h;
	}
}