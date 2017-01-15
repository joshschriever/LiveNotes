/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * MusicXML <harmony> element
 */
public class Harmony
{
	public static final int Harmony_unset = 0;
	public static final int Harmony_explicit = 1;
	public static final int Harmony_implied = 2;
	public static final int Harmony_alternate = 3;

	/**
	 * define a (step/alter) pitch value
	 */
	public static class HarmonyPitch
	{
		public static final int Step_A = 0;
		public static final int Step_B = 1;
		public static final int Step_C = 2;
		public static final int Step_D = 3;
		public static final int Step_E = 4;
		public static final int Step_F = 5;
		public static final int Step_G = 6;
		public static final int Step_undef = 7;

		/**
		 * one of Step_
		 */
		public final int step;
		
		/**
		 * +1 is one sharp, -1 is one flat
		 */
		public final int alter;
		
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			switch(step)
			{
			case Step_A: sb.append("A");break;
			case Step_B: sb.append("B");break;
			case Step_C: sb.append("C");break;
			case Step_D: sb.append("D");break;
			case Step_E: sb.append("E");break;
			case Step_F: sb.append("F");break;
			case Step_G: sb.append("G");break;
			default:break;
			}
			if (alter > 0)
				sb.append("+").append(alter);
			else if (alter < 0)
				sb.append(alter);				
			return sb.toString();
		}
		HarmonyPitch(){
			this.step = Step_undef;
			this.alter = 0;
		}
		HarmonyPitch(int step, int alter){
			this.step = step;
			this.alter = alter;
		}
	}

	/**
	 * MusicXML <kind> element
	 */
	public static class HarmonyKind
	{
		/**
		 * see values in MusicXML spec
		 */
		public final int value;
		public final boolean use_symbols;
		public final String text;
		public final boolean stack_degrees;
		public final boolean parentheses_degrees;
		public final boolean bracket_degrees;
		
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("kind:").append(value);
			if (use_symbols)
				sb.append(" use_symbols:");
			if (text != null && text.length() > 0)
				sb.append(" text:").append(text);
			if (stack_degrees)
				sb.append(" stack_degrees");
			if (parentheses_degrees)
				sb.append(" parentheses_degrees");
			if (bracket_degrees)
				sb.append(" bracket_degrees");
			return sb.toString();
		}

		private HarmonyKind(){
			this.value = 0;
			this.use_symbols = false;
			this.text = "";
			this.stack_degrees = this.parentheses_degrees = this.bracket_degrees = false;
		}
		private HarmonyKind(int v, boolean sym, String text, boolean stack, boolean paren, boolean brack){
			this.value = v;
			this.use_symbols = sym;
			this.text = text;
			this.stack_degrees = stack;
			this.parentheses_degrees = paren;
			this.bracket_degrees = brack;
		}
	}
	
	/**
	 * a harmony chord
	 */
	public static class Chord
	{
		/**
		 * root of chord
		 */
		public final HarmonyPitch root;
		/**
		 * kind of harmony
		 */
		public final HarmonyKind kind;
		/**
		 * inversion number - 0 is none
		 */
		public final int inversion;
		/**
		 * bass of chord. null if undefined
		 */
		public final HarmonyPitch bass;
		
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Chord: root:").append(root.toString());
			if (kind != null)
				sb.append(" ").append(kind.toString());
			if (inversion != 0)
				sb.append(" inversion:").append(inversion);
			if (bass != null && bass.step != HarmonyPitch.Step_undef)
				sb.append(" bass:").append(bass.toString());
			return sb.toString();
		}
		private Chord(){
			this.root = null;
			this.kind = null;
			this.inversion = 0;
			this.bass = null;
		}
		private Chord(HarmonyPitch root, HarmonyKind kind, int inversion, HarmonyPitch bass){
			this.root = root;
			this.kind = kind;
			this.inversion = inversion;
			this.bass = bass;
		}
	}
	
	/**
	 * a note in a frame
	 */
	public static class FrameNote
	{
		/**
		 * types of start/stop which specify barre start and stop
		 */
		public static final int StartStop_undef = 0;
		public static final int StartStop_start = 1;
		public static final int StartStop_stop = 2;
		public static final int StartStop_unknown = 3;
		
		/**
		 * The instrument string
		 */
		public final int string;
		
		/**
		 * The instrument fret
		 */
		public final int fret;
		
		/**
		 * The fingering - null if undefined
		 */
		public final String fingering;
		
		/**
		 * The barre - StartStop_
		 */
		public final int barre;
		
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("FrameNote: string:").append(string);
			sb.append(" fret:").append(fret);
			if (fingering != null)
				sb.append(" fingering:").append(fingering);
			if (barre != StartStop_undef)
				sb.append(" barre:").append(barre);
			return sb.toString();
		}
		
		private FrameNote() {
			this.string = 0;
			this.fret = 0;
			this.fingering = "";
			this.barre = 0;
		}
		private FrameNote(int string, int fret, String fingering, int barre) {
			this.string = string;
			this.fret = fret;
			this.fingering = fingering;
			this.barre = barre;
		}
	}

	/**
	 *  a harmony frame
	 */
	public static class Frame
	{
		public final int strings;
		public final int frets;
		public final int firstfret;		
		public final FrameNote[] framenotes;
		
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			if (strings != 0 || frets != 0 || firstfret != 0 || framenotes.length > 0)
			{
				sb.append("Frame: strings:").append(strings);
				sb.append(" frets:").append(frets);
				sb.append(" firstfret:").append(firstfret);
				for (FrameNote fn : framenotes)
				{
					sb.append(" ").append(fn.toString());
				}
			}
			return sb.toString();
		}
		private Frame(int strings, int frets, int firstfret, FrameNote[] framenotes){
			this.strings = strings;
			this.frets = frets;
			this.firstfret = firstfret;
			this.framenotes = framenotes;
		}
	}

	/**
	 * array of Chords
	 */
	public final Chord[] chords;
	
	/**
	 * Frame or null
	 */
	public final Frame frame;
	
	/**
	 * any MusicXML defined offset
	 */
	public float offset;

	/**
	 * Harmony_? value
	 */
	public final int type;
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (chords != null && chords.length > 0)
		{
			for (Chord chord : chords)
			{
				sb.append(chord.toString()).append(", ");
			}
		}
		if (frame != null)
			sb.append(" " + frame.toString());
		if (offset != 0)
			sb.append(" offset:").append(offset);
		switch(type)
		{
		default:
		case Harmony_unset: break;
		case Harmony_explicit: sb.append(" explicit type"); break;
		case Harmony_implied: sb.append(" implied type"); break;
		case Harmony_alternate: sb.append(" alternate type"); break;
		}
		return sb.toString();
	}
	private Harmony(Chord[] chords, Frame frame, float offset, int type){
		this.chords = chords;
		this.frame = frame;
		this.offset = offset;
		this.type = type;
	}
}