package com.mediatek.ngin3d.presentation;

import com.mediatek.ngin3d.Rotation;

public interface IObject3d extends Presentation {
    /**
     * Specify the object to load.
     *
     * @param src object source
     */
    void setObjectSource(ObjectSource src);

    /**
     * Update Object3D animation by progress.
     *
     * @param progress thr progress of animation
     */
    void update(float progress);

    void start(float time);

    void stop();

    void setLoop(boolean loop);

    int getLength();

    void setRotation(String sceneNodeName, Rotation rotation);

    void setMaterialType(int materialType);
    void setMaterialType(String sceneNodeName, int materialType);
    void setMaterialTexture(String sceneNodeName, String textureFilename);
    void setMaterialTexture(String sceneNodeName, int textureLayer, String textureFilename);
}
