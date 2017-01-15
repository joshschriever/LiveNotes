/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * Define options when loading the score
 */
public class LoadOptions {

	/**
	 * Construct
	 * 
	 * @param key the key obtained from Dolphin Computing defining which features are available
	 * @param check_xml when set returns info about inconsistencies in the score in SScore.getLoadWarnings()
	 */
	public LoadOptions(SScoreKey key, boolean check_xml)
	{
		this.key = key;
		this.check_xml = check_xml;
	}

	final SScoreKey key;
	final boolean check_xml;
}

