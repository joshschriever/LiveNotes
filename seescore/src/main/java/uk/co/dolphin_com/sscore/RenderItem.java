/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * Identify an item for special rendering and the define the rendering parameters (colour)
 * to use with SSystem.drawWithOptions()
 */
public class RenderItem
{
	/**
	 * Define a Colour with alpha
	 */
	public static class Colour
	{
		/**
		 * @param r red in range [0..1]
		 * @param g green in range [0..1]
		 * @param b blue in range [0..1]
		 * @param a alpha in range [0..1]
		 */
		public Colour(float r, float g, float b, float a)
		{
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
		}
		public final float r;
		public final float g;
		public final float b;
		public final float a;
	}

	public static Colour kBlack = new Colour(0,0,0,1);

	/**
	 * Values to be used in colouredRender
	 */
	public static int ColourRenderFlags_notehead = 0;
	public static int ColourRenderFlags_stem = 1;
	public static int ColourRenderFlags_beam = 2;
	public static int ColourRenderFlags_accidental = 3;
	public static int ColourRenderFlags_dot = 4;
	public static int ColourRenderFlags_rest = 5;
	public static int ColourRenderFlags_notation = 6;
	public static int ColourRenderFlags_lyric = 7;
	public static int ColourRenderFlags_ledger = 8;
	public static int ColourRenderFlags_clef = 9;
	public static int ColourRenderFlags_timesig = 10;
	public static int ColourRenderFlags_keysig = 11;
	public static int ColourRenderFlags_direction_text = 12;
	public static int ColourRenderFlags_harmony = 13;

	/**
	 * @param partIndex the index of the part containing the item
	 * @param barIndex the index of the bar containing the item
	 * @param item_h define the item for special rendering
	 * @param colour the colour to render this item
	 * @param colouredRender set of flags (ColourRenderFlags_?) defining parts of item to colour
	 */
	public RenderItem(int partIndex, int barIndex, int item_h, Colour colour, int[] colouredRender)
	{
		this.item_h = item_h;
		this.partIndex = partIndex;
		this.barIndex = barIndex;
		this.colour = colour;
		this.colouredRender = colouredRender;
	}

	public final int item_h;
	public final int partIndex;
	public final int barIndex;
	public final Colour colour;
	public final int[] colouredRender;
}