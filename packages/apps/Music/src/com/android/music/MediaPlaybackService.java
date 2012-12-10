/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.music;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.media.audiofx.AudioEffect;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Playlists;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Random;
import java.util.Vector;
import java.util.List;

// BT AVRCP Start 
import com.mediatek.bluetooth.avrcp.IBTAvrcpMusic;
import com.mediatek.bluetooth.avrcp.IBTAvrcpMusicCallback;
import android.os.RemoteCallbackList;
// BT AVRCP End 

import com.mediatek.featureoption.FeatureOption;

import android.drm.DrmStore;

/**
 * Provides "background" audio playback capabilities, allowing the
 * user to switch between activities without stopping playback.
 */
public class MediaPlaybackService extends Service {
    /** used to specify whether enqueue() should start playing
     * the new list of files right away, next or once all the currently
     * queued files have been played
     */
    public static final int NOW = 1;
    public static final int NEXT = 2;
    public static final int LAST = 3;
    public static final int PLAYBACKSERVICE_STATUS = 1;
    
    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE_NORMAL = 1;
    public static final int SHUFFLE_AUTO = 2;
    
    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_CURRENT = 1;
    public static final int REPEAT_ALL = 2;
    
    //open reverb for MusicFX
    public static final String ATTACHAUXAUDIOEFFECT = "com.android.music.attachauxaudioeffect";
    public static final String DETACHAUXAUDIOEFFECT = "com.android.music.detachauxaudioeffect";
    // Add for handle media player error when attachAuxEffect an effect id is not exist because MusicFX
    // has died and these effect source already have been released
    public static final int MEDIA_ERROR_MUSICFX_DIED = 0x800000FF;

    public static final String PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
    public static final String META_CHANGED = "com.android.music.metachanged";
    public static final String QUEUE_CHANGED = "com.android.music.queuechanged";
    public static final String QUIT_PLAYBACK = "com.android.music.quitplayback";
    // BT AVRCP Start 
    public static final String PLAYBACK_COMPLETE = "com.android.music.playbackcomplete";
    // BT AVRCP End 

    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPLAY = "play";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";
    
    //  AVRCP and Android Music AP supports the FF/REWIND
    public static final String CMDFORWARD = "forward";
    public static final String CMDREWIND = "rewind";
    public static final String DELTATIME = "deltatime";
    private static final int SPEED_NORMAL = 16;     //means 16X  maybe 10X 32X
    private static final int SPEED_FAST = 40;       //means 40X 
    private static final int POSITION_FOR_SPEED_FAST = 5000;
    
    public static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
    public static final String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
    public static final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
    public static final String NEXT_ACTION = "com.android.music.musicservicecommand.next";


    private static final String FILEMANAGER_DELETE = "com.mediatek.filemanager.ACTION_DELETE";
    private static final long MAX = 1000;
    private static final int TRACK_ENDED = 1;
    //private static final int RELEASE_WAKELOCK = 2;
    private static final int SERVER_DIED = 3;
    private static final int OPEN_FAILED = 4;
    private static final int FOCUSCHANGE = 5;
    private static final int FADEDOWN = 6;
    private static final int FADEUP = 7;

    //ALPS00122225
    //private static final int RETRY_PLAY = 6;
    
    //ALPS00122096
    //private static final int STOP_PLAYER = 7;
    private static final int MAX_HISTORY_SIZE = 100;
    // BT AVRCP Start 
    private static final int CHANGE_SETTING_MODE = 0x65;  //AVRCP14
    // BT AVRCP End 
    
    private MultiPlayer mPlayer;
    private String mFileToPlay;
    private int mShuffleMode = SHUFFLE_NONE;
    private int mRepeatMode = REPEAT_NONE;
    private int mMediaMountedCount = 0;
    private long [] mAutoShuffleList = null;
    private long [] mPlayList = null;
    private int mPlayListLen = 0;
    private Vector<Integer> mHistory = new Vector<Integer>(MAX_HISTORY_SIZE);
    private Cursor mCursor;
    private int mPlayPos = -1;
    private static final String TAG = "MusicService";
    private final Shuffler mRand = new Shuffler();
    private int mOpenFailedCounter = 0;
    private long mDurationOverride = -1;
    String[] mCursorCols = new String[] {
            "audio._id AS _id",             // index must match IDCOLIDX below
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.IS_PODCAST, // index must match PODCASTCOLIDX below
            MediaStore.Audio.Media.BOOKMARK,    // index must match BOOKMARKCOLIDX below
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.IS_DRM,
            MediaStore.Audio.Media.DRM_METHOD,
    };
    private final static int IDCOLIDX = 0;
    private final static int PODCASTCOLIDX = 8;
    private final static int BOOKMARKCOLIDX = 9;
    private BroadcastReceiver mUnmountReceiver = null;
    private WakeLock mWakeLock;
    private int mServiceStartId = -1;
    private boolean mServiceInUse = false;
    private boolean mIsPlayerReady = false;
    private boolean mDoSeekWhenPrepared = false;
    private boolean mIsMediaSeekable = true;
    private boolean mIsSupposedToBePlaying = false;
    private boolean mQuietMode = false;
    private AudioManager mAudioManager;
    private StorageManager mStorageManager = null;
    private boolean mQueueIsSaveable = true;
    // used to track what type of audio focus loss caused the playback to pause
    private boolean mPausedByTransientLossOfFocus = false;
    
    // used to track what type of audio focus loss during the delay for AUDIOFOCUS_GAIN
    private boolean mLossOfFocusDuringGainDelay = false;
    
    private boolean mIsPlaylistCompleted = false;
    
    private boolean mNeedCheckPauseState = false;
    
    private boolean mReceiverUnregistered = false;
 
    private boolean mIsPrev = false;

    private boolean mNeedPlayWhenDelete = true;
    private boolean isReloadSuccess = false;
    
    private SharedPreferences mPreferences;
    // We use this to distinguish between different cards when saving/restoring playlists.
    // This will have to change if we want to support multiple simultaneous cards.
    private int mCardId;
    
    private MediaAppWidgetProvider mAppWidgetProvider = MediaAppWidgetProvider.getInstance();
    
    // async album art to get current song's album and replace it on notification
    private AlbumArtWorker mAsyncAlbumArtWorker = null;
    
    // interval after which we stop the service when idle
    private static final int IDLE_DELAY = 60000;
    
    // Auxiliary audio effect id to attach MediaPlayer on
    private int mAuxEffectId = 0;
    // Eject card path for multi-SD card support
    private String mEjectingCardPath = null;
    
    // Add to stop service after DynamicQuery thread has finish
    /*private boolean mIsDynamicQueryRunning = false;
    protected DynamicQuery mdynamicQuery = null;
    private Object mLock = new Object();*/
    
    // For DRM completion detection
    public static boolean mTrackCompleted = false;

    private long mPreAudioId;
    private RemoteControlClient mRemoteControlClient;
    
    // For album art backup for notification when skin changed
    private Bitmap mAlbumArt = null;

    //  AVRCP and Android Music AP supports the FF/REWIND
    private long mSeekPositionForAnotherSong = 0;    
    
    // BT AVRCP Start 
    public byte getPlayStatus(){
        if( true == isPlaying() ){
            return 1; // playing
        }else{
            if( mCursor != null ){
                return 2; // pause
            }
            return 0; // stop
        }
    }
    // BT AVRCP End 

