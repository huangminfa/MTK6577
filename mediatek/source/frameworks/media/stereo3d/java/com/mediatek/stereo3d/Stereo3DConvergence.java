/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.stereo3d;

import android.graphics.Bitmap;

import com.mediatek.xlog.Xlog;

import java.util.Arrays;

/**
 * Stereo3DConvergence is the class that calculates the convergence offsets
 * for a stereoscopic image. Convergence refers to the proper alignment of the left
 * and right images. If the 3D image is not producing a clear image,
 * Stereo3DConvergence.execute API can be called to give offsets information for adjusting
 * left and right image positions when rendering on the surface view
 * in order to achieve a clearer image.
 */
public class Stereo3DConvergence {
    private static final String TAG = "Stereo3DConvergence";
    private static final int NUM_OF_INTERVALS = 9;
    private static final int SUCCESS = 0;

    @SuppressWarnings("PMD.SingularField")
    private final Bitmap mBitmapL; // native will refer to this left bitmap
    @SuppressWarnings("PMD.SingularField")
    private final Bitmap mBitmapR; // native will refer to this right bitmap
    private int mLeftOffsetX;
    private int mLeftOffsetY;
    private int mRightOffsetX;
    private int mRightOffsetY;
    private int mDefaultPosition;
    private final int mCropImageWidth;
    private final int mCropImageHeight;
    private final int[] mCroppingIntervalL;
    private final int[] mCroppingIntervalR;
    private final int[] mActiveFlags;

    static {
        System.loadLibrary("gia");
        System.loadLibrary("giajni");
    }

    private static native int init3DConvergence(int width, int height);
    private static native int process3DConvergence(Stereo3DConvergence convergence,
            boolean isMutable);
    private static native int close3DConvergence();

    /**
     * Constructs the Stereo3DConvergence object
     *
     * @param leftImage The left image used to calculate the convergence
     * @param rightImage The right image used to calculate the convergence
     * @hide
     */
    public Stereo3DConvergence(Bitmap leftImage, Bitmap rightImage) {
        mBitmapL = leftImage;
        mBitmapR = rightImage;

        mCropImageWidth = (int)((float)mBitmapL.getWidth() / 1.1 + 0.5) / 10 * 10;
        mCropImageHeight = (int)((float)mBitmapL.getHeight() / 1.1 + 0.5) / 10 * 10;
        mCroppingIntervalL = new int[NUM_OF_INTERVALS];
        mCroppingIntervalR = new int[NUM_OF_INTERVALS];
        mActiveFlags = new int[NUM_OF_INTERVALS];
    }

    /**
     * Executes convergence algorithm for a 3D side-by-side image and produces
     * offsets information for better alignment of the left and right images.
     *
     * @param bitmapL The left bitmap
     * @param bitmapR The right bitmap
     * @return The convergence data
     */
    public static Stereo3DConvergence execute(Bitmap bitmapL, Bitmap bitmapR) {
        return execute(bitmapL, bitmapR, false);
    }

    /**
     * Executes convergence algorithm for a 3D side-by-side image and produces
     * offsets information for better alignment of the left and right images.
     *
     * @param bitmapL The left bitmap
     * @param bitmapR The right bitmap
     * @param isRewritable Whether left and right bitmaps can be rewritable or not
     * @return The convergence data
     */
    public static Stereo3DConvergence execute(Bitmap bitmapL, Bitmap bitmapR, boolean isRewritable) {
        if (bitmapL == null || bitmapR == null) {
            Xlog.e(TAG, "Bitmaps are null");
            return null;
        }

        if (bitmapL.getWidth() != bitmapR.getWidth()) {
            Xlog.e(TAG, "Bitmaps are not valid");
            return null;
        }

        Stereo3DConvergence convergence = new Stereo3DConvergence(bitmapL, bitmapR);

        Xlog.i(TAG, "Execute convergence: " + bitmapL.getWidth() + " x " + bitmapL.getHeight());

        int result = -1;

        synchronized (Stereo3DConvergence.class) {
            result = init3DConvergence(bitmapL.getWidth(), bitmapL.getHeight());

            if (result == SUCCESS) {
                result = process3DConvergence(convergence, isRewritable);
                close3DConvergence();
            }
        }

        if (result == SUCCESS) {
            return convergence;
        }

        return null;
    }

    /**
     * Gets the width of the cropping image.
     *
     * @return The cropping image width
     */
    public int getCropImageWidth() {
        return mCropImageWidth;
    }

    /**
     * Gets the height of the cropping image.
     *
     * @return The cropping image height
     */
    public int getCropImageHeight() {
        return mCropImageHeight;
    }

    /**
     * Gets the offset of the specified image in the x direction.
     *
     * @param isLeftImage Whether the specified image is left image or not
     * @return The offset of the specified image in the x direction
     */
    public int getOffsetX(boolean isLeftImage) {
        if (isLeftImage) {
            return mLeftOffsetX;
        }

        return mRightOffsetX;
    }

    /**
     * Gets the offset of the specified image in the y direction.
     *
     * @param isLeftImage Whether the specified image is left image or not
     * @return The offset of the specified image in the y direction
     */
    public int getOffsetY(boolean isLeftImage) {
        if (isLeftImage) {
            return mLeftOffsetY;
        }

        return mRightOffsetY;
    }

    /**
     * Gets the index for the default position of the convergence.
     * The default positions are the ideal offsets for adjusting alignment of the left and
     * right images.
     *
     * @return The default position of the convergence
     */
    public int getDefaultPosition() {
        return mDefaultPosition;
    }

    /**
     * Gets the interval array for convergence adjustment for the image.
     *
     * @param isLeftImage Whether the specified image is left image or not
     * @return The intervals for the specified image
     */
    public int[] getCroppingIntervals(boolean isLeftImage) {
        if (isLeftImage) {
            return Arrays.copyOf(mCroppingIntervalL, NUM_OF_INTERVALS);
        }

        return Arrays.copyOf(mCroppingIntervalR, NUM_OF_INTERVALS);
    }

    /**
     * Gets the active flags to indicate whether each offset in the cropping intervals array
     * is an outstanding value. That is, adjusting to those outstanding offsets would achieve
     * better alignment when adjusting in different 3D depth.
     *
     * @return The active flags
     */
    public int[] getActiveFlags() {
        return Arrays.copyOf(mActiveFlags, NUM_OF_INTERVALS);
    }
}