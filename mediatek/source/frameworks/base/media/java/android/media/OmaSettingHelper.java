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

package android.media;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.Uri;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

/**
* @hide
*/
public class OmaSettingHelper {
    private static final String TAG = "OmaSettingHelper";
    private static final boolean LOG = true;
    
    private static final int UNKNOWN_PORT = -1;
    
    //rtsp info
    private static final String KEY_NAME = "NAME";
    private static final String KEY_PROVIDER_ID = "PROVIDER-ID";
    private static final String KEY_TO_PROXY = "TO-PROXY";
    private static final String KEY_TO_NAPID = "TO-NAPID";
    private static final String KEY_MAX_BANDWIDTH = "MAX-BANDWIDTH";
    private static final String KEY_NETINFO = "NETINFO";
    private static final String KEY_MIN_UDP_PORT = "MIN-UDP-PORT";
    private static final String KEY_MAX_UDP_PORT = "MAX-UDP-PORT";
    private static final String KEY_SIM_ID = "SIM-ID";
    
    private static final String KEY_RTSP_PROXY_HOST = "MTK-RTSP-PROXY-HOST";
    private static final String KEY_RTSP_PROXY_PORT = "MTK-RTSP-PROXY-PORT";
    private static final String KEY_HTTP_PROXY_HOST = "MTK-HTTP-PROXY-HOST";
    private static final String KEY_HTTP_PROXY_PORT = "MTK-HTTP-PROXY-PORT";
    
    private static final String KEY_HTTP_BUFFER_SIZE = "MTK-HTTP-CACHE-SIZE";
    private static final String KEY_RTSP_BUFFER_SIZE = "MTK-RTSP-CACHE-SIZE";
    private static final int DEFAULT_HTTP_BUFFER_SIZE = 10;//seconds
    private static final int DEFAULT_RTSP_BUFFER_SIZE = 6;//seconds
    
    /**
     * Indicates the file is unknown streaming type.
     */
    public static final int STREAMING_UNKNOWN = -1;
    
    /**
     * Indicates the file is local file.
     */
    public static final int STREAMING_LOCAL = 0;
    
    /**
     * Indicates the file is http streaming type.
     */
    public static final int STREAMING_HTTP = 1;
    
    /**
     * Indicates the file is rtsp streaming type.
     */
    public static final int STREAMING_RTSP = 2;
    
    /**
     * Judge the input Uri's streaming type.
     * @param uri
     * @return the current uri's streamign type.
     */
    public static int judgeStreamingType(Uri uri) {
        if (LOG) Log.i(TAG, "judgeStreamingType(" + String.valueOf(uri) + ")");
        int streamingType = STREAMING_UNKNOWN;
        if (uri == null) {
            Log.w(TAG, "uri is null, cannot judge streaming type.");
            return -1;
        }
        String scheme = uri.getScheme();
        if ("rtsp".equalsIgnoreCase(scheme)) {
            streamingType = STREAMING_RTSP;
        } else if ("http".equalsIgnoreCase(scheme)) {
            streamingType = STREAMING_HTTP;
        } else {
            streamingType = STREAMING_LOCAL;
        }
        if (LOG) Log.i(TAG, "judgeStreamingType() return " + streamingType);
        return streamingType;
    }
    
    /**
     * Whether oma is supported or not.
     * @return true enabled, otherwise false.
     */
    public static boolean isEnabledOMA() {
        boolean enabled = com.mediatek.featureoption.FeatureOption.MTK_OMACP_SUPPORT 
            || com.mediatek.featureoption.FeatureOption.MTK_DM_APP;
        if (LOG) Log.i(TAG, "isEnabledOMA()=" + enabled);
        return enabled;
    }
    
    /**
     * Fill the header according the feature option.
     * @param context
     * @param uri
     * @param headers
     * @return
     */
    public static Map<String, String> setSettingHeader(Context context, Uri uri, Map<String, String> headers) {
        if (LOG) Log.i(TAG, "setSettingHeader(" + uri + "," + headers + ")");
        if (isEnabledOMA() || isCMCCOperator()) {
            int type = judgeStreamingType(uri);
            if (STREAMING_RTSP == type || STREAMING_HTTP == type) {
                return setOmaSettingHeader(context, headers);
            }
        }
        return headers;
    }
    
