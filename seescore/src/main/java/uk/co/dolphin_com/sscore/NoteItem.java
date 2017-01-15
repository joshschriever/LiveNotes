/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * a note or rest in the score
 */
public class NoteItem extends TimedItem
{
	/**
	 * define types of notation
	 */
	public static final int NotationsType_unset = 0;
	public static final int NotationsType_tied = 1;
	public static final int NotationsType_slur = 2;
	public static final int NotationsType_tuplet = 3;
	public static final int NotationsType_glissando = 4;
	public static final int NotationsType_slide = 5;
	public static final int NotationsType_ornaments = 6;
	public static final int NotationsType_technical = 7;
	public static final int NotationsType_articulations = 8;
	public static final int NotationsType_dynamics = 9;
	public static final int NotationsType_fermata = 10;
	public static final int NotationsType_arpeggiate = 11;
	public static final int NotationsType_non_arpeggiate = 12;
	public static final int NotationsType_accidental_mark = 13;
	public static final int NotationsType_other = 14;
	public static final int NotationsType_unknown = 15;

	/**
	 * The MIDI pitch of this note ie 60 = C4; 0 => rest
	 */
	public final int midipitch;
	
	/**
	 * The value of the note 2 = minim, 4 = crochet etc.
	 */
	public final int noteType;
	
	/**
	 * number of dots - 1 if dotted, 2 if double-dotted
	 */
	public final int numdots;
	
	/**
	 * any accidentals defined ie +1 = 1 sharp, -1 = 1 flat etc.
	 */
	public final int accidentals;
	
	/**
	 * True if this is a chord note (not set for first note of chord)
	 */
	public final boolean ischord;
	
	/**
	 * Array of notations - NotationsType_tied,NotationsType_slur.. etc
	 */
	public final int[] notations;
	
	/**
	 * If this is a tied note this contains information about the tie - else null
	 */
	public final Tied tied;
	
	/**
	 * true if this is a grace note
	 */
	public final boolean grace;

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		if (midipitch > 0) // else rest
			sb.append(" pitch:").append(midipitch);
		sb.append(" value:").append(noteType);
		if (numdots == 1)
			sb.append(" dot");
		else if (numdots > 1)
			sb.append(" ").append(numdots).append(" dots");
		if (accidentals > 0)
			sb.append(" ").append(accidentals).append(" sharps");
		else if (accidentals < 0)
			sb.append(" ").append(-accidentals).append(" flats");
		if (ischord)
			sb.append(" (chord)");
		if (notations.length > 0)
		{
			sb.append(" {");
			int i = 0;
			for (int n : notations)
			{
				if (i > 0)
					sb.append(", ");
				switch(n)
				{
				case NotationsType_unset:sb.append("unset");break;
				case NotationsType_tied:sb.append("tied");break;
				case NotationsType_slur:sb.append("slur");break;
				case NotationsType_tuplet:sb.append("tuplet");break;
				case NotationsType_glissando:sb.append("glissando");break;
				case NotationsType_slide:sb.append("slide");break;
				case NotationsType_ornaments:sb.append("ornaments");break;
				case NotationsType_technical:sb.append("technical");break;
				case NotationsType_articulations:sb.append("articulations");break;
				case NotationsType_dynamics:sb.append("dynamics");break;
				case NotationsType_fermata:sb.append("fermata");break;
				case NotationsType_arpeggiate:sb.append("arpeggiate");break;
				case NotationsType_non_arpeggiate:sb.append("non_arpeggiate");break;
				case NotationsType_accidental_mark:sb.append("accidental_mark");break;
				case NotationsType_other:sb.append("other");break;
				default:
				case NotationsType_unknown:sb.append("unknown");break;
				}
				++i;
			}
			sb.append("}");
		}
		if (tied != null)
			sb.append(" ").append(tied);
		if (grace)
			sb.append(" grace");
		return sb.toString();
	}

	private NoteItem(int staff, int item_h, int start, int duration,
		 	int itemtype, int midipitch, int noteType, int numdots, int accidentals,
		 	boolean ischord, int[] notations, Tied tied, boolean grace) {
	super(itemtype, staff, item_h, start, duration);
	this.midipitch = midipitch;
	this.noteType = noteType;
	this.numdots = numdots;
	this.accidentals = accidentals;
	this.ischord = ischord;
	this.notations = notations;
	this.tied = tied;
	this.grace = grace;
	}
}