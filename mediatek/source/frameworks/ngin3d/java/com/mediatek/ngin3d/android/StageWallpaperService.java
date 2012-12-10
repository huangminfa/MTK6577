package com.mediatek.ngin3d.android;

import android.os.SystemProperties;
import android.util.Log;
import com.mediatek.ngin3d.Ngin3d;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.presentation.PresentationEngine;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class StageWallpaperService extends GLWallpaperService {
    private static final boolean DEBUG_WALLPAPER = true;
    private static final String TAG = "StageWallpaper";

    public StageWallpaperService() {
        super();
    }

    public class StageRenderer implements Renderer {
        private PresentationEngine mPresentationEngine;
        private final PresentationEngine.RenderCallback mRenderCallback;
        private final Stage mStage;

        public StageRenderer(Stage stage) {
            this(stage, null);
        }

        public StageRenderer(Stage stage, PresentationEngine.RenderCallback renderCallback) {
            super();
            mStage = stage;
            mRenderCallback = renderCallback;
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            if (DEBUG_WALLPAPER) {
                Log.d(TAG, "onSurfaceCreated, id: " + this);
            }
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            if (DEBUG_WALLPAPER) {
                Log.d(TAG, "onSurfaceChanged - width: " + width + "height: " + height + " id: " + this);
            }
            if (mPresentationEngine == null) {
                String cacheDir = getCacheDir().getAbsolutePath();
                boolean enableNDB = SystemProperties.getBoolean("ngin3d.enableNDB", false);
                mPresentationEngine = Ngin3d.createPresentationEngine(mStage);
                mPresentationEngine.initialize(width, height, getResources(), cacheDir, enableNDB);
                mPresentationEngine.setRenderCallback(mRenderCallback);
            } else {
                /** The actors' position might be normalized values and the real position values depend on Width/Height.
                 *  We need make position property dirty to recalculate correct position of actors after surface changed
                 */
                mStage.touchProperty("position");

                // Elsewhere, the viewport is set by the service rather than the
                // presentation engine, for consistency set the glViewport here.
                gl.glViewport(0, 0, width, height);

                // Inform the presentation engine of the change
                mPresentationEngine.resize(width, height);
            }
            mRenderCallback.requestRender();

        }

        public void onDrawFrame(GL10 gl) {
            if (DEBUG_WALLPAPER) {
                Log.d(TAG, "onDrawFrame , id: " + this);
            }
            mPresentationEngine.render();
        }

        public void onDestroy() {
            if (DEBUG_WALLPAPER) {
                Log.d(TAG, "onDestroy, id: " + this);
            }
            mPresentationEngine.uninitialize();
        }
    }

    public class StageEngine extends GLEngine {
        private final Stage mStage;
        private StageRenderer mRenderer;

        public StageEngine(Stage stage) {
            if (stage == null) {
                throw new IllegalArgumentException("Stage can not be null");
            }
            mStage = stage;
        }

        public StageEngine() {
            super();

            mStage = new Stage(AndroidUiHandler.create());
            boolean showFPS =  SystemProperties.getBoolean("ngin3d.showfps", false);
            if (showFPS) {
                setRenderer(new StageRenderer(mStage));
                setRenderMode(RENDERMODE_CONTINUOUSLY);
            } else {
                PresentationEngine.RenderCallback mRenderCallback = new PresentationEngine.RenderCallback() {
                    public void requestRender() {
                        StageEngine.this.requestRender();
                    }
                };
                setRenderer(new StageRenderer(mStage, mRenderCallback));
                setRenderMode(RENDERMODE_WHEN_DIRTY);
            }
        }

        @Override
        public void onDestroy() {
            if (DEBUG_WALLPAPER) {
                Log.d(TAG, "StageEngine onDestroy, id: " + this);
            }
            super.onDestroy();
            if (mRenderer != null) {
                mRenderer.onDestroy();
                mRenderer = null;
            }
        }

        @Override
        public void onPause() {
            if (DEBUG_WALLPAPER) {
                Log.d(TAG, "StageEngine onPause, id: " + this);
            }
            super.onPause();
        }

        @Override
        public void onResume() {
            if (DEBUG_WALLPAPER) {
                Log.d(TAG, "StageEngine onResume, id: " + this);
            }
            super.onResume();
        }

        public Stage getStage() {
            return mStage;
        }

        public final void setRenderer(StageRenderer renderer) {
            super.setRenderer(renderer);
            mRenderer = renderer;

        }
    }
}
