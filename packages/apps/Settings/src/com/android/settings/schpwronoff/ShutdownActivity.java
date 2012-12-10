package com.android.settings.schpwronoff;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.Window;

import com.mediatek.xlog.Xlog;

public class ShutdownActivity extends Activity {
	private static final String TAG="ShutdownActivity";
    public static CountDownTimer mCountDownTimer = null;
    private KeyguardLock lock;
    private String mMessage;
    private int secondsCountdown;
    private static final int DIALOG = 1;
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SchPwrWakeLock.acquireCpuWakeLock(this);
        PowerManager pm =
            (PowerManager) getSystemService(Context.POWER_SERVICE);
        Log.d(TAG,"screen is on ? ----- "+pm.isScreenOn());
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        
        if(savedInstanceState!=null){
            secondsCountdown = savedInstanceState.getInt("lefttime");
            mMessage = savedInstanceState.getString("message");
        }else{
            secondsCountdown = 11;
        }
        mCountDownTimer = new CountDownTimer(secondsCountdown*1000,1000){
            @Override
            public void onTick(long millisUntilFinished) {
                secondsCountdown = (int) (millisUntilFinished / 1000);
                if(secondsCountdown>1){
                    mMessage = getString(com.android.settings.R.string.schpwr_shutdown_message,secondsCountdown);
                }else{
                    mMessage = getString(com.android.settings.R.string.schpwr_shutdown_message_second,secondsCountdown);
                }
                    Xlog.d(TAG,"showDialog");	
                showDialog(DIALOG);
            }

            @Override
            public void onFinish() {
                if(SchPwrOnOffService.isInCall){
                    Xlog.d(TAG,"phone is incall, countdown end");
                    finish();
                } else {
                    fireShutDown();
                    mCountDownTimer = null;
                }
            }
        };
        
        Xlog.d(TAG,"ShutdonwActivity onCreate");
        if (mCountDownTimer != null) {
            mCountDownTimer.start();                             
        }else{
            finish();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("lefttime",secondsCountdown );
        outState.putString("message",mMessage );
    }
    
    private void cancelCountDownTimer(){
        if (mCountDownTimer != null) {
            Xlog.d(TAG,"cancel mCountDownTimer");
            mCountDownTimer.cancel();
            mCountDownTimer = null;
            SchPwrWakeLock.releaseCpuLock();
        }
    }
    protected void onDestory(){
        Xlog.d(TAG,"onDestory");
        super.onDestroy();
        cancelCountDownTimer();
    }
    
    public void onResume(){
        Xlog.d(TAG,"onResume");
    	super.onResume();
    }
    public void onStop(){
        Xlog.d(TAG,"onStop");
        super.onStop();
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent){
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Xlog.d(TAG,"onCreateDialog");
        AlertDialog dialog = new AlertDialog.Builder(this)
        .setCancelable(false)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(com.android.internal.R.string.power_off)
        .setMessage(mMessage)
        .setPositiveButton(com.android.internal.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (mCountDownTimer != null){
                                mCountDownTimer.cancel();
                                mCountDownTimer = null;
                            }
                            fireShutDown();
                        }
                    })
        .setNegativeButton(com.android.internal.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            cancelCountDownTimer();
                            finish();
                        }
                    })
        .create();
        if (!getResources().getBoolean(
                com.android.internal.R.bool.config_sf_slowBlur)) {
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }
        Window win = dialog.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED;
        win.setAttributes(winParams);
        return dialog;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog){
        ((AlertDialog)dialog).setMessage(mMessage);
    }

    private void fireShutDown() {
        Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }
}
