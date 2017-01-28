package com.joshschriever.livenotes.midi;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MidiDispatcher extends CloseableReceiver implements MidiConstants {

    private List<CloseableReceiver> receivers;

    public MidiDispatcher(CloseableReceiver... receivers) {
        this.receivers = Arrays.asList(receivers);
    }

    @Override
    public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
        for (CloseableReceiver receiver : receivers) {
            receiver.send(msg, offset, count, timestamp);
        }
    }

    @Override
    public void close() throws IOException {
        for (CloseableReceiver receiver : receivers) {
            receiver.close();
        }
    }

}
