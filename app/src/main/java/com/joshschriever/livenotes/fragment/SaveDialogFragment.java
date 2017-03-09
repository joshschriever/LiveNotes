package com.joshschriever.livenotes.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.joshschriever.livenotes.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SaveDialogFragment extends DialogFragment {

    private String filename;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        filename = "Composition_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss", Locale.US)
                .format(Calendar.getInstance().getTime()) + ".xml";

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
            ((Callbacks) getActivity()).onSave(filename);
        } else {
            ((Callbacks) getActivity()).onCancelSaving();
        }
        dismiss();
    }

    public interface Callbacks {

        void onSave(String fileName);

        void onCancelSaving();
    }

}
