package com.joshschriever.livenotes.midi;

public interface MidiConstants {

    byte STATUS_COMMAND_MASK = (byte) 0xF0;
    byte STATUS_NOTE_OFF = (byte) 0x80;
    byte STATUS_NOTE_ON = (byte) 0x90;
}
