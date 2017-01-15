/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * From MusicXML <sound> element - see MusicXML spec.
 * A value of 0 or null indicates that the parameter is undefined
 */
public class Sound
{
	public final int offset;
	public final int tempo;
	public final float dynamics;
	public final boolean dacapo;
	public final String segno;
	public final String dalsegno;
	public final String coda;
	public final String tocoda;
	public final boolean forward_repeat;
	public final float divisions;
	public final String fine;
	public final String timeonly;
	public final boolean pizz;
	public final String damper_pedal;
	public final String soft_pedal;
	public final String sostenuto_pedal;

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (offset != 0)
			sb.append(" offset:").append(offset);
		if (tempo != 0)
			sb.append(" tempo:").append(tempo);
		if (dynamics != 0)
			sb.append(" dynamics:").append(dynamics);
		if (dacapo)
			sb.append(" dacapo");
		if (segno != null)
			sb.append(" segno:").append(segno);
		if (dalsegno != null)
			sb.append(" dalsegno:").append(dalsegno);
		if (coda != null)
			sb.append(" coda:").append(coda);
		if (tocoda != null)
			sb.append(" tocoda:").append(tocoda);
		if (forward_repeat)
			sb.append(" forward_repeat");
		if (divisions != 0)
			sb.append(" divisions:").append(divisions);
		if (fine != null)
			sb.append(" fine:").append(fine);
		if (timeonly != null)
			sb.append(" timeonly:").append(timeonly);
		if (pizz)
			sb.append(" pizz");
		if (damper_pedal != null)
			sb.append(" damper_pedal:").append(damper_pedal);
		if (soft_pedal != null)
			sb.append(" soft_pedal:").append(soft_pedal);
		if (sostenuto_pedal != null)
			sb.append(" sostenuto_pedal:").append(sostenuto_pedal);
		return sb.toString();
	}
	
	private Sound(int offset, int tempo, float dynamics, boolean dacapo, String segno, String dalsegno, String coda,
			String tocoda, boolean forward_repeat, float divisions, String fine, String timeonly, boolean pizz,
			String damper_pedal, String soft_pedal, String sostenuto_pedal) {
		this.offset = offset;
		this.tempo = tempo;
		this.dynamics = dynamics;
		this.dacapo = dacapo;
		this.segno = segno;
		this.dalsegno = dalsegno;
		this.coda = coda;
		this.tocoda = tocoda;
		this.forward_repeat = forward_repeat;
		this.divisions = divisions;
		this.fine = fine;
		this.timeonly = timeonly;
		this.pizz = pizz;
		this.damper_pedal = damper_pedal;
		this.soft_pedal = soft_pedal;
		this.sostenuto_pedal = sostenuto_pedal;
	}
}