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

package com.mediatek.ygps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.mediatek.ygps.R;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.GpsStatus.NmeaListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.StrictMode;
import com.mediatek.xlog.Xlog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.content.Intent;

public class YGPSActivity extends TabActivity implements SatelliteDataProvider {

	public final static String TAG = "EM/YGPS_Activity";
	private static final boolean debug = false;
	private static final boolean NMEALOG_SD = true;
	private static final String NMEALOG_PATH = "/data/misc/nmea_log";
    private static final String FIRST_TIME = "first.time";
	// private final int LAYOUT_SATELLITE = 1;
	// private final int LAYOUT_INFO = 2;
	private static final int COUNTER = 1000;
	private static final int UPDATE_RESULT = 1001;
	private static final int COUNT_PRECISION = 500; // ms
	private static final int EXCEEDSECOND = 999;
	private static final int SETCURRENTTIMES = 1030;
	private static final int STARTBUTTONENABLE = 1040;
	private static final int SETCOUNTDOWN = 1050;
	private static final int SETMEANTTFF = 1070;
	private static final int EXCEEDPERIOD = 1080;
	private static final int SETPARAMRECONNECT = 1090;
	private static final int COMMAND_JAMMINGSCAN = 1;
	private static final int COMMAND_GETVERSION = 2;
	private static final int COMMAND_OTHERS = 3;
	// Added to receive PowerKey pressed
	private IntentFilter powerKeyFilter = null;
	private BroadcastReceiver powerKeyReceiver = null;

	int ephemerisMask = 0;
	int almanacMask = 0;
	int[] mUsedInFixMask = new int[8];
	int mTTFF = 0;
	int mSatellites = 0;
	int mTotalTimes = 0;
	int mCurrentTimes = 0;
	int iTestInterval = 0;
	static int mNewMsgCount = 0;
	static int mCounterNum = 0;
	float mMeanTTFF = 0f;
	int[] mPrns = new int[maxSatellites];
	float[] mSnrs = new float[maxSatellites];
	float[] mElevation = new float[maxSatellites];
	float[] mAzimuth = new float[maxSatellites];

	Iterable<GpsSatellite> mSatelliteList;
	Toast mPrompt = null;
	Toast mStatusPrompt = null;

	boolean mProviderIsDisable;
	boolean mShowLoc = false;
	boolean mShowStatus = false;
	boolean mbNmeaStart = false;
	boolean mFirstFix = false;
	boolean mbNeed3DFix = false;
	boolean mbTestRunning = false;
	boolean mbExit = false; // when exit, if it is testing, do not request GPS
	String txt_padding = "   ";

	boolean mbRunInBG = false;
	boolean mbFirstFix = true;
	boolean mbShowVersion = false;
	
	private ClientSocket client = null;

	// @SuppressWarnings("unused")
	private static void log(String logMsg) {
		if (debug) {
			Thread current = Thread.currentThread();
			long threadID = current.getId();
			StackTraceElement[] stack = current.getStackTrace();
			String methodName = stack[3].getMethodName();
			logMsg = "[" + threadID + "] [" + methodName + "]" + logMsg;
			Xlog.d(TAG, logMsg);
		}
	}

	public void setSatelliteStatus(int svCount, int[] prns, float[] snrs,
			float[] elevations, float[] azimuths, int ephemerisMask,
			int almanacMask, int[] usedInFixMask) {
		Xlog.v(TAG, "Enter setSatelliteStatus function");
		synchronized (this) {
			emptyArray();
			mSatellites = svCount;
			System.arraycopy(prns, 0, mPrns, 0, mSatellites);
			System.arraycopy(snrs, 0, mSnrs, 0, mSatellites);
			System.arraycopy(elevations, 0, mElevation, 0, mSatellites);
			System.arraycopy(azimuths, 0, mAzimuth, 0, mSatellites);
			System.arraycopy(usedInFixMask, 0, mUsedInFixMask, 0, 8);
		}
		mSatelliteView.postInvalidate();
		mSignalView.postInvalidate();
	}

	private String toString(int[] array, int count) {
		String str = "(";
		for (int idx = 0; idx < count; idx++) {
			str += Integer.toString(array[idx]) + ",";
                }
		str += ")";
		return str;
	}

	private String toString(float[] array, int count) {
		String str = "(";
		for (int idx = 0; idx < count; idx++) {
			str += Float.toString(array[idx]) + ",";
		}
		str += ")";
		return str;
	}

	private void emptyArray() {
		mSatellites = 0;
		for (int i = 0; i < maxSatellites; i++) {
			mPrns[i] = 0;
			mSnrs[i] = 0;
			mElevation[i] = 0;
			mAzimuth[i] = 0;
			if (i < 8) {
			    mUsedInFixMask[i] = 0;
			}
		}
	}

	public void setSatelliteStatus(Iterable<GpsSatellite> list) {
		Xlog.v(TAG, "Enter setSatelliteStatus function");
		synchronized (this) {
			emptyArray();
			int index = 0;
			for (GpsSatellite sate : list) {
				mPrns[index] = sate.getPrn();
				mSnrs[index] = sate.getSnr();
				mElevation[index] = sate.getElevation();
				mAzimuth[index] = sate.getAzimuth();
				if (sate.usedInFix()) {
				    int i = mPrns[index] - 1;
				    mUsedInFixMask[i/32] |= (1 << (i%32));
				}
				index++;
			}
			mSatellites = index;
		}
		Xlog.v(TAG, "Found " + mSatellites + " Satellites:"
				+ toString(mPrns, mSatellites) + ","
				+ toString(mSnrs, mSatellites));
		for (int i=0; i<mUsedInFixMask.length; i++) {
		        Xlog.v(TAG, "Satellites Masks "+i+": 0x"
						+ Integer.toHexString(mUsedInFixMask[i]));
		}
		mSatelliteView.postInvalidate();
		mSignalView.postInvalidate();
	}

	public int getSatelliteStatus(int[] prns, float[] snrs, float[] elevations,
			float[] azimuths, int ephemerisMask, int almanacMask,
			int[] usedInFixMask) {
		synchronized (this) {
			if (prns != null) {
				System.arraycopy(mPrns, 0, prns, 0, mSatellites);
			}
			if (snrs != null) {
				System.arraycopy(mSnrs, 0, snrs, 0, mSatellites);
			}
			if (azimuths != null) {
				System.arraycopy(mAzimuth, 0, azimuths, 0, mSatellites);
			}
			if (elevations != null) {
				System.arraycopy(mElevation, 0, elevations, 0, mSatellites);
			}
			if (usedInFixMask != null) {
				System.arraycopy(mUsedInFixMask, 0, usedInFixMask, 0, 8);
			}
			return mSatellites;
		}
	}

	private SatelliteSkyView mSatelliteView = null;
	private SatelliteSignalView mSignalView = null;
	private NMEAListener mNmeaListener = null;
	private LocationManager mLocationManager = null;
	private YGPSWakeLock mYGPSWakeLock = null;
	private Location mLastLocation = null;
	private Button mBtnColdStart = null;
	private Button mBtnWarmStart = null;
	private Button mBtnHotStart = null;
	private Button mBtnFullStart = null;
	private Button mBtnReStart = null;
	private Button mBtnHotStill = null;

	private Button mBtnNMEAStart = null;
	private Button mBtnNMEAStop = null;
	private Button mBtnNMEADbgDbg = null;
	private Button mBtnNMEADbgNmea = null;
	private Button mBtnNMEADbgDbgFile = null;
	private Button mBtnNMEADbgNmeaDDMS = null;

	private Button mBtnNMEAClear = null;
	private Button mBtnNMEASave = null;
	private Button mBtnGPSTestStart = null;
	private Button mBtnGPSTestStop = null;
	private EditText mTestTimes = null;
	private CheckBox mNeed3DFix = null;
	private EditText mTestInterval = null;

	private LooperThread mCountThread = null;
	private AutoTestThread matThread = null;
	private String mProvider = "";
	private String mStatus = "";
	private ProgressDialog mProgressDialog = null;
	// added by chaozhong @2010.10.12
	private boolean bStopPressedHandling = false;
	private boolean bStartPressedHandling = false;
	// add end
	private TextView mTVNMEALog = null;
	private TextView mTVNMEAHint = null;
	private FileOutputStream mOutputNMEALog = null;
	// added by Ben Niu @ 2012.03.09
	private Button mBtnGPSHwTest = null;
	private Button mBtnGPSJamming = null;
	private EditText mGPSJammingTimes = null;

