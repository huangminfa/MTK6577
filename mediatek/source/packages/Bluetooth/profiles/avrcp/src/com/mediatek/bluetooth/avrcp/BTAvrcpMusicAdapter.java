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

package com.mediatek.bluetooth.avrcp;

import android.util.Log;
import android.os.*;
import android.content.BroadcastReceiver;


import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.os.Looper;

import java.lang.ref.WeakReference;

import android.media.AudioManager;
import java.util.BitSet;

/**
 * @brief Avrcp-Music Adpater for the native Android Music player
 * Note: bind Music service will be disconnected by framework if the service is idle around 1mins
 */
public class BTAvrcpMusicAdapter extends Thread {
	public static final String TAG = "MMI_AVRCP";

	private BluetoothAvrcpService avrcpSrv = null;
	private AudioManager mAudioMgr = null;
	private int mAudioMax = 100;

	private byte mCapabilities[]; // suppport event list. from 0x01~0x0d
	private byte mAttrs[]; // save the attr_id
	private byte mValueNum[]; // save the num of values which is regarding to attr_id
	private byte mCurValue[]; // save the setting value to attr_id
	private byte mValuesEqualizer[];
	private byte mValuesRepeat[];
	private byte mValuesShuffle[];
	private byte mValuesScan[];

	private byte mPlayerStatus = 2;
	private byte mVolume = 0x12; // 0x00~0x7f(0%~100%)

	public byte mCurEqualSetting = 1;
	public byte mCurRepeatSetting = 1;
	public byte mCurShuffleSetting = 1;
	public byte mCurScanSetting = 1;

	private Context mContext;
	private long[] mAddToNowList;


	private final int ACTION_KEY         = 0x11;
	private final int ACTION_SET_SETTING = 0x12;
	private final int ACTION_KEY_INFO    = 0x21;
	private final int ACTION_REG_NOTIFY  = 0x22; /* registered event */

	private boolean mbStartBind = false;

	private volatile Looper mServiceLooper = null;
	
	private BitSet mRegBit;
	private BitSet mPendingRegBit; /* register when mMusicService is ready ! */

	private long mNotifySongId = 0;
	
	BTAvrcpMusicAdapter( Context context, BluetoothAvrcpService server ){
		mContext = context;
		avrcpSrv = server;
		mAudioMgr = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
		byte i;

		setName( "BTAvrcpMusicAdapterThread " );
		if( mAudioMgr != null ){
		mAudioMax = mAudioMgr.getStreamMaxVolume( AudioManager.STREAM_MUSIC); // 0x7f(100%)
		mVolume = convertToAbosoluteVolume( mAudioMgr.getStreamVolume(AudioManager.STREAM_MUSIC) );
		}else{
		mAudioMax = mVolume = 0;
		}

		convertToMgrVolume((byte)(0x7f/2));
		convertToMgrVolume((byte)0x7f);
		convertToAbosoluteVolume((int)(mAudioMax/2) );
		convertToAbosoluteVolume((int)(mAudioMax) );		


		mAttrs = new byte[2]; //BTAvrcpProfile.AVRCP_MAX_ATTRIBUTE_NUM];
		mValueNum = new byte[2] ;//BTAvrcpProfile.AVRCP_MAX_ATTRIBUTE_NUM];
		mCurValue = new byte[BTAvrcpProfile.AVRCP_MAX_ATTRIBUTE_NUM];

		// setup the Android default music player's capability
		i = 0;
		mCapabilities = new byte[0x03];
		mCapabilities[i++] = BTAvrcpProfile.EVENT_PLAYBACK_STATUS_CHANGED;
		mCapabilities[i++] = BTAvrcpProfile.EVENT_TRACK_CHANGED ;
		//mCapabilities[i++] = BTAvrcpProfile.EVENT_TRACK_REACHED_END ;
		//mCapabilities[i++] = BTAvrcpProfile.EVENT_TRACK_REACHED_START ;
		//mCapabilities[i++] = BTAvrcpProfile.EVENT_PLAYBACK_POS_CHANGED ;
		//mCapabilities[i++] = BTAvrcpProfile.EVENT_BATT_STATUS_CHANGED ;
		//mCapabilities[i++] = BTAvrcpProfile.EVENT_SYSTEM_STATUS_CHANGED ;
		//mCapabilities[i++] = BTAvrcpProfile.EVENT_PLAYER_APPLICATION_SETTING_CHANGED ;
		mCapabilities[i++] = BTAvrcpProfile.EVENT_NOW_PLAYING_CONTENT_CHANGED ;
		//mCapabilities[i++] = BTAvrcpProfile.EVENT_AVAILABLE_PLAYERS_CHANGED ;
		//mCapabilities[i++] = BTAvrcpProfile.EVENT_ADDRESSED_PLAYER_CHANGED ;
		//mCapabilities[i++] = BTAvrcpProfile.EVENT_UIDS_CHANGED ;
		//mCapabilities[i++] = BTAvrcpProfile.EVENT_VOLUME_CHANGED ;


		i = 0;// page 143 0x01~0x04
		//mAttrs[i++] = BTAvrcpProfile.APP_SETTING_EQUALIZER;
		mAttrs[i++] = BTAvrcpProfile.APP_SETTING_REPEAT_MODE;
		mAttrs[i++] = BTAvrcpProfile.APP_SETTING_SHUFFLE;
		//mAttrs[i++] = BTAvrcpProfile.APP_SETTING_SCAN;


		// page 143, number of Values
		i = 0;
		mValueNum[i++] = 2;
		mValueNum[i++] = 2;
		//mValueNum[i++] = 2;
		//mValueNum[i++] = 1;

		// page 143, sample
		mCurValue[0] = 1; //OFF
		mCurValue[1] = 1;
		//mCurValue[2] = 1;
		//mCurValue[3] = 1;

		//mValuesEqualizer = new byte[2];
		mValuesRepeat = new byte[3];
		mValuesShuffle = new byte[2];
		//mValuesScan = new byte[2];

		mValuesRepeat[0] = BTAvrcpProfile.REPEAT_MODE_OFF;
		mValuesRepeat[1] = BTAvrcpProfile.REPEAT_MODE_SINGLE_TRACK;
		mValuesRepeat[2] = BTAvrcpProfile.REPEAT_MODE_ALL_TRACK;

		mValuesShuffle[0] = BTAvrcpProfile.SHUFFLE_OFF;
		mValuesShuffle[1] = BTAvrcpProfile.SHUFFLE_ALL_TRACK;

		mRegBit = new BitSet(16);
		mRegBit.clear();

		mPendingRegBit = new BitSet(16);
		mPendingRegBit.clear();

		if( true == BluetoothAvrcpService.bSupportMusicUI ){
		this.start(); //start to run a thread
		}else{
			Log.i( TAG, "[BT][AVRCP] No AvrcpMusic debug looper"); 
		}
	}

