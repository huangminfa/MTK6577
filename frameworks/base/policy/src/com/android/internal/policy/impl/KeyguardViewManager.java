/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.internal.policy.impl;

import com.android.internal.R;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Canvas;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.os.SystemProperties;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;

// ALPS00264727 begin
import android.os.Handler;
import android.os.Message;
import com.android.internal.policy.impl.KeyguardUpdateMonitor.SystemStateCallback;  
// ALPS00264727 end


import android.graphics.Color;

/**
 * Manages creating, showing, hiding and resetting the keyguard.  Calls back
 * via {@link com.android.internal.policy.impl.KeyguardViewCallback} to poke
 * the wake lock and report that the keyguard is done, which is in turn,
 * reported to this class by the current {@link KeyguardViewBase}.
 */
public class KeyguardViewManager implements KeyguardWindowController {
    private static boolean DEBUG = true;
    private static String TAG = "KeyguardViewManager";

    private final Context mContext;
    private final ViewManager mViewManager;
    private final KeyguardViewCallback mCallback;
    private final KeyguardViewProperties mKeyguardViewProperties;

    private final KeyguardUpdateMonitor mUpdateMonitor;

    private WindowManager.LayoutParams mWindowLayoutParams;
    private boolean mNeedsInput = false;

    private FrameLayout mKeyguardHost;
    private KeyguardViewBase mKeyguardView;

    private static boolean mScreenOn = false;

    // ALPS00264727 begin
    private int mScrnOrientationModeBeforeShutdown = ActivityInfo.SCREEN_ORIENTATION_SENSOR; // to keep the screen orientation sensor mode before shutdown
    H mH = new H();
    // ALPS00264727 end

    public interface ShowListener {
        void onShown(IBinder windowToken);
    };
    
    /**
     * @param context Used to create views.
     * @param viewManager Keyguard will be attached to this.
     * @param callback Used to notify of changes.
     */
    public KeyguardViewManager(Context context, ViewManager viewManager,
            KeyguardViewCallback callback, KeyguardViewProperties keyguardViewProperties,
            KeyguardUpdateMonitor updateMonitor) {
        mContext = context;
        mViewManager = viewManager;
        mCallback = callback;
        mKeyguardViewProperties = keyguardViewProperties;

        mUpdateMonitor = updateMonitor;

        // ALPS00264727, register the callback for tablet only
        if ("tablet".equals(SystemProperties.get("ro.build.characteristics"))) {
            updateMonitor.registerSystemStateCallback(mSystemStateCallback); 
        }
    }

    /** ALPS00264727 begin , handle orientation sensor mode update at shutdown or bootup state **/
    final class H extends Handler {
        public static final int SET_LOCKSCREEN_SENSOR_MODE = 1;

        public H() {
        }

        @Override
        public void handleMessage(Message msg) {

            if (DEBUG) Xlog.d(TAG, "handle msg: " + msg.what + ", arg1=" + msg.arg1);

            switch (msg.what) {
                case SET_LOCKSCREEN_SENSOR_MODE: {
                    if ((mWindowLayoutParams != null) && (mWindowLayoutParams.screenOrientation != msg.arg1)) {
		        if (DEBUG) Xlog.d(TAG, "curr_orientation=" + mWindowLayoutParams.screenOrientation);
                        mWindowLayoutParams.screenOrientation = msg.arg1;

                        /* notify WMS for the lock screen orientation mode change */
                        mViewManager.updateViewLayout(mKeyguardHost, mWindowLayoutParams);
                    }
                } break;
            }
        }
    }
    
    private KeyguardUpdateMonitor.SystemStateCallback mSystemStateCallback = new KeyguardUpdateMonitor.SystemStateCallback() {
        public void onSysShutdown() {
            if (DEBUG) Xlog.d(TAG, "onSysShutdown called.");

            if (mWindowLayoutParams != null) {
                mScrnOrientationModeBeforeShutdown = mWindowLayoutParams.screenOrientation;

                Message m = mH.obtainMessage(H.SET_LOCKSCREEN_SENSOR_MODE);
                m.arg1 = ActivityInfo.SCREEN_ORIENTATION_USER;
                mH.sendMessage(m);            
            } else {
                if (DEBUG) Xlog.d(TAG, "mWindowLayoutParams is null, ignore the message.");
            }
        }
        
        public void onSysBootup() {
            if (DEBUG) Xlog.d(TAG, "onSysBootup called, mScrnOrientationModeBeforeShutdown = " + mScrnOrientationModeBeforeShutdown);
            Message m = mH.obtainMessage(H.SET_LOCKSCREEN_SENSOR_MODE);
            m.arg1 = mScrnOrientationModeBeforeShutdown;
            mH.sendMessage(m);            
        }
    };
    /** ALPS00264727 end **/
    
