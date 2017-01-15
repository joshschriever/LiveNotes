/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * Information about items in a bar of a particular part
 * returned from SScore.getBarContents()
 */
public class BarGroup
{
	/**
	 * index of part containing this group
	 */
	public final int partIndex;
	
	/**
	 *  index of bar containing this group
	 */
	public final int barIndex;
	
	/**
	 * items in bar
	 */
	public final Item[] items;
	
	/**
	 * divisions per quarter note (crotchet)
	 */
	public final int divisions;
	
	/**
	 * total divisions in bar
	 */
	public final int divisions_in_bar;
	

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("BarGroup: part:").append(partIndex);
		sb.append(" bar idx:").append(barIndex);
		sb.append(" items:");
		for (Item item : items)
		{
			sb.append(item).append(",");
		}
		sb.append(" div:").append(divisions);
		sb.append(" div in bar:").append(divisions_in_bar);
		return sb.toString();
	}

	private BarGroup(int p, int b, Item[] i, int d, int db){
		this.partIndex = p;
		this.barIndex = b;
		this.items = i;
		this.divisions = d;
		this.divisions_in_bar = db;
	}
}