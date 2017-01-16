/**
 * SeeScore For Android Sample App
 * Dolphin Computing http://www.dolphin-com.co.uk
 */
package uk.co.dolphin_com.seescoreandroid;

import java.util.ArrayList;
import java.util.List;

import uk.co.dolphin_com.sscore.Component;
import uk.co.dolphin_com.sscore.LayoutCallback;
import uk.co.dolphin_com.sscore.LayoutOptions;
import uk.co.dolphin_com.sscore.RenderItem;
import uk.co.dolphin_com.sscore.SScore;
import uk.co.dolphin_com.sscore.SSystem;
import uk.co.dolphin_com.sscore.SSystem.BarRange;
import uk.co.dolphin_com.sscore.SSystemList;
import uk.co.dolphin_com.sscore.Size;
import uk.co.dolphin_com.sscore.ex.NoPartsException;
import uk.co.dolphin_com.sscore.ex.ScoreException;
import uk.co.dolphin_com.sscore.playdata.Note;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import uk.co.dolphin_com.sscore.StaffLayout;
import uk.co.dolphin_com.sscore.BarLayout;

/**
 * SeeScoreView manages layout of a {@link SScore} and placement of {@link SystemView}s into a scrolling View.
 */
public class SeeScoreView extends LinearLayout  {

    /**
     * The type of cursor, a vertical line or a rectangle around a bar
     */
    public static enum CursorType
    {
        line, box
    }

    /**
     * for notification of zoom change
     */
	public interface ZoomNotification
	{
        /**
         * called on notification of zoom
         * @param scale the new zoom scale
         */
		void zoom(float scale);
	}

    /**
     * for notification of a tap in the view
     */
    public interface TapNotification
    {
        /**
         * called on notification of a user tap on a system with information about what was tapped
         * @param systemIndex the index of the system tapped (0 is the top system)
         * @param partIndex the 0-based part index of the part tapped
         * @param barIndex the 0-based bar index of the bar tapped
         * @param components the components tapped
         */
        void tap(int systemIndex, int partIndex, int barIndex, Component[] components);

		void longTap(int systemIndex, int partIndex, int barIndex, Component[] components);
    }

    /**
     * autoscroll centres the current playing bar around here
     */
    private static final float kWindowPlayingCentreFractionFromTop = 0.333F;

	/**
	 * the minimum magnification
	 */
	private static final float kMinMag = 0.2F;

	/**
	 * the maximum magnification
	 */
	private static final float kMaxMag = 3.F;

	/**
	 * the margin between the edge of the screen and the edge of the layout
	 */
	static final float kMargin = 10;

    /**
     * construct the SeeScore scrollable View
     * @param context (usually the MainActivity)
     * @param am the asset manager for font handling
     * @param zn the zoom notification which is called on (pinch) zoom change
     * @param tn the tap notification which is called on a tap in the view with info about what was tapped
	 *           if tn == null then taps will not be intercepted and pinch-zoom is enabled
	 *           NB tn disables pinch-zoom
     */
	public SeeScoreView(Activity context, AssetManager am, ZoomNotification zn, TapNotification tn) {
		super(context);
		setOrientation(VERTICAL);
		this.assetManager = am;
		this.magnification = 1.0F;
		this.zoomNotify = zn;
        this.tapNotify = tn;
		DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
		Display display = context.getWindowManager().getDefaultDisplay();
		display.getMetrics(displayMetrics);
		displayDPI = displayMetrics.densityDpi;
		android.graphics.Point screenSize = new android.graphics.Point();
		display.getSize(screenSize);
		screenHeight = screenSize.y;
	}

    /**
     * set a handler to be called on completion of layout (not usually necessary)
     * @param handler run() is called on layout completion
     */
	public void setLayoutCompletionHandler(Runnable handler)
	{
		layoutCompletionHandler = handler;
	}

    /**
     * * get the magnification
     * @return the current magnification
     */
	public float getMagnification()
	{
		return magnification;
	}

    /**
     * get the width and height of the SeeScoreView
     * @return the bounds
     */
	public Size getBounds()
	{
		return systems.getBounds(0);
	}

	private void addSystem(final SSystem sys)
	{
		systems.addSystem(sys);
		new Handler(Looper.getMainLooper()).post(new Runnable(){

			public void run() {
				SystemView sv = new SystemView(getContext(), score, sys, SeeScoreView.this.assetManager, tapNotify);
				addView(sv);
				views.add(sv);
			}
		});
	}
	
	private List<BarRange> getAllBarRanges()
	{
		ArrayList<BarRange> rval = new ArrayList<BarRange>();
		for (int sysindex = 0; sysindex < systems.getSize(); ++sysindex)
			rval.add(systems.getSystemAt(sysindex).getBarRange());
		return rval;
	}

