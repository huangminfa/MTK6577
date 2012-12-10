package com.mediatek.widgetdemos.musicplayer3d;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RemoteViews.RemoteView;

@RemoteView
public class GestureView extends View {
    private GestureDetector mGestureDetector;
    private int mPositionX;
    private int mPositionY;
    private Context mContext;

    public GestureView(Context context) {
        super(context);
        mContext = context;
    }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setPosition(int x, int y) {
        mPositionX = x;
        mPositionY = y;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        offsetLeftAndRight(mPositionX + 1);
        offsetTopAndBottom(mPositionY + 1);
    }

    public void setOnGestureListener(GestureDetector.OnGestureListener listener) {
        mGestureDetector = new GestureDetector(mContext, listener);
    }
}
