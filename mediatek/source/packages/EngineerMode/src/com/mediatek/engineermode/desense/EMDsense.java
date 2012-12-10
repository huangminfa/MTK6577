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

package com.mediatek.engineermode.desense;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mediatek.engineermode.ChipSupport;
import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.ShellExe;
import com.mediatek.engineermode.emsvr.AFMFunctionCallEx;
import com.mediatek.engineermode.emsvr.FunctionReturn;

public class EMDsense {
    // constants below.
    public final static String TAG = "EM_DSENSE";
    public final static String LCD_DUTY_FILE = "/sys/class/leds/lcd-backlight/brightness"; // duty
    public final static String LCD_FREQ_FILE = "/sys/class/leds/lcd-backlight/div";
    public final static String LCD_FREQ_HZ_FILE = "/sys/class/leds/lcd-backlight/frequency";

    private final static int FB0_LCDWriteCycleGetMinVal = 1;
    private final static int FB0_LCDWriteCycleGetCurrentVal = 2;
    private final static int FB0_LCDWriteCycleSetVal = 3;

    private static int FB0_Fucntion(int... param) {
        AFMFunctionCallEx A = new AFMFunctionCallEx();
        boolean result = A
                .StartCallFunctionStringReturn(AFMFunctionCallEx.FUNCTION_EM_FB0_IOCTL);
        A.WriteParamNo(param.length);
        for (int i : param) {
            A.WriteParamInt(i);
        }

        if (!result) {
            return -1;
        }

        int valueRet = -1;
        FunctionReturn r;
        do {
            r = A.GetNextResult();
            if (r.returnString == "") {
                break;
            } else {
                if (r.returnString.equalsIgnoreCase("FFFFFFFF")) {
                    valueRet = -1;
                    break;
                }
                try {
                    valueRet = Integer.valueOf(r.returnString);
                } catch (NumberFormatException e) {
                    Elog.e(TAG, r.returnString);
                    valueRet = -1;
                }
            }
        } while (r.returnCode == AFMFunctionCallEx.RESULT_CONTINUE);

        if (r.returnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
            // error
            return -1;
        } else {
            return valueRet;
        }
    }

    public static int LCDWriteCycleGetMinVal() {
        return FB0_Fucntion(FB0_LCDWriteCycleGetMinVal);
    }

    public static int LCDWriteCycleGetCurrentVal() {
        return FB0_Fucntion(FB0_LCDWriteCycleGetCurrentVal);
    }

    public static int LCDWriteCycleSetVal(int level) {
        return FB0_Fucntion(FB0_LCDWriteCycleSetVal, level);
    }

    public static native int ClassDSwitch(boolean on);

    public static native int getClassDStatus();

    public static int BacklightSetPwmFreq(int level) {
        if (level > 7 || level < 0) {
            return -1;
        }
        String cmd = "echo " + level + " > " + LCD_FREQ_FILE;

        String r = getInfo(cmd);
        Elog.v(DesenseActivity.TAG, "BacklightSetPwmFreq level = " + level);
        if (null == r) {
            return -1;
        } else {
            return 0;
        }
    }

    public static int BacklightGetCurrentPwmFreq() {
        String cmd = "cat " + LCD_FREQ_FILE;

        String r = getInfo(cmd);
        if (null == r) {
            return -1;
        } else {
            return Integer.valueOf(r);
        }
    }

    public static int BacklightSetPwmDuty(int level) {
        String cmd = "echo " + level + " > " + LCD_DUTY_FILE;

        String r = getInfo(cmd);
        if (null == r) {
            return -1;
        } else {
            return 0;
        }

    }

    public static int BacklightGetCurrentPwmDuty() {
        String cmd = "cat " + LCD_DUTY_FILE;

        String r = getInfo(cmd);
        if (null == r) {
            return -1;
        } else {
            return Integer.valueOf(r);
        }
    }

    public static String BacklightGetCurrentPwmFreqHZ() {
        String cmd = "cat " + LCD_FREQ_HZ_FILE;

        String r = getInfo(cmd);

        return r;
    }

    public static class PLLStruct {
        public int id;
        public String name;
        public String hexVal;

    }

    public static ArrayList<PLLStruct> PLLGetAllInfo() {
        String cmd = "cat /proc/pm_pll_fsel";
        if (ChipSupport.GetChip() == ChipSupport.MTK_6575_SUPPORT) {
            cmd = "cat /proc/clkmgr/pll_fsel";
        }
        Elog.i(DesenseActivity.TAG, cmd);
        String info = getInfo(cmd);
        String regex = "\\[[\\s\\S]*?\\]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(info);

        int idx = 0;
        ArrayList<PLLStruct> list = new ArrayList<PLLStruct>();
        String v = null;
        EMDsense.PLLStruct s = null;

        while (m.find()) {
            v = m.group();

            if (idx == 0) {
                s = new EMDsense.PLLStruct();
                s.id = Integer.valueOf(v.substring(1, v.length() - 1));
                idx++;
            } else if (idx == 1) {
                s.name = v.substring(1, v.length() - 1);
                idx++;
            } else {
                if (v.equalsIgnoreCase("[-1]")) {
                    s.hexVal = "-1";
                } else {
                    s.hexVal = v.substring(3, v.length() - 1);
                }
                idx = 0;
                list.add(s);
            }
        }
        return list;
    }

    public static int PLLSetClock(int id, String hexVal) {
        String cmd = "echo 1 " + id + " 1 > /proc/pm_pll_test";
        if (ChipSupport.GetChip() == ChipSupport.MTK_6575_SUPPORT) {
            cmd = "echo enable " + id + " >/proc/clkmgr/pll_test";
        }
        Elog.i(DesenseActivity.TAG, cmd);
        String ret = getInfo(cmd);
        if (ret == null) {
            return -1;
        }

        cmd = "echo " + id + " " + hexVal + " > /proc/pm_pll_fsel";
        if (ChipSupport.GetChip() == ChipSupport.MTK_6575_SUPPORT) {
            cmd = "echo " + id + " " + hexVal + " >/proc/clkmgr/pll_fsel";
        }
        String info = getInfo(cmd);
        Elog.i(DesenseActivity.TAG, cmd);
        Elog.i(DesenseActivity.TAG, "getInfo(cmd): " + info);
        return 0;

    }

    private static String getInfo(String cmd) {
        String result = null;
        try {
            String[] cmdx = { "/system/bin/sh", "-c", cmd }; // file must
                                                             // exist// or
                                                             // wait() return2
            int ret = ShellExe.execCommand(cmdx);
            if (0 == ret) {
                result = ShellExe.getOutput();
            } else {
                // result = null;
                result = ShellExe.getOutput();
            }

        } catch (IOException e) {
            Elog.i(TAG, e.toString());
            result = null;
        }
        return result;
    }

    static {
        System.loadLibrary("em_dsense_jni");

    }
}
