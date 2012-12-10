package com.mediatek.ngin3d;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import com.mediatek.ngin3d.presentation.BitmapGenerator;
import com.mediatek.ngin3d.presentation.ImageDisplay;
import com.mediatek.ngin3d.presentation.ImageSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A special actor that extend from Plane can contain image data.
 */
public class Image extends Plane<ImageDisplay> {

    public static final int FILTER_QUALITY_LOW = 0;
    public static final int FILTER_QUALITY_MEDIUM = 1;
    public static final int FILTER_QUALITY_HIGH = 2;

    ///////////////////////////////////////////////////////////////////////////
    // Property handling

    /**
     * @hide
     */
    static final Property<ImageSource> PROP_IMG_SRC = new Property<ImageSource>("image_source", null);
    /**
     * @hide
     */
    static final Property<Integer> PROP_FILTER_QUALITY = new Property<Integer>("filter_quality", FILTER_QUALITY_HIGH);
    /**
     * @hide
     */
    static final Property<Boolean> PROP_KEEP_ASPECT_RATIO = new Property<Boolean>("keep_aspect_ratio", false);
    /**
     * @hide
     */
    static final Property<Integer> PROP_REPEAT_X = new Property<Integer>("repeat_x", 0);
    /**
     * @hide
     */
    static final Property<Integer> PROP_REPEAT_Y = new Property<Integer>("repeat_y", 0);
    /**
     * @hide
     */
    static final Property<Boolean> PROP_ENABLE_MIPMAP = new Property<Boolean>("enable_mipmap", false);
    /**
     * @hide
     */
    static final Property<Boolean> PROP_DOUBLE_SIDED = new Property<Boolean>("double_sided", false);

    static {
        PROP_SIZE.addDependsOn(PROP_IMG_SRC);
        PROP_SRC_RECT.addDependsOn(PROP_IMG_SRC);
    }

    /**
     * Apply the image information data
     *
     * @param property property type to be applied
     * @param value    property value to be applied
     * @return if the property is successfully applied
     * @hide
     */
    protected boolean applyValue(Property property, Object value) {
        if (super.applyValue(property, value)) {
            return true;
        }

        if (property.sameInstance(PROP_IMG_SRC)) {
            ImageSource src = (ImageSource) value;
            if (src == null) {
                return false;
            }
            mPresentation.setImageSource(src);
            // Store image real size if there is no specific value of size
            if (getValue(PROP_SIZE).width < 0) {
                setSize(mPresentation.getSize());
            }

            // After texture object name has been got from the engine,
            // Generate surface texture, setup video player, and start to play.
            if (src.srcType == VIDEOTEXTURE) {
                int textureName = mPresentation.getTexName();
                if (textureName > 0) {
                    VideoTexture videoTexture = (VideoTexture) src.srcInfo;
                    videoTexture.genSurfaceTexture(textureName);
                }
            }
            return true;
        } else if (property.sameInstance(PROP_FILTER_QUALITY)) {
            Integer quality = (Integer) value;
            mPresentation.setFilterQuality(quality);
            return true;
        } else if (property.sameInstance(PROP_KEEP_ASPECT_RATIO)) {
            Boolean kar = (Boolean) value;
            mPresentation.setKeepAspectRatio(kar);
            return true;
        } else if (property.sameInstance(PROP_REPEAT_X)) {
            enableApplyFlags(FLAG_APPLY_LATER_IN_BATCH);
            return true;
        } else if (property.sameInstance(PROP_REPEAT_Y)) {
            enableApplyFlags(FLAG_APPLY_LATER_IN_BATCH);
            return true;
        } else if (property.sameInstance(PROP_ENABLE_MIPMAP)) {
            Boolean enable = (Boolean) value;
            mPresentation.enableMipmap(enable);
            return true;
        } else if (property.sameInstance(PROP_DOUBLE_SIDED)) {
            Boolean enable = (Boolean) value;
            mPresentation.enableDoubleSided(enable);
            return true;
        }
        return false;
    }

    public VideoTexture getVideoTexture() {
        ImageSource src = getValue(PROP_IMG_SRC);
        if (src != null && src.srcType == VIDEOTEXTURE) {
            return (VideoTexture) src.srcInfo;
        }
        return null;
    }

    /**
     * @hide
     */
    @Override
    protected void applyBatchValues() {
        Integer repeatX = getValue(PROP_REPEAT_X);
        Integer repeatY = getValue(PROP_REPEAT_Y);
        mPresentation.setRepeat(repeatX, repeatY);
    }

