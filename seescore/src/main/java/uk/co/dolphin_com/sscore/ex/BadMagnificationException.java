/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore.ex;

@SuppressWarnings("serial")
public class BadMagnificationException extends ScoreException
{
	public BadMagnificationException(String detail)
	{
		super(detail);
	}
}