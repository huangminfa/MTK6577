package com.android.phone;

import static com.android.phone.TimeConsumingPreferenceActivity.EXCEPTION_ERROR;
import static com.android.phone.TimeConsumingPreferenceActivity.FDN_FAIL;
import static com.android.phone.TimeConsumingPreferenceActivity.RESPONSE_ERROR;

import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;

import static com.android.phone.TimeConsumingPreferenceActivity.RESPONSE_ERROR;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

import com.android.internal.telephony.Phone;
import com.mediatek.xlog.Xlog;

/* Fion add start */
import com.android.internal.telephony.gemini.GeminiPhone;
import com.mediatek.featureoption.FeatureOption;
/* Fion add end */

public class CallWaitingCheckBoxPreference extends CheckBoxPreference {
    private static final String LOG_TAG = "Settings/CallWaitingCheckBoxPreference";
    private final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);

    private final MyHandler mHandler = new MyHandler();
    Phone phone;
    TimeConsumingPreferenceListener tcpListener;

/* Fion add start */
    public static final int DEFAULT_SIM = 2; /* 0: SIM1, 1: SIM2 */
    private int mSimId = DEFAULT_SIM;
/* Fion add end */
    
    private int mServiceClass = CommandsInterface.SERVICE_CLASS_VOICE;

    public CallWaitingCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        phone = PhoneApp.getPhone();
    }

    public CallWaitingCheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.checkBoxPreferenceStyle);
    }

    public CallWaitingCheckBoxPreference(Context context) {
        this(context, null);
    }

/* Fion add start */
    void init(TimeConsumingPreferenceListener listener, boolean skipReading, int simId) {
        Xlog.d(LOG_TAG,"init, simId = "+simId);
        tcpListener = listener;
        mSimId = simId;
        
        /*if (this.mServiceClass == CommandsInterface.SERVICE_CLASS_VIDEO) {
            return;	
        }*/

        if (!skipReading) {
/* Fion add start */
            if (CallSettings.isMultipleSim())
            {
            	if (mServiceClass == CommandsInterface.SERVICE_CLASS_VIDEO)
            	{
            		((GeminiPhone)phone).getVtCallWaitingGemini(mHandler.obtainMessage(MyHandler.MESSAGE_GET_CALL_WAITING,
	                        MyHandler.MESSAGE_GET_CALL_WAITING, MyHandler.MESSAGE_GET_CALL_WAITING), mSimId);
            	}else
            	{
	                ((GeminiPhone)phone).getCallWaitingGemini(mHandler.obtainMessage(MyHandler.MESSAGE_GET_CALL_WAITING,
	                        MyHandler.MESSAGE_GET_CALL_WAITING, MyHandler.MESSAGE_GET_CALL_WAITING), mSimId);
            	}
            }
            else
            {
            	if (mServiceClass == CommandsInterface.SERVICE_CLASS_VIDEO)
            	{
            		phone.getVtCallWaiting(mHandler.obtainMessage(MyHandler.MESSAGE_GET_CALL_WAITING,
		                    MyHandler.MESSAGE_GET_CALL_WAITING, MyHandler.MESSAGE_GET_CALL_WAITING));
            	}else
            	{
		            phone.getCallWaiting(mHandler.obtainMessage(MyHandler.MESSAGE_GET_CALL_WAITING,
		                    MyHandler.MESSAGE_GET_CALL_WAITING, MyHandler.MESSAGE_GET_CALL_WAITING));
            	}
            }
/* Fion add end */
            if (tcpListener != null) {
                tcpListener.onStarted(this, true);
            }
        }
    }
