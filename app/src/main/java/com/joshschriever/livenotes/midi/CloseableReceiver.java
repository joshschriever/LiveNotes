package com.joshschriever.livenotes.midi;

import android.media.midi.MidiReceiver;

import java.io.Closeable;

public abstract class CloseableReceiver extends MidiReceiver implements Closeable {

}
