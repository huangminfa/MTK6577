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
 * Model3D Presentation for A3M
 */
package com.mediatek.ngin3d.a3m;

import android.util.Log;

import com.mediatek.a3m.Matrix4;
import com.mediatek.a3m.Quaternion;
import com.mediatek.a3m.SceneNode;
import com.mediatek.a3m.SceneUtility;
import com.mediatek.a3m.Solid;
import com.mediatek.a3m.Texture2D;
import com.mediatek.a3m.Vector2;
import com.mediatek.a3m.Vector3;
import com.mediatek.a3m.Vector4;
import com.mediatek.a3m.Appearance;
import com.mediatek.ngin3d.presentation.ImageSource;
import com.mediatek.ngin3d.presentation.ImageDisplay;
import com.mediatek.ngin3d.presentation.Model3d;
import com.mediatek.ngin3d.utils.Ngin3dException;

/**
 * Presentation layer object representing a built-in primitive shape.
 * @hide
 */
public class Model3dPresentation extends SceneNodePresentation<SceneNode>
    implements Model3d {

    public static final String TAG = "Model3dPresentation";

    private final int mType;

    private ImageSource mImageSource;
    private Texture2D mTexture;

    /**
     * Initializes this object with presentation engine and model type.
     * @param engine  the Engine to be used for initialization.
     * @param type  model type, could be sphere or cube.
     */
    public Model3dPresentation(A3mPresentationEngine engine, int type) {
        super(engine);
        mType = type;
    }

    /**
     * Initializes this object
     */
    @Override
    public void onInitialize() {
        super.onInitialize();

        // Replace the default scene node with an appropriate object
        mSceneNode.setParent(null);

        if (mType == Model3d.SPHERE) {
            mSceneNode = SceneUtility.createSphere(
                    mEngine.getAssetPool(), 16, 32, new Vector2(1, 1));
            mSceneNode.setScale(new Vector3(1, 1, -1));
            // \todo determine why sphere needs Z inversion
            mSceneNode.setRotation(new Quaternion(
                        new Vector3(0, 1, 0), -(float) Math.PI / 2.0f));
        } else if (mType == Model3d.CUBE) {
            mSceneNode = SceneUtility.createCube(
                    mEngine.getAssetPool(), new Vector2(1, 1));
            mSceneNode.setScale(new Vector3(1, 1, -1));
            // \todo determine why cube needs Z inversion
        } else {
            throw new Ngin3dException("Unsupported model type " + mType);
        }
        Appearance appearance = ((Solid) mSceneNode).getAppearance();
        appearance.setShaderProgram(mEngine.getAssetPool(), "ngin3d:quad.sp");
        appearance.setProperty("M_UV_OFFSET_SCALE",
                new Vector4(0.f, 0.f, 1.f, 1.f));

        // Basic transparency blending
        appearance.setBlendFactors(
                Appearance.BlendFactor.SRC_ALPHA,
                Appearance.BlendFactor.SRC_ALPHA,
                Appearance.BlendFactor.ONE_MINUS_SRC_ALPHA,
                Appearance.BlendFactor.ONE_MINUS_SRC_ALPHA);
        appearance.setDepthWriteEnabled(false);

        mSceneNode.setParent(mAnchorSceneNode);
    }

    /**
     * Un-initializes this object.
     */
    public void onUninitialize() {

        mImageSource = null;
        mTexture = null;

        super.onUninitialize();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Model3d

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
    }

    /**
     * Sets the texture of this object.
     * @param src image source
     */
    public void setTexture(ImageSource src) {
        Texture2D texture = mEngine.getTextureCache().getTexture(src);
        replaceImageSource(src, texture);
    }

    // Perform shape intersection
    @Override
    protected float raycast(Vector3 normal, Vector4 rayStart,
            Vector4 rayVector) {
        if (mType == Model3d.SPHERE) {
            return raycastSphere(normal, rayStart, rayVector);
        }

        return -1.0f;
    }

    // Perform bounded sphere-line intersection
    private float raycastSphere(Vector3 normal, Vector4 rayStart,
            Vector4 rayVector) {
        // Transform the ray into object-space (the sphere is regular, and has a
        // defined radius of 0.5 in object-space).
        Matrix4 iWtMatrix = mSceneNode.getWorldTransform().inverse();
        Vector4 rayStartLocal = Matrix4.multiply(iWtMatrix, rayStart);
        Vector4 rayVectorLocal = Matrix4.multiply(iWtMatrix, rayVector);

        // In order to later find the distance of the hit in world-space, we
        // must record the scale of the ray in object-space.
        Vector3 rayVectorLocal3 = new Vector3(
                rayVectorLocal.x(), rayVectorLocal.y(), rayVectorLocal.z());
        float scale = rayVectorLocal3.length();
        rayVectorLocal3 = rayVectorLocal3.normalize();

        // The equation takes the centre point of the sphere, and assumes that
        // the line starts from the (0, 0, 0).
        Vector3 rayStartLocal3 = new Vector3(
                rayStartLocal.x(), rayStartLocal.y(), rayStartLocal.z());
        Vector3 origin = new Vector3(0, 0, 0);
        Vector3 centre = Vector3.subtract(origin, rayStartLocal3);
        final float radius = 0.5f;

        // Find whether an intersection has occurred
        float rayVectorDotCentre = Vector3.dot(rayVectorLocal3, centre);
        float det = rayVectorDotCentre * rayVectorDotCentre
            - Vector3.dot(centre, centre) + radius * radius;

        // Negative one indicates no intersection occurred
        float dist = -1.0f;

        if (det >= 0.0f) {
            float sqrtDet = (float)Math.sqrt(det);
            float dist1 = rayVectorDotCentre + sqrtDet;
            float dist2 = rayVectorDotCentre - sqrtDet;

            // If the distance is negative, then the intersection occurred
            // before the ray start.
            if (dist1 >= 0.0f || dist2 >= 0.0f) {
                // Find the minimum non-negative distance
                if (dist1 >= 0.0f && dist2 < 0.0f) {
                    dist = dist1;
                } else if (dist2 >= 0.0f && dist1 < 0.0f) {
                    dist = dist2;
                } else {
                    dist = Math.min(dist1, dist2);
                }

                // Scale into world space
                dist = dist / scale;
            }
        }

        return dist;
    }

}

