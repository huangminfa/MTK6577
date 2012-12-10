/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.FMRadio;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.text.SimpleDateFormat;
import android.text.format.DateFormat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ContentValues;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.Log;
import android.provider.MediaStore;

public class FMRecorder implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaRecorder.OnErrorListener, MediaScannerConnection.MediaScannerConnectionClient {
    private static final String LOGTAG = "FMRecorder";
    private static final String RECORDING_FILE_PREFIX = "FM";
    public static final String RECORDING_FILE_EXTENSION = ".3gpp";

    public static final int ERROR_SDCARD_NOT_PRESENT = 0;
    public static final int ERROR_SDCARD_INSUFFICIENT_SPACE = 1;
    public static final int ERROR_SDCARD_WRITE_FAILED = 2;
    public static final int ERROR_RECORDER_INTERNAL = 3;
    public static final int ERROR_PLAYER_INTERNAL = 4;
    public static final int STATE_IDLE = 5;
    public static final int STATE_RECORDING = 6;
    public static final int STATE_PLAYBACK = 7;
    public static final int STATE_INVALID = -1;
    
    public static final long LOW_SPACE_THRESHOLD = 512 * 1024;
    
    private boolean mDeleteUponSDInsertion = false;
    
    public int mInternalState = STATE_IDLE;
    private int mSDCardID = 0;
    private long mRecordTime = 0;
    //private long mOldRecordTime = 0;
    private long mRecordStartTime = 0;
    private String mOldFilePath = null;
    private File mRecordFile = null;
    private boolean mIsRecordingFileSaved = false;
    private onRecorderStateChangedListener mStateListener = null;
    private boolean mIsPlayCompleted = false;
    private MediaPlayer mPlayer = null;
    private MediaRecorder mRecorder = null;
    // Scan the recording file for updating duration before adding to music playlist
    private MediaScannerConnection mConnection = null;
    
    public void startRecording() {
        FMRadioLogUtils.d(LOGTAG, ">> startRecording");
        mRecordTime = 0;
        //mRecordStartTime = System.currentTimeMillis();
        // Prepare recording file
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // external storage is NOT ready
            FMRadioLogUtils.e(LOGTAG, "SD card is not ready!!");
            setError(ERROR_SDCARD_NOT_PRESENT);
            return;
        } else {
            String sSDPath = Environment.getExternalStorageDirectory().getPath();
            mSDCardID = FileUtils.getFatVolumeId(sSDPath);
            StatFs fs = new StatFs(sSDPath);
            long blocks = fs.getAvailableBlocks();
            long blockSize = fs.getBlockSize();
            FMRadioLogUtils.d(LOGTAG, "SD card free blocks=" + blocks + ", blocksize=" + blockSize);
            if ((blocks * blockSize) <= LOW_SPACE_THRESHOLD) {
                setError(ERROR_SDCARD_INSUFFICIENT_SPACE);
                FMRadioLogUtils.e(LOGTAG, "SD card does not have sufficient space!!");
                return;
            }
            FMRadioLogUtils.d(LOGTAG, "current card id=" + mSDCardID);
        }

        File sdDir = Environment.getExternalStorageDirectory();
        FMRadioLogUtils.d(LOGTAG, "external storage dir = " + sdDir.getAbsolutePath());
        try {
            File recordingDir = new File(sdDir, "FM Recording");
            if (!recordingDir.exists()) {
                boolean mkdirResult = recordingDir.mkdir();
                if (!mkdirResult) {
                    setError(ERROR_SDCARD_WRITE_FAILED);
                    return;
                }
            } else if (recordingDir.exists() && !recordingDir.isDirectory()) {
                FMRadioLogUtils.e(LOGTAG, "A FILE with name \"FM Recording\" already exists!!");
                setError(ERROR_SDCARD_WRITE_FAILED);
                return;
            }
            long curTime = System.currentTimeMillis();
            Date date=new Date(curTime);
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd_HHmmss");
            String time=simpleDateFormat.format(date);
            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append(RECORDING_FILE_PREFIX).append("_").append(time).append(RECORDING_FILE_EXTENSION);
            String name=stringBuilder.toString();
            mRecordFile=new File(recordingDir,name);
            if (false == mRecordFile.createNewFile()) {
                FMRadioLogUtils.e(LOGTAG, "Can not createNewFile");
            }
            //mRecordFile = File.createTempFile(RECORDING_FILE_PREFIX, RECORDING_FILE_EXTENSION, recordingDir);
        } catch (IOException ioex) {
            FMRadioLogUtils.e(LOGTAG, "IOException while createTempFile: " + ioex);
            ioex.printStackTrace();
            setError(ERROR_SDCARD_WRITE_FAILED);
            return;
        }

