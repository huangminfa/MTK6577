package com.android.phone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.app.ActivityManagerNative;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.phone.PhoneUtils.CallerInfoToken;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.sip.SipPhone;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.CallStateException;
import com.mediatek.vt.VTManager;
import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.TelephonyProperties;
import android.os.SystemProperties;

public class VTCallUtils {
	
	private static final String LOG_TAG = "VTCallUtils";
    private static final boolean DBG = true;// (PhoneApp.DBG_LEVEL >= 2);
    private static final boolean VDBG = true;// (PhoneApp.DBG_LEVEL >= 2);
    
    /**
     * Video Call will control some resource, such as Camera, Media.
     * So Phone App will broadcast Intent to other APPs before acquire and after release the resource.
     * Intent action:
     * Before - "android.phone.extra.VT_CALL_START"
     * After - "android.phone.extra.VT_CALL_END"
     */
    public static final String VT_CALL_START = "android.phone.extra.VT_CALL_START";
    public static final String VT_CALL_END = "android.phone.extra.VT_CALL_END";

    // "chmod" is a command to change file permission, 6 is for User, 4 is for Group
    public static final String CHANGE_FILE_PERMISSION = "chmod 640 ";

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
	
    /**
     * for InCallScreen to update VT UI
     * In old code, there are more than 2 modes.
     * But change to 2 mode OPEN/CLOSE.
     * In fact, it can be handled by InCallScreen.mVTInCallCanvas's visible/invisible.
     * But we also use VTScreenMode, because if need more than 2 modes in future, we can add it easily. 
     */
	static enum VTScreenMode{
		VT_SCREEN_CLOSE,
		VT_SCREEN_OPEN
	}
	
	static void showVTIncomingCallUi() {
        if (DBG) log("showVTIncomingCallUi()...");
        
        VTSettingUtils.getInstance().updateVTEngineerModeValues();
        
        PhoneApp app = PhoneApp.getInstance();

        try {
            ActivityManagerNative.getDefault().closeSystemDialogs("call");
        } catch (RemoteException e) {
        }

        app.preventScreenOn(true);
        app.requestWakeState(PhoneApp.WakeState.FULL);

        if (DBG) log("- updating notification from showVTIncomingCall()...");
        // incoming call use voice call GUI, so use "true" as parameter
        app.displayCallScreen(true);
    }
	
