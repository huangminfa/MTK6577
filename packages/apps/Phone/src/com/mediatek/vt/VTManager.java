package com.mediatek.vt;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.AsyncResult;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.gemini.*;
import com.android.internal.telephony.CallManager;
import com.mediatek.featureoption.FeatureOption;
import android.content.ContentResolver;
import android.content.ContentValues;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.media.MediaScannerConnection;
import android.os.storage.StorageManager;


public class VTManager {
	public enum State {
		CLOSE, OPEN, READY, CONNECTED
	}

	static final String TAG = "VTManager";

	public static final int RECORD_TYPE_VIDEO_AUDIO = 1;
	public static final int RECORD_TYPE_AUDIO_ONLY = 2;
	public static final int RECORD_TYPE_VIDEO_ONLY = 3;

	/*****
	* VT Manager's State
	*/
	public static final int SET_VT_CLOSE = 0;
	public static final int SET_VT_OPEN = 1;
	public static final int SET_VT_READY = 2;
	public static final int QUIT_THREAD = 0x8000000;
	public static final int VT_MSG_VTSTATUS = 0x6;
	public static final int VT_MSG_RING = 0x7;

	/*
	 * begin, MSG or ERROR may need MMI to handle, same as
	 * vt_native_msg_to_mmi.h
	 */
	public static final int VT_MSG_CLOSE = 0x0001;
	public static final int VT_MSG_OPEN = 0x0002;
	public static final int VT_MSG_READY = 0x0003;
	public static final int VT_MSG_CONNECTED = 0x0004;
	public static final int VT_MSG_DISCONNECTED = 0x0005;
	public static final int VT_MSG_EM_INDICATION = 0x0006;
	public static final int VT_MSG_START_COUNTER = 0x0007;
	public static final int VT_MSG_RECEIVE_FIRSTFRAME = 0x0008;
	public static final int VT_MSG_PEER_CAMERA_OPEN = 0x0009;
	public static final int VT_MSG_PEER_CAMERA_CLOSE = 0x0010;	
	public static final int VT_MSG_CAM_BEGIN = 0x1000;

	public static final int VT_ERROR_CALL_DISCONNECT = 0x8001;
	public static final int VT_ERROR_START_VTS_FAIL = 0x8002;
	public static final int VT_ERROR_CAMERA = 0x8003;
	public static final int VT_ERROR_MEDIA_SERVER_DIED = 0x8004;
	public static final int VT_ERROR_MEDIA_RECORDER_EVENT_INFO = 0x8005;
	public static final int VT_ERROR_MEDIA_RECORDER_EVENT_ERROR = 0x8006;
	public static final int VT_ERROR_MEDIA_RECORDER_COMPLETE = 0x8007;
    public static final int VT_NORMAL_END_SESSION_COMMAND = 0x8101;
	/* end, MSG or ERROR may need MMI to handle */

	private static final int VIDEO_TYPE_CAMERA = 0;
	private static final int VIDEO_TYPE_IMAGE = 1;
	private static final int VIDEO_TYPE_LAST_SHOT = 2;

	public static final int VT_VQ_SHARP = 0;
	public static final int VT_VQ_NORMAL = 1;
	public static final int VT_VQ_SMOOTH = 2;

	public static final int VT_RET_FROM_JNI_TRUE = 0;
	public static final int VT_RET_FROM_JNI_FALSE = 1;

	State mState = State.CLOSE;

	public State getState() {
		return mState;
	}

	public Handler getmVTListener()
	{
		return mVTListener;
	}

	Context mContext;
	// Phone mPhone;
	// Because MTKCallManager is not inherited from CallManager
	// so use they common parent Object as member data
	Object mCallManager;

	VTSettings mSettings;

	Handler mVTListener = null;

	Thread mVTThread;
	Handler mVtHandler = null;
	Thread mTelMsgThread;
	Handler mTelMsgHandler = null;

	private Integer mVTListenerLock = new Integer(0);
	private Integer mEndCallLock = new Integer(0);

	private boolean invokeHideMeBeforeOpen = false;
	private boolean invokeLockPeerVideoBeforeOpen = false;
	private boolean closingVTService = false;
	private boolean bStartVTSMALFail = false;
	
	// added for enabling replace peer video
	private int bEnableReplacePeerVideo;
	private String sReplacePeerVideoPicturePath;

	//For for recording feature
	private String recordedFilePath = null;
	private String recordedFileName = null;
	private long dateTakenRecording = 0;

