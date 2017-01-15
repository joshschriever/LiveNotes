package uk.co.dolphin_com.seescoreandroid;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.dolphin_com.sscore.SScore;
import uk.co.dolphin_com.sscore.ex.ScoreException;
import uk.co.dolphin_com.sscore.playdata.Bar;
import uk.co.dolphin_com.sscore.playdata.BarIterator;
import uk.co.dolphin_com.sscore.playdata.PlayData;
import uk.co.dolphin_com.sscore.playdata.PlayData.PlayControls;
import uk.co.dolphin_com.sscore.playdata.UserTempo;

/**
 * Player creates PlayData from the SScore and creates a Dispatcher to drive bar start, beat and note handlers.
 * It (optionally) also creates a temporary MIDI file and plays it using the Android MediaPlayer
 * The aim is for the dispatched handlers to move a cursor in time with the playing of the MediaPlayer
 * With careful timing this usually succeeds!
 */
public class Player {

    private static final int kDefaultTempoBPM = 80;

    /**
     * An exception from the Player
     */
    public static class PlayerException extends Exception {
        PlayerException(String detailMessage) {
            super(detailMessage);
        }
    }

    /**
     * the state of the Player
     */
    public enum State {
        NotStarted, Started, Paused, Stopped, Completed
    }

    /**
     * The state of the MediaPlayer
     * there is a chart of these states in the MediaPlayer documentation
     */
    public enum MediaPlayerState {
        Null, Idle, Initialized, Prepared, Started, Stopped, PlaybackCompleted, Paused, Error
    }

    /**
     * Construct the Player
     *
     * @param score     the score to play
     * @param userTempo access to UI (eg slider) which defines the tempo
     * @param context   the context (eg MainActivity)
     * @param playNotes true to use the MediaPlayer to play the notes, else it dispatches events silently
     * @throws PlayerException on error
     */
    public Player(SScore score, UserTempo userTempo, Context context, boolean playNotes, PlayControls playControls,
                  int startLoopBarIndex, int endLoopBarIndex, int numRepeats) throws PlayerException {
        mediaPlayerState = MediaPlayerState.Null;
        this.context = context;
        this.userTempo = userTempo;
        this.playNotes = playNotes;
        this.playControls = playControls;
        try {
            if (numRepeats > 0 && startLoopBarIndex >= 0 && endLoopBarIndex >= 0)
                playData = new PlayData(score, userTempo, startLoopBarIndex, endLoopBarIndex, numRepeats);
            else
                playData = new PlayData(score, userTempo);
            tempoType = (score.hasDefinedTempo()) ? TempoType.scaled : TempoType.absolute;
        } catch (ScoreException e) {
            e.printStackTrace();
            throw new PlayerException("cannot create PlayData");
        }

        dispatcher = new Dispatcher(playData, new Runnable() {
            @Override
            public void run() {
                // set state on end of play
                state = State.Completed;
            }
        });
        state = State.NotStarted;
        if (playNotes) { // setup MediaPlayer
            File documentsDir = getDocumentsDir(context);
            midiFilePath = documentsDir.getAbsolutePath() + File.separator + "midifile.mid";
            boolean ok = playData.createMIDIFileWithControls(midiFilePath, playControls);
            if (!ok)
                throw new PlayerException("cannot create MIDI file " + midiFilePath);
            scaleMidiFileTempo();
        }
    }

    public Player(SScore score, UserTempo userTempo, Context context, boolean playNotes, PlayControls playControls) throws PlayerException {
        new Player(score, userTempo, context, playNotes, playControls, -1, -1, 0);
    }

    public void updateMedia()
    {
        boolean ok = playData.createMIDIFileWithControls(midiFilePath, playControls);
        scaleMidiFileTempo();
    }

    /**
     * get the duration of the shortest bar
     * @return the minimum ms duration of any bar in the play data
     */
    public int minBarDuration()
    {
        int min = 1000000;
        for (Bar bar : playData) {
            if (bar.duration < min)
                min = bar.duration;
        }
        return min;
    }

    /**
     * min bar time of less than 2s requires fast cursor
     * We use this to disable the note cursor which takes a lot of processing and can fill up the event queue
     * faster than it can be emptied
     * @return true if the min bar time is less than 2s
     */
    public boolean needsFastCursor()
    {
        return minBarDuration() < 2000;
    }

    /**
     * get a value to use for the scroll animation so it scrolls faster for a fast score
     * @return ms animation time hint
     */
    public int bestScrollAnimationTime()
    {
        int minBarTime = minBarDuration();
        if (minBarTime < 4000)
            return minBarTime/2; // for min bar less than 4s use half bar time so animation isn't slower than bar time
        else
            return 2000; // 2s is the maximum time to animate the cursor
    }

    /**
     * the bar which was last dispatched
     * @return the current bar
     */
    public int currentBar()
    {
        return currentBar;
    }

