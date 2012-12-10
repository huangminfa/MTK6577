package com.mediatek.ngin3d.android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import com.mediatek.glui.GLRootView;
import com.mediatek.glui.GLView;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Ngin3d;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Text;
import com.mediatek.ngin3d.presentation.PresentationEngine;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * A view that can display Ngin3d stage contents.
 */
public class StageView extends GLRootView {

    private static final String TAG = "StageView";
    private static final float STEREO3D_EYE_DISTANCE = 200;

    protected final Stage mStage;
    private Text mTextFPS;
    private final PresentationEngine mPresentationEngine;
    private Resources mResources;
    private String mCacheDir;
    private boolean mShowFPS;

    public StageView(Context context) {
        this(context, (Stage) null);
    }

    public StageView(Context context, AttributeSet attrs) {
        this(context, attrs, new Stage(), false);
    }

    public StageView(Context context, AttributeSet attrs, boolean antiAlias) {
        this(context, attrs, new Stage(), antiAlias);
    }

    /**
     * Initialize this object with android context and Stage class object.
     *
     * @param context android context
     * @param stage   Stage class object
     */
    public StageView(Context context, Stage stage) {
        this(context, null, stage, false);
    }

    /**
     * Initialize this object with android context and Stage class object.
     *
     * @param context android context
     * @param stage   Stage class object
     * @param antiAlias   enable anti-aliasing if true
     */
    public StageView(Context context, Stage stage, boolean antiAlias) {
        this(context, null, stage, antiAlias);
    }

    private StageView(Context context, AttributeSet attrs, Stage stage, boolean antiAlias) {
        super(context, attrs, antiAlias);

        if (stage == null) {
            mStage = new Stage(AndroidUiHandler.create());
        } else {
            mStage = stage;
        }

        mResources = context.getResources();
        mPresentationEngine = Ngin3d.createPresentationEngine(mStage);
        mPresentationEngine.setRenderCallback(new PresentationEngine.RenderCallback() {
            public void requestRender() {
                StageView.this.requestRender();
            }
        });

        mCacheDir = context.getCacheDir().getAbsolutePath();
        mShowFPS = SystemProperties.getBoolean("ngin3d.showfps", false);

        // Add text to show FPS
        if (mShowFPS) {
            setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            setupFPSText();
        } else {
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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
     * Get the original stage object of this class
     *
     * @return original stage object
     */
    public final Stage getStage() {
        return mStage;
    }


    /**
     * Get FPS
     *
     * @return FPS value
     */
    public double getFPS() {
        return mPresentationEngine.getFPS();
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
     * Enable/disable stereoscopic 3d display mode
     *
     * @param enable true if you want to enable stereo 3d display mode
     */
    public void enableStereoscopic3D(boolean enable) {
        mStage.setStereo3D(enable, STEREO3D_EYE_DISTANCE);
    }

    /**
     * Enable/disable stereoscopic 3d display mode
     *
     * @param enable       true if you want to enable stereo 3d display mode
     * @param eyesDistance the distance between two eyes
     */
    public void enableStereoscopic3D(boolean enable, float eyesDistance) {
        mStage.setStereo3D(enable, eyesDistance);
    }

    /**
     * Get the the screen shot of current render frame
     *
     * @return A Bitmap object representing the render frame
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (obj instanceof Bitmap) {
            return (Bitmap) obj;
        }
        return null;
    }

    private double mLastFps = -1;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        Log.v(TAG, "onSurfaceCreated()");

        // if there are any paused animation, resume it
        resumeRendering();

        setContentPane(new GLView() {
            @Override
            protected void renderBackground(GLRootView view, GL11 gl) {
                // Update FPS
                if (mShowFPS) {
                    double newFps = Math.floor(mPresentationEngine.getFPS() * 100) / 100;
                    if (Double.compare(newFps, mLastFps) != 0) {
                        mLastFps = newFps;
                        mTextFPS.setText(String.format("FPS: %.2f", newFps));
                    }
                    mPresentationEngine.render();
                } else {
                    if (mPresentationEngine.render()) {
                        requestRender();
                    }
                }
            }
        });

        final int w = getWidth();
        final int h = getHeight();
        boolean enableNDB = SystemProperties.getBoolean("ngin3d.enableNDB", false);
        mPresentationEngine.initialize(w, h, mResources, mCacheDir, enableNDB);

        synchronized (mSurfaceReadyLock) {
            mSurfaceReadyLock.notifyAll();
        }

        if (Ngin3d.DEBUG) {
            mPresentationEngine.dump();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
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

    @Override
    public void onPause() {

        // pause rendering and animations
        pauseRendering();
        // Uninitialize presentation engine after GLSurface paused the rendering thread.
        if (mPresentationEngine != null) {

            // Uninitialize presentation engine before GLSurface paused the rendering thread.
            FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {
                public Void call() {
                    mPresentationEngine.uninitialize();
                    return null;
                }
            });
            runInGLThread(task);
        }
        super.onPause();
    }

    /**
     * Pause the rendering
     */
    public void pauseRendering() {
        if (mShowFPS) {
            setRenderMode(RENDERMODE_WHEN_DIRTY);
        }

        mPresentationEngine.pauseRendering();
    }

    /**
     * Resume the rendering
     */
    public void resumeRendering() {
        // adjust all timelines by current tick time
        mPresentationEngine.resumeRendering();

        if (mShowFPS) {
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        } else {
            requestRender();
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
