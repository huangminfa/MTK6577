package com.android.settings.applications;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;
import android.view.View;
import android.view.ViewConfiguration;

public class CustomizedScrollView extends ScrollView {
	
	private int mOverscrollDistance;
	
    public CustomizedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mOverscrollDistance = configuration.getScaledOverscrollDistance();
    }
    
    @Override
    public void scrollTo(int x, int y) {
        // we rely on the fact the View.scrollBy calls scrollTo.
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            final int range = getScrollRange();
            final int overscrollMode = getOverScrollMode();
            final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS ||
                    (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);
            if(!canOverscroll || mOverscrollDistance <= 0) {
                x = clamp(x, getWidth() - mPaddingRight - mPaddingLeft, child.getWidth());
                y = clamp(y, getHeight() - mPaddingBottom - mPaddingTop, child.getHeight());
            }            
            if (x != mScrollX || y != mScrollY) {
                super.scrollTo(x, y);
            }
        }
    }
    
    private int getScrollRange() {
        int scrollRange = 0;
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            scrollRange = Math.max(0,
                    child.getHeight() - (getHeight() - mPaddingBottom - mPaddingTop));
        }
        return scrollRange;
    }
    

    private int clamp(int n, int my, int child) {
        if (my >= child || n < 0) {
            return 0;
        }
        if ((my+n) > child) {
            return child-my;
        }
        return n;
    }
}