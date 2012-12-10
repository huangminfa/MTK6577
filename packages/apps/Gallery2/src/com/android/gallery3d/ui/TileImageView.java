/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.Display;

import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DecodeUtils;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.MtkLog;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.JobContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.opengles.GL11;

import java.util.concurrent.ConcurrentHashMap;

import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.StereoHelper;
import com.mediatek.stereo3d.Stereo3DConvergence;

public class TileImageView extends GLView {
    public static final int SIZE_UNKNOWN = -1;

    @SuppressWarnings("unused")
    private static final String TAG = "TileImageView";

    // TILE_SIZE must be 2^N - 2. We put one pixel border in each side of the
    // texture to avoid seams between tiles.
    private static int TILE_SIZE = 254;
    private static final int TILE_BORDER = 1;
    private static final int UPLOAD_LIMIT = 1;

    /*
     *  This is the tile state in the CPU side.
     *  Life of a Tile:
     *      ACTIVATED (initial state)
     *              --> IN_QUEUE - by queueForDecode()
     *              --> RECYCLED - by recycleTile()
     *      IN_QUEUE --> DECODING - by decodeTile()
     *               --> RECYCLED - by recycleTile)
     *      DECODING --> RECYCLING - by recycleTile()
     *               --> DECODED  - by decodeTile()
     *               --> DECODE_FAIL - by decodeTile()
     *      RECYCLING --> RECYCLED - by decodeTile()
     *      DECODED --> ACTIVATED - (after the decoded bitmap is uploaded)
     *      DECODED --> RECYCLED - by recycleTile()
     *      DECODE_FAIL -> RECYCLED - by recycleTile()
     *      RECYCLED --> ACTIVATED - by obtainTile()
     */
    private static final int STATE_ACTIVATED = 0x01;
    private static final int STATE_IN_QUEUE = 0x02;
    private static final int STATE_DECODING = 0x04;
    private static final int STATE_DECODED = 0x08;
    private static final int STATE_DECODE_FAIL = 0x10;
    private static final int STATE_RECYCLING = 0x20;
    private static final int STATE_RECYCLED = 0x40;

    private Model mModel;
    protected BitmapTexture mBackupImage;
    protected int mLevelCount;  // cache the value of mScaledBitmaps.length

    // The mLevel variable indicates which level of bitmap we should use.
    // Level 0 means the original full-sized bitmap, and a larger value means
    // a smaller scaled bitmap (The width and height of each scaled bitmap is
    // half size of the previous one). If the value is in [0, mLevelCount), we
    // use the bitmap in mScaledBitmaps[mLevel] for display, otherwise the value
    // is mLevelCount, and that means we use mBackupTexture for display.
    private int mLevel = 0;

    // The offsets of the (left, top) of the upper-left tile to the (left, top)
    // of the view.
    private int mOffsetX;
    private int mOffsetY;

    private int mUploadQuota;
    private boolean mRenderComplete;

    private final RectF mSourceRect = new RectF();
    private final RectF mTargetRect = new RectF();

    //private final HashMap<Long, Tile> mActiveTiles = new HashMap<Long, Tile>();
    // M: change to concurrent version of HashMap
    private final ConcurrentHashMap<Long, Tile> mActiveTiles =
                      new ConcurrentHashMap<Long, Tile>();

    // The following three queue is guarded by TileImageView.this
    private TileQueue mRecycledQueue = new TileQueue();
    private TileQueue mUploadQueue = new TileQueue();
    private TileQueue mDecodeQueue = new TileQueue();

    // The width and height of the full-sized bitmap
    protected int mImageWidth = SIZE_UNKNOWN;
    protected int mImageHeight = SIZE_UNKNOWN;

    protected int mCenterX;
    protected int mCenterY;
    protected float mScale;
    protected int mRotation;

    // Temp variables to avoid memory allocation
    private final Rect mTileRange = new Rect();
    private final Rect mActiveRange[] = {new Rect(), new Rect()};

    private final TileUploader mTileUploader = new TileUploader();
    private boolean mIsTextureFreed;
    private Future<Void> mTileDecoder;
    private ThreadPool mThreadPool;
    private boolean mBackgroundTileUploaded;

    private static final boolean mIsGifAnimationSupported = 
                               MediatekFeature.isGifAnimationSupported();
    private static final boolean mIsStereoDisplaySupported = 
                        MediatekFeature.isStereoDisplaySupported();
    //added to support stereo display
    protected float mOffsetXRate = 0.0f;//0.03f;
    protected float mOffsetYRate = 0.0f;//0.15f;
    protected float mWidthRate = 1.0f;//0.9f;
    protected float mHeightRate = 1.0f;//0.85f;

    private RectF mLogicImageRect = new RectF();
    private Rect mDisplayedLogicImageRect = new Rect();
    private RectF mTempLogicImageRect = new RectF();
    protected boolean mAcEnabled = true;

