package com.mediatek.world3d;

import android.content.Context;
import android.view.WindowManager;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageView;

import javax.microedition.khronos.opengles.GL10;

public class WorldStageView extends StageView {

    public WorldStageView(Context ctx, Stage stage) {
        super(ctx, stage);
    }

    public void activateStereo3D(boolean activated) {
        int EYE_DISTANCE = 40;
        enableStereoscopic3D(activated, EYE_DISTANCE);
        int flags = activated ? 
                    WindowManager.LayoutParams.FLAG_EX_S3D_3D | 
                    WindowManager.LayoutParams.FLAG_EX_S3D_SIDE_BY_SIDE :
                    WindowManager.LayoutParams.FLAG_EX_S3D_2D;

        setFlagsEx(flags, WindowManager.LayoutParams.FLAG_EX_S3D_MASK);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        final int cameraZDistance = -2111;
        mStage.setCamera(
                new Point(width/2 , height/2, cameraZDistance),
                new Point(width/2 , height/2, 0) );
    }
}