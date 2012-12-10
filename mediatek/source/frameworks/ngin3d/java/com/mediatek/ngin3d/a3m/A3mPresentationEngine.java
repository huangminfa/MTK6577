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
 * Ngin3D Presentation Layer for A3M Engine
 */
package com.mediatek.ngin3d.a3m;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.util.Log;

import com.mediatek.a3m.A3m;
import com.mediatek.a3m.AssetPool;
import com.mediatek.a3m.Camera;
import com.mediatek.a3m.Matrix4;
import com.mediatek.a3m.Renderer;
import com.mediatek.a3m.SceneNode;
import com.mediatek.a3m.SceneUtility;
import com.mediatek.a3m.Vector3;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Transaction;
import com.mediatek.ngin3d.animation.MasterClock;
import com.mediatek.ngin3d.debugtools.android.serveragent.SocketManager;
import com.mediatek.ngin3d.presentation.Graphics2d;
import com.mediatek.ngin3d.presentation.Graphics3d;
import com.mediatek.ngin3d.presentation.IObject3d;
import com.mediatek.ngin3d.presentation.ImageDisplay;
import com.mediatek.ngin3d.presentation.Model3d;
import com.mediatek.ngin3d.presentation.Presentation;
import com.mediatek.ngin3d.presentation.PresentationEngine;
import com.mediatek.ngin3d.utils.Ngin3dDebugUtils;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of presentation engine using A3M.
 *
 * @hide
 */
public class A3mPresentationEngine implements PresentationEngine {
    private static final String TAG = "A3mPresentationEngine";

    // Default Camera Z position.
    // The value of 1111 is a remnant from AfterEffects default position.
    // Demo code based on this still relies on this default value.
    // New code must NOT rely on this as it may be changed when all legacy
    // code is updated or disused.
    private static final float DEFAULT_Z_CAM = 1111.f;

    private final Stage mStage;
    private final TextureCache mTextureCache;
    private Resources mResources;
    private Renderer mRenderer;
    private AssetPool mAssetPool;
    private SceneNode mTrueRootNode;
    private SceneNode mRootNode;

    // Camera device, local copy of position, and scene node it is pointed at
    private Camera mActiveCamera;
    private Camera mCamera;
    private Point mCameraPos = new Point(0.0f, 0.0f, DEFAULT_Z_CAM);
    private Point mCameraLookAt = new Point(0.0f, 0.0f, 0.0f);
    private float mCameraFov = 40.0f;

    // screen dimensions set on initialisation
    private int mWidth;
    private int mHeight;

    // Default projection is camera fixed looking at a planar 'stage'
    private int mProjectionMode = Stage.UI_PERSPECTIVE;

    // Z-clipping values
    private float mZNear = 2.f;
    private float mZFar = 3000.f;

    // Stereo: Distance in camera-Z to the plane where stereo cameras match
    private float mCameraFocalLength = DEFAULT_Z_CAM;
    // Distance between 'eye's.  Set zero to disable by default.
    private float mCameraEyeSeparation;

    // View-Projection Matrix
    private Matrix4 mVpMatrix;
    private Matrix4 mInverseVpMatrix;
    private boolean mVpMatrixDirty = true;

    private boolean mRenderingPaused;

    // Timing-related
    private static final int NANOSECS_PER_SECOND = 1000000000;
    private long mTickTime;
    private long mTimeOrigin;
    private int mMaxFPS;
    private FpsLimiter mFpsLimiter;
    private int mFrameCount;
    private long mFrameCountingStartTimeNs;
    private double mFPS;

    protected RenderCallback mRenderCallback;

    /**
     * Imports Stage object to initialize this A3M Presentation Engine.
     *
     * @param stage stage object to be used for this engine
     */
    public A3mPresentationEngine(Stage stage) {
        mStage = stage;
        mTextureCache = new TextureCache(this);
    }

    SocketManager mSocketManager;
    Ngin3dDebugUtils mNgin3dDt;

