package com.mediatek.ngin3d.presentation;

/**
 * A basic 3D model class.
 */
public interface Model3d extends Presentation {

    /**
     * Sphere setting.
     */
    int SPHERE = 0;
    /**
     * Cube setting.
     */
    int CUBE = 1;

    /**
     * Specify the texture to be used on the model.
     *
     * @param src image source
     */
    void setTexture(ImageSource src);
}
