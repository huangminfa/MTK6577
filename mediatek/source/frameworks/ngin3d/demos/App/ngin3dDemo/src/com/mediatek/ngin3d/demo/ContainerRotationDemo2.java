package com.mediatek.ngin3d.demo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import com.mediatek.ngin3d.Box;
import com.mediatek.ngin3d.Canvas2d;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.demo.R;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.presentation.Graphics2d;

/**
 * It's Container rotation demo and the children of container are always face to camera
 */
public class ContainerRotationDemo2 extends StageActivity {

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

        Container container = new Container();
        container.add(photo1);
        container.add(photo2);
        container.add(photo3);
        mStage.add(container);
        photo1.setScale(new Scale(0.5f, 0.5f, 1.0f));
        photo2.setScale(new Scale(0.5f, 0.5f, 1.0f));
        photo3.setScale(new Scale(0.5f, 0.5f, 1.0f));

        container.setPosition(new Point(400f, 240f, false));
        photo1.setPosition(new Point(0f, 0f, 300f, true));
        photo2.setPosition(new Point(268.2f, 0f, -134.16f, false));
        photo3.setPosition(new Point(-268.2f, 0f, -134.16f, false));

        new PropertyAnimation(photo1, "rotation", new Rotation(0, 0, 0), new Rotation(0, 360, 0)).setDuration(5000).setLoop(true).start();
        new PropertyAnimation(photo2, "rotation", new Rotation(0, 0, 0), new Rotation(0, 360, 0)).setDuration(5000).setLoop(true).start();
        new PropertyAnimation(photo3, "rotation", new Rotation(0, 0, 0), new Rotation(0, 360, 0)).setDuration(5000).setLoop(true).start();
        new PropertyAnimation(container, "rotation", new Rotation(0, 0, 0), new Rotation(0, -360, 0)).setDuration(5000).setLoop(true).start();

    }
}
