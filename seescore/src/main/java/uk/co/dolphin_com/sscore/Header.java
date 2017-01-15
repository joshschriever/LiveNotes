/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates the MusicXML Score Header information
 * 
 */
public class Header {
	
	/**
	 * The MusicXML <score-header><work><work-number> element
	 */
	public final String work_number;

	/**
	 * The MusicXML <score-header><work><work-title> element
	 */
	public final String work_title;

	/**
	 * The MusicXML <movement-number> element
	 */
	public final String movement_number;
	
	/**
	 * The MusicXML <movement-title> element
	 */
	public final String movement_title;

	/**
	 * The contents of any "composer" field in the MusicXML <identification><creator> element
	 */
	public final String composer;

	/**
	 * The contents of any "lyricist" field in the MusicXML <identification><creator> element
	 */
	public final String lyricist;

	/**
	 * The contents of any "arranger" field in the MusicXML <identification><creator> element
	 */
	public final String arranger;
	
	/**
	 * The MusicXML <credit><credit-words> elements
	 */
	public final List<String> credit_words;
	
	/**
	 * The MusicXML <part-list><score-part><part-name> elements
	 */
	public final List<PartName> partnames;
	
	public String toString()
	{
		String rval = " work-number:" + work_number + "\n"
				+ " work_title:" + work_title + "\n"
				+ " movement_number:" + movement_number + "\n"
				+ " movement_title:" + movement_title + "\n"
				+ " composer:" + composer + "\n"
				+ " lyricist:" + lyricist + "\n"
				+ " arranger:" + arranger + "\n";
		rval += "credits:{";
		for (String cw : credit_words)
		{
			rval += cw + ", ";
		}
		rval += "}\nparts:{";
		for (PartName p : partnames)
		{
			rval += " part:" + p + ", ";
		}
		rval += "}";
		return rval;
	}
	
	Header(String workn, String workt, String movn, String movt,
			String comp, String lyr, String arr,
			String[] creditw,
			PartName[] partnames)
	{
		this.work_number = workn;
		this.work_title = workt;
		this.movement_number = movn;
		this.movement_title = movt;
		this.composer = comp;
		this.lyricist = lyr;
		this.arranger = arr;
		this.credit_words = Arrays.asList(creditw);
		this.partnames = Arrays.asList(partnames);
	}
}
