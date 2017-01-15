/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;


/**
 * Define the full/abbreviated name for a part
 */
public class PartName
{
	/**
	 * The full name for the part (eg "Violin")
	 */
	public final String name;
	
	/**
	 * The abbreviated name for the part (eg "Vln")
	 */
	public final String abbrev;
	
	public String toString()
	{
		return " " + name + " (" + abbrev + ")";
	}
	
	PartName(String n, String a)
	{
		name = n;
		abbrev = a;
	}
}