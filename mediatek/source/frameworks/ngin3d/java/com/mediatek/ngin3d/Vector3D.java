/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mediatek.ngin3d;

import com.mediatek.ngin3d.utils.Ngin3dException;

import java.io.Serializable;

/**
 * This class implements vectors in a three-dimensional space.
 * <p>Instance of this class are guaranteed to be immutable.</p>
 *
 * @version $Revision: 1056554 $ $Date: 2011-01-07 23:59:46 +0100 (Fr, 07 Jan 2011) $
 * @since 1.2
 */
public class Vector3D implements Serializable {
    /**
     * Null vector (coordinates: 0, 0, 0).
     */
    public static final Vector3D ZERO = new Vector3D(0, 0, 0);

    /**
     * First canonical vector (coordinates: 1, 0, 0).
     */
    public static final Vector3D PLUS_I = new Vector3D(1, 0, 0);

    /**
     * Opposite of the first canonical vector (coordinates: -1, 0, 0).
     */
    public static final Vector3D MINUS_I = new Vector3D(-1, 0, 0);

    /**
     * Second canonical vector (coordinates: 0, 1, 0).
     */
    public static final Vector3D PLUS_J = new Vector3D(0, 1, 0);

    /**
     * Opposite of the second canonical vector (coordinates: 0, -1, 0).
     */
    public static final Vector3D MINUS_J = new Vector3D(0, -1, 0);

    /**
     * Third canonical vector (coordinates: 0, 0, 1).
     */
    public static final Vector3D PLUS_K = new Vector3D(0, 0, 1);

    /**
     * Opposite of the third canonical vector (coordinates: 0, 0, -1).
     */
    public static final Vector3D MINUS_K = new Vector3D(0, 0, -1);

    // CHECKSTYLE: stop ConstantName
    /**
     * A vector with all coordinates set to NaN.
     */
    public static final Vector3D NaN = new Vector3D(Float.NaN, Float.NaN, Float.NaN);
    // CHECKSTYLE: resume ConstantName

    /**
     * A vector with all coordinates set to positive infinity.
     */
    public static final Vector3D POSITIVE_INFINITY =
        new Vector3D(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);

