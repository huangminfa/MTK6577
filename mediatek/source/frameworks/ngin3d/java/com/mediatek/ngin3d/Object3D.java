package com.mediatek.ngin3d;

import android.content.res.Resources;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.animation.Timeline;
import com.mediatek.ngin3d.presentation.IObject3d;
import com.mediatek.ngin3d.presentation.ImageDisplay;
import com.mediatek.ngin3d.presentation.ObjectSource;
import com.mediatek.ngin3d.presentation.PresentationEngine;
import com.mediatek.ngin3d.utils.Ngin3dException;

/**
 * Object3D is a object representing 3D model.
 */
public abstract class Object3D extends Actor<IObject3d> {
    /**
     * @hide
     */
    public static final String TAG = "Object3D";

    /**
     * @hide
     */
    public static final String DEFAULT = "default";
    private ObjectSource mObjectSource;
    private Object3DAnimation mAnimation;

    /**
     * @hide
     */
    public static final String PROPNAME_NODE_ROTATION = "node_rotation";
    /**
     * @hide
     */
    public static final String PROPNAME_NODE_MATERIAL_TYPE = "node_material_type";
    /**
     * @hide
     */
    public static final String PROPNAME_MATERIAL_TYPE_PARAM = "material_type_param";
    /**
     * @hide
     */
    public static final String PROPNAME_NODE_MATERIAL_TYPE_PARAM = "node_material_type_param";
    /**
     * @hide
     */
    public static final String PROPNAME_NODE_MATERIAL_TEXTURE = "node_material_texture";

    /**
     * @hide
     */
    public static final Property<Integer> PROP_MATERIAL_TYPE = new Property<Integer>("material_type", null);

    /**
     * @hide
     */
    @Override
    protected IObject3d createPresentation(PresentationEngine engine) {
        IObject3d iObject3d = engine.createObject3d();
        iObject3d.setObjectSource(mObjectSource);
        if (mAnimation == null) {
            mAnimation = new Object3DAnimation(this, iObject3d.getLength());
        } else {
            mAnimation.setDuration(iObject3d.getLength());
        }

        // For performance purpose, avoid loop animation if the duration is 0.
        if (iObject3d.getLength() == 0) {
            mAnimation.setLoop(false);
        }
        return iObject3d;
    }

    /**
     * @hide
     */
    protected boolean applyValue(Property property, Object value) {
        if (super.applyValue(property, value)) {
            return true;
        }

        if (property.sameInstance(PROP_MATERIAL_TYPE)) {
            if (value != null) {
                int materialType = (Integer) value;
                mPresentation.setMaterialType(materialType);
            }
            return true;
        } else if (property instanceof KeyPathProperty) {
            KeyPathProperty kp = (KeyPathProperty) property;
            String sceneNodeName = kp.getFirstKey();
            String propertyName = kp.getKey(1);

            if (propertyName.equals(PROPNAME_NODE_ROTATION)) {
                if (value != null) {
                    Rotation rotation = (Rotation) value;
                    mPresentation.setRotation(sceneNodeName, rotation);
                }
                return true;

            } else if (propertyName.equals(PROPNAME_NODE_MATERIAL_TYPE)) {
                if (value != null) {
                    int materialType = (Integer) value;
                    mPresentation.setMaterialType(sceneNodeName, materialType);
                    return true;
                }
                return true;

            } else if (propertyName.equals(PROPNAME_NODE_MATERIAL_TEXTURE)) {
                if (value != null) {
                    int textureLayer = Integer.parseInt(kp.getKey(2));
                    String textureFilename = (String) value;
                    mPresentation.setMaterialTexture(sceneNodeName, textureLayer, textureFilename);
                }
                return true;
            }

        }

        return false;
    }


    /**
     * Specify the Object3D object by the object3d file name.
     *
     * @param filename object3d file name
     */
    public void setObjectFromFile(String filename) {
        if (filename == null) {
            throw new NullPointerException("filename cannot be null");
        }
        mObjectSource = new ObjectSource(ObjectSource.FILE, filename);
    }

