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

package com.mediatek.GoogleOta;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.mediatek.GoogleOta.Util.DeviceInfo;
import com.mediatek.GoogleOta.Util.DownloadDescriptor;
import com.mediatek.featureoption.FeatureOption;

public class HttpManager {
    private static final String TAG = "HttpManager:";
    private static HttpManager mHttpManager;
    private static final String NETTYPENAME = "";
    public static CookieStore cookies = null;

    private static final int HTTP_RESPONSE_SUCCESS         = 1000;
    private static final int HTTP_RESPONSE_SN_REQUIRE      = 1001;
    private static final int HTTP_RESPONSE_AUTHEN_ERROR    = 1002;
    private static final int HTTP_RESPONSE_ILLEGAL_ACCESS  = 1004;
    private static final int HTTP_RESPONSE_TOKEN_REQUIRE   = 1005;
    private static final int HTTP_RESPONSE_TOKEN_INVALID   = 1006;
    private static final int HTTP_RESPONSE_SN_LOST         = 1008;
    private static final int HTTP_RESPONSE_VERSION_REQUIRE = 1009;
    private static final int HTTP_RESPONSE_NO_NEW_VERSION  = 1010;
    private static final int HTTP_RESPONSE_DATABASE_ERROR  = 1103;
    private static final int HTTP_RESPONSE_PARAM_ERROR     = 1104;
    private static final int HTTP_RESPONSE_VERSION_ILLEGAL = 1105;
    private static final int HTTP_RESPONSE_VERSION_DELETE  = 1106;
    private static final int HTTP_RESPONSE_NO_SPEC_NETWORK = 1200;
    private static final int HTTP_RESPONSE_NETWORK_ERROR   = 1201;
    private static final int HTTP_RESPONSE_DELTA_DELETE    = 1900;
    private static final int HTTP_DETECTED_SDCARD_CRASH_OR_UNMOUNT = 1901;
    private static final int HTTP_DETECTED_SDCARD_ERROR            = 1902;
    private static final int HTTP_DETECTED_SDCARD_INSUFFICENT      = 1903;
    private static final int HTTP_UNKNOW_ERROR                     = 2000;
    private static final int HTTP_RESPONSE_CONN_TIMEOUT            = 2001;
    private static final int HTTP_RESPONSE_UNZIP_ERROR             = 2002;
    private static final int HTTP_RESPONSE_UNZIP_CKSUM             = 2003;
    
    private boolean mQueryAbort = false;
    private boolean mIsDownloading = false;

    private Handler mHandler;
    private Context mContext = null;
    private DownloadDescriptor mNewVersionInfo = null;
    private DownloadStatus mDownloadStatus = null;
    private int mErrorCode = HTTP_RESPONSE_SUCCESS;
    private int mOTAresult = Util.OTAresult.CHECK_OK;
    private NotifyManager mNotification;
    private DefaultHttpClient mHttpClient;


    public static HttpManager getInstance(Context context) {
    	Util.logInfo(TAG, "getInstance, context = "+context+", mHttpManager = "+mHttpManager); 
        if (mHttpManager == null) {
            return mHttpManager = new HttpManager(context);
        } else {
            return mHttpManager;
        }
    }
    private HttpManager(Context context) {
    	Util.logInfo(TAG, "HttpManager enter"); 
        mContext = context;
        mDownloadStatus = DownloadStatus.getInstance(mContext);
        mNotification = new NotifyManager(mContext);
    }

    public void onSetMessageHandler(Handler mHandler) {
    	Util.logInfo(TAG, "onSetMessageHandler, mHandler = "+mHandler); 
    	this.mHandler = mHandler;
    }

    public void onSetAbort(boolean isAbort) {
        mQueryAbort = isAbort;
    }

