package com.mediatek.ngin3d;

import com.mediatek.util.JSON;

/**
 * This class is responsible for rotation operation in 3D space.
 * There are several mathematical entities to represent a Rotation.
 * The class hiding this implementation details in quaternion
 * and presents an higher level API for user. User can build a
 * rotation from any of these representations and quaternion can
 * help transform the entities between them.
 */

public class Rotation implements JSON.ToJson {

    /**
     * Use Quaternion to represent the rotation
     */
    public static final int MODE_QUATERNION = 0;
    /**
     * An Euler way to represent the rotation
     */
    public static final int MODE_XYZ_EULER = 1;
    /**
     * Axis and angle way to represent the rotation
     */
    public static final int MODE_AXIS_ANGLE = 2;

    /**
     * The Euler Angles in degree.
     */
    private final Quaternion mQuaternion = new Quaternion();

    /**
     * The Euler Angles in degree.
     */
    private float[] mEulerAngles = new float[3];

    /**
     * The angle of the rotation in degree
     */
    private float mAngle;

    /**
     * The axis of the rotation
     */
    private Vector3D mAxis;

    /**
     * The intrinsic type of rotation.
     */
    private int mMode;

    public Rotation() {
        this(1, 0, 0, 0);
    }

    /**
     * Build a rotation from the quaternion coordinates.
     *
     * @param q0        scalar part of the quaternion
     * @param q1        first coordinate of the vectorial part of the quaternion
     * @param q2        second coordinate of the vectorial part of the quaternion
     * @param q3        third coordinate of the vectorial part of the quaternion
     * @param normalize if true, the coordinates are considered
     *                  not to be normalized, a normalization preprocessing step is performed
     *                  before using them
     */
    public Rotation(float q0, float q1, float q2, float q3, boolean normalize) {
        set(q0, q1, q2, q3, normalize);
    }

    public final void set(float q0, float q1, float q2, float q3, boolean normalize) {
        mQuaternion.set(q0, q1, q2, q3);
        if (normalize) {
            mQuaternion.nor();
        }
        mMode = MODE_QUATERNION;
    }

    /**
     * Build a rotation from an axis and an angle.
     *
     * @param axis  axis around which to rotate
     * @param angle rotation angle in degree.
     * @throws ArithmeticException if the axis norm is zero
     */
    public Rotation(Vector3D axis, float angle) {
        set(axis, angle);
    }

    public final void set(Vector3D axis, float angle) {
        mQuaternion.set(axis, angle);
        mMode = MODE_AXIS_ANGLE;
        mAngle = angle;
        mAxis = axis;
    }

    public Rotation(float x, float y, float z, float angle) {
        set(new Vector3D(x, y, z), angle);
    }

    public final void set(float x, float y, float z, float angle) {
        set(new Vector3D(x, y, z), angle);
    }

    /**
     * Build a rotation from three Euler elementary rotations with specific order.
     *
     * @param order order of rotations to use
     * @param x     angle of the first elementary rotation
     * @param y     angle of the second elementary rotation
     * @param z     angle of the third elementary rotation
     */
    public Rotation(EulerOrder order, float x, float y, float z) {
        set(order, x, y, z);
    }

    /**
     * Build a rotation from three Euler elementary rotations with default XYZ order.
     *
     * @param x     angle of the first elementary rotation
     * @param y     angle of the second elementary rotation
     * @param z     angle of the third elementary rotation
     */
    public Rotation(float x, float y, float z) {
        this(EulerOrder.XYZ , x, y, z);
    }

    public final void set(EulerOrder order, float x, float y, float z) {
        if (order.equals(EulerOrder.XYZ)) {
            mQuaternion.setEulerAngles(order, x, y, z);
        } else if (order.equals(EulerOrder.XZY)) {
            mQuaternion.setEulerAngles(order, x, z, y);
        } else if (order.equals(EulerOrder.ZYX)) {
            mQuaternion.setEulerAngles(order, z, y, x);
        } else if (order.equals(EulerOrder.ZXY)) {
            mQuaternion.setEulerAngles(order, z, x, y);
        } else if (order.equals(EulerOrder.YZX)) {
            mQuaternion.setEulerAngles(order, y, z, x);
        } else if (order.equals(EulerOrder.YXZ)) {
            mQuaternion.setEulerAngles(order, y, x, z);
        } else {
            mQuaternion.setEulerAngles(order, x, y, z);
        }

        mEulerAngles[0] = x;
        mEulerAngles[1] = y;
        mEulerAngles[2] = z;

        mMode = MODE_XYZ_EULER;
    }

    public final void set(float x, float y, float z) {
        set(EulerOrder.XYZ, x, y, z);
    }

