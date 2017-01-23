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

public class MusicXmlRenderer implements ParserListener {

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
    }

    public String getDefaultMusicXMLString() {
        Document xomDoc = this.getMusicXMLDoc();
        return xomDoc.toXML();
    }

    public final String getMusicXMLString() {
        String xml = getDefaultMusicXMLString();
        int divisionsEnd = xml.indexOf("</divisions>") + 12;
        int keyStart = xml.indexOf("<key>");
        int keyEnd = xml.indexOf("</key>") + 6;

        return xml.substring(0, divisionsEnd)
                + xml.substring(keyStart, keyEnd)
                + xml.substring(divisionsEnd, keyStart)
                + xml.substring(keyEnd);
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

        Document var6 = new Document(this.root);
        DocType var7 = new DocType("score-partwise", "-//Recordare//DTD MusicXML 1.1 Partwise//EN",
                                   "http://www.musicxml.org/dtds/partwise.dtd");
        var6.insertChild(var7, 0);
        return var6;
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
                elSign = new Element("time");
                elLine = new Element("beats");
                elLine.appendChild(Integer.toString(4));
                elSign.appendChild(elLine);
                Element elBeatType = new Element("beat-type");
                elBeatType.appendChild(Integer.toString(4));
                elSign.appendChild(elBeatType);
                elAttributes.appendChild(elSign);
            }

            if (bAddDefaults) {
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
                this.doKeySig(new KeySignature((byte) 0, (byte) 0));
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
        Integer iBPM = Integer.valueOf((new Float(PPMtoBPM(tempo.getTempo()))).intValue());
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
        Integer nextNumber = Integer.valueOf(1);
        boolean bNewMeasure = true;
        Elements elMeasures = this.elCurPart.getChildElements("measure");
        Element elLastMeasure = null;
        if (elMeasures.size() > 0) {
            elLastMeasure = elMeasures.get(elMeasures.size() - 1);
            Attribute elNumber = elLastMeasure.getAttribute("number");
            if (elLastMeasure.getChildElements("note").size() < 1) {
                bNewMeasure = false;
            } else {
                nextNumber = Integer.valueOf(Integer.parseInt(elNumber.getValue()) + 1);
            }
        } else {
            bNewMeasure = this.elCurMeasure.getChildElements("note").size() > 0;
        }

        if (bNewMeasure) {
            this.elCurMeasure = new Element("measure");
            this.elCurMeasure.addAttribute(
                    new Attribute("number", Integer.toString(nextNumber.intValue())));
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
        Element elNote = new Element("note");
        if (bChord) {
            elNote.appendChild(new Element("chord"));
        }

        Element elDuration;
        int iXMLDuration;
        if (note.isRest()) {
            elDuration = new Element("rest");
            elNote.appendChild(elDuration);
        } else {
            elDuration = new Element("pitch");
            Element dDuration = new Element("step");
            String sPitch = Note.NOTES[note.getValue() % 12];
            iXMLDuration = 0;
            if (sPitch.length() > 1) {
                iXMLDuration = sPitch.contains("#") ? 1 : -1;
                sPitch = sPitch.substring(0, 1);
            }

            dDuration.appendChild(sPitch);
            elDuration.appendChild(dDuration);
            Element bDoNotation;
            if (iXMLDuration != 0) {
                bDoNotation = new Element("alter");
                bDoNotation.appendChild(Integer.toString(iXMLDuration));
                elDuration.appendChild(bDoNotation);
            }

            bDoNotation = new Element("octave");
            bDoNotation.appendChild(Integer.toString(note.getValue() / 12));
            elDuration.appendChild(bDoNotation);
            elNote.appendChild(elDuration);
        }

        elDuration = new Element("duration");
        double dDuration1 = note.getDecimalDuration();
        iXMLDuration = (int) (dDuration1 * 1024.0D * 4.0D / 256.0D);
        elDuration.appendChild(Integer.toString(iXMLDuration));
        elNote.appendChild(elDuration);
        boolean bDoNotation1 = false;
        Element sDuration;
        Attribute bDotted;
        if (note.isStartOfTie()) {
            sDuration = new Element("tie");
            bDotted = new Attribute("type", "start");
            sDuration.addAttribute(bDotted);
            elNote.appendChild(sDuration);
            bDoNotation1 = true;
        } else if (note.isEndOfTie()) {
            sDuration = new Element("tie");
            bDotted = new Attribute("type", "stop");
            sDuration.addAttribute(bDotted);
            elNote.appendChild(sDuration);
            bDoNotation1 = true;
        }

        boolean bDotted1 = false;
        String sDuration1;
        if (dDuration1 <= 0.0625D) {
            sDuration1 = "16th";
        } else if (dDuration1 <= 0.09375D) {
            sDuration1 = "16th";
            bDotted1 = true;
        } else if (dDuration1 <= 0.125D) {
            sDuration1 = "eighth";
        } else if (dDuration1 <= 0.1875D) {
            sDuration1 = "eighth";
            bDotted1 = true;
        } else if (dDuration1 <= 0.25D) {
            sDuration1 = "quarter";
        } else if (dDuration1 <= 0.375D) {
            sDuration1 = "quarter";
            bDotted1 = true;
        } else if (dDuration1 <= 0.5D) {
            sDuration1 = "half";
        } else if (dDuration1 <= 0.75D) {
            sDuration1 = "half";
            bDotted1 = true;
        } else {
            sDuration1 = "whole";
        }

//        if (dDuration1 == 1.0D) {
//            sDuration1 = "whole";
//        } else if (dDuration1 == 0.75D) {
//            sDuration1 = "half";
//            bDotted1 = true;
//        } else if (dDuration1 == 0.5D) {
//            sDuration1 = "half";
//        } else if (dDuration1 == 0.375D) {
//            sDuration1 = "quarter";
//            bDotted1 = true;
//        } else if (dDuration1 == 0.25D) {
//            sDuration1 = "quarter";
//        } else if (dDuration1 == 0.1875D) {
//            sDuration1 = "eighth";
//            bDotted1 = true;
//        } else if (dDuration1 == 0.125D) {
//            sDuration1 = "eighth";
//        } else if (dDuration1 == 0.09375D) {
//            sDuration1 = "16th";
//            bDotted1 = true;
//        } else if (dDuration1 == 0.0625D) {
//            sDuration1 = "16th";
//        } else if (dDuration1 == 0.046875D) {
//            sDuration1 = "32nd";
//            bDotted1 = true;
//        } else if (dDuration1 == 0.03125D) {
//            sDuration1 = "32nd";
//        } else if (dDuration1 == 0.0234375D) {
//            sDuration1 = "64th";
//            bDotted1 = true;
//        } else if (dDuration1 == 0.015625D) {
//            sDuration1 = "64th";
//        } else if (dDuration1 == 0.01171875D) {
//            sDuration1 = "128th";
//            bDotted1 = true;
//        } else if (dDuration1 == 0.0078125D) {
//            sDuration1 = "128th";
//        } else {
//            sDuration1 = "/" + Double.toString(dDuration1);
//        }

        Element elType = new Element("type");
        elType.appendChild(sDuration1);
        elNote.appendChild(elType);
        Element elNotations;
        if (bDotted1) {
            elNotations = new Element("dot");
            elNote.appendChild(elNotations);
        }

        if (bDoNotation1) {
            elNotations = new Element("notations");
            Element elTied;
            Attribute atStart;
            if (note.isStartOfTie()) {
                elTied = new Element("tied");
                atStart = new Attribute("type", "start");
                elTied.addAttribute(atStart);
                elNotations.appendChild(elTied);
            } else if (note.isEndOfTie()) {
                elTied = new Element("tied");
                atStart = new Attribute("type", "stop");
                elTied.addAttribute(atStart);
                elNotations.appendChild(elTied);
            }

            elNote.appendChild(elNotations);
        }

        if (this.elCurMeasure == null) {
            this.doFirstMeasure(true);
        }

        this.elCurMeasure.appendChild(elNote);
    }

    public void sequentialNoteEvent(Note note) {
    }

    public void parallelNoteEvent(Note note) {
        this.doNote(note, true);
    }

    public static float PPMtoBPM(int ppm) {
        return (new Float(14400.0F / (float) ppm)).floatValue();
    }

}
