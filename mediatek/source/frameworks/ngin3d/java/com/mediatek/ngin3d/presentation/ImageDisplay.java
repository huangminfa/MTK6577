package com.mediatek.ngin3d.presentation;

import android.content.res.Resources;
import com.mediatek.ngin3d.Box;
import com.mediatek.ngin3d.Dimension;
import com.mediatek.util.JSON;

/**
 * Provide image decoding and display.
 */
public interface ImageDisplay extends Presentation {

    /**
     * A Inner class to contain resource information  for other class to use.
     */
    public static class Resource implements JSON.ToJson {
        public Resources resources;
        public int resId;

        public Resource(Resources resources, int resId) {
            this.resources = resources;
            this.resId = resId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Resource resource = (Resource) o;

            if (resId != resource.resId) return false;
            if (resources == null ? resource.resources != null : !resources.equals(resource.resources)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = resources == null ? 0 : resources.hashCode();
            result = 31 * result + resId;
            return result;
        }

        @Override
        public String toString() {
            return String.format("Resource:{resources:\"%s\", resId:%d}", resources, resId);
        }

        public String toJson() {
            return String.format("{Resource:{resources:\"%s\", resId:%d}}", resources, resId);
        }
    }

    /**
     * Specify the image to display.
     *
     * @param src image source
     */
    void setImageSource(ImageSource src);

    /**
     * To display only part of source image.
     *
     * @param rect the rectangle to display. If null is specified, the entire source image will be displayed.
     */
    void setSourceRect(Box rect);

    /**
     * Query the dimension of image
     *
     * @return image dimension. return (0, 0) if image is not available.
     */
    Dimension getSourceDimension();

    /**
     * Specify the width/height to display the image.
     *
     * @param size in pixels
     */
    void setSize(Dimension size);

    /**
     * Gets the size of this object.
     * @return   size of this object.
     */
    Dimension getSize();

    /**
     * Specify the opacity of image when alpha source is set to image.
     *
     * @param opacity The value of opacity
     */
    void setOpacity(int opacity);

    /**
     * Get opacity.
     *
     * @return opacity of image
     */
    int getOpacity();

    /**
     * Get the value of filter quality
     * @return The value of filter quality
     */
    int getFilterQuality();

    void setFilterQuality(int quality);

    /**
     * Sets this object to keep the aspect ratio of the image.
     * @param kar  a boolean value to indicate the status.
     */
    void setKeepAspectRatio(boolean kar);

    /**
     * Checks this object if it keeps the aspect ratio of the image.
     * @return  true if the image of this object keeps the aspect ratio.
     */
    boolean isKeepAspectRatio();

     /**
     * Sets the x and y repeat times of the image in this object.
     * @param x  x axis repeating times
     * @param y  y axis repeating times
     */
    void setRepeat(int x, int y);

     /**
     * Gets the repeating times of x axis.
     * @return  a value of x repeating times
     */
    int getRepeatX();

     /**
     * Gets the repeating times of y axis.
     * @return  a value of y repeating times
     */
    int getRepeatY();

    /**
     * @hide
     */
    int EMT_SOLID = 0;
    /**
     * @hide
     */
    int EMT_REFLECTION_2_LAYER = 11;
    /**
     * @hide
     */
    int EMT_TRANSPARENT_ALPHA_CHANNEL = 13;
    /**
     * @hide
     */
    int EMT_TRANSPARENT_VERTEX_ALPHA = 15;
    /**
     * @hide
     */
    int EMT_ONETEXTURE_BLEND = 23;
    /**
     * @hide
     */
    int EMT_RIPPLE_REFLECTION = 24;
    /**
     * @hide
     */
    int EMT_SIMPLE_BLUR = 25;
    /**
     * @hide
     */
    int EMT_VIDEO_TEXTURE = 26;

    /**
     * @hide
     */
    int EBF_ZERO = 0;
    /**
     * @hide
     */
    int EBF_ONE = 1;
    /**
     * @hide
     */
    int EBF_DST_COLOR = 2;
    /**
     * @hide
     */
    int EBF_ONE_MINUS_DST_COLOR = 3;
    /**
     * @hide
     */
    int EBF_SRC_COLOR = 4;
    /**
     * @hide
     */
    int EBF_ONE_MINUS_SRC_COLOR = 5;
    /**
     * @hide
     */
    int EBF_SRC_ALPHA = 6;
    /**
     * @hide
     */
    int EBF_ONE_MINUS_SRC_ALPHA = 7;
    /**
     * @hide
     */
    int EBF_DST_ALPHA = 8;
    /**
     * @hide
     */
    int EBF_ONE_MINUS_DST_ALPHA = 9;
    /**
     * @hide
     */
    int EBF_SRC_ALPHA_SATURATE = 10;

    /**
     * Sets the material type of this object.
     * @param type the value of material type
     */
    void setMaterialType(int type);

    /**
     * Enable mipmap of the object or not.
     *
     * @param enable true for enable and false for disable
     */
    void enableMipmap(boolean enable);

    /**
     * Check mipmap of the object is enabled or not
     *
     * @return true for enable and false for disable.
     */
    boolean isMipmapEnabled();

    /**
     * Make the rect drawable regardless of orientation of the normal.
     * Makes the 'back' of the rectangle visible, whereas it would
     * normally be culled to enhance performance
     *
     * @param enable true for enable and false for disable
     */
    void enableDoubleSided(boolean enable);

    /**
     * Get texture name
     *
     * @return the name of texture
     */
    int getTexName();
}
