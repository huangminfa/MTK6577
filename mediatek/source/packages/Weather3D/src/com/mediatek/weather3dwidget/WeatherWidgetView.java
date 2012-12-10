package com.mediatek.weather3dwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnPostDrawListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.IMTKWidget;
import android.widget.RemoteViews.RemoteView;

@RemoteView
public class WeatherWidgetView extends FrameLayout implements IMTKWidget, OnPostDrawListener{
    private static final String TAG = "W3D/WeatherWidgetView";

    private static final int SCREEN_UNKNOWN = -10000;

    private WeatherView mWeatherView;
    private ImageView mImageView;

    private int mAppWidgetScreen = SCREEN_UNKNOWN;
    private int mAppWidgetId = -1;
    private int mPostDrawWaitCount = -2;
    private int mPostDrawCurCount = 0;

    private int mLauncherState = 0;
    private static final int LAUNCHER_STATE_START_DRAG = 1;
    private static final int LAUNCHER_STATE_STOP_DRAG = -1;
    private static final int LAUNCHER_STATE_MOVE_OUT = 2;
    private static final int LAUNCHER_STATE_MOVE_IN = -2;
    private static final int LAUNCHER_STATE_START_COVERED = 3;
    private static final int LAUNCHER_STATE_STOP_COVERED = -3;
    private static final int LAUNCHER_STATE_PAUSE = 4;
    private static final int LAUNCHER_STATE_RESUME = -4;

    public WeatherWidgetView(Context context) {
        super(context);
        LogUtil.v(TAG, "WeatherWidgetView - 1");
    }