    //added to support stereo display
    protected int mStereoIndex = StereoHelper.STEREO_INDEX_NONE;
    private boolean mNoFirstImage;
    
    public static interface Model {
        public int getLevelCount();
        public Bitmap getBackupImage();
        public int getImageWidth();
        public int getImageHeight();

        //added for stereo display feature
        public int getLevelCount(int stereoIndex);
        public Bitmap getFirstImage();
        public Bitmap getSecondImage();
        public int getImageWidth(int stereoIndex);
        public int getImageHeight(int stereoIndex);
        public Bitmap getTile(int level, int x, int y, 
                              int tileSize, int stereoIndex);
        public Stereo3DConvergence getStereoConvergence();


        // The method would be called in another thread
        public Bitmap getTile(int level, int x, int y, int tileSize);
        public boolean isFailedToLoad();
    }

    public TileImageView(GalleryContext context) {
        mThreadPool = context.getThreadPool();
        mTileDecoder = mThreadPool.submit(new TileDecoder());
        // If the screen resolution is relatively high, then we twice the tile
        // size to enhance the display speed
        final Display display = ((Activity) context).getWindowManager()
                .getDefaultDisplay();
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();
        if (displayWidth > 900 || displayHeight > 900) {
            TILE_SIZE = 510;
        }
        // added for debugging TileImageView
        if (MtkLog.isDebugTile()) {
            initializeDBGBitmap();
        }
    }

    public void setModel(Model model) {
        mModel = model;
        if (model != null) notifyModelInvalidated();
    }

    private void updateBackupTexture(Bitmap backup) {
        if (backup == null) {
            if (mBackupImage != null) mBackupImage.recycle();
            mBackupImage = null;
        } else {
            if (mBackupImage != null) {
                //as GIF animation, we need to refresh always!
                if (mIsGifAnimationSupported ||
                    mBackupImage.getBitmap() != backup) {
                    mBackupImage.recycle();
                    mBackupImage = new BitmapTexture(backup);
                }
            } else {
                mBackupImage = new BitmapTexture(backup);
            }
        }
    }

    public void setStereoIndex(int stereoIndex) {
        if (!mIsStereoDisplaySupported) return;
        mStereoIndex = stereoIndex;
    }

    public void notifyModelInvalidated() {
        //we postpone invalidateTiles() to save computation
        //invalidateTiles();
        if (mModel == null) {
            invalidateTiles();
            mBackupImage = null;
            mImageWidth = 0;
            mImageHeight = 0;
            mLevelCount = 0;
        } else if (!mIsStereoDisplaySupported) {
            invalidateTiles();
            updateBackupTexture(mModel.getBackupImage());
            mImageWidth = mModel.getImageWidth();
            mImageHeight = mModel.getImageHeight();
            mLevelCount = mModel.getLevelCount();
        } else {
            if (StereoHelper.STEREO_INDEX_NONE == mStereoIndex) {
                updateBackupTexture(mModel.getBackupImage());
            } else if (StereoHelper.STEREO_INDEX_FIRST == mStereoIndex) {
                updateBackupTexture(mModel.getFirstImage());
            } else {
                updateBackupTexture(mModel.getSecondImage());
            }
            mImageWidth = mModel.getImageWidth(mStereoIndex);
            mImageHeight = mModel.getImageHeight(mStereoIndex);
            int tempLevelCount = mModel.getLevelCount(mStereoIndex);
            if (mLevelCount != tempLevelCount) {
                mLevelCount = tempLevelCount;
                invalidateTiles();
            }
            Log.d(TAG, "notifyModelInvalidated:mImageWidth=" + mImageWidth +
                       " //mStereoIndex=" + mStereoIndex);
            Log.v(TAG, "notifyModelInvalidated:mImageHeight=" + mImageHeight +
                       " //mStereoIndex=" + mStereoIndex);
            Log.v(TAG, "notifyModelInvalidated:mLevelCount=" + mLevelCount+
                       " //mStereoIndex=" + mStereoIndex);
            //we check if the first image is null
            //if so, the file should be true stereo image (not 2d to 3d one)
            //we use this info for rendering
            mNoFirstImage = null == mModel.getFirstImage();
        }
        layoutTiles(mCenterX, mCenterY, mScale, mRotation);
        invalidate();
    }

    @Override
    protected void onLayout(
            boolean changeSize, int left, int top, int right, int bottom) {
        super.onLayout(changeSize, left, top, right, bottom);
        if (changeSize && mIsStereoDisplaySupported) setBackupPosition();
        if (changeSize) layoutTiles(mCenterX, mCenterY, mScale, mRotation);
    }

