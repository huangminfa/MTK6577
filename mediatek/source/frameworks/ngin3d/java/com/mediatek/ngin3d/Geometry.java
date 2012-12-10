package com.mediatek.ngin3d;

/**
 * The rectangle containing an actor's bounding box, measured in pixels.
 */
public class Geometry {

    public int x, y, width, height;

    public Geometry() {
        // Do nothing by default
    }

    public Geometry(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Geometry geometry = (Geometry) o;

        if (height != geometry.height) return false;
        if (width != geometry.width) return false;
        if (x != geometry.x) return false;
        if (y != geometry.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }
}