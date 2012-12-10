package com.mediatek.vt;

import android.util.Log;
import android.view.Surface;

public class VTelProvider {
	static {
		//todo "VTel_jni" is a temporary name
    	System.loadLibrary("mtk_vt_client");
    } 
	private static final boolean DEBUG = true;
	private static final String TAG = "VTelProvider";
	
	
    private static native final void nativeSetParameters(String params);
    private static native final String nativeGetParameters();
    
    public static native int switchCamera();

    
    public static native final int openVTSerice(); 
    public static native int initVTService(Surface local, Surface peer);
    
    public static native final int startVTService();    
    public static native int stopVTService();
    public static native int closeVTService();
	public static native void setEndCallFlag();
    
    //type -> video / image / freeze me
    public static native void setLocalVideoType(int type, String path);
    public static native int replacePeerVideoSettings(int enableFlag, String filePath);
    
    //type -> local / peer
    public static native int snapshot(int type, String path);

	public static native int getCameraSensorCount();
    
    public static native void turnOnMicrophone(int isOn);
    public static native int isMicrophoneOn();
    public static native void turnOnSpeaker(int isOn);
    public static native int isSpeakerOn();    
    public static native void setPeerVideo(int quality);    
    public static native int setVTVisible(int isOn, Surface local, Surface peer);
    public static native void onUserInput(String input);    
    
    public static native int lockPeerVideo();
    public static native int unlockPeerVideo();

	public static native int enableAlwaysAskSettings(int flag);
	public static native int userSelectYes(int flag);
	public static native int enableHideYou(int flag);	
	public static native int enableHideMe(int flag);
	public static native int incomingVideoDispaly(int flag);
	public static native int incomingVTCall(int flag);
	public static native int setInvokeLockPeerVideoBeforeOpen(int invoked);
	public static native int startRecording(int type, String path, long maxSize);
	public static native int stopRecording(int type);
    
    //VT EM settings
    public static native void setEM(int item, int arg1, int arg2);
    
    /**
     * Sets the Parameters for pictures from this Camera service.
     *
     * @param params the Parameters to use for this Camera service
     */
    static public void setParameters(CameraParamters params) {
    	Log.i(TAG, params.flatten());
        nativeSetParameters(params.flatten());
        Log.i(TAG, "setParameters ok");
    }

    /**
     * Returns the picture Parameters for this Camera service.
     */
    static public CameraParamters getParameters() {
    	CameraParamters p = new CameraParamters();
        String s = nativeGetParameters();   
        p.unflatten(s);
		p.dump();
        return p;
    }

    static public CameraParamters updateParameters(CameraParamters p) {
        String s = nativeGetParameters();   
        p.unflatten(s);
        return p;
    }
    
    public static void postEventFromNative(int msg, int arg1, int arg2, Object obj) {
    	VTManager.getInstance().postEventFromNative(msg, arg1, arg2, obj);
    }    
  
}
