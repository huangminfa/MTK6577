package com.mediatek.ngin3d.tests;

import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageWallpaperService;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyWallpaperService extends StageWallpaperService {
    public MyWallpaperService() {

    }

    class MyRenderer extends StageRenderer {
        MyRenderer(Stage stage) {
            super(stage);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            super.onSurfaceCreated(gl, config);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            super.onSurfaceChanged(gl, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            super.onDrawFrame(gl);
        }
    }

    class MyEngine extends StageEngine {
        MyEngine() {
            super();
            Stage stage = getStage();
            stage.setStereo3D(true, 100);
            stage.setBackgroundColor(Color.TRANSPARENT);
            final Image photo = Image.createFromResource(getResources(), R.drawable.wp_common09);
            photo.setPosition(new Point(240, 400, 0));
            stage.add(photo);
        }
    }

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }
}
