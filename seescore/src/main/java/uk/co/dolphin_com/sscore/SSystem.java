/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

import android.graphics.RectF;
import uk.co.dolphin_com.sscore.Point;
import uk.co.dolphin_com.sscore.ex.ScoreException;
import uk.co.dolphin_com.sscore.StaffLayout;

/**
 * A System is a range of bars able to draw itself in a Canvas, and is a product of calling SScore.layout()
 * <p>
 * draw() and drawWithOptions() draw the system into a canvas, the latter allowing item colouring (and requiring an additional licence)
 * <p>
 * getPartIndexForYPos(), getBarIndexForXPos() can be used to locate the bar and part under the cursor/finger
 * <p>
 * hitTest() is used to find the exact layout components (eg notehead, stem, beam) at a particular location (requiring a contents licence)
 * <p>
 * getComponentsForItem() is used to find all the layout components of a particular score item (requiring a contents licence)
 * 
 * @author J.Sutton Dolphin Computing
 */
public class SSystem {

	/**
	 * Get the index of this system from the top of the score.
	 * <p>Index 0 is the topmost.
	 * 
	 * @return the index of this system
	 */
	public native int index();

	/**
	 * define a range of bars which the system includes
	 */
	public static class BarRange {
		
		public final int startBarIndex;
		public final int numBars;

        public boolean containsBar(int barIndex) {
            return barIndex >= startBarIndex && barIndex < startBarIndex + numBars;
        }

        private BarRange(int s, int n) {
			startBarIndex = s;
			numBars = n;
		}
	}

	/**
	 * Get the start bar index and number of bars for this system.
	 * 
	 * @return the start bar and number of bars
	 */
	public native BarRange getBarRange();

    public boolean containsBar(int barIndex)
    {
        return getBarRange().containsBar(barIndex);

    }

	/**
	 * Get the bounding box of this system.
	 * 
	 * @return the bounds
	 */
	public native Size bounds();

	/**
	 * Draw this system at the given point.
	 * 
	 * @param canvas the Canvas
	 * @param am the AssetManager for loading fonts from assets
	 * @param tl the top left point to draw this system in the Canvas
	 * @param magnification the scale to draw at. NB This is normally 1, except during active zooming.
	 * The overall magnification is set in sscore_layout
	 */
	public native void draw(android.graphics.Canvas canvas,
			android.content.res.AssetManager am,
			Point tl,
			float magnification);
	
	/**
	 * Draw the system allowing optional colouring of particular items/components in the layout
	 * 
	 * @param canvas the Canvas
	 * @param am the AssetManager for loading fonts from assets
	 * @param tl the top left point to draw this system in th Canvas
	 * @param magnification the scale to draw at. NB This is normally 1, except during active zooming.
	 * The overall magnification is set in sscore_layout
	 * @param renderItems each RenderItem object in the array defines special colouring of a particular score item
	 */
	public native void drawWithOptions(android.graphics.Canvas canvas,
			android.content.res.AssetManager am,
			Point tl,
			float magnification,
			RenderItem[] renderItems) throws ScoreException;

	/**
	 * Get the cursor rectangle for a particular system and bar
	 * 
	 * @param canvas a canvas for measurement
	 * @param barIndex the index of the bar in the system
	 * @return the cursor info
	 */
	public native CursorRect getCursorRect(android.graphics.Canvas canvas, int barIndex);

	/**
	 * get the part index of the part enclosing the given y coordinate in this system
	 * 
	 * @param ypos the y coord
	 * @return the part index
	 */
	public native int getPartIndexForYPos(float ypos);

	/**
	 * get the bar index of the bar enclosing the given x coordinate in this system
	 * 
	 * @param xpos the x coord
	 * @return the bar index
	 */
	public native int getBarIndexForXPos(float xpos);

	/**
	 * get the default vertical spacing to the next system
	 * 
	 * @return the default spacing
	 */
	public native float getDefaultSpacing();
	
	/**
	 get staff measurement for the part in this system
	 
	 @param partIndex the 0-based part index
	 @return staff measurements
	 */
	public native StaffLayout getStaffLayout(int partIndex);
	
	/**
	 get barline measurements for the part in this system
	 
	 @param partIndex the 0-based part index
	 @return barline dimensions
	 */
	public native BarLayout getBarLayout(int partIndex);

	/**
	 * Get an array of components which intersect a given a point in this system
	 * <p>contents licence is required
	 * 
	 * @param p the point
	 * @return array of intersecting Component
	 */
	public native Component[] hitTest(Point p) throws ScoreException;

	/**
	 * Get an array of layout components which belong to a particular score item in this system
	 * <p>contents licence is required
	 * 
	 * @param item_h the unique identifier for an item (eg note) in the score
	 * @return array of Component
	 */
	public native Component[] getComponentsForItem(int item_h) throws ScoreException;

	/**
	 * Get a bounding box which encloses all layout components for a score item in this system
	 * <p>contents licence is required
	 *
	 * @param item_h
	 * @return the bounds of (all components of) the item
	 */
	public native RectF getBoundsForItem(int item_h) throws ScoreException;;

	private SSystem(long nativePointer, long nativeSc, int dpi)
	{
		this.nativePointer = nativePointer;
		this.nativeSc = nativeSc;
		this.dpi = dpi;
	}
	private final long nativePointer;
	private final long nativeSc;
	private final int dpi;
}
