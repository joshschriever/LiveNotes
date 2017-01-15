/**
 * SeeScore For Android Sample App
 * Dolphin Computing http://www.dolphin-com.co.uk
 */
package uk.co.dolphin_com.seescoreandroid;

import uk.co.dolphin_com.sscore.BarLayout;
import uk.co.dolphin_com.sscore.Component;
import uk.co.dolphin_com.sscore.CursorRect;
import uk.co.dolphin_com.sscore.Point;
import uk.co.dolphin_com.sscore.RenderItem;
import uk.co.dolphin_com.sscore.RenderItem.Colour;
import uk.co.dolphin_com.sscore.SScore;
import uk.co.dolphin_com.sscore.SSystem;
import uk.co.dolphin_com.sscore.StaffLayout;
import uk.co.dolphin_com.sscore.ex.ScoreException;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The SystemView is a {@link View} which displays a single {@link SSystem}.
 * <p>{@link SeeScoreView} manages layout and placement of these into a scrolling View to display the complete {@link SScore}
 */
public class SystemView extends View {

    /**
     * type of cursor
     */
    static enum CursorType
    {
        /** no cursor */
        none,

        /** vertical line cursor */
        line,

        /** rectangular cursor around bar */
        box
    }

	/**
	 * construct the SystemView
	 * 
	 * @param context the Context
	 * @param score the score
	 * @param sys the system
	 * @param am the AssetManager for fonts
	 */
	public SystemView(Context context, SScore score, SSystem sys, AssetManager am, SeeScoreView.TapNotification tn)
	{
		super(context);
		this.assetManager = am;
		this.system = sys;
		this.score = score;
        this.tapNotify = tn;
		leftPlayLoopBarIndex = rightPlayLoopBarIndex = -1;
		backgroundPaint = new Paint();
		backgroundPaint.setStyle(Paint.Style.FILL);
		backgroundPaint.setColor(0xFFFFFFFA);
		backgroundPaintRect = new RectF();
		tappedItemPaint = new Paint();
		tappedItemPaint.setStyle(Paint.Style.STROKE);
		tappedItemPaint.setColor(0xFF000080); // red
		barRectPaint = new Paint();
		barRectPaint.setStyle(Paint.Style.STROKE);
		barRectPaint.setStrokeWidth(3);
		barRectPaint.setColor(0xFF0000FF); // blue
		this.tl = new Point(0,0);// topleft
		zoomingMag = 1;
		viewRect = new Rect();
		if (tn != null) {
			gestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {
				@Override
				public boolean onDown(MotionEvent motionEvent) {
					return true; // returning true here prevents SeeScoreView from seeing the event so pinch zoom is disabled
					// alternatively returning false prevents the other methods in GestureDetector from being called so long press and tap fail!
				}

				@Override
				public void onShowPress(MotionEvent motionEvent) {
				}

				@Override
				public boolean onSingleTapUp(MotionEvent motionEvent) {
					lastTapPos = new Point(motionEvent.getX(), motionEvent.getY());
					int partIndex = system.getPartIndexForYPos(motionEvent.getY());
					int barIndex = system.getBarIndexForXPos(motionEvent.getX());
					try {
						Component[] components = system.hitTest(lastTapPos);
						tapNotify.tap(system.index(), partIndex, barIndex, components);

					} catch (ScoreException e) {
						java.lang.System.out.println(" exception:" + e.toString());
					}
					return true;
				}

				@Override
				public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
					return false;
				}

				@Override
				public void onLongPress(MotionEvent motionEvent) {
					int partIndex = system.getPartIndexForYPos(motionEvent.getY());
					int barIndex = system.getBarIndexForXPos(motionEvent.getX());
					try {
						Component[] components = system.hitTest(new Point(motionEvent.getX(), motionEvent.getY()));
						tapNotify.longTap(system.index(), partIndex, barIndex, components);

					} catch (ScoreException e) {
						java.lang.System.out.println(" exception:" + e.toString());
					}

				}

				@Override
				public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
					return false;
				}
			}, new Handler());
		}
	}

    /**
     * return the fractional position of the bar in the system for a pseudo smooth scroll
     * @param barIndex the 0-based bar index
     * @return the fractional position of the bar in the system (0 for leftmost, <1 for others)
     */
    public float fractionalScroll(int barIndex) {
        SSystem.BarRange br = system.getBarRange();
        return (float)(barIndex - br.startBarIndex) / br.numBars;
    }

    /**
     *
     * @param barIndex the 0-based bar index
     * @return true if the bar is in this system
     */
    public boolean containsBar(int barIndex)
    {
        return system.containsBar(barIndex);
    }

    /**
     * set the cursor at a given bar
     * @param barIndex the 0-based bar index
     * @param type the type of cursor (line or box)
     * @return true if the bar is in this system
     */
    public boolean setCursorAtBar(int barIndex, CursorType type)
    {
        boolean rval = system.containsBar(barIndex);
        if (rval) {
            cursorBarIndex = barIndex;
            cursorType = type;
            cursor_xpos = 0; // line cursor is drawn at left of bar
            invalidate();
        } else if (cursorType != CursorType.none) {
            cursorType = CursorType.none;
            cursor_xpos = 0;
            invalidate();
        }
        return rval;
    }

    /**
     * set the cursor at a given bar with a given x position in the system
     * @param barIndex the 0-based bar index
     * @param xpos the x coordinate from the left of the system
     * @return true if the bar is in this system
     */
    public boolean setCursorAtBar(int barIndex, float xpos)
    {
        boolean rval = system.containsBar(barIndex);
        if (rval) {
            cursorBarIndex = barIndex;
            cursor_xpos = xpos;
            cursorType = CursorType.line;
            invalidate();
        } else if (cursorType != CursorType.none) {
            cursorType = CursorType.none;
            invalidate();
        }
        return rval;
    }

	/** request a special colouring for a particular item in this System
	 * 
	 * @param item_h the unique identifier for the item
	 * @param col the colour. If black ie (0,0,0,1) then the colouring is removed
	 */
	public void colourItem(int partIndex, int barIndex, int item_h, Colour col)
	{
		if (item_h != 0)
		{
			Integer key = new Integer(item_h);
			if (col.r == 0 && col.g == 0 && col.b == 0 && col.a > 0.999) { // black
				// clear colouring for item_h
				renderItems.remove(key);
			}
			else {
				int[] coloured_render = new int[1];
				coloured_render[0] = RenderItem.ColourRenderFlags_notehead;
				renderItems.put(key, new RenderItem(partIndex, barIndex, item_h, col, coloured_render));
			}
		}
		else
		{
			renderItems = null;
		}
		invalidate();		
	}

	public void clearColouringForBarRange(int startBarIndex, int numBars)
	{
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		for (Map.Entry<Integer, RenderItem> entry : renderItems.entrySet())
		{
			int barIndex = entry.getValue().barIndex;
			if (barIndex >= startBarIndex
					&& barIndex < startBarIndex + numBars)
			{
				toRemove.add(new Integer(entry.getKey()));
			}
		}
		for (Integer key : toRemove) {
			renderItems.remove(key);
		}
		invalidate();
	}

	public void clearAllColouring()
	{
		renderItems.clear();
		invalidate();
	}

    /**
     * called by android to measure this view
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		float w = system.bounds().width;
		float h = system.bounds().height;
		setMeasuredDimension((int)w,(int)h);
	}

	/***
	 Play loop graphics.
	 We draw a standard repeat barline (thick, thin barlines with 2 dots) in each staff in blue over the score aligned with the barline.
	 Also draw a translucent background to prevent the black score obscuring the blue.
	 ***/
	static final float kThickBarlineThicknessTenths = 5F;
	static final float kThinBarlineThicknessTenths = 2F;
	static final float kBarlineSeparationTenths = 3.5F;
	static final float kRepeatDotsBarlineGap = 4F; // gap from barline to repeat dots
	static final float kRepeatDotRadius = 2.4F;
	static final float kRepeatDotOffset = 5F; // from centre of staff
	static final float kBarlineBackgroundMargin = 5F;

	void drawRepeatBarline(Canvas canvas, StaffLayout staffLayout, BarLayout.Barline barline, BarLayout.BarlineLoc loc) {
		final float thick = kThickBarlineThicknessTenths * staffLayout.tenthSize;
		final float thin = kThinBarlineThicknessTenths * staffLayout.tenthSize;
		final float gap = kBarlineSeparationTenths * staffLayout.tenthSize;
		final float dotGap = kRepeatDotsBarlineGap * staffLayout.tenthSize;
		final float dotRadius =  kRepeatDotRadius * staffLayout.tenthSize;
		final float dotOffset = kRepeatDotOffset * staffLayout.tenthSize;
		final float margin = kBarlineBackgroundMargin * staffLayout.tenthSize;

		// construct for repeat double barline - left or right
		Path path = new Path();
		Paint paint = new Paint();
		paint.setARGB(255, 0, 0, 255);
		RectF thickBarline = new RectF(barline.rect.centerX()-thick/2, barline.rect.top, barline.rect.centerX()+thick/2, barline.rect.bottom);
		path.addRect(thickBarline, Path.Direction.CW);
		float thinLeft;
		float dotCentrex;
		if (loc == BarLayout.BarlineLoc.left) {
			thinLeft = thickBarline.right + gap;
			dotCentrex = thinLeft + thin + dotGap + dotRadius;
		} else {
			thinLeft = thickBarline.left - gap - thin;
			dotCentrex = thinLeft - dotGap - dotRadius;
		}
		RectF thinBarline = new RectF(thinLeft, barline.rect.top, thinLeft + thin, barline.rect.bottom);
		path.addRect(thinBarline, Path.Direction.CW);
		// repeat dots on each staff
		for (StaffLayout.Staff staff : staffLayout.staves) {
			float centreStaffY = staff.staffRect.centerY();
			if (centreStaffY + dotOffset < getHeight()) { // ensure we don't add dots below bottom of view height
				path.addCircle(dotCentrex, centreStaffY - dotOffset, dotRadius, Path.Direction.CW);
				path.addCircle(dotCentrex, centreStaffY + dotOffset, dotRadius, Path.Direction.CW);
			}
		}
		// paint translucent white background
		Path bgPath = new Path();
		Paint bgPaint = new Paint();
		bgPaint.setARGB(180, 255, 255, 255);
		// background overlaps foreground by margin
		if (loc == BarLayout.BarlineLoc.left)
			bgPath.addRect(new RectF(thickBarline.left - margin, thickBarline.top-margin, dotCentrex+dotRadius+margin, thickBarline.bottom + margin), Path.Direction.CW);
		else
			bgPath.addRect(new RectF(dotCentrex - dotRadius - margin, thickBarline.top-margin, thickBarline.right + margin, thickBarline.bottom + margin), Path.Direction.CW);
		canvas.drawPath(bgPath, bgPaint);
		// paint repeat double barline in blue
		canvas.drawPath(path, paint);
	}
	/**
	 * called by android to draw the View
     * @param canvas the canvas
     */
	protected void onDraw(Canvas canvas)
	{
		backgroundPaintRect.left = 0;
		backgroundPaintRect.top = 0;
		backgroundPaintRect.right = canvas.getWidth();
		backgroundPaintRect.bottom = getHeight();
		canvas.drawRect(backgroundPaintRect, backgroundPaint);
		if (system != null)
		{
			if (renderItems != null)
			{
				// render notehead opaque red
				try {
					RenderItem[] ritems = new RenderItem[renderItems.size()];
					renderItems.values().toArray(ritems);
					system.drawWithOptions(canvas, assetManager, tl, zoomingMag, ritems);
				} catch (ScoreException e) {
					System.out.println(" error on draw:" + e);
				}
			}
			else
				system.draw(canvas, assetManager, tl, zoomingMag);
		}
		if (cursorType != CursorType.none && cursorBarIndex >= 0)
		{
			CursorRect cr = system.getCursorRect(canvas, cursorBarIndex);
			if (cr.barInSystem)
			{
                if (cursorType == CursorType.box)
				    canvas.drawRect(cr.rect, barRectPaint);
                else if (cursorType == CursorType.line) {
                    if (cursor_xpos > 0)
                        canvas.drawLine(cursor_xpos, cr.rect.top, cursor_xpos, cr.rect.top + cr.rect.height(), barRectPaint);
                    else
                        canvas.drawLine(cr.rect.left, cr.rect.top, cr.rect.left, cr.rect.top + cr.rect.height(), barRectPaint);
                }
			}
		}
		if (leftPlayLoopBarIndex >= 0 || rightPlayLoopBarIndex >= 0)
		{
			for (int p = 0; p < score.numParts(); ++p) {
				StaffLayout staffLayout = system.getStaffLayout(p);
				float tenth = staffLayout.tenthSize;
				BarLayout barLayout = system.getBarLayout(p);
				for (BarLayout.Barline barline : barLayout.barlines) {
					if (leftPlayLoopBarIndex >= 0
							&& barline.barIndex == leftPlayLoopBarIndex
							&& barline.loc == BarLayout.BarlineLoc.left) {
						drawRepeatBarline(canvas, staffLayout, barline, BarLayout.BarlineLoc.left);
					}
					else if (rightPlayLoopBarIndex >= 0
							&& (barline.barIndex == rightPlayLoopBarIndex + 1
							&& barline.loc == BarLayout.BarlineLoc.left) // right barline of bar n is left barline of bar n+1
								|| (barline.barIndex == rightPlayLoopBarIndex
							&& barline.loc == BarLayout.BarlineLoc.right) ) { // use a real right barline if available
						drawRepeatBarline(canvas, staffLayout, barline, BarLayout.BarlineLoc.right);
					}
				}
			}
		}
	}

	/**
	 * called during active pinch-zooming. We just draw the same system magnified
	 * @param zoom the magnification
	 */
	void zooming(final float zoom)
	{
		zoomingMag = zoom;
		if (!isZooming)
		{
			viewRect.top = getTop();
			viewRect.bottom = getBottom();
			isZooming = true;
		}
		setTop((int) (viewRect.top * zoom));
		setBottom((int) (viewRect.bottom * zoom));
		invalidate();
	}
	
	/**
	 * send touch events to the tap handler
     * NOTE: We really need to filter out the pinch zoom events and scroll events so these aren't seen as taps
	 */
	public boolean onTouchEvent (MotionEvent event)
	{
		if (gestureDetector == null)
			return super.onTouchEvent(event);
		else
			return gestureDetector.onTouchEvent(event);
	}

	void displayLeftPlayLoopGraphic(int barIndex) {
		leftPlayLoopBarIndex = barIndex;
		invalidate();
	}
	void displayRightPlayLoopGraphic(int barIndex) {
		rightPlayLoopBarIndex = barIndex;
		invalidate();
	}

	void hideLeftPlayLoopGraphic() {
		leftPlayLoopBarIndex = -1;
		invalidate();
	}
	void hideRightPlayLoopGraphic() {
		rightPlayLoopBarIndex = -1;
		invalidate();
	}

	private SScore score;
	private SSystem system;
	private AssetManager assetManager;
	private Point tl;
	private Paint backgroundPaint;
	private RectF backgroundPaintRect;
	private Paint tappedItemPaint;
	private Paint barRectPaint;
	private float zoomingMag;
	private Map<Integer, RenderItem> renderItems = new TreeMap<Integer, RenderItem>();
	private Rect viewRect;
	private boolean isZooming = false;
	private int cursorBarIndex;
    private CursorType cursorType = CursorType.none;
    private float cursor_xpos = 0;
    private SeeScoreView.TapNotification tapNotify;
	private Point lastTapPos;
	private GestureDetector gestureDetector;
	private int leftPlayLoopBarIndex;
	private int rightPlayLoopBarIndex;
}