    // Prepare the tiles we want to use for display.
    //
    // 1. Decide the tile level we want to use for display.
    // 2. Decide the tile levels we want to keep as texture (in addition to
    //    the one we use for display).
    // 3. Recycle unused tiles.
    // 4. Activate the tiles we want.
    private void layoutTiles(int centerX, int centerY, float scale, int rotation) {
        // The width and height of this view.
        int width = getWidth();
        int height = getHeight();

        // The tile levels we want to keep as texture is in the range
        // [fromLevel, endLevel).
        int fromLevel;
        int endLevel;

        // We want to use a texture larger than or equal to the display size.
        mLevel = Utils.clamp(Utils.floorLog2(1f / scale), 0, mLevelCount);

        // We want to keep one more tile level as texture in addition to what
        // we use for display. So it can be faster when the scale moves to the
        // next level. We choose a level closer to the current scale.
        if (mLevel != mLevelCount) {
            Rect range = mTileRange;
            getRange(range, centerX, centerY, mLevel, scale, rotation);
            mOffsetX = Math.round(width / 2f + (range.left - centerX) * scale);
            mOffsetY = Math.round(height / 2f + (range.top - centerY) * scale);
            fromLevel = scale * (1 << mLevel) > 0.75f ? mLevel - 1 : mLevel;
        } else {
            // Activate the tiles of the smallest two levels.
            fromLevel = mLevel - 2;
            mOffsetX = Math.round(width / 2f - centerX * scale);
            mOffsetY = Math.round(height / 2f - centerY * scale);
        }

        fromLevel = Math.max(0, Math.min(fromLevel, mLevelCount - 2));
        endLevel = Math.min(fromLevel + 2, mLevelCount);

        Rect range[] = mActiveRange;
        for (int i = fromLevel; i < endLevel; ++i) {
            getRange(range[i - fromLevel], centerX, centerY, i, rotation);
        }

        // If rotation is transient, don't update the tile.
        if (rotation % 90 != 0) return;

        synchronized (this) {
            mDecodeQueue.clean();
            mUploadQueue.clean();
            mBackgroundTileUploaded = false;
        }

        // Recycle unused tiles: if the level of the active tile is outside the
        // range [fromLevel, endLevel) or not in the visible range.
        Iterator<Map.Entry<Long, Tile>>
                iter = mActiveTiles.entrySet().iterator();
        while (iter.hasNext()) {
            Tile tile = iter.next().getValue();
            int level = tile.mTileLevel;
            if (level < fromLevel || level >= endLevel
                    || !range[level - fromLevel].contains(tile.mX, tile.mY)) {
                iter.remove();
                recycleTile(tile);
            }
        }

        for (int i = fromLevel; i < endLevel; ++i) {
            int size = TILE_SIZE << i;
            Rect r = range[i - fromLevel];
            for (int y = r.top, bottom = r.bottom; y < bottom; y += size) {
                for (int x = r.left, right = r.right; x < right; x += size) {
                    activateTile(x, y, i);
                }
            }
        }
        invalidate();
    }

    protected synchronized void invalidateTiles() {
        mDecodeQueue.clean();
        mUploadQueue.clean();
        // TODO disable decoder
        for (Tile tile : mActiveTiles.values()) {
            recycleTile(tile);
        }
        mActiveTiles.clear();
    }

    private void getRange(Rect out, int cX, int cY, int level, int rotation) {
        getRange(out, cX, cY, level, 1f / (1 << (level + 1)), rotation);
    }

    // If the bitmap is scaled by the given factor "scale", return the
    // rectangle containing visible range. The left-top coordinate returned is
    // aligned to the tile boundary.
    //
    // (cX, cY) is the point on the original bitmap which will be put in the
    // center of the ImageViewer.
    private void getRange(Rect out,
            int cX, int cY, int level, float scale, int rotation) {

        double radians = Math.toRadians(-rotation);
        double w = getWidth();
        double h = getHeight();

        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        int width = (int) Math.ceil(Math.max(
                Math.abs(cos * w - sin * h), Math.abs(cos * w + sin * h)));
        int height = (int) Math.ceil(Math.max(
                Math.abs(sin * w + cos * h), Math.abs(sin * w - cos * h)));

        int left = (int) Math.floor(cX - width / (2f * scale));
        int top = (int) Math.floor(cY - height / (2f * scale));
        int right = (int) Math.ceil(left + width / scale);
        int bottom = (int) Math.ceil(top + height / scale);

        // align the rectangle to tile boundary
        int size = TILE_SIZE << level;
        left = Math.max(0, size * (left / size));
        top = Math.max(0, size * (top / size));
        right = Math.min(mImageWidth, right);
        bottom = Math.min(mImageHeight, bottom);

        out.set(left, top, right, bottom);
    }

    //these are temp variables that is used only for image auto/manual convergence.
    private float mTempCenterX;
    private float mTempCenterY;
    private float mTempScaledBy;

    private int mBackupCenterX;
    private int mBackupCenterY;
    private float mBackupScaledBy;
    private int mBackupRotation;

    private final boolean mRemainZoomRate = true;

