package com.mediatek.ngin3d.demo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import com.mediatek.ngin3d.Box;
import com.mediatek.ngin3d.Canvas2d;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.demo.R;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.presentation.Graphics2d;

public class Canvas2dDemo extends StageActivity {

    private static class Photo extends Canvas2d {

        private Bitmap mFrame;
        private Bitmap mContent;

        public Photo(Resources resources, Bitmap frame, int resIdContent) {
            mFrame = frame;
            mContent = BitmapFactory.decodeResource(resources, resIdContent);
            setDirtyRect(null);
        }

        @Override
        protected void drawRect(Box rect, Graphics2d g2d) {
            super.drawRect(rect, g2d);

            int w = mFrame.getWidth();
            int h = mFrame.getHeight();
            Canvas canvas = g2d.beginDraw(w, h, 0);

            // Draw the content to canvas
            canvas.drawBitmap(mContent, (w - mContent.getWidth())/2, (h - mContent.getHeight())/2, mPaint);
            // Draw the frame to canvas
            canvas.drawBitmap(mFrame, 0, 0, mPaint);

            // Can draw other text to canvas here

            g2d.endDraw();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bitmap frame = BitmapFactory.decodeResource(getResources(), R.drawable.photo_frame);

        Photo photo1 = new Photo(getResources(), frame, R.drawable.photo_01);
        Photo photo2 = new Photo(getResources(), frame, R.drawable.photo_02);
        Photo photo3 = new Photo(getResources(), frame, R.drawable.photo_03);
        Photo photo4 = new Photo(getResources(), frame, R.drawable.photo_04);

        mStage.add(photo1);
        photo1.setPosition(new Point(0.2f, 0.2f, true));
        photo1.setScale(new Scale(0.5f, 0.5f, 1.0f));
        new PropertyAnimation(photo1, "rotation", new Rotation(0, 0, 0), new Rotation(0, 0, 360)).setDuration(5000).setLoop(true).start();

        mStage.add(photo2);
        photo2.setPosition(new Point(0.4f, 0.4f, true));
        photo2.setScale(new Scale(0.5f, 0.5f, 1.0f));
        new PropertyAnimation(photo2, "rotation", new Rotation(0, 0, 30), new Rotation(0, 0, 390)).setDuration(5000).setLoop(true).start();

        mStage.add(photo3);
        photo3.setPosition(new Point(0.6f, 0.6f, true));
        photo3.setScale(new Scale(0.5f, 0.5f, 1.0f));
        new PropertyAnimation(photo3, "rotation", new Rotation(0, 0, 60), new Rotation(0, 0, 420)).setDuration(5000).setLoop(true).start();

        mStage.add(photo4);
        photo4.setPosition(new Point(0.8f, 0.8f, true));
        photo4.setScale(new Scale(0.5f, 0.5f, 1.0f));
        new PropertyAnimation(photo4, "rotation", new Rotation(0, 0, 90), new Rotation(0, 0, 450)).setDuration(5000).setLoop(true).start();
    }
}