    /**
     * Helper class to host the keyguard view.
     */
    private static class KeyguardViewHost extends FrameLayout {
        private final KeyguardViewCallback mCallback;

        private KeyguardViewHost(Context context, KeyguardViewCallback callback) {
            super(context);
            mCallback = callback;
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            //only need to notify when the screen is off
            if (false == mScreenOn){
                mCallback.keyguardDoneDrawing();
            }
        }
    }

    /**
     * Show the keyguard.  Will handle creating and attaching to the view manager
     * lazily.
     */
    public synchronized void show() {
        if (DEBUG) Xlog.d(TAG, "show(); mKeyguardView==" + mKeyguardView);

        Resources res = mContext.getResources();
        boolean enableScreenRotation =
                SystemProperties.getBoolean("lockscreen.rot_override",false)
                || res.getBoolean(R.bool.config_enableLockScreenRotation);
        if (mKeyguardHost == null) {
            if (DEBUG) Xlog.d(TAG, "keyguard host is null, creating it...");

            mKeyguardHost = new KeyguardViewHost(mContext, mCallback);

            final int stretch = ViewGroup.LayoutParams.MATCH_PARENT;
            int flags = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
                    | WindowManager.LayoutParams.FLAG_KEEP_SURFACE_WHILE_ANIMATING
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
            if (mUpdateMonitor.DM_IsLocked()) {//in the first created
                flags &= ~WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
                flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                flags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
            } else {
                flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
                flags |= WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
            }
            if (!mNeedsInput) {
                flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            }
            if (ActivityManager.isHighEndGfx(((WindowManager)mContext.getSystemService(
                    Context.WINDOW_SERVICE)).getDefaultDisplay())) {
                flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            }
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    stretch, stretch, WindowManager.LayoutParams.TYPE_KEYGUARD,
                    flags, PixelFormat.TRANSLUCENT);
            lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
            lp.windowAnimations = com.android.internal.R.style.Animation_LockScreen;
            if (ActivityManager.isHighEndGfx(((WindowManager)mContext.getSystemService(
                    Context.WINDOW_SERVICE)).getDefaultDisplay())) {
                lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
                lp.privateFlags |=
                        WindowManager.LayoutParams.PRIVATE_FLAG_FORCE_HARDWARE_ACCELERATED;
            }

            lp.setTitle("Keyguard");
            mWindowLayoutParams = lp;

            mViewManager.addView(mKeyguardHost, lp);
        }

        if (enableScreenRotation || FeatureOption.MTK_TB_APP_LANDSCAPE_SUPPORT) {
            if (DEBUG) Log.d(TAG, "Rotation sensor for lock screen On!");
            mWindowLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        } else {
            if (DEBUG) Log.d(TAG, "Rotation sensor for lock screen Off!");
            mWindowLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
        }

        mViewManager.updateViewLayout(mKeyguardHost, mWindowLayoutParams);

        if (mKeyguardView == null) {
            if (DEBUG) Xlog.d(TAG, "keyguard view is null, creating it...");
            mKeyguardView = mKeyguardViewProperties.createKeyguardView(mContext, mUpdateMonitor, this);
            mKeyguardView.setId(R.id.lock_screen);
            mKeyguardView.setCallback(mCallback);

            final ViewGroup.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            mKeyguardHost.addView(mKeyguardView, lp);

            if (mScreenOn) {
                mKeyguardView.show();
            }
        }

        // Disable aspects of the system/status/navigation bars that are not appropriate or
        // useful for the lockscreen but can be re-shown by dialogs or SHOW_WHEN_LOCKED activities.
        // Other disabled bits are handled by the KeyguardViewMediator talking directly to the
        // status bar service.
        int visFlags = ( View.STATUS_BAR_DISABLE_BACK
                | View.STATUS_BAR_DISABLE_HOME);
        
        mKeyguardHost.setSystemUiVisibility(visFlags);

