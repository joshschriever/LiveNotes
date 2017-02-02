package com.joshschriever.livenotes.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.joshschriever.livenotes.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SaveDialogFragment extends DialogFragment {

    private Callbacks callbacks;
    private String filename;

    public SaveDialogFragment() {
    }

    @SuppressWarnings("ValidFragment")
    public SaveDialogFragment(Callbacks callbacks) {
        this.callbacks = callbacks;
        filename = "Composition_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss", Locale.US)
                .format(Calendar.getInstance(TimeZone.getDefault()).getTime()) + ".xml";
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.save_musicxml_file)
                .setMessage(getString(R.string.save_score_as, filename))
                .setPositiveButton(R.string.save, (d, w) -> dismiss(true))
                .setNegativeButton(R.string.cancel, (d, w) -> dismiss(false))
                .create();

        setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void dismiss(boolean save) {
        if (save) {
            callbacks.onSave(filename);
        } else {
            callbacks.onCancelSaving();
        }
        dismiss();
    }

    public interface Callbacks {

        void onSave(String fileName);

        void onCancelSaving();
    }

}
