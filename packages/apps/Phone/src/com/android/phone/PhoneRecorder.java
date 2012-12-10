//New file Added by delong.liu@archermind.com
package com.android.phone;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.StatFs;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class PhoneRecorder extends Recorder implements MediaScannerConnection.MediaScannerConnectionClient {

	private final static String TAG = "PhoneRecorder";
	private final static String AUDIO_DB_TITLE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static String AUDIO_DB_PLAYLIST_NAME;// = "My recordings";

	private static final String AUDIO_3GPP = "audio/3gpp";
	private static final String AUDIO_AMR = "audio/amr";
	private static final String AUDIO_ANY = "audio/*";

    public static final long LOW_STORAGE_THRESHOLD = 512L * 1024L;

	// Added by tianxiang.qin@archermind.com
	private static PhoneRecorder  mPhoneRecorder = null;
	private static byte[] lock = new byte[0];
	//private boolean mFlagRecord = false;
	private static boolean mFlagRecord = false;
	//private boolean mSDcardFullFlag = false;
	private static boolean mSDcardFullFlag = false;
	// Added by tianxiang.qin@archermind.com end

	boolean mSampleInterrupted = false;
	String mRequestedType = AUDIO_ANY;
	long mMaxFileSize = -1;

	private Context mContext = null;
	private AlertDialog mAlertDialog;
	
	private MediaScannerConnection mConnection;

	private PhoneRecorder(Context context){
		 super();
		 mContext = context;
         AUDIO_DB_PLAYLIST_NAME = mContext.getString(R.string.str_db_playlist_name);
         mConnection = new MediaScannerConnection(context, this);
	}

	public void setMContext(Context context) {
		mContext = context;
	}

	public static PhoneRecorder getInstance(Context context) {
		synchronized (lock) {
			if(mPhoneRecorder == null) {
				mPhoneRecorder = new PhoneRecorder(context);
			}
			return mPhoneRecorder;
		}
	}

	public boolean ismFlagRecord() {
		return mFlagRecord;
	}

	public void setmFlagRecord(boolean flag) {
		mFlagRecord = flag;
	}

	public static boolean isRecording(){
		return mFlagRecord;
	}

	public void startRecord() {
        log("startRecord, mRequestedType = "+mRequestedType);
        if(mFlagRecord) {
			return;
		}
		if (!PhoneUtils.isExternalStorageMounted()) {
			mSampleInterrupted = true;
			Log.e(TAG, "-----Please insert an SD card----");
		} else if (!PhoneUtils.diskSpaceAvailable(Constants.PHONE_RECORD_LOW_STORAGE_THRESHOLD)) {
			mSampleInterrupted = true;
			Log.e(TAG, "--------Storage is full-------");
		} else {
			try {
				if (AUDIO_AMR.equals(mRequestedType)) {
					startRecording(MediaRecorder.OutputFormat.RAW_AMR, ".amr");
				} else if (AUDIO_3GPP.equals(mRequestedType)) {
					startRecording(MediaRecorder.OutputFormat.THREE_GPP,
							".3gpp");

				} else if (AUDIO_ANY.equals(mRequestedType)) {
					// mRequestedType = AUDIO_3GPP;
					startRecording(MediaRecorder.OutputFormat.THREE_GPP,
							".3gpp");
				} else {
					throw new IllegalArgumentException(
							"Invalid output file type requested");
				}
				mPhoneRecorder.setmFlagRecord(true); //start successfully.qintx
			} catch(Exception oe) {
				mPhoneRecorder.setmFlagRecord(false);
			}
		}
	}

	public void stop() {
		if(!mFlagRecord) {
			return;
		}
		super.stop();
		mFlagRecord = false;
	}

	public void stopRecord(boolean mount){
		if(!mFlagRecord) {
			return;
		}
        log("stopRecord");
        stop();

		PhoneApp app = PhoneApp.getInstance();
	    if (mount) {
            saveSample();
            if (mSDcardFullFlag) {
                Toast.makeText(mContext, mContext.getResources().
                        getText(R.string.confirm_device_info_full), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, mContext.getResources().
                        getText(R.string.confirm_save_info_saved), Toast.LENGTH_LONG).show();
            }
		} else {
			delete();
			Toast.makeText(mContext, mContext.getResources().
			        getText(R.string.ext_media_badremoval_notification_title),Toast.LENGTH_LONG).show();
		}
	}

    public static boolean sdcardFullFlag() {
        mSDcardFullFlag = !PhoneUtils.diskSpaceAvailable(Constants.PHONE_RECORD_LOW_STORAGE_THRESHOLD);
        return mSDcardFullFlag;
    }

	/**
	 * If we have just recorded a smaple, this adds it to the media data base
	 * and sets the result to the sample's URI.
	 */
	public boolean saveSample() {
		if (this.sampleLength() == 0l) {
			return false;
		}
		Uri uri = null;
		try {
			uri = this.addToMediaDB(this.sampleFile());
		} catch (UnsupportedOperationException ex) {
			//Database manipulation failure
			return false;
		}
		if (uri == null) {
			return false;
		}

		return true;
	}

	/*
	 * Adds file and returns content uri.
	 */
	private Uri addToMediaDB(File file) {
		ContentValues cv = new ContentValues();
		long current = System.currentTimeMillis();
		long modDate = file.lastModified();
		Date date = new Date(current);
		
        String sTime = DateFormat.getTimeFormat(mContext).format(date);
        String sDate = DateFormat.getDateFormat(mContext).format(date);
        String title = sDate + " " + sTime;

		// Lets label the recorded audio file as NON-MUSIC so that the file
		// won't be displayed automatically, except for in the playlist.
        
        // Currently if scan media, all db information will be cleared
        // so no need to put all information except MediaStore.Audio.Media.DATA
        
		//cv.put(MediaStore.Audio.Media.IS_MUSIC, "0");
		//cv.put(MediaStore.Audio.Media.TITLE, title);
		cv.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());
		//cv.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
		//cv.put(MediaStore.Audio.Media.MIME_TYPE, mRequestedType);

		//cv.put(MediaStore.Audio.Media.ARTIST, mContext.getString(R.string.your_recordings));
		//cv.put(MediaStore.Audio.Media.ALBUM, mContext.getString(R.string.audio_recordings));
		//cv.put(MediaStore.Audio.Media.DURATION, this.sampleLength());
		//Log.d(TAG, "Inserting audio record: " + cv.toString());
		ContentResolver resolver = mContext.getContentResolver();
		Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		//Log.d(TAG, "ContentURI: " + base);
		Uri result = resolver.insert(base, cv);

		if (result == null) {
			Log.e(TAG, "----- Unable to save recorded audio !!");
			return null;
		}

		if (getPlaylistId() == -1) {
			createPlaylist(resolver);
		}
		int audioId = Integer.valueOf(result.getLastPathSegment());
		addToPlaylist(resolver, audioId, getPlaylistId());

		// Notify those applications such as Music listening to the
		// scanner events that a recorded audio file just created.
		mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
		if(null != mConnection)
			mConnection.connect();
		return result;
	}

	/*
	 * Obtain the id for the default play list from the audio_playlists table.
	 */
	private int getPlaylistId() {
		Uri uri = MediaStore.Audio.Playlists.getContentUri("external");
		final String[] ids = new String[] { MediaStore.Audio.Playlists._ID };
		final String where = MediaStore.Audio.Playlists.NAME + "=?";
        AUDIO_DB_PLAYLIST_NAME = mContext.getString(R.string.str_db_playlist_name);
		final String[] args = new String[] { AUDIO_DB_PLAYLIST_NAME };
		Cursor cursor = query(uri, ids, where, args, null);
		if (cursor == null) {
			Log.v(TAG, "query returns null");
		}
		int id = -1;
		if (cursor != null) {
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				id = cursor.getInt(0);
			}
            cursor.close();
        }
        return id;
    }

	/*
	 * Create a playlist with the given default playlist name, if no such
	 * playlist exists.
	 */
	private Uri createPlaylist(ContentResolver resolver) {
		ContentValues cv = new ContentValues();
        AUDIO_DB_PLAYLIST_NAME = mContext.getString(R.string.str_db_playlist_name);		
		cv.put(MediaStore.Audio.Playlists.NAME, AUDIO_DB_PLAYLIST_NAME);
		Uri uri = resolver.insert(MediaStore.Audio.Playlists
				.getContentUri("external"), cv);
		if (uri == null) {
			Log.e(TAG, "---- Unable to save recorded audio -----");
		}
		return uri;
	}

	/*
     * Add the given audioId to the playlist with the given playlistId; and maintain the
     * play_order in the playlist.
     */
    private void addToPlaylist(ContentResolver resolver, int audioId, long playlistId) {
        String[] cols = new String[] {  "count(*)"  };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        if (null != cur)
        {
            cur.moveToFirst();
            final int base = cur.getInt(0);
            cur.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(base + audioId));
            values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
            resolver.insert(uri, values);
        }
    }

	/*
     * A simple utility to do a query into the databases.
     */
    private Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            ContentResolver resolver = mContext.getContentResolver();
            if (resolver == null) {
                return null;
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
         } catch (UnsupportedOperationException ex) {
            return null;
        }
    }

	public void onError(MediaRecorder mp, int what, int extra) {
		Log.e(TAG, "MediaRecorder error...");
		/*
		Intent mRecorderServiceIntent = new Intent(mContext,PhoneRecorderServices.class);
		mContext.stopService(mRecorderServiceIntent);
		mSDacrdFullFlag=true;
		*/
	}
	
    public void onMediaScannerConnected() {
    	if(null != mConnection){
    		File file = this.sampleFile();
    		if(null != file)
    			mConnection.scanFile(file.getAbsolutePath(), null);
    	}
    }
    
    public void onScanCompleted(String path, Uri uri) {
    	if(null != mConnection)
    		mConnection.disconnect();
    }
	
	public void log(String msg) {
        Log.d(TAG, msg);
	}
}


