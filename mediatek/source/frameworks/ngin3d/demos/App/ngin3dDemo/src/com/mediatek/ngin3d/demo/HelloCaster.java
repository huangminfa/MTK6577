package com.mediatek.ngin3d.demo;

import android.app.Activity;
import android.os.Bundle;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageView;
import com.mediatek.ngin3d.demo.R;

import java.util.Random;

/**
 * Add description here.
 */
public class HelloCaster extends Activity {

    private Stage mStage = new Stage();
    private StageView mStageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStageView = new StageView(this, mStage);
        setContentView(mStageView);

        mStage.setBackgroundColor(Color.BLUE);

        for (int i = 0; i < 10; i++) {
            Image caster = Image.createFromResource(getResources(), R.drawable.caster);
            caster.setPosition(new Point(36 + i * 72, 36));
            mStage.add(caster);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStageView.onResume();
    }

    @Override
    protected void onPause() {
        mStageView.onPause();
        super.onPause();
    }
}
