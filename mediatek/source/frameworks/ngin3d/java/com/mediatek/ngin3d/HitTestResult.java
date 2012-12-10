package com.mediatek.ngin3d;

/**
 * Data structure containing the results of a hit test.
 */
public class HitTestResult {
    private Actor mActor;
    private Point mRayStart;
    private Point mRayEnd;
    private Point mRayHit;
    private Point mRayHitNormal;

    /**
     * Default constructor.
     */
    public HitTestResult() {
    }

    /**
     * Fills the hit test result structure with data about the raycast.
     * @param rayStart Ray start point
     * @param rayEnd Ray end point
     * @param rayHit Ray intersection point
     * @param rayHitNormal Ray intersection normal vector
     */
    public void setRay(Point rayStart, Point rayEnd,
            Point rayHit, Point rayHitNormal) {
        mRayStart = rayStart;
        mRayEnd = rayEnd;
        mRayHit = rayHit;
        mRayHitNormal = rayHitNormal;
    }

    /**
     * Sets the actor which was intersected by the raycast.
     * @param actor Actor intersected by ray
     */
    public void setActor(Actor actor) {
        mActor = actor;
    }

    /**
     * Returns the actor intersected by the hit test.
     * @return Actor intersected by ray
     */
    public Actor getActor() {
        return mActor;
    }

    /**
     * Returns the start point of the raycast.
     * @return Ray start point
     */
    public Point getRayStart() {
        return mRayStart;
    }

    /**
     * Returns the end point of the raycast.
     * @return Ray end point
     */
    public Point getRayEnd() {
        return mRayEnd;
    }

    /**
     * Returns the point at which the ray intersected the actor.
     * If no actor was intersected, this point is set to null.
     * @return Ray intersection point
     */
    public Point getRayHit() {
        return mRayHit;
    }

    /**
     * Returns the normal of the surface of the actor that was intersected.
     * If no actor was intersected, this point is set to null.
     * @return Ray intersection normal vector
     */
    public Point getRayHitNormal() {
        return mRayHitNormal;
    }
}

