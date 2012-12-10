/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.systemui.statusbar.tablet;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Slog;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;

import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.R;


public class NotificationArea extends LinearLayout {
    private static final String TAG = "NotificationArea";
	private static final boolean DEBUG = false;	
	private TabletStatusBar mTabletStatusBar;
	private View mMoreView;
	private int newNotificationIconsShow;
	private Context mContext;
    
    public NotificationArea(Context context, AttributeSet attrs) {
        super(context, attrs);
		mContext = context;
    }

    @Override
    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        if (super.onRequestSendAccessibilityEvent(child, event)) {
            // The event is coming from a descendant like battery but append
            // the content of the entire notification area so accessibility
            // services can choose how to present the content to the user.
            AccessibilityEvent record = AccessibilityEvent.obtain();
            onInitializeAccessibilityEvent(record);
            dispatchPopulateAccessibilityEvent(record);
            event.appendRecord(record);
            return true;
        }
        return false;
    }   

	@Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
		checkOverflow(getWidth());
    }

	private void checkOverflow(int width)
	{
	       if (mMoreView == null) return;
	       
		int moreIconWidth = (int) mContext.getResources().getDimension(R.dimen.status_bar_icon_drawing_size);
		if (mMoreView.getVisibility() == View.VISIBLE)
		{
			width -= moreIconWidth;
		}
		
		newNotificationIconsShow = 0;
		int mNotificationIconsShow = mTabletStatusBar.getShowingNotificationIcons();
		//Each navigation button is 80dip. 12dip is the margin.
		int widthExcludingNavigationButtons = mContext.getResources().getDisplayMetrics().widthPixels - 80 * 3 + 12;		
		int leftWidth = widthExcludingNavigationButtons - (width - (mNotificationIconsShow * 32));
		newNotificationIconsShow = (int) (leftWidth / 32);

		if (DEBUG)
		{
			Slog.d(TAG, "Resolution width: " + mContext.getResources().getDisplayMetrics().widthPixels);
			Slog.d(TAG, "NotificationArea width: " + width);
			Slog.d(TAG, "leftWidth: " + leftWidth);
			Slog.d(TAG, "mNotificationIcons: " + mNotificationIconsShow);
			Slog.d(TAG, "newNotificationIcons: " + newNotificationIconsShow);
		}
		
		if (newNotificationIconsShow < 3)
		{
			newNotificationIconsShow--;
			post(new Runnable()
			{
				@Override
				public void run()
				{
					if (mMoreView.getVisibility() == View.GONE)
					{
						mMoreView.setVisibility(View.VISIBLE);
					}
					mTabletStatusBar.setMaxNotificationIcons(newNotificationIconsShow);
				}
			});				
		}
		else
		{
			newNotificationIconsShow = 3;
			post(new Runnable()
			{
				@Override
				public void run()
				{
					if (mMoreView.getVisibility() == View.VISIBLE)
					{
						mMoreView.setVisibility(View.GONE);
					}
					mTabletStatusBar.setMaxNotificationIcons(newNotificationIconsShow);
				}
			});
		}
	}
	public void setTabletStatusBar(TabletStatusBar statusBar)
	{
		mTabletStatusBar = statusBar;
	}

	public void setOverflowIndicator(View v)
	{
        mMoreView = v;
    }
}
