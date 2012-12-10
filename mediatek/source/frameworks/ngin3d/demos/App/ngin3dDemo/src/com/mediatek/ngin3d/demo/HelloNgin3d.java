package com.mediatek.ngin3d.demo;

import android.app.Activity;
import android.os.Bundle;
import com.mediatek.ngin3d.Ngin3d;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Text;
import com.mediatek.ngin3d.android.StageView;

/**
 * Add description here.
 */
public class HelloNgin3d extends Activity {

    private Stage mStage = new Stage();
    private StageView mStageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStageView = new StageView(this, mStage);
        setContentView(mStageView);

        Text hello = new Text("Hello ngin3D!");
        // adding a background to check bounding box
        hello.setBackgroundColor(new Color(255, 0, 0, 128));
        hello.setPosition(new Point(100, 100));
        mStage.add(hello);
    }

    @Override
    protected void onPause() {
        mStageView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStageView.onResume();
    }

}