	public void replacePeerVideoSettings(int bEnableReplacePeerVideo, String sReplacePeerVideoPicturePath) {
		Log.i(TAG, "replacePeerVideoSettings");
		this.bEnableReplacePeerVideo = bEnableReplacePeerVideo;
		this.sReplacePeerVideoPicturePath = sReplacePeerVideoPicturePath;
		VTelProvider.replacePeerVideoSettings(bEnableReplacePeerVideo, sReplacePeerVideoPicturePath);
	}

	void createThreads() {
		if (mVtHandler != null || mTelMsgHandler != null) {
			Log.e(TAG, "init error");
			return;
		}

		mVTThread = new Thread()
		{
			@Override
			public void run()
			{
				Log.i(TAG, "run(), mVTThread");
				Looper.prepare();
				synchronized(mVTThread)
				{
					mVtHandler = new VTHanlder();
					Log.i(TAG, "mVTThread.notify()");
					mVTThread.notify();
				}

				Looper.loop();
			}
		};
		mVTThread.start();

		mTelMsgThread = new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				synchronized (this) {
					mTelMsgHandler = new TelMsgHandler();
					notify();
				}

				Log.i(TAG, "register TelMsg, notify--sync");
				if (FeatureOption.MTK_GEMINI_SUPPORT) {
					((MTKCallManager) mCallManager).registerForVtStatusInfoGemini(mTelMsgHandler, VT_MSG_VTSTATUS, null, Phone.GEMINI_SIM_1);
					((MTKCallManager) mCallManager).registerForVtRingInfoGemini(mTelMsgHandler, VT_MSG_RING, null, Phone.GEMINI_SIM_1);
					if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
						((MTKCallManager) mCallManager).registerForVtStatusInfoGemini(mTelMsgHandler, VT_MSG_VTSTATUS, null, Phone.GEMINI_SIM_2);
						((MTKCallManager) mCallManager).registerForVtRingInfoGemini(mTelMsgHandler, VT_MSG_RING, null, Phone.GEMINI_SIM_2);
					}
				} else {
					((CallManager) mCallManager).registerForVtStatusInfo(mTelMsgHandler, VT_MSG_VTSTATUS, null);
					((CallManager) mCallManager).registerForVtRingInfo(mTelMsgHandler, VT_MSG_RING, null);
				}
				Looper.loop();
			}
		};

		//To make sure that mVtHandler is not null.
		synchronized(mVTThread)
		{
			if (mVtHandler == null)
			{
				try
				{
					mVTThread.wait();
				}
				catch (InterruptedException e)
				{
					Log.e(TAG, "createThreads, wait error");
					e.printStackTrace();
				}
			}
		}
		
		Log.i(TAG, "mVtHandler.sendEmptyMessage()");
		mVtHandler.sendEmptyMessage(SET_VT_OPEN);
		mTelMsgThread.start();
	}

	void joinThreads() {
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			((MTKCallManager) mCallManager).unregisterForVtStatusInfoGemini(mTelMsgHandler, Phone.GEMINI_SIM_1);
			((MTKCallManager) mCallManager).unregisterForVtRingInfoGemini(mTelMsgHandler, Phone.GEMINI_SIM_1);
			if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
				((MTKCallManager) mCallManager).unregisterForVtStatusInfoGemini(mTelMsgHandler, Phone.GEMINI_SIM_2);
				((MTKCallManager) mCallManager).unregisterForVtRingInfoGemini(mTelMsgHandler, Phone.GEMINI_SIM_2);
			}
		} else {
			((CallManager) mCallManager).unregisterForVtStatusInfo(mTelMsgHandler);
			((CallManager) mCallManager).unregisterForVtRingInfo(mTelMsgHandler);
		}

		mVtHandler.sendEmptyMessage(QUIT_THREAD);

		if(null == mTelMsgHandler)
		{
			Log.i(TAG, "null == mTelMsgHandler in joinThreads()");
		}
		else
		{
			Log.i(TAG, "null != mTelMsgHandler in joinThreads()");
			mTelMsgHandler.sendEmptyMessage(QUIT_THREAD);
		}

		//If user hang up the VT call too quickly, mTelMsgHandler maybe be waiting for VTManager's ready always.
		//It's needed to notify mTelMsgHandler.
		synchronized(mTelMsgHandler)
		{
			try
			{
				Log.i(TAG, "mTelMsgHandler notify in joinThreads()");
				mTelMsgHandler.notify();
			}
			catch (Exception e)
			{
				Log.e(TAG, "mTelMsgHandler notify in joinThreads(), wait error");
				e.printStackTrace();
			}
		}

		try {
			mVTThread.join();
			mTelMsgThread.join();
		} catch (InterruptedException e) {
			Log.e(TAG, "joinThreads error");
			e.printStackTrace();
		}
		mVTThread = null;
		mVtHandler = null;

		mTelMsgThread = null;
		mTelMsgHandler = null;

	}

	private static VTManager sVTManager = new VTManager();

	public static VTManager getInstance() {
		return sVTManager;
	}

	private VTManager() {
		mState = State.CLOSE;
		mSettings = new VTSettings();
	}

	public void init(Context context, Object callManager) {
		mContext = context;
		mCallManager = callManager;
		mSettings.init(context);
		createThreads();
	}

	public void deinit() {
		joinThreads();
		mSettings.deinit();
		mContext = null;
		mCallManager = null;
	}

	/**
	 * Open VT manager.
	 */
	public void setVTOpen(Context context, Object callManager) {
		Log.i(TAG, "setVTOpen");
		if (mState != State.CLOSE) {
			Log.e(TAG, "setVTOpen, mState != State.CLOSE");
			return;
		}
		init(context, callManager);
		closingVTService = false;
	}

	private synchronized void setVTOpenImpl() {
		int ret = VTelProvider.openVTSerice();
		if (0 != ret) {
			Log.e(TAG, "setVTOpenImpl, error");
			return;
		}
		mState = State.OPEN;
		// todo remove this
		this.notify();
		Log.i(TAG, mState.toString());
		Log.i(TAG, ""+invokeHideMeBeforeOpen);
		if (invokeHideMeBeforeOpen) {
			setLocalVideoType(mSettings.getVideoType(), mSettings.getImagePath());
		}

		/**
		if(invokeLockPeerVideoBeforeOpen) {
			lockPeerVideo();
		}
		**/

	}

	/**
	 * set VT manager ready for sending and receiving video data.
	 */
	public synchronized void setVTReady() {
		Log.i(TAG, "setVTReady, mVtHandler = " + mVtHandler);
		if ((State.OPEN != mState) && (State.CLOSE != mState)) {
			Log.e(TAG, "setVTReadyImpl, error");
			return;
		}
                if (null != mVtHandler) {
		    mVtHandler.sendEmptyMessage(SET_VT_READY);
                }
	}

	private synchronized void setVTReadyImpl() {
		Log.i(TAG, "setVTReadyImpl, mTelMsgHandler = " + mTelMsgHandler);
		int ret = 0;
		if (mSettings.getIsSwitch()) {
			ret = VTelProvider.initVTService(mSettings.getPeerSurface().getSurface(), mSettings.getLocalSurface().getSurface());
		} else {
			ret = VTelProvider.initVTService(mSettings.getLocalSurface().getSurface(), mSettings.getPeerSurface().getSurface());
		}

		if (mTelMsgHandler == null)
		{
			synchronized(mTelMsgThread) {
				try {
					Log.i(TAG, "setVTReadyImpl mTelMsgThread wait ");
					mTelMsgThread.wait();
				} catch (InterruptedException e) {
					Log.e(TAG, "setVTReadyImpl, wait error");
					e.printStackTrace();
				}
			}
		}
		if (0 != ret) {
			bStartVTSMALFail = true;
			Log.e(TAG, "setVTReadyImpl, error");
            //postEventToMMI(VT_ERROR_START_VTS_FAIL, 0, 0, null);//234064
			synchronized(mTelMsgHandler) {
				mTelMsgHandler.notify();
			}
			return;
		}
		mState = State.READY;		
		Log.i(TAG, mState.toString());
		mSettings.getCameraSettings();
		postEventToMMI(VT_MSG_READY, 0, 0, null);
		synchronized(mTelMsgHandler) {
			mTelMsgHandler.notify();
		}
	}

	/**
	 * set VT manager close
	 */
	public void setVTClose() {
		Log.i(TAG, "setVTClose");
		if (State.CLOSE == mState) {
			Log.e(TAG, "setVTCloseImpl, error");
			return;
		}
		mVtHandler.sendEmptyMessage(SET_VT_CLOSE);
		deinit();
	}

	private synchronized void setVTCloseImpl() {
		Log.i(TAG, "setVTCloseImpl");
		while (mState == State.CONNECTED) {
			try {
				wait();
			} catch (InterruptedException e) {
				Log.e(TAG, "createThreads, wait error");
				e.printStackTrace();
				break;
			}
		}

		/*****
		 * send a message VT_MSG_CLOSE to MMI.
		 */
		postEventToMMI(VT_MSG_CLOSE, 0, 0, null);

		// added for closing VT service
		closingVTService = true;

		synchronized (mEndCallLock) {
			int ret = VTelProvider.closeVTService();
			if (0 != ret) {
				Log.e(TAG, "setVTCloseImpl, error");
				return;
			}
			mState = State.CLOSE;
			bStartVTSMALFail = false;
			Log.i(TAG, mState.toString());
		}
	}

	public synchronized void onConnected() {
		Log.i(TAG, "onConnected");
		if (State.CONNECTED == mState)
			return;
		if (State.CLOSE == mState) {
			Log.e(TAG, "onConnected, error");
			return;
		}

		/*
		if (State.READY != mState) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				Log.e(TAG, "onConnected, wait error");
				e.printStackTrace();
			}
		}*/
		
		int ret = VTelProvider.startVTService();
		if (0 != ret) {
			Log.e(TAG, "onConnected, error");
			return;
		}
		mState = State.CONNECTED;
		Log.i(TAG, mState.toString());
		postEventToMMI(VT_MSG_CONNECTED, 0, 0, null);
	}

    public void onDisconnected() {
		Log.i(TAG, "pre-onDisconnected");
		VTelProvider.setEndCallFlag();
		onDisconnectedActual();
	}

	public synchronized void onDisconnectedActual() {
		Log.i(TAG, "onDisconnected");
		if (State.CONNECTED != mState) {
			Log.e(TAG, "onDisconnected, VT Manager state error");
			return;
		}
		int ret = VTelProvider.stopVTService();
		if (0 != ret) {
			Log.e(TAG, "onDisconnected, error");
			return;
		}
		notify();
		mState = State.READY;
		Log.i(TAG, mState.toString());
		postEventToMMI(VT_MSG_DISCONNECTED, 0, 0, null);
	}

	public synchronized void setVTDisconnect() {
		Message msg = new Message();
		msg.what = VT_MSG_VTSTATUS;
		msg.obj = null;
		// arg1 0, indicate is VT disconnect
		msg.arg1 = 0;
		msg.arg2 = 0;
		mTelMsgHandler.sendMessage(msg);
	}

	/**
	 * Dial a number.
	 * 
	 * @param number
	 *            the number to be dialed.
	 */
	/*
	 * Connection vtDial(String number) throws CallStateException { if
	 * (State.CLOSE == mState || null == mCallManager) return null; if
	 * (FeatureOption.MTK_GEMINI_SUPPORT) { return ((MTKCallManager)
	 * mCallManager).vtDial(number); } else { return ((CallManager)
	 * mCallManager).vtDial(number); }
	 * 
	 * 
	 * }
	 */

	public void registerVTListener(Handler h) {
		synchronized (mVTListenerLock) {
			mVTListener = h;
		}
	}

	public void unregisterVTListener() {
		synchronized (mVTListenerLock) {
			mVTListener = null;
		}
	}

	private class VTHanlder extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SET_VT_CLOSE:
				setVTCloseImpl();
				break;
			case SET_VT_OPEN:
				setVTOpenImpl();
				break;
			case SET_VT_READY:
				setVTReadyImpl();
				break;
			case QUIT_THREAD:
				Looper.myLooper().quit();
				break;
			default:
				break;
			}
		}
	}

	private class TelMsgHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case VT_MSG_VTSTATUS:
			{
				boolean isDisconent = (msg.arg1 == 0);
				AsyncResult ar = (AsyncResult) msg.obj;
				if (null != ar) {
					isDisconent = ((int[]) ar.result)[0] != 0;
				}
				if (isDisconent) {
					//onDisconnected();
				} else{
					/***
					* When receiving connected command from network and State is Close then the system
					* should wait until invoking setVTOpenImpl(). When receiving connected command from 
					* network and State is OPEN and bStartVTSMALFail is false then the system wait until
					* invoking setVTReadyImpl().
					*
					*/
					if(State.CLOSE == mState || (State.OPEN== mState && !bStartVTSMALFail)) {
						synchronized(mTelMsgHandler) {
							try {
								Log.i(TAG, "wait for setVTReadyImpl");
								mTelMsgHandler.wait();
							} catch (InterruptedException e) {
								Log.e(TAG, "wait for setVTReadyImpl, wait error");
								e.printStackTrace();
							}
						}
					}
					//If startVTSMal returns successfully, then invokes onConnected(), otherwise, does nothing.
					if(!bStartVTSMALFail) {
						onConnected();
					}
				}
				break;
			}

			case VT_MSG_RING:

				break;
			case QUIT_THREAD:
				Looper.myLooper().quit();
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Sets the SurfaceHolder to be used for local & peer video view
	 * 
	 * @param local
	 *            the SurfaceHolder upon which to place the local video
	 * @param peer
	 *            the SurfaceHolder upon which to place the peer video
	 */
	public void setDisplay(SurfaceHolder local, SurfaceHolder peer) {
		Log.i(TAG, "setDisplay " + local + ", " + peer);
		mSettings.setLocalSurface(local);
		mSettings.setPeerSurface(peer);
	}

	/**
	 * switch local and peer video display area
	 */
	public void switchDisplaySurface() {
		if (State.READY != mState) {
			Log.e(TAG, "switchDisplaySurface, error");
			return;
		}
		mSettings.setIsSwitch(!mSettings.getIsSwitch());
	}

	/**
	 * enlargeDisplaySurface
	 */
	public void enlargeDisplaySurface(boolean isEnlarge) {
		// todo
	}

	/**
	 * switch front and back camera
	 */
	public boolean switchCamera() {
		int ret = 0;
		synchronized (mEndCallLock) {
			// The switchCamera button can not be used if VTManager's closed.
			if (State.CLOSE == mState) {
				return false;
			}
			ret = VTelProvider.switchCamera();
			mSettings.getCameraSettings();
		}
		return (0 == ret) ? false : true;
	}

	/**
	 * Set the local video type
	 * 
	 * @param videoType
	 *            -> video / image / free me
	 * @param path
	 *            , if image==videoType, path specify image path
	 */
	public void setLocalVideoType(int videoType, String path) {
		Log.i(TAG, "setLocalVideoType, closingVTService = " + closingVTService + " mState = " + mState + " videoType = " + videoType + " path = " + path);
		if (path == null)
			Log.i(TAG, "setLocalVideoType, path is null");
		if (closingVTService) {
			synchronized (mEndCallLock) {
				mSettings.setVideoType(videoType);
				mSettings.setImagePath(path);
				if (State.CLOSE == mState) {
					setInvokeHideMeBeforeOpen(true);
					return;
				}
				VTelProvider.setLocalVideoType(videoType, path);
			}
		} else {
			mSettings.setVideoType(videoType);
			mSettings.setImagePath(path);
			if (State.CLOSE == mState) {
				setInvokeHideMeBeforeOpen(true);
				return;
			}
			VTelProvider.setLocalVideoType(videoType, path);
		}
	}

	/**
	 * photograph take from peer view and save to "my pictures" folder
	 */
	public boolean savePeerPhoto() {
		long dateTaken = System.currentTimeMillis();
		//String name = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken).toString();
		String name = new SimpleDateFormat("yyyy-MM-dd kk.mm.ss.SSS").format(dateTaken).toString();
		name = name + ".png";
		final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString();
		String path = CAMERA_IMAGE_BUCKET_NAME + "/DCIM/Camera/IMG_" + name;

		File image_directory = new File(CAMERA_IMAGE_BUCKET_NAME + "/DCIM/Camera/");
		// create directory anyway
		image_directory.mkdirs();

		int flag = VTelProvider.snapshot(0, path);
		if (flag != 0) {
			Log.i(TAG, "***snapshot() fail in Manager layer***");
			return false;
		}

		// add taken photo to media data base
		ContentResolver cr = mContext.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(Images.Media.TITLE, name);
		values.put(Images.Media.DISPLAY_NAME, name);
		values.put(Images.Media.DATE_TAKEN, dateTaken);
		values.put(Images.Media.MIME_TYPE, "image/png");
		values.put(Images.Media.ORIENTATION, 0);

		File image_File = new File(path);
		if (!image_File.exists()) {
			Log.i(TAG, "***image_File does not exist in Manager layer***");
		}
		long size = image_File.length();
		values.put(Images.Media.SIZE, size);
		values.put(Images.Media.DATA, path);
		Log.i(TAG, values.toString());
		cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
		return true;
	}

	void getParameters() {
		mSettings.mCameraParamters = VTelProvider.getParameters();
		// mSettings.mCameraParamters = new CameraParamters();
		// String s = ((VTManagerTest) mContext).mSurfaceMng.camera
		// .getParameters().flatten();
		// mSettings.mCameraParamters.unflatten(s);
		return;
	}

	void setParameters() {
		VTelProvider.setParameters(mSettings.mCameraParamters);
		VTelProvider.updateParameters(mSettings.mCameraParamters);
		// ((VTManagerTest) mContext).mSurfaceMng.camera
		// .setParameters(mSettings.mCameraParamters.flatten());
	}

	public boolean canDecBrightness() {
		return mSettings.canDecBrightness();
	}

	public boolean canIncBrightness() {
		return mSettings.canIncBrightness();
	}

	public boolean decBrightness() {
		boolean ret = mSettings.decBrightness();
		if (ret)
			setParameters();
		return ret;
	}

	public boolean incBrightness() {
		boolean ret = mSettings.incBrightness();
		if (ret)
			setParameters();
		return ret;
	}

	public boolean canDecZoom() {
		return mSettings.canDecZoom();
	}

	public boolean canIncZoom() {
		return mSettings.canIncZoom();
	}

	public boolean decZoom() {
		boolean ret = mSettings.decZoom();
		if (ret)
			setParameters();
		return ret;
	}

	public boolean incZoom() {
		boolean ret = mSettings.incZoom();
		if (ret)
			setParameters();
		return ret;
	}

	public boolean canDecContrast() {
		return mSettings.canDecContrast();
	}

	public boolean canIncContrast() {
		return mSettings.canIncContrast();
	}

	public boolean decContrast() {
		boolean ret = mSettings.decContrast();
		if (ret)
			setParameters();
		return ret;
	}

	public boolean incContrast() {
		boolean ret = mSettings.incContrast();
		if (ret)
			setParameters();
		return ret;
	}

	public String getColorEffect() {
		return mSettings.getColorEffect();
	}

	public void setColorEffect(String value) {
		mSettings.setColorEffect(value);
		setParameters();
	}

	public List<String> getSupportedColorEffects() {
		return mSettings.getSupportedColorEffects();
	}

	public boolean isSupportNightMode() {
		return mSettings.isSupportNightMode();
	}

	public boolean getNightMode() {
		return mSettings.getNightMode();
	}

	public void setNightMode(boolean isOn) {
		mSettings.setNightMode(isOn);
		setParameters();
	}

	/**
	 * Sets peer video quality
	 * 
	 * @param quality
	 */
	public void setVideoQuality(int quality) {
		mSettings.setVideoQuality(quality);
		VTelProvider.setPeerVideo(quality);
	}

	public int getVideoQuality() {
		return mSettings.getVideoQuality();
	}

	public void setMute(Phone phone, boolean muted) {
		VTelProvider.turnOnMicrophone((muted ? 0 : 1));
	}

	public boolean getMute(Phone phone) {
		return (VTelProvider.isMicrophoneOn() == 0) ? true : false;
	}

	public int getCameraSensorCount() {
		int ret = 0;
		synchronized (mEndCallLock) {
			if (State.CLOSE == mState) {
				return ret;
			}
			ret = VTelProvider.getCameraSensorCount();
		}
		return ret;
	}

	public void turnOnSpeaker(boolean isOn) {
		synchronized (mEndCallLock) {
			if (State.CLOSE == mState) {
				return;
			}
			VTelProvider.turnOnSpeaker((isOn ? 1 : 0));
		}
	}

	public boolean isSpeakerOn(Context context) {
		boolean result = false;
		synchronized (mEndCallLock) {
			if (State.CLOSE == mState) {
				return false;
			}
			result = (VTelProvider.isSpeakerOn() == 0) ? false : true;
		}
		return result;
	}

	public static void setEM(int item, int arg1, int arg2) {
		VTelProvider.setEM(item, arg1, arg2);
	}

	/**
	 * call this when VT activity is set to invisible, but VT call is still
	 * running a normal case is the Home key is pressed
	 */
	public void setVTVisible(boolean isVisible) {
		if (State.CLOSE == mState)
			return;
		if (!isVisible) {
			VTelProvider.setVTVisible(0, (Surface) (null), (Surface) (null));
		} else {
			if((null == mSettings)) {
				Log.e(TAG, "error setVTVisible, null == mSettings");
				return;
			}
			if((null == mSettings.getPeerSurface())) {
				Log.e(TAG, "error setVTVisible, null == getPeerSurface");
				return;
			}
			if((null == mSettings.getPeerSurface().getSurface())) {
				Log.e(TAG, "error setVTVisible, null == getSurface");
				return;
			}
			
			if (mSettings.getIsSwitch()) {
				VTelProvider.setVTVisible(1, mSettings.getPeerSurface().getSurface(), mSettings.getLocalSurface().getSurface());
			} else {
				VTelProvider.setVTVisible(1, mSettings.getLocalSurface().getSurface(), mSettings.getPeerSurface().getSurface());
			}
		}

	}

	void postEventToMMI(int what, int arg1, int arg2, Object obj) {
		if(closingVTService && VT_ERROR_CALL_DISCONNECT == what) {
			return;
		}
		synchronized (mVTListenerLock) {
			Log.i(TAG, "postEventToMMI [" + what + "]");
			if (null == mVTListener)
				Log.e(TAG, "error postEventToMMI");
			else
				mVTListener.sendMessage(mVTListener.obtainMessage(what, arg1, arg2, obj));
		}
	}

	void postEventFromNative(int what, int arg1, int arg2, Object obj) {
		Log.i(TAG, "postEventFromNative [" + what + "]");
		postEventToMMI(what, arg1, arg2, obj);
	}

	/**
	 * Handle user input
	 * 
	 * @param input
	 */
	public void onUserInput(String input) {
		if (State.CLOSE == mState) {
			Log.e(TAG, "onUserInput, vtmanager state error");
			return;
		}
		VTelProvider.onUserInput(input);
	}

	/**
	 * Lock peer video
	 * 
	 * @param none
	 */
	public void lockPeerVideo() {
		if (State.CLOSE == mState) {
			Log.e(TAG, "lockPeerVideo, vtmanager state error");
			setInvokeLockPeerVideoBeforeOpen(true);
			VTelProvider.setInvokeLockPeerVideoBeforeOpen(1);
			return;
		}
		VTelProvider.lockPeerVideo();
	}

	/**
	 * unlock peer video
	 * 
	 * @param none
	 */
	public void unlockPeerVideo() {
		if (State.CLOSE == mState) {
			Log.e(TAG, "unlockPeerVideo, vtmanager state error");
			return;
		}
		VTelProvider.unlockPeerVideo();
	}

	/********************************************************************************************
	*
	* type=1 indicate that we'll record video and audio
	* type=3 indicate that we'll record peer video only
	*/
	public int startRecording(int type, long maxSize) {
		Log.i(TAG, "startRecording() in VTManager.java, type is: " + type + "maxSize = " + maxSize);
		int ret = 0;

		//startRecording can work only after VT Manager's open.
		if (State.CLOSE == mState) {
			Log.e(TAG, "startRecording() in VTManager.java, State=CLOSE");
			return ret;
		}

		//To create file's path
		final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString();
		Log.i(TAG, "CAMERA_IMAGE_BUCKET_NAME is: "+CAMERA_IMAGE_BUCKET_NAME);

		//To obtain current time used to name recorded file.
		dateTakenRecording = System.currentTimeMillis();
		String timeSuffix = new SimpleDateFormat("yyyy-MM-dd kk.mm.ss.SSS").format(dateTakenRecording).toString();

		//To get recorded file name
		if(RECORD_TYPE_VIDEO_AUDIO == type) {
			recordedFileName = "Video_and_Audio_" + timeSuffix;
		}
		else if(RECORD_TYPE_AUDIO_ONLY == type) {
			Log.i(TAG, "type is wrong in startRecording() in VTManager.java");
			return ret;
		}
		else if(RECORD_TYPE_VIDEO_ONLY == type) {
			recordedFileName = "Only_Peer_Video_" + timeSuffix;
		}
		else {
			Log.i(TAG, "type is wrong in startRecording() in VTManager.java");
			return ret;
		}

		StorageManager storageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        if (null == storageManager)
		{
            Log.e(TAG, "-----story manager is null----");
            return ret;
        }
        File sampleDir = new File(storageManager.getDefaultPath());

		if (!sampleDir.canWrite())
		{
			Log.i(TAG, "----- file can't write!! ---");
		recordedFilePath = CAMERA_IMAGE_BUCKET_NAME + "/PhoneRecord/" + recordedFileName + ".3gp";
		Log.i(TAG, "recordedFileName is: " + recordedFilePath);
		
		//To create file's directory
		File record_directory = new File(CAMERA_IMAGE_BUCKET_NAME + "/PhoneRecord/");
		record_directory.mkdirs();
		}
		else
		{
			sampleDir = new File(sampleDir.getAbsolutePath() + "/PhoneRecord");
			if(sampleDir.exists() == false)
			{
		       sampleDir.mkdirs();
			}
			recordedFilePath = sampleDir.getAbsolutePath() + "/" + recordedFileName + ".3gp";
			Log.i(TAG, "recordedFileName is: " + recordedFilePath);
		}

		//Begin to record
		ret = VTelProvider.startRecording(type, recordedFilePath, maxSize);
		if(VT_RET_FROM_JNI_FALSE == ret) {
			Log.e(TAG, "VT_RET_FROM_JNI_FALSE == ret in startRecording() in VTManager.java");
			return ret;
		}

		return ret;
	}

	public int stopRecording(int type) {
		Log.i(TAG, "stopRecording() in VTManager.java, type is: " + type);
		int ret = 0;
		
		if (State.CLOSE == mState) {
			Log.e(TAG, "stopRecording() in VTManager.java, State=CLOSE");
			return ret;
		}

		if(!(RECORD_TYPE_VIDEO_AUDIO == type || RECORD_TYPE_VIDEO_ONLY == type)) {
			Log.e(TAG, "stopRecording() in VTManager.java, type is wrong");
			return ret;
		}

		ret = VTelProvider.stopRecording(type);
		if(VT_RET_FROM_JNI_FALSE == ret) {
			Log.e(TAG, "VT_RET_FROM_JNI_FALSE == ret in stopRecording() in VTManager.java");
			return ret;
		}

		//to save recorded file to media database
		if(null == recordedFilePath) {
			Log.e(TAG, "null == recordedFilePath in stopRecording() in VTManager.java");
			return ret;			
		}
		File video_File = new File(recordedFilePath);
		if (!video_File.exists()) {
			Log.i(TAG, "***video_File does not exist in stopRecording()***");
		}

        ContentValues values = new ContentValues();
		long size = video_File.length();
        values.put(Video.Media.TITLE, recordedFileName);
        values.put(Video.Media.DISPLAY_NAME, recordedFileName);
        values.put(Video.Media.DATE_TAKEN, dateTakenRecording);
        values.put(Video.Media.MIME_TYPE, "video/3gpp");
		values.put(Video.Media.SIZE, size);
        values.put(Video.Media.DATA, recordedFilePath);

		ContentResolver cr = mContext.getContentResolver();
		//cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
		//cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);

		//Uri videoTable = Uri.parse("content://media/external/video/media");

		try {
			//cr.insert(videoTable, values);
			cr.insert(Video.Media.EXTERNAL_CONTENT_URI, values);
            MediaScannerConnection.scanFile(mContext, new String [] {video_File.toString()}, null, null);
		} catch (Exception e) {
			Log.e(TAG, "insert fail in stopRecording()");
		}
		
		return ret;
	}

	public boolean isInvokeHideMeBeforeOpen() {
		return invokeHideMeBeforeOpen;
	}

	private void setInvokeHideMeBeforeOpen(boolean invokeHideMeBeforeOpen) {
		this.invokeHideMeBeforeOpen = invokeHideMeBeforeOpen;
	}

	public boolean isInvokeLockPeerVideoBeforeOpenn() {
		return invokeLockPeerVideoBeforeOpen;
	}

	public void enableAlwaysAskSettings(int flag) {
		Log.i(TAG, "enableAlwaysAskSettings in VTManager.java, flag=" + flag);
		VTelProvider.enableAlwaysAskSettings(flag);
	}

	public void userSelectYes(int flag) {
		Log.i(TAG, "userSelectYes in VTManager.java, flag=" + flag);
		VTelProvider.userSelectYes(flag);
	}

	public void enableHideYou(int flag) {
		Log.i(TAG, "enableHideYou in VTManager.java, flag=" + flag);
		VTelProvider.enableHideYou(flag);
	}

	public void enableHideMe(int flag) {
		Log.i(TAG, "enableHideMe in VTManager.java, flag=" + flag);
		VTelProvider.enableHideMe(flag);
	}

	public void incomingVideoDispaly(int flag) {
		Log.i(TAG, "incomingVideoDispaly in VTManager.java, flag=" + flag);
		VTelProvider.incomingVideoDispaly(flag);
	}

	public void incomingVTCall(int flag) {
		Log.i(TAG, "incomingVTCall in VTManager.java, flag=" + flag);
		VTelProvider.incomingVTCall(flag);
	}

	private void setInvokeLockPeerVideoBeforeOpen(boolean invokeLockPeerVideoBeforeOpen) {
		this.invokeLockPeerVideoBeforeOpen = invokeLockPeerVideoBeforeOpen;
	}
}