	public static void checkVTFile()
	{
		if (DBG) log("start checkVTFile() ! ");
		if( !(new File( VTAdvancedSetting.getPicPathDefault() ).exists()) )
    	{
			if (DBG) log("checkVTFile() : the default pic file not exists , create it ! ");
    		
    		try {
    			Bitmap btp1 = BitmapFactory.decodeResource(PhoneApp.getInstance().getResources(), R.drawable.vt_incall_pic_qcif);
    			VTCallUtils.saveMyBitmap( VTAdvancedSetting.getPicPathDefault() , btp1 );
    			btp1.recycle();
    			if (DBG) log(" - Bitmap.isRecycled() : " + btp1.isRecycled() );
			} catch (IOException e) {
				e.printStackTrace();
			}
   		
    	}
    	
    	if( !(new File( VTAdvancedSetting.getPicPathUserselect() ).exists()) )
    	{
    		if (DBG) log("checkVTFile() : the default user select pic file not exists , create it ! ");
    		
    		try {
    			Bitmap btp2 = BitmapFactory.decodeResource(PhoneApp.getInstance().getResources(), R.drawable.vt_incall_pic_qcif);
        		VTCallUtils.saveMyBitmap( VTAdvancedSetting.getPicPathUserselect() , btp2 );
        		btp2.recycle();
        		if (DBG) log(" - Bitmap.isRecycled() : " + btp2.isRecycled() );
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	if( !(new File( VTAdvancedSetting.getPicPathDefault2() ).exists()) )
    	{
			if (DBG) log("checkVTFile() : the default pic2 file not exists , create it ! ");
    		
    		try {
    			Bitmap btp3 = BitmapFactory.decodeResource(PhoneApp.getInstance().getResources(), R.drawable.vt_incall_pic_qcif);
    			VTCallUtils.saveMyBitmap( VTAdvancedSetting.getPicPathDefault2() , btp3 );
    			btp3.recycle();
    			if (DBG) log(" - Bitmap.isRecycled() : " + btp3.isRecycled() );
			} catch (IOException e) {
				e.printStackTrace();
			}
   		
    	}
    	
    	if( !(new File( VTAdvancedSetting.getPicPathUserselect2() ).exists()) )
    	{
			if (DBG) log("checkVTFile() : the default user select pic2 file not exists , create it ! ");
    		
    		try {
    			Bitmap btp4 = BitmapFactory.decodeResource(PhoneApp.getInstance().getResources(), R.drawable.vt_incall_pic_qcif);
    			VTCallUtils.saveMyBitmap( VTAdvancedSetting.getPicPathUserselect2() , btp4 );
    			btp4.recycle();
    			if (DBG) log(" - Bitmap.isRecycled() : " + btp4.isRecycled() );
			} catch (IOException e) {
				e.printStackTrace();
			}
   		
    	}
    	
    	if (DBG) log("end checkVTFile() ! ");
	}

    public static void saveMyBitmap(String bitName, Bitmap bitmap) throws IOException {
        if (DBG) log("saveMyBitmap()...");

        File file = new File(bitName);
        file.createNewFile();
        FileOutputStream fOut = null;

        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (DBG) log("Change file visit right for mediaserver process");
            // Mediaserver process can only visit the file with group permission,
            // So we change here, or else, hide me function will not work
            String command = CHANGE_FILE_PERMISSION + file.getAbsolutePath();
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
            if (DBG) log("exception happens when change file permission");
        }

        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * VT needs special timing method for different number:
	 * In MT call, the timing method is the same as voice call:
	 * starting timing when the call state turn to "ACTIVE".
	 * In MO call, because of the multimedia ringtone, the timing method is different 
	 * which we starting timing when receive the Message - VTManager.VT_MSG_START_COUNTER.
	 * Because when the multimedia ringtone is playing, the call state is already "ACTIVE",
	 * but then we are connecting with the multimedia ringtone server but not the number we dialing.
	 * So we must wait to start timing when connected with the number we dialing.
	 * The Message VTManager.VT_MSG_START_COUNTER is to tell us that we have connected with the number we dialing.
	 * But it is not to follow this method for all numbers in MO call.
	 * Some numbers don't need timing - vtNumbers_none [].
	 * Some numbers need timing with the voice call method - vtNumbers_default [].
	 * You can UPDATE the numbers in them here.
	 * 
	 */
	
	final static String vtNumbers_none [] = {"12531","+8612531"};
	final static String vtNumbers_default [] = {"12535","13800100011","+8612535","+8613800100011"};
	
	static enum VTTimingMode {
		VT_TIMING_NONE, /*VT_TIMING_SPECIAL,*/ VT_TIMING_DEFAULT
	}

	public static VTTimingMode checkVTTimingMode(String number) {
		if (DBG) log("checkVTTimingMode - number:" + number);

		ArrayList<String> mArrayList_none = new ArrayList<String>(Arrays.asList(vtNumbers_none));
		ArrayList<String> mArrayList_default = new ArrayList<String>(Arrays.asList(vtNumbers_default));

		if (mArrayList_none.indexOf(number) >= 0) {
			if (DBG)
				log("checkVTTimingMode - return:" + VTTimingMode.VT_TIMING_NONE);
			return VTTimingMode.VT_TIMING_NONE;
		}

		if (mArrayList_default.indexOf(number) >= 0) {
			if (DBG)
				log("checkVTTimingMode - return:"
						+ VTTimingMode.VT_TIMING_DEFAULT);
			return VTTimingMode.VT_TIMING_DEFAULT;
		}

		/*if (DBG) log("checkVTTimingMode - return:" + VTTimingMode.VT_TIMING_SPECIAL);*/
		//return VTTimingMode.VT_TIMING_SPECIAL;
		return VTTimingMode.VT_TIMING_DEFAULT;
	}
	
    public static int placeVTCall(Phone phone, String number, Uri contactRef, int simId) {
        int status = Constants.CALL_STATUS_DIALED;
        try {
            if (DBG) log("placeVTCall: '" + number + "'..." + "simId : "+simId);

            if(Phone.State.IDLE != PhoneApp.getInstance().mCM.getState()) {
                return Constants.CALL_STATUS_FAILED;
            }
            if (PhoneNumberUtils.isIdleSsString(number)) {
                if (DBG) log("the number for VT call is idle ss string");
                return Constants.CALL_STATUS_FAILED;
            }
            //In current stage, video call doesn't support uri number
            if (PhoneNumberUtils.isUriNumber(number) || phone instanceof SipPhone) {
                if (DBG) log("the number for VT call is idle uri string");
                return Constants.CALL_STATUS_FAILED;
            }
            
            VTInCallScreenFlags.getInstance().reset();
            checkVTFile();
            VTSettingUtils.getInstance().updateVTSettingState();
            VTSettingUtils.getInstance().updateVTEngineerModeValues();
            
            log("==> placeVTCall(): simId: " + simId);
            int nCSNetType = 0;// so,nCSNetType: 1-GSM, 2-GPRS
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (0 == simId) {
                    nCSNetType = SystemProperties.getInt(
                            TelephonyProperties.PROPERTY_CS_NETWORK_TYPE, -1);
                } else if (1 == simId) {
                    nCSNetType = SystemProperties.getInt(
                            TelephonyProperties.PROPERTY_CS_NETWORK_TYPE_2, -1);
                }
            } else {
                nCSNetType = SystemProperties.getInt(
                            TelephonyProperties.PROPERTY_CS_NETWORK_TYPE, -1);
            }
            log("==> placeVTCall(): nCSNetType: " + nCSNetType);
                        
            if ((1 == nCSNetType) || (2 == nCSNetType)) {
                status = Constants.CALL_STATUS_DROP_VOICECALL;
            }

            if(!VTInCallScreenFlags.getInstance().mVTInControlRes){
                PhoneApp.getInstance().sendBroadcast(new Intent(VTCallUtils.VT_CALL_START));
                VTInCallScreenFlags.getInstance().mVTInControlRes = true;
            }
            VTInCallScreenFlags.getInstance().mVTIsMT = false;
            VTInCallScreenFlags.getInstance().mVTPeerBigger = VTSettingUtils.getInstance().mPeerBigger;
            if (VDBG) log("- set VTManager open ! ");
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                VTManager.getInstance().setVTOpen(PhoneApp.getInstance().getBaseContext(), 
                                                  PhoneApp.getInstance().mCMGemini);
            }else{
                VTManager.getInstance().setVTOpen(PhoneApp.getInstance().getBaseContext(), 
                                                  PhoneApp.getInstance().mCM);
            }
            if (VDBG) log("- finish set VTManager open ! ");

            if(VTInCallScreenFlags.getInstance().mVTSurfaceChangedH 
                    && VTInCallScreenFlags.getInstance().mVTSurfaceChangedL) {
                if (VDBG) log("- set VTManager ready ! ");
                VTManager.getInstance().setVTReady(); 
                if (VDBG) log("- finish set VTManager ready ! ");
            } else {
                VTInCallScreenFlags.getInstance().mVTSettingReady = true;
            }
            
            PhoneUtils.placeCallRegister(phone);
            Connection cn;
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
                    cn = PhoneApp.getInstance().mCMGemini.vtDialGemini(phone, number, simId);
                } else {
                    cn = PhoneApp.getInstance().mCMGemini.vtDialGemini(phone, number, Phone.GEMINI_SIM_1);
                }
            } else {
                cn = PhoneApp.getInstance().mCM.vtDial(phone, number);
            }
            if (DBG) log("vtDial() returned: " + cn);
            if (cn == null) {
                if (phone.getPhoneType() == Phone.PHONE_TYPE_GSM) {
                    // On GSM phones, null is returned for MMI codes
                    if (DBG) log("dialed MMI code: " + number);
                    status = Constants.CALL_STATUS_DIALED_MMI;
                    PhoneUtils.setMMICommandToService(number);
                } else {
                    status = Constants.CALL_STATUS_FAILED;
                }
            } else {
                PhoneUtils.setAudioControlState(PhoneUtils.AUDIO_OFFHOOK);

                // phone.dial() succeeded: we're now in a normal phone call.
                // attach the URI to the CallerInfo Object if it is there,
                // otherwise just attach the Uri Reference.
                // if the uri does not have a "content" scheme, then we treat
                // it as if it does NOT have a unique reference.
                String content = phone.getContext().getContentResolver().SCHEME_CONTENT;
                if ((contactRef != null) && (contactRef.getScheme().equals(content))) {
                    Object userDataObject = cn.getUserData();
                    if (userDataObject == null) {
                        cn.setUserData(contactRef);
                    } else {
                        // TODO: This branch is dead code, we have
                        // just created the connection 'cn' which has
                        // no user data (null) by default.
                        if (userDataObject instanceof CallerInfo) {
                            ((CallerInfo) userDataObject).contactRefUri = contactRef;
                        } else {
                            ((PhoneUtils.CallerInfoToken) userDataObject).currentInfo.contactRefUri =
                                contactRef;
                        }
                    }
                }

                // Check is phone in any dock, and turn on speaker accordingly
                PhoneUtils.activateSpeakerIfDocked(phone);
            }
        } catch (CallStateException ex) {
            Log.w(LOG_TAG, "Exception from vtDial()", ex);
            status = Constants.CALL_STATUS_FAILED;
        }

        return status;
    }
    
    public static boolean isVTCall(Call call) {
        if (null == call) {
            return false;
        }
        if (null == call.getLatestConnection()) {
            return false;
        }
        return call.getLatestConnection().isVideo();
    }
    
    /*public VTCallUtils.VTTimingMode getVTTimingMode() {
        if (true != FeatureOption.MTK_VT3G324M_SUPPORT)
            return VTCallUtils.VTTimingMode.VT_TIMING_DEFAULT;
        if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
                if (((GeminiPhone) phone).getForegroundCall().getLatestConnection().isVideo()
                        && !((GeminiPhone) phone).getForegroundCall().getLatestConnection().isIncoming()) {
                    return VTCallUtils.checkVTTimingMode(((GeminiPhone) phone)
                            .getForegroundCall().getLatestConnection()
                            .getAddress());
                }
            }
        } else {
            if (Call.State.ACTIVE == phone.getForegroundCall().getState()) {
                if (phone.getForegroundCall().getLatestConnection().isVideo()
                        && !phone.getForegroundCall().getLatestConnection()
                                .isIncoming()) {
                    return VTCallUtils.checkVTTimingMode(phone
                            .getForegroundCall().getLatestConnection()
                            .getAddress());
                }
            }
        }
        return VTCallUtils.VTTimingMode.VT_TIMING_DEFAULT;
    }*/
}