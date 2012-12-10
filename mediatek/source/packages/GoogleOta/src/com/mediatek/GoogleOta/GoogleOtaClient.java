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

package com.mediatek.GoogleOta;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.GoogleOta.Util.DownloadDescriptor;

public class GoogleOtaClient extends Activity {
    private Button mScreenButton;
    private Button mPositiveButton;
    private Button mNegativeButton;
    private Button mUpdateButton;

    private TextView mScreenText;
    private TextView mDlNewContentTitle;    
    private TextView mDlNewNotesContent;
    private TextView mDlRatio;
    private TextView mDlProgress;

    private ListView mUpdateRadioList;
    private ProgressBar mDlRatioProgressBar;
    private ProgressDialog mUnzipProgressDialog;
    private AlertDialog mDownloadCancelDialog;
    private AlertDialog mDownloadStorageDialog;
    private AlertDialog mAlertDialog;

    private static SessionStateControlThread mQueryNewVersionThread;
    private SessionStateControlThread mDlPkgProgressThread;

    private IGoogleOtaService mService = null;	
    private DownloadStatus mDownloadStatus = null;
    private static GoogleOtaClient mClientInstance = null;
    private static boolean isBg = false;

    private int mQueryType = 0;
    private int mOTADialogTitleResId = 0;
    private int mOTADialogMessageResId = 0;
    private boolean mNeedReset = false;
    
    private static enum MenuStatus {
    	Menu_None,
    	Menu_Download,
    	Menu_Downloading,
    	Menu_Resume,
    	Menu_Finish,
    	Menu_Retry,
    	Menu_Upgrade
    }   ;
    
    private MenuStatus mMenuStatus = MenuStatus.Menu_None;

    
    private static final int NETWORKERROR_SPECIFIED	   = 1;

    private static final int DIALOG_QUERYWAITING 	   = 0;
    private static final int DIALOG_CANCELDOWNLOAD 	   = 1;
    private static final int DIALOG_NOENOUGHSPACE 	   = 2;
    private static final int DIALOG_NETWORKERROR       = 3;
    private static final int DIALOG_SERVERERROR        = 4;
    private static final int DIALOG_NOSDCARD 	       = 5;
    private static final int DIALOG_UNKNOWNERROR       = 6;
    private static final int DIALOG_OTARESULT          = 7;
    private static final int DIALOG_UNZIPPING          = 8;
    
    private static final String OTARESULT_DLG_TITLE = "otaresult_dlg_title";
    private static final String OTARESULT_DLG_MSG = "otaresult_dlg_msg";
    
    private static final int MENU_ID_DOWNLOAD = Menu.FIRST;
    private static final int MENU_ID_PAUSE = Menu.FIRST + 1;
    private static final int MENU_ID_CANCEL = Menu.FIRST + 2;
    private static final int MENU_ID_RESUME = Menu.FIRST + 3;
    private static final int MENU_ID_OK = Menu.FIRST + 4;
    private static final int MENU_ID_RETRY = Menu.FIRST + 5;
    private static final int MENU_ID_UPGRADE = Menu.FIRST + 6;

    private static final String TAG = "GoogleOtaClient:";
	
	
    static GoogleOtaClient getInstance() {
        return mClientInstance;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Util.logInfo(TAG, "On create enter, thread name = " + Thread.currentThread().getName());
        super.onCreate(savedInstanceState);
        mDownloadStatus = DownloadStatus.getInstance(this);
        mClientInstance = this;

    }

    public void onDestroy() {
        Util.logInfo(TAG, "onDestroy");
        mDownloadStatus = null;
        mClientInstance = null;
        super.onDestroy();	
    }

