/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

import android.graphics.RectF;

/**
 * a bar cursor rectangle
 */
public class CursorRect {

	/**
	 * true if the bar is in the System
	 */
	public final boolean barInSystem;
	
	/**
	 * the rectangle outline of the bar
	 */
	public final RectF rect;

	private CursorRect(boolean barInSystem, float xorigin, float yorigin, float width, float height){
		this.barInSystem = barInSystem;
		this.rect = new RectF(xorigin, yorigin, xorigin + width, yorigin + height);
	}
}