    /**
     * Read OMA RTSP settings and fill these info into headers.
     * If headers is null, new Map<String, String> object will be created and returned.
     * Otherwise, key value will be filled into headers.
     * @param context
     * @param headers
     * @return filled headers
     */
    public static Map<String, String> setOmaSettingHeader(Context context, Map<String, String> headers) {
        if (LOG) Log.i(TAG, "setOmaSettingHeader(" + context + "," + headers + ")");
        if (context == null) {
            Log.w(TAG, "setOmaSettingHeader() context=null, cannot oma get rtsp setting.");
            return headers;
        }
        //get oma rtsp setting
        //Settings supports client and server cache mechanism,
        //so here we needn't to cache the values.
        ContentResolver cr = context.getContentResolver();
        Map<String, String> tempHeaders = headers;
        if (tempHeaders == null) {
            tempHeaders = new HashMap<String, String>();
        }
        
        int minUdpPort = UNKNOWN_PORT;
        int maxUdpPort = UNKNOWN_PORT;
        int rtspProxyEnable = 0;//0 false, 1 true
        String rtspProxyHost = null;
        int rtspProxyPort = UNKNOWN_PORT;
        int httpProxyEnable = 0;//0 false, 1 true
        String httpProxyHost = null;
        int httpProxyPort = UNKNOWN_PORT;
        
        //get buffer size info
        int httpBufferSize = Settings.System.getInt(cr, KEY_HTTP_BUFFER_SIZE, DEFAULT_HTTP_BUFFER_SIZE);
        fillHeader(tempHeaders, KEY_HTTP_BUFFER_SIZE, String.valueOf(httpBufferSize));
        int rtspBufferSize = Settings.System.getInt(cr, KEY_RTSP_BUFFER_SIZE, DEFAULT_RTSP_BUFFER_SIZE);
        fillHeader(tempHeaders, KEY_RTSP_BUFFER_SIZE, String.valueOf(rtspBufferSize));
        
        //get rtsp udp port info
        minUdpPort = Settings.System.getInt(cr, MediaStore.Streaming.Setting.MIN_UDP_PORT, UNKNOWN_PORT);
        maxUdpPort = Settings.System.getInt(cr, MediaStore.Streaming.Setting.MAX_UDP_PORT, UNKNOWN_PORT);
        if (minUdpPort != UNKNOWN_PORT && maxUdpPort != UNKNOWN_PORT) {
            fillHeader(tempHeaders, KEY_MIN_UDP_PORT, String.valueOf(minUdpPort));
            fillHeader(tempHeaders, KEY_MAX_UDP_PORT, String.valueOf(maxUdpPort));
        }
        //get rtsp proxy info
        rtspProxyEnable = Settings.System.getInt(cr, MediaStore.Streaming.Setting.RTSP_PROXY_ENABLED, 0);
        if (rtspProxyEnable == 1) {
            rtspProxyHost = Settings.System.getString(cr, MediaStore.Streaming.Setting.RTSP_PROXY_HOST);
            rtspProxyPort = Settings.System.getInt(cr, MediaStore.Streaming.Setting.RTSP_PROXY_PORT, UNKNOWN_PORT);
            if (rtspProxyHost != null && rtspProxyPort != UNKNOWN_PORT) { 
                fillHeader(tempHeaders, KEY_RTSP_PROXY_HOST, rtspProxyHost);
                fillHeader(tempHeaders, KEY_RTSP_PROXY_PORT, String.valueOf(rtspProxyPort));
            }
        }
        //get http proxy info
        httpProxyEnable = Settings.System.getInt(cr, MediaStore.Streaming.Setting.HTTP_PROXY_ENABLED, 0);
        if (httpProxyEnable == 1) {
            httpProxyHost = Settings.System.getString(cr, MediaStore.Streaming.Setting.HTTP_PROXY_HOST);
            httpProxyPort = Settings.System.getInt(cr, MediaStore.Streaming.Setting.HTTP_PROXY_PORT, UNKNOWN_PORT);
        }
        //If not enable streaming http proxy or not set streaming proxy,
        //pass the wifi or gprs's proxy to stagefright.
        //Otherwise, use streaming http proxy instead.
        if (httpProxyEnable != 1 || httpProxyPort == UNKNOWN_PORT) {
            //Proxy will returns corresponding proxy host and port according 
            //to the connection type(mobile or wifi) since 4.0.
            httpProxyHost = Proxy.getHost(context);
            httpProxyPort = Proxy.getPort(context);
        }
        
        if (httpProxyHost != null && httpProxyPort != UNKNOWN_PORT) {
            fillHeader(tempHeaders, KEY_HTTP_PROXY_HOST, httpProxyHost);
            fillHeader(tempHeaders, KEY_HTTP_PROXY_PORT, String.valueOf(httpProxyPort));
        }
        if (LOG) {
            Log.i(TAG, "getSettings() minUdpPort=" + minUdpPort);
            Log.i(TAG, "getSettings() maxUdpPort=" + maxUdpPort);
            Log.i(TAG, "getSettings() rtspProxyEnable=" + rtspProxyEnable);
            Log.i(TAG, "getSettings() rtspProxyHost=" + rtspProxyHost);
            Log.i(TAG, "getSettings() rtspProxyPort=" + rtspProxyPort);
            Log.i(TAG, "getSettings() httpProxyEnable=" + httpProxyEnable);
            Log.i(TAG, "getSettings() httpProxyHost=" + httpProxyHost);
            Log.i(TAG, "getSettings() httpProxyPort=" + httpProxyPort);
            Log.v(TAG, "getSettings() httpBufferSize=" + httpBufferSize);
            Log.v(TAG, "getSettings() rtspBufferSize=" + rtspBufferSize);
            
            java.util.Set<String> keySet = tempHeaders.keySet();
            String[] keys = new String[keySet.size()];
            keySet.toArray(keys);
            for(String key : keys) {
                Log.i(TAG, "setHeaders() header " + key + "=" + tempHeaders.get(key));
            }
        }
        return tempHeaders;
    }
    