	private void setLayout() {
		log("setLayout is called");
		mSatelliteView = (SatelliteSkyView) findViewById(R.id.skyview);
		if (mSatelliteView != null) {
			mSatelliteView.setDataProvider(this);
		}
		mSignalView = (SatelliteSignalView) findViewById(R.id.signalview);
		if (mSignalView != null) {
			mSignalView.setDataProvider(this);
		}
		mBtnColdStart = (Button) findViewById(R.id.btn_cold);
		if (mBtnColdStart != null) {
			mBtnColdStart.setOnClickListener(mRestart);
		}
		mBtnWarmStart = (Button) findViewById(R.id.btn_warm);
		if (mBtnWarmStart != null) {
			mBtnWarmStart.setOnClickListener(mRestart);
		}
		mBtnHotStart = (Button) findViewById(R.id.btn_hot);
		if (mBtnHotStart != null) {
			mBtnHotStart.setOnClickListener(mRestart);
		}
		mBtnFullStart = (Button) findViewById(R.id.btn_full);
		if (mBtnFullStart != null) {
			mBtnFullStart.setOnClickListener(mRestart);
		}
		mBtnReStart = (Button) findViewById(R.id.btn_restart);
		if (mBtnReStart != null) {
			mBtnReStart.setOnClickListener(mRestart);
		}
		mBtnHotStill = (Button) findViewById(R.id.btn_hotstill);
		if (mBtnHotStill != null) {
			mBtnHotStill.setOnClickListener(mRestart);
		}

		mTVNMEALog = (TextView) findViewById(R.id.txt_NmeaLog);
		mTVNMEAHint = (TextView) findViewById(R.id.txt_NmeaHint);

		mBtnNMEAStart = (Button) findViewById(R.id.Button_NMEA_start);
		if (mBtnNMEAStart != null) {
			mBtnNMEAStart.setOnClickListener(mRestart);
		}
		mBtnNMEAStop = (Button) findViewById(R.id.Button_NMEA_stop);
		if (mBtnNMEAStop != null) {
			mBtnNMEAStop.setOnClickListener(mRestart);
			mBtnNMEAStop.setEnabled(false);
		}
		// new
		mBtnNMEADbgDbg = (Button) findViewById(R.id.NMEA_DBG_DBG);
		if (mBtnNMEADbgDbg != null) {
			mBtnNMEADbgDbg.setOnClickListener(mRestart);
		}
		mBtnNMEADbgNmea = (Button) findViewById(R.id.NMEA_DBG_NMEA);
		if (mBtnNMEADbgNmea != null) {
			mBtnNMEADbgNmea.setOnClickListener(mRestart);
		}
		mBtnNMEADbgDbgFile = (Button) findViewById(R.id.NMEA_DBG_DBG_FILE);
		if (mBtnNMEADbgDbgFile != null) {
			mBtnNMEADbgDbgFile.setOnClickListener(mRestart);
		}
		mBtnNMEADbgNmeaDDMS = (Button) findViewById(R.id.NMEA_DBG_NMEA_FILE);
		if (mBtnNMEADbgNmeaDDMS != null) {
			mBtnNMEADbgNmeaDDMS.setOnClickListener(mRestart);
		}
		mBtnNMEAClear = (Button) findViewById(R.id.Button_NMEA_clear);
		if (mBtnNMEAClear != null) {
			mBtnNMEAClear.setOnClickListener(mRestart);
		}
		mBtnNMEASave = (Button) findViewById(R.id.Button_NMEA_save);
		if (mBtnNMEASave != null) {
			mBtnNMEASave.setOnClickListener(mRestart);
		}
		mBtnGPSTestStart = (Button) findViewById(R.id.Button_GPStest_start);
		if (mBtnGPSTestStart != null) {
			mBtnGPSTestStart.setOnClickListener(mRestart);
		}
		mBtnGPSTestStop = (Button) findViewById(R.id.Button_GPStest_stop);
		if (mBtnGPSTestStop != null) {
			mBtnGPSTestStop.setOnClickListener(mRestart);
			mBtnGPSTestStop.setEnabled(false);
		}
		mTestTimes = (EditText) findViewById(R.id.EditView_GPStest_times);
		if (null == mTestTimes) {
			Xlog.w(TAG, "findViewById mTestTimes failed");
		}
		mNeed3DFix = (CheckBox) findViewById(R.id.CheckBox_Need3DFix);
		if (null == mNeed3DFix) {
			Xlog.w(TAG, "findViewById mTestTimes failed");
		}
		mTestInterval = (EditText) findViewById(R.id.EditView_GPStest_interval);
		if (null == mTestInterval) {
			Xlog.w(TAG, "findViewById mTestTimes failed");
		}
		mBtnGPSHwTest = (Button) findViewById(R.id.GPS_HW_TEST);
		if (null != mBtnGPSHwTest) {
		    mBtnGPSHwTest.setOnClickListener(mRestart);
		}
	        mBtnGPSJamming = (Button) findViewById(R.id.GPS_JAMMING_SCAN);
	        if (null != mBtnGPSJamming) {
	            mBtnGPSJamming.setOnClickListener(mRestart);
	        }
	        mGPSJammingTimes = (EditText) findViewById(R.id.txt_GPS_JAMMING_TIMES);
	        if (null == mGPSJammingTimes) {
	            Xlog.w(TAG, "findViewById mGPSJammingTimes failed");
	        } else {
	            mGPSJammingTimes.setText("20");
	            mGPSJammingTimes.setSelection(mGPSJammingTimes.getText().length());
	        }
        String ss = MNLSetting.getMnlProp(MNLSetting.KEY_DEBUG_DBG2SOCKET,
                MNLSetting.PROP_VALUE_0);
                if (ss.equals(MNLSetting.PROP_VALUE_0)) {
                    mBtnNMEADbgDbg.setText("Enable dbg2socket [Need Restart]");
                } else {
                    mBtnNMEADbgDbg.setText("Disable dbg2socket [Need Restart]");
                }
                ss = MNLSetting.getMnlProp(MNLSetting.KEY_DEBUG_NMEA2SOCKET,
                MNLSetting.PROP_VALUE_0);
                if (ss.equals(MNLSetting.PROP_VALUE_0)) {
                    mBtnNMEADbgNmea.setText("Enable nmea2socket [Need Restart]");
                } else {
                    mBtnNMEADbgNmea.setText("Disable nmea2socket [Need Restart]");
                }

                ss = MNLSetting.getMnlProp(MNLSetting.KEY_DEBUG_DBG2FILE,
                MNLSetting.PROP_VALUE_0);
        if (ss.equals(MNLSetting.PROP_VALUE_0)) {
                    mBtnNMEADbgDbgFile.setText("Enable dbg2file [Need Restart]");
                } else {
                    mBtnNMEADbgDbgFile.setText("Disable dbg2file [Need Restart]");
                }

                ss = MNLSetting.getMnlProp(MNLSetting.KEY_DEBUG_DEBUG_NMEA,
                MNLSetting.PROP_VALUE_1);
        if (ss.equals(MNLSetting.PROP_VALUE_1)) // default enabled
                {
                    mBtnNMEADbgNmeaDDMS.setText("Disable dbg2ddms [Need Restart]");
                } else {
                    mBtnNMEADbgNmeaDDMS.setText("Enable dbg2ddms [Need Restart]");
        
                }
                ss = MNLSetting.getMnlProp(MNLSetting.KEY_BEE_ENABLED,
                MNLSetting.PROP_VALUE_1);
        if (ss.equals(MNLSetting.PROP_VALUE_1)) {
                    mBtnHotStill.setText("Disable Hotstill [Need Restart]");
                } else {
                    mBtnHotStill.setText("Enable Hotstill [Need Restart]");
                }
                mBtnNMEADbgDbg.setVisibility(View.GONE);
                mBtnNMEADbgNmea.setVisibility(View.GONE);
                // end new
        
                boolean bClearHwTest = false;
                final SharedPreferences preferences = this.getSharedPreferences(
                    FIRST_TIME, android.content.Context.MODE_PRIVATE);
                ss = preferences.getString(FIRST_TIME, null);
                if (ss != null) {
            if (ss.equals(MNLSetting.PROP_VALUE_1)) {
                preferences.edit().putString(FIRST_TIME,
                        MNLSetting.PROP_VALUE_2).commit();
            } else if (ss.equals(MNLSetting.PROP_VALUE_2)) {
                bClearHwTest = true;
            }
        }
        ss = MNLSetting.getMnlProp(MNLSetting.KEY_TEST_MODE,
                MNLSetting.PROP_VALUE_0);
        if (ss.equals(MNLSetting.PROP_VALUE_0)) {
            mBtnGPSHwTest.setText("Enable dbg2GPSDoctor [Need Restart]");
        } else {
            if (bClearHwTest) {
                MNLSetting.setMnlProp(MNLSetting.KEY_TEST_MODE,
                        MNLSetting.PROP_VALUE_0);
                mBtnGPSHwTest.setText("Enable dbg2GPSDoctor [Need Restart]");
            } else {
                mBtnGPSHwTest.setText("Disable dbg2GPSDoctor [Need Restart]");
            }
        }
	}

	private void clearLayout() {
		log("Enter clearLayout function");
		// clear all information in layout
		((TextView) findViewById(R.id.txt_date)).setText("");
		((TextView) findViewById(R.id.txt_time)).setText("");
		((TextView) findViewById(R.id.txt_latitude)).setText("");
		((TextView) findViewById(R.id.txt_longitude)).setText("");
		((TextView) findViewById(R.id.txt_altitude)).setText("");
		((TextView) findViewById(R.id.txt_accuracy)).setText("");
		((TextView) findViewById(R.id.txt_bearing)).setText("");
		((TextView) findViewById(R.id.txt_speed)).setText("");
		((TextView) findViewById(R.id.txt_distance)).setText("");
		// ((TextView) findViewById(R.id.txt_provider)).setText("");
		// ((TextView) findViewById(R.id.txt_status)).setText("");
		if (mbFirstFix) {
		    ((TextView) findViewById(R.id.first_longtitude_text)).setText("");
		    ((TextView) findViewById(R.id.first_latitude_text)).setText("");
		}
	}

