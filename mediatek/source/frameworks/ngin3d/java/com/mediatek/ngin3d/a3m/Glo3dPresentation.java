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
 * Glo3D Presentation for A3M
 */
package com.mediatek.ngin3d.a3m;

import com.mediatek.a3m.AnimationGroup;
import com.mediatek.a3m.Glo;
import com.mediatek.a3m.Quaternion;
import com.mediatek.a3m.SceneNode;
import com.mediatek.a3m.Solid;
import com.mediatek.a3m.Texture2D;
import com.mediatek.ngin3d.presentation.IObject3d;
import com.mediatek.ngin3d.presentation.ObjectSource;
import com.mediatek.ngin3d.Rotation;

import android.util.Log;

/**
 * A presentation object which represents an instance of a Glo object.
 * @hide
 */

public class Glo3dPresentation extends SceneNodePresentation<SceneNode>
    implements IObject3d {

    private static final String TAG = "Glo3dPresentation";

    private AnimationGroup mAnimation;

    /**
     * Initializes this object with A3M presentation engine
     * @param engine
     */
    public Glo3dPresentation(A3mPresentationEngine engine) {
        super(engine);
    }

    /**
     * Called by the presentation engine to initialize the object
     */
    @Override
    public void onInitialize() {
        super.onInitialize();
    }

    /**
     * Called by the presentation engine to unintitialize the object
     */
    @Override
    public void onUninitialize() {
        mAnimation = null;
        super.onUninitialize();
    }

    /**
     * Initializes the Glo presentation from a generic source object
     *
     * @param src object source
     */
    public void setObjectSource(ObjectSource src) {
        super.onInitialize();

        // It isn't clear what the difference between loading from file,
        // and loaded from an asset file is, so they are the same here.
        if (src.srcType == ObjectSource.FILE
                || src.srcType == ObjectSource.ASSET) {
            mSceneNode.setParent(null);
            Log.v(TAG, "Loading file asset: " + (String) src.srcInfo);
            Glo glo = new Glo(mEngine.getAssetPool(),
                    mEngine.getRootNode(), (String) src.srcInfo);
            mSceneNode = glo.getNode();
            mAnimation = glo.getAnimation();

            if (mSceneNode == null) {
                mSceneNode = new SceneNode();
            }

            mSceneNode.setParent(mAnchorSceneNode);

        } // else if (src.srcType == ObjectSource.RES_ID) {
            // \todo implement
            // Loading from a Android resource (a file specified using an
            // ID from the generated R.java files) will most likely be done
            // by loading the resource in Java and passing the data to A3M.
        //}
    }

    public void update(float progress) {
        if (mAnimation != null) {
            mAnimation.update(progress);
        }
    }

    public void start(float time) {
        if (mAnimation != null) {
            mAnimation.start(time);
        }
    }

    public void stop() {
        if (mAnimation != null) {
            // reset GLO animation to initial status
            mAnimation.start(0f);
            mAnimation.update(0f);
            mAnimation.stop();
        }
    }

    public int getLength() {
        if (mAnimation == null) {
            Log.w(TAG, "Glo object has no animation, so its length is 0");
            return 0;
        } else {
            // ngin3d takes milliseconds whereas A3M takes seconds as length.
            // Hence, time length supplied by native A3M code is
            // multiplied by 1000.
            // i.e. seconds to milliseconds conversion
            return ((int) (mAnimation.length() * 1000.0f));
        }
    }

    public void setLoop(boolean loop) {
        if (mAnimation != null) {
            mAnimation.setLooping(loop);
        }
    }

    private SceneNode getSceneNode(String name) {
        return mSceneNode.find(name);
    }

    public void setRotation(String sceneNodeName, Rotation rotation) {
        SceneNode node = getSceneNode(sceneNodeName);
        node.setRotation(new Quaternion(rotation.getQuaternion().getQ0(), rotation.getQuaternion().getQ1(),
            rotation.getQuaternion().getQ2(), rotation.getQuaternion().getQ3()));
    }

    private void setMaterialTextureRecursive(SceneNode sceneNode,
            int textureLayer, Texture2D texture) {
        Solid solid = null;
        if (Solid.class.isInstance(sceneNode)) {
            solid = (Solid) sceneNode;
        }

        if (solid != null) {
            solid.getAppearance().setProperty("M_DIFFUSE_TEXTURE", texture);
        }

        int numChildren = sceneNode.getChildCount();
        for (int i = 0; i < numChildren; ++i) {
            SceneNode childSceneNode = sceneNode.getChild(i);
            setMaterialTextureRecursive(childSceneNode, textureLayer, texture);
        }
    }

    public void setMaterialType(int materialType) {
        setMaterialTypeRecursive(mSceneNode, materialType);
    }

    public void setMaterialType(String sceneNodeName, int materialType) {
        SceneNode sceneNode = getSceneNode(sceneNodeName);
        if (sceneNode == null) {
            Log.v(TAG, "setMaterialType Unsuccessful");
        } else {
            Log.v(TAG, "setMaterialType Success: " + sceneNodeName + ", "
                    + Integer.toString(materialType));
            setMaterialTypeRecursive(sceneNode, materialType);
        }
    }

    // Sets the texture at layer zero applied to a sub-node of this object
    public void setMaterialTexture(String sceneNodeName, String textureFilename) {
        setMaterialTexture(sceneNodeName, 0, textureFilename);
    }

    // Finds specified scene node and sets texture recursively to all the Solid
    // scene nodes attached to this scene node
    public void setMaterialTexture(String sceneNodeName, int textureLayer,
            String textureFilename) {
        Texture2D texture = mEngine.getAssetPool().getTexture2D(
                textureFilename);

        if (texture == null) {
            Log.e(TAG, "Failed to find texture " + textureFilename);
        } else {
            SceneNode sceneNode = mSceneNode.find(sceneNodeName);
            if (sceneNode == null) {
                Log.e(TAG, "Failed to find scenenode " + sceneNodeName);
            } else {
                setMaterialTextureRecursive(sceneNode, textureLayer, texture);
            }
        }
    }
}