    private Handler mMediaplayerHandler = new Handler() {
        float mCurrentVolume = 1.0f;
        @Override
        public void handleMessage(Message msg) {
            // BT AVRCP Start 
            int mode = 0;
            // BT AVRCP Start 

            MusicUtils.debugLog("mMediaplayerHandler.handleMessage " + msg.what);
            switch (msg.what) {
                case FADEDOWN:
                    mCurrentVolume -= .05f;
                    if (mCurrentVolume > .2f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEDOWN, 10);
                    } else {
                        mCurrentVolume = .2f;
                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;
                case FADEUP:
                    mCurrentVolume += .05f;
                    if (mCurrentVolume < 1.0f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEUP, 50);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;
                case SERVER_DIED:
                    MusicLogUtils.w(TAG, "SERVER_DIED");
                    Intent i = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
                    i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
                    i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                    sendBroadcast(i);
                    if (mIsSupposedToBePlaying) {
                        next(true);
                    } else {
                        MusicLogUtils.d(TAG, "SERVER_DIED: -> openCurrent");
                        // the server died when we were idle, so just
                        // reopen the same song (it will start again
                        // from the beginning though when the user
                        // restarts)
                        boolean bDoseek = mDoSeekWhenPrepared;
                        mQuietMode = true;
                        openCurrent();
                        mDoSeekWhenPrepared = bDoseek;
                        MusicLogUtils.d(TAG, "SERVER_DIED: doseek restored to:" + mDoSeekWhenPrepared);
                        MusicLogUtils.d(TAG, "SERVER_DIED: <- openCurrent");
                    }
                    break;
                case TRACK_ENDED:
                    if (mRepeatMode == REPEAT_CURRENT) {
                        mTrackCompleted = false;
                        seek(0);
                        if (isPlaying()) {
                            play();
                        }
                    } else {
                        next(false);
                    }
                    // BT AVRCP Start                     
                    notifyChange(PLAYBACK_COMPLETE); //AVRCP14
                    // BT AVRCP End                     
                    break;
                //case RELEASE_WAKELOCK:
                //    mWakeLock.release();
                //    break;
                case OPEN_FAILED:
                    Toast.makeText(MediaPlaybackService.this, R.string.playback_failed, Toast.LENGTH_SHORT).show();
                    notifyChange(QUIT_PLAYBACK);
                    break;
                // BT AVRCP Start 
                case CHANGE_SETTING_MODE:
                    MusicLogUtils.v(TAG, String.format("[AVRCP] CHANGE_SETTING_MODE arg1:%d arg2:%d", msg.arg1, msg.arg2) );
                    switch(msg.arg1){
                        case 0x02: //shuffle
                            mode = getShuffleMode();
                            if( mode != msg.arg2 ){
                                setShuffleMode(msg.arg2);
                            }
                        break;
                        case 0x03: //repeat
                            mode = getRepeatMode();
                            if( mode != msg.arg2 ){
                                setRepeatMode(msg.arg2);
                            }
                        break;
                    }
                    break;
                // BT AVRCP End 
                    
                case FOCUSCHANGE:
                    // This code is here so we can better synchronize it with the code that
                    // handles fade-in
                    // AudioFocus is a new feature: focus updates are made verbose on purpose
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                            MusicLogUtils.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS");
                            if(isPlaying()) {
                                mPausedByTransientLossOfFocus = false;
                            }
                            mLossOfFocusDuringGainDelay = true;
                            pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            mMediaplayerHandler.removeMessages(FADEUP);
                            mMediaplayerHandler.sendEmptyMessage(FADEDOWN);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            MusicLogUtils.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
                            if(isPlaying()) {
                                mPausedByTransientLossOfFocus = true;
                            }
                            mLossOfFocusDuringGainDelay = true;
                            pause();
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            MusicLogUtils.v(TAG, "AudioFocus: received AUDIOFOCUS_GAIN");
                            if (false == mLossOfFocusDuringGainDelay){
                            if(!isPlaying() && mPausedByTransientLossOfFocus) {
                                mPausedByTransientLossOfFocus = false;
                                mCurrentVolume = 0f;
                                mPlayer.setVolume(mCurrentVolume);
                                play(); // also queues a fade-in
                            } else {
                                mMediaplayerHandler.removeMessages(FADEDOWN);
                                mMediaplayerHandler.sendEmptyMessage(FADEUP);
                            }
                            }
                            break;
                        default:
                            MusicLogUtils.e(TAG, "Unknown audio focus change code");
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mReceiverUnregistered)
                return;
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            MusicUtils.debugLog("mIntentReceiver.onReceive " + action + " / " + cmd);
            MusicLogUtils.v(TAG, "mIntentReceiver.onReceive: " + action + "/" + cmd);
            
            if (action.equals(Intent.ACTION_SHUTDOWN) 
                    || action.equals("android.intent.action.ACTION_SHUTDOWN_IPO")) {
                // When shutting down, saveQueue first then stop the player
                saveQueue(true);
                stop();
            } else if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
                if (mCardId != -1) {
                    next(true);
                }
            } else if (CMDPREVIOUS.equals(cmd) || PREVIOUS_ACTION.equals(action)) {
                prev();
            } else if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
                if (isPlaying()) {
                    Editor ed = mPreferences.edit();
                    ed.putBoolean("pausedbytransientlossoffocus", false);
                    ed.commit();
                    MusicLogUtils.i(TAG, "pause state saved to shared preference!!");
                    pause();
                    mPausedByTransientLossOfFocus = false;
                } else {
                    play();
                }
            } else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
                Editor ed = mPreferences.edit();
                ed.putBoolean("pausedbytransientlossoffocus", false);
                ed.commit();
                MusicLogUtils.i(TAG, "pause state saved to shared preference!!");
                pause();
                mPausedByTransientLossOfFocus = false;
            } else if (CMDPLAY.equals(cmd)) {
                play();
            } else if (CMDSTOP.equals(cmd)) {
                Editor ed = mPreferences.edit();
                ed.putBoolean("pausedbytransientlossoffocus", false);
                ed.commit();
                MusicLogUtils.i(TAG, "pause state saved to shared preference!!");
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
            } else if (MediaAppWidgetProvider.CMDAPPWIDGETUPDATE.equals(cmd)) {
                // Someone asked us to refresh a set of specific widgets, probably
                // because they were just added.
                int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetProvider.performUpdate(MediaPlaybackService.this, appWidgetIds);
            } else if (ATTACHAUXAUDIOEFFECT.equals(action)) {
                // This means that user has selected an auxiliary audio effect, which requires a MediaPlayer instance to operate on
                mAuxEffectId = intent.getIntExtra("auxaudioeffectid", 0); 
                MusicLogUtils.v(TAG, "ATTACHAUXAUDIOEFFECT with EffectId=" + mAuxEffectId);
                if (mPlayer != null && mPlayer.isInitialized() && mAuxEffectId > 0) {
                    mPlayer.attachAuxEffect(mAuxEffectId);
                    mPlayer.setAuxEffectSendLevel(1.0f);
                }
            } else if (DETACHAUXAUDIOEFFECT.equals(action)) {
                // User has switched to other audio effect, so detach current auxiliary effect from MediaPlayer
                int auxEffectId = intent.getIntExtra("auxaudioeffectid", 0);
                MusicLogUtils.v(TAG, "DETACHAUXAUDIOEFFECT with EffectId=" + auxEffectId);
                if (mAuxEffectId == auxEffectId) {
                    mAuxEffectId = 0;
                    if (mPlayer != null && mPlayer.isInitialized()) {
                        mPlayer.attachAuxEffect(0);
                    }
                }
            }
        }
    };
    
    private ContentObserver mContentObserver = new ContentObserver(mMediaplayerHandler){
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (mPlayPos >= 0) {
               Cursor c = MusicUtils.query(MediaPlaybackService.this,
                           MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                           new String [] {"_id"}, "_id=" + mPlayList[mPlayPos] , null, null);
               if(null == c) {
                   MusicLogUtils.w(TAG, "mContentObserver: cursor is null, db not ready!");
                   return;
               }
               MusicLogUtils.w(TAG, "mContentObserver: cursor count is " + c.getCount());
               if (c.getCount() == 0) {
                   removeTrack(mPlayList[mPlayPos]);
               }
               c.close();
            }
        }
    };        
    private BroadcastReceiver mDeletionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mReceiverUnregistered)
                return;

            String action = intent.getAction();
            Uri uri = intent.getData();
            String data = Uri.decode(uri.toString());
            MusicLogUtils.d(TAG, "deletionReceiver: action=" + action + ", data=" + data);
            if (FILEMANAGER_DELETE.equals(action) && data != null) {
            	//Work around for the JE for query the filename containing  "'" that is the escape character of SQL
            	data = data.replaceAll("'", "''");
            	MusicLogUtils.d(TAG, "deletionReceiver: data=" + data);
                String where = "_data LIKE '%" +data.replaceFirst("file:///", "") + "'";                
                MusicLogUtils.d(TAG, "deletionReceiver: where=" + where);
                Cursor c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        , new String[] {"_id"}
                        , where
                        , null, null);
                int numOfRecords = 0;
                if (c != null) {
                    numOfRecords = c.getCount();
                    if (numOfRecords > 0) {
                        c.moveToFirst();
                        int colIdx = c.getColumnIndex("_id");
                        int trackId = c.getInt(colIdx);
                        MusicLogUtils.d(TAG, "deletionReceiver: track id=" + trackId);
                        removeTrack(trackId);
                    }
                    c.close();
                    c = null;
                } else {
                    MusicLogUtils.e(TAG, "Cannot find " + uri.toString() + " in DB!");
                }
            }
        }
    };
    private BroadcastReceiver mSkinChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isPlaying()) {
                updateNotification(MediaPlaybackService.this, mAlbumArt);
            }
        }
    };

    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            // AudioFocus is a new feature: focus updates are made verbose on purpose
            // delay 1.8 second to avoid noises after call.
            int delay = 0;
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                //delay = 1800;
                mLossOfFocusDuringGainDelay = false;
            }
            mMediaplayerHandler.sendMessageDelayed(mMediaplayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0), delay);
        }
    };

    public MediaPlaybackService() {
    }

    @Override
    public void onCreate() {
        MusicLogUtils.v(TAG, ">> onCreate");
        super.onCreate();
        stopForeground(true);
        
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ComponentName rec = new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(rec);
        // TODO update to new constructor
//        mRemoteControlClient = new RemoteControlClient(rec);
//        mAudioManager.registerRemoteControlClient(mRemoteControlClient);
//
//        int flags = RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
//                | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
//                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY
//                | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
//                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
//                | RemoteControlClient.FLAG_KEY_MEDIA_STOP;
//        mRemoteControlClient.setTransportControlFlags(flags);
        
        mPreferences = getSharedPreferences("Music", MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
        //mCardId = MusicUtils.getCardId(this);
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE); 
        mCardId = MusicUtils.getSDCardId(mStorageManager);
        MusicLogUtils.v(TAG, "onCreate: cardid=" + mCardId);
        registerExternalStorageListener();

        // Needs to be done in this thread, since otherwise ApplicationContext.getPowerManager() crashes.
        mPlayer = new MultiPlayer();
        mPlayer.setHandler(mMediaplayerHandler);
        
        // restore audio effects
        Intent i = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(i);

        mNeedCheckPauseState = true;
        reloadQueue();
        //notifyChange(QUEUE_CHANGED);
        //notifyChange(META_CHANGED);
        
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(SERVICECMD);
        commandFilter.addAction(TOGGLEPAUSE_ACTION);
        commandFilter.addAction(PAUSE_ACTION);
        commandFilter.addAction(NEXT_ACTION);
        commandFilter.addAction(PREVIOUS_ACTION);
        commandFilter.addAction(Intent.ACTION_SHUTDOWN);
        commandFilter.addAction(ATTACHAUXAUDIOEFFECT);
        commandFilter.addAction(DETACHAUXAUDIOEFFECT);

        registerReceiver(mIntentReceiver, commandFilter);
        
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(FILEMANAGER_DELETE);
        iFilter.addDataScheme("file");
        registerReceiver(mDeletionReceiver, iFilter);
        this.getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false, mContentObserver); 
        iFilter = new IntentFilter();
        iFilter.addAction(Intent.ACTION_SKIN_CHANGED);
        registerReceiver(mSkinChangedReceiver, iFilter);
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
        mWakeLock.setReferenceCounted(false);

        // If the service was idle, but got killed before it stopped itself, the
        // system will relaunch it. Make sure it gets stopped again in that case.
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        MusicLogUtils.v(TAG, "<< onCreate");
    }

    @Override
    public void onDestroy() {
        MusicLogUtils.v(TAG, ">> onDestroy");
        // Check that we're not being destroyed while something is still playing.
        if (isPlaying()) {
            MusicLogUtils.e(TAG, "Service being destroyed while still playing.");
            pause();
        }
        // Cancel all task in timer task
        /*if (mdynamicQuery != null) {
            mdynamicQuery.cancel();
        }*/

        unregisterReceiver(mIntentReceiver);
        unregisterReceiver(mDeletionReceiver);
        unregisterReceiver(mSkinChangedReceiver);        
        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;
        }
        mReceiverUnregistered = true;
        this.getContentResolver().unregisterContentObserver(mContentObserver);
        // release all MediaPlayer resources, including the native player and wakelocks
        Intent i = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(i);
        mPlayer.release();

        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        //mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
        
        // make sure there aren't any other messages coming
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mMediaplayerHandler.removeCallbacksAndMessages(null);

        
        mDurationOverride = -1;
        
        if (mAsyncAlbumArtWorker != null) {
                mAsyncAlbumArtWorker.cancel(true);
        }
        mWakeLock.release();
        // Wait for the timer task thread finish, then set mPlayer = null and close mCursor
        /*if (mIsDynamicQueryRunning) {
            int waitTime = 0;
            int totalTime = 8000;
            synchronized (mLock) {
                while (mIsDynamicQueryRunning) {
                    try {
                        waitTime += 500;
                        mLock.wait(500);
                        MusicLogUtils.v(TAG, "Wait for timer task thread end!");
                        if (waitTime > totalTime) {
                            MusicLogUtils.v(TAG, "Wait for timer task more than 8 second, so just return!");
                            break;
                        }
                    } catch (Exception e) {
                        MusicLogUtils.e(TAG, "wait has been interupted " + e);
                    }
                }
            }
        }
        mdynamicQuery = null;*/
        mPlayer = null;
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        super.onDestroy();
        MusicLogUtils.v(TAG, "<< onDestroy");
    }
    
    private final char hexdigits [] = new char [] {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };

    private void saveQueue(boolean full) {
        MusicLogUtils.v(TAG, "saveQueue(" + full + ")");
        if (!mQueueIsSaveable) {
            MusicLogUtils.w(TAG, "saveQueue: queue NOT savable!!");
            return;
        }
        
        if (mPlayPos >= mPlayListLen) {
            // bogus playlist, do NOT save it!!
            MusicLogUtils.w(TAG, "saveQueue: bogus playlist: listlen=" + mPlayListLen + ", pos=" + mPlayPos);
            return;
        }

        Editor ed = mPreferences.edit();
        //long start = System.currentTimeMillis();
        if (full) {
            StringBuilder q = new StringBuilder();
            
            // The current playlist is saved as a list of "reverse hexadecimal"
            // numbers, which we can generate faster than normal decimal or
            // hexadecimal numbers, which in turn allows us to save the playlist
            // more often without worrying too much about performance.
            // (saving the full state takes about 40 ms under no-load conditions
            // on the phone)
            int len = mPlayListLen;
            for (int i = 0; i < len; i++) {
                long n = mPlayList[i];
                if (n < 0) {
                    continue;
                } else if (n == 0) {
                    q.append("0;");
                } else {
                    while (n != 0) {
                        int digit = (int)(n & 0xf);
                        n >>>= 4;
                        q.append(hexdigits[digit]);
                    }
                    q.append(";");
                }
            }
            //MusicLogUtils.i("@@@@ service", "created queue string in " + (System.currentTimeMillis() - start) + " ms");
            ed.putString("queue", q.toString());
            MusicLogUtils.v(TAG, "saveQueue: queue=" + q.toString());
            ed.putInt("cardid", mCardId);
            if (mShuffleMode != SHUFFLE_NONE) {
                // In shuffle mode we need to save the history too
                len = mHistory.size();
                q.setLength(0);
                for (int i = 0; i < len; i++) {
                    int n = mHistory.get(i);
                    if (n == 0) {
                        q.append("0;");
                    } else {
                        while (n != 0) {
                            int digit = (n & 0xf);
                            n >>>= 4;
                            q.append(hexdigits[digit]);
                        }
                        q.append(";");
                    }
                }
                ed.putString("history", q.toString());
            }
        }
        ed.putInt("curpos", mPlayPos);
        MusicLogUtils.v(TAG, "saveQueue: mPlayPos=" + mPlayPos);
        if (mPlayer.isInitialized() && mIsPlayerReady) {
            ed.putLong("seekpos", mPlayer.position());
        }
        ed.putInt("repeatmode", mRepeatMode);
        ed.putInt("shufflemode", mShuffleMode);
        SharedPreferencesCompat.apply(ed);

        //MusicLogUtils.i("@@@@ service", "saved state in " + (System.currentTimeMillis() - start) + " ms");
    }
    /*private class DynamicQuery {
        Timer timer;
        final int playPosition;
        int timeCount = 5;
        
        public DynamicQuery(int pos) {
            timer = new Timer();
            timer.schedule(new QueryTask(),
                   0,        //initial delay
                   1*1000);  //subsequent rate
            playPosition = pos;
            MusicLogUtils.v(TAG, "DynamicQuery:playPosition = " + playPosition);
        }

        public void cancel() {
            timeCount = -1;
            return;
        }
        
        class QueryTask extends TimerTask {
            Cursor crsr = null;
            public void run() {
                //MusicLogUtils.v(TAG, "QueryTask:Run at = " + timeCount);
                if (timeCount < 0 || (playPosition != mPlayPos)) {
                    MusicLogUtils.v(TAG, "reloadQueue:timeCount = " + timeCount 
                                            + " ; mPlayPos = " + mPlayPos);
                    notifyChange(META_CHANGED);
                    timer.cancel();
                    if (crsr != null) {
                        crsr.close();
                        crsr = null;
                    }
                    synchronized (mLock) {
                        mLock.notify();
                    }
                    return;
                }
                
                mIsDynamicQueryRunning = true;
                MusicLogUtils.v(TAG, "reloadQueue:query start, mIsDynamicQueryRunning = " + mIsDynamicQueryRunning);
                crsr = MusicUtils.query(getApplicationContext(),
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String [] {"_id"}, "_id=" + mPlayList[playPosition] , null, null);
                //MusicLogUtils.v(TAG, "reloadQueue:query end");
                if ((crsr == null) || (crsr.getCount() == 0)) {
                    // decrease the count to query again.
                    MusicLogUtils.v(TAG, "reloadQueue:timeCount = " + timeCount);
                    timeCount--;
                } else {
                    // already got the data,set the count to be 0,cancel the timer task.
                    timeCount = -1;
                    timer.cancel();
                    if (crsr != null) {
                        crsr.close();
                        crsr = null;
                    }
                    mOpenFailedCounter = 20;
                    
                    // Add check for DRM to decide whether to open current track
                    if (FeatureOption.MTK_DRM_APP) {
                        Cursor c_drm = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                new String[] {MediaStore.Audio.Media.IS_DRM, MediaStore.Audio.Media.DRM_METHOD},
                                "_id=" + mPlayList[playPosition] , null, null);
                        if (c_drm != null) {
                            if (c_drm.moveToFirst()) {
                                int is_drm = c_drm.getInt(0);
                                int drm_method = c_drm.getInt(1);
                                MusicLogUtils.v(TAG, "is_drm=" + is_drm + ", drm_method=" + drm_method);
                                if (is_drm != 1 || (is_drm == 1 && drm_method == DrmStore.DrmMethod.METHOD_FL)) {
                                    openCurrent();
                                }
                            }
                            c_drm.close();
                        }
                      } else {
                        //mQuietMode = true;
                        openCurrent();
                        //mQuietMode = false;
                      }
                    if (!mPlayer.isInitialized()) {
                        // couldn't restore the saved state
                        MusicLogUtils.e(TAG, "reloadQueue: open failed! not inited!");
                        mPlayListLen = 0;
                        synchronized (mLock) {
                            mLock.notify();
                        }
                        mIsDynamicQueryRunning = false;
                        MusicLogUtils.v(TAG, "reloadQueue:query end, mIsDynamicQueryRunning = " + mIsDynamicQueryRunning);
                        return;
                    }
                    
                    mDoSeekWhenPrepared = true;
                    
                    int repmode = mPreferences.getInt("repeatmode", REPEAT_NONE);
                    if (repmode != REPEAT_ALL && repmode != REPEAT_CURRENT) {
                        repmode = REPEAT_NONE;
                    }
                    mRepeatMode = repmode;

                    int shufmode = mPreferences.getInt("shufflemode", SHUFFLE_NONE);
                    if (shufmode != SHUFFLE_AUTO && shufmode != SHUFFLE_NORMAL) {
                        shufmode = SHUFFLE_NONE;
                    }
                    if (shufmode != SHUFFLE_NONE) {
                        // in shuffle mode we need to restore the history too
                       String q = mPreferences.getString("history", "");
                       int qlen = q != null ? q.length() : 0;
                       if (qlen > 1) {
                            int plen = 0;
                            int  n = 0;
                            int shift = 0;
                            mHistory.clear();
                            for (int i = 0; i < qlen; i++) {
                                char c = q.charAt(i);
                                if (c == ';') {
                                    if (n >= mPlayListLen) {
                                        // bogus history data
                                        mHistory.clear();
                                        break;
                                    }
                                    mHistory.add(n);
                                    n = 0;
                                    shift = 0;
                                } else {
                                    if (c >= '0' && c <= '9') {
                                        n += ((c - '0') << shift);
                                    } else if (c >= 'a' && c <= 'f') {
                                        n += ((10 + c - 'a') << shift);
                                    } else {
                                        // bogus history data
                                        mHistory.clear();
                                        break;
                                    }
                                    shift += 4;
                                }
                            }
                        }
                    }
                    if (shufmode == SHUFFLE_AUTO) {
                        if (! makeAutoShuffleList()) {
                            shufmode = SHUFFLE_NONE;
                        }
                    }
                    mShuffleMode = shufmode;
                    notifyChange(QUEUE_CHANGED);
                    notifyChange(META_CHANGED);
                }
                // jump from while loop;
                synchronized (mLock) {
                    mLock.notify();
                }
                mIsDynamicQueryRunning = false;
                MusicLogUtils.v(TAG, "reloadQueue:query end, mIsDynamicQueryRunning = " + mIsDynamicQueryRunning);
            }               
        }
    }*/
    private void reloadQueue() {
        MusicLogUtils.v(TAG, "reloadQueue");
        String q = null;
        
        boolean newstyle = false;
        
        // ALPS00121756
        if (mCardId == -1) {
            // No SD card mounted, should not do normal operations
            MusicLogUtils.w(TAG, "reloadQueue: no sd card!");
            return;
        }
        MusicLogUtils.d(TAG, "reloadQueue: new cardid=" + mCardId);
        int id = mCardId;
        if (mPreferences.contains("cardid")) {
            newstyle = true;
            id = mPreferences.getInt("cardid", ~mCardId);
            MusicLogUtils.v(TAG, "reloadQueue: old cardid=" + id);
        }
        if (id == mCardId) {
            // Only restore the saved playlist if the card is still
            // the same one as when the playlist was saved
            MusicLogUtils.v(TAG, "reloadQueue: same card id!");
            q = mPreferences.getString("queue", "");
        }
        int qlen = q != null ? q.length() : 0;
        MusicLogUtils.v(TAG, "reloadQueue: qlen=" + qlen);
        if (qlen > 1) {
            //MusicLogUtils.i("@@@@ service", "loaded queue: " + q);
            int plen = 0;
            int n = 0;
            int shift = 0;
            for (int i = 0; i < qlen; i++) {
                char c = q.charAt(i);
                if (c == ';') {
                    ensurePlayListCapacity(plen + 1);
                    mPlayList[plen] = n;
                    plen++;
                    n = 0;
                    shift = 0;
                } else {
                    if (c >= '0' && c <= '9') {
                        n += ((c - '0') << shift);
                    } else if (c >= 'a' && c <= 'f') {
                        n += ((10 + c - 'a') << shift);
                    } else {
                        // bogus playlist data
                        plen = 0;
                        break;
                    }
                    shift += 4;
                }
            }
            mPlayListLen = plen;

            int pos = mPreferences.getInt("curpos", 0);
            MusicLogUtils.d(TAG, "reloadQueue: mPlayListLen=" + mPlayListLen + ", curpos=" + pos);
            if (pos < 0 || pos >= mPlayListLen) {
                // The saved playlist is bogus, discard it
                mPlayListLen = 0;
                return;
            }
            mPlayPos = pos;
            MusicLogUtils.d(TAG, "reloadQueue: mPlayPos=" + pos);
            /*if (mPlayPos != -1){
                mdynamicQuery = new DynamicQuery(mPlayPos);
            }
            
            if (mNeedCheckPauseState) {
                MusicLogUtils.i(TAG, "reloadQueue: mNeedCheckPauseState = true!!");
                mNeedCheckPauseState = false;
                boolean bPausedByTransientLossOfFocus = mPreferences.getBoolean("pausedbytransientlossoffocus", false);
                if (bPausedByTransientLossOfFocus) {
                    MusicLogUtils.i(TAG, "reloadQueue: previously paused by loss of focus, so WILL PLAY!!!!!!");
                    mQuietMode = false;
                    Editor ed = mPreferences.edit();
                    ed.putBoolean("pausedbytransientlossoffocus", false);
                    ed.commit();
                } else {
                    mQuietMode = true;
                }
            } else {
                mQuietMode = true;
            }*/
     
            
            // When reloadQueue is called in response to a card-insertion,
            // we might not be able to query the media provider right away.
            // To deal with this, try querying for the current file, and if
            // that fails, wait a while and try again. If that too fails,
            // assume there is a problem and don't restore the state.
            

            // Make sure we don't auto-skip to the next song, since that
            // also starts playback. What could happen in that case is:
            // - music is paused
            // - go to UMS and delete some files, including the currently playing one
            // - come back from UMS
            // (time passes)
            // - music app is killed for some reason (out of memory)
            // - music service is restarted, service restores state, doesn't find
            //   the "current" file, goes to the next and: playback starts on its
            //   own, potentially at some random inconvenient time.
            //mOpenFailedCounter = 20;
            if (mNeedCheckPauseState) {
                MusicLogUtils.i(TAG, "reloadQueue: mNeedCheckPauseState = true!!");
                mNeedCheckPauseState = false;
                boolean bPausedByTransientLossOfFocus = mPreferences.getBoolean("pausedbytransientlossoffocus", false);
                if (bPausedByTransientLossOfFocus) {
                    MusicLogUtils.i(TAG, "reloadQueue: previously paused by loss of focus, so WILL PLAY!!!!!!");
                    mQuietMode = false;
                    Editor ed = mPreferences.edit();
                    ed.putBoolean("pausedbytransientlossoffocus", false);
                    ed.commit();
                } else {
                    mQuietMode = true;
                }
            } else {
                mQuietMode = true;
            }
            
            // Add check for DRM to decide whether to open current track
            if (FeatureOption.MTK_DRM_APP) {
                Cursor c_drm = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[] {MediaStore.Audio.Media.IS_DRM, MediaStore.Audio.Media.DRM_METHOD},
                        "_id=" + mPlayList[mPlayPos] , null, null);
                if (c_drm != null) {
                    if (c_drm.moveToFirst()) {
                        int is_drm = c_drm.getInt(0);
                        int drm_method = c_drm.getInt(1);
                        MusicLogUtils.v(TAG, "is_drm=" + is_drm + ", drm_method=" + drm_method);
                        if (is_drm != 1 || (is_drm == 1 && drm_method == DrmStore.DrmMethod.METHOD_FL)) {
                            openCurrent();
                            isReloadSuccess = true;
                        }
                    }
                    c_drm.close();
                }
              } else {
                //mQuietMode = true;
                Cursor crsr = MusicUtils.query(this,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{"_id"}, "_id=" + mPlayList[mPlayPos],
                        null, null);
                if (crsr != null || crsr.getCount() > 0) {
                    openCurrent();
                    isReloadSuccess = true;
                }
                if (crsr != null) {
                    crsr.close();
                }
                //mQuietMode = false;
              }
            if (!mPlayer.isInitialized()) {
                // couldn't restore the saved state
                MusicLogUtils.e(TAG, "reloadQueue: open failed! not inited!");
                mPlayListLen = 0;
                return;
            }
            
            mDoSeekWhenPrepared = true;
            
            int repmode = mPreferences.getInt("repeatmode", REPEAT_NONE);
            if (repmode != REPEAT_ALL && repmode != REPEAT_CURRENT) {
                repmode = REPEAT_NONE;
            }
            mRepeatMode = repmode;

            int shufmode = mPreferences.getInt("shufflemode", SHUFFLE_NONE);
            if (shufmode != SHUFFLE_AUTO && shufmode != SHUFFLE_NORMAL) {
                shufmode = SHUFFLE_NONE;
            }
            if (shufmode != SHUFFLE_NONE) {
                // in shuffle mode we need to restore the history too
                q = mPreferences.getString("history", "");
                qlen = q != null ? q.length() : 0;
                if (qlen > 1) {
                    plen = 0;
                    n = 0;
                    shift = 0;
                    mHistory.clear();
                    for (int i = 0; i < qlen; i++) {
                        char c = q.charAt(i);
                        if (c == ';') {
                            if (n >= mPlayListLen) {
                                // bogus history data
                                mHistory.clear();
                                break;
                            }
                            mHistory.add(n);
                            n = 0;
                            shift = 0;
                        } else {
                            if (c >= '0' && c <= '9') {
                                n += ((c - '0') << shift);
                            } else if (c >= 'a' && c <= 'f') {
                                n += ((10 + c - 'a') << shift);
                            } else {
                                // bogus history data
                                mHistory.clear();
                                break;
                            }
                            shift += 4;
                        }
                    }
                }
            }
            if (shufmode == SHUFFLE_AUTO) {
                if (! makeAutoShuffleList()) {
                    shufmode = SHUFFLE_NONE;
                }
            }
            mShuffleMode = shufmode;
        } else {
            // Reset mPlayListLen so that we have a chance
            // to enter party shuffle mode if user clicks play on widgets
            mPlayListLen = 0;
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // BT AVRCP Start 
        MusicLogUtils.v("MediaPlaybackService", String.format("intent %s stubname:%s", intent.getAction(), ServiceAvrcpStub.class.getName()) );
        if( IBTAvrcpMusic.class.getName().equals(intent.getAction())) {
            MusicLogUtils.d("MISC_AVRCP", "MediaPlayer returns IBTAvrcpMusic");
            return mBinderAvrcp;
        }else if( "com.android.music.IMediaPlaybackService".equals(intent.getAction())) {
            MusicLogUtils.d("MISC_AVRCP", "MediaPlayer returns ServiceAvrcp inetrface");
            return mBinderAvrcp;
        }
        // BT AVRCP End 

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceStartId = startId;
        mDelayedStopHandler.removeCallbacksAndMessages(null);

        if (intent != null) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            MusicUtils.debugLog("onStartCommand " + action + " / " + cmd);
            MusicLogUtils.d(TAG, "onStartCommand: " + action + "/" + cmd);

            if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
                if (mCardId == -1) {
                    mCardId = MusicUtils.getSDCardId(mStorageManager);
                    MusicLogUtils.v(TAG, "onStartCommand re-try: cardid = " + mCardId);
                    notifyChange(QUIT_PLAYBACK);
                }
                if (mCardId != -1) {
                    next(true);
                } 
            } else if (CMDPREVIOUS.equals(cmd) || PREVIOUS_ACTION.equals(action)) {
                if (position() < 2000) {
                    prev();
                } else {
                    seek(0);
                    play();
                }
            } else if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
                if (isPlaying()) {
                    Editor ed = mPreferences.edit();
                    ed.putBoolean("pausedbytransientlossoffocus", false);
                    ed.commit();
                    MusicLogUtils.i(TAG, "pause state saved to shared preference!!");
                    pause();
                    mPausedByTransientLossOfFocus = false;
                } else {
                    play();
                    if (isPlaying() != true) {
                        mQuietMode = false;
                    }
                }
            } else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
                Editor ed = mPreferences.edit();
                ed.putBoolean("pausedbytransientlossoffocus", false);
                ed.commit();
                MusicLogUtils.i(TAG, "pause state saved to shared preference!!");
                pause();
                mPausedByTransientLossOfFocus = false;
            } else if (CMDPLAY.equals(cmd)) {
                play();
            } else if (CMDSTOP.equals(cmd)) {
                Editor ed = mPreferences.edit();
                ed.putBoolean("pausedbytransientlossoffocus", false);
                ed.commit();
                MusicLogUtils.i(TAG, "pause state saved to shared preference!!");
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
            //  AVRCP and Android Music AP supports the FF/REWIND
            } else if (CMDFORWARD.equals(cmd)) {
                long deltaTime = intent.getLongExtra(DELTATIME, 0);
                scanForward(deltaTime);
            } else if (CMDREWIND.equals(cmd)) {
                long deltaTime = intent.getLongExtra(DELTATIME, 0);
                scanBackward(deltaTime);
            }
        }
        
        // make sure the service will shut down on its own if it was
        // just started but not bound to and nothing is playing
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        return START_STICKY;
    }

    //  AVRCP and Android Music AP supports the FF/REWIND
    private void scanBackward(long delta) {
        long startSeekPos = position();
        long scanDelta = 0;
        MusicLogUtils.d(TAG,"startSeekPos: " + startSeekPos);
        if (delta < POSITION_FOR_SPEED_FAST) {
            // seek at 10x speed for the first 5 seconds
            scanDelta = delta * SPEED_NORMAL; 
        } else {
            // seek at 40x after that
            scanDelta = POSITION_FOR_SPEED_FAST + (delta - POSITION_FOR_SPEED_FAST) * SPEED_FAST;
        }
        long newpos = startSeekPos - scanDelta;
        if (newpos < 0) {
            // move to previous track
            prev();
            long duration = duration();
            MusicLogUtils.d(TAG,"duration: " + duration);
            //startSeekPos += duration;
            newpos += duration;

            //Editor ed = mPreferences.edit();
            if ( newpos > 0) {
                mDoSeekWhenPrepared = true;
                mSeekPositionForAnotherSong = newpos;
            }
        } else {
            seek(newpos);
        }
    }

    //  AVRCP and Android Music AP supports the FF/REWIND
    private void scanForward(long delta) {
        //MusicLogUtils.d(TAG,Thread.currentThread().getStackTrace()[2].getMethodName());
        long scanDelta = 0;
        long startSeekPos = position();
        if (delta < POSITION_FOR_SPEED_FAST) {
            // seek at 10x speed for the first 5 seconds
            scanDelta = delta * SPEED_NORMAL; 
        } else {
            // seek at 40x after that
            scanDelta = POSITION_FOR_SPEED_FAST + (delta - POSITION_FOR_SPEED_FAST) * SPEED_FAST;
        }
        long newpos = startSeekPos + scanDelta;
        long duration = duration();
        if (newpos >= duration) {
            // move to next track
            next(true);
            //startSeekPos -= duration; // is OK to go negative
            newpos -= duration;
            duration = duration();
            if (newpos > duration) {
                newpos = duration;
            }
            mDoSeekWhenPrepared = true;
            mSeekPositionForAnotherSong = newpos;
        } else {
            seek(newpos);

        }
    }

    
    @Override
    public boolean onUnbind(Intent intent) {
        mServiceInUse = false;

        // Take a snapshot of the current playlist
        saveQueue(true);

        if (isPlaying() || mPausedByTransientLossOfFocus) {
            // something is currently playing, or will be playing once 
            // an in-progress action requesting audio focus ends, so don't stop the service now.
            return true;
        }
        
        // If there is a playlist but playback is paused, then wait a while
        // before stopping the service, so that pause/resume isn't slow.
        // Also delay stopping the service if we're transitioning between tracks.
        if (mPlayListLen > 0  || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
            Message msg = mDelayedStopHandler.obtainMessage();
            mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
            return true;
        }
        
        // No active playlist, OK to stop the service right now
        stopSelf(mServiceStartId);
        return true;
    }
    
    private Handler mDelayedStopHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Check again to make sure nothing is playing right now
            if (isPlaying() || mPausedByTransientLossOfFocus || mServiceInUse
                    || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
                return;
            }
            // save the queue again, because it might have changed
            // since the user exited the music app (because of
            // party-shuffle or because the play-position changed)
            saveQueue(true);
            stopSelf(mServiceStartId);
        }
    };

    /**
     * Called when we receive a ACTION_MEDIA_EJECT notification.
     *
     * @param storagePath path to mount point for the removed media
     */
    public void closeExternalStorageFiles(String storagePath) {
        // stop playback and clean up if the SD card is going to be unmounted.
        stop(true);
        notifyChange(QUEUE_CHANGED);
        // Because MediaPlayer has been stopped, it does not has any meaning
        // to update the meta data

        // UPDATE: we still need this message since Music can call updateTrackInfo 
        // to decide whether to finish itself after a card is plugged out
        notifyChange(META_CHANGED);
        // Instead, tell'em player state is changed
        notifyChange(PLAYSTATE_CHANGED);
    }

    /**
     * Registers an intent to listen for ACTION_MEDIA_EJECT notifications.
     * The intent will call closeExternalStorageFiles() if the external media
     * is going to be ejected, so applications can clean up any files they have open.
     */
    public void registerExternalStorageListener() {
        if (mUnmountReceiver == null) {
            mUnmountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (mReceiverUnregistered)
                        return;
                    String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        MusicLogUtils.d(TAG, "MEDIA_EJECT");
                        // For multi-SD card, we should decide whether the internal SD card is ejected
                        //String ejectCardPath = intent.getData().getPath();
                        mEjectingCardPath = intent.getData().getPath();
                        MusicLogUtils.d(TAG, "ejected card path=" + mEjectingCardPath);
                        MusicLogUtils.d(TAG, "main card path=" + Environment.getExternalStorageDirectory().getPath());
                        if (mEjectingCardPath.equals(Environment.getExternalStorageDirectory().getPath())) {
                            // internal card is being ejected
                            MusicLogUtils.d(TAG, "MEDIA_EJECT: internal card unmounting...");
                            saveQueue(true);
                            mQueueIsSaveable = false;
                            closeExternalStorageFiles(intent.getData().getPath());
                            //mCardId = -1;
                            mMediaMountedCount--;
                            mCardId = MusicUtils.getSDCardId(mStorageManager);
                            MusicLogUtils.v(TAG, "card eject: cardid=" + mCardId);
                        } else {
                            // see if the currently playing track is on the removed card...
                            // if true, jump to next one
                            if (mCursor != null && !mCursor.isAfterLast()) {
                                String curTrackPath = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                                if (curTrackPath != null && curTrackPath.contains(mEjectingCardPath)) {
                                    // current file IS on removed card
                                    MusicLogUtils.d(TAG, "MEDIA_EJECT: current track on an unmounting external card");
                                    if (isPlaying()) {
                                        // find the next available track to play
                                        next(false);
                                    } else {
                                        closeExternalStorageFiles(intent.getData().getPath());
                                    }
                                }
                            }
                            
                        }
                        mEjectingCardPath = null;
                } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED) && mMediaMountedCount < 0) {
                        MusicLogUtils.d(TAG, "MEDIA_MOUNTED");
                        // This service assumes that these two intents come as a pair. But some 
                        // third party application sends only mounted Intent, which causes
                        // chaos here. It can only perform resume playing if SD card is ejected
                        // before, which is indicated by 'mMediaMountedCount < 0'.
                        mMediaMountedCount++;    /// TODO: decide carefully whether mount count should include non-internal card re-mount
                        //mCardId = MusicUtils.getCardId(MediaPlaybackService.this);

                            String mountedCardPath = intent.getData().getPath();
                            MusicLogUtils.d(TAG, "mounted card path=" + mountedCardPath);
                            MusicLogUtils.d(TAG, "main card path=" + Environment.getExternalStorageDirectory().getPath());
                            if (mountedCardPath.equals(Environment.getExternalStorageDirectory().getPath())) {
                                // internal card mounted
                                MusicLogUtils.d(TAG, "MEDIA_MOUNTED: internal card mounted...");
                                //mCardId = FileUtils.getFatVolumeId(intent.getData().getPath());
                                mCardId = MusicUtils.getSDCardId(mStorageManager);
                                MusicLogUtils.v(TAG, "card mounted: cardid=" + mCardId);
                                reloadQueue();
                                mQueueIsSaveable = true;
                                notifyChange(QUEUE_CHANGED);
                                notifyChange(META_CHANGED);
                            }
                    } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                        MusicLogUtils.d(TAG, "ACTION_MEDIA_SCANNER_FINISHED");
                        mCardId = MusicUtils.getSDCardId(mStorageManager);
                        if (mCardId != -1 && !isReloadSuccess) {
                            MusicLogUtils.d(TAG, "reload queue in ACTION_MEDIA_SCANNER_FINISHED");
                            reloadQueue();
                        }
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            iFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
            iFilter.addDataScheme("file");
            registerReceiver(mUnmountReceiver, iFilter);
        }
    }

    /**
     * Notify the change-receivers that something has changed.
     * The intent that is sent contains the following data
     * for the currently playing track:
     * "id" - Integer: the database row ID
     * "artist" - String: the name of the artist
     * "album" - String: the name of the album
     * "track" - String: the name of the track
     * The intent has an action that is one of
     * "com.android.music.metachanged"
     * "com.android.music.queuechanged",
     * "com.android.music.playbackcomplete"
     * "com.android.music.playstatechanged"
     * respectively indicating that a new track has
     * started playing, that the playback queue has
     * changed, that playback has stopped because
     * the last file in the list has been played,
     * or that the play-state changed (paused/resumed).
     */
    private void notifyChange(String what) {
        MusicLogUtils.d(TAG, "notifyChange(" + what + ")");
        Intent i = new Intent(what);
        i.putExtra("id", Long.valueOf(getAudioId()));
        i.putExtra("artist", getArtistName());
        i.putExtra("album",getAlbumName());
        i.putExtra("track", getTrackName());
        i.putExtra("playing", isPlaying());
        if (QUIT_PLAYBACK.equals(what)) {
            // for QUIT_PLAYBACK, do NOT use sticky broadcast
            sendBroadcast(i);
        } else {
            sendStickyBroadcast(i);
        if (what.equals(PLAYSTATE_CHANGED)) {
//            mRemoteControlClient.setPlaybackState(isPlaying() ?
//                    RemoteControlClient.PLAYSTATE_PLAYING : RemoteControlClient.PLAYSTATE_PAUSED);
        } else if (what.equals(META_CHANGED)) {
//            RemoteControlClient.MetadataEditor ed = mRemoteControlClient.editMetadata(true);
//            ed.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, getTrackName());
//            ed.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, getAlbumName());
//            ed.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, getArtistName());
//            ed.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, duration());
//            Bitmap b = MusicUtils.getArtwork(this, getAudioId(), getAlbumId(), false);
//            if (b != null) {
//                ed.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, b);
//            }
//            ed.apply();
        }
        }

        if (what.equals(QUEUE_CHANGED)) {
            saveQueue(true);
        } else {
            saveQueue(false);
        }
        
        // Share this notification directly with our widgets
        mAppWidgetProvider.notifyChange(this, what);
        
        // BT AVRCP Start 
        notifyBTAvrcp(what); //AVRCP14
        // BT AVRCP End         
    }

    private void ensurePlayListCapacity(int size) {
        if (mPlayList == null || size > mPlayList.length) {
            // reallocate at 2x requested size so we don't
            // need to grow and copy the array for every
            // insert
            long [] newlist = new long[size * 2];
            int len = mPlayList != null ? mPlayList.length : mPlayListLen;
            for (int i = 0; i < len; i++) {
                newlist[i] = mPlayList[i];
            }
            mPlayList = newlist;
        }
        // FIXME: shrink the array when the needed size is much smaller
        // than the allocated size
    }
    
    // insert the list of songs at the specified position in the playlist
    private void addToPlayList(long [] list, int position) {
        int addlen = list.length;
        if (position < 0) { // overwrite
            mPlayListLen = 0;
            position = 0;
        }
        ensurePlayListCapacity(mPlayListLen + addlen);
        if (position > mPlayListLen) {
            position = mPlayListLen;
        }
        
        // move part of list after insertion point
        int tailsize = mPlayListLen - position;
        for (int i = tailsize ; i > 0 ; i--) {
            mPlayList[position + i] = mPlayList[position + i - addlen]; 
        }
        
        // copy list into playlist
        for (int i = 0; i < addlen; i++) {
            mPlayList[position + i] = list[i];
        }
        mPlayListLen += addlen;
        if (mPlayListLen == 0) {
            mCursor.close();
            mCursor = null;
            notifyChange(META_CHANGED);
        }
    }
    
    /**
     * Appends a list of tracks to the current playlist.
     * If nothing is playing currently, playback will be started at
     * the first track.
     * If the action is NOW, playback will switch to the first of
     * the new tracks immediately.
     * @param list The list of tracks to append.
     * @param action NOW, NEXT or LAST
     */
    public void enqueue(long [] list, int action) {
        synchronized(this) {
            if (action == NEXT && mPlayPos + 1 < mPlayListLen) {
                addToPlayList(list, mPlayPos + 1);
                notifyChange(QUEUE_CHANGED);
            } else {
                // action == LAST || action == NOW || mPlayPos + 1 == mPlayListLen
                addToPlayList(list, Integer.MAX_VALUE);
                notifyChange(QUEUE_CHANGED);
                if (action == NOW) {
                    mPlayPos = mPlayListLen - list.length;
                    openCurrent();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                }
            }
            if (mPlayPos < 0) {
                mPlayPos = 0;
                openCurrent();
                play();
                notifyChange(META_CHANGED);
            }
        }
    }

    /**
     * Replaces the current playlist with a new list,
     * and prepares for starting playback at the specified
     * position in the list, or a random position if the
     * specified position is 0.
     * @param list The new list of tracks.
     */
    public void open(long [] list, int position) {
        synchronized (this) {
            if (mShuffleMode == SHUFFLE_AUTO) {
                mShuffleMode = SHUFFLE_NORMAL;
            }
            long oldId = getAudioId();
            int listlength = list.length;
            boolean newlist = true;
            if (mPlayListLen == listlength) {
                // possible fast path: list might be the same
                newlist = false;
                for (int i = 0; i < listlength; i++) {
                    if (list[i] != mPlayList[i]) {
                        newlist = true;
                        break;
                    }
                }
            }
            if (newlist) {
                addToPlayList(list, -1);
                notifyChange(QUEUE_CHANGED);
            }
            int oldpos = mPlayPos;
            if (position >= 0) {
                mPlayPos = position;
            } else {
                mPlayPos = mRand.nextInt(mPlayListLen);
            }
            mHistory.clear();

            saveBookmarkIfNeeded();
            openCurrent();
            if (oldId != getAudioId()) {
                notifyChange(META_CHANGED);
            }
        }
    }
    
    /**
     * Moves the item at index1 to index2.
     * @param index1
     * @param index2
     */
    public void moveQueueItem(int index1, int index2) {
        synchronized (this) {
            if (index1 >= mPlayListLen) {
                index1 = mPlayListLen - 1;
            }
            if (index2 >= mPlayListLen) {
                index2 = mPlayListLen - 1;
            }
            if (index1 < index2) {
                long tmp = mPlayList[index1];
                for (int i = index1; i < index2; i++) {
                    mPlayList[i] = mPlayList[i+1];
                }
                mPlayList[index2] = tmp;
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index1 && mPlayPos <= index2) {
                        mPlayPos--;
                }
            } else if (index2 < index1) {
                long tmp = mPlayList[index1];
                for (int i = index1; i > index2; i--) {
                    mPlayList[i] = mPlayList[i-1];
                }
                mPlayList[index2] = tmp;
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index2 && mPlayPos <= index1) {
                        mPlayPos++;
                }
            }
            notifyChange(QUEUE_CHANGED);
        }
    }

    /**
     * Returns the current play list
     * @return An array of integers containing the IDs of the tracks in the play list
     */
    public long [] getQueue() {
        synchronized (this) {
            int len = mPlayListLen;
            long [] list = new long[len];
            for (int i = 0; i < len; i++) {
                list[i] = mPlayList[i];
            }
            return list;
        }
    }

    private void openCurrent() {
        synchronized (this) {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
            
            mDurationOverride = -1;
            
            if (mPlayListLen == 0) {
                return;
            }
            stop(false);

            String id = String.valueOf(mPlayList[mPlayPos]);
            
            mCursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mCursorCols, "_id=" + id , null, null);
            if (mCursor != null) {
                if (!mCursor.moveToFirst()) {
                    mCursor.close();
                    mCursor = null;
                    MusicLogUtils.e(TAG, "openCurrent: get cursor fail!");
                }
                open(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id);
                // go to bookmark if needed
                if (isPodcast()) {
                    long bookmark = getBookmark();
                    // Start playing a little bit before the bookmark,
                    // so it's easier to get back in to the narrative.
                    seek(bookmark - 5000);
                }
            }
        }
    }

    /**
     * Opens the specified file and readies it for playback.
     *
     * @param path The full path of the file to be opened.
     */
    public void open(String path) {
        synchronized (this) {
            MusicLogUtils.d(TAG, "open(" + path + ")");
            if (path == null) {
                return;
            }
            
            // if mCursor is null, try to associate path with a database cursor
            if (mCursor == null) {

                ContentResolver resolver = getContentResolver();
                Uri uri;
                String where;
                String selectionArgs[];
                if (path.startsWith("content://media/")) {
                    uri = Uri.parse(path);
                    where = null;
                    selectionArgs = null;
                } else {
                   uri = MediaStore.Audio.Media.getContentUriForPath(path);
                   where = MediaStore.Audio.Media.DATA + "=?";
                   selectionArgs = new String[] { path };
                }
                
                try {
                    mCursor = resolver.query(uri, mCursorCols, where, selectionArgs, null);
                    if  (mCursor != null) {
                        if (mCursor.getCount() == 0) {
                            mCursor.close();
                            mCursor = null;
                        } else {
                            mCursor.moveToNext();
                            ensurePlayListCapacity(1);
                            mPlayListLen = 1;
                            mPlayList[0] = mCursor.getLong(IDCOLIDX);
                            mPlayPos = 0;
                        }
                    }
                } catch (UnsupportedOperationException ex) {
                }
            }
            mFileToPlay = path;
            mIsPlayerReady = false;
            mIsMediaSeekable = true;
            mIsPlaylistCompleted = false;
            
            mTrackCompleted = false;

            // Open Asynchronously
            mPlayer.setDataSourceAsync(mFileToPlay);
            if (! mPlayer.isInitialized()) {
                stop(true);
                if (mOpenFailedCounter++ < 10 &&  mPlayListLen > 1) {
                    // beware: this ends up being recursive because next() calls open() again.
                    if (mIsPrev) {
                        MusicLogUtils.d(TAG, "open(" + path + ") failed, calling prev()");
                        prev();
                    } else {
                        MusicLogUtils.d(TAG, "open(" + path + ") failed, calling next(false)");
                        next(false);
                    }
                }
                if (! mPlayer.isInitialized() && mOpenFailedCounter != 0) {
                    // need to make sure we only shows this once
                    mOpenFailedCounter = 0;
                    // need to play for next time when play fail.
                    mNeedPlayWhenDelete = true;
                    if (!mQuietMode) {
                        mMediaplayerHandler.sendMessage(mMediaplayerHandler.obtainMessage(OPEN_FAILED));
                    }
                    MusicLogUtils.d(TAG, "Failed to open file for playback");
                }
            } else {
                mOpenFailedCounter = 0;
            }
        }
    }

    /**
     * Starts playback of a previously opened file.
     */
    public void play() {
        synchronized(this) {
            MusicLogUtils.d(TAG, ">> play: init=" + mPlayer.isInitialized() + ", ready=" + mIsPlayerReady + ", listlen=" + mPlayListLen);
            mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
            mAudioManager.registerMediaButtonEventReceiver(new ComponentName(this.getPackageName(),
                MediaButtonIntentReceiver.class.getName()));
            // If play() gets called, onPrepare() should call play()
            // and Music should not be silent any more, if error occurred
            mQuietMode = false;
            if (mPlayer.isInitialized() && mIsPlayerReady) {
                // if we are at the end of the song, go to the next song first
                long duration = mPlayer.duration();
                if (mRepeatMode != REPEAT_CURRENT && (mIsPlaylistCompleted ||
                            (duration > 0 && mPlayer.position() >= duration - duration/MAX))) {
                    mIsPlaylistCompleted = false;
                    next(true);
                    // next(true) will call open() and play() again. This play() should end here.
                    notifyChange(PLAYSTATE_CHANGED);
                    MusicLogUtils.d(TAG, "<< play: go to next song first");
                    return;
                }
                mPlayer.start();
                MusicLogUtils.d("MusicPerformanceTest", "[mtk performance result]: " + System.currentTimeMillis());
                // make sure we fade in, in case a previous fadein was stopped because
                // of another focus loss
                mMediaplayerHandler.removeMessages(FADEDOWN);
                mMediaplayerHandler.sendEmptyMessage(FADEUP);

                mIsPlaylistCompleted = false;
                
                updateNotification(this, null);
                
                if (!mIsSupposedToBePlaying) {
                    mIsSupposedToBePlaying = true;
                    // Notify FM radio to stop since Music has started
                    Intent i = new Intent("com.mediatek.FMRadio.FMRadioService.ACTION_TOFMSERVICE_POWERDOWN");
                    sendBroadcast(i);
                    notifyChange(PLAYSTATE_CHANGED);
                }            
                if(mPreAudioId != getAudioId()) {
                    mAsyncAlbumArtWorker = new AlbumArtWorker();
                    mAsyncAlbumArtWorker.execute(Long.valueOf(getAlbumId()));
                }
            } else if (mPlayListLen <= 0 && !(mPlayer.isInitialized() || mIsPlayerReady)) {
                // This is mostly so that if you press 'play' on a bluetooth headset
                // without every having played anything before, it will still play
                // something.
                setShuffleMode(SHUFFLE_AUTO);
            }
            mPreAudioId = -1;
            MusicLogUtils.d(TAG, "<< play");
    }
    }
    
    private void stop(boolean remove_status_icon) {
        synchronized(this) {
            MusicLogUtils.d(TAG, "stop(" + remove_status_icon + ")");
        if (mPlayer.isInitialized()) {
            mPlayer.stop();
        }
            mIsPlayerReady = false;
            mDoSeekWhenPrepared = false;
            mIsMediaSeekable = true;
        mFileToPlay = null;
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        mDurationOverride = -1;
        if (remove_status_icon) {
            gotoIdleState();
        } else {
            stopForeground(false);
        }
        if (remove_status_icon) {
            mIsSupposedToBePlaying = false;
        }
    }
    }

    /**
     * Stops playback.
     */
    public void stop() {
        stop(true);
    }

    /**
     * Pauses playback (call play() to resume)
     */
    public void pause() {
        synchronized(this) {
            MusicLogUtils.d(TAG, "pause");
            // If Music is fading in, force it to stop
            mMediaplayerHandler.removeMessages(FADEUP);
            mPlayer.setVolume(1.0f);
            if (isPlaying() && mPlayer.isInitialized()) {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }
                gotoIdleState();
                mIsSupposedToBePlaying = false;
                notifyChange(PLAYSTATE_CHANGED);
                saveBookmarkIfNeeded();
            }
        }
    }

    /** Returns whether something is currently playing
     *
     * @return true if something is playing (or will be playing shortly, in case
     * we're currently transitioning between tracks), false if not.
     */
    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    /*
      Desired behavior for prev/next/shuffle:

      - NEXT will move to the next track in the list when not shuffling, and to
        a track randomly picked from the not-yet-played tracks when shuffling.
        If all tracks have already been played, pick from the full set, but
        avoid picking the previously played track if possible.
      - when shuffling, PREV will go to the previously played track. Hitting PREV
        again will go to the track played before that, etc. When the start of the
        history has been reached, PREV is a no-op.
        When not shuffling, PREV will go to the sequentially previous track (the
        difference with the shuffle-case is mainly that when not shuffling, the
        user can back up to tracks that are not in the history).

        Example:
        When playing an album with 10 tracks from the start, and enabling shuffle
        while playing track 5, the remaining tracks (6-10) will be shuffled, e.g.
        the final play order might be 1-2-3-4-5-8-10-6-9-7.
        When hitting 'prev' 8 times while playing track 7 in this example, the
        user will go to tracks 9-6-10-8-5-4-3-2. If the user then hits 'next',
        a random track will be picked again. If at any time user disables shuffling
        the next/previous track will be picked in sequential order again.
     */

    public void prev() {
        synchronized (this) {
            MusicLogUtils.d(TAG, "prev");
            if (mShuffleMode == SHUFFLE_NORMAL) {
                // go to previously-played track and remove it from the history
                int histsize = mHistory.size();
                if (histsize == 0) {
                    // prev is a no-op
                    return;
                }
                Integer pos = mHistory.remove(histsize - 1);
                mPlayPos = pos.intValue();
            } else {
                if (mPlayPos > 0) {
                    mPlayPos--;
                } else {
                    mPlayPos = mPlayListLen - 1;
                }
            }
            mIsPrev = true;
            saveBookmarkIfNeeded();
            stop(false);
            openCurrent();
            play();
            notifyChange(META_CHANGED);
            mIsPrev = false;
        }
    }

    public void next(boolean force) {
        synchronized (this) {
            MusicLogUtils.d(TAG, ">> next(" + force + ")");
            if (mPlayListLen <= 0) {
                MusicLogUtils.d(TAG, "No play queue");
                return;
            }

            if (mShuffleMode == SHUFFLE_NORMAL) {
                // Pick random next track from the not-yet-played ones
                // TODO: make it work right after adding/removing items in the queue.

                // Store the current file in the history, but keep the history at a
                // reasonable size
                if (mPlayPos >= 0) {
                    mHistory.add(mPlayPos);
                }
                if (mHistory.size() > MAX_HISTORY_SIZE) {
                    mHistory.removeElementAt(0);
                }

                int numTracks = mPlayListLen;
                int[] tracks = new int[numTracks];
                for (int i=0;i < numTracks; i++) {
                    tracks[i] = i;
                }

                int numHistory = mHistory.size();
                int numUnplayed = numTracks;
                for (int i=0;i < numHistory; i++) {
                    int idx = mHistory.get(i).intValue();
                    if (idx < numTracks && tracks[idx] >= 0) {
                        numUnplayed--;
                        tracks[idx] = -1;
                    }
                }

                // 'numUnplayed' now indicates how many tracks have not yet
                // been played, and 'tracks' contains the indices of those
                // tracks.
                if (numUnplayed <=0) {
                    // everything's already been played
                    if (mRepeatMode == REPEAT_ALL || force) {
                        //pick from full set
                        numUnplayed = numTracks;
                        for (int i=0;i < numTracks; i++) {
                            tracks[i] = i;
                        }
                    } else {
                        // all done
                        gotoIdleState();
                        if (mIsSupposedToBePlaying) {
                            mIsSupposedToBePlaying = false;
                            notifyChange(PLAYSTATE_CHANGED);
                        }
                        return;
                    }
                }
                int skip = mRand.nextInt(numUnplayed);
                int cnt = -1;
                while (true) {
                    while (tracks[++cnt] < 0)
                        ;
                    skip--;
                    if (skip < 0) {
                        break;
                    }
                }
                mPlayPos = cnt;
            } else if (mShuffleMode == SHUFFLE_AUTO) {
                doAutoShuffleUpdate();
                mPlayPos++;
            } else {
                if (mPlayPos >= mPlayListLen - 1) {
                    // we're at the end of the list
                    MusicLogUtils.d(TAG, "next: end of list...");
                    if (mRepeatMode == REPEAT_NONE && !force) {
                        // all done
                        gotoIdleState();
                        
                        // BT AVRCP Start 
                        notifyChange(PLAYBACK_COMPLETE); //AVRCP14
                        // BT AVRCP End
                        
                        // Stops MediaPlayer if the last track is still on removed card
                        if (mEjectingCardPath != null) {
                            if (mCursor != null) {
                                if (!mCursor.isAfterLast()) {
                                    String filePath = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                                    if (filePath.contains(mEjectingCardPath)) {
                                        // Current file under ejecting card, close it
                                        MusicLogUtils.d(TAG, "next: all DONE, but last file is on removed card, close it!!");
                                        closeExternalStorageFiles(mEjectingCardPath);
                                        //notifyChange(QUIT_PLAYBACK);
                                    }
                                }
                            }
                        }
                        
                        mIsSupposedToBePlaying = false;
                        mIsPlaylistCompleted = true;
                        // Force fading up to stop to avoid Music keep calling play() if current status is pause
                        mMediaplayerHandler.removeMessages(FADEUP);
                        mPlayer.setVolume(1.0f);
                        notifyChange(PLAYSTATE_CHANGED);
                        return;
                    } else if (mRepeatMode == REPEAT_ALL || force) {
                        mPlayPos = 0;
                    }
                } else {
                    mPlayPos++;
                }
            }
            saveBookmarkIfNeeded();
            stop(false);
            openCurrent();
            play();
            notifyChange(META_CHANGED);
            MusicLogUtils.d(TAG, "<< next(" + force + ")");
        }
    }
    
    private void gotoIdleState() {
        MusicLogUtils.d(TAG, "gotoIdleState");
        mAlbumArt = null;
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        stopForeground(true);
    }
    
    private void saveBookmarkIfNeeded() {
        try {
            if (isPodcast()) {
                long pos = position();
                long bookmark = getBookmark();
                long duration = duration();
                if ((pos < bookmark && (pos + 10000) > bookmark) ||
                        (pos > bookmark && (pos - 10000) < bookmark)) {
                    // The existing bookmark is close to the current
                    // position, so don't update it.
                    return;
                }
                if (pos < 15000 || (pos + 10000) > duration) {
                    // if we're near the start or end, clear the bookmark
                    pos = 0;
                }
                
                // write 'pos' to the bookmark field
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.BOOKMARK, pos);
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursor.getLong(IDCOLIDX));
                getContentResolver().update(uri, values, null, null);
            }
        } catch (SQLiteException ex) {
        }
    }

    // Make sure there are at least 5 items after the currently playing item
    // and no more than 10 items before.
    private void doAutoShuffleUpdate() {
        boolean notify = false;

        // remove old entries
        if (mPlayPos > 10) {
            removeTracks(0, mPlayPos - 9);
            notify = true;
        }
        // add new entries if needed
        int to_add = 7 - (mPlayListLen - (mPlayPos < 0 ? -1 : mPlayPos));
        for (int i = 0; i < to_add; i++) {
            // pick something at random from the list

            int lookback = mHistory.size();
            int idx = -1;
            while(true) {
                idx = mRand.nextInt(mAutoShuffleList.length);
                if (!wasRecentlyUsed(idx, lookback)) {
                    break;
                }
                lookback /= 2;
            }
            mHistory.add(idx);
            if (mHistory.size() > MAX_HISTORY_SIZE) {
                mHistory.remove(0);
            }
            ensurePlayListCapacity(mPlayListLen + 1);
            mPlayList[mPlayListLen++] = mAutoShuffleList[idx];
            notify = true;
        }
        if (notify) {
            notifyChange(QUEUE_CHANGED);
        }
    }

    // check that the specified idx is not in the history (but only look at at
    // most lookbacksize entries in the history)
    private boolean wasRecentlyUsed(int idx, int lookbacksize) {

        // early exit to prevent infinite loops in case idx == mPlayPos
        if (lookbacksize == 0) {
            return false;
        }

        int histsize = mHistory.size();
        if (histsize < lookbacksize) {
            MusicLogUtils.d(TAG, "lookback too big");
            lookbacksize = histsize;
        }
        int maxidx = histsize - 1;
        for (int i = 0; i < lookbacksize; i++) {
            long entry = mHistory.get(maxidx - i);
            if (entry == idx) {
                return true;
            }
        }
        return false;
    }

    // A simple variation of Random that makes sure that the
    // value it returns is not equal to the value it returned
    // previously, unless the interval is 1.
    private static class Shuffler {
        private int mPrevious;
        private Random mRandom = new Random();
        public int nextInt(int interval) {
            int ret;
            do {
                ret = mRandom.nextInt(interval);
            } while (ret == mPrevious && interval > 1);
            mPrevious = ret;
            return ret;
        }
    };

    private boolean makeAutoShuffleList() {
        ContentResolver res = getContentResolver();
        Cursor c = null;
        try {
            c = res.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.IS_MUSIC + "=1",
                    null, null);
            if (c == null || c.getCount() == 0) {
                return false;
            }
            int len = c.getCount();
            long [] list = new long[len];
            for (int i = 0; i < len; i++) {
                c.moveToNext();
                list[i] = c.getLong(0);
            }
            mAutoShuffleList = list;
            return true;
        } catch (RuntimeException ex) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return false;
    }
    
    /**
     * Removes the range of tracks specified from the play list. If a file within the range is
     * the file currently being played, playback will move to the next file after the
     * range. 
     * @param first The first file to be removed
     * @param last The last file to be removed
     * @return the number of tracks deleted
     */
    public int removeTracks(int first, int last) {
        int numremoved = removeTracksInternal(first, last);
        if (numremoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numremoved;
    }
    
    private int removeTracksInternal(int first, int last) {
        synchronized (this) {
            if (last < first) return 0;
            if (first < 0) first = 0;
            if (last >= mPlayListLen) last = mPlayListLen - 1;

            boolean gotonext = false;
            if (first <= mPlayPos && mPlayPos <= last) {
                mPlayPos = first;
                gotonext = true;
            } else if (mPlayPos > last) {
                mPlayPos -= (last - first + 1);
            }
            int num = mPlayListLen - last - 1;
            for (int i = 0; i < num; i++) {
                mPlayList[first + i] = mPlayList[last + 1 + i];
            }
            mPlayListLen -= last - first + 1;
            MusicLogUtils.d(TAG, "removeTracksInternal need goto next(" + gotonext + ")");
            if (gotonext) {
                if (mPlayListLen == 0) {
                    stop(true);
                    mPlayPos = -1;
                    if (mCursor != null) {
                        mCursor.close();
                        mCursor = null;
                    }
                } else {
                    if (mPlayPos >= mPlayListLen) {
                        mPlayPos = 0;
                    }
                    boolean wasPlaying = isPlaying();
                    mNeedPlayWhenDelete = wasPlaying;
                    stop(false);
                    openCurrent();
                    if (!wasPlaying) {
                        gotoIdleState();
                    } 
                }
                notifyChange(META_CHANGED);
            }
            return last - first + 1;
        }
    }
    
    /**
     * Removes all instances of the track with the given id
     * from the playlist.
     * @param id The id to be removed
     * @return how many instances of the track were removed
     */
    public int removeTrack(long id) {
        MusicLogUtils.d(TAG, "removeTrack>>>: id = " + id);
        int numremoved = 0;
        synchronized (this) {
            for (int i = mPlayListLen - 1; i > mPlayPos; i--) {
                if (mPlayList[i] == id) {
                    MusicLogUtils.d(TAG, "remove rewind(" + i + "),Play position is " + mPlayPos);
                    numremoved += removeTracksInternal(i, i);
                    //i--;
                }
            }
            /// M: 2. Second remove others before mPlayPos. use length to keep circle times
            /// and the circle times must be not bigger than mPlayListLen
            int length = (mPlayPos < mPlayListLen) ? (mPlayPos + 1) : mPlayListLen;
            /// M: Current delete songs position, if don't remove, move to next
            int currentPos= 0;
            for (int i = 0; i < length; i++) {
                if (mPlayList[currentPos] == id) {
                    MusicLogUtils.d(TAG, "remove forward(" + currentPos + "),Play position is "
                            + mPlayPos + ",removed num = " + numremoved);
                    numremoved += removeTracksInternal(currentPos, currentPos);
                } else {
                    currentPos++;
                } 
            }
            /// @}
        }
        if (numremoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        MusicLogUtils.d(TAG, "removeTrack<<<: num = " + numremoved);
        return numremoved;
    }
    
    public void setShuffleMode(int shufflemode) {
        synchronized(this) {
            MusicLogUtils.d(TAG, "setShuffleMode(" + shufflemode + ")");
            if (mShuffleMode == shufflemode && mPlayListLen > 0) {
                return;
            }
            mShuffleMode = shufflemode;
            if (mShuffleMode == SHUFFLE_AUTO) {
                if (makeAutoShuffleList()) {
                    mPlayListLen = 0;
                    doAutoShuffleUpdate();
                    mPlayPos = 0;
                    openCurrent();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                } else {
                    // failed to build a list of files to shuffle
                    mShuffleMode = SHUFFLE_NONE;
                }
            }
            saveQueue(false);
        }
    }
    public int getShuffleMode() {
        return mShuffleMode;
    }
    
    public void setRepeatMode(int repeatmode) {
        synchronized(this) {
            MusicLogUtils.d(TAG, "setRepeatMode(" + repeatmode + ")");
            mRepeatMode = repeatmode;
            saveQueue(false);
        }
    }
    public int getRepeatMode() {
        return mRepeatMode;
    }

    public int getMediaMountedCount() {
        return mMediaMountedCount;
    }

    /**
     * Returns the path of the currently playing file, or null if
     * no file is currently playing.
     */
    public String getPath() {
        return mFileToPlay;
    }
    
    /**
     * Returns the rowid of the currently playing file, or -1 if
     * no file is currently playing.
     */
    public long getAudioId() {
        synchronized (this) {
            if (mPlayPos >= 0 && mPlayer.isInitialized()) {
                return mPlayList[mPlayPos];
            }
        }
        return -1;
    }
    
    /**
     * Returns the position in the queue 
     * @return the position in the queue
     */
    public int getQueuePosition() {
        synchronized(this) {
            return mPlayPos;
        }
    }
    
    /**
     * Starts playing the track at the given position in the queue.
     * @param pos The position in the queue of the track that will be played.
     */
    public void setQueuePosition(int pos) {
        synchronized(this) {
            stop(false);
            mPlayPos = pos;
            openCurrent();
            play();
            notifyChange(META_CHANGED);
            if (mShuffleMode == SHUFFLE_AUTO) {
                doAutoShuffleUpdate();
            }
        }
    }

    public String getArtistName() {
        synchronized(this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        }
    }
    
    public long getArtistId() {
        synchronized (this) {
            if (mCursor == null) {
                return -1;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
        }
    }

    public String getAlbumName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        }
    }

    public long getAlbumId() {
        synchronized (this) {
            if (mCursor == null) {
                return -1;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        }
    }

    public String getTrackName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        }
    }

    // add for Music lyrics supporting ---->
    public String getTrackFilePathName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        }
    }
    // <----

    public String getMIMEType() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
        }
    }

    private boolean isPodcast() {
        synchronized (this) {
            if (mCursor == null) {
                return false;
            }
            return (mCursor.getInt(PODCASTCOLIDX) > 0);
        }
    }
    
    private long getBookmark() {
        synchronized (this) {
            if (mCursor == null) {
                return 0;
            }
            return mCursor.getLong(BOOKMARKCOLIDX);
        }
    }
    
    /**
     * Returns the duration of the file in milliseconds.
     * Currently this method returns -1 for the duration of MIDI files.
     */
    public long duration() {
        if (mDurationOverride != -1) {
            MusicLogUtils.i(TAG, "duration: override=" + mDurationOverride);
            return mDurationOverride;
        }
        synchronized(this) {
        if (mCursor != null) {
            int durationColIdx = mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            if (!mCursor.isNull(durationColIdx)) {
                MusicLogUtils.i(TAG, "duration: mCursor=" + mCursor.getLong(durationColIdx));
                return mCursor.getLong(durationColIdx);
            }
        }
		}
        if (mPlayer.isInitialized() && mIsPlayerReady) {
            mDurationOverride = mPlayer.duration();
            MusicLogUtils.i(TAG, "duration: mPlayer=" + mDurationOverride);
            return mDurationOverride;
        }
        return 0;
    }

    /**
     * Returns the current playback position in milliseconds
     */
    public long position() {
        if (mPlayer.isInitialized() && mIsPlayerReady) {
            long position = mPlayer.position();
            MusicLogUtils.i(TAG, "Position=" + position);
            return position;
        }
        return -1;
    }

    /**
     * Seeks to the position specified.
     *
     * @param pos The position to seek to, in milliseconds
     */
    public long seek(long pos) {
        MusicLogUtils.d(TAG, "seek(" + pos + ")");
        if (mPlayer.isInitialized() && mIsPlayerReady) {
            if (pos != 0 && !(mIsMediaSeekable && mediaCanSeek())) {
                MusicLogUtils.e(TAG, "seek, sorry, seek is not supported");
                return -1;
            }
            if (pos < 0) pos = 0;
            
            // ALPS00122253
            //if (pos > mPlayer.duration()) pos = mPlayer.duration();
            final long d = mPlayer.duration();
            if (pos >= d) {
                pos = d;
            } else {
                // Avoid confusion of seeking after playlist completed
                mIsPlaylistCompleted = false;
            }
            
            return mPlayer.seek(pos);
        }
        return -1;
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId: the audio session ID.
     */
    public void setAudioSessionId(int sessionId) {
        synchronized (this) {
            mPlayer.setAudioSessionId(sessionId);
        }
    }

    /**
     * Returns the audio session ID.
     */
    public int getAudioSessionId() {
        synchronized (this) {
            return mPlayer.getAudioSessionId();
        }
    }

    /**
     * Return true if current media can be sought
     */
    private boolean mediaCanSeek() {
        synchronized (this) {
            if (mCursor == null) {
                return true;
            }
            final String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            return (path != null && 
                    !(path.toLowerCase().endsWith(".imy") && duration() == 0x7fffffffL));
        }
    }
    
    private void updateNotification(Context context, Bitmap bitmap) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.newstatusbar);
        String trackinfo = getTrackName();
        String artist = getArtistName();
        if (artist == null || artist.equals(MediaStore.UNKNOWN_STRING)) {
            artist = getString(R.string.unknown_artist_name);
        }
        if (FeatureOption.MTK_THEMEMANAGER_APP) {
            Resources res = getResources();
            int textColor = res.getThemeMainColor();
            if (textColor != 0) {
                views.setTextColor(R.id.txt_trackinfo, textColor);
            }                
        }
        trackinfo += " - " + artist;
        views.setTextViewText(R.id.txt_trackinfo, trackinfo);
        Intent intent;
        PendingIntent pIntent;
        
        intent = new Intent("com.android.music.PLAYBACK_VIEWER");
        intent.putExtra("collapse_statusbar", true);
        pIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.iv_cover, pIntent);
        
        intent = new Intent(PREVIOUS_ACTION);
        intent.setClass(context, MediaPlaybackService.class);
        pIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.btn_prev, pIntent);
        
        intent = new Intent(PAUSE_ACTION);
        intent.setClass(context, MediaPlaybackService.class);
        pIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.btn_pause, pIntent);
        
        intent = new Intent(NEXT_ACTION);
        intent.setClass(context, MediaPlaybackService.class);
        pIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.btn_next, pIntent);
          
        intent = new Intent("my.nullaction");
        pIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.rl_newstatus, pIntent);
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.iv_cover, bitmap);
            mAlbumArt = bitmap;
        }  
        Notification status = new Notification();
        status.contentView = views;
        status.flags |= Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.stat_notify_musicplayer;
        status.contentIntent = PendingIntent.getService(context, 0, intent, 0);
        status.contentViewTouchHandle = 1;
        startForeground(PLAYBACKSERVICE_STATUS, status);
    }

    /**
     * Provides a unified interface for dealing with midi files and
     * other media files.
     */
    private class MultiPlayer {
        private MediaPlayer mMediaPlayer = new MediaPlayer();
        private Handler mHandler;
        private boolean mIsInitialized = false;

        public MultiPlayer() {
            mMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
        }

        public void setDataSourceAsync(String path) {
            MusicLogUtils.d(TAG, "setDataSourceAsync(" + path + ")");
            try {
                mMediaPlayer.reset();
                //mMediaPlayer.setDataSource(path);
                // Open local files with setDataSource(String path)
                // will cause MediaPlayer error on Android 2.3
                if (path.startsWith("content://")) {
                    mMediaPlayer.setDataSource(MediaPlaybackService.this, Uri.parse(path));
                } else {
                    mMediaPlayer.setDataSource(path);
                }
                
                if (mAuxEffectId > 0) {
                    // Attach auxiliary audio effect only with valid effect id
                    mMediaPlayer.attachAuxEffect(mAuxEffectId);
                    mMediaPlayer.setAuxEffectSendLevel(1.0f);
                    MusicLogUtils.d(TAG, "setDataSourceAsync: attachAuxEffect mAuxEffectId = " + mAuxEffectId);
                }

                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setOnPreparedListener(preparedlistener);
                mMediaPlayer.prepareAsync();
            } catch (IOException ex) {
                // TODO: notify the user why the file couldn't be opened
                MusicLogUtils.e(TAG, "setDataSourceAsync: " + ex);
                mIsInitialized = false;
                return;
            } catch (IllegalArgumentException ex) {
                // TODO: notify the user why the file couldn't be opened
                MusicLogUtils.e(TAG, "setDataSourceAsync: " + ex);
                mIsInitialized = false;
                return;
            } catch (IllegalStateException ex) {
                MusicLogUtils.e(TAG, "setDataSourceAsync: " + ex);
                mIsInitialized = false;
                return;
            }
            mMediaPlayer.setOnCompletionListener(listener);
            mMediaPlayer.setOnErrorListener(errorListener);
            mMediaPlayer.setOnInfoListener(infoListener);
            mMediaPlayer.setOnDurationUpdateListener(durationListener);
            mIsInitialized = true;
        }
        
        public void setDataSource(String path) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setOnPreparedListener(null);
                if (path.startsWith("content://")) {
                    mMediaPlayer.setDataSource(MediaPlaybackService.this, Uri.parse(path));
                } else {
                    mMediaPlayer.setDataSource(path);
                }
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepare();
            } catch (IOException ex) {
                // TODO: notify the user why the file couldn't be opened
                mIsInitialized = false;
                return;
            } catch (IllegalArgumentException ex) {
                // TODO: notify the user why the file couldn't be opened
                mIsInitialized = false;
                return;
            }
            mMediaPlayer.setOnCompletionListener(listener);
            mMediaPlayer.setOnErrorListener(errorListener);
            
            Intent i = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
            i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
            i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
            sendBroadcast(i);
            mMediaPlayer.setOnInfoListener(infoListener);
            
            mIsInitialized = true;
        }
        
        public boolean isInitialized() {
            return mIsInitialized;
        }

        public void start() {
            MusicUtils.debugLog(new Exception("MultiPlayer.start called"));
            mMediaPlayer.start();
        }

        public void stop() {
            mMediaPlayer.reset();
            mIsInitialized = false;
        }

        /**
         * You CANNOT use this player anymore after calling release()
         */
        public void release() {
            stop();
            mMediaPlayer.release();
        }
        
        public void pause() {
            mMediaPlayer.pause();
        }
        
        public void setHandler(Handler handler) {
            mHandler = handler;
        }

        MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
            public boolean onInfo(MediaPlayer mp, int what, int msg) {
                switch (what) {
                case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                    mIsMediaSeekable = false;
                    MusicLogUtils.e(TAG, "onInfo, Disable the seeking for this media");
                    return true;
                case MediaPlayer.MEDIA_INFO_SEEKABLE:
                    if (mPlayPos >= 0 && mPlayList[mPlayPos] > 0) {
                        MusicLogUtils.i(TAG, "onInfo, current track is seekable now!!");
                        mIsMediaSeekable = true;
                    } else {
                        MusicLogUtils.e(TAG, "onInfo, MEDIA_INFO_SEEKABLE is not for current track!!");
                    }
                    return true;
                case MediaPlayer.MEDIA_INFO_AUDIO_NOT_SUPPORTED:
                    MusicLogUtils.e(TAG, "onInfo, Don't support the audio Format!!");
                    // Try to jump to next song in the playlist, if there are any
                    if (mOpenFailedCounter++ < 10 && mPlayListLen > 1 && 
                            (mPlayPos < mPlayListLen - 1 || mRepeatMode == REPEAT_ALL)) {
                        // next() will call stop() before open() next media
                        // beware: this ends up being recursive because next() calls open() again.
                        next(false);
                    } else if (mPlayPos >= mPlayListLen - 1) {
                        MediaPlaybackService.this.stop(true);
                        notifyChange(QUIT_PLAYBACK);
                    } else {
                        MediaPlaybackService.this.stop(true);
                    }
                    if (mOpenFailedCounter != 0) {
                        // need to make sure we only shows this once
                        mOpenFailedCounter = 0;
                        // Notify the user
                        if (!mQuietMode) {
                            Toast.makeText(MediaPlaybackService.this, R.string.fail_to_start_stream, Toast.LENGTH_SHORT).show();
                        }
                        mQuietMode = false;
                    }
                    return true;
                default:
                    break;
                }
                return false;
            }
        };
        
        MediaPlayer.OnDurationUpdateListener durationListener = new MediaPlayer.OnDurationUpdateListener() {
            public void onDurationUpdate(MediaPlayer mp, int duration) {
                MusicLogUtils.i(TAG, ">> onDurationUpdate(" + duration + ")");
                if (mIsPlayerReady) {
                    // workaround for duration update issue
                    mMediaPlayer.getDuration();
                }
                if (duration <= 0) {
                    return;
                }
                
                long currentTrackId = mPlayPos >= 0 ? mPlayList[mPlayPos] : -1;
                
                if (currentTrackId < 0 || mCursor == null) {
                    // unknown track, so return directly
                    MusicLogUtils.e(TAG, "onDurationUpdate: unknown track..");
                    return;
                }
                int oldDuration = mCursor.getInt(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                if ( oldDuration != duration) {
                    ContentValues cv = new ContentValues();
                    MusicLogUtils.i(TAG, "Old Duration is " + oldDuration);
                    cv.put(MediaStore.Audio.Media.DURATION, duration);
                    try {
                        getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
                                cv, "_id = " + currentTrackId, null);
                    } catch (SQLiteException sqlex) {
                        MusicLogUtils.e(TAG, sqlex.toString() + " when updating duration to DB!!");
                        return;
                    }
                    // notify the playlist cursor to change the duration display accordingly
                    getContentResolver().notifyChange(MediaStore.Audio.Playlists.getContentUri("external"), null);
                    MusicLogUtils.i(TAG, "duration updated to DB!!");
                    // re-query for mCursor
                    if (mCursor != null) {
                        mCursor.close();
                        mCursor = null;
                    }
                    synchronized (MediaPlaybackService.this) {
                        mCursor = getContentResolver().query(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                mCursorCols, "_id=" + currentTrackId, null, null);
                        if (mCursor != null) {
                            if (mCursor.getCount() == 0) {
                              MusicLogUtils.e(TAG, "onDurationUpdate: requery for mCursor returns 0 record!!!");
                              mCursor.close();
                              mCursor = null;
                            } else {
                              mCursor.moveToFirst();
                            }
                        }
                    }
                }
                
            }
        };

        MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mTrackCompleted = true;
                mPreAudioId = getAudioId();
                // Acquire a temporary wakelock, since when we return from
                // this callback the MediaPlayer will release its wakelock
                // and allow the device to go to sleep.
                // This temp wakelock ensure MediaPlayer can acquire its wakelock if playlist
                // is not finished. 
                MusicLogUtils.d(TAG, "onCompletion");
                mWakeLock.acquire(3000);
                mHandler.sendEmptyMessage(TRACK_ENDED);
                //mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
            }
        };

        MediaPlayer.OnPreparedListener preparedlistener = new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                synchronized (MediaPlaybackService.this) {
                    MusicLogUtils.d(TAG, ">> onPrepared: doseek=" + mDoSeekWhenPrepared + ", mediaseekable=" + mIsMediaSeekable
                            + ", quietmode=" + mQuietMode);
                    mIsPlayerReady = true;
                    if (mMediaPlayer.getDuration() == 0) {
                        MusicLogUtils.e(TAG, "onPrepared, bad media: duration is 0");
                        final boolean old = mQuietMode;
                        if (mShuffleMode == SHUFFLE_NONE && mRepeatMode != REPEAT_ALL && !mDoSeekWhenPrepared && mPlayPos >= mPlayListLen - 1) {
                            Toast.makeText(MediaPlaybackService.this, R.string.fail_to_start_stream, Toast.LENGTH_SHORT).show();
                        }
                        
                        mQuietMode = true;
                        errorListener.onError(mMediaPlayer, 0, 0);
                        mQuietMode = old;
                        MusicLogUtils.d(TAG, "<< onPrepared, bad media..");
                        return;
                    }
                    if (mDoSeekWhenPrepared && mIsMediaSeekable) {
                        long seekpos = mPreferences.getLong("seekpos", 0);
                        //  AVRCP and Android Music AP supports the FF/REWIND
                        if ( mSeekPositionForAnotherSong != 0) {
                            seekpos = mSeekPositionForAnotherSong;
                            mSeekPositionForAnotherSong = 0;
                        }
                        MusicLogUtils.d(TAG, "seekpos=" + seekpos); 
                        seek(seekpos >= 0 && seekpos <= duration() ? seekpos : 0);
                        MusicLogUtils.d(TAG, "restored queue, currently at position "
                                + position() + "/" + duration()
                                + " (requested " + seekpos + ")");
                        mDoSeekWhenPrepared = false;
                    } else if (!mIsMediaSeekable) {
                        MusicLogUtils.e(TAG, "onPrepared: media NOT seekable, so skip seek operation!!");
                        mDoSeekWhenPrepared = false;
                    }
                    //ALPS00122225
                    if (!mQuietMode && mNeedPlayWhenDelete) {
                        // Should not call play if from reloadQueue() or pausing when delete the current song                    
                        play();
                        notifyChange(META_CHANGED);
                    } else if (!mNeedPlayWhenDelete) {
                        mNeedPlayWhenDelete = true;
                    }
                    MusicLogUtils.d(TAG, "<< onPrepared");
                }
            }
        };
 
        MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    MusicLogUtils.d(TAG, "onError: MEDIA_ERROR_SERVER_DIED");
                    Intent i = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
                    i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
                    i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                    sendBroadcast(i);
                    mIsInitialized = false;
                    mMediaPlayer.release();
                    // Creating a new MediaPlayer and settings its wakemode does not
                    // require the media service, so it's OK to do this now, while the
                    // service is still being restarted
                    mMediaPlayer = new MediaPlayer(); 
                    mMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
                    return true;
                case MEDIA_ERROR_MUSICFX_DIED:
                    // Add for CR ALPS00238519 by jackie
                    // If MusicFX has been killed when Music has been playing with Audio Effect,Music
                    // can not attach the reverb effect id to mediaplayer because all the reverb effect
                    // source have been released. We handle the error message in mediaplayer error listenner.
                    // Set the reverb effect id mAuxEffectId to 0 and send broadcast to open audio effect again,
                    // then open the current song and play again
                    MusicLogUtils.d(TAG, "onError: MEDIA_ERROR_MUSICFX_DIED, extra = " + extra);
                    mAuxEffectId = 0;
                    i = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
                    i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
                    i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                    sendBroadcast(i);
                    openCurrent();
                    play();
                    return true;
                default:
                    /* 
                     * We should have used defined  error codes as constants and handle them by 'case'.
                     * There are some constants in android.media.MediaPlayer but not enough. Because
                     * framework will return many error codes without pre-defined and there are also
                     * some operation status from JNI returned as error. As a result, we have to make
                     * a decision here: 
                     *   1. For operation status INVALID_OPERATION whose code is -38, do nothing
                     *      For every call to MediaPlayer, it will check its state, if the requesting 
                     *      operation is not allowed in the current state, it does nothing and return
                     *      INVALID_OPERATION to JNI. JNI will notify operation status to upper layer
                     *      as a error with MEDIA_ERROR, which, as a result, causes onError is called.
                     *      Because MediaPlayer has protected itself from this operation, we have no
                     *      reason to shutdown the player.
                     *   2. For other codes, notify user and shutdown player.
                     */
                    MusicLogUtils.d(TAG, "onError: what=" + what + ", extra=" + extra);
                    if (what != -38) {
                        boolean isStreaming = (mFileToPlay != null && mFileToPlay.toLowerCase().startsWith("http://")) ? true : false;
                        MusicLogUtils.e("MultiPlayer", "Unknown error (" + what + ", " + extra + ") returned from framework, stop player");
                        // Try to jump to next song in the playlist, if there are any
                        if (mOpenFailedCounter++ < 10 && mPlayListLen > 1 && 
                                (mPlayPos < mPlayListLen - 1 || mRepeatMode == REPEAT_ALL)) {
                            // next() will call stop() before open() next media
                            // beware: this ends up being recursive because next() calls open() again.
                            next(false);
                        } else if (mPlayPos >= mPlayListLen - 1) {
                            MediaPlaybackService.this.stop(true);
                            notifyChange(QUIT_PLAYBACK);
                        } else {
                            MediaPlaybackService.this.stop(true);
                        }
                        if (mOpenFailedCounter != 0) {
                            // need to make sure we only shows this once
                            mOpenFailedCounter = 0;
                            // Notify the user
                            if (!mQuietMode) {
                                Toast.makeText(MediaPlaybackService.this, R.string.fail_to_start_stream, Toast.LENGTH_SHORT).show();
                            }
                            mQuietMode = false;
                        }
                    }
                    return true;
                }
           }
        };

        public long duration() {
            return mMediaPlayer.getDuration();
        }

        public long position() {
            return mMediaPlayer.getCurrentPosition();
        }

        public long seek(long whereto) {
            mMediaPlayer.seekTo((int) whereto);
            return whereto;
        }

        public void setVolume(float vol) {
            mMediaPlayer.setVolume(vol, vol);
        }

        public void setAudioSessionId(int sessionId) {
            mMediaPlayer.setAudioSessionId(sessionId);
        }

        public int getAudioSessionId() {
            return mMediaPlayer.getAudioSessionId();
        }
        public boolean isPlaying() {
            return mMediaPlayer.isPlaying();
        }
        
        // Auxiliary audio effect interface
        public void attachAuxEffect(int effectId) {
            mMediaPlayer.attachAuxEffect(effectId);
        }
        
        public void setAuxEffectSendLevel(float level) {
            mMediaPlayer.setAuxEffectSendLevel(level);
        }

    }

    /*
     * By making this a static class with a WeakReference to the Service, we
     * ensure that the Service can be GCd even when the system process still
     * has a remote reference to the stub.
     */
    static class ServiceStub extends IMediaPlaybackService.Stub {
        WeakReference<MediaPlaybackService> mService;
        
        ServiceStub(MediaPlaybackService service) {
            mService = new WeakReference<MediaPlaybackService>(service);
        }

        public void openFile(String path)
        {
            mService.get().open(path);
        }
        public void open(long [] list, int position) {
            mService.get().open(list, position);
        }
        public int getQueuePosition() {
            return mService.get().getQueuePosition();
        }
        public void setQueuePosition(int index) {
            mService.get().setQueuePosition(index);
        }
        public boolean isPlaying() {
            return mService.get().isPlaying();
        }
        public void stop() {
            mService.get().stop();
        }
        public void pause() {
            mService.get().pause();
        }
        public void play() {
            mService.get().play();
        }
        public void prev() {
            mService.get().prev();
        }
        public void next() {
            mService.get().next(true);
        }
        public String getTrackName() {
            return mService.get().getTrackName();
        }
        // add for Music lyrics supporting ---->
        public String getTrackFilePathName() {
            return mService.get().getTrackFilePathName();
        }
        // <----
        public String getAlbumName() {
            return mService.get().getAlbumName();
        }
        public long getAlbumId() {
            return mService.get().getAlbumId();
        }
        public String getArtistName() {
            return mService.get().getArtistName();
        }
        public long getArtistId() {
            return mService.get().getArtistId();
        }
        public String getMIMEType() {
            return mService.get().getMIMEType();
        }
        public void enqueue(long [] list , int action) {
            mService.get().enqueue(list, action);
        }
        public long [] getQueue() {
            return mService.get().getQueue();
        }
        public void moveQueueItem(int from, int to) {
            mService.get().moveQueueItem(from, to);
        }
        public String getPath() {
            return mService.get().getPath();
        }
        public long getAudioId() {
            return mService.get().getAudioId();
        }
        public long position() {
            return mService.get().position();
        }
        public long duration() {
            return mService.get().duration();
        }
        public long seek(long pos) {
            return mService.get().seek(pos);
        }
        public void setShuffleMode(int shufflemode) {
            mService.get().setShuffleMode(shufflemode);
        }
        public int getShuffleMode() {
            return mService.get().getShuffleMode();
        }
        public int removeTracks(int first, int last) {
            return mService.get().removeTracks(first, last);
        }
        public int removeTrack(long id) {
            return mService.get().removeTrack(id);
        }
        public void setRepeatMode(int repeatmode) {
            mService.get().setRepeatMode(repeatmode);
        }
        public int getRepeatMode() {
            return mService.get().getRepeatMode();
        }
        public int getMediaMountedCount() {
            return mService.get().getMediaMountedCount();
        }
        public int getAudioSessionId() {
            return mService.get().getAudioSessionId();
        }
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.println("" + mPlayListLen + " items in queue, currently at index " + mPlayPos);
        writer.println("Currently loaded:");
        writer.println(getArtistName());
        writer.println(getAlbumName());
        writer.println(getTrackName());
        writer.println(getPath());
        writer.println("playing: " + mIsSupposedToBePlaying);
        writer.println("actual: " + mPlayer.mMediaPlayer.isPlaying());
        writer.println("shuffle mode: " + mShuffleMode);
        MusicUtils.debugDump(writer);
    }

    private final IBinder mBinder = new ServiceStub(this);