    /**
     * Specify the Object3D object by android resource and resource id.
     *
     * @param resources android resource
     * @param resId     android resource id
     */
    public void setObjectFromResource(Resources resources, int resId) {
        if (resources == null) {
            throw new NullPointerException("resources cannot be null");
        }
        mObjectSource = new ObjectSource(ObjectSource.RES_ID, new ImageDisplay.Resource(resources, resId));
    }

    /**
     * Specify the Object3D object by the asset name.
     *
     * @param assetName asset file name
     */
    public void setObjectFromAsset(String assetName) {
        if (assetName == null) {
            throw new NullPointerException("assetname cannot be null");
        }
        mObjectSource = new ObjectSource(ObjectSource.ASSET, assetName);
    }

    public BasicAnimation getAnimation() {
        if (mAnimation == null) {
            mAnimation = new Object3DAnimation(this, 0);
        }

        return mAnimation;
    }

    public BasicAnimation getAnimation(String name) {
        if (name.compareTo(DEFAULT) == 0) {
            return getAnimation();
        }
        return null;
    }


    public void setRotation(String sceneNodeName, Rotation rotation) {
        setKeyPathValue(sceneNodeName + "." + PROPNAME_NODE_ROTATION, rotation);
    }

    public void setMaterialType(int type) {
        setValueInTransaction(PROP_MATERIAL_TYPE, type);
    }

    public void setMaterialType(String sceneNodeName, int type) {
        setKeyPathValue(sceneNodeName + "." + PROPNAME_NODE_MATERIAL_TYPE, type);
    }

    public void setMaterialTexture(String sceneNodeName, String textureFilename) {
        setMaterialTexture(sceneNodeName, 0, textureFilename);
    }

    public void setMaterialTexture(String sceneNodeName, int textureLayer,
            String textureFilename) {
        setKeyPathValue(sceneNodeName + "." + PROPNAME_NODE_MATERIAL_TEXTURE + "." + Integer.toString(textureLayer), textureFilename);
    }

    private void update(float time) {
        if (isRealized()) {
            mPresentation.update(time);
        }
    }

    private void start(float time) {
        if (isRealized()) {
            mPresentation.start(time);
        }
    }

    private void stop() {
        if (isRealized()) {
            mPresentation.stop();
        }
    }

    private void setLoop(boolean loop) {
        if (isRealized()) {
            mPresentation.setLoop(loop);
        }
    }



    /**
     * Object3DAnimation is an inner class of Object3D that can operation on Object3D only
     * This class can be created by Object3D only
     */
    private class Object3DAnimation extends BasicAnimation {
        private final Object3D mTarget;
        private long mLoopTime;

        Object3DAnimation(Object3D target, int duration) {
            mTarget = target;
            mTimeline.setDuration(duration, duration);

            mTimeline.addListener(new Timeline.Listener() {
                public void onStarted(Timeline timeline) {
                    mTarget.onAnimationStarted(Object3DAnimation.this.toString(), Object3DAnimation.this);
                }

                public void onNewFrame(Timeline timeline, int elapsedMsecs) {
                    mTarget.update((mLoopTime + elapsedMsecs) / 1000f);
                }

                public void onMarkerReached(Timeline timeline, int elapsedMsecs, String marker, int direction) {
                    // do nothing now
                }

                public void onPaused(Timeline timeline) {
                    mTarget.onAnimationStopped(Object3DAnimation.this.toString());
                }

                public void onCompleted(Timeline timeline) {
                    // do nothing now
                }

                public void onLooped(Timeline timeline) {
                    mLoopTime += mTimeline.getProgressDuration();
                }
            });
        }

        @Override
        public Actor getTarget() {
            return mTarget;
        }

        @Override
        public Animation start() {
            super.start();
            mTarget.start(mLoopTime);
            mTarget.requestRender();
            return this;
        }

        @Override
        public Animation stop() {
            super.stop();
            mTarget.stop();
            mLoopTime = 0;
            return this;
        }

        @Override
        public BasicAnimation setLoop(boolean loop) {
            if (isRealized()) {
                // For performance purpose, avoid loop animation if the duration is 0.
                if (mAnimation.getDuration() == 0) {
                    return this;
                }
            }
            super.setLoop(loop);
            mTarget.setLoop(loop);
            return this;
        }

        @Override
        public final Animation setTarget(Actor target) {
            throw new Ngin3dException("Object3DAnimation can not change target.");
        }
    }
}
