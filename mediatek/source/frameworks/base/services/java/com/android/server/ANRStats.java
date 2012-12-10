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

package com.android.server;

import com.android.server.am.ActivityManagerService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.FileUtils;
import android.provider.Settings;
import android.util.Config;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/*debug flag for dump all thread backtrace for ANR*/	
//this class is for more ANR log mechanism	  
public class ANRStats {
    static final String TAG = "ANRStats";
    private static final String PREFIX_PROC = "/proc/";
    private static final String PREFIX_CMD = "/stat";
    private static final int[] PROCESS_CMDLINE_NAME_FORMAT = new int[] {
        Process.PROC_SPACE_TERM,
        Process.PROC_OUT_STRING,
        //Process.PROC_OUT_STRING,
    };

    private static final String[] mProcessCmdlineString = new String[1];
		public static String ANRStatsFilePath = "/data/anr/traces.txt";
    
    public static final int FULLTRACELEVEL_DIS = -1; //Disable ANR full log function
    public static final int FULLTRACELEVEL_0 = 0;//show all the thread backtrace
    public static final int FULLTRACELEVEL_1 = 1;//show java thread backtrace
    public static final int FULLTRACELEVEL_2 = 2;//show the special java thread backtrace

    //to keep current pid array
    public int [] mpids = null;
    public int [] mJavaPids = null;
    public int [] mNativePids = null;
    //keep the real length of pid array when collectCurrentPID return true;
    public int mJavaLen = 0;
    public int mNativeLen = 0;
    //size of mpids mJavaPids and mNativePids
    public int mArraySize = 0;
    
    public native int native_rtt_is_ready(int block);
    public native int native_rtt_dump_backtrace(int pid,int tid,String file_path);
    public native int native_rtt_dump_all_backtrace(int pid,String file_path);
    public native int native_rtt_dump_all_backtrace_in_one_file(int[] pid,int num,String file_path);
		
			
    public ANRStats(int num) {
        mArraySize = num;
			
        mpids = new int[num];
        for(int i=0;i<num;i++) mpids[i] = -1;
        
        //Arrays.fill(mpids,-1);
        			
        mJavaPids = new int[num];
        //Arrays.fill(mJavaPids,-1);
        for(int i=0;i<num;i++) mJavaPids[i] = -1;
        
        mNativePids = new int[num];
        for(int i=0;i<num;i++) mNativePids[i] = -1;
        //Arrays.fill(mNativePids,-1);
    }
	
    private void printJavaCurrentPID() {
        if (mJavaLen==0) {
            Slog.w(TAG,"Javapids is null,please not print");
        } else {
            for(int i=0;i<mJavaLen && mJavaPids[i]!=-1;i++){
                Slog.i(TAG,(i+1) + "th"+" curJavaPids:" + mJavaPids[i]);
            }
        }
    }

    private void printNativeCurrentPID() {
        if(mNativeLen==0) {
            Slog.w(TAG,"native pids is null,please not print");
        } else {
            for (int i=0;i<mNativeLen && mNativePids[i]!=-1;i++) {
                Slog.i(TAG,(i+1) + "th"+" curNativePids:"+mNativePids[i]);
            }
        }
    }
			
    public void collectCurrentPID() {
        int length= 0;
        int i=0;
        int [] curPids = Process.getPids("/proc", mpids);
				
        if (curPids==null) {
            Slog.e(TAG,"Warning: process getPids failed");
            mJavaLen = 0;
            mNativeLen = 0;
        } else {
            for(i=0,mJavaLen=0,mNativeLen=0;curPids[i]!=-1 && i<mArraySize;i++) {
                String cmdlinePath = PREFIX_PROC + curPids[i] + PREFIX_CMD;
                final String[] cmdline = mProcessCmdlineString;
                //Slog.i(TAG,"cmdlinePath: "+cmdlinePath);
    
                //get process name: the method following is not very good, it is best to use /proc/pid/stat
                if (Process.readProcFile(cmdlinePath,PROCESS_CMDLINE_NAME_FORMAT, cmdline, null, null)) {
                    Slog.i(TAG,"process "+curPids[i]+":"+cmdline[0]);
                
                    if (cmdline[0].indexOf(".")!=-1 || cmdline[0].indexOf("system_server")!=-1) {
                        mJavaPids[mJavaLen++] = curPids[i];
                    } else {
                        mNativePids[mNativeLen++] = curPids[i];
                    }
                } else {
                    Slog.w(TAG,"Warning: readProcFile failed");
                    //mNativePids[mNativeLen++] = curPids[i];
                }
				    }
            Slog.i(TAG,"************mJavaLen=" + mJavaLen + "**************");
            Slog.i(TAG,"************mNativeLen=" + mNativeLen + "**************");
        }

        printNativeCurrentPID();
        printJavaCurrentPID();								
    }

