/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.data.Conversation;
import com.android.mms.ui.MmsPlayerActivity.OnDataSetChangedListener;
import com.android.mms.util.SmileyParser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.text.ClipboardManager;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import com.mediatek.banyan.widget.MTKImageView;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;
import android.os.SystemProperties;

/**
 * The back-end data adapter for MmsPlayerActivity.
 */
public class MmsPlayerActivityAdapter extends BaseAdapter {
    private static final String TAG = "Mms/MmsPlayerAdapter";
    private static final boolean LOCAL_LOGV = false;

    private final LayoutInflater mFactory;
    private ArrayList<MmsPlayerActivityItemData> mListItem;
    private int mAllCount;
    private Context mContext;
    private float textSize = 18;
    private DisplayMetrics mDisplayMetrics;
    private HashMap<Integer, View> mListItemViewCache = new HashMap<Integer, View>();
    private SmileyParser parser = SmileyParser.getInstance();
    
    public void setTextSize(float size) {
    	textSize = size;
    }

    public MmsPlayerActivityAdapter(Context context, ArrayList<MmsPlayerActivityItemData> listItem) {
        mFactory = LayoutInflater.from(context);
        mListItem = listItem;
        mAllCount = mListItem.size();
        mContext = context;
        mDisplayMetrics = context.getResources().getDisplayMetrics();
    }

    public void onMovedToScrapHeap(View view) {
    }

    public ArrayList<MmsPlayerActivityItemData> getListItem() {
    	return mListItem;
    }
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mAllCount;
	}

	@Override
	public MmsPlayerActivityItemData getItem(int arg0) {
		return mListItem.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        Xlog.i(TAG, "getView, for position " + arg0 + ", current size " + textSize);
        MmsPlayerActivityItem itemView = (MmsPlayerActivityItem) mListItemViewCache.get(arg0);
        if (itemView != null) {
            String text = mListItem.get(arg0).getText();
            if (text != null) {
                TextView mText = itemView.getCurrentTextView();
                Xlog.i(TAG, "getView(): text view is null? " + (mText == null));
                if (mText != null && !isCurrentTextSize(mText.getTextSize())
                        && mText.getVisibility() != View.GONE) {
                    Xlog.i(TAG, "getView(): before set text size, textSize = " + mText.getTextSize());
                    mText.setTextSize(textSize);
                    Xlog.i(TAG, "getView(): after set text size, textSize = " + mText.getTextSize());
                }
            }
            Xlog.i(TAG, "getView(): from cache.");
            return itemView;
        }
        Xlog.i(TAG, "getView(): create new one.");

        itemView = (MmsPlayerActivityItem) mFactory.inflate(R.layout.mms_player_activity_item, null);

		TextView mPageIndex = (TextView) itemView.findViewById(R.id.page_index);
		MTKImageView mImage = (MTKImageView) itemView.findViewById(R.id.image);
		ImageView mVideo = (ImageView) itemView.findViewById(R.id.video);
		View mAudio = (View) itemView.findViewById(R.id.audio);
		TextView mAudioName = (TextView) itemView.findViewById(R.id.audio_name);
		ImageView mAudioIcon = (ImageView) itemView.findViewById(R.id.audio_icon);
        TextView mText = (TextView) itemView.findViewById(R.id.top_text);

        final MmsPlayerActivityItemData item = mListItem.get(arg0);

		// show page index
        String index = mContext.getResources().getString(R.string.page, arg0 + 1);
		mPageIndex.setText(index);
		
		// show image
		Uri imageUri = item.getImageUri();
        if (imageUri != null) {
        	Xlog.i(TAG, "set image: "+ imageUri);
        	mImage.setPadding(0, 1, 0, 0);
        	mImage.setImageURI(imageUri);
        	mImage.setVisibility(View.VISIBLE);
        } else {
        	mImage.setVisibility(View.GONE);
        }
        
        // show video thumbnail
        Bitmap t = item.getVideoThumbnail();
		if (t != null) {
			mVideo.setImageBitmap(t);
			mVideo.setVisibility(View.VISIBLE);
        } else {
        	mVideo.setVisibility(View.GONE);
        }

        String audioName = item.getAudioName();
        if (audioName != null) {
        	Xlog.i(TAG, "show audio name:" + audioName);
            mAudioName.setText(audioName);
            mAudioName.setTextSize(18);
            mAudioIcon.setVisibility(View.VISIBLE);
            mAudio.setVisibility(View.VISIBLE);
            mAudioName.setVisibility(View.VISIBLE);
        } else {
        	mAudioIcon.setVisibility(View.GONE);
        	mAudioName.setVisibility(View.GONE);
        	mAudio.setVisibility(View.GONE);
        }

        String text = item.getText();
        if ((imageUri != null || t != null) && text != null) {
            int leftAbs = Math.abs((item.getImageOrVideoLeft() - item.getTextLeft()));
            int topAbs = Math.abs(item.getImageOrVideoTop() - item.getTextTop());
            if (leftAbs > topAbs) {
                if (item.getImageOrVideoLeft() > item.getTextLeft()) {
                    mText = (TextView) itemView.findViewById(R.id.left_text);
                    if (item.getTextWidth() > 0) {
                        mText.setWidth(item.getTextWidth());
                    } else {
                        mText.setWidth(item.getImageOrVideoLeft());
                    }
                } else {
                    mText = (TextView) itemView.findViewById(R.id.right_text);
                    if (item.getTextWidth() > 0) {
                        mText.setWidth(item.getTextWidth());
                    } else {
                        mText.setWidth(item.getTextLeft());
                    }
                }
            } else if (item.getTextTop() > item.getImageOrVideoTop()) {
                mText = (TextView) itemView.findViewById(R.id.bottom_text);
            }
//            default text view is top_text
//            else {
//                mText = (TextView) itemView.findViewById(R.id.top_text);
//            }
        }
        if (text != null) {
            mText.setText(parser.addSmileySpans(text));
        	mText.setTextSize(textSize);
            mText.setVisibility(View.VISIBLE);
            //MTK_OP01_PROTECT_START
            if ("OP01".equals(SystemProperties.get("ro.operator.optr"))) {
                mText.setEnableShowUrlDialog(true);
            }
            //MTK_OP01_PROTECT_END
            itemView.setCurrentTextView(mText);
        } else {
        	mText.setVisibility(View.GONE);
        }

        mListItemViewCache.put(arg0, itemView);
        return itemView;
    }

	public void clearAllCache() {
	    if (mListItemViewCache.size() > 0) {
	        View itemView = null;
	        for (Integer key : mListItemViewCache.keySet()) {
	            itemView = mListItemViewCache.get(key);
	            MTKImageView mImage = (MTKImageView) itemView.findViewById(R.id.image);
	            mImage.setImageURI(null);
	        }
            mListItemViewCache.clear();
	    }
	}

    private boolean isCurrentTextSize(float viewTextSize) {
        if (viewTextSize == (textSize * mDisplayMetrics.scaledDensity)) {
            return true;
        }
        return false;
    }
}