	public void init(){
		startToBind();
	}

	public void deinit(){
		if( null != mConnection && true == mbStartBind ){
			Log.i( TAG, "[BT][AVRCP] Adapter deinit" );
			if( mMusicService != null ){
				Log.i(TAG, String.format("[BT][AVRCP][TT] unregistercallback "));
				try{
					mMusicService.unregisterCallback(mAdapterCallback);
				}catch(Exception ex){
				}

			}			
			mMusicService = null;
			stopToBind();
		}		
		if( null != mServiceLooper ){
			mServiceLooper.quit();
			mServiceLooper = null;
		}
		if( null != mHandler ){
			Log.i( TAG, "[BT][AVRCP] BTAvrcpMusicAdapter mHandler join 2" );
			this.interrupt();
					
			try {
				this.join(100);
			}catch (Exception ex){
				Log.i( TAG, "[BT][AVRCP] join fail");
			}
		}
	}

	public void onConnect(){
		Log.i( TAG, "[BT][AVRCP] Adapter onConnect" );
		startToBind();

		if( null != mAudioMgr ){
			//mAudioMgr.registerMediaButtonEventReceiver( new ComponentName( avrcpSrv.get ) );
		}
		synchronized (mRegBit){
			mRegBit.clear();
		}
		synchronized (mPendingRegBit){
			mPendingRegBit.clear();
		}
	}

	public void onDisconnect(){
		if( null != mConnection && true == mbStartBind ){
			Log.i( TAG, "[BT][AVRCP] Adapter onDisconnect" );
			
			if( mMusicService != null ){
				Log.i(TAG, String.format("[BT][AVRCP][TT] unregistercallback "));
				try{
					mMusicService.unregisterCallback(mAdapterCallback);
				}catch(Exception ex){
				}

			}			
			mMusicService = null;
			stopToBind();
		}

		if( null != mAudioMgr ){
			//mAudioMgr.unregisterMediaButtonEventReceiver((ComponentName)BluetoothAvrcpService.class.toString());
		}

		synchronized (mRegBit){
			mRegBit.clear();
		}
		synchronized (mPendingRegBit){
			mPendingRegBit.clear();
		}
		
	}

	public byte[] playerAppCapabilities(){
		return mCapabilities;
	}

	public byte[] listPlayerAppAttribute() {
		return mAttrs;
	}

	public byte[] listPlayerAppValue(byte attr_id) {
		byte a[];
		// search the attr id 

		switch(attr_id){
		case BTAvrcpProfile.APP_SETTING_EQUALIZER:
			return mValuesEqualizer;
			//break;
		case BTAvrcpProfile.APP_SETTING_REPEAT_MODE:
			return mValuesRepeat;
			//break;
		case BTAvrcpProfile.APP_SETTING_SHUFFLE:
			return mValuesShuffle;
			//break;
		case BTAvrcpProfile.APP_SETTING_SCAN:
			return mValuesScan;
			//break;
		default:
			break;
		}
		Log.w( TAG, String.format("[BT][AVRCP] listPlayerAppValue attr_id:%d", attr_id) );
		return null;
	}

	public byte getCurPlayerAppValue(byte attr_id){
		int value = 0;
		
		checkAndBindMusicService();
		switch( attr_id ){
		case BTAvrcpProfile.APP_SETTING_REPEAT_MODE:
			try {
				value = mMusicService.getRepeatMode();
				Log.i( TAG, String.format("[BT][AVRCP] getRepeatMode ret %d", value) );
			} catch (Exception ex) {
				Log.w( TAG, String.format("[BT][AVRCP] Exception ! Fail to getRepeatMode %d %s", value, ex.toString() ) );
			}
			return (byte) value;
			//break;
		case BTAvrcpProfile.APP_SETTING_SHUFFLE:
			try {
				value = mMusicService.getShuffleMode();
				Log.i( TAG, String.format("[BT][AVRCP] getShuffleMode ret %d", value) );
			} catch (Exception ex) {
				Log.w( TAG, String.format("[BT][AVRCP] Exception ! Fail to getShuffleMode %d %s", value, ex.toString()) );
			}
			return (byte) value;
			//break;
		}
		//Pass to player

		Log.w( TAG, String.format("[BT][AVRCP] attr_id is not find attr_id:%d", attr_id) );
		return 0;
	}

