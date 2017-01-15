/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */
package uk.co.dolphin_com.sscore.playdata;

/**
 * An interface passed to {link PlayData} allowing the user to set the tempo and the PlayData to calculate
 * note timings
 */
public interface UserTempo
{
	/**
	 * Get a user-defined beats per minute tempo value when it is not defined by a metronome marking or sound tempo in the score.
	 * <p>If PlayData.hasDefinedTempo() returns false this method is used by the SeeScoreLib.
	 * 
	 * @return the user-defined tempo [10 .. 360]
	 */
	public int getUserTempo();

	/**
	 * Get a user-defined scaling for tempo values defined in the score.
	 * <p>If PlayData.hasDefinedTempo() returns true this method is used by the SeeScoreLib.
	 * 
	 * @return the user-defined scaling for the tempo [0.1 .. 10]
	 */
	public float getUserTempoScaling();
}