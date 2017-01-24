package com.joshschriever.livenotes.musicxml;

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

import nu.xom.Attribute;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

// Forked from JFugue
public class MusicXmlRenderer implements ParserListener {

    private Document document;
    private Element root = new Element("score-partwise");
    private Element elCurMeasure;
    private Element elPartList;
    private Element elCurScorePart;
    private Element elCurPart;

    public MusicXmlRenderer() {
        Element elID = new Element("identification");
        Element elCreator = new Element("creator");
        elCreator.addAttribute(new Attribute("type", "software"));
        elCreator.appendChild("JFugue MusicXMLRenderer");
        elID.appendChild(elCreator);
        this.root.appendChild(elID);
        this.elPartList = new Element("part-list");
        this.root.appendChild(this.elPartList);
        document = new Document(root);
        document.insertChild(new DocType("score-partwise",
                                         "-//Recordare//DTD MusicXML 1.1 Partwise//EN",
                                         "http://www.musicxml.org/dtds/partwise.dtd"),
                             0);
    }

    public String getMusicXMLString() {
        return getMusicXMLDoc().toXML();
    }

    public Document getMusicXMLDoc() {
        this.finishCurrentVoice();
        Elements elDocParts = this.root.getChildElements("part");

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

    public void doFirstMeasure(boolean bAddDefaults) {
        if (this.elCurPart == null) {
            this.newVoice(new Voice((byte) 0));
        }

        if (this.elCurMeasure == null) {
            this.elCurMeasure = new Element("measure");
            this.elCurMeasure.addAttribute(new Attribute("number", Integer.toString(1)));
            Element elAttributes = new Element("attributes");
            Element elClef;
            Element elSign;
            Element elLine;
            if (bAddDefaults) {
                elClef = new Element("divisions");
                elClef.appendChild(Integer.toString(4));
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
                elLine.appendChild(Integer.toString(4));
                elSign.appendChild(elLine);
                Element elBeatType = new Element("beat-type");
                elBeatType.appendChild(Integer.toString(4));
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
            }

            if (elAttributes.getChildCount() > 0) {
                this.elCurMeasure.appendChild(elAttributes);
            }

            if (bAddDefaults) {
                this.doTempo(new Tempo(120));
            }
        }
    }

    public void voiceEvent(Voice voice) {
        String sReqVoice = voice.getMusicString();
        String sCurPartID =
                this.elCurPart == null ? null : this.elCurPart.getAttribute("id").getValue();
        if (sCurPartID == null || sReqVoice.compareTo(sCurPartID) != 0) {
            boolean bNewVoiceExists = false;
            Elements elParts = this.root.getChildElements("part");
            Element elExistingNewPart = null;

            for (int x = 0; x < elParts.size(); ++x) {
                Element elP = elParts.get(x);
                String sPID = elP.getAttribute("id").getValue();
                if (sPID.compareTo(sReqVoice) == 0) {
                    bNewVoiceExists = true;
                    elExistingNewPart = elP;
                }
            }

            this.finishCurrentVoice();
            if (bNewVoiceExists) {
                this.elCurPart = elExistingNewPart;
            } else {
                this.newVoice(voice);
            }

            this.newMeasure();
        }
    }

    private void finishCurrentVoice() {
        String sCurPartID =
                this.elCurPart == null ? null : this.elCurPart.getAttribute("id").getValue();
        boolean bCurVoiceExists = false;
        Elements elParts = this.root.getChildElements("part");
        Element elExistingCurPart = null;

        for (int x = 0; x < elParts.size(); ++x) {
            Element elP = elParts.get(x);
            String sPID = elP.getAttribute("id").getValue();
            if (sPID.compareTo(sCurPartID) == 0) {
                bCurVoiceExists = true;
                elExistingCurPart = elP;
            }
        }

        if (this.elCurPart != null) {
            this.finishCurrentMeasure();
            if (bCurVoiceExists) {
                this.root.replaceChild(elExistingCurPart, this.elCurPart);
            } else {
                this.root.appendChild(this.elCurPart);
            }
        }

    }

    private void newVoice(Voice voice) {
        this.elCurScorePart = new Element("score-part");
        Attribute atPart = new Attribute("id", voice.getMusicString());
        this.elCurScorePart.addAttribute(atPart);
        this.elCurScorePart.appendChild(new Element("part-name"));
        Element elPL = this.root.getFirstChildElement("part-list");
        elPL.appendChild(this.elCurScorePart);
        this.elCurPart = new Element("part");
        Attribute atPart2 = new Attribute(atPart);
        this.elCurPart.addAttribute(atPart2);
        this.elCurMeasure = null;
        this.doFirstMeasure(true);
    }

    public void instrumentEvent(Instrument instrument) {
        Element elInstrName = new Element("instrument-name");
        elInstrName.appendChild(instrument.getInstrumentName());
        Element elInstrument = new Element("score-instrument");
        elInstrument.addAttribute(new Attribute("id", Byte.toString(instrument.getInstrument())));
        elInstrument.appendChild(elInstrName);
    }

    public void tempoEvent(Tempo tempo) {
        this.doTempo(tempo);
    }

    private void doTempo(Tempo tempo) {
        Element elDirection = new Element("direction");
        elDirection.addAttribute(new Attribute("placement", "above"));
        Element elDirectionType = new Element("direction-type");
        Element elMetronome = new Element("metronome");
        Element elBeatUnit = new Element("beat-unit");
        elBeatUnit.appendChild("quarter");
        Element elPerMinute = new Element("per-minute");
        Integer iBPM = Float.valueOf(PPMtoBPM(tempo.getTempo())).intValue();
        elPerMinute.appendChild(iBPM.toString());
        elMetronome.appendChild(elBeatUnit);
        elMetronome.appendChild(elPerMinute);
        elDirectionType.appendChild(elMetronome);
        elDirection.appendChild(elDirectionType);
        if (this.elCurMeasure == null) {
            this.doFirstMeasure(true);
        }

        this.elCurMeasure.appendChild(elDirection);
    }

    public void layerEvent(Layer layer) {
    }

    public void timeEvent(Time time) {
    }

    public void keySignatureEvent(KeySignature keySig) {
        this.doKeySig(keySig);
    }

    private void doKeySig(KeySignature keySig) {
        Element elKey = new Element("key");
        Element elFifths = new Element("fifths");
        elFifths.appendChild(Byte.toString(keySig.getKeySig()));
        elKey.appendChild(elFifths);
        Element elMode = new Element("mode");
        elMode.appendChild(keySig.getScale() == 1 ? "minor" : "major");
        elKey.appendChild(elMode);
        if (this.elCurMeasure == null) {
            this.doFirstMeasure(true);
        }

        Element elAttributes = this.elCurMeasure.getFirstChildElement("attributes");
        boolean bNewAttributes = elAttributes == null;
        if (bNewAttributes) {
            elAttributes = new Element("attributes");
        }

        elAttributes.appendChild(elKey);
        if (bNewAttributes) {
            this.elCurMeasure.appendChild(elAttributes);
        }

    }

    public void measureEvent(Measure measure) {
        if (this.elCurMeasure == null) {
            this.doFirstMeasure(false);
        } else {
            this.finishCurrentMeasure();
            this.newMeasure();
        }

    }

    private void finishCurrentMeasure() {
        if (this.elCurMeasure.getParent() == null) {
            this.elCurPart.appendChild(this.elCurMeasure);
        } else {
            int sCurMNum = Integer.parseInt(this.elCurMeasure.getAttributeValue("number"));
            Elements elMeasures = this.elCurPart.getChildElements("measure");

            for (int x = 0; x < elMeasures.size(); ++x) {
                Element elM = elMeasures.get(x);
                int sMNum = Integer.parseInt(elM.getAttributeValue("number"));
                if (sMNum == sCurMNum) {
                    this.elCurPart.replaceChild(elM, this.elCurMeasure);
                }
            }
        }

    }

    private void newMeasure() {
        int nextNumber = 1;
        boolean bNewMeasure = true;
        Elements elMeasures = this.elCurPart.getChildElements("measure");
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
            bNewMeasure = this.elCurMeasure.getChildElements("note").size() > 0;
        }

        if (bNewMeasure) {
            this.elCurMeasure = new Element("measure");
            this.elCurMeasure.addAttribute(
                    new Attribute("number", Integer.toString(nextNumber)));
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
        this.doNote(note, false);
    }

    private void doNote(Note note, boolean bChord) {
        //TODO - when duration > 0, replace the note with 0 duration instead of adding a new one
        Element elNote = new Element("note");
        if (bChord) {
            elNote.appendChild(new Element("chord"));
        }

        if (note.isRest()) {
            elNote.appendChild(new Element("rest"));
        } else {
            Element elPitch = new Element("pitch");
            Element elStep = new Element("step");
            String sPitch = Note.NOTES[note.getValue() % 12];
            int iAlter = 0;
            if (sPitch.length() > 1) {
                iAlter = sPitch.contains("#") ? 1 : -1;
                sPitch = sPitch.substring(0, 1);
            }
            elStep.appendChild(sPitch);
            elPitch.appendChild(elStep);

            if (iAlter != 0) {
                Element elAlter = new Element("alter");
                elAlter.appendChild(Integer.toString(iAlter));
                elPitch.appendChild(elAlter);
            }

            Element elOctave = new Element("octave");
            elOctave.appendChild(Integer.toString(note.getValue() / 12));
            elPitch.appendChild(elOctave);
            elNote.appendChild(elPitch);
        }

        Element elDuration = new Element("duration");
        double decimalDuration = note.getDecimalDuration();
        int iXMLDuration = (int) (decimalDuration * 1024.0D * 4.0D / 256.0D);
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

        String sType;
        boolean bDotted = false;
        /*if (decimalDuration <= 0.0078125D) {
            sType = "128th";
        } else if (decimalDuration <= 0.01171875D) {
            sType = "128th";
            bDotted = true;
        } else if (decimalDuration <= 0.015625D) {
            sType = "64th";
        } else if (decimalDuration <= 0.0234375D) {
            sType = "64th";
            bDotted = true;
        } else if (decimalDuration <= 0.03125D) {
            sType = "32nd";
        } else if (decimalDuration <= 0.046875D) {
            sType = "32nd";
            bDotted = true;
        } else*/ if (decimalDuration <= 0.0625D) {
            sType = "16th";
        } else if (decimalDuration <= 0.09375D) {
            sType = "16th";
            bDotted = true;
        } else if (decimalDuration <= 0.125D) {
            sType = "eighth";
        } else if (decimalDuration <= 0.1875D) {
            sType = "eighth";
            bDotted = true;
        } else if (decimalDuration <= 0.25D) {
            sType = "quarter";
        } else if (decimalDuration <= 0.375D) {
            sType = "quarter";
            bDotted = true;
        } else if (decimalDuration <= 0.5D) {
            sType = "half";
        } else if (decimalDuration <= 0.75D) {
            sType = "half";
            bDotted = true;
        } else {
            sType = "whole";
        }

        Element elType = new Element("type");
        elType.appendChild(sType);
        elNote.appendChild(elType);
        if (bDotted) {
            elNote.appendChild(new Element("dot"));
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

        this.elCurMeasure.appendChild(elNote);
    }

    public void sequentialNoteEvent(Note note) {
    }

    public void parallelNoteEvent(Note note) {
        this.doNote(note, true);
    }

    public static float PPMtoBPM(int ppm) {
        return 14400.0F / (float) ppm;
    }

}
