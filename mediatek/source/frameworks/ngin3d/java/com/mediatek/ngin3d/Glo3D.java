package com.mediatek.ngin3d;

import android.content.res.Resources;

/**
 * GLO is a object representing 3D model.
 */
public final class Glo3D extends Object3D {

    private Glo3D() {
        // Do nothing
    }

     /**
     * Create an 3D object from specific file name
     *
     * @param filename image file name
     * @return an 3D mesh object that is created by file name
     */
    public static Glo3D createFromFile(String filename) {
        Glo3D glo3D = new Glo3D();
        glo3D.setObjectFromFile(filename);
        return glo3D;
    }

    /**
     * Create an Object3D from android resource and resource id
     *
     * @param resources android resource
     * @param resId     android resource id
     * @return an Object3D object that is created from android resource
     */
    public static Glo3D createFromResource(Resources resources, int resId) {
        Glo3D glo3D = new Glo3D();
        glo3D.setObjectFromResource(resources, resId);
        return glo3D;
    }

     /**
     * Create an 3D object from specific asset name
     *
     * @param assetname image file name
     * @return an 3D mesh object that is created by file name
     */
    public static Glo3D createFromAsset(String assetname) {
        Glo3D glo3D = new Glo3D();
        glo3D.setObjectFromAsset(assetname);
        return glo3D;
    }

}
