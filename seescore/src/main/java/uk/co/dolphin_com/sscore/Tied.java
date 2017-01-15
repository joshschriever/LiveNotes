/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * Define parameters for a tie in the layout
 */
public class Tied
{
	/**
	 * types of start/stop which specify where slurs/ties/tuplets etc start and end
	 */
	public static final int StartStop_undef = 0;
	public static final int StartStop_start = 1;
	public static final int StartStop_stop = 2;
	public static final int StartStop_unknown = 3;
	
	/**
	 * placement
	 */
	public static final int Placement_undef = 0;
	public static final int Placement_above = 1;
	public static final int Placement_below = 2;
	public static final int Placement_unknown = 3;

	/**
	 * orientation
	 */
	public static final int Orientation_undef = 0;
	public static final int Orientation_over = 1;
	public static final int Orientation_under = 2;
	public static final int Orientation_unknown = 3;

	/**
	 * Defines whether this is the left, middle or right of a tie - StartStop_? 
	 */
	public final int startstop;
	
	/**
	 * Defines whether this tie is placed above or below the corresponding note - Placement_? 
	 */
	public final int placement;
	
	/**
	 * Defines whether this tie rises or falls in the centre - Orientation_? 
	 */
	public final int orientation;
	
	public String toString()
	{
		if (startstop != StartStop_undef)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("tied:");
			switch(startstop)
			{
			default:
			case StartStop_undef: sb.append("undef");break;
			case StartStop_start: sb.append("start");break;
			case StartStop_stop: sb.append("stop");break;
			case StartStop_unknown: sb.append("unknown");break;
			}
			if (placement != Placement_undef)
			{
				sb.append(" placement:");
				switch(placement)
				{
				case Placement_above: sb.append("above");break;
				case Placement_below: sb.append("below");break;
				default:
				case Placement_unknown: sb.append("unknown");break;
				}
			}
			if (orientation != Orientation_undef)
			{
				sb.append(" orientation:");
				switch(orientation)
				{
				case Orientation_over: sb.append("over");break;
				case Orientation_under: sb.append("under");break;
				default:
				case Orientation_unknown: sb.append("unknown");break;
				}
			}
			return sb.toString();
		}
		else
			return "";
	}

	private Tied(int startstop, int placement, int orientation) {
		this.startstop = startstop;
		this.placement = placement;
		this.orientation = orientation;
	}
}