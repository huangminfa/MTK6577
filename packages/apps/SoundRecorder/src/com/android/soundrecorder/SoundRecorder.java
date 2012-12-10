package com.android.soundrecorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.featureoption.FeatureOption;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Calculates remaining recording time based on available disk space and
 * optionally a maximum recording file size.
 * 
 * The reason why this is not trivial is that the file grows in blocks every few
 * seconds or so, while we want a smooth count down.
 */

class RemainingTimeCalculator {
    public static final int UNKNOWN_LIMIT = 0;
    public static final int FILE_SIZE_LIMIT = 1;
    public static final int DISK_SPACE_LIMIT = 2;

    private static final String TAG = "RemainingTimeCalculator";
    private static final int ONE_SECOND = 1000;
    private static final int BIT_RATE = 8;

    // which of the two limits we will hit (or have fit) first
    private int mCurrentLowerLimit = UNKNOWN_LIMIT;

    private String mSDCardDirectory;

    // State for tracking file size of recording.
    private File mRecordingFile;
    private long mMaxBytes;

    // Rate at which the file grows
    private int mBytesPerSecond;

    // the last time run timeRemaining()
    private long mLastTimeRunTimeRemaining;
    // if recording has been pause
    private boolean mPauseTimeRemaining = false;
    // the last remaining time
    private long mLastRemainingTime = -1;

    // time at which number of free blocks last changed
    private long mBlocksChangedTime;
    // number of available blocks at that time
    private long mLastBlocks;

    // time at which the size of the file has last changed
    private long mFileSizeChangedTime;
    // size of the file at that time
    private long mLastFileSize;

    private final StorageManager mStorageManager;

    public RemainingTimeCalculator(StorageManager storageManager) {
        mStorageManager = storageManager;
        getSDCardDirectory();
    }

    /**
     * Get the SD Card Directory.
     */
    private void getSDCardDirectory() {
        if (mStorageManager != null) {
            mSDCardDirectory = mStorageManager.getDefaultPath();
        }
    }

    /**
     * If called, the calculator will return the minimum of two estimates: how
     * long until we run out of disk space and how long until the file reaches
     * the specified size.
     * 
     * @param file
     *            the file to watch
     * @param maxBytes
     *            the limit
     */

    public void setFileSizeLimit(File file, long maxBytes) {
        mRecordingFile = file;
        mMaxBytes = maxBytes;
    }

    /**
     * Resets the interpolation.
     */
    public void reset() {
        mCurrentLowerLimit = UNKNOWN_LIMIT;
        mBlocksChangedTime = -1;
        mFileSizeChangedTime = -1;
        mPauseTimeRemaining = false;
        mLastRemainingTime = -1;
        mLastBlocks = -1;
        getSDCardDirectory();
    }

    public int getByteRate() {
        return mBytesPerSecond;
    }

    public void setPauseTimeRemaining(boolean pause) {
        mPauseTimeRemaining = pause;
    }

    /**
     * Returns how long (in seconds) we can continue recording.
     */
    public long timeRemaining(boolean isFirstTimeGetRemingTime) {
        // Calculate how long we can record based on free disk space

        boolean blocksNotChangeMore = false;
        StatFs fs = new StatFs(mSDCardDirectory);
        long blocks = fs.getAvailableBlocks() - 1;
        long blockSize = fs.getBlockSize();

        long now = System.currentTimeMillis();

        if (mBlocksChangedTime == -1 || blocks != mLastBlocks) {
            SRLogUtils.i(TAG, "blocks has changed from " + mLastBlocks + " to "
                    + blocks);
            blocksNotChangeMore = (blocks <= mLastBlocks) ? true : false;
            SRLogUtils.i(TAG, "blocksNotChangeMore = " + blocksNotChangeMore);
            mBlocksChangedTime = now;
            mLastBlocks = blocks;
        } else if (blocks == mLastBlocks) {
            blocksNotChangeMore = true;
        }

        /*
         * The calculation below always leaves one free block, since free space
         * in the block we're currently writing to is not added. This last block
         * might get nibbled when we close and flush the file, but we won't run
         * out of disk.
         */

        // at mBlocksChangedTime we had this much time
        float resultTemp = ((float) (mLastBlocks * blockSize - SoundRecorder.LOW_STORAGE_THRESHOLD / 4))
                / mBytesPerSecond;
        // if recording has been pause, we should add pause time to
        // mBlocksChangedTime
        if (mPauseTimeRemaining) {
            mBlocksChangedTime += (now - mLastTimeRunTimeRemaining);
            mPauseTimeRemaining = false;
            SRLogUtils.i(TAG, "<timeRemaining> mPauseTimeRemaining = true");
        }
        mLastTimeRunTimeRemaining = now;
        // so now we have this much time
        resultTemp -= ((float) (now - mBlocksChangedTime)) / ONE_SECOND;
        long result = (long) resultTemp;

        if (mLastRemainingTime == -1) {
            mLastRemainingTime = result;
        }
        if (blocksNotChangeMore && result > mLastRemainingTime) {
            SRLogUtils.i(TAG, "<timeRemaining> result = " + result
                    + " blocksNotChangeMore = true");
            result = mLastRemainingTime;
            SRLogUtils.i(TAG, "<timeRemaining> result = " + result);
        } else {
            mLastRemainingTime = result;
            SRLogUtils.i(TAG, "<timeRemaining> result = " + result);
        }
        if (mRecordingFile == null && !isFirstTimeGetRemingTime) {
            mCurrentLowerLimit = DISK_SPACE_LIMIT;
            SRLogUtils.i(TAG, "mCurrentLowerLimit = DISK_SPACE_LIMIT "
                    + mCurrentLowerLimit);
            return result;
        }

        // If we have a recording file set, we calculate a second estimate
        // based on how long it will take us to reach mMaxBytes.
        if (null != mRecordingFile) {
            mRecordingFile = new File(mRecordingFile.getAbsolutePath());
            long fileSize = mRecordingFile.length();

            if (mFileSizeChangedTime == -1 || fileSize != mLastFileSize) {
                mFileSizeChangedTime = now;
                mLastFileSize = fileSize;
            }

            long result2 = (mMaxBytes - fileSize) / mBytesPerSecond;
            result2 -= (now - mFileSizeChangedTime) / ONE_SECOND;
            result2 -= 1; // just for safety

            mCurrentLowerLimit = result < result2 ? DISK_SPACE_LIMIT
                    : FILE_SIZE_LIMIT;

            SRLogUtils.i(TAG, "mCurrentLowerLimit = " + mCurrentLowerLimit);

            return Math.min(result, result2);
        }
        return (long) 0;
    }

    /**
     * Indicates which limit we will hit (or have hit) first, by returning one
     * of FILE_SIZE_LIMIT or DISK_SPACE_LIMIT or UNKNOWN_LIMIT. We need this to
     * display the correct message to the user when we hit one of the limits.
     */
    public int currentLowerLimit() {
        return mCurrentLowerLimit;
    }

    /**
     * Is there any point of trying to start recording?
     */
    public boolean diskSpaceAvailable() {
        StatFs fs = new StatFs(mSDCardDirectory);
        // keep one free block
        long blocks = fs.getAvailableBlocks() - 1;
        if (blocks <= 1) {
            return false;
        } else {
            int blockSize = fs.getBlockSize();
            long spaceRemaining = ((blocks * blockSize) - (SoundRecorder.LOW_STORAGE_THRESHOLD / 4))
                    / mBytesPerSecond;
            SRLogUtils.i(TAG, "blocks = " + blocks);
            SRLogUtils.i(TAG, "blockSize = " + blockSize);
            SRLogUtils.i(TAG, "spaceRemaining = " + spaceRemaining);
            return spaceRemaining > 0;
        }
    }

    /**
     * Sets the bit rate used in the interpolation.
     * 
     * @param bitRate
     *            the bit rate to set in bits/sec.
     */
    public void setBitRate(int bitRate) {
        mBytesPerSecond = bitRate / BIT_RATE;
    }
}