    /**
     * A vector with all coordinates set to negative infinity.
     */
    public static final Vector3D NEGATIVE_INFINITY =
        new Vector3D(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

    /**
     * Serializable version identifier.
     */
    private static final long serialVersionUID = 5133268763396045979L;

    /**
     * Abscissa.
     */
    private float mX;

    /**
     * Ordinate.
     */
    private float mY;

    /**
     * Height.
     */
    private float mZ;

    /**
     * Simple constructor.
     * Build a vector from its coordinates
     *
     * @param x abscissa
     * @param y ordinate
     * @param z height
     * @see #getX()
     * @see #getY()
     * @see #getZ()
     */
    public Vector3D(float x, float y, float z) {
        set(x, y, z);
    }

    public final void set(float x, float y, float z) {
        this.mX = x;
        this.mY = y;
        this.mZ = z;
    }

    /**
     * Simple constructor.
     * Build a vector from its azimuthal coordinates
     *
     * @param alpha azimuth (&alpha;) around Z
     *              (0 is +X, &pi;/2 is +Y, &pi; is -X and 3&pi;/2 is -Y)
     * @param delta elevation (&delta;) above (XY) plane, from -&pi;/2 to +&pi;/2
     * @see #getAlpha()
     * @see #getDelta()
     */
    public Vector3D(float alpha, float delta) {
        float cosDelta = (float) Math.cos(delta);
        this.mX = (float) Math.cos(alpha) * cosDelta;
        this.mY = (float) Math.sin(alpha) * cosDelta;
        this.mZ = (float) Math.sin(delta);
    }

    /**
     * Multiplicative constructor
     * Build a vector from another one and a scale factor.
     * The vector built will be a * u
     *
     * @param a scale factor
     * @param u base (unscaled) vector
     */
    public Vector3D(float a, Vector3D u) {
        this.mX = a * u.mX;
        this.mY = a * u.mY;
        this.mZ = a * u.mZ;
    }

    /**
     * Linear constructor
     * Build a vector from two other ones and corresponding scale factors.
     * The vector built will be a1 * u1 + a2 * u2
     *
     * @param a1 first scale factor
     * @param u1 first base (unscaled) vector
     * @param a2 second scale factor
     * @param u2 second base (unscaled) vector
     */
    public Vector3D(float a1, Vector3D u1, float a2, Vector3D u2) {
        this.mX = a1 * u1.mX + a2 * u2.mX;
        this.mY = a1 * u1.mY + a2 * u2.mY;
        this.mZ = a1 * u1.mZ + a2 * u2.mZ;
    }

    /**
     * Linear constructor
     * Build a vector from three other ones and corresponding scale factors.
     * The vector built will be a1 * u1 + a2 * u2 + a3 * u3
     *
     * @param a1 first scale factor
     * @param u1 first base (unscaled) vector
     * @param a2 second scale factor
     * @param u2 second base (unscaled) vector
     * @param a3 third scale factor
     * @param u3 third base (unscaled) vector
     */
    public Vector3D(float a1, Vector3D u1, float a2, Vector3D u2,
                    float a3, Vector3D u3) {
        this.mX = a1 * u1.mX + a2 * u2.mX + a3 * u3.mX;
        this.mY = a1 * u1.mY + a2 * u2.mY + a3 * u3.mY;
        this.mZ = a1 * u1.mZ + a2 * u2.mZ + a3 * u3.mZ;
    }

    /**
     * Linear constructor
     * Build a vector from four other ones and corresponding scale factors.
     * The vector built will be a1 * u1 + a2 * u2 + a3 * u3 + a4 * u4
     *
     * @param a1 first scale factor
     * @param u1 first base (unscaled) vector
     * @param a2 second scale factor
     * @param u2 second base (unscaled) vector
     * @param a3 third scale factor
     * @param u3 third base (unscaled) vector
     * @param a4 fourth scale factor
     * @param u4 fourth base (unscaled) vector
     */
    public Vector3D(float a1, Vector3D u1, float a2, Vector3D u2,
                    float a3, Vector3D u3, float a4, Vector3D u4) {
        this.mX = a1 * u1.mX + a2 * u2.mX + a3 * u3.mX + a4 * u4.mX;
        this.mY = a1 * u1.mY + a2 * u2.mY + a3 * u3.mY + a4 * u4.mY;
        this.mZ = a1 * u1.mZ + a2 * u2.mZ + a3 * u3.mZ + a4 * u4.mZ;
    }

    /**
     * Get the abscissa of the vector.
     *
     * @return abscissa of the vector
     * @see #Vector3D(float, float, float)
     */
    public float getX() {
        return mX;
    }

    /**
     * Get the ordinate of the vector.
     *
     * @return ordinate of the vector
     * @see #Vector3D(float, float, float)
     */
    public float getY() {
        return mY;
    }

    /**
     * Get the height of the vector.
     *
     * @return height of the vector
     * @see #Vector3D(float, float, float)
     */
    public float getZ() {
        return mZ;
    }

    /**
     * Get the L<sub>1</sub> norm for the vector.
     *
     * @return L<sub>1</sub> norm for the vector
     */
    public float getNorm1() {
        return (float) Math.abs(mX) + (float) Math.abs(mY) + (float) Math.abs(mZ);
    }

    /**
     * Get the L<sub>2</sub> norm for the vector.
     *
     * @return euclidian norm for the vector
     */
    public float getNorm() {
        return (float) Math.sqrt(mX * mX + mY * mY + mZ * mZ);
    }

    /**
     * Get the square of the norm for the vector.
     *
     * @return square of the euclidian norm for the vector
     */
    public float getNormSq() {
        return mX * mX + mY * mY + mZ * mZ;
    }

    /**
     * Get the L<sub>&infin;</sub> norm for the vector.
     *
     * @return L<sub>&infin;</sub> norm for the vector
     */
    public float getNormInf() {
        return Math.max(Math.max(Math.abs(mX), Math.abs(mY)), Math.abs(mZ));
    }

    /**
     * Get the azimuth of the vector.
     *
     * @return azimuth (&alpha;) of the vector, between -&pi; and +&pi;
     * @see #Vector3D(float, float)
     */
    public float getAlpha() {
        return (float) Math.atan2(mY, mX);
    }

    /**
     * Get the elevation of the vector.
     *
     * @return elevation (&delta;) of the vector, between -&pi;/2 and +&pi;/2
     * @see #Vector3D(float, float)
     */
    public float getDelta() {
        return (float) Math.asin(mZ / getNorm());
    }

    /**
     * Add a vector to the instance.
     *
     * @param v vector to add
     * @return a new vector
     */
    public Vector3D add(Vector3D v) {
        return new Vector3D(mX + v.mX, mY + v.mY, mZ + v.mZ);
    }

    /**
     * Add a scaled vector to the instance.
     *
     * @param factor scale factor to apply to v before adding it
     * @param v      vector to add
     * @return a new vector
     */
    public Vector3D add(float factor, Vector3D v) {
        return new Vector3D(mX + factor * v.mX, mY + factor * v.mY, mZ + factor * v.mZ);
    }

    /**
     * Subtract a vector from the instance.
     *
     * @param v vector to subtract
     * @return a new vector
     */
    public Vector3D subtract(Vector3D v) {
        return new Vector3D(mX - v.mX, mY - v.mY, mZ - v.mZ);
    }

    /**
     * Subtract a scaled vector from the instance.
     *
     * @param factor scale factor to apply to v before subtracting it
     * @param v      vector to subtract
     * @return a new vector
     */
    public Vector3D subtract(float factor, Vector3D v) {
        return new Vector3D(mX - factor * v.mX, mY - factor * v.mY, mZ - factor * v.mZ);
    }

    /**
     * Get a normalized vector aligned with the instance.
     *
     * @return a new normalized vector
     * @throws ArithmeticException if the norm is zero
     */
    public Vector3D normalize() {
        float s = getNorm();
        if (s == 0) {
            throw new Ngin3dException("MathArithmeticException");
        }
        return scalarMultiply(1 / s);
    }

    /**
     * Get a vector orthogonal to the instance.
     * <p>There are an infinite number of normalized vectors orthogonal
     * to the instance. This method picks up one of them almost
     * arbitrarily. It is useful when one needs to compute a reference
     * frame with one of the axes in a predefined direction. The
     * following example shows how to build a frame having the k axis
     * aligned with the known vector u :
     * <pre><code>
     *   Vector3D k = u.normalize();
     *   Vector3D i = k.orthogonal();
     *   Vector3D j = Vector3D.crossProduct(k, i);
     * </code></pre></p>
     *
     * @return a new normalized vector orthogonal to the instance
     * @throws ArithmeticException if the norm of the instance is null
     */
    public Vector3D orthogonal() {

        float threshold = 0.6f * getNorm();
        if (threshold == 0) {
            throw new Ngin3dException("MathArithmeticException");
        }

        if ((mX >= -threshold) && (mX <= threshold)) {
            float inverse = 1 / (float) Math.sqrt(mY * mY + mZ * mZ);
            return new Vector3D(0, inverse * mZ, -inverse * mY);
        } else if ((mY >= -threshold) && (mY <= threshold)) {
            float inverse = 1 / (float) Math.sqrt(mX * mX + mZ * mZ);
            return new Vector3D(-inverse * mZ, 0, inverse * mX);
        }
        float inverse = 1 / (float) Math.sqrt(mX * mX + mY * mY);
        return new Vector3D(inverse * mY, -inverse * mX, 0);

    }

    /**
     * Compute the angular separation between two vectors.
     * <p>This method computes the angular separation between two
     * vectors using the dot product for well separated vectors and the
     * cross product for almost aligned vectors. This allows to have a
     * good accuracy in all cases, even for vectors very close to each
     * other.</p>
     *
     * @param v1 first vector
     * @param v2 second vector
     * @return angular separation between v1 and v2
     * @throws ArithmeticException if either vector has a null norm
     */
    public static float angle(Vector3D v1, Vector3D v2) {

        float normProduct = v1.getNorm() * v2.getNorm();
        if (normProduct == 0) {
            throw new Ngin3dException("MathArithmeticException");
        }

        float dot = dotProduct(v1, v2);
        float threshold = normProduct * 0.9999f;
        if ((dot < -threshold) || (dot > threshold)) {
            // the vectors are almost aligned, compute using the sine
            Vector3D v3 = crossProduct(v1, v2);
            if (dot >= 0) {
                return (float) Math.asin(v3.getNorm() / normProduct);
            }
            return (float) Math.PI - (float) Math.asin(v3.getNorm() / normProduct);
        }

        // the vectors are sufficiently separated to use the cosine
        return (float) Math.acos(dot / normProduct);

    }

    /**
     * Get the opposite of the instance.
     *
     * @return a new vector which is opposite to the instance
     */
    public Vector3D negate() {
        return new Vector3D(-mX, -mY, -mZ);
    }

    /**
     * Multiply the instance by a scalar
     *
     * @param a scalar
     * @return a new vector
     */
    public Vector3D scalarMultiply(float a) {
        return new Vector3D(a * mX, a * mY, a * mZ);
    }

    /**
     * Returns true if any coordinate of this vector is NaN; false otherwise
     *
     * @return true if any coordinate of this vector is NaN; false otherwise
     */
    public boolean isNaN() {
        return Double.isNaN(mX) || Double.isNaN(mY) || Double.isNaN(mZ);
    }

    /**
     * Returns true if any coordinate of this vector is infinite and none are NaN;
     * false otherwise
     *
     * @return true if any coordinate of this vector is infinite and none are NaN;
     *         false otherwise
     */
    public boolean isInfinite() {
        return !isNaN() && (Double.isInfinite(mX) || Double.isInfinite(mY) || Double.isInfinite(mZ));
    }

    /**
     * Test for the equality of two 3D vectors.
     * <p>
     * If all coordinates of two 3D vectors are exactly the same, and none are
     * <code>Double.NaN</code>, the two 3D vectors are considered to be equal.
     * </p>
     * <p>
     * <code>NaN</code> coordinates are considered to affect globally the vector
     * and be equals to each other - i.e, if either (or all) coordinates of the
     * 3D vector are equal to <code>Double.NaN</code>, the 3D vector is equal to
     * {@link #NaN}.
     * </p>
     *
     * @param other Object to test for equality to this
     * @return true if two 3D vector objects are equal, false if
     *         object is null, not an instance of Vector3D, or
     *         not equal to this Vector3D instance
     */
    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (other instanceof Vector3D) {
            final Vector3D rhs = (Vector3D) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return (mX == rhs.mX) && (mY == rhs.mY) && (mZ == rhs.mZ);
        }
        return false;
    }

