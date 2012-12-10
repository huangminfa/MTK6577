package com.android.phone;

import java.io.File;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class PhoneRecorderServices extends Service {
	private static final String LOG_TAG = "RecorderServices";
	private static final String PHONE_VOICE_RECORD_STATE_CHANGE_MESSAGE = "com.android.phone.VoiceRecorder.STATE";
	private PhoneRecorder mPhoneRecorder = null;
	private boolean mMount = true;
	IPhoneRecordStateListener mStateListener = null;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_MEDIA_EJECT.equals(intent.getAction()) || 
					Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
				mMount = false;
                stopSelf();
	        } else if ( Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())) {
	        	mMount = true;
	        }
	    }
    };

	public IBinder onBind(Intent intent) {
		Log.d(LOG_TAG, "onBind");
		return mBinder;
	}

	public boolean onUnbind(Intent intent) {
		Log.d(LOG_TAG, "onUnbind");
		return super.onUnbind(intent);
	}	

	public void onCreate() {
		super.onCreate();
        log("onCreate");
		mPhoneRecorder = PhoneRecorder.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();	
	    intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
	    intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
	    intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
	    intentFilter.addDataScheme("file");
	    registerReceiver(mBroadcastReceiver, intentFilter);

	    if( null != mPhoneRecorder ){
	    	mPhoneRecorder.setOnStateChangedListener( mPhoneRecorderStateListener );
	    }
	}

	public void onDestroy() {
        super.onDestroy();
        log("onDestroy");
        if( null != mPhoneRecorder ){
        	mPhoneRecorder.stopRecord(mMount);
        	mPhoneRecorder = null;
        }
        if( null != mBroadcastReceiver){
        	unregisterReceiver(mBroadcastReceiver);
        	mBroadcastReceiver = null;
        }
	}

	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
        log("onStart");
        if(null != mPhoneRecorder){
            mPhoneRecorder.startRecord();
        }
	}
	
    public void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    private int mPhoneRecorderStatus = PhoneRecorder.IDLE_STATE;
    private PhoneRecorder.OnStateChangedListener mPhoneRecorderStateListener = new PhoneRecorder.OnStateChangedListener(){
		public void onStateChanged(int state){
			int iPreviousStatus = PhoneRecorderServices.this.mPhoneRecorderStatus;
			PhoneRecorderServices.this.mPhoneRecorderStatus = state;
			if(( iPreviousStatus != state )){
	    		Intent broadcastIntent = new Intent(PHONE_VOICE_RECORD_STATE_CHANGE_MESSAGE);
	    		broadcastIntent.putExtra("state", state);
	    		sendBroadcast(broadcastIntent);
				if(null != mStateListener){
					try{
						log("onStateChanged");
						mStateListener.onStateChange( state );
	            	} catch (RemoteException e) {
	            		Log.e(LOG_TAG, "PhoneRecordService: call listener onStateChange failed", new IllegalStateException());
	                }
				}
			}
		}
		public void onError(int error){
		    if(null != mStateListener ){
                try{
                    log("onError");
                    mStateListener.onError(error);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "PhoneRecordService: call listener onError() failed", new IllegalStateException());
                }
		    }
    	}
    };
    
    private final IPhoneRecorder.Stub mBinder = new IPhoneRecorder.Stub(){
    	public void listen(IPhoneRecordStateListener callback){
    		log("listen");
    		if( null != callback ){
    			mStateListener = callback;
    		}
    	}
    	
    	public void remove(){
    		log("remove");
    		mStateListener = null;
    	}
    	
    	public void startRecord(){
    		log("startRecord");
    		if( null != mPhoneRecorder){
    			mPhoneRecorder.startRecord();
    		}
    	}
    	
    	public void stopRecord(){
    		log("stopRecord");
    		if( null != mPhoneRecorder){
    			mPhoneRecorder.stopRecord(mMount);
    		}
    		mPhoneRecorder = null;
    	}
    };
}
