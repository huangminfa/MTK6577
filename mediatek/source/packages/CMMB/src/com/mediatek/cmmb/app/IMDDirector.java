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

package com.mediatek.cmmb.app;

import com.mediatek.cmmb.app.ChannelListManager.Channel;
import com.mediatek.mbbms.IMD;
import com.mediatek.mbbms.IMD.MediaObjectGroup;
import com.mediatek.mbbms.IMDManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Downloads;
import android.util.Log;
import android.net.Uri;
import android.net.WebAddress;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.DownloadListener; 
import android.widget.Toast;

import java.util.ArrayList;

public class IMDDirector implements IMDManager.IMDListener {
	private static final String TAG = "CMMB::IMDDirector";	
	private static final String CMCC_USER_AGENT = "CMCC_MTVBrowser/1.0 ";
		
    private static final int MSG_NEW_IMD = 1;	
    private static final int MSG_INPUT_TIME_EXPIRED = 2;	
    private static final int MSG_VILIDATION_PERIOD_EXPIRED = 3;	
	
	private IMD mCurrentIMD;
	private MediaObjectGroup mCurrentMOG;
	private boolean mInputTimeExpired;
	private Context mContext;
	private IMDActor mIMDActor;
	private WebView mWebView;
	private IMDManager mIMDStorageManager;

	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "handleMessage what = "+msg.what);
		
			switch (msg.what) {
				case MSG_NEW_IMD: {
					handleNewIMD((IMD)msg.obj);
					break;
				}

				case MSG_INPUT_TIME_EXPIRED: {
					handleInputTimeExpiry();
					break;
				}
				
				case MSG_VILIDATION_PERIOD_EXPIRED: {
					Log.d(TAG,"validation period expires of CDI with validTo = "+mCurrentIMD.validTo);					
					//Not clear what to do now.
			        //closeIMD();
			        mIMDActor.onValidationExpired();   
			    }
				default:
					break;
			}
		}
	};
	
	public IMDDirector(Context c,IMDActor a,WebView wv) {
		mContext = c;
		if (a == null) {
			mIMDActor = new IMDActorImpl();
		} else {
			mIMDActor = a;
		}
		
		if (wv != null) {
			mWebView = wv;
			initWebView();			
		}
		mIMDStorageManager = IMDManager.getInstance(c.getContentResolver());		
		mIMDStorageManager.addListener(this);		
	}
	
	//implements IMDListener.
	public void onNew(IMD imd) {
		Log.d(TAG, "onNew");
		mHandler.sendMessage(mHandler.obtainMessage(MSG_NEW_IMD,0,0,imd));
	}	

	public void handleInputTimeExpiry() {	
		Log.d(TAG, "handleInputTimeExpiry");
		mInputTimeExpired = true;
		IMD max_imd = mIMDStorageManager.getMaxPosition(mCurrentIMD.groupID);
		if (max_imd != null && max_imd.groupPosition != mCurrentIMD.groupPosition) {
			Log.d(TAG, "inputAllowedTime expired and a new IMD with bigger group position found."); 			
			//a new IMD with bigger group position exists,show it.
			mIMDActor.onNewIMD(max_imd,true);
		} else if (mCurrentMOG.onTimeoutMog != null) {
			showMOG(mCurrentMOG.onTimeoutMog);
		} else {
			//TBD:not clear what to do in this case.
		}	
	}		
		
	
	public void setWebView(WebView wv) {
		Log.d(TAG, "setWebView mWebView = "+mWebView);
		if (mWebView != wv) {
			closeIMD();
			mWebView = wv;
			initWebView();			
		}
	}
	
	public void close() {
		mIMDStorageManager.removeListener(this);
	}
	
	public void showIMD(IMD imd) {
		if (mCurrentIMD != null) {
			closeIMD();
		}
		Log.d(TAG, "showIMD canGoBack = "+mWebView.canGoBack());		
		mCurrentIMD = imd;
		//check validTo.
		if (mCurrentIMD.validTo != IMD.UNKNOWN_VALUE) {
			long delay = (long)(mCurrentIMD.validTo) * 1000L - System.currentTimeMillis();
			if (delay > 0) {
				Log.d(TAG, "showIMD start timer for validTo = "+mCurrentIMD.validTo
						+" delay = "+delay);		
				
				mHandler.sendEmptyMessageDelayed(MSG_VILIDATION_PERIOD_EXPIRED,delay);
			} else {
				Log.d(TAG,"validation period has expired aready of CDI with validTo = "+mCurrentIMD.validTo);	
				//TBD:not clear what to do
			}
		}		
		showMOG(mCurrentIMD.startGroup);
		if (!imd.hasRead) {
			mIMDStorageManager.readInteractivity(imd);	
			mIMDActor.onFirstTimeRead();
		}
	}
	
	public void closeIMD() {
		Log.d(TAG, "closeIMD mCurrentIMD = "+mCurrentIMD);		

		if (mCurrentIMD != null) {	
			closeMOG();
			//do not allow to go back any more.Anyway,should we support back action?
			mWebView.clearHistory();
			Log.d(TAG, "closeIMD canGoBack = "+mWebView.canGoBack());		
			mHandler.removeMessages(MSG_VILIDATION_PERIOD_EXPIRED);
			mCurrentIMD = null;
		}
	}

	private void closeMOG() {
		Log.d(TAG, "closeMOG mCurrentMOG = "+mCurrentMOG);
		if (mCurrentMOG != null) {
			mCurrentMOG = null;
			mInputTimeExpired = false;
			mHandler.removeMessages(MSG_INPUT_TIME_EXPIRED);
			//stop previous loading
			mWebView.stopLoading();		
		}
	}

	private void handleNewIMD(IMD imd) {
		Log.d(TAG, "handleNewIMD");
		boolean show = false;
		if (mCurrentIMD == null) {
			show = true;	
		} else if (mCurrentIMD.groupID.equals(imd.groupID)) {
			if (imd.groupPosition == mCurrentIMD.groupPosition) {
				Log.w(TAG, "same IMD received groupID = "+imd.groupID
					+" groupPosition = "+imd.groupPosition);
				return;
			} else {
				Log.d(TAG, "groupPosition do not matched with current");
				if (imd.groupPosition > mCurrentIMD.groupPosition) {
					IMD max_imd = mIMDStorageManager.getMaxPosition(imd.groupID);
					if (imd.groupPosition == max_imd.groupPosition) {
						Log.d(TAG, "groupPosition matched with the maximum");					
						//it is the maximum IMD we have now.
						if (mInputTimeExpired) {
							Log.d(TAG, "input time expired already");					
							//no input time restraint or input time already expired
							show = true;			
						} else {
							Log.d(TAG, "input time not expired");					
							//wait until input time expired.
							return;
						}
					} else {//a new IMD is received but it is not the maximum we have now.
						Log.d(TAG, "groupPosition do not matched with the maximum");					
					}
				} else {
					Log.d(TAG, "groupPosition is smaller than current");					
				}
			}
		} 
		
		Log.d(TAG, "handleNewIMD show = "+show);
		
		mIMDActor.onNewIMD(imd,show);
	}	

	public IMD getCurrentIMD() {
		return mCurrentIMD;
	}
	
	private String getHandsetVersion() {
		//TBD.get from resource?
		return "MTK_Mobile/1.0";
	}
	
	private void initWebView() {
		//we don't need Zoom function in CMMB.The reason we add Zoom support is just to
		//solve the issue that some web pages can not be dragged to the its bottom 
		//(bounce to above the bottom automatically when dragging). 
		//mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUserAgentString(CMCC_USER_AGENT+getHandsetVersion());
        mWebView.setWebViewClient(new MyWebViewClient());  
        mWebView.setWebChromeClient(new MyWebChromeClient());  
        mWebView.setDownloadListener(new MyDownloadListener()); 
        mWebView.addJavascriptInterface(new MyJavaScriptInterface(), "cmmb");  
	}	
	
	private void showMOG(final MediaObjectGroup mog) {
		Log.d(TAG, "showMOG mog = "+mog); 			
		closeMOG();
		mCurrentMOG = mog;
		mWebView.loadUrl("file://"+mCurrentIMD.rootPath + mog.startPage);
		
		if (mog.inputAllowedTime > 0) {
			Log.d(TAG, "showMOG start timer for inputAllowedTime = "+mog.inputAllowedTime);		
			mHandler.sendEmptyMessageDelayed(MSG_INPUT_TIME_EXPIRED,mog.inputAllowedTime * 1000L);
		} else {
			mInputTimeExpired = true;
		}		
	}
	
    
    private class MyWebViewClient extends WebViewClient {    
    	@Override    
    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.d(TAG, "shouldOverrideUrlLoading url = "+url);		
	    		view.loadUrl(url);        
	    		return true;    
    	}

        @Override
        public void onPageFinished (WebView view, String url) {
        		/* This js implements 2 functions that CMCC requires:
				*   1. delay the submit action with offset time and ramdom time taken into account.
				*   2. detect object tag and replace it with a button that allows switching 
				*       to a channel specified in object tag on clicking.
				*/
                view.loadUrl("javascript:(function() {" +
            		  	"var forms = document.forms;" +
	                    "for (var i = 0; i < forms.length; i++) {" +
	                    "forms[i].onsubmit = (function(index) {" +
	                    "return function() {" +
	                         "setTimeout('document.forms[' + index + '].submit();', cmmb.getTimeout());" +
	                         "return false;" +
	                    "};})(i);}" +   	                    
            		    "var objs = document.getElementsByTagName('object');"+
            		    "for (var i = 0; i < objs.length;i++) {" +       
		                  "if (objs[i].getAttribute('classID') == 'ApplicationProxy') {"+
		                      "for (var j = 0; j < objs[i].childNodes.length; j++) {"+                      
                              "if (objs[i].childNodes[j].name == 'ServiceID') {"+
                              "var value = objs[i].childNodes[j].getAttribute(',value');"+                                
                              "var a = document.createElement('input');"+
                              "a.type = 'button';"+
                              "a.value = (function() {return cmmb.getChannelName(value);})();"+
                              "a.onclick = function() {cmmb.switchChannel(value);};"+
                              "objs[i].parentNode.replaceChild(a, objs[i]);"+
                              "break;}}}}})()");

        }  		
    }
            
    private class MyDownloadListener implements DownloadListener,DownloadStatusReceiver.DownloadCompleteListener {    
    	private ProgressDialog mProgressDialog;
    	public void onDownloadStart (String url, String userAgent, String contentDisposition, 
    			String mimetype, long contentLength)  {
			Log.d(TAG, "onDownloadStart url = "+url	+" mimetype = "+mimetype);			
            String filename = URLUtil.guessFileName(url,contentDisposition, mimetype);

            String status = Environment.getExternalStorageState();
            if (!status.equals(Environment.MEDIA_MOUNTED)) {
                int title;
                String msg;

                if (status.equals(Environment.MEDIA_SHARED)) {
                    msg = mContext.getResources().getString(R.string.download_sdcard_busy_dlg_msg);
                    title = R.string.download_sdcard_busy_dlg_title;
                } else {
                    msg = mContext.getResources().getString(R.string.download_no_sdcard_dlg_msg, filename);
                    title = R.string.download_no_sdcard_dlg_title;
                }

                new AlertDialog.Builder(mContext)
                    .setTitle(title)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
                return;
            }

            WebAddress webAddress;
            try {
                webAddress = new WebAddress(url);
                webAddress.setPath(encodePath(webAddress.getPath()));
            } catch (Exception e) {
                Log.e(TAG, "Exception trying to parse url:" + url);
                return;
            }

            String cookies = CookieManager.getInstance().getCookie(url);

            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_URI, webAddress.toString());
            values.put(Downloads.Impl.COLUMN_COOKIE_DATA, cookies);
            values.put(Downloads.Impl.COLUMN_USER_AGENT, userAgent);
            values.put(Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE,
                    mContext.getPackageName());
            values.put(Downloads.Impl.COLUMN_NOTIFICATION_CLASS,
            		DownloadStatusReceiver.class.getCanonicalName());
            values.put(Downloads.Impl.COLUMN_VISIBILITY,
                    Downloads.Impl.VISIBILITY_HIDDEN);
            values.put(Downloads.Impl.COLUMN_MIME_TYPE, mimetype);
            values.put(Downloads.Impl.COLUMN_FILE_NAME_HINT, filename);
            values.put(Downloads.Impl.COLUMN_DESCRIPTION, webAddress.getHost());
            if (contentLength > 0) {
                values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, contentLength);
            }
            
            values.put(Downloads.Impl.DOWNLOAD_PATH_SELECTED_FROM_FILEMANAGER,
            			getDownloadPath(mimetype));

			DownloadStatusReceiver.setDownloadCompleteListener(this);

            //start download.
            final Uri contentUri = mContext.getContentResolver().insert(Downloads.Impl.CONTENT_URI, values);                    
            if (mProgressDialog == null) {
            	mProgressDialog = new ProgressDialog(mContext);
            	mProgressDialog.setIndeterminate(true);				
            	mProgressDialog.setCancelable(false);
            }
            mProgressDialog.setMessage(mContext.getResources().getString(R.string.downloading));
            mProgressDialog.show();
        }
    	
    	private String getDownloadPath(String mimetype) {
    		String path = Environment.getExternalStorageDirectory().toString();
    		if (mimetype != null) {
	    		int pos = mimetype.indexOf("/");
	    		if (pos >= 0) {
	    			path += "/"+Character.toUpperCase(mimetype.charAt(0)) 
	    				+ mimetype.substring(1, pos);
	    		}
    		}
    		return path;    		
    	}

	    private String encodePath(String path) {
	        char[] chars = path.toCharArray();

	        boolean needed = false;
	        for (char c : chars) {
	            if (c == '[' || c == ']') {
	                needed = true;
	                break;
	            }
	        }
	        if (needed == false) {
	            return path;
	        }

	        StringBuilder sb = new StringBuilder("");
	        for (char c : chars) {
	            if (c == '[' || c == ']') {
	                sb.append('%');
	                sb.append(Integer.toHexString(c));
	            } else {
	                sb.append(c);
	            }
	        }

	        return sb.toString();
	    }		

		public void onDownloadCompleted() {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}	
		}		 	
    }
    
    private final class MyWebChromeClient extends WebChromeClient {     
    	@Override     
    	public boolean onJsAlert(WebView view, String url, String message, JsResult result) {         
    			Log.d(TAG, "onJsAlert message = "+message);         
    			Toast.makeText(view.getContext(),message,1000).show();         
			    result.confirm();         
			    return true;     
		    } 
    }
    
    private final class MyJavaScriptInterface {                        
    	/**           
	    	 * * This is not called on the UI thread. 
	    	 * */          
    	public void switchChannel(final String serviceId) {              
    		mHandler.post(new Runnable() {                  
    			public void run() {                     
    					Log.d(TAG,"switch channel serviceId = "+serviceId);   
    					mIMDActor.switchChannel(serviceId);
    				}              
    		});            
    	} 
		
	    public int getTimeout() {
			int timeout = 0;
			if (mCurrentMOG != null) {
				timeout = mCurrentMOG.offsetTime + mCurrentMOG.randomTime;
			}
			Log.d(TAG,"getTimeout timeout = "+timeout);   
	    	return timeout;
	    }
	    
	    public String getChannelName(String serviceId) {
			Log.d(TAG,"getChannelName serviceId = "+serviceId);   
			ArrayList<Channel> channelList = ChannelListManager.getChannelListManager(mContext).getChannelList();
			if (channelList == null) {
				Log.e(TAG,"getChannelName channelList is null!");	
			} else {
				int size = channelList.size();
				for (int i = 0;i < size;i++) {
					if (channelList.get(i).serviceID.equals(serviceId)) {
						return channelList.get(i).name;
					}
				}	
			}
			Log.w(TAG,"getChannelName no match serviceId is found!");   
	    	return serviceId;
	    }      
    }      
    
	public interface IMDActor {
		public void onValidationExpired(); 
		public void onNewIMD(IMD imd,boolean show); 
		public void switchChannel(String serviceID);		
		public void onFirstTimeRead(); 		
	}	

	private class IMDActorImpl implements IMDActor {
		public void onValidationExpired() {
			Log.d(TAG, "onValidationExpired"); 		
		}
		public void onNewIMD(IMD imd,boolean show) {
			Log.d(TAG, "onNewIMD"); 	
			if (show) {
				showIMD(imd);
			}
		} 
		public void switchChannel(String serviceID) {
			Log.d(TAG, "switchChannel:"+serviceID); 	
		}	
		public void onFirstTimeRead() {
			Log.d(TAG, "onFirstTimeRead"); 	
		}			
	}
}