    /**
     * Get a hashCode for the 3D vector.
     * <p>
     * All NaN values have the same hash code.</p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        if (isNaN()) {
            return 8;
        }
        return 31 * (23 * (int) (mX) + 19 * (int) (mY) + (int) (mZ));
    }

    /**
     * Compute the dot-product of two vectors.
     *
     * @param v1 first vector
     * @param v2 second vector
     * @return the dot product v1.v2
     */
    public static float dotProduct(Vector3D v1, Vector3D v2) {
        return v1.mX * v2.mX + v1.mY * v2.mY + v1.mZ * v2.mZ;
    }

    /**
     * Compute the cross-product of two vectors.
     *
     * @param v1 first vector
     * @param v2 second vector
     * @return the cross product v1 ^ v2 as a new Vector
     */
    public static Vector3D crossProduct(Vector3D v1, Vector3D v2) {
        return new Vector3D(v1.mY * v2.mZ - v1.mZ * v2.mY,
            v1.mZ * v2.mX - v1.mX * v2.mZ,
            v1.mX * v2.mY - v1.mY * v2.mX);
    }

    /**
     * Compute the distance between two vectors according to the L<sub>1</sub> norm.
     * <p>Calling this method is equivalent to calling:
     * <code>v1.subtract(v2).getNorm1()</code> except that no intermediate
     * vector is built</p>
     *
     * @param v1 first vector
     * @param v2 second vector
     * @return the distance between v1 and v2 according to the L<sub>1</sub> norm
     */
    public static float distance1(Vector3D v1, Vector3D v2) {
        final float dx = Math.abs(v2.mX - v1.mX);
        final float dy = Math.abs(v2.mY - v1.mY);
        final float dz = Math.abs(v2.mZ - v1.mZ);
        return dx + dy + dz;
    }

