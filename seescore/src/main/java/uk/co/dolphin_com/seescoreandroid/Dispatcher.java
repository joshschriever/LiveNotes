package uk.co.dolphin_com.seescoreandroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.dolphin_com.sscore.playdata.Bar;
import uk.co.dolphin_com.sscore.playdata.BarIterator;
import uk.co.dolphin_com.sscore.playdata.Note;
import uk.co.dolphin_com.sscore.playdata.Part;
import uk.co.dolphin_com.sscore.playdata.PlayData;

/**
 * dispatch events driven by SeeScore PlayData (bar start, beat, note, play end)
 */
public class Dispatcher {

    /**
     * the state of the dispatcher
     */
    public static enum State { Started, Stopped, Paused }

    /**
     * a generic handler for bar start, beat and end
     */
    public interface EventHandler {
        /**
         * called at the event for bar start, beat and end
         * @param index bar index for bar change handler, beat index for beat handler, and unused for endHandler
         * @param countIn is true for count-in bar(s)
         */
        public void event(int index, boolean countIn);
    }

    /**
     * a handler for notes starting
     */
    public interface NoteEventHandler {
        /**
         * called for each note/chord starting
         * @param notes note or list of notes in chord starting
         */
        public void startNotes(List<Note> notes);
    }

    /**
     * wrapper for an event handler to convey the delay time
     */
    static class EventHandlerWrapper
    {
        EventHandlerWrapper(EventHandler eventHandler, int delay_ms)
        {
            this.eventHandler = eventHandler;
            this.delay_ms = delay_ms;
        }
        EventHandler eventHandler;
        int delay_ms;
    }

    /**
     * wrapper for a note event handler to convey the delay time
     */
    static class NoteEventHandlerWrapper
    {
        NoteEventHandlerWrapper(NoteEventHandler eventHandler, int delay_ms)
        {
            this.eventHandler = eventHandler;
            this.delay_ms = delay_ms;
        }
        NoteEventHandler eventHandler;
        int delay_ms;
    }

    static class DispatchItem {
        DispatchItem(Timer timer, final Runnable task, Date callTime)
        {
            this.task = task;
            this.handlerCallTime = callTime;
            isCancelled = false;
            isComplete = false;
            timer.schedule(new TimerTask() {
                public void run() {
                    if (!isCancelled) {
                        DispatchItem.this.task.run();
                        isComplete = true;
                    }

                }

            }, callTime);
        }

        void cancel()
        {
            isCancelled = true;
        }
        boolean isCancelled;
        boolean isComplete;
        private final Runnable task;
        private final Date handlerCallTime;
    }

    static class PausedItem {
        PausedItem(Date now, DispatchItem ditem)
        {
            this.task = ditem.task;
            millisecondToRun = ditem.handlerCallTime.getTime() - now.getTime();
        }

        Runnable task;
        long millisecondToRun;
    }


    /**
     * construct the Dispatcher
     * @param playData the PlayData derived from the score
     * @param playEndHandler a handler to call on true end of play to update state in the caller
     *  NB This is not the same as the registered end handler which may have an associated delay
     */
    public Dispatcher(PlayData playData, Runnable playEndHandler)
    {
        this.playData = playData;
        this.playEndHandler = playEndHandler;
        this.dispatchItems = new ArrayList<DispatchItem>();
        this.pausedItems = new ArrayList<PausedItem>();
    }

    void dispatch(Runnable task, Date handlerCallTime)
    {
        dispatchItems.add(new DispatchItem(getTimer(), task, handlerCallTime));
    }
    /**
     * the current state of the Dispatcher
     * @return the state, Started or Stopped
     */
    public State state() { return state;}

