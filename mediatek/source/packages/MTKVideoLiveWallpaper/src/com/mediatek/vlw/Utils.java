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

package com.mediatek.vlw;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public final class Utils {
	// device limitation about video coder/decoder
	static final int MAX_WIDTH_RES = 864;
	static final int MAX_HEIGHT_RES = 480;
	static final Random mRandom = new Random();
	
	private Utils() {
		// need not to instantiate this class object
	}
	
	public static void showInfo(Context context, int id, boolean silent) {
		if (silent) {
			Toast.makeText(context, id, Toast.LENGTH_LONG).show();
		} else if (context instanceof Activity) {
			new AlertDialog.Builder(context)
            .setTitle(R.string.VideoEditor_error_title)
            .setMessage(id)
            .setPositiveButton(R.string.VideoEditor_error_button,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            /* If we get here, there is no onError listener, so
                             * at least inform them that the video is over.
                             */
                            //TODO
                        }
                    })
            .setCancelable(false)
            .show();
		}
	}
	
    public static boolean isDefaultVideo(Uri uri) {
		String scheme = uri.getScheme();
		if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
			return true;
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)
                || ContentResolver.SCHEME_FILE.equals(scheme)) {
			return false;
		}
		
		return false;
	}
    
	public static ArrayList<Uri> getUrisFromBucketId( Context context, String bucketId) {
		ArrayList<Uri> uris = new ArrayList<Uri>();
		Cursor cursor = null;
		try {
			String where = MediaStore.Video.VideoColumns.BUCKET_ID + "=" + bucketId;
			ContentResolver cr = context.getContentResolver();
			String[] projection = { MediaStore.Video.Media.DATA };
			cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
					projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					String uriString = cursor.getString(0);
					uris.add( Uri.fromFile(new File(uriString)) );
				} while (cursor.moveToNext());
			}
			
		} catch (Exception e) {
			// ignore
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		Collections.sort(uris, new Comparator<Uri>() {
            final Collator mCollator;

            {
                mCollator = Collator.getInstance();
            }

            public int compare(Uri uri1, Uri uri2) {
                return mCollator.compare(uri1.getPath(), uri2.getPath());
            }
        });
		
		return uris;
	}
	
	public static String queryFolderInfo(Context context, String bucketId) {
		String info = null;
		Cursor cursor = null;
		try {
			String where = MediaStore.Video.VideoColumns.BUCKET_ID + "=" + bucketId;
			ContentResolver cr = context.getContentResolver();
			String[] projection = {MediaStore.Video.Media.BUCKET_DISPLAY_NAME};
			cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
					projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				info = cursor.getString(0);
			}
			
		} catch (Exception e) {
			// ignore
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		return info;
	}
	
	public static boolean isExternalFileExists(String path) {
		if (path == null) {
			return false;
		}
		File externalFile = new File(path);
		return externalFile.exists();
	}
	
	public static boolean isExternalFileExists(Context context, Uri uri) {
		if (uri == null) {
			return false;
		}
		boolean result = false;
		Cursor cursor = null;
		try {
			// video from SDCARD
			ContentResolver cr = context.getContentResolver();
			String[] proj = { MediaStore.Video.Media.DATA };
			cursor = cr.query(uri, proj, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int ci = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
				String path = cursor.getString(ci);
				File externalFile = new File(path);
				if (externalFile.exists())
					result = true;
			}
		} catch (Exception e) {
			// ignore
		} finally {	
			// actually, the Cursor from managedQury() will take care of
			// its own life
			if (cursor != null) {
				cursor.close();
			}
		}
		
		return result;
	}
	
	/**
	 * Return next index according to loop mode
	 * @param mode
	 * @param curPos
	 * @param len
	 * @return > 0 next index;
	 * 		   -1  len is 0, there is nothing
	 */
    public static int getLoopIndex(LoopMode mode, int curPos, 
    		ArrayList<Uri> uris, ArrayList<Uri> invalid) {
    	final int bound = uris.size() - 1;
    	
    	int position = (curPos < 0 || curPos > bound) ? 0 : curPos;
    	// return -1 if len <= 0 or all videos are invalid
    	if (bound < 0 || (invalid != null && (invalid.size() == uris.size()))) {
    		return -1;
    	}
    	// Now at least one valid video exists
    	switch(mode) {
    	case SINGLE:
    		if (invalid != null && invalid.contains(uris.get(position))) {
    			position = -1;
    		} else {
    			position = curPos;
    		}
    		
    		break;
    	case ALL:
    		position = ++curPos;
    		if (position > bound) 
    			position = 0;
    		while (invalid != null && invalid.contains(uris.get(position)))
    			if (++position > bound)
    				position = 0;

    		break;
    	case RANDOM:
    	default:
    		position = mRandom.nextInt(bound);
    		while (invalid != null && invalid.contains(uris.get(position)))
    			position = mRandom.nextInt(bound);
    		
    		break;
    	}
    	
    	return position;
    }
    
    public static float queryResolutionRatio(Context context) {
    	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    	Display disp = wm.getDefaultDisplay();
    	final int width = disp.getWidth() * 2;
    	final int height = disp.getHeight();
    	
    	return (float)width / height;
    }
    
    /**
     * Check if this video resides in resources, if not return it's storage path
     * @param uri
     * @return null default video from resources
     * 		   not null, external storage path
     */
    public static String getVideoPath(Context context, Uri uri) {
		String path = null;
		String scheme = uri.getScheme();
		if (ContentResolver.SCHEME_CONTENT.equals(scheme)
                || ContentResolver.SCHEME_FILE.equals(scheme)) {
			Cursor cursor = null;
			try {
				// video from SDCARD
				ContentResolver cr = context.getContentResolver();
				String[] proj = { MediaStore.Video.Media.DATA };
				cursor = cr.query(uri, proj, null, null, null);
				if (cursor != null && cursor.moveToFirst()) {
					int ci = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
					path = cursor.getString(ci);
				}
			} catch (Exception e) {
				// ignore
			} finally {	
				// actually, the Cursor from managedQury() will take care of
				// its own life
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		
		return path;
	}
    
    public static Uri swapSdcardUri(Uri uri) {
        Uri swapUri = null;
        String path = uri.getPath();
        if (path.contains("/sdcard/")) {
            String swapPath = path.replace("/sdcard/", "/sdcard2/");
            swapUri = Uri.fromFile(new File(swapPath));
        } else if (path.contains("/sdcard2/")) {
            String swapPath = path.replace("/sdcard2/", "/sdcard/");
            swapUri = Uri.fromFile(new File(swapPath));
        }
        return swapUri;
    }
    
    public enum LoopMode {
    	// loop mode 
    	RANDOM			(0),
    	SINGLE			(1),
    	ALL				(2);
    	LoopMode(int mode) {
			// TODO Auto-generated constructor stub
    		this.mMode = mode;
		}
    	final int mMode;
    	
    	public int getValue() {
    		return mMode;
    	}
    }
}
