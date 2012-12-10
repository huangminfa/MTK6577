
package com.mediatek.ngin3d.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Glo3D;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageView;

/**
 * A demo for usage of Object3D.
 */
public class Glo3DAntiAliasingDemo extends Activity {

    private StageView mStageView;
    private Stage mStage = new Stage();
    private Container mScenario;
    private int mWidth;
    private int mHight;

    private void getDisplaySize() {
        Display display = getWindowManager().getDefaultDisplay();
        mWidth = display.getWidth(); // deprecated
        mHight = display.getHeight(); // deprecated

    }

    private void init(boolean aliasing) {
        mStageView = new StageView(this, mStage, aliasing);
        getDisplaySize();
        setContentView(mStageView);

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(true);

        mScenario = new Container();

        final Glo3D landscape = Glo3D.createFromAsset("landscape.glo");
        mScenario.add(landscape);
        mScenario.setPosition(new Point(mWidth / 2, mHight / 2, 0));
        mScenario.setRotation(new Rotation(10, 30, 0));
        mScenario.setScale(new Scale(50, -50, 50));

        mStage.add(mScenario);
    }
}