    private static void fillHeader(Map<String, String> headers, String key, String value) {
        if (value != null && !"".equals(value.trim())) {
            headers.put(key, value);
        } else {
            Log.w(TAG, "cannot fill key=" + key + " value=" + value);
        }
    }

    //operator info
    private static final String OPERATOR_OPTR = "ro.operator.optr";
    private static final String OPERATOR_SPEC = "ro.operator.spec";
    private static final String OPERATOR_SEG = "ro.operator.seg";
    private static final String OPERATOR_OPTR_CMCC = "OP01";
    private static final String OPERATOR_OPTR_ORANGE = "OP03";
    
    private static boolean sIsGetOperatorInfo = false;
    private static String sOperatorOptr;
    private static String sOperatorSpec;
    private static String sOperatorSeg;
    
    private static String getOperatorOptr() {
        if (!sIsGetOperatorInfo) {
            getOperatorInfo();
            sIsGetOperatorInfo = true;
        }
        if (LOG) Log.i(TAG, "getOperatorOptr() sOperatorOptr=" + sOperatorOptr);
        return sOperatorOptr;
    }
    
    private static void getOperatorInfo() {
        sOperatorOptr = SystemProperties.get(OPERATOR_OPTR);
        sOperatorSpec = SystemProperties.get(OPERATOR_SPEC);
        sOperatorSeg = SystemProperties.get(OPERATOR_SEG);
        if (LOG) Log.i(TAG, "getOperatorInfo() sOperatorOptr=" + sOperatorOptr);
        if (LOG) Log.i(TAG, "getOperatorInfo() sOperatorSpec=" + sOperatorSpec);
        if (LOG) Log.i(TAG, "getOperatorInfo() sOperatorSeg=" + sOperatorSeg);
    }
    
    /**
     * Current operator is CMCC or not.
     * @return
     */
    public static boolean isCMCCOperator() {
        boolean result = false;
        String optr = getOperatorOptr();
        if (OPERATOR_OPTR_CMCC.equals(optr)) {
            result = true;
        }
        if (LOG) Log.i(TAG, "isCMCCOperator() return " + result);
        return result;
    }
}
