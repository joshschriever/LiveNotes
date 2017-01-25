package com.joshschriever.livenotes.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.ScrollView;

import com.joshschriever.livenotes.R;
import com.joshschriever.livenotes.midi.MidiAdapter;
import com.joshschriever.livenotes.midi.MidiReceiver;
import com.joshschriever.livenotes.musicxml.MidiToXMLRenderer;
import com.joshschriever.livenotes.task.SingleParamResultlessTask;

import java.util.ArrayList;
import java.util.List;

import uk.co.dolphin_com.seescoreandroid.SeeScoreView;
import uk.co.dolphin_com.sscore.Component;
import uk.co.dolphin_com.sscore.LoadOptions;
import uk.co.dolphin_com.sscore.SScore;
import uk.co.dolphin_com.sscore.ex.ScoreException;

import static java8.util.J8Arrays.stream;
import static java8.util.stream.Collectors.toList;
import static java8.util.stream.StreamSupport.stream;
import static uk.co.dolphin_com.seescoreandroid.LicenceKeyInstance.SeeScoreLibKey;

public class LiveNotesActivity extends Activity implements MidiToXMLRenderer.Callbacks {

    private static final LoadOptions LOAD_OPTIONS = new LoadOptions(SeeScoreLibKey, true);
    private static final int PERMISSION_REQUEST_ALL_REQUIRED = 1;
    private static final List<String> REQUIRED_PERMISSIONS = new ArrayList<>();

    static {
        System.loadLibrary("stlport_shared");
        System.loadLibrary("SeeScoreLib");

        REQUIRED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private SeeScoreView scoreView;

    private MidiToXMLRenderer midiToXMLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_notes);

        checkPermissions();
    }

    private void checkPermissions() {
        String[] permissionsToRequest = stream(REQUIRED_PERMISSIONS)
                .filter(s -> checkSelfPermission(s) == PackageManager.PERMISSION_DENIED)
                .toArray(String[]::new);
        if (permissionsToRequest.length > 0) {
            requestPermissions(permissionsToRequest, PERMISSION_REQUEST_ALL_REQUIRED);
        } else {
            initialize();
        }
    }

    @Override
    public void onRequestPermissionsResult(int code,
                                           @NonNull String permissions[],
                                           @NonNull int[] results) {
        if (results.length > 0
                && stream(results).allMatch(r -> r == PackageManager.PERMISSION_GRANTED)) {
            initialize();
        } else {
            checkPermissions();
        }
    }

    private void initialize() {
        initializeWindow();
        initializeScoreView();
        initializeScore();
        initializeMidi();
    }

    private void initializeWindow() {
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON | LayoutParams.FLAG_FULLSCREEN);
    }

    private void initializeScoreView() {
        scoreView = new SeeScoreView(this,
                                     getAssets(),
                                     scale -> Log.i("ZoomNotification", "scale: " + scale),
                                     new SeeScoreView.TapNotification() {
                                         @Override
                                         public void tap(int systemIndex,
                                                         int partIndex,
                                                         int barIndex,
                                                         Component[] components) {
                                             Log.i("TapNotification", "tap");
                                         }

                                         @Override
                                         public void longTap(int systemIndex,
                                                             int partIndex,
                                                             int barIndex,
                                                             Component[] components) {
                                             Log.i("TapNotification", "longTap");
                                         }
                                     });

        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
        scrollView.addView(scoreView);
        scrollView.setOnTouchListener((view, event) -> scoreView.onTouchEvent(event));
    }

    private void initializeScore() {
        midiToXMLRenderer = new MidiToXMLRenderer(this);
        onXMLUpdated();
    }

    private void initializeMidi() {
        new MidiReceiver(this, new MidiAdapter(midiToXMLRenderer));
    }
//TODO - timing - it thinks everything is a whole note
    @Override
    public void onXMLUpdated() {
        setScoreXML(midiToXMLRenderer.getXML());
    }

    private void setScoreXML(String newXML) {
        Log.i("setScoreXML", newXML);
        new SingleParamResultlessTask<String>() {
            @Override
            protected void doInBackground(String xml) {
                try {
                    setScore(SScore.loadXMLData(xml.getBytes(), LOAD_OPTIONS));
                } catch (ScoreException e) {
                    e.printStackTrace();
                }
            }
        }.execute(newXML);
    }

    private void setScore(SScore score) {
        scoreView.setScore(score,
                           stream(new Boolean[score.numParts()])
                                   .map(__ -> Boolean.TRUE).collect(toList()),
                           1.0f);
    }

}
