package com.joshschriever.livenotes.midi;

import org.jfugue.MidiMessageRecipient;

import java.io.IOException;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.ShortMessage;

public class MidiMessageAdapter extends CloseableReceiver implements MidiConstants {

    private MidiMessageRecipient messageRecipient;

    public MidiMessageAdapter(MidiMessageRecipient messageRecipient) {
        this.messageRecipient = messageRecipient;
    }

    @Override
    public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
        final byte status = (byte) (msg[0] & STATUS_COMMAND_MASK);
        if (status == STATUS_NOTE_ON || status == STATUS_NOTE_OFF) {
            try {
                messageRecipient.messageReady(new ShortMessage(status == STATUS_NOTE_ON
                                                               ? ShortMessage.NOTE_ON
                                                               : ShortMessage.NOTE_OFF,
                                                               msg[1] - 12,
                                                               msg[2]),
                                              timestamp / 1000000);
            } catch (InvalidMidiDataException e) {
                throw new IOException(e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public void close() throws IOException {
    }

}
