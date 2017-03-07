package com.joshschriever.livenotes.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.joshschriever.livenotes.R;
import com.joshschriever.livenotes.musicxml.DurationHandler;

public class PrecisionDialogFragment extends DialogFragment {

    private Callbacks callbacks;
    int minimumPrecision;

    private ToggleButton thirtySecond;
    private ToggleButton sixteenth;
    private ToggleButton eighth;
    private ToggleButton quarter;

    public PrecisionDialogFragment() {
    }

    @SuppressWarnings("ValidFragment")
    public PrecisionDialogFragment(Callbacks callbacks, int beatType) {
        this.callbacks = callbacks;
        minimumPrecision = DurationHandler.minimumPrecisionForBeatType(beatType);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.precision_dialog_title)
                .setView(R.layout.dialog_precision)
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
        thirtySecond = (ToggleButton) dialog.findViewById(R.id.thirty_second);
        sixteenth = (ToggleButton) dialog.findViewById(R.id.sixteenth);
        eighth = (ToggleButton) dialog.findViewById(R.id.eighth);
        quarter = (ToggleButton) dialog.findViewById(R.id.quarter);

        View thirtySecondParent = dialog.findViewById(R.id.thirty_second_parent);
        View sixteenthParent = dialog.findViewById(R.id.sixteenth_parent);
        View eighthParent = dialog.findViewById(R.id.eighth_parent);
        View QuarterParent = dialog.findViewById(R.id.quarter_parent);

        if (minimumPrecision <= DurationHandler.PRECISION_THIRTY_SECOND) {
            thirtySecondParent.setOnClickListener(__ -> onThirtySecondClicked());
        } else {
            thirtySecondParent.setVisibility(View.GONE);
        }
        if (minimumPrecision <= DurationHandler.PRECISION_SIXTEENTH) {
            sixteenthParent.setOnClickListener(__ -> onSixteenthClicked());
        } else {
            sixteenthParent.setVisibility(View.GONE);
        }
        if (minimumPrecision <= DurationHandler.PRECISION_EIGHTH) {
            eighthParent.setOnClickListener(__ -> onEighthClicked());
        } else {
            eighthParent.setVisibility(View.GONE);
        }
        if (minimumPrecision <= DurationHandler.PRECISION_QUARTER) {
            QuarterParent.setOnClickListener(__ -> onQuarterClicked());
        } else {
            QuarterParent.setVisibility(View.GONE);
        }

        onEighthClicked();
    }

    private void onThirtySecondClicked() {
        if (!thirtySecond.isChecked()) {
            thirtySecond.setChecked(true);
            sixteenth.setChecked(false);
            eighth.setChecked(false);
            quarter.setChecked(false);
        }
    }

    private void onSixteenthClicked() {
        if (!sixteenth.isChecked()) {
            sixteenth.setChecked(true);
            thirtySecond.setChecked(false);
            eighth.setChecked(false);
            quarter.setChecked(false);
        }
    }

    private void onEighthClicked() {
        if (!eighth.isChecked()) {
            eighth.setChecked(true);
            thirtySecond.setChecked(false);
            sixteenth.setChecked(false);
            quarter.setChecked(false);
        }
    }

    private void onQuarterClicked() {
        if (!quarter.isChecked()) {
            quarter.setChecked(true);
            thirtySecond.setChecked(false);
            sixteenth.setChecked(false);
            eighth.setChecked(false);
        }
    }

    private void dismiss(boolean callback) {
        if (callback) {
            callbacks.onPrecisionSet(thirtySecond.isChecked()
                                     ? DurationHandler.PRECISION_THIRTY_SECOND
                                     : sixteenth.isChecked()
                                       ? DurationHandler.PRECISION_SIXTEENTH
                                       : eighth.isChecked()
                                         ? DurationHandler.PRECISION_EIGHTH
                                         : DurationHandler.PRECISION_QUARTER);
        }
        dismiss();
    }

    public interface Callbacks {

        void onPrecisionSet(int precision);
    }

}