    @Override
    public void onStart() {
        Util.logInfo(TAG, "onStart");
        isBg = false;
        Intent serviceIntent = new Intent(this, GoogleOtaService.class);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    public void onStop() {
        Util.logInfo(TAG, "onStop");
        isBg = true;
        //mHandler = null;
        try {
            mService.runningBg();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        onDoClearAction();
        unbindService(mConnection);
        super.onStop();
    }


    @Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onRestoreInstanceState(savedInstanceState);
		mOTADialogTitleResId = savedInstanceState.getInt(OTARESULT_DLG_TITLE);
		mOTADialogMessageResId = savedInstanceState.getInt(OTARESULT_DLG_MSG);
		
	}

    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		
		outState.putInt(OTARESULT_DLG_TITLE, mOTADialogTitleResId);
		outState.putInt(OTARESULT_DLG_MSG, mOTADialogMessageResId);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
    	
    	if(mMenuStatus == MenuStatus.Menu_Download) {
            menu.add(Menu.NONE, MENU_ID_DOWNLOAD, 0, R.string.btn_download)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	} else if(mMenuStatus == MenuStatus.Menu_Downloading) {

            menu.add(Menu.NONE, MENU_ID_PAUSE, 0, R.string.btn_pause)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(Menu.NONE, MENU_ID_CANCEL, 0, R.string.btn_cancel)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	} else if(mMenuStatus == MenuStatus.Menu_Finish) {
            menu.add(Menu.NONE, MENU_ID_OK, 0, R.string.btn_ok)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	} else if(mMenuStatus == MenuStatus.Menu_Retry) {
            menu.add(Menu.NONE, MENU_ID_RETRY, 0, R.string.btn_retry)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

    	} else if(mMenuStatus == MenuStatus.Menu_Upgrade) {
            menu.add(Menu.NONE, MENU_ID_UPGRADE, 0, R.string.btn_ok)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);  
    	} else if(mMenuStatus == MenuStatus.Menu_Resume) {
            menu.add(Menu.NONE, MENU_ID_RESUME, 0, R.string.btn_resume)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(Menu.NONE, MENU_ID_CANCEL, 0, R.string.btn_cancel)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	}
      
