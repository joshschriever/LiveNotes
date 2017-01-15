/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

/**
 * The callback from SScore.layout which adds each SSystem to the layout
 */
public interface LayoutCallback
{
	/**
	 * Called during SScore.layout to notify that a new SSystem (ie layout of a range of bars) has been
	 * constructed and can be added to the view.
	 * 
	 * @param sys the new SSystem
	 * @return false to abort the layout
	 */
	public boolean addSystem(SSystem sys);
}