        mViewManager.updateViewLayout(mKeyguardHost, mWindowLayoutParams);
        mKeyguardHost.setVisibility(View.VISIBLE);
        mKeyguardView.requestFocus();
    }

    public void setNeedsInput(boolean needsInput) {
        mNeedsInput = needsInput;
        if (mWindowLayoutParams != null) {
            if (needsInput) {
                mWindowLayoutParams.flags &=
                    ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            } else {
                mWindowLayoutParams.flags |=
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            }
            mViewManager.updateViewLayout(mKeyguardHost, mWindowLayoutParams);
        }
    }

    /**
     * Reset the state of the view.
     */
    public synchronized void reset() {
        boolean flag = mUpdateMonitor.DM_IsLocked();
        //if (DEBUG) Xlog.d(TAG, "reset(), flag="+flag);
        //reLayoutScreen(flag);
        if (mKeyguardView != null) {
            mKeyguardView.reset();
        } else {
            Log.i(TAG, "Oh, Timing issue, actually, we needn't skip to here");
            mCallback.doKeyguardLocked();
        }
    }

    /**
     * Update layout for KeyguardView
     * 
     * @param dmLock
     */
    public void reLayoutScreen(boolean dmLock) {
        if (mWindowLayoutParams != null) {
            Log.i(TAG, "reLayoutScreen, dmLock="+dmLock);
            if (dmLock) {
                mWindowLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
                mWindowLayoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                mWindowLayoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
            } else {
                mWindowLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                mWindowLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
                mWindowLayoutParams.flags |= WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
            }
            mViewManager.updateViewLayout(mKeyguardHost, mWindowLayoutParams);
        }
    }
    
    public synchronized void onScreenTurnedOff() {
        if (DEBUG) Xlog.d(TAG, "onScreenTurnedOff()");
        mScreenOn = false;
        if (mKeyguardView != null) {
            mKeyguardView.onScreenTurnedOff();
        }
    }

    public synchronized void onScreenTurnedOn(
            final KeyguardViewManager.ShowListener showListener) {
        if (DEBUG) Xlog.d(TAG, "onScreenTurnedOn()");
        mScreenOn = true;
        if (mKeyguardView != null) {
            mKeyguardView.onScreenTurnedOn();

            // Caller should wait for this window to be shown before turning
            // on the screen.
            if (mKeyguardHost.getVisibility() == View.VISIBLE) {
                // Keyguard may be in the process of being shown, but not yet
                // updated with the window manager...  give it a chance to do so.
                mKeyguardHost.post(new Runnable() {
                    @Override public void run() {
                        if (mKeyguardHost.getVisibility() == View.VISIBLE) {
                            showListener.onShown(mKeyguardHost.getWindowToken());
                        } else {
                            showListener.onShown(null);
                        }
                    }
                });
            } else {
                showListener.onShown(null);
            }
        } else {
            showListener.onShown(null);
        }
    }

    public synchronized void verifyUnlock() {
        if (DEBUG) Xlog.d(TAG, "verifyUnlock()");
        show();
        mKeyguardView.verifyUnlock();
    }

    /**
     * A key has woken the device.  We use this to potentially adjust the state
     * of the lock screen based on the key.
     *
     * The 'Tq' suffix is per the documentation in {@link android.view.WindowManagerPolicy}.
     * Be sure not to take any action that takes a long time; any significant
     * action should be posted to a handler.
     *
     * @param keyCode The wake key.
     */
    public boolean wakeWhenReadyTq(int keyCode) {
        Xlog.d(TAG, "wakeWhenReady(" + keyCode + ")");
        if (mKeyguardView != null) {
            mKeyguardView.wakeWhenReadyTq(keyCode);
            return true;
        } else {
            Xlog.w(TAG, "mKeyguardView is null in wakeWhenReadyTq");
            return false;
        }
    }

    /**
     * Hides the keyguard view
     */
    public synchronized void hide() {
        if (DEBUG) Xlog.d(TAG, "hide()");

        if (mKeyguardHost != null) {
            mKeyguardHost.setVisibility(View.GONE);
            // Don't do this right away, so we can let the view continue to animate
            // as it goes away.
            if (mKeyguardView != null) {
                final KeyguardViewBase lastView = mKeyguardView;
                mKeyguardView = null;
                mKeyguardHost.post(new Runnable() {
                    public void run() {
                        synchronized (KeyguardViewManager.this) {
                            lastView.cleanUp();
                            mKeyguardHost.removeView(lastView);
                        }
                    }
                });
            }
        }
    }

    /**
     * @return Whether the keyguard is showing
     */
    public synchronized boolean isShowing() {
        return (mKeyguardHost != null && mKeyguardHost.getVisibility() == View.VISIBLE);
    }

    public void setScreenStatus(boolean screenOn){
        mScreenOn = screenOn;
    }

    public void setDebugFilterStatus(boolean debugFlag){
        DEBUG = debugFlag;
    }
}
