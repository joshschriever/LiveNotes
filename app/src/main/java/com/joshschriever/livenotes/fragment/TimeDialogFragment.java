package com.joshschriever.livenotes.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.joshschriever.livenotes.R;

public class TimeDialogFragment extends DialogFragment {

    private Callbacks callbacks;

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
                .setMessage("hello!")//TODO - setView(layoutResId) instead
                .setPositiveButton(R.string.ok, (d, w) -> dismiss(true))
                .create();

        setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void dismiss(boolean callback) {
        if (callback) {
            callbacks.onTimeSet(6, 8, 101);
        }
        dismiss();
    }

    public interface Callbacks {

        void onTimeSet(int timeSigBeats, int timeSigBeatValue, int tempoBPM);
    }

}
