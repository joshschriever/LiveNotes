package com.joshschriever.livenotes.musicxml;

import java.util.List;

public interface SimpleParserListener {

    void noteEvent(Note note, List<Note> tiedNotes);

    void measureEvent();
}
