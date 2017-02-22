package com.joshschriever.livenotes.musicxml;

import org.jfugue.Note;
//TODO - make my own note class that has duration, timeStamp, isRest, isTieStart, isTieEnd, xmlString, isDotted
//TODO - have noteEvent take the base note, and a list of notes tied to it (all in the same measure)
//TODO --- the base note will be inserted normally, and the tied notes positioned by their timeStamp
public interface SimpleParserListener {

    void noteEvent(Note note, long timeStamp);

    void measureEvent();
}