	private boolean createFileForSavingNMEALog() {
		Xlog.v(TAG, "Enter startSavingNMEALog function");
        if (NMEALOG_SD) {
            if (!(android.os.Environment.getExternalStorageState()
                    .equals(android.os.Environment.MEDIA_MOUNTED))) {
                Xlog.v(TAG, "saveNMEALog function: No SD card");
                Toast.makeText(this, getString(R.string.NoSdCard),
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }

        java.text.DateFormat df = new java.text.SimpleDateFormat(
                "yyyyMMddhhmmss");
        String strTime = df.format(new Date(System.currentTimeMillis()));
        File file = null;
        if (NMEALOG_SD) {
            String strFileName = "/sdcard/NmeaLog" + strTime + ".txt";
            file = new File(strFileName);
        } else {
            File nmeaPath = new File(NMEALOG_PATH);
            if (!nmeaPath.exists()) {
                nmeaPath.mkdirs();
            }
            file = new File(nmeaPath, "nmealog" + strTime + ".txt");
        }
        if (file != null) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Xlog.w(TAG, "create new file failed!");
                Toast.makeText(this, "create new file failed",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }

        try {
            mOutputNMEALog = new FileOutputStream(file);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        }

        // set nmea hint
        if (mTVNMEAHint != null) {
            mTVNMEAHint.setText(getString(R.string.NMEA_hint));
        }

        return true;
    }

    private void saveNMEALog(String nmea) {
        boolean bSaved = true;
        try {
            mOutputNMEALog.write(nmea.getBytes(), 0, nmea.getBytes().length);
            mOutputNMEALog.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            bSaved = false;
            Xlog.v(TAG, "write NMEA log to file failed!");
            e.printStackTrace();
        } finally {
            if (!bSaved) {
                finishSavingNMEALog();
                Toast.makeText(this, "Please check your SD card",
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    private void finishSavingNMEALog() {
        try {
            mbNmeaStart = false;
            mBtnNMEAStop.setEnabled(false);
            mBtnNMEAStart.setEnabled(true);

            if (mTVNMEAHint != null) {
                mTVNMEAHint.setText("");
            }
            mTVNMEALog.setText("");

            mOutputNMEALog.close();
            mOutputNMEALog = null;
            Toast.makeText(
                    this,
                    "NMEA log saved at "
                            + (NMEALOG_SD ? Environment
                                    .getExternalStorageDirectory()
                                    .getAbsolutePath() : NMEALOG_PATH),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Xlog.w(TAG, "Close file failed!");
            e.printStackTrace();
        }
    }

    private void saveNMEALog() {
        log("Enter saveNMEALog function");
        if (NMEALOG_SD) {
            if (android.os.Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED)) {

                java.text.DateFormat df = new java.text.SimpleDateFormat(
                        "yyyyMMddhhmmss");
                String strTime = df
                        .format(new Date(System.currentTimeMillis()));
                String strFileName = "/sdcard/NmeaLog" + strTime + ".txt";
                File file = new File(strFileName);
                try {
                    if (!file.createNewFile()) {
                        Toast.makeText(this, "create new file failed",
                                Toast.LENGTH_LONG).show();
                    }

                    FileOutputStream outs = new FileOutputStream(file);
                    String Nmea = ((TextView) findViewById(R.id.txt_NmeaLog))
                            .getText().toString();
                    if (0 == Nmea.getBytes().length) {
                        Toast.makeText(this, "No Log!", Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    outs.write(Nmea.getBytes(), 0, Nmea.getBytes().length);
                    outs.flush();
                    outs.close();
                    Xlog.v(TAG, "Save Nmealog to file Finished");
                    Toast.makeText(
                            this,
                            "Save NmeaLog to "
                                    + Environment.getExternalStorageDirectory()
                                            .getAbsolutePath() + " Succeed!",
                            Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(this, "Save NmeaLog failed!",
                            Toast.LENGTH_LONG).show();
                    Xlog.w(TAG, "Save Nmealog Failed");
                }

            } else {
                Xlog.v(TAG, "saveNMEALog function: No SD card");
                Toast.makeText(this, getString(R.string.NoSdCard),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            java.text.DateFormat df = new java.text.SimpleDateFormat(
                    "yyyyMMddhhmmss");
            String strTime = df.format(new Date(System.currentTimeMillis()));
            File nmeaPath = new File(NMEALOG_PATH);
            if (!nmeaPath.exists()) {
                nmeaPath.mkdirs();
            }
            File file = new File(nmeaPath, "nmealog" + strTime + ".txt");
            if (file != null) {
                try {
                    file.createNewFile();
                    FileOutputStream outs = new FileOutputStream(file);
                    String Nmea = ((TextView) findViewById(R.id.txt_NmeaLog))
                            .getText().toString();
                    if (0 == Nmea.getBytes().length) {
                        Toast.makeText(this, "No Log!", Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    outs.write(Nmea.getBytes(), 0, Nmea.getBytes().length);
                    outs.flush();
                    outs.close();
                    Xlog.v(TAG, "Save Nmealog to file Finished");
                    Toast.makeText(this,
                            "Save NmeaLog to " + NMEALOG_PATH + " Succeed!",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Xlog.w(TAG, "Save NmeaLog failed!");
                    Toast.makeText(this, "Save NmeaLog failed",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
		}

	}

	// added by chaozhong @2010.10.12 handle the start/stop state in uniform way
	// when start button is pressed, views excepts mBtnGPSTestStop must be
	// disabled
	private void setViewToStartState() {

		mBtnGPSTestStart.setFocusableInTouchMode(false);
		mBtnGPSTestStart.refreshDrawableState();
		mBtnGPSTestStart.setEnabled(false);
		if (null != mTestTimes) {
			mTestTimes.setFocusable(false);
			mTestTimes.refreshDrawableState();
			mTestTimes.setEnabled(false);
		}
		if (null != mNeed3DFix) {
			mNeed3DFix.setFocusable(false);
			mNeed3DFix.refreshDrawableState();
			mNeed3DFix.setEnabled(false);
		}
		if (null != mTestInterval) {
			mTestInterval.setFocusable(false);
			mTestInterval.refreshDrawableState();
			mTestInterval.setEnabled(false);
		}
		mBtnGPSTestStop.setEnabled(true);
		clearLayout();
	}

	// added by chaozhong @2010.10.12 handle the start/stop state in uniform way
	// when start button is pressed, views excepts mBtnGPSTestStop must be
	// disabled
	private void setViewToStopState() {

		mBtnGPSTestStop.setEnabled(false);
		if (null != mTestTimes) {
			mTestTimes.setEnabled(true);
			mTestTimes.setFocusableInTouchMode(true);
			mTestTimes.refreshDrawableState();
		}
		if (null != mNeed3DFix) {
			mNeed3DFix.setEnabled(true);
			mNeed3DFix.refreshDrawableState();
		}
		if (null != mTestInterval) {
			mTestInterval.setEnabled(true);
			mTestInterval.setFocusableInTouchMode(true);
			mTestInterval.refreshDrawableState();
		}

		mBtnGPSTestStart.setEnabled(true);
		mBtnGPSTestStart.setFocusableInTouchMode(false);
		mBtnGPSTestStart.refreshDrawableState();
	}

	private void startGPSAutoTest() {
		// check Times
		if (null != mTestTimes) {
			if (0 == mTestTimes.getText().length()) {
				Toast.makeText(YGPSActivity.this, "Please input Times",
						Toast.LENGTH_LONG).show();
				mBtnGPSTestStart.setEnabled(true);
				return;
			} else {
				Integer nTimes = new Integer(mTestTimes.getText().toString());
				if (nTimes.intValue() < 0 || nTimes.intValue() > 999) {
					Toast.makeText(YGPSActivity.this,
							"Please input a number between 0 and 999",
							Toast.LENGTH_LONG).show();
					mBtnGPSTestStart.setEnabled(true);
					return;
				}
				mTotalTimes = nTimes.intValue();
			}
		}

		// check Interval
		if (null != mTestInterval) {
			if (0 == mTestInterval.getText().length()) {
				Toast.makeText(YGPSActivity.this, "Please input Interval",
						Toast.LENGTH_LONG).show();
				mBtnGPSTestStart.setEnabled(true);
				return;
			} else {
				Integer nInterval = new Integer(mTestInterval.getText()
						.toString());
				if (nInterval.intValue() < 0 || nInterval.intValue() > 999) {
					Toast.makeText(YGPSActivity.this,
							"Please input a number between 0 and 999",
							Toast.LENGTH_LONG).show();
					mBtnGPSTestStart.setEnabled(true);
					return;
				}
				iTestInterval = nInterval.intValue();
			}
		}

		// need 3D fix? check it
		if (null != mNeed3DFix) {
			mbNeed3DFix = mNeed3DFix.isChecked();
		}
		mbTestRunning = true;
		resetTestView();
		// start test now
		// the next if statement is added by chaozhong @2010.10.12, to prevent
		// start been pressed more times
		if (!bStartPressedHandling) {
			bStartPressedHandling = true;
			setViewToStartState();
			// original code
			matThread = new AutoTestThread();
			if (null != matThread) {
				matThread.start();
			} else {
				Xlog.w(TAG, "new matThread failed");
			}

		} else {
			Xlog.w(TAG, "start button has been pushed.");
			mBtnGPSTestStart.refreshDrawableState();
			mBtnGPSTestStart.setEnabled(false);
		}

	}

	private void stopGPSAutoTest() {
		resetTestParam();
		// Bundle extras = new Bundle();
		// extras.putBoolean("ephemeris", true);
		// resetParam(extras, false); // do connect when stop test
		SetTestParam();
	}

	private void resetTestParam() {
		mbNeed3DFix = false;
		mTotalTimes = 0;
		mCurrentTimes = 0;
		iTestInterval = 0;
		mMeanTTFF = 0f;
		mbTestRunning = false;
	}

	private void resetTestView() {
		// ((TextView)YGPSActivity.this.findViewById(R.id.txt_CurrentTimes)).setText("");
		// ((TextView)YGPSActivity.this.findViewById(R.id.txt_Reconnect_Countdown)).setText("");
		((TextView) YGPSActivity.this.findViewById(R.id.txt_mean_ttff))
				.setText("");
		((TextView) YGPSActivity.this.findViewById(R.id.txt_last_ttff))
				.setText("");
	}

	private float meanTTFF(int n) {
		return (mMeanTTFF * (n - 1) + mTTFF) / n;
	}

	private Handler mAutoTestHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SETCURRENTTIMES:
				((TextView) YGPSActivity.this
						.findViewById(R.id.txt_CurrentTimes))
						.setText(new Integer(msg.arg1).toString());
				break;
			case SETCOUNTDOWN:
				((TextView) YGPSActivity.this
						.findViewById(R.id.txt_Reconnect_Countdown))
						.setText(new Integer(msg.arg1).toString());
				break;
			case STARTBUTTONENABLE:
				mBtnGPSTestStart.setEnabled(msg.arg1 == 1 ? true : false);
				mBtnGPSTestStop.setEnabled(msg.arg1 == 1 ? false : true);
				if (msg.arg1 == 1) {
					setViewToStopState();
				}
				break;
			case EXCEEDPERIOD:
				String str = new String("Exceed ");
				str += new Integer(msg.arg1).toString();
				str += " seconds";
				Toast.makeText(YGPSActivity.this, str, Toast.LENGTH_LONG)
						.show();
				str = null;
				break;
			case SETMEANTTFF:
				((TextView) YGPSActivity.this.findViewById(R.id.txt_mean_ttff))
						.setText(new Float(mMeanTTFF).toString());
				break;
			case SETPARAMRECONNECT:
				Bundle extras = new Bundle();
				extras.putBoolean("ephemeris", true);
				resetParam(extras, false);
				break;
			}
		}
	};

	private class AutoTestThread extends Thread {
		public void run() {
			super.run();
			Looper.prepare();
			try {
				SetStartButtonEnable(false);
				reconnectTest();
				SetStartButtonEnable(true);
				interrupted();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void reconnectTest() {
		boolean bExceed = false;
		try {
			Bundle extras = new Bundle();
			extras.putBoolean("ephemeris", true);
			for (int i = 1; i <= mTotalTimes && mbTestRunning; ++i) {
				mCurrentTimes = i;
				Xlog.v(TAG, "reconnectTest function: "
						+ new Integer(mCurrentTimes).toString());
				SetCurrentTimes(i);
				resetParam(extras, true);
				if (mbNeed3DFix) {
					Long beginTime = Calendar.getInstance().getTime().getTime() / 1000;
					for (; mbTestRunning;) {
						Long nowTime = Calendar.getInstance().getTime()
								.getTime() / 1000;
						if (mFirstFix) {
							log("3D fix!");
							break;
						} else if (nowTime - beginTime > EXCEEDSECOND) {
							bExceed = true;
							ShowExceedPeriod(EXCEEDSECOND);
							log("Exceed Max Period!");
							break;
						}
					}
					if (bExceed) {
						break;
					}
				} else {
					Thread.sleep(2 * 1000);
				}
			}
			Thread.sleep(1000);
			stopGPSAutoTest();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Exception", Toast.LENGTH_LONG).show();
		}
	}

	private void SetTestParam() {
		log("Enter SetTestParam  function");
		Message msg = new Message();
		msg.what = YGPSActivity.this.SETPARAMRECONNECT;
		YGPSActivity.this.mAutoTestHandler.sendMessage(msg);
	}

	private void SetStartButtonEnable(boolean bEnable) {
		log("Enter SetStartButtonEnable  function");
		Message msg = new Message();
		msg.what = YGPSActivity.this.STARTBUTTONENABLE;
		msg.arg1 = true == bEnable ? 1 : 0;
		YGPSActivity.this.mAutoTestHandler.sendMessage(msg);
	}

	private void SetCurrentTimes(int nTimes) {
		log("Enter SetCurrentTimes  function");
		Message msg = new Message();
		msg.what = YGPSActivity.this.SETCURRENTTIMES;
		msg.arg1 = nTimes;
		YGPSActivity.this.mAutoTestHandler.sendMessage(msg);
	}

	private void SetCountDown(int Num) {
		log("Enter SetCountDown  function");
		Message msg = new Message();
		msg.what = YGPSActivity.this.SETCOUNTDOWN;
		msg.arg1 = Num;
		YGPSActivity.this.mAutoTestHandler.sendMessage(msg);
	}

	private void ShowExceedPeriod(int period) {
		log("Enter ShowExceedPeriod  function");
		Message msg = new Message();
		msg.what = YGPSActivity.this.EXCEEDPERIOD;
		msg.arg1 = period;
		YGPSActivity.this.mAutoTestHandler.sendMessage(msg);
	}
	
	private long lastTimestamp = -1;

	public class NMEAListener implements NmeaListener {
		public void onNmeaReceived(long timestamp, String nmea) {
			log("Enter onNmeaReceived  function");
			if (!mbShowVersion) {
				if (timestamp - lastTimestamp > 1000) {
				    showVersion();
				    lastTimestamp = timestamp;
				}
			}
			if (mbNmeaStart) {
				saveNMEALog(nmea);
				mTVNMEALog.setText(nmea);
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Xlog.v(TAG, "Enter onCreate  function of Main Activity");
		super.onCreate(savedInstanceState);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//		.detectDiskReads()
//		.detectDiskWrites()
		.detectNetwork()   // or .detectAll() for all detectable problems
//		.penaltyLog()
		.build());
//		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//		.detectLeakedSqlLiteObjects()
//		.detectLeakedClosableObjects()
//		.penaltyLog()
//		.penaltyDeath()
//		.build());
		TabHost tabHost = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.tabs1,
				tabHost.getTabContentView(), true);
		// tab1
		tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.satellites))
				.setIndicator(this.getString(R.string.satellites)).setContent(
						R.id.LinerLayout_Satellate));

		// tab2
		tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.information))
				.setIndicator(this.getString(R.string.information)).setContent(
						R.id.LinerLayout_Info));

		// tab3
		tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.NMEALog))
				.setIndicator(this.getString(R.string.NMEALog)).setContent(
						R.id.LinerLayout_NMEA));

		// tab4
		tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.GPStest))
				.setIndicator(this.getString(R.string.GPStest)).setContent(
						R.id.LinerLayout_GPStest));

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					String str = YGPSActivity.this.getString(R.string.GPStest);
					if (tabId.equals(str)) {
						imm.showSoftInput(YGPSActivity.this
								.findViewById(R.id.EditView_GPStest_times), 0);
					} else {
						View v = YGPSActivity.this.getCurrentFocus();
						if (null != v) {
							imm.hideSoftInputFromWindow(v.getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
						} else {
							Xlog
									.w(TAG,
											"YGPSActivity.this.getCurrentFocus() failed");
						}
					}
				} else {
					Xlog.w(TAG, "getSystemService InputMethodManager failed");
				}
			}
		});

		setLayout();

		Intent it = new Intent("com.mediatek.ygps.YGPSService");
		getBaseContext().startService(it);
		Xlog.v(TAG, "START service");
		mYGPSWakeLock = new YGPSWakeLock();
		mYGPSWakeLock.acquireScreenWakeLock(this);
		mNmeaListener = new NMEAListener();
		try {
			mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (mLocationManager != null) {
				mLocationManager.requestLocationUpdates("gps", 0, 0,
						mLocListener);
				mLocationManager.addGpsStatusListener(mGpsListener);
				mLocationManager.addNmeaListener(mNmeaListener);
				if (mLocationManager.isProviderEnabled("gps")) {
				    mProvider = "gps" + " enabled" + txt_padding;
				} else {
				    mProvider = "gps" + " disabled" + txt_padding;
				}
				mStatus = "UNKNOWN" + txt_padding;
			} else {
				Xlog.w(TAG, "new mLocationManager failed");
			}
		} catch (SecurityException e) {
			Toast.makeText(this, "security exception", Toast.LENGTH_LONG)
					.show();
			Xlog.w(TAG, "Exception: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Xlog.w(TAG, "Exception: " + e.getMessage());
		}
		mCountThread = new LooperThread();
		if (mCountThread != null) {
			mCountThread.start();
		} else {
			Xlog.w(TAG, "new mCountThread failed");
		}

		final SharedPreferences preferences = this.getSharedPreferences(
				"RunInBG", android.content.Context.MODE_PRIVATE);
		if (preferences.getBoolean("runInBG", false)) {
			mbRunInBG = true;
		} else {
			mbRunInBG = false;
		}
		mbFirstFix = true;
		powerKeyFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
		powerKeyReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Xlog.v(TAG, "onReceive, receive SCREEN_OFF event");
//				finish();
			}
		};
		registerReceiver(powerKeyReceiver, powerKeyFilter);
		Xlog.v(TAG, "registerReceiver powerKeyReceiver");
		client = new ClientSocket(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		log("Enter onCreateOptionsMenu  function");
		boolean supRetVal = super.onCreateOptionsMenu(menu);
		// menu.add(0, 0, 0, getString(R.string.menu_copyright));

		if (mShowLoc) {
			menu.add(0, 1, 0, getString(R.string.menu_hideloc));
		} else {
			menu.add(0, 1, 0, getString(R.string.menu_showloc));
		}

		final SharedPreferences preferences = this.getSharedPreferences(
				"RunInBG", android.content.Context.MODE_PRIVATE);
		if (preferences.getBoolean("runInBG", false)) {
			menu.add(0, 2, 0, "Disable Run in BG. [Need Restart.]");
		} else {
			menu.add(0, 2, 0, "Enable Run in BG. [Need Restart.]");
		}
		return supRetVal;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// ComponentName comp;
		switch (item.getItemId()) {
		// case 0:
		// log("onOptionsItemSelected  function case 0");
		// comp = new ComponentName(this.getPackageName(),
		// CopyrightInfo.class.getName());
		// startActivity(new Intent().setComponent(comp));
		// return true;

		case 1:
			log("onOptionsItemSelected  function case 1");
			if (mShowLoc == true) {
				mShowLoc = false;
				if (mPrompt != null)
					mPrompt.cancel();
				item.setTitle(R.string.menu_showloc);
			} else {
				mShowLoc = true;
				item.setTitle(R.string.menu_hideloc);
			}
			return true;

		case 2:
			final SharedPreferences preferences = this.getSharedPreferences(
					"RunInBG", android.content.Context.MODE_PRIVATE);
			if (preferences.getBoolean("runInBG", false)) {
				item.setTitle("Enable Run in BG. [Need Restart.]");
				preferences.edit().putBoolean("runInBG", false).commit();
				Xlog.v("EM/YGPS_BG", "now should *not* be in bg.");
			} else {
				item.setTitle("Disable Run in BG. [Need Restart.]");
				preferences.edit().putBoolean("runInBG", true).commit();
				Xlog.v("EM/YGPS_BG", "now should be in bg.");
			}
			return true;

		}
		return false;
	}

	@Override
	public void onPause() {
		super.onPause();
		Xlog.v(TAG, "Enter onPause function");
//		Xlog.v("EM/YGPS_BG", "mbRunInBG " + mbRunInBG);
//		if (!mbRunInBG) {
//			mLocationManager.removeUpdates(mLocListener);
//			mLocationManager.removeGpsStatusListener(mGpsListener);
//		}
		// mLocationManager.removeUpdates(mLocListener);
		// mLocationManager.removeGpsStatusListener(mGpsListener);
		// Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Xlog.v(TAG, "Enter onResume function");

//		Xlog.v("EM/YGPS_BG", "mbRunInBG " + mbRunInBG);
//		if (!mbRunInBG) {
//			mLocationManager.requestLocationUpdates(
//					LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
//			mLocationManager.addGpsStatusListener(mGpsListener);
//		}

		mProviderIsDisable = false;
		// mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		// 0, 0, mLocListener);
		// mLocationManager.addGpsStatusListener(mGpsListener);
//		if (!mProviderIsDisable && mLocListener != null) {
//			mProvider = LocationManager.GPS_PROVIDER + " enabled" + txt_padding;
//			mStatus = "STOPPED" + txt_padding;
			TextView txt_provider = (TextView) findViewById(R.id.txt_provider);
			if (txt_provider != null) {
				txt_provider.setText(mProvider);
			}
			TextView txt_status = (TextView) findViewById(R.id.txt_status);
			if (txt_status != null) {
				txt_status.setText(mStatus);
			}
//		}
//		showVersion();
		// Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
	}
	
	private void showVersion() {
	    Xlog.v(TAG, "Enter show version");
	    if(mbExit) {
	        return;
	    }
        TextView txt_chip_version = (TextView) findViewById(R.id.txt_chip_version);
//      TextView txt_mnl_version = (TextView) findViewById(R.id.txt_mnl_version);
        if (null != txt_chip_version) {
            txt_chip_version.setText(MNLSetting.getChipVersion("UNKNOWN")
                    + txt_padding);
        } else {
            Xlog.v(TAG, "txt_chip_version is null");
        }
//      if (null != txt_mnl_version) {
//          txt_mnl_version.setText(FWVersion.getMNLVersion() + txt_padding);
//      } else {
//          Xlog.v(TAG, "txt_mnl_version is null");
//      }
	sendCommand("PMTK605");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Xlog.v(TAG, "Enter onStop function");
		Xlog.v("EM/YGPS_BG", "mbRunInBG " + mbRunInBG);
		if (!mbRunInBG) {
                    mLocationManager.removeUpdates(mLocListener);
                    mLocationManager.removeGpsStatusListener(mGpsListener);
                }
		mYGPSWakeLock.release();
		if (mPrompt != null)
			mPrompt.cancel();
		if (mStatusPrompt != null)
			mStatusPrompt.cancel();
		// Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onRestart() {
		Xlog.v(TAG, "Enter onRestart function");
		Xlog.v("EM/YGPS_BG", "mbRunInBG " + mbRunInBG);
		if (!mbRunInBG) {
		    if (mLocationManager.isProviderEnabled("gps")) {
                            mProvider = "gps" + " enabled" + txt_padding;
                    } else {
                            mProvider = "gps" + " disabled" + txt_padding;
                    }
                    mStatus = "UNKNOWN" + txt_padding;
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
                    mLocationManager.addGpsStatusListener(mGpsListener);
                }
//		TextView first_longitude = (TextView) findViewById(R.id.first_longtitude_text);
//		if (first_longitude != null) {
//			first_longitude.setText("");
//		}
//		TextView first_latitude = (TextView) findViewById(R.id.first_latitude_text);
//		if (first_latitude != null) {
//			first_latitude.setText("");
//		}
//		mYGPSWakeLock = new YGPSWakeLock();
                if (null != mYGPSWakeLock) {
		        mYGPSWakeLock.acquireScreenWakeLock(this);
                } else {
                        Xlog.d(TAG, "mYGPSWakeLock is null");
                }
//		if (mBtnHotStart != null) {
//			mBtnHotStart.performClick();
//		}
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		Xlog.v(TAG, "enter onDestroy function");
		mLocationManager.removeUpdates(mLocListener);
		mLocationManager.removeGpsStatusListener(mGpsListener);
		mLocationManager.removeNmeaListener(mNmeaListener);
		mHandler.removeMessages(UPDATE_RESULT);
                mHandler.removeMessages(COUNTER);
		mbExit = true;
		if (mCountThread != null) {
			mCountThread.interrupt();
		}
		if (mOutputNMEALog != null) {
			finishSavingNMEALog();
		}
		mProgressDialog = null;
		Intent it = new Intent("com.mediatek.ygps.YGPSService");
		getBaseContext().stopService(it);
		Xlog.v(TAG, "STOP service");
		unregisterReceiver(powerKeyReceiver);
		Xlog.v(TAG, "unregisterReceiver powerKeyReceiver");
		client.endClient();
		final SharedPreferences preferences = this.getSharedPreferences(
                FIRST_TIME, android.content.Context.MODE_PRIVATE);
        String ss = preferences.getString(FIRST_TIME, null);
        if (ss != null && ss.equals(MNLSetting.PROP_VALUE_2)) {
            MNLSetting.setMnlProp(MNLSetting.KEY_TEST_MODE,
                    MNLSetting.PROP_VALUE_0);
        }
		super.onDestroy();
	}

	public final LocationListener mLocListener = new LocationListener() {

		// @Override
		public void onLocationChanged(Location location) {
			Xlog.v(TAG, "Enter onLocationChanged function");
			final int maxLen = 12;
			// if (mCountThread != null) {
			// mCountThread.stopCounting();
			// mFirstFix = true;
			// mCountThread = null;
			// }
			if (!mFirstFix) {
				Xlog.w(TAG, "mFirstFix is false, onLocationChanged");
			}
			{
				if (mShowLoc) {
					String str;
					String tmp;
					Date da;

					da = new Date(location.getTime());
					str = da.toString() + "\n";
					tmp = String.valueOf(location.getLatitude());
					if (tmp.length() > maxLen)
						tmp = tmp.substring(0, maxLen);
					str += tmp + ",";
					tmp = String.valueOf(location.getLongitude());
					if (tmp.length() > maxLen)
						tmp = tmp.substring(0, maxLen);
					str += tmp;
					if (mPrompt == null) {
						mPrompt = Toast.makeText(YGPSActivity.this, str,
								Toast.LENGTH_SHORT);
                                                mPrompt.setGravity(Gravity.BOTTOM, 0, 150);
					} else {
						mPrompt.setText(str);
                                        }
					mPrompt.show();
					if (debug)
						log("LocationChanged");
					da = null;
				}
			}

			{
				Date d = new Date(location.getTime());
				String date = String.format("%s %+02d %04d/%02d/%02d", "GMT", d
						.getTimezoneOffset(), d.getYear() + 1900,
						d.getMonth() + 1, d.getDate());
				String time = String.format("%02d:%02d:%02d", d.getHours(), d
						.getMinutes(), d.getSeconds());

				TextView txt_time = (TextView) findViewById(R.id.txt_time);
				if (txt_time != null) {
					txt_time.setText(time + txt_padding);
				}

				TextView txt_date = (TextView) findViewById(R.id.txt_date);
				if (txt_date != null) {
					txt_date.setText(date + txt_padding);
				}

				if (mbFirstFix) {
					mbFirstFix = false;
					TextView first_longitude = (TextView) findViewById(R.id.first_longtitude_text);
					if (first_longitude != null) {
						first_longitude.setText(String.valueOf(location
								.getLongitude())
								+ txt_padding);
					}

					TextView first_latitude = (TextView) findViewById(R.id.first_latitude_text);
					if (first_latitude != null) {
						first_latitude.setText(String.valueOf(location
								.getLatitude())
								+ txt_padding);
					}
				}

				TextView txt_lat = (TextView) findViewById(R.id.txt_latitude);
				if (txt_lat != null) {
					txt_lat.setText(String.valueOf(location.getLatitude())
							+ txt_padding);
				}

				TextView txt_lon = (TextView) findViewById(R.id.txt_longitude);
				if (txt_lon != null) {
					txt_lon.setText(String.valueOf(location.getLongitude())
							+ txt_padding);
				}

				TextView txt_alt = (TextView) findViewById(R.id.txt_altitude);
				if (txt_alt != null) {
					txt_alt.setText(String.valueOf(location.getAltitude())
							+ txt_padding);
				}

				TextView txt_acc = (TextView) findViewById(R.id.txt_accuracy);
				if (txt_acc != null) {
					txt_acc.setText(String.valueOf(location.getAccuracy())
							+ txt_padding);
				}

				TextView txt_bear = (TextView) findViewById(R.id.txt_bearing);
				if (txt_bear != null) {
					txt_bear.setText(String.valueOf(location.getBearing())
							+ txt_padding);
				}

				TextView txt_speed = (TextView) findViewById(R.id.txt_speed);
				if (txt_speed != null) {
					txt_speed.setText(String.valueOf(location.getSpeed())
							+ txt_padding);
				}

				if (mLastLocation != null) {
					TextView txt_dist = (TextView) findViewById(R.id.txt_distance);
					if (txt_dist != null) {
						txt_dist.setText(String.valueOf(location
								.distanceTo(mLastLocation))
								+ txt_padding);
					}
				}

				TextView txt_ttff = (TextView) findViewById(R.id.txt_ttff);
				if (txt_ttff != null) {
					txt_ttff.setText(mTTFF + " ms" + txt_padding);
				}

				// TextView txt_test_ttff =
				// (TextView)findViewById(R.id.txt_test_ttff);
				// txt_test_ttff.setText(mTTFF+ " ms"+txt_padding);

				TextView txt_provider = (TextView) findViewById(R.id.txt_provider);
				if (txt_provider != null) {
					txt_provider.setText(mProvider);
				}

				TextView txt_status = (TextView) findViewById(R.id.txt_status);
				if (txt_status != null) {
					txt_status.setText(mStatus);
				}
				d = null;
			}
			mLastLocation = location;
		}

		// @Override
		public void onProviderDisabled(String provider) {
			Xlog.v(TAG, "Enter onProviderDisabled function");
			mProviderIsDisable = true;
			mProvider = provider + " disabled" + txt_padding;
			TextView txt_provider = (TextView) findViewById(R.id.txt_provider);
			if (txt_provider != null) {
				txt_provider.setText(mProvider);
			}
			// Toast.makeText(YGPSActivity.this, "Provider disabled",
			// Toast.LENGTH_SHORT).show();
		}

		// @Override
		public void onProviderEnabled(String provider) {
			Xlog.v(TAG, "Enter onProviderEnabled function");
			mProviderIsDisable = false;
			mProvider = provider + " enabled" + txt_padding;
			TextView txt_provider = (TextView) findViewById(R.id.txt_provider);
			if (txt_provider != null) {
				txt_provider.setText(mProvider);
			}
			mTTFF = 0;
			// Toast.makeText(YGPSActivity.this, "Provider Enabled",
			// Toast.LENGTH_SHORT).show();
		}

		// @Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
//			Xlog.v(TAG, "Enter onStatusChanged function");
//			if (status == LocationProvider.AVAILABLE)
//				mStatus = "AVAILABLE" + txt_padding;
//			else if (status == LocationProvider.OUT_OF_SERVICE)
//				mStatus = "OUT_OF_SERVICE" + txt_padding;
//			else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
//				mStatus = "UNAVAILABLE" + txt_padding;
//
//			TextView txt_status = (TextView) findViewById(R.id.txt_status);
//			if (txt_status != null) {
//				txt_status.setText(mStatus);
//				Xlog.v(TAG, "onStatusChanged: mStatus-->" + mStatus);
//			}
			// Toast.makeText(YGPSActivity.this, "onStatusChanged",
			// Toast.LENGTH_SHORT).show();
		}
	};

	private void showGpsStatus(CharSequence txt, int duration) {
		Xlog.v(TAG, "Enter showGpsStatus function");
		if (mShowStatus || duration == Toast.LENGTH_LONG) {
			if (mStatusPrompt == null) {
				mStatusPrompt = Toast
						.makeText(YGPSActivity.this, txt, duration);
			} else {
				mStatusPrompt.setText(txt);
				mStatusPrompt.setDuration(duration);
			}
			mStatusPrompt.show();
		}
	}

	public final GpsStatus.Listener mGpsListener = new GpsStatus.Listener() {
		private void onFirstFix(int ttff) {
			Xlog.v(TAG, "Enter onFirstFix function: ttff = " + ttff);
			int currentTimes = mCurrentTimes;
			if (mCountThread != null) {
				mTTFF = ttff;
				mCountThread.stopCounting();
				mCountThread = null;
				mFirstFix = true;
				// mTTFF = ttff;
			} else {
				if (mFirstFix) {
					mTTFF = ttff;
					Xlog.d(TAG, "FirstFix is true, set mTTFF is ttff: " + ttff);
				}
			}
			if (ttff != mTTFF) {
				Xlog.w(TAG, "ttff != mTTFF");
				mTTFF = ttff;
			}
			CharSequence txt = new String("GPS Fix after " + ttff + "ms");
			showGpsStatus(txt, Toast.LENGTH_LONG);

			TextView txt_ttff = (TextView) findViewById(R.id.txt_ttff);
			if (txt_ttff != null) {
				txt_ttff.setText(mTTFF + " ms" + txt_padding);
			}
			if (mbTestRunning) {
				TextView txt_last_ttff = (TextView) findViewById(R.id.txt_last_ttff);
				if (txt_last_ttff != null) {
					txt_last_ttff.setText(mTTFF + " ms" + txt_padding);
				}

				mMeanTTFF = meanTTFF(currentTimes);
				((TextView) YGPSActivity.this.findViewById(R.id.txt_mean_ttff))
						.setText(new Float(mMeanTTFF).toString() + " ms"
								+ txt_padding);
			}
		}

		private void onGpsStarted() {
			Xlog.v(TAG, "Enter onGpsStarted function");
			CharSequence txt = new String("GPS started");
			showGpsStatus(txt, Toast.LENGTH_SHORT);
		}

		private void onGpsStopped() {
			Xlog.v(TAG, "Enter onGpsStopped function");
			CharSequence txt = new String("GPS stopped");
			showGpsStatus(txt, Toast.LENGTH_SHORT);
		}

		private boolean isLocationFixed(Iterable<GpsSatellite> list) {
			boolean fixed = false;
			synchronized (this) {
				int index = 0;
				for (GpsSatellite sate : list) {
					if (sate.usedInFix()) {
						fixed = true;
						break;
					}
					index++;
				}
			}
			return fixed;
		}

		public void onGpsStatusChanged(int event) {
			Xlog.v(TAG, "Enter onGpsStatusChanged function");
			GpsStatus sta = mLocationManager.getGpsStatus(null);
			if (event == GpsStatus.GPS_EVENT_STARTED) {
				onGpsStarted();
				mStatus = "STARTED" + txt_padding;
			} else if (event == GpsStatus.GPS_EVENT_STOPPED) {
				onGpsStopped();
				mStatus = "STOPPED" + txt_padding;
			} else if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
				onFirstFix(sta.getTimeToFirstFix());
				mStatus = "FIRST_FIX" + txt_padding;
			} else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
				setSatelliteStatus(sta.getSatellites());
				if (isLocationFixed(sta.getSatellites()) == false) {
					clearLayout();
					mStatus = "UNAVAILABLE" + txt_padding;
				} else {
					mStatus = "AVAILABLE" + txt_padding;
				}
				if (!mbShowVersion) {
       				    showVersion();
				}
			}
			TextView txt_status = (TextView) findViewById(R.id.txt_status);
			if (txt_status != null) {
				txt_status.setText(mStatus);
			}
			Xlog.v(TAG, "onGpsStatusChanged:" + event + " Status:" + mStatus);
		}
	};

	private void resetParam(Bundle extras, boolean bAutoConnectTest) {
		Xlog.v(TAG, "Enter resetParam function");
		mLocationManager.removeUpdates(mLocListener);
		if (!bAutoConnectTest) {
			mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER,
					"delete_aiding_data", extras);
		}

		if (!bAutoConnectTest) {
			clearLayout();
		}
		mFirstFix = false;
		mTTFF = 0;
		if (mCountThread == null) {

			mCountThread = new LooperThread();
			if (mCountThread != null) {
				mCountThread.start();
			} else {
				Xlog.w(TAG, "new mCountThread failed");
			}
		}
		try {
			if (bAutoConnectTest && iTestInterval != 0) {
				for (int i = iTestInterval; i >= 0 && mbTestRunning; --i) {
					SetCountDown(i);
					Thread.sleep(1000);
				}
				if (!mbTestRunning) {
					SetCountDown(0);
				}
			} else {
				Thread.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!mbExit) {
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
		}
		// reset autotest start button
		if (!bAutoConnectTest && !mBtnGPSTestStart.isEnabled()) {
			SetStartButtonEnable(true);
			if (mProgressDialog != null) {

				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			// added by chaozhong @2010.10.12
			bStopPressedHandling = false;
			bStartPressedHandling = false;

			// add end
		}
	}

	private boolean GpsTestRunning() {
		if (mbTestRunning) {
			Toast.makeText(this, "Please stop GPS test", Toast.LENGTH_LONG)
					.show();
			return true;
		}
		return false;
	}

	public final OnClickListener mRestart = new OnClickListener() {
		public void onClick(View v) {
			if (mStatusPrompt != null)
				mStatusPrompt.cancel();
			Bundle extras = new Bundle();
			if (v == (View) mBtnGPSTestStart) {
				mBtnGPSTestStart.refreshDrawableState();
				mBtnGPSTestStart.setEnabled(false);
				Xlog.v(TAG, "GPSTest Start button is pressed");
				startGPSAutoTest();
			} else if (v == (View) mBtnGPSTestStop) {
				// the next line is added by chaozhong @2010.10.11
				mBtnGPSTestStop.setEnabled(false);
				mBtnGPSTestStop.refreshDrawableState();
				// the next if statement is added by chaozhong @2010.10.11
				// purpose is to test weather mProgressDialog is already exist
				// or not, to resolve the multi ProgressDialog created problem
				// mBtnGPSTestStop.setEnabled(false);//added by chaozhong ,in
				// case that as soon as mProgressDialog is dismissed,
				// mBtnGPSTestStop can still be pressed, and this dialog will
				// pop up but will not be dismissed automatically
				// if(null == mProgressDialog)
				if (!bStopPressedHandling) {
					bStopPressedHandling = true;
					if (null != mProgressDialog) {
						mProgressDialog.dismiss();
						mProgressDialog = null;
					}
					mProgressDialog = new ProgressDialog(YGPSActivity.this);
					Xlog.v(TAG, "GPSTest Stop button is pressed");
					if (null != mProgressDialog) {
						// mProgressDialog.setIcon(R.drawable.icon);
						mProgressDialog.setTitle("Stopping");
						mProgressDialog.setMessage("Please wait ...");
						mProgressDialog
								.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						// the next line is added by chaozhong @2010.10.11
						// forbide the ProgressDialog to be cancelled by user
						mProgressDialog.setCancelable(false);
						mProgressDialog.setMax(100);
						mProgressDialog.setProgress(0);
						mProgressDialog.show();
					} else {
						Xlog.v(TAG, "new mProgressDialog failed");
					}
					mbTestRunning = false;
				} else {
					Xlog.v(TAG, "stop has been clicked.");
				}
			} else if (v == (View) mBtnHotStart) {
				if (GpsTestRunning()) {
					return;
				}
				mbFirstFix = true;
				disableRestartBtns();
				// nothing should be put
				Xlog.v(TAG, "Hot Start button is pressed");
				extras.putBoolean("rti", true);
				resetParam(extras, false);
				enableRestartBtns();
			} else if (v == (View) mBtnWarmStart) {
				if (GpsTestRunning()) {
					return;
				}
				mbFirstFix = true;
				disableRestartBtns();
				Xlog.v(TAG, "Warm Start button is pressed");
				extras.putBoolean("ephemeris", true);
				resetParam(extras, false);
				enableRestartBtns();
			} else if (v == (View) mBtnColdStart) {
				if (GpsTestRunning()) {
					return;
				}
				mbFirstFix = true;
				disableRestartBtns();
				Xlog.v(TAG, "Cold Start button is pressed");
				extras.putBoolean("ephemeris", true);
				extras.putBoolean("position", true);
				extras.putBoolean("time", true);
				extras.putBoolean("iono", true);
				extras.putBoolean("utc", true);
				extras.putBoolean("health", true);
				resetParam(extras, false);
				enableRestartBtns();
			} else if (v == (View) mBtnFullStart) {
				if (GpsTestRunning()) {
					return;
				}
				mbFirstFix = true;
				disableRestartBtns();
				Xlog.v(TAG, "Full Start button is pressed");
				extras.putBoolean("all", true);
				resetParam(extras, false);
				enableRestartBtns();
				// add by Baochu, 2011/07/8
			} else if (v == (View) mBtnReStart) {
				if (GpsTestRunning()) {
					return;
				}
				disableRestartBtns();
				Xlog.v(TAG, "Restart button is pressed");
				extras.putBoolean("ephemeris", true);
				extras.putBoolean("almanac", true);
				extras.putBoolean("position", true);
				extras.putBoolean("time", true);
				extras.putBoolean("iono", true);
				extras.putBoolean("utc", true);
				resetParam(extras, false);
				enableRestartBtns();

			} else if (v == (View) mBtnNMEAStart) {
				Xlog.v(TAG, "NMEA Start button is pressed");
				if (!createFileForSavingNMEALog()) {
					Xlog.i(TAG, "createFileForSavingNMEALog return false");
					return;
				}
				mbNmeaStart = true;
				mBtnNMEAStart.setEnabled(false);
				mBtnNMEAStop.setEnabled(true);
			} else if (v == (View) mBtnNMEAStop) {
				Xlog.v(TAG, "NMEA Stop button is pressed");
				mbNmeaStart = false;
				finishSavingNMEALog();

			} else if (v == (View) mBtnNMEADbgDbg) {
				Xlog.v(TAG, "NMEA DbgDbg is pressed");
				String ss = MNLSetting.getMnlProp(
                        MNLSetting.KEY_DEBUG_DBG2SOCKET,
                        MNLSetting.PROP_VALUE_0);
                if (ss.equals(MNLSetting.PROP_VALUE_0)) {
					mBtnNMEADbgDbg.setText("Disable dbg2socket [Need Restart]");
					MNLSetting.setMnlProp(MNLSetting.KEY_DEBUG_DBG2SOCKET,
                            MNLSetting.PROP_VALUE_1);
				} else {
					mBtnNMEADbgDbg.setText("Enable dbg2socket [Need Restart]");
					MNLSetting.setMnlProp(MNLSetting.KEY_DEBUG_DBG2SOCKET,
                            MNLSetting.PROP_VALUE_0);
				}

			} else if (v == (View) mBtnNMEADbgNmea) {
				Xlog.v(TAG, "NMEA DbgNmea button is pressed");
				String ss = MNLSetting.getMnlProp(
                        MNLSetting.KEY_DEBUG_NMEA2SOCKET,
                        MNLSetting.PROP_VALUE_0);
                if (ss.equals(MNLSetting.PROP_VALUE_0)) {
					mBtnNMEADbgNmea
							.setText("Disable nmea2socket [Need Restart]");
					MNLSetting.setMnlProp(MNLSetting.KEY_DEBUG_NMEA2SOCKET,
                            MNLSetting.PROP_VALUE_1);
				} else {
					mBtnNMEADbgNmea
							.setText("Enable nmea2socket [Need Restart]");
					MNLSetting.setMnlProp(MNLSetting.KEY_DEBUG_NMEA2SOCKET,
                            MNLSetting.PROP_VALUE_0);
				}
			} else if (v == (View) mBtnNMEADbgDbgFile) {
				Xlog.v(TAG, "NMEA DbgDbgFile is pressed");
				String ss = MNLSetting.getMnlProp(
                        MNLSetting.KEY_DEBUG_DBG2FILE, MNLSetting.PROP_VALUE_0);
                if (ss.equals(MNLSetting.PROP_VALUE_0)) {
					mBtnNMEADbgDbgFile
							.setText("Disable dbg2file [Need Restart]");
					MNLSetting.setMnlProp(MNLSetting.KEY_DEBUG_DBG2FILE,
                            MNLSetting.PROP_VALUE_1);
				} else {
					mBtnNMEADbgDbgFile
							.setText("Enable dbg2file [Need Restart]");
					MNLSetting.setMnlProp(MNLSetting.KEY_DEBUG_DBG2FILE,
                            MNLSetting.PROP_VALUE_0);
				}

			} else if (v == (View) mBtnNMEADbgNmeaDDMS) {
				Xlog.v(TAG, "NMEA debug2ddms button is pressed");
				String ss = MNLSetting.getMnlProp(
                        MNLSetting.KEY_DEBUG_DEBUG_NMEA,
                        MNLSetting.PROP_VALUE_1);
                if (ss.equals(MNLSetting.PROP_VALUE_1)) // default enabled
				{
					mBtnNMEADbgNmeaDDMS
							.setText("Enable dbg2ddms [Need Restart]");
					MNLSetting.setMnlProp(MNLSetting.KEY_DEBUG_DEBUG_NMEA,
                            MNLSetting.PROP_VALUE_0);
				} else {
					mBtnNMEADbgNmeaDDMS
							.setText("Disable dbg2ddms [Need Restart]");
					MNLSetting.setMnlProp(MNLSetting.KEY_DEBUG_DEBUG_NMEA,
                            MNLSetting.PROP_VALUE_1);
				}
			} else if (v == (View) mBtnHotStill) {
				Xlog.v(TAG, "Hot still button is pressed");
				String ss = MNLSetting.getMnlProp(MNLSetting.KEY_BEE_ENABLED,
                        MNLSetting.PROP_VALUE_1);
                if (ss.equals(MNLSetting.PROP_VALUE_1)) {
				 mBtnHotStill.setText("Enable Hotstill [Need Restart]");
				 MNLSetting.setMnlProp(MNLSetting.KEY_BEE_ENABLED,
                         MNLSetting.PROP_VALUE_0);
				} else {
				    mBtnHotStill.setText("Disable Hotstill [Need Restart]");
				    MNLSetting.setMnlProp(MNLSetting.KEY_BEE_ENABLED,
                            MNLSetting.PROP_VALUE_1);
				}
			} else if (v == (View) mBtnNMEAClear) {
				Xlog.v(TAG, "NMEA Clear button is pressed");
				mTVNMEALog.setText("");
			} else if (v == (View) mBtnNMEASave) {
				Xlog.v(TAG, "NMEA Save button is pressed");
				saveNMEALog();
			} else if (v == (View) mBtnGPSHwTest) {
			    Xlog.v(TAG, "mBtnGPSHwTest Button is pressed");
			    handleGPSHwTest();
			} else if (v == (View) mBtnGPSJamming) {
			    Xlog.v(TAG, "mBtnGPSJamming Button is pressed");
			    handleGPSJammingScan();
			} else {
				return;
			}
		}
	};
	
	private void sendCommand(String command) {
	    Xlog.v(TAG, "GPS Command is " + command);
	    if (null == command || command.trim().equals("")) {
	        Toast.makeText(this, "Command error", Toast.LENGTH_LONG).show();
	        return;
	    }
	    int index1 = command.indexOf("$");
	    int index2 = command.indexOf("*");
	    if (index1 != -1 && index2 != -1) {
	        if (index2 < index1) {
	            Toast.makeText(this, "Command error", Toast.LENGTH_LONG).show();
	            return;
	        }
	        command = command.substring(index1+1, index2);
	    } else if (index1 != -1) {
	        command = command.substring(index1+1);
	    } else if (index2 != -1) {
	        command = command.substring(0, index2);
	    }
	    client.sendCommand(command.trim());
	}
	
    public void getResponse(String res) {
        Xlog.v(TAG, "Enter getResponse: " + res);
        if (null == res || res.isEmpty()) {
            return;
        }
        Message m = new Message();
        m.what = UPDATE_RESULT;
        if (res.startsWith("$PMTK705")) {
            m.arg1 = this.COMMAND_GETVERSION;
        } else if (res.contains("PMTK001")) {
            m.arg1 = this.COMMAND_JAMMINGSCAN;
        } else {
            m.arg1 = this.COMMAND_OTHERS;
        }
        m.obj=res;
        mHandler.sendMessage(m);
    }
	
	private void handleGPSHwTest() {
	    String ss = MNLSetting.getMnlProp(MNLSetting.KEY_TEST_MODE,
                MNLSetting.PROP_VALUE_0);
        if (ss.equals(MNLSetting.PROP_VALUE_0)) {
                mBtnGPSHwTest.setText("Disable dbg2GPSDoctor [Need Restart]");
                MNLSetting.setMnlProp(MNLSetting.KEY_TEST_MODE,
                        MNLSetting.PROP_VALUE_1);
                mBtnNMEADbgDbgFile.setText("Disable dbg2file [Need Restart]");
                MNLSetting.setMnlProp(MNLSetting.KEY_DEBUG_DBG2FILE,
                        MNLSetting.PROP_VALUE_1);
            } else {
                mBtnGPSHwTest.setText("Enable dbg2GPSDoctor [Need Restart]");
                MNLSetting.setMnlProp(MNLSetting.KEY_TEST_MODE,
                        MNLSetting.PROP_VALUE_0);
            }
        final SharedPreferences preferences = this.getSharedPreferences(
                FIRST_TIME, android.content.Context.MODE_PRIVATE);
        preferences.edit().putString(FIRST_TIME, MNLSetting.PROP_VALUE_1)
                .commit();
	}
	
	private void handleGPSJammingScan() {
	    if (0 == mGPSJammingTimes.getText().length()) {
                Toast.makeText(YGPSActivity.this, "Please input Jamming scan times",
                    Toast.LENGTH_LONG).show();
                return;
            } else {
                Integer times = new Integer(mGPSJammingTimes.getText()
                        .toString());
                if (times <= 0 || times > 999) {
                Toast.makeText(YGPSActivity.this, "Jamming scan times error",
                        Toast.LENGTH_LONG).show();
                return;
            }
                sendCommand("PMTK837,1," + times);
            }
	}

	private void disableRestartBtns() {
		Xlog.v(TAG, "enter disableRestartBtns");
		mBtnHotStart.setEnabled(false);
		mBtnWarmStart.setEnabled(false);
		mBtnColdStart.setEnabled(false);
		mBtnFullStart.setEnabled(false);
		mBtnReStart.setEnabled(false);
		mBtnHotStill.setEnabled(false);
	}

	private void enableRestartBtns() {
		Xlog.v(TAG, "enter enableRestartBtns");
		mBtnHotStart.setEnabled(true);
		mBtnWarmStart.setEnabled(true);
		mBtnColdStart.setEnabled(true);
		mBtnFullStart.setEnabled(true);
		mBtnReStart.setEnabled(true);
		mBtnHotStill.setEnabled(true);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case COUNTER:
				log("Handle COUNTER Message "
						+ new Integer(mCounterNum).toString());
				mCounterNum++;
				if (mFirstFix == false) {
					mTTFF += COUNT_PRECISION;
					TextView txt_ttff = (TextView) findViewById(R.id.txt_ttff);
					if (txt_ttff != null) {
						txt_ttff.setText(mTTFF + " ms" + txt_padding);
					}
				}
				break;
			case UPDATE_RESULT:
			    String response = msg.obj.toString();
			    switch (msg.arg1) {
			        case COMMAND_JAMMINGSCAN:
			            if (response.contains("PMTK001,837")) {
		                    Toast.makeText(YGPSActivity.this, "Jamming scan setting success", Toast.LENGTH_LONG).show();
		                } else {
		                    Toast.makeText(YGPSActivity.this, "Jamming scan setting fail", Toast.LENGTH_LONG).show();
		                }
			            break;
			        case COMMAND_GETVERSION:
	                    if (response.startsWith("$PMTK705")) {
	                        String[] strA = response.split(",");
	                        if (strA.length >= 4) {
	                            TextView txt_mnl_version = (TextView) findViewById(R.id.txt_mnl_version);
	                            if (null != txt_mnl_version) {
	                                if (!txt_mnl_version.getText().toString().startsWith("MNL")) {
	                                    txt_mnl_version.setText(strA[3] + txt_padding);
	                                    mbShowVersion = true;
	                                }
	                            } else {
	                                Xlog.v(TAG, "txt_mnl_version is null");
	                            }
	                        }
	                    }
			            break;
			        case COMMAND_OTHERS:
			            break;
			        default:
			            break;
			    }
			    break;
			}
			super.handleMessage(msg);
		}
	};

	private class LooperThread extends Thread {
		private boolean mStop = false;

		public void stopCounting() {
			mStop = true;
		}

		public void startCounting() {
			mStop = false;
		}

		public void run() {
			super.run();
			Xlog.v(TAG, "i am running, my Id = " + getId() + " my Name = "
					+ getName());
			try {
				do {
					Thread.sleep(COUNT_PRECISION);
					Message m = new Message();
					log("new Message object: "
							+ new Integer(mNewMsgCount).toString());
					m.what = YGPSActivity.COUNTER;
					YGPSActivity.this.mHandler.sendMessage(m);
					m = null;
					log("Delete Message object: "
							+ new Integer(mNewMsgCount).toString());
					mNewMsgCount++;
				} while (YGPSActivity.LooperThread.interrupted() == false
						&& mStop == false);
			} catch (Exception e) {
				Xlog.v(TAG, "Thread.sleep interrupted: " + e.getMessage());
			}
			Xlog.v(TAG, "byebye, my Id = " + getId() + " my Name = "
					+ getName());
		}
	}

	class YGPSWakeLock {
		private PowerManager.WakeLock sScreenWakeLock;
		private PowerManager.WakeLock sCpuWakeLock;

		void acquireCpuWakeLock(Context context) {
			Xlog.v(TAG, "Acquiring cpu wake lock");
			if (sCpuWakeLock != null) {
				return;
			}

			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);

			sCpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
					| PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
//					| PowerManager.ON_AFTER_RELEASE, TAG);
			sCpuWakeLock.acquire();
		}

		void acquireScreenWakeLock(Context context) {
			Xlog.v(TAG, "Acquiring screen wake lock");
			if (sScreenWakeLock != null) {
				return;
			}

			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);

			sScreenWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
					| PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
//					| PowerManager.ON_AFTER_RELEASE, TAG);
			sScreenWakeLock.acquire();
		}

		void release() {
			Xlog.v(TAG, "Releasing wake lock");
			if (sCpuWakeLock != null) {
				sCpuWakeLock.release();
				sCpuWakeLock = null;
			}
			if (sScreenWakeLock != null) {
				sScreenWakeLock.release();
				sScreenWakeLock = null;
			}
		}
	}

}

