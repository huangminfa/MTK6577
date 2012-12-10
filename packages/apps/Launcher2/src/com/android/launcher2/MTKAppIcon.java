/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.launcher2;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.launcher.R;

/**
 * An icon on a PagedView, specifically for items in the launcher's paged view (with compound
 * drawables on the top).
 */
public class MTKAppIcon extends RelativeLayout implements Checkable {
    private static final String TAG = "MTKAppIcon";
    
    protected Launcher mLauncher;
    PagedViewIcon mAppIcon;
    TextView mUnread;  
    private ApplicationInfo mInfo;
    private int mAlpha = 255;
    private int mHolographicAlpha;
    private boolean mIsChecked;

    public MTKAppIcon(Context context) {
        super(context);
        init(context);
    }

    public MTKAppIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MTKAppIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    private void init(Context context) {
        mLauncher = (Launcher)context;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();  
        
        /* If use the default id, can get the view, but if not, may return null, so 
         * be careful when create the shortcut icon from different layout, make it the same
         * id is very important, like application and boxed_application.
         */
        mAppIcon = (PagedViewIcon)findViewById(R.id.app_customize_application_icon);
        mUnread = (TextView)findViewById(R.id.app_customize_unread); 
    }

    /**
     * Update unread message of the shortcut, the number of unread information comes from
     * the list. 
     */
    public void updateUnreadNum() {
        if (mInfo.unreadNum <= 0) {
            mUnread.setVisibility(View.GONE);
        } else {
            mUnread.setVisibility(View.VISIBLE);
            if (mInfo.unreadNum > 99) {
                mUnread.setText(MTKUnreadLoader.getExceedText());
            } else {
                mUnread.setText(String.valueOf(mInfo.unreadNum));
            }
        }  
    }
    
    /**
     * Update the unread message of the shortcut with the given information.
     * 
     * @param unreadCount the number of the unread message.
     */
    public void updateUnreadNum(int unreadNum) {
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "updateUnreadNum: unreadNum = " + unreadNum + ",mInfo = " + mInfo);
        }
        if (unreadNum <= 0) {
            mInfo.unreadNum = 0;
            mUnread.setVisibility(View.GONE);
        } else {
            mInfo.unreadNum = unreadNum;
            mUnread.setVisibility(View.VISIBLE);
            if (unreadNum > 99) {
                mUnread.setText(MTKUnreadLoader.getExceedText());
            } else {
                mUnread.setText(String.valueOf(unreadNum));
            }
        }
        setTag(mInfo);
    }
    
    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        mAppIcon.setTag(tag);
        mUnread.setTag(tag);
        mInfo = (ApplicationInfo)tag;
    }

    protected HolographicPagedViewIcon getHolographicOutlineView() {
        return mAppIcon.getHolographicOutlineView();
    }

    protected Bitmap getHolographicOutline() {
        return mAppIcon.getHolographicOutline();
    }
    
    public void applyFromApplicationInfo(ApplicationInfo info, boolean scaleUp,
            HolographicOutlineHelper holoOutlineHelper) {
        mAppIcon.applyFromApplicationInfo(info, scaleUp, holoOutlineHelper);
        setTag(info);
        updateUnreadNum();
    }

    public void setHolographicOutline(Bitmap holoOutline) {
        mAppIcon.setHolographicOutline(holoOutline);
    }

    @Override
    public void setAlpha(float alpha) {
        final float viewAlpha = HolographicOutlineHelper.viewAlphaInterpolator(alpha);
        final float holographicAlpha = HolographicOutlineHelper.highlightAlphaInterpolator(alpha);
        int newViewAlpha = (int) (viewAlpha * 255);
        int newHolographicAlpha = (int) (holographicAlpha * 255);
        if ((mAlpha != newViewAlpha) || (mHolographicAlpha != newHolographicAlpha)) {
            mAlpha = newViewAlpha;
            mHolographicAlpha = newHolographicAlpha;
            super.setAlpha(viewAlpha);
        }
    }

    public void invalidateCheckedImage() {
        mAppIcon.invalidate();
    }

    @Override
    public boolean isChecked() {
        return mAppIcon.isChecked();
    }

    void setChecked(boolean checked, boolean animate) {
        mAppIcon.setChecked(checked, animate);
    }

    @Override
    public void setChecked(boolean checked) {
        mAppIcon.setChecked(checked, true);
    }

    @Override
    public void toggle() {
        mAppIcon.toggle();
    }
}
