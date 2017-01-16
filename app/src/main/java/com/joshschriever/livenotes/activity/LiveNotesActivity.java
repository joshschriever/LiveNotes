package com.joshschriever.livenotes.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ScrollView;

import com.joshschriever.livenotes.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.dolphin_com.seescoreandroid.LicenceKeyInstance;
import uk.co.dolphin_com.seescoreandroid.SeeScoreView;
import uk.co.dolphin_com.sscore.Component;
import uk.co.dolphin_com.sscore.LoadOptions;
import uk.co.dolphin_com.sscore.SScore;
import uk.co.dolphin_com.sscore.ex.ScoreException;

import static java8.util.Spliterators.spliterator;
import static java8.util.stream.Collectors.toList;
import static java8.util.stream.StreamSupport.intStream;
import static java8.util.stream.StreamSupport.stream;

public class LiveNotesActivity extends Activity {

    public static final LoadOptions loadOptions
            = new LoadOptions(LicenceKeyInstance.SeeScoreLibKey, true);

    private static final int PERMISSION_REQUEST_ALL_REQUIRED = 1;
    private static final List<String> REQUIRED_PERMISSIONS;

    static {
        System.loadLibrary("stlport_shared");
        System.loadLibrary("SeeScoreLib");

        REQUIRED_PERMISSIONS = new ArrayList<>();
        REQUIRED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private SeeScoreView scoreView;

    private SScore score;

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
    public void onRequestPermissionsResult(int code, String permissions[], int[] results) {
        if (results.length > 0 && intStream(spliterator(results, 0), false)
                .allMatch(result -> result == PackageManager.PERMISSION_GRANTED)) {
            initialize();
        } else {
            checkPermissions();
        }
    }

    private void initialize() {
        initializeScoreView();
        initializeScore();
    }

    private void initializeScoreView() {
        scoreView = new SeeScoreView(this,
                                     getAssets(),
                                     scale -> Log.d("ZoomNotification", "scale: " + scale),
                                     new SeeScoreView.TapNotification() {
                                         @Override
                                         public void tap(int systemIndex,
                                                         int partIndex,
                                                         int barIndex,
                                                         Component[] components) {
                                             Log.d("TapNotification", "tap");
                                         }

                                         @Override
                                         public void longTap(int systemIndex,
                                                             int partIndex,
                                                             int barIndex,
                                                             Component[] components) {
                                             Log.d("TapNotification", "longTap");
                                         }
                                     });

        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
        scrollView.addView(scoreView);
        scrollView.setOnTouchListener((view, event) -> scoreView.onTouchEvent(event));
    }

    private void initializeScore() {
        File file = new File(Environment.getExternalStorageDirectory(), "BeetAnGeSample.xml");
        try {
            score = SScore.loadXMLFile(file, loadOptions);
        } catch (ScoreException e) {
            Log.e("ScoreException", e.getMessage());
            e.printStackTrace();
        }

        scoreView.setScore(score,
                           stream(new ArrayList<Boolean>(score.numParts()))
                                   .map(__ -> Boolean.TRUE).collect(toList()),
                           1.0f);
    }

}
