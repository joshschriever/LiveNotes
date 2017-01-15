/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * a <direction> element in the score
 */
public class DirectionItem extends TimedItem
{
	/**
	 * types of direction
	 */
	public static final int Direction_rehearsal = 0;
	public static final int Direction_segno = 1;
	public static final int Direction_words = 2;
	public static final int Direction_coda = 3;
	public static final int Direction_wedge = 4;
	public static final int Direction_dynamics = 5;
	public static final int Direction_dashes = 6;
	public static final int Direction_bracket = 7;
	public static final int Direction_pedal = 8;
	public static final int Direction_metronome = 9;
	public static final int Direction_octave_shift = 10;
	public static final int Direction_harp_pedals = 11;
	public static final int Direction_damp = 12;
	public static final int Direction_damp_all = 13;
	public static final int Direction_eyeglasses = 14;
	public static final int Direction_string_mute = 15;
	public static final int Direction_scordatura = 16;
	public static final int Direction_image = 17;
	public static final int Direction_principal_voice = 18;
	public static final int Direction_accordion_registration = 19;
	public static final int Direction_percussion = 20;
	public static final int Direction_other = 21;

	/**
	 * Array of Direction_? values defined above
	 */
	public final int[] direction_types;
	
	/**
	 * a <sound> may be included in a <direction> element
	 * null if not defined
	 */
	public final Sound sound;
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		for (int d : direction_types)
		{
			switch(d)
			{
			case Direction_rehearsal:sb.append("rehearsal");break;
			case Direction_segno:sb.append("segno");break;
			case Direction_words:sb.append("words");break;
			case Direction_coda:sb.append("coda");break;
			case Direction_wedge:sb.append("wedge");break;
			case Direction_dynamics:sb.append("dynamics");break;
			case Direction_dashes:sb.append("dashes");break;
			case Direction_bracket:sb.append("bracket");break;
			case Direction_pedal:sb.append("pedal");break;
			case Direction_metronome:sb.append("metronome");break;
			case Direction_octave_shift:sb.append("octave_shift");break;
			case Direction_harp_pedals:sb.append("harp_pedals");break;
			case Direction_damp:sb.append("damp");break;
			case Direction_damp_all:sb.append("damp_all");break;
			case Direction_eyeglasses:sb.append("eyeglasses");break;
			case Direction_string_mute:sb.append("string_mute");break;
			case Direction_scordatura:sb.append("scordatura");break;
			case Direction_image:sb.append("image");break;
			case Direction_principal_voice:sb.append("principal_voice");break;
			case Direction_accordion_registration:sb.append("accordion_registration");break;
			case Direction_percussion:sb.append("percussion");break;
			case Direction_other :sb.append("other");break;
			}
			sb.append(",");
		}
		if (sound != null)
			sb.append(" sound:").append(sound);
		return sb.toString();
	}

	private DirectionItem(int staff, int item_h, int start, int duration, int[] direction_types, Sound sound) {
		super(ItemType_direction, staff, item_h, start, duration);
		this.direction_types = direction_types;
		this.sound = sound;
	}
}