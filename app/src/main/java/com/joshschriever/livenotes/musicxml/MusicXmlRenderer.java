package com.joshschriever.livenotes.musicxml;

import android.util.SparseArray;

import org.jfugue.Note;
import org.jfugue.ParserListenerAdapter;
import org.jfugue.Voice;

import java8.lang.Integers;
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

// Forked from JFugue
public class MusicXmlRenderer extends ParserListenerAdapter {

    private static final int ONE_MINUTE = 60_000;
    private static final int DIVISIONS_PER_BEAT = 4;

    private static final SparseArray<String> BEAT_UNIT_STRINGS = new SparseArray<>(5);

    static {
        BEAT_UNIT_STRINGS.put(2, "half");
        BEAT_UNIT_STRINGS.put(4, "quarter");
        BEAT_UNIT_STRINGS.put(8, "eighth");
    }

    private Document document;
    private Element root = new Element("score-partwise");
    private Element elCurMeasure;
    private Element elCurPart;

    private int beatsPerMeasure;
    private int beatType;
    private int markedTempo;
    private int actualBeatsTempo;

    public MusicXmlRenderer(int beatsPerMeasure, int beatType, int tempo) {
        Element elID = new Element("identification");
        Element elCreator = new Element("creator");
        elCreator.addAttribute(new Attribute("type", "software"));
        elCreator.appendChild("JFugue MusicXMLRenderer");
        elID.appendChild(elCreator);
        root.appendChild(elID);
        root.appendChild(new Element("part-list"));
        document = new Document(root);
        document.insertChild(new DocType("score-partwise",
                                         "-//Recordare//DTD MusicXML 1.1 Partwise//EN",
                                         "http://www.musicxml.org/dtds/partwise.dtd"),
                             0);

        this.beatsPerMeasure = beatsPerMeasure;
        this.beatType = beatType;
        markedTempo = tempo;
        actualBeatsTempo = markedTempo * (isTimeSignatureCompound() ? 3 : 1);

        doFirstMeasure(true);
    }

    public String getMusicXMLString() {
        return getInternalMusicXMLString().replaceAll("<duration>0</duration>",
                                                      "<duration>1</duration>");
    }

    private String getInternalMusicXMLString() {
        return getMusicXMLDoc().toXML();
    }

    private Document getMusicXMLDoc() {
        finishCurrentVoice();
        Elements elDocParts = root.getChildElements("part");

        for (int xomDoc = 0; xomDoc < elDocParts.size(); ++xomDoc) {
            Element docType = elDocParts.get(xomDoc);
            Elements elPartMeasures = docType.getChildElements("measure");

            for (int xM = 0; xM < elPartMeasures.size(); ++xM) {
                if (elPartMeasures.get(xM).getChildCount() < 1) {
                    docType.removeChild(xM);
                }
            }
        }

        return document;
    }

