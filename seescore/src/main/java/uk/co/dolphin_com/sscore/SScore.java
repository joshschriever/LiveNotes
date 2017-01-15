/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

import java.io.File;
import java.text.NumberFormat;

import uk.co.dolphin_com.sscore.ex.ScoreException;
import android.content.res.AssetManager;
import android.graphics.Canvas;

/**
 * The main class of the SeeScore API, this encapsulates all information about the score loaded from a MusicXML file
 * <p>
 * loadXMLFile() or loadXMLData() should be used to load a file and create a SScore object
 * <p>
 * layout() should be called on a background thread to create a layout, and SSystems are generated sequentially from
 * the top and can be added to the display as they are produced. This is handled by uk.co.dolphin_com.seescoreandroid.SeeScoreView
 * <p>
 * numBars(), numParts(), getHeader(), getPartNameForPart(), getBarNumberForIndex() all return basic information about the score.
 * <p>
 * setTranspose() allows you to transpose the score.
 * <p>
 * Other methods return detailed information about items in the score and require a contents or contents-detail licence.
 */
public class SScore
{
	/**
	 * Get the version of the SeeScore library
	 * NB if lo < 10 it must be printed with a leading zero ie (1,1) -> "1.01"
	 * Use getVersion().toString() to get the correct printable representation
	 * 
	 * @return the version hi.lo
	 */
	public static native Version getVersion();

	/**
	 * Load xml data in UTF-8 byte buffer and return score. NB cannot handle mxl data which must be decompressed 
	 * by the caller (use ZipInputStream).
	 * 
	 * @param buffer contains the xml
	 * @param loadOptions the options for load, or NULL for default options
	 * @return the score
	 * @throws ScoreException defining any error on load
	 */
	public static native SScore loadXMLData(byte[] buffer, LoadOptions loadOptions) throws ScoreException;

	/**
	 * Load xml file and return score. NB cannot handle mxl file.
	 * 
	 * @param file the full pathname of the file to load
	 * @param loadOptions the options for load including the licence key
	 * @return the score
	 * @throws ScoreException defining any error on load
	 */
	public static native SScore loadXMLFile(File file, LoadOptions loadOptions) throws ScoreException;
	
	/**
	 * Get any warnings about problems with the file consistency discovered during load.
	 * 
	 * @return the array of warnings
	 */
	public LoadWarning[] getLoadWarnings() { return loadWarnings;}

	/**
	 * Get the total number of bars in the score.
	 * 
	 * @return the number of bars
	 */
	public native int numBars();
	
	/**
	 * Get the number of parts in the score.
	 * 
	 * @return the number of parts
	 */
	public native int numParts();

	/**
	 * Get the number of staves in a given part
	 *
	 * @param partIndex the part index
	 * @return the number of staves (0 or 1)
	 */
	public native int numStavesForPart(int partIndex);

	/**
	 * Return the name for the part.
	 * 
	 * @param partindex the index of the part [0..numparts-1]
	 * @return PartName (full + abbreviated)
	 */
	public native PartName getPartNameForPart(int partindex);

	/**
	 * Layout a single system with a single part.
	 * You specify a start bar index and a width and magnification and it will display
	 * as many bars as will fit into this width.
	 * <p>Useful for display of individual parts for part selection.
	 *  
	 * @param canvas the canvas is used to measure bounds of items
	 * @param am the AssetManager is used to load fonts from assets
	 * @param displayDPI the dots-per-inch value for the display viz. android.view.Display.getMetrics(android.util.DisplayMetrics).densityDPI
	 * @param startbarindex the index of the first bar in the system (usually 0)
	 * @param max_height the maximum height available to display the system to control truncation. =0 for no truncation
	 * @param partindex the index of the single part to layout [0..numparts-1]
	 * @param magnification the scale at which to display this (1.0 is default)
	 * @return the system
	 */
	public native SSystem layout1System(Canvas canvas, AssetManager am, int displayDPI,
						 int startbarindex, float width, float max_height,
						 int partindex, float magnification);

	/**
	 * Layout a set of systems and return them through a callback function.
	 * <p>This should be called on a background thread and it will call cb for each system laid out,
	 * from top to bottom.
	 * cb will normally add the system to a sscore_systemlist on the foreground (gui event dispatch)
	 *  thread.
	 * <p>systems are stored in this SScore as they are produced.
	 * <p>This allows the UI to remain active during concurrent layout which may take many seconds
	 *  
	 * @param canvas the canvas is used to measure bounds of items
	 * @param am the AssetManager is used to load fonts from assets
	 * @param displayDPI the dots-per-inch value for the display viz. android.view.Display.getMetrics(android.util.DisplayMetrics).densityDPI
	 * @param width the width available to display the systems in screen coordinates
	 * @param max_system_height the maximum height available to display each system to control truncation. =0 for no truncation
	 * @param parts array of boolean, 1 per part, true to include, false to exclude
	 * @param cb the callback function to be called for each completed system
	 * @param magnification the scale at which to display this (1.0 is default)
	 * @param opt pointer to options or NULL for default
	 * @throws ScoreException defining any error encountered during layout
	 */
	public native void layout(Canvas canvas, AssetManager am, int displayDPI,
			float width, float max_system_height,
			boolean[] parts,
			LayoutCallback cb,
			float magnification,
			LayoutOptions opt)  throws ScoreException;	

	/**
	 * Get the xml score-header information.
	 * 
	 * @return the header
	 */
	public native Header getHeader();