    void adjustForConvergence(int centerX, int centerY, float scale, int rotation) {
        //Log.e(TAG,"adjustForConvergence(centerX="+centerX+",centerY="+centerY+
        //",scale="+scale+",rotation="+rotation+")//mStereoIndex="+mStereoIndex);
        if (!mIsStereoDisplaySupported || null == mModel) return;

        mBackupCenterX = centerX;
        mBackupCenterY = centerY;
        mBackupScaledBy = scale;
        mBackupRotation = rotation;

        float W0 = mModel.getImageWidth(); //primary image width
        float H0 = mModel.getImageHeight(); //primary image height
        float W1 = mModel.getImageWidth(mStereoIndex); //current decoded image width
        float H1 = mModel.getImageHeight(mStereoIndex); //current decoded image height

        float w = W1 * mWidthRate; //width of logic image
        float h = H1 * mHeightRate; //height of logic image
        float offsetX = W1 * mOffsetXRate; //x offset of logic image in current decoded image
        float offsetY = H1 * mOffsetYRate; //y offset of logic image in current decode image
        float scaledBy = 1.0f;
        float X = 0.0f;
        float Y = 0.0f;

        if (!mAcEnabled) {
            w = W1;
            h = H1;
            offsetX = 0.0f;
            offsetY = 0.0f;
        }

        float cropCenterX = offsetX + w / 2.0f;
        float cropCenterY = offsetY + h / 2.0f;

        float originCenterX = W1 / 2.0f;
        float originCenterY = H1 / 2.0f;

        float shiftX = (cropCenterX - originCenterX);
        float shiftY = (cropCenterY - originCenterY);

        scaledBy = Math.min(W0/W1, H0/H1);

        mTempCenterX = (float)centerX / scaledBy + shiftX;
        mTempCenterY = (float)centerY / scaledBy + shiftY;
        mTempScaledBy = scale * scaledBy;

        // define logic cropped logic image rect
        mLogicImageRect.set(offsetX, offsetY, offsetX + w, offsetY + h);

        calcuImageRect(rotation);
    }

    private void calcuImageRect(int rotation) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        // prepare the final position of logic image displayed on the screen
        mTempLogicImageRect.set(mLogicImageRect);
        // shift the very point to origin
        mTempLogicImageRect.offset(-mTempCenterX, -mTempCenterY);

        // rotate the image according to rotation info
        float absLeft = - mTempLogicImageRect.left;
        float absTop = - mTempLogicImageRect.top;
        float absRight = mTempLogicImageRect.right;
        float absBottom = mTempLogicImageRect.bottom;

        float left;
        float top;
        float right;
        float bottom;