    public WeatherWidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogUtil.v(TAG, "WeatherWidgetView - 2");
    }

    /*
     * Be careful to check this function when you modify classes with "MTKWidget" prefix.
     * The changes of your modification will cause more or less draw count.
     */
    public void setPostDrawWaitCount(int count) {
        LogUtil.v(TAG, "setPostCount(" + count + ")");
        mPostDrawCurCount = 0;
        mPostDrawWaitCount = mPostDrawCurCount + count;
    }

    public boolean onPostDraw() {
        mPostDrawCurCount += 1;
        // LogUtil.v(TAG, "onPostDraw(): curCount = " + mPostDrawCurCount + ", waitCount = " + mPostDrawWaitCount);

        if (mPostDrawCurCount == mPostDrawWaitCount){
            mWeatherView.setVisibility(View.INVISIBLE);
            setPostDrawWaitCount(-2);
            LogUtil.v(TAG, "mLauncherState = " + mLauncherState);
        }
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LogUtil.i(TAG, "onFinishInflate");

        mWeatherView = (WeatherView)this.findViewWithTag("weather_view");
        mImageView = (ImageView)this.findViewWithTag("snapshot");;
        mImageView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnPostDrawListener(this);
        //sendOnAttachedIntent();
        LogUtil.v(TAG, "onAttachedToWindow() - id = " + mAppWidgetId);
        if (getWidgetId() != AppWidgetManager.INVALID_APPWIDGET_ID) {
            // send out intent to notify WeatherWidget -> UpdateService for ready to update screen
            sendOnAttachedIntent();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPostDrawListener(this);
        LogUtil.v(TAG, "onDetachedFromWindow() - id = " + mAppWidgetId);
    }

    private void sendOnAttachedIntent() {
        LogUtil.v(TAG, "sendOnAttachedIntent, id = " + getWidgetId());
        Intent intent = new Intent(WeatherWidgetAction.ACTION_WEATHER_WIDGET_VIEW_ATTACH);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, getWidgetId());
        getContext().sendBroadcast(intent);
    }

    /**
     * The count could be installed in launcher.
     * @return
     */
    public int getPermittedCount() {
        LogUtil.i(TAG, "getPermittedCount");
        return 1;
    }

    /**
     * The screen index of current AppWidget.
     * @return
     */
    public int getScreen() {
        LogUtil.i(TAG, "getScreen = " + mAppWidgetScreen);
        return mAppWidgetScreen;
    }

    /**
     * Set the screen index of current AppWidget.
     * @param screen
     */
    public void setScreen(int screen) {
        LogUtil.i(TAG, "setScreen(" + screen + "), mAppWidgetScreen = " + mAppWidgetScreen);
        mAppWidgetScreen = screen;
    }

    /**
     * The AppWidgetId of current AppWidget.
     * @return
     */
    public int getWidgetId() {
        LogUtil.i(TAG, "getWidgetId = " + mAppWidgetId);
        return mAppWidgetId;
    }

    /**
     * Set the AppWidgetId of current AppWidget.
     * @param widgetId
     */
    public void setWidgetId(int widgetId) {
        mAppWidgetId = widgetId;
        LogUtil.i(TAG, "setWidgetId(" + widgetId + ")");
    }

    private boolean startPause(int curScreen) {
        LogUtil.i(TAG, "startPause() - curScreen = " + curScreen);
        //when in move state,
        //launcher will use the return value of moveOut().
        mWeatherView.pauseRendering();
        LogUtil.i(TAG, "startPause, pauseRendering");
        Bitmap bitmap = mWeatherView.getScreenShot();
        mImageView.setImageBitmap(bitmap);
        LogUtil.i(TAG, "startPause, setImageBitmap");
        mImageView.setVisibility(View.VISIBLE);
        setPostDrawWaitCount(1);
        return true;
    }

    private void stopPause(int curScreen) {
        LogUtil.i(TAG, "stopPause() - curScreen = " + curScreen);
        setPostDrawWaitCount(-2);

        mImageView.setVisibility(View.INVISIBLE);
        LogUtil.i(TAG, "stopPause() - set image invisible");
        mWeatherView.resumeRendering();
        LogUtil.i(TAG, "startPause, resumeRendering");
        mWeatherView.setVisibility(View.VISIBLE);
        LogUtil.i(TAG, "stopPause() - set Weather visible");
        // resume 3D Stage View again
        LogUtil.i(TAG, "stop pause ");
    }

    /**
     * Will be called when user start to drag current AppWidget.
     */
    public void startDrag() {
        LogUtil.i(TAG, "startDrag(), mLauncherState = " + mLauncherState);

        if (mLauncherState == 0) {
            mLauncherState = LAUNCHER_STATE_START_DRAG;
            startPause(mAppWidgetScreen);
        } else if (mLauncherState > 0) {
            mLauncherState = LAUNCHER_STATE_START_DRAG;
            LogUtil.i(TAG, "change mLauncherState to " + mLauncherState);
        } else {
            //do nothing when it is not correct.
            LogUtil.w(TAG, "handle startDrag. mLauncherState = " + mLauncherState);
        }
    }

    /**
     * Will be called when user stop to drag current AppWidget.
     */
    public void stopDrag() {
        LogUtil.i(TAG, "stopDrag(), mLauncherState = " + mLauncherState);

        if ((mLauncherState + LAUNCHER_STATE_STOP_DRAG) == 0) {
            mLauncherState = 0;
            stopPause(mAppWidgetScreen);
        } else {
            //do nothing when it is not correct.
            LogUtil.w(TAG, "handle stopDrag. mLauncherState = " + mLauncherState);
        }
    }

    /**
     * Will be called when user leave the screen which current AppWidget locates in.
     * @param curScreen which side's screen user will be seen.
     * -1 means move to left, +1 means move to right.
     * @return if IMTKWidget's implemention is ready for moving out, it will return true.
     * otherwise, return false.
     * <br/>Note: while return true, the Launcher will
     */
    public boolean moveOut(int curScreen) {
        LogUtil.i(TAG, "moveOut(" + curScreen + "), mLauncherState = " + mLauncherState);
        return true;
    }

    /**
     * Will be called when the screen which AppWidget locates in will be seen by user.
     * @param curScreen the screen AppWidget locates in.
     */
    public void moveIn(int curScreen) {
        LogUtil.i(TAG, "moveIn(" + curScreen + ") , mLauncherState = " + mLauncherState);
    }

    /**
     * Will be called when the current AppWidget will be not seen
     * before launcher makes other views cover the current AppWidget.
     * @param curScreen
     */
    public void startCovered(int curScreen) {
        LogUtil.i(TAG, "startCovered(" + curScreen + "), mLauncherState = " + mLauncherState);
        if (mLauncherState == 0) {
            mLauncherState = LAUNCHER_STATE_START_COVERED;
            startPause(curScreen);
        } else if (mLauncherState > 0) {
            mLauncherState = LAUNCHER_STATE_START_COVERED;
            LogUtil.i(TAG, "change mLauncherState to " + mLauncherState);
        } else {
            //do nothing when it is not correct.
            LogUtil.w(TAG, "handle startCovered. mLauncherState = " + mLauncherState);
        }
    }

    /**
     * Will be called when the current AppWidget will be seen
     * after launcher moves away other views on the top of current AppWidget.
     * @param curScreen
     */
    public void stopCovered(int curScreen) {
        LogUtil.i(TAG, "stopCovered(" + curScreen + "), mLauncherState = " + mLauncherState);
        if ((mLauncherState + LAUNCHER_STATE_STOP_COVERED) == 0) {
            mLauncherState = 0;
            stopPause(curScreen);
        } else {
            //do nothing when it is not correct.
            LogUtil.w(TAG, "handle stopCovered. mLauncherState = " + mLauncherState);
        }
    }

    /**
     * Will be called when launcher's onPause be called.
     * @param curScreen
     */
    public void onPauseWhenShown(int curScreen) {
        LogUtil.i(TAG, "onPauseWhenShown(" + curScreen + "), mLauncherState = " + mLauncherState);
        if (mLauncherState == 0) {
            mLauncherState = LAUNCHER_STATE_PAUSE;
            startPause(curScreen);
        } else if (mLauncherState > 0) {
            mLauncherState = LAUNCHER_STATE_PAUSE;
            LogUtil.i(TAG, "change mLauncherState to " + mLauncherState);
        } else {
            //do nothing when it is not correct.
            LogUtil.w(TAG, "handle onPauseWhenShown. mLauncherState = " + mLauncherState);
        }
    }

    /**
     * Will be called when launcher's onResume be called.
     * @param curScreen
     */
    public void onResumeWhenShown(int curScreen){
        LogUtil.i(TAG, "onResumeWhenShown(" + curScreen + "), mLauncherState = " + mLauncherState);
        if ((mLauncherState + LAUNCHER_STATE_RESUME) == 0) {
            mLauncherState = 0;
            stopPause(curScreen);
        } else {
            //do nothing when it is not correct.
            LogUtil.w(TAG, "handle onResumeWhenShown. mLauncherState = " + mLauncherState);
        }
    }

    /**
     * Will be called after launcher's onSaveInstanceState is called.
     * @param outSate
     */
    public void onSaveInstanceState(Bundle outSate) {
        LogUtil.i(TAG, "onSaveInstanceState");
    }

    /**
     * Will be called after launcher's onRestoreInstanceState is called.
     * @param state
     */
    public void onRestoreInstanceState(Bundle state) {
        LogUtil.i(TAG, "onRestoreInstanceState");
    }
    
    public void leaveAppwidgetScreen() {
        LogUtil.i(TAG, "leaveAppwidgetScreen");
        mWeatherView.pauseRendering();
        mWeatherView.setVisibility(View.INVISIBLE);
    }
    
    public void enterAppwidgetScreen() {
        LogUtil.i(TAG, "enterAppwidgetScreen");
        mImageView.setVisibility(View.INVISIBLE);
        mWeatherView.setVisibility(View.VISIBLE);
        mWeatherView.resumeRendering();
        mLauncherState = 0;
    }
}