	public boolean setPlayerAppValue(byte attr_id, byte value){
		boolean l_done = false;
		int mode = 0;
		
		checkAndBindMusicService();
		if( attr_id == BTAvrcpProfile.APP_SETTING_REPEAT_MODE ){
			try{
				mode = mMusicService.getRepeatMode();
			}catch(Exception ex){
			}
			if( value == mode ){
				Log.i( TAG, "[BT][AVRCP] Already in repeat mode");
				return true;
			}			
			switch( value ){
			case BTAvrcpProfile.REPEAT_MODE_OFF:
			case BTAvrcpProfile.REPEAT_MODE_SINGLE_TRACK:
			case BTAvrcpProfile.REPEAT_MODE_ALL_TRACK:
				try {
					l_done = mMusicService.setRepeatMode(value);
					if( l_done == true ){
						Log.i( TAG, String.format("[BT][AVRCP] setRepeatMode ret %s", Boolean.toString(l_done) ) );
					}					
				} catch (Exception ex) {
					Log.w( TAG, String.format("[BT][AVRCP] Exception ! Fail to setRepeatMode %d %s", value, ex.toString()) );
				}
				break;
			}
		}
		if( attr_id == BTAvrcpProfile.APP_SETTING_SHUFFLE){
			try{
				mode = mMusicService.getShuffleMode();
			}catch (Exception ex){
			}
			if( value == mode ){
				Log.i( TAG, "Already in shutffle mode");
				return true;
			}
			switch( value ){
			case BTAvrcpProfile.SHUFFLE_OFF:
			case BTAvrcpProfile.SHUFFLE_ALL_TRACK:
				try {
					l_done = mMusicService.setShuffleMode(value);
					if( l_done == true ){
						Log.i( TAG, String.format("[BT][AVRCP] setShuffleMode ret %s", Boolean.toString(l_done)) );
					}
				} catch (Exception ex) {
					Log.w( TAG, String.format("[BT][AVRCP] Exception ! Fail to setShuffleMode %d %s", value, ex.toString()) );
					Log.w( TAG, ex.toString() );
				}
				break;
			case BTAvrcpProfile.REPEAT_MODE_ALL_TRACK:
				break;
			}			
		}

		if( false == l_done ){
			Log.w( TAG, String.format("[BT][AVRCP] fail to set attr_id:%d to value:%d", attr_id, value) );
		}
		return l_done;
	}

	public String getPlayerAppAttrText(byte attr_id){

		switch(attr_id){
		case BTAvrcpProfile.APP_SETTING_EQUALIZER:
			return "Equalizer Setting";
		case BTAvrcpProfile.APP_SETTING_REPEAT_MODE:
			return "RepeatMode Setting";
		case BTAvrcpProfile.APP_SETTING_SHUFFLE:
			return "Shuffle Setting";
		case BTAvrcpProfile.APP_SETTING_SCAN:
			return "Scan Setting";
		}
		Log.w( TAG, String.format("[BT][AVRCP] getPlayerAppAttrText unknow id:%d", attr_id) );
		return null;
	}

	public String getPlayerAppValueText(byte attr_id, byte value_id){
		switch(attr_id){
		case BTAvrcpProfile.APP_SETTING_EQUALIZER:
		{
			switch(value_id){
			case BTAvrcpProfile.EQUALIZER_OFF:
				return "Equal Off";
			case BTAvrcpProfile.EQUALIZER_ON:
				return "Equal On";
			}
		}
		break;
		case BTAvrcpProfile.APP_SETTING_REPEAT_MODE:
		{
			switch(value_id){
			case BTAvrcpProfile.REPEAT_MODE_OFF:
				return "Repeat Off";
			case BTAvrcpProfile.REPEAT_MODE_SINGLE_TRACK:
				return "Repeat Single";
			case BTAvrcpProfile.REPEAT_MODE_ALL_TRACK:
				return "Repeat All";
			}
		}
		case BTAvrcpProfile.APP_SETTING_SHUFFLE:
		{
			switch(value_id){
			case BTAvrcpProfile.SHUFFLE_OFF:
				return "Shuffle Off";
			case BTAvrcpProfile.SHUFFLE_ALL_TRACK:
				return "Shuffle All";
			}
		}
		case BTAvrcpProfile.APP_SETTING_SCAN:
		{
			switch(value_id){
			case BTAvrcpProfile.SCAN_OFF:
				return "Equal Off";
			case BTAvrcpProfile.SCAN_ALL_TRACK:
				return "Equal On";
			}
		}
		}	
		return null;	
	}

	public void informBatteryStatus(byte status){
		Log.i( TAG, String.format("[BT][AVRCP] informBatteryStatus status:%d", status) );
	}

	public boolean informDisplayCharset(byte count , short charsets[]){
		// go through all charsets. if not support any one, reject it

		for( byte i = 0; i < charsets.length && i < count ; i++){
			Log.w( TAG, String.format("[BT][AVRCP] charset i:%d value:%d", i, charsets[i]) );
			if( charsets[i] == 0x6a ){
				return true;
			}
		}
		// no support charset in list.
		return false;
	}

	public void notificationBatteryStatusChanged(byte error,byte isinterim, byte status){
		if( null != avrcpSrv ){
			avrcpSrv.notificationBatteryStatusChanged(error, isinterim, status);
		}
	}
	public void notificationSystemStatusChanged(byte error, byte isinterim, byte status){
		if( null != avrcpSrv ){
			avrcpSrv.notificationSystemStatusChanged(error, isinterim, status);
		}
	}
	public void notificationVolumeChanged(byte error, byte isinterim, byte volume){
		if( null != avrcpSrv ){
			avrcpSrv.notificationVolumeChanged(error, isinterim, volume);
		}
	}

	public byte getPlayerstatus(){
		byte status = (byte) 0xff;
		
		checkAndBindMusicService();
		if( null != mMusicService ){
			Log.i( TAG, "[AVRCP] get status from player");
			try {
				status = mMusicService.getPlayStatus();
			} catch (Exception ex) {
				Log.w( TAG, "Exception ! Fail to get Player status");
			}
		}else{
			Log.i( TAG, "[AVRCP] no bind. return 0xff");
		}		
		// get current player status (start, pause, resume)
		mPlayerStatus = status;
		return (byte) status;
	}