    /**
     * Get the normalized axis of the rotation.
     *
     * @return normalized axis of the rotation
     * @see #Rotation(Vector3D, float)
     */
    public Vector3D getAxis() {
        if (mMode == MODE_AXIS_ANGLE) {
            return mAxis;
        } else {
            return mQuaternion.getAxis();
        }
    }

    /**
     * Get the angle of the rotation.
     *
     * @return angle of the rotation (between 0 and &pi;)
     * @see #Rotation(Vector3D, float)
     */
    public float getAxisAngle() {
        if (mMode == MODE_AXIS_ANGLE) {
            return mAngle;
        } else {
            return mQuaternion.getAxisAngle();
        }
    }

    public float[] getEulerAngles(EulerOrder order) {
        if (mMode == MODE_XYZ_EULER) {
            return mEulerAngles;
        } else {
            return mQuaternion.getEulerAngles(order);
        }

    }

    public float[] getEulerAngles() {
        return getEulerAngles(EulerOrder.XYZ);
    }

    /**
     * Get the coordinate system of this rotation object
     *
     * @return coordinate system mode
     */
    public int getMode() {
        return mMode;
    }

    public Quaternion getQuaternion() {
        return mQuaternion;
    }

    /**
     * Compare the input object is the same as this rotation object.
     *
     * @param o input object to be compared
     * @return true if two objects are the same or their properties are the same.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rotation rotation = (Rotation) o;

        if (mMode != rotation.getMode()) return false;

        if (mMode == MODE_XYZ_EULER) {
            float[] euler = rotation.getEulerAngles();
            if (Float.compare(mEulerAngles[0], euler[0]) != 0) return false;
            if (Float.compare(mEulerAngles[1], euler[1]) != 0) return false;
            if (Float.compare(mEulerAngles[2], euler[2]) != 0) return false;
        } else if (mMode == MODE_AXIS_ANGLE) {
            float angle = rotation.getAxisAngle();
            Vector3D axis = rotation.getAxis();
            if (Float.compare(mAngle, angle) != 0) return false;

            return mAxis.equals(axis);
        } else {
            return mQuaternion.equals(rotation.getQuaternion());
        }

        return true;
    }

    /**
     * Create a new hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int result;
        if (mMode == MODE_XYZ_EULER) {
            result = (mEulerAngles[0] == +0.0f ? 0 : Float.floatToIntBits(mEulerAngles[0]));
            result = 31 * result + (mEulerAngles[1] == +0.0f ? 0 : Float.floatToIntBits(mEulerAngles[1]));
            result = 31 * result + (mEulerAngles[2] == +0.0f ? 0 : Float.floatToIntBits(mEulerAngles[2]));
            return result;
        } else if (mMode == MODE_AXIS_ANGLE) {
            result = (mAxis.getX() == +0.0f ? 0 : Float.floatToIntBits(mAxis.getX()));
            result = 31 * result + (mAxis.getY() == +0.0f ? 0 : Float.floatToIntBits(mAxis.getY()));
            result = 31 * result + (mAxis.getZ() == +0.0f ? 0 : Float.floatToIntBits(mAxis.getZ()));
            result = 31 * result + (mAngle == +0.0f ? 0 : Float.floatToIntBits(mAngle));
            return result;
        } else {
            return 31 * mQuaternion.hashCode();
        }
    }

    /**
     * Convert the rotation property to string for output
     * @return   output string
     */
    @Override
    public String toString() {
        if (mMode == MODE_AXIS_ANGLE) {
            return "Rotation:[" + mAxis.getX() + ", " + mAxis.getY() + ", " + mAxis.getZ() + "], Mode: \"Axis Angle\", Angle: " + mAngle;
        } else {
            return "Rotation:[" + mEulerAngles[0] + ", " + mEulerAngles[1] + ", " + mEulerAngles[2] + "], Mode: \"Euler\" ";
        }
    }

    /**
     * Convert the rotation property to JSON formatted String
     * @return   output JSON formatted String
     */
    public String toJson() {
        if (mMode == MODE_AXIS_ANGLE) {
            return "{Rotation:[" + mAxis.getX() + ", " + mAxis.getY() + ", " + mAxis.getZ() + "], Mode: \"Axis Angle\", Angle: " + mAngle + "}";
        } else {
            return "{Rotation:[" + mEulerAngles[0] + ", " + mEulerAngles[1] + ", " + mEulerAngles[2] + "], Mode: \"Euler\" " + "}";
        }
    }

    public static Rotation newFromString(String positionString) {
        float[] xyz = Utils.parseStringToFloat(positionString);
        return new Rotation(xyz[0], xyz[1], xyz[2]);
    }
}