    private void doFirstMeasure(boolean bAddDefaults) {
        if (elCurPart == null) {
            newVoice(new Voice((byte) 0));
        }

        if (elCurMeasure == null) {
            elCurMeasure = new Element("measure");
            elCurMeasure.addAttribute(new Attribute("number", "1"));

            if (bAddDefaults) {
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

                doTempo(markedTempo);
            }
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

    private void finishCurrentVoice() {
        String sCurPartID = elCurPart == null ? null : elCurPart.getAttribute("id").getValue();
        boolean bCurVoiceExists = false;
        Elements elParts = root.getChildElements("part");
        Element elExistingCurPart = null;

        for (int x = 0; x < elParts.size(); ++x) {
            Element elP = elParts.get(x);
            String sPID = elP.getAttribute("id").getValue();
            if (sPID.compareTo(sCurPartID) == 0) {
                bCurVoiceExists = true;
                elExistingCurPart = elP;
            }
        }

        if (elCurPart != null) {
            finishCurrentMeasure();
            if (bCurVoiceExists) {
                root.replaceChild(elExistingCurPart, elCurPart);
            } else {
                root.appendChild(elCurPart);
            }
        }
    }

    private void newVoice(Voice voice) {
        Element elScorePart = new Element("score-part");
        Attribute atPart = new Attribute("id", voice.getMusicString());
        elScorePart.addAttribute(atPart);
        elScorePart.appendChild(new Element("part-name"));
        Element elPL = root.getFirstChildElement("part-list");
        elPL.appendChild(elScorePart);
        elCurPart = new Element("part");
        Attribute atPart2 = new Attribute(atPart);
        elCurPart.addAttribute(atPart2);
        elCurMeasure = null;
        doFirstMeasure(true);
    }

    private void doTempo(int tempo) {
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
        elPerMinute.appendChild(Integer.toString(tempo));
        elMetronome.appendChild(elPerMinute);

        elDirectionType.appendChild(elMetronome);
        elDirection.appendChild(elDirectionType);
        elCurMeasure.appendChild(elDirection);
    }

    private String getBeatUnitString() {
        return BEAT_UNIT_STRINGS.get(beatType / (isTimeSignatureCompound() ? 2 : 1));
    }

    private boolean isTimeSignatureCompound() {
        return ((beatsPerMeasure % 3) == 0) && ((beatsPerMeasure / 3) > 1);
    }

    private void finishCurrentMeasure() {
        if (elCurMeasure.getParent() == null) {
            elCurPart.appendChild(elCurMeasure);
        } else {
            int sCurMNum = Integer.parseInt(elCurMeasure.getAttributeValue("number"));
            Elements elMeasures = elCurPart.getChildElements("measure");

            for (int x = 0; x < elMeasures.size(); ++x) {
                Element elM = elMeasures.get(x);
                int sMNum = Integer.parseInt(elM.getAttributeValue("number"));
                if (sMNum == sCurMNum) {
                    elCurPart.replaceChild(elM, elCurMeasure);
                }
            }
        }
    }

    private void newMeasure() {
        if (elCurMeasure == null) {
            doFirstMeasure(false);
        } else {
            finishCurrentMeasure();

            int nextNumber = 1;
            boolean bNewMeasure = true;
            Elements elMeasures = elCurPart.getChildElements("measure");
            Element elLastMeasure;
            if (elMeasures.size() > 0) {
                elLastMeasure = elMeasures.get(elMeasures.size() - 1);
                Attribute elNumber = elLastMeasure.getAttribute("number");
                if (elLastMeasure.getChildElements("note").size() < 1) {
                    bNewMeasure = false;
                } else {
                    nextNumber = Integer.parseInt(elNumber.getValue()) + 1;
                }
            } else {
                bNewMeasure = elCurMeasure.getChildElements("note").size() > 0;
            }

            if (bNewMeasure) {
                elCurMeasure = new Element("measure");
                elCurMeasure.addAttribute(new Attribute("number", Integer.toString(nextNumber)));
            }
        }
    }

    public void noteEvent(Note note) {
        doNote(note, false);
    }

    public void parallelNoteEvent(Note note) {
        doNote(note, true);
    }

    private void doNote(Note note, boolean bChord) {
        Element elNote = new Element("note");
        if (bChord) {
            elNote.appendChild(new Element("chord"));
        }

        int iAlter = alterForNoteValue(note.getValue());
        if (note.isRest()) {
            elNote.appendChild(new Element("rest"));
        } else {
            Element elPitch = new Element("pitch");
            Element elStep = new Element("step");
            elStep.appendChild(stepForNoteValue(note.getValue()));
            elPitch.appendChild(elStep);

            if (iAlter != 0) {
                Element elAlter = new Element("alter");
                elAlter.appendChild(Integer.toString(iAlter));
                elPitch.appendChild(elAlter);
            }

            Element elOctave = new Element("octave");
            elOctave.appendChild(octaveForNoteValue(note.getValue()));
            elPitch.appendChild(elOctave);
            elNote.appendChild(elPitch);
        }

        int iXMLDuration = note.getDuration() == 0L
                           ? 0
                           : Integers.max(note.isRest() ? 0 : 1,
                                          (int) (note.getDuration() * DIVISIONS_PER_BEAT
                                                  * actualBeatsTempo / ONE_MINUTE));

        Element elDuration = new Element("duration");
        elDuration.appendChild(Integer.toString(iXMLDuration));
        elNote.appendChild(elDuration);

        Element elTie;
        Attribute atTieType;
        boolean bTied = false;
        if (note.isStartOfTie()) {
            elTie = new Element("tie");
            atTieType = new Attribute("type", "start");
            elTie.addAttribute(atTieType);
            elNote.appendChild(elTie);
            bTied = true;
        } else if (note.isEndOfTie()) {
            elTie = new Element("tie");
            atTieType = new Attribute("type", "stop");
            elTie.addAttribute(atTieType);
            elNote.appendChild(elTie);
            bTied = true;
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

        Element elStaff = new Element("staff");
        elStaff.appendChild(note.getValue() >= 48 ? "1" : "2");
        elNote.appendChild(elStaff);

        if (bTied) {
            Element elNotations = new Element("notations");
            Element elTied;
            Attribute atType;
            if (note.isStartOfTie()) {
                elTied = new Element("tied");
                atType = new Attribute("type", "start");
                elTied.addAttribute(atType);
                elNotations.appendChild(elTied);
            } else if (note.isEndOfTie()) {
                elTied = new Element("tied");
                atType = new Attribute("type", "stop");
                elTied.addAttribute(atType);
                elNotations.appendChild(elTied);
            }

            elNote.appendChild(elNotations);
        }

        if (note.isRest()) {
            if (iXMLDuration > 0) {
                elCurMeasure.appendChild(elNote);
            }
        } else if (iXMLDuration == 0) {
            elCurMeasure.appendChild(elNote);
        } else {
            stream(root.getChildElements("part"))
                    .flatMap(part -> stream(part.getChildElements("measure"))
                            .flatMap(measure -> stream(measure.getChildElements("note"))))
                    .filter(noteMatches(note.getValue(), 0)).findFirst()
                    .ifPresent(elOldNote -> elCurMeasure.replaceChild(elOldNote, elNote));
        }
    } //TODO - handle measures

    private static String stepForNoteValue(int value) {
        return Note.NOTES[value % 12].substring(0, 1);
    }

    private static int alterForNoteValue(int value) {
        String pitch = Note.NOTES[value % 12];
        return pitch.length() > 1 ? pitch.contains("#") ? 1 : -1 : 0;
    }

    private static String octaveForNoteValue(int value) {
        return Integer.toString(value / 12);
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

    private static Stream<Element> stream(Elements elements) {
        return StreamSupport.stream(new ElementsSpliterator(elements), false);
    }

    private static class ElementsSpliterator extends AbstractSpliterator<Element> {

        private Elements elements;
        private int current = 0;

        private ElementsSpliterator(Elements elements) {
            super(elements.size(), Spliterator.ORDERED | Spliterator.SIZED | Spliterator.IMMUTABLE);
            this.elements = elements;
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

}
