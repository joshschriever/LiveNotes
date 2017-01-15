/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 *  define a 2d size
 */
public class Size {

	/**
	 * construct a size
	 * 
	 * @param width the width
	 * @param height the height
	 */
	public Size(float width, float height)
	{
		this.width = width;
		this.height = height;
	}

	/**
	 * the width
	 */
	public final float width;
	
	/**
	 * the height
	 */
	public final float height;

}
