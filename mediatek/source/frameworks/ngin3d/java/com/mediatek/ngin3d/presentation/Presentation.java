package com.mediatek.ngin3d.presentation;

import com.mediatek.ngin3d.Box;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.HitTestResult;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;

/**
 * Basic presentation.
 */
public interface Presentation {

    /**
     * Initialize a presentation before calling any other methods.
     * @param owner The owner
     */
    void initialize(Object owner);

    /**
     * Check if this presentation engine is initialized.
     * @return true if this presentation engine is initialized.
     */
    boolean isInitialized();

    /**
     * Gets the owner of this object.
     * @return the reference of owner.
     */
    Object getOwner();

    /**
     *  Sets the name of this objects
     * @param name a string to be set for name
     */
    void setName(String name);

     /**
     * Gets the name of this object.
     * @return  name of this object.
     */
    String getName();

    /**
     *  Sets the position of this object
     * @param pos  position setting using Point object
     */
    void setPosition(Point pos);

    /**
     * Gets the position with normalize argument. When normalize is true means the position is using absolute coordinates, false is for relational coordinate
     * @param normalized  boolean value for normalize setting
     * @return   position point value
     */
    Point getPosition(boolean normalized);

    /**
     * Sets the visible setting of this presentation engine.
     * @param visible the value of visible
     */
    void setVisible(boolean visible);

    /**
     *  Gets the visible status for this object
     * @return true if it is visible
     */
    boolean getVisible();

    /**
     *  Gets the truly visible status for this object
     * @return true if it is truly visible
     */
    boolean getTrulyVisible();

     /**
     *  Sets the rotation values for this object.
     * @param rotation   a rotation object for setting up the rotation value of this object.
     */
    void setRotation(Rotation rotation);

    /**
     * Gets the rotation values of this object.
     * @return  the rotation value.
     */
    Rotation getRotation();

    /**
     *  Sets the scale values for this object.
     * @param scale  a scale object for setting up the scale values of this object
     */
    void setScale(Scale scale);

     /**
     *  Gets the scale values of this object.
     * @return  scale value
     */
    Scale getScale();

    /**
     *  Sets the anchor point values for this object.
     * @param point   a point object to be used for setting up the anchor point of this object.
     */
    void setAnchorPoint(Point point);

    /**
     *  Gets the anchor point values of this object.
     * @return   anchor point value
     */
    Point getAnchorPoint();

    /**
     * Set the color tone of image. Will display solid color if image is not specified.
     * @param color color
     */
    void setColor(Color color);

     /**
     *  Gets the color value of this object
     * @return   color value
     */
    Color getColor();

    /**
     * Sets the alpha component of the diffuse color of this object.
     *
     * @see #setColor(Color)
     *
     * @param opacity Opacity value (ranges from 0 to 255).
     */
    void setOpacity(int opacity);

    /**
     * Returns the alpha component of the diffuse color of this object.
     *
     * @see #setColor(Color)
     *
     * @return Opacity value (ranges from 0 to 255).
     */
    int getOpacity();

    void setRenderZOrder(int zOrder);

    int getRenderZOrder();

    /**
     * Set the display area of this object.
     * @param area the area where object can be shown. If passing null value the display area will be cleared.
     */
    void setDisplayArea(Box area);

    /**
     * Performs a raycast hit test on the scene using a screen space coordinate.
     * The nearest presentation object intersected will be returned, along with
     * additional details about the raycast test.
     *
     * @param result Hit test results and details
     * @param screenPoint Screen space point on the screen to pick
     * @return The presentation object intersected by the hit test (or null)
     */
    Presentation hitTest(HitTestResult result, Point screenPoint);

    /**
     * Add a presentation as child of another in presentation tree. If the child is already added to
     * another parent, it will be removed from the old one first and then added to this presentation.
     *
     * @param child child presentation
     */
    void addChild(Presentation child);

    /**
     * Get child presentation at specified index.
     * @param index child index
     * @return child presentation
     */
    Presentation getChild(int index);

    /**
     * @return total count of child presentation
     */
    int getChildrenCount();

    /**
     * Request renderer render a frame.
     */
    void requestRender();

    /**
     * Uninitialize the presentation. Should not call any methods after it is uninitialized.
     */
    void uninitialize();

}
