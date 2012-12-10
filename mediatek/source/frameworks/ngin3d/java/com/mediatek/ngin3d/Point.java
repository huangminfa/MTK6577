package com.mediatek.ngin3d;

import com.mediatek.util.JSON;

/**
 * A point in 3D space.
 */
public class Point implements JSON.ToJson {
    public float x, y, z;
    public boolean isNormalized;

    /**
     * Construct a (0, 0, 0) point.
     */
    public Point() {
        // Do nothing by default
    }

    /**
     * Construct a (0, 0, 0) point with specified normalized flag.
     *
     * @param isNormalized
     */
    public Point(boolean isNormalized) {
        this.isNormalized = isNormalized;
    }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point(float x, float y, boolean isNormalized) {
        this.x = x;
        this.y = y;
        this.isNormalized = isNormalized;
    }

    public Point(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point(float x, float y, float z, boolean isNormalized) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.isNormalized = isNormalized;
    }

    public Point(Point other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.isNormalized = other.isNormalized;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (isNormalized != point.isNormalized) return false;
        if (Float.compare(point.x, x) != 0) return false;
        if (Float.compare(point.y, y) != 0) return false;
        if (Float.compare(point.z, z) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (x == +0.0f ? 0 : Float.floatToIntBits(x));
        result = 31 * result + (y == +0.0f ? 0 : Float.floatToIntBits(y));
        result = 31 * result + (z == +0.0f ? 0 : Float.floatToIntBits(z));
        result = 31 * result + (isNormalized ? 1 : 0);
        return result;
    }

    /**
     * Convert the point property to string for output
     * @return   output string
     */
    @Override
    public String toString() {
        return "Point:[" + this.x + ", " + this.y + ", " + this.z + "], isNormalized : " + isNormalized;
    }

    /**
     * Convert the point property to JSON formatted String
     * @return   output JSON formatted String
     */
    public String toJson() {
        return "{Point:[" + this.x + ", " + this.y + ", " + this.z + "], isNormalized : " + isNormalized + "}";
    }

    public static Point newFromString(String positionString) {
        float[] xyz = Utils.parseStringToFloat(positionString);
        return new Point(xyz[0], xyz[1], xyz[2]);
    }

}
