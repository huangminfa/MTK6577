package com.mediatek.ngin3d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import com.mediatek.ngin3d.presentation.ImageDisplay;
import com.mediatek.ngin3d.presentation.ImageSource;
import com.mediatek.ngin3d.presentation.Model3d;
import com.mediatek.ngin3d.presentation.Presentation;

/**
 * A Basic 3D object class.
 */
public abstract class Basic3D<T extends Presentation>  extends Actor<Model3d> {

    ///////////////////////////////////////////////////////////////////////////
    // Property handling

    public static final Property<ImageSource> PROP_IMG_SRC = new Property<ImageSource>("image_source", null);

    protected boolean applyValue(Property property, Object value) {
        if (property.sameInstance(PROP_IMG_SRC)) {
            ImageSource src = (ImageSource) value;
            if (src == null) {
                return false;
            }
            mPresentation.setTexture(src);
            return true;
        } else if (property.sameInstance(PROP_SCALE)) {
            Scale scale = (Scale) value;
            mPresentation.setScale(new Scale(scale.x, -scale.y, scale.z));
            return true;
        }

        if (super.applyValue(property, value)) {
            return true;
        }

        return false;
    }

    /**
     * Sets the texture image of this basic 3D object from file name reference.
     * @param filename  file name of image.
     */
    public void setImageFromFile(String filename) {
        if (filename == null) {
            throw new NullPointerException("filename cannot be null");
        }
        setValue(PROP_IMG_SRC, new ImageSource(ImageSource.FILE, filename));
    }

    /**
     * Sets the texture image of the basic 3D object from bitmap data.
     * @param bitmap  bitmap data of the image.
     */
    public void setImageFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            throw new NullPointerException("bitmap cannot be null");
        }
        setValue(PROP_IMG_SRC, new ImageSource(ImageSource.BITMAP, bitmap));
    }

    /**
     * Sets the texture image of the basic 3D object from android resource manager.
     * @param resources  gets android resource manager.
     * @param resId    resource id
     */
    public void setImageFromResource(Resources resources, int resId) {
        if (resources == null) {
            throw new NullPointerException("resources cannot be null");
        }
        setValue(PROP_IMG_SRC, new ImageSource(ImageSource.RES_ID,
            new ImageDisplay.Resource(resources, resId)));
    }
}
