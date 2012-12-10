
package com.android.phone;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.gemini.MTKCallManager;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.CallStateException;
import com.mediatek.vt.VTManager;
import com.android.internal.telephony.Connection;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.telephony.PhoneNumberUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.vt.VTManager;
import com.mediatek.vt.VTManager.State;

import com.android.internal.telephony.Phone;

import android.view.View.OnTouchListener;
import android.util.FloatMath;
import android.view.MotionEvent;

public class VTInCallScreen extends RelativeLayout implements IVTInCallScreen, SurfaceHolder.Callback,
        View.OnClickListener, PopupMenu.OnMenuItemClickListener, PopupMenu.OnDismissListener {

    private static final String LOG_TAG = "VTInCallScreen";
    private static final boolean DBG = true;
    private static final boolean VDBG = true;
    
    private final static int WAITING_TIME_FOR_ASK_VT_SHOW_ME = 5;
    
    private static final int DELAYED_CLEANUP_AFTER_DISCONNECT = 146;
    private static final int DELAYED_CLEANUP_AFTER_DISCONNECT2 = 147;
    
    private static final int VT_PEER_SNAPSHOT_OK = 126;
    private static final int VT_PEER_SNAPSHOT_FAIL = 127;
    
    private static final int VT_MEDIA_RECORDER_NO_I_FRAME = 0x7FFF;
    private static final int VT_MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED = 801;
    private static final int VT_MEDIA_RECORDER_ERROR_UNKNOWN = 1;
    private static final int VT_MEDIA_OCCUPIED = 1;
    private static final int VT_MEDIA_ERROR_VIDEO_FAIL = 1;

    // GUI related
    private ViewGroup mVTInCallCanvas;
    private SurfaceView mVTHighVideo;
    private SurfaceView mVTLowVideo;
    private CompoundButton mVTMute;
    private CompoundButton mVTAudio;
    private CompoundButton mVTDialpad;
    private CompoundButton mVTSwapVideo;
    private ImageButton mVTOverflowMenu;
    private ImageButton mVTHangUp;
    private ImageButton mVTHighUp;
    private ImageButton mVTHighDown;
    private ImageButton mVTLowUp;
    private ImageButton mVTLowDown;
    private PopupMenu mVTPopupMenu;
    private VTCallUtils.VTScreenMode mVTScreenMode = VTCallUtils.VTScreenMode.VT_SCREEN_CLOSE;
    //private TextView mVTPhoneNumber;
    private ImageView mVTVoiceRecordingIcon;
    private CallBanner mCallBanner;
    private LinearLayout mVTHangUpWrapper;
    private SurfaceHolder mLowVideoHolder;
    private SurfaceHolder mHighVideoHolder;
    private AlertDialog mInCallVideoSettingDialog = null;
    private AlertDialog mInCallVideoSettingLocalEffectDialog = null;
    private AlertDialog mInCallVideoSettingLocalNightmodeDialog = null;
    private AlertDialog mInCallVideoSettingPeerQualityDialog = null;
    private AlertDialog mVTMTAsker = null;
    private AlertDialog mVTVoiceReCallDialog = null;
    private AlertDialog mVTRecorderSelector = null;
    
    ArrayList<String> mVTRecorderEntries = null;
    
    // "Audio mode" PopupMenu
    private PopupMenu mAudioModePopup;
    private boolean mAudioModePopupVisible = false;
    
    private PowerManager mVTPowerManager;
    private PowerManager.WakeLock mVTWakeLock;
    // private String mInVoiceAnswerVideoCallNumber = null;
    private VTBackgroundBitmapHandler mBkgBitmapHandler = null;

    private CallBannerController mVTCallBannerController;
    private InCallScreen mInCallScreen;
    private CallManager mCM;
    private MTKCallManager mCMGemini;
    private DTMFTwelveKeyDialerProxy mDialer;
    
    private OnTouchListener mTouchListener;
    private boolean mLocaleChanged = false;

    public VTInCallScreen(Context context) {
        super(context);

        if (DBG)
            log("VTInCallScreen constructor...");
        if (DBG)
            log("- this = " + this);

        // // Inflate the contents of this VTInCallScreen
        // LayoutInflater inflater = LayoutInflater.from(context);
        // inflater.inflate(R.layout.vt_incall_screen, // resource
        // this, // root
        // true);
        //mContext = context;
        mCM = PhoneApp.getInstance().mCM;
        mCMGemini = PhoneApp.getInstance().mCMGemini;
    }

    public VTInCallScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        //mContext = context;
        mCM = PhoneApp.getInstance().mCM;
        mCMGemini = PhoneApp.getInstance().mCMGemini;
    }
    
    public void onDestroy() {
        unregisterForVTPhoneStates();
        if (null == mVTWakeLock) {
            return;
        }
        try {
            if (mVTWakeLock.isHeld())
                mVTWakeLock.release();
        } catch (Exception ex) {
            if (DBG) log("onDestroy() : mVTWakeLock.release() unsuccessfully , exception !");
        }
    }

    public VTInCallScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //mContext = context;
        mCM = PhoneApp.getInstance().mCM;
        mCMGemini = PhoneApp.getInstance().mCMGemini;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            // if (mIsDestroyed) {
            // if (DBG) log("Handler: ignoring message " + msg +
            // "; we're destroyed!");
            // return;
            // }
        	if (DBG) log("VTInCallScreen Handler message:" + msg);

            switch (msg.what) {

                case VTManager.VT_MSG_DISCONNECTED:
                    if (DBG)
                        log("- handler : VT_MSG_DISCONNECTED ! ");
                    VTInCallScreenFlags.getInstance().mVTVideoConnected = false;
                    VTInCallScreenFlags.getInstance().mVTHasReceiveFirstFrame = false;
                    updateVTScreen(getVTScreenMode());
                    break;

                case VTManager.VT_MSG_CONNECTED:
                    if (DBG)
                        log("- handler : VT_MSG_CONNECTED ! ");
                    VTInCallScreenFlags.getInstance().mVTVideoConnected = true;
                    updateVTScreen(getVTScreenMode());
                    break;

                case VTManager.VT_MSG_START_COUNTER:
                    if (DBG) log("- handler : VT_MSG_START_COUNTER ! ");
                    onReceiveVTManagerStartCounter();
                    break;

                case VTManager.VT_MSG_CLOSE:
                case VTManager.VT_MSG_OPEN:
                    VTInCallScreenFlags.getInstance().mVTVideoReady = false;
                    updateVTScreen(getVTScreenMode());
                    break;

                case VTManager.VT_MSG_READY:
                    if (DBG) log("- handler : VT_MSG_READY ! ");
                    onReceiveVTManagerReady();
                    break;

                case VTManager.VT_MSG_EM_INDICATION:
                    if (DBG)
                        log("- handler : VT_MSG_EM_INDICATION ! ");
                    showToast((String) msg.obj);
                    break;

                case VT_PEER_SNAPSHOT_OK:
                    if (DBG)
                        log("- handler : VT_PEER_SNAPSHOT_OK ! ");
                    showToast(getResources().getString(R.string.vt_pic_saved_to_sd));
                    break;

                case VT_PEER_SNAPSHOT_FAIL:
                    if (DBG)
                        log("- handler : VT_PEER_SNAPSHOT_FAIL ! ");
                    showToast(getResources().getString(R.string.vt_pic_saved_to_sd_fail));
                    break;

                case VTManager.VT_ERROR_CALL_DISCONNECT:
                    if (DBG)
                        log("- handler : VT_ERROR_CALL_DISCONNECT ! ");
                    if ((!VTInCallScreenFlags.getInstance().mVTInEndingCall)
                            && (mCM.getState() != Phone.State.IDLE)) {
                        showToast(getResources().getString(R.string.vt_error_network));
                        log("toast is shown, string is "
                                + getResources().getString(R.string.vt_error_network));
                        VTInCallScreenFlags.getInstance().mVTInEndingCall = true;
                    }
                    if (null != mCM.getActiveFgCall()) {
                        if (DBG)
                            log("- handler : (VT_ERROR_CALL_DISCONNECT) - ForegroundCall exists, so hangup it ! ");
                        try {
                            mCM.hangupActiveCall(mCM.getActiveFgCall());
                        } catch (Exception e) {
                            if (DBG)
                                log("- handler : (VT_ERROR_CALL_DISCONNECT) - Exception ! ");
                        }
                    }
                    break;

                case VTManager.VT_NORMAL_END_SESSION_COMMAND:
                    if (DBG) log("- handler : VT_NORMAL_END_SESSION_COMMAND ! ");
                	if ((!VTInCallScreenFlags.getInstance().mVTInEndingCall)
                			&& (mCM.getState() != Phone.State.IDLE)) {
                		VTInCallScreenFlags.getInstance().mVTInEndingCall = true;
                	}
                	if (null != mCM.getActiveFgCall()){
                		if (DBG) log("- handler : (VT_NORMAL_END_SESSION_COMMAND) - ForegroundCall exists, so hangup it ! ");
                		try{
                			mCM.hangupActiveCall(mCM.getActiveFgCall());
                		}catch(Exception e){
                			if (DBG) log("- handler : (VT_NORMAL_END_SESSION_COMMAND) - Exception ! ");
                		}
                	}
                    break;

                case VTManager.VT_ERROR_START_VTS_FAIL:
                    if (DBG)
                        log("- handler : VT_ERROR_START_VTS_FAIL ! ");
                    if ((!VTInCallScreenFlags.getInstance().mVTInEndingCall)
                            && (mCM.getState() != Phone.State.IDLE)) {
                        if(VT_MEDIA_ERROR_VIDEO_FAIL == msg.arg2){
                            showToast(getResources().getString(R.string.vt_media_video_fail));
                        }else{
                            showToast(getResources().getString(R.string.vt_error_media));
                        }
                        VTInCallScreenFlags.getInstance().mVTInEndingCall = true;
                    }
                    // because we cannot init the VTManager successfully
                    // we have to hangup all video call now
                    mInCallScreen.internalHangupAll();
                    break;

                case VTManager.VT_ERROR_CAMERA:
                    if (DBG)
                        log("- handler : VT_ERROR_CAMERA ! ");
                    if ((!VTInCallScreenFlags.getInstance().mVTInEndingCall)
                            && (mCM.getState() != Phone.State.IDLE)) {
                         if(VT_MEDIA_OCCUPIED == msg.arg2){
                            showToast(getResources().getString(R.string.vt_media_occupied));
                         }else{
                            showToast(getResources().getString(R.string.vt_error_media));
                         }
                        VTInCallScreenFlags.getInstance().mVTInEndingCall = true;
                    }
                    if (null != mCM.getActiveFgCall()) {
                        if (DBG)
                            log("- handler : (VT_ERROR_CAMERA) - ForegroundCall exists, so hangup it ! ");
                        try {
                            //mCM.hangupActiveCall(mCM.getActiveFgCall());
                            mInCallScreen.internalHangupAll();
                        } catch (Exception e) {
                            if (DBG)
                                log("- handler : (VT_ERROR_CAMERA) - Exception ! ");
                        }
                    }
                    break;

                case VTManager.VT_ERROR_MEDIA_SERVER_DIED:
                    if (DBG)
                        log("- handler : VT_ERROR_MEDIA_SERVER_DIED ! ");
                    if ((!VTInCallScreenFlags.getInstance().mVTInEndingCall)
                            && (mCM.getState() != Phone.State.IDLE)) {
                        showToast(getResources().getString(R.string.vt_error_media));
                        VTInCallScreenFlags.getInstance().mVTInEndingCall = true;
                    }
                    if (null != mCM.getActiveFgCall()) {
                        if (DBG)
                            log("- handler : (VT_ERROR_MEDIA_SERVER_DIED) - ForegroundCall exists, so hangup it ! ");
                        try {
                            mCM.hangupActiveCall(mCM.getActiveFgCall());
                        } catch (Exception e) {
                            if (DBG)
                                log("- handler : (VT_ERROR_MEDIA_SERVER_DIED) - Exception ! ");
                        }
                    }
                    break;

                case VTManager.VT_MSG_RECEIVE_FIRSTFRAME:
                    if (DBG)
                        log("- handler : VT_MSG_RECEIVE_FIRSTFRAME ! ");
                    onVTReceiveFirstFrame();
                    updateVTScreen(getVTScreenMode());
                    break;

                case VTManager.VT_ERROR_MEDIA_RECORDER_EVENT_INFO:
                    if(VT_MEDIA_RECORDER_NO_I_FRAME == msg.arg1){
                        showToast(getResources().getString(R.string.vt_recorder_only_voice));
                    }else if(VT_MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED == msg.arg1){
                        stopRecord();
                        mInCallScreen.handleStorageFull(false); // false for recording case
                    }
                    break;

                case VTManager.VT_ERROR_MEDIA_RECORDER_EVENT_ERROR:
                    if(VT_MEDIA_RECORDER_ERROR_UNKNOWN == msg.arg1){
                        showToast(getResources().getString(R.string.vt_recording_error));
                        stopRecord();
                    }
                    break;

                case VTManager.VT_ERROR_MEDIA_RECORDER_COMPLETE:
                    int OK = 0;
                    if( OK == msg.arg1){
                        if (DBG) log("- handler : VT_ERROR_MEDIA_RECORDER_COMPLETE, arg is OK ");
                        showToast(getResources().getString(R.string.vt_recording_saved));
                    } else {
                        if (DBG) log("- handler : VT_ERROR_MEDIA_RECORDER_COMPLETE, arg is not OK ");
                        showToast(getResources().getString(R.string.vt_recording_saved_fail));
                    }
                    break;

                case VTManager.VT_MSG_PEER_CAMERA_OPEN:
                    if (DBG) log("- handler : VT_MSG_PEER_CAMERA_OPEN ! ");
                    showToast(getResources().getString(R.string.vt_peer_camera_open));
                    break;

                case VTManager.VT_MSG_PEER_CAMERA_CLOSE:
                    if (DBG) log("- handler : VT_MSG_PEER_CAMERA_CLOSE ! ");
                    showToast(getResources().getString(R.string.vt_peer_camera_close));
                    break;

                default:
                    Log.wtf(LOG_TAG, "mHandler: unexpected message: " + msg);
                    break;
            }
        }
    };

    public void updateVTScreen(VTCallUtils.VTScreenMode mode) {
        if (DBG) log("updateVTScreen : " + mode);

        if (mode != VTCallUtils.VTScreenMode.VT_SCREEN_OPEN)
            return;

        if (mCM.getFirstActiveRingingCall().getState() == Call.State.INCOMING
                && !mCM.getActiveFgCall().getState().isDialing()) {
            mVTCallBannerController.updateState(mCM.getFirstActiveRingingCall());
        } else if (mCM.getActiveFgCall().getState() != Call.State.IDLE){
            mVTCallBannerController.updateState(mCM.getActiveFgCall());
        } else {
            mVTCallBannerController.updateState(null);
        }

        mInCallScreen.updateCallTime();

        //mVTHighVideo.setClickable(true);
        mVTLowVideo.setClickable(true);
        mVTHangUp.setEnabled(true);

        if (DBG)
            log("updateVTScreen : VTInCallScreenFlags.getInstance().mVTHideMeNow - "
                    + VTInCallScreenFlags.getInstance().mVTHideMeNow);
        if (DBG)
            log("updateVTScreen : VTSettingUtils.getInstance().mEnableBackCamera - "
                    + VTSettingUtils.getInstance().mEnableBackCamera);

        if (!PhoneApp.getInstance().isVTActive()) {
            VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting = false;
            VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting = false;
            VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting = false;
            hideLocalZoomOrBrightness();
            dismissVideoSettingDialogs();
            if (VTCallUtils.VTScreenMode.VT_SCREEN_OPEN == getVTScreenMode()) {
                mInCallScreen.closeOptionsMenu();
            }
        }

        if (VTInCallScreenFlags.getInstance().mVTHideMeNow) {
            VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting = false;
            VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting = false;
            VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting = false;
            hideLocalZoomOrBrightness();
        }

        updateVTInCallButtons();
  
        if (PhoneUtils.isDMLocked()) {
            mVTDialpad.setEnabled(false);
            mVTAudio.setEnabled(false);
            mVTOverflowMenu.setEnabled(false);
            mVTSwapVideo.setEnabled(false);
            hideLocalZoomOrBrightness();
        }

        if (DBG) log("updateVTScreen end");
    }

    private void updateVTInCallButtons() {
        if (DBG) log("updateVTInCallButtons()...");
        
        if (VTInCallScreenFlags.getInstance().mVTInEndingCall || PhoneApp.getInstance().isVTIdle()) {
            mVTAudio.setEnabled(false);
            mVTMute.setEnabled(false);
            mVTOverflowMenu.setEnabled(false);
            mVTSwapVideo.setEnabled(false);
        } else {
            mVTAudio.setEnabled(true);
            mVTMute.setEnabled(PhoneApp.getInstance().isVTActive());
            mVTOverflowMenu.setEnabled(true);
            mVTSwapVideo.setEnabled(VTInCallScreenFlags.getInstance().mVTHasReceiveFirstFrame);
        }
        
        if (DBG) log("updateVTInCallButtons() : update mVTMute 's src !");
        mVTMute.setChecked(PhoneUtils.getMute());
        
        if (DBG) log("updateVTInCallButtons() : update mVTSwapVideo 's src !");
        mVTSwapVideo.setChecked(!VTInCallScreenFlags.getInstance().mVTPeerBigger);

        InCallControlState inCallControlState = mInCallScreen.getUpdatedInCallControlState();
        
        if (DBG) log("updateVTInCallButtons() : update mVTDialpad 's src !");
        mVTDialpad.setEnabled(inCallControlState.dialpadEnabled);
        mVTDialpad.setChecked(inCallControlState.dialpadVisible);

        if (DBG) log("updateVTInCallButtons() : update mVTAudio 's src !");
        updateAudioButton(inCallControlState);
        // update audio mode popup
        final Phone.State state = mCM.getState();  // IDLE, RINGING, or OFFHOOK
        final Call.State fgCallState = mCM.getActiveFgCallState();
        if ((state == Phone.State.OFFHOOK)
                && (fgCallState == Call.State.ALERTING || fgCallState == Call.State.ACTIVE)) {
            // The audio mode popup is allowed to be visible in this state.
            // So if it's up, leave it alone.
        } else {
            // The Audio mode popup isn't relevant in this state, so make sure
            // it's not visible.
            dismissAudioModePopup();  // safe even if not active
        }
    }

    void initVTInCallScreen() {
        if (DBG)
            log("initVTInCallCanvas()...");

        mVTPowerManager = (PowerManager) mInCallScreen.getSystemService(Context.POWER_SERVICE);
        mVTWakeLock = mVTPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                                                  PowerManager.ON_AFTER_RELEASE,
                                                  "VTWakeLock");
        mCallBanner = (CallBanner) findViewById(R.id.callBanner);
        mVTCallBannerController = new CallBannerController(mCallBanner, mInCallScreen);

        mVTInCallCanvas = (ViewGroup) findViewById(R.id.VTInCallCanvas);
        mVTInCallCanvas.setVisibility(View.GONE);
        mVTInCallCanvas.setClickable(false);

        mVTHighUp = (ImageButton) findViewById(R.id.VTHighUp);
        mVTHighUp.setBackgroundColor(0);
        mVTHighUp.setOnClickListener(this);
        mVTHighUp.setVisibility(View.GONE);

        mVTHighDown = (ImageButton) findViewById(R.id.VTHighDown);
        mVTHighDown.setBackgroundColor(0);
        mVTHighDown.setOnClickListener(this);
        mVTHighDown.setVisibility(View.GONE);

        mVTLowUp = (ImageButton) findViewById(R.id.VTLowUp);
        mVTLowUp.setBackgroundColor(0);
        mVTLowUp.setOnClickListener(this);
        mVTLowUp.setVisibility(View.GONE);

        mVTLowDown = (ImageButton) findViewById(R.id.VTLowDown);
        mVTLowDown.setBackgroundColor(0);
        mVTLowDown.setOnClickListener(this);
        mVTLowDown.setVisibility(View.GONE);

        mVTHighVideo = (SurfaceView) findViewById(R.id.VTHighVideo);
        mVTHighVideo.setFocusable(false);
        mVTHighVideo.setFocusableInTouchMode(false);

        mVTLowVideo = (SurfaceView) findViewById(R.id.VTLowVideo);
        mVTLowVideo.setFocusable(false);
        mVTLowVideo.setFocusableInTouchMode(false);

        mVTMute = (CompoundButton) findViewById(R.id.VTMute);
        mVTMute.setFocusable(true);
        mVTMute.setFocusableInTouchMode(false);
        //mVTMute.setImageResource(R.drawable.vt_incall_button_mute);

        mVTAudio = (CompoundButton) findViewById(R.id.VTSpeaker);
        mVTAudio.setFocusable(true);
        mVTAudio.setFocusableInTouchMode(false);
        // Voice call's speaker button also show bluetooth and handset
        // For video call, we just use speaker now, so disable some layer here
        // In future, suggest indicating bluetooth state here
        // Constants used below with Drawable.setAlpha():
        final int HIDDEN = 0;
        final int VISIBLE = 255;
        LayerDrawable layers = (LayerDrawable) mVTAudio.getBackground();
        if (DBG) log("- 'layers' drawable: " + layers);
        layers.findDrawableByLayerId(R.id.compoundBackgroundItem).setAlpha(VISIBLE);
        layers.findDrawableByLayerId(R.id.moreIndicatorItem).setAlpha(HIDDEN);
        layers.findDrawableByLayerId(R.id.bluetoothItem).setAlpha(HIDDEN);
        layers.findDrawableByLayerId(R.id.handsetItem).setAlpha(HIDDEN);
        layers.findDrawableByLayerId(R.id.speakerphoneItem).setAlpha(VISIBLE);

        mVTDialpad = (CompoundButton) findViewById(R.id.VTDialpad);
        mVTDialpad.setFocusable(true);
        mVTDialpad.setFocusableInTouchMode(false);
        
        mVTSwapVideo = (CompoundButton) findViewById(R.id.VTSwapVideo);
        mVTSwapVideo.setFocusable(true);
        mVTSwapVideo.setFocusableInTouchMode(false);
        
        mVTOverflowMenu = (ImageButton) findViewById(R.id.VTOverflowMenu);
        mVTOverflowMenu.setFocusable(true);
        mVTOverflowMenu.setFocusableInTouchMode(false);

        mVTHangUp = (ImageButton) findViewById(R.id.VTHangUp);
        mVTHangUp.setFocusable(true);
        mVTHangUp.setFocusableInTouchMode(false);
        
        mVTHangUpWrapper = (LinearLayout) findViewById(R.id.VTHangUpWrapper);

        mVTMute.setOnClickListener(this);
        mVTAudio.setOnClickListener(this);
        mVTDialpad.setOnClickListener(this);
        mVTSwapVideo.setOnClickListener(this);
        mVTOverflowMenu.setOnClickListener(this);
        mVTHangUp.setOnClickListener(this);
        
        if (ViewConfiguration.get(mInCallScreen).hasPermanentMenuKey()) {
            mVTSwapVideo.setVisibility(View.VISIBLE);
            mVTOverflowMenu.setVisibility(View.INVISIBLE);
        } else {
            mVTSwapVideo.setVisibility(View.INVISIBLE);
            mVTOverflowMenu.setVisibility(View.VISIBLE);
        }

        mVTHighVideo.setOnClickListener(this);
        mVTLowVideo.setOnClickListener(this);

        if (FeatureOption.MTK_PHONE_VOICE_RECORDING) {
            mVTVoiceRecordingIcon = (ImageView) findViewById(R.id.VTVoiceRecording);
            mVTVoiceRecordingIcon.setFocusable(false);
            mVTVoiceRecordingIcon.setFocusableInTouchMode(false);
            mVTVoiceRecordingIcon.setBackgroundResource(R.drawable.voice_record);
            mVTVoiceRecordingIcon.setVisibility(View.GONE);
        }

        // set focus start
        mVTAudio.setNextFocusLeftId(R.id.VTSpeaker);
        mVTAudio.setNextFocusRightId(R.id.VTMute);
        mVTAudio.setNextFocusUpId(R.id.VTSpeaker);
        mVTAudio.setNextFocusDownId(R.id.VTDialpad);

        mVTMute.setNextFocusLeftId(R.id.VTSpeaker);
        mVTMute.setNextFocusRightId(R.id.VTMute);
        mVTMute.setNextFocusUpId(R.id.VTMute);
        if (ViewConfiguration.get(mInCallScreen).hasPermanentMenuKey()) {
            mVTMute.setNextFocusDownId(R.id.VTSwapVideo);
        } else {
            mVTMute.setNextFocusDownId(R.id.VTOverflowMenu);
        }

        mVTDialpad.setNextFocusLeftId(R.id.VTDialpad);
        if (ViewConfiguration.get(mInCallScreen).hasPermanentMenuKey()) {
            mVTDialpad.setNextFocusRightId(R.id.VTSwapVideo);
        } else {
            mVTDialpad.setNextFocusRightId(R.id.VTOverflowMenu);
        }
        mVTDialpad.setNextFocusUpId(R.id.VTSpeaker);
        mVTDialpad.setNextFocusDownId(R.id.VTHangUp);
        
        mVTSwapVideo.setNextFocusLeftId(R.id.VTDialpad);
        mVTSwapVideo.setNextFocusRightId(R.id.VTSwapVideo);
        mVTSwapVideo.setNextFocusUpId(R.id.VTMute);
        mVTSwapVideo.setNextFocusDownId(R.id.VTHangUp);
        
        mVTOverflowMenu.setNextFocusLeftId(R.id.VTDialpad);
        mVTOverflowMenu.setNextFocusRightId(R.id.VTOverflowMenu);
        mVTOverflowMenu.setNextFocusUpId(R.id.VTMute);
        mVTOverflowMenu.setNextFocusDownId(R.id.VTHangUp);
        
        mVTHangUp.setNextFocusLeftId(R.id.VTHangUp);
        mVTHangUp.setNextFocusRightId(R.id.VTHangUp);
        mVTHangUp.setNextFocusUpId(R.id.VTDialpad);
        mVTHangUp.setNextFocusDownId(R.id.VTHangUp);
        // set focus end

        mHighVideoHolder = mVTHighVideo.getHolder();
        mLowVideoHolder = mVTLowVideo.getHolder();

        mHighVideoHolder.addCallback(this);
        mLowVideoHolder.addCallback(this);

        mHighVideoHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mLowVideoHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mBkgBitmapHandler = new VTBackgroundBitmapHandler();
        
        mTouchListener = new OnTouchListener() {

            private static final int NONEPOINT = 0;
            private static final int DRAGPOINT = 1; // 1 point
            private static final int ZOOMPOINT = 2; // 2 point

            private int mMode = NONEPOINT;
            private float mOldDist;
            private final float mChangeThreshold = mInCallScreen.getResources().getDisplayMetrics().density * 20;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        log("MotionEvent.ACTION_DOWN");
                        hideLocalZoomOrBrightness();
                        VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting = false;
                        VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting = false;
                        VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting = false;
                        mMode = DRAGPOINT;
                        break;
                    case MotionEvent.ACTION_UP:
                        log("MotionEvent.ACTION_UP");
                    case MotionEvent.ACTION_POINTER_UP:
                        log("MotionEvent.ACTION_POINTER_UP");
                        mMode = NONEPOINT;
                        mOldDist = 0;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mOldDist = spacing(event);
                        mMode = ZOOMPOINT;
                        log("MotionEvent.ACTION_POINTER_DOWN, mOldDist is" + mOldDist);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        log("MotionEvent.ACTION_MOVE, mode is " + mMode);
                        if ((mMode == ZOOMPOINT)
                                && VTInCallScreenFlags.getInstance().mVTHasReceiveFirstFrame) {
                            // moving first point
                            float newDist = spacing(event);
                            log("MotionEvent.ACTION_MOVE, new dist is " + newDist + 
                                    ", old dist is " + mOldDist + " threshold is " + mChangeThreshold);
                            if ((newDist - mOldDist > mChangeThreshold)
                                    && (!VTInCallScreenFlags.getInstance().mVTFullScreen)) {
                                setVTDisplayScreenMode(true);
                                mMode = NONEPOINT;
                                mOldDist = 0;
                            } else if ((mOldDist - newDist > mChangeThreshold)
                                    && (VTInCallScreenFlags.getInstance().mVTFullScreen)) {
                                setVTDisplayScreenMode(false);
                                mMode = NONEPOINT;
                            }
                        }
                        break;
                    }
                return true;
            }

            /**
             * Compute two point distance
             */
            private float spacing(MotionEvent event) {
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return FloatMath.sqrt(x*x + y*y);
            }
        };

        // MTK_OP01_PROTECT_START
        if ("OP01".equals(PhoneUtils.getOptrProperties())) {
            mVTHighVideo.setOnTouchListener(mTouchListener);
            mVTInCallCanvas.setOnTouchListener(mTouchListener);
        }
        // MTK_OP01_PROTECT_END
    }

    public void openVTInCallCanvas() {
        if (DBG)
            log("openVTInCallCanvas!");
        if (null != mVTInCallCanvas) {
            mVTInCallCanvas.setClickable(true);
            mVTInCallCanvas.setVisibility(View.VISIBLE);
        }

        if (null != mVTHighVideo) {
            mVTHighVideo.setVisibility(View.VISIBLE);
        }

        if (null != mVTLowVideo) {
            mVTLowVideo.setVisibility(View.VISIBLE);
        }
    }

    public void closeVTInCallCanvas() {
        if (DBG)
            log("closeVTInCallCanvas!");
        if (null != mVTPopupMenu) {
            mVTPopupMenu.dismiss();
        }
        dismissAudioModePopup();
        if (null != mVTInCallCanvas) {
            mVTInCallCanvas.setClickable(false);
            mVTInCallCanvas.setVisibility(View.GONE);
        }

        if (null != mVTHighVideo) {
            mVTHighVideo.setVisibility(View.GONE);
        }
        if (null != mVTLowVideo) {
            mVTLowVideo.setVisibility(View.GONE);
        }
    }

    public void setVTScreenMode(VTCallUtils.VTScreenMode mode) {
        if (DBG)
            log("setVTScreenMode : " + mode);

        if (VTCallUtils.VTScreenMode.VT_SCREEN_OPEN != getVTScreenMode()
                && VTCallUtils.VTScreenMode.VT_SCREEN_OPEN == mode) {
            openVTInCallCanvas();
            if (DBG)
                log("setVTScreenMode : mVTWakeLock.acquire() ");
            try {
                if (!mVTWakeLock.isHeld())
                    mVTWakeLock.acquire();
            } catch (Exception ex) {
                if (DBG)
                    log("setVTScreenMode : mVTWakeLock.acquire() unsuccessfully , exception !");
            }
        }

        if (VTCallUtils.VTScreenMode.VT_SCREEN_OPEN == getVTScreenMode()
                && VTCallUtils.VTScreenMode.VT_SCREEN_OPEN != mode) {
            closeVTInCallCanvas();
            if (DBG)
                log("setVTScreenMode : mVTWakeLock.release() ");
            try {
                if (mVTWakeLock.isHeld())
                    mVTWakeLock.release();
            } catch (Exception ex) {
                if (DBG)
                    log("setVTScreenMode : mVTWakeLock.release() unsuccessfully , exception !");
            }
        }
        mVTScreenMode = mode;
    }

    // public void updateVTPhoneNumber(CharSequence text ) {
    // if(DBG)log("updateVTPhoneNumber : "+text);
    // mVTPhoneNumber.setText(text);
    // }

    public VTCallUtils.VTScreenMode getVTScreenMode() {
        if (DBG)
            log("getVTScreenMode : " + mVTScreenMode);
        return mVTScreenMode;
    }

    private void showVTLocalZoom() {
        if (DBG)
            log("showVTLocalZoom()...");

        if (!VTInCallScreenFlags.getInstance().mVTVideoReady)
            return;

        if (VTInCallScreenFlags.getInstance().mVTPeerBigger) {
            mVTLowUp.setImageResource(R.drawable.vt_incall_button_zoomup);
            mVTLowDown.setImageResource(R.drawable.vt_incall_button_zoomdown);
            mVTLowUp.setVisibility(View.VISIBLE);
            mVTLowDown.setVisibility(View.VISIBLE);
            mVTLowUp.setEnabled(VTManager.getInstance().canIncZoom());
            mVTLowDown.setEnabled(VTManager.getInstance().canDecZoom());
        } else {
            mVTHighUp.setImageResource(R.drawable.vt_incall_button_zoomup);
            mVTHighDown.setImageResource(R.drawable.vt_incall_button_zoomdown);
            mVTHighUp.setVisibility(View.VISIBLE);
            mVTHighDown.setVisibility(View.VISIBLE);
            mVTHighUp.setEnabled(VTManager.getInstance().canIncZoom());
            mVTHighDown.setEnabled(VTManager.getInstance().canDecZoom());
        }

        VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting = true;
        VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting = false;
        VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting = false;

    }

    private void showVTLocalBrightness() {
        if (DBG)
            log("showVTLocalBrightness()...");

        if (!VTInCallScreenFlags.getInstance().mVTVideoReady)
            return;

        if (VTInCallScreenFlags.getInstance().mVTPeerBigger) {
            mVTLowUp.setImageResource(R.drawable.vt_incall_button_brightnessup);
            mVTLowDown.setImageResource(R.drawable.vt_incall_button_brightnessdown);
            mVTLowUp.setVisibility(View.VISIBLE);
            mVTLowDown.setVisibility(View.VISIBLE);
            mVTLowUp.setEnabled(VTManager.getInstance().canIncBrightness());
            mVTLowDown.setEnabled(VTManager.getInstance().canDecBrightness());
        } else {
            mVTHighUp.setImageResource(R.drawable.vt_incall_button_brightnessup);
            mVTHighDown.setImageResource(R.drawable.vt_incall_button_brightnessdown);
            mVTHighUp.setVisibility(View.VISIBLE);
            mVTHighDown.setVisibility(View.VISIBLE);
            mVTHighUp.setEnabled(VTManager.getInstance().canIncBrightness());
            mVTHighDown.setEnabled(VTManager.getInstance().canDecBrightness());
        }

        VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting = false;
        VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting = true;
        VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting = false;

    }

    private void showVTLocalContrast() {
        if (DBG)
            log("showVTLocalContrast()...");

        if (!VTInCallScreenFlags.getInstance().mVTVideoReady)
            return;

        if (VTInCallScreenFlags.getInstance().mVTPeerBigger) {
            mVTLowUp.setImageResource(R.drawable.vt_incall_button_contrastup);
            mVTLowDown.setImageResource(R.drawable.vt_incall_button_contrastdown);
            mVTLowUp.setVisibility(View.VISIBLE);
            mVTLowDown.setVisibility(View.VISIBLE);
            mVTLowUp.setEnabled(VTManager.getInstance().canIncContrast());
            mVTLowDown.setEnabled(VTManager.getInstance().canDecContrast());
        } else {
            mVTHighUp.setImageResource(R.drawable.vt_incall_button_contrastup);
            mVTHighDown.setImageResource(R.drawable.vt_incall_button_contrastdown);
            mVTHighUp.setVisibility(View.VISIBLE);
            mVTHighDown.setVisibility(View.VISIBLE);
            mVTHighUp.setEnabled(VTManager.getInstance().canIncContrast());
            mVTHighDown.setEnabled(VTManager.getInstance().canDecContrast());
        }

        VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting = false;
        VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting = false;
        VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting = true;

    }

    // when we call this method, we will hide local zoom,brightness and contrast
    private void hideLocalZoomOrBrightness() {
        if (DBG)
            log("hideLocalZoomOrBrightness()...");

        mVTHighUp.setVisibility(View.GONE);
        mVTHighDown.setVisibility(View.GONE);
        mVTLowUp.setVisibility(View.GONE);
        mVTLowDown.setVisibility(View.GONE);
    }

    private void updateLocalZoomOrBrightness() {
        if (DBG)
            log("updateLocalZoomOrBrightness()...");

        if (VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting) {
            mVTLowUp.setEnabled(VTManager.getInstance().canIncZoom());
            mVTLowDown.setEnabled(VTManager.getInstance().canDecZoom());
        } else if (VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting) {
            mVTLowUp.setEnabled(VTManager.getInstance().canIncBrightness());
            mVTLowDown.setEnabled(VTManager.getInstance().canDecBrightness());
        } else if (VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting) {
            mVTHighUp.setEnabled(VTManager.getInstance().canIncContrast());
            mVTHighDown.setEnabled(VTManager.getInstance().canDecContrast());
        }
    }

    public void internalAnswerVTCallPre() {
        if (DBG)
            log("internalAnswerVTCallPre()...");

        if (PhoneApp.getInstance().isVTActive()) {
            closeVTManager();
            if (DBG)
                log("internalAnswerVTCallPre: set VTInCallScreenFlags.getInstance().mVTShouldCloseVTManager = false");
            VTInCallScreenFlags.getInstance().mVTShouldCloseVTManager = false;
            VTInCallScreenFlags.getInstance().resetPartial();
        }

        if (DBG) log("Incallscreen, before incomingVTCall");
        VTManager.getInstance().incomingVTCall(1);
        if (DBG) log("Incallscreen, after incomingVTCall");
        
        VTInCallScreenFlags.getInstance().reset();
        VTCallUtils.checkVTFile();

        if ((!PhoneApp.getInstance().isHeadsetPlugged()) && (!mInCallScreen.isBluetoothAvailable()))
            PhoneUtils.turnOnSpeaker(mInCallScreen, true, true, false);

        VTInCallScreenFlags.getInstance().mVTIsMT = true;

        VTSettingUtils.getInstance().updateVTSettingState();
        VTInCallScreenFlags.getInstance().mVTPeerBigger = VTSettingUtils.getInstance().mPeerBigger;

        if (null != mBkgBitmapHandler) {
            mBkgBitmapHandler.forceUpdateBitmapBySetting();
            if (null != mBkgBitmapHandler.getBitmap() 
                    && !VTInCallScreenFlags.getInstance().mVTHasReceiveFirstFrame) {
                updateVideoBkgDrawable();
            }
        }
        mVTCallBannerController.clearCallBannerInfo();

        setVTDisplayScreenMode(false);
        updateVTLocalPeerDisplay();

        if (!getVTInControlRes()) {
            mInCallScreen.sendBroadcast(new Intent(VTCallUtils.VT_CALL_START));
            setVTInControlRes(true);
        }

        if (null != mVTPopupMenu) {
            mVTPopupMenu.dismiss();
        }
        dismissAudioModePopup();

        setVTScreenMode(VTCallUtils.VTScreenMode.VT_SCREEN_OPEN);
        updateVTScreen(getVTScreenMode());

        registerForVTPhoneStates();
        
        if (VDBG) log("- set VTManager open ! ");
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            VTManager.getInstance().setVTOpen(PhoneApp.getInstance().getBaseContext(), mCMGemini);
        } else {
            VTManager.getInstance().setVTOpen(PhoneApp.getInstance().getBaseContext(), mCM);
        }
        if (VDBG) log("- finish set VTManager open ! ");

        if (!VTSettingUtils.getInstance().mShowLocalMT.equals("0"))
            onVTHideMeClick2();
        if (PhoneUtils.isDMLocked()) {
            if (VDBG)
                log("- Now DM locked, VTManager.getInstance().lockPeerVideo() start");
            VTManager.getInstance().lockPeerVideo();
            if (VDBG)
                log("- Now DM locked, VTManager.getInstance().lockPeerVideo() end");
        }

        if (VTInCallScreenFlags.getInstance().mVTSurfaceChangedH
                && VTInCallScreenFlags.getInstance().mVTSurfaceChangedL) {
            if (VDBG) log("- set VTManager ready ! ");
            VTManager.getInstance().setVTReady();
            if (VDBG) log("- finish set VTManager ready ! ");
        } else {
            VTInCallScreenFlags.getInstance().mVTSettingReady = true;
        }
    }

    private void onVTTakePeerPhotoClick() {
        if (DBG)
            log("onVTTakePeerPhotoClick()...");

        if (VTManager.getInstance().getState() != VTManager.State.CONNECTED)
            return;

        if (VTInCallScreenFlags.getInstance().mVTInSnapshot) {
            if (DBG)
                log("VTManager is handling snapshot now, so returns this time.");
            return;
        }

        if (!PhoneUtils.isExternalStorageMounted()) {
            Toast.makeText(mInCallScreen, getResources().getString(R.string.vt_sd_null), Toast.LENGTH_SHORT).show();
            return;
        } else {
            long blockSize, availableBlocks;
            try {
                if (!PhoneUtils.diskSpaceAvailable(1000000)) {
                    Toast.makeText(mInCallScreen, getResources().getString(R.string.vt_sd_not_enough),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(mInCallScreen, getResources().getString(R.string.vt_sd_null),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            (new Thread() {
                public void run() {
                    VTInCallScreenFlags.getInstance().mVTInSnapshot = true;
                    boolean ret = VTManager.getInstance().savePeerPhoto();
                    if (DBG)
                        log("onVTTakePeerPhotoClick(): VTManager.getInstance().savePeerPhoto(), return "
                                + ret);
                    if (ret) {
                        mHandler.sendMessage(Message.obtain(mHandler, VT_PEER_SNAPSHOT_OK));
                    } else {
                        mHandler.sendMessage(Message.obtain(mHandler, VT_PEER_SNAPSHOT_FAIL));
                    }
                    VTInCallScreenFlags.getInstance().mVTInSnapshot = false;
                }
            }).start();
        }
    }

    private void onVTHideMeClick() {
        if (DBG)
            log("onVTHideMeClick()...");

        if (VTManager.getInstance().getState() != VTManager.State.READY
                && VTManager.getInstance().getState() != VTManager.State.CONNECTED)
            return;

        VTCallUtils.checkVTFile();

        if (VTInCallScreenFlags.getInstance().mVTHideMeNow) {
            VTManager.getInstance().setLocalVideoType(0, "");
        } else {
            if (VTSettingUtils.getInstance().mPicToReplaceLocal.equals("0")) {
                VTManager.getInstance().setLocalVideoType(1, VTAdvancedSetting.getPicPathDefault());
            } else if (VTSettingUtils.getInstance().mPicToReplaceLocal.equals("1")) {
                VTManager.getInstance().setLocalVideoType(2, "");
            } else {
                VTManager.getInstance().setLocalVideoType(1,
                        VTAdvancedSetting.getPicPathUserselect());
            }
        }

        VTInCallScreenFlags.getInstance().mVTHideMeNow = !VTInCallScreenFlags.getInstance().mVTHideMeNow;
        updateVTScreen(getVTScreenMode());
    }

    // this method is for hide local video when user select don't show local
    // video to peer when MO/MT
    private void onVTHideMeClick2() {
        if (DBG)
            log("onVTHideMeClick2()...");

        VTCallUtils.checkVTFile();

        if (VTSettingUtils.getInstance().mPicToReplaceLocal.equals("2")) {
            VTManager.getInstance().setLocalVideoType(1, VTAdvancedSetting.getPicPathUserselect());
        } else if (VTSettingUtils.getInstance().mPicToReplaceLocal.equals("1")) {
            VTManager.getInstance().setLocalVideoType(2, "");
        } else {
            VTManager.getInstance().setLocalVideoType(1, VTAdvancedSetting.getPicPathDefault());
        }

        VTInCallScreenFlags.getInstance().mVTHideMeNow = true;
        //updateVTScreen(getVTScreenMode());
    }

    private void onVTSwitchCameraClick() {
        if (DBG) log("onVTSwitchCameraClick()...");

        if (VTManager.getInstance().getState() != VTManager.State.READY
                && VTManager.getInstance().getState() != VTManager.State.CONNECTED)
            return;

        if (VTInCallScreenFlags.getInstance().mVTInSwitchCamera) {
            if (DBG)
                log("VTManager is handling switchcamera now, so returns this time.");
            return;
        }

        // because switch camera may spend 2-4 second
        // new a thread to finish it so that it cannot block UI update
        (new Thread() {
            public void run() {
                VTInCallScreenFlags.getInstance().mVTInSwitchCamera = true;
                VTManager.getInstance().switchCamera();
                VTInCallScreenFlags.getInstance().mVTInSwitchCamera = false;
            }
        }).start();

        VTInCallScreenFlags.getInstance().mVTFrontCameraNow = !VTInCallScreenFlags.getInstance().mVTFrontCameraNow;
        updateVTScreen(getVTScreenMode());

        VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting = false;
        VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting = false;
        VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting = false;
        hideLocalZoomOrBrightness();
    }

    public class DialogCancelTimer {

        private final Timer timer = new Timer();

        private final int seconds;

        private AlertDialog asker = null;

        public DialogCancelTimer(int seconds, AlertDialog dialog) {
            this.seconds = seconds;
            this.asker = dialog;
        }

        public void start() {
            timer.schedule(new TimerTask() {
                public void run() {
                    if (asker != null)
                        if (asker.isShowing())
                            asker.cancel();
                    timer.cancel();
                }
            }, seconds * 1000);
        }
    }

    public void resetVTFlags() {
        if (DBG)
            log("resetVTFlags()...");

        VTInCallScreenFlags.getInstance().reset();

//        if (mVTPhoneNumber != null)
//            mVTPhoneNumber.setText("");

        dismissVTDialogs();

        if (mVTLowVideo != null)
            if (mVTLowVideo.getBackground() != null)
                mVTLowVideo.setBackgroundDrawable(null);

        if (mVTHighVideo != null)
            if (mVTHighVideo.getBackground() != null)
                mVTHighVideo.setBackgroundDrawable(null);

        if (mBkgBitmapHandler != null)
            mBkgBitmapHandler.recycle();
    }

    public String getVTPicPathUserselect() {
        if (DBG)
            log("getVTPicPathUserselect()...");
        return "/data/data/" + mInCallScreen.getPackageName() + "/"
                + VTAdvancedSetting.NAME_PIC_TO_REPLACE_LOCAL_VIDEO_USERSELECT + ".vt";
    }

    public String getVTPicPathDefault() {
        if (DBG)
            log("getVTPicPathDefault()...");
        return "/data/data/" + mInCallScreen.getPackageName() + "/"
                + VTAdvancedSetting.NAME_PIC_TO_REPLACE_LOCAL_VIDEO_DEFAULT + ".vt";
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (DBG)
            log(" surfaceChanged : " + holder.toString());

        if (holder == mHighVideoHolder) {
            if (DBG)
                log(" surfaceChanged : HighVideo , set mVTSurfaceChangedH = true ");
            VTInCallScreenFlags.getInstance().mVTSurfaceChangedH = true;
        }

        if (holder == mLowVideoHolder) {
            if (DBG)
                log(" surfaceChanged : LowVideo , set mVTSurfaceChangedL = true ");
            VTInCallScreenFlags.getInstance().mVTSurfaceChangedL = true;
        }

        if (VTInCallScreenFlags.getInstance().mVTSurfaceChangedH
                && VTInCallScreenFlags.getInstance().mVTSurfaceChangedL) {
            updateVTLocalPeerDisplay();

            if (DBG)
                log("surfaceChanged : VTManager.getInstance().setVTVisible(true) start ...");
            VTManager.getInstance().setVTVisible(true);
            try {
                if (!mVTWakeLock.isHeld())
                    mVTWakeLock.acquire();
            } catch (Exception ex) {
                if (DBG)
                    log("surfaceChanged : mVTWakeLock.acquire() unsuccessfully , exception !");
            }
            if (DBG)
                log("surfaceChanged : VTManager.getInstance().setVTVisible(true) end ...");

            if (VTInCallScreenFlags.getInstance().mVTSettingReady) {
                if (DBG)
                    log("- set VTManager ready ! ");
                VTManager.getInstance().setVTReady();
                if (DBG)
                    log("- finish set VTManager ready ! ");
                VTInCallScreenFlags.getInstance().mVTSettingReady = false;
            }
            updateVTScreen(getVTScreenMode());
            // debugVTUIInfo();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (DBG)
            log(" surfaceCreated : " + holder.toString());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (DBG)
            log(" surfaceDestroyed : " + holder.toString());

        if (holder == mHighVideoHolder) {
            if (DBG)
                log(" surfaceDestroyed : HighVideo , set mVTSurfaceChangedH = false ");
            VTInCallScreenFlags.getInstance().mVTSurfaceChangedH = false;
        }

        if (holder == mLowVideoHolder) {
            if (DBG)
                log(" surfaceDestroyed : LowVideo , set mVTSurfaceChangedL = false ");
            VTInCallScreenFlags.getInstance().mVTSurfaceChangedL = false;
        }

        if ((!VTInCallScreenFlags.getInstance().mVTSurfaceChangedH)
                && (!VTInCallScreenFlags.getInstance().mVTSurfaceChangedL)) {
            if (DBG)
                log("surfaceDestroyed : VTManager.getInstance().setVTVisible(false) start ...");
            VTManager.getInstance().setVTVisible(false);
            try {
                if (mVTWakeLock.isHeld())
                    mVTWakeLock.release();
            } catch (Exception ex) {
                if (DBG)
                    log("surfaceDestroyed : mVTWakeLock.release() unsuccessfully , exception !");
            }
            if (DBG)
                log("surfaceDestroyed : VTManager.getInstance().setVTVisible(false) end ...");
        }
    }

    private void onVTShowDialpad() {
        if (DBG)
            log("onVTShowDialpad() ! ");

        if (mDialer.isOpened()) {
            log("onShowHideDialpad(): Set mInCallTitle VISIBLE");
            mDialer.closeDialer(true);
        }
        mDialer.openDialer(true);
        // mDialer.setHandleVisible(true);
    }

    private void onVTSwapVideos() {
        if (DBG)
            log("onVTSwapVideos() ! ");

        if (VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting
                || VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting
                || VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting)
            hideLocalZoomOrBrightness();

        VTInCallScreenFlags.getInstance().mVTPeerBigger
            = !VTInCallScreenFlags.getInstance().mVTPeerBigger;
        VTManager.getInstance().setVTVisible(false);
        updateVTLocalPeerDisplay();
        VTManager.getInstance().setVTVisible(true);

        if (VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting)
            showVTLocalZoom();
        if (VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting)
            showVTLocalBrightness();
        if (VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting)
            showVTLocalContrast();
    }

    private void onVTInCallVideoSetting() {
        if (DBG) log("onVTInCallVideoSetting() ! ");

        DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingDialog != null) {
                    mInCallVideoSettingDialog.dismiss();
                    mInCallVideoSettingDialog = null;
                }
                if (0 == which) {
                    if (DBG)
                        log("onVTInCallVideoSetting() : select - 0 ");
                    if (!VTManager.getInstance().canDecZoom()
                            && !VTManager.getInstance().canIncZoom())
                        showToast(getResources().getString(R.string.vt_cannot_support_setting));
                    else
                        showVTLocalZoom();
                } else if (1 == which) {
                    if (DBG)
                        log("onVTInCallVideoSetting() : select - 1 ");
                    if (!VTManager.getInstance().canDecBrightness()
                            && !VTManager.getInstance().canIncBrightness())
                        showToast(getResources().getString(R.string.vt_cannot_support_setting));
                    else
                        showVTLocalBrightness();
                } else if (2 == which) {
                    if (DBG)
                        log("onVTInCallVideoSetting() : select - 2 ");
                    if (!VTManager.getInstance().canDecContrast()
                            && !VTManager.getInstance().canIncContrast())
                        showToast(getResources().getString(R.string.vt_cannot_support_setting));
                    else
                        showVTLocalContrast();
                } else if (3 == which) {
                    if (DBG)
                        log("onVTInCallVideoSetting() : select - 3 ");
                    onVTInCallVideoSettingLocalEffect();
                } else if (4 == which) {
                    if (DBG)
                        log("onVTInCallVideoSetting() : select - 4 ");
                    onVTInCallVideoSettingLocalNightMode();
                } else {
                    if (DBG)
                        log("onVTInCallVideoSetting() : select - 5 ");
                    onVTInCallVideoSettingPeerQuality();
                }
            }
        };

        AlertDialog.Builder myBuilder = new AlertDialog.Builder(mInCallScreen);
        myBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingDialog != null) {
                    mInCallVideoSettingDialog.dismiss();
                    mInCallVideoSettingDialog = null;
                }
            }
        });

        if (!VTInCallScreenFlags.getInstance().mVTHideMeNow) {
            if (VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting) {
                myBuilder.setSingleChoiceItems(R.array.vt_incall_video_setting_entries, 0,
                        myClickListener).setTitle(R.string.vt_settings);
            } else if (VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting) {
                myBuilder.setSingleChoiceItems(R.array.vt_incall_video_setting_entries, 1,
                        myClickListener).setTitle(R.string.vt_settings);
            } else if (VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting) {
                myBuilder.setSingleChoiceItems(R.array.vt_incall_video_setting_entries, 2,
                        myClickListener).setTitle(R.string.vt_settings);
            } else {
                myBuilder.setSingleChoiceItems(R.array.vt_incall_video_setting_entries, -1,
                        myClickListener).setTitle(R.string.vt_settings);
            }
        } else {
            myBuilder.setSingleChoiceItems(R.array.vt_incall_video_setting_entries2, -1,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (mInCallVideoSettingDialog != null) {
                                mInCallVideoSettingDialog.dismiss();
                                mInCallVideoSettingDialog = null;
                            }
                            onVTInCallVideoSettingPeerQuality();
                        }
                    }).setTitle(R.string.vt_settings);
        }
        mInCallVideoSettingDialog = myBuilder.create();
        mInCallVideoSettingDialog.show();
    }

    private void onVTInCallVideoSettingLocalEffect() {
        if (DBG)
            log("onVTInCallVideoSettingLocalEffect() ! ");
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(mInCallScreen);
        myBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingLocalEffectDialog != null) {
                    mInCallVideoSettingLocalEffectDialog.dismiss();
                    mInCallVideoSettingLocalEffectDialog = null;
                }
            }
        });

        List<String> supportEntryValues = VTManager.getInstance().getSupportedColorEffects();

        if (supportEntryValues == null || supportEntryValues.size() <= 0)
            return;

        CharSequence[] entryValues = getResources().getStringArray(
                R.array.vt_incall_setting_local_video_effect_values);
        CharSequence[] entries = getResources().getStringArray(
                R.array.vt_incall_setting_local_video_effect_entries);
        ArrayList<CharSequence> entryValues2 = new ArrayList<CharSequence>();
        ArrayList<CharSequence> entries2 = new ArrayList<CharSequence>();

        for (int i = 0, len = entryValues.length; i < len; i++) {
            if (supportEntryValues.indexOf(entryValues[i].toString()) >= 0) {
                entryValues2.add(entryValues[i]);
                entries2.add(entries[i]);
            }
        }

        if (DBG)
            log("onVTInCallVideoSettingLocalEffect() : entryValues2.size() - "
                    + entryValues2.size());
        int currentValue = entryValues2.indexOf(VTManager.getInstance().getColorEffect());

        InCallVideoSettingLocalEffectListener myClickListener = new InCallVideoSettingLocalEffectListener();
        myClickListener.setValues(entryValues2);
        myBuilder.setSingleChoiceItems(entries2.toArray(new CharSequence[entryValues2.size()]),
                currentValue, myClickListener);
        myBuilder.setTitle(R.string.vt_local_video_effect);
        mInCallVideoSettingLocalEffectDialog = myBuilder.create();
        mInCallVideoSettingLocalEffectDialog.show();
    }

    class InCallVideoSettingLocalEffectListener implements DialogInterface.OnClickListener {
        private ArrayList<CharSequence> mValues = new ArrayList<CharSequence>();

        public void setValues(ArrayList<CharSequence> values) {
            for (int i = 0; i < values.size(); i++) {
                mValues.add(values.get(i));
            }
        }

        public void onClick(DialogInterface dialog, int which) {

            if (mInCallVideoSettingLocalEffectDialog != null) {
                mInCallVideoSettingLocalEffectDialog.dismiss();
                mInCallVideoSettingLocalEffectDialog = null;
            }
            VTManager.getInstance().setColorEffect(mValues.get(which).toString());
            updateLocalZoomOrBrightness();
        }
    }

    private void onVTInCallVideoSettingLocalNightMode() {
        if (DBG)
            log("onVTInCallVideoSettingLocalNightMode() ! ");

        AlertDialog.Builder myBuilder = new AlertDialog.Builder(mInCallScreen);
        myBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingLocalNightmodeDialog != null) {
                    mInCallVideoSettingLocalNightmodeDialog.dismiss();
                    mInCallVideoSettingLocalNightmodeDialog = null;
                }
            }
        });
        myBuilder.setTitle(R.string.vt_local_video_nightmode);

        DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingLocalNightmodeDialog != null) {
                    mInCallVideoSettingLocalNightmodeDialog.dismiss();
                    mInCallVideoSettingLocalNightmodeDialog = null;
                }
                if (0 == which) {
                    if (DBG)
                        log("onVTInCallVideoSettingLocalNightMode() : VTManager.getInstance().setNightMode(true);");
                    VTManager.getInstance().setNightMode(true);
                    updateLocalZoomOrBrightness();
                } else if (1 == which) {
                    if (DBG)
                        log("onVTInCallVideoSettingLocalNightMode() : VTManager.getInstance().setNightMode(false);");
                    VTManager.getInstance().setNightMode(false);
                    updateLocalZoomOrBrightness();
                }
            }
        };

        if (VTManager.getInstance().isSupportNightMode()) {

            if (VTManager.getInstance().getNightMode()) {
                myBuilder
                        .setSingleChoiceItems(
                                R.array.vt_incall_video_setting_local_nightmode_entries, 0,
                                myClickListener);
            } else {
                myBuilder
                        .setSingleChoiceItems(
                                R.array.vt_incall_video_setting_local_nightmode_entries, 1,
                                myClickListener);
            }
        } else {
            myBuilder.setSingleChoiceItems(
                    R.array.vt_incall_video_setting_local_nightmode_entries2, 0,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            if (mInCallVideoSettingLocalNightmodeDialog != null) {
                                mInCallVideoSettingLocalNightmodeDialog.dismiss();
                                mInCallVideoSettingLocalNightmodeDialog = null;
                            }
                        }
                    });
        }

        mInCallVideoSettingLocalNightmodeDialog = myBuilder.create();
        mInCallVideoSettingLocalNightmodeDialog.show();
    }

    private void onVTInCallVideoSettingPeerQuality() {
        if (DBG)
            log("onVTInCallVideoSettingPeerQuality() ! ");
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(mInCallScreen);
        myBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingPeerQualityDialog != null) {
                    mInCallVideoSettingPeerQualityDialog.dismiss();
                    mInCallVideoSettingPeerQualityDialog = null;
                }
            }
        });
        myBuilder.setTitle(R.string.vt_peer_video_quality);

        DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingPeerQualityDialog != null) {
                    mInCallVideoSettingPeerQualityDialog.dismiss();
                    mInCallVideoSettingPeerQualityDialog = null;
                }
                if (0 == which) {
                    if (DBG)
                        log("onVTInCallVideoSettingPeerQuality() : VTManager.getInstance().setVideoQuality( VTManager.VT_VQ_NORMAL );");
                    VTManager.getInstance().setVideoQuality(VTManager.VT_VQ_NORMAL);
                } else if (1 == which) {
                    if (DBG)
                        log("onVTInCallVideoSettingPeerQuality() : VTManager.getInstance().setVideoQuality( VTManager.VT_VQ_SHARP );");
                    VTManager.getInstance().setVideoQuality(VTManager.VT_VQ_SHARP);
                }
            }
        };

        if (VTManager.VT_VQ_NORMAL == VTManager.getInstance().getVideoQuality()) {
            myBuilder.setSingleChoiceItems(R.array.vt_incall_video_setting_peer_quality_entries, 0,
                    myClickListener);
        } else if (VTManager.VT_VQ_SHARP == VTManager.getInstance().getVideoQuality()) {
            myBuilder.setSingleChoiceItems(R.array.vt_incall_video_setting_peer_quality_entries, 1,
                    myClickListener);
        } else {
            if (DBG)
                log("VTManager.getInstance().getVideoQuality() is not VTManager.VT_VQ_SHARP or VTManager.VT_VQ_NORMAL , error ! ");
        }

        mInCallVideoSettingPeerQualityDialog = myBuilder.create();
        mInCallVideoSettingPeerQualityDialog.show();
    }

    public void dismissVTDialogs() {
        if (DBG)
            log("dismissVTDialogs() ! ");
        if (mInCallVideoSettingDialog != null) {
            mInCallVideoSettingDialog.dismiss();
            mInCallVideoSettingDialog = null;
        }
        if (mInCallVideoSettingLocalEffectDialog != null) {
            mInCallVideoSettingLocalEffectDialog.dismiss();
            mInCallVideoSettingLocalEffectDialog = null;
        }
        if (mInCallVideoSettingLocalNightmodeDialog != null) {
            mInCallVideoSettingLocalNightmodeDialog.dismiss();
            mInCallVideoSettingLocalNightmodeDialog = null;
        }
        if (mInCallVideoSettingPeerQualityDialog != null) {
            mInCallVideoSettingPeerQualityDialog.dismiss();
            mInCallVideoSettingPeerQualityDialog = null;
        }
        if (mVTMTAsker != null) {
            mVTMTAsker.dismiss();
            mVTMTAsker = null;
        }
        if (mVTVoiceReCallDialog != null) {
            mVTVoiceReCallDialog.dismiss();
            if (mCM.getActiveFgCall().isIdle() && mCM.getFirstActiveBgCall().isIdle()
                    && mCM.getFirstActiveRingingCall().isIdle()) {
                mInCallScreen.endInCallScreenSession();
            }
            mVTVoiceReCallDialog = null;
        }
        if (mVTRecorderSelector != null) {
            mVTRecorderSelector.dismiss();
            mVTRecorderSelector = null;
        }
    }

    private boolean getVTInControlRes() {
        return VTInCallScreenFlags.getInstance().mVTInControlRes;
    }

    private void setVTInControlRes(boolean value) {
        VTInCallScreenFlags.getInstance().mVTInControlRes = value;
    }

    private void onVTReceiveFirstFrame() {
        if (DBG) log("onVTReceiveFirstFrame() ! ");
        if (VTInCallScreenFlags.getInstance().mVTPeerBigger) {
            if (mVTHighVideo != null) {
                if (mVTHighVideo.getBackground() != null)
                    mVTHighVideo.setBackgroundDrawable(null);
            }
        } else {
            if (mVTLowVideo != null) {
                if (mVTLowVideo.getBackground() != null)
                    mVTLowVideo.setBackgroundDrawable(null);
            }
        }
        if (!VTInCallScreenFlags.getInstance().mVTHasReceiveFirstFrame)
            VTInCallScreenFlags.getInstance().mVTHasReceiveFirstFrame = true;
    }

    private void closeVTManager() {

        if (DBG) log("closeVTManager()!");
        dismissVTDialogs();
        updateVTScreen(getVTScreenMode());

        PhoneUtils.turnOnSpeaker(mInCallScreen, false, true);

        mHandler.removeMessages(VTManager.VT_ERROR_CALL_DISCONNECT);

        if (PhoneUtils.isSupportFeature("VT_VOICE_RECORDING")
                || PhoneUtils.isSupportFeature("VT_VIDEO_RECORDING")) {
            if (PhoneRecorderHandler.getInstance().isVTRecording()) {
                stopRecord();
            }
        }

        if (VDBG) log("- call VTManager onDisconnected ! ");
        VTManager.getInstance().onDisconnected();
        if (VDBG) log("- finish call VTManager onDisconnected ! ");

        if (VDBG) log("- set VTManager close ! ");
        VTManager.getInstance().setVTClose();
        if (VDBG) log("- finish set VTManager close ! ");

        if (getVTInControlRes()) {
            mInCallScreen.sendBroadcast(new Intent(VTCallUtils.VT_CALL_END));
            setVTInControlRes(false);
        }
    }

    public void updateVideoCallRecordState(final int state) {
        if (false == FeatureOption.MTK_VT3G324M_SUPPORT)
            return;
        if (VDBG) log("updateVideoCallRecordState(), state = " + state);
        //AnimationDrawable animDrawable = (AnimationDrawable) mVTVoiceRecordingIcon.getBackground();
        if (PhoneRecorder.RECORDING_STATE == state /*&& null != animDrawable*/) {
            mVTVoiceRecordingIcon.setVisibility(View.VISIBLE);
            //animDrawable.start();
        } else if ((PhoneRecorder.IDLE_STATE == state /*&& null != animDrawable*/)) {
            //animDrawable.stop();
            mVTVoiceRecordingIcon.setVisibility(View.GONE);
        }
    }

    public void setVTVisible(final boolean bIsVisible) {
        if (bIsVisible) {
            if (VTInCallScreenFlags.getInstance().mVTSurfaceChangedH
                    && VTInCallScreenFlags.getInstance().mVTSurfaceChangedL) {
                if (DBG)
                    log("VTManager.getInstance().setVTVisible(true) start ...");
                VTManager.getInstance().setVTVisible(true);
                if (DBG)
                    log("VTManager.getInstance().setVTVisible(true) end ...");
            }
        } else {
            if (DBG)
                log("VTManager.getInstance().setVTVisible(false) start ...");
            VTManager.getInstance().setVTVisible(false);
            if (DBG)
                log("VTManager.getInstance().setVTVisible(false) start ...");
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (VDBG)
            log("onClick(View " + view + ", id " + id + ")...");

        switch (id) {

            case R.id.VTHighVideo:
                if (VDBG)
                    log("onClick: VTHighVideo...");
                if (!VTInCallScreenFlags.getInstance().mVTPeerBigger) {
                    hideLocalZoomOrBrightness();
                    VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting = false;
                    VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting = false;
                    VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting = false;
                }
                break;

            case R.id.VTLowVideo:
                if (VDBG)
                    log("onClick: VTLowVideo...");
                if (VTInCallScreenFlags.getInstance().mVTPeerBigger) {
                    hideLocalZoomOrBrightness();
                    VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting = false;
                    VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting = false;
                    VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting = false;
                }
                break;

            case R.id.VTMute:
                if (VDBG)
                    log("onClick: VTMute");
                mInCallScreen.onMuteClick();
                break;

            case R.id.VTSpeaker:
                if (VDBG)
                    log("onClick: VTSpeaker...");
                handleAudioButtonClick();
                break;

            case R.id.VTDialpad:
                if (VDBG)
                    log("onClick: VTDialpad...");
                mInCallScreen.onShowHideDialpad();
                break;

            case R.id.VTSwapVideo:
                if (VDBG)
                    log("onClick: VTSwapVideo...");
                onVTSwapVideos();
                break;

            case R.id.VTHangUp:
                if (VDBG)
                    log("onClick: VTHangUp...");
                VTInCallScreenFlags.getInstance().mVTInEndingCall = true;
                updateVTScreen(getVTScreenMode());
                PhoneUtils.hangup(PhoneApp.getInstance().mCM);
                break;

            case R.id.VTLowUp:
                if (VDBG)
                    log("onClick: VTLowUp...");
                if (VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting) {
                    VTManager.getInstance().incZoom();
                    mVTLowUp.setEnabled(VTManager.getInstance().canIncZoom());
                    mVTLowDown.setEnabled(VTManager.getInstance().canDecZoom());
                } else if (VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting) {
                    VTManager.getInstance().incBrightness();
                    mVTLowUp.setEnabled(VTManager.getInstance().canIncBrightness());
                    mVTLowDown.setEnabled(VTManager.getInstance().canDecBrightness());
                } else if (VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting) {
                    VTManager.getInstance().incContrast();
                    mVTLowUp.setEnabled(VTManager.getInstance().canIncContrast());
                    mVTLowDown.setEnabled(VTManager.getInstance().canDecContrast());
                }
                break;

            case R.id.VTHighUp:
                if (VDBG)
                    log("onClick: VTHighUp...");
                if (VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting) {
                    VTManager.getInstance().incZoom();
                    mVTHighUp.setEnabled(VTManager.getInstance().canIncZoom());
                    mVTHighDown.setEnabled(VTManager.getInstance().canDecZoom());
                } else if (VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting) {
                    VTManager.getInstance().incBrightness();
                    mVTHighUp.setEnabled(VTManager.getInstance().canIncBrightness());
                    mVTHighDown.setEnabled(VTManager.getInstance().canDecBrightness());
                } else if (VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting) {
                    VTManager.getInstance().incContrast();
                    mVTHighUp.setEnabled(VTManager.getInstance().canIncContrast());
                    mVTHighDown.setEnabled(VTManager.getInstance().canDecContrast());
                }
                break;

            case R.id.VTLowDown:
                if (VDBG)
                    log("onClick: VTLowDown...");
                if (VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting) {
                    VTManager.getInstance().decZoom();
                    mVTLowUp.setEnabled(VTManager.getInstance().canIncZoom());
                    mVTLowDown.setEnabled(VTManager.getInstance().canDecZoom());
                } else if (VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting) {
                    VTManager.getInstance().decBrightness();
                    mVTLowUp.setEnabled(VTManager.getInstance().canIncBrightness());
                    mVTLowDown.setEnabled(VTManager.getInstance().canDecBrightness());
                } else if (VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting) {
                    VTManager.getInstance().decContrast();
                    mVTLowUp.setEnabled(VTManager.getInstance().canIncContrast());
                    mVTLowDown.setEnabled(VTManager.getInstance().canDecContrast());
                }
                break;

            case R.id.VTHighDown:
                if (VDBG) log("onClick: VTHighDown...");
                if (VTInCallScreenFlags.getInstance().mVTInLocalZoomSetting) {
                    VTManager.getInstance().decZoom();
                    mVTHighUp.setEnabled(VTManager.getInstance().canIncZoom());
                    mVTHighDown.setEnabled(VTManager.getInstance().canDecZoom());
                } else if (VTInCallScreenFlags.getInstance().mVTInLocalBrightnessSetting) {
                    VTManager.getInstance().decBrightness();
                    mVTHighUp.setEnabled(VTManager.getInstance().canIncBrightness());
                    mVTHighDown.setEnabled(VTManager.getInstance().canDecBrightness());
                } else if (VTInCallScreenFlags.getInstance().mVTInLocalContrastSetting) {
                    VTManager.getInstance().decContrast();
                    mVTHighUp.setEnabled(VTManager.getInstance().canIncContrast());
                    mVTHighDown.setEnabled(VTManager.getInstance().canDecContrast());
                }
                break;
            
            case R.id.VTOverflowMenu:
                if (VDBG) log("onClick: VTOverflowMenu...");
                if (null != mVTPopupMenu) {
                    mVTPopupMenu.dismiss();
                }
                PopupMenu popup = constructPopupMenu(mVTOverflowMenu);
                if (popup != null) {
                    popup.show();
                }
                break;

            default:
                log("onClick: unexpected click from ID " + id + " (View = " + view + ")");
        }
    }

    protected void showGenericErrorDialog(int resid, boolean isStartupError) {
        mInCallScreen.showGenericErrorDialog(resid, isStartupError);
    }

    // The return value means whether the caller needs return immediately
    // true : return immediately
    // false : not need return, continue
    public boolean onDisconnectVT(final Connection connection, final int slotId, final boolean isForeground) {
        if (null == connection) {
            return false;
        }
        Connection.DisconnectCause cause = connection.getDisconnectCause();
        log("onDisconnectVT(), cause = " + cause + " isForeground = " + isForeground + " slotId = " + slotId);
        if (isForeground) {
            // the follwing is to check abnormal number disconnect
            if (cause == Connection.DisconnectCause.UNOBTAINABLE_NUMBER
                    || cause == Connection.DisconnectCause.INVALID_NUMBER_FORMAT
                    || cause == Connection.DisconnectCause.INVALID_NUMBER) {
                showGenericErrorDialog(R.string.callFailed_unobtainable_number, false);
                return true;
            } else if (cause == Connection.DisconnectCause.CM_MM_RR_CONNECTION_RELEASE) {
                showGenericErrorDialog(R.string.vt_network_unreachable, false);
                return true;
            } else

            // the followings are to handle IOT call disconnect UI
            if (cause == Connection.DisconnectCause.NO_ROUTE_TO_DESTINATION) {
                showGenericErrorDialog(R.string.vt_iot_error_01, false);
                return true;
            } else if (cause == Connection.DisconnectCause.BUSY) {
                showGenericErrorDialog(R.string.vt_iot_error_02, false);
                return true;
            } else if (cause == Connection.DisconnectCause.NO_USER_RESPONDING) {
                showGenericErrorDialog(R.string.vt_iot_error_03, false);
                return true;
            } else if (cause == Connection.DisconnectCause.USER_ALERTING_NO_ANSWER) {
                showGenericErrorDialog(R.string.vt_iot_error_03, false);
                return true;
            } else if (cause == Connection.DisconnectCause.CALL_REJECTED) {
                showGenericErrorDialog(R.string.vt_iot_error_01, false);
                return true;
            } else if (cause == Connection.DisconnectCause.FACILITY_REJECTED) {
                showGenericErrorDialog(R.string.vt_iot_error_01, false);
                return true;
                // Need to check whether it's MO call because cause of MT case
                // also can be NORMAL_UNSPECIFIED possibly
            } else if (cause == Connection.DisconnectCause.NORMAL_UNSPECIFIED 
                    && false == connection.isIncoming()) {
                showGenericErrorDialog(R.string.vt_iot_error_01, false);
                return true;
            } else if (cause == Connection.DisconnectCause.CONGESTION) {
                showGenericErrorDialog(R.string.vt_iot_error_01, false);
                return true;
            } else if (cause == Connection.DisconnectCause.SWITCHING_CONGESTION) {
                showGenericErrorDialog(R.string.vt_iot_error_04, false);
                return true;
            } else if (cause == Connection.DisconnectCause.SERVICE_NOT_AVAILABLE) {
                showGenericErrorDialog(R.string.vt_iot_error_01, false);
                return true;
            } else if (cause == Connection.DisconnectCause.BEARER_NOT_IMPLEMENT) {
                showGenericErrorDialog(R.string.vt_iot_error_01, false);
                return true;
            } else if (cause == Connection.DisconnectCause.FACILITY_NOT_IMPLEMENT) {
                showGenericErrorDialog(R.string.vt_iot_error_01, false);
                return true;
            } else if (cause == Connection.DisconnectCause.RESTRICTED_BEARER_AVAILABLE) {
                showGenericErrorDialog(R.string.vt_iot_error_01, false);
                return true;
            } else if (cause == Connection.DisconnectCause.OPTION_NOT_AVAILABLE) {
                showGenericErrorDialog(R.string.vt_iot_error_01, false);
                return true;
            }
        }

        if ((VTSettingUtils.getInstance().mAutoDropBack || isForeground) && !connection.isIncoming()) {
            final String number = connection.getAddress();
            // the followings are to check drop back
            if (cause == Connection.DisconnectCause.INCOMPATIBLE_DESTINATION) {
                if (DBG)
                    Log.d("InCallScreen", "VT call dropback INCOMPATIBLE_DESTINATION");
                showReCallDialog(R.string.callFailed_dsac_vt_incompatible_destination, number, slotId);
                return true;
            } else if (cause == Connection.DisconnectCause.RESOURCE_UNAVAILABLE) {
                if (DBG)
                    Log.d("InCallScreen", "VT call dropback RESOURCE_UNAVAILABLE");
                showReCallDialog(R.string.callFailed_dsac_vt_resource_unavailable, number, slotId);
                return true;
            } else if (cause == Connection.DisconnectCause.BEARER_NOT_AUTHORIZED) {
                if (DBG)
                    Log.d("InCallScreen", "VT call dropback BEARER_NOT_AUTHORIZED");
                if ("OP01".equals(PhoneUtils.getOptrProperties())){
                    showReCallDialog(R.string.callFailed_dsac_vt_bear_not_authorized, number, slotId);
                } else {
                    showReCallDialog(R.string.callFailed_dsac_vt_bearer_not_avail, number, slotId);
                }
                return true;
            } else if (cause == Connection.DisconnectCause.BEARER_NOT_AVAIL) {
                if (DBG)
                    Log.d("InCallScreen", "VT call dropback BEARER_NOT_AVAIL");
                showReCallDialog(R.string.callFailed_dsac_vt_bearer_not_avail, number, slotId);
                return true;
            } else if (cause == Connection.DisconnectCause.NORMAL
                    || cause == Connection.DisconnectCause.ERROR_UNSPECIFIED) {
                if (DBG)
                    Log.d("InCallScreen", "VT call dropback NORMAL or ERROR_UNSPECIFIED");
                int nCSNetType;
                if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
                    int mode_3G = PhoneApp.getInstance().phoneMgr.get3GCapabilitySIM();
                    if (0 == mode_3G) {
                        nCSNetType = SystemProperties.getInt(
                                TelephonyProperties.PROPERTY_CS_NETWORK_TYPE, -1);
                    } else if (1 == mode_3G) {
                        nCSNetType = SystemProperties.getInt(
                                TelephonyProperties.PROPERTY_CS_NETWORK_TYPE_2, -1);
                    } else {// -1 == mode_3G
                        nCSNetType = 1;
                    }
                } else {
                    nCSNetType = SystemProperties.getInt(
                            TelephonyProperties.PROPERTY_CS_NETWORK_TYPE, -1);
                }
                // so,nCSNetType: 1-GSM, 2-GPRS
                if (DBG)
                    Log.d("InCallScreen", "VT call dropback nCSNetType = " + nCSNetType);
                if ((1 == nCSNetType) || (2 == nCSNetType)) {
                    showReCallDialog(R.string.callFailed_dsac_vt_out_of_3G_yourphone, number, slotId);
                    return true;
                }
            } else if (cause == Connection.DisconnectCause.NO_CIRCUIT_AVAIL) {
                if (DBG)
                    Log.d("InCallScreen", "VT call dropback NO_CIRCUIT_AVAIL");
                showReCallDialog(R.string.callFailed_dsac_vt_bearer_not_avail, number, slotId);
                return true;
            }
        }
        return false;
    }

    public void onStop() {
        Phone.State state = mCM.getState();
        if (DBG) log("onStop: state = " + state);

        if (state == Phone.State.IDLE) {
            if(FeatureOption.MTK_VT3G324M_SUPPORT == true) {
                setVTScreenMode(VTCallUtils.VTScreenMode.VT_SCREEN_CLOSE);
                updateVTScreen(getVTScreenMode());
                resetVTFlags();
            }
        }
        if (null != mVTPopupMenu) {
            mVTPopupMenu.dismiss();
        }
        dismissAudioModePopup();
    }

    private void makeVoiceReCall(final String number, final int slot) {

        if (DBG) log("makeVoiceReCall(), number is " + number + " slot is " + slot);

        final Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", number, null));
        intent.putExtra(Constants.EXTRA_SLOT_ID, slot);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PhoneApp.getInstance().startActivity(intent);
        mInCallScreen.finish();
    }

    public void showReCallDialog(final int resid, final String number, final int slot) {

        if (DBG)
            log("showReCallDialog... ");

        if (VTSettingUtils.getInstance().mAutoDropBack) {
            showToast(getResources().getString(R.string.vt_voice_connecting));
            PhoneUtils.turnOnSpeaker(mInCallScreen, false, true);
            makeVoiceReCall(number, slot);
        } else {
            showReCallDialogEx(resid, number, slot);
        }
    }

    private void showReCallDialogEx(final int resid, final String number, final int slot) {
        if (DBG)
            log("showReCallDialogEx... ");

        if (null != mVTVoiceReCallDialog) {
            if (mVTVoiceReCallDialog.isShowing()) {
                return;
            }
        }
        CharSequence msg = getResources().getText(resid);

        // create the clicklistener and cancel listener as needed.
        DialogInterface.OnClickListener clickListener1, clickListener2, clickListener;
        DialogInterface.OnClickListener negativeClickListener;
        OnCancelListener cancelListener;

        clickListener1 = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (DBG)
                    log("showReCallDialogEx... , on click, which=" + which);
                if (null != mVTVoiceReCallDialog) {
                    mVTVoiceReCallDialog.dismiss();
                    mVTVoiceReCallDialog = null;
                }
                PhoneUtils.turnOnSpeaker(mInCallScreen, false, true);
                makeVoiceReCall(number, slot);
            }
        };

        clickListener2 = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (DBG)
                    log("showReCallDialogEx... , on click, which=" + which);

                if (null != mVTVoiceReCallDialog) {
                    mVTVoiceReCallDialog.dismiss();
                    mVTVoiceReCallDialog = null;
                }

                if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                    mInCallScreen
                            .delayedCleanupAfterDisconnect(mInCallScreen.DELAYED_CLEANUP_AFTER_DISCONNECT2);
                }
                mInCallScreen
                        .delayedCleanupAfterDisconnect(mInCallScreen.DELAYED_CLEANUP_AFTER_DISCONNECT);
            }
        };

        cancelListener = new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {

                if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                    mInCallScreen
                            .delayedCleanupAfterDisconnect(mInCallScreen.DELAYED_CLEANUP_AFTER_DISCONNECT2);
                }
                mInCallScreen
                        .delayedCleanupAfterDisconnect(mInCallScreen.DELAYED_CLEANUP_AFTER_DISCONNECT);

            }
        };

        mVTVoiceReCallDialog = new AlertDialog.Builder(mInCallScreen).setMessage(msg).setNegativeButton(
                R.string.vt_dis_exit, clickListener2).setPositiveButton(R.string.vt_dis_callback,
                clickListener1).setOnCancelListener(cancelListener).create();
        mVTVoiceReCallDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mVTVoiceReCallDialog.setOnShowListener(mInCallScreen);

        mVTVoiceReCallDialog.show();
    }

    void setInCallScreenInstance(InCallScreen inCallScreen) {
        mInCallScreen = inCallScreen;
    }

    /* package */void setDialerProxy(DTMFTwelveKeyDialerProxy dialer) {
        mDialer = dialer;
    }

    /* package */void registerForVTPhoneStates() {
        if (FeatureOption.MTK_VT3G324M_SUPPORT == true) {
            if (DBG)
                log("- VTManager.getInstance().registerVTListener() start ! ");
            VTManager.getInstance().registerVTListener(mHandler);
            if (DBG)
                log("- VTManager.getInstance().registerVTListener() end ! ");
        }
    }

    /* package */void unregisterForVTPhoneStates() {
        if (FeatureOption.MTK_VT3G324M_SUPPORT == true) {
            // Here we need judge whether mHandler is same as
            // Handle set to VTManager because there may be 2 InCallScreen instances
            // exiting in some case even if InCallScreen activity is single instance.
            // if mHandle != VTManager.getInstance().getmVTListener(), 
            // that means another InCallScreen is active,
            // no need unregister VT Listener
            if (mHandler != VTManager.getInstance().getmVTListener()) {
                if (DBG)
                    log("- mHandler does not equal to VTManager.getInstance().getmVTListener(), just return");
                return;
            }
            VTManager.getInstance().setDisplay(null, null);
            if (DBG)
                log("- VTManager.getInstance().unregisterVTListener() start ! ");
            VTManager.getInstance().unregisterVTListener();
            if (DBG)
                log("- VTManager.getInstance().unregisterVTListener() end ! ");
        }
    }

    public void updateElapsedTime(final long elapsedTime) {
        Call call = mCM.getActiveFgCall();
        if (null != call.getLatestConnection()
                && VTCallUtils.VTTimingMode.VT_TIMING_NONE == VTCallUtils.checkVTTimingMode(call.getLatestConnection().getAddress())) {
            mVTCallBannerController.updateElapsedTimeWidget(-1);
        } else {
            mVTCallBannerController.updateElapsedTimeWidget(elapsedTime);
        }
    }

    /* package */boolean isDialerOpened() {
        return (mDialer != null && mDialer.isOpened());
    }
    
    public void setupMenuItems(Menu menu) {
        if (PhoneUtils.isDMLocked()) {
            return;
        }
        
        final MenuItem switchCameraMenu = menu.findItem(R.id.menu_switch_camera);
        final MenuItem takePeerPhotoMenu = menu.findItem(R.id.menu_take_peer_photo);
        final MenuItem hideLocalVideoMenu = menu.findItem(R.id.menu_hide_local_video);
        final MenuItem swapVideosMenu = menu.findItem(R.id.menu_swap_videos);
        final MenuItem voiceRecordMenu = menu.findItem(R.id.menu_voice_record);
        //final MenuItem bluetoothMenu = menu.findItem(R.id.menu_vt_bluetooth);
        final MenuItem videoSettingMenu = menu.findItem(R.id.menu_video_setting);
        
        switchCameraMenu.setVisible(true);
        int cameraSensorCount = VTManager.getInstance().getCameraSensorCount();
        if (DBG)
            log("setupMenuItems() : VTManager.getInstance().getCameraSensorCount() == "
                    + cameraSensorCount);
        switchCameraMenu.setVisible(2 == cameraSensorCount);
        boolean bIsSwitchCameraEnable = VTSettingUtils.getInstance().mEnableBackCamera
                                                && (!VTInCallScreenFlags.getInstance().mVTHideMeNow);
        switchCameraMenu.setEnabled(bIsSwitchCameraEnable);

        takePeerPhotoMenu.setVisible(true);
        takePeerPhotoMenu.setEnabled(!isDialerOpened() && 
                VTInCallScreenFlags.getInstance().mVTVideoConnected);
        
        hideLocalVideoMenu.setVisible(true);
        if (!VTInCallScreenFlags.getInstance().mVTHideMeNow) {
            hideLocalVideoMenu.setTitle(getResources().getString(R.string.vt_menu_hide_me));
        } else {
            hideLocalVideoMenu.setTitle(getResources().getString(R.string.vt_menu_show_me));
        }
        
        swapVideosMenu.setVisible(!ViewConfiguration.get(mInCallScreen).hasPermanentMenuKey());
        swapVideosMenu.setEnabled(VTInCallScreenFlags.getInstance().mVTHasReceiveFirstFrame);
        
        voiceRecordMenu.setVisible(true);
        voiceRecordMenu.setEnabled(false);
        if (okToRecordVoice()) {
            voiceRecordMenu.setEnabled(true);
            if (PhoneRecorderHandler.getInstance().isVTRecording()) {
                voiceRecordMenu.setTitle(R.string.stop_record_vt);
            } else {
                voiceRecordMenu.setTitle(R.string.start_record_vt);
            }
        } else if (DualTalkUtils.isSupportDualTalk) {
            voiceRecordMenu.setVisible(false);
        }
        
        videoSettingMenu.setVisible(true);
        videoSettingMenu.setEnabled(VTInCallScreenFlags.getInstance().mVTVideoConnected);
    }

    private void updateVideoBkgDrawable() {
        if (VTSettingUtils.getInstance().mToReplacePeer) {
            if (null != mBkgBitmapHandler && null != mBkgBitmapHandler.getBitmap()) {
                if (DBG) log("updatescreen(): replace the peer video");
                if (VTSettingUtils.getInstance().mPeerBigger) {
                    mVTHighVideo.setBackgroundDrawable(new BitmapDrawable(mBkgBitmapHandler.getBitmap()));
                } else {
                    mVTLowVideo.setBackgroundDrawable(new BitmapDrawable(mBkgBitmapHandler.getBitmap()));
                }
            } else {
                if (DBG) log("mBkgBitmapHandler is null or mBkgBitmapHandler.getBitmap() is null");
            }
        } else {
            if (VTSettingUtils.getInstance().mPeerBigger) {
                mVTHighVideo.setBackgroundColor(Color.BLACK);
            } else {
                mVTLowVideo.setBackgroundColor(Color.BLACK);
            }
        }
    }

    private boolean okToRecordVoice() {
        if (!FeatureOption.MTK_PHONE_VOICE_RECORDING 
                || DualTalkUtils.isSupportDualTalk) {
            return false;
        }
        return VTInCallScreenFlags.getInstance().mVTVideoConnected;
    }

    private PopupMenu constructPopupMenu(View anchorView) {
        if (null == mVTPopupMenu) {
            mVTPopupMenu = new PopupMenu(mInCallScreen, anchorView);
            mVTPopupMenu.inflate(R.menu.vt_incall_menu);
            mVTPopupMenu.setOnMenuItemClickListener(this);
        }
        setupMenuItems(mVTPopupMenu.getMenu());
        return mVTPopupMenu;
    }
    
    public boolean onMenuItemClick(MenuItem arg0) {
        return handleOnScreenMenuItemClick(arg0);
    }
    
    public boolean handleOnScreenMenuItemClick(MenuItem menuItem) {
        if (DBG) log("- handleOnScreenMenuItemClick: " + menuItem);
        if (DBG) log("  id: " + menuItem.getItemId());
        if (DBG) log("  title: '" + menuItem.getTitle() + "'");

        if (mInCallScreen == null) {
            log("handleOnScreenMenuItemClick(" + menuItem + "), but null mInCallScreen!");
            return true;
        }

        switch(menuItem.getItemId()) {
            case R.id.menu_switch_camera:
                onVTSwitchCameraClick();
                return true;
            case R.id.menu_take_peer_photo:
                onVTTakePeerPhotoClick();
                return true;
            case R.id.menu_hide_local_video:
                onVTHideMeClick();
                return true;
            case R.id.menu_swap_videos:
                onVTSwapVideos();
                return true;
            case R.id.menu_voice_record:
                onVoiceVideoRecordClick(menuItem);
                return true;
            case R.id.menu_video_setting:
                onVTInCallVideoSetting();
                return true;
            case R.id.audio_mode_speaker:
                mInCallScreen.switchInCallAudio(InCallScreen.InCallAudioMode.SPEAKER);
                return true;
            case R.id.audio_mode_earpiece:
            case R.id.audio_mode_wired_headset:
                // InCallAudioMode.EARPIECE means either the handset earpiece,
                // or the wired headset (if connected.)
                mInCallScreen.switchInCallAudio(InCallScreen.InCallAudioMode.EARPIECE);
                return true;
            case R.id.audio_mode_bluetooth:
                mInCallScreen.switchInCallAudio(InCallScreen.InCallAudioMode.BLUETOOTH);
                return true;
        }
        return false;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
        return handleOnScreenMenuItemClick(item);
    }

    public void initDialingSuccessVTState() {
        if (null != mBkgBitmapHandler) {
            mBkgBitmapHandler.forceUpdateBitmapBySetting();
        }
        if ((!PhoneApp.getInstance().isHeadsetPlugged()) && (!mInCallScreen.isBluetoothAvailable())) {
            PhoneUtils.turnOnSpeaker(mInCallScreen, true, true, false);
        }
        if (!VTSettingUtils.getInstance().mShowLocalMO) {
            onVTHideMeClick2();
        }
        setVTDisplayScreenMode(false);
    }
    
    public void initDialingVTState() {
        registerForVTPhoneStates();
        mVTCallBannerController.clearCallBannerInfo();
        if (null != mDialer) {
            if (mDialer.isOpened()) {
                log("initDialingVTState(): closeDialer");
                mDialer.closeDialer(true);
            }
        }
    }

    public void initCommonVTState() {
        if (null != mBkgBitmapHandler) {
            mBkgBitmapHandler.updateBitmapBySetting();
        }

        if (!VTInCallScreenFlags.getInstance().mVTHasReceiveFirstFrame) {
            updateVideoBkgDrawable();
        }

        if (PhoneUtils.isDMLocked()) {
            if (VDBG) log("- Now DM locked, VTManager.getInstance().lockPeerVideo() start");
            VTManager.getInstance().lockPeerVideo();
            if (VDBG) log("- Now DM locked, VTManager.getInstance().lockPeerVideo() end");
        }
        if (null != mVTPopupMenu) {
            mVTPopupMenu.dismiss();
        }
        dismissAudioModePopup();
        updateVTLocalPeerDisplay();
    }
    
    /**
     * Brings up the "Audio mode" popup.
     */
    private void showAudioModePopup() {
        if (DBG) log("showAudioModePopup()...");

        dismissAudioModePopup();

        if (null == mAudioModePopup) {
            mAudioModePopup = new PopupMenu(mInCallScreen /* context */,
                                            mVTAudio /* anchorView */);
            mAudioModePopup.getMenuInflater().inflate(R.menu.incall_audio_mode_menu,
                                                      mAudioModePopup.getMenu());
            mAudioModePopup.setOnMenuItemClickListener(this);
            mAudioModePopup.setOnDismissListener(this);
        }

        // Update the enabled/disabledness of menu items based on the
        // current call state.
        InCallControlState inCallControlState = mInCallScreen.getUpdatedInCallControlState();

        Menu menu = mAudioModePopup.getMenu();

        // TODO: Still need to have the "currently active" audio mode come
        // up pre-selected (or focused?) with a blue highlight.  Still
        // need exact visual design, and possibly framework support for this.
        // See comments below for the exact logic.

        MenuItem speakerItem = menu.findItem(R.id.audio_mode_speaker);
        speakerItem.setEnabled(inCallControlState.speakerEnabled);
        // TODO: Show speakerItem as initially "selected" if
        // inCallControlState.speakerOn is true.

        // We display *either* "earpiece" or "wired headset", never both,
        // depending on whether a wired headset is physically plugged in.
        MenuItem earpieceItem = menu.findItem(R.id.audio_mode_earpiece);
        MenuItem wiredHeadsetItem = menu.findItem(R.id.audio_mode_wired_headset);
        final boolean usingHeadset = PhoneApp.getInstance().isHeadsetPlugged();
        // earpieceItem should not be shown on tablet since there's no earpiece on tablet
        if (FeatureOption.MTK_TB_APP_CALL_FORCE_SPEAKER_ON == true)
        {
          earpieceItem.setVisible(false);
          earpieceItem.setEnabled(false);
        }
        else
        {
        earpieceItem.setVisible(!usingHeadset);
        earpieceItem.setEnabled(!usingHeadset);
        }
        wiredHeadsetItem.setVisible(usingHeadset);
        wiredHeadsetItem.setEnabled(usingHeadset);
        // TODO: Show the above item (either earpieceItem or wiredHeadsetItem)
        // as initially "selected" if inCallControlState.speakerOn and
        // inCallControlState.bluetoothIndicatorOn are both false.

        MenuItem bluetoothItem = menu.findItem(R.id.audio_mode_bluetooth);
        bluetoothItem.setEnabled(inCallControlState.bluetoothEnabled);
        // TODO: Show bluetoothItem as initially "selected" if
        // inCallControlState.bluetoothIndicatorOn is true.

        mAudioModePopup.show();

        // Unfortunately we need to manually keep track of the popup menu's
        // visiblity, since PopupMenu doesn't have an isShowing() method like
        // Dialogs do.
        mAudioModePopupVisible = true;
    }

    /**
     * Dismisses the "Audio mode" popup if it's visible.
     *
     * This is safe to call even if the popup is already dismissed, or even if
     * you never called showAudioModePopup() in the first place.
     */
    private void dismissAudioModePopup() {
        if (mAudioModePopup != null) {
            mAudioModePopup.dismiss();  // safe even if already dismissed
            mAudioModePopup = null;
            mAudioModePopupVisible = false;
        }
    }
    
    // PopupMenu.OnDismissListener implementation; see showAudioModePopup().
    // This gets called when the PopupMenu gets dismissed for *any* reason, like
    // the user tapping outside its bounds, or pressing Back, or selecting one
    // of the menu items.
    public void onDismiss(PopupMenu menu) {
        if (DBG) log("- onDismiss: " + menu);
        mAudioModePopupVisible = false;
    }
    
    /**
     * Refreshes the "Audio mode" popup if it's visible.  This is useful
     * (for example) when a wired headset is plugged or unplugged,
     * since we need to switch back and forth between the "earpiece"
     * and "wired headset" items.
     *
     * This is safe to call even if the popup is already dismissed, or even if
     * you never called showAudioModePopup() in the first place.
     */
    public void refreshAudioModePopup() {
        if (mAudioModePopup != null && mAudioModePopupVisible) {
            // Dismiss the previous one
            mAudioModePopup.dismiss();  // safe even if already dismissed
            // And bring up a fresh PopupMenu
            showAudioModePopup();
        }
    }
    
    /**
     * Handles a click on the "Audio mode" button.
     * - If bluetooth is available, bring up the "Audio mode" popup
     *   (which provides a 3-way choice between earpiece / speaker / bluetooth).
     * - If bluetooth is *not* available, just toggle between earpiece and
     *   speaker, with no popup at all.
     */
    private void handleAudioButtonClick() {
        InCallControlState inCallControlState = mInCallScreen.getUpdatedInCallControlState();
        if (inCallControlState.bluetoothEnabled) {
            if (DBG) log("- handleAudioButtonClick: 'popup menu' mode...");
            showAudioModePopup();
            // Here should be set audio button as unchecked for resolving the issue
            // that sometimes other toggle button checked state is not right
            mVTAudio.setChecked(false);
        } else {
            if (DBG) log("- handleAudioButtonClick: 'speaker toggle' mode...");
            mInCallScreen.toggleSpeaker();
        }
    }
    
    /**
     * Updates the onscreen "Audio mode" button based on the current state.
     *
     * - If bluetooth is available, this button's function is to bring up the
     *   "Audio mode" popup (which provides a 3-way choice between earpiece /
     *   speaker / bluetooth).  So it should look like a regular action button,
     *   but should also have the small "more_indicator" triangle that indicates
     *   that a menu will pop up.
     *
     * - If speaker (but not bluetooth) is available, this button should look like
     *   a regular toggle button (and indicate the current speaker state.)
     *
     * - If even speaker isn't available, disable the button entirely.
     */
    private void updateAudioButton(InCallControlState inCallControlState) {
        if (DBG) log("updateAudioButton()...");

        // The various layers of artwork for this button come from
        // btn_compound_audio.xml.  Keep track of which layers we want to be
        // visible:
        //
        // - This selector shows the blue bar below the button icon when
        //   this button is a toggle *and* it's currently "checked".
        boolean showToggleStateIndication = false;
        //
        // - This is visible if the popup menu is enabled:
        boolean showMoreIndicator = false;
        //
        // - Foreground icons for the button.  Exactly one of these is enabled:
        boolean showSpeakerIcon = false;
        boolean showHandsetIcon = false;
        boolean showBluetoothIcon = false;

        if (inCallControlState.bluetoothEnabled) {
            if (DBG) log("- updateAudioButton: 'popup menu action button' mode...");

            mVTAudio.setEnabled(true);

            // The audio button is NOT a toggle in this state.  (And its
            // setChecked() state is irrelevant since we completely hide the
            // btn_compound_background layer anyway.)
            mVTAudio.setChecked(false);


            // Update desired layers:
            showMoreIndicator = true;
            if (inCallControlState.bluetoothIndicatorOn) {
                showBluetoothIcon = true;
            } else if (inCallControlState.speakerOn) {
                showSpeakerIcon = true;
            } else {
                showHandsetIcon = true;
                // TODO: if a wired headset is plugged in, that takes precedence
                // over the handset earpiece.  If so, maybe we should show some
                // sort of "wired headset" icon here instead of the "handset
                // earpiece" icon.  (Still need an asset for that, though.)
            }
        } else if (inCallControlState.speakerEnabled) {
            if (DBG) log("- updateAudioButton: 'speaker toggle' mode...");

            mVTAudio.setEnabled(true);

            // The audio button *is* a toggle in this state, and indicates the
            // current state of the speakerphone.
            mVTAudio.setChecked(inCallControlState.speakerOn);

            // Update desired layers:
            showToggleStateIndication = true;
            showSpeakerIcon = true;
        } else {
            if (DBG) log("- updateAudioButton: disabled...");

            // The audio button is a toggle in this state, but that's mostly
            // irrelevant since it's always disabled and unchecked.
            mVTAudio.setEnabled(false);
            mVTAudio.setChecked(false);

            // Update desired layers:
            showToggleStateIndication = true;
            showSpeakerIcon = true;
        }

        // Finally, update the drawable layers (see btn_compound_audio.xml).

        // Constants used below with Drawable.setAlpha():
        final int HIDDEN = 0;
        final int VISIBLE = 255;

        LayerDrawable layers = (LayerDrawable) mVTAudio.getBackground();
        if (DBG) log("- 'layers' drawable: " + layers);

        // Below layer should not be set alpha, or else, other button who use
        // same drawable will be influenced
        //layers.findDrawableByLayerId(R.id.compoundBackgroundItem)
                //.setAlpha(showToggleStateIndication ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.moreIndicatorItem)
                .setAlpha(showMoreIndicator ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.bluetoothItem)
                .setAlpha(showBluetoothIcon ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.handsetItem)
                .setAlpha(showHandsetIcon ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.speakerphoneItem)
                .setAlpha(showSpeakerIcon ? VISIBLE : HIDDEN);
    }
    
    private void stopVideoRecord() {
        log("stopVideoRecorder() ...");
        PhoneRecorderHandler.getInstance().stopVideoRecord();
        updateVideoCallRecordState(Recorder.IDLE_STATE);
    }
    
    public void stopRecord() {
        log("stopRecord");
        if (PhoneRecorder.isRecording()) {
            log("stopVoiceRecord");
            PhoneRecorderHandler.getInstance().stopVoiceRecord();
        } else if (PhoneRecorder.RECORDING_STATE == PhoneRecorderHandler.getInstance().getPhoneRecorderState()) {
            log("stopVideoRecord");
            stopVideoRecord();
        }
    }

    private void startRecord(int type) {
        log("startVTRecorder() ...");
        long sdMaxSize = PhoneUtils.getDiskAvailableSize() - Constants.PHONE_RECORD_LOW_STORAGE_THRESHOLD;
        if (sdMaxSize > 0) {
            if (Constants.PHONE_RECORDING_TYPE_ONLY_VOICE == type) {
                if (!PhoneRecorder.getInstance(PhoneApp.getInstance()).ismFlagRecord()) {
                    log("startRecord");
                    //mInCallScreen.startRecord(Constants.PHONE_RECORDING_VIDEO_CALL_CUSTOM_VALUE);
                    PhoneRecorderHandler.getInstance().startVoiceRecord(Constants.PHONE_RECORDING_VIDEO_CALL_CUSTOM_VALUE);
                }
            } else if (type > 0) {
                PhoneRecorderHandler.getInstance().startVideoRecord(type, sdMaxSize,
                                                                    Constants.PHONE_RECORDING_VIDEO_CALL_CUSTOM_VALUE);
                updateVideoCallRecordState(Recorder.RECORDING_STATE);
            }
        } else if (-1 == sdMaxSize) {
            showToast(getResources().getString(R.string.vt_sd_null));
        } else {
            showToast(getResources().getString(R.string.vt_sd_not_enough));
        }
    }

    private void showStartVTRecorderDialog() {
        log("showStartVTRecorderDialog() ...");
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(mInCallScreen);
        myBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mVTRecorderSelector != null) {
                            mVTRecorderSelector.dismiss();
                            mVTRecorderSelector = null;
                        }
                    }
                });
        myBuilder.setTitle(R.string.vt_recorder_start);

        if (mVTRecorderEntries == null)
            mVTRecorderEntries = new ArrayList<String>();
        else
            mVTRecorderEntries.clear();

        if (PhoneUtils.isSupportFeature("VT_VIDEO_RECORDING") &&
                PhoneUtils.isSupportFeature("VT_VIDEO_RECORDING"))
            mVTRecorderEntries.add(getResources().getString(
                    R.string.vt_recorder_voice_and_peer_video));
        if (PhoneUtils.isSupportFeature("VT_VOICE_RECORDING"))
            mVTRecorderEntries.add(getResources().getString(
                    R.string.vt_recorder_only_voice));
        if (PhoneUtils.isSupportFeature("VT_VIDEO_RECORDING"))
            mVTRecorderEntries.add(getResources().getString(
                    R.string.vt_recorder_only_peer_video));

        DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mVTRecorderSelector != null) {
                    mVTRecorderSelector.dismiss();
                    mVTRecorderSelector = null;
                }

                String currentString = mVTRecorderEntries.get(which);
                int type = 0;
                
                if (currentString.equals(getResources().getString(
                        R.string.vt_recorder_voice_and_peer_video))) {
                    log("The choice of start VT recording : voice and peer video");
                    type = Constants.PHONE_RECORDING_TYPE_VOICE_AND_PEER_VIDEO;
                } else if (currentString.equals(getResources().getString(
                        R.string.vt_recorder_only_voice))) {
                    log("The choice of start VT recording : only voice");
                    type = Constants.PHONE_RECORDING_TYPE_ONLY_VOICE;
                } else if (currentString.equals(getResources().getString(
                        R.string.vt_recorder_only_peer_video))) {
                    log("The choice of start VT recording : only peer video");
                    type = Constants.PHONE_RECORDING_TYPE_ONLY_PEER_VIDEO;
                } else {
                    log("The choice of start VT recording : wrong string");
                    return;
                }
                startRecord(type);
            }
        };

        myBuilder.setSingleChoiceItems(mVTRecorderEntries
                .toArray(new CharSequence[mVTRecorderEntries.size()]), -1,
                myClickListener);

        mVTRecorderSelector = myBuilder.create();
        mVTRecorderSelector.show();
    }

    private void onVoiceVideoRecordClick(MenuItem menuItem) {
        log("onVoiceVideoRecordClick");
        String title = menuItem.getTitle().toString();
        if (title == null) {
            return;
        }
        if (!PhoneUtils.isExternalStorageMounted()) {
            Toast.makeText(mInCallScreen, getResources().getString(R.string.error_sdcard_access), Toast.LENGTH_LONG).show();
            return;
        }
        if (!PhoneUtils.diskSpaceAvailable(Constants.PHONE_RECORD_LOW_STORAGE_THRESHOLD)) {
            mInCallScreen.handleStorageFull(true); // true for checking case
            return;
        }

        if (title.equals(mInCallScreen.getString(R.string.start_record_vt))) {
            log("want to startRecord");
            if (PhoneRecorder.IDLE_STATE == PhoneRecorderHandler.getInstance().getPhoneRecorderState()) {
                log("startRecord");
                showStartVTRecorderDialog();
            }
        } else if (title.equals(mInCallScreen.getString(R.string.stop_record_vt))) {
            stopRecord();
        }
    }
    
    public void onReceiveVTManagerStartCounter() {
        if (VTInCallScreenFlags.getInstance().mVTConnectionStarttime.mStarttime < 0) {
            Call call = mCM.getActiveFgCall();
            if (mCM.hasActiveRingingCall()) {
                call = mCM.getFirstActiveRingingCall();
            }
            mInCallScreen.triggerTimerStartCount(call);

            if (null != mCM.getActiveFgCall()) {
                if (mCM.getActiveFgCall().getLatestConnection() != null) {
                        VTInCallScreenFlags.getInstance().mVTConnectionStarttime.mStarttime = SystemClock.elapsedRealtime();
                        VTInCallScreenFlags.getInstance().mVTConnectionStarttime.mStartDate = System.currentTimeMillis();
                        VTInCallScreenFlags.getInstance().mVTConnectionStarttime.mConnection = mCM.getActiveFgCall().getLatestConnection();
                        //VTInCallScreenFlags.getInstance().mVTInTiming = true;
                        PhoneApp.getInstance().notificationMgr.updateInCallNotification();
                        if (null != mVTCallBannerController) {
                            mVTCallBannerController.updateState(mCM.getActiveFgCall());
                        }
                }
            }
        }
    }
    
    private void onReceiveVTManagerReady() {
        VTInCallScreenFlags.getInstance().mVTVideoReady = true;
        updateVTScreen(getVTScreenMode());
        
        if (DBG) log("Incallscreen, before call setting");
        //To set HideYou
        if (VTSettingUtils.getInstance().mToReplacePeer) {
            VTManager.getInstance().enableHideYou(1);
        } else {
            VTManager.getInstance().enableHideYou(0);
        }
        //To set HideMe
        if (VTSettingUtils.getInstance().mShowLocalMO) {
            VTManager.getInstance().enableHideMe(0);
        } else {
            VTManager.getInstance().enableHideMe(1);
        }
        //To set incoming video display
        if (VTSettingUtils.getInstance().mShowLocalMT.equals("0")) {
            VTManager.getInstance().incomingVideoDispaly(0);
        } else if (VTSettingUtils.getInstance().mShowLocalMT.equals("1")) {
            VTManager.getInstance().incomingVideoDispaly(1);
        } else {
            VTManager.getInstance().incomingVideoDispaly(2);
        }
        if (DBG) log("Incallscreen, after call setting");
        
        if (!PhoneUtils.isDMLocked()) {

            if (DBG)
                log("Now DM not locked, VTManager.getInstance().unlockPeerVideo() start;");
            VTManager.getInstance().unlockPeerVideo();
            if (DBG)
                log("Now DM not locked, VTManager.getInstance().unlockPeerVideo() end;");

            if (VTSettingUtils.getInstance().mShowLocalMT.equals("1")
                    && VTInCallScreenFlags.getInstance().mVTIsMT) {
                if (DBG)
                    log("- VTSettingUtils.getInstance().mShowLocalMT : 1 !");

                if (DBG) log("Incallscreen, before enableAlwaysAskSettings");
                VTManager.getInstance().enableAlwaysAskSettings(1);
                if (DBG) log("Incallscreen, after enableAlwaysAskSettings");
                
                mVTMTAsker = new AlertDialog.Builder(PhoneApp.getInstance().getInCallScreenInstance())
                        .setMessage(getResources().getString(R.string.vt_ask_show_local))
                        .setPositiveButton(getResources().getString(R.string.vt_ask_show_local_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int which) {
                                        if (DBG) log(" user select yes !! ");

                                        if (DBG) log("Incallscreen, before userSelectYes");
                                        VTManager.getInstance().userSelectYes(1);
                                        if (DBG) log("Incallscreen, after userSelectYes");
                                        
                                        if (mVTMTAsker != null) {
                                            mVTMTAsker.dismiss();
                                            mVTMTAsker = null;
                                        }
                                        VTSettingUtils.getInstance().mShowLocalMT = "0";
                                        onVTHideMeClick();
                                        return;
                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.vt_ask_show_local_no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        if (DBG) log(" user select no !! ");
                                        
                                        if (DBG) log("Incallscreen, before userSelectYes");
                                        VTManager.getInstance().userSelectYes(0);
                                        if (DBG) log("Incallscreen, after userSelectYes");
                                        
                                        if (mVTMTAsker != null) {
                                            mVTMTAsker.dismiss();
                                            mVTMTAsker = null;
                                        }
                                        VTSettingUtils.getInstance().mShowLocalMT = "2";
                                        return;
                                    }
                                }).setOnCancelListener(
                                new DialogInterface.OnCancelListener() {
                                    public void onCancel(DialogInterface arg0) {
                                        if (DBG) log(" user no selection , default show !! ");
                                        
                                        if (DBG) log("Incallscreen, before userSelect default");
                                        VTManager.getInstance().userSelectYes(2);
                                        if (DBG) log("Incallscreen, after userSelect default");
                                        
                                        if (mVTMTAsker != null) {
                                            mVTMTAsker.dismiss();
                                            mVTMTAsker = null;
                                        }
                                        VTSettingUtils.getInstance().mShowLocalMT = "0";
                                        onVTHideMeClick();
                                        return;
                                    }
                                }).create();
                mVTMTAsker.show();

                new DialogCancelTimer(WAITING_TIME_FOR_ASK_VT_SHOW_ME, mVTMTAsker)
                        .start();
            }
        }
    }
    
    public void setVTDisplayScreenMode(final boolean isFullScreenMode){
        log("setVTDisplayScreenMode(), isFullScreenMode is " + isFullScreenMode);
        if (isFullScreenMode) {
            VTInCallScreenFlags.getInstance().mVTFullScreen = true;
            mCallBanner.setVisibility(View.INVISIBLE);
            mVTMute.setVisibility(View.INVISIBLE);
            mVTHangUpWrapper.setVisibility(View.INVISIBLE);
            mVTDialpad.setVisibility(View.INVISIBLE);
            mVTAudio.setVisibility(View.INVISIBLE);
            mVTOverflowMenu.setVisibility(View.INVISIBLE);
            mVTSwapVideo.setVisibility(View.INVISIBLE);
            mVTLowVideo.setBackgroundColor(Color.BLACK);
        } else {
            VTInCallScreenFlags.getInstance().mVTFullScreen = false;
            mCallBanner.setVisibility(View.VISIBLE);
            mVTMute.setVisibility(View.VISIBLE);
            mVTHangUpWrapper.setVisibility(View.VISIBLE);
            mVTDialpad.setVisibility(View.VISIBLE);
            mVTAudio.setVisibility(View.VISIBLE);
            if (ViewConfiguration.get(mInCallScreen).hasPermanentMenuKey()) {
                mVTSwapVideo.setVisibility(View.VISIBLE);
            } else {
                mVTOverflowMenu.setVisibility(View.VISIBLE);
            }
            mVTLowVideo.setBackgroundDrawable(null);
        }
    }
    
    private void updateVTLocalPeerDisplay() {
        if (VTInCallScreenFlags.getInstance().mVTPeerBigger) {
            VTManager.getInstance().setDisplay(mLowVideoHolder, mHighVideoHolder);
        } else {
            VTManager.getInstance().setDisplay(mHighVideoHolder, mLowVideoHolder);
        }
    }
    
    private void showToast(String string) {
        Toast.makeText(PhoneApp.getInstance(), string, Toast.LENGTH_LONG).show();
    }
    
    public void NotifyLocaleChange() {
        mVTCallBannerController.setNeedClearUserData(true);
    }

    private void dismissVideoSettingDialogs() {
        if (mInCallVideoSettingDialog != null) {
            mInCallVideoSettingDialog.dismiss();
            mInCallVideoSettingDialog = null;
        }
        if (mInCallVideoSettingLocalEffectDialog != null) {
            mInCallVideoSettingLocalEffectDialog.dismiss();
            mInCallVideoSettingLocalEffectDialog = null;
        }
        if (mInCallVideoSettingLocalNightmodeDialog != null) {
            mInCallVideoSettingLocalNightmodeDialog.dismiss();
            mInCallVideoSettingLocalNightmodeDialog = null;
        }
        if (mInCallVideoSettingPeerQualityDialog != null) {
            mInCallVideoSettingPeerQualityDialog.dismiss();
            mInCallVideoSettingPeerQualityDialog = null;
        }
    }

    private void debugVTUIInfo() {
        if (DBG)
            log("debugVTUIInfo : output the Visibility info : ");
        if (DBG)
            log(" - debugVTUIInfo : mVTMute - " + mVTMute.getVisibility());
        if (DBG)
            log(" - debugVTUIInfo : mVTAudio - " + mVTAudio.getVisibility());
        if (DBG)
            log(" - debugVTUIInfo : mVTDialpad - " + mVTDialpad.getVisibility());
        if (DBG)
            log(" - debugVTUIInfo : mVTSwapVideo - " + mVTSwapVideo.getVisibility());
        if (DBG)
            log(" - debugVTUIInfo : mVTHangUp - " + mVTHangUp.getVisibility());
        
        if (DBG)
            log("debugVTUIInfo : output the enable info : ");
        if (DBG)
            log(" - debugVTUIInfo : mVTMute - " + mVTMute.isEnabled());
        if (DBG)
            log(" - debugVTUIInfo : mVTAudio - " + mVTAudio.isEnabled());
        if (DBG)
            log(" - debugVTUIInfo : mVTDialpad - " + mVTDialpad.isEnabled());
        if (DBG)
            log(" - debugVTUIInfo : mVTSwapVideo - " + mVTSwapVideo.isEnabled());
        if (DBG)
            log(" - debugVTUIInfo : mVTHangUp - " + mVTHangUp.isEnabled());
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
