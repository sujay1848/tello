package com.nielsen.cloudapi.model;

/*
 * 27.Mar.14    LFR    Integrated the new MediaPlayerExtension
 * 01.Apr.14    LFR    Included the new MPX that supplies the version
 */

import java.util.ArrayList;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.util.Log;
import android.view.SurfaceHolder;

import com.nielsen.cloudapi.activity.MasterActivity;
import com.nielsen.cloudapi.fragment.VideosFragment;
import com.nielsen.mpx.id3extractor.ID3TAGEventListener;
import com.nielsen.mpx.mediaplayer.MediaPlayerExtension;

public class Player implements OnBufferingUpdateListener, OnCompletionListener,
                               OnPreparedListener, OnVideoSizeChangedListener,
                               OnErrorListener, ID3TAGEventListener

{

    private final String TAG = Player.class.getSimpleName();
    public String mpxVersion = "unknown";

    private MediaPlayerExtension mMediaPlayer = null;
    private VideosFragment         videoFragment = null;
    private SurfaceHolder        mSurfaceHolder;
    private static Player        mInstance = null;
    private Context              mContext = null;

    private int mVideoWidth, mVideoHeight;

    private PLAYER_STATE mPlayerState = PLAYER_STATE.NOT_INITIALIZED;
    private enum PLAYER_STATE {
        NOT_INITIALIZED,
        ASYNC_PREPAIRE,
        PLAYING,
        PAUSED,
        STOPPED,
    }

/************************************************************************************ PUBLIC INTERFACE METHODS ***/
    public static Player getInstance(Context context, VideosFragment mainActivity) throws Exception
    {
        if (mInstance == null) {
            mInstance = new Player(context, mainActivity);
        }
        return mInstance;
    }

    private Player(Context context, VideosFragment videoFragment) throws Exception
    {
        Log.i(TAG, "Creating media player.");

        this.videoFragment = videoFragment;
        if (this.videoFragment == null)
            throw new Exception("this.videoFragment == null");

        mMediaPlayer = new MediaPlayerExtension(context);
        if (mMediaPlayer == null)
            throw new Exception("MediaPlayerExtension can NOT be created");

        mContext = context;
        mpxVersion = mMediaPlayer.getVersionInfo();
        mMediaPlayer.setID3TagListener(this);
        mSurfaceHolder = this.videoFragment.uiGetPlayerView().getHolder();
    }

    public boolean isPlaying()
    {
        return ((mMediaPlayer != null) &&
                (mMediaPlayer.isPlaying()));
    }

    public boolean isActivated()
    {
        boolean ret = false;
        if (mMediaPlayer != null) {
            PLAYER_STATE state = getPlayerState();
            switch (state) {
            case ASYNC_PREPAIRE:
            case PLAYING:
            case PAUSED:
            case STOPPED:
                ret = true;
                break;
            case NOT_INITIALIZED:
            default:
                ret = false;
            }
        }
        return ret;
    }

    public int videoDuration()
    {
        if (mMediaPlayer == null)
            return 0;
        else
            return mMediaPlayer.getDuration() / 1000;
        
    }

    public int videoPosition()
    {
        if (mMediaPlayer == null)
            return 0;
        else
            return mMediaPlayer.getCurrentPosition() / 1000;
    }

    public boolean changeChannel(String movUrl)
    {
        if (mMediaPlayer == null)
            return false;

        if (isPlaying())
            resetMediaPlayer();

        return prepareMedia(movUrl);
    }

    public boolean prepareMedia(String movUrl)
    {
        Log.i(TAG, "Preparing media player item: " + movUrl);

        if ((this.videoFragment == null) ||
                (this.videoFragment != null && MasterActivity.mAppInBackground))
            return false;

        if(!NetworkStatusReceiver.getConnectedStatus(mContext)){
            this.videoFragment.uiSetPopMessage("Media Player: Network unavailable. Please check internet connection.");
            this.videoFragment.uiResetPlayPause();
            return false;
        }

        if (mMediaPlayer != null && !isPlaying()) {

            if ((movUrl == null) || (movUrl.equals(""))) {
                /** Tell the user to provide a media file URL */
                this.videoFragment.uiSetPopMessage("Media Player: No URL specified");
                this.videoFragment.uiResetPlayPause();
                return false;
            }

            try {
                Log.i(TAG, "To Play =" + movUrl);
                /** set the listeners for the media player */
                if (!setPlayerState(PLAYER_STATE.ASYNC_PREPAIRE)) {
                    return false;
                }

                doCleanUp();
                this.videoFragment.uiSetProgressDialog("Loading... please wait");

                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(movUrl);
                mMediaPlayer.setDisplay(mSurfaceHolder);

                mMediaPlayer.setOnBufferingUpdateListener(this);
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setOnVideoSizeChangedListener(this);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepareAsync();
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "error in playing the url");
                this.videoFragment.uiResetPlayPause();
                this.videoFragment.uiSetAlertDialog("Error in playback", e.getMessage());
            }
        }
        return false;
    }

    public void stopVideo()
    {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            resetMediaPlayer();
        }
    }

    public boolean pauseVideo()
    {
        boolean ret = false;
        if (isPlaying()) {
            try {
                if (setPlayerState(PLAYER_STATE.PAUSED)) {
                    mMediaPlayer.pause();
                    ret = true;
                }
            } catch (Exception e) {
                Log.d(TAG, "pauseVideo(): Illegal state");
            }
        }
        return ret;
    }

    public boolean continueVideo()
    {
        boolean ret = false;
        if (mMediaPlayer != null && !isPlaying()) {
            try {
                if (setPlayerState(PLAYER_STATE.PLAYING)) {
                    mMediaPlayer.setDisplay(mSurfaceHolder);
                    mMediaPlayer.start();
                    ret = true;
                }
            } catch (Exception e) {
                Log.d(TAG, "continueVideo(): Illegal state");
            }
        }
        return ret;
    }

    public void setPlayhead(int nePosition)
    {
        if (isPlaying()) {
            try {
                mMediaPlayer.seekTo(nePosition * 1000);
            } catch (Exception e) {
                Log.d(TAG, "setPlayhead(): Illegal state");
            }
        }
    }

    public void releaseMediaPlayer() throws Exception
    {
        if (this.videoFragment != null)
            this.videoFragment.setPlayerActive(false);

        if (mMediaPlayer != null) {
            setPlayerState(PLAYER_STATE.NOT_INITIALIZED);
            mMediaPlayer.release();
            mMediaPlayer = null;
            mInstance = null;
        }
    }

    public void resetMediaPlayer()
    {
        if (mMediaPlayer != null) {
            try {
                if (setPlayerState(PLAYER_STATE.STOPPED)) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }
            } catch (Exception e) {
                Log.d(TAG, "resetMediaPlayer(): Illegal state");
            }
        }
    }

