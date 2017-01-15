/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * Define a warning about any problem discovered during load
 */
public class LoadWarning {

	public static enum W
	{
		/**
		 *  no warning !
		 */
		none,
		
		/**
		 * <accidental> or <stem> elements are not defined in the file and will be reconstructed by SeeScore
		 */
		missingelement,
		
		/**
		 * different parts do not have the same number of bars (measures)
		 */
	    measurecount,
	    
	    /**
	     * a note has the wrong number of beams defined for the note type
	     */
	    beamcount,

		/**
		 * an error in ordering of beam,slur,tied,tuplet begin/start,continue,end/stop
		 */
		consistency,

		/**
		 * beam/slur/tie/tuplet.. 2nd start without stop
		 */
		unexpectedStart,

	    /**
	     * beam/slur continue without start
	     */
	    unexpectedContinue,
	    
	    /**
	     * beam/slur/tie/tuplet stop without start
	     */
	    unexpectedStop,
	    
	    /**
	     * beam/slur/tied/tuplet unclosed at end of score/measure
	     */
	    unclosed,

		/**
		 * half of paired type (start/stop) is missing or extra
		 */
		unpaired,

		/**
		 * level number is > 6
		 */
		badlevel,
		
		/**
		 * accidental does not agree with note pitch value
		 */
		badAccidental,
		
		/**
		 * the pitch needs an accidental but there isn't one
		 */
		noAccidental
	}

	/**
	 * Define the type of element with the problem
	 */
	public static enum E
	{
	    none,
	    beam,
	    slur,
	    tied,
		lyric,
		bracket,
	    tuplet,
		slide,
		glissando,
		wedge,
		dashes,
		pedal,
		octave_shift,
		principal_voice,
	    part,
		accidental,
		stem,
		tremolo,
		unknown
	};

	/**
	 * The warning type
	 */
	public final W warning;
	
	/**
	 * The index of the part containing the problem -- -1 for all parts
	 */
	public final int partIndex;
	
	/**
	 * The index of the bar containing the problem -- -1 for all bars
	 */
	public final int barIndex;
	
	/**
	 * The type of element which has a problem
	 */
	public final E element;

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(" warning:");
		switch(warning)
		{
		case none:break;
		case missingelement: buf.append("missingelement");break;
		case measurecount: buf.append("measurecount");break;
		case beamcount: buf.append("beamcount");break;
		case unexpectedStart: buf.append("unexpectedStart");break;
		case unexpectedContinue: buf.append("unexpectedContinue");break;
		case unexpectedStop: buf.append("unexpectedStop");break;
		case unclosed: buf.append("unclosed");break;
		}
		buf.append(" partIndex:").append(partIndex);
		buf.append(" barIndex:").append(barIndex);
		buf.append(" element:");
		switch (element)
		{
		case none:break;
		case beam: buf.append("beam");break;
		case slur: buf.append("slur");break;
		case tied: buf.append("tied");break;
		case tuplet: buf.append("tuplet");break;
		case part: buf.append("part");break;
		case accidental: buf.append("accidental");break;
		case stem: buf.append("stem");break;
		}
		return buf.toString();
	}


	private LoadWarning()
	{
		warning = W.none;
		element = E.none;
		partIndex = 0;
		barIndex = 0;
	}
	private LoadWarning(int w, int p, int b, int e)
	{
		switch (w)
		{
		default:
		case 0: warning = W.none;break;
		case 1: warning = W.missingelement;break;
		case 2: warning = W.measurecount;break;
		case 3: warning = W.beamcount;break;
		case 4: warning = W.unexpectedStart;break;
		case 5: warning = W.unexpectedContinue;break;
		case 6: warning = W.unexpectedStop;break;
		case 7: warning = W.unclosed;break;
		}
		switch (e)
		{
		default:
		case 0: element = E.none;break;
		case 1: element = E.beam;break;
		case 2: element = E.slur;break;
		case 3: element = E.tied;break;
		case 4: element = E.tuplet;break;
		case 5: element = E.part;break;
		case 6: element = E.accidental;break;
		case 7: element = E.stem;break;
		}
		partIndex = p;
		barIndex = b;
	}
}
