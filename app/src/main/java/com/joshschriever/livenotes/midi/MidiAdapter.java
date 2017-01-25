package com.joshschriever.livenotes.midi;

import android.media.midi.MidiReceiver;

import org.jfugue.MidiMessageRecipient;

import java.io.IOException;

public class MidiAdapter extends MidiReceiver {

    private MidiMessageRecipient messageRecipient;

    public MidiAdapter(MidiMessageRecipient messageRecipient) {
        this.messageRecipient = messageRecipient;
    }

    @Override
    public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
        //TODO - create MidiMessage and send to messageRecipient
    }

}
