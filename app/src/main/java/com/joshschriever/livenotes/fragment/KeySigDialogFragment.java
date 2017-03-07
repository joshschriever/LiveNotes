package com.joshschriever.livenotes.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

import com.joshschriever.livenotes.R;
import com.joshschriever.livenotes.musicxml.KeySigHandler;

//TODO - drawables?
public class KeySigDialogFragment extends DialogFragment {

    private Callbacks callbacks;

    private NumberPicker key;
    private ToggleButton major;
    private ToggleButton minor;

    public KeySigDialogFragment() {
    }

    @SuppressWarnings("ValidFragment")
    public KeySigDialogFragment(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.key_sig_dialog_title)
                .setView(R.layout.dialog_key_sig)
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
        major = (ToggleButton) dialog.findViewById(R.id.major);
        minor = (ToggleButton) dialog.findViewById(R.id.minor);
        dialog.findViewById(R.id.major_parent).setOnClickListener(__ -> onMajorClicked());
        dialog.findViewById(R.id.minor_parent).setOnClickListener(__ -> onMinorClicked());

        key = (NumberPicker) dialog.findViewById(R.id.key);
        key.setWrapSelectorWheel(false);
        key.setMinValue(0);
        key.setMaxValue(KeySigHandler.FIFTHS.length - 1);

        onMajorClicked();
        key.setValue(6);
    }

    private void onMajorClicked() {
        if (!major.isChecked()) {
            major.setChecked(true);
            minor.setChecked(false);
            key.setDisplayedValues(KeySigHandler.KEYS_MAJOR);
        }
    }

    private void onMinorClicked() {
        if (!minor.isChecked()) {
            minor.setChecked(true);
            major.setChecked(false);
            key.setDisplayedValues(KeySigHandler.KEYS_MINOR);
        }
    }

    private void dismiss(boolean callback) {
        if (callback) {
            callbacks.onKeySigSet(KeySigHandler.FIFTHS[key.getValue()], major.isChecked());
        }
        dismiss();
    }

    public interface Callbacks {

        void onKeySigSet(int fifths, boolean isMajor);
    }

}