	public int getPlayerstatusSongLength(){
		int duration = 0;
		
		checkAndBindMusicService();
		if( null != mMusicService ){
			try {
				duration = (int)mMusicService.duration();
			} catch (Exception ex) {
				duration = 0;
			}
			return duration;
		}
		return 0x0;
	}

	public int getPlayerstatusSongPos(){
		int position = 0;
		
		checkAndBindMusicService();		
		if( null != mMusicService ){
			try {
				position = (int)mMusicService.position();
			} catch (Exception ex) {
				position = 0;
			}
			return position;
		}
		return 0x0;
	}

	public boolean registerNotification(byte eventId, int interval){
		boolean bReg = false;
		byte status;
		long lvalue;
		/// register the notification event to Music service and return the interim response
		switch(eventId){
		case BTAvrcpProfile.EVENT_TRACK_REACHED_END:
		case BTAvrcpProfile.EVENT_TRACK_REACHED_START:
		case BTAvrcpProfile.EVENT_PLAYBACK_POS_CHANGED:
		case BTAvrcpProfile.EVENT_PLAYER_APPLICATION_SETTING_CHANGED:
		{
			Log.i( TAG, String.format("[BT][AVRCP] MusicAdapter blocks support register event:%d", eventId) );
			bReg = false;
		}
		break;
		case BTAvrcpProfile.EVENT_PLAYBACK_STATUS_CHANGED:
		case BTAvrcpProfile.EVENT_TRACK_CHANGED:
		case BTAvrcpProfile.EVENT_NOW_PLAYING_CONTENT_CHANGED:			
		{
			checkAndBindMusicService();
			
			if( null != mMusicService ){
				// register it
				try {
					bReg = mMusicService.regNotificationEvent(eventId, interval);
				} catch (Exception ex) {
					bReg = false;
				}
				
				Log.i( TAG, "[BT][AVRCP] registerNotification (notifyChange) bReg:" + bReg );

				if( false == bReg ){
					Log.i( TAG, String.format("[BT][AVRCP] Fail to register eventId:%d with MusicPlayer", eventId) );
				}else{
					try {
						switch(eventId){
						case BTAvrcpProfile.EVENT_PLAYBACK_STATUS_CHANGED:
							status = mMusicService.getPlayStatus();
							avrcpSrv.notificationPlayStatusChangedNative( (byte)0, (byte)1, (byte) status);
							break;
						case BTAvrcpProfile.EVENT_TRACK_CHANGED:
							lvalue = mMusicService.getAudioId();
							if( BTAvrcpProfile.PLAY_STATUS_PLAYING != getPlayerstatus() ){
								// TC_TG_NFY_BV_04_C no play should return 0xFFFFFFFF
								lvalue = 0xFFFFFFFF;
								avrcpSrv.notificationTrackChangedNative((byte)0x0, (byte)1, (long)lvalue);
							}else{
								avrcpSrv.notificationTrackChangedNative((byte)0x0, (byte)1, (long)lvalue);
							}
							break;
						case BTAvrcpProfile.EVENT_NOW_PLAYING_CONTENT_CHANGED:
							avrcpSrv.notificationNowPlayingChangedNative((byte)0x0, (byte)1);
							break;
						default:
							Log.e(TAG, "[BT][AVRCP] Unknow how to get register data" );
							bReg = false;
							break;
						}
					} catch (Exception ex) {
						bReg = false;
					}
					if( false == bReg ){
						Log.i( TAG, String.format("[BT][AVRCP] Fail to get register back data"));
					}
				}
			}else{
				/* Need to response immediate response now */
				synchronized (mPendingRegBit){
				switch(eventId){
						case BTAvrcpProfile.EVENT_PLAYBACK_STATUS_CHANGED:
							/* assume stop status */
							avrcpSrv.notificationPlayStatusChangedNative( (byte)0, (byte)1, (byte) 0);

							/* player need notify this whether register or not*/
							bReg = true;
							mPendingRegBit.set( eventId );
							break;
						case BTAvrcpProfile.EVENT_TRACK_CHANGED:
							lvalue = 0xFFFFFFFF;
							avrcpSrv.notificationTrackChangedNative((byte)0x0, (byte)1, (long)lvalue);

							bReg = true;
							mPendingRegBit.set( eventId );
							break;	
						case BTAvrcpProfile.EVENT_NOW_PLAYING_CONTENT_CHANGED:
							avrcpSrv.notificationNowPlayingChangedNative((byte)0x0, (byte)1);

							bReg = true;
							mPendingRegBit.set( eventId );
						break;
				}
				}
				Log.w( TAG, "[BT][AVRCP] registerNotification mMusicService is null (notifyChange)! bReg " + bReg + " pending");
			}
		}
		break;
		case BTAvrcpProfile.EVENT_BATT_STATUS_CHANGED:
		case BTAvrcpProfile.EVENT_SYSTEM_STATUS_CHANGED:
		{
			if( null != mSystemListener ){
				bReg = mSystemListener.regNotificationEvent(eventId, interval);
			}
		}
		break;
		case BTAvrcpProfile.EVENT_VOLUME_CHANGED:
		{
			Log.i( TAG, String.format("[BT][AVRCP] MusicAdapter blocks support register event:%d", eventId) );
			bReg = false;
		}
		break;
		}

		if( true == bReg ){
			synchronized (mRegBit){
				mRegBit.set( eventId );
			}
		}
		
		return bReg;
	}

	public void abortContinueInd(){
		/// show a tost
		Log.w( TAG, "Receive an abort indication !" );
	}