/* Fion add end */

    @Override
    protected void onClick() {
        super.onClick();
        boolean toState=isChecked();
		setChecked(!toState);
/* Fion add start */
            if (CallSettings.isMultipleSim())
            {
            	if (mServiceClass == CommandsInterface.SERVICE_CLASS_VIDEO)
            	{
            		((GeminiPhone)phone).setVtCallWaitingGemini(toState,
	                        mHandler.obtainMessage(MyHandler.MESSAGE_SET_CALL_WAITING), mSimId);
            	}else
            	{
	                ((GeminiPhone)phone).setCallWaitingGemini(toState,
	                        mHandler.obtainMessage(MyHandler.MESSAGE_SET_CALL_WAITING), mSimId);
            	}
            }
            else
            {
            	if (mServiceClass == CommandsInterface.SERVICE_CLASS_VIDEO)
            	{
            		phone.setVtCallWaiting(toState,
    	                    mHandler.obtainMessage(MyHandler.MESSAGE_SET_CALL_WAITING));
            	}else
            	{
	                phone.setCallWaiting(toState,
	                    mHandler.obtainMessage(MyHandler.MESSAGE_SET_CALL_WAITING));
            	}
            }
/* Fion add end */
        if (tcpListener != null) {
            tcpListener.onStarted(this, false);
        }
    }

    private class MyHandler extends Handler {
        private static final int MESSAGE_GET_CALL_WAITING = 0;
        private static final int MESSAGE_SET_CALL_WAITING = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GET_CALL_WAITING:
                    handleGetCallWaitingResponse(msg);
                    break;
                case MESSAGE_SET_CALL_WAITING:
                    handleSetCallWaitingResponse(msg);
                    break;
            }
        }

        private void handleGetCallWaitingResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;

            if (tcpListener != null) {
                if (msg.arg2 == MESSAGE_SET_CALL_WAITING) {
                    tcpListener.onFinished(CallWaitingCheckBoxPreference.this, false);
                } else {
                    tcpListener.onFinished(CallWaitingCheckBoxPreference.this, true);
                }
            }

            if (ar.exception != null) {
                if (DBG) {
                    Xlog.d(LOG_TAG, "handleGetCallWaitingResponse: ar.exception=" + ar.exception);
                }
                setEnabled(false);
                if (tcpListener != null) {
                    tcpListener.onException(CallWaitingCheckBoxPreference.this,
                            (CommandException)ar.exception);
                }
            } else if (ar.userObj instanceof Throwable) {
                if (tcpListener != null) tcpListener.onError(CallWaitingCheckBoxPreference.this, RESPONSE_ERROR);
            } else {
                if (DBG) Xlog.d(LOG_TAG, "handleGetCallWaitingResponse: CW state successfully queried.");
                int[] cwArray = (int[])ar.result;
                // If cwArray[0] is = 1, then cwArray[1] must follow,
                // with the TS 27.007 service class bit vector of services
                // for which call waiting is enabled.
                try {
                    setChecked(((cwArray[0] == 1) && ((cwArray[1] & 0x01) == 0x01)));
                } catch (ArrayIndexOutOfBoundsException e) {
                    Xlog.e(LOG_TAG, "handleGetCallWaitingResponse: improper result: err ="
                            + e.getMessage());
                }
            }
        }

        private void handleSetCallWaitingResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;

            if (ar.exception != null) {
                if (DBG) Xlog.d(LOG_TAG, "handleSetCallWaitingResponse: ar.exception=" + ar.exception);
                //setEnabled(false);
            }
            if (DBG) Xlog.d(LOG_TAG, "handleSetCallWaitingResponse: re get");

/* Fion add start */
            if (CallSettings.isMultipleSim())
            {
            	if (mServiceClass == CommandsInterface.SERVICE_CLASS_VIDEO)
            	{
            		((GeminiPhone)phone).getVtCallWaitingGemini(obtainMessage(MESSAGE_GET_CALL_WAITING,
                            MESSAGE_SET_CALL_WAITING, MESSAGE_SET_CALL_WAITING, ar.exception), mSimId);
            	}else
            	{
                    ((GeminiPhone)phone).getCallWaitingGemini(obtainMessage(MESSAGE_GET_CALL_WAITING,
                        MESSAGE_SET_CALL_WAITING, MESSAGE_SET_CALL_WAITING, ar.exception), mSimId);
            	}
            }
            else
            {
            	if (mServiceClass == CommandsInterface.SERVICE_CLASS_VIDEO)
            	{
            		phone.getVtCallWaiting(obtainMessage(MESSAGE_GET_CALL_WAITING,
    	                    MESSAGE_SET_CALL_WAITING, MESSAGE_SET_CALL_WAITING, ar.exception));
            	}else
            	{
	                phone.getCallWaiting(obtainMessage(MESSAGE_GET_CALL_WAITING,
	                    MESSAGE_SET_CALL_WAITING, MESSAGE_SET_CALL_WAITING, ar.exception));
            	}
        }
/* Fion add end */
        }
    }
    
    public void setServiceClass(int serviceClass)
    {
    	mServiceClass = serviceClass;
    }
}
