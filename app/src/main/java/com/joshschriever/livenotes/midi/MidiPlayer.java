package com.joshschriever.livenotes.midi;

import org.billthefarmer.mididriver.MidiDriver;

import java.io.IOException;

public class MidiPlayer extends CloseableReceiver implements MidiConstants {

    private MidiDriver player;

    public MidiPlayer() {
        player = new MidiDriver();
        player.start();
    }

    @Override
    public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
        final byte status = (byte) (msg[0] & STATUS_COMMAND_MASK);
        if (status == STATUS_NOTE_ON || status == STATUS_NOTE_OFF) {
            player.queueEvent(new byte[] {msg[0], msg[1], 127});
        }
    }

    @Override
    public void close() throws IOException {
        player.stop();
    }

}
