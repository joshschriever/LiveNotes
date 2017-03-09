package com.joshschriever.livenotes.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

import com.joshschriever.livenotes.R;
import com.joshschriever.livenotes.musicxml.KeySigHandler;

public class KeySigDialogFragment extends DialogFragment
        implements NumberPicker.OnValueChangeListener {

    private static final SparseIntArray KEY_SIG_DRAWABLES = new SparseIntArray(15);

    static {
        KEY_SIG_DRAWABLES.put(-7, R.drawable.key_flat_7);
        KEY_SIG_DRAWABLES.put(-6, R.drawable.key_flat_6);
        KEY_SIG_DRAWABLES.put(-5, R.drawable.key_flat_5);
        KEY_SIG_DRAWABLES.put(-4, R.drawable.key_flat_4);
        KEY_SIG_DRAWABLES.put(-3, R.drawable.key_flat_3);
        KEY_SIG_DRAWABLES.put(-2, R.drawable.key_flat_2);
        KEY_SIG_DRAWABLES.put(-1, R.drawable.key_flat_1);
        KEY_SIG_DRAWABLES.put(0, R.drawable.key_0);
        KEY_SIG_DRAWABLES.put(1, R.drawable.key_sharp_1);
        KEY_SIG_DRAWABLES.put(2, R.drawable.key_sharp_2);
        KEY_SIG_DRAWABLES.put(3, R.drawable.key_sharp_3);
        KEY_SIG_DRAWABLES.put(4, R.drawable.key_sharp_4);
        KEY_SIG_DRAWABLES.put(5, R.drawable.key_sharp_5);
        KEY_SIG_DRAWABLES.put(6, R.drawable.key_sharp_6);
        KEY_SIG_DRAWABLES.put(7, R.drawable.key_sharp_7);
    }

    private ImageView keySigImage;
    private NumberPicker key;
    private ToggleButton major;
    private ToggleButton minor;

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

        keySigImage = (ImageView) dialog.findViewById(R.id.key_sig_image);

        key = (NumberPicker) dialog.findViewById(R.id.key);
        key.setWrapSelectorWheel(false);
        key.setMinValue(0);
        key.setMaxValue(KeySigHandler.FIFTHS.length - 1);

        major.setChecked(false);
        onMajorClicked();
        key.setOnValueChangedListener(this);
        key.setValue(6);
    }

    @Override
    public void onResume() {
        super.onResume();
        onValueChange(key, 6, 6);
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

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        keySigImage.setImageDrawable(getResources().getDrawable(KEY_SIG_DRAWABLES.get(fifths()),
                                                                null));
    }

    private int fifths() {
        return KeySigHandler.FIFTHS[key.getValue()];
    }

    private void dismiss(boolean callback) {
        if (callback) {
            ((Callbacks) getActivity()).onKeySigSet(fifths(), major.isChecked());
        }
        dismiss();
    }

    public interface Callbacks {

        void onKeySigSet(int fifths, boolean isMajor);
    }

}
