package com.android.soundrecorder;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.storage.StorageManager;

import com.mediatek.featureoption.FeatureOption;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Recorder implements OnCompletionListener {

    class OnErrorPlayer implements MediaPlayer.OnErrorListener {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            stop();
            SRLogUtils.e(TAG, "Player Error Type: " + what);
            Recorder.this.setError(SDCARD_ACCESS_ERROR);
            return true;
        }
    }

    class OnErrorRecorder implements MediaRecorder.OnErrorListener {
        public void onError(MediaRecorder mr, int what, int extra) {
            stop();
            SRLogUtils.e(TAG, "Recorder Error Type: " + what);
            Recorder.this.setError(INTERNAL_ERROR);
        }
    }

    static final String TAG = "Recorder";
    static final String SAMPLE_PREFIX = "record";
    static final String SAMPLE_PATH_KEY = "sample_path";
    static final String SAMPLE_LENGTH_KEY = "sample_length";
    public static final String RECORD_FOLDER = "Recording";

    public static final int IDLE_STATE = 0;
    public static final int RECORDING_STATE = 1;
    public static final int PLAYING_STATE = 2;
    public static final int PAUSE_PLAYING_STATE = 3;
    public static final int PAUSE_RECORDING_STATE = 4;
    private static final int STEREO = 2;
    private static final int MONO = 1;
    private static final int ONE_SECOND = 1000;

    int mState = IDLE_STATE;
    // record soundrecorder class mDoWhat value;
    public String mSoundRecorderDoWhat = null;

    public static final int NO_ERROR = 0;
    public static final int SDCARD_ACCESS_ERROR = 1;
    public static final int INTERNAL_ERROR = 2;
    public static final int IN_CALL_RECORD_ERROR = 3;
    OnErrorPlayer mOnErrorPlayer;
    OnErrorRecorder mOnErrorRecorder;

    public interface OnStateChangedListener {
        void onStateChanged(int state);

        void onError(int error);
    }

    OnStateChangedListener mOnStateChangedListener = null;

    long mSampleStart = 0; // time at which latest record or play operation
    long mPreviousTime = 0;// record previous total time
    // started
    int mSampleLength = 0; // length of current sample
    File mSampleFile = null;
    private int mMode = 0;

    MediaRecorder mRecorder = null;
    MediaPlayer mPlayer = null;

    public static final String TEMP_SUFFIX = ".tmp";
    // String mExtensionBackup = "";

    private final StorageManager mStorageManager;
    private AudioManager mAudioManager = null;
    private OnAudioFocusChangeListener mFocusChangeListener = null;
    private boolean mGetFocus = false;
    private static final boolean MTK_AUDIO_HD_REC_SUPPORT = FeatureOption.MTK_AUDIO_HD_REC_SUPPORT;

    public Recorder(StorageManager storageManager,AudioManager audioManager) {
        mStorageManager = storageManager;
        mAudioManager = audioManager;
        mOnErrorPlayer = new OnErrorPlayer();
        mOnErrorRecorder = new OnErrorRecorder();

        mFocusChangeListener = new OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    SRLogUtils
                            .i(TAG,
                                    "<startPlayback()> audio focus changed to AUDIOFOCUS_GAIN, start play back");
                    mGetFocus = true;
                    startPlayback();
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    SRLogUtils
                            .i(TAG,
                                    "<startPlayback()> audio focus loss, pause play back");
                    mGetFocus = false;
                    pausePlayback();
                }
            }
        };
    }

    public void saveState(Bundle recorderState) {
        recorderState.putString(SAMPLE_PATH_KEY, mSampleFile.getAbsolutePath());
        recorderState.putInt(SAMPLE_LENGTH_KEY, mSampleLength);
    }

    public int getMaxAmplitude() {
        if (mState != RECORDING_STATE) {
            return 0;
        }
        return mRecorder.getMaxAmplitude();
    }

    public String getRecordingFilePath() {
        return (null == mSampleFile) ? null : mSampleFile.getAbsolutePath();
    }

    // if the recording is interrupt
    public void restoreState(Bundle recorderState) {
        String samplePath = recorderState.getString(SAMPLE_PATH_KEY);
        boolean lastSampleNotSaved = recorderState.getBoolean(
                SoundRecorder.NOT_SAVED_KEY, false);

        if (samplePath == null && (!lastSampleNotSaved)) {
            return;
        }
        int sampleLength = recorderState.getInt(SAMPLE_LENGTH_KEY, -1);
        mPreviousTime = sampleLength * ONE_SECOND;

        recorderState.putString(SAMPLE_PATH_KEY, "");
        recorderState.putInt(SAMPLE_LENGTH_KEY, -1);

        if (sampleLength == -1) {
            return;
        }

        File file = new File(samplePath);
        if (!file.exists()) {
            return;
        }
        if (mSampleFile != null
                && mSampleFile.getAbsolutePath().compareTo(
                        file.getAbsolutePath()) == 0) {
            return;
        }

        delete();
        mSampleFile = file;
        mSampleLength = sampleLength;

        signalStateChanged(IDLE_STATE);
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mOnStateChangedListener = listener;
    }

    public int state() {
        return mState;
    }

    public void setRecordMode(int mode) {
        this.mMode = mode;
    }

    public int progress() {
        if (mState == RECORDING_STATE) {
            return (int) ((System.currentTimeMillis() - mSampleStart + mPreviousTime) / ONE_SECOND);
        }
        if (mState == PLAYING_STATE || mState == PAUSE_PLAYING_STATE) {
            return (int) (mPlayer.getCurrentPosition() / ONE_SECOND);
        }
        if (mState == PAUSE_RECORDING_STATE) {
            return (int) (mPreviousTime / ONE_SECOND);
        }
        return 0;
    }

    public int sampleLength() {
        return mSampleLength;
    }

    public File sampleFile() {
        return mSampleFile;
    }

    public void sampleFileDelSuffix() {
        if ((mSampleFile != null) && mSampleFile.exists()) {
            String oldPath = mSampleFile.getAbsolutePath();
            if (oldPath.endsWith(TEMP_SUFFIX)) {
                String newPath = oldPath.substring(0,
                        oldPath.lastIndexOf(TEMP_SUFFIX));
                File newFile = new File(newPath);
                boolean result = mSampleFile.renameTo(newFile);
                if (result) {
                    mSampleFile = newFile;
                    SRLogUtils.i(TAG, "<sampleFileDelSuffix()> rename file <"
                            + oldPath + "> to <" + newPath + ">");
                } else {
                    SRLogUtils.i(TAG,
                            "<sampleFileDelSuffix()> rename file fail");
                }
            } else {                
                SRLogUtils.i(TAG, "<sampleFileDelSuffix()> file <" + oldPath
                        + "> is not end with <.tmp>");
                return;
            }
        }
    }

    /**
     * Resets the recorder state. If a sample was recorded, the file is deleted.
     */
    public void delete() {
        stop();

        if (mSampleFile == null) {
            SRLogUtils.i(TAG, "<delete()> mSampleFile is null");
        } else {
            SRLogUtils.i(
                    TAG,
                    "<delete()> mSampleFile is "
                            + mSampleFile.getAbsolutePath());
            boolean result = mSampleFile.delete();
            if (result) {
                SRLogUtils.i(TAG, "<delete()> file delete success");
            } else {
                SRLogUtils.i(TAG, "<delete()> file delete fail");
            }
        }

        SRLogUtils.i(TAG, "<delete> set mSampleFile = null");
        mSampleFile = null;
        mSampleLength = 0;

        signalStateChanged(IDLE_STATE);
    }

    /**
     * Resets the recorder state. If a sample was recorded, the file is left on
     * disk and will be reused for a new recording.
     */
    public void clear() {
        stop();

        mSampleLength = 0;

        signalStateChanged(IDLE_STATE);
    }

    /**
     * Reset recorder sample file and state. If another recording is started,
     * new sample file will be created.
     */
    // /AN: add
    public void finish() {
        stop();
        SRLogUtils.i(TAG, "<finish> set mSampleFile = null");
        mSampleFile = null;
        mSampleLength = 0;
        signalStateChanged(IDLE_STATE);
    }

    public void startRecording(int outputfileformat, int recordingType,
            String extension, Context context) {
        SRLogUtils.i(TAG, "in startRecording() 4 param");

        if (mSampleFile != null
                && mSampleFile.exists()
                && !(mSoundRecorderDoWhat != null && mSoundRecorderDoWhat
                        .equals(SoundRecorder.PLAY))) {
            if (!mSampleFile.delete()) {
                SRLogUtils.i(TAG, "<startRecording> delete file fail");
            }
        }

        stop();

        // back then extension and add the temp suffix.
        // mExtensionBackup = extension;
        // ZQY modify
        String myExtension = extension + TEMP_SUFFIX;
        // String myExtension = extension;
        // extension= extension+TEMP_SUFFIX;
        SRLogUtils.i(TAG, "<startRecording> set mSampleFile = null");
        mSampleFile = null;
        if (mSampleLength > 0) {
            mSampleLength = 0;
        }
        // if (mSampleFile == null) {
        File sampleDir = null;
        if (mStorageManager != null) {
            sampleDir = new File(mStorageManager.getDefaultPath());
            SRLogUtils.i(TAG, "sd card directory is:" + sampleDir);
        }
        String sampleDirPath = null;
        if (sampleDir != null) {
            sampleDirPath = sampleDir.getAbsolutePath() + File.separator
                    + RECORD_FOLDER;
        }
        if (sampleDirPath != null) {
            sampleDir = new File(sampleDirPath);
        }
        if (sampleDir != null && !sampleDir.exists()) {
            if (!sampleDir.mkdirs()) {
                SRLogUtils.i(TAG, "<startRecording> make dirs fail");
            }
        }
        try {
            if (null != sampleDir) {
                SRLogUtils.i(TAG, "SR sampleDir  is:" + sampleDir.toString());
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    "yyyyMMddHHmmss");
            String time = simpleDateFormat.format(new Date(System
                    .currentTimeMillis()));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(SAMPLE_PREFIX).append(time)
                    .append(myExtension);
            String name = stringBuilder.toString();
            mSampleFile = new File(sampleDir, name);
            boolean result = mSampleFile.createNewFile();
            if (result) {
                SRLogUtils.i(TAG, "creat file success");
            }

            SRLogUtils.i(TAG, "SR mSampleFile.getAbsolutePath() is: "
                    + mSampleFile.getAbsolutePath());

        } catch (IOException e) {
            setError(SDCARD_ACCESS_ERROR);
            SRLogUtils.i(TAG, "io exception happens");
            return;
        }
        // } else {
        // // add the temp suffix.
        // StringBuilder stringBuilder = new StringBuilder();
        // String path = mSampleFile.getAbsolutePath();
        // stringBuilder.append(path).append(TEMP_SUFFIX);
        // path = stringBuilder.toString();
        // File tempFile = new File(path);
        // boolean result = mSampleFile.renameTo(tempFile);
        // if(result){
        // SRLogUtils.i(TAG, "file rename success");
        // }
        // mSampleFile = tempFile;
        // SRLogUtils.i(TAG, "SR mSampleFile.getAbsolutePath() is: "
        // + mSampleFile.getAbsolutePath());
        // }

        mRecorder = new MediaRecorder();
        if (MTK_AUDIO_HD_REC_SUPPORT) {
            mRecorder.setHDRecordMode(mMode, false);
        }
        mRecorder.setOnErrorListener(mOnErrorRecorder);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(outputfileformat);

        mRecorder.setOutputFile(mSampleFile.getAbsolutePath());
        SRLogUtils.i(TAG, "SR file path is: " + mSampleFile.getAbsolutePath());
        switch (recordingType) {
        case MediaRecorder.AudioEncoder.AAC:
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setAudioEncodingBitRate(SoundRecorder.BITRATE_AAC);
            mRecorder.setAudioSamplingRate(SoundRecorder.SAMPLE_RATE_AAC);
            mRecorder.setAudioChannels(STEREO);
            break;

        case MediaRecorder.AudioEncoder.AMR_WB:
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            mRecorder.setAudioEncodingBitRate(SoundRecorder.BITRATE_AWB);
            mRecorder.setAudioSamplingRate(SoundRecorder.SAMPLE_RATE_AWB);
            break;

        case MediaRecorder.AudioEncoder.AMR_NB:
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            break;

        case MediaRecorder.AudioEncoder.VORBIS:
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.VORBIS);
            mRecorder.setAudioEncodingBitRate(SoundRecorder.BITRATE_VORBIS);
            mRecorder.setAudioSamplingRate(SoundRecorder.SAMPLE_RATE_VORBIS);
            mRecorder.setAudioChannels(STEREO);
            break;

        default:
            break;

        }

        // Handle IOException
        try {
            mRecorder.prepare();
        } catch (IOException exception) {
            SRLogUtils.i(TAG, "recorder IO exception in recorder.prepare()");
            setError(INTERNAL_ERROR);
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            return;
        }

        // Handle RuntimeException if the recording couldn't start
        try {
            mRecorder.start();
        } catch (RuntimeException exception) {
            AudioManager audioMngr = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            boolean isInCall = ((audioMngr.getMode() == AudioManager.MODE_IN_CALL) || (audioMngr
                    .getMode() == AudioManager.MODE_IN_COMMUNICATION));
            if (isInCall) {
                setError(IN_CALL_RECORD_ERROR);
            } else {
                setError(INTERNAL_ERROR);
                SRLogUtils.i(TAG,
                        "recorder illegalstate exception in recorder.start()");
            }
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            return;
        }
        mPreviousTime = 0;
        mSampleStart = System.currentTimeMillis();
        setState(RECORDING_STATE);
    }

    public void goOnRecording() {
        if (mRecorder == null) {
            return;
        }
        mRecorder.start();
        mSampleStart = System.currentTimeMillis();
        setState(RECORDING_STATE);
    }

    public void stopRecording() {
        if (mRecorder == null) {
            return;
        }

        // boolean mark status
        boolean isAdd = false;
        if (mState == RECORDING_STATE) {
            isAdd = true;
        }

        try {
            mRecorder.stop();
        } catch (RuntimeException exception) {
            setError(INTERNAL_ERROR);
            SRLogUtils.i(TAG,
                    "recorder illegalstate exception in recorder.stop()");
        }
        // /AN: add
        mRecorder.reset();

        mRecorder.release();
        mRecorder = null;

        if (isAdd) {
            mPreviousTime += System.currentTimeMillis() - mSampleStart;
        }
        mSampleLength = (int) (mPreviousTime / ONE_SECOND);

        // if mSampleLength == 0, delete recording file
        // (because when mSampleLength == 0,SoundRecorder will not show
        // accept/discard button)
        if ((mSampleLength == 0) && (mSampleFile != null)) {
            if (mSampleFile.delete()) {
                SRLogUtils
                        .i(TAG,
                                "<stopRecording> mSampleLength == 0,recording file delete success");
            }
            SRLogUtils.i(TAG, "<stopRecording> set mSampleFile = null when mSampleLength == 0");
            mSampleFile = null;
        }

        setState(IDLE_STATE);
    }

    public void startPlayback() {
        if (null == mSampleFile || mAudioManager == null
                || mFocusChangeListener == null) {
            return;
        }

        if (!mGetFocus) {
            int result = mAudioManager.requestAudioFocus(mFocusChangeListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                SRLogUtils.i(TAG, "<startPlayback()> request audio focus fail");
                mGetFocus = false;
                return;
            } else {
                SRLogUtils.i(TAG,
                        "<startPlayback()> request audio focus success");
                mGetFocus = true;
            }
        }

        if (null == mPlayer) {
            stop();
            //modify the nullpointer exception
            if(mSampleFile == null){
                SRLogUtils.i(TAG, "<startPlayback()> mSampleFile is null");
                setState(IDLE_STATE);
                return;
            }
            mPlayer = new MediaPlayer();
            mPlayer.setOnErrorListener(mOnErrorPlayer);
            try {
                mPlayer.setDataSource(mSampleFile.getAbsolutePath());
                mPlayer.setOnCompletionListener(this);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IllegalArgumentException e) {
                setError(INTERNAL_ERROR);
                mPlayer = null;
                abandonAudioFocus();
                return;
            } catch (IOException e) {
                setError(SDCARD_ACCESS_ERROR);
                mPlayer = null;
                abandonAudioFocus();
                return;
            }
            mSampleStart = System.currentTimeMillis();
        } else {
            mPlayer.start();
        }
        setState(PLAYING_STATE);
    }

    public void pausePlayback() {
        if (mPlayer == null) {
            return;
        }
        
        mPlayer.pause();
        abandonAudioFocus();
        setState(PAUSE_PLAYING_STATE);
    }

    public void pauseRecording() {
        if (mRecorder == null) {
            return;
        }
        mRecorder.pause();
        mPreviousTime += System.currentTimeMillis() - mSampleStart;
        setState(PAUSE_RECORDING_STATE);
    }

    public boolean isPlaying() {
        if (mPlayer == null) {
            return false;
        } else {
            return mPlayer.isPlaying();
        }
    }

    public void stopPlayback() {
        if (mPlayer == null) {// we were not in playback
            return;
        }

        mPlayer.stop();
        abandonAudioFocus();
        mPlayer.release();
        mPlayer = null;
        setState(IDLE_STATE);
    }

    public void stop() {
        stopRecording();
        stopPlayback();
        // Remove the temp suffix.
        // if (null != mSampleFile) {
        // String path = mSampleFile.getAbsolutePath();
        // if (path.endsWith(TEMP_SUFFIX)) {
        // path = path.replaceAll(mExtensionBackup + TEMP_SUFFIX,
        // mExtensionBackup);
        // File finalFile = new File(path);
        // boolean result = mSampleFile.renameTo(finalFile);
        // if(result){
        // SRLogUtils.i(TAG, "file rename success");
        // }
        // mSampleFile = finalFile;
        // }
        // }
    }

    public void onCompletion(MediaPlayer mp) {
        stop();
    }

    private void setState(int state) {
        if (state == mState) {
            return;
        }

        mState = state;
        signalStateChanged(mState);
    }

    private void signalStateChanged(int state) {
        if (mOnStateChangedListener != null) {
            mOnStateChangedListener.onStateChanged(state);
        }
    }

    private void setError(int error) {
        if (mOnStateChangedListener != null) {
            mOnStateChangedListener.onError(error);
        }
    }
    
    private void abandonAudioFocus() {
        if (mGetFocus && mAudioManager != null && mFocusChangeListener != null) {
            if(mAudioManager.abandonAudioFocus(mFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                SRLogUtils.i(TAG, "<abandonAudioFocus()> abandon audio focus success");
                mGetFocus = false;
            } else {
                SRLogUtils.e(TAG, "<abandonAudioFocus()> abandon audio focus faild");
            }
        }
    }
}
