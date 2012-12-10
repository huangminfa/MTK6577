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

package com.android.gallery3d.ui;

import com.android.gallery3d.anim.CanvasAnimation;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.os.Process;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.android.gallery3d.util.MediatekFeature;
import android.view.WindowManager;

// The root component of all <code>GLView</code>s. The rendering is done in GL
// thread while the event handling is done in the main thread.  To synchronize
// the two threads, the entry points of this package need to synchronize on the
// <code>GLRootView</code> instance unless it can be proved that the rendering
// thread won't access the same thing as the method. The entry points include:
// (1) The public methods of HeadUpDisplay
// (2) The public methods of CameraHeadUpDisplay
// (3) The overridden methods in GLRootView.
public class GLRootView extends GLSurfaceView
        implements GLSurfaceView.Renderer, GLRoot {
    private static final String TAG = "GLRootView";

    private static final boolean DEBUG_FPS = false;
    private int mFrameCount = 0;
    private long mFrameCountingStart = 0;

    private static final boolean DEBUG_INVALIDATE = false;
    private int mInvalidateColor = 0;

    private static final boolean DEBUG_DRAWING_STAT = false;

    private static final int FLAG_INITIALIZED = 1;
    private static final int FLAG_NEED_LAYOUT = 2;

    private GL11 mGL;
    private GLCanvasImpl mCanvas;

    private GLView mContentView;
    private DisplayMetrics mDisplayMetrics;

    private int mFlags = FLAG_NEED_LAYOUT;
    private volatile boolean mRenderRequested = false;

    private Rect mClipRect = new Rect();
    private int mClipRetryCount = 0;

    private final GalleryEGLConfigChooser mEglConfigChooser =
            new GalleryEGLConfigChooser();

    private final ArrayList<CanvasAnimation> mAnimations =
            new ArrayList<CanvasAnimation>();

    private final LinkedList<OnGLIdleListener> mIdleListeners =
            new LinkedList<OnGLIdleListener>();

    private final IdleRunner mIdleRunner = new IdleRunner();

    private final ReentrantLock mRenderLock = new ReentrantLock();

    private static final int TARGET_FRAME_TIME = 16;
    private long mLastDrawFinishTime;
    private boolean mInDownState = false;

    //ADDED for stereo photo display
    private static final boolean mIsStereoDisplaySupported = 
                        MediatekFeature.isStereoDisplaySupported();

    private boolean mStereoMode = false;
    private boolean mSurfaceStereoMode = false;

    private static final int STEREO_DRAW_LEFT_PASS = 0;
    private static final int STEREO_DRAW_RIGHT_PASS = 1;

    private static class StereoPass {
        public StereoPass(float s, float t) {
            scale = s;
            transf = t;
        }
        public float scale;
        public float transf;
    }

    private StereoPass mStereoPass[] = new StereoPass[2];
    private StereoPass mCurrSP = null;
    private int mStereoPassId;

    private void InitStereoDisplay(){
        mStereoPass[STEREO_DRAW_LEFT_PASS] = new StereoPass(0.5f,0.0f);
        mStereoPass[STEREO_DRAW_RIGHT_PASS] = new StereoPass(0.5f,0.5f);
        mStereoPassId = MediatekFeature.STEREO_DISPLAY_INVALID_PASS;
    }

    @Override
    public void setStereoMode(boolean mode) {
        if (mIsStereoDisplaySupported) {
            if (mStereoMode == mode) {
                return;
            }
            mStereoMode = mode;
            //operate Surface view to render stereo should be
            //performed each time onDrawFrame() is called.
            //Or there may be a chance that surface attributes
            //are modified during frame is drawing
        } else {
            mStereoMode = false;
        }
    }

    @Override
    public boolean getStereoMode() {
        return mSurfaceStereoMode;//mStereoMode;
    }

    @Override
    public int getStereoPassId() {
        return mStereoPassId;
    }

    private void setSurfaceStereoMode(boolean surfaceStereoMode) {
        //call SurfaceView.setFlagsEx() to enter the right mode
        int flagsEx = surfaceStereoMode ? (WindowManager.LayoutParams.FLAG_EX_S3D_3D | 
                WindowManager.LayoutParams.FLAG_EX_S3D_SIDE_BY_SIDE) :
                WindowManager.LayoutParams.FLAG_EX_S3D_2D;
        Log.d(TAG, "setSurfaceStereoMode: stereoMode=" + surfaceStereoMode +
                   ", flagsEx=" + Integer.toHexString(flagsEx));
        setFlagsEx(flagsEx, WindowManager.LayoutParams.FLAG_EX_S3D_MASK);
    }

    public GLRootView(Context context) {
        this(context, null);
    }

    public GLRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFlags |= FLAG_INITIALIZED;
        setBackgroundDrawable(null);
        setEGLConfigChooser(mEglConfigChooser);
        setRenderer(this);
        getHolder().setFormat(PixelFormat.RGB_565);

        if (mIsStereoDisplaySupported) {
            InitStereoDisplay();
        }

        // Uncomment this to enable gl error check.
        //setDebugFlags(DEBUG_CHECK_GL_ERROR);
    }

    public GalleryEGLConfigChooser getEGLConfigChooser() {
        return mEglConfigChooser;
    }

    @Override
    public boolean hasStencil() {
        return getEGLConfigChooser().getStencilBits() > 0;
    }

    @Override
    public void registerLaunchedAnimation(CanvasAnimation animation) {
        // Register the newly launched animation so that we can set the start
        // time more precisely. (Usually, it takes much longer for first
        // rendering, so we set the animation start time as the time we
        // complete rendering)
        mAnimations.add(animation);
    }

    @Override
    public void addOnGLIdleListener(OnGLIdleListener listener) {
        synchronized (mIdleListeners) {
            mIdleListeners.addLast(listener);
            mIdleRunner.enable();
        }
    }

    @Override
    public void setContentPane(GLView content) {
        if (mContentView == content) return;
        if (mContentView != null) {
            if (mInDownState) {
                long now = SystemClock.uptimeMillis();
                MotionEvent cancelEvent = MotionEvent.obtain(
                        now, now, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                mContentView.dispatchTouchEvent(cancelEvent);
                cancelEvent.recycle();
                mInDownState = false;
            }
            mContentView.detachFromRoot();
            BasicTexture.yieldAllTextures();
        }
        mContentView = content;
        if (content != null) {
            content.attachToRoot(this);
            requestLayoutContentPane();
        }
    }

    public GLView getContentPane() {
        return mContentView;
    }

    @Override
    public void requestRender() {
        if (DEBUG_INVALIDATE) {
            StackTraceElement e = Thread.currentThread().getStackTrace()[4];
            String caller = e.getFileName() + ":" + e.getLineNumber() + " ";
            Log.d(TAG, "invalidate: " + caller);
        }
        if (mRenderRequested) return;
        mRenderRequested = true;
        super.requestRender();
    }

    @Override
    public void requestLayoutContentPane() {
        mRenderLock.lock();
        try {
            if (mContentView == null || (mFlags & FLAG_NEED_LAYOUT) != 0) return;

            // "View" system will invoke onLayout() for initialization(bug ?), we
            // have to ignore it since the GLThread is not ready yet.
            if ((mFlags & FLAG_INITIALIZED) == 0) return;

            mFlags |= FLAG_NEED_LAYOUT;
            requestRender();
        } finally {
            mRenderLock.unlock();
        }
    }

    private void layoutContentPane() {
        mFlags &= ~FLAG_NEED_LAYOUT;
        int width = getWidth();
        int height = getHeight();
        Log.i(TAG, "layout content pane " + width + "x" + height);
        if (mContentView != null && width != 0 && height != 0) {
            mContentView.layout(0, 0, width, height);
        }
        // Uncomment this to dump the view hierarchy.
        //mContentView.dumpTree("");
    }

    @Override
    protected void onLayout(
            boolean changed, int left, int top, int right, int bottom) {
        if (changed) requestLayoutContentPane();
    }

    /**
     * Called when the context is created, possibly after automatic destruction.
     */
    // This is a GLSurfaceView.Renderer callback
    @Override
    public void onSurfaceCreated(GL10 gl1, EGLConfig config) {
        GL11 gl = (GL11) gl1;
        if (mGL != null) {
            // The GL Object has changed
            Log.i(TAG, "GLObject has changed from " + mGL + " to " + gl);
        }
        mGL = gl;
        mCanvas = new GLCanvasImpl(gl);
        if (!DEBUG_FPS) {
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        } else {
            setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        }
    }

    /**
     * Called when the OpenGL surface is recreated without destroying the
     * context.
     */
    // This is a GLSurfaceView.Renderer callback
    @Override
    public void onSurfaceChanged(GL10 gl1, int width, int height) {
        Log.i(TAG, "onSurfaceChanged: " + width + "x" + height
                + ", gl10: " + gl1.toString());
        Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
        GalleryUtils.setRenderThread();
        GL11 gl = (GL11) gl1;
        Utils.assertTrue(mGL == gl);

        mCanvas.setSize(width, height);

        mClipRect.set(0, 0, width, height);
        mClipRetryCount = 2;
    }

    private void outputFps() {
        long now = System.nanoTime();
        if (mFrameCountingStart == 0) {
            mFrameCountingStart = now;
        } else if ((now - mFrameCountingStart) > 1000000000) {
            Log.d(TAG, "fps: " + (double) mFrameCount
                    * 1000000000 / (now - mFrameCountingStart));
            mFrameCountingStart = now;
            mFrameCount = 0;
        }
        ++mFrameCount;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mRenderLock.lock();
        try {
            //added for stereo photo display
            boolean stereoMode = mStereoMode;
            boolean updateStereoMode = false;
            //first, we should check the correct stereo mode
            if (mIsStereoDisplaySupported &&
                mSurfaceStereoMode != stereoMode) {
                mSurfaceStereoMode = stereoMode;
                //then we need to set the stereo mode of surface
                updateStereoMode = true;
                //we delay the actual updating process until drawing finished,
                //this is workaround to make screen flash less frequently
            }

            //if stereo photo should be display
            //we have to draw twice! once left, and once right
            //now, left
            if (mIsStereoDisplaySupported && mSurfaceStereoMode) {
                //set new viewport and projection matrix on left
                mCanvas.setSize(0, 0, getWidth()/2, getHeight());
                mClipRect.set(0, 0, getWidth()/2, getHeight());
                //set current pass parameters
                mCurrSP = mStereoPass[STEREO_DRAW_LEFT_PASS];
                //set current pass id
                mStereoPassId = 
                    MediatekFeature.STEREO_DISPLAY_LEFT_PASS;
            }
            onDrawFrameLocked(gl);
            //now, right
            if (mIsStereoDisplaySupported && mSurfaceStereoMode) {
                //set new viewport and projection matrix on right
                mCanvas.setSize(getWidth()/2, 0, getWidth()/2,
                                getHeight());
                mClipRect.set(getWidth()/2, 0, getWidth(), getHeight());
                //set current pass parameters
                mCurrSP = mStereoPass[STEREO_DRAW_RIGHT_PASS];
                //set current pass id
                mStereoPassId = 
                    MediatekFeature.STEREO_DISPLAY_RIGHT_PASS;
                //draw right screen content
                onDrawFrameLocked(gl);
                //reset canvas mode to normal mode
                mCanvas.setSize(getWidth(), getHeight());
                //reset glScissor clip
                mClipRect.set(0, 0, getWidth(), getHeight());
            }
            //finally, update stereo mode if needed
            if (mIsStereoDisplaySupported && updateStereoMode) {
                setSurfaceStereoMode(mSurfaceStereoMode);
            }
        } finally {
            mRenderLock.unlock();
        }
        long end = SystemClock.uptimeMillis();

        if (mLastDrawFinishTime != 0) {
            long wait = mLastDrawFinishTime + TARGET_FRAME_TIME - end;
            if (wait > 0) {
                SystemClock.sleep(wait);
            }
        }
        mLastDrawFinishTime = SystemClock.uptimeMillis();
    }

    private void onDrawFrameLocked(GL10 gl) {
        if (DEBUG_FPS) outputFps();

        // release the unbound textures and deleted buffers.
        mCanvas.deleteRecycledResources();

        // reset texture upload limit
        UploadedTexture.resetUploadLimit();

        mRenderRequested = false;

        if ((mFlags & FLAG_NEED_LAYOUT) != 0) layoutContentPane();

        // OpenGL seems having a bug causing us not being able to reset the
        // scissor box in "onSurfaceChanged()". We have to do it in the second
        // onDrawFrame().
        if (mClipRetryCount > 0) {
            --mClipRetryCount;
            Rect clip = mClipRect;
            gl.glScissor(clip.left, clip.top, clip.width(), clip.height());
        }

        if (mIsStereoDisplaySupported && mSurfaceStereoMode) {
            mCanvas.translate((float)getWidth() * 
                              mCurrSP.transf, 0.0f, 0.0f);
            mCanvas.scale(mCurrSP.scale,1.0f,1.0f);
        }

        mCanvas.setCurrentAnimationTimeMillis(SystemClock.uptimeMillis());
        if (mContentView != null) {
           mContentView.render(mCanvas);
        }

        if (!mAnimations.isEmpty()) {
            long now = SystemClock.uptimeMillis();
            for (int i = 0, n = mAnimations.size(); i < n; i++) {
                mAnimations.get(i).setStartTime(now);
            }
            mAnimations.clear();
        }

        if (UploadedTexture.uploadLimitReached()) {
            requestRender();
        }

        synchronized (mIdleListeners) {
            if (!mRenderRequested && !mIdleListeners.isEmpty()) {
                mIdleRunner.enable();
            }
        }

        if (DEBUG_INVALIDATE) {
            mCanvas.fillRect(10, 10, 5, 5, mInvalidateColor);
            mInvalidateColor = ~mInvalidateColor;
        }

        if (DEBUG_DRAWING_STAT) {
            mCanvas.dumpStatisticsAndClear();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_UP) {
            mInDownState = false;
        } else if (!mInDownState && action != MotionEvent.ACTION_DOWN) {
            return false;
        }
        mRenderLock.lock();
        try {
            // If this has been detached from root, we don't need to handle event
            boolean handled = mContentView != null
                    && mContentView.dispatchTouchEvent(event);
            if (action == MotionEvent.ACTION_DOWN && handled) {
                mInDownState = true;
            }
            return handled;
        } finally {
            mRenderLock.unlock();
        }
    }

    public DisplayMetrics getDisplayMetrics() {
        if (mDisplayMetrics == null) {
            mDisplayMetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager()
                    .getDefaultDisplay().getMetrics(mDisplayMetrics);
        }
        return mDisplayMetrics;
    }

    public GLCanvas getCanvas() {
        return mCanvas;
    }

    private class IdleRunner implements Runnable {
        // true if the idle runner is in the queue
        private boolean mActive = false;

        @Override
        public void run() {
            OnGLIdleListener listener;
            synchronized (mIdleListeners) {
                mActive = false;
                if (mRenderRequested) return;
                if (mIdleListeners.isEmpty()) return;
                listener = mIdleListeners.removeFirst();
            }
            mRenderLock.lock();
            try {
                if (!listener.onGLIdle(GLRootView.this, mCanvas)) return;
            } finally {
                mRenderLock.unlock();
            }
            synchronized (mIdleListeners) {
                mIdleListeners.addLast(listener);
                enable();
            }
        }

        public void enable() {
            // Who gets the flag can add it to the queue
            if (mActive) return;
            mActive = true;
            queueEvent(this);
        }
    }

    @Override
    public void lockRenderThread() {
        mRenderLock.lock();
    }

    @Override
    public void unlockRenderThread() {
        mRenderLock.unlock();
    }
}
