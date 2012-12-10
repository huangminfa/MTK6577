/**************************************************************************
 *
 * Copyright (c) 2012 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/** \file
 * Rectangle Scene Node Presentation for A3M
 */
package com.mediatek.ngin3d.a3m;

import android.util.Log;

import com.mediatek.a3m.Matrix4;
import com.mediatek.a3m.SceneNode;
import com.mediatek.a3m.SceneUtility;
import com.mediatek.a3m.Solid;
import com.mediatek.a3m.Texture2D;
import com.mediatek.a3m.Vector2;
import com.mediatek.a3m.Vector3;
import com.mediatek.a3m.Vector4;
import com.mediatek.a3m.Appearance;
import com.mediatek.ngin3d.Box;
import com.mediatek.ngin3d.Dimension;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.presentation.ImageDisplay;
import com.mediatek.ngin3d.presentation.ImageSource;

/**
 * Represents a quad in 3D space
 * @hide
 */
public class RectSceneNodePresentation extends SceneNodePresentation<SceneNode>
    implements ImageDisplay {

    public static final String TAG = "RectSceneNodePresentation";

    private final Dimension mApparentSize = new Dimension(1, 1);

    private ImageSource mImageSource;
    private Texture2D mTexture;
    private Dimension mSize = new Dimension(1, 1);
    private Scale mScale = new Scale(1, 1, 1);
    private float mAspectRatio = 1;
    private boolean mKeepAspectRatio; // false by default
    private int mRepeatX = 1;
    private int mRepeatY = 1;
    private Box mSourceRect;
    private int mFilterQuality = Image.FILTER_QUALITY_MEDIUM;
    private boolean mUseMipmaps = true;

    /**
     * Initializes this object with A3M presentation engine
     */
    public RectSceneNodePresentation(A3mPresentationEngine engine) {
        super(engine);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        // Replace the default scene node with the quad
        mSceneNode.setParent(null);
        mSceneNode = SceneUtility.createSquare(
            mEngine.getAssetPool(), new Vector2(1, 1));
        mSceneNode.setParent(mAnchorSceneNode);

        // Offset the quad so it its origin is at its top-left corner
        mSceneNode.setPosition(new Vector3(0.5f, 0.5f, 0.0f));

        // By default the texture loaded is inverted with respect to OpenGL's
        // standard, so we scale the quad by -1 in Y to correct for this.
        mSceneNode.setScale(new Vector3(1, -1, 1));

        Solid solid = (Solid) mSceneNode;
        Appearance appearance = solid.getAppearance();
        appearance.setShaderProgram(mEngine.getAssetPool(), "ngin3d:quad.sp");

        // Basic transparency blending
        appearance.setBlendFactors(
                Appearance.BlendFactor.SRC_ALPHA,
                Appearance.BlendFactor.SRC_ALPHA,
                Appearance.BlendFactor.ONE_MINUS_SRC_ALPHA,
                Appearance.BlendFactor.ONE_MINUS_SRC_ALPHA);
        appearance.setDepthWriteEnabled(false);

        // Ensure that UV mapping shader uniform is initialised.
        applyTextureMapping();
    }

    @Override
    public void onUninitialize() {

        mImageSource = null;
        mTexture = null;
        mSize = null;
        mScale = null;

        super.onUninitialize();
    }

    private void replaceImageSource(ImageSource src, Texture2D texture) {
        if (texture == null) {
            if (src.srcType == ImageSource.RES_ID) {
                Log.e(TAG, "failed to load image source: "
                        + mEngine.getResources().getResourceName(
                            ((ImageDisplay.Resource) src.srcInfo).resId) + "; "
                        + src);
            } else {
                Log.e(TAG, "Failed to load image source " + src);
            }
            return;
        }

        Texture2D replaced = mTexture;
        if (replaced != null && replaced.equals(texture)) {
            return;
        }

        ((Solid) mSceneNode).getAppearance().setProperty(
                "M_DIFFUSE_TEXTURE", texture);

        mTexture = texture;
        mImageSource = src;

        mAspectRatio = (float) mTexture.getWidth() / mTexture.getHeight();

        setSize(new Dimension(mTexture.getWidth(), mTexture.getHeight()));

        // Clear source rectangle for new texture
        mSourceRect = null;
        applyTextureMapping();
        updateApparentSize();
        updateTextureFilter();
    }

    /**
     * Sets a new image source for this object.
     *
     * @param src image source
     */
    public void setImageSource(ImageSource src) {
        Texture2D texture = mEngine.getTextureCache().getTexture(src);
        replaceImageSource(src, texture);
    }

    /**
     * Get OpenGL ID ("name" but an integer) for use with live video textures.
     * Exposing OpenGL data at this level is undesirable but currently
     * necessary.  Do not rely on this being available in the long term.
     *
     * @return The 'name' of texture or -1 on failure
     */
    public int getTexName() {
        if (mTexture == null) {
            return -1;
        }
        return mEngine.getAssetPool().getOpenglTextureId(mTexture);
    }

    /**
     * Sets which rectangular area of the texture is mapped to this object.
     * Calling this function will overwrite the effect of any prior calls to
     * {@link #setRepeat(int,int)}.
     *
     * @param rect
     *            the rectangle to display. If null is specified, the entire
     *            source image will be displayed.
     */
    public void setSourceRect(Box rect) {
        if (rect != null) {
            mSourceRect = new Box(rect);
            applyTextureMapping();
        }
    }

    /**
     * Gets the dimension of the image source of this object.
     *
     * @return a dimension object with the setting of this object
     */
    public Dimension getSourceDimension() {
        if (mTexture == null) {
            return new Dimension(0, 0);
        } else {
            return new Dimension(mTexture.getWidth(), mTexture.getHeight());
        }
    }

    /**
     * Sets the scale values for this object.
     *
     * @param scale a scale object for setting up the scale values of
     * this object
     */
    @Override
    public void setScale(Scale scale) {
        mScale = scale;
        applyScale();
    }

    /**
     * Gets the scale values of this object.
     *
     * @return scale value
     */
    @Override
    public Scale getScale() {
        return mScale;
    }

    /**
     * Combines scale and size and applies to underlying object.
     */
    private void applyScale() {
        Vector3 vector = new Vector3(mScale.x * mApparentSize.width,
                mScale.y * mApparentSize.height, mScale.z);
        mRootSceneNode.setScale(vector);
    }

    /**
     * Updates the apparent size of the object depending on its settings.
     * The apparent size of an object depends on its actual size, and whether
     * it is synced to the aspect ratio.
     */
    private void updateApparentSize() {
        mApparentSize.width = mSize.width;
        mApparentSize.height = mSize.height;

        if (mKeepAspectRatio) {
            // Enforce the aspect ratio, changing the larger dimension to
            // meet the smaller dimension
            if (mAspectRatio > mSize.width / mSize.height) {
                mApparentSize.height = mSize.width / mAspectRatio;
            } else {
                mApparentSize.width = mSize.height * mAspectRatio;
            }
        }

        applyScale();
    }

    /**
     * Sets the size of this object using dimension object.
     *
     * @param size a dimension object with size setting
     */
    public void setSize(Dimension size) {
        if (size.width >= 1 && size.height >= 1) {
            mSize.width = (int) size.width;
            mSize.height = (int) size.height;

            updateApparentSize();
        }
    }

    /**
     * Gets the apparent size of this object.
     *
     * @return Apparent size
     */
    public Dimension getSize() {
        return mApparentSize;
    }

    public int getFilterQuality() {
        return mFilterQuality;
    }

    public void setFilterQuality(int quality) {
        mFilterQuality = quality;
        updateTextureFilter();
    }

    /**
     * Sets whether the object must have the same aspect ratio as its texture.
     * When using {@link #setSize(Dimension)}, if this value is set, the size
     * of the image will be reduced to match the aspect ratio of the texture.
     *
     * This option defaults to false.
     *
     * @param flag Boolean flag
     */
    public void setKeepAspectRatio(boolean flag) {
        if (flag != mKeepAspectRatio) {
            mKeepAspectRatio = flag;
            updateApparentSize();
        }
    }

    /**
     * Checks whether the object must have the same aspect ratio as its texture.
     *
     * @return Boolean flag
     */
    public boolean isKeepAspectRatio() {
        return mKeepAspectRatio;
    }

    /**
     * Applies texture mapping to the rectangle, depending on the current mode.
     */
    private void applyTextureMapping() {
        Vector4 uvCoordOffsetScale;

        if (mSourceRect == null) {
            uvCoordOffsetScale = new Vector4(0, 1, mRepeatX, -mRepeatY);

        } else {
            // Incoming coordinates are in pixels - top left then bottom right.
            // These need to be converted to OpenGL 0->1 coordinates.
            Dimension dim = getSourceDimension(); // Size of tex, not of rect
            if (dim.width == 0.f) dim.width = 1.f;
            if (dim.height == 0.f) dim.height = 1.f;

            uvCoordOffsetScale = new Vector4(
                    mSourceRect.x1 / dim.width,
                    mSourceRect.y2 / dim.height,
                    (mSourceRect.x2 - mSourceRect.x1) / dim.width,
                    (mSourceRect.y1 - mSourceRect.y2) / dim.height);
        }

        ((Solid) mSceneNode).getAppearance().setProperty(
            "M_UV_OFFSET_SCALE", uvCoordOffsetScale);
    }

    /**
     * Sets the x and y repeat times of the image in this object.
     * Calling this function will overwrite any source rectangle set by calls to
     * {@link #setSourceRect(Box)}.
     *
     * @param x x axis repeating times
     * @param y y axis repeating times
     */
    public void setRepeat(int x, int y) {
        if (x != 0 && y != 0) {
            mRepeatX = x;
            mRepeatY = y;
            mSourceRect = null;
            applyTextureMapping();
        }
    }

    /**
     * Gets the repeating times of x axis.
     *
     * @return a value of x repeating times
     */
    public int getRepeatX() {
        return mRepeatX;
    }

    /**
     * Gets the repeating times of y axis.
     *
     * @return a value of y repeating times
     */
    public int getRepeatY() {
        return mRepeatY;
    }

    /**
     * Sets the material type of this object.
     *
     * @param type
     */
    public void setMaterialType(int type) {
        setMaterialTypeRecursive(mSceneNode, type);
    }

    /**
     * Enable mipmap of the object or not.
     *
     * @param enable true for enable and false for disable
     */
    public void enableMipmap(boolean enable) {
        mUseMipmaps = enable;
        updateTextureFilter();
    }

    /**
     * Check mipmap of the object is enabled or not
     *
     * @return true for enable and false for disable.
     */
    public boolean isMipmapEnabled() {
        return mUseMipmaps;
    }

    /**
     * Make the rect drawable regardless of orientation of the normal.
     * Makes the 'back' of the rectangle visible, whereas it would
     * normally be culled to enhance performance
     *
     * @param enable true for enable and false for disable
     */
    public void enableDoubleSided(boolean enable) {
        if (enable) {
            // Disable culling so both sides are seen
            ((Solid) mSceneNode).getAppearance().setCullingMode(
                Appearance.CullingMode.CULL_NONE);
        } else {
            // Restore the default that back-facing polygons are culled
            ((Solid) mSceneNode).getAppearance().setCullingMode(
                Appearance.CullingMode.CULL_BACK);
        }
    }

    // Perform bounded plane-line intersection
    @Override
    protected float raycast(Vector3 normal, Vector4 rayStart,
            Vector4 rayVector) {
        // Get the scene node transform matrices
        Matrix4 wtMatrix = mSceneNode.getWorldTransform();
        Matrix4 iWtMatrix = wtMatrix.inverse();

        // The plane on which the rectangle resides is defined by the plane
        // normal.  The normal of the rectangle's face is (0, 0, 1), so we
        // transform that into world space using the world transform matrix.
        Vector4 origin = Matrix4.multiply(wtMatrix, new Vector4(0, 0, 0, 1));
        Vector4 normal4 = Matrix4.multiply(wtMatrix, Vector4.Z_AXIS).normalize();

        // Solving to find the distance to the plane
        float dotRayNormal = Vector4.dot(rayVector, normal4);

        float dist = -1.0f;

        dist = Vector4.dot(Vector4.subtract(origin, rayStart), normal4)
            / dotRayNormal;

        if (dist > 0.0f) {
            // Find the point on the plane and transform back to object space
            Vector4 point = Vector4.add(rayStart,
                    Vector4.multiply(rayVector, dist));
            Vector4 localPoint = Matrix4.multiply(iWtMatrix, point);

            // Test the intersection to see if it is outside the bounds of the
            // unit square that makes up the untransformed rectangle.
            if (Math.abs(localPoint.x()) > 0.5f
                    || Math.abs(localPoint.y()) > 0.5f) {
                dist = -1.0f;
            }

        } else {
            // Set to -1 just to standardize return value for failure
            dist = -1.0f;
        }

        if (dist >= 0.0f) {
            // Return normal of intersection
            normal.x(normal4.x());
            normal.y(normal4.y());
            normal.z(normal4.z());
        }

        return dist;
    }

    private void updateTextureFilter() {
        if (mTexture == null) {
            return;
        }

        if (mFilterQuality == Image.FILTER_QUALITY_LOW) {
            mTexture.setMagFilter(mTexture.FILTER_MODE_NEAREST);
        } else {
            mTexture.setMagFilter(mTexture.FILTER_MODE_LINEAR);
        }

        if (mUseMipmaps) {
            if (mFilterQuality == Image.FILTER_QUALITY_HIGH) {
                mTexture.setMinFilter(mTexture.FILTER_MODE_LINEAR_MIPMAP_LINEAR);
            } else if (mFilterQuality == Image.FILTER_QUALITY_MEDIUM) {
                mTexture.setMinFilter(mTexture.FILTER_MODE_LINEAR_MIPMAP_NEAREST);
            } else {
                mTexture.setMinFilter(mTexture.FILTER_MODE_NEAREST_MIPMAP_NEAREST);
            }

        } else if (mFilterQuality == Image.FILTER_QUALITY_LOW) {
            mTexture.setMinFilter(mTexture.FILTER_MODE_NEAREST);
        } else {
            mTexture.setMinFilter(mTexture.FILTER_MODE_LINEAR);
        }
    }

}