        try {
            FMRadioLogUtils.d(LOGTAG, "new record file is:" + mRecordFile.getName());

            mRecorder = new MediaRecorder();
            FMRadioLogUtils.d(LOGTAG, "startRecording: create new media record instance");
            mRecorder.setOnErrorListener(this);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.FM);                    // Change this to FM later
            FMRadioLogUtils.d(LOGTAG, "startRecording: setAudioSource");
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);    // make this variable
            FMRadioLogUtils.d(LOGTAG, "startRecording: setOutputFormat");
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);        // make this variable
            //setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            FMRadioLogUtils.d(LOGTAG, "startRecording: setAudioEncoder");
            mRecorder.setAudioSamplingRate(8000);
            //setAudioSamplingRate(48000);
            //setAudioSamplingRate(16000);
            FMRadioLogUtils.d(LOGTAG, "startRecording: setAudioSamplingRate");
            mRecorder.setAudioEncodingBitRate(12200);
            //setAudioEncodingBitRate(128000);
            //setAudioEncodingBitRate(28500);
            FMRadioLogUtils.d(LOGTAG, "startRecording: setAudioEncodingBitRate");
            mRecorder.setAudioChannels(1);
            //setAudioChannels(2);
            FMRadioLogUtils.d(LOGTAG, "startRecording: setAudioChannels");
            
            mRecorder.setOutputFile(mRecordFile.getAbsolutePath());
            FMRadioLogUtils.d(LOGTAG, "startRecording: setOutputFile");
            
            mRecorder.prepare();
            FMRadioLogUtils.d(LOGTAG, "startRecording: prepare");
            mRecordStartTime = SystemClock.elapsedRealtime();
            mRecorder.start();
            mIsRecordingFileSaved = false;
            FMRadioLogUtils.d(LOGTAG, "startRecording: start");
        } catch (IllegalStateException e) {
            FMRadioLogUtils.e(LOGTAG, "IllegalStateException while starting recording!");
            setError(ERROR_RECORDER_INTERNAL);
            return;
        } catch (IOException e) {
            FMRadioLogUtils.e(LOGTAG, "IOException while starting recording!");
            setError(ERROR_RECORDER_INTERNAL);
            return;
        }
        setState(STATE_RECORDING);
        FMRadioLogUtils.d(LOGTAG, "<< startRecording");
    }    // startRecording
    
    public void stopRecording() {
        FMRadioLogUtils.d(LOGTAG, ">> stopRecording");
        if (mInternalState != STATE_RECORDING) {
            FMRadioLogUtils.w(LOGTAG, "stopRecording() called in wrong state!!");
            return;
        }

        mRecordTime = SystemClock.elapsedRealtime() - mRecordStartTime;
        try {
            if (mRecorder != null) {
                mRecorder.stop();
                FMRadioLogUtils.d(LOGTAG, "stopRecording: stop");
                mRecorder.release();
                FMRadioLogUtils.d(LOGTAG, "stopRecording: release");
                mRecorder = null;
            }
        } catch (Exception ex) {
            FMRadioLogUtils.e(LOGTAG, "Exception while reset()" + ex);
        }
        setState(STATE_IDLE);
        FMRadioLogUtils.d(LOGTAG, "<< stopRecording");
    }
    
    public void startPlayback() {
        FMRadioLogUtils.d(LOGTAG, ">> startPlayback");
        if (mRecordFile == null) {
            FMRadioLogUtils.e(LOGTAG, "no file to playback!");
            return;
        }

        mPlayer = new MediaPlayer();
        try {
            mPlayer.reset();
            mPlayer.setDataSource(mRecordFile.getAbsolutePath());
            FMRadioLogUtils.d(LOGTAG, "MediaPlayer.setDataSource(" + mRecordFile.getAbsolutePath() + ")");
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.prepare();
            FMRadioLogUtils.d(LOGTAG, "MediaPlayer.prepare()");
            mPlayer.start();
        } catch (Exception ex) {
            FMRadioLogUtils.e(LOGTAG, "Exception while trying to playback recording file: " + ex);
            setError(ERROR_PLAYER_INTERNAL);
            return;
        }
        setState(STATE_PLAYBACK);
        FMRadioLogUtils.d(LOGTAG, "<< startPlayback");
    }
    
    public void stopPlayback() {
        FMRadioLogUtils.d(LOGTAG, ">> stopPlayback");
        if (mPlayer == null || mInternalState != STATE_PLAYBACK || (mPlayer != null && !mPlayer.isPlaying())) {
            FMRadioLogUtils.w(LOGTAG, "stopPlayback called in wrong state!!");
            return;
        }
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        setState(STATE_IDLE);
        FMRadioLogUtils.d(LOGTAG, "<< stopPlayback");
    }
    
    private void setError(int error) {
        FMRadioLogUtils.e(LOGTAG, "setError: " + error);
        if (mStateListener != null) {
            mStateListener.onRecorderError(error);
        }
    }
    
    private void setState(int state) {
        mInternalState = state;
        if (mStateListener != null) {
            mStateListener.onRecorderStateChanged(state);
        }
    }

    
    public long recordTime() {
        if (mInternalState == STATE_RECORDING) {
            mRecordTime = SystemClock.elapsedRealtime() - mRecordStartTime;
        }
        return mRecordTime;
    }
    
    public int getState() {
        return mInternalState;
    }
    