// BT AVRCP Start 
final RemoteCallbackList<IBTAvrcpMusicCallback> mAvrcpCallbacksList
            = new RemoteCallbackList<IBTAvrcpMusicCallback>();
     /*
     * By making this a static class with a WeakReference to the Service, we
     * ensure that the Service can be GCd even when the system process still
     * has a remote reference to the stub.
     */
    class ServiceAvrcpStub extends IBTAvrcpMusic.Stub {
        
        WeakReference<MediaPlaybackService> mService;
        
        private int mRepeatMode = 1;
        private int mShuffleMode = 1;
        
        ServiceAvrcpStub(MediaPlaybackService service) {
            mService = new WeakReference<MediaPlaybackService>(service);
        }
        public void registerCallback(IBTAvrcpMusicCallback callback){
            if( callback != null ){
                mAvrcpCallbacksList.register(callback);
            }
            getRepeatMode();
            getShuffleMode();
        }
        public void unregisterCallback(IBTAvrcpMusicCallback callback){
            if( callback != null ){
                mAvrcpCallbacksList.unregister(callback);
            }
        }
        
        public boolean regNotificationEvent(byte eventId, int interval){
            switch(eventId){
                case 0x01: // playstatus
                    mService.get().bPlaybackFlag = true;
                    MusicLogUtils.v(TAG, "[AVRCP] bPlaybackFlag flag is " + mService.get().bPlaybackFlag );
                    return true;
                case 0x02: // track change
                    mService.get().bTrackchangeFlag = true;
                    MusicLogUtils.v(TAG, "[AVRCP] bTrackchange flag is " + mService.get().bTrackchangeFlag );
                    return mService.get().bTrackchangeFlag;
                case 0x09: //playing content
                    mService.get().bTrackNowPlayingChangedFlag = true;
                    return true;
                default:
                    MusicLogUtils.e(TAG, "[AVRCP] MusicApp doesn't support eventId:" + eventId);                
                break;
            }
        
            return false;
        }
        public boolean setPlayerApplicationSettingValue(byte attrId, byte value){
            return false;
        }
        public byte[] getCapabilities(){
            return null;
        }
        public void stop() {
            mService.get().stop();
        }
        public void pause() {
            mService.get().pause();
        }
        public void resume(){
        }
        public void play() {
            mService.get().play();
        }
        public void prev() {
            mService.get().prev();
        }
        public void next() {
            mService.get().next(true);
        }
        public void nextGroup(){
            mService.get().next(true);
        }
        public void prevGroup() {
            mService.get().prev();
        }
        public byte getPlayStatus(){
            return mService.get().getPlayStatus();
        }
        public long getAudioId() {
            return mService.get().getAudioId();
        }        
        public String getTrackName() {
            return mService.get().getTrackName();
        }
        public String getAlbumName() {
            return mService.get().getAlbumName();
        }
        public long getAlbumId() {
            return mService.get().getAlbumId();
        }
        public String getArtistName() {
            return mService.get().getArtistName();
        }
        public long position() {
            return mService.get().position();
        }
        public long duration() {
            return mService.get().duration();
        }
        
        public boolean setEqualizeMode(int equalizeMode){
            return false;
        }
        public int getEqualizeMode(){
            return 0;
        }
        public boolean setShuffleMode(int shufflemode) {
            int mode = 0;
            switch(shufflemode){
                case 1: //SHUFFLE_NONE
                    mode = 0;
                break;
                case 2: //SHUFFLE_NORMAL
                    mode = 1;
                break;
                default:
                return false;                
            }
            MusicLogUtils.d(TAG, "[AVRCP] setShuffleMode music_mode:" + mode);
            mService.get().setShuffleMode(mode);
               //sendBTDelaySetting( 2, mode);
            return true;
        }
        public int getShuffleMode() {
            mShuffleMode = (mService.get().getShuffleMode()+1);
            return mShuffleMode;
        }
        public boolean setRepeatMode(int repeatmode) {
            // avrcp repeat mode to local mode TODO: delay this
            // REPEAT_NONE = 0;
            // REPEAT_CURRENT = 1;
            // REPEAT_ALL = 2;
            int mode = 0;
            switch(repeatmode){
                case 1: //REPEAT_MODE_OFF
                    mode = 0;
                break;
                case 2: //REPEAT_MODE_SINGLE_TRACK
                    mode = 1;
                break;
                case 3: //REPEAT_MODE_ALL_TRACK
                    mode = 2;
                break;
                default:
                return false;
            }
            MusicLogUtils.d(TAG, String.format("[AVRCP] setRepeatMode musid_mode:%d" , mode) );
            mService.get().setRepeatMode(mode);
               //sendBTDelaySetting( 3, mode);
            return true;
        }
        public int getRepeatMode() {
            mRepeatMode = (mService.get().getRepeatMode()+1);
            return mRepeatMode;
        }
        public boolean setScanMode(int scanMode){
            return false;
        }
        public int getScanMode(){
            return 0;
        }
        
        public boolean informDisplayableCharacterSet(int charset){
            if( charset == 0x6a ){
                return true;
            }
            return false;
        }
        
        public boolean informBatteryStatusOfCT(){
            return true;
        }
        
        public void enqueue(long [] list , int action){
            mService.get().enqueue(list, action);
        }
        
        public long [] getNowPlaying(){
            return mService.get().getQueue();
        }
        
        public String getNowPlayingItemName(long id){
            return null;
        }
        public void open(long [] list, int position) {
            mService.get().open(list, position);
        }
        public int getQueuePosition() {
            return mService.get().getQueuePosition();
        }
        public void setQueuePosition(int index) {
            mService.get().setQueuePosition(index);
        }        
        
    }
    
    protected boolean bPlaybackFlag = false;
    protected boolean bTrackchangeFlag = false;
    protected boolean bTrackReachStartFlag = false;
    protected boolean bTrackReachEndFlag = false;
    protected boolean bTrackPosChangedFlag = false;
    protected boolean bTrackAppSettingChangedFlag = false;
    protected boolean bTrackNowPlayingChangedFlag = false;
    
    private final IBinder mBinderAvrcp = new ServiceAvrcpStub(this);    
    
    /* AVRCP callback interface */
    protected void notifyPlaybackStatus(byte status){
        // check the register & callback it back
        //if( true != bPlaybackFlag ){
        //    MusicLogUtils.v(TAG, "notifyPlaybackStatus ignore bPlaybackFlag:" + bPlaybackFlag);
        //    return;
        //}
        bPlaybackFlag = false;
        MusicLogUtils.d(TAG, "[AVRCP] notifyPlaybackStatus " + status);
        
        final int N = mAvrcpCallbacksList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IBTAvrcpMusicCallback listener = mAvrcpCallbacksList.getBroadcastItem(i);
            try {
                listener.notifyPlaybackStatus(status);
            } catch (Exception ex) {
                // The RemoteCallbackList will take care of removing the
                // dead listeners.
            }
        }
        mAvrcpCallbacksList.finishBroadcast();        
        
    }

    protected void notifyTrackChanged(){
        // check the register & callback it back
        //if( true != bTrackchangeFlag ){
        //    return;
        //}
        bTrackchangeFlag = false;
        MusicLogUtils.d(TAG, "[AVRCP] notifyTrackChanged ");
                
        final int N = mAvrcpCallbacksList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IBTAvrcpMusicCallback listener = mAvrcpCallbacksList.getBroadcastItem(i);
            try {
                listener.notifyTrackChanged(getAudioId());
            } catch (Exception ex) {
                // The RemoteCallbackList will take care of removing the
                // dead listeners.
            }
        }
        mAvrcpCallbacksList.finishBroadcast();        
    }
    protected void notifyTrackReachStart(){
        // check the register & callback it back
        if( true != bTrackReachStartFlag ){
            return;
        }
        
        // Default Music Player dones't support this                
    }
    protected void notifyTrackReachEnd(){
        // check the register & callback it back
        if( true != bTrackReachEndFlag ){
            return;
        }
                
        // Default Music Player dones't support this                
    }
    protected void notifyPlaybackPosChanged(){
        if( true != bTrackPosChangedFlag || null == mAvrcpCallbacksList){
            return;
        }

        // Default Music Player dones't support this                
    }
    protected void notifyAppSettingChanged(){
        if( true != bTrackAppSettingChangedFlag|| null == mAvrcpCallbacksList ){
            return;
        }
        bTrackAppSettingChangedFlag = false;
                
        // check the register & callback it back
        final int N = mAvrcpCallbacksList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IBTAvrcpMusicCallback listener = mAvrcpCallbacksList.getBroadcastItem(i);
            try {
                listener.notifyAppSettingChanged();
            } catch (Exception ex) {
                // The RemoteCallbackList will take care of removing the
                // dead listeners.
            }
        }
        mAvrcpCallbacksList.finishBroadcast();
    }
    
    protected void notifyNowPlayingContentChanged(){
        MusicLogUtils.v(TAG, "[AVRCP] notifyNowPlayingContentChanged " );    
        if(null == mAvrcpCallbacksList){
            return;
        }
        bTrackNowPlayingChangedFlag = false;
                
        // check the register & callback it back
        final int N = mAvrcpCallbacksList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IBTAvrcpMusicCallback listener = mAvrcpCallbacksList.getBroadcastItem(i);
            try {
                listener.notifyNowPlayingContentChanged();
            } catch (Exception ex) {
                // The RemoteCallbackList will take care of removing the
                // dead listeners.
            }
        }
        mAvrcpCallbacksList.finishBroadcast();        
    }
    
    protected void notifyVolumehanged(byte volume){
        MusicLogUtils.v(TAG, "[AVRCP] notifyVolumehanged " + volume );    
    }
    
    synchronized protected void notifyBTAvrcp(String s){
        MusicLogUtils.v(TAG, "[AVRCP] notifyBTAvrcp " + s );        
        if( PLAYSTATE_CHANGED.equals(s) ){
            notifyPlaybackStatus(getPlayStatus());
        }
        if( PLAYBACK_COMPLETE.equals(s) ){
            notifyTrackChanged();
            //notifyTrackReachEnd();
        }
        if( QUEUE_CHANGED.equals(s) ){
            notifyTrackChanged();
            notifyNowPlayingContentChanged();
        }
        if( META_CHANGED.equals(s) ){
            notifyTrackChanged();
        }
    }

    protected void sendBTDelaySetting(int attr, int value){
        Message msg = mDelayedStopHandler.obtainMessage();
        msg.what = CHANGE_SETTING_MODE;
        msg.arg1 = attr;
        msg.arg2 = value;
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
    }
// BT AVRCP End


    private class AlbumArtWorker extends AsyncTask<Long, Void, Bitmap> {
        protected Bitmap doInBackground(Long... albumId) {
            Bitmap bm = null;
            try {
                long id = albumId[0].longValue();
                bm = MusicUtils.getArtwork(MediaPlaybackService.this, -1, id, true);
            } catch (Exception ex) {
                MusicLogUtils.e(TAG, "AlbumArtWorker called with wrong parameters");
                return null;
            }
            MusicLogUtils.d(TAG, "AlbumArtWorker: getArtwork returns " + bm);
            return bm;
        }
        
        protected void onPostExecute(Bitmap bm) {
            MusicLogUtils.d(TAG, ">> AlbumArtWorker.onPostExecute");
            if (bm != null && mIsSupposedToBePlaying) {
                updateNotification(MediaPlaybackService.this, bm);
                MusicLogUtils.d(TAG, "<< AlbumArtWorker.onPostExecute");
            }
        }
    }

}
