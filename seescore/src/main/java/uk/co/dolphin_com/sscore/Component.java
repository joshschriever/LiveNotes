/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */
package uk.co.dolphin_com.sscore;

import android.graphics.RectF;

/**
 * an atomic component of the layout for hit-testing etc.
 * Eg. a note consists of a notehead and possibly stem, accidental, lyric etc
 */
public class Component
{
	/**
	 * define the types of component
	 */
	public static enum Type 
	{
		notehead, rest, accidental, note_stem, timesig, keysig, clef, note_dots,
		lyric, ledgers, beamgroup, beam, tremolo,
		direction_text, direction_dynamics, direction_pedal, direction_metro, direction_reh, direction_segno, direction_coda,
		notation_slur, notation_tied, notation_slide, notation_glissando, notation_tuplet,
		direction_wedge, direction_dashes, _direction_bracket, direction_oshift, direction_principalvoice,
		note_ornament, note_articulation, note_dynamics, note_fermata, note_arpeggiate,
		note_tech, note_tech_fingering, note_tech_string, note_tech_fret, note_tech_hammerpull,
		barline_thin, barline_thick, barline_repeat_left, barline_repeat_right, barline_segno, barline_coda, barline_fermata,
		harmony, harmony_frame, repeat_brace, multiple, parent, undefined, none };

	/**
	 * the type of this component
	 */
	public final Component.Type type;
	
	/**
	 * the partindex for the part containing this component
	 */
	public final int partindex;
	
	/**
	 * the bar index for the bar containing this component
	 */
	public final int barindex;
	
	/**
	 * the bounding box of this component in the layout
	 */
	public final RectF rect;
	
	/**
	 * the unique identifier for this component in the layout
	 */
	public final int layout_h;
	
	/**
	 * the unique identifier of the parent item in the score (note/rest etc)
	 */
	public final int item_h;
	
	public String toString()
	{
		return " Component:" + type + " p:" + partindex + " b:" + barindex + " r:" + rect  + " l:" + layout_h + " i:" + item_h;
	}

	private Component()
	{
		this.type = Type.undefined;
		this.partindex = 0;
		this.barindex = 0;
		this.rect = null;
		this.layout_h = 0;
		this.item_h = 0;
	}
	private Component(Component.Type tp, int partindex, int barindex, RectF rect, int layout_h, int item_h)
	{
		this.type = tp;
		this.partindex = partindex;
		this.barindex = barindex;
		this.rect = rect;
		this.layout_h = layout_h;
		this.item_h = item_h;
	}
}