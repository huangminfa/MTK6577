package com.mediatek.ngin3d.tests;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Sphere;
import com.mediatek.ngin3d.presentation.ImageSource;

public class SphereTest extends Ngin3dTest {
    public void testCreateFromBitmap() {
        Bitmap bitmap = BitmapFactory.decodeResource(getInstrumentation().getContext().getResources(), R.drawable.earth);
        Sphere sphere = Sphere.createFromBitmap(bitmap);
        assertEquals(ImageSource.BITMAP, sphere.getValue(Sphere.PROP_IMG_SRC).srcType);
    }

    public void testCreateFromFile() {
        Sphere sphere = Sphere.createFromFile("/sdcard/a3d/photo_01_big.jpg");
        assertEquals(ImageSource.FILE, sphere.getValue(Sphere.PROP_IMG_SRC).srcType);
    }

    public void testCreateFromResource() {
        Sphere sphere = Sphere.createFromResource(getInstrumentation().getContext().getResources(), R.drawable.earth);
        assertEquals(ImageSource.RES_ID, sphere.getValue(Sphere.PROP_IMG_SRC).srcType);
    }

    public void testSetScale() {
        Sphere sphere = new Sphere();
        Scale scale = new Scale(1, 1);
        sphere.setScale(scale);
        assertEquals(1f, sphere.getScale().y);
    }

}
