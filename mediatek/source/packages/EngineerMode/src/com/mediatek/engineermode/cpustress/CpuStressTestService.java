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

package com.mediatek.engineermode.cpustress;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.mediatek.engineermode.ShellExe;
import com.mediatek.engineermode.emsvr.AFMFunctionCallEx;
import com.mediatek.engineermode.emsvr.FunctionReturn;
import com.mediatek.xlog.Xlog;

public class CpuStressTestService extends Service {

    private static final String TAG = "EM/CpuStressTestService";

    private static final int INDEX_TEST_APMCU = 1;
    private static final int INDEX_TEST_VIDEOCODEC = 2;
    private static final int INDEX_TEST_BACKUP = 3;
    private static final int INDEX_TEST_RESTORE = 4;

    public static final String VALUE_RUN = "run";
    public static final String VALUE_LOOPCOUNT = "loopcount";
    public static final String VALUE_ITERATION = "iteration";
    public static final String VALUE_MASK = "mask";
    public static final String VALUE_RESULT = "result";
    public static final String RESULT_L2C = "result_l2c";
    public static final String RESULT_PASS_L2C = "result_pass_l2c";
    public static final String RESULT_NEON = "result_neon";
    public static final String RESULT_PASS_NEON = "result_pass_neon";
    public static final String RESULT_CA9 = "result_ca9";
    public static final String RESULT_PASS_CA9 = "result_pass_ca9";
    public static final String RESULT_VIDEOCODEC = "result_video_codec";
    public static final String RESULT_PASS_VIDEOCODEC = "result_pass_video_codec";

    private static final String PASS = "PASS";
    private static final String FAIL = "FAIL";
    private static final String SKIP = "CPU1 is powered off";

    private static final int TIME_DELAYED = 100;

    public static Boolean bDualCore = null;
    public static boolean bThermal = false;
    public static boolean bThermalDisable = false;
    public static int sIndexMode = 0;

    private long lLoopCountApMcu = 99999999;
    private int iMask = 0;
    private int iResultApMcu = 0;
    private boolean bRunApMcu = false;
    private int iResultPassL2C = 0;
    private int iResultL2C = 0;
    private int iResultPassNeon = 0;
    private int iResultNeon = 0;
    private int iResultPassCA9 = 0;
    private int iResultCA9 = 0;

    private long lLoopCountVideoCodec = 99999999;
    private int iIterationVideoCodec = 0;
    private int iResultVideoCodec = 0;
    private boolean bRunVideoCodec = false;
    private int iResultPassVideoCodec = 0;
    private int iResultTotalVideoCodec = 0;

    private boolean bRunClockSwitch = false;

    private Thread testThread1 = null;
    private Thread testThread2 = null;
    private Thread testThread3 = null;
    private Handler mTestHandler1 = null;
    private Handler mTestHandler2 = null;
    private Handler mTestHandler3 = null;

    public static boolean bWantStopApmcu = false;
    public static boolean bWantStopSwCodec = false;
    public ICpuStressTestComplete testObject = null;

    private static WakeLock wakeLock = null;

