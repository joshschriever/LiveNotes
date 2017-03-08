package com.joshschriever.livenotes.midi;

public class AdaptedMidiMessage implements MidiConstants {

    public final int command;
    public final int data;

    public AdaptedMidiMessage(int command, int data) {
        this.command = command;
        this.data = data;
    }
}
