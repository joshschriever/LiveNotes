package com.joshschriever.livenotes.musicxml;

import android.util.SparseArray;

import java.util.List;

import java8.lang.Integers;
import java8.util.Optional;
import java8.util.Spliterator;
import java8.util.Spliterators.AbstractSpliterator;
import java8.util.function.Consumer;
import java8.util.function.Predicate;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;
import nu.xom.Attribute;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

// Based on JFugue
public class MusicXmlRenderer implements SimpleParserListener {

    private static final int ONE_MINUTE = 60_000;
    private static final int DIVISIONS_PER_BEAT = 4;

    private static final String[] NOTES = new String[]
            {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"};
    private static final SparseArray<String> BEAT_UNIT_STRINGS = new SparseArray<>(3);

    static {
        BEAT_UNIT_STRINGS.put(2, "half");
        BEAT_UNIT_STRINGS.put(4, "quarter");
        BEAT_UNIT_STRINGS.put(8, "eighth");
    }

    private Document document;
    private Element elRoot;
    private Element elPart;
    private Element elCurMeasure;

    private int beatsPerMeasure;
    private int beatType;
    private int markedTempo;
    private int actualBeatsTempo;

    public MusicXmlRenderer(int beatsPerMeasure, int beatType, int tempo) {
        this.beatsPerMeasure = beatsPerMeasure;
        this.beatType = beatType;
        markedTempo = tempo;
        actualBeatsTempo = markedTempo * (isTimeSignatureCompound() ? 3 : 1);

        elRoot = new Element("score-partwise");
        Element elID = new Element("identification");
        Element elCreator = new Element("creator");
        elCreator.addAttribute(new Attribute("type", "software"));
        elCreator.appendChild("Live Notes");
        elID.appendChild(elCreator);
        elRoot.appendChild(elID);

        document = new Document(elRoot);
        document.insertChild(new DocType("score-partwise",
                                         "-//Recordare//DTD MusicXML 1.1 Partwise//EN",
                                         "http://www.musicxml.org/dtds/partwise.dtd"),
                             0);

        elRoot.appendChild(new Element("part-list"));
        doFirstMeasure();
    }

    //TODO - remove all occurrences of timeStamp attribute
    public String getMusicXMLString() {
        return document.toXML().replaceAll("<duration>0</duration>",
                                           "<duration>1</duration>");
    }

    public void removeTrailingEmptyMeasures() {
        streamInReverse(allMeasures())
                .takeWhile(elMeasure -> elMeasure.getChildCount() == 0
                        || stream(elMeasure.getChildElements("note"))
                        .allMatch(elNote -> elNote.getFirstChildElement("rest") != null))
                .forEach(elMeasure -> elPart.removeChild(elMeasure));
    }

    private void doFirstMeasure() {
        if (elPart == null) {
            Element elScorePart = new Element("score-part");
            elScorePart.addAttribute(new Attribute("id", "V0"));
            elScorePart.appendChild(new Element("part-name"));
            elRoot.getFirstChildElement("part-list").appendChild(elScorePart);

            elPart = new Element("part");
            elPart.addAttribute(new Attribute("id", "V0"));
            elRoot.appendChild(elPart);
        }

        if (elCurMeasure == null) {
            elCurMeasure = new Element("measure");
            elCurMeasure.addAttribute(new Attribute("number", "1"));

            Element elAttributes = new Element("attributes");
            Element elDivisions = new Element("divisions");
            elDivisions.appendChild(Integer.toString(DIVISIONS_PER_BEAT));
            elAttributes.appendChild(elDivisions);

            Element elKey = new Element("key");
            Element elFifths = new Element("fifths");
            elFifths.appendChild("0");
            elKey.appendChild(elFifths);
            Element elMode = new Element("mode");
            elMode.appendChild("major");
            elKey.appendChild(elMode);
            elAttributes.appendChild(elKey);

            Element elTime = new Element("time");
            Element elBeats = new Element("beats");
            elBeats.appendChild(Integer.toString(beatsPerMeasure));
            elTime.appendChild(elBeats);
            Element elBeatType = new Element("beat-type");
            elBeatType.appendChild(Integer.toString(beatType));
            elTime.appendChild(elBeatType);
            elAttributes.appendChild(elTime);

            Element elStaves = new Element("staves");
            elStaves.appendChild("2");
            elAttributes.appendChild(elStaves);
            elAttributes.appendChild(clefElementFrom(1, "G", 2));
            elAttributes.appendChild(clefElementFrom(2, "F", 4));

            elCurMeasure.appendChild(elAttributes);

            Element elDirection = new Element("direction");
            elDirection.addAttribute(new Attribute("placement", "above"));
            Element elDirectionType = new Element("direction-type");
            Element elMetronome = new Element("metronome");

            Element elBeatUnit = new Element("beat-unit");
            elBeatUnit.appendChild(getBeatUnitString());
            elMetronome.appendChild(elBeatUnit);
            if (isTimeSignatureCompound()) {
                elMetronome.appendChild(new Element("beat-unit-dot"));
            }

            Element elPerMinute = new Element("per-minute");
            elPerMinute.appendChild(Integer.toString(markedTempo));
            elMetronome.appendChild(elPerMinute);

            elDirectionType.appendChild(elMetronome);
            elDirection.appendChild(elDirectionType);
            elCurMeasure.appendChild(elDirection);

            elPart.appendChild(elCurMeasure);
        }
    }

    private Element clefElementFrom(int number, String sign, int line) {
        Element elClef = new Element("clef");
        elClef.addAttribute(new Attribute("number", Integer.toString(number)));
        Element elSign = new Element("sign");
        elSign.appendChild(sign);
        elClef.appendChild(elSign);
        Element elLine = new Element("line");
        elLine.appendChild(Integer.toString(line));
        elClef.appendChild(elLine);
        return elClef;
    }

    private String getBeatUnitString() {
        return BEAT_UNIT_STRINGS.get(beatType / (isTimeSignatureCompound() ? 2 : 1));
    }

    private boolean isTimeSignatureCompound() {
        return ((beatsPerMeasure % 3) == 0) && ((beatsPerMeasure / 3) > 1);
    }

    @Override
    public void measureEvent() {
        int nextNumber = Integer.parseInt(allMeasures().get(allMeasures().size() - 1)
                                                       .getAttributeValue("number")) + 1;

        elCurMeasure = new Element("measure");
        elCurMeasure.addAttribute(new Attribute("number", Integer.toString(nextNumber)));
        elPart.appendChild(elCurMeasure);
    }

    //TODO - handle tiedNotes
    //TODO - save timeStamp as attribute on notes and use it to put tied notes in correct positions
    //TODO - handle chords - compare timeStamp to timeStamp attributes on notes
    //TODO --- to mark a chord, insert new Element("chord") as the first child of the note element
    @Override
    public void noteEvent(Note note, List<Note> tiedNotes) {
        doNote(note);
    }

    private void doNote(Note note) {
        Element elNote = new Element("note");

        int iAlter = alterForNoteValue(note.value);
        if (note.isRest) {
            elNote.appendChild(new Element("rest"));
        } else {
            Element elPitch = new Element("pitch");
            Element elStep = new Element("step");
            elStep.appendChild(stepForNoteValue(note.value));
            elPitch.appendChild(elStep);

            if (iAlter != 0) {
                Element elAlter = new Element("alter");
                elAlter.appendChild(Integer.toString(iAlter));
                elPitch.appendChild(elAlter);
            }

            Element elOctave = new Element("octave");
            elOctave.appendChild(octaveForNoteValue(note.value));
            elPitch.appendChild(elOctave);
            elNote.appendChild(elPitch);
        }

        int iXMLDuration = note.durationMillis == 0L
                           ? 0
                           : Integers.max(note.isRest ? 0 : 1,
                                          (int) (note.durationMillis * DIVISIONS_PER_BEAT
                                                  * actualBeatsTempo / ONE_MINUTE));

        Element elDuration = new Element("duration");
        elDuration.appendChild(Integer.toString(iXMLDuration));
        elNote.appendChild(elDuration);

        boolean bTied = false;
        if (!note.isRest) {
            if (note.isEndOfTie) {
                Element elTieStop = new Element("tie");
                elTieStop.addAttribute(new Attribute("type", "stop"));
                elNote.appendChild(elTieStop);
                bTied = true;
            }
            if (note.isStartOfTie) {
                Element elTieStart = new Element("tie");
                elTieStart.addAttribute(new Attribute("type", "start"));
                elNote.appendChild(elTieStart);
                bTied = true;
            }
        }

        Element elType = new Element("type");
        elType.appendChild(XMLDurationMap.noteStringForDuration(iXMLDuration, beatType));
        elNote.appendChild(elType);
        if (XMLDurationMap.noteDottedForDuration(iXMLDuration, beatType)) {
            elNote.appendChild(new Element("dot"));
        }

        if (iAlter != 0) {
            Element elAccidental = new Element("accidental");
            elAccidental.appendChild(iAlter == 1 ? "sharp" : "flat");
            elNote.appendChild(elAccidental);
        }

        int iStaff = note.value >= 48 ? 1 : 2;
        Element elStaff = new Element("staff");
        elStaff.appendChild(Integer.toString(iStaff));
        elNote.appendChild(elStaff);

        if (bTied) {
            Element elNotations = new Element("notations");

            if (note.isEndOfTie) {
                Element elTiedStop = new Element("tied");
                elTiedStop.addAttribute(new Attribute("type", "stop"));
                elNotations.appendChild(elTiedStop);
            }
            if (note.isStartOfTie) {
                Element elTiedStart = new Element("tied");
                elTiedStart.addAttribute(new Attribute("type", "start"));
                elNotations.appendChild(elTiedStart);
            }

            elNote.appendChild(elNotations);
        }

        if (note.isRest) {
            if (iXMLDuration == 0) {
                firstNoteThatMatches(restMatches(iStaff, 0))
                        .ifPresentOrElse(elOldNote -> elOldNote.getParent().removeChild(elOldNote),
                                         () -> elCurMeasure.appendChild(elNote));
            } else {
                firstNoteThatMatches(restMatches(iStaff, 0))
                        .ifPresent(elOldNote -> elOldNote.getParent()
                                                         .replaceChild(elOldNote, elNote));
            }
        } else {
            if (iXMLDuration == 0) {
                elCurMeasure.appendChild(elNote);
            } else {
                firstNoteThatMatches(noteMatches(note.value, 0))
                        .ifPresent(elOldNote -> elOldNote.getParent()
                                                         .replaceChild(elOldNote, elNote));
            }
        }
    }

    private static String stepForNoteValue(int value) {
        return NOTES[value % 12].substring(0, 1);
    }

    private static int alterForNoteValue(int value) {
        String pitch = NOTES[value % 12];
        return pitch.length() > 1 ? pitch.contains("#") ? 1 : -1 : 0;
    }

    private static String octaveForNoteValue(int value) {
        return Integer.toString(value / 12);
    }

    private static Predicate<Element> restMatches(int staff, int duration) {
        return elNote -> elNote.getFirstChildElement("rest") != null
                && elNote.getFirstChildElement("staff").getValue().equals(Integer.toString(staff))
                && elNote.getFirstChildElement("duration")
                         .getValue().equals(Integer.toString(duration));
    }

    private static Predicate<Element> noteMatches(int value, int duration) {
        return elNote -> elNote.getFirstChildElement("rest") == null
                && pitchMatches(elNote.getFirstChildElement("pitch"), value)
                && elNote.getFirstChildElement("duration")
                         .getValue().equals(Integer.toString(duration));
    }

    private static boolean pitchMatches(Element elPitch, int value) {
        return elPitch.getFirstChildElement("step").getValue()
                      .equals(stepForNoteValue(value))
                && elPitch.getFirstChildElement("octave").getValue()
                          .equals(octaveForNoteValue(value))
                && alterMatches(elPitch.getFirstChildElement("alter"), value);
    }

    private static boolean alterMatches(Element elAlter, int value) {
        int alter = alterForNoteValue(value);
        return alter == 0 ? elAlter == null
                          : elAlter != null && elAlter.getValue().equals(Integer.toString(alter));
    }

    private Optional<Element> firstNoteThatMatches(Predicate<Element> predicate) {
        return stream(allMeasures()).flatMap(measure -> stream(measure.getChildElements("note")))
                                    .filter(predicate).findFirst();
    }

    private Elements allMeasures() {
        return elPart.getChildElements("measure");
    }

    private static Stream<Element> stream(Elements elements) {
        return StreamSupport.stream(new ElementsSpliterator(elements), false);
    }

    private static Stream<Element> streamInReverse(Elements elements) {
        return StreamSupport.stream(new ReverseElementsSpliterator(elements), false);
    }

    private static class ElementsSpliterator extends AbstractSpliterator<Element> {

        Elements elements;
        int current;

        ElementsSpliterator(Elements elements) {
            super(elements.size(), Spliterator.ORDERED | Spliterator.SIZED | Spliterator.IMMUTABLE);
            this.elements = elements;
            current = 0;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Element> action) {
            if (current < elements.size()) {
                action.accept(elements.get(current));
                current++;
                return true;
            } else {
                return false;
            }
        }
    }

    private static class ReverseElementsSpliterator extends ElementsSpliterator {

        private ReverseElementsSpliterator(Elements elements) {
            super(elements);
            current = elements.size() - 1;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Element> action) {
            if (current >= 0) {
                action.accept(elements.get(current));
                current--;
                return true;
            } else {
                return false;
            }
        }
    }

}
