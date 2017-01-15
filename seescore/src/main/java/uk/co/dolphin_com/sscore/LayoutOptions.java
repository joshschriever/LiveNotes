/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * Define options for the score layout
 */
public class LayoutOptions {

	/**
	 * Define options for layout
	 * 
	 * @param hidePartNames if true no part names are to be displayed at left of score
	 * @param hideBarNumbers if true no bar numbers are to be displayed in the score
	 * @param simplifyHarmonyEnharmonicSpelling if true harmonies are displayed in the simplest way (eg F-sharp instead of E double-sharp) 
	 */
	public LayoutOptions(boolean hidePartNames,
						 boolean hideBarNumbers,
						 boolean simplifyHarmonyEnharmonicSpelling)
	{
		this.hidePartNames = hidePartNames;
		this.hideBarNumbers = hideBarNumbers;
		this.simplifyHarmonyEnharmonicSpelling = simplifyHarmonyEnharmonicSpelling;
		this.ignoreXmlPositions = false;
		this.useXMLxLayout = false;
	}

	/**
	 * Define options for layout
	 *
	 * @param hidePartNames if true no part names are to be displayed at left of score
	 * @param hideBarNumbers if true no bar numbers are to be displayed in the score
	 * @param simplifyHarmonyEnharmonicSpelling if true harmonies are displayed in the simplest way (eg F-sharp instead of E double-sharp)
	 * @param ignoreXmlPositions, if true ignore default-x,default-y,relative-x,relative-y positions
	 * @param useXMLxLayout, if true use any XML-defined x layout information (default-x in notes and measure widths) and heed system breaks
	 */
	public LayoutOptions(boolean hidePartNames,
						 boolean hideBarNumbers,
						 boolean simplifyHarmonyEnharmonicSpelling,
						 boolean ignoreXmlPositions,
						 boolean useXMLxLayout)
	{
		this.hidePartNames = hidePartNames;
		this.hideBarNumbers = hideBarNumbers;
		this.simplifyHarmonyEnharmonicSpelling = simplifyHarmonyEnharmonicSpelling;
		this.ignoreXmlPositions = ignoreXmlPositions;
		this.useXMLxLayout = useXMLxLayout;
	}

	/**
	 * construct with default options
	 */
	public LayoutOptions()
	{
		this.hidePartNames = false;
		this.hideBarNumbers = false;
		this.simplifyHarmonyEnharmonicSpelling = false;
		this.ignoreXmlPositions = false;
		this.useXMLxLayout = false;
	}

	final boolean hidePartNames;
	final boolean hideBarNumbers;
	final boolean simplifyHarmonyEnharmonicSpelling;
	final boolean ignoreXmlPositions;
	final boolean useXMLxLayout;

}
