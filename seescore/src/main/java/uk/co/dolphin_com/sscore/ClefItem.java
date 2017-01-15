/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * a clef in the score
 */
public class ClefItem extends TimedItem
{
	/**
	 * treble clef (G clef) type constant
	 */
	public static final int Clef_treble = 0;
	/**
	 * treble clef (G clef) with 8 below
	 */
	public static final int Clef_treble_sub8 = 1;
	/**
	 * alto clef (C clef centred on middle line)
	 */
	public static final int Clef_alto = 2;
	/**
	 * tenor clef (C clef)
	 */
	public static final int Clef_tenor = 3;
	/**
	 * tenor clef (C clef)
	 */
	public static final int Clef_soprano = 4;
	/**
	 * bass clef (F clef)
	 */
	public static final int Clef_bass = 5;
	/**
	 * percussion clef
	 */
	public static final int Clef_percussion = 6;
	/**
	 * TAB clef (stave lines indicate guitar strings, numbers indicate fingers)
	 */
	public static final int Clef_TAB = 7;
	/**
	 * no clef
	 */
	public static final int Clef_none = 8;
	/**
	 * unknown clef
	 */
	public static final int Clef_unknown = 9;
	/**
	 * treble clef with 8 above
	 */
	public static final int Clef_treble_super8 = 10;
	/**
	 * bass clef with 8 below
	 */
	public static final int Clef_bass_sub8 = 11;
	/**
	 * bass clef with 8 above (unknown?)
	 */
	public static final int Clef_bass_super8 = 12;

	/**
	 * type of clef - Clef_?
	 */
	public final int clefType;
	
	public String toString()
	{ 
		switch (clefType)
		{
			case Clef_treble: 		return " treble " + super.toString();
			case Clef_treble_sub8: 	return  " treble -8 " + super.toString();
			case Clef_alto: 		return  " alto " + super.toString();
			case Clef_tenor:		return  " tenor " + super.toString();
			case Clef_soprano:		return  " soprano " + super.toString();
			case Clef_bass: 		return  " bass " + super.toString();
			case Clef_percussion: 	return  " percussion " + super.toString();
			case Clef_TAB: 			return  " TAB " + super.toString();
			case Clef_none: 		return  " none " + super.toString();
			default:
			case Clef_unknown: 		return " unknown " + super.toString();
			case Clef_treble_super8: return " treble +8 " + super.toString();
			case Clef_bass_sub8: 	return " bass -8 " + super.toString();
			case Clef_bass_super8: 	return " bass +8 " + super.toString();
		}
	}
	
	private ClefItem(int staff, int item_h, int start, int duration, int clefType) {
		super(ItemType_clef, staff, item_h, start, duration);
		this.clefType = clefType;
	}
}