    /**
     * stop playing and reset to start
     */
    public void reset() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            mediaPlayerState = MediaPlayerState.Idle;
            state = State.NotStarted;
        }
        dispatcher.stop();
        currentBar = 0;
    }

    public void stop()
    {
        int cb = currentBar;
        reset();
        currentBar = cb;
    }

    public void restart(boolean countIn)
    {
        startAt(currentBar, countIn);
    }

    /**
     * notification that the tempo has changed (eg when the user has changed a tempo slider)
     * Everything is stopped and restarted at the start of the current bar with the new tempo
     * @throws PlayerException on error
     */
    public void updateTempo() throws PlayerException
    {
        boolean wasPlaying = state == State.Started;
        dispatcher.stop();
        currentBar = dispatcher.currentBar();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayerState = MediaPlayerState.Idle;
            state = State.NotStarted;
            scaleMidiFileTempo();
        }
        if (wasPlaying)
            startAt(currentBar, false/*countIn*/);
    }

    /**
     * the current Player.State
     * @return the state
     */
    public State state() {
        return state;
    }

    /**
     * pause play and dispatch
     */
    public void pause()
    {
        if (state == State.Started) {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                mediaPlayerState = MediaPlayerState.Paused;
            }
            dispatcher.pause();
            currentBar = dispatcher.currentBar();
            state = State.Paused;
        }
    }

    /**
     * resume from the start of the current bar after pause (with count-in)
     */
    public void resume()
    {
        if (state() == State.Paused) {
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayerState = MediaPlayerState.Started;
            }
            dispatcher.resume();
            state = State.Started;
        }
    }

    /**
     * start playing and dispatching handlers from the given bar with optional count-in
     * @param barIndex 0-based bar index to start at
     * @param countIn if true a count-in bar is played before the first bar
     */
    public void startAt(final int barIndex, boolean countIn)
    {
        currentBar = barIndex;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, 1000);
        Date startTime = cal.getTime();
        Bar startBar = null;
        for (Bar bar : playData) {
            if (bar.index == barIndex) {
                startBar = bar;
                break;
            }
        }
        if (startBar != null) {
            if (mediaPlayer == null)
            {
                try {
                    mediaPlayer =  createMediaPlayer(context, new File(midiFilePath));
                } catch (PlayerException e) {
                    e.printStackTrace();
                    System.out.println("could not construct MediaPlayer" + e);
                }
            }
            if (mediaPlayer != null) {
                prepareMediaPlayer();
                 if (countIn) {
                    Bar countInBar = startBar.createCountIn();
                    cal.add(Calendar.MILLISECOND, countInBar.duration);
                }
                Date playStartTime = cal.getTime();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    public void run() {
                        try {
                            mediaPlayer.start();
                            if (mediaPlayer.isPlaying() && barIndex > 0)
                                mediaPlayer.seekTo(dispatcher.barStartTime(barIndex));
                        } catch (IllegalStateException ex) {
                            System.out.println("mediaplayer illegal state " + ex);
                        } catch (Exception ex) {
                            System.out.println("mediaplayer error " + ex);
                        }
                    }
                }, playStartTime);
            }
            dispatcher.startAt(startTime, barIndex, countIn);
            state = State.Started;
        }
    }

    /**
     * set the handler to be called on each bar start
     * @param handler the bar start handler - index argument is 0-based bar index. ci is true for a count-in bar
     * @param delay_ms the delay from the event to calling the handler. Can be negative to anticipate the event
     */
    public void setBarStartHandler(Dispatcher.EventHandler handler, int delay_ms) {
        dispatcher.setBarStartHandler(handler, delay_ms);
    }

    /**
     * set the handler to ba called on each beat
     * @param handler the beat handler - index argument to event is 0-based beat index. ci is true for a count-in bar
     * @param delay_ms the delay from the event to calling the handler. Can be negative to anticipate the event
     */
    public void setBeatHandler(Dispatcher.EventHandler handler, int delay_ms) {
        dispatcher.setBeatHandler(handler, delay_ms);
    }

    /**
     * set the handler to be called at end of play (not on stop)
     * @param handler the end handler - called on completion of play, but not on stop or pause
     * @param delay_ms the delay from the event to calling the handler. Can be negative to anticipate the event
     */
    public void setEndHandler(Dispatcher.EventHandler handler, int delay_ms) {
        dispatcher.setEndHandler(handler, delay_ms);
    }

    /**
     * set the handler to be called for each note or chord as it is played
     * @param handler the note handler - called with a list of notes (chord) starting at this time
     * @param delay_ms the delay from the event to calling the handler. Can be negative to anticipate the event
     */
    public void setNoteHandler(Dispatcher.NoteEventHandler handler, int delay_ms) {
        dispatcher.setNoteHandler(handler, delay_ms);
    }

    /** we scale the tempo in the MIDI file by altering the tempo bytes in the file */
    private void scaleMidiFileTempo()
    {
        if (midiFilePath != null) {
            if (tempoType == TempoType.scaled)
                PlayData.scaleMIDIFileTempo(midiFilePath, userTempo.getUserTempoScaling());
            else
                PlayData.scaleMIDIFileTempo(midiFilePath, (float)userTempo.getUserTempo()/kDefaultTempoBPM);
        }
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    private static File getDocumentsDir(Context context) throws PlayerException {
        File documentsDir;
        if (isExternalStorageWritable()) {
            documentsDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC), "SeeScoreAndroid");
            if (!documentsDir.exists()) {
                if (!documentsDir.mkdirs())
                    throw new PlayerException("cannot create directory " + documentsDir);
            }
        } else {
            documentsDir = new File(context.getDir("SeeScoreAndroid", 0), "SeeScoreAndroid");
            if (!documentsDir.exists()) {
                if (!documentsDir.mkdirs())
                    throw new PlayerException("cannot create directory " + documentsDir);
            }
        }
        return documentsDir;
    }

    private MediaPlayer createMediaPlayer(Context context, File midiFile)  throws PlayerException
    {
        MediaPlayer mp = MediaPlayer.create(context, Uri.fromFile(midiFile));
        if (mp == null)
            throw new PlayerException("cannot create MediaPlayer");
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                mediaPlayerState = MediaPlayerState.Error;
                System.out.println("MediaPlayer error " + (i == MediaPlayer.MEDIA_ERROR_SERVER_DIED ? "server died" : i == MediaPlayer.MEDIA_ERROR_UNKNOWN ? "unknown" : "code: " + i));
                switch (i2) {
                    case MediaPlayer.MEDIA_ERROR_IO:
                        System.out.println("MEDIA_ERROR_IO");
                        break;
                    case MediaPlayer.MEDIA_ERROR_MALFORMED:
                        System.out.println("MEDIA_ERROR_MALFORMED");
                        break;
                    case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                        System.out.println("MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
                        break;
                    case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                        System.out.println("MEDIA_ERROR_TIMED_OUT");
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        System.out.println("MEDIA_ERROR_UNKNOWN");
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                        System.out.println("MEDIA_ERROR_UNSUPPORTED");
                        break;
                }
                return true;
            }
        });
        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i2) {
                System.out.println("MediaPlayer info " + i + " " + i2);
                return true;
            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayerState = MediaPlayerState.PlaybackCompleted;
            }
        });
        mediaPlayerState = MediaPlayerState.Prepared;
        return mp;
    }

    /** do whatever it takes to get to the Prepared state (see state diagram in the MediaPlayer documentation) */
    private void prepareMediaPlayer() {
        assert(mediaPlayer != null);
        updateMedia();
        try {
            for (int i = 0; i < 5 && mediaPlayerState != MediaPlayerState.Prepared
                    && mediaPlayerState != MediaPlayerState.PlaybackCompleted; ++i) {
                switch (mediaPlayerState) {

                    case Null: // not expected
                        mediaPlayer.reset();
                        mediaPlayerState = MediaPlayerState.Idle;
                        break;

                    case Idle:
                        mediaPlayer.setDataSource(midiFilePath);
                        mediaPlayerState = MediaPlayerState.Initialized;
                        break;

                    case Initialized:
                        mediaPlayer.prepare();
                        mediaPlayerState = MediaPlayerState.Prepared;
                        break;

                    case Prepared: // this is what we are aiming for
                        break;

                    case Started:
                        mediaPlayer.stop();
                        mediaPlayerState = MediaPlayerState.Stopped;
                        break;

                    case Stopped:
                        mediaPlayer.prepare();
                        mediaPlayerState = MediaPlayerState.Prepared;
                        break;

                    case PlaybackCompleted: // this is effectively the same as Prepared
                        break;

                    case Paused:
                        mediaPlayer.stop();
                        mediaPlayerState = MediaPlayerState.Stopped;
                        break;

                    case Error:
                        mediaPlayer.reset();
                        mediaPlayerState = MediaPlayerState.Idle;
                        break;
                }
            }
        }
        catch (IOException ex){
            System.out.println(" " + ex);
        }
        catch (Exception ex){
            System.out.println(" " + ex);
        }
    }

    private Context context;
    private State state = State.NotStarted;
    private MediaPlayerState mediaPlayerState;
    private PlayData playData;
    private Dispatcher dispatcher;
    private int currentBar;
    private String midiFilePath;
    private UserTempo userTempo;
    private static enum TempoType {absolute, scaled}
    private TempoType tempoType;
    private boolean playNotes;
    private MediaPlayer mediaPlayer;
    private PlayControls playControls;
}