/******************************************************************************************** INTERNAL METHODS ***/

    /**
    * receive the extracted ID3 tags from the MediaPlayerExtensionSDK and
    * update the UI
    */

    private void doCleanUp()
    {
        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private boolean startVideoPlayback()
    {
        boolean ret = false;
        Log.v(TAG, "startVideoPlayback");
        if ((mVideoWidth > 0) && (mVideoHeight > 0))
            //mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
        if (mMediaPlayer != null && !isPlaying()  && this.videoFragment != null) {
            try {
                if (setPlayerState(PLAYER_STATE.PLAYING)) {
                    mMediaPlayer.start();
                    this.videoFragment.uiResetProgressDialog();
                    ret = true;
                }
            } catch (Exception e) {
                Log.d(TAG, "startVideoPlayback(): Illegal state");
            }
        }
        return ret;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        Log.d(TAG, "onError called");
        try {
            resetMediaPlayer();
            releaseMediaPlayer();
            if(this.videoFragment != null) {
                this.videoFragment.uiResetPlayPause();
                this.videoFragment.uiSetAlertDialog("Media Player Error", "Media player error codes:" + what + ", " + extra);
            }
        } catch (Exception e) {
            // TODO: add log? 
        }
        return true;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height)
    {
        Log.v(TAG, String.format("onVideoSizeChanged  width: %d height: %d", width, height));

        if (width == 0 || height == 0) {
            Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
            return;
        }
        //mVideoWidth     = width;
        //mVideoHeight    = height;
        startVideoPlayback();
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        Log.d(TAG, "onPrepared called");
        startVideoPlayback();
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        Log.d(TAG, "onCompletion called");
        if(this.videoFragment != null) {
            this.videoFragment.uiSetAlertDialog("Playback Completed", "Playback of the stream has completed");
            this.videoFragment.uiResetPlayPause();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent)
    {
        Log.d(TAG, "onBufferingUpdate percent:" + percent);
    }

    @Override
    public void getID3TagData(ArrayList<String> id3Tags)
    {
        if ((id3Tags != null) && (id3Tags.size() > 0) && (this.videoFragment != null)) {
            for (int j = 0; j < id3Tags.size(); j++) {
                String temp = new String(id3Tags.get(j)); // + System.getProperty("line.separator");
                Log.d(TAG, "id3Tag #" + j + " [" + temp + "]");
                this.videoFragment.appProcessID3tag(temp);
            }
        }
    }

    private PLAYER_STATE getPlayerState()
    {
        return mPlayerState;
    }

    private boolean setPlayerState(PLAYER_STATE state) throws Exception
    {
        Exception e = null;
        boolean ret = true;
        Log.d(TAG, "setPlayerState: mPlayerState=" + mPlayerState + ", state = " + state);
        switch (mPlayerState) {
        case NOT_INITIALIZED: 
            switch (state) {
            case ASYNC_PREPAIRE:
                mPlayerState = state;
                break;
            case PLAYING:
            case PAUSED:
            case STOPPED:
                e = new Exception("Illegal Player state transition!"); 
                break;
            case NOT_INITIALIZED: 
            default:
                ret = false;
                break;
            }
            break;
        case ASYNC_PREPAIRE: 
            switch (state) {
            case PLAYING: // Playback started
            case STOPPED: // Player being reset while preparing
            case NOT_INITIALIZED: // drop everything
                mPlayerState = state;
                break;
            case PAUSED:
                e = new Exception("Illegal Player state transition!");
                break;
            case ASYNC_PREPAIRE: //PlayVideo() called second time. Not good
            default:
                ret = false;
                break;
            }
            break;
        case PLAYING: 
            switch (state) {
            case ASYNC_PREPAIRE: // logical error above
                e = new Exception("Illegal Player state transition!");
                break;
            case STOPPED: // Player being reset. Likely we're changing channel or destroying app
            case NOT_INITIALIZED: // drop everything
            case PAUSED:
                mPlayerState = state;
                break;
            case PLAYING:
            default:
                //should NOT be here
                break;
            }
            break;
        case PAUSED: 
            switch (state) {
            case ASYNC_PREPAIRE: // logical error above
                e = new Exception("Illegal Player state transition!");
                break;
            case PLAYING:
            case STOPPED: // Player being reset while preparing
            case NOT_INITIALIZED: // drop everything
                mPlayerState = state;
                break;
            case PAUSED:
            default:
                //should NOT be here
                break;
            }
            break;
        case STOPPED: 
            switch (state) {
            case ASYNC_PREPAIRE: // logical error above
            case NOT_INITIALIZED: // drop everything
                mPlayerState = state;
                break;
            case PLAYING:
            case PAUSED:
                e = new Exception("Illegal Player state transition!");
                break;
            case STOPPED: // Player being reset while preparing
            default:
                ret = false;
                break;
            }
            break;
        default:
            e = new Exception("Illegal Player state!");
        }

        if (e != null)
            throw e;

        return ret;
    }
}