    /**
     * @hide
     */
    @Override
    protected void updateStreamingTexture() {
        ImageSource src = getValue(PROP_IMG_SRC);
        if (src.srcType == VIDEOTEXTURE) {
            ((VideoTexture) src.srcInfo).applyUpdate();
        }
    }

    /**
     * Un-realize this Image.
     * @hide
     */
    public void unrealize() {
        super.unrealize();
        VideoTexture vt = getVideoTexture();
        if (vt != null) {
            vt.stop();
        }
    }

    /**
     * Create an Image object with blank bitmap
     * @return  an Image object that is blank
     */
    public static Image createEmptyImage() {
        Image image = new Image();
        image.setEmptyImage();
        return image;
    }

    /**
     * Create an Image object from specific file name
     *
     * @param filename image file name
     * @return an Image object that is created by file name
     */
    public static Image createFromFile(String filename) {
        Image image = new Image();
        image.setImageFromFile(filename);
        return image;
    }

    /**
     * Create an Image object from bitmap. Note that the bitmap cannot be recycled. Otherwise the image cannot be
     * displayed correctly after rendering engine is shutdown and restarted again.
     *
     * @param bitmap the bitmap image
     * @return created Image
     */
    public static Image createFromBitmap(Bitmap bitmap) {
        Image image = new Image();
        image.setImageFromBitmap(bitmap);
        return image;
    }

    /**
     * Create an Image object from specified bitmap generator. Note that the generated bitmap may be recycled anytime to
     * reduce memory footprint. If the bitmap is needed again, the generate() method will be called again to generate a
     * new one.
     *
     * @param bitmapGenerator the bitmap generator
     * @return created Image
     */
    public static Image createFromBitmapGenerator(BitmapGenerator bitmapGenerator) {
        Image image = new Image();
        image.setImageFromBitmapGenerator(bitmapGenerator);
        return image;
    }

    /**
     * Create an Image object from android resource and resource id
     *
     * @param resources android resource
     * @param resId     android resource id
     * @return an Image object that is created from android resource
     */
    public static Image createFromResource(Resources resources, int resId) {
        Image image = new Image();
        image.setImageFromResource(resources, resId);
        return image;
    }

    /**
     * Set an empty Image object with size 1 x 1 blank bitmap.
     */
    public void setEmptyImage() {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
        setValue(PROP_IMG_SRC, new ImageSource(ImageSource.BITMAP, bitmap));
    }

    public static Image createFromVideo(Context ctx, Uri uri, int width, int height) {
        Image image = new Image();
        // Essential to set material type here to get the shader uniform in place
        image.setMaterialType(ImageDisplay.EMT_VIDEO_TEXTURE);
        image.setImageFromVideo(ctx, uri, width, height);
        return image;
    }

    /**
     * Specify the Image object by the image file name.
     *
     * @param filename image file name
     */
    public void setImageFromFile(String filename) {
        if (filename == null) {
            throw new NullPointerException("filename cannot be null");
        }
        setValue(PROP_IMG_SRC, new ImageSource(ImageSource.FILE, filename));
    }