    private StressTestBinder binder = new StressTestBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        Xlog.v(TAG, "Enter onCreate");
        super.onCreate();
        testThread1 = new Thread(new TestRunnable1());
        testThread1.start();
        testThread2 = new Thread(new TestRunnable2());
        testThread2.start();
        testThread3 = new Thread(new TestRunnable3());
        testThread3.start();
        int coreNumber = coreNum();
        bDualCore = Boolean.valueOf(coreNumber != 1);
        if (bDualCore) {
            iResultApMcu |= 1 << 31;
            iResultVideoCodec |= 1 << 31;
        }
        bThermal = new File("/etc/.tp/.ht120.mtc").exists();
        wakeLock = new WakeLock();
        Xlog.i(TAG, "Core Number: " + coreNumber);
    }

    @Override
    public void onDestroy() {
        Xlog.v(TAG, "enter onDestroy");
        restore(sIndexMode);
        mTestHandler1.getLooper().quit();
        mTestHandler2.getLooper().quit();
        mTestHandler3.getLooper().quit();
        wakeLock.release();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Xlog.v(TAG, "Enter onStartCommand");
        return START_STICKY;
    }

    public void startTest(Bundle data) {
        Xlog.v(TAG, "Enter startTest");
        if (testObject instanceof ApMcu) {
            Xlog.v(TAG, "startTest for ApMcu");
            if (bRunApMcu) {
                Xlog.v(TAG, "ApMpu test is running");
                return;
            }
            this.lLoopCountApMcu = data.getLong(VALUE_LOOPCOUNT);
            this.iMask = data.getInt(VALUE_MASK);
            bRunApMcu = true;
            bWantStopApmcu = false;
            iResultApMcu = 0;
            if (bDualCore) {
                iResultApMcu |= 1 << 31;
            }
            iResultL2C = 0;
            iResultPassL2C = 0;
            iResultNeon = 0;
            iResultPassNeon = 0;
            iResultCA9 = 0;
            iResultPassCA9 = 0;
            updateWakeLock();
            mTestHandler1.sendEmptyMessage(INDEX_TEST_APMCU);
        } else if (testObject instanceof SwVideoCodec) {
            Xlog.v(TAG, "startTest for SwVideoCodec");
            if (bRunVideoCodec) {
                Xlog.v(TAG, "VideoCodec test is running");
                return;
            }
            this.lLoopCountVideoCodec = data.getLong(VALUE_LOOPCOUNT);
            this.iIterationVideoCodec = data.getInt(VALUE_ITERATION);
            bRunVideoCodec = true;
            bWantStopSwCodec = false;
            iResultVideoCodec = 0;
            if (bDualCore.booleanValue()) {
                iResultVideoCodec |= 1 << 31;
            }
            iResultPassVideoCodec = 0;
            iResultTotalVideoCodec = 0;
            updateWakeLock();
            mTestHandler2.sendEmptyMessage(INDEX_TEST_VIDEOCODEC);
        } else if (testObject instanceof ClockSwitch) {
            Xlog.v(TAG, "startTest for ClockSwitch");
            this.bRunClockSwitch = true;
            updateWakeLock();
        }
    }

    public void stopTest() {
        Xlog.v(TAG, "Enter stopTest, testObject is: " + testObject);
        if (testObject instanceof ApMcu) {
            Xlog.v(TAG, "stopTest for ApMcu");
            //bRunApMcu = false;
            bWantStopApmcu = true;
        } else if (testObject instanceof SwVideoCodec) {
            Xlog.v(TAG, "stopTest for SwVideoCodec");
            //bRunVideoCodec = false;
            bWantStopSwCodec = true;
        } else if (testObject instanceof ClockSwitch) {
            Xlog.v(TAG, "stopTest for ClockSwitch");
            this.bRunClockSwitch = false;
        }
        // updateWakeLock();
    }

    public Bundle updateData(Bundle data) {
        Xlog.v(TAG, "updateData, data is null ? " + (data == null));
        if (null == data) {
            if (this.testObject instanceof ApMcu) {
                return dataGenerator(INDEX_TEST_APMCU);
            } else if (this.testObject instanceof SwVideoCodec) {
                return dataGenerator(INDEX_TEST_VIDEOCODEC);
            }
        } else {
            if (this.testObject instanceof ApMcu) {
                iMask = data.getInt(VALUE_MASK);
            } else if (this.testObject instanceof SwVideoCodec) {
            }
        }
        return null;
    }

    public Bundle getData() {
        return updateData(null);
    }

    private class TestRunnable1 implements Runnable {

        public void run() {
            Looper.prepare();
            mTestHandler1 = new Handler() {
                public void handleMessage(Message msg) {
                    Xlog.v(TAG, "mTestHandler1 receive msg: " + msg.what);
                    switch (msg.what) {
                        case INDEX_TEST_APMCU:
                            if (lLoopCountApMcu <= 0) {
                                lLoopCountApMcu = 0;
                                bRunApMcu = false;
                                updateWakeLock();
                                this.removeMessages(INDEX_TEST_APMCU);
                            } else {
                                if (!bWantStopApmcu) {
                                    // lLoopCountApMcu--;
                                    doApMcuTest();
                                    this.sendEmptyMessageDelayed(
                                            INDEX_TEST_APMCU, TIME_DELAYED);
                                } else {
                                    bRunApMcu = false;
                                    bWantStopApmcu = false;
                                    this.removeMessages(INDEX_TEST_APMCU);
                                    updateWakeLock();
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    if (lLoopCountApMcu > 0 && bRunApMcu) {
                        lLoopCountApMcu--;
                    }
                    if (null != testObject) {
                        if (testObject instanceof ApMcu
                                || testObject instanceof CpuStressTest) {
                            testObject.onGetTestResult();
                        }
                    }
                    super.handleMessage(msg);
                }
            };
            Looper.loop();
        }
    }

    private class TestRunnable2 implements Runnable {

        public void run() {
            Looper.prepare();
            mTestHandler2 = new Handler() {
                public void handleMessage(Message msg) {
                    Xlog.v(TAG, "mTestHandler2 receive msg: " + msg.what);
                    switch (msg.what) {
                        case INDEX_TEST_VIDEOCODEC:
                            if (lLoopCountVideoCodec <= 0) {
                                lLoopCountVideoCodec = 0;
                                bRunVideoCodec = false;
                                updateWakeLock();
                                this.removeMessages(INDEX_TEST_VIDEOCODEC);
                            } else {
                                if (!bWantStopSwCodec) {
                                    // lLoopCountVideoCodec--;
                                    doVideoCodecTest();
                                    this
                                            .sendEmptyMessageDelayed(
                                                    INDEX_TEST_VIDEOCODEC,
                                                    TIME_DELAYED);
                                } else {
                                    bRunVideoCodec = false;
                                    bWantStopSwCodec = false;
                                    this.removeMessages(INDEX_TEST_VIDEOCODEC);
                                    updateWakeLock();
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    if (lLoopCountVideoCodec > 0 && bRunVideoCodec) {
                        lLoopCountVideoCodec--;
                    }
                    if (null != testObject) {
                        if (testObject instanceof SwVideoCodec
                                || testObject instanceof CpuStressTest) {
                            testObject.onGetTestResult();
                        }
                    }
                    super.handleMessage(msg);
                }
            };
            Looper.loop();
        }
    }

    private class TestRunnable3 implements Runnable {

        public void run() {
            Looper.prepare();
            mTestHandler3 = new Handler() {
                public void handleMessage(Message msg) {
                    Xlog.v(TAG, "mTestHandler3 receive msg: " + msg.what);
                    switch (msg.what) {
                        case INDEX_TEST_BACKUP:
                        case INDEX_TEST_RESTORE:
                            doBackupRestore(msg.arg1);
                            break;
                        default:
                            break;
                    }
                    if (null != testObject) {
                        testObject.onGetTestResult();
                    }
                    super.handleMessage(msg);
                }
            };
            Looper.loop();
        }

        private void doBackupRestore(int index) {
            Xlog.v(TAG, "Enter doBackupRestore: " + index);
            AFMFunctionCallEx A = new AFMFunctionCallEx();
            boolean result = A
                    .StartCallFunctionStringReturn(AFMFunctionCallEx.FUNCTION_EM_CPU_STRESS_TEST_BACKUP);
            A.WriteParamNo(1);
            A.WriteParamInt(index);
            String response = "";
            if (!result) {
                Xlog.d(TAG, "AFMFunctionCallEx return false");
                response = "ERROR";
            } else {
                FunctionReturn r;
                do {
                    r = A.GetNextResult();
                    if (r.returnString == "") {
                        break;
                    }
                    response += r.returnString;
                } while (r.returnCode == AFMFunctionCallEx.RESULT_CONTINUE);
                if (r.returnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
                    Xlog.d(TAG, "AFMFunctionCallEx: RESULT_IO_ERR");
                    response = "ERROR";
                }
            }
            Xlog.v(TAG, "doBackupRestore: " + response);
        }
    }

    private void doApMcuTest() {
        Xlog.v(TAG, "enter doApMpuTest");
        if (0 != (iMask & 1 << ApMcu.MASK_L2C)) {
            doApMcuTest(ApMcu.MASK_L2C);
        }
        if (!bDualCore.booleanValue()) {
            if (0 != (iMask & 1 << ApMcu.MASK_NEON)) {
                doApMcuTest(ApMcu.MASK_NEON);
            }
            if (0 != (iMask & 1 << ApMcu.MASK_CA9)) {
                doApMcuTest(ApMcu.MASK_CA9);
            }
        } else {
            switch (sIndexMode) {
                case CpuStressTest.INDEX_SINGLE:
                    if (0 != (iMask & 1 << ApMcu.MASK_NEON_0)) {
                        doApMcuTest(ApMcu.MASK_NEON);
                    }
                    if (0 != (iMask & 1 << ApMcu.MASK_CA9_0)) {
                        doApMcuTest(ApMcu.MASK_CA9);
                    }
                    break;
                case CpuStressTest.INDEX_TEST:
                case CpuStressTest.INDEX_DUAL:
                    if (0 != (iMask & 1 << ApMcu.MASK_NEON_0)) {
                        doApMcuTest(ApMcu.MASK_NEON_0);
                    }
                    if (0 != (iMask & 1 << ApMcu.MASK_CA9_0)) {
                        doApMcuTest(ApMcu.MASK_CA9_0);
                    }
                    break;
                default:
                    break;
            }

        }
        Xlog.v(TAG, "iResultApMpu is 0x" + Integer.toHexString(iResultApMcu));
    }

    private void doApMcuTest(int index) {
        Xlog.v(TAG, "doApMpuTest index is: " + index);
        String response = "";
        AFMFunctionCallEx A = new AFMFunctionCallEx();
        boolean result = A
                .StartCallFunctionStringReturn(AFMFunctionCallEx.FUNCTION_EM_CPU_STRESS_TEST_APMCU);
        A.WriteParamNo(1);
        A.WriteParamInt(index);
        if (!result) {
            Xlog.d(TAG, "AFMFunctionCallEx return false");
            response = "ERROR";
        } else {
            FunctionReturn r;
            do {
                r = A.GetNextResult();
                if (r.returnString == "") {
                    break;
                }
                response += r.returnString;
            } while (r.returnCode == AFMFunctionCallEx.RESULT_CONTINUE);
            if (r.returnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
                Xlog.d(TAG, "AFMFunctionCallEx: RESULT_IO_ERR");
                response = "ERROR";
            }
        }
        Xlog.v(TAG, "doApMcuTest response: " + response);
        if (null == response) {
            return;
        }
        switch (index) {
            case ApMcu.MASK_L2C:
                iResultL2C++;
                if (response.contains(PASS)) {
                    iResultApMcu |= (1 << ApMcu.MASK_L2C);
                    iResultPassL2C++;
                } else {
                    iResultApMcu &= ~(1 << ApMcu.MASK_L2C);
                }
                break;
            case ApMcu.MASK_NEON:
                iResultNeon++;
                if (response.contains(PASS)) {
                    iResultPassNeon++;
                    iResultApMcu |= (1 << ApMcu.MASK_NEON);
                } else {
                    iResultApMcu &= ~(1 << ApMcu.MASK_NEON);
                }
                break;
            case ApMcu.MASK_CA9:
                iResultCA9++;
                if (response.contains(PASS)) {
                    iResultPassCA9++;
                    iResultApMcu |= (1 << ApMcu.MASK_CA9);
                } else {
                    iResultApMcu &= ~(1 << ApMcu.MASK_CA9);
                }
                break;
            case ApMcu.MASK_NEON_0:
                String[] s = response.split(";");
                iResultNeon++;
                if (2 == s.length) {
                    // if (s[0].contains(PASS) && s[1].contains(PASS)) {
                    //     iResultPassNeon++;
                    // }
                    boolean bPass = true;
                    if (s[0].contains(PASS)) {
                        iResultApMcu |= (1 << ApMcu.MASK_NEON_0);
                    } else {
                        iResultApMcu &= ~(1 << ApMcu.MASK_NEON_0);
                        bPass = false;
                    }
                    if (s[1].contains(PASS)) {
                        iResultApMcu |= (1 << ApMcu.MASK_NEON_1);
                    } else if (s[1].contains(SKIP)) {
                        Xlog.d(TAG, "NEON test, CPU1 OFFLINE, skip");
                        iResultApMcu |= (1 << ApMcu.MASK_NEON_1);
                    } else {
                        iResultApMcu &= ~(1 << ApMcu.MASK_NEON_1);
                        bPass = false;
                    }
                    if (bPass) {
                        iResultPassNeon++;
                    }
                } else {
                    iResultApMcu &= ~(1 << ApMcu.MASK_NEON_0);
                    iResultApMcu &= ~(1 << ApMcu.MASK_NEON_1);
                }
                break;
            case ApMcu.MASK_CA9_0:
                iResultCA9++;
                String[] sArray = response.split(";");
                if (2 == sArray.length) {
                    // if (sArray[0].contains(PASS) && sArray[1].contains(PASS)) {
                    //     iResultPassCA9++;
                    // }
                    boolean bPass = true;
                    if (sArray[0].contains(PASS)) {
                        iResultApMcu |= (1 << ApMcu.MASK_CA9_0);
                    } else {
                        iResultApMcu &= ~(1 << ApMcu.MASK_CA9_0);
                        bPass = false;
                    }
                    if (sArray[1].contains(PASS)) {
                        iResultApMcu |= (1 << ApMcu.MASK_CA9_1);
                    } else if (sArray[1].contains(SKIP)) {
                        Xlog.d(TAG, "CA9 test, CPU1 OFFLINE, skip");
                        iResultApMcu |= (1 << ApMcu.MASK_CA9_1);
                    } else {
                        iResultApMcu &= ~(1 << ApMcu.MASK_CA9_1);
                        bPass = false;
                    }
                    if (bPass) {
                        iResultPassCA9++;
                    }
                } else {
                    iResultApMcu &= ~(1 << ApMcu.MASK_CA9_0);
                    iResultApMcu &= ~(1 << ApMcu.MASK_CA9_1);
                }
                break;
        }
    }

    private void doVideoCodecTest() {
        Xlog.v(TAG, "enter doVideoCodecTest");
        String response = "";
        int testIndex = -1;
        if (!bDualCore.booleanValue()) {
            testIndex = SwVideoCodec.SWCODEC_TEST_SINGLE;
        } else {
            switch (CpuStressTestService.sIndexMode) {
                case CpuStressTest.INDEX_SINGLE:
                    testIndex = SwVideoCodec.SWCODEC_TEST_FORCE_SINGLE;
                    break;
                case CpuStressTest.INDEX_TEST:
                case CpuStressTest.INDEX_DUAL:
                    testIndex = SwVideoCodec.SWCODEC_TEST_FORCE_DUAL;
                    break;
                default:
                    break;
            }
        }
        if (-1 == testIndex) {
            return;
        }

        AFMFunctionCallEx A = new AFMFunctionCallEx();
        boolean result = A
                .StartCallFunctionStringReturn(AFMFunctionCallEx.FUNCTION_EM_CPU_STRESS_TEST_SWCODEC);
        A.WriteParamNo(2);
        A.WriteParamInt(testIndex); // TODO change 1 to 10
        A.WriteParamInt(1);
        if (!result) {
            Xlog.d(TAG, "AFMFunctionCallEx return false");
            response = "ERROR";
        } else {
            FunctionReturn r;
            do {
                r = A.GetNextResult();
                if (r.returnString == "") {
                    break;
                }
                response += r.returnString;
            } while (r.returnCode == AFMFunctionCallEx.RESULT_CONTINUE);
            if (r.returnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
                Xlog.d(TAG, "AFMFunctionCallEx: RESULT_IO_ERR");
                response = "ERROR";
            }
        }

        Xlog.v(TAG, "doVideoCodecTest response: " + response);
        if (null == response) {
            iResultTotalVideoCodec++;
            iResultVideoCodec &= ~(0xF << SwVideoCodec.SWCODEC_MASK_SINGLE);
            return;
        }
        if (bDualCore.booleanValue()) {
            switch (CpuStressTestService.sIndexMode) {
                case CpuStressTest.INDEX_SINGLE:
                    iResultTotalVideoCodec++;
                    if (response.contains(PASS)) {
                        iResultVideoCodec |= 1 << SwVideoCodec.SWCODEC_MASK_FORCE_SINGLE;
                        iResultPassVideoCodec++;
                    } else {
                        iResultVideoCodec &= ~(1 << SwVideoCodec.SWCODEC_MASK_FORCE_SINGLE);
                    }
                    break;
                case CpuStressTest.INDEX_TEST:
                case CpuStressTest.INDEX_DUAL:
                    iResultTotalVideoCodec++;
                    String[] sArray = response.split(";");
                    if (2 == sArray.length) {
                        // if (sArray[0].contains(PASS)
                        //         && sArray[1].contains(PASS)) {
                        //     iResultPassVideoCodec++;
                        // }
                        boolean bPass = true;
                        if (sArray[0].contains(PASS)) {
                            iResultVideoCodec |= (1 << SwVideoCodec.SWCODEC_MASK_FORCE_DUAL_0);
                        } else {
                            iResultVideoCodec &= ~(1 << SwVideoCodec.SWCODEC_MASK_FORCE_DUAL_0);
                            bPass = false;
                        }
                        if (sArray[1].contains(PASS)) {
                            iResultVideoCodec |= (1 << SwVideoCodec.SWCODEC_MASK_FORCE_DUAL_1);
                        } else if (sArray[1].contains(SKIP)) {
                            Xlog.d(TAG, "SWCODEC test, CPU1 OFFLINE, skip");
                            iResultVideoCodec |= (1 << SwVideoCodec.SWCODEC_MASK_FORCE_DUAL_1);
                        } else {
                            iResultVideoCodec &= ~(1 << SwVideoCodec.SWCODEC_MASK_FORCE_DUAL_1);
                            bPass = false;
                        }
                        if (bPass) {
                            iResultPassVideoCodec++;
                        }
                    } else {
                        iResultVideoCodec &= ~(1 << SwVideoCodec.SWCODEC_MASK_FORCE_DUAL_0);
                        iResultVideoCodec &= ~(1 << SwVideoCodec.SWCODEC_MASK_FORCE_DUAL_1);
                    }
                    break;
                default:
                    break;
            }
        } else {
            iResultTotalVideoCodec++;
            if (response.contains(PASS)) {
                iResultVideoCodec |= 1 << SwVideoCodec.SWCODEC_MASK_SINGLE;
                iResultPassVideoCodec++;
            } else {
                iResultVideoCodec &= ~(1 << SwVideoCodec.SWCODEC_MASK_SINGLE);
            }
        }
    }

    private Bundle dataGenerator(int index) {
        Xlog.v(TAG, "dataGenerator index is " + index);
        Bundle data = new Bundle();
        switch (index) {
            case INDEX_TEST_APMCU:
                data.putBoolean(VALUE_RUN, bRunApMcu);
                data.putLong(VALUE_LOOPCOUNT, lLoopCountApMcu);
                data.putInt(VALUE_MASK, iMask);
                data.putInt(VALUE_RESULT, iResultApMcu);
                data.putInt(RESULT_L2C, iResultL2C);
                data.putInt(RESULT_PASS_L2C, iResultPassL2C);
                data.putInt(RESULT_NEON, iResultNeon);
                data.putInt(RESULT_PASS_NEON, iResultPassNeon);
                data.putInt(RESULT_CA9, iResultCA9);
                data.putInt(RESULT_PASS_CA9, iResultPassCA9);
                break;
            case INDEX_TEST_VIDEOCODEC:
                data.putBoolean(VALUE_RUN, bRunVideoCodec);
                data.putLong(VALUE_LOOPCOUNT, lLoopCountVideoCodec);
                data.putInt(VALUE_ITERATION, iIterationVideoCodec);
                data.putInt(VALUE_RESULT, iResultVideoCodec);
                data.putInt(RESULT_VIDEOCODEC, iResultTotalVideoCodec);
                data.putInt(RESULT_PASS_VIDEOCODEC, iResultPassVideoCodec);
                break;
            default:
                break;
        }
        return data;
    }

    public boolean isTestRun() {
        return this.bRunApMcu || this.bRunClockSwitch || this.bRunVideoCodec;
    }

    public boolean isClockSwitchRun() {
        return this.bRunClockSwitch;
    }

    private int coreNum() {
        File online = new File("/sys/devices/system/cpu/cpu1/online");
        if (online.exists()) {
            return 2;
        } else {
            return 1;
        }
    }

    public class StressTestBinder extends Binder {
        CpuStressTestService getService() {
            return CpuStressTestService.this;
        }
    }

    public interface ICpuStressTestComplete {

        public void onGetTestResult();
    }

    public void setIndexMode(int indexDefault) {
        Xlog.v(TAG, "setIndexMode: " + indexDefault + " sIndexMode: "
                + sIndexMode);
        if (indexDefault == sIndexMode) {
            return;
        }
        if (0 == sIndexMode) {
            backup(indexDefault);
        } else if (0 == indexDefault) {
            restore(sIndexMode);
        } else {
            restore(sIndexMode);
            backup(indexDefault);
        }
        sIndexMode = indexDefault;
    }

    private void backup(int index) {
        Xlog.v(TAG, "Enter backup: " + index);
        Message m = new Message();
        m.what = INDEX_TEST_BACKUP;
        m.arg1 = index + CpuStressTest.TEST_BACKUP;
        mTestHandler3.sendMessage(m);
    }

    private void restore(int index) {
        Xlog.v(TAG, "Enter restore: " + index);
        Message m = new Message();
        m.what = INDEX_TEST_RESTORE;
        m.arg1 = index + CpuStressTest.TEST_RESTORE;
        mTestHandler3.sendMessage(m);
    }

    public synchronized void updateWakeLock() {
        if (isTestRun()) {
            wakeLock.acquire(this);
        } else {
            wakeLock.release();
        }
    }

    class WakeLock {
        private PowerManager.WakeLock sScreenWakeLock = null;
        private PowerManager.WakeLock sCpuWakeLock = null;

        void acquireCpuWakeLock(Context context) {
            Xlog.v(TAG, "Acquiring cpu wake lock");
            if (sCpuWakeLock != null) {
                return;
            }

            PowerManager pm = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);

            sCpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
            // | PowerManager.ON_AFTER_RELEASE, TAG);
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
            // | PowerManager.ON_AFTER_RELEASE, TAG);
            sScreenWakeLock.acquire();
        }

        void acquire(Context context) {
            acquireScreenWakeLock(context);
            // acquireCpuWakeLock(context);
        }

        void release() {
            Xlog.v(TAG, "Releasing wake lock");
            try {
                if (sCpuWakeLock != null) {
                    sCpuWakeLock.release();
                    sCpuWakeLock = null;
                }
                if (sScreenWakeLock != null) {
                    sScreenWakeLock.release();
                    sScreenWakeLock = null;
                }
            } catch (RuntimeException e) {
                Xlog.w(TAG, "RuntimeException: " + e.getMessage());
            }
        }
    }
}