	protected void onSizeChanged (int w, int h, int oldw, int oldh)
	{
		if (w > 0 && score != null)
		{
			layout();
		}
	}

	/**
	 * Set the loaded SScore to be displayed by this.
	 * <p>
	 * This will initiate an asynchronous layout and the View will be updated as
	 * each System completes layout
	 * 
	 * @param score the SScore to be displayed
	 * @param parts the parts to view
     * @param magnification the magnification to use (default is 1.0)
	 */
	public void setScore(final SScore score, final List<Boolean> parts, final float magnification)
	{
		abortLayout(new Runnable(){
			public void run()
			{

				SeeScoreView.this.score = score;
				SeeScoreView.this.parts = parts;
				SeeScoreView.this.magnification = magnification;
				if (score != null && getWidth() > 0)
					layout();
				// else layout will be called in OnSizeChanged()
				else
				{
					systems = null;
					removeAllViews();
				}
			}
		});
	}

    /** slower than smoothScrollTo */
    private void slowSmoothScrollTo(final ScrollView parentScrollView, int scrollY, int autoScrollAnimationTime) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ValueAnimator realSmoothScrollAnimation =
                    ValueAnimator.ofInt(parentScrollView.getScrollY(), scrollY);
            realSmoothScrollAnimation.setDuration(autoScrollAnimationTime);
            realSmoothScrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int scrollTo = (Integer) animation.getAnimatedValue();
                    parentScrollView.scrollTo(0, scrollTo);
                }
            });

            realSmoothScrollAnimation.start();
        } else {
            parentScrollView.smoothScrollTo(0, scrollY);
        }
    }

    private void scrollToBar(int barIndex, int autoScrollAnimationTime) {
        ViewParent parentView = getParent();
        if (parentView instanceof ScrollView) {
            final ScrollView parentScrollView = (ScrollView) parentView;
            for (View view : views) {
                if (view instanceof SystemView
                        && view.isShown()) {
                    SystemView systemView = (SystemView) view;
                    if (systemView.containsBar(barIndex)) {
                        int scroll_max = getBottom() - parentScrollView.getHeight();
                        int scroll_min = 0;
                        float windowHeight = parentScrollView.getHeight();
                        float windowPlayingCentre = kWindowPlayingCentreFractionFromTop * windowHeight;
                        float sysFrac = systemView.fractionalScroll(barIndex);
                        float playingCentre = view.getTop() + view.getHeight() * sysFrac;
                        float scroll_y = playingCentre - windowPlayingCentre;

                        if (scroll_y < scroll_min)
                            scroll_y = scroll_min;
                        else if (scroll_y > scroll_max)
                            scroll_y = scroll_max;
                        slowSmoothScrollTo(parentScrollView, (int) scroll_y, autoScrollAnimationTime);
                    }
                }
            }
        }
    }

    /**
     * set the cursor at a given bar
     * @param barIndex the (0-based) bar index
     * @param type use a vertical line or a box around the bar
     * @param autoScrollAnimationTime a hint for the scroll time in ms
     * @return true if it succeeded
     */
    public boolean setCursorAtBar(int barIndex, CursorType type, int autoScrollAnimationTime) {
        boolean rval = false;
        for (View view : views) {
            if (view instanceof SystemView
                    && view.isShown()) {
                SystemView systemView = (SystemView) view;
                if (systemView.setCursorAtBar(barIndex, (type==CursorType.line)?SystemView.CursorType.line:SystemView.CursorType.box)) {
                    scrollToBar(barIndex, autoScrollAnimationTime);
                }
            }
        }
        return rval;
    }

    /**
     * set the vertical line cursor at a given bar with a given xpos (from the system left)
     * @param barIndex the (0-based) bar index
     * @param xpos the x coordinate from the system left
     * @param autoScrollAnimationTime a hint for the scroll time in ms
     * @return true if it succeeded
     */
    public boolean setCursorAtBar(int barIndex, float xpos, int autoScrollAnimationTime) {
        boolean rval = false;
        for (View view : views) {
            if (view instanceof SystemView
                    && view.isShown()) {
                SystemView systemView = (SystemView) view;
                if (systemView.setCursorAtBar(barIndex, xpos)) {
                    scrollToBar(barIndex, autoScrollAnimationTime);
                }
            }
        }
        return rval;
    }

    private SSystem systemContainingBarIndex(int barIndex) {
        for (SSystem system : systems)
        {
            if (system.containsBar(barIndex))
                return system;
        }
        return null;
    }

    private float noteXPos(Note note) {
        SSystem system = systemContainingBarIndex(note.startBarIndex);
        if (system != null) {
            try {
                Component[] components = system.getComponentsForItem(note.item_h);
                for (Component comp : components) {
                    if (comp.type == Component.Type.notehead)
                        return comp.rect.left + comp.rect.width() / 2; // centre of notehead
                }
            }
            catch (ScoreException e){
                System.out.println("failed getComponentsForItem");
            }
        }
        return 0;
    }

    /**
     * align the vertical line cursor with the first of the notes in the List for which it can find an xpos
     * @param notes a list of notes in a chord
     * @param animationTime a hint for the scroll time in ms
     */
    public void moveNoteCursor(List<Note> notes, int animationTime)
    {
        for (Note note : notes)
        {
            int barIndex = note.startBarIndex;
            if (note.start >= 0) // ignore cross-bar tied notes
            {

                float xpos = noteXPos(note);
                if (xpos > 0) {
                    setCursorAtBar(barIndex, xpos, animationTime);
                    return; // we only need one notehead in the chord to move the cursor to
                }
            }
        }
    }

    /**
	 * A Thread to perform a complete (abortable) layout of the entire score which
	 * may take unlimited time, but periodically updates the UI whenever a new laid-out system
	 * is ready to add to the display
	 */
	private class LayoutThread extends Thread
	{
		LayoutThread(float displayHeight)
		{
			super("LayoutThread");
			this.displayHeight = displayHeight;
			aborting = false;
			views.clear();
		}

		/**
		 *  set the abort flag so that the layout thread will stop ASAP
		 */
		public void abort()
		{
			aborting = true;
		}

		public void run()
		{
			if (aborting || score == null)
				return;
			Canvas canvas = new Canvas();
			int numParts = score.numParts();
			boolean[] partsArray = new boolean[numParts];
			if (numParts <= parts.size()) {
				for (int i = 0; i < numParts; ++i) {
					partsArray[i] = parts.get(i).booleanValue();
				}
			}
			else {
				for (int i = 0; i < numParts; ++i) {
					partsArray[i] = true;
				}
			}
			LayoutOptions opt = new LayoutOptions();
			if (displayHeight > 100
				&& !aborting)
			{
				try
				{
					score.layout(canvas, assetManager, displayDPI, getWidth() - 2*kMargin, displayHeight, partsArray,
						new LayoutCallback(){
					public boolean addSystem(SSystem sys)
					{
						if (!aborting)
							SeeScoreView.this.addSystem(sys);
						return !aborting; // return false to abort layout
					}
				},
				magnification,opt);
				}
				catch (NoPartsException e)
				{
					Log.w("sscore", "layout no parts error");
				}
				catch (ScoreException e)
				{
					Log.w("sscore", "layout error:" + e);
				}
			}
            if (SeeScoreView.this.layoutCompletionHandler != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    public void run() {
                        SeeScoreView.this.layoutCompletionHandler.run();
                    }
                });
            }
		}
		
		private boolean aborting;
		private float displayHeight;
	}
	
	/**
	 *  abort the layout and notify completion of abort on the main thread through the Runnable argument
	 *  
	 * @param thenRunnable run() is executed when abort is complete on the main thread
	 */
	public void abortLayout(final Runnable thenRunnable)
	{
		if (isAbortingLayout)
			return; // already aborting - thenRunnable is DISCARDED!
		if (layoutThread != null)
		{
			isAbortingLayout = true;
			layoutThread.abort();
			new Thread(new Runnable() { // start a thread to await completion of the abort
				public void run()
				{
					{
						try {
							layoutThread.join(); // await completion of abort
						} catch (InterruptedException e) {
							// don't care if interrupted during join
						}
						layoutThread = null;
						isAbortingLayout = false;
						new Handler(Looper.getMainLooper()).post(new Runnable(){

							public void run() {
								thenRunnable.run();
							}
						});
					}
				}
			}, "AbortThread").start();
		}
		else
			thenRunnable.run();
	}

	private void layout()
	{
		if (!isAbortingLayout && layoutThread == null)
		{
			systems = new SSystemList();
			removeAllViews();
			layoutThread = new LayoutThread(screenHeight);
			layoutThread.start();
		}
	}
	
	/**
	 * called during active pinching. Do quick rescale of display without relayout
	 * Relayout happens on completion of gesture
	 * 
	 * @param zoom the current magnification based on the finger spacing compared
	 * to the spacing at the start of the pinch
	 */
	private void zooming(final float zoom)
	{
		float mag;
		if (magnification < kMinMag)
			magnification = kMinMag; // ensure no div-by-zero
		if (zoom * magnification < kMinMag)
			mag = kMinMag / magnification;
		else if (zoom * magnification > kMaxMag)
			mag = kMaxMag / magnification;
		else if (Math.abs(zoom * magnification - 1.0) < 0.05) // make easy to set 1.0
			mag = 1.0F / magnification;
		else
			mag = zoom;
		
		for (View view : views)
		{
			if (view instanceof SystemView
				&& view.isShown())
			{
				Rect r = new Rect();
				boolean vis = view.getGlobalVisibleRect (r);
				if (vis)
				{
					SystemView sv = (SystemView)view;
					sv.zooming(mag);
				}
			}
		}
		zoomNotify.zoom(zoom * magnification);
	}
	
	/**
	 * abort any current layout and make a new layout at the new magnification
	 * 
	 * @param zoom the new magnification to use for the layout
	 */
	public void zoom(final float zoom)
	{
		abortLayout(new Runnable() {
			public void run() {
				magnification = Math.min(Math.max(zoom, kMinMag), kMaxMag);
				if (Math.abs(magnification - 1.0) < 0.05)
					magnification = 1.0F; // gravitate towards 1.0
				layout();
				zoomNotify.zoom(magnification);
			}
		});
	}
	
	/** spacing between fingers during a pinch gesture */
	private float fingerSpacing(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float)java.lang.Math.sqrt(x * x + y * y);
	}
	
	/**
	 * called from the system for a touch notification
	 *
	 * NB The GestureDetector in SystemView intercepts all events so this is never called
	 *
     * @param event the touch event
     * @return true if handled
     */
	public boolean onTouchEvent (MotionEvent event)
	{
		int action = MotionEventCompat.getActionMasked(event);
		switch(action)
		{
		case MotionEvent.ACTION_POINTER_DOWN:
			startPinchFingerSpacing = fingerSpacing(event);
			if (startPinchFingerSpacing > 10f) {
				isZooming = true;
			}
			return true;

		case MotionEvent.ACTION_MOVE:
			if (isZooming && startPinchFingerSpacing > 10 ) {
				float mag = fingerSpacing(event) / startPinchFingerSpacing;
				zooming(mag);
				return true;
			}
			break;
			
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_UP:
			if (isZooming)
			{
				float mag = magnification * fingerSpacing(event) / startPinchFingerSpacing;
				zoom(mag);	
				isZooming = false;
				return true;
			}
			break;

		}
		return false;
	}

	void colourItem(int partIndex, int barIndex, int item_h, RenderItem.Colour col)
	{
		for (View view : views) {
			if (view instanceof SystemView
				&& view.isShown()) {
				SystemView systemView = (SystemView) view;
				if (systemView.containsBar(barIndex))
				{
					systemView.colourItem(partIndex, barIndex, item_h, col);
					return;
				}
			}
		}
	}

	public void clearAllColouring()
	{
		for (View view : views) {
			if (view instanceof SystemView
					&& view.isShown()) {
				SystemView systemView = (SystemView) view;
				systemView.clearAllColouring();
			}
		}
	}

	public void clearColouringForBarRange(int startBarIndex, int numBars) {
		for (View view : views) {
			if (view instanceof SystemView
					&& view.isShown()) {
				SystemView systemView = (SystemView) view;
				systemView.clearColouringForBarRange(startBarIndex, numBars);
			}
		}
	}

	public void displayLoopGraphics(int startBar, int endBar) {
		loopStart = startBar;
		loopEnd = endBar;
		for (View view : views) {
			if (view instanceof SystemView
					&& view.isShown()) {
				SystemView systemView = (SystemView) view;
				if (loopStart >= 0 && systemView.containsBar(loopStart))
					systemView.displayLeftPlayLoopGraphic(loopStart);
				else
					systemView.hideLeftPlayLoopGraphic();
				if (loopEnd >= 0 && systemView.containsBar(loopEnd))
					systemView.displayRightPlayLoopGraphic(loopEnd);
				else
					systemView.hideRightPlayLoopGraphic();
			}
		}
	}
	public void hideLoopGraphics() {
		loopStart = -1;
		loopEnd = -1;
		for (View view : views) {
			if (view instanceof SystemView
					&& view.isShown()) {
				SystemView systemView = (SystemView) view;
				systemView.hideLeftPlayLoopGraphic();
				systemView.hideRightPlayLoopGraphic();
			}
		}
	}

    private SScore score;
    private AssetManager assetManager;
    private int displayDPI;
    private SSystemList systems;
    private float magnification;
    private LayoutThread layoutThread;
    private float screenHeight;
    private boolean isAbortingLayout = false;
    private float startPinchFingerSpacing;
    private boolean isZooming = false;
    private ArrayList<SystemView> views = new ArrayList<SystemView>();
    private ZoomNotification zoomNotify;
    private TapNotification tapNotify;
    private Runnable layoutCompletionHandler;
	private List<Boolean> parts;

	private int loopStart;
	private int loopEnd;
}