    public void onQueryNewVersion() {
        boolean hasNewVersion = false;
        resetDescriptionInfo();
        hasNewVersion = onQuery();
        if (mQueryAbort) {
        	Util.logInfo(TAG, "onQueryNewVersion, query abort, return directly"); 
            return;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat(Util.DATE_FORMAT);
        String strDate = dateFormat.format(new Date());
        mDownloadStatus.setQueryDate(strDate);
        
        Util.logInfo(TAG, "onQueryNewVersion, hasNewVersion = "+hasNewVersion+", mErrorCode = "+mErrorCode); 
        if (hasNewVersion) {
        	mDownloadStatus.setDownloadDesctiptor(mNewVersionInfo);
        	mDownloadStatus.setDLSessionStatus(DownloadStatus.STATE_NEWVERSION_READY);
	        if(mHandler == null) {
	            mNotification.showNewVersionNotification(mNewVersionInfo.version);
	            Util.logInfo(TAG, "onQueryNewVersion, Show new version notification."); 
	        } else {
	            mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_NEWVERSIONDETECTED));
	            Util.logInfo(TAG, "onQueryNewVersion, Send new version founded message to activity.");
	        }
        } else {
            sendErrorMessage();
        }
    }
    
    public void resetDescriptionInfo() {
    	Util.logInfo(TAG, "resetDescriptionInfo");

		mNeedServiceDo = MSG_DELETE_COMMANDFILE;
		boolean isbind = doBindService(mContext);
		if (isbind) {
			doActionUseService();
		}
		
		Util.deleteFile(Util.getPackagePathName(mContext));

		mDownloadStatus.setDownLoadPercent(-1);
		mDownloadStatus.setDLSessionUnzipState(false);
        mDownloadStatus.setDLSessionRenameState(false);
		mDownloadStatus.setDLSessionStatus(DownloadStatus.STATE_QUERYNEWVERSION);
		DownloadDescriptor dd = mDownloadStatus.getDownloadDescriptor();
		if (dd != null) {
		    dd.deltaId = -1;
		    dd.size = -1;
		    dd.newNote = null;
		    dd.version = null;
		    mDownloadStatus.setDownloadDesctiptor(dd);
		}
    }
    
    private boolean onQuery() {
        Util.logInfo(TAG, "onQuery"); 
        boolean isNetworkAvailable = isNetworkAvailable(mContext, NETTYPENAME);
        if (!isNetworkAvailable) {
            return false;
        }
        boolean isAuthenPass = onHandsakeAuthentication();
        if (!isAuthenPass) {
            return false;
        }
        boolean hasNewVersion = onCheckNewVersion();
        if (!hasNewVersion) {
            return false;
        }
        return true;
    }

    private boolean onHandsakeAuthentication() {
        Util.logInfo(TAG, "onHandsakeAuthentication"); 
        if (mContext == null) {
            return false;
        }
        String url = mContext.getResources().getString(R.string.address_login);
        cookies = null;
        DeviceInfo deviceInfo = Util.getDeviceInfo(mContext);
        Util.logInfo(TAG, "onHandsakeAuthentication, imei = "+deviceInfo.imei+", sn = "+deviceInfo.sn+", sim = "+deviceInfo.sim+", operator = "+deviceInfo.operator);
        BasicNameValuePair imei = new BasicNameValuePair("imei", deviceInfo.imei);
        //BasicNameValuePair sn = new BasicNameValuePair("sn", deviceInfo.sn);
        BasicNameValuePair sim = new BasicNameValuePair("sim", deviceInfo.sim);
        BasicNameValuePair operator = new BasicNameValuePair("operator", deviceInfo.operator);
        BasicNameValuePair sn=new BasicNameValuePair("sn", "15811375356");
        ArrayList<BasicNameValuePair> bnvpa = new ArrayList<BasicNameValuePair>();
        bnvpa.add(imei);
        bnvpa.add(sn);
        bnvpa.add(sim);
        bnvpa.add(operator);

        HttpResponse response = doPost(url, null, bnvpa);
        if (response == null) {
        	Util.logInfo(TAG, "onHandsakeAuthentication, net request fail response = null");
        	return false;
        }
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() != HttpStatus.SC_OK) {
        	Util.logInfo(TAG, "onHandsakeAuthentication, ReasonPhrase = "+status.getReasonPhrase());
        	mErrorCode = HTTP_RESPONSE_AUTHEN_ERROR;
        	return false;
        }
        
        try {
            String content = getChunkedContent(response.getEntity());
            Util.logInfo(TAG, "onHandsakeAuthentication, response content = "+content); 
            HttpResponseContent res = parseAuthenInfo(content);
            if (res == null) {
                return false;
            }
            BasicClientCookie cookie1 = new BasicClientCookie("PHPRAND", String.valueOf(res.rand));
            if (cookies == null) {
                Util.logError("HttpManager:onHandsakeAuthentication, cookies = null, it is a error");
                mErrorCode = HTTP_RESPONSE_CONN_TIMEOUT;
                return false;
            }
            cookies.addCookie(cookie1);
            Util.logInfo(TAG, "cookies size = "+cookies.getCookies().size());
            Util.logInfo(TAG, "onHandsakeAuthentication, rand = "+res.rand+", sessionId = "+ res.sessionId); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    private boolean onReHandsakeAuthentication() {
        Util.logInfo(TAG, "onReHandsakeAuthentication"); 
        return onHandsakeAuthentication();
    }

    private boolean onCheckNewVersion() {
        Util.logInfo(TAG, "onCheckNewVersion"); 
        String url = mContext.getResources().getString(R.string.address_check_version);
        try {
            String tokenCode = getToken();
            if (tokenCode == null) {
                mErrorCode = HTTP_RESPONSE_AUTHEN_ERROR;
                return false;
            }
            BasicNameValuePair token   = new BasicNameValuePair("token",URLEncoder.encode(tokenCode));
            BasicNameValuePair version = new BasicNameValuePair("version", Util.getDeviceVersionInfo());
            ArrayList<BasicNameValuePair> bnvpa = new ArrayList<BasicNameValuePair>();
            bnvpa.add(version);
            bnvpa.add(token);
            HttpResponse response = doPost(url, null, bnvpa);
            if (response == null) {
            	Util.logInfo(TAG, "onCheckNewVersion, net request fail response = null");
            	return false;
            }
            StatusLine status = response.getStatusLine();	
            if (status.getStatusCode() != HttpStatus.SC_OK) {
                Util.logInfo(TAG, "onCheckNewVersion, ReasonPhrase = "+status.getReasonPhrase());
                cookies = null;
                mErrorCode = HTTP_RESPONSE_AUTHEN_ERROR;
                return false;
            }
            String content = getChunkedContent(response.getEntity());
            Util.logInfo(TAG, "onCheckNewVersion, response content = "+content); 
            HttpResponseContent res = parseCheckVersionInfo(content);
            Util.logInfo(TAG, "onCheckNewVersion, res = "+res);
            if (res == null) {
                return false;
            }
            if (res.fileSize <= 0 || res.deltaId < 0) {
            	mErrorCode = HTTP_RESPONSE_NO_NEW_VERSION;
            	Util.logError("HttpManager:onCheckNewVersion, fileSize = "+res.fileSize + ", deltaId = "+res.deltaId);
                return false;
            }
            mNewVersionInfo = mDownloadStatus.getDownloadDescriptor();
            mNewVersionInfo.newNote = res.releaseNote;
            mNewVersionInfo.version = res.versionName;
            mNewVersionInfo.size = res.fileSize;
            mNewVersionInfo.deltaId = res.deltaId;
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            mErrorCode = HTTP_RESPONSE_AUTHEN_ERROR;
            return false;
        }
    }

    private HttpResponse doPost(String url, Map<String, String> headers, ArrayList<BasicNameValuePair> bnvpa) {
        HttpResponse response=null;
        HttpParams httpParam = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParam, 30*1000);
        HttpConnectionParams.setSoTimeout(httpParam, 30*1000);
        mHttpClient = new DefaultHttpClient(httpParam);
        DefaultHttpClient client = mHttpClient;
        Util.logInfo(TAG, "doPost, url = "+url+", cookies = "+cookies); 
        HttpContext localcontext=new BasicHttpContext();
        if (cookies != null) {
        	localcontext.setAttribute(ClientContext.COOKIE_STORE, cookies);
        }

        try {
            HttpHost host = null;
            HttpPost httpPost = null;

            if (url.contains("https")) {
                Uri u = Uri.parse(url);
                host = new HttpHost(u.getHost(), 443, u.getScheme());
                httpPost = new HttpPost(u.getPath());
            } else {
                httpPost = new HttpPost(url);
            }

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            
            if (bnvpa != null) {
				httpPost.setEntity(new UrlEncodedFormEntity(bnvpa));
			}
            try {
                if (url.contains("https")) {
            	    Util.logInfo(TAG, "doPost, https");
                    response = client.execute(host, httpPost);
                } else {
            	    Util.logInfo(TAG, "doPost, http");
            	    response = client.execute(httpPost,localcontext);
                }
                if (cookies == null) {
                    cookies = client.getCookieStore();
                    Util.logInfo(TAG, "cookies size = "+cookies.getCookies().size());
                }
                return response;
            } catch (ConnectTimeoutException e) {
                Util.logError("HttpManager:doPost, timeout");
                e.printStackTrace();
                mErrorCode = HTTP_RESPONSE_CONN_TIMEOUT;
            }
        } catch (Exception e) {
            Util.logError("HttpManager:doPost, url = "+url);
            e.printStackTrace();
            mErrorCode = HTTP_RESPONSE_CONN_TIMEOUT;
        }
        return response;
    }

    private String getChunkedContent(HttpEntity entity) throws IOException {
        Util.logInfo(TAG, "getChunkedContent"); 
        int rCount = 0;
        byte[] ret = new byte[0];
        byte[] buff = new byte[1024];
        Util.logInfo(TAG, "getChunkedContent, isChunked = "+ Boolean.valueOf(entity.isChunked()).toString());
        InputStream in = entity.getContent();
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();

        try {
            while ((rCount = in.read(buff, 0, 1024)) > 0) {
                swapStream.write(buff, 0, rCount);
            }
            ret = swapStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            onShutdownConn();
            if (mDownloadStatus.getDLSessionStatus() == DownloadStatus.STATE_DOWNLOADING) {
                mDownloadStatus.setDLSessionStatus(DownloadStatus.STATE_PAUSEDOWNLOAD);
                Util.logError("HttpManagergetChunkedContent, exception to set pause state");
            }
	    mErrorCode = HTTP_RESPONSE_CONN_TIMEOUT;
        }
        return new String(ret);
    }

    private HttpResponseContent parseAuthenInfo(String result) {
    	Util.logInfo(TAG, "parseAuthenInfo"); 
        try {
            JSONObject jo = new JSONObject(result);
            HttpResponseContent res = new HttpResponseContent();
            if (jo.getInt("status") == HTTP_RESPONSE_SUCCESS) {
            	res.rand = jo.getInt("rand");
                res.sessionId = jo.getString("sessionId");
                mErrorCode = HTTP_RESPONSE_SUCCESS;
                return res;
            } else {
            	String info = jo.getString("info");
            	mErrorCode = jo.getInt("status");
            	Util.logError("HttpManager:parseAuthenInfo, error info = "+info);
            	return null;
            }
	} catch (Exception e) {
	    e.printStackTrace();
	    onShutdownConn();
	    mErrorCode = HTTP_RESPONSE_CONN_TIMEOUT;
	    return null;
        }
    }

    private HttpResponseContent parseCheckVersionInfo(String result) {
    	Util.logInfo(TAG, "parseCheckVersionInfo"); 
        try {
            JSONObject jo = new JSONObject(result);
            HttpResponseContent res = new HttpResponseContent();
            if (jo.getInt("status") == HTTP_RESPONSE_SUCCESS) {
            	res.versionName = jo.getString("name");
                res.fileSize = jo.getLong("size");
                res.releaseNote = jo.getString("release_notes");
                res.deltaId = jo.getInt("deltaId");
                mErrorCode = HTTP_RESPONSE_SUCCESS;
                Util.logInfo(TAG, "parseCheckVersionInfo, res.deltaId = "+ res.deltaId);
                return res;
            } else {
            	String info = jo.getString("info");
            	mErrorCode = jo.getInt("status");
            	Util.logError("HttpManager:parseCheckVersionInfo, error info = "+info);
            	return null;
            }
	} catch (Exception e) {
	    e.printStackTrace();
	    onShutdownConn();
	    mErrorCode = HTTP_RESPONSE_CONN_TIMEOUT;;
	    return null;
        }
    }

    private String getToken() {
    	Util.logInfo(TAG, "getToken"); 
        StringBuffer buf = new StringBuffer();
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            String str = null;
            if (cookies != null) {
            	
            	int nSize = cookies.getCookies().size();
            	
            	if(nSize <1) {
                	Util.logInfo(TAG, "Error: cookies.getCookies().size() is "+ nSize); 
                	return null;
            	}
            	
            	Cookie cookieRand = cookies.getCookies().get(nSize-1);
            	
            	if((cookieRand == null)||(cookieRand.getName() != "PHPRAND")) {
            		return null;
            	}

                str = "15811375356" + cookieRand.getValue();
                Util.logInfo(TAG, "getToken, str = "+str);
            } else {
                if (onReHandsakeAuthentication()) {
                    str = "15811375356" + cookies.getCookies().get(1).getValue();
                } else {
                	Util.logInfo(TAG, "getToken, reHandshake error"); 
                	return null;
                }
            }
            md5.update(str.getBytes());
            byte[] bytes=md5.digest();
            for(int i = 0; i < bytes.length; i++) {
                String s = Integer.toHexString(bytes[i] & 0xff);
                if(s.length()==1) {
                    buf.append("0");
                }
                buf.append(s);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        return buf.toString();
    }

    int writeFile(HttpResponse response, long currSize) {
    	Util.logInfo(TAG, "writeFile");
        try {
        	//response.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        	InputStream in = response.getEntity().getContent();
    		File ifolder = new File(Util.getPackagePathName(mContext));
    		if (!ifolder.exists()) {
    		    boolean cr = ifolder.mkdirs();
    		    Util.logInfo(TAG, "writeFile, create googleota folder result ="+cr);
    		}
    		RandomAccessFile out = null;
    		try {
	            out = new RandomAccessFile(Util.getPackageFileName(mContext), "rws");
	            out.seek(currSize);
            } catch (Exception e) {
                e.printStackTrace();
                onShutdownConn();
                return mErrorCode = HTTP_DETECTED_SDCARD_CRASH_OR_UNMOUNT;
    		}
            byte[] buff = new byte[4096];
            int rc = 0;
            int i = 0;
            int j = 0;
            boolean rightnow = false;
            boolean finish = false;
            while ((rc = in.read(buff, 0, 4096)) > 0) {
            	try {
                    out.write(buff, 0, rc);
                } catch (Exception e) {
                    e.printStackTrace();
                    onShutdownConn();
                    return mErrorCode = HTTP_DETECTED_SDCARD_CRASH_OR_UNMOUNT;
        	}
                i++;
                int status = mDownloadStatus.getDLSessionStatus();
                if (status == DownloadStatus.STATE_PAUSEDOWNLOAD ||
                		status == DownloadStatus.STATE_QUERYNEWVERSION) {
                	Util.logInfo(TAG, "writeFile, DownloadStatus = "+status);
                	cookies = null;
                	finish = false;
                	break;
                }
            	if (mHandler == null) {
            		if (rightnow) {
            			i = 200;
            			rightnow = false;
            		}
            		if (i == 200) {
            			onDownloadProcessUpdate();
            			i = 0;
            		}
            	} else {
            		if (!rightnow) {
            			i = 18;
            			rightnow = true;
            		}
            		if(i == 20) {
            		    i=0;
                	    onDownloadProcessUpdate();
                	}
                }
            	j++;
            	if (j == 20) {
            		onTransferRatio();
            		j = 0;
            	}
            	finish = true;
            }
            Util.logInfo(TAG, "writeFile, finish, rc = "+rc+"bytes"+". finish = "+finish);
            if (finish) {
            	onTransferRatio();
            	onDownloadProcessUpdate();
            }
            out.close();
            onShutdownConn();
            return 0;
        } catch (SocketTimeoutException e) {
            Util.logError("HttpManager:writeFile, timeout");
            e.printStackTrace();
	} catch (Exception e) {
            e.printStackTrace();
	}
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_NETWORKERROR, 0, 0));
        }
        if (mDownloadStatus.getDLSessionStatus() == DownloadStatus.STATE_DOWNLOADING) {
            mDownloadStatus.setDLSessionStatus(DownloadStatus.STATE_PAUSEDOWNLOAD);
            Util.logError("HttpManager:writeFile, exception to set pause state");
        }
        onShutdownConn();
        Util.logError("HttpManager:writeFile exception exit");
	return mErrorCode = HTTP_RESPONSE_CONN_TIMEOUT;
    }

    private void onTransferRatio() {
    	if (mHandler != null) {
    		//return;
    	}
    	long totalSize = mDownloadStatus.getUpdateImageSize();
        long currSize = Util.getFileSize(Util.getPackageFileName(mContext));
        if (currSize < 0) {
            currSize = 0;
        }
        if (totalSize == 0) {
            totalSize = -1;
        }
        if (totalSize < 0) {
            return;
        }
        int ratio = (int)(((double)currSize / (double)totalSize) * 100);
        if (ratio > 100) {
            ratio = 100;
            currSize = totalSize;
        }

    	mDownloadStatus.setDownLoadPercent(ratio);
    }
    public void onDownloadProcessUpdate() {
        Util.logInfo(TAG, "onDownloadProcessUpdate, mHandler = "+mHandler + ", mNotification = "+mNotification);
        mNewVersionInfo = mDownloadStatus.getDownloadDescriptor();
		if(mHandler == null) {
            mNotification.showDownloadingNotificaton(mNewVersionInfo.version, (int)(((double)Util.getFileSize(Util.getPackageFileName(mContext))/(double)mNewVersionInfo.size)*100));
            Util.logInfo(TAG, "onDownloadProcessUpdate, notification progress update"); 
        } else {
        	mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_DLPKGUPGRADE));
            Util.logInfo(TAG, "onDownloadProcessUpdate, send message to update UI progress");
        }
    }

    private class HttpResponseContent {
        public int rand = -1;
        public String sessionId = null;
        public String versionName = null;
        public long    fileSize = -1;
        public String releaseNote = null;
        public int deltaId = -1;
    }

    private boolean isNetworkAvailable(Context context, String typeName) {
        boolean isAvailable;
        if (typeName != null && typeName.length() > 0)
        {
            isAvailable = Util.isSpecifiedNetWorkAvailable(context, typeName);
            if (!isAvailable) {
                mErrorCode = HTTP_RESPONSE_NO_SPEC_NETWORK;
                return false;
            }
        } else {
            isAvailable = Util.isNetWorkAvailable(context);
            if (!isAvailable) {
                mErrorCode = HTTP_RESPONSE_NETWORK_ERROR;
                return false;
            }
        }
        return true;
    }
    
    private void sendErrorMessage() {
        if(mHandler != null) {
        	Util.logInfo(TAG, "sendErrorMessage, mErrorCode = "+mErrorCode);
        	if (mErrorCode == HTTP_RESPONSE_NO_SPEC_NETWORK)
        	{
        	    mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_NETWORKERROR, 1, 1));
        	} else if (mErrorCode == HTTP_RESPONSE_NO_NEW_VERSION || mErrorCode == HTTP_RESPONSE_SN_REQUIRE) {
        	    mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_NONEWVERSIONDETECTED));
            } else if (mErrorCode == HTTP_RESPONSE_VERSION_ILLEGAL) {
            	mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_NOTSUPPORT));
            } else if (mErrorCode == HTTP_RESPONSE_VERSION_DELETE) {
            	mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_NOTSUPPORT_TEMP));
            } else if (mErrorCode == HTTP_RESPONSE_VERSION_REQUIRE) {
            	mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_NOVERSIONINFO));
            } else if (mErrorCode == HTTP_RESPONSE_DELTA_DELETE) {
            	mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_DELTADELETED));
            } else if (mErrorCode == HTTP_DETECTED_SDCARD_ERROR) {
                mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_SDCARDUNKNOWNERROR));
            } else if (mErrorCode == HTTP_DETECTED_SDCARD_INSUFFICENT) {
                mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_SDCARDINSUFFICENT));
            } else if (mErrorCode == HTTP_DETECTED_SDCARD_CRASH_OR_UNMOUNT) {
                mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_SDCARDCRASHORUNMOUNT));
            } else if (mErrorCode == HTTP_RESPONSE_NETWORK_ERROR || mErrorCode == HTTP_RESPONSE_CONN_TIMEOUT) {
           		mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_NETWORKERROR, 0, 0));
            } else if (mErrorCode == HTTP_RESPONSE_UNZIP_ERROR) {
           		mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_UNZIP_ERROR));
            } else if (mErrorCode == HTTP_RESPONSE_UNZIP_CKSUM) {
           		mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_CKSUM_ERROR));
            } else {
            	mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_UNKNOWERROR));
            }
        }
    }

    private void sendCheckOTAMessage() {
        if(mHandler != null) {
            Util.logInfo(TAG, "sendCheckOTAMessage, mOTAresult = "+mOTAresult);
            switch (mOTAresult) {
                case Util.OTAresult.CHECK_OK:
                    mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_OTA_CLOSECLIENTUI));
                    break;
                case Util.OTAresult.ERROR_OTA_FILE:
                case Util.OTAresult.ERROR_FILE_OPEN:
                case Util.OTAresult.ERROR_FILE_WRITE:
                case Util.OTAresult.ERROR_OUT_OF_MEMORY:
                case Util.OTAresult.ERROR_DIFFERENTIAL_VERSION:
                case Util.OTAresult.ERROR_PARTITION_SETTING:
                case Util.OTAresult.ERROR_MATCH_DEVICE:
                    //send message to prompt user delta error and delete delta
                    mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_OTA_PACKAGEERROR));
                    deletePackage();
                    break;
                case Util.OTAresult.ERROR_RUN:
                case Util.OTAresult.ERROR_INVALID_ARGS:
                    mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_OTA_RUNCHECKERROR));
                    deletePackage();
                    //send message to delete delta, this is for debug
                    break;
                case Util.OTAresult.ERROR_ONLY_FULL_CHANGE_SIZE:
                    mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_OTA_NEEDFULLPACKAGE));
                    deletePackage();
                    //send message to prompt user delta is proper and delete delta
                    break;
                case Util.OTAresult.ERROR_ACCESS_SD:
                case Util.OTAresult.ERROR_SD_WRITE_PROTECTED:
                    mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_OTA_SDCARDERROR));
                    //send message to prompt user sdcard error and need check
                    break;
                case Util.OTAresult.ERROR_ACCESS_USERDATA:
                    mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_OTA_USERDATAERROR));
                    //send message to prompt user user data partition error and delete image
                    break;
                case Util.OTAresult.ERROR_SD_FREE_SPACE:
                    mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_OTA_SDCARDINFUFFICENT));
                    //send message to prompt user sdcard insufficent and need to delete some file form sdcard
                    break;
                case Util.OTAresult.ERROR_USERDATA_FREE_SPACE:
                    mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_OTA_USERDATAINSUFFICENT));
                    //send message to prompt user user data insufficent and need to delete some file form sdcard
                    break;
                default:
                    mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_OTA_PACKAGEERROR));
                    deletePackage();
                    //send message to prompt unknown error and delete delta.
                    break;
            }
        }
    }

    private void deletePackage() {
    	Util.logInfo(TAG, "deletePackage");
        File imgf = new File(Util.getPackageFileName(mContext));
        if (imgf == null) {
             Util.logInfo(TAG, "deletePackage, new file error");
             return;
        }
        if (imgf.exists()) {
            Util.logInfo(TAG, "deletePackage, delte package");
            Util.deleteFile(Util.getPackageFileName(mContext));
        }
    }

    private boolean sdcardCheck() {
    	Util.logInfo(TAG, "sdcardCheck");
		DownloadDescriptor dd = mDownloadStatus.getDownloadDescriptor();
		if (!Util.isSdcardAvailable(mContext)) {
			mErrorCode = HTTP_DETECTED_SDCARD_ERROR;
			return false;
		}
		long csize = Util.getFileSize(Util.getPackageFileName(mContext));
		long needsize = (long)(dd.size * 2.5);
		if (csize > 0) {
			needsize = needsize - csize;
		}
		switch (Util.checkSdcardIsAvailable(mContext,needsize)) {
		case Util.SDCARD_STATE_INSUFFICIENT:
			mErrorCode = HTTP_DETECTED_SDCARD_INSUFFICENT;
			return false;
		case Util.SDCARD_STATE_LOST:
		case Util.SDCARD_STATE_UNMOUNT:
			mErrorCode = HTTP_DETECTED_SDCARD_ERROR;
			return false;
	    }
		return true;
	}
	
    public void onDownloadImage() {
        Util.logInfo(TAG, "onDownloadImage");
        
        if(mIsDownloading) {
        	return ;
        }
        mIsDownloading = true;
        boolean isunzip = mDownloadStatus.getDLSessionUnzipState();
        boolean isren = mDownloadStatus.getDLSessionRenameState();
        if (isren && isunzip) {
            GoogleOtaUnzipChecksum.deleteCrashFile(Util.getPackagePathName(mContext));
            onDownloadPackageUnzipAndCheck();
            mIsDownloading = false;
            return;
        }
        boolean isNetworkAvailable = isNetworkAvailable(mContext, NETTYPENAME);
        if (!isNetworkAvailable) {
            sendErrorMessage();
            mIsDownloading = false;
            return;
        }

        if (!sdcardCheck()) {
            sendErrorMessage();
            mIsDownloading = false;
            return;
        }
        mDownloadStatus.setDLSessionStatus(DownloadStatus.STATE_DOWNLOADING);
        String url = mContext.getResources().getString(R.string.address_download);;
        String tokenCode = getToken();
        if (tokenCode == null) {
            mErrorCode = HTTP_RESPONSE_AUTHEN_ERROR;
            sendErrorMessage();
            mIsDownloading = false;
            return;
        }
        mNewVersionInfo = mDownloadStatus.getDownloadDescriptor();
        BasicNameValuePair token = new BasicNameValuePair("token", tokenCode);
        BasicNameValuePair deltaId = new BasicNameValuePair("deltaId", String.valueOf(mNewVersionInfo.deltaId));
        Util.logInfo(TAG, "onDownloadImage, deltaId = "+ String.valueOf(mNewVersionInfo.deltaId));
        ArrayList<BasicNameValuePair> bnvpa = new ArrayList<BasicNameValuePair>();
        bnvpa.add(token);
        bnvpa.add(deltaId);
        long currentSize = Util.getFileSize(Util.getPackageFileName(mContext));
        currentSize -= Util.FAULT_TOLERANT_BUFFER;
        if (currentSize < 0) {
            currentSize = 0;
        }
        BasicNameValuePair sizePar = new BasicNameValuePair("HTTP_RANGE", String.valueOf(currentSize));
        bnvpa.add(sizePar);
        HttpResponse response = doPost(url, null, bnvpa);
        if (response == null) {
        	Util.logInfo(TAG, "onDownloadImage, net request fail response = null");
        	sendErrorMessage();
            mIsDownloading = false;
        	return;
        }
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() != HttpStatus.SC_OK &&
        		status.getStatusCode() != HttpStatus.SC_PARTIAL_CONTENT) {
            Util.logInfo(TAG, "onDownloadImage, ReasonPhrase = "+status.getReasonPhrase() + ", status.getStatusCode() = "+status.getStatusCode());
            cookies = null;
            if (status.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            	resetDescriptionInfo();
            	mErrorCode = HTTP_RESPONSE_DELTA_DELETE;
            } else {
                mErrorCode = HTTP_RESPONSE_AUTHEN_ERROR;
            }
            sendErrorMessage();
            mIsDownloading = false;
            return;
        }
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        int ret = writeFile(response, currentSize);
        Util.logInfo(TAG, "onDownloadImage, download result = "+ret + ", mHttpClient = "+mHttpClient);
        if ((ret == 0) && (mDownloadStatus.getDLSessionStatus() == DownloadStatus.STATE_PAUSEDOWNLOAD ||
        		mDownloadStatus.getDLSessionStatus() == DownloadStatus.STATE_QUERYNEWVERSION)) {
            mIsDownloading = false;
            return;
        }
        if (ret == HTTP_DETECTED_SDCARD_CRASH_OR_UNMOUNT) {
        	resetDescriptionInfo();
        	sendErrorMessage();
            mIsDownloading = false;
            return;
        }
        if (ret == HTTP_RESPONSE_CONN_TIMEOUT) {
            mIsDownloading = false;
            return;
        }
        onDownloadPackageUnzipAndCheck();
        mIsDownloading = false;
    }
	
    private void onShutdownConn() {
    	Util.logInfo(TAG, "onShutdownConn");
        if (mHttpClient != null) {
        	mHttpClient.getConnectionManager().shutdown();
        }
    	mHttpClient = null;
    	cookies = null;
    }

    private void onDownloadPackageUnzipAndCheck() {
    	Util.logInfo(TAG, "onDownloadPackageUnzipAndCheck");
    	onPackageUnzipping();
    	onDownloadPause();
    	mDownloadStatus.setDLSessionUnzipState(true);
    	if (!mDownloadStatus.getDLSessionRenameState()) {
    		File pkg = new File(Util.getPackageFileName(mContext));
    	    boolean rename = pkg.renameTo(new File(Util.getPackagePathName(mContext)+"/package.zip"));
    	    if (!rename) {
    	         mErrorCode = HTTP_RESPONSE_UNZIP_ERROR;
    	         sendErrorMessage();
    	         return;
    	    }
    	    mDownloadStatus.setDLSessionRenameState(true);
    	}
    	int sdstat = GoogleOtaUnzipChecksum.checkUnzipSpace(mContext,Util.getPackagePathName(mContext)+"/package.zip");
    	switch (sdstat) {
		case Util.SDCARD_STATE_INSUFFICIENT:
			mErrorCode = HTTP_DETECTED_SDCARD_INSUFFICENT;
			sendErrorMessage();
			return;
		case Util.SDCARD_STATE_LOST:
		case Util.SDCARD_STATE_UNMOUNT:
			mErrorCode = HTTP_DETECTED_SDCARD_ERROR;
			sendErrorMessage();
			return;
	    }
        int result = GoogleOtaUnzipChecksum.unzipDelta(Util.getPackagePathName(mContext)+"/package.zip");
        if (result == GoogleOtaUnzipChecksum.UNZIP_SUCCESS) {
             onDownloadComplete();
             mDownloadStatus.setDLSessionUnzipState(false);
             mDownloadStatus.setDLSessionRenameState(false);
             GoogleOtaUnzipChecksum.deleteUnuseFile(Util.getPackagePathName(mContext));
             return;
        } else if (result == GoogleOtaUnzipChecksum.CKSUM_ERROR) {
             mErrorCode = HTTP_RESPONSE_UNZIP_CKSUM;
             sendErrorMessage();
    	     return;
        } else {
             mErrorCode = HTTP_RESPONSE_UNZIP_ERROR;
    	     sendErrorMessage();
    	     return;
        }
    }

    public void onPackageUnzipping() {
        Util.logInfo(TAG, "onPackageUnzipping");
		if(mHandler != null) {
        	mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_UNZIP_LODING));
            Util.logInfo(TAG, "onPackageUnzipping, Send  upzip message to activity.");
        }
    }

    public void onDownloadComplete() {
        Util.logInfo(TAG, "onDownloadComplete");
        mNewVersionInfo = mDownloadStatus.getDownloadDescriptor();
		mDownloadStatus.setDLSessionStatus(DownloadStatus.STATE_DLPKGCOMPLETE);
		if(mHandler == null) {
			mNotification.clearNotification(NotifyManager.NOTIFY_DOWNLOADING);
            mNotification.showDownloadCompletedNotification(mNewVersionInfo.version);
            Util.logInfo(TAG, "onDownloadComplete, Show download complete notification."); 
        } else {
        	mHandler.sendMessage(mHandler.obtainMessage(DownloadStatus.MSG_DLPKGCOMPLETE));
            Util.logInfo(TAG, "onDownloadComplete, Send  download complete message to activity.");
        }

        mNewVersionInfo.deltaId = -1;
        mNewVersionInfo.newNote = null;
        mNewVersionInfo.size = -1;
        //mNewVersionInfo.version = null;
        mDownloadStatus.setDownloadDesctiptor(mNewVersionInfo);
        mDownloadStatus.setDownLoadPercent(100);
    }

    public void onSendCloseClientMessage() {
        Util.logInfo(TAG, "onSendCloseClientMessage");
        mOTAresult = Util.OTAresult.CHECK_OK;
        sendCheckOTAMessage();
    }

    public boolean onSetRebootRecoveryFlag() {
        Util.logInfo(TAG, "onSetRebootRecoveryFlag");

        try {
            IBinder binder = ServiceManager.getService("GoogleOtaBinder");
            GoogleOtaBinder agent = GoogleOtaBinder.Stub.asInterface (binder); 
            if (agent == null) {
                Util.logInfo(TAG, "engine error to set flag to reboot into recovery mode.");
                return false;
            }
            
            if (FeatureOption.MTK_EMMC_SUPPORT) {
                if (!agent.clearUpdateResult()) {
                    Util.logInfo(TAG, "clearUpdateResult() false"); 
                    return false;
                }
            } 

            if (!agent.setRebootFlag()) {
                Util.logInfo(TAG, "setRebootFlag() false");
                return false;
            }

            mDownloadStatus.setUpgradeStartedState(true);

            Intent intent = new Intent("android.intent.action.GoogleOta.WriteCommandService");
            intent.putExtra(Util.COMMAND_PART2, Util.OTA_PATH_IN_RECOVERY);
            mContext.startService(intent);
            return true;
        } catch (Exception e) {
            Util.logInfo(TAG, "Exception accur when set flag to reboot into recovery mode.");
            e.printStackTrace ();
            return false;
        }
    }

    public boolean isDeltaPackageOk() {
        mOTAresult = onCheckDeltaPackage();
        Util.logInfo(TAG, "onCheckDeltaPackage, run check_ota mOTAresult = " + mOTAresult);
        if (mOTAresult == Util.OTAresult.CHECK_OK) {
            return true;
        } else {
        	sendCheckOTAMessage();
            return false;
        }
    }
    
    private int onCheckDeltaPackage() {
        Util.logInfo(TAG, "onCheckDeltaPackage");
        Process process = null;
        try {
             File imgf = new File(Util.getPackageFileName(mContext));
            if (imgf == null || !imgf.exists()) {
                Util.logInfo(TAG, "onCheckDeltaPackage, file not exist");
                return Util.OTAresult.ERROR_RUN;
            }
            process = new ProcessBuilder().command("/system/bin/check_ota", Util.getPackageFileName(mContext)).start();
            Util.logInfo(TAG, "xxxxxxxxxxxxxxxxxxxxxxxxxx, process.ud="+android.os.Process.myUid());

            if (process == null) {
            	return Util.OTAresult.ERROR_RUN;
            }
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            if (error == null) {
            	return Util.OTAresult.ERROR_RUN;
            }
            int num = 0;
            num = error.read();
            if (num != -1) {
            	return Util.OTAresult.ERROR_RUN;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (reader == null) {
            	return Util.OTAresult.ERROR_RUN;
            }
            
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            if (output == null) {
            	return Util.OTAresult.ERROR_RUN;
            }
            try {
                while ((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
            } catch (Exception e) {
        	    Util.logInfo(TAG, "onCheckDeltaPackage, read result error");
                e.printStackTrace();
                return Util.OTAresult.ERROR_RUN;
            }
            finally {
            	if (reader != null) {
            		reader.close();
            	}
            }
            process.waitFor();
            int result = Integer.parseInt(output.toString());
            Util.logInfo(TAG, "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx result = "+result);
            //result = 0;//9;//12;//11;//10;//7;
            return result;
        } catch (Exception e) {
        	Util.logInfo(TAG, "onCheckDeltaPackage, run check_ota exception");
            e.printStackTrace();
            return Util.OTAresult.ERROR_RUN;
        }
        finally {
        	if (process != null) {
                process.destroy();
            }
        }
    }

    public void onDownloadPause() {
        Util.logInfo(TAG, "onDownloadPause");
        mDownloadStatus.setDLSessionStatus(DownloadStatus.STATE_PAUSEDOWNLOAD);
    }

    
    /**
     * ******************system operator service start******************
     **/

    /**
     * These constant flag must be the same as that defined in SysOperService.java,
     * please follow them.
     **/
    public static final int MSG_NONE               = -1;
    public static final int MSG_REBOOT_TARGET      = 1;
    public static final int MSG_CREATE_COMMANDFILE = 2;
    public static final int MSG_DELETE_COMMANDFILE = 3;
    public static final String COMMANDFILEKEY = "COMMANDFILE";
    
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    Messenger mService = null;
    boolean mIsBound = false;
    private int mNeedServiceDo = MSG_NONE;
    
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	Util.logInfo(TAG, "handleMessage, msg.what="+msg.what);
            switch (msg.what) {
                case MSG_CREATE_COMMANDFILE:
                    Util.logInfo(TAG, "handleMessage msg.arg1=" + msg.arg1);
                    break;
                case MSG_DELETE_COMMANDFILE:
                    Util.logInfo(TAG, "handleMessage msg.arg1=" + msg.arg1);
                    break;
                default:
                super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Util.logInfo(TAG, "onServiceConnected, mNeedServiceDo="+mNeedServiceDo);
            mService = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };
    
    private void doActionUseService() {
	    try {
            switch(mNeedServiceDo) {
            case MSG_REBOOT_TARGET:
                    break;
            case MSG_CREATE_COMMANDFILE:
                    break;
                case MSG_DELETE_COMMANDFILE:
                    Message msg = Message.obtain(null, MSG_DELETE_COMMANDFILE);
                    msg.replyTo = mMessenger;
                    Bundle data = new Bundle();
                    if (data == null) {
                    	return;
                    }
                    data.putString(COMMANDFILEKEY, Util.COMMAND_FILE);
                    msg.setData(data);
                    if (mService != null) {
                        mService.send(msg);
                    }
                    break;
                default:
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            Util.logInfo(TAG, "onServiceConnected send message error");
        }
    }
    private boolean doBindService(Context context) {
        Util.logInfo(TAG, "doBindService");
        boolean isbind = false;
        if (context != null) {
            isbind = context.bindService(new Intent("android.intent.action.GoogleOta.SysOperService"), mConnection, Context.BIND_AUTO_CREATE);
        }
        Util.logInfo(TAG, "dobindService, isbind="+isbind);
        mIsBound = true;
        return isbind;
    }
   
    private void doUnbindService(Context context) {
    	Util.logInfo(TAG, "doUnbindService");
        if (mIsBound) {
            mService = null;
            if (context != null) {
                context.unbindService(mConnection);
            }
            mIsBound = false;
        }
    }
    /**
     * ******************system operator service stop******************
     **/
}