    /**
     * Initializes this  with specific width and height
     *
     * @param width     in pixels
     * @param height    in pixels
     * @param resources Resources
     * @param cacheDir  Folder for caching items
     * @param enableNDB switch on/off ngin3d debug bridge
     */
    public void initialize(int width, int height, Resources resources,
                           String cacheDir, boolean enableNDB) {
        A3m.init();

        float centX = (float) width / 2;
        float centY = (float) height / 2;

        mWidth = width;
        mHeight = height;
        mAssetPool = new AssetPool();

        mCameraPos = new Point(0.0f, 0.0f, DEFAULT_Z_CAM);

        // If Resources were passed, register them with the asset pool
        if (resources != null) {
            mAssetPool.registerSource(resources);
            mAssetPool.registerSource(resources.getAssets());

            mResources = resources;
        }

        mAssetPool.registerSource("//sdcard/ngin3d/assets");

        if (cacheDir != null) {
            mAssetPool.setCacheSource(cacheDir);
        }

        mRenderer = new Renderer(mAssetPool);

        mFpsLimiter = new FpsLimiter();

        // Info: The existance of both TrueRootNode and RootNode allows us
        // to map between different coordinate systems.  For example it's used
        // later to map between Perspective (Y-up) and UI-Perspective (Y-down)
        mTrueRootNode = new SceneNode();
        mRootNode = new SceneNode();
        mRootNode.setParent(mTrueRootNode);

        // Setting up a nominal camera on initialisation prevents a null
        // camera being given to the renderer.
        mCamera = new Camera();
        mCamera.setParent(mRootNode);
        mActiveCamera = mCamera;

        // Initialise camera setup
        updateCamera();

        enableMipMaps(true);
        mStage.realize(this);

        if (enableNDB) {
            mNgin3dDt = new Ngin3dDebugUtils(this, mStage);
            mSocketManager = new SocketManager(31286, mNgin3dDt);
            mSocketManager.startListen();
        }
        mTimeOrigin = System.nanoTime();
    }

    public void initialize(int width, int height) {
        initialize(width, height, null, null, false);
    }

    public void initialize(int width, int height, boolean debug) {
        initialize(width, height, null, null, debug);
    }

    public void initialize(int width, int height, Resources resources) {
        initialize(width, height, resources, null, false);
    }

    /**
     * Uninitialize this object.
     */
    public void uninitialize() {
        if (mSocketManager != null) {
            mSocketManager.stopListen();
        }
        mSocketManager = null;
        mNgin3dDt = null;

        mStage.unrealize();

        // Explicitly release device resources
        if (mAssetPool != null) {
            mAssetPool.releaseResources();
        }

        // Do the last tick so that animation can complete.
        MasterClock.getDefault().tick();

        mRenderer = null;
        mAssetPool = null;
        mTrueRootNode = null;
        mRootNode = null;
        mCamera = null;

        mCameraPos = null;
    }

    // ------------------------------------------------------------------------
    // Camera and projections
    // ------------------------------------------------------------------------

    // Update the projection following a change of screen size (i.e.
    // orientation) or camera position (posn.z used in FoV calc), etc.
    private void applyProjection() {

        switch (mProjectionMode) {
        case Stage.ORTHOGRAPHIC:
            setOrthogonalProjection(mWidth, -mHeight);
            break;

        case Stage.PERSPECTIVE:
            setClassicPerspectiveProjection();
            break;

        case Stage.UI_PERSPECTIVE:
        case Stage.UI_PERSPECTIVE_LHC:
            setUiPerspectiveProjection();
            break;

        default:
            break;
        }

        // Cause the View Projection matrix to be recalculated
        mVpMatrixDirty = true;
    }

    /**
     * Set new projection mode
     */
    public void setProjectionMode(int mode) {
        mProjectionMode = mode;
        updateCamera();
    }

    /**
     * Set the near and far clipping distances
     */
    public void setClipDistances(float zNear, float zFar) {
        mZNear = zNear;
        mZFar = zFar;
    }

    /**
     * Set camera Z position.
     * Mainly relevant to the UI_PERSPECTIVE view where X and Y are fixed
     */
    public void setCameraZ(float zCamera) {
        mCameraPos.z = zCamera;
        updateCamera();
    }

    /**
     * Set camera field of view in degrees.
     */
    public void setCameraFov(float fov) {
        mCameraFov = (float)Math.toRadians(fov);
        updateCamera();
    }

    private void setOrthogonalProjection(float widthOfViewVolume,
                                         float heightOfViewVolume) {
        // \todo implement
    }

