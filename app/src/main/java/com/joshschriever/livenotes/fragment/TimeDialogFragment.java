package com.joshschriever.livenotes.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.joshschriever.livenotes.R;

public class TimeDialogFragment extends DialogFragment {

    private Callbacks callbacks;

    private int[] simpleBeatsOptions;
    private int[] simpleBeatValueOptions;
    private int[] compoundBeatsOptions;
    private int[] compoundBeatValueOptions;
    private int simpleDefaultBeatsIndex;
    private int simpleDefaultBeatValueIndex;
    private int compoundDefaultBeatsIndex;
    private int compoundDefaultBeatValueIndex;

    private ToggleButton simple;
    private ToggleButton compound;
    private TextView beats;
    private TextView beatValue;
    private Button incrementBeats;
    private Button decrementBeats;
    private Button incrementBeatValue;
    private Button decrementBeatValue;

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
        simpleBeatsOptions = resources.getIntArray(R.array.simple_time_beats_options);
        simpleBeatValueOptions = resources.getIntArray(R.array.simple_time_beat_value_options);
        compoundBeatsOptions = resources.getIntArray(R.array.compound_time_beats_options);
        compoundBeatValueOptions = resources.getIntArray(R.array.compound_time_beat_value_options);

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
        incrementBeats = (Button) dialog.findViewById(R.id.increment_beats);
        decrementBeats = (Button) dialog.findViewById(R.id.decrement_beats);
        incrementBeatValue = (Button) dialog.findViewById(R.id.increment_beat_value);
        decrementBeatValue = (Button) dialog.findViewById(R.id.decrement_beat_value);

        simple.setOnClickListener(__ -> onSimpleClicked());
        compound.setOnClickListener(__ -> onCompoundClicked());
        incrementBeats.setOnClickListener(__ -> onIncrementBeatsClicked());
        decrementBeats.setOnClickListener(__ -> onDecrementBeatsClicked());
        incrementBeatValue.setOnClickListener(__ -> onIncrementBeatValueClicked());
        decrementBeatValue.setOnClickListener(__ -> onDecrementBeatValueClicked());

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
            compound.setChecked(false);
            //TODO
        }
    }

    private void onCompoundClicked() {
        if (!compound.isChecked()) {
            simple.setChecked(false);
            //TODO
        }
    }

    private void onIncrementBeatsClicked() {
        //TODO
    }

    private void onDecrementBeatsClicked() {
        //TODO
    }

    private void onIncrementBeatValueClicked() {
        //TODO
    }

    private void onDecrementBeatValueClicked() {
        //TODO
    }

    private void dismiss(boolean callback) {
        if (callback) {
            callbacks.onTimeSet(6, 8, tempo.getValue());//TODO
        }
        dismiss();
    }

    public interface Callbacks {

        void onTimeSet(int timeSigBeats, int timeSigBeatValue, int tempoBPM);
    }

}
