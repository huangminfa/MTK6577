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
public class TextDemo extends Activity {

    private Stage mStage = new Stage();
    private StageView mStageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStageView = new StageView(this, mStage);
        setContentView(mStageView);

        for (int x = 0; x <= 800; x += 100) {
            for (int y = 0; y <= 480; y += 80) {
                Text text = new Text(String.format("%d,%d", x, y));
                text.setAnchorPoint(new Point(0, 0));
                text.setPosition(new Point(x, y));
                text.setTextSize(16);

                if (y == 80) {
                    text.setBackgroundColor(Color.RED);
                } else if (y == 160) {
                    text.setBackgroundColor(Color.GREEN);
                } else if (y == 240) {
                    text.setBackgroundColor(Color.BLUE);
                } else if (y == 320) {
                    text.setTextColor(new Color(255, 0, 255, 128));
                    text.setBackgroundColor(new Color(255, 255, 255, 128));
                }

                mStage.add(text);
            }
        }
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