	public boolean setAbsoluteVolume(byte volume){
		int adjVolume;
		int getVolume;
		int compare;
		if( mAudioMgr == null ){
			return false;
		}

		//if(volume > 0x7f){
		//	return false;
		//}


		// 0(0%) 0x7f(100%) to 0 - mAudioMax
		adjVolume = convertToMgrVolume(volume);
		if( mAudioMgr != null ){
		getVolume = mAudioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
		mAudioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, adjVolume ,AudioManager.FLAG_PLAY_SOUND);
		compare = mAudioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
		Log.i( TAG, String.format("[BT][AVRCP] Adapter before:%d to-set:%d after:%d", getVolume, adjVolume, compare) );
		if( compare == adjVolume ) {
			mVolume = volume;
			return true;
		}		
		}
		return false;
	}

	public byte getAbsoluteVolume(){
		return mVolume;
	}

	private byte convertToAbosoluteVolume(int iMgrVolume){
		byte ret = 0;
		ret = (byte) (((float)iMgrVolume/mAudioMax) * 0x7f);
		Log.i( TAG, String.format("[BT][AVRCP] Adapter convertToAbosoluteVolume Mgr(%d) to abs(%d) MaxMgr(%d)", iMgrVolume, ret, mAudioMax) );
		return ret;
	}

	private int convertToMgrVolume(byte absolute){
		int ret = 0;
		ret =  (int) (((float)absolute/0x7f) * mAudioMax);
		Log.i( TAG, String.format("[BT][AVRCP] Adapter convertToMgrVolume absolute(%d) to Mgr(%d) MaxMgr(%d)", absolute, ret, mAudioMax) );
		return ret;
	}

  /** 
   * When using bindService with default param, 
   * the bindService will onDisconnect state if idle 1 mins.
   * need to bind again.
   */
	public void checkAndBindMusicService(){
		if( null == mMusicService){
			try{
				startToBind();
				sleep(2000);
			}catch(Exception ex){
                                
			}
		}
	}

	public boolean playItems(long id){
		boolean ret = false;
		boolean hasExit = false;
		long curList[];
		int i =0;

		// add this to now playing list
		if( id == 0 ){
			Log.v( TAG, "[BT][AVRCP] Wrong id 0");			
			return true;
		}
                
                checkAndBindMusicService();
		if( null == mMusicService){
			try{
				sleep(2000);
			}catch(Exception ex){
			}
			Log.v( TAG, "[BT][AVRCP] no mMusicService. Delay 2000" );
		}
		
		if( null == mMusicService){
			Log.v( TAG, "[BT][AVRCP] no mMusicService to play" );
			return false;
		}
		try {
			if( null == mAddToNowList ){
				mAddToNowList = new long[1];
			}
			curList = mMusicService.getNowPlaying();
			if( curList != null ){
				for( i = 0 ; i< curList.length; i++){
					if( id == curList[i] ){
						// should move to the item
						Log.v( TAG, "[BT][AVRCP] Found id:" + id + " in playing list" );
						mMusicService.setQueuePosition( i);

						hasExit = true;
						ret = true;
						break;

					}
				}
			}

			if( false == hasExit ){
				mAddToNowList[0] = id;
				mMusicService.enqueue(mAddToNowList, 1) ; // 1 is NOW in MediaPlaybackService
				ret = true;
			}
		}catch(Exception ex){

		}
		return ret;
	}

	public boolean addToNowPlaying(long id){
		boolean ret = false;
		
		checkAndBindMusicService();
		if( null == mMusicService){
			return false;
		}
		/* add this to the end of queue */
		try {
			if( null == mAddToNowList ){
				mAddToNowList = new long[1];
			}
			mAddToNowList[0] = id;
			mMusicService.enqueue(mAddToNowList, 3) ; // 3 is LAST in MediaPlaybackService
			ret = true;
		}catch(Exception ex){

		}

		return ret;
	}

	public long[] getNowPlaying(){
		long playing[] = null;
		
		checkAndBindMusicService();
		if( null == mMusicService){
			return null;
		}
		try {
			playing = mMusicService.getNowPlaying() ;
		}catch(Exception ex){
			playing = null;
		}		
		return playing;
	}

	//String getNowPlayingItemName(long id){
	//	return "[Song (" + id + ") Name]";
	//}

	public void sendAvrcpKeyEvent(int keyvalue, byte isPress){
		Message msg;
		int apKey = 0;
		String sMsg;
		sMsg = String.format("[BT][AVRCP] Receive a Avrcpkey:%d (APKey:%d)", keyvalue, apKey);

		Log.v( TAG, sMsg );

		if( null != mContext && isPress == 1 && null != mHandler ){
			//Toast.makeText( mContext, sMsg, Toast.LENGTH_SHORT);
			// send a message to itself
			msg = mHandler.obtainMessage();
			msg.what = ACTION_KEY;

			msg.arg1 = keyvalue;
			msg.arg2 = isPress;

			mHandler.sendMessage(msg);

		}

		// convert AvrcpKey to MMI key
	}

	/*
	 * Only show the indication
	 */
	public void passThroughKeyInd(int keyvalue, byte isPress){
		Message msg;
		int apKey = 0;
		String sMsg;
		sMsg = String.format("[BT][AVRCP] Receive a Avrcpkey:%d (APKey:%d)", keyvalue, apKey);

		Log.v( TAG, sMsg );

		if( null != mContext && isPress == 1 && true == BluetoothAvrcpService.bSupportMusicUI ){
			//Toast.makeText( mContext, sMsg, Toast.LENGTH_SHORT);
			// send a message to itself
			if( null != mHandler ){
			msg = mHandler.obtainMessage();
			msg.what = ACTION_KEY_INFO;

			msg.arg1 = keyvalue;
			msg.arg2 = isPress;

			mHandler.sendMessage(msg);
	        }
		}

		// convert AvrcpKey to MMI key
	}

	public boolean passNotifyMsg(int event, int interval ){
		Message msg;
		if( null != mHandler ){
			msg = mHandler.obtainMessage();
			msg.what = ACTION_REG_NOTIFY;

			msg.arg1 = event;
			msg.arg2 = interval;

			mHandler.sendMessage(msg);
			return true;
	    }
		return false;
	}

	public String getElementAttribute(long identifier, int attr_id ){
		String s = null;
		if( identifier != 0 ){
			// spec 58 all other values other than 0x0 are currently reserved
			Log.w( TAG, String.format("[BT][AVRCP] AVRCP getElementAttribute identifider:%d", identifier) );
			return null;
		}
		
		checkAndBindMusicService();
		if( null == mMusicService){
			Log.w( TAG, "No mMusicService");
			return s;
		}

		try {
			switch( attr_id ){
			case BTAvrcpProfile.MEIDA_ATTR_TITLE:
				s = mMusicService.getTrackName();
				if( s == null ){
					s = "";
				}
				break;
			case BTAvrcpProfile.MEIDA_ATTR_ARTIST:
				s = mMusicService.getArtistName();
				if( s == null ){
					s = "";
				}
				break;
			case BTAvrcpProfile.MEIDA_ATTR_ALBUM:
				s = mMusicService.getAlbumName();
				if( s == null ){
					s = "";
				}					
				break;
			case BTAvrcpProfile.MEIDA_ATTR_NUM_OF_ALBUM:

				break;
			case BTAvrcpProfile.MEIDA_ATTR_TOTAL_NUM:

				break;
			case BTAvrcpProfile.MEIDA_ATTR_GENRE:

				break;

			case BTAvrcpProfile.MEIDA_ATTR_PLAYING_TIME_MS:
				break;
			default:
				return s;
			}
			// Support the attr_id but cannot get it from player
			if( s == null ){
				s = "";
			}

		}catch(Exception ex){
			s = "";
		}

		return s;
	}

	private Handler mHandler;

	/**
	 * @brief MusicAdapter as a Looper
	 */ 	
	public void run(){
		//startToBind();

		Looper.prepare();
		mServiceLooper = Looper.myLooper();
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				// process incoming messages here
				passToHandleMessage(msg);
			}
		};

		Looper.loop();
		mHandler = null;
	}

	public void passToHandleMessage(Message msg){
		switch(msg.what){
		case ACTION_SET_SETTING:
			handleSettingMessage(msg);
			break;
		case ACTION_KEY:
		default:
			handleKeyMessage(msg);
			break;
		}
	}

	private void handleSettingMessage(Message msg){

	}

	private void handleKeyMessage(Message msg){
		int apKey = 0;
		String sMsg;
		long id = 0;

		switch( msg.what ){
		case ACTION_KEY:
		{
			sMsg = String.format("[BT][AVRCP] Receive a Avrcpkey:%d ", msg.arg1);	
			Log.i( TAG, String.format("[BT][AVRCP] ACTION_KEY msg.what:%d arg1:%d arg2:%d", msg.what, msg.arg1, msg.arg2) );
			Toast.makeText( mContext, sMsg, Toast.LENGTH_SHORT).show();	

			handleKeyMessageKeyEvent(msg);
		}
		break;
		case ACTION_KEY_INFO:
		{

				Log.i( TAG, String.format("[BT][AVRCP] KEY_INFO msg.what:%d arg1:%d arg2:%d", msg.what, msg.arg1, msg.arg2) );
				switch( msg.arg1 ){
				case BTAvrcpProfile.AVRCP_POP_POWER:
					sMsg = "POWER Key";
					break;
				case BTAvrcpProfile.AVRCP_POP_VOLUME_UP:
					sMsg = "VOLUME UP";
					break;					
				case BTAvrcpProfile. AVRCP_POP_VOLUME_DOWN:
					sMsg = "VOLUME DOWN";
					break;					
				case BTAvrcpProfile. AVRCP_POP_MUTE:
					sMsg = "MUTE";
					break;
				case BTAvrcpProfile. AVRCP_POP_PLAY:
					sMsg = "PLAY";
					break;					
				case BTAvrcpProfile. AVRCP_POP_STOP:
					sMsg = "STOP";
					break;					
				case BTAvrcpProfile. AVRCP_POP_PAUSE:
					sMsg = "PAUSE";
					break;					
				case BTAvrcpProfile. AVRCP_POP_RECORD:
					sMsg = "RECORD";
					break;					
				case BTAvrcpProfile. AVRCP_POP_REWIND:
					sMsg = "REWIND";
					break;					
				case BTAvrcpProfile. AVRCP_POP_FAST_FORWARD:
					sMsg = "FAST FORWARD";
					break;					
				case BTAvrcpProfile. AVRCP_POP_EJECT:
					sMsg = "EJECT";
					break;					
				case BTAvrcpProfile. AVRCP_POP_FORWARD:
					sMsg = "FORWARD";
					break;					
				case BTAvrcpProfile. AVRCP_POP_BACKWARD:
					sMsg = "BACKWARD";
					break;
				default:
					sMsg = String.format("KeyCode:%d", msg.arg1);
					break;
				}
		//Toast.makeText( mContext, sMsg, Toast.LENGTH_SHORT).show();	
		}
		break;
		case ACTION_REG_NOTIFY:
		{
			Log.i( TAG, String.format("[BT][AVRCP] ACTION_REG_NOTIFY for notifyChange msg.what:%d arg1:%d arg2:%d", msg.what, msg.arg1, msg.arg2) );
			switch( msg.arg1 ){
			case BTAvrcpProfile.EVENT_PLAYBACK_STATUS_CHANGED:
				avrcpSrv.notificationPlayStatusChangedNative((byte)0, (byte)0, (byte)msg.arg1);
				break;
			case BTAvrcpProfile.EVENT_TRACK_CHANGED:
				/* use mNotifySongId as the notify 'long' value */
				try{
					if( BTAvrcpProfile.PLAY_STATUS_PLAYING != getPlayerstatus() ){
						// TC_TG_NFY_BV_04_C no play should return 0xFFFFFFFF
						mNotifySongId = 0xFFFFFFFF;
					}else{
						mNotifySongId = mMusicService.getAudioId();
					}
					Log.i( TAG, "[BT][AVRCP] songid:" + mNotifySongId );
				avrcpSrv.notificationTrackChangedNative((byte)0, (byte)0, mNotifySongId);
				}catch(Exception ex){
				}

				break;
			case BTAvrcpProfile.EVENT_NOW_PLAYING_CONTENT_CHANGED:
				avrcpSrv.notificationNowPlayingChangedNative((byte)0, (byte)0);
				break;
			}
		}
		break;

		}
	}

	private void handleKeyMessageKeyEvent(Message msg){
		if( null == mMusicService ){
			return;
		}

		try{

			switch(msg.arg1){
			case BTAvrcpProfile.AVRCP_POP_PLAY:
				if( 1 != mMusicService.getPlayStatus() ){ // PLAYING
					mMusicService.play();
				}
				break;
			case BTAvrcpProfile.AVRCP_POP_STOP:
				if( 0 != mMusicService.getPlayStatus() ){ // STOPPED
					mMusicService.stop();
				}
				break;
			case BTAvrcpProfile.AVRCP_POP_PAUSE:
				if( 1 == mMusicService.getPlayStatus() ){ //PLAYING
					mMusicService.pause();
				}
				break;
			case BTAvrcpProfile.AVRCP_POP_FORWARD:
				mMusicService.next();
				break;
			case BTAvrcpProfile.AVRCP_POP_BACKWARD:
				mMusicService.prev();
				break;
			default:
				Log.i(TAG, String.format("[BT][AVRCP] Unhandle AvrcpKey:%d", msg.what));
				break;
			}
		}catch(Exception ex){
			Log.i( TAG, String.format("[BT][AVRCP] AVRCP fail to passToHandleMessage what:%d", msg.what) );	
		}
	}		

	private BTAvrcpSystemListener mSystemListener = new BTAvrcpSystemListener(this){
		@Override 
		public void onBatteryStatusChange(int status){
			Log.i( TAG, String.format("[BT][AVRCP] onBatteryStatusChange status:%d", status) );
		}

		@Override
		public void onSystemStatusChange(int status){
			Log.i( TAG, String.format("[BT][AVRCP] onSystemStatusChange status:%d", status) );
		}

		public void onVolumeStatusChange(int volume){
			Log.i( TAG, String.format("[BT][AVRCP] onSystemStatusChange volumn:%d", volume) );
		}
	};	

	private IBTAvrcpMusic mMusicService = null;

	public void startToBind(){
		Log.i( TAG, "[BT][AVRCP][b] AVRCPMusicAdapter startToBind 4(a) " + mbStartBind );
		if( null == mMusicService ){ // allow to bind again if it is not connected
			Log.i( TAG, "[BT][AVRCP][b] startService");
			// invoke the service to start 
			avrcpSrv.startService(new Intent(IBTAvrcpMusic.class.getName())); 
			// bind to the api of that serivce
			avrcpSrv.bindService(new Intent(IBTAvrcpMusic.class.getName()),
					mConnection, 0);
			mbStartBind = true;

		}
	}

	public void stopToBind(){
			Log.i( TAG, "[BT][AVRCP][b] stopToBind");
			avrcpSrv.unbindService(mConnection);
			mbStartBind = false;
			// Don't stop service. background music playing will stop if anyone invoke stopService
			// avrcpSrv.stopService(new Intent(IBTAvrcpMusic.class.getName())); 
	}

	private boolean mDebug = false;

	/* connection */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
				IBinder service) {	
			int eventId;
			int interval = 0; /* use 0 to as pending event's interval value */
			boolean bReg = false;			
				
			Log.i( TAG, String.format("[BT][AVRCP][b] onServiceConnected className:%s", className.getClassName()) );

			mMusicService = IBTAvrcpMusic.Stub.asInterface( service );

			if( mDebug == true ){
				Toast.makeText( mContext, "[BT][AVRCP] MusicService onConnected", Toast.LENGTH_SHORT).show();
			}

			try{
				if( null != mMusicService ){
					Log.i(TAG, String.format("[BT][AVRCP][b] registercallback"));
					mMusicService.registerCallback(mAdapterCallback);
					Log.i(TAG, String.format("[BT][AVRCP] mMusicService.getAudioId:%d" , mMusicService.getAudioId()) );
				}else{
					Log.i(TAG, String.format("[BT][AVRCP] mMusicService.getAudioId:null") );             		
				}

				/* ready to use! register all pending notify events */
				if( null != mMusicService ){
					synchronized (mPendingRegBit){
						eventId = BTAvrcpProfile.EVENT_PLAYBACK_STATUS_CHANGED;
						if( true == mPendingRegBit.get(eventId) ){
							bReg = mMusicService.regNotificationEvent((byte)eventId, interval);
							/* clear the pending */
							mPendingRegBit.clear(eventId);
							if( false == bReg ){
								Log.i( TAG, "[BT][AVRCP] Fail to register eventId:%d with MusicPlayer" + eventId );
								avrcpSrv.notificationPlayStatusChangedNative((byte)0, (byte)0, (byte)0);
							}else{
								Log.i( TAG, "[BT][AVRCP] ok to register pending eventId:%d with MusicPlayer" + eventId );				
							}
						}
						
						eventId = BTAvrcpProfile.EVENT_TRACK_CHANGED;
						if( true == mPendingRegBit.get(eventId) ){
							bReg = mMusicService.regNotificationEvent((byte)eventId, interval);
							/* clear the pending */
							mPendingRegBit.clear(eventId);
							if( false == bReg ){
								Log.i( TAG, "[BT][AVRCP] Fail to register eventId:%d with MusicPlayer" + eventId );
								avrcpSrv.notificationTrackChangedNative((byte)0, (byte)0, (byte)0); 
							}else{
								Log.i( TAG, "[BT][AVRCP] ok to register pending eventId:%d with MusicPlayer" + eventId );				
							}
						}
						
						eventId = BTAvrcpProfile.EVENT_NOW_PLAYING_CONTENT_CHANGED;
						if( true == mPendingRegBit.get(eventId) ){
							bReg = mMusicService.regNotificationEvent((byte)eventId, interval);
							/* clear the pending */
							mPendingRegBit.clear(eventId);
							if( false == bReg ){
								Log.i( TAG, "[BT][AVRCP] Fail to register eventId:%d with MusicPlayer" + eventId );
								avrcpSrv.notificationNowPlayingChangedNative((byte)0, (byte)0);
							}else{
								Log.i( TAG, "[BT][AVRCP] ok to register pending eventId:%d with MusicPlayer" + eventId );				
							}
						}
					}
				}

			}catch(Exception ex){
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.i( TAG, String.format("[BT][AVRCP][b] onServiceDisconnected className:%s", className.getClassName()) );
			if( mMusicService != null ){
				
				Log.i(TAG, String.format("[BT][AVRCP][b] unregistercallback "));
				try{
					mMusicService.unregisterCallback(mAdapterCallback);
				}catch(Exception ex){
				}
			}
			mMusicService = null;

			if( mDebug == true ){
				Toast.makeText( mContext, "[BT][AVRCP] MusicService onDisconnected !", Toast.LENGTH_SHORT).show();
			}
		}
	};

	private AvrcpMusicAdapterStub mAdapterCallback = new AvrcpMusicAdapterStub(this);

	class AvrcpMusicAdapterStub extends IBTAvrcpMusicCallback.Stub {
		WeakReference<BTAvrcpMusicAdapter> mAdapter;

		AvrcpMusicAdapterStub(BTAvrcpMusicAdapter adapter) {
			mAdapter = new WeakReference<BTAvrcpMusicAdapter>(adapter);
		}

		public void notifyPlaybackStatus(byte status){
			int eventId = BTAvrcpProfile.EVENT_PLAYBACK_STATUS_CHANGED;
			synchronized (mRegBit){
				if( true != mRegBit.get(eventId) ){
					return;
				}
				mRegBit.clear(eventId);
                         }
			Log.i( TAG, String.format("[BT][AVRCP] callback notifyPlaybackStatus status:%d", status) );			
			if( true != passNotifyMsg( eventId, status ) ){
				Log.i( TAG, "[BT][AVRCP] callback notifyPlaybackStatus fail" );
			}
			
		}
		public void notifyTrackChanged(long id){
			int eventId = BTAvrcpProfile.EVENT_TRACK_CHANGED;
			synchronized (mRegBit){
				if( true != mRegBit.get(eventId) ){
					Log.i( TAG, String.format("[BT][AVRCP] callback notifyTrackChanged skip") );	
					return;
				}
				mRegBit.clear(eventId);
			}
			Log.i( TAG, String.format("[BT][AVRCP] callback notifyTrackChanged (notifyChange) id:%d", id) );
			mNotifySongId = id;
			if( true != passNotifyMsg( eventId, 0 ) ){
				Log.i( TAG, "[BT][AVRCP] callback notifyTrackChanged (notifyChange) fail" );
			}
		}
		public void notifyTrackReachStart(){
			int eventId = BTAvrcpProfile.EVENT_TRACK_REACHED_START;
			synchronized (mRegBit){
				if( true != mRegBit.get(eventId) ){
					return;
				}
				mRegBit.clear(eventId);
			}
			Log.i( TAG, String.format("[BT][AVRCP] callback notifyTrackReachStart ") );			
		}
		public void notifyTrackReachEnd(){
			int eventId = BTAvrcpProfile.EVENT_TRACK_REACHED_END;
			if( true != mRegBit.get(eventId) ){
				return;
			}
			mRegBit.clear(eventId);
			Log.i( TAG, String.format("[BT][AVRCP] callback notifyTrackReachEnd ") );			
		}
		public void notifyPlaybackPosChanged(){
			int eventId = BTAvrcpProfile.EVENT_PLAYBACK_POS_CHANGED;
			if( true != mRegBit.get(eventId) ){
				return;
			}
			mRegBit.clear(eventId);
			Log.i( TAG, String.format("[BT][AVRCP] callback notifyPlaybackPosChanged ") );			
		}
		public void notifyAppSettingChanged(){
			int eventId = BTAvrcpProfile.EVENT_PLAYER_APPLICATION_SETTING_CHANGED;
			if( true != mRegBit.get(eventId) ){
				return;
			}
			mRegBit.clear(eventId);
			Log.i( TAG, String.format("[BT][AVRCP] callback notifyAppSettingChanged ")  );
		}
		public void notifyNowPlayingContentChanged(){
			int eventId = BTAvrcpProfile.EVENT_NOW_PLAYING_CONTENT_CHANGED;
                        synchronized (mRegBit){
			if( true != mRegBit.get(eventId) ){
				return;
			}
			mRegBit.clear(eventId);
                        }
			Log.i( TAG, String.format("[BT][AVRCP] callback notifyNowPlayingContentChanged ")  );
			if( true != passNotifyMsg( eventId, 0 ) ){
				Log.i( TAG, "[BT][AVRCP] callback notifyNowPlayingContentChanged fail" );
			}
		}
		public void notifyVolumehanged(byte volume){
			int eventId = BTAvrcpProfile.EVENT_VOLUME_CHANGED;
			if( true != mRegBit.get(eventId) ){
				return;
			}
			mRegBit.clear(eventId);
			Log.i( TAG, String.format("[BT][AVRCP] callback notifyVolumehanged ")  );
			avrcpSrv.notificationVolumeChangedNative((byte)0, (byte)0, volume);
		}

	}
}