    /**
     * Operations necessary when switching to a "UI" perspective projection.
     * Mainly - calculate the field of view to pass to the engine via
     * the camera properties, and recalculate the projection matrix.
     */
    private void setUiPerspectiveProjection() {

        // Ensure camera exists
        if (mCamera == null) {
            Log.e(TAG, "No camera defined in setUiPerspectiveProjection");
            return;
        }

        // Initially assume portrait
        float largerDim = (float) mHeight;
        float smallerDim = (float) mWidth;

        // correct if landscape
        if (mWidth > mHeight) {
            largerDim = (float) mWidth;
            smallerDim = (float) mHeight;
        }

        float distToScreen = Math.abs(mCameraPos.z);

        float fov = (float) (Math.atan((smallerDim / 2) / Math.abs(distToScreen)) * 2);

        switch (mProjectionMode) {
        case Stage.UI_PERSPECTIVE:
            // The UI projection has Y-down so invert Y here
            mTrueRootNode.setScale(new Vector3(1, -1, 1));
            mCamera.setScale(new Vector3(1, -1, 1));
            break;
        case Stage.UI_PERSPECTIVE_LHC:
            // The Left-handed legacy projection inverts Z as well as Y
            mTrueRootNode.setScale(new Vector3(1, -1, -1));
            mCamera.setScale(new Vector3(1, -1, -1));
            break;
        default:
            Log.e(TAG, "Invalid mode in setUiPerspectiveProjection.");
            return;
        }

        mCamera.setFov(fov);

        // While configuring the camera, update any other parameters that
        // may have changed, noting that stereo settings are configured whenever
        // stereo is enabled/disabled
        mCamera.setNear(mZNear);
        mCamera.setFar(mZFar);
    }

    /**
     * Operations necessary when switching to a vanilla perspective projection.
     */
    private void setClassicPerspectiveProjection() {
        // Regular perspective projection is free of axis flipping workarounds
        mTrueRootNode.setScale(new Vector3(1, 1, 1));
        mCamera.setScale(new Vector3(1, 1, 1));

        // FOV is set by the client in PERSPECTIVE projection mode
        mCamera.setFov(mCameraFov);

        // Set addition parameters that may have changed
        mCamera.setNear(mZNear);
        mCamera.setFar(mZFar);
    }

    /**
     * Updates the camera according to the current projection mode.
     */
    private void updateCamera() {

        // Abort if there's no camera
        if (mCamera == null) {
            Log.e(TAG, "No camera defined in updateCamera");
            return;
        }

        Vector3 cameraPosition = Vector3.ZERO;
        Vector3 targetPosition = Vector3.ZERO;

        switch (mProjectionMode) {
        case Stage.ORTHOGRAPHIC:
            // Not yet implemented
            break;

        case Stage.PERSPECTIVE:
            // Camera setup is fully specified by client
            cameraPosition = new Vector3(
                mCameraPos.x, mCameraPos.y, mCameraPos.z);
            targetPosition = new Vector3(
                mCameraLookAt.x, mCameraLookAt.y, mCameraLookAt.z);
            break;

        case Stage.UI_PERSPECTIVE_LHC:
            // Support for Legacy Apps.  Apps that use LHC may be
            // relying on the OLD default value of -1111.
            // The app may have set a Z value since initialisation which
            // we must retain, even after a pause and resume.
            // The assumption here is that legacy apps will only ever set
            // a negative Z camera value, so if the Z value at this point
            // is still the new/positive default, it needs negating.
            if (mCameraPos.z == DEFAULT_Z_CAM) {
                mCameraPos.z = -DEFAULT_Z_CAM;
            }
            // Now continue into the UI-Perspective code...
        case Stage.UI_PERSPECTIVE:
            // Camera is fixed mid-screen
            float centX = (float) mWidth / 2;
            float centY = (float) mHeight / 2;

            cameraPosition = new Vector3(centX, centY, mCameraPos.z);
            targetPosition = new Vector3(centX, -centY, 0);
            break;
        default:
            break;
        }

        // Set positions of camera and look-at point
        mCamera.setPosition(cameraPosition);
        SceneUtility.pointAt(mCamera, targetPosition);

        // Camera position used in calculation of FoV etc.
        applyProjection();
    }

