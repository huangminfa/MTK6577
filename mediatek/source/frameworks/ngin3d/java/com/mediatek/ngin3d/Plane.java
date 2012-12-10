package com.mediatek.ngin3d;

import com.mediatek.ngin3d.presentation.ImageDisplay;
import com.mediatek.ngin3d.presentation.PresentationEngine;

/**
 * Plane is actor with width and height.
 */
public class Plane<T extends ImageDisplay> extends Actor<T> {

    /**
     * @hide
     */
    @Override
    protected T createPresentation(PresentationEngine engine) {
        return (T) engine.createImageDisplay();
    }

    public static final String REFLECTION = "reflection";
    public static final String BLUR = "blur";
    ///////////////////////////////////////////////////////////////////////////
    // Property handling

    /**
     * Only unused internally to indicate invalid alpha source
     * @hide
     */
    private static final int INVALID_ALPHA_SOURCE = 0;
    /**
     * Indicates that the alpha should come from texel color
     */
    public static final int FROM_TEXEL = 1;
    /**
     * Indicates that the alpha should come from vertex color
     */
    public static final int FROM_VERTEX_COLOR = 2;
    /**
     * Indicates that the alpha should come from blending of vertex and texel color
     */
    public static final int FROM_TEXEL_VERTEX = 3;
    /**
     * Indicates that the alpha will be opaque no matter what alpha value of vertex and texel has
     */
    public static final int OPAQUE = 4;

    /**
     * Indicates the texture comes from streaming video
     */
    public static final int VIDEOTEXTURE = 5;

    /**
     * @hide
     */
    public static final String PROPNAME_MATERIAL_TYPE_PARAM = "material_type_param";

    /**
     * @hide
     */
    public static final Property<Dimension> PROP_SIZE = new Property<Dimension>("size", new Dimension());
    /**
     * @hide
     */
    public static final Property<Box> PROP_SRC_RECT = new Property<Box>("src_rect", null);
    /**
     * @hide
     */
    public static final Property<Integer> PROP_MATERIAL_TYPE = new Property<Integer>("material_type", ImageDisplay.EMT_TRANSPARENT_ALPHA_CHANNEL);
    /**
     * @hide
     */
    protected boolean applyValue(Property property, Object value) {
        if (super.applyValue(property, value)) {
            return true;
        }

        if (property.sameInstance(PROP_SRC_RECT)) {
            Box box = (Box) value;
            mPresentation.setSourceRect(box);
            return true;
        } else if (property.sameInstance(PROP_SIZE)) {
            Dimension size = (Dimension) value;
            mPresentation.setSize(size);
            return true;
        } else if (property.sameInstance(PROP_MATERIAL_TYPE)) {
            Integer materialType = (Integer) value;
            mPresentation.setMaterialType(materialType);
            return true;
        }
        return false;
    }

    public static Plane create() {
        Plane plane = new Plane();
        plane.setMaterialType(ImageDisplay.EMT_SOLID);
        return plane;
    }

    public static Plane create(Dimension size) {
        Plane plane = create();
        plane.setSize(size);
        return plane;
    }

    public void setSize(Dimension size) {
        if (size.width < 0 || size.height < 0) {
            throw new IllegalArgumentException("negative value");
        }
        setValueInTransaction(PROP_SIZE, size);
    }

    public Dimension getSize() {
        return getValue(PROP_SIZE);
    }

    private int alphaSourceToMaterialType(int alphaSource) {
        switch (alphaSource) {
        case FROM_TEXEL:
            return ImageDisplay.EMT_TRANSPARENT_ALPHA_CHANNEL;

        case FROM_VERTEX_COLOR:
            return ImageDisplay.EMT_TRANSPARENT_VERTEX_ALPHA;

        case FROM_TEXEL_VERTEX:
            return ImageDisplay.EMT_ONETEXTURE_BLEND;

        case OPAQUE:
            return ImageDisplay.EMT_SOLID;

        case VIDEOTEXTURE:
            return ImageDisplay.EMT_VIDEO_TEXTURE;

        default:
            throw new IllegalArgumentException("Invalid alpha source " + alphaSource);
        }
    }

    private int materialTypeToAlphaSource(int materialType) {
        switch (materialType) {
        case ImageDisplay.EMT_TRANSPARENT_ALPHA_CHANNEL:
            return FROM_TEXEL;

        case ImageDisplay.EMT_TRANSPARENT_VERTEX_ALPHA:
            return FROM_VERTEX_COLOR;

        case ImageDisplay.EMT_ONETEXTURE_BLEND:
            return FROM_TEXEL_VERTEX;

        case ImageDisplay.EMT_SOLID:
            return OPAQUE;

        case ImageDisplay.EMT_VIDEO_TEXTURE:
            return VIDEOTEXTURE;

        default:
            return INVALID_ALPHA_SOURCE;
        }
    }

    /**
     * Specify where the alpha source should come from.
     *
     * @param alphaSource alpha source constant, such as FROM_TEXEL.
     */
    public void setAlphaSource(int alphaSource) {
        if (getAlphaSource() == INVALID_ALPHA_SOURCE) {
            throw new IllegalArgumentException("Cannot change alpha source because special material type is specified");
        }

        setValue(PROP_MATERIAL_TYPE, alphaSourceToMaterialType(alphaSource));
    }

    /**
     * @return current alpha source.
     */
    public int getAlphaSource() {
        return materialTypeToAlphaSource(getValue(PROP_MATERIAL_TYPE));
    }

    /**
     * Note that changing material type will also affect the alpha source value.
     *
     * @param type material type, such as EMT_EMT_SOLID.
     */
    public void setMaterialType(int type) {
        setValue(PROP_MATERIAL_TYPE, type);
    }

    /**
     * @return current material type
     */
    public int getMaterialType() {
        return getValue(PROP_MATERIAL_TYPE);
    }

    public void setSourceRect(Box srcRect) {
        setValueInTransaction(PROP_SRC_RECT, srcRect);
    }

    public Box getSourceRect() {
        return getValue(PROP_SRC_RECT);
    }

    public void setZOrderOnTop(boolean enable, int zOrder) {
        // enable parameter is now redundant
        setValue(PROP_ZORDER_ON_TOP, zOrder);
    }

    public void setRenderingHint(String effect, boolean enable) {
        if (REFLECTION.equals(effect)) {
            if (enable) {
                setValue(PROP_MATERIAL_TYPE, ImageDisplay.EMT_RIPPLE_REFLECTION);
            } else {
                setValue(PROP_MATERIAL_TYPE, ImageDisplay.EMT_TRANSPARENT_ALPHA_CHANNEL);
            }
        } else if (BLUR.equals(effect)) {
            if (enable) {
                setValue(PROP_MATERIAL_TYPE, ImageDisplay.EMT_SIMPLE_BLUR);
            } else {
                setValue(PROP_MATERIAL_TYPE, ImageDisplay.EMT_TRANSPARENT_ALPHA_CHANNEL);
            }
        }
    }

    /**
     * Return the value is dirty or has animated effect that needs to render continuously
     * @return true is the dirty value is set or there is an animated effect
     * @hide
     */
    @Override
    public boolean isDirty() {
        return super.isDirty() || hasAnimatedEffect();
    }

    private boolean hasAnimatedEffect() {
        return getTrulyVisible() && getValue(PROP_MATERIAL_TYPE) == ImageDisplay.EMT_RIPPLE_REFLECTION;
    }
}
