/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore.playdata;

/**
 * A note which contains all information required to play it
 */
public class Note
{
	/**
	 * not grace note type
	 */
	public static final int Grace_No = 0;
	
	/**
	 * appoggiatura grace type
	 */
	public static final int Grace_Appoggiatura = 1;
	
	/**
	 * acciaccatura grace type
	 */
	public static final int Grace_Acciaccatura = 2;

	/**
	 * 60 = C4. 0 = unpitched (ie percussion or metronome)
     * For the metronome part this is the beat index
	 */
	public final int midiPitch;

	/**
	 * index of part containing  this note
	 */
	public final int partIndex;

	/**
	 * index of bar in which this note starts (may be tied)
	 */
	public final int startBarIndex;

	/**
	 * start time from start of bar (milliseconds)
	 */
	public final int start;
	
	/**
	 * (ms) may be longer than a bar if tied
	 */
	public final int duration;
	
	/**
	 * [0..100+] value of the last dynamic
	 * may exceed 100 for ff - see PlayData.maxSoundDynamic
	 */
	public final int dynamic;
	
	/**
	 * Grace_* set for grace note
	 */
	public final int grace;
	
	/**
	 * item handle used in sscore_contents
	 */
	public final int item_h;

	/**
	 * midi start time (24 ticks per crotchet/quarter note)
	 */
	public final int midi_start;

	/**
	 * midi duration (24 ticks per crotchet/quarter note)
	 */
	public final int midi_duration;

	/**
	 * index of the staff containing this note. 0 is top (or only) staff
	 */
	public final int staffindex;

	/**
	 * true if note has a staccato dot
	 */
	public final boolean staccato;

	/**
	 * true if note has an accent
	 */
	public final boolean accent;

	/**
	 * true if note is in a section marked pizz.
	 */
	public final boolean pizzicato;

	/**
	 * true if this 'note' is in fact a rest
	 */
	public final boolean rest;

	/**
	 * true if this note is tied to a following note
	 */
	public final boolean tied;

	/**
	 * true if this is a percussion note
	 */
	public final boolean percussion;

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Note");
		sb.append(" midiPitch:").append(midiPitch);
		sb.append(" partIndex:").append(partIndex);
		sb.append(" startBarIndex:").append(startBarIndex);
		sb.append(" start:").append(start);
		sb.append(" duration:").append(duration);
		sb.append(" dynamic:").append(dynamic);
		if (grace != Grace_No)
		{
			switch (grace)
			{
			case Grace_Appoggiatura: sb.append("appoggiatura grace");break;
			case Grace_Acciaccatura: sb.append("acciaccatura grace");break;
			}
		}
		sb.append(" item_h:").append(item_h);
		return sb.toString();
	}
	private Note(int m, int pindex, int sbi, int s, int dur, int dyn, int g, int item_h,
				 int mid_start, int mid_dur, int staffi, int articulation_flags) {
		this.midiPitch = m;
		this.partIndex = pindex;
		this.startBarIndex = sbi;
		this.start = s;
		this.duration = dur;
		this.dynamic = dyn;
		this.grace = g;
		this.item_h = item_h;
		this.midi_start = mid_start;
		this.midi_duration = mid_dur;
		this.staffindex = staffi;
		/*
		sscore_pd_artic_staccato_flag = 1,
		sscore_pd_artic_accent_flag = 1<<1,
		sscore_pd_artic_pizz_flag = 1<<2,
		sscore_pd_artic_rest_flag = 1<<3,
		sscore_pd_artic_tied_flag = 1<<4,
		sscore_pd_artic_percussion_flag = 1<<5
		 */
		this.staccato = (articulation_flags & 1) > 0;
		this.accent = (articulation_flags & 2) > 0;
		this.pizzicato = (articulation_flags & 4) > 0;
		this.rest = (articulation_flags & 8) > 0;
		this.tied = (articulation_flags & 16) > 0;
		this.percussion = (articulation_flags & 32) > 0;
	}
}