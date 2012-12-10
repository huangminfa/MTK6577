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
 * Scene Node Presentation for A3M
 */
package com.mediatek.ngin3d.a3m;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.mediatek.a3m.Appearance;
import com.mediatek.a3m.AxisAngle;
import com.mediatek.a3m.Matrix4;
import com.mediatek.a3m.Quaternion;
import com.mediatek.a3m.SceneNode;
import com.mediatek.a3m.Solid;
import com.mediatek.a3m.Vector3;
import com.mediatek.a3m.Vector4;
import com.mediatek.ngin3d.Box;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.HitTestResult;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.presentation.Presentation;

/**
 * Base presentation object which represents an empty scene node.
 * This class can be extended to provide additional types of scene
 * node presentation.
 * @hide
 */
public class SceneNodePresentation<T extends SceneNode>
    implements Presentation {

    private static final String TAG = "SceneNodePresentation";

    private static final int MAX_RENDER_LAYER = 3;

    protected A3mPresentationEngine mEngine;
    protected SceneNode mRootSceneNode;
    protected SceneNode mAnchorSceneNode;
    protected T mSceneNode;
    protected String mName;
    protected Object mOwner;

    private final List<SceneNodePresentation> mChildren =
            new LinkedList<SceneNodePresentation>();

    private SceneNodePresentation mParent;
    private final Vector4 mColor = new Vector4(1, 1, 1, 1);
    private Vector4 mParentColor = new Vector4(1, 1, 1, 1);
    private final ArrayList<Vector4> mNodeColours = new ArrayList();

    public SceneNodePresentation(A3mPresentationEngine engine) {
        mEngine = engine;
    }

    public Object getOwner() {
        return mOwner;
    }

    /**
     * Initialize with the owner object.
     *
     * @param owner The owner
     */
    public void initialize(Object owner) {
        onInitialize();
        mOwner = owner;
    }

    protected void onInitialize() {
        if (mAnchorSceneNode == null) {
            // Transformation are applied to the root scene node
            mRootSceneNode = new SceneNode();
            mRootSceneNode.setParent(mEngine.getRootNode());

            // The anchor scene node is used to apply an anchor point offset.
            // The anchor point is the effective local origin of the node.
            mAnchorSceneNode = new SceneNode();
            mAnchorSceneNode.setParent(mRootSceneNode);

            // The scene node is replaced by whatever type of object this
            // presentation is representing.  We create an empty dummy node
            // to start with.
            mSceneNode = (T) new SceneNode();
            mSceneNode.setParent(mAnchorSceneNode);
        }
    }

    /**
     * Checks if this object is initialized
     *
     * @return true if this object is initialized
     */
    public boolean isInitialized() {
        return mSceneNode != null;
    }

    /**
     * Un-initialize this object
     */
    public void uninitialize() {
        onUninitialize();
    }

    protected void onUninitialize() {
        if (mParent != null) {
            mParent.removeChild(this);
        }

        if (mRootSceneNode != null) {
            // Remove the nodes from the scene graph
            mRootSceneNode.setParent(null);
            mAnchorSceneNode.setParent(null);
            mSceneNode.setParent(null);
        }

        mEngine = null;
        mRootSceneNode = null;
        mAnchorSceneNode = null;
        mSceneNode = null;
        mOwner = null;
        mName = null;
        mNodeColours.clear();
    }

    /**
     * Sets the position of this object
     *
     * @param pos position setting using Point object
     */
    public void setPosition(Point pos) {
        Vector3 newPos;
        if (pos.isNormalized) {
            newPos = new Vector3(pos.x * mEngine.getWidth(),
                    pos.y * mEngine.getHeight(), pos.z);
        } else {
            newPos = new Vector3(pos.x, pos.y, pos.z);
        }
        mRootSceneNode.setPosition(newPos);
    }

    /**
     * Gets the position with normalize argument.
     * When normalize is true means the position is using absolute coordinates,
     * false is for relational coordinate
     *
     * @param normalized boolean value for normalize setting
     * @return position point value
     */
    public Point getPosition(boolean normalized) {
        Vector3 pos = mRootSceneNode.getPosition();
        if (normalized) {
            return new Point(pos.x() / mEngine.getWidth(),
                    pos.y() / mEngine.getHeight(), pos.z(), true);
        } else {
            return new Point(pos.x(), pos.y(), pos.z());
        }
    }

    /**
     * Sets the visible status for this object
     *
     * @param visible - 'true' to make scene node visible, 'false' otherwise
     */
    public void setVisible(boolean visible) {
        mRootSceneNode.setVisibilityFlag(visible);
    }

    /**
     * Gets the visibility status of this object.
     *
     * @return true if visibility flag set, false otherwise
     */
    public boolean getVisible() {
        return mRootSceneNode.getVisibilityFlag();
    }

    /**
     * Checks whether this node is currently visible. A node may be invisible
     * because either its visibility flag is set FALSE or if one of its
     * parents has a visibility flag set FALSE.
     *
     * @return TRUE if this node is visible.
     */
    public boolean getTrulyVisible() {
        return mRootSceneNode.isVisible();
    }

    /**
     * Sets the name of this objects
     *
     * @param name a string to be set for name
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Gets the name of this object.
     *
     * @return name of this object.
     */
    public String getName() {
        return mName;
    }

    /**
     * Sets the rotation values for this object.
     *
     * @param rotation a rotation object for setting up the
     * rotation value of this object.
     */
    public void setRotation(Rotation rotation) {
        mRootSceneNode.setRotation(new Quaternion(
                    rotation.getQuaternion().getQ0(),
                    rotation.getQuaternion().getQ1(),
                    rotation.getQuaternion().getQ2(),
                    rotation.getQuaternion().getQ3()));
    }

    /**
     * Gets the rotation values of this object.
     *
     * @return the rotation value.
     */
    public Rotation getRotation() {
        AxisAngle axisAngle = mRootSceneNode.getRotation().toAxisAngle();
        return new Rotation(axisAngle.axis.x(), axisAngle.axis.y(),
                axisAngle.axis.z(), (float) Math.toDegrees(axisAngle.angle));
    }

    /**
     * Sets the scale values for this object.
     *
     * @param scale a scale object for setting up the scale values
     * of this object
     */
    public void setScale(Scale scale) {
        Vector3 vector = new Vector3(scale.x, scale.y, scale.z);
        mRootSceneNode.setScale(vector);
    }

    /**
     * Gets the scale values of this object.
     *
     * @return scale value
     */
    public Scale getScale() {
        Vector3 scale = mRootSceneNode.getScale();
        return new Scale(scale.x(), scale.y(), scale.z());
    }

    /**
     * Sets the anchor point values for this object.
     *
     * @param point a point object to be used for setting up
     * the anchor point of this object.
     */
    public void setAnchorPoint(Point point) {
        Vector3 offset = new Vector3(-point.x, -point.y, -point.z);
        mAnchorSceneNode.setPosition(offset);
    }

    /**
     * Gets the anchor point values of this object.
     *
     * @return anchor point value
     */
    public Point getAnchorPoint() {
        Vector3 offset = mAnchorSceneNode.getPosition();
        return new Point(-offset.x(), -offset.y(), -offset.z());
    }

    /**
     * Sets the diffuse color of this object.
     * The specified colour will be recursively applied to the A3M scene graph
     * for this presentation, and will be recursively applied as a derived
     * colour for all child presentations using the following equation:
     *
     *   derivedColor = parentColor * colour
     *
     * @param color Colour to set
     */
    public void setColor(Color color) {
        mColor.x(color.red / 255.0f);
        mColor.y(color.green / 255.0f);
        mColor.z(color.blue / 255.0f);
        mColor.w(color.alpha / 255.0f);

        applyColor(mParentColor);
    }

    /**
     * Gets the diffuse color of this object.
     *
     * @see #setColor(Color)
     *
     * @return Color object.
     */
    public Color getColor() {
        return new Color(
                (int) (mColor.x() * 255.0f),
                (int) (mColor.y() * 255.0f),
                (int) (mColor.z() * 255.0f),
                (int) (mColor.w() * 255.0f));
    }

    /**
     * Sets the alpha component of the diffuse color of this object.
     *
     * @see #setColor(Color)
     *
     * @param opacity Opacity value (ranges from 0 to 255).
     */
    public void setOpacity(int opacity) {
        mColor.w(opacity / 255.0f);

        applyColor(mParentColor);
    }

    /**
     * Returns the alpha component of the diffuse color of this object.
     *
     * @see #setColor(Color)
     *
     * @return Opacity value (ranges from 0 to 255).
     */
    public int getOpacity() {
        return (int) (mColor.w() * 255.0f);
    }

    /**
     * Returns the product of this presentation's color and its parent's color.
     *
     * @return Derived colur value
     */
    private Vector4 getDerivedColor() {
        return Vector4.multiply(mParentColor, mColor);
    }

    /**
     * Applies the presentation's derived colour recursively to all children.
     *
     * @param parentColor Color of the parent scene node presentation
     */
    private void applyColor(Vector4 parentColor) {
        mParentColor = parentColor;
        Vector4 derivedColor = getDerivedColor();

        boolean cacheColours = mNodeColours.isEmpty();
        setColourRecursive(mSceneNode, derivedColor, 0, cacheColours);

        // Iteratively set color for all child presentations.
        int childCount = getChildrenCount();
        for (int i = 0; i < childCount; ++i) {
            SceneNodePresentation child = (SceneNodePresentation) getChild(i);
            child.applyColor(derivedColor);
        }
    }

    /**
     * Sets the diffuse colour of A3M SceneNodes recursively.
     *
     * @param node Node for which to set colour
     * @param colour Colour to set
     */
    private int setColourRecursive(SceneNode node, Vector4 colour,
            int colourIndexParam, boolean cacheColours) {

        int colourIndex = colourIndexParam; // for PMD high priority warning

        if (Solid.class.isInstance(node)) {
            Solid solid = (Solid) node;
            Appearance appearance = solid.getAppearance();

            Vector4 diffuse;

            // If this is the first time this function has been called, we will
            // cache all of the diffuse colours in a list, as they will be
            // overwritten by the compound colour.
            if (cacheColours) {
                diffuse = appearance.getPropertyVector4("M_DIFFUSE_COLOUR");
                mNodeColours.add(diffuse);
            } else {
                diffuse = mNodeColours.get(colourIndex);
                ++colourIndex;
            }

            Vector4 compound = Vector4.multiply(diffuse, colour);
            appearance.setProperty("M_DIFFUSE_COLOUR", compound);
        }

        // Iteratively set colour for all child A3M scene nodes.
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            SceneNode childNode = node.getChild(i);
            colourIndex = setColourRecursive(childNode, colour, colourIndex,
                    cacheColours);
        }

        return colourIndex;
    }

    /**
     * Performs a raycast hit test on the scene using a screen space coordinate.
     * The nearest presentation object intersected will be returned, along with
     * additional details about the raycast test.
     *
     * @param result Hit test results and details
     * @param screenPoint Screen space point on the screen to pick
     * @return The presentation object intersected by the hit test (or null)
     */
    public Presentation hitTest(HitTestResult result, Point screenPoint) {
        if (!getTrulyVisible()) {
            return null;
        }

        // Get the camera projection matrix
        Matrix4 iVpMatrix = mEngine.getInverseViewProjectionMatrix();

        // In screen space (post-projection), the screen exists on a 2x2 plane
        // at -1 on the z-axis, so to find the ray that corresponds to a
        // particular screen coordinate, we must map the screen coordinate in
        // pixels onto the 2x2 plane, and then find the vector from the origin
        // to that point.  The ray is then transformed back into world-space
        // using the inverse world view projection matrix.
        float x = (screenPoint.x / mEngine.getWidth() - 0.5f) * 2.0f;
        float y = (screenPoint.y / mEngine.getHeight() - 0.5f) * -2.0f;

        Vector4 rayStart = Matrix4.multiply(iVpMatrix,
                new Vector4(x, y, -1, 1));
        rayStart = Vector4.divide(rayStart, rayStart.w());

        Vector4 rayEnd = Matrix4.multiply(iVpMatrix,
                new Vector4(x, y, 1, 1));
        rayEnd = Vector4.divide(rayEnd, rayEnd.w());

        Vector4 rayVector = Vector4.subtract(rayEnd, rayStart).normalize();

        // Perform recursive raycast on all children
        RaycastResult nearest = new RaycastResult();
        raycastRecursive(nearest, rayStart, rayVector);

        if (nearest.node != null) {
            Vector4 rayHit = Vector4.add(rayStart, Vector4.multiply(
                        rayVector, nearest.distance));

            Point start = new Point(rayStart.x(), rayStart.y(), rayStart.z());
            Point end = new Point(rayEnd.x(), rayEnd.y(), rayEnd.z());
            Point hit = new Point(rayHit.x(), rayHit.y(), rayHit.z());
            Point normal = new Point(nearest.normal.x(),
                    nearest.normal.y(), nearest.normal.z());

            result.setRay(start, end, hit, normal);
        }

        return nearest.node;
    }

    /**
     * Recursively performs raycast on all children of this node.
     * The raycast intersection nearest to the start of the ray is always stored
     * in the RaycastResult object recursively passed to this function.
     *
     * @param nearest Contains nearest raycast intersection
     * @param rayStart The start of the ray
     * @param rayVector The direction vector of the ray
     */
    private void raycastRecursive(RaycastResult nearest, Vector4 rayStart,
            Vector4 rayVector) {

        // Since we checked isTrulyVisible() in hitTest(), we can just check the
        // normal visibility flag from now on.
        if (getVisible()) {
            // Check whether the ray intersects this node sooner than its
            // children
            Vector3 normal = new Vector3();
            float distance = raycast(normal, rayStart, rayVector);
            int layer = mSceneNode.getRenderLayer();

            if (distance >= 0
                    && (nearest.distance < 0 || distance < nearest.distance
                        || layer > nearest.layer)) {

                nearest.distance = distance;
                nearest.normal = normal;
                nearest.layer = layer;
                nearest.node = this;
            }

            // Test all children
            int childCount = getChildrenCount();

            for (int i = 0; i < childCount; ++i) {
                SceneNodePresentation child = (SceneNodePresentation) getChild(i);
                child.raycastRecursive(nearest, rayStart, rayVector);
            }
        }
    }

    /**
     * Tests to see if a ray intersects with this presentation.
     *
     * @param normal The normal of the surface where an intersection occurred.
     *               The value of normal is set by the function if an
     *               intersection occurred.
     * @param rayStart The point where the ray begins
     * @param rayVector The direction to cast from the ray start point
     * @return The distance from the start point along ray vector at which an
     *         intersection occurred, such that rayStart + rayVector * distance
     *         will give the intersection point (only if rayVector is normalized
     *         will the distance represent the actual distance of the
     *         intersection point from the ray start point).  A distance less
     *         than zero indicates that the ray did not intersect.
     */
    protected float raycast(Vector3 normal, Vector4 rayStart,
            Vector4 rayVector) {
        return -1;
    }

    /**
     * Add child to this scene node
     *
     * @param presentation The presentation
     */
    public void addChild(Presentation presentation) {
        assert presentation instanceof SceneNodePresentation;
        SceneNodePresentation sceneNodePresentation =
                (SceneNodePresentation) presentation;
        sceneNodePresentation.mRootSceneNode.setParent(mRootSceneNode);
        mChildren.add(sceneNodePresentation);
        sceneNodePresentation.mParent = this;

        // Apply derived color recursively to new child node
        sceneNodePresentation.applyColor(getDerivedColor());
    }

    /**
     * Removes child from this scene node
     *
     * @param presentation The presentation
     */
    public void removeChild(Presentation presentation) {
        assert presentation instanceof SceneNodePresentation;
        SceneNodePresentation sceneNodePresentation =
                (SceneNodePresentation) presentation;

        if (mChildren.remove(sceneNodePresentation)) {
            sceneNodePresentation.mRootSceneNode.setParent(null);
            sceneNodePresentation.mParent = null;
        }
    }

    /**
     * Removes all children from this scene node
     */
    public void removeAllChildren() {
        for (SceneNodePresentation sceneNodePresentation : mChildren) {
            sceneNodePresentation.mRootSceneNode.setParent(null);
            sceneNodePresentation.mParent = null;
        }

        mChildren.clear();
    }

    /**
     * Gets the child through the child index
     *
     * @param index child index
     * @return a child presentation
     */
    public Presentation getChild(int index) {
        return mChildren.get(index);
    }

    /**
     * Gets the number of this class's children
     *
     * @return number of children
     */
    public int getChildrenCount() {
        return mChildren.size();
    }

    /**
     * Gets the scene node of this object.
     *
     * @return scene node
     */
    public SceneNode getSceneNode() {
        return mSceneNode;
    }

    /**
     * Set render z order
     *
     * @param zOrder The value of z order
     */
    public void setRenderZOrder(int zOrder) {
        // Layers are rendered in increasing numerical order.  Negative zOrder
        // means z-order check is disabled, and non-z-order checked objects are
        // rendered first.  Z-orders are sorted in reverse numerical order.
        int layer = zOrder;

        if (layer < 0) {
            layer = MAX_RENDER_LAYER + 1;
        }

        layer = MAX_RENDER_LAYER + 1 - layer;

        mSceneNode.setRenderLayer(layer);
    }

    /**
     * Get render z order
     *
     * @return The value of z order
     */
    public int getRenderZOrder() {
        int zOrder = mSceneNode.getRenderLayer();

        zOrder = MAX_RENDER_LAYER + 1 - zOrder;

        if (zOrder == MAX_RENDER_LAYER + 1) {
            zOrder = -1;
        }

        return zOrder;
    }

    /**
     * Set clip rect of node
     *
     * @param area the rectangle
     */
    public void setDisplayArea(Box area) {
        if (Solid.class.isInstance(mSceneNode)) {
            Solid solid = (Solid) mSceneNode;
            Appearance appearance = solid.getAppearance();

            if (area == null) {
                appearance.setScissorTestEnabled(false);
            } else {
                // The Box passed into the function is defined by the top-left
                // and bottom-right corners, using the top-left of the screen
                // as the origin.  The scissor rectangle function expects the
                // bottom-left corner, and the width and height, using the
                // bottom-left corner as the origin.
                appearance.setScissorTestEnabled(true);
                appearance.setScissorRectangle(
                        (int) area.x1,
                        (int) (mEngine.getHeight() - area.y2),
                        (int) area.x2,
                        (int) (area.y2 - area.y1));
            }
        }
    }

    /**
     * Request renderer render a frame.
     */
    public void requestRender() {
        mEngine.requestRender();
    }

    protected void setMaterialTypeRecursive(SceneNode sceneNode,
            int materialType) {
        if (Solid.class.isInstance(sceneNode)) {
            Solid solid = (Solid)sceneNode;
            if (solid != null) {
                switch (materialType) {
                case 24: // EMT_RIPPLE_REFLECTION
                    Log.v(TAG, "Shader a3m:ripple.sp");
                    solid.getAppearance().setShaderProgram(
                            mEngine.getAssetPool(),
                            "a3m:ripple.sp");
                    // Re-enable depth writing for the ripple, as it is not
                    // transparent.
                    solid.getAppearance().setDepthWriteEnabled(true);

                    break;
                case 26: // EMT_VIDEO_TEXTURE
                    solid.getAppearance().setShaderProgram(
                            mEngine.getAssetPool(),
                            "ngin3d:vidquad.sp");
                    break;
                case 34: // Weather3D EMT_WEATHER_MODEL_SOLID
                    solid.getAppearance().setShaderProgram(
                            mEngine.getAssetPool(),
                            "ngin3d:weather.sp");
                    break;
                case 35: // Weather3D EMT_WEATHER_MODEL_BLEND
                    solid.getAppearance().setShaderProgram(
                            mEngine.getAssetPool(),
                            "ngin3d:weather.sp");
                    solid.getAppearance().setBlendFactors(
                            Appearance.BlendFactor.SRC_ALPHA_SATURATE,
                            Appearance.BlendFactor.SRC_ALPHA_SATURATE,
                            Appearance.BlendFactor.ONE_MINUS_SRC_ALPHA,
                            Appearance.BlendFactor.ONE_MINUS_SRC_ALPHA);
                    break;
                case 37: // Weather3D EMT_UNLIT
                    solid.getAppearance().setShaderProgram(
                            mEngine.getAssetPool(),
                            "ngin3d:quad_premultiply.sp");
                    break;
                case 40: // EMT_FOG
                    solid.getAppearance().setShaderProgram(
                            mEngine.getAssetPool(),
                            "a3m:fog.sp");
                    break;
                case 32: // CLOCK_EMISSIVE
                    solid.getAppearance().setShaderProgram(
                            mEngine.getAssetPool(),
                            "emissive.sp");
                    break;
                case 30: // CLOCK_TRANSPARENT
                    solid.getAppearance().setShaderProgram(
                            mEngine.getAssetPool(),
                            "glass.sp");
                    solid.getAppearance().setBlendFactors(
                            Appearance.BlendFactor.SRC_ALPHA,
                            Appearance.BlendFactor.SRC_ALPHA,
                            Appearance.BlendFactor.ONE,
                            Appearance.BlendFactor.ONE);
                    break;
                case 33: // FOLDERS_FLAT
                    solid.getAppearance().setShaderProgram(
                            mEngine.getAssetPool(),
                            "flat.sp");
                    solid.getAppearance().setBlendFactors(
                            Appearance.BlendFactor.SRC_ALPHA,
                            Appearance.BlendFactor.SRC_ALPHA,
                            Appearance.BlendFactor.ONE_MINUS_SRC_ALPHA,
                            Appearance.BlendFactor.ONE_MINUS_SRC_ALPHA);
                    break;
                default:
                    // Nothing to do
                    break;
                }
            }
        }
        // Iteratively set material type for all children
        int numChildren = sceneNode.getChildCount();
        for (int i = 0; i < numChildren; ++i) {
            SceneNode childSceneNode = sceneNode.getChild(i);
            setMaterialTypeRecursive(childSceneNode, materialType);
        }
    }
}
