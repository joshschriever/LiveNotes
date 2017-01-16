/**
 * SeeScore For Android
 * Dolphin Computing http://www.dolphin-com.co.uk
 */
/* SeeScoreLib Key for evaluation

 IMPORTANT! This file is for evaluation only.
 It must be used only for the application for which it is licensed,
 and must not be released to any other individual or company.
 */

package uk.co.dolphin_com.seescoreandroid;

import uk.co.dolphin_com.sscore.SScoreKey;

/**
 * The licence key to enable features in SeeScoreLib supplied by Dolphin Computing
 */

public class LicenceKeyInstance
{
    // licence keys: contents, transpose, play_data, item_colour, multipart, android, midi_out
    private static final int[] keycap = {0X1044b4,0X0};
    private static final int[] keycode = {0X25a648b9,0X47f07cfe,0Xab211079,0X7d9e2c97,0X1b81adef,0Xc966ff4,0X5ec23404,0X1fe52336,0X8394c0e5,0X7c6abe95,0X44066803,0Xe9c9c6fc,0X96bfd9,0X290d3fdc,0Xf0965ce};

    public static final SScoreKey SeeScoreLibKey = new SScoreKey("evaluation", keycap, keycode);
}
