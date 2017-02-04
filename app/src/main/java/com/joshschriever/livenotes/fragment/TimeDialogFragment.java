package com.joshschriever.livenotes.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.NumberPicker;

import com.joshschriever.livenotes.R;

public class TimeDialogFragment extends DialogFragment {

    private Callbacks callbacks;

    //TODO

    //TODO
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
    public void onStart() {
        super.onStart();
        initializeViews(getDialog());
    }

    private void initializeViews(Dialog dialog) {
        //TODO

        tempo = (NumberPicker) dialog.findViewById(R.id.tempo_picker);
        tempo.setMinValue(getResources().getInteger(R.integer.min_tempo));
        tempo.setMaxValue(getResources().getInteger(R.integer.max_tempo));
        tempo.setValue(getResources().getInteger(R.integer.default_tempo));
        tempo.setWrapSelectorWheel(false);
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
