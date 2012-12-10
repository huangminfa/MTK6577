package com.android.settings.lbs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;

import com.android.settings.R;
import com.mediatek.agps.MtkAgpsConfig;
import com.mediatek.agps.MtkAgpsManager;
import com.mediatek.agps.MtkAgpsProfile;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class AgpsNotifyDialog extends Activity {

    private static final String XLOGTAG = "Settings/AgpsDlg";
    private static final String PREFERENCE_FILE = "com.android.settings_preferences";

    private static final String DISABLE_KEY = "disable_agps_on_reboot";
    private static final String AGPS_ENABLE_KEY = "location_agps_enable";
    private static final String EM_ENABLE_KEY = "EM_Indication";
    private static final String UNKNOWN_VALUE="UNKNOWN_VALUE";

//    private   AgpsProfileManager  mProfileMgr;
    private   Handler mHandler;
    
    private String mMessage;
    private String mRequestId;
    private String mCliecntName;
    
    protected static MtkAgpsManager mAgpsMgr;
    
    private static final int IND_EM_DIALOG_ID = 0;
    private static final int NOTIFY_ALLOW_NO_ANSWER_DIALOG_ID = 1;
    private static final int NOTIFY_ALLOW_NO_DENY_DIALOG_ID = 2;
    private static final int IND_ERROR_DIALOG_ID = 3;
    private static final int NOTIFY_ONLY_DIALOG_ID = 4;

    private Timer mTimer = new Timer();
    private boolean mIsUserResponse = false;
    private boolean mGetOtherNotify = false;
    private Dialog mDialog = null;
    private String mTitle = new String();
    
    public void sendNotification(Context context, int icon, String ticker, String title, String content, int id) {

        Intent intent = new Intent("");
        PendingIntent appIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        
        Notification notification = new Notification(); 
        notification.icon = icon;
        notification.tickerText = ticker; 
        notification.defaults = 0;
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.setLatestEventInfo(context, title, content, appIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }
    
    
    public void finishActivity() {
        mTimer.cancel();
        if(mIsUserResponse == false) {
            sendNotification(this, R.drawable.ic_btn_next, mTitle, 
                mTitle, mMessage, new Random().nextInt(10000));
        }
        if(mDialog != null) {
            mDialog.dismiss();
        }

        if(mGetOtherNotify == false) {
            finish();
        } else{
            mGetOtherNotify = false;
        }
    }

    //type=0: notify only
    private void setTimerIfNeed(int type) {
        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                finishActivity();
            }
        };

        MtkAgpsConfig config = mAgpsMgr.getConfig();
        
        int notifyTimeout = config.notifyTimeout;
        int verifyTimeout = config.verifyTimeout;
        boolean timerEnabled = (config.niTimer==1)?true:false;

        log("notifyTimeout=" + notifyTimeout + " verifyTimeout=" + verifyTimeout + " timerEnabled=" + timerEnabled);

        if(timerEnabled == true) {
            int timeout = 0;
            if(type == 0) {
                timeout = notifyTimeout*1000;
            } else {
                timeout = verifyTimeout*1000;
            }
            mTimer.schedule(task, timeout);
        }

    }

    private void setup(Intent intent) {
        
        if(mAgpsMgr == null) {
            mAgpsMgr = (MtkAgpsManager)getSystemService(Context.MTK_AGPS_SERVICE);    
        }
        if(mHandler == null) {
            mHandler = new Handler();
        }
        
        Bundle bundle = intent.getExtras();
        int mtype = -1;
        int mid = -1;

        if(bundle != null) {
            mtype = bundle.getInt("msg_type", MtkAgpsManager.AGPS_IND_ERROR);
            mid = bundle.getInt("msg_id", MtkAgpsManager.AGPS_CAUSE_NONE);
            mMessage = this.getString(getStringID(mtype, mid));
            mRequestId = bundle.getString("request_id");
            mCliecntName = bundle.getString("client_name");
        } else {
            log("Error: Bundle is null");
        }

        
        if(mRequestId != null && mCliecntName != null && 
                !(mRequestId.equals(UNKNOWN_VALUE)) && !(mCliecntName.equals(UNKNOWN_VALUE))) {
            
            mMessage = mMessage + "\n" + getString(R.string.NI_Request_ID) + ": " + mRequestId + "\n" + getString(R.string.NI_Request_ClientName)
                + ": " + mCliecntName + "\n";
        } else if(mRequestId != null && !(mRequestId.equals(UNKNOWN_VALUE))) {
            mMessage = mMessage + "\n" + getString(R.string.NI_Request_ID) + ": " + mRequestId;
        } else if(mCliecntName != null && !(mCliecntName.equals(UNKNOWN_VALUE))) {
            mMessage = mMessage + "\n" + getString(R.string.NI_Request_ClientName) + ": " + mCliecntName;
        }

        switch(mtype) {
            case MtkAgpsManager.AGPS_IND_EM:
                showDialog(IND_EM_DIALOG_ID);
                break;
            case MtkAgpsManager.AGPS_IND_NOTIFY:
                if(mid == MtkAgpsManager.AGPS_NOTIFY_ALLOW_NO_ANSWER) {
                    mTitle = getString(R.string.agps_str_verify);
                    setTimerIfNeed(1);
                    showDialog(NOTIFY_ALLOW_NO_ANSWER_DIALOG_ID);
                } else if(mid == MtkAgpsManager.AGPS_NOTIFY_DENY_NO_ANSWER) {
                    mTitle = getString(R.string.agps_str_verify);
                    setTimerIfNeed(1);
                    showDialog(NOTIFY_ALLOW_NO_DENY_DIALOG_ID);
                } else if(mid == MtkAgpsManager.AGPS_NOTIFY_ONLY) {
                    mTitle = getString(R.string.agps_str_notify);
                    setTimerIfNeed(0);
                    showDialog(NOTIFY_ONLY_DIALOG_ID);
                }
                break;
            case MtkAgpsManager.AGPS_IND_ERROR:
                showDialog(IND_ERROR_DIALOG_ID);
                break;            
        }
        
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 

        mIsUserResponse = false;
        setup(getIntent());
    }
    
    @Override
    public void onNewIntent(Intent intent) {
        log("onNewIntent is called ");
        mGetOtherNotify = true;
        finishActivity();
        setup(intent);
    }
    
    public Dialog onCreateDialog(int id){
        switch(id){
            case IND_EM_DIALOG_ID :
                mDialog = new AlertDialog.Builder(AgpsNotifyDialog.this)
                    .setTitle(R.string.EM_NotifyDialog_title)
                    .setMessage(mMessage)
                    .setOnCancelListener (new DialogInterface.OnCancelListener() {  
                    public void onCancel(DialogInterface dialog) {
                        log("EM_INDICATION click back key");
                        finishActivity();
                    }
                    }).setPositiveButton(R.string.agps_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        log("EM_INDICATION ok");
                        finishActivity();
                    }}).create();
                break;
            case NOTIFY_ALLOW_NO_ANSWER_DIALOG_ID:
                mDialog = new AlertDialog.Builder(AgpsNotifyDialog.this)
                    .setTitle(R.string.agps_str_verify)
                    .setMessage(mMessage)
                    .setOnCancelListener (new DialogInterface.OnCancelListener() {  
                     public void onCancel(DialogInterface dialog) { 
                        log("NOTIFY_ALLOW_NO_ANSWER_DIALOG_ID click back key");
                        mAgpsMgr.niUserResponse(2);
                        mIsUserResponse = true;
                        finishActivity();
                    }
                    }).setPositiveButton(R.string.agps_str_allow,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface,int i) {
                        log("NOTIFY_ALLOW_NO_ANSWER_DIALOG_ID click allow");
                        mAgpsMgr.niUserResponse(1);
                        mIsUserResponse = true;
                        finishActivity();
                    }
                    }).setNegativeButton(R.string.agps_str_deny,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        log("NOTIFY_ALLOW_NO_ANSWER_DIALOG_ID click deny");
                        mAgpsMgr.niUserResponse(2);
                        mIsUserResponse = true;
                        finishActivity();
                    }
                }).create();
                break;
            case NOTIFY_ALLOW_NO_DENY_DIALOG_ID:
                mDialog = new AlertDialog.Builder(AgpsNotifyDialog.this)
                .setTitle(R.string.agps_str_verify)
                .setMessage(mMessage)
                .setOnCancelListener (new DialogInterface.OnCancelListener() {  
                     public void onCancel(DialogInterface dialog) { 
                        log("NOTIFY_ALLOW_NO_DENY_DIALOG_ID click back key");
                        mAgpsMgr.niUserResponse(2);
                        mIsUserResponse = true;
                        finishActivity();
                    }
                }).setPositiveButton(R.string.agps_str_allow,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface,int i) {
                        log("NOTIFY_ALLOW_NO_DENY_DIALOG_ID click allow");
                        mAgpsMgr.niUserResponse(1);
                        mIsUserResponse = true;
                        finishActivity();
                    }
                }).setNegativeButton(R.string.agps_str_deny,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        log("NOTIFY_ALLOW_NO_DENY_DIALOG_ID click deny");
                        mAgpsMgr.niUserResponse(2);
                        mIsUserResponse = true;
                        finishActivity();
                    }
                }).create();
                break;
            case IND_ERROR_DIALOG_ID:
                mDialog = new AlertDialog.Builder(AgpsNotifyDialog.this)
                .setTitle(R.string.agps_str_error)
                .setMessage(mMessage)
                .setOnCancelListener (new DialogInterface.OnCancelListener() {  
                     public void onCancel(DialogInterface dialog) {
                        log("IND_ERROR_DIALOG_ID click back key");
                        finishActivity();
                    }
                }).setPositiveButton(R.string.agps_OK,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface,int i) {
                        log("IND_ERROR_DIALOG_ID click ok");
                        finishActivity();
                    }
                }).create();
                break;
            case NOTIFY_ONLY_DIALOG_ID:
                mDialog = new AlertDialog.Builder(AgpsNotifyDialog.this)
                    .setTitle(R.string.agps_str_notify)
                    .setMessage(mMessage)
                    .setOnCancelListener (new DialogInterface.OnCancelListener() {  
                     public void onCancel(DialogInterface dialog) {
                        log("NOTIFY_ONLY_DIALOG_ID click back key");
                        mIsUserResponse = true;
                        finishActivity();
                    }
                    }).setPositiveButton(R.string.agps_OK,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        log("NOTIFY_ONLY_DIALOG_ID click ok");
                        mIsUserResponse = true;
                        finishActivity();
                    }
                }).create();
                break;
            default:
                log("WARNING: No such dialog");
        };
        return mDialog;
    }


    //===== resource id ==========
    public int getStringID(int type, int id) {
        int result = R.string.AGPS_DEFAULT_STRING;
        switch(type){
        case MtkAgpsManager.AGPS_IND_INFO:
            result = getStringFromIndex(id, MtkAgpsManager.AGPS_INFO_CNT, INFO_STRING_LIST);
            break;
        case MtkAgpsManager.AGPS_IND_NOTIFY:
            result = getStringFromIndex(id, MtkAgpsManager.AGPS_NOTIFY_CNT, NOTIFY_STRING_LIST);
            break;
        case MtkAgpsManager.AGPS_IND_ERROR:
            result = getStringFromIndex(id, MtkAgpsManager.AGPS_CAUSE_CNT, ERROR_STRING_LIST);
            break;
        case MtkAgpsManager.AGPS_IND_EM:
            result = getStringFromIndex(id, MtkAgpsManager.AGPS_EM_CNT, EM_STRING_LIST);
            break;
        default:
            break;
        }
        return result;
    }
    
    private int getStringFromIndex(int id, int max, int[] list){
        for(int index = 1; index < max; index ++){
            if(index == id){
                return list[index];
            }
        }
        return R.string.AGPS_DEFAULT_STRING;
    }

    private static final int ERROR_STRING_LIST[] = {
        R.string.AGPS_DEFAULT_STRING,
        R.string.AGPS_CAUSE_NETWORK_CREATE_FAIL,
        R.string.AGPS_CAUSE_BAD_PUSH_CONTENT,
        R.string.AGPS_CAUSE_NOT_SUPPORTED,
        R.string.AGPS_CAUSE_REQ_NOT_ACCEPTED,
        R.string.AGPS_CAUSE_NO_RESOURCE,
        R.string.AGPS_CAUSE_NETWORK_DISCONN,
        R.string.AGPS_CAUSE_REMOTE_ABORT,
        R.string.AGPS_CAUSE_TIMER_EXPIRY,
        R.string.AGPS_CAUSE_REMOTE_MSG_ERROR,
        R.string.AGPS_CAUSE_USER_AGREE,
        R.string.AGPS_CAUSE_USER_DENY,
        R.string.AGPS_CAUSE_NO_POSITION,
        R.string.AGPS_CAUSE_TLS_AUTH_FAIL,
        R.string.USER_RESPONSE_TIMEOUT,
        R.string.AGPS_MODEM_RESET_HAPPEN
    };

    private static final int EM_STRING_LIST[] = {
        R.string.AGPS_DEFAULT_STRING,
        R.string.AGPS_EM_RECV_SI_REQ,
        R.string.AGPS_EM_POS_FIXED
    };

    private static final int NOTIFY_STRING_LIST[] = {
        R.string.AGPS_DEFAULT_STRING,
        R.string.AGPS_NOTIFY_ONLY,
        R.string.AGPS_NOTIFY_ALLOW_NO_ANSWER,
        R.string.AGPS_NOTIFY_DENY_NO_ANSWER,
        R.string.AGPS_NOTIFY_PRIVACY
    };

    private static final int INFO_STRING_LIST[] = {
        R.string.AGPS_DEFAULT_STRING
    };

       
    private void log(String info) {
        Xlog.d("hugo_app", "[AgpsNotify] " + info + " ");
    }
    
}