    /**
     * Compute the distance between two vectors according to the L<sub>2</sub> norm.
     * <p>Calling this method is equivalent to calling:
     * <code>v1.subtract(v2).getNorm()</code> except that no intermediate
     * vector is built</p>
     *
     * @param v1 first vector
     * @param v2 second vector
     * @return the distance between v1 and v2 according to the L<sub>2</sub> norm
     */
    public static float distance(Vector3D v1, Vector3D v2) {
        final float dx = v2.mX - v1.mX;
        final float dy = v2.mY - v1.mY;
        final float dz = v2.mZ - v1.mZ;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Compute the distance between two vectors according to the L<sub>&infin;</sub> norm.
     * <p>Calling this method is equivalent to calling:
     * <code>v1.subtract(v2).getNormInf()</code> except that no intermediate
     * vector is built</p>
     *
     * @param v1 first vector
     * @param v2 second vector
     * @return the distance between v1 and v2 according to the L<sub>&infin;</sub> norm
     */
    public static double distanceInf(Vector3D v1, Vector3D v2) {
        final double dx = Math.abs(v2.mX - v1.mX);
        final double dy = Math.abs(v2.mY - v1.mY);
        final double dz = Math.abs(v2.mZ - v1.mZ);
        return Math.max(Math.max(dx, dy), dz);
    }

    /**
     * Compute the square of the distance between two vectors.
     * <p>Calling this method is equivalent to calling:
     * <code>v1.subtract(v2).getNormSq()</code> except that no intermediate
     * vector is built</p>
     *
     * @param v1 first vector
     * @param v2 second vector
     * @return the square of the distance between v1 and v2
     */
    public static double distanceSq(Vector3D v1, Vector3D v2) {
        final double dx = v2.mX - v1.mX;
        final double dy = v2.mY - v1.mY;
        final double dz = v2.mZ - v1.mZ;
        return dx * dx + dy * dy + dz * dz;
    }

}
