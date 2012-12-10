/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.text.style;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.SystemProperties;
import android.provider.Browser;
import android.text.ParcelableSpan;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class URLSpan extends ClickableSpan implements ParcelableSpan {

    private final String mURL;
    private static final boolean IS_CMCC = SystemProperties.get("ro.operator.optr").equals("OP01");

    public URLSpan(String url) {
        mURL = url;
    }

    public URLSpan(Parcel src) {
        mURL = src.readString();
    }
    
    public int getSpanTypeId() {
        return TextUtils.URL_SPAN;
    }
    
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mURL);
    }

    public String getURL() {
        return mURL;
    }

    @Override
    public void onClick(View widget) {
        Uri uri = Uri.parse(getURL());
        final Context context = widget.getContext();
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        try {
	        if(IS_CMCC && (widget instanceof TextView)) {
	        	TextView tv = (TextView)widget;
	        	boolean isWebURL = false;
	        	String scheme = uri.getScheme();
	        	if (scheme != null) {
	        		isWebURL = scheme.equalsIgnoreCase("http")  
	        		         || scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("rtsp");
	        	}
	        	
	        	if(tv.isEnableShowUrlDialog() && isWebURL) {
	       		    AlertDialog.Builder b = new AlertDialog.Builder(context);
	 	            b.setTitle(com.mediatek.internal.R.string.url_dialog_choice_title);
	 	            b.setMessage(com.mediatek.internal.R.string.url_dialog_choice_message);
	 	            b.setCancelable(true);
	 	            
	 	            b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	 	                public final void onClick(DialogInterface dialog, int which) {
	 	                    context.startActivity(intent);
	 	                    dialog.dismiss();
	 	                }
	 	            });
	 	            
	 	            b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	 	                public final void onClick(DialogInterface dialog, int which) {
	 	                    dialog.dismiss();
	 	                }
	 	            });
	 	            
	 	            b.show();
	        	} else {
	        		context.startActivity(intent);
	        	}
	        } else {
	        	context.startActivity(intent);
	        }
        } catch (ActivityNotFoundException e) {
            Intent mChooserIntent = Intent.createChooser(intent, null);
            context.startActivity(mChooserIntent);
        }
    }
}
