package com.joshschriever.livenotes.midi;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;

import java.io.IOException;
import java.util.Arrays;

import static java8.util.J8Arrays.stream;

public class MidiReceiver extends android.media.midi.MidiReceiver
        implements MidiManager.OnDeviceOpenedListener,
        MidiConstants {

    private android.media.midi.MidiReceiver listener;

    public MidiReceiver(Context context, android.media.midi.MidiReceiver listener) {
        this.listener = listener;
        MidiManager midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        stream(midiManager.getDevices())
                .filter(device -> device.getOutputPortCount() > 0).findFirst()
                .ifPresent(device -> midiManager.openDevice(device, this, null));
    }

    @Override
    public void onDeviceOpened(MidiDevice device) {
        stream(device.getInfo().getPorts())
                .filter(port -> port.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT).findFirst()
                .ifPresent(port -> device.openOutputPort(port.getPortNumber()).connect(this));
    }

    @Override
    public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
        for (int i = offset; i < offset + count; i++) {
            byte status = (byte) (msg[i] & STATUS_COMMAND_MASK);
            if (status == STATUS_NOTE_ON || status == STATUS_NOTE_OFF) {
                listener.send(Arrays.copyOfRange(msg, i, i + 3), 0, 3, timestamp);
                i += 2;
            }
        }
    }

}
