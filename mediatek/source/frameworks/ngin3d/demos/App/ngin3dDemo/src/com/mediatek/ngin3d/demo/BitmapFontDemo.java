package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import com.mediatek.ngin3d.*;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.demo.R;


public class BitmapFontDemo extends StageActivity {
    private static final String TAG = "BmFontDemo";

    public void onCreate(Bundle savedInstanceState) {
        // setting the bitmap font for FPS indicator
        BitmapFont font = new BitmapFont(getResources(), R.raw.bmfont, R.drawable.bmfont);
        BitmapText.setDefaultFont(font);
        super.onCreate(savedInstanceState);

        // Setting a string using BitmapFont
        BitmapFont font3 = new BitmapFont(getResources(), R.raw.bmfont1, R.drawable.bmfont1);
        BitmapText text = new BitmapText("MediaTek", font3);
        text.setPosition(new Point(240, 200));
        mStage.add(text);

        // setting a string using anther Bitmap Font
        BitmapFont font2 = new BitmapFont(getResources(), R.raw.bmfont2, R.drawable.bmfont2);
        BitmapText text2 = new BitmapText("Android", font2);
        text2.setPosition(new Point(240, 300));
        mStage.add(text2);

        // Setting a string using system text
        Text text3 = new Text("MediaTek");
        text3.setTextColor(Color.WHITE);
        text3.setTextSize(40f);
        text3.setPosition(new Point(240, 400));
        mStage.add(text3);

    }
}