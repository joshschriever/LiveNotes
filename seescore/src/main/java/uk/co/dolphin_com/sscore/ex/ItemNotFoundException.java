/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore.ex;

@SuppressWarnings("serial")
public class ItemNotFoundException extends ScoreException
{
	public ItemNotFoundException(String detail)
	{
		super(detail);
	}
}