    /**
     * Positions and aims the camera for this stage.
     * Only the camera Z position is used in UI_PERSPECTIVE projection mode.
     *
     * @param pos    camera position
     * @param lookAt camera focus point position
     */
    public void setCamera(Point pos, Point lookAt) {
        mCameraPos = pos;
        mCameraLookAt = lookAt;

        updateCamera();
    }

    /**
     * Sets the currently active debug camera.
     * Passing an empty string activates the default camera.
     *
     * This function is deprecated and marked for removal in the near future.
     *
     * @deprecated
     */
    public void setDebugCamera(String name) {
        if (name.isEmpty()) {
            mActiveCamera = mCamera;
        } else {
            SceneNode node = mRootNode.find(name);

            if (Camera.class.isInstance(node)) {
                mActiveCamera = (Camera)node;
            }
        }
    }

    /**
     * Returns a list of names of cameras in the scene.
     *
     * This function is deprecated and marked for removal in the near future.
     *
     * @deprecated
     */
    public String[] getDebugCameraNames() {
        List<String> names = new ArrayList<String>();
        compileDebugCameraNames(names, mRootNode);
        String[] namesArray = new String[names.size()];
        names.toArray(namesArray);
        return namesArray;
    }

    /**
     * Traverses the scene graph and compiles a list of all the cameras.
     *
     * This function is deprecated and marked for removal in the near future.
     *
     * @deprecated
     */
    private void compileDebugCameraNames(List<String> names, SceneNode node) {
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            SceneNode child = node.getChild(i);

            String name = child.getName();
            if ((!name.isEmpty()) && Camera.class.isInstance(child)) {
                names.add(name);
            }

            compileDebugCameraNames(names, child);
        }
    }

    /**
     * Sets the object size with new value.
     * Typically part of the response to an onSurfaceChanged()
     *
     * @param width  in pixels
     * @param height in pixels
     */
    public void resize(int width, int height) {
        mWidth = width;
        mHeight = height;
        updateCamera();
    }

    /**
     * Calculates the view projection matrix and its inverse.
     * Will update the projection matrices only if necessary.
     */
    private void updateViewProjectionMatrix() {
        if (mVpMatrixDirty) {
            float aspectRatio = (float) mWidth / mHeight;
            Matrix4 view = mActiveCamera.getWorldTransform().inverse();
            Matrix4 projection = mActiveCamera.getProjection(aspectRatio);
            mVpMatrix = Matrix4.multiply(projection, view);
            mInverseVpMatrix = mVpMatrix.inverse();
            mVpMatrixDirty = false;
        }
    }

    /**
     * Returns the view projection matrix for the scene camera.
     * This function will update the projection matrix only if necessary.
     */
    protected Matrix4 getViewProjectionMatrix() {
        updateViewProjectionMatrix();
        return mVpMatrix;
    }

    /**
     * Returns the inverse view projection matrix for the scene camera.
     * This function will update the projection matrix only if necessary.
     */
    protected Matrix4 getInverseViewProjectionMatrix() {
        updateViewProjectionMatrix();
        return mInverseVpMatrix;
    }

    // ------------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------------

    /**
     * Process all transactions and render the scene
     *
     * @return true if the render process is successful.
     */
    public boolean render() {
        if (mRenderingPaused) {
            return false;
        }

        // Tick the clock to do animation and make Stage dirty
        MasterClock.getDefault().tick();

        // Apply transaction for animations.
        Transaction.applyOperations();

        // Check stage is dirty or is there any animation running.
        boolean dirty = mStage.isDirty() || mStage.isAnimationStarted();

        // Apply all property changes into scene graph
        mStage.applyChanges(this);

        // Flush unused assets and resources
        mAssetPool.flush();

        Color bkgColor = mStage.getBackgroundColor();

        GLES20.glColorMask(true, true, true, true);
        GLES20.glDepthMask(true);
        GLES20.glClearColor(bkgColor.red / 255f, bkgColor.green / 255f,
            bkgColor.blue / 255f, bkgColor.alpha / 255f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mRenderer.update((float) (System.nanoTime() - mTimeOrigin)
            / (float) NANOSECS_PER_SECOND);
        mRenderer.render(mActiveCamera, mTrueRootNode);

        // Calculate and fix frame rate
        mFpsLimiter.calculateAndFixFrameTime();

        long now = System.nanoTime();
        if (mFrameCountingStartTimeNs == 0) {
            mFrameCountingStartTimeNs = now;
        } else if ((now - mFrameCountingStartTimeNs) > NANOSECS_PER_SECOND) {
            mFPS = (double) mFrameCount * NANOSECS_PER_SECOND / (now - mFrameCountingStartTimeNs);
            mFrameCountingStartTimeNs = now;
            mFrameCount = 0;
        }
        ++mFrameCount;

        return dirty;
    }

    /**
     * Pause the rendering
     */
    public void pauseRendering() {
        mRenderingPaused = true;
        MasterClock.getDefault().pause();
    }

    /**
     * Resume the rendering.
     */
    public void resumeRendering() {
        mRenderingPaused = false;
        MasterClock.getDefault().resume();
    }

    /**
     * Check the rendering status
     *
     * @return the rendering is pause or not
     */
    public boolean isRenderingPaused() {
        return mRenderingPaused;
    }

    public void setRenderCallback(RenderCallback render) {
        Transaction.setRenderCallback(render);
        mRenderCallback = render;
    }

    /**
     * Sends a request to the engine to do the render process.
     */
    public void requestRender() {
        if (mRenderCallback != null) {
            mRenderCallback.requestRender();
        }
    }

    // ------------------------------------------------------------------------
    // Admin, Control, Getter/Setter
    // ------------------------------------------------------------------------

    /**
     * Returns the root scene node
     */
    public SceneNode getRootNode() {
        return mRootNode;
    }

    /**
     * Returns the asset pool object, used to load assets in A3M
     */
    public AssetPool getAssetPool() {
        return mAssetPool;
    }

    /**
     * Returns the texture cache, from which textures are loaded
     */
    public TextureCache getTextureCache() {
        return mTextureCache;
    }

    /**
     * Dump the properties of this object out.
     */
    public void dump() {
        // \todo implement
    }

    /**
     * Gets the width of the screen
     *
     * @return width value
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Gets the height of the screen
     *
     * @return height value
     */
    public int getHeight() {
        return mHeight;
    }

    public int getTotalCImageBytes() {
        // \todo implement
        return 0;
    }

    public int getTotalTextureBytes() {
        // \todo implement
        return 0;
    }

    /**
     * Return the Android Resources container.
     *
     * @return Android resources
     */
    protected Resources getResources() {
        return mResources;
    }

    /**
     * Create a special scene node with empty presentation engine setting.
     *
     * @return a new scene node presentation.
     */
    public Presentation createEmpty() {
        return new SceneNodePresentation(this);
    }

    /**
     * Create a container.
     *
     * @return a new scene node presentation.
     */
    public Presentation createContainer() {
        return new SceneNodePresentation(this);
    }

    /**
     * Create a image display object.
     *
     * @return a new rectangular scene node presentation.
     */
    public ImageDisplay createImageDisplay() {
        return new RectSceneNodePresentation(this);
    }

    /**
     * Create basic 3D model.
     *
     * @param type model type, such as Model3d.CUBE or Model3d.Sphere
     * @return a new model 3D presentation.
     */
    public Model3d createModel3d(int type) {
        return new Model3dPresentation(this, type);
    }

    /**
     * Create object 3D model.
     *
     * @return a new model 3D presentation.
     */
    public IObject3d createObject3d() {
        return new Glo3dPresentation(this);
    }

    /**
     * Create a 2D object for drawing.
     *
     * @return new 2D presentation object for graphic
     */
    public Graphics2d createGraphics2d() {
        return new Graphics2dPresentation(this);
    }

    /**
     * Create a 3D object for drawing.
     *
     * @return new 3D presentation object for graphic
     */
    public Graphics3d createGraphics3d() {
        return new Graphics3dPresentation(this);
    }

    public Object getScreenShot() {
        // Grab backbuffer into a byte array
        byte[] pixels = mRenderer.getPixels(0, 0, mWidth, mHeight);

        // Copy the pixel data into a buffer
        ByteBuffer buffer = ByteBuffer.allocate(pixels.length);
        buffer.put(pixels);
        buffer.rewind();

        // Construct a bitmap from the buffer
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight,
            Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        // Invert bitmap in Y by scaling by -1. Android and OpenGL use
        // different bitmap origins (top-left vs. bottom-left)
        Matrix matrix = new Matrix();
        matrix.setScale(1, -1, mWidth * 0.5f, mHeight * 0.5f);
        Bitmap flipBitmap = Bitmap.createBitmap(mWidth, mHeight,
            Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(flipBitmap);
        canvas.drawBitmap(bitmap, matrix, new Paint());

        return flipBitmap;
    }

    public void enableMipMaps(boolean enable) {
        // mVideoDriver.setTextureCreationFlag(ETCF_CREATE_MIP_MAPS, enable);
    }

    /*
     * Enable/Configure stereocopic display
     *
     * WARNING:
     * Historically this method has taken the eye-separation distance as a
     * parameter.  This is not especially useful as the focal length
     * (camera-to-model-centre) is more meaningful, both as a world unit
     * and as a useful measure for app developers.
     * This should be changed over - but in the mean time, the one is
     * derived from the other here using a common rule-of-thumb.
     *
     *

From http://paulbourke.net/miscellaneous/stereographics/stereorender/

The degree of the stereo effect depends on both the distance of the camera to
the projection plane and the separation of the left and right camera. Too large
a separation can be hard to resolve and is known as hyperstereo. A good ballpark
separation of the cameras is 1/20 of the distance to the projection plane, this
is generally the maximum separation for comfortable viewing. Another constraint
in general practice is to ensure the negative parallax (projection plane behind
the object) does not exceed the eye separation.

A common measure is the parallax angle defined as theta = 2 atan(DX / 2d) where
DX is the horizontal separation of a projected point between the two eyes and d
is the distance of the eye from the projection plane. For easy fusing by the
majority of people, the absolute value of theta should not exceed 1.5 degrees
for all points in the scene. Note theta is positive for points behind the scene
and negative for points in front of the screen. It is not uncommon to restrict
the negative value of theta to some value closer to zero since negative parallax
is more difficult to fuse especially when objects cut the boundary of the
projection plane.

     * Considering the above...
     * If the distance to the focal plane is 40cm, a theta of 1.5' suggests
     * a max separation of 1cm - which is much larger than we are using in
     * practice.
     *
     */
    public void enableStereoscopic3D(boolean enable, float eyesDistance) {
        if (enable) {
            mCameraEyeSeparation = eyesDistance;
            mCameraFocalLength = mCameraEyeSeparation * 30.0f;
        } else {
            mCameraEyeSeparation = 0.0f; // disables stereo
        }

        // Configure for stereo projection here
        mCamera.setStereo(mCameraFocalLength, mCameraEyeSeparation);
        mVpMatrixDirty = true;
    }

    public boolean isStereo3dMode() {
        return (mCameraEyeSeparation != 0.0f);
    }

    /**
     * Checks whether the presentation has been initialized
     *
     * @return true if initialized
     */
    public boolean isReady() {
        return (mRootNode != null);
    }

    // ------------------------------------------------------------------------
    // Framerate
    // ------------------------------------------------------------------------

    /**
     * get FPS.
     */
    public double getFPS() {
        return mFPS;
    }

    /**
     * Record the maximum of FPS number.
     *
     * @param fps fps number
     */
    public void setMaxFPS(int fps) {
        mMaxFPS = fps;
    }

    /**
     * Get time the last frame cost.
     *
     * @return the time of frame interval
     */
    public int getFrameInterval() {
        return mFpsLimiter.waitForFrameTime();
    }

    private void fixFrameRate() {
        long tickTime = System.currentTimeMillis();
        long diff = tickTime - mTickTime;
        mTickTime = tickTime;
        int period = 1000 / mMaxFPS;
        if (diff < period) {
            try {
                Thread.sleep((period - diff));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * This class is used to log and calculate the frame time
     */
    class FpsLimiter {
        private int mLastFrameTime;
        private long mTickTime;

        void update() {
            long now = System.currentTimeMillis();
            if (mTickTime != 0) {
                mLastFrameTime = (int) (now - mTickTime);
            }
            mTickTime = now;
        }

        int waitForFrameTime() {
            synchronized (this) {
                try {
                    mFpsLimiter.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return mLastFrameTime;
        }

        private void calculateAndFixFrameTime() {
            synchronized (this) {
                mFpsLimiter.update();
                mFpsLimiter.notifyAll();
            }

            if (mMaxFPS > 0) {
                int period = 1000 / mMaxFPS;
                if (mLastFrameTime < period) {
                    try {
                        Thread.sleep((period - mLastFrameTime));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
