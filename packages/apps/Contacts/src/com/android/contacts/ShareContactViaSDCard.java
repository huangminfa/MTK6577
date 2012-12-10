package com.android.contacts;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.ServiceManager;
import android.content.Intent;
import android.text.TextUtils;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import android.database.Cursor;
import android.content.ContentResolver;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.app.AlertDialog;
import android.os.Environment;
import java.io.File;

import com.android.contacts.R;
import com.android.contacts.vcard.ExportVCardActivity;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.StatFs;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;

public class ShareContactViaSDCard extends Activity {
	
	private static final String TAG = "ShareContactViaSDCard";
	private String mAction;
	private Uri dataUri;
	private int singleContactId = -1;
	String lookUpUris;
	Intent intent;
	private ProgressDialog mProgressDialog;
	private SearchContactThread mSearchContactThread;
	
	boolean sdIsVisible = true;
	
	static final String[] CONTACTS_PROJECTION = new String[] { Contacts._ID, // 0
		Contacts.DISPLAY_NAME_PRIMARY, // 1
		Contacts.DISPLAY_NAME_ALTERNATIVE, // 2
		Contacts.SORT_KEY_PRIMARY, // 3
		Contacts.DISPLAY_NAME, // 4
    };
	
	static final int PHONE_ID_COLUMN_INDEX = 0;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        intent = getIntent();
        mAction = intent.getAction();
        String contactId = intent.getStringExtra("contactId");
        String userProfile = intent.getStringExtra("userProfile");
        if (userProfile != null && "true".equals(userProfile)) {
        	Toast.makeText(this.getApplicationContext(), getString(R.string.user_profile_cannot_sd_card), Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
        
        if (contactId != null && !"".equals(contactId)) {
            singleContactId = Integer.parseInt(contactId);
        }
        
        Log.i(TAG,"mAction is " + mAction);
        if (!checkSDCardAvaliable()) {
            AlertDialog alert = new AlertDialog.Builder(this).create();
            alert.setCanceledOnTouchOutside(true);
            alert.setMessage(this.getResources().getText(R.string.no_sdcard_message));
            alert.setTitle(R.string.no_sdcard_title);
            alert.setIcon(com.android.internal.R.drawable.ic_dialog_alert_holo_light);
            alert.setButton(this.getResources().getText(android.R.string.ok), mCancelListener);
            alert.setOnDismissListener(new OnDismissListener() {
                
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
            alert.show();
            
            sdIsVisible = false;
        }
        
        if (checkSDCardAvaliable() && isSDCardFull()) {
            AlertDialog alert = new AlertDialog.Builder(this).create();
            alert.setCanceledOnTouchOutside(true);
            alert.setMessage(this.getResources().getText(R.string.storage_full));
            alert.setTitle(R.string.storage_full);
            alert.setIcon(com.android.internal.R.drawable.ic_dialog_alert_holo_light);
            alert.setButton(this.getResources().getText(android.R.string.ok), mCancelListener);
            alert.setOnDismissListener(new OnDismissListener() {
                
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
            alert.show();
            sdIsVisible = false;
        }
        lookUpUris = intent.getStringExtra("LOOKUPURIS");
        if ((lookUpUris == null || "".equals(lookUpUris)) && singleContactId == -1) {
    		Toast.makeText(this.getApplicationContext(), getString(R.string.file_already_on_sd_card), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
		}
        
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		if (sdIsVisible) {
            if (Intent.ACTION_SEND.equals(mAction) && intent.hasExtra(Intent.EXTRA_STREAM)) {
            	mSearchContactThread = new SearchContactThread();
    			showProgressDialog();
            }
        }
	}
    
    private void showProgressDialog() {
		if (mProgressDialog == null) {
            String title = getString(R.string.please_wait);
            String message = getString(R.string.please_wait);
            mProgressDialog = ProgressDialog.show(this, title, message, true, false);
            mProgressDialog.setOnCancelListener(mSearchContactThread);
            mSearchContactThread.start();
        }
	}
    
	public void shareViaSDCard(String lookUpUris) {
		StringBuilder contactsID = new StringBuilder();
		int curIndex = 0;
		Cursor cursor = null;
		String id = null;
		if (singleContactId == -1) {
		    String[] tempUris = lookUpUris.split(":");
            StringBuilder selection = new StringBuilder(Contacts.LOOKUP_KEY + " in (");
            int index = 0;
            for (int i = 0; i < tempUris.length; i++) {
                selection.append("'" + tempUris[i] + "'");
                if (index != tempUris.length-1) {
                    selection.append(",");
                }
                index++;
            }
            selection.append(")");
            
			cursor = getContentResolver().query(/*dataUri*/Contacts.CONTENT_URI, CONTACTS_PROJECTION, selection.toString(), null, null);
			Log.i(TAG,"cursor is " + cursor);
			if (null != cursor) {
				while (cursor.moveToNext()) {				
					if (cursor != null) id = cursor.getString(PHONE_ID_COLUMN_INDEX);
					if (curIndex++ != 0) {
						contactsID.append("," + id);
					} else {
						contactsID.append(id);
					}
				}
				cursor.close();
			}
		} else {			
			id = Integer.toString(singleContactId);
			contactsID.append(id);
		}
		
		String exportselection = Contacts._ID + " IN (" + contactsID.toString() +")";
		
        Intent it = new Intent(this, ExportVCardActivity.class);
        it.putExtra("multi_export_type", 1);
        it.putExtra("exportselection", exportselection);
        this.startActivity(it);
        finish();
        return;
        }

	private boolean checkSDCardAvaliable() {
		return (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED));
	}

	private boolean isSDCardFull() {
	    getExternalStorageDirectory();
        String state = getExternalStorageState(); 
        /*
         * Bug Fix by Mediatek End.
         */
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = getExternalStorageDirectory();
            String path = sdcardDir.getPath();
            if (TextUtils.isEmpty(path)) {
                return false;
            }
            Log.d(TAG, "isSDCardFull storage path is " + path);
            StatFs sf = null;
            try {
                sf = new StatFs(path);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
                return false;
            }

            if (sf == null) {
                Log.e(TAG, "isSDCardFull sf is null ");
                return false;
            }
            long availCount = sf.getAvailableBlocks();
            if (availCount > 0) {
                return false;
            } else {
                return true;
            }
        } 

        return true;
	}
	private static File mFile;
	
	public File getExternalStorageDirectory(){
        StorageManager mSM = (StorageManager) getApplicationContext().getSystemService(STORAGE_SERVICE);
        String path = mSM.getDefaultPath();
        final File file = getDirectory(path, "/mnt/sdcard");
        Log.i(TAG,"[getExternalStorageDirectory]file.path : "+file.getPath());
        mFile = file;
        return file;
    }
	
	public  File getDirectory(String path, String defaultPath) {
        Log.i("getDirectory","path : "+path);
        return path == null ? new File(defaultPath) : new File(path);
    }
	
    public static String getExternalStorageState() {
        try {
            IMountService mountService = IMountService.Stub.asInterface(ServiceManager
                        .getService("mount"));
            Log.i(TAG, "[getExternalStorageState] mFile : " + mFile);
            return mountService.getVolumeState(mFile
                        .toString());
        } catch (Exception rex) {
            return Environment.MEDIA_REMOVED;
        }
    }
	
    private class CancelListener
            implements DialogInterface,DialogInterface.OnClickListener, DialogInterface.OnCancelListener,DialogInterface.OnKeyListener {
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }

        public void onCancel(DialogInterface dialog) {
            finish();
        }

        public void cancel() {
        }

        public void dismiss() {
            finish();
        }

        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            finish();
            return false;
        }
    }

	private CancelListener mCancelListener = new CancelListener();
	
	private class SearchContactThread extends Thread implements OnCancelListener, OnClickListener {
        // To avoid recursive link.
        private class CanceledException extends Exception {
        	
        }

        public SearchContactThread() {
            
        }

        @Override
        public void run() {
        	String type = intent.getType();
            dataUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            Log.i(TAG,"dataUri is " + dataUri);
            Log.i(TAG,"type is " + type);
            if (dataUri != null && type != null) {
                shareViaSDCard(lookUpUris);
            }
        }


        public void onCancel(DialogInterface dialog) {
//            mCanceled = true;
            finish();
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                finish();
            }
        }
    }


}