package com.mediatek.ngin3d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import com.mediatek.ngin3d.presentation.Model3d;
import com.mediatek.ngin3d.presentation.PresentationEngine;

/**
 * A 3D Sphere.
 */
public class Sphere extends Basic3D<Model3d> {

    public Sphere() {
        setAnchorPoint(new Point(0, 0, 0));
    }
    /**
     * @hide
     */
    @Override
    protected Model3d createPresentation(PresentationEngine engine) {
        return engine.createModel3d(Model3d.SPHERE);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public methods

    /**
     * Creates a sphere which texture image is from file name reference.
     * @param filename  texture image file name.
     * @return  result sphere.
     */
    public static Sphere createFromFile(String filename) {
        Sphere sphere = new Sphere();
        sphere.setImageFromFile(filename);
        return sphere;
    }

    /**
     * Creates a sphere which texture image is from bitmap data.
     * @param bitmap   texture image bitmap data
     * @return  result sphere.
     */
    public static Sphere createFromBitmap(Bitmap bitmap) {
        Sphere sphere = new Sphere();
        sphere.setImageFromBitmap(bitmap);
        return sphere;
    }

    /**
     * Creates a sphere which texture image is from android resource manager.
     * @param resources  gets android resource manager.
     * @param resId  resource id
     * @return   result sphere
     */
    public static Sphere createFromResource(Resources resources, int resId) {
        Sphere sphere = new Sphere();
        sphere.setImageFromResource(resources, resId);
        return sphere;
    }

}

