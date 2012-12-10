package com.mediatek.ngin3d.demo;

import android.os.Bundle;

import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Glo3D;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.BasicAnimation;

/**
 * A demo for usage of Object3D.
 */
public class OptimusPrime extends StageActivity {

    // Warning:
    // The following positioning numbers are model specific.
    // Change the glo model almost certain require re-tune these numbers.
    // Here is not intended to implement as reference how to view a glo file.
    private static final float Z_NEAR = 800.f;
    private static final float Z_FAR = 1200.f;
    private static final float MODEL_SCALE = 60;
    private static final float CAMERA_Z = 1000.f;

    BasicAnimation mRobot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Glo3D robot = Glo3D.createFromAsset("OptimusPrime.glo");

        Container scenario = new Container();
        scenario.add(robot);

        //Position the model on the screen.
        scenario.setPosition(new Point(220, 300, 0));
        // Scale Y -1 as UI-Perspective is Y-down, but model is Y-up
        scenario.setScale(new Scale(MODEL_SCALE, -MODEL_SCALE, MODEL_SCALE));

        //Tune zNear/zFar ratio to avoid z fighting.
        //Todo: need to bottom up why this range is so sensitive for this model
        //or any potential issue in the engine.
        mStage.setProjection(Stage.UI_PERSPECTIVE, Z_NEAR, Z_FAR, CAMERA_Z);

        mStage.add(scenario);

        // Get animations
        mRobot = robot.getAnimation();

        if (mRobot.isStarted())
        {
          mRobot.stop();
        }
        else
        {
          mRobot.setLoop(true).start();
        }
   }
}

