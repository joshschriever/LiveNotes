package uk.co.dolphin_com.sscore;

import java.text.NumberFormat;

/**
 * The library version information from SScore.getVersion()
 */
public class Version
{
	/**
	 * 1..
	 */
	public final int hi;
	
	/**
	 * 0 - 99
	 */
	public final int lo;

	public String toString()
	{
		StringBuilder b = new StringBuilder();
		b.append(hi);
		b.append('.');
		if (lo < 10)
			b.append('0');
		b.append(lo);
		return b.toString();
	}

	private Version(int hi, int lo) {
		this.hi = hi;
		this.lo = lo;
	}
}