package com.mediatek.ngin3d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import com.mediatek.ngin3d.presentation.Model3d;
import com.mediatek.ngin3d.presentation.PresentationEngine;

/**
 * A 3D Cube.
 */
public class Cube extends Basic3D<Model3d> {

    public Cube() {
        setAnchorPoint(new Point(0, 0, 0));
    }

    @Override
    protected Model3d createPresentation(PresentationEngine engine) {
        return engine.createModel3d(Model3d.CUBE);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public methods

    /**
     * Creates a cube which texture image is from file name reference.
     * @param filename  texture image file name.
     * @return  result cube.
     */
    public static Cube createFromFile(String filename) {
        Cube cube = new Cube();
        cube.setImageFromFile(filename);
        return cube;
    }

    /**
     * Creates a cube which texture image is from bitmap data.
     * @param bitmap   texture image bitmap data
     * @return  result cube.
     */
    public static Cube createFromBitmap(Bitmap bitmap) {
        Cube cube = new Cube();
        cube.setImageFromBitmap(bitmap);
        return cube;
    }

    /**
     * Creates a cube which texture image is from android resource manager.
     * @param resources  gets android resource manager.
     * @param resId  resource id
     * @return   result cube
     */
    public static Cube createFromResource(Resources resources, int resId) {
        Cube cube = new Cube();
        cube.setImageFromResource(resources, resId);
        return cube;
    }


}