/*    public int getDuration() {
        if (mPlayer != null) {
            return mPlayer.getDuration();
        }
        return -1;
    }
*/    
    public int getPosition() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            return mPlayer.getCurrentPosition();
        }
        return -1;
    }
    
    public String getRecordingName() {
        if (mRecordFile != null) {
            String fileName = mRecordFile.getName();
            if (fileName.toLowerCase().endsWith(RECORDING_FILE_EXTENSION) 
                    && fileName.length() > RECORDING_FILE_EXTENSION.length()) {
                // remove the extension sub string first
                fileName = fileName.substring(0, fileName.length() - 5);
            }
            return fileName;
        } else {
            return null;
        }
    }
    
    public void saveRecording(Context context, String newName) {
        FMRadioLogUtils.d(LOGTAG, ">> saveRecording(" + newName + ")");
        File parentFile = null;
        if (null != mRecordFile) {
            parentFile = mRecordFile.getParentFile();
        }
        if ((null == mRecordFile) || (null == parentFile)){
            FMRadioLogUtils.e(LOGTAG, "<< saveRecording: recording file is null!");
            return;
        }
        if (newName != null && !newName.equals(getRecordingName())) {
            File sdFile = new File(parentFile.getPath(), newName + RECORDING_FILE_EXTENSION);
            if (sdFile.exists()) {
                FMRadioLogUtils.w(LOGTAG, "A file with the same new name will be deleted: " + sdFile.getAbsolutePath());
                if (false == sdFile.delete()) {
                    FMRadioLogUtils.e(LOGTAG, "can't delete the file already exits");
                }
            }
            if (null != parentFile) {
                if (false == mRecordFile.renameTo(new File(parentFile.getPath(), newName + RECORDING_FILE_EXTENSION))) {
                    FMRadioLogUtils.e(LOGTAG, "can't rename file");
                }
                mRecordFile = new File(parentFile.getPath(), newName + RECORDING_FILE_EXTENSION);
            }
        }
        mIsRecordingFileSaved = true;
        mConnection = new MediaScannerConnection(context, this);
        addCurrentRecordingToMediaDB(context);
        FMRadioLogUtils.d(LOGTAG, "<< saveRecording(" + newName + ")");
    }
    
    public void discardRecording() {
        FMRadioLogUtils.d(LOGTAG, ">> discardRecording");
        if ((mInternalState == STATE_RECORDING) && (mRecorder != null)) {
            mRecorder.stop();
            FMRadioLogUtils.d(LOGTAG, "discardRecording: stop");
            mRecorder.release();
            FMRadioLogUtils.d(LOGTAG, "discardRecording: release");
            mRecorder = null;
        } else if ((mInternalState == STATE_PLAYBACK) && (mPlayer != null)) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        boolean bRes = false;
        if (mRecordFile != null && (!mIsRecordingFileSaved)) {
            bRes = mRecordFile.delete();
            if (!bRes && (mRecordFile != null)) {
                // deletion failed, possibly due to hot plug out SD card
                // delete again the next time SD card is inserted again.
                FMRadioLogUtils.d(LOGTAG, "discardRecording: deletion failed! will try deleting it again when card is inserted back");
                mOldFilePath = mRecordFile.getAbsolutePath();
                mDeleteUponSDInsertion = true;
            }
            mRecordFile = null;
            mRecordStartTime = 0;
            mRecordTime = 0;
        }
        setState(STATE_IDLE);
        FMRadioLogUtils.d(LOGTAG, "<< discardRecording");
    }

    public void registerRecorderStateListener(onRecorderStateChangedListener listener) {
        mStateListener = listener;
    }
    
    public interface onRecorderStateChangedListener {
        public void onRecorderStateChanged(int state);
        public void onRecorderError(int error);
        public void onPlayRecordFileComplete();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        FMRadioLogUtils.d(LOGTAG, ">> MediaPlayer.onCompletion");
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        setPlayCompleted(true);
        setState(STATE_IDLE);
        if (mStateListener != null) {
            mStateListener.onPlayRecordFileComplete();
        }
        FMRadioLogUtils.d(LOGTAG, "<< MediaPlayer.onCompletion");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        FMRadioLogUtils.e(LOGTAG, "MediaPlayer.onError: what=" + what + ", extra=" + extra);
        setError(ERROR_PLAYER_INTERNAL);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        if (mInternalState == STATE_PLAYBACK)
            setState(STATE_IDLE);
        return true;
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        FMRadioLogUtils.e(LOGTAG, "MediaRecorder.onError: what=" + what + ", extra=" + extra);
        setError(ERROR_RECORDER_INTERNAL);
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;        
        }
        if (mInternalState == STATE_RECORDING) {
            setState(STATE_IDLE);
        }
    }
    
    public void onSDRemoved() {
        FMRadioLogUtils.d(LOGTAG, ">> onSDRemoved");
        //mOldRecordTime = recordTime();
        mRecordTime = 0;
        FMRadioLogUtils.d(LOGTAG, "<< onSDRemoved");
    }
    
    public void onSDInserted() {
        FMRadioLogUtils.d(LOGTAG, ">> onSDInserted");
        int newCardID = FileUtils.getFatVolumeId(Environment.getExternalStorageDirectory().getPath());
        FMRadioLogUtils.d(LOGTAG, "new card id=" + newCardID);
        if (mSDCardID != newCardID) {
            // SD card has changed...
            FMRadioLogUtils.d(LOGTAG, "onSDInserted: card has been changed!!");
            mSDCardID = newCardID;
        } else if (mDeleteUponSDInsertion) {
            // Previous deletion has failed
            // and card is the same one
            mDeleteUponSDInsertion = false;
            //FMRadioLogUtils.d(LOGTAG, "onSDInserted: trying to delete previous unfinished recording file...");
            File oldFile = new File(mOldFilePath);
            boolean isDeleted = oldFile.delete();
            FMRadioLogUtils.d(LOGTAG, "onSDInserted: delete old recording file=" + isDeleted);
            mRecordFile = null;
            mOldFilePath = null;
        } else {
            // Try re-opening previous recording file
//            if (mRecordFile != null) {
//                if (mRecordFile.exists()) {
//                    FMRadioLogUtils.d(LOGTAG, "onSDInserted: old file still exists!");
//                    mRecordTime = mOldRecordTime;
//                    setState(STATE_IDLE);
//                } else {
//                    FMRadioLogUtils.d(LOGTAG, "onSDInserted: old file is GONE!");
//                }
//            }
        }
        FMRadioLogUtils.d(LOGTAG, "<< onSDInserted");
    }

    @Override
    public void onMediaScannerConnected() {
        mConnection.scanFile(mRecordFile.getAbsolutePath(), null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mConnection.disconnect();
        FMRadioLogUtils.d(LOGTAG, "onScanCompleted: path = " + path + " ,uri = " + uri);
    }
    
    private void addCurrentRecordingToMediaDB(final Context context) {
        FMRadioLogUtils.v(LOGTAG, ">> addCurrentRecordingToMediaDB");
        if (mRecordFile == null || !mRecordFile.exists()) {
            FMRadioLogUtils.e(LOGTAG, "<< addCurrentRecordingToMediaDB: file does not exists");
            return;
        }
        new Thread() {
            public void run() {
                long curTime = System.currentTimeMillis();
                long modDate = mRecordFile.lastModified();
                Date date = new Date(curTime);
                java.text.DateFormat dateFormatter = DateFormat.getDateFormat(context);
                java.text.DateFormat timeFormatter = DateFormat.getTimeFormat(context);
                String title = getRecordingName();
                String artist = "FM Recording " + dateFormatter.format(date) + " " + timeFormatter.format(date);
                FMRadioLogUtils.d(LOGTAG, "date formatter=" + dateFormatter.format(date) + ", time formatter=" + timeFormatter.format(date));
                
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Audio.Media.IS_MUSIC, 0);
                cv.put(MediaStore.Audio.Media.TITLE, title);
                cv.put(MediaStore.Audio.Media.DATA, mRecordFile.getAbsolutePath());
                cv.put(MediaStore.Audio.Media.DATE_ADDED, (int) (curTime / 1000));
                cv.put(MediaStore.Audio.Media.DATE_MODIFIED, (int) (modDate / 1000));
                cv.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
                cv.put(MediaStore.Audio.Media.ARTIST, artist);
                cv.put(MediaStore.Audio.Media.ALBUM, "FM Recordings");
                cv.put(MediaStore.Audio.Media.DURATION, mRecordTime);
                
                Uri insertResult = null;
                boolean bItemExistsInDB = false;
                int recordingId = -1;
                // If there's already a file with the same name in DB,
                // instead of deleting -> inserting item,
                // update the item.
                Cursor cExistingItem = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] {"_id"},
                    MediaStore.Audio.Media.DATA + "=?",
                    new String[] {mRecordFile.getAbsolutePath()},
                    null);
                if (cExistingItem != null) {
                    // already an item with the same path(DATA) in DB
                    try {
                        if (cExistingItem.getCount() > 0) {
                            bItemExistsInDB = true;
                            cExistingItem.moveToFirst();
                            recordingId = cExistingItem.getInt(0);
                            FMRadioLogUtils.d(LOGTAG, "existing id=" + recordingId);
                            int rowCnt = context.getContentResolver().update(
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    cv,
                                    MediaStore.Audio.Media.DATA + "=?",
                                    new String[] {mRecordFile.getAbsolutePath()});
                            FMRadioLogUtils.d(LOGTAG, "existing items update count=" + rowCnt);
                        } else {
                            FMRadioLogUtils.d(LOGTAG, "addCurrentRecordingToMediaDB: -> insert data");
                            insertResult = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cv);
                            FMRadioLogUtils.d(LOGTAG, "addCurrentRecordingToMediaDB: <- insert data");
                            if (null != insertResult) {
                                recordingId = Integer.valueOf(insertResult.getLastPathSegment());
                            }
                        }
                    }
                    finally {
                        cExistingItem.close();
                        cExistingItem = null;
                    }
                }
                
                if (!bItemExistsInDB && insertResult == null) {
                    // no similar item in DB and insertion failed
                    FMRadioLogUtils.e(LOGTAG, "insert DB failed!!");
                } else {
                    FMRadioLogUtils.d(LOGTAG, "addCurrentRecordingToMediaDB: -> query playlist");
                    Cursor playlistCursor = context.getContentResolver().query(MediaStore.Audio.Playlists.getContentUri("external")
                            , new String[] {MediaStore.Audio.Playlists._ID}
                            , MediaStore.Audio.Playlists.NAME + "=?"
                            , new String[] {"FM Recordings"}
                            , null);
                    FMRadioLogUtils.d(LOGTAG, "addCurrentRecordingToMediaDB: <- query playlist");
                    
                    // make sure playlist exists in DB, creating it if necessary
                    int playlistId = -1;
                    if (playlistCursor != null) {
                        int listCount = playlistCursor.getCount();
                        if (listCount > 0) {
                            playlistCursor.moveToFirst();
                            playlistId = playlistCursor.getInt(0);
                        }
                        playlistCursor.close();
                    }
                    
                    if (playlistId == -1) {
                        // playlist is not found in media DB
                        cv = new ContentValues();
                        cv.put(MediaStore.Audio.Playlists.NAME, "FM Recordings");
                        FMRadioLogUtils.d(LOGTAG, "addCurrentRecordingToMediaDB: -> insert playlist");
                        Uri newPlaylistUri = context.getContentResolver().insert(MediaStore.Audio.Playlists.getContentUri("external"),cv);
                        FMRadioLogUtils.d(LOGTAG, "addCurrentRecordingToMediaDB: <- insert playlist");
                        playlistId = Integer.valueOf(newPlaylistUri.getLastPathSegment());
                    }
                    
                    Uri membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
                    if (bItemExistsInDB) {
                        // Item exists in DB, update instead of insert
                        // First see if this item is in the FM recording playlist
                        Cursor cItemInMembers = context.getContentResolver().query(membersUri,
                                new String[] {MediaStore.Audio.Playlists.Members.AUDIO_ID},
                                MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?",
                                new String[] {String.valueOf(recordingId)},
                                null); 
                        if (cItemInMembers != null) {
                            try {
                                if (cItemInMembers.getCount() > 0) {
                                    // Item already in playlist member, 
                                    // no further actions (modify play_order is difficult, 
                                    // since a same audio_id can appear multiple times in one playlist 
                                    // with different play_order)
                                    FMRadioLogUtils.d(LOGTAG, "new item already in playlists.members table");
                                    FMRadioLogUtils.d(LOGTAG, "<< addCurrentRecordingToMediaDB");
                                    return;
                                }
                            }
                            finally {
                                cItemInMembers.close();
                                cItemInMembers = null;
                            }
                        }
                    }
                    
                    
                    FMRadioLogUtils.d(LOGTAG, "addCurrentRecordingToMediaDB: -> query members");
                    Cursor membersCursor = context.getContentResolver().query(
                            membersUri,
                            new String[] {"count(*)"},
                            null,
                            null,
                            null);
                    FMRadioLogUtils.d(LOGTAG, "addCurrentRecordingToMediaDB: <- query members");
                    if (membersCursor != null) {
                        int membersCount = membersCursor.getCount();
                        try {
                            if (membersCount > 0) {
                                membersCursor.moveToFirst();
                                int base = membersCursor.getInt(0);
                                FMRadioLogUtils.w(LOGTAG, "members base=" + base);
                                cv = new ContentValues();
                                cv.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(base));
                                cv.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, recordingId);
                                FMRadioLogUtils.d(LOGTAG, "addCurrentRecordingToMediaDB: -> insert to members");
                                context.getContentResolver().insert(membersUri, cv);
                                FMRadioLogUtils.d(LOGTAG, "addCurrentRecordingToMediaDB: <- insert to members");
                            }
                        }
                        finally {
                            membersCursor.close();
                            membersCursor = null;
                        }
                    }

                    // Notify those applications such as Music listening to the
                    // scanner events that a recorded audio file just created.
                    // Connect to mediascanner to update duration
                    mConnection.connect();
                    }
            }
        }.start();
        
        FMRadioLogUtils.v(LOGTAG, "<< addCurrentRecordingToMediaDB");
    }
    
    public void resetRecorder() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        mRecordFile = null;
        mRecordStartTime = 0;
        mRecordTime = 0;
        mInternalState = STATE_IDLE;
    }
    
    public void setPlayCompleted(boolean isPlayCompleted) {
        mIsPlayCompleted = isPlayCompleted;
    }
    
    public boolean getPlayCompleted() {
        return mIsPlayCompleted;
    }
}
