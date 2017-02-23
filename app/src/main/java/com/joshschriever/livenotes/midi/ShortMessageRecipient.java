package com.joshschriever.livenotes.midi;

import jp.kshoji.javax.sound.midi.ShortMessage;

public interface ShortMessageRecipient {

    void messageReady(ShortMessage midiMessage, long timeStamp);
}
