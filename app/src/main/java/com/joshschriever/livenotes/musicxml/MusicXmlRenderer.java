package com.joshschriever.livenotes.musicxml;

import android.util.SparseArray;

import org.jfugue.ChannelPressure;
import org.jfugue.Controller;
import org.jfugue.Instrument;
import org.jfugue.KeySignature;
import org.jfugue.Layer;
import org.jfugue.Measure;
import org.jfugue.Note;
import org.jfugue.ParserListener;
import org.jfugue.PitchBend;
import org.jfugue.PolyphonicPressure;
import org.jfugue.Tempo;
import org.jfugue.Time;
import org.jfugue.Voice;

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
public class MusicXmlRenderer implements ParserListener {

    private static final int DEFAULT_TEMPO = 120;
    private static final int DIVISIONS_PER_BEAT = 24;

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
            elCurMeasure.addAttribute(new Attribute("number", Integer.toString(1)));
            Element elAttributes = new Element("attributes");
            Element elClef;
            Element elSign;
            Element elLine;
            if (bAddDefaults) {
                elClef = new Element("divisions");
                elClef.appendChild(Integer.toString(DIVISIONS_PER_BEAT));
                elAttributes.appendChild(elClef);

                Element elKey = new Element("key");
                Element elFifths = new Element("fifths");
                elFifths.appendChild("0");
                elKey.appendChild(elFifths);
                Element elMode = new Element("mode");
                elMode.appendChild("major");
                elKey.appendChild(elMode);
                elAttributes.appendChild(elKey);

                elSign = new Element("time");
                elLine = new Element("beats");
                elLine.appendChild(Integer.toString(beatsPerMeasure));
                elSign.appendChild(elLine);
                Element elBeatType = new Element("beat-type");
                elBeatType.appendChild(Integer.toString(beatType));
                elSign.appendChild(elBeatType);
                elAttributes.appendChild(elSign);

                elClef = new Element("clef");
                elSign = new Element("sign");
                elSign.appendChild("G");
                elLine = new Element("line");
                elLine.appendChild("2");
                elClef.appendChild(elSign);
                elClef.appendChild(elLine);
                elAttributes.appendChild(elClef);

                elCurMeasure.appendChild(elAttributes);

                doTempo(markedTempo);
            }
        }
    }

    public void voiceEvent(Voice voice) {
        String sReqVoice = voice.getMusicString();
        String sCurPartID =
                elCurPart == null ? null : elCurPart.getAttribute("id").getValue();
        if (sCurPartID == null || sReqVoice.compareTo(sCurPartID) != 0) {
            boolean bNewVoiceExists = false;
            Elements elParts = root.getChildElements("part");
            Element elExistingNewPart = null;

            for (int x = 0; x < elParts.size(); ++x) {
                Element elP = elParts.get(x);
                String sPID = elP.getAttribute("id").getValue();
                if (sPID.compareTo(sReqVoice) == 0) {
                    bNewVoiceExists = true;
                    elExistingNewPart = elP;
                }
            }

            finishCurrentVoice();
            if (bNewVoiceExists) {
                elCurPart = elExistingNewPart;
            } else {
                newVoice(voice);
            }

            newMeasure();
        }
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

    public void instrumentEvent(Instrument instrument) {
        Element elInstrName = new Element("instrument-name");
        elInstrName.appendChild(instrument.getInstrumentName());
        Element elInstrument = new Element("score-instrument");
        elInstrument.addAttribute(new Attribute("id", Byte.toString(instrument.getInstrument())));
        elInstrument.appendChild(elInstrName);
    }

    public void tempoEvent(Tempo tempo) {
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

    public void layerEvent(Layer layer) {
    }

    public void timeEvent(Time time) {
    }

    public void keySignatureEvent(KeySignature keySig) {
        doKeySig(keySig);
    }

    private void doKeySig(KeySignature keySig) {
        Element elKey = new Element("key");
        Element elFifths = new Element("fifths");
        elFifths.appendChild(Byte.toString(keySig.getKeySig()));
        elKey.appendChild(elFifths);
        Element elMode = new Element("mode");
        elMode.appendChild(keySig.getScale() == 1 ? "minor" : "major");
        elKey.appendChild(elMode);
        if (elCurMeasure == null) {
            doFirstMeasure(true);
        }

        Element elAttributes = elCurMeasure.getFirstChildElement("attributes");
        boolean bNewAttributes = elAttributes == null;
        if (bNewAttributes) {
            elAttributes = new Element("attributes");
        }

        elAttributes.appendChild(elKey);
        if (bNewAttributes) {
            elCurMeasure.appendChild(elAttributes);
        }

    }

    public void measureEvent(Measure measure) {
        if (elCurMeasure == null) {
            doFirstMeasure(false);
        } else {
            finishCurrentMeasure();
            newMeasure();
        }

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

    public void controllerEvent(Controller controller) {
    }

    public void channelPressureEvent(ChannelPressure channelPressure) {
    }

    public void polyphonicPressureEvent(PolyphonicPressure polyphonicPressure) {
    }

    public void pitchBendEvent(PitchBend pitchBend) {
    }

    public void noteEvent(Note note) {
        doNote(note, false);
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

        Element elDuration = new Element("duration");
        double decimalDuration = note.getDecimalDuration() * actualBeatsTempo / DEFAULT_TEMPO;
        int iXMLDuration = (int) (decimalDuration * DIVISIONS_PER_BEAT);
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

        NoteType noteType = noteTypeForDuration(iXMLDuration);
        Element elType = new Element("type");
        elType.appendChild(noteType.typeString);
        elNote.appendChild(elType);
        if (noteType.dotted) {
            elNote.appendChild(new Element("dot"));
        }

        if (iAlter != 0) {
            Element elAccidental = new Element("accidental");
            elAccidental.appendChild(iAlter == 1 ? "sharp" : "flat");
            elNote.appendChild(elAccidental);
        }

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

        if (iXMLDuration == 0) {
            elCurMeasure.appendChild(elNote);
        } else {
            stream(root.getChildElements("part"))
                    .flatMap(part -> stream(part.getChildElements("measure"))
                            .flatMap(measure -> stream(measure.getChildElements("note"))))
                    .filter(noteMatches(note.getValue(), 0)).findFirst()
                    .ifPresent(elOldNote -> elCurMeasure.replaceChild(elOldNote, elNote));
        }
    }

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

    //TODO - actually implement handling the time signature correctly
    private NoteType noteTypeForDuration(int duration) {
        if (duration <= ((DIVISIONS_PER_BEAT / 4)
                + (DIVISIONS_PER_BEAT * 3 / 8)) / 2) {
            return new NoteType("16th", false);
        } else if (duration <= ((DIVISIONS_PER_BEAT * 3 / 8)
                + (DIVISIONS_PER_BEAT / 2)) / 2) {
            return new NoteType("16th", true);
        } else if (duration <= ((DIVISIONS_PER_BEAT / 2)
                + (DIVISIONS_PER_BEAT * 3 / 4)) / 2) {
            return new NoteType("eighth", false);
        } else if (duration <= ((DIVISIONS_PER_BEAT * 3 / 4)
                + (DIVISIONS_PER_BEAT)) / 2) {
            return new NoteType("eighth", true);
        } else if (duration <= ((DIVISIONS_PER_BEAT)
                + (DIVISIONS_PER_BEAT * 3 / 2)) / 2) {
            return new NoteType("quarter", false);
        } else if (duration <= ((DIVISIONS_PER_BEAT * 3 / 2)
                + (DIVISIONS_PER_BEAT * 2)) / 2) {
            return new NoteType("quarter", true);
        } else if (duration <= ((DIVISIONS_PER_BEAT * 2)
                + (DIVISIONS_PER_BEAT * 3)) / 2) {
            return new NoteType("half", false);
        } else if (duration <= ((DIVISIONS_PER_BEAT * 3)
                + (DIVISIONS_PER_BEAT * 4)) / 2) {
            return new NoteType("half", true);
        } else {
            return new NoteType("whole", false);
        }
    }

    private class NoteType {

        private String typeString;
        private boolean dotted;

        private NoteType(String typeString, boolean dotted) {
            this.typeString = typeString;
            this.dotted = dotted;
        }
    }

    public void sequentialNoteEvent(Note note) {
    }

    public void parallelNoteEvent(Note note) {
        doNote(note, true);
    }

    private static Predicate<Element> noteMatches(int value, int duration) {
        return elNote -> pitchMatches(elNote.getFirstChildElement("pitch"), value)
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