		return super.onCreateOptionsMenu(menu);
	}

    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
        switch (item.getItemId()) {
        
        case MENU_ID_DOWNLOAD:{

        	DownloadDescriptor dd = mDownloadStatus.getDownloadDescriptor();
        	if (!Util.isSdcardAvailable(this)) {
        		Toast.makeText(GoogleOtaClient.this, GoogleOtaClient.this.getString(R.string.sdcard_crash_or_unmount), Toast.LENGTH_LONG).show();
                return true;
        	}

			if (mDownloadStatus.getDLSessionStatus() == DownloadStatus.STATE_NEWVERSION_READY) {
				Util.deleteFile(Util.getPackageFileName(this));
			}
			
        	long lSizeNeeded = Util.checkSdcardSpaceNeeded(this,(long)(dd.size * 2.5));
        	if(lSizeNeeded == -1) {
        		Toast.makeText(GoogleOtaClient.this, GoogleOtaClient.this.getString(R.string.unmount_sdcard), Toast.LENGTH_LONG).show();
        		return true;

        	} else if(lSizeNeeded>0) {
        		Toast.makeText(GoogleOtaClient.this, GoogleOtaClient.this.getString(R.string.insufficient_space_content, lSizeNeeded), Toast.LENGTH_LONG).show();
        		return true;

        	}

            mDlPkgProgressThread = new SessionStateControlThread(DownloadStatus.STATE_DOWNLOADING);
            mDlPkgProgressThread.start();
            onDownloadingPkg();
        }
            return true;
        case MENU_ID_PAUSE:{

        	Util.logInfo(TAG,"onDownloadingPkg, pause");
            onDlPkgPaused(true);
        
        }
            return true;
        case MENU_ID_CANCEL:{

        	Util.logInfo(TAG,"onDownloadingPkg, cancel");
            onDlPkgCanceled();
        
        }
            return true;
        case MENU_ID_RESUME: {

        	onDlPkgResume();
        
        }

            return true;
        case MENU_ID_OK: {

            finish();
        
        }

            return true;
        case MENU_ID_RETRY: {

            onQueryNewVersion();
        
        }

            return true;
            
        case MENU_ID_UPGRADE: {

            onQueryTypeSelect(mUpdateRadioList.getCheckedItemPosition());
        
        
        }

            return true;
    }
		return super.onOptionsItemSelected(item);
	}

	private void onDoClearAction() {

    	dismissDialog(mUnzipProgressDialog);
        dismissDialog(mDownloadCancelDialog);
        dismissDialog(mDownloadStorageDialog);
        dismissDialog(mAlertDialog);

    	mUnzipProgressDialog = null;
        mDownloadCancelDialog = null;
        mDownloadStorageDialog = null;
        mAlertDialog = null;
    }

    private void onQueryNewVersion() {
        Util.logInfo(TAG,"onQueryNewVersion");
        setContentView(R.layout.main);
        findViewById(R.id.ClientIssueScreen).setVisibility(View.GONE);
        showDialog(DIALOG_QUERYWAITING);
        
        if(mService == null) {
        	return;
        }
        
        try {
            mService.setStartFlag();
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        if (mQueryNewVersionThread != null && mQueryNewVersionThread.isAlive()) {
        	Util.logInfo(TAG,"onQueryNewVersion back from interrupt, mQueryNewVersionThread="+mQueryNewVersionThread);
            return;
        }
        mQueryNewVersionThread = new SessionStateControlThread(DownloadStatus.STATE_QUERYNEWVERSION);
        mQueryNewVersionThread.start();
    }
    
    private void onQueryNewVersionAbort() {
        Util.logInfo(TAG,"onQueryNewVersionAbort");
        try {
            mService.queryNewVersionAbort();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void onNonDialogPrompt(int resId) {
        Util.logInfo(TAG,"onNonDialogPrompt");
    	removeDialog(DIALOG_QUERYWAITING);
        setContentView(R.layout.main);
        findViewById(R.id.ClientIssueScreen).setVisibility(View.VISIBLE);
        mScreenText = (TextView)findViewById(R.id.ClientIssueText);
        mScreenText.setText(resId);
        
        mMenuStatus = MenuStatus.Menu_Finish;
        invalidateOptionsMenu();
        
    }

    private void onDialogPrompt(int id) {
        Util.logInfo(TAG,"onDialogPrompt, id = "+ id);
        if (id == DIALOG_NOENOUGHSPACE) {
        	showDialog(DIALOG_NOENOUGHSPACE);
        } else if (id == DIALOG_NOSDCARD ) {
        	showDialog(DIALOG_NOSDCARD);
        } else {
        	showDialog(DIALOG_UNKNOWNERROR);
        }
    }

    private void onUnknowPrompt() {
    	if (DownloadStatus.STATE_QUERYNEWVERSION == mDownloadStatus.getDLSessionStatus()) {
    	    onNonDialogPrompt(R.string.unknown_error_content);
    	} else {
    		onDialogPrompt(DIALOG_UNKNOWNERROR);
    	}
    	
    }

    private void onNetworkError(int netErrType, int netErrCode) {
        Util.logInfo(TAG,"onNetworkError, isServerNetwork = "+netErrType);
        if (netErrType == NETWORKERROR_SPECIFIED) {
            onLayoutErrorInfo(mDownloadStatus.getDLSessionStatus(), netErrCode);
        } else {
        	onLayoutErrorInfo(mDownloadStatus.getDLSessionStatus(), netErrCode);
        }
    }
    
    private void onLayoutErrorInfo(int status, int netErrCode) {
        Util.logInfo(TAG,"onLayoutErrorInfo, status = "+status);
        if (status == DownloadStatus.STATE_QUERYNEWVERSION) {
        	removeDialog(DIALOG_QUERYWAITING);
            setContentView(R.layout.main);
            if (netErrCode == NETWORKERROR_SPECIFIED) {
            	findViewById(R.id.ClientIssueScreen).setVisibility(View.GONE);
            	showDialog(DIALOG_SERVERERROR);
            	return;
            }
            findViewById(R.id.ClientIssueScreen).setVisibility(View.VISIBLE);
            mScreenText = (TextView)findViewById(R.id.ClientIssueText);
            mScreenText.setText(R.string.network_error);
            
            mMenuStatus = MenuStatus.Menu_Retry;
            invalidateOptionsMenu();
            
        } else if (status == DownloadStatus.STATE_DOWNLOADING || status == DownloadStatus.STATE_PAUSEDOWNLOAD) {
        	if (netErrCode == NETWORKERROR_SPECIFIED) {
        	    showDialog(DIALOG_SERVERERROR);
        	} else {
        	    showDialog(DIALOG_NETWORKERROR);
        	}
        	onDlPkgPaused(false);
            mMenuStatus = MenuStatus.Menu_Resume;
            invalidateOptionsMenu();
        }
    }

    private void onRequeryNeed() {
        Util.logInfo(TAG,"onRequeryNeed");
        setContentView(R.layout.main);
        findViewById(R.id.ClientIssueScreen).setVisibility(View.VISIBLE);
        mScreenText = (TextView)findViewById(R.id.ClientIssueText);
        mScreenText.setText(R.string.delta_has_deleted);
        mMenuStatus = MenuStatus.Menu_Retry;
        invalidateOptionsMenu();
    }

    private void onNoNewVersionDetected() {
        Util.logInfo(TAG,"onNoNewVersionDetected");
    	removeDialog(DIALOG_QUERYWAITING);
        setContentView(R.layout.main);
        findViewById(R.id.ClientIssueScreen).setVisibility(View.VISIBLE);
        mScreenText = (TextView)findViewById(R.id.ClientIssueText);
        mScreenText.setText(R.string.no_new_version);
        mMenuStatus = MenuStatus.Menu_Finish;
        invalidateOptionsMenu();
    }
    
    private void onNewVersionDetected() {
        Util.logInfo(TAG,"onNewVersionDetected");
    	removeDialog(DIALOG_QUERYWAITING);
        DownloadDescriptor dd = mDownloadStatus.getDownloadDescriptor();
        onLayoutNewVersionInfo(dd);
        findViewById(R.id.ClientNewVersionProgress).setVisibility(View.GONE);
        
        mMenuStatus = MenuStatus.Menu_Download;
        invalidateOptionsMenu();    }
    
    private void onDownloadingPkg(){   
        Util.logInfo(TAG,"onDownloadingPkg");
        DownloadDescriptor dd = mDownloadStatus.getDownloadDescriptor();
        onLayoutNewVersionInfo(dd);
        findViewById(R.id.ClientNewVersionProgress).setVisibility(View.VISIBLE);

        
        mMenuStatus = MenuStatus.Menu_Downloading;
        invalidateOptionsMenu();
        onDlPkgUpgrade(dd.size);
    }

    private void onDlPkgPaused(boolean exitClientScreen) {
        Util.logInfo(TAG,"onDlPkgPaused");
        new SessionStateControlThread(DownloadStatus.STATE_PAUSEDOWNLOAD).start();
        if (exitClientScreen){
            finish();
        }
    }

    private void onDlPkgResume() {
        Util.logInfo(TAG,"onDlPkgResume");
        mDlPkgProgressThread = new SessionStateControlThread(DownloadStatus.STATE_DOWNLOADING);
        mDlPkgProgressThread.start();
        onDownloadingPkg();
    }

    private void onDlPkgCanceled() {
        Util.logInfo(TAG,"onDlPkgCancelled");
        onDlPkgPaused(false);
        showDialog(DIALOG_CANCELDOWNLOAD);
    }

    private void onDlPkgUpgrade(long totalSize) {
    	float fsize = -1;
    	float tsize = -1;
        long currSize = Util.getFileSize(Util.getPackageFileName(this));
        if (currSize < 0) {
            currSize = 0;
        }
        if (totalSize == 0) {
            totalSize = -1;
        }
        Util.logInfo(TAG,"onDlPkgUpgrade dlSize:" + currSize + " totalSize:" +totalSize);
        if (totalSize < 0) {
            return;
        }
        mDlRatioProgressBar = (ProgressBar)findViewById(R.id.ClientNewVersionbar);
		mDlRatio = (TextView)findViewById(R.id.ClientNewVersionPercent);
		mDlProgress = (TextView)findViewById(R.id.ClientNewVersionRatio);
		if (mDownloadStatus.getDLSessionRenameState() && mDownloadStatus.getDownLoadPercent() == 100) {
		    currSize = totalSize;
		    Util.logInfo(TAG,"onDlPkgUpgrade, download complete but upzip terminate by exception");
		}
        int ratio = (int)(((double)currSize / (double)totalSize) * 100);
        if (ratio > 100) {
            ratio = 100;
            currSize = totalSize;
        }
        mDownloadStatus.setDownLoadPercent(ratio);
        mDlRatioProgressBar.setProgress(ratio);
        CharSequence ratioText = Integer.toString(ratio) + "%";
        mDlRatio.setText(ratioText);
        fsize = (float)((float)currSize / 1024.0);
        fsize = (float)(((int)(fsize*100))/100.0);
        tsize = (float)((float)totalSize / 1024.0);
        tsize = (float)(((int)(tsize*100))/100.0);
        CharSequence progressText = Float.toString(fsize) + " / " + Float.toString(tsize) + " (KB)";
        mDlProgress.setText(progressText);
    }

    private void onLayoutNewVersionInfo(final DownloadDescriptor dd) {
        Util.logInfo(TAG,"onLayoutNewVersionInfo");
        setContentView(R.layout.downloading_client_screen);
        mDlNewContentTitle = (TextView)findViewById(R.id.new_ver_content_title);

        mDlNewNotesContent = (TextView)findViewById(R.id.new_ver_Notes_content);
        float kb = (float)((float)dd.size / 1024.0);
        kb = (float)(((int)(kb*100))/100.0);

        mDlNewContentTitle.setText(dd.version + " " + "(" +kb + " KB)");

        String str;

        if((dd.newNote == null)||(dd.newNote.isEmpty())) {
        	mDlNewNotesContent.setVisibility(View.GONE);

        	return;
        }
        mDlNewNotesContent.setText(dd.newNote);
    }

    private void onDlPkgComplete() {  	
        Util.logInfo(TAG,"onDlPkgComplete");
        String[] updateTypes = this.getResources().getStringArray(R.array.enquire_type);
        setContentView(R.layout.update_enquire);
        onShowUpdateList(updateTypes);
        mMenuStatus = MenuStatus.Menu_Upgrade;
        invalidateOptionsMenu();
    }

	private void onShowUpdateList(String[] updateTypes){
		
		Util.logInfo(TAG,"onShowUpdateList");
		mUpdateRadioList = (ListView)findViewById(R.id.UpdateEnquireList);
		mUpdateRadioList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, updateTypes));
		mUpdateRadioList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mUpdateRadioList.setClickable(true);
		mUpdateRadioList.setItemsCanFocus(false);
		mUpdateRadioList.setItemChecked(0, true);
	}

	private void onQueryTypeSelect(long typeId){
		Util.logInfo(TAG, "onQueryTypeSelect typeId = " + typeId); 
		mQueryType = (int)typeId;
		new SessionStateControlThread(DownloadStatus.STATE_DLPKGCOMPLETE).start();
		//GoogleOtaClient.this.finish();
	}

    private void dismissDialog(Dialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        Util.logInfo(TAG, "onCreateDialog id, dialog id = "+ id ); 
        switch (id) {
        case DIALOG_QUERYWAITING:
        	ProgressDialog preDialog = new ProgressDialog(this);
        	preDialog.setIndeterminate(true);
        	preDialog.setCancelable(true);
        	preDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        	preDialog.setMessage(getString(R.string.new_version_query));
        	preDialog.show();
        	preDialog.setOnCancelListener(
                new DialogInterface.OnCancelListener() {	
                    public void onCancel(DialogInterface dialog) {
                    	onQueryNewVersionAbort();
                    	GoogleOtaClient.this.finish();

                    }
                });
            return preDialog;
        case DIALOG_UNZIPPING:
            mUnzipProgressDialog = new ProgressDialog(this);
            mUnzipProgressDialog.setIndeterminate(true);
            mUnzipProgressDialog.setCancelable(true);
            mUnzipProgressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
            mUnzipProgressDialog.setMessage(getString(R.string.package_unzip));
            mUnzipProgressDialog.show();
            mUnzipProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						GoogleOtaClient.this.finish();
						return true;
					}
					return false;
				}
			});
            mUnzipProgressDialog.setOnDismissListener(
                new DialogInterface.OnDismissListener() {	
                    public void onDismiss(DialogInterface dialog) {
                    	mUnzipProgressDialog = null;
                    }
                });
            return mUnzipProgressDialog;
        case DIALOG_NETWORKERROR:
			return mAlertDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.network_error)
           .setMessage(GoogleOtaClient.this.getString(R.string.network_error_content))
            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {				
                public void onClick(DialogInterface dialog, int whichButton) {
                	int status = mDownloadStatus.getDLSessionStatus();
                	if (status == DownloadStatus.STATE_PAUSEDOWNLOAD) {
                		Util.logInfo(TAG,"onCreateDialog, DIALOG_NETWORKERROR resume");
                    	onDlPkgResume();
                    }
                }
            })
            .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {				
                public void onClick(DialogInterface dialog, int whichButton) {
                    GoogleOtaClient.this.finish();
				}
            })
            .create();
        case DIALOG_SERVERERROR:
			return mAlertDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.server_error_title)
           .setMessage(GoogleOtaClient.this.getString(R.string.server_error_content))
            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {				
                public void onClick(DialogInterface dialog, int whichButton) {
                	int status = mDownloadStatus.getDLSessionStatus();
                	Intent it = new Intent();
                    it.setAction("android.settings.WIRELESS_SETTINGS");
                    startActivity(it);
                    if (status == DownloadStatus.STATE_QUERYNEWVERSION) {
                        GoogleOtaClient.this.finish();
                    }
                }
            })
            .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {				
                public void onClick(DialogInterface dialog, int whichButton) {
                    GoogleOtaClient.this.finish();
				}
            })
            .create();
        case DIALOG_NOENOUGHSPACE:
            return mDownloadStorageDialog = new AlertDialog.Builder(this)
            .setTitle(R.string.insufficient_space_title)
            .setMessage(GoogleOtaClient.this.getString(R.string.insufficient_space))
            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {				
                public void onClick(DialogInterface dialog, int whichButton) {
                	//GoogleOtaClient.this.resetDescriptionInfo();
                	GoogleOtaClient.this.finish();
                }
            })
            .create();
        case DIALOG_NOSDCARD:
            return mDownloadStorageDialog = new AlertDialog.Builder(this)
            .setTitle(R.string.error_sdcard)
            .setMessage(GoogleOtaClient.this.getString(R.string.sdcard_crash_or_unmount))
            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {				
                public void onClick(DialogInterface dialog, int whichButton) {
                	GoogleOtaClient.this.resetDescriptionInfo();
                	GoogleOtaClient.this.finish();
                }
            })
            .create();
        case DIALOG_UNKNOWNERROR:
            return mAlertDialog = new AlertDialog.Builder(this)
            .setTitle(R.string.unknown_error)
            .setMessage(GoogleOtaClient.this.getString(R.string.unknown_error_content))
            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {				
                public void onClick(DialogInterface dialog, int whichButton) {
                	GoogleOtaClient.this.finish();
                }
            })
            .create();
        case DIALOG_CANCELDOWNLOAD:
            return mDownloadCancelDialog = new AlertDialog.Builder(this)
            .setTitle(R.string.cancel_download_title)
            .setMessage(GoogleOtaClient.this.getString(R.string.cancel_download_content))
            .setPositiveButton(R.string.cancel_download_positive_btn, new DialogInterface.OnClickListener() {				
                public void onClick(DialogInterface dialog, int whichButton) {
                    mDownloadCancelDialog = null;
                    GoogleOtaClient.this.onDownloadCancel();
                    new SessionStateControlThread(DownloadStatus.STATE_CANCELDOWNLOAD).start();

                    GoogleOtaClient.this.finish();
                }
            })
            .setNegativeButton(R.string.cancel_download_negative_btn, new DialogInterface.OnClickListener() {				
                public void onClick(DialogInterface dialog, int whichButton) {
                    int status = mDownloadStatus.getDLSessionStatus();
                    if (status == DownloadStatus.STATE_PAUSEDOWNLOAD) {
                        Util.logInfo(TAG,"onCreateDialog, DIALOG_NETWORKERROR resume");
                        onDlPkgResume();
                    }
                    mDownloadCancelDialog = null;
                }
            })
            .create();
        case DIALOG_OTARESULT:
            return mAlertDialog = new AlertDialog.Builder(this)
            .setTitle(mOTADialogTitleResId)
            .setMessage(GoogleOtaClient.this.getString(mOTADialogMessageResId))
            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {				
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (mNeedReset) {
                    	GoogleOtaClient.this.resetDownloadDesctiptor();
                        Util.logInfo(TAG,"onCreateDialog, DDIALOG_OTARESULT reset otaclient");
                        new SessionStateControlThread(DownloadStatus.STATE_PACKAGEERROR).start();
                    }
                    GoogleOtaClient.this.finish();
                }
            }).setOnKeyListener(new DialogInterface.OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						GoogleOtaClient.this.finish();
						return true;
					}
					return false;
				}
			})
            .create();
        }
        return null;
    }

    public void onDownloadCancel() {
        Util.logInfo(TAG, "onDownloadCancel");
        Util.deleteFile(Util.getPackageFileName(this));
        mDownloadStatus.setDLSessionStatus(DownloadStatus.STATE_QUERYNEWVERSION);
        DownloadDescriptor NewVersionInfo = mDownloadStatus.getDownloadDescriptor();
        NewVersionInfo.deltaId = -1;
        NewVersionInfo.newNote = null;
        NewVersionInfo.size = -1;
        NewVersionInfo.version = null;
        mDownloadStatus.setDownloadDesctiptor(NewVersionInfo);
        mDownloadStatus.setDownLoadPercent(-1);
    }
    
    private void resetDescriptionInfo() {
        Util.logInfo(TAG, "resetDescriptionInfo");
        File imgf = new File(Util.getPackageFileName(this));
        if (imgf.exists()) {
            Util.logInfo(TAG, "resetDescriptionInfo, image exist, delete it");
            Util.deleteFile(Util.getPackageFileName(this));
        }
        resetDownloadDesctiptor();
    }
    
    private void resetDownloadDesctiptor() {
    	mDownloadStatus.setDownLoadPercent(-1);
    	mDownloadStatus.setDLSessionUnzipState(false);
        mDownloadStatus.setDLSessionRenameState(false);
        mDownloadStatus.setDLSessionStatus(DownloadStatus.STATE_QUERYNEWVERSION);
        DownloadDescriptor dd = mDownloadStatus.getDownloadDescriptor();
        if (dd == null) {
        	return;
        }
        dd.deltaId = -1;
        dd.size = -1;
        dd.newNote = null;
        dd.version = null;
        mDownloadStatus.setDownloadDesctiptor(dd);
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
    	switch (id) {
        case DIALOG_UNZIPPING:
            if (mUnzipProgressDialog == null) {
                mUnzipProgressDialog = (ProgressDialog)dialog;
            }
            break;
        case DIALOG_CANCELDOWNLOAD:
            if (mDownloadCancelDialog == null) {
            	mDownloadCancelDialog = (AlertDialog)dialog;
            }
            break;
        }
        super.onPrepareDialog(id, dialog);
    }
    
    public class SessionStateControlThread extends Thread {
        public SessionStateControlThread(int statusType) {
	        status = statusType;
        }

        public void run() {
            if(mService == null) {
                Util.logInfo(TAG, "SessionStateControlThread mService = null");
                return;
            }
            Util.logInfo(TAG, "SessionStateControlThread, status = " + status+", thread name = " + Thread.currentThread().getName());

            try {
                switch(status) {
                case DownloadStatus.STATE_QUERYNEWVERSION:
                    try {
                        sleep (150);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mService.queryNewVersion();
                    break;
                case DownloadStatus.STATE_DOWNLOADING:
                    mService.startDlPkg();
                    break;
                case DownloadStatus.STATE_CANCELDOWNLOAD:
                	mService.cancelDlPkg();
                    break;
                case DownloadStatus.STATE_PAUSEDOWNLOAD:
                	mService.pauseDlPkg();
                    break;
                case DownloadStatus.STATE_DLPKGCOMPLETE:
                    mService.setUpdateType(mQueryType);
                    break;
                case DownloadStatus.STATE_PACKAGEERROR:
                    mService.resetDescriptionInfo();
                    break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        private int status;
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Util.logInfo(TAG, "onServiceConnected, thread name = " + Thread.currentThread().getName());
            mService = IGoogleOtaService.Stub.asInterface(service);
            Util.logInfo(TAG, "onServiceConnected, mService = " + mService);

            int status = mDownloadStatus.getDLSessionStatus();
            Util.logInfo(TAG, "onServiceConnected, download status = " + status);
            switch(status) {
            case DownloadStatus.STATE_QUERYNEWVERSION:
                onQueryNewVersion();
                break;
            case DownloadStatus.STATE_NEWVERSION_READY:
			    Util.clearNotification(mClientInstance, NotifyManager.NOTIFY_NEW_VERSION);
			    onNewVersionDetected();
                break;
            case DownloadStatus.STATE_DOWNLOADING:
            	if (!Util.isSdcardAvailable(GoogleOtaClient.this)) {
            		mDownloadStatus.setDLSessionStatus(IDownloadStatus.STATE_PAUSEDOWNLOAD);
            		onDlPkgResume();
            	} else {
            	Util.clearNotification(mClientInstance, NotifyManager.NOTIFY_DOWNLOADING);
				onDownloadingPkg();
            	}
                break;
            case DownloadStatus.STATE_PAUSEDOWNLOAD:
                onDlPkgResume();
                break;
            case DownloadStatus.STATE_DLPKGCOMPLETE:
            	Util.clearNotification(mClientInstance, NotifyManager.NOTIFY_DL_COMPLETED);
            	onDlPkgComplete();
                break;
            default:
                break;
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Util.logInfo(TAG, "onServiceDisconnected");	
            mService = null;
        }
    };
	
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Util.logInfo(TAG, "handleMessage msg.what = "+ msg.what + ", mHandler = "+ mHandler); 
            Util.logInfo(TAG, "handleMessage thread name = "+ Thread.currentThread().getName() );
            if (isBg/*mHandler == null*/) {
            	super.handleMessage(msg);
            	return;
            }

            switch(msg.what) {
            case DownloadStatus.MSG_NETWORKERROR:
                onNetworkError(msg.arg1, msg.arg2);
                break;
            case DownloadStatus.MSG_NONEWVERSIONDETECTED:
                onNoNewVersionDetected();
                break;
            case DownloadStatus.MSG_NEWVERSIONDETECTED:
            	onNewVersionDetected();
                break;
            case DownloadStatus.MSG_DLPKGCOMPLETE:
            	dismissDialog(mUnzipProgressDialog);
            	onDlPkgComplete();
                break;
            case DownloadStatus.MSG_DLPKGUPGRADE:
            	DownloadDescriptor dd = mDownloadStatus.getDownloadDescriptor();
            	onDlPkgUpgrade(dd.size);
                break;
            case DownloadStatus.MSG_NOTSUPPORT:
            case DownloadStatus.MSG_NOTSUPPORT_TEMP:
            	onNonDialogPrompt(R.string.not_support_version);
                break;
            case DownloadStatus.MSG_NOVERSIONINFO:
            	onNonDialogPrompt(R.string.need_target_info);
                break;
            case DownloadStatus.MSG_DELTADELETED:
            	onRequeryNeed();
                break;
            case DownloadStatus.MSG_SDCARDCRASHORUNMOUNT:
            	onNonDialogPrompt(R.string.sdcard_crash_or_unmount);
                break;
            case DownloadStatus.MSG_SDCARDUNKNOWNERROR:
            	dismissDialog(mUnzipProgressDialog);
            	onDialogPrompt(DIALOG_NOSDCARD);
                break;
            case DownloadStatus.MSG_SDCARDINSUFFICENT:
            	dismissDialog(mUnzipProgressDialog);
            	onDialogPrompt(DIALOG_NOENOUGHSPACE);
                break;
            case DownloadStatus.MSG_UNKNOWERROR:
            	onUnknowPrompt();
                break;
            case DownloadStatus.MSG_OTA_PACKAGEERROR:
            case DownloadStatus.MSG_OTA_RUNCHECKERROR:
            	mNeedReset = true;
            	mOTADialogTitleResId = R.string.package_error_title;
                mOTADialogMessageResId = R.string.package_error_message_invalid;
            	showDialog(DIALOG_OTARESULT);
            	break;
            case DownloadStatus.MSG_OTA_NEEDFULLPACKAGE:
            	mNeedReset = true;
            	mOTADialogTitleResId = R.string.package_error_title;
                mOTADialogMessageResId = R.string.package_error_message_full;
            	showDialog(DIALOG_OTARESULT);
            	break;
            case DownloadStatus.MSG_OTA_SDCARDERROR:
            	mNeedReset = false;
            	mOTADialogTitleResId = R.string.package_error_title;
                mOTADialogMessageResId = R.string.unmount_sdcard;
            	showDialog(DIALOG_OTARESULT);
            	break;
            case DownloadStatus.MSG_OTA_USERDATAERROR:
            	mNeedReset = true;
            	mOTADialogTitleResId = R.string.package_error_title;
                mOTADialogMessageResId = R.string.package_error_message_crash;
            	showDialog(DIALOG_OTARESULT);
            	break;
            case DownloadStatus.MSG_OTA_SDCARDINFUFFICENT:
            	mNeedReset = false;
            	mOTADialogTitleResId = R.string.package_error_title;
                mOTADialogMessageResId = R.string.insufficient_space;
            	showDialog(DIALOG_OTARESULT);
            	break;
            case DownloadStatus.MSG_OTA_USERDATAINSUFFICENT:
            	mNeedReset = false;
            	mOTADialogTitleResId = R.string.package_error_title;
                mOTADialogMessageResId = R.string.package_error_message_insuff;
            	showDialog(DIALOG_OTARESULT);
            	break;
            case DownloadStatus.MSG_UNZIP_LODING:
            	showDialog(DIALOG_UNZIPPING);
            	break;
            case DownloadStatus.MSG_CKSUM_ERROR:
            case DownloadStatus.MSG_UNZIP_ERROR:
                dismissDialog(mUnzipProgressDialog);
            	mNeedReset = true;
            	mOTADialogTitleResId = R.string.package_unzip_error;
                mOTADialogMessageResId = R.string.package_error_message_invalid;
            	showDialog(DIALOG_OTARESULT);
            	break;
            case DownloadStatus.MSG_OTA_CLOSECLIENTUI:
            	GoogleOtaClient.this.finish();
            	break;
            default:
                super.handleMessage(msg);
            }
        }
    };
    

}