    private void scheduleBarStartHandler(final Bar bar, final Date barStartTime)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(barStartTime);
        if (barStartHandler != null && barStartHandler.delay_ms != 0)
            cal.add(Calendar.MILLISECOND, barStartHandler.delay_ms);
        final Date handlerCallTime = cal.getTime();
        dispatch(new Runnable() {
            final int index = bar.index;
            final boolean countIn = bar.countIn;

            public void run() {
                lastBarStart = index;
                if (barStartHandler != null) {
                    barStartHandler.eventHandler.event(index, countIn);
                }
            }
        }, handlerCallTime);
    }

    private void scheduleBeatsForBar(final Bar bar, final Date barStartTime) {
        if (beatHandler != null) {
            Part metroPart = bar.metronome();
            Calendar cal = Calendar.getInstance();
            cal.setTime(barStartTime);
            if (barStartHandler.delay_ms != 0)
                cal.add(Calendar.MILLISECOND, barStartHandler.delay_ms);
            int lastStart = -1;
            for (final Note note : metroPart) {
                if (lastStart >= 0)
                    cal.add(Calendar.MILLISECOND, note.start - lastStart);
                dispatch(new Runnable() {
                    final int index = note.midiPitch;
                    final boolean countIn = bar.countIn;

                    public void run() {
                        beatHandler.eventHandler.event(index, countIn);
                    }
                }, cal.getTime());
                lastStart = note.start;
            }
        }
    }

    private List<Note> allNotesInBar(final Bar bar) {
        final ArrayList<Note> notes = new ArrayList<Note>();
        for (int partIndex = 0; partIndex < playData.numParts(); partIndex++) {
            Part part = bar.part(partIndex);
            for (final Note note : part) {
                notes.add(note);
            }
        }
        Collections.sort(notes, new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                return (n1.start < n2.start) ? -1
                        : n1.start == n2.start ? 0
                        : +1;
            }
        });
        return notes;
    }

    private void scheduleNoteChangeHandler(int noteStart_ms, Date barStartTime, final List<Note> notes) {
        if (noteHandler != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(barStartTime);
            if (noteHandler.delay_ms != 0)
                cal.add(Calendar.MILLISECOND, noteHandler.delay_ms);
            cal.add(Calendar.MILLISECOND, noteStart_ms);

            dispatch(new Runnable() {
                final List<Note> localNotes = new ArrayList<Note>(notes);// copy the list

                public void run() {
                    noteHandler.eventHandler.startNotes(localNotes);
                }
            }, cal.getTime());
        }
    }
    private void scheduleNotesForBar(final Bar bar, final Date barStartTime) {
        if (noteHandler != null && !bar.countIn){
            List<Note> notes = allNotesInBar(bar);
            // gather chords and schedule for each new start time
            ArrayList<Note> chord = new ArrayList<Note>();
            int lastStartTime = -1;
            for (Note note : notes)
            {
                if (note.start >= 0) // ignore notes with negative start times - these belong to the previous bar
                {
                    boolean isChord = (note.start == lastStartTime);
                    if (lastStartTime >= 0 && !isChord && chord.size() > 0) // new note time
                    {
                        scheduleNoteChangeHandler(lastStartTime, barStartTime, chord);
                        chord.clear();
                    }
                    chord.add(note);
                    lastStartTime = note.start;
                }
            }
            if (chord.size() > 0) // handle last chord in bar
            {
                scheduleNoteChangeHandler(lastStartTime, barStartTime, chord);
            }
        }
    }

    private void scheduleEndEvent(final Date endTime)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(endTime);
        if (endHandler != null && endHandler.delay_ms != 0)
            cal.add(Calendar.MILLISECOND, endHandler.delay_ms);
        Date handlerCallTime = cal.getTime();
        dispatch(new Runnable() {
            public void run() {
                if (endHandler != null) {
                    endHandler.eventHandler.event(0, false);
                }
            }
        }, handlerCallTime);
        dispatch(new Runnable() {
            public void run() {
                state = State.Stopped;
                if (playEndHandler != null)
                    playEndHandler.run();
            }
        }, endTime);
    }

    private void scheduleBarEvents(final Bar bar, final Date barStartTime)
    {
        scheduleBarStartHandler(bar, barStartTime);
        scheduleBeatsForBar(bar, barStartTime);
        scheduleNotesForBar(bar, barStartTime);
    }

    /**
     * start dispatching at the given start time. The bar start handler will be called for the first bar at startTime
     * @param startTime the time to start
     * @param barIndex the 0-based bar index to start at
     * @param countIn if true a count-in bar (copy of the barIndex bar with a maximum of 4 beats and the count-in flag set) is played first
     */
    public void startAt(Date startTime, int barIndex, boolean countIn) {
        lastBarStart = barIndex;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);

        boolean reachedStart = false;
        for (Bar bar : playData) {
            if (!reachedStart && bar.index == barIndex)
                reachedStart = true;
            if (reachedStart)
            {
                if (countIn) {
                    Bar countInBar = bar.createCountIn();
                    // schedule events for the count-in bar
                    scheduleBarEvents(countInBar, startTime);
                    cal.add(Calendar.MILLISECOND, countInBar.duration);
                    countIn = false;
                }
                // schedule events for the first bar
                scheduleBarEvents(bar, cal.getTime());
                cal.add(Calendar.MILLISECOND, bar.duration);
            }
        }
        // schedule the play end event
        scheduleEndEvent(cal.getTime());

        state = State.Started;
    }

    /**
     * stop dispatching
     */
    public void stop()
    {
        if ((state == State.Started || state == State.Paused) && localTimer != null) {
            localTimer.cancel();
            localTimer = null;
            state = State.Stopped;
        }
        for (DispatchItem ditem : dispatchItems) {
            ditem.cancel();
        }
        pausedItems.clear();
        dispatchItems.clear();
    }

    /**
     * pause the Dispatcher
     */
    public void pause()
    {
        if (state == State.Started && localTimer != null) {
            state = State.Paused;
            pausedItems.clear();
            localTimer.cancel();
            localTimer = null;
            Date now = new Date();
            for (DispatchItem ditem : dispatchItems) {
                ditem.cancel();
                if (!ditem.isComplete)
                    pausedItems.add(new PausedItem(now, ditem));
            }
            dispatchItems.clear();
        }
    }

    /**
     * resume the Dispatcher
     */
    public void resume()
    {
        if (state == State.Paused) {
            Date now = new Date();
            // reschedule the paused items
            for (PausedItem pitem : pausedItems) {
                dispatch(pitem.task, new Date(now.getTime() + pitem.millisecondToRun));
            }
            if (pausedItems.size() > 0)
                state = State.Started;
            else
                state = State.Stopped;
        }
    }

    /**
     * get the last bar which was dispatched
     * @return the current bar
     */
    public int currentBar() {
        return lastBarStart;
    }

    /**
     * time in ms from the start of the score to the start of the bar
     * @return ms time to the bar
     */
    public int barStartTime(int barIndex)
    {
        int time = 0;
        for (Bar bar : playData) {
            if (bar.index < barIndex)
                time += bar.duration;
            else
                return time;
        }
        return time;
    }

    /**
     * register an event handler to be called at the start of each bar
     * @param handler the handler to be called at the start of each bar
     * @param delay_ms a millisecond delay for the handler call, which can be negative to anticipate the bar change eg for an animated cursor
     */
    public void setBarStartHandler(EventHandler handler, int delay_ms)
    {
        barStartHandler = new EventHandlerWrapper(handler, delay_ms);
    }

    /**
     * register an event handler to be called on each beat in the bar
     * @param handler the handler to be called on each beat
     * @param delay_ms a millisecond delay for the handler call, which can be negative to anticipate the event
     */
    public void setBeatHandler(EventHandler handler, int delay_ms)
    {
        beatHandler = new EventHandlerWrapper(handler, delay_ms);
    }

    /**
     * register an event handler to be called on completion of play. NB It is not called when the dispatcher is stopped by a call to stop
     * @param handler the handler to be called at the end of play
     * @param delay_ms a millisecond delay for the handler call, which can be negative to anticipate the event
     */
    public void setEndHandler(EventHandler handler, int delay_ms)
    {
        endHandler = new EventHandlerWrapper(handler, delay_ms);
    }

    /**
     * register an event handler to be called on the start of new note/chord
     * This can be used to move a cursor onto each note as it is played.
     * NB for a piece with many fast notes you need to ensure your handler is fast enough to handle the throughput.
     * @param handler the handler to be called at the start of each note
     * @param delay_ms a millisecond delay for the handler call, which can be negative to anticipate the event
     */
    public void setNoteHandler(NoteEventHandler handler, int delay_ms)
    {
        noteHandler = new NoteEventHandlerWrapper(handler, delay_ms);
    }

    private Timer createTimer()
    {
        return new Timer("uk.co.dolphin_com.sscore.Dispatcher");
    }

    private Timer getTimer()
    {
        if (localTimer == null)
            localTimer = createTimer();
        return localTimer;
    }

    private State state = State.Stopped;
    private PlayData playData;
    private Timer localTimer;
    private EventHandlerWrapper barStartHandler;
    private EventHandlerWrapper beatHandler;
    private EventHandlerWrapper endHandler;
    private NoteEventHandlerWrapper noteHandler;
    private int lastBarStart;
    private Runnable playEndHandler;

    private ArrayList<DispatchItem> dispatchItems;
    private ArrayList<PausedItem> pausedItems;
}