public class SoundRecorder extends Activity implements Button.OnClickListener,
        Recorder.OnStateChangedListener,
        MediaScannerConnection.MediaScannerConnectionClient {
    static final String TAG = "SoundRecorder";
    static final String PERFORMANCETAG = "SoundRecorderPerformanceTest";
    static final String STATE_FILE_NAME = "soundrecorder.state";
    static final String RECORDER_STATE_KEY = "recorder_state";
    static final String SAMPLE_INTERRUPTED_KEY = "sample_interrupted";
    static final String MAX_FILE_SIZE_KEY = "max_file_size";
    static final String NOT_SAVED_KEY = "not_saved";
    static final String TO_BE_DELETED_FILEPATH_KEY = "to_be_deleted_file_path";
    static final String SELECTED_RECORDING_FORMAT = "selected_recording_format";
    // add recording mode **
    static final String SELECTED_RECORDING_MODE = "selected_recording_mode";
    static final String BYTE_RATE = "byte_rate";

    static final String AUDIO_3GPP = "audio/3gpp";
    static final String AUDIO_VORBIS = "audio/vorbis";
    static final String AUDIO_AMR = "audio/amr";
    static final String AUDIO_AWB = "audio/awb";
    static final String AUDIO_OGG = "application/ogg";
    static final String AUDIO_AAC = "audio/aac";
    static final String AUDIO_ANY = "audio/*";
    static final String ANY_ANY = "*/*";
    static final int HIGH = 0;
    static final int MID = 1;
    static final int LOW = 2;
    // add the recording mode
    static final int NORMAL = 0;
    static final int INDOOR = 1;
    static final int OUTDOOR = 2;

    static final int BITRATE_AMR = 12200; // bits/sec;
    // static final int BITRATE_3GPP = 12200;
    static final int BITRATE_AWB = 28500;
    static final int BITRATE_AAC = 128000;
    static final int BITRATE_VORBIS = 128000;
    private static final int BIT_RATE = 8;
    private static final int TIME_BASE = 60;
    private static final int BYTE_BASE = 1024;
    static final int BYTE_PER_SEC = 1600;

    static final int SAMPLE_RATE_AAC = 48000;
    static final int SAMPLE_RATE_AWB = 16000;
    static final int SAMPLE_RATE_AMR = 8000;
    static final int SAMPLE_RATE_VORBIS = 48000;

    static final String SOUND_RECORDER_DATA = "sound_recorder_data";
    static final String SOUND_RECORDER_DURATION = "sound_recorder_duration";
    static final String EXTRA_MAX_BYTES = android.provider.MediaStore.Audio.Media.EXTRA_MAX_BYTES;

    private String mMimeType = AUDIO_ANY; // for addToMediaDB
    // private String mWhoStartThis = ""; // for MMS
    private boolean mHasFileSizeLimitation = false;
    private boolean mRunFromLauncher = true;
    private int mSelectedFormat = -1;// current recording format:high=0/mid/low
    private int mSelectedMode = -1;
    WakeLock mWakeLock;
    String mRequestedType = AUDIO_ANY;
    Recorder mRecorder;

    // Whether save recorded audio successfully
    // private boolean mSaved = true;
    private boolean mIsDeleted = false;

    boolean mSampleInterrupted = false;

    boolean mIsSDCardmounted = false;
    boolean mIsSDCardFull = false;

    private boolean mSuspended = false;
    private boolean mRecordingFinishedRunFromApp = false;
    String mErrorUiMessage = null; // Some error messages are displayed in the
    // UI,

    // not a dialog. This happens when a recording
    // is interrupted for some reason.
    private long mRecordingTimeLimitation = 180;
    private long mRecordingLeftTime;
    private long mTotalRecordingTime = -1l;// ms
    private long mStartRecordingTime = -1l;// ms
    long mMaxFileSize = -1; // can be specified in the intent
    RemainingTimeCalculator mRemainingTimeCalculator;

    String mTimerFormat;
    final Handler mHandler = new Handler();
    Runnable mUpdateTimer = new Runnable() {
        public void run() {
            updateTimerView();
        }
    };

    private final Handler mSaveHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            showDialog(DB_ERROR_DIALOG);
        }
    };

    ImageButton mRecordButton;
    ImageButton mPlayButton;
    ImageButton mStopButton;
    ImageButton mFileListButton;
    ImageButton mPauseRecordingButton;

    ImageView mStateLED;
    TextView mStateMessage1;// message below the state message
    TextView mStateMessage2;// state message with LED
    ProgressBar mStateProgressBar;
    TextView mTimerView;
    ImageView mCurrState;
    TextView mRecordingFileName;

    LinearLayout mExitButtons;
    Button mAcceptButton;
    Button mDiscardButton;
    VUMeter mVUMeter;
    Menu mMenu;
    LinearLayout mButtonParent;
    private BroadcastReceiver mSDCardMountEventReceiver = null;

    private OnScreenHint mStorageHint;
    private int mStorageStatus;

    private static final long NO_STORAGE_ERROR = -1L;
    private static final long CANNOT_STAT_ERROR = -2L;
    public static final long LOW_STORAGE_THRESHOLD = 512L * 1024L;
    private static final int STORAGE_STATUS_OK = 0;
    private static final int STORAGE_STATUS_LOW = 1;
    private static final int STORAGE_STATUS_NONE = 2;
    private static final long FACTOR_FOR_SECOND_AND_MINUTE = 1000;

    private static final boolean HAVE_AACENCODE_FEATURE = FeatureOption.HAVE_AACENCODE_FEATURE;
    // private static final boolean HAVE_AACENCODE_FEATURE = false;

    private static final boolean HAVE_AWBENCODE_FEATURE = FeatureOption.HAVE_AWBENCODE_FEATURE;
    // private static final boolean HAVE_AWBENCODE_FEATURE = false;

    private static final boolean HAVE_VORBISENC_FEATURE = FeatureOption.HAVE_VORBISENC_FEATURE;
    // private static final boolean HAVE_VORBISENC_FEATURE = false;

    private static final boolean MTK_AUDIO_HD_REC_SUPPORT = FeatureOption.MTK_AUDIO_HD_REC_SUPPORT;

    private static final int OPTIONMENU_SELECT_FORMAT = 0;
    // add the recording mode menu
    private static final int OPTIONMENU_SELECT_MODE = 1;

    private boolean mIsSDCardPlugOut = false;
    private String mRecordingFilePath = null;
    private String mFileName = null;
    private SharedPreferences mPrefs;
    private boolean mConfigChangeRunFromApp = false;
    private boolean mRetainNonConfigChangeHasRun = false;
    private int mByteRate;
    private MediaScannerConnection mConnection;
    private File mRecordingFile;

    private StorageManager mStorageManager = null;

    private String mDoWhat = null;
    private static final int REQURST_FILE_LIST = 1;
    private static final int CHANNEL_CHOOSE_DIALOG = 1;
    private static final int RECORDER_ERROR_DIALOG = 2;
    private static final int DB_ERROR_DIALOG = 3;
    private static final int MODE_CHOOSE_DIALOG = 4;

    public static final String PLAY = "play";
    public static final String RECORD = "record";
    public static final String INIT = "init";
    public static final String DOWHAT = "dowhat";
    private static final String PATH = "path";
    private static final String DURATION = "duration";
    private static final String ERROR = "error";

    @Override
    public void onCreate(Bundle icycle) {
        super.onCreate(icycle);
        SRLogUtils.i(TAG, "onCreate()");
        Intent i = getIntent();
        if (i != null) {
            String s = i.getType();
            mRunFromLauncher = i.getAction().equals(
                    "android.intent.action.MAIN");

            if (AUDIO_AMR.equals(s) || AUDIO_3GPP.equals(s)
                    || AUDIO_ANY.equals(s) || ANY_ANY.equals(s)) {

                mRequestedType = s;
            } else if (s != null) {
                // we only support amr and 3gpp formats right now
                setResult(RESULT_CANCELED);
                finish();
                return;
            }

            mMaxFileSize = i.getLongExtra(EXTRA_MAX_BYTES, -1);
            mHasFileSizeLimitation = (mMaxFileSize != -1);
            if (mMaxFileSize > BYTE_PER_SEC) {
                mRecordingTimeLimitation = (mMaxFileSize - BYTE_PER_SEC * 2)
                        / BYTE_PER_SEC;
                mRecordingLeftTime = mRecordingTimeLimitation;
            }
        }
        // modify for supporting multi-format recording

        if (AUDIO_ANY.equals(mRequestedType) || ANY_ANY.equals(mRequestedType)) {
            if (HAVE_VORBISENC_FEATURE) {
                mRequestedType = AUDIO_VORBIS;
            } else {
                mRequestedType = AUDIO_3GPP;
            }
        }

        setContentView(R.layout.main);

        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);

        initRecorder();

        mConnection = new MediaScannerConnection(this, this);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "SoundRecorder");// PowerManager.ON_AFTER_RELEASE

        initResourceRefs();

        setResult(RESULT_CANCELED);
        registerExternalStorageListener();

        // get the old data
        Bundle myIcycle = (Bundle) getLastNonConfigurationInstance();
        if (myIcycle != null) {
            Bundle recorderState = myIcycle.getBundle(RECORDER_STATE_KEY);
            if (recorderState != null) {
                mRecorder.restoreState(recorderState);
                mTotalRecordingTime = mRecorder.sampleLength()
                        * FACTOR_FOR_SECOND_AND_MINUTE;
                mSampleInterrupted = recorderState.getBoolean(
                        SAMPLE_INTERRUPTED_KEY, false);

                mMaxFileSize = recorderState.getLong(MAX_FILE_SIZE_KEY, -1);

                SRLogUtils.i(TAG, "byteRate got is: " + mByteRate);
                if (null != recorderState.getString(TO_BE_DELETED_FILEPATH_KEY)) {
                    deleteRecordingFile(recorderState
                            .getString(TO_BE_DELETED_FILEPATH_KEY));

                }

                if (0 != mByteRate) {
                    mRemainingTimeCalculator.setBitRate(mByteRate * BIT_RATE);
                }
                SRLogUtils.i(TAG, "Byte rate in onCreate() is:" + mByteRate);
            }
        }

        if (null == mPrefs) {
            mPrefs = getSharedPreferences(SOUND_RECORDER_DATA, 0);
        }
        mSelectedFormat = mPrefs.getInt(SELECTED_RECORDING_FORMAT, LOW);
        mSelectedMode = mPrefs.getInt(SELECTED_RECORDING_MODE, NORMAL);
        mByteRate = mPrefs.getInt(BYTE_RATE, 0);
        SRLogUtils.i(TAG, "oncreate() get byte rate is:" + mByteRate);
        updateUi();

    }

    private void initRecorder() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mRecorder = new Recorder(mStorageManager, audioManager);
        mRecorder.setOnStateChangedListener(this);
        mRemainingTimeCalculator = new RemainingTimeCalculator(mStorageManager);
    }

    private void deleteRecordingFile(String filePath) {
        File recordingFile = new File(filePath);
        if (recordingFile.exists()) {
            boolean result = recordingFile.delete();
            if (result) {
                SRLogUtils.i(TAG, "file delete success");
            }
            mRecordingFilePath = null;
            mIsSDCardPlugOut = false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        SRLogUtils.i(TAG, "Option menu is tapped!");
        mMenu = menu;
        if (HAVE_AACENCODE_FEATURE || HAVE_AWBENCODE_FEATURE
                || HAVE_VORBISENC_FEATURE) {
            if (!mRunFromLauncher
                    || mRecorder.state() == Recorder.RECORDING_STATE
                    || mRecorder.state() == Recorder.PLAYING_STATE
                    || mRecorder.state() == Recorder.PAUSE_RECORDING_STATE
                    || mRecorder.state() == Recorder.PAUSE_PLAYING_STATE) {
                menu.getItem(OPTIONMENU_SELECT_FORMAT).setVisible(false);
                if (MTK_AUDIO_HD_REC_SUPPORT) {
                    menu.getItem(OPTIONMENU_SELECT_MODE).setVisible(false);
                }
            } else {
                menu.getItem(OPTIONMENU_SELECT_FORMAT).setVisible(true);
                if (MTK_AUDIO_HD_REC_SUPPORT) {
                    menu.getItem(OPTIONMENU_SELECT_MODE).setVisible(true);
                    SRLogUtils
                            .i(TAG,
                                    "<onPrepareOptionsMenu>Select Mode item is showing");
                }
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (HAVE_AACENCODE_FEATURE || HAVE_AWBENCODE_FEATURE
                || HAVE_VORBISENC_FEATURE) {
            menu.add(0, OPTIONMENU_SELECT_FORMAT, 0,
                    getString(R.string.voice_quality));
            if (MTK_AUDIO_HD_REC_SUPPORT) {
                menu.add(0, OPTIONMENU_SELECT_MODE, 0,
                        getString(R.string.recording_mode));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (OPTIONMENU_SELECT_FORMAT == id) {
            showDialog(CHANNEL_CHOOSE_DIALOG);
        } else if (OPTIONMENU_SELECT_MODE == id) {
            showDialog(MODE_CHOOSE_DIALOG);
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        AlertDialog chooseDialog;
        switch (id) {
        case CHANNEL_CHOOSE_DIALOG:
            chooseDialog = dlgChooseChannel();
            break;
        case RECORDER_ERROR_DIALOG:
            chooseDialog = recorderErrorDialog(args);
            break;
        case DB_ERROR_DIALOG:
            chooseDialog = dbErrorDialog();
            break;
        case MODE_CHOOSE_DIALOG:
            chooseDialog = modeChooseDialog();
            break;
        default:
            chooseDialog = null;
            break;
        }
        return chooseDialog;
    }

    protected AlertDialog recorderErrorDialog(Bundle args) {
        Resources res = getResources();
        int error = args.getInt(ERROR);

        String message = null;
        String title = null;
        switch (error) {
        case Recorder.SDCARD_ACCESS_ERROR:
            message = res.getString(R.string.error_sdcard_access);
            title = res.getString(R.string.app_name);
            break;
        case Recorder.IN_CALL_RECORD_ERROR:
            // TODO:update error message to reflect that the recording could not
            // be performed during a call.
        case Recorder.INTERNAL_ERROR:
            message = res.getString(R.string.error_app_recorder_occupied);
            title = res.getString(R.string.error_app_failed_title);
            break;
        default:
            break;
        }

        updateUi();
        SRLogUtils.i(TAG, "Update UI for error.");
        if (message == null) {
            return null;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.button_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    finish();
                                }
                            }).setCancelable(false);
            return builder.create();
        }
    }

    protected AlertDialog dbErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.error_mediadb_new_record))
                .setPositiveButton(getString(R.string.button_ok), null)
                .setCancelable(false);
        return builder.create();
    }

    protected AlertDialog modeChooseDialog() {
        CharSequence[] modeArray = null;
        modeArray = new CharSequence[3];
        modeArray[0] = getString(R.string.recording_mode_nomal);
        modeArray[1] = getString(R.string.recording_mode_meeting);
        modeArray[2] = getString(R.string.recording_mode_lecture);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_recording_mode))
                .setSingleChoiceItems(modeArray, mSelectedMode,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                switch (which) {
                                case 0:
                                    mSelectedMode = NORMAL;
                                    break;
                                case 1:
                                    mSelectedMode = INDOOR;
                                    break;
                                case 2:
                                    mSelectedMode = OUTDOOR;
                                    break;
                                default:
                                    break;
                                }
                                dialog.dismiss();
                            }
                        }).setNegativeButton(getString(R.string.cancel), null);

        return builder.create();
    }

    /**
     * The method creates an alert choose dialog
     * 
     * @param args
     *            argument, the boolean value who will indicates whether the
     *            selected files just only one. The prompt message will be
     *            different.
     * @return a dialog
     */
    protected AlertDialog dlgChooseChannel() {
        CharSequence[] encodeFormatArray = null;

        if ((HAVE_AACENCODE_FEATURE) || (HAVE_VORBISENC_FEATURE)) {
            if (HAVE_AWBENCODE_FEATURE) {
                encodeFormatArray = new CharSequence[3];
                if (HAVE_VORBISENC_FEATURE) {
                    encodeFormatArray[0] = getString(R.string.recording_format_high)
                            + "(.ogg)";
                } else if (HAVE_AACENCODE_FEATURE) {
                    encodeFormatArray[0] = getString(R.string.recording_format_high)
                            + "(.3gpp)";
                }
                // encodeFormatArray[0] =
                // getString(R.string.recording_format_high)+"(.3gpp)";
                encodeFormatArray[1] = getString(R.string.recording_format_mid)
                        + "(.3gpp)";
                encodeFormatArray[2] = getString(R.string.recording_format_low)
                        + "(.amr)";
            } else {
                encodeFormatArray = new CharSequence[2];
                if (HAVE_VORBISENC_FEATURE) {
                    encodeFormatArray[0] = getString(R.string.recording_format_high)
                            + "(.ogg)";
                } else if (HAVE_AACENCODE_FEATURE) {
                    encodeFormatArray[0] = getString(R.string.recording_format_high)
                            + "(.3gpp)";
                }
                // encodeFormatArray[0] =
                // getString(R.string.recording_format_high)+"(.3gpp)";
                encodeFormatArray[1] = getString(R.string.recording_format_low)
                        + "(.amr)";
            }
        } else if (HAVE_AWBENCODE_FEATURE) {
            encodeFormatArray = new CharSequence[2];
            if (HAVE_VORBISENC_FEATURE) {
                encodeFormatArray[0] = getString(R.string.recording_format_high)
                        + "(.ogg)";
            } else if (HAVE_AACENCODE_FEATURE) {
                encodeFormatArray[0] = getString(R.string.recording_format_high)
                        + "(.3gpp)";
            }
            // encodeFormatArray[0] =
            // getString(R.string.recording_format_high)+"(.3gpp)";
            encodeFormatArray[1] = getString(R.string.recording_format_low)
                    + "(.amr)";
        } else {
            SRLogUtils.e(TAG, "No featureOption enable");
        }

        // Resources mResources = this.getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_voice_quality)
                .setSingleChoiceItems(encodeFormatArray, mSelectedFormat,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                switch (which) {
                                case 0:
                                    if ((HAVE_AACENCODE_FEATURE)
                                            || (HAVE_VORBISENC_FEATURE)) {
                                        mSelectedFormat = HIGH;
                                    } else if (HAVE_AWBENCODE_FEATURE) {
                                        mSelectedFormat = MID;// mid
                                    } else {
                                        SRLogUtils.e(TAG,
                                                "No featureOption enable");
                                    }
                                    break;

                                case 1:
                                    if ((HAVE_AACENCODE_FEATURE)
                                            || (HAVE_VORBISENC_FEATURE)) {
                                        if (HAVE_AWBENCODE_FEATURE) {
                                            mSelectedFormat = MID;
                                        } else {
                                            mSelectedFormat = LOW;
                                        }
                                    } else if (HAVE_AWBENCODE_FEATURE) {
                                        mSelectedFormat = LOW;
                                    } else {
                                        SRLogUtils.e(TAG,
                                                "No featureOption enable");
                                    }
                                    break;

                                case 2:
                                    mSelectedFormat = LOW;// low
                                    break;

                                default:
                                    break;
                                }
                                dialog.dismiss();
                            }
                        }).setNegativeButton(getString(R.string.cancel), null);

        return builder.create();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        View viewFocus = this.getCurrentFocus();
        int viewId = -1;
        if (viewFocus != null) {
            viewId = viewFocus.getId();
        }
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.main);
        initResourceRefs();

        // flag the state when recording start by MMS
        if (!mRunFromLauncher
                && (Recorder.RECORDING_STATE == mRecorder.state())) {
            if(!this.getResources().getBoolean(R.bool.isTablet)) {        	
                mConfigChangeRunFromApp = true;
            }
        }

        updateUi();

        if (viewId >= 0) {
            View mView = findViewById(viewId);
            if (null != mView) {
                mView.setFocusable(true);
                mView.requestFocus();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SRLogUtils.i(TAG, "onSaveInstanceState()");
        if (mRecorder.sampleLength() == 0) {
            return;
        }

        Bundle recorderState = new Bundle();
        if (!(mDoWhat != null && mDoWhat.equals(PLAY))) {
            mRecorder.saveState(recorderState);
        }
        recorderState.putBoolean(SAMPLE_INTERRUPTED_KEY, mSampleInterrupted);
        recorderState.putLong(MAX_FILE_SIZE_KEY, mMaxFileSize);
        recorderState.putBoolean(NOT_SAVED_KEY, true);
        if (null != mRecordingFilePath && mIsSDCardPlugOut && mDoWhat == null) {
            recorderState.putString(TO_BE_DELETED_FILEPATH_KEY,
                    mRecordingFilePath);
        }

        outState.putBundle(RECORDER_STATE_KEY, recorderState);
        super.onSaveInstanceState(outState);
    }

    /*
     * save the old data
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        SRLogUtils.i(TAG, "onRetainNonConfigurationInstance()");
        mRetainNonConfigChangeHasRun = true;
        if (mRecorder != null
                && mRecorder.state() == Recorder.PAUSE_RECORDING_STATE) {
            mRecorder.stop();
        }
        Bundle outState = new Bundle();
        Bundle recorderState = new Bundle();

        if (mRecorder != null && mRecorder.sampleLength() == 0) {
            return outState;
        }

        if (!(mDoWhat != null && mDoWhat.equals(PLAY))) {
            mRecorder.saveState(recorderState);
        }
        recorderState.putBoolean(SAMPLE_INTERRUPTED_KEY, mSampleInterrupted);
        recorderState.putLong(MAX_FILE_SIZE_KEY, mMaxFileSize);
        recorderState.putBoolean(NOT_SAVED_KEY, true);
        if (null != mRecordingFilePath && mIsSDCardPlugOut && mDoWhat == null) {
            recorderState.putString(TO_BE_DELETED_FILEPATH_KEY,
                    mRecordingFilePath);
        }

        outState.putBundle(RECORDER_STATE_KEY, recorderState);
        return outState;
    }

    /*
     * Whenever the UI is re-created (due f.ex. to orientation change) we have
     * to reinitialize references to the views.
     */
    private void initResourceRefs() {
        mRecordButton = (ImageButton) findViewById(R.id.recordButton);
        mStopButton = (ImageButton) findViewById(R.id.stopButton);
        mPlayButton = (ImageButton) findViewById(R.id.playButton);
        mFileListButton = (ImageButton) findViewById(R.id.fileListButton);
        mPauseRecordingButton = (ImageButton) findViewById(R.id.pauseRecordingButton);
        mButtonParent = (LinearLayout) findViewById(R.id.buttonParent);
        if (mRunFromLauncher) {
            mPlayButton.setOnClickListener(this);
            mFileListButton.setOnClickListener(this);
            mPauseRecordingButton.setOnClickListener(this);
        } else {
            mPlayButton.setVisibility(View.GONE);
            mFileListButton.setVisibility(View.GONE);
            mPauseRecordingButton.setVisibility(View.GONE);
        }

        mRecordButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);

        mStateLED = (ImageView) findViewById(R.id.stateLED);
        mStateMessage1 = (TextView) findViewById(R.id.stateMessage1);
        mStateMessage2 = (TextView) findViewById(R.id.stateMessage2);
        mStateProgressBar = (ProgressBar) findViewById(R.id.stateProgressBar);
        mTimerView = (TextView) findViewById(R.id.timerView);
        mCurrState = (ImageView) findViewById(R.id.currState);
        mRecordingFileName = (TextView) findViewById(R.id.recordingFileName);

        mExitButtons = (LinearLayout) findViewById(R.id.exitButtons);
        mAcceptButton = (Button) findViewById(R.id.acceptButton);
        mDiscardButton = (Button) findViewById(R.id.discardButton);
        mVUMeter = (VUMeter) findViewById(R.id.uvMeter);

        mAcceptButton.setOnClickListener(this);
        mDiscardButton.setOnClickListener(this);

        mTimerFormat = getResources().getString(R.string.timer_format);
        setTitle(getResources().getString(R.string.app_name));

        mVUMeter.setRecorder(mRecorder);
        /*
         * if (FeatureOption.MTK_THEMEMANAGER_APP) {
         * mWhole.setThemeContentBgColor(0xff000000); }
         */
    }

    /*
     * Make sure we're not recording music playing in the background, ask the
     * MediaPlaybackService to pause playback.
     */
    private void stopAudioPlayback() {
        // Shamelessly copied from MediaPlaybackService.java, which
        // should be public, but isn't.
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");

        sendBroadcast(i);
    }

    /*
     * Handle the buttons.
     */
    public void onClick(View button) {
        if (!button.isEnabled()) {
            return;
        }
        String storageState;
        switch (button.getId()) {
        case R.id.recordButton:
            SRLogUtils.i(TAG, "<onClick> recordButton");
            if (mRecorder.state() == Recorder.PAUSE_RECORDING_STATE) {
                stopAudioPlayback();
                mRecorder.goOnRecording();
            } else {
                if ((mRecorder.state() == Recorder.PAUSE_PLAYING_STATE)
                        || (mRecorder.state() == Recorder.PLAYING_STATE)) {
                    mRecorder.stop();
                }
                record();
                long mEndRecordTime = System.currentTimeMillis();
                Log.i(PERFORMANCETAG,
                        "[Performance test][SoundRecorder] recording end ["
                                + mEndRecordTime + "]");
            }
            break;
        case R.id.playButton:
            SRLogUtils.i(TAG, "<onClick> playButton");
            play();
            break;
        case R.id.stopButton:
            SRLogUtils.i(TAG, "<onClick> stopButton");
            // if (mRecorder.state() == Recorder.RECORDING_STATE) {
            // mTotalRecordingTime = System.currentTimeMillis()
            // - mStartRecordingTime;
            // }

            mRecorder.stop();
            mTotalRecordingTime = mRecorder.mPreviousTime;
            long mEndStopTime = System.currentTimeMillis();
            Log.i(PERFORMANCETAG,
                    "[Performance test][SoundRecorder] recording stop end ["
                            + mEndStopTime + "]");
            // if ((null != mWhoStartThis)
            // && (!mWhoStartThis.equals("android.intent.action.MAIN")) &&
            // mIsFromLemei) {
            //
            // saveSample();
            // mSaved = true;
            //
            // finish();
            // }
            break;
        case R.id.acceptButton:
            SRLogUtils.i(TAG, "<onClick> acceptButton");
            mAcceptButton.setEnabled(false);
            mDiscardButton.setEnabled(false);
            mRecordButton.setEnabled(false);

            storageState = mStorageManager.getVolumeState(mStorageManager
                    .getDefaultPath());

            if (storageState != null
                    && !storageState.equals(Environment.MEDIA_MOUNTED)) {
                mSampleInterrupted = true;
                mIsSDCardmounted = false;
                mErrorUiMessage = getResources().getString(
                        R.string.insert_sd_card);
                Bundle bundle = new Bundle();
                bundle.putInt(ERROR, Recorder.SDCARD_ACCESS_ERROR);
                SRLogUtils
                        .i(TAG,
                                "<onClick> acceptButton: sd card is unmounted, show error dialog");
                showDialog(RECORDER_ERROR_DIALOG, bundle);
                return;
            }

            if (!mRunFromLauncher) {
                mRecordingFinishedRunFromApp = true;
            }
            // Refresh recording time before calling stop().
            if (mRecorder != null
                    && mRecorder.state() == Recorder.RECORDING_STATE) {
                mTotalRecordingTime = System.currentTimeMillis()
                        - mStartRecordingTime;
            }

            if (mRecorder != null) {
                mRecorder.stop();

                if (mRecorder.sampleFile() == null) {
                    mIsDeleted = true;
                }
            }

            SRLogUtils.i(TAG, "<onClick>call saveSample when click accept");
            saveSample();
            break;
        case R.id.discardButton:
            SRLogUtils.i(TAG, "<onClick> discardButton");

            storageState = mStorageManager.getVolumeState(mStorageManager
                    .getDefaultPath());

            if (storageState != null
                    && !storageState.equals(Environment.MEDIA_MOUNTED)) {
                mSampleInterrupted = false;
                mIsSDCardmounted = false;
                mRecorder.delete();
                SRLogUtils.i(TAG, "<onClick> discardButton update UI");
                updateUi();
                return;
            }

            if (!mRunFromLauncher) {
                mRecordingFinishedRunFromApp = true;
            }
            deleteFromMediaDB();
            mRecorder.delete();

            this.mIsSDCardFull = false;

            long currentRemainingTime = mRemainingTimeCalculator
                    .timeRemaining(true);
            if (currentRemainingTime < mRecordingLeftTime) {
                mRecordingLeftTime = currentRemainingTime;
            }
            updateUi();

            // /AN: Modified
            if (!mRunFromLauncher) {
                finish();
            }
            mVUMeter.mCurrentAngle = 0;
            mVUMeter.invalidate();
            break;
        case R.id.fileListButton:
            mFileListButton.setEnabled(false);
            SRLogUtils.i(TAG, "<onClick> fileListButton");
            //leave this activity,set mSampleFile is null
            if ((mRecorder != null) && mRecorder.sampleFile() != null) {
                mRecorder.mSampleFile = null;
                mRecorder.mSampleLength = 0;
            }
            Intent mIntent = new Intent();
            mIntent.setClass(this, RecordingFileList.class);
            startActivityForResult(mIntent, REQURST_FILE_LIST);
            break;
        case R.id.pauseRecordingButton:
            SRLogUtils.i(TAG, "<onClick> pauseRecordingButton");
            mRecorder.pauseRecording();
            if (mRemainingTimeCalculator != null) {
                mRemainingTimeCalculator.setPauseTimeRemaining(true);
            }
            break;
        default:
            break;
        }
    }

    // after save to update ui
    private void afterSave(Uri uri) {
        long mEndSaveTime = System.currentTimeMillis();
        Log.i(PERFORMANCETAG,
                "[Performance test][SoundRecorder] recording save end ["
                        + mEndSaveTime + "]");
        SRLogUtils.i(TAG, "<afterSave> start");
        mRecorder.finish();
        if (uri != null) {
            Log.e(TAG, "uri is " + uri);
            SRLogUtils.i(TAG, "<afterSave> set Result as " + uri.toString());
            setResult(RESULT_OK, new Intent().setData(uri));
        }

        if (mRunFromLauncher) {
            mAcceptButton.setEnabled(true);
            mDiscardButton.setEnabled(true);
            if (!mIsDeleted && uri != null) {
                Toast.makeText(SoundRecorder.this,
                        R.string.tell_save_record_success, Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            finish();
        }
        mIsDeleted = false;
        mRecordingFilePath = null;
        mVUMeter.mCurrentAngle = 0;
        mVUMeter.invalidate();
        SRLogUtils.i(TAG, "<afterSave> end");
    }

    public void play() {
        // if recording file is not exist
        if (mRecorder.isPlaying()) {
            mRecorder.pausePlayback();
        } else {
            mRecorder.startPlayback();
        }
    }

    public void record() {
        SRLogUtils.i(TAG, "Record is start!");
        if (mMenu != null) {
            mMenu.close();
        }
        mRemainingTimeCalculator.reset();
        String storageState = mStorageManager.getVolumeState(mStorageManager
                .getDefaultPath());

        if (storageState != null
                && !storageState.equals(Environment.MEDIA_MOUNTED)) {

            mSampleInterrupted = false;

            mIsSDCardmounted = false;

            mErrorUiMessage = getResources().getString(R.string.insert_sd_card);

            mRecorder.delete();

            updateUi();

        } else {
            int outputFileFormat = MediaRecorder.OutputFormat.AMR_NB;
            int recordingType = MediaRecorder.AudioEncoder.AMR_NB;
            String extension = ".amr";
            stopAudioPlayback();
            // set the recording mode
            if (MTK_AUDIO_HD_REC_SUPPORT) {
                switch (mSelectedMode) {
                case NORMAL:
                    mRecorder.setRecordMode(MediaRecorder.HDRecordMode.NORMAL);
                    SRLogUtils.i(TAG, "mSelectedMode" + "is NORMAL");
                    break;
                case INDOOR:
                    mRecorder.setRecordMode(MediaRecorder.HDRecordMode.INDOOR);
                    SRLogUtils.i(TAG, "mSelectedMode" + "is INDOOR");
                    break;
                case OUTDOOR:
                    mRecorder.setRecordMode(MediaRecorder.HDRecordMode.OUTDOOR);
                    SRLogUtils.i(TAG, "mSelectedMode" + "is OUTDOOR");
                    break;
                default:
                    break;
                }
            }
            mRecorder.mSoundRecorderDoWhat = mDoWhat;
            // MMS start sound recorder
            if (!mRunFromLauncher) {
                SRLogUtils.i(TAG, "in mms start branch");

                mHandler.removeCallbacks(mUpdateTimer);
                // delete not save file
                if (mRecorder.sampleFile() != null
                        && mRecorder.sampleFile().exists()) {
                    if (mRecorder.sampleFile().delete()) {
                        SRLogUtils.i(TAG, "<record> delete file ["
                                + mRecorder.sampleFile().getAbsolutePath()
                                + "] fail");
                    }
                }
                // mRecordingLeftTime resume init
                mRecordingLeftTime = mRecordingTimeLimitation;
                mConfigChangeRunFromApp = false;

                mRemainingTimeCalculator.setBitRate(BITRATE_AMR);

                mMimeType = AUDIO_AMR;
                outputFileFormat = MediaRecorder.OutputFormat.AMR_NB;
                recordingType = MediaRecorder.AudioEncoder.AMR_NB;
                extension = ".amr";
                // mRecorder.startRecording(MediaRecorder.OutputFormat.AMR_NB,
                // MediaRecorder.AudioEncoder.AMR_NB, ".amr", this);
            } else if (AUDIO_AMR.equals(mRequestedType)) {
                mRemainingTimeCalculator.setBitRate(BITRATE_AMR);
                mMimeType = AUDIO_AMR;
                outputFileFormat = MediaRecorder.OutputFormat.AMR_NB;
                recordingType = MediaRecorder.AudioEncoder.AMR_NB;
                extension = ".amr";
                // mRecorder.startRecording(MediaRecorder.OutputFormat.AMR_NB,
                // MediaRecorder.AudioEncoder.AMR_NB, ".amr", this);

            } else if (AUDIO_AWB.equals(mRequestedType)) {
                mRemainingTimeCalculator.setBitRate(BITRATE_AWB);
                mMimeType = AUDIO_AWB;
                outputFileFormat = MediaRecorder.OutputFormat.THREE_GPP;
                recordingType = MID;
                extension = ".awb";
                // mRecorder.startRecording(MediaRecorder.OutputFormat.THREE_GPP,
                // MID, ".awb", this);

            } else if (AUDIO_AAC.equals(mRequestedType)) {
                mRemainingTimeCalculator.setBitRate(BITRATE_AAC);
                mMimeType = AUDIO_AAC;
                outputFileFormat = MediaRecorder.OutputFormat.AAC_ADIF;
                recordingType = MediaRecorder.AudioEncoder.AAC;
                extension = ".aac";
                // mRecorder.startRecording(MediaRecorder.OutputFormat.AAC_ADIF,
                // MediaRecorder.AudioEncoder.AAC, ".aac", this);
            } else if (AUDIO_3GPP.equals(mRequestedType)
                    || (AUDIO_VORBIS.equals(mRequestedType))) {
                switch (mSelectedFormat) {
                case HIGH:
                    if (HAVE_VORBISENC_FEATURE) {
                        mRemainingTimeCalculator.setBitRate(BITRATE_VORBIS);

                        mMimeType = AUDIO_OGG;
                        outputFileFormat = MediaRecorder.OutputFormat.OUTPUT_FORMAT_OGG;
                        recordingType = MediaRecorder.AudioEncoder.VORBIS;
                        extension = ".ogg";
                        // mRecorder
                        // .startRecording(
                        // MediaRecorder.OutputFormat.OUTPUT_FORMAT_OGG,
                        // MediaRecorder.AudioEncoder.VORBIS,
                        // ".ogg", this);
                    } else if (HAVE_AACENCODE_FEATURE) {
                        mRemainingTimeCalculator.setBitRate(BITRATE_AAC);
                        mMimeType = AUDIO_3GPP;
                        outputFileFormat = MediaRecorder.OutputFormat.THREE_GPP;
                        recordingType = MediaRecorder.AudioEncoder.AAC;
                        extension = ".3gpp";
                        // mRecorder.startRecording(
                        // MediaRecorder.OutputFormat.THREE_GPP,
                        // MediaRecorder.AudioEncoder.AAC, ".3gpp", this);
                    }
                    break;

                case MID:
                    mRemainingTimeCalculator.setBitRate(BITRATE_AWB);

                    mMimeType = AUDIO_3GPP;
                    outputFileFormat = MediaRecorder.OutputFormat.THREE_GPP;
                    recordingType = MediaRecorder.AudioEncoder.AMR_WB;
                    extension = ".3gpp";
                    // mRecorder.startRecording(
                    // MediaRecorder.OutputFormat.THREE_GPP,
                    // MediaRecorder.AudioEncoder.AMR_WB, ".3gpp", this);

                    break;

                case LOW:
                    mRemainingTimeCalculator.setBitRate(BITRATE_AMR);

                    mMimeType = AUDIO_AMR;
                    outputFileFormat = MediaRecorder.OutputFormat.AMR_NB;
                    recordingType = MediaRecorder.AudioEncoder.AMR_NB;
                    extension = ".amr";
                    // mRecorder.startRecording(MediaRecorder.OutputFormat.AMR_NB,
                    // MediaRecorder.AudioEncoder.AMR_NB, ".amr", this);
                    break;

                default:
                    break;
                }

            } else {
                throw new IllegalArgumentException(
                        "Invalid output file type requested");
            }

            if (mRunFromLauncher
                    && mRemainingTimeCalculator.timeRemaining(false) <= 2) {
                SRLogUtils.i(TAG, "space is not enough");
                mSampleInterrupted = true;
                mIsSDCardFull = true;
                mErrorUiMessage = getResources().getString(
                        R.string.storage_is_full);
                updateUi();
                return;
            } else {
                mRemainingTimeCalculator.reset();
                mRecorder.startRecording(outputFileFormat, recordingType,
                        extension, this);
            }

            if (mMaxFileSize != -1) {
                mRemainingTimeCalculator.setFileSizeLimit(
                        mRecorder.sampleFile(), mMaxFileSize);
            }
            mRecordingFilePath = mRecorder.getRecordingFilePath();
        }
        mStartRecordingTime = System.currentTimeMillis();
        mDoWhat = null;
        mVUMeter.invalidate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            mFileListButton.setEnabled(true);
            Intent intent = data;
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                mDoWhat = bundle.getString(DOWHAT);
                // mWhoStartThis = "android.intent.action.MAIN";
                if (mDoWhat != null) {
                    if (mDoWhat.equals(RECORD)) {
                        record();
                    } else if (mDoWhat.equals(PLAY)) {
                        // playing hide the save & cancel
                        // set the recording file
                        String path = null;
                        if (intent.getExtras() != null
                                && intent.getExtras().getString(PATH) != null) {
                            path = intent.getExtras().getString(PATH);
                            File file = new File(path);
                            mRecorder.mSampleFile = file;
                            mRecorder.mSampleLength = intent.getExtras()
                                    .getInt(DURATION) / 1000;
                            play();
                        }
                    } else {
                        // init the activity
                        initRecorder();
                        mDoWhat = null;
                        mVUMeter.setRecorder(mRecorder);
                    }
                }
                updateUi();
            }
        }
    }

    @Override
    public void onBackPressed() {
        switch (mRecorder.state()) {
        case Recorder.IDLE_STATE:

            if (mRecorder.sampleLength() > 0 && mDoWhat == null) {
                if (mExitButtons.getVisibility() == View.VISIBLE) {
                    mRecorder.delete();
                } else {
                    SRLogUtils
                            .i(TAG,
                                    "<onBackPressed>call saveSample when case IDLE_STATE");
                    saveSample();
                }
            }
            finish();
            break;
        case Recorder.PLAYING_STATE:
            mRecorder.stop();
            if (mDoWhat == null) {
                SRLogUtils
                        .i(TAG,
                                "<onBackPressed>call saveSample when case PLAYING_STATE");
                saveSample();
            }
            break;
        case Recorder.PAUSE_PLAYING_STATE:
            mRecorder.stop();
            if (mDoWhat == null) {
                SRLogUtils
                        .i(TAG,
                                "<onBackPressed>call saveSample when case PAUSE_PLAYING_STATE");
                saveSample();
            }
            break;
        case Recorder.RECORDING_STATE:
            // mRecorder.clear();
            mRecorder.stop();
            break;
        case Recorder.PAUSE_RECORDING_STATE:
            mRecorder.stop();
            break;
        default:
            break;
        }
    }

    @Override
    public void onStop() {
        SRLogUtils.i(TAG, "onStop()");
        if (mRecorder.state() == Recorder.PLAYING_STATE
                || mRecorder.state() == Recorder.RECORDING_STATE) {
            mRecorder.stop();
        }

        if (null == mPrefs) {
            mPrefs = getSharedPreferences(SOUND_RECORDER_DATA, 0);
        }
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt(SELECTED_RECORDING_FORMAT, mSelectedFormat);
        ed.putInt(SELECTED_RECORDING_MODE, mSelectedMode);
        ed.putLong(SOUND_RECORDER_DURATION, mTotalRecordingTime);

        ed.commit();

        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (0 != mByteRate) {
            mRemainingTimeCalculator.setBitRate(mByteRate * BIT_RATE);
        }
        SRLogUtils.i(TAG, "onResume() got byte rate is:" + mByteRate);

        // check current recording file whether exist,if not exist, change
        // current status to initial state
        if (this.mRecorder != null && mRecorder.sampleFile() != null) {
            SRLogUtils.i(TAG, "<onResume> sample file whether exist: "
                    + mRecorder.sampleFile().exists());
            if (!mRecorder.sampleFile().exists()) {
                deleteFromMediaDB();
                mRecorder.delete();
                this.mIsSDCardFull = false;
                mVUMeter.mCurrentAngle = 0;
                mVUMeter.invalidate();
                updateUi();
            }
        }

        if (null == mPrefs) {

            mPrefs = getSharedPreferences(SOUND_RECORDER_DATA, 0);
        }
        mRecordingFilePath = mPrefs.getString(TO_BE_DELETED_FILEPATH_KEY, null);
        SRLogUtils.i(TAG, "<onResume> set mRecordingFilePath as "
                + mRecordingFilePath + " from SharedPreferences");
        mTotalRecordingTime = mPrefs.getLong(SOUND_RECORDER_DURATION, -1l);

        if (null != mRecordingFilePath && "" != mRecordingFilePath) {
            deleteRecordingFile(mRecordingFilePath);
            SRLogUtils.i(TAG, "<onResume> deleteRecordingFile: "
                    + mRecordingFilePath);
        }

        if (null == mRecordingFilePath) {
            SRLogUtils.i(TAG, "RecordingPath is null");
        }
    }

    @Override
    protected void onPause() {
        SRLogUtils.i(TAG, "onPause()");
        releaseWakeLock();
        // mSampleInterrupted = mRecorder.state() == Recorder.RECORDING_STATE;
        if (null == mPrefs) {
            mPrefs = getSharedPreferences(SOUND_RECORDER_DATA, 0);
        }
        SharedPreferences.Editor ed = mPrefs.edit();
        if (null != mRecordingFilePath && mIsSDCardPlugOut && mDoWhat == null) {
            ed.putString(TO_BE_DELETED_FILEPATH_KEY, mRecordingFilePath);
        }
        ed.putInt(BYTE_RATE, mRemainingTimeCalculator.getByteRate());
        SRLogUtils.i(TAG, "byte rate is recorded ,is:"
                + mRemainingTimeCalculator.getByteRate());
        ed.commit();
        mSuspended = true;

        int recorderState = mRecorder.state();
        if (recorderState == Recorder.RECORDING_STATE) {
            mRecorder.stop();
            mTotalRecordingTime = mRecorder.mPreviousTime;
        } else if ((recorderState == Recorder.PLAYING_STATE)
                || (recorderState == Recorder.PAUSE_RECORDING_STATE)) {
            mRecorder.stop();
        }
        super.onPause();
    }

    /*
     * If we have just recorded a sample, this adds it to the media data base
     * and sets the result to the sample's URI.
     */
    private void saveSample() {
        if (mRecorder.sampleLength() == 0) {
            return;
        }
        try {
            mRecorder.sampleFileDelSuffix();
            mRecordingFilePath = mRecorder.getRecordingFilePath();
            SRLogUtils.i(TAG, "<saveSample> mRecorder.sampleFile() = "
                    + mRecorder.sampleFile().getAbsolutePath());
            SaveDataTask saveTask = new SaveDataTask();
            saveTask.execute();
        } catch (UnsupportedOperationException ex) { // Database manipulation
            // failure
            return;
        }
    }

    /*
     * Called on destroy to unregister the SD card mount event receiver.
     */
    @Override
    public void onDestroy() {
        SRLogUtils.i(TAG, "<onDestroy> begin");
        // to do add stop
        if (mRecorder != null
                && mRecorder.state() == Recorder.PAUSE_RECORDING_STATE) {
            mRecorder.stop();
        }
        mSaveHandler.removeCallbacksAndMessages(null);
        if(!mRetainNonConfigChangeHasRun) {
            if (mRecorder == null) {
                SRLogUtils.i(TAG, "mRecorder is null");
            } else {
                String filepath = mRecorder.getRecordingFilePath();
                if (filepath == null) {
                    SRLogUtils.i(TAG, "<onDestroy> recording file path is null");
                } else {
                    SRLogUtils.i(TAG, "<onDestroy> recording file path is "
                            + filepath);
                }
                if (filepath != null && filepath.endsWith(Recorder.TEMP_SUFFIX)) {
                    SRLogUtils.i(TAG, "run mRecorder.delete()");
                    mRecorder.delete();
                }
            }
        }

        if (mSDCardMountEventReceiver != null) {
            unregisterReceiver(mSDCardMountEventReceiver);
            mSDCardMountEventReceiver = null;
        }
        mRemainingTimeCalculator = null;
        mStorageManager = null;
        super.onDestroy();
        SRLogUtils.i(TAG, "<onDestroy> end");
    }
    
    /**
     * release wake lock
     */
    private void releaseWakeLock() {
        // if mWakeLock is not release, release it
        if ((mWakeLock != null) && mWakeLock.isHeld()) {
            mWakeLock.release();
            SRLogUtils.i(TAG, "release WakeLock");
        }
    }

    public void onBackToInit() {
        if (mRecorder.mState == mRecorder.PLAYING_STATE
                || mRecorder.mState == mRecorder.PAUSE_PLAYING_STATE) {
            mRecorder.stop();
        }
        initRecorder();
        mDoWhat = null;
        mVUMeter.setRecorder(mRecorder);
        updateUi();
    }

    /*
     * Registers an intent to listen for ACTION_MEDIA_EJECT/ACTION_MEDIA_MOUNTED
     * notifications.
     */
    private void registerExternalStorageListener() {
        if (mSDCardMountEventReceiver == null) {
            mSDCardMountEventReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        mIsSDCardPlugOut = true;
                        if (mDoWhat == null) {
                            mRecorder.delete();
                        } else {
                            onBackToInit();
                        }
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        mSampleInterrupted = false;
                        mIsSDCardmounted = true;

                        if (mIsSDCardPlugOut && (null != mRecordingFilePath)
                                && mDoWhat == null) {
                            deleteRecordingFile(mRecordingFilePath);
                        } else {
                            mIsSDCardPlugOut = false;
                        }
                        updateUi();
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            iFilter.addDataScheme("file");
            registerReceiver(mSDCardMountEventReceiver, iFilter);
        }
    }

    /*
     * A simple utility to do a query into the databases.
     */
    private Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        try {
            ContentResolver resolver = getContentResolver();
            if (resolver == null) {
                return null;
            }
            return resolver.query(uri, projection, selection, selectionArgs,
                    sortOrder);

        } catch (UnsupportedOperationException ex) {
            return null;
        }
    }

    /*
     * Add the given audioId to the playlist with the given playlistId; and
     * maintain the play_order in the playlist.
     */
    private void addToPlaylist(ContentResolver resolver, int audioId,
            long playlistId) {
        String[] cols = new String[] { "count(*)" };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
                playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        cur.moveToFirst();
        final int base = cur.getInt(0);
        cur.close();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,
                Integer.valueOf(base + audioId));

        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
        Uri result = null;
        try {
            result = resolver.insert(uri, values);
        } catch (UnsupportedOperationException e) {
            SRLogUtils.e(TAG,
                    "<addToPlaylist> insert in DB failed: " + e.getMessage());
            result = null;
        }

        if (result == null) {
            mSaveHandler.sendEmptyMessage(0);
        }

    }

    /*
     * Obtain the id for the default play list from the audio_playlists table.
     */
    private int getPlaylistId(Resources res) {
        Uri uri = MediaStore.Audio.Playlists.getContentUri("external");
        final String[] ids = new String[] { MediaStore.Audio.Playlists._ID };
        final String where = MediaStore.Audio.Playlists.NAME + "=?";
        final String[] args = new String[] { res
                .getString(R.string.audio_db_playlist_name) };

        Cursor cursor = query(uri, ids, where, args, null);
        int id = -1;
        try {
            if (cursor == null) {
                SRLogUtils.v(TAG, "query returns null");
            }
            if (cursor != null) {
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    id = cursor.getInt(0);
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return id;
    }

    /*
     * Create a playlist with the given default playlist name, if no such
     * playlist exists.
     */
    private Uri createPlaylist(Resources res, ContentResolver resolver) {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Audio.Playlists.NAME,
                res.getString(R.string.audio_db_playlist_name));
        Uri uri = null;
        try {
            uri = resolver.insert(
                    MediaStore.Audio.Playlists.getContentUri("external"), cv);
        } catch (UnsupportedOperationException e) {
            SRLogUtils.e(TAG,
                    "<createPlaylist> insert in DB failed: " + e.getMessage());
            uri = null;
        }

        if (uri == null) {
            mSaveHandler.sendEmptyMessage(0);
        }
        return uri;
    }

    public void onMediaScannerConnected() {
        mConnection.scanFile(mRecordingFile.getAbsolutePath(), null);
    }

    public void onScanCompleted(String path, Uri uri) {
        Resources res = getResources();
        long current = System.currentTimeMillis();
        Date date = new Date(current);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources()
                .getString(R.string.audio_db_title_format));
        String title = simpleDateFormat.format(date);

        final String where = MediaStore.Audio.Media.DATA + " LIKE '%"
                + path.replaceFirst("file:///", "") + "'";

        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Audio.Media.IS_MUSIC, "0");
        cv.put(MediaStore.Audio.Media.TITLE, title);
        cv.put(MediaStore.Audio.Media.DATE_ADDED,
                (int) (current / FACTOR_FOR_SECOND_AND_MINUTE));
        cv.put(MediaStore.Audio.Media.DATA, path);
        cv.put(MediaStore.Audio.Media.ARTIST,
                res.getString(R.string.unknown_artist_name));
        cv.put(MediaStore.Audio.Media.ALBUM,
                res.getString(R.string.audio_db_album_name));
        ContentResolver resolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        int result = resolver.update(base, cv, where, null);
        if (result > 0) {
            SRLogUtils.v(TAG, "UPDATE Success!");
        }
        mConnection.disconnect();
    }

    /*
     * delete file frome Media DB, if the file is add by media scanner .
     */
    private void deleteFromMediaDB() {
        if (mRecorder == null || mRecorder.sampleFile() == null) {
            return;
        }
        ContentResolver resolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String[] ids = new String[] { MediaStore.Audio.Media._ID };
        final String where = MediaStore.Audio.Media.DATA
                + " LIKE '%"
                + mRecorder.sampleFile().getAbsolutePath()
                        .replaceFirst("file:///", "") + "'";

        Cursor cursor = query(base, ids, where, null, null);
        try {
            if (null != cursor && cursor.getCount() > 0) {
                int deleteNum = resolver.delete(base, where, null);
                SRLogUtils.i(
                        TAG,
                        "<deleteFromMediaDB> delete "
                                + String.valueOf(deleteNum) + " items in db");
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /*
     * Adds file and returns content uri.
     */
    private Uri addToMediaDB(File file) {
        SRLogUtils.i(TAG, "<addToMediaDB> start");
        if (file == null) {
            SRLogUtils.i(TAG, "<addToMediaDB> file is null, return null");
            return null;
        }
        deleteFromMediaDB();
        Resources res = getResources();
        long current = System.currentTimeMillis();
        Date date = new Date(current);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources()
                .getString(R.string.audio_db_title_format));
        String title = simpleDateFormat.format(date);

        ContentValues cv = new ContentValues();
        // Add Information for MMS
        cv.put(MediaStore.Audio.Media.IS_MUSIC, "0");

        cv.put(MediaStore.Audio.Media.TITLE, title);
        cv.put(MediaStore.Audio.Media.DATE_ADDED,
                (int) (current / FACTOR_FOR_SECOND_AND_MINUTE));
        // cv.put(MediaStore.Audio.Media.DATE_MODIFIED, (int) (modDate /
        // FACTOR_FOR_SECOND_AND_MINUTE));
        SRLogUtils.v(TAG, "File type is " + mMimeType);
        cv.put(MediaStore.Audio.Media.MIME_TYPE, mMimeType);
        cv.put(MediaStore.Audio.Media.ARTIST,
                res.getString(R.string.unknown_artist_name));
        cv.put(MediaStore.Audio.Media.ALBUM,
                res.getString(R.string.audio_db_album_name));

        cv.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());
        cv.put(MediaStore.Audio.Media.DURATION, mRecorder.mPreviousTime);
        SRLogUtils.d(TAG, "Reocrding time output to database is :DURATION= "
                + mRecorder.mPreviousTime);
        mRecordingFile = file;

        ContentResolver resolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        SRLogUtils.d(TAG, "ContentURI: " + base);
        Uri result = null;
        try {
            result = resolver.insert(base, cv);
        } catch (UnsupportedOperationException e) {
            SRLogUtils.e(TAG,
                    "<addToMediaDB> Save in DB failed: " + e.getMessage());
            result = null;
        }

        if (result == null) {
            mSaveHandler.sendEmptyMessage(0);
        } else {
            SRLogUtils.i(TAG, "<addToMediaDB> Save susceeded in DB");
            if (getPlaylistId(res) == -1) {
                createPlaylist(res, resolver);
            }
            int audioId = Integer.valueOf(result.getLastPathSegment());
            if (getPlaylistId(res) != -1) {
                addToPlaylist(resolver, audioId, getPlaylistId(res));
            }
            // Notify those applications such as Music listening to the
            // scanner events that a recorded audio file just created.
            mConnection.connect();
        }
        return result;
    }

    /**
     * Update the big MM:SS timer. If we are in playback, also update the
     * progress bar.
     */
    private void updateTimerView() {
        if (mRecordingFinishedRunFromApp) {
            return;
        }
        Resources res = getResources();
        int state = mRecorder.state();

        boolean ongoing = state == Recorder.RECORDING_STATE
                || state == Recorder.PLAYING_STATE
                || state == Recorder.PAUSE_PLAYING_STATE
                || state == Recorder.PAUSE_RECORDING_STATE;

        long time = (long) -1;
        time = ongoing ? mRecorder.progress() : mRecorder.sampleLength();
        String timeStr = String.format(mTimerFormat, time / TIME_BASE, time
                % TIME_BASE);

        mTimerView.setText(timeStr);

        if ((time / TIME_BASE) >= 100) {
            mTimerView.setTextSize(70);
        } else {
            mTimerView.setTextSize(90);
        }

        if (state == Recorder.PLAYING_STATE
                || state == Recorder.PAUSE_PLAYING_STATE) {
            if (mRecorder.sampleLength() != 0) {
                mStateProgressBar.setProgress((int) (100 * time / mRecorder
                        .sampleLength()));
            }
        } else if (state == Recorder.RECORDING_STATE) {
            updateAndShowStorageHint(true);
            if (mRunFromLauncher || !mHasFileSizeLimitation) {
                updateTimeRemaining();
            } else {
                int t = (int) mRecordingTimeLimitation - mRecorder.progress();
                String timeString = "";
                if (t < TIME_BASE) {
                    timeString = String.format(
                            res.getString(R.string.sec_available), t);
                } else if (t <= mRecordingTimeLimitation) {
                    if (t % TIME_BASE == 0) {
                        timeString = String.format(
                                res.getString(R.string.min_available), t
                                        / TIME_BASE);
                    } else {
                        timeString = String.format(
                                res.getString(R.string.time_available), t
                                        / TIME_BASE, t % TIME_BASE);
                    }
                }
                mStateMessage1.setText(timeString);
            }
        }

        if ((state == Recorder.IDLE_STATE && mRecorder.sampleLength() != 0)) {
            if (!mRunFromLauncher) {
                mHandler.postDelayed(mUpdateTimer, 50);
            }
        }

        if (ongoing) {
            if (mRunFromLauncher || !mHasFileSizeLimitation) {
                mHandler.postDelayed(mUpdateTimer, 50);
            } else {
                if ((mRecordingLeftTime > 0)
                        && (mRecorder.progress() < mRecordingTimeLimitation)
                        && mRecorder.sampleFile().length() < mMaxFileSize) {
                    mRecordingLeftTime--;
                    mHandler.postDelayed(mUpdateTimer,
                            FACTOR_FOR_SECOND_AND_MINUTE);
                } else {
                    mRecorder.stop();
                    mHandler.postDelayed(mUpdateTimer, 50);
                    // mRecordingLeftTime resume init
                    mRecordingLeftTime = mRecordingTimeLimitation;
                    // if ((null != mWhoStartThis)
                    // && (!mWhoStartThis.equals("android.intent.action.MAIN"))
                    // && mIsFromLemei) {
                    // Button acceptButton = (Button)
                    // findViewById(R.id.acceptButton);
                    // if (null != acceptButton) {
                    // acceptButton.performClick();
                    // }
                    // }
                }
            }

        }
    }

    private void updateAndShowStorageHint(boolean mayHaveSd) {
        mStorageStatus = getStorageStatus(mayHaveSd);
        showStorageHint();
    }

    private int getStorageStatus(boolean mayHaveSd) {
        long remaining = mayHaveSd ? getAvailableStorage() : NO_STORAGE_ERROR;
        SRLogUtils.i(TAG, "remaining storate is :" + remaining);
        if (remaining == NO_STORAGE_ERROR) {
            return STORAGE_STATUS_NONE;
        }

        return remaining < LOW_STORAGE_THRESHOLD ? STORAGE_STATUS_LOW
                : STORAGE_STATUS_OK;
    }

    private void showStorageHint() {
        String errorMessage = null;
        switch (mStorageStatus) {
        case STORAGE_STATUS_NONE:
            errorMessage = getString(R.string.insert_sd_card);
            break;
        case STORAGE_STATUS_LOW:
            errorMessage = getString(R.string.spaceIsLow_content);
            break;
        default:
            break;
        }

        if (errorMessage != null) {
            if (mStorageHint == null) {
                mStorageHint = OnScreenHint.makeText(this, errorMessage);
            } else {
                mStorageHint.setText(errorMessage);
            }
            mStorageHint.show();
        } else if (mStorageHint != null) {
            mStorageHint.cancel();
            mStorageHint = null;
        }
    }

    /**
     * Returns
     * 
     * @return number of bytes available, or an ERROR code.
     */
    private long getAvailableStorage() {
        try {
            String storageState = mStorageManager
                    .getVolumeState(mStorageManager.getDefaultPath());

            if ((storageState == null)
                    || storageState.equals(Environment.MEDIA_MOUNTED)) {
                String storageDirectory = mStorageManager.getDefaultPath();

                StatFs stat = new StatFs(storageDirectory);

                // SRLogUtils.i(TAG,"AvailableBlocks is:"+stat.getAvailableBlocks()
                // +"; "+"BlockSize is:"+
                // stat.getBlockSize());

                return (long) stat.getAvailableBlocks()
                        * (long) stat.getBlockSize();
            } else {
                return NO_STORAGE_ERROR;
            }
        } catch (IllegalStateException ex) {
            // if we can't stat the filesystem then we don't know how many
            // free bytes exist. It might be zero but just leave it
            // blank since we really don't know.
            return CANNOT_STAT_ERROR;
        }
    }

    /*
     * Called when we're in recording state. Find out how much longer we can go
     * on recording. If it's under 5 minutes, we display a count-down in the UI.
     * If we've run out of time, stop the recording.
     */
    private void updateTimeRemaining() {

        long t = 0;
        if (mRunFromLauncher || !mHasFileSizeLimitation) {
            t = mRemainingTimeCalculator.timeRemaining(false);
        } else {// //for mms max recording time is 3 min
            t = mRecordingLeftTime;// 180s
        }
        if (t <= 0) {
            mSampleInterrupted = true;

            int limit = mRemainingTimeCalculator.currentLowerLimit();
            switch (limit) {
            case RemainingTimeCalculator.DISK_SPACE_LIMIT:
                SRLogUtils.i(TAG, "Is dist space limit");
                this.mIsSDCardFull = true;

                mErrorUiMessage = getResources().getString(
                        R.string.storage_is_full);
                break;
            case RemainingTimeCalculator.FILE_SIZE_LIMIT:
                mErrorUiMessage = getResources().getString(
                        R.string.max_length_reached);
                break;
            default:
                mErrorUiMessage = null;
                break;
            }
            if (mRecorder.state() == Recorder.RECORDING_STATE) {
                mTotalRecordingTime = System.currentTimeMillis()
                        - mStartRecordingTime;
                SRLogUtils.i(TAG, "mTotalRecordingTime=" + mTotalRecordingTime);
            }
            mRecorder.stop();
            return;
        }

        Resources res = getResources();
        String timeStr = "";

        if (t < TIME_BASE) {
            timeStr = String.format(res.getString(R.string.sec_available), t);

        } else if (t < 540) {
            if ((180 == t) && !mRunFromLauncher) {
                timeStr = String.format(res.getString(R.string.min_available),
                        t / TIME_BASE);
            } else {
                if (t % TIME_BASE == 0) {
                    timeStr = String.format(
                            res.getString(R.string.min_available), t
                                    / TIME_BASE);
                } else {
                    timeStr = String.format(
                            res.getString(R.string.time_available), t
                                    / TIME_BASE, t % TIME_BASE);
                }
            }
        }
        mStateMessage1.setText(timeStr);
    }

    private void updateUIOnIdleState() {
        Resources res = getResources();
        if (mRecorder.sampleLength() == 0) {
            mRecordButton.setEnabled(true);
            mRecordButton.setFocusable(true);
            mRecordButton.setSoundEffectsEnabled(true);
            mStopButton.setEnabled(false);
            mStopButton.setFocusable(false);
            if (mRunFromLauncher) {
                mButtonParent.setWeightSum(3);
                mPlayButton.setVisibility(View.VISIBLE);
                mPlayButton.setEnabled(false);
                mPlayButton.setFocusable(false);
                mStopButton.setVisibility(View.GONE);
                mFileListButton.setVisibility(View.VISIBLE);
                mFileListButton.setEnabled(true);
                mFileListButton.setFocusable(true);
                mPauseRecordingButton.setVisibility(View.GONE);
                mPauseRecordingButton.setSoundEffectsEnabled(false);
            } else {
                mButtonParent.setWeightSum(2);
            }
            mRecordButton.requestFocus();

            mStateMessage1.setVisibility(View.INVISIBLE);
            mStateLED.setVisibility(View.INVISIBLE);
            mStateMessage2.setVisibility(View.INVISIBLE);
            mCurrState.setVisibility(View.GONE);
            mRecordingFileName.setVisibility(View.INVISIBLE);

            mExitButtons.setVisibility(View.INVISIBLE);
            mVUMeter.setVisibility(View.VISIBLE);
            mVUMeter.mCurrentAngle = 0;

            mStateProgressBar.setVisibility(View.INVISIBLE);
        } else {
            mRecordButton.setEnabled(true);
            mRecordButton.setFocusable(true);
            mRecordButton.setSoundEffectsEnabled(true);
            if (mRunFromLauncher) {
                mButtonParent.setWeightSum(3);
                mPlayButton.setVisibility(View.VISIBLE);
                mPlayButton.setEnabled(true);
                mPlayButton.setFocusable(true);
                mPlayButton.setImageResource(R.drawable.play);
                mStopButton.setVisibility(View.VISIBLE);
                mFileListButton.setVisibility(View.GONE);
                mPauseRecordingButton.setVisibility(View.GONE);
                mPauseRecordingButton.setSoundEffectsEnabled(false);
            } else {
                mButtonParent.setWeightSum(2);
            }
            mStopButton.setEnabled(false);
            mStopButton.setFocusable(false);
            mStateMessage1.setVisibility(View.INVISIBLE);
            mStateLED.setVisibility(View.INVISIBLE);
            mStateMessage2.setVisibility(View.INVISIBLE);
            mCurrState.setImageResource(R.drawable.stop);
            mCurrState.setVisibility(View.VISIBLE);
            mRecordingFileName.setVisibility(View.VISIBLE);

            if (mDoWhat != null && mDoWhat.equals(PLAY)) {
                mExitButtons.setVisibility(View.INVISIBLE);
                mStopButton.setVisibility(View.GONE);
                mFileListButton.setVisibility(View.VISIBLE);
            } else {
                mExitButtons.setVisibility(View.VISIBLE);
            }

            SRLogUtils.i(TAG, " in update ui idlestate length>0");
            mVUMeter.setVisibility(View.INVISIBLE);

            mStateProgressBar.setVisibility(View.INVISIBLE);
        }

        if (mSampleInterrupted) {
            mStateMessage2.setVisibility(View.VISIBLE);
            mStateMessage2.setText(res.getString(R.string.recording_stopped));
            // mStateLED.setImageResource(R.drawable.idle_led);
            // mStateLED.setVisibility(View.VISIBLE);
            mStateLED.setVisibility(View.INVISIBLE);
        }

        if (mErrorUiMessage != null) {
            mStateMessage1.setText(mErrorUiMessage);

            if (mIsSDCardmounted
                    && (mErrorUiMessage.toString().equals(getResources()
                            .getString(R.string.insert_sd_card).toString()))) {
                mStateMessage1.setVisibility(View.INVISIBLE);
            } else if (!mIsSDCardFull
                    && (mErrorUiMessage.toString().equals(getResources()
                            .getString(R.string.storage_is_full).toString()))) {
                mStateMessage1.setVisibility(View.INVISIBLE);

                // if ((mStateMessage2.getText().toString())
                // .equals(getResources().getString(
                // R.string.recording_stopped).toString())) {
                // mStateMessage2.setText(res
                // .getString(R.string.press_record));
                // }
            } else {
                mStateMessage1.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateUIOnPausePlayingState() {
        mRecordButton.setEnabled(true);
        mRecordButton.setFocusable(true);
        mRecordButton.setSoundEffectsEnabled(true);
        mPlayButton.setEnabled(true);
        mPlayButton.setFocusable(true);
        mPlayButton.setImageResource(R.drawable.play);
        mStopButton.setVisibility(View.VISIBLE);
        mFileListButton.setVisibility(View.GONE);
        mPauseRecordingButton.setVisibility(View.GONE);
        mPauseRecordingButton.setSoundEffectsEnabled(false);
        mStopButton.setEnabled(true);
        mStopButton.setFocusable(true);

        mStateMessage1.setVisibility(View.INVISIBLE);
        mStateLED.setVisibility(View.INVISIBLE);
        mStateMessage2.setVisibility(View.INVISIBLE);
        mCurrState.setImageResource(R.drawable.pause);
        mCurrState.setVisibility(View.VISIBLE);
        mRecordingFileName.setVisibility(View.VISIBLE);

        if (mDoWhat != null && mDoWhat.equals(PLAY)) {
            mExitButtons.setVisibility(View.INVISIBLE);
        } else {
            mExitButtons.setVisibility(View.VISIBLE);
        }
        Log.i(TAG, " in update ui idlestate length>0");
        mVUMeter.setVisibility(View.INVISIBLE);
    }

    private void updateUIOnRecordingState() {
        Resources res = getResources();
        mRecordButton.setEnabled(false);
        mRecordButton.setFocusable(false);
        mRecordButton.setSoundEffectsEnabled(true);
        mStopButton.setVisibility(View.VISIBLE);
        if (mRunFromLauncher) {
            mPlayButton.setEnabled(false);
            mPlayButton.setFocusable(false);
            mPlayButton.setVisibility(View.GONE);
            mFileListButton.setVisibility(View.GONE);
            mPauseRecordingButton.setVisibility(View.VISIBLE);
            mPauseRecordingButton.setEnabled(true);
            mPauseRecordingButton.setFocusable(true);
            mPauseRecordingButton.setSoundEffectsEnabled(false);
        }
        mStopButton.setEnabled(true);
        mStopButton.setFocusable(true);

        mStateMessage1.setVisibility(View.VISIBLE);
        mStateLED.setVisibility(View.VISIBLE);
        mStateLED.setImageResource(R.drawable.recording_led);
        mStateMessage2.setVisibility(View.VISIBLE);
        mStateMessage2.setText(res.getString(R.string.recording));
        mCurrState.setVisibility(View.GONE);
        mRecordingFileName.setVisibility(View.VISIBLE);

        mExitButtons.setVisibility(View.INVISIBLE);
        mVUMeter.setVisibility(View.VISIBLE);

        mStateProgressBar.setVisibility(View.INVISIBLE);
    }

    private void updateUIInPauseRecordingState() {
        Resources res = getResources();
        mRecordButton.setEnabled(true);
        mRecordButton.setFocusable(true);
        mRecordButton.setSoundEffectsEnabled(false);
        mPlayButton.setVisibility(View.GONE);
        mFileListButton.setVisibility(View.GONE);
        mPauseRecordingButton.setEnabled(false);
        mPauseRecordingButton.setFocusable(false);
        mPauseRecordingButton.setSoundEffectsEnabled(false);
        mStopButton.setVisibility(View.VISIBLE);
        mStopButton.setEnabled(true);
        mStopButton.setFocusable(true);

        mStateMessage1.setVisibility(View.INVISIBLE);
        mStateLED.setVisibility(View.VISIBLE);
        mStateLED.setImageResource(R.drawable.idle_led);
        mStateMessage2.setVisibility(View.VISIBLE);
        mStateMessage2.setText(res.getString(R.string.recording_paused));
        mCurrState.setVisibility(View.GONE);
        mRecordingFileName.setVisibility(View.VISIBLE);

        mExitButtons.setVisibility(View.INVISIBLE);
        mVUMeter.setVisibility(View.VISIBLE);
        mVUMeter.mCurrentAngle = 0;

        mStateProgressBar.setVisibility(View.INVISIBLE);
    }

    private void updateUIOnPlayingState() {
        mRecordButton.setEnabled(true);
        mRecordButton.setFocusable(true);
        mRecordButton.setSoundEffectsEnabled(true);
        mPlayButton.setEnabled(true);
        mPlayButton.setFocusable(true);
        mPlayButton.setImageResource(R.drawable.pause);
        mFileListButton.setVisibility(View.GONE);
        mPauseRecordingButton.setVisibility(View.GONE);
        mPauseRecordingButton.setSoundEffectsEnabled(false);
        mStopButton.setVisibility(View.VISIBLE);
        mStopButton.setEnabled(true);
        mStopButton.setFocusable(true);

        mStateMessage1.setVisibility(View.INVISIBLE);
        mStateLED.setVisibility(View.INVISIBLE);
        mStateMessage2.setVisibility(View.INVISIBLE);
        mCurrState.setVisibility(View.VISIBLE);
        mCurrState.setImageResource(R.drawable.play);
        mRecordingFileName.setVisibility(View.VISIBLE);

        if (mDoWhat != null && mDoWhat.equals(PLAY)) {
            mExitButtons.setVisibility(View.INVISIBLE);
        } else {
            mExitButtons.setVisibility(View.VISIBLE);
        }
        mVUMeter.setVisibility(View.INVISIBLE);
        mStateProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Shows/hides the appropriate child views for the new state.
     */
    private void updateUi() {
        if (mRecordingFinishedRunFromApp) {
            return;
        }
        invalidateOptionsMenu();

        if (mRecorder.sampleFile() != null) {
            mFileName = mRecorder.sampleFile().getName().toString();
            if ((mFileName != null) && mFileName.endsWith(Recorder.TEMP_SUFFIX)) {
                mFileName = mFileName.substring(0,
                        mFileName.lastIndexOf(Recorder.TEMP_SUFFIX));
            }
        }

        if (mFileName == null) {
            mRecordingFileName.setText("");
        } else {
            mRecordingFileName.setText(mFileName);
        }

        if (mRunFromLauncher) {
            mAcceptButton.setText(R.string.save_record);
        } else {
            mAcceptButton.setText(R.string.accept);
        }

        switch (mRecorder.state()) {
        case Recorder.IDLE_STATE:
            updateUIOnIdleState();
            break;
        case Recorder.PAUSE_PLAYING_STATE:
            updateUIOnPausePlayingState();
            break;
        case Recorder.RECORDING_STATE:
            updateUIOnRecordingState();
            break;
        case Recorder.PAUSE_RECORDING_STATE:
            updateUIInPauseRecordingState();
            break;
        case Recorder.PLAYING_STATE:
            updateUIOnPlayingState();
            break;
        default:
            break;
        }

        if (!mConfigChangeRunFromApp) {
            updateTimerView();
        }
        // updateTimerView();
        // if (!((null != mWhoStartThis)
        // && (!mWhoStartThis.equals("android.intent.action.MAIN")) &&
        // mIsMmsRecordingStopped)) {
        // updateTimerView();
        // }
        mVUMeter.invalidate();
    }

    /*
     * Called when Recorder changed it's state.
     */
    public void onStateChanged(int state) {
        PowerManager pm = null;
        if (null != getSystemService(Context.POWER_SERVICE)) {
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        }
        if (null != pm) {
            if (!mSuspended) {
                pm.userActivity(SystemClock.uptimeMillis(), true);
            }
        }
        mSuspended = false;

        if (state == Recorder.PLAYING_STATE
                || state == Recorder.RECORDING_STATE) {

            mSampleInterrupted = false;
            mErrorUiMessage = null;
            // we don't want to go to sleep while recording or playing
            if ((mWakeLock != null) && !mWakeLock.isHeld()) {
                mWakeLock.acquire();
                SRLogUtils.i(TAG, "acquire WakeLock");
            }
        } else {

            if (!mRunFromLauncher && mRecordingLeftTime <= 0 && null != pm) {
                pm.userActivity(SystemClock.uptimeMillis(), false);
            }

            releaseWakeLock();

            mStorageStatus = STORAGE_STATUS_OK;
            showStorageHint();
        }
        updateUi();

    }

    /*
     * Called when MediaPlayer encounters an error.
     */
    public void onError(int error) {
        Bundle bundle = new Bundle();
        bundle.putInt(ERROR, error);
        showDialog(RECORDER_ERROR_DIALOG, bundle);
    }

    /**
     * through AsyncTask to deal with save recording file to database
     */
    public class SaveDataTask extends AsyncTask<Void, Object, Uri> {

        /**
         * save recording file to database
         */
        protected Uri doInBackground(Void... params) {
            return addToMediaDB(mRecorder.sampleFile());
        }

        /**
         * uodate ui
         */
        protected void onPostExecute(Uri result) {
            afterSave(result);
        }
    }
}
