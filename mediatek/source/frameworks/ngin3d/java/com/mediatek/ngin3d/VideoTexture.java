package com.mediatek.ngin3d;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;

/**
 * The texture comes from video streaming
 */
public class VideoTexture {
    Context mCtx;
    Uri mUri;
    

    public VideoTexture(Context context, Uri uri) {
        mCtx = context;
        mUri = uri;
    }

    public Uri getUri() {
        return mUri;
    }

    private boolean mUpdateLayer;
    private final byte[] mLock = new byte[0];

    // Update texture image in GL thread if video source has notified new frame is available.
    public void applyUpdate() {
        if (mSurface == null) {
            return;
        }

        synchronized (mLock) {
            if (!mUpdateLayer) {
                return;
            }
            mUpdateLayer = false;
            mSurface.updateTexImage();
            if (mListener != null) {
                mListener.onSurfaceTextureUpdated(mSurface);
            }
        }
    }

    SurfaceTexture mSurface;
    VideoProxy mVideo;
    SurfaceTexture.OnFrameAvailableListener mUpdateListener;
    
    public void genSurfaceTexture(int textureName) {
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }

        mSurface = new SurfaceTexture(textureName);

        mUpdateListener = new SurfaceTexture.OnFrameAvailableListener() {
            public void onFrameAvailable(android.graphics.SurfaceTexture surfaceTexture) {
                synchronized (mLock) {
                    mUpdateLayer = true;
                }
            }
        };

        mSurface.setOnFrameAvailableListener(mUpdateListener);
        mVideo = new VideoProxy(mCtx, this);
        mVideo.setVideoURI(mUri);
        if (mListener != null) {
            mListener.onSurfaceTextureAvailable(mSurface);
        }
        mVideo.start();
    }

    // From TextureView
    public interface SurfaceTextureListener  {
        void onSurfaceTextureAvailable(android.graphics.SurfaceTexture surfaceTexture);
        void onSurfaceTextureSizeChanged(android.graphics.SurfaceTexture surfaceTexture);
        boolean onSurfaceTextureDestroyed(android.graphics.SurfaceTexture surfaceTexture);
        void onSurfaceTextureUpdated(android.graphics.SurfaceTexture surfaceTexture);
    }

    SurfaceTextureListener mListener;
    public SurfaceTextureListener getSurfaceTextureListener() {
        return mListener;
    }

    public void setSurfaceTextureListener(SurfaceTextureListener listener) {
        mListener = listener;
    }

    public void play() {
        if (mVideo != null) {
            mVideo.start();
        }
    }

    public void stop() {
        if (mVideo != null) {
            mVideo.stopPlayback();
        }
    }
}
