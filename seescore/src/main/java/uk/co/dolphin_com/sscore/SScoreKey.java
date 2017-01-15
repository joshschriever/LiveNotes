/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * Define the licence key for SeeScore Library feature access
 */
public class SScoreKey
{
	/**
	 * Construct the licence key
	 * 
	 * @param identity key owner identity
	 * @param capabilities encoded capabilities for this identity
	 * @param key key associated with identity and capabilities
	 */
	public SScoreKey(String identity, int capabilities[], int key[])
	{
		this.ident = identity;
		this.cap = capabilities;
		this.key = key;
	}

	final String ident;
	final int cap[];
	final int key[];
}
