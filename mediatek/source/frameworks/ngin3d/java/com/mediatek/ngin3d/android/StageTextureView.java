package com.mediatek.ngin3d.android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Ngin3d;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Text;
import com.mediatek.ngin3d.presentation.PresentationEngine;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * A view that can display Ngin3d stage contents with TextureView.
 */
public class StageTextureView extends GLTextureView implements GLTextureView.Renderer {
    private static final String TAG = "StageTextureView";
    private static final float STEREO3D_EYE_DISTANCE = 200;
    private Text mTextFPS;
    private Resources mResources;
    private String mCacheDir;
    protected Stage mStage;

    public StageTextureView(Context context) {
        this(context, (Stage) null);
    }

    public StageTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, new Stage(), false);
    }

    public StageTextureView(Context context, AttributeSet attrs, boolean antiAlias) {
        this(context, attrs, new Stage(), antiAlias);
    }

    /**
     * Initialize this object with android context and Stage class object.
     *
     * @param context android context
     * @param stage   Stage class object
     */
    public StageTextureView(Context context, Stage stage) {
        this(context, null, stage, false);
    }


    /**
     * Initialize this object with android context and Stage class object.
     *
     * @param context android context
     * @param stage   Stage class object
     * @param antiAlias   enable anti-aliasing if true
     */
    public StageTextureView(Context context, Stage stage, boolean antiAlias) {
        this(context, null, stage, antiAlias);
    }

    private StageTextureView(Context context, AttributeSet attrs, Stage stage, boolean antiAlias) {
        super(context, attrs);

        mResources = context.getResources();
        mCacheDir = context.getCacheDir().getAbsolutePath();

        if (stage == null) {
            mStage = new Stage(AndroidUiHandler.create());
        } else {
            mStage = stage;
        }

        setEGLContextClientVersion(2);
        if (antiAlias) {
            setEGLConfigChooser(new MultisampleConfigChooser());
        }

        setRenderer(this);

        // Add text to show FPS
        if (mShowFPS) {
            setRenderMode(RENDERMODE_CONTINUOUSLY);
            setupFPSText();
        } else {
            setRenderMode(RENDERMODE_WHEN_DIRTY);
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause from activity");
        // pause rendering and animations
        pauseRendering();

        // Uninitialize presentation engine after GLSurface paused the rendering thread.
        if (mPresentationEngine != null) {

            // Uninitialize presentation engine before GLSurface paused the rendering thread.
            FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {
                public Void call() {
                    mPresentationEngine.uninitialize();
                    mPresentationEngine = null;
                    return null;
                }
            });
            runInGLThread(task);
        }

        super.onPause();
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow");

            // Uninitialize presentation engine after GLSurface paused the rendering thread.
        if (mPresentationEngine != null) {

            // Uninitialize presentation engine before the rendering thread be destroyed.
            FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {
                public Void call() {
                    mPresentationEngine.uninitialize();
                    mPresentationEngine = null;
                    return null;
                }
            });
            runInGLThread(task);
        }

        super.onDetachedFromWindow();
    }

    public void onResume() {
        Log.d(TAG, "onResume from activity");

        super.onResume();
        // resume rendering and animations
        resumeRendering();
    }

    /**
     * Get the original stage object of this class
     *
     * @return original stage object
     */
    public final Stage getStage() {
        return mStage;
    }

    /**
     * Get the presentation engine of this object
     *
     * @return presentation engine
     */
    public PresentationEngine getPresentationEngine() {
        return mPresentationEngine;
    }

    /**
     * Get the the screen shot of current render frame in StageTextureView
     *
     * @return A Bitmap object representing the rendering texture
     */
    public Bitmap getScreenShot() {
        FutureTask<Object> task = new FutureTask<Object>(new Callable<Object>() {
            public Object call() {
                return mPresentationEngine.getScreenShot();
            }
        });

        runInGLThread(task);
        Object obj = null;
        try {
            obj = task.get();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (ExecutionException ex) {
            ex.printStackTrace();
        }

        if (obj instanceof Bitmap) {
            return (Bitmap) obj;
        }
        return null;
    }

    public double getFPS() {
        return mPresentationEngine.getFPS();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        Log.d(TAG, "onVisibilityChanged, visibility is:" + visibility);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,  h, oldw, oldh);
        Log.d(TAG, "onSizeChanged, w: " + w + " h: " + h + " oldw: " + oldw + " oldh: " + oldh);
        if (mShowFPS) {
            mTextFPS.setPosition(new Point(w, 0));  // show it at right-bottom corner
        } else {
            requestRender();
        }
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");

        // Increase the priority of the render thread
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
        boolean enableNDB = SystemProperties.getBoolean("ngin3d.enableNDB", false);
        mPresentationEngine = Ngin3d.createPresentationEngine(mStage);
        mPresentationEngine.setRenderCallback(new PresentationEngine.RenderCallback() {
            public void requestRender() {
                StageTextureView.this.requestRender();
            }
        });
        mPresentationEngine.initialize(getWidth(), getHeight(), mResources, mCacheDir, enableNDB);

        // if there are any paused animation, resume it
        resumeRendering();

        synchronized (mSurfaceReadyLock) {
            mSurfaceReadyLock.notifyAll();
        }

        if (Ngin3d.DEBUG) {
            mPresentationEngine.dump();
        }
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.v(TAG, "onSurfaceChanged(width = " + width + ", height = " + height + ")");

        /** The actors' position might be normalized values and the real position values depend on Width/Height.
        *  We need make position property dirty to recalculate correct position of actors after surface changed
        */
        mStage.touchProperty("position");

        mPresentationEngine.resize(width, height);
        if (mShowFPS) {
            mTextFPS.setPosition(new Point(width, 0));  // show it at right-bottom corner
        } else {
            requestRender();
        }
    }

    public void onDrawFrame(GL10 gl) {
        if (mFirstOnDrawFrameTime == INVALID_TIME) {
            mFirstOnDrawFrameTime = SystemClock.uptimeMillis();
            Log.d(TAG, "onDrawFrame() invoked @" + mFirstOnDrawFrameTime);
        }

        if (mShowFPS) {
            mTextFPS.setText(String.format("FPS: %.2f", mPresentationEngine.getFPS()));
            mPresentationEngine.render();
        } else {
            if (mPresentationEngine.render()) {
                requestRender();
            }
        }
    }

    private void setupFPSText() {
        mTextFPS = new Text("");
        mTextFPS.setAnchorPoint(new Point(1.f, 0.f));
        mTextFPS.setPosition(new Point(0, 0));
        mTextFPS.setTextColor(Color.YELLOW);
        mStage.add(mTextFPS);
    }

    /**
     * This method can change the cache path where binary shaders are stored.
     * Must invoke the method in the constructor to apply the new cache directory or
     * default cache path (application's cache directory) will be used.
     *
     * @param cacheDir cache directory that binary
     */
    public void setCacheDir(String cacheDir) {
        mCacheDir = cacheDir;
    }

    /**
     * Pause the rendering
     */
    public void pauseRendering() {
        if (mShowFPS) {
            setRenderMode(RENDERMODE_WHEN_DIRTY);
        }

        if (mPresentationEngine != null) {
            mPresentationEngine.pauseRendering();
        }
    }

    /**
     * Resume the rendering
     */
    public void resumeRendering() {
        // adjust all timelines by current tick time
        if (mPresentationEngine != null) {
            mPresentationEngine.resumeRendering();

            requestRender();
            if (mShowFPS) {
                setRenderMode(RENDERMODE_CONTINUOUSLY);
            }
        }
    }

    public Boolean isSurfaceReady() {
        return mPresentationEngine.isReady();
    }

    private final Object mSurfaceReadyLock = new Object();

    public void waitSurfaceReady() {
        synchronized (mSurfaceReadyLock) {
            while (!isSurfaceReady()) {
                try {
                    mSurfaceReadyLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