    /**
     * Specify the image by a Bitmap object. Note that the bitmap cannot be recycled. Otherwise the image cannot be
     * displayed correctly after rendering engine is shutdown and restarted again.
     *
     * @param bitmap the bitmap image
     */
    public void setImageFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            throw new NullPointerException("bitmap cannot be null");
        }
        setValue(PROP_IMG_SRC, new ImageSource(ImageSource.BITMAP, bitmap));
    }

    /**
     * Specify the image by a Bitmap generator. Note that the generated bitmap cannot be recycled. Otherwise the image cannot be
     * displayed correctly after rendering engine is shutdown and restarted again.
     *
     * @param bitmapGenerator the bitmap generator
     */
    public void setImageFromBitmapGenerator(BitmapGenerator bitmapGenerator) {
        if (bitmapGenerator == null) {
            throw new NullPointerException("bitmapGenerator cannot be null");
        }
        setValue(PROP_IMG_SRC, new ImageSource(ImageSource.BITMAP_GENERATOR, bitmapGenerator));
    }

    /**
     * Specify the Image object by android resource and resource id.
     *
     * @param resources android resource
     * @param resId     android resource id
     */
    public void setImageFromResource(Resources resources, int resId) {
        if (resources == null) {
            throw new NullPointerException("resources cannot be null");
        }
        ImageDisplay.Resource res = new ImageDisplay.Resource(resources, resId);
        Box box = new Box();
        Dimension dim = new Dimension(-1, -1);

        // IMG_SRC, SRC_RECT and SIZE must be applied together or there will be trouble when using texture atlas
        Transaction.beginPropertiesModification();
        if (TextureAtlas.getDefault().getFrame(res, box, dim)) {
            setSourceRect(box);
        } else {
            setSourceRect(null);
        }
        // The image might ever use TextureAtlas, we need reset image size property if it's not in TextureAtlas now
        setValueInTransaction(PROP_SIZE, dim);
        setPropImgSrc(new ImageSource(ImageSource.RES_ID, res));
        Transaction.commit();
    }

    // Create an empty bitmap with specific size
    public void setImageFromVideo(Context ctx, Uri uri, int width, int height) {
        Dimension dim = new Dimension(width, height);
        setValue(PROP_SIZE, dim);
        setValue(PROP_IMG_SRC, new ImageSource(ImageSource.VIDEO_TEXTURE, new VideoTexture(ctx, uri)));
    }

    private void setPropImgSrc(ImageSource imgSrc) {
        setValueInTransaction(PROP_IMG_SRC, imgSrc);
    }

    /**
     * Set the filter quality of this Image object
     *
     * @param quality the quality value to be set
     */
    public void setFilterQuality(int quality) {
        if (quality < FILTER_QUALITY_LOW || quality > FILTER_QUALITY_HIGH) {
            throw new IllegalArgumentException("Invalid quality value: " + quality);
        }
        setValue(PROP_FILTER_QUALITY, quality);
    }

    /**
     * Get the quality value of this Image object.
     *
     * @return the quality value
     */
    public int getFilterQuality() {
        return getValue(PROP_FILTER_QUALITY);
    }

    /**
     * Set if the image of this Image object need to keep the aspect ratio
     *
     * @param kar setting for need to keep aspect ratio or not
     */
    public void setKeepAspectRatio(boolean kar) {
        setValue(PROP_KEEP_ASPECT_RATIO, kar);
    }

    /**
     * Check the image of this Image object is keeping aspect ratio.
     *
     * @return true if the image keeps its aspect ratio
     */
    public boolean isKeepAspectRatio() {
        return getValue(PROP_KEEP_ASPECT_RATIO);
    }

    /**
     * Set the repeat times of the image of this Image object
     *
     * @param repeatX repeating times in x axis
     * @param repeatY repeating times in y axis
     */
    public void setRepeat(int repeatX, int repeatY) {
        setValue(PROP_REPEAT_X, repeatX);
        setValue(PROP_REPEAT_Y, repeatY);
    }

    /**
     * Get the repeating times in x axis
     *
     * @return repeating times in x axis
     */
    public int getRepeatX() {
        return getValue(PROP_REPEAT_X);
    }

    /**
     * Get the repeating times in y axis
     *
     * @return repeating times in y axis
     */
    public int getRepeatY() {
        return getValue(PROP_REPEAT_Y);
    }

    /**
     * Enable mipmap of the object or not.
     *
     * @param enable true for enable and false for disable
     */
    public void enableMipmap(boolean enable) {
        setValue(PROP_ENABLE_MIPMAP, enable);
    }

    /**
     * Check mipmap of the object is enabled or not
     *
     * @return true for enable and false for disable.
     */
    public boolean isMipmapEnable() {
        return getValue(PROP_ENABLE_MIPMAP);
    }

    /**
     * Set whether the image is double-sided or not. Normally polygons facing
     * away from the camera are omitted from the rendering to optimise speed.
     * Occasionally it is necessary to mark certain polygon as
     * visible-from-both-sides so this optimisation is to be turned off and
     * the polygon drawn regardless.
     *
     * @param enable true to make the image double-sided
     */
    public void setDoubleSided(boolean enable) {
        setValue(PROP_DOUBLE_SIDED, enable);
    }

    private static ExecutorService sExecutorService;

    private class BitmapLoader implements Runnable {
        private final ImageSource mSource;

        BitmapLoader(ImageSource src) {
            mSource = src;
        }

        public void run() {
            BitmapGenerator generator = (BitmapGenerator) mSource.srcInfo;
            generator.cacheBitmap();
            setValue(PROP_IMG_SRC, new ImageSource(ImageSource.BITMAP_GENERATOR, generator));
            Thread.yield();
        }
    }

    /**
     * @hide
     */
    public void asyncLoader() {
        ImageSource src = getValue(PROP_IMG_SRC);
        if (src != null) {
            if (src.srcType == ImageSource.BITMAP_GENERATOR) {
                BitmapGenerator generator = (BitmapGenerator) src.srcInfo;
                if (generator.getCachedBitmap() == null) {
                    if (sExecutorService == null) {
                        sExecutorService = Executors.newSingleThreadExecutor();
                    }
                    sExecutorService.submit(new BitmapLoader(src));
                }
            }
        }
    }

}
