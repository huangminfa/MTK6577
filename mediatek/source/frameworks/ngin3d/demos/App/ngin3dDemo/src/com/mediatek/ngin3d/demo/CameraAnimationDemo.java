package com.mediatek.ngin3d.demo;

// Imports for Stereo3D version
//import android.content.Context;
//import android.view.WindowManager;
//import com.mediatek.ngin3d.android.StageView;

import android.os.Bundle;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Glo3D;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.animation.PropertyAnimation;

import com.mediatek.ngin3d.demo.R;

/**
 * A demo for usage of Object3D.
 */
public class CameraAnimationDemo extends StageActivity {

    private static final float Z_NEAR = 2f;
    private static final float Z_FAR = 3000f;
    // Demo is set to landscape in the manifest xml
    private static final int HEIGHT = 480;
    private static final int WIDTH = 800;
    private static final float CAMERA_Z_1 = 1111f;
    private static final float CAMERA_Z_2 = 900f;

    BasicAnimation mBendGail;
    BasicAnimation mBlowGail;
    BasicAnimation mSheepWalk;
    BasicAnimation mShowHide;

    BasicAnimation mCameraMove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simple mod for Stereo3D version: (Note additional imports above)
        // Use the StageView so we can get access to the set3DLayout
        // Focus stereo 200 units in front of camera, mid range of camera movement
        // Legacy parameter is not focal length but eye-separation, convert /30

        // mStageView.enableStereoscopic3D(true, 200.0f/50.0f);
        // mStageView.set3DLayout(WindowManager.LayoutParams.LAYOUT3D_SIDE_BY_SIDE );


        final Glo3D landscape = Glo3D.createFromAsset("landscape.glo");
        // tree
        final Container tree = new Container();
        final Glo3D tree_bend_gail = Glo3D.createFromAsset("tree_bend_gail.glo");
        tree.add(tree_bend_gail);

        // sheep
        final Container sheep = new Container();
        final Glo3D sheep_walk = Glo3D.createFromAsset("sheep_walk.glo");
        sheep.add(sheep_walk);

        // sunmoon
        final Container sun_moon = new Container();
        final Glo3D sunmoon = Glo3D.createFromAsset("sunmoon.glo");
        final Glo3D sunmoon_show_hide = Glo3D.createFromAsset("sunmoon_show_hide.glo");
        sun_moon.add(sunmoon, sunmoon_show_hide);

        // leaves
        final Container leaves = new Container();
        final Glo3D leaves_blow_gail = Glo3D.createFromAsset("leaves_blow_gail.glo");
        leaves.add(leaves_blow_gail);

        final Glo3D stars_twinkle = Glo3D.createFromAsset("stars_twinkle.glo");
        final Glo3D rain_fall = Glo3D.createFromAsset("rain_fall.glo");


        Container scenario = new Container();
        scenario.add(landscape, tree, sheep, leaves, sun_moon, stars_twinkle, rain_fall);
        scenario.setPosition(new Point(WIDTH/2, HEIGHT/2, 800));
        scenario.setRotation(new Rotation(-10, -30, 0));
        // Demo uses the UI-Perspective which is Y-down, but model is Y-up
        scenario.setScale(new Scale(6f, -6f, 6f));
        mStage.add(scenario);

        // Get animations
        mBendGail = tree_bend_gail.getAnimation();
        mSheepWalk = sheep_walk.getAnimation();
        mBlowGail = leaves_blow_gail.getAnimation();
        mShowHide = sunmoon_show_hide.getAnimation();

        mBendGail.setLoop(true).start();
        mSheepWalk.setLoop(true).start();
        mBlowGail.setLoop(true).start();
        mShowHide.setLoop(true).start();

        // Set up the view
        mStage.setProjection(Stage.UI_PERSPECTIVE, Z_NEAR, Z_FAR, CAMERA_Z_1);

        Point cameraPos = new Point(WIDTH / 2, HEIGHT / 2, CAMERA_Z_1);
        Point cameraLookAt = new Point(WIDTH / 2, HEIGHT / 2, 0);

        Stage.Camera cameraFrom = new Stage.Camera(cameraPos, cameraLookAt);
        Stage.Camera cameraTo = new Stage.Camera(new Point(WIDTH / 2, HEIGHT / 2, CAMERA_Z_2), cameraLookAt);

        mCameraMove = new PropertyAnimation(mStage, "camera", cameraFrom, cameraTo)
            .setDuration(3000)
            .setLoop(true)
            .setAutoReverse(true);
        mCameraMove.start();

    }

}
