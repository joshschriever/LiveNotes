package com.joshschriever.livenotes.midi;

public interface AdaptedMessageRecipient {

    void messageReady(AdaptedMidiMessage message, long timeStamp);
}
