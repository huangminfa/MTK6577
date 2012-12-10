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

package com.mediatek.glui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.ConditionVariable;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import com.mediatek.ngin3d.android.MultisampleConfigChooser;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class GLRootView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static final String TAG = "GLRootView";

    private static final int VERTEX_BUFFER_SIZE = 8;

    private static final int FLAG_INITIALIZED = 1;
    private static final int FLAG_NEED_LAYOUT = 2;

    private static float sPixelDensity = -1f;

    private GL11 mGL;
    private GLView mContentView;
    private DisplayMetrics mDisplayMetrics;

    private final ArrayList<Animation> mAnimations = new ArrayList<Animation>();

    private final Stack<Transformation> mFreeTransform =
        new Stack<Transformation>();

    private final Transformation mTransformation = new Transformation();
    private final Stack<Transformation> mTransformStack =
        new Stack<Transformation>();

    private float mLastAlpha = mTransformation.getAlpha();

    private final float[] mMatrixValues = new float[16];

    private final float[] mCoordBuffer = new float[8];
    private final float[] mPointBuffer = new float[4];

    private ByteBuffer mTexCoordBuffer;

    private int mFlags = FLAG_NEED_LAYOUT;
    private long mAnimationTime;

    private Thread mGLThread;

    private boolean mIsQueueActive = true;

    // TODO: move this part (handler) into GLSurfaceView
    private final Looper mLooper;

    public GLRootView(Context context) {
        this(context, null, false);
    }

    public GLRootView(Context context, AttributeSet attrs, boolean antiAlias) {
        super(context, attrs);
        initialize(antiAlias);
        mLooper = Looper.getMainLooper();
    }

    void registerLaunchedAnimation(Animation animation) {
        // Register the newly launched animation so that we can set the start
        // time more precisely. (Usually, it takes much longer for the first
        // rendering, so we set the animation start time as the time we
        // complete rendering)
        mAnimations.add(animation);
    }

    public long currentAnimationTimeMillis() {
        return mAnimationTime;
    }

    public static float dpToPixel(Context context, float dp) {
        synchronized (GLRootView.class) {
            if (sPixelDensity < 0) {
                DisplayMetrics metrics = new DisplayMetrics();
                ((Activity) context).getWindowManager()
                    .getDefaultDisplay().getMetrics(metrics);
                sPixelDensity =  metrics.density;
            }
            return sPixelDensity * dp;
        }
    }

    public static int dpToPixel(Context context, int dp) {
        return (int)(dpToPixel(context, (float) dp) + .5f);
    }

    public Transformation obtainTransformation() {
        if (!mFreeTransform.isEmpty()) {
            Transformation t = mFreeTransform.pop();
            t.clear();
            return t;
        }
        return new Transformation();
    }

    public void freeTransformation(Transformation freeTransformation) {
        mFreeTransform.push(freeTransformation);
    }

    public Transformation getTransformation() {
        return mTransformation;
    }

    public Transformation pushTransform() {
        Transformation trans = obtainTransformation();
        trans.set(mTransformation);
        mTransformStack.push(trans);
        return mTransformation;
    }

    public void popTransform() {
        Transformation trans = mTransformStack.pop();
        mTransformation.set(trans);
        freeTransformation(trans);
    }

    public void runInGLThread(Runnable runnable) {
        if (Thread.currentThread() == mGLThread) {
            runnable.run();
        } else {
            queueEvent(runnable);
        }
    }

    private void initialize(boolean antiAlias) {
        mFlags |= FLAG_INITIALIZED;
        setEGLContextClientVersion(2);             // use OpenGLES 2.0
        if (antiAlias) {
            setEGLConfigChooser(new MultisampleConfigChooser());
        } else {
            setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        }

        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);

        setRenderer(this);

        ByteBuffer vertexBuffer = ByteBuffer
                        .allocateDirect(VERTEX_BUFFER_SIZE * Float.SIZE / Byte.SIZE)
                        .order(ByteOrder.nativeOrder());
        vertexBuffer.asFloatBuffer()
            .put(new float[] {0, 0, 1, 0, 0, 1, 1, 1})
            .position(0);
        mTexCoordBuffer = ByteBuffer
                          .allocateDirect(VERTEX_BUFFER_SIZE * Float.SIZE / Byte.SIZE)
                          .order(ByteOrder.nativeOrder());
    }

    public void setContentPane(GLView content) {
        mContentView = content;
        content.onAttachToRoot(this);

        // no parent for the content pane
        content.onAddToParent(null);
        requestLayoutContentPane();
    }

    public GLView getContentPane() {
        return mContentView;
    }

    void handleLowMemory() {
        // TODO: delete texture from GL
    }

    public void requestLayoutContentPane() {
        synchronized (this) {
            if (mContentView == null || (mFlags & FLAG_NEED_LAYOUT) != 0) {
                return;
            }

            // "View" system will invoke onLayout() for initialization(bug ?), we
            // have to ignore it since the GLThread is not ready yet.
            if ((mFlags & FLAG_INITIALIZED) == 0) {
                return;
            }

            mFlags |= FLAG_NEED_LAYOUT;
            requestRender();
        }
    }

    private void layoutContentPane() {
        synchronized (this) {
            mFlags &= ~FLAG_NEED_LAYOUT;
            int width = getWidth();
            int height = getHeight();
            Log.v(TAG, "layout content pane " + width + "x" + height);
            if (mContentView != null && width != 0 && height != 0) {
                mContentView.layout(0, 0, width, height);
            }
        }

    }

    @Override
    protected void onLayout(
        boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            requestLayoutContentPane();
        }
    }

    /**
     * Called when the context is created, possibly after automatic destruction.
     */
    // This is a GLSurfaceView.Renderer callback
    public void onSurfaceCreated(GL10 gl1, EGLConfig config) {
        GL11 gl = (GL11) gl1;
        if (mGL != null) {
            // The GL Object has changed
            Log.i(TAG, "GLObject has changed from " + mGL + " to " + gl);
        }
        mGL = gl;

        // Increase the priority of the render thread
        Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
        mGLThread = Thread.currentThread();

        if (mContentView != null) {
            mContentView.onSurfaceCreated();
        }
    }

    /**
     * Called when the OpenGL surface is recreated without destroying the
     * context.
     */
    // This is a GLSurfaceView.Renderer callback
    public void onSurfaceChanged(GL10 gl1, int width, int height) {
        Log.v(TAG, "onSurfaceChanged: " + width + "x" + height
              + ", gl10: " + gl1.toString());
        GL11 gl = (GL11) gl1;
        mGL = gl;

        gl.glViewport(0, 0, width, height);

    }

    private void setAlphaValue(float alpha) {
        if (mLastAlpha == alpha) {
            return;
        }

        GL11 gl = mGL;
        mLastAlpha = alpha;
        if (alpha >= 0.95f) {
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV,
                         GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
        } else {
            gl.glTexEnvf(GL11.GL_TEXTURE_ENV,
                         GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            gl.glColor4f(alpha, alpha, alpha, alpha);
        }
    }

    public void drawRect(int x, int y, int width, int height) {
        float[] matrix = mMatrixValues;
        mTransformation.getMatrix().getValues(matrix);
        drawRect(x, y, width, height, matrix, mTransformation.getAlpha());
    }

    private void drawRect(
        int x, int y, int width, int height, float[] matrix, float alpha) {
        GL11 gl = mGL;
        gl.glPushMatrix();
        setAlphaValue(alpha);
        gl.glMultMatrixf(toGLMatrix(matrix), 0);
        gl.glTranslatef(x, y, 0);
        gl.glScalef(width, height, 1);
        gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
        gl.glPopMatrix();
    }

    public void drawRect(int x, int y, int width, int height, float alpha) {
        float[] matrix = mMatrixValues;
        mTransformation.getMatrix().getValues(matrix);
        drawRect(x, y, width, height, matrix, alpha);
    }

    private float[] mapPoints(Matrix matrix, int x1, int y1, int x2, int y2) {
        float[] point = mPointBuffer;
        point[0] = x1;
        point[1] = y1;
        point[2] = x2;
        point[3] = y2;
        matrix.mapPoints(point);
        return point;
    }

    public void clipRect(int x0, int y0, int width0, int height0) {
        float[] point = mapPoints(mTransformation.getMatrix(), x0, y0 + height0, x0 + width0, y0);

        int x, y, width, height;
        // mMatrix could be a rotation matrix. In this case, we need to find
        // the boundaries after rotation. (only handle 90 * n degrees)
        if (point[0] > point[2]) {
            x = (int) point[2];
            width = (int) point[0] - x;
        } else {
            x = (int) point[0];
            width = (int) point[2] - x;
        }
        if (point[1] > point[3]) {
            y = (int) point[3];
            height = (int) point[1] - y;
        } else {
            y = (int) point[1];
            height = (int) point[3] - y;
        }
        mGL.glScissor(x, y, width, height);
    }

    public void clearClip() {
        mGL.glScissor(0, 0, getWidth(), getHeight());
    }

    private static float[] toGLMatrix(float[] v) {
        v[15] = v[8];
        v[13] = v[5];
        v[5] = v[4];
        v[4] = v[1];
        v[12] = v[2];
        v[1] = v[3];
        v[3] = v[6];
        v[2] = 0;
        v[6] = 0;
        v[8] = 0;
        v[9] = 0;
        v[10] = 1;
        return v;
    }

    public void drawTexture(
        Texture texture, int x0, int y0, int width0, int height0, float alpha) {

        if (!texture.bind(this, mGL)) {
            throw new RuntimeException("cannot bind" + texture.toString());
        }
        if (width0 <= 0 || height0 <= 0) return;

        Matrix matrix = mTransformation.getMatrix();
        matrix.getValues(mMatrixValues);

        // Test whether it has been rotated or flipped, if so, glDrawTexiOES
        // won't work
        if (isMatrixRotatedOrFlipped(mMatrixValues)) {
            texture.getTextureCoords(mCoordBuffer, 0);
            mTexCoordBuffer.asFloatBuffer().put(mCoordBuffer).position(0);
            drawRect(x0, y0, width0, height0, mMatrixValues, alpha);
        } else {
            // draw the rect from bottom-left to top-right
            float[] points = mapPoints(matrix, x0, y0 + height0, x0 + width0, y0);
            int x = (int) points[0];
            int y = (int) points[1];
            int width = (int) points[2] - x;
            int height = (int) points[3] - y;
            if (width > 0 && height > 0) {
                setAlphaValue(alpha);
                ((GL11Ext) mGL).glDrawTexiOES(x, y, 0, width, height);
            }
        }
    }

    private static boolean isMatrixRotatedOrFlipped(float[] matrix) {
        return matrix[Matrix.MSKEW_X] != 0 || matrix[Matrix.MSKEW_Y] != 0
               || matrix[Matrix.MSCALE_X] < 0 || matrix[Matrix.MSCALE_Y] > 0;
    }

    public void drawTexture(
        Texture texture, int x, int y, int width, int height) {
        drawTexture(texture, x, y, width, height, mTransformation.getAlpha());
    }

    public static final long INVALID_TIME = -1;
    long mFirstOnDrawFrameTime = INVALID_TIME;

    public long getFirstOnDrawTime() {
        return mFirstOnDrawFrameTime;
    }

    // This is a GLSurfaceView.Renderer callback
    public void onDrawFrame(GL10 gl) {

        if (mFirstOnDrawFrameTime == INVALID_TIME) {
            mFirstOnDrawFrameTime = SystemClock.uptimeMillis();
            Log.d(TAG, "onDrawFrame() invoked @" + mFirstOnDrawFrameTime);
        }

        if ((mFlags & FLAG_NEED_LAYOUT) != 0) layoutContentPane();

        clearClip();
        mAnimationTime = SystemClock.uptimeMillis();
        if (mContentView != null) {
            mContentView.render(GLRootView.this, (GL11) gl);
        }
        long now = SystemClock.uptimeMillis();

        int size = mAnimations.size(); // Use indexing rather than iterator to prevent frequent GC
        for (int i = 0; i < size; ++i) {
            mAnimations.get(i).setStartTime(now);
        }
        mAnimations.clear();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (super.dispatchTouchEvent(event)) {
            return true;
        }
        // If this has been detached from root, we don't need to handle event
        if (!mIsQueueActive) return false;

        // dispatch the touch event in GLRootView first (blocking)
        // and return the result.
        // if it handled, it will callback to app in GL thread
        // app have to put it to UI thread and handle it later.
        FutureTask<Boolean> task = new FutureTask<Boolean>(
            new TouchEventHandler(event));
        queueEventOrThrowException(task);
        try {
            return task.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class TouchEventHandler implements Callable<Boolean> {

        private final MotionEvent mEvent;

        public TouchEventHandler(MotionEvent event) {
            mEvent = event;
        }

        public Boolean call() throws Exception {
            if (mContentView == null) return false;
            return mContentView.dispatchTouchEvent(mEvent);
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

    public void queueEventOrThrowException(Runnable runnable) {
        synchronized (this) {
            if (!mIsQueueActive) {
                throw new IllegalStateException("GLThread has exit");
            }
            super.queueEvent(runnable);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        final ConditionVariable var = new ConditionVariable();
        synchronized (this) {
            mIsQueueActive = false;
            queueEvent(new Runnable() {
                public void run() {
                    var.open();
                }
            });
        }

        // Make sure all the runnables in the event queue is executed.
        var.block();
        super.onDetachedFromWindow();
    }

    protected Looper getTimerLooper() {
        return mLooper;
    }
}
