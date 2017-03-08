package com.joshschriever.livenotes.activity;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.joshschriever.livenotes.R;
import com.joshschriever.livenotes.midi.AdaptedMidiMessage;
import com.joshschriever.livenotes.musicxml.MidiToXMLRenderer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class LiveNotesActivityTest {

    @Rule
    public ActivityTestRule<LiveNotesActivity> activityRule
            = new ActivityTestRule<>(LiveNotesActivity.class);

    private LiveNotesActivity activity;

    @Before
    public void setup() {
        activity = activityRule.getActivity();
    }

    @Test
    public void test44Time100BpmFMajor() {
        onView(withText(R.string.ok)).perform(click());
        onView(withText(R.string.ok)).perform(click());
        onView(withText(R.string.ok)).perform(click());
        MidiToXMLRenderer renderer = activity.getMidiToXMLRenderer();

        renderer.messageReady(noteOn(48), 600);
        renderer.messageReady(noteOff(48), 1200);
        renderer.messageReady(noteOn(49), 1200);
        renderer.messageReady(noteOff(49), 1800);
        renderer.messageReady(noteOn(49), 1800);
        renderer.messageReady(noteOff(49), 2400);
        renderer.messageReady(noteOn(49), 2400);
        renderer.messageReady(noteOff(49), 3900);
        renderer.messageReady(noteOn(49), 3900);
        renderer.messageReady(noteOff(49), 4200);
        renderer.messageReady(noteOn(48), 4200);
        renderer.messageReady(noteOff(48), 4800);
        renderer.messageReady(noteOn(48), 4800);
        renderer.messageReady(noteOff(48), 5400);
        renderer.messageReady(noteOn(49), 5400);
        renderer.messageReady(noteOff(49), 6000);
        renderer.messageReady(noteOn(48), 6000);
        renderer.messageReady(noteOff(48), 6600);
        renderer.messageReady(noteOn(49), 6600);
        renderer.messageReady(noteOff(49), 7200);
        renderer.messageReady(noteOn(48), 7200);
        renderer.messageReady(noteOff(48), 7800);
        renderer.messageReady(noteOn(49), 9000);
        renderer.messageReady(noteOff(49), 12300);
        renderer.messageReady(noteOn(49), 12300);
        renderer.messageReady(noteOff(49), 12900);

        sleep(999);
        renderer.stopRecording();
        sleep(999999);

        //Should be | nu, sm, su, su | su, sm, nm, nu | sm, nm, sm, nm | r, sm | su, su, sm | su, r
    }

    private static AdaptedMidiMessage noteOn(int value) {
        return new AdaptedMidiMessage(AdaptedMidiMessage.STATUS_NOTE_ON, value);
    }

    private static AdaptedMidiMessage noteOff(int value) {
        return new AdaptedMidiMessage(AdaptedMidiMessage.STATUS_NOTE_OFF, value);
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
}