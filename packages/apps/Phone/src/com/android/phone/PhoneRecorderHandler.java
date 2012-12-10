package com.android.phone;

import com.android.phone.PhoneFeatureConstants.FeatureOption;
import com.mediatek.vt.VTManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class PhoneRecorderHandler {

    private static final String LOG_TAG = "PhoneRecorderHandler";
    private static final boolean DBG = true;
    private static final boolean VDBG = true;
    
    private Intent mRecorderServiceIntent = new Intent(PhoneApp.getInstance(), 
                                                       PhoneRecorderServices.class);
    private IPhoneRecorder mPhoneRecorder = null;
    private int mPhoneRecorderState = PhoneRecorder.IDLE_STATE;
    private int mCustomValue = 0;
    private int mRecordType = 0;
    private Listener mListener;

    public interface Listener {
        void requestUpdateRecordState(final int state, final int customValue);
        void onStorageFull();
    }
    
    private PhoneRecorderHandler() {}
    
    private static PhoneRecorderHandler mInstance = new PhoneRecorderHandler();
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };
    
    private Runnable mRecordDiskCheck = new Runnable() {
        public void run(){
            checkRecordDisk();
        }
    };
    
    public static PhoneRecorderHandler getInstance() {
        return mInstance;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void clearListener(Listener listener) {
        if (listener == mListener) {
            mListener = null;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mPhoneRecorder = IPhoneRecorder.Stub.asInterface(service);
            try{
                log("onServiceConnected");
                if( null != mPhoneRecorder ){
                    mPhoneRecorder.listen(mPhoneRecordStateListener);
                    mPhoneRecorder.startRecord();
                    mHandler.postDelayed(mRecordDiskCheck, 500);
                }
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "onServiceConnected: couldn't register to record service", new IllegalStateException());
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mPhoneRecorder = null;
        }
    };
    
    private IPhoneRecordStateListener mPhoneRecordStateListener = new IPhoneRecordStateListener.Stub() {
        public void onStateChange(int state){
            log("onStateChange, state is " + state);
            mPhoneRecorderState = state;
            if (null != mListener) {
                mListener.requestUpdateRecordState(state, mCustomValue);
            }
        }

        public void onError(int iError) {
            String message = null;
            switch (iError) {
                case Recorder.SDCARD_ACCESS_ERROR:
                    message = PhoneApp.getInstance().getResources().getString(R.string.error_sdcard_access);
                    break;
                case Recorder.INTERNAL_ERROR:
                    message = PhoneApp.getInstance().getResources().getString(R.string.alert_device_error);
                    break;
            }
            if (null != mPhoneRecorder) {
                Toast.makeText(PhoneApp.getInstance(), message, Toast.LENGTH_LONG).show();
            }
        }
    };
    
    public void startVoiceRecord(final int customValue) {
        mCustomValue = customValue;
        mRecordType = Constants.PHONE_RECORDING_TYPE_ONLY_VOICE;
        if (null != mRecorderServiceIntent && null == mPhoneRecorder) {
            PhoneApp.getInstance().bindService(mRecorderServiceIntent, mConnection, 
                                               Context.BIND_AUTO_CREATE);
        } else if (null != mRecorderServiceIntent && null != mPhoneRecorder) {
            try{
                mPhoneRecorder.startRecord();
                mHandler.postDelayed(mRecordDiskCheck, 500);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "start Record failed", new IllegalStateException());
            }
        }
    }
    
    public void stopVoiceRecord(){
        try{
            log("stopRecord");
            if (null != mPhoneRecorder) {
                mPhoneRecorder.stopRecord();
                mPhoneRecorder.remove();
                if( null != mConnection ){
                    PhoneApp.getInstance().unbindService(mConnection);
                }
                mPhoneRecorder = null;
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "stopRecord: couldn't call to record service", new IllegalStateException());
        }
    }
    
    public void startVideoRecord(final int type, final long sdMaxSize, final int customValue) {
        mRecordType = type;
        mCustomValue = customValue;
        log("- start call VTManager.startRecording() : type = " + type + " sd max size = " + sdMaxSize);
        VTManager.getInstance().startRecording(type, sdMaxSize);
        log("- end call VTManager.startRecording()");
        mPhoneRecorderState = PhoneRecorder.RECORDING_STATE;
        if(null != mListener){
            mListener.requestUpdateRecordState(mPhoneRecorderState, mCustomValue);
        }
        mHandler.postDelayed(mRecordDiskCheck, 500);
    }
    
    public void stopVideoRecord() {
        log("- start call VTManager.stopRecording() : " + mRecordType);
        VTManager.getInstance().stopRecording(mRecordType);
        log("- end call VTManager.stopRecording() : " + mRecordType);
        mPhoneRecorderState = PhoneRecorder.IDLE_STATE;
        if(null != mListener){
            mListener.requestUpdateRecordState(mPhoneRecorderState, mCustomValue);
        }
    }
    
    public int getPhoneRecorderState(){
        return mPhoneRecorderState;
    }
    
    public void setPhoneRecorderState(final int state){
        mPhoneRecorderState = state;
    }
    
    public int getCustomValue() {
        return mCustomValue;
    }
    
    public void setCustomValue(final int customValue){
        mCustomValue = customValue;
    }
    
    public int getRecordType() {
        return mRecordType;
    }
    
    public void setRecordType(final int recordType) {
        mRecordType = recordType;
    }
    
    public boolean isVTRecording() {
        return Constants.PHONE_RECORDING_VIDEO_CALL_CUSTOM_VALUE == mCustomValue &&
               PhoneRecorder.RECORDING_STATE == mPhoneRecorderState;
    }
    
    private void checkRecordDisk() {
        if (!PhoneUtils.diskSpaceAvailable(Constants.PHONE_RECORD_LOW_STORAGE_THRESHOLD)) {
            Log.e("AN: ", "Checking result, disk is full, stop recording...");
            if (PhoneRecorder.isRecording() || isVTRecording()) {
                if (PhoneRecorder.isRecording()) {
                    stopVoiceRecord();
                } else if (isVTRecording()) {
                    stopVideoRecord();
                }
                if (null != mListener) {
                    mListener.onStorageFull();
                }
            }
        } else {
            mHandler.postDelayed(mRecordDiskCheck, 50);
        }
    }
    
    private void log(String msg) {
        PhoneLog.d(LOG_TAG, msg);
    }
}