	/**
	 * Get the bar number (String) given the index.
	 * 
	 * @param barindex integer index [0..numBars-1]
	 * @return the score-defined number String (usually "1" for index 0)
	 */
	public native String getBarNumberForIndex(int barindex);
	
	/** set a transposition for the score.
	 * Call layout() after calling setTranspose for a new transposed layout.
	 * <p>Requires the transpose licence.
	 * 
	 * @param semitones (- for down, + for up)
	 */
	public native void setTranspose(int semitones) throws ScoreException;

	/**
	 * Get the current transpose value set with setTranspose.
	 * <p>Requires the transpose licence.
	 *
	 * @return the current transpose
	 */
	public native int getTranspose();
	
	/**
	 * return detailed information about an item in the score.
	 * <p>
	 * Requires contents-detail licence.
	 * 
	 * @param partindex 0-based part index - 0 is the top part
	 * @param barindex 0-based bar index
	 * @param item_h unique id for item
	 * @return TimedItem which can be cast to the specific derived type - NoteItem/DirectionItem etc.
	 */
	public native TimedItem getItemForHandle(int partindex, int barindex, int item_h) throws ScoreException;

	/**
	 * return the XML for the item in the part/bar.
	 * <p>
	 * Requires contents licence.
	 * 
	 * @param partindex the 0-based part index - 0 is top
	 * @param barindex the 0-based bar index
	 * @param item_h the unique id of the item
	 * @return the XML as a String
	 */
	public native String getXmlForItem(int partindex, int barindex, int item_h) throws ScoreException;

	/**
	 * Get information about the contents of a particular part/bar.
	 * <p>
	 * Requires contents-detail licence.
	 * 
	 * @param partindex the index of the part (0-based)
	 * @param barindex the index of the bar (0-based)
	 * @return the BarGroup containing an array of Item
	 */
	public native BarGroup getBarContents(int partindex, int barindex) throws ScoreException;
	
	/**
	 * Return the raw XML for this given part/bar index as a String.
	 * <p>
	 * Requires contents-detail licence.
	 * 
	 * @param partindex the index of the part (0-based)
	 * @param barindex the index of the bar (0-based)
	 * @return the XML as a String
	 */
	public native String getXmlForBar(int partindex, int barindex) throws ScoreException;

    /**
     * does the score define any tempo at the start with metronome or sound tempo elements?
     *
     * @return true if the score defines tempo. Use this to determine whether the user defines
     *  the absolute tempo or the tempo scaling
     */
    public native boolean hasDefinedTempo();

    /**
     * a normal full bar
     */
    public static final int Bartype_full_bar = 0;

    /**
     * partial bar is first bar in score (ie anacrusis)
     */
    public static final int Bartype_partial_first_bar = 1;

    /**
     * partial bar including beat 1 (ie before repeat mark)
     */
    public static final int Bartype_partial_bar_start = 2;

    /**
     * partial bar missing beat 1 (ie after repeat mark)
     */
    public static final int Bartype_partial_bar_end = 3;

    /**
     * for default argument
     */
    public static final int Bartype_default = 4;

    /**
     * get the type of the bar
     *
     * @param barIndex
     * @return Bartype_*
     */
    public native int barTypeForBar(int barIndex);

    /**
     * get the applicable time signature for a particular bar
     *
     * @param barIndex the index of the bar. 0 is the first bar
     * @return the applicable TimeSig
     * @throws ScoreException on error
     */
    public native TimeSig timeSigForBar(int barIndex) throws ScoreException;

    /**
     * get the actual number of beats in the bar and the beat type
     * This is normally the same as timeSigForBar, but will have fewer beats for a partial bar (eg anacrusis)
     *
     * @param barIndex the index of the bar. 0 is the first bar
     * @return the effective TimeSig
     * @throws ScoreException on error
     */
    public native TimeSig actualBeatsForBar(int barIndex) throws ScoreException;

    /**
     * get information about any metronome defined in the bar
     *
     * @param barIndex the index of the bar. 0 is the first bar
     * @return metronome information
     * @throws ScoreException on error
     */
    public native Tempo metronomeForBar(int barIndex) throws ScoreException;

    /**
     * get the effective tempo at the bar accounting for any sound tempo elements and metronome elements
     *
     * @param barIndex the index of the bar (0 is 1st)
     * @return the effective Tempo
     * @throws ScoreException on error
     */
    public native Tempo tempoAtBar(int barIndex) throws ScoreException;

    /**
     * get the effective tempo at start of the score accounting for any sound tempo elements and metronome elements
     *
     * @return the effective Tempo
     * @throws ScoreException on error
     */
    public native Tempo tempoAtStart() throws ScoreException;

    /**
     * get a beats-per-minute value for a given Tempo and TimeSig
     *
     * @param tempo the score-defined or user-defined tempo
     * @param timesig the effective time signature
     * @return beats per minute
     */
    public native int convertTempoToBPM(Tempo tempo, TimeSig timesig);

    /**
     * get the number of beats in a bar and the beat timing
     *
     * @param barIndex the index of the bar (0 is 1st)
     * @param bpm the effective beats-per-minute value
     * @param bartype one of Bartype_?
     * @return beat number and timing
     * @throws ScoreException
     */
    public native BarBeats getBarBeats(int barIndex, int bpm, int bartype) throws ScoreException;

    private SScore(long nativePointer, LoadWarning[] w, SScoreKey key)
	{
		this.nativePointer = nativePointer;
		this.loadWarnings = w;
		this.key = key;
	}

	private final long nativePointer;
	private final LoadWarning[] loadWarnings;
	private final SScoreKey key;
}
