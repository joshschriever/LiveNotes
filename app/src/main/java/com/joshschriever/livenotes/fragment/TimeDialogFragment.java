package com.joshschriever.livenotes.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.joshschriever.livenotes.R;

import java.util.List;

import static java8.util.J8Arrays.stream;
import static java8.util.stream.Collectors.toList;

public class TimeDialogFragment extends DialogFragment {

    private static final SparseIntArray NOTE_DRAWABLES = new SparseIntArray(5);

    static {
        NOTE_DRAWABLES.put(2, R.mipmap.half_note);
        NOTE_DRAWABLES.put(3, R.mipmap.dotted_half_note);
        NOTE_DRAWABLES.put(4, R.mipmap.quarter_note);
        NOTE_DRAWABLES.put(6, R.mipmap.dotted_quarter_note);
        NOTE_DRAWABLES.put(8, R.mipmap.eighth_note);
    }

    private Callbacks callbacks;

    private List<Integer> simpleBeatsOptions;
    private List<Integer> simpleBeatValueOptions;
    private List<Integer> compoundBeatsOptions;
    private List<Integer> compoundBeatValueOptions;
    private int simpleDefaultBeatsIndex;
    private int simpleDefaultBeatValueIndex;
    private int compoundDefaultBeatsIndex;
    private int compoundDefaultBeatValueIndex;

    private ToggleButton simple;
    private ToggleButton compound;
    private TextView beats;
    private TextView beatValue;
    private Button decrementBeats;
    private Button incrementBeats;
    private Button decrementBeatValue;
    private Button incrementBeatValue;

    private ImageView tempoNote;
    private NumberPicker tempo;

    public TimeDialogFragment() {
    }

    @SuppressWarnings("ValidFragment")
    public TimeDialogFragment(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.time_dialog_title)
                .setView(R.layout.dialog_time)
                .setPositiveButton(R.string.ok, (d, w) -> dismiss(true))
                .create();

        setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeFromResources(getResources());
    }

    @Override
    public void onStart() {
        super.onStart();
        initializeViews(getDialog());
    }

    private void initializeFromResources(Resources resources) {
        simpleBeatsOptions = stream(resources.getIntArray(
                R.array.simple_time_beats_options)).boxed().collect(toList());
        simpleBeatValueOptions = stream(resources.getIntArray(
                R.array.simple_time_beat_value_options)).boxed().collect(toList());
        compoundBeatsOptions = stream(resources.getIntArray(
                R.array.compound_time_beats_options)).boxed().collect(toList());
        compoundBeatValueOptions = stream(resources.getIntArray(
                R.array.compound_time_beat_value_options)).boxed().collect(toList());

        simpleDefaultBeatsIndex =
                resources.getInteger(R.integer.simple_time_default_beats_index);
        simpleDefaultBeatValueIndex =
                resources.getInteger(R.integer.simple_time_default_beat_value_index);
        compoundDefaultBeatsIndex =
                resources.getInteger(R.integer.compound_time_default_beats_index);
        compoundDefaultBeatValueIndex =
                resources.getInteger(R.integer.compound_time_default_beat_value_index);
    }

    private void initializeViews(Dialog dialog) {
        simple = (ToggleButton) dialog.findViewById(R.id.simple);
        compound = (ToggleButton) dialog.findViewById(R.id.compound);
        beats = (TextView) dialog.findViewById(R.id.beats);
        beatValue = (TextView) dialog.findViewById(R.id.beat_value);
        decrementBeats = (Button) dialog.findViewById(R.id.decrement_beats);
        incrementBeats = (Button) dialog.findViewById(R.id.increment_beats);
        decrementBeatValue = (Button) dialog.findViewById(R.id.decrement_beat_value);
        incrementBeatValue = (Button) dialog.findViewById(R.id.increment_beat_value);

        dialog.findViewById(R.id.simple_parent).setOnClickListener(__ -> onSimpleClicked());
        dialog.findViewById(R.id.compound_parent).setOnClickListener(__ -> onCompoundClicked());

        decrementBeats.setOnClickListener(__ -> onDecrementBeatsClicked());
        incrementBeats.setOnClickListener(__ -> onIncrementBeatsClicked());
        decrementBeatValue.setOnClickListener(__ -> onDecrementBeatValueClicked());
        incrementBeatValue.setOnClickListener(__ -> onIncrementBeatValueClicked());

        tempoNote = (ImageView) dialog.findViewById(R.id.tempo_note);
        tempo = (NumberPicker) dialog.findViewById(R.id.tempo);
        tempo.setMinValue(getResources().getInteger(R.integer.min_tempo));
        tempo.setMaxValue(getResources().getInteger(R.integer.max_tempo));
        tempo.setValue(getResources().getInteger(R.integer.default_tempo));
        tempo.setWrapSelectorWheel(false);

        onSimpleClicked();
    }

    private void onSimpleClicked() {
        if (!simple.isChecked()) {
            simple.setChecked(true);
            compound.setChecked(false);
            setBeats(true, simpleDefaultBeatsIndex);
            setBeatValue(true, simpleDefaultBeatValueIndex);
        }
    }

    private void onCompoundClicked() {
        if (!compound.isChecked()) {
            compound.setChecked(true);
            simple.setChecked(false);
            setBeats(false, compoundDefaultBeatsIndex);
            setBeatValue(false, compoundDefaultBeatValueIndex);
        }
    }

    private void onDecrementBeatsClicked() {
        if (simple.isChecked()) {
            if (beats() > simpleBeatsOptions.get(0)) {
                setBeats(true, simpleBeatsOptions.indexOf(beats()) - 1);
            }
        } else {
            if (beats() > compoundBeatsOptions.get(0)) {
                setBeats(false, compoundBeatsOptions.indexOf(beats()) - 1);
            }
        }
    }

    private void onIncrementBeatsClicked() {
        if (simple.isChecked()) {
            if (beats() < simpleBeatsOptions.get(simpleBeatsOptions.size() - 1)) {
                setBeats(true, simpleBeatsOptions.indexOf(beats()) + 1);
            }
        } else {
            if (beats() < compoundBeatsOptions.get(compoundBeatsOptions.size() - 1)) {
                setBeats(false, compoundBeatsOptions.indexOf(beats()) + 1);
            }
        }
    }

    private void onDecrementBeatValueClicked() {
        if (simple.isChecked()) {
            if (beatValue() > simpleBeatValueOptions.get(0)) {
                setBeatValue(true, simpleBeatValueOptions.indexOf(beatValue()) - 1);
            }
        } else {
            if (beatValue() > compoundBeatValueOptions.get(0)) {
                setBeatValue(false, compoundBeatValueOptions.indexOf(beatValue()) - 1);
            }
        }
    }

    private void onIncrementBeatValueClicked() {
        if (simple.isChecked()) {
            if (beatValue() < simpleBeatValueOptions.get(simpleBeatValueOptions.size() - 1)) {
                setBeatValue(true, simpleBeatValueOptions.indexOf(beatValue()) + 1);
            }
        } else {
            if (beatValue() < compoundBeatValueOptions.get(compoundBeatValueOptions.size() - 1)) {
                setBeatValue(false, compoundBeatValueOptions.indexOf(beatValue()) + 1);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setBeats(boolean simple, int index) {
        if (simple) {
            beats.setText(Integer.toString(simpleBeatsOptions.get(index)));
        } else {
            beats.setText(Integer.toString(compoundBeatsOptions.get(index)));
        }
    }

    @SuppressLint("SetTextI18n")
    private void setBeatValue(boolean simple, int index) {
        if (simple) {
            beatValue.setText(Integer.toString(simpleBeatValueOptions.get(index)));
            setTempoNote(simpleBeatValueOptions.get(index));
        } else {
            beatValue.setText(Integer.toString(compoundBeatValueOptions.get(index)));
            setTempoNote(compoundBeatValueOptions.get(index) * 3 / 4);
        }
    }

    private void setTempoNote(int noteDrawableKey) {
        tempoNote.setImageDrawable(getResources().getDrawable(NOTE_DRAWABLES.get(noteDrawableKey),
                                                              null));
    }

    private int beats() {
        return Integer.parseInt(beats.getText().toString());
    }

    private int beatValue() {
        return Integer.parseInt(beatValue.getText().toString());
    }

    private void dismiss(boolean callback) {
        if (callback) {
            callbacks.onTimeSet(beats(),
                                beatValue(),
                                tempo.getValue());
        }
        dismiss();
    }

    public interface Callbacks {

        void onTimeSet(int timeSigBeats, int timeSigBeatValue, int tempoBPM);
    }

}
