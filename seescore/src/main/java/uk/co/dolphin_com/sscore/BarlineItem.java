/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * MusicXML <barline> in the score
 */
public class BarlineItem extends TimedItem
{
	/**
	 * define style of barline
	 */
	public static final int Barline_unset = 0;
	public static final int Barline_regular = 1;
	public static final int Barline_heavy = 2;
	public static final int Barline_double = 3;
	public static final int Barline_none = 4;
	
	/**
	 * define location of special barline in the bar
	 */
	public static final int Location_unset = 0;
	public static final int Location_right = 1;
	public static final int Location_left = 2;
	public static final int Location_middle = 3;
	
	/**
	 * define the repeat ending type (ie numbered ending which can encompass multiple bars with brace above)
	 */
	public static final int Ending_start = 0;
	public static final int Ending_stop = 1;
	public static final int Ending_undefined = 2;
	
	/**
	 * define the repeat type - dots to left of barline
	 */
	public static final int Repeat_backward = 0;
	
	/**
	 * define the repeat type - dots to right of barline
	 */
	public static final int Repeat_forward = 1;
	
	/**
	 * no repeat
	 */
	public static final int Repeat_undefined = 2;
	
	/**
	 * Barline style Barline_?
	 */
	public final int barline_style;
	
	/**
	 * Location_?
	 */
	public final int location;
	
	/**
	 * true if fermata at barline
	 */
	public final boolean fermata;
	
	/**
	 * eg "1", "2" or "1,2" or NULL
	 */
	public final String ending_numbers;
	
	/**
	 * Ending_?
	 */
	public final int ending_type;
	
	/**
	 * Repeat_?
	 */
	public final int repeat_type;
	
	/**
	 * number of repeats at this barline
	 * 0 if undefined
	 */
	public final int repeat_times;
	
	/**
	 * true if Segno at barline
	 */
	public final boolean segno;
	
	/**
	 * true if Coda at barline
	 */
	public final boolean coda;

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		if (barline_style != Barline_unset)
		{
			sb.append(" style:");
			switch (barline_style)
			{
			case Barline_regular:sb.append("regular");break;
			case Barline_heavy:sb.append("heavy");break;
			case Barline_double:sb.append("double");break;
			case Barline_none:sb.append("none");break;
			default:sb.append("?");break;
			}
		}
		if (location != Location_unset)
		{
			sb.append(" location:");
			switch(location)
			{
			case Location_right:sb.append("right");break;
			case Location_left:sb.append("left");break;
			case Location_middle:sb.append("middle");break;
			default:sb.append("?");break;
			}
		}
		if (fermata)
			sb.append(" fermata");
		if (ending_numbers != null && ending_numbers.length() > 0)
			sb.append(" ending numbers:").append(ending_numbers);
		if (ending_type != Ending_undefined)
		{
			sb.append(" ending:");
			switch(ending_type)
			{
			case Ending_start:sb.append("start");break;
			case Ending_stop:sb.append("stop");break;
			default:sb.append("?");break;
			}
		}
		if (repeat_type != Repeat_undefined)
		{
			sb.append(" repeat:");
			switch(repeat_type)
			{
			case Repeat_backward:sb.append("backward");break;
			case Repeat_forward:sb.append("forward");break;
			default:sb.append("?");break;
			}
		}
		if (repeat_times > 0)
		{
			sb.append(" repeat times:").append(repeat_times);
		}
		if (segno)
			sb.append(" segno");
		if (coda)
			sb.append(" coda");
		return sb.toString();
	}
	
	private BarlineItem(int staff, int item_h, int start, int duration,
			int barline,int loc, boolean ferm, String endnum, int ending, int reptype, int reptimes, boolean segno, boolean coda) {
		super(ItemType_barline, staff, item_h, start, duration);
		this.barline_style = barline;
		this.location = loc;
		this.fermata = ferm;
		this.ending_numbers = endnum;
		this.ending_type = ending;
		this.repeat_type = reptype;
		this.repeat_times = reptimes;
		this.segno = segno;
		this.coda = coda;
	}
}