        switch (rotation) {
            case 0:
                left = - absLeft;
                top = - absTop;
                right = absRight;
                bottom = absBottom;
                break;
            case 90:
                left = - absBottom;
                top = - absLeft;
                right = absTop;
                bottom = absRight;
                break;
            case 180:
                left = - absRight;
                top = - absBottom;
                right = absLeft;
                bottom = absTop;
                break;
            case 270:
                left = - absTop;
                top = - absRight;
                right = absBottom;
                bottom = absLeft;
                break;
            default: throw new IllegalArgumentException(String.valueOf(rotation));
        }
        // scale the image to final size
        mTempLogicImageRect.set(
            left * mTempScaledBy,
            top * mTempScaledBy,
            right * mTempScaledBy,
            bottom * mTempScaledBy);
        // shift the very point to screen center.
        mTempLogicImageRect.offset(viewWidth / 2f, viewHeight / 2f);
        // convert float version to int version
        mDisplayedLogicImageRect.set(
            Math.round(mTempLogicImageRect.left),
            Math.round(mTempLogicImageRect.top),
            Math.round(mTempLogicImageRect.right),
            Math.round(mTempLogicImageRect.bottom));
        if (!mDisplayedLogicImageRect.intersect(0, 0, viewWidth, viewHeight)) {
            //Log.w(TAG,"adjustForConvergence:logic image out of screen//"
            //          + mStereoIndex);
        }
    }

    public boolean setBackupPosition() {
        return setPosition(mBackupCenterX, mBackupCenterY, 
                           mBackupScaledBy, mBackupRotation);
    }

    public boolean setPosition(int centerX, int centerY, float scale, int rotation) {
        //adjust position and scale ...
        if (mIsStereoDisplaySupported && null != mModel) {
            adjustForConvergence(centerX, centerY, scale, rotation);
            centerX = (int)mTempCenterX;
            centerY = (int)mTempCenterY;
            scale = mTempScaledBy;
        }

        if (mCenterX == centerX
                && mCenterY == centerY && mScale == scale) return false;
        mCenterX = centerX;
        mCenterY = centerY;
        mScale = scale;
        mRotation = rotation;
        layoutTiles(centerX, centerY, scale, rotation);
        invalidate();
        return true;
    }

    public void freeTextures() {
        mIsTextureFreed = true;

        if (mTileDecoder != null) {
            // We first try to remove mTileDecoder from the internal queue of
            // mThreadPool. Success of this operation means mTileDecoder has not
            // run yet, otherwise we should cancel this running mTileDecoder and
            // wait it done
            if (!mThreadPool.remove((Runnable)mTileDecoder)) {
                mTileDecoder.cancel();
                mTileDecoder.get();
            }
            mTileDecoder = null;
        }

        for (Tile texture : mActiveTiles.values()) {
            texture.recycle();
        }
        mTileRange.set(0, 0, 0, 0);
        mActiveTiles.clear();

        synchronized (this) {
            mUploadQueue.clean();
            mDecodeQueue.clean();
            Tile tile = mRecycledQueue.pop();
            while (tile != null) {
                tile.recycle();
                tile = mRecycledQueue.pop();
            }
        }
        updateBackupTexture(null);
    }

    public void prepareTextures() {
        if (mTileDecoder == null) {
            mTileDecoder = mThreadPool.submit(new TileDecoder());
        }
        if (mIsTextureFreed) {
            layoutTiles(mCenterX, mCenterY, mScale, mRotation);
            mIsTextureFreed = false;
            updateBackupTexture(mModel != null ? mModel.getBackupImage() : null);
        }
    }

    @Override
    protected void render(GLCanvas canvas) {
        mUploadQuota = UPLOAD_LIMIT;
        mRenderComplete = true;

        int level = mLevel;
        int rotation = mRotation;

        if (rotation != 0) {
            canvas.save(GLCanvas.SAVE_FLAG_MATRIX);
            int centerX = getWidth() / 2, centerY = getHeight() / 2;
            canvas.translate(centerX, centerY, 0);
            canvas.rotate(rotation, 0, 0, 1);
            canvas.translate(-centerX, -centerY, 0);
        }
        GL11 gl = canvas.getGLInstance();
        retrieveScissorBox(gl);
        try {
            if (isTileViewVisible()) {
                setScissorBox(gl);
                //confine the current drawing within the logical image bound
                if (level != mLevelCount) {
                    int size = (TILE_SIZE << level);
                    float length = size * mScale;
                    Rect r = mTileRange;
                    long time = 0;
                    for (int ty = r.top, i = 0; ty < r.bottom; ty += size, i++) {
                        float y = mOffsetY + i * length;
                        for (int tx = r.left, j = 0; tx < r.right; tx += size, j++) {
                            float x = mOffsetX + j * length;
                            if (MtkLog.isDebugTile()) {
                                time = SystemClock.uptimeMillis();
                                MtkLog.d(TAG, "=> drawTile: level=" + level + ", x=" +
                                              x + ", y=" + y + ", length=" + length);
                            }
                            drawTile(canvas, tx, ty, level, x, y, length);
                            if (MtkLog.isDebugTile()) {
                                time = SystemClock.uptimeMillis() - time;
                                MtkLog.d(TAG, "<= drawTile: level=" + level + ", x=" +
                                              x + ", y=" + y + ", length=" + length +
                                              ", consumed (" + time + " ms)");
                            }
                        }
                    }
                } else if (mBackupImage != null) {
                    mBackupImage.draw(canvas, mOffsetX, mOffsetY,
                            Math.round(mImageWidth * mScale),
                            Math.round(mImageHeight * mScale));
                }
            }
        } finally {
            if (rotation != 0) canvas.restore();
            restoreScissorBox(gl);
        }

        if (MtkLog.isDebugTile()) {
            MtkLog.w(TAG, "render is " + (mRenderComplete ? "COMPLETE" : "ONGOING"));
        }
        if (mRenderComplete) {
            if (!mBackgroundTileUploaded) uploadBackgroundTiles(canvas);
        } else {
            invalidate();
        }
    }

    private int[] mTempScissorBox = new int[4];
    private void retrieveScissorBox(GL11 gl) {
        if (mAcEnabled) {
            gl.glGetIntegerv(GL11.GL_SCISSOR_BOX, mTempScissorBox, 0);
        }
    }
    private void restoreScissorBox(GL11 gl) {
        if (mAcEnabled) {
            gl.glScissor(mTempScissorBox[0], mTempScissorBox[1], 
                         mTempScissorBox[2], mTempScissorBox[3]);
        }
    }

    private void setScissorBox(GL11 gl) {
        if (mIsStereoDisplaySupported && mAcEnabled) {
            if (null == getGLRoot()) {
                Log.w(TAG, "setScissorBox:why GLRoot is null!");
                return;
            }
            int width = getWidth();
            int height = getHeight();
            if (getGLRoot().getStereoMode()) {
                int stereoPass = getGLRoot().getStereoPassId();
                if (MediatekFeature.STEREO_DISPLAY_LEFT_PASS == stereoPass) {
                    gl.glScissor(mDisplayedLogicImageRect.left / 2,
                                 height - mDisplayedLogicImageRect.bottom,
                                 (mDisplayedLogicImageRect.right - 
                                  mDisplayedLogicImageRect.left) / 2,
                                 (mDisplayedLogicImageRect.bottom -
                                  mDisplayedLogicImageRect.top));
                } else {
                    gl.glScissor(mDisplayedLogicImageRect.left / 2 + width / 2,
                                 height - mDisplayedLogicImageRect.bottom,
                                 (mDisplayedLogicImageRect.right - 
                                  mDisplayedLogicImageRect.left) / 2,
                                 (mDisplayedLogicImageRect.bottom -
                                  mDisplayedLogicImageRect.top));
                }
            } else {
                gl.glScissor(mDisplayedLogicImageRect.left,
                             height - mDisplayedLogicImageRect.bottom,
                             (mDisplayedLogicImageRect.right - 
                              mDisplayedLogicImageRect.left),
                             (mDisplayedLogicImageRect.bottom -
                              mDisplayedLogicImageRect.top));
            }
        }
    }

    private boolean isTileViewVisible() {
        boolean visible = true;
        if (mIsStereoDisplaySupported) {
            if (null != getGLRoot() && getGLRoot().getStereoMode()) {
                int stereoPass = getGLRoot().getStereoPassId();
                if (MediatekFeature.STEREO_DISPLAY_LEFT_PASS == stereoPass) {
                    if (mNoFirstImage) {
                        if (StereoHelper.STEREO_INDEX_NONE != mStereoIndex) {
                            visible = false;
                        }
                    } else {
                        if (StereoHelper.STEREO_INDEX_FIRST != mStereoIndex) {
                            visible = false;
                        }
                    }
                } else {
                    if (StereoHelper.STEREO_INDEX_SECOND != mStereoIndex) {
                        visible = false;
                    }
                }
            } else {
                //only the 2d image can be visible
                if (StereoHelper.STEREO_INDEX_NONE != mStereoIndex) {
                    visible = false;
                }
            }
        }
        return visible;
    }


    private void uploadBackgroundTiles(GLCanvas canvas) {
        if (MtkLog.isDebugTile()) {
            MtkLog.e(TAG, "begin uploadBackgroundTiles");
        }
        mBackgroundTileUploaded = true;
        for (Tile tile : mActiveTiles.values()) {
            if (!tile.isContentValid(canvas)) queueForDecode(tile);
        }
    }

    void queueForUpload(Tile tile) {
        synchronized (this) {
            mUploadQueue.push(tile);
        }
        if (mTileUploader.mActive.compareAndSet(false, true)) {
            getGLRoot().addOnGLIdleListener(mTileUploader);
        }
    }

    synchronized void queueForDecode(Tile tile) {
        if (tile.mTileState == STATE_ACTIVATED) {
            tile.mTileState = STATE_IN_QUEUE;
            if (mDecodeQueue.push(tile)) notifyAll();
            if (MtkLog.isDebugTile()) {
                MtkLog.i(TAG, "[" + tile + "] queued for decode!!");
            }
        }
    }

    boolean decodeTile(Tile tile) {
        synchronized (this) {
            if (tile.mTileState != STATE_IN_QUEUE) return false;
            tile.mTileState = STATE_DECODING;
        }
        boolean decodeComplete = tile.decode();
        synchronized (this) {
            if (tile.mTileState == STATE_RECYCLING) {
                tile.mTileState = STATE_RECYCLED;
                tile.mDecodedTile = null;
                mRecycledQueue.push(tile);
                return false;
            }
            tile.mTileState = decodeComplete ? STATE_DECODED : STATE_DECODE_FAIL;
            return decodeComplete;
        }
    }

    private synchronized Tile obtainTile(int x, int y, int level) {
        Tile tile = mRecycledQueue.pop();
        if (tile != null) {
            tile.mTileState = STATE_ACTIVATED;
            tile.update(x, y, level);
            return tile;
        }
        return new Tile(x, y, level);
    }

    synchronized void recycleTile(Tile tile) {
        if (tile.mTileState == STATE_DECODING) {
            tile.mTileState = STATE_RECYCLING;
            return;
        }
        tile.mTileState = STATE_RECYCLED;
        tile.mDecodedTile = null;
        mRecycledQueue.push(tile);
    }

    private void activateTile(int x, int y, int level) {
        Long key = makeTileKey(x, y, level);
        Tile tile = mActiveTiles.get(key);
        if (tile != null) {
            if (tile.mTileState == STATE_IN_QUEUE) {
                tile.mTileState = STATE_ACTIVATED;
            }
            return;
        }
        tile = obtainTile(x, y, level);
        mActiveTiles.put(key, tile);
    }

    private Tile getTile(int x, int y, int level) {
        return mActiveTiles.get(makeTileKey(x, y, level));
    }

    private static Long makeTileKey(int x, int y, int level) {
        long result = x;
        result = (result << 16) | y;
        result = (result << 16) | level;
        return Long.valueOf(result);
    }

    private class TileUploader implements GLRoot.OnGLIdleListener {
        AtomicBoolean mActive = new AtomicBoolean(false);

        @Override
        public boolean onGLIdle(GLRoot root, GLCanvas canvas) {
            int quota = UPLOAD_LIMIT;
            Tile tile;
            while (true) {
                synchronized (TileImageView.this) {
                    tile = mUploadQueue.pop();
                //}
                    if (tile == null || quota <= 0) break;
                    if (!tile.isContentValid(canvas)) {
                        //Utils.assertTrue(tile.mTileState == STATE_DECODED);
                        if (tile.mTileState != STATE_DECODED) {
                            MtkLog.e(TAG, tile + " not in STATE_DECODED when trying to updateContent");
                            continue;
                        }
                        
                        tile.updateContent(canvas);
                        --quota;
                    }
                }
            }
            mActive.set(tile != null);
            return tile != null;
        }
    }

    // Draw the tile to a square at canvas that locates at (x, y) and
    // has a side length of length.
    public void drawTile(GLCanvas canvas,
            int tx, int ty, int level, float x, float y, float length) {
        RectF source = mSourceRect;
        RectF target = mTargetRect;
        target.set(x, y, x + length, y + length);
        source.set(0, 0, TILE_SIZE, TILE_SIZE);

        Tile tile = getTile(tx, ty, level);
        if (tile != null) {
            if (!tile.isContentValid(canvas)) {
                if (tile.mTileState == STATE_DECODED) {
                    if (mUploadQuota > 0) {
                        --mUploadQuota;
                        tile.updateContent(canvas);
                    } else {
                        mRenderComplete = false;
                    }
                } else if (tile.mTileState != STATE_DECODE_FAIL){
                    mRenderComplete = false;
                    queueForDecode(tile);
                }
            }
            if (drawTile(tile, canvas, source, target)) return;
        }
        if (mBackupImage != null) {
            BasicTexture backup = mBackupImage;
            int size = TILE_SIZE << level;
            float scaleX = (float) backup.getWidth() / mImageWidth;
            float scaleY = (float) backup.getHeight() / mImageHeight;
            source.set(tx * scaleX, ty * scaleY, (tx + size) * scaleX,
                    (ty + size) * scaleY);
            canvas.drawTexture(backup, source, target);
        }
    }

    // TODO: avoid drawing the unused part of the textures.
    static boolean drawTile(
            Tile tile, GLCanvas canvas, RectF source, RectF target) {
        boolean isParentDrawn = false;
        while (true) {
            if (tile.isContentValid(canvas)) {
                // offset source rectangle for the texture border.
                source.offset(TILE_BORDER, TILE_BORDER);
                canvas.drawTexture(tile, source, target);
                if (MtkLog.isDebugTile()) {
                    if (!isParentDrawn && !tile.mIsDrawn) {
                        MtkLog.e(TAG, "[" + tile + "] is actually drawn!!");
                    }
                    tile.mIsDrawn = true;
                }
                return true;
            }

            // Parent can be divided to four quads and tile is one of the four.
            Tile parent = tile.getParentTile();
            if (parent == null) return false;
            if (tile.mX == parent.mX) {
                source.left /= 2f;
                source.right /= 2f;
            } else {
                source.left = (TILE_SIZE + source.left) / 2f;
                source.right = (TILE_SIZE + source.right) / 2f;
            }
            if (tile.mY == parent.mY) {
                source.top /= 2f;
                source.bottom /= 2f;
            } else {
                source.top = (TILE_SIZE + source.top) / 2f;
                source.bottom = (TILE_SIZE + source.bottom) / 2f;
            }
            tile = parent;
            isParentDrawn = true;
        }
    }

    private class Tile extends UploadedTexture {
        int mX;
        int mY;
        int mTileLevel;
        Tile mNext;
        Bitmap mDecodedTile;
        volatile int mTileState = STATE_ACTIVATED;
        boolean mIsDrawn = false;

        public Tile(int x, int y, int level) {
            mX = x;
            mY = y;
            mTileLevel = level;
        }

        @Override
        protected void onFreeBitmap(Bitmap bitmap) {
            bitmap.recycle();
        }

        boolean decode() {
            // Get a tile from the original image. The tile is down-scaled
            // by (1 << mTilelevel) from a region in the original image.
            int tileLength = (TILE_SIZE + 2 * TILE_BORDER);
            int borderLength = TILE_BORDER << mTileLevel;
            try {
                if (!mIsStereoDisplaySupported) {
                    mDecodedTile = DecodeUtils.ensureGLCompatibleBitmap(
                            mModel.getTile(mTileLevel, mX - borderLength,
                            mY - borderLength, tileLength));
                } else {
                    mDecodedTile = DecodeUtils.ensureGLCompatibleBitmap(
                            mModel.getTile(mTileLevel, mX - borderLength,
                            mY - borderLength, tileLength, mStereoIndex));
                }
                // draw the translucent overlay onto current bitmap;
                if (MtkLog.isDebugTile() && mDbgOverlay != null) {
                    long time = SystemClock.uptimeMillis();
                    if (!mDecodedTile.isMutable()) {
                        mDecodedTile = mDecodedTile.copy(mDecodedTile.getConfig(), true);
                    }
                    Canvas c = new Canvas(mDecodedTile);
                    c.drawBitmap(mDbgOverlay,
                                 new Rect(0, 0, mDbgOverlay.getWidth(),
                                                mDbgOverlay.getHeight()),
                                 new Rect(0, 0, mDecodedTile.getWidth(),
                                                mDecodedTile.getHeight()), null);
                    time = SystemClock.uptimeMillis() - time;
                    MtkLog.w(TAG, "{OVERHEAD} repaint decoded tile takes (" + time + ") ms");
                }
            } catch (Throwable t) {
                Log.w(TAG, "fail to decode tile", t);
            }
            return mDecodedTile != null;
        }

        @Override
        protected Bitmap onGetBitmap() {
            Utils.assertTrue(mTileState == STATE_DECODED);
            Bitmap bitmap = mDecodedTile;
            mDecodedTile = null;
            mTileState = STATE_ACTIVATED;
            return bitmap;
        }

        public void update(int x, int y, int level) {
            mX = x;
            mY = y;
            mTileLevel = level;
            invalidateContent();
        }

        public Tile getParentTile() {
            if (mTileLevel + 1 == mLevelCount) return null;
            int size = TILE_SIZE << (mTileLevel + 1);
            int x = size * (mX / size);
            int y = size * (mY / size);
            return getTile(x, y, mTileLevel + 1);
        }

        @Override
        public String toString() {
//            return String.format("tile(%s, %s, %s / %s)",
//                    mX / TILE_SIZE, mY / TILE_SIZE, mLevel, mLevelCount);
            return String.format("tile(%s, %s, %s / %s)",
                  mX, mY, mTileLevel, mLevelCount);
        }
    }

    private static class TileQueue {
        private Tile mHead;

        public Tile pop() {
            Tile tile = mHead;
            if (tile != null) mHead = tile.mNext;
            return tile;
        }

        public boolean push(Tile tile) {
            boolean wasEmpty = mHead == null;
            tile.mNext = mHead;
            mHead = tile;
            return wasEmpty;
        }

        public void clean() {
            mHead = null;
        }
    }

    private class TileDecoder implements ThreadPool.Job<Void> {

        private CancelListener mNotifier = new CancelListener() {
            @Override
            public void onCancel() {
                synchronized (TileImageView.this) {
                    TileImageView.this.notifyAll();
                }
            }
        };

        @Override
        public Void run(JobContext jc) {
            jc.setMode(ThreadPool.MODE_NONE);
            jc.setCancelListener(mNotifier);
            long time = 0;
            while (!jc.isCancelled()) {
                Tile tile = null;
                synchronized(TileImageView.this) {
                    tile = mDecodeQueue.pop();
                    if (tile == null && !jc.isCancelled()) {
                        Utils.waitWithoutInterrupt(TileImageView.this);
                    }
                }
                if (tile == null) continue;
                if (MtkLog.isDebugTile()) {
                    time = SystemClock.uptimeMillis();
                    MtkLog.d(TAG, "[TileDecoder]: => decodeTile: [" + tile + "]");
                }
                boolean decodeFinish = decodeTile(tile);
                if (MtkLog.isDebugTile()) {
                    time = SystemClock.uptimeMillis() - time;
                    MtkLog.d(TAG, "[TileDecoder]: <= decodeTile: [" + tile + "]: consumed (" + time + " ms)");
                }
                if (decodeFinish) queueForUpload(tile);
            }
            return null;
        }
    }
    
    // added for performance test debug purpose:
    // draw an translucent overlay on to the texture
    // to visually indicate the completion of tile decode process
    private Bitmap mDbgOverlay;
    private void initializeDBGBitmap() {
        if (mDbgOverlay != null) return;
        long time = SystemClock.uptimeMillis();
        int colorint = Color.argb(100, 255, 0, 0);
        int[] colorArray = new int[4]; 
        for (int i = 0; i < 4; ++i) {
            colorArray[i] = colorint;
        }
        mDbgOverlay = Bitmap.createBitmap(colorArray, 2, 2, Bitmap.Config.ARGB_8888);
        time = SystemClock.uptimeMillis() - time;
        Log.w(TAG, "{OVERHEAD} initializeBitmap takes (" + time + ") ms");
    }
}
