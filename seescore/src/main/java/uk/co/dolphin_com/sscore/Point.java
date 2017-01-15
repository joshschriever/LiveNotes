/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * Define an (x,y) coordinate
 */
public class Point {

	/**
	 * Construct coordinate
	 *
	 * @param x the x value
	 * @param y the y value
	 */
	public Point(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * The x value
	 */
	public final float x;
	
	/**
	 * The y value
	 */
	public final float y;
}
