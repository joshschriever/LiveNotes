package com.joshschriever.livenotes.midi;

import java.io.IOException;

public class MidiMessageAdapter extends CloseableReceiver implements MidiConstants {

    private AdaptedMessageRecipient messageRecipient;

    public MidiMessageAdapter(AdaptedMessageRecipient messageRecipient) {
        this.messageRecipient = messageRecipient;
    }

    @Override
    public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
        final byte status = (byte) (msg[0] & STATUS_COMMAND_MASK);
        if (status == STATUS_NOTE_ON || status == STATUS_NOTE_OFF) {
            messageRecipient.messageReady(new AdaptedMidiMessage(status, msg[1] - 12),
                                          timestamp / 1000000);
        }
    }

    @Override
    public void close() throws IOException {
    }

}