    public static File createFullStackTracesFile(boolean clearTraces) {
        String tracesPath = ANRStatsFilePath;
        if (tracesPath == null || tracesPath.length() == 0) {
            return null;
        }

        File tracesFile = new File(tracesPath);
        if (tracesFile!=null) {
            try {
                File tracesDir = tracesFile.getParentFile();
                if (tracesDir==null) return null;
                if (!tracesDir.exists()) tracesFile.mkdirs();
                FileUtils.setPermissions(tracesDir.getPath(), 0775, -1, -1);  // drwxrwxr-x
                
                if (clearTraces && tracesFile.exists())  {
                    File f1 = new File("/data/anr/full_traces_50.txt");
                    if (f1.exists()) f1.delete();

                    for (int i = 49; i > 0; i--) {
                        f1 = new File("/data/anr/full_traces_" + Integer.toString(i) + ".txt");
                        if (f1.exists()) {
                            File f2 = new File("/data/anr/full_traces_" + Integer.toString(i+1) + ".txt");
                            f1.renameTo(f2);
                        }
                    }
                    f1 = new File("/data/anr/full_traces_1.txt");
                    tracesFile.renameTo(f1);
                }

                tracesFile.createNewFile();
                FileUtils.setPermissions(tracesFile.getPath(), 0666, -1, -1); // -rw-rw-rw-
            } catch (IOException e) {
                Slog.w(TAG, "Unable to prepare full ANR traces file: " + tracesPath, e);
                return null;
            }
        }
        return tracesFile;
    }

    public boolean dumpAllBackTrace(int level,String file_path,ArrayList<Integer> pids) {
        
        if(file_path==null || file_path.length()==0) {
            Slog.w(TAG,"Warning: dumpAllBackTrace file_path invalid");
            return false;				    
        }

        switch(level) {

        case FULLTRACELEVEL_0:
            if(native_rtt_is_ready(0)!=0) {
                native_rtt_dump_all_backtrace_in_one_file(mNativePids,mNativeLen,file_path);
            } else {
                Slog.i(TAG,"rtt process not ready");
                return false;
            }

            if(native_rtt_is_ready(0)!=0) {
                native_rtt_dump_all_backtrace_in_one_file(mJavaPids,mJavaLen,file_path);
            } else {
                Slog.i(TAG,"rtt process not ready");
                return false;
            }	
            break;

        case FULLTRACELEVEL_1:    				
            if(native_rtt_is_ready(0)!=0) {
                native_rtt_dump_all_backtrace_in_one_file(mJavaPids,mJavaLen,file_path);
            } else {
                Slog.i(TAG,"rtt process not ready");
                return false;
            }	
            break;

        case FULLTRACELEVEL_2:
            if (pids==null) {
                Slog.e(TAG,"Error: dumpAllBackTrace level2 pids invalid");
                return false;
            }

            int [] tmpPids = new int[pids.size()];
            if (tmpPids!=null) {
                for(int i=0 ; i<pids.size() ; i++) {
                    tmpPids[i] = pids.get(i);
                }

                if (native_rtt_is_ready(0)!=0) {
                native_rtt_dump_all_backtrace_in_one_file(tmpPids,pids.size(),file_path);
                } else {
                    Slog.i(TAG,"rtt process not ready");
                    return false;
                }
            }
            break;

        case FULLTRACELEVEL_DIS:
            Slog.w(TAG,"WARN: Disable ANR full log function");
            return false;

        default:
            Slog.e(TAG,"Error: dumpAllBackTrace level wrong");
            return false;
        }
        return true;
    }

	public native boolean dumpBinderState(String file_path);
	public native boolean native_rtt_stop_trace();
}
