package com.joshschriever.livenotes.activity;

import android.app.Activity;
import android.os.Bundle;

import com.joshschriever.livenotes.R;

public class LiveNotesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_notes);
    }

}
