package com.mediatek.ngin3d.presentation;

import android.content.res.Resources;
import com.mediatek.ngin3d.Point;

/**
 * Interface to presentation engine.
 */
public interface PresentationEngine {
    /**
     * Initialize presentation engine with viewport dimension.
     *
     * @param width  in pixels
     * @param height in pixels
     */
    void initialize(int width, int height);

    /**
     * Initialize presentation engine with viewport dimension and Resources.
     *
     * @param width  in pixels
     * @param height in pixels
     * @param resources Resources
     */
    void initialize(int width, int height, Resources resources);

    /**
     * Initialize presentation engine with viewport dimension and Resources.
     *
     * @param width  in pixels
     * @param height in pixels
     * @param resources Resources
     * @param cacheDir The binary shader cache directory
     * @param debug enable or disable debug server
     */
    void initialize(int width, int height, Resources resources, String cacheDir, boolean debug);

    /**
     * Initialize presentation engine with viewport dimension
     *
     * @param width  in pixels
     * @param height in pixels
     * @param debug enable or disable debug server
     */
    void initialize(int width, int height, boolean debug);

    /**
     * Can be used to know whether the required resource, such as OpenGL context, is ready or not.
     *
     * @return true if the context is ready. false otherwise.
     */
    boolean isReady();

    /**
     * Sets the currently active debug camera.
     * Passing an empty string activates the default camera.
     *
     * This function is deprecated and marked for removal in the near future.
     *
     * @deprecated
     * @param name Camera name (an empty string activates the default camera)
     */
    void setDebugCamera(String name);

    /**
     * Returns a list of names of cameras in the scene.
     *
     * This function is deprecated and marked for removal in the near future.
     *
     * @deprecated
     * @return List of cameras
     */
    String[] getDebugCameraNames();

    /**
     * Sets a virtual camera of this object for seeing stage.
     *
     * @param pos    camera position
     * @param lookAt camera focus point position
     */
    void setCamera(Point pos, Point lookAt);

    /**
     * Set camera Z position. This is normally only used for the
     * UI_PERSPECTIVE projection where X and Y are fixed relative
     * to the screen size.
     * However, for completeness it will also affect the Z position
     * when in PERSPECTIVE mode.
     *
     * @param zCamera Z component of camera position
     */
    void setCameraZ(float zCamera);

    /**
     * Set camera field of view (FOV) in degrees.
     * The field of view for the smaller screen dimension is specified (e.g. if
     * the screen is taller than it is wide, the horizontal FOV is specified).
     *
     * This parameter is only used by the PERSPECTIVE projection. In the 'UI'
     * projections the FOV is derived from the camera Z position and the screen
     * width (pixels) which are considered to be in the same coordinate space.
     *
     * @param fov Camera field of view in degrees
     */
    void setCameraFov(float fov);

    /**
     * Sets the near and far clipping distances. Note these are distances
     * from the camera, in the forward direction of the camera axis; they
     * are NOT planes positioned along the global Z axis despite often called
     * Znear and Zfar.
     *
     * @param near objects nearer than this are clipped
     * @param far objects further away than this are clipped
     */
    void setClipDistances(float near, float far);

    /**
     * Record the maximum of FPS number.
     *
     * @param fps fps number
     */
    void setMaxFPS(int fps);

    /**
     * Get time the last frame cost.
     *
     * @return the time of frame interval
     */
    int getFrameInterval();

    /**
     * Enable/disable stereoscopic 3d display mode
     * @param enable  true if you want to enable stereo 3d display mode
     * @param eyesDistance  the distance between two eyes
     */
    void enableStereoscopic3D(boolean enable, float eyesDistance);

    /**
     * Get the screen shot of current render frame.
     * @return An Object representing the render frame.
     */
    Object getScreenShot();

    /**
     * Register with real renderer's requestRender
     */
    public interface RenderCallback {
        void requestRender();
    }

    /**
     * Specify the callback for rendering.
     *
     * @param renderCallback
     */
    void setRenderCallback(RenderCallback renderCallback);

    /**
     * Render the presentation.
     *
     * @return true if still dirty
     */
    boolean render();

    /**
     * Resize the presentation area.
     *
     * @param width  in pixels
     * @param height in pixels
     */
    void resize(int width, int height);

    /**
     * Deinitialize the context.
     */
    void uninitialize();

    /**
     * Dump debug information.
     */
    void dump();

    /**
     * Gets the width of this presentation engine object.
     *
     * @return width value
     */
    int getWidth();

    /**
     * Gets the height of this presentation engine object.
     *
     * @return height value
     */
    int getHeight();

    /**
     * @return total memory usage of CImage in bytes.
     */
    int getTotalCImageBytes();

    /**
     * @return total memory usage of OpenGL texture.
     */
    int getTotalTextureBytes();

    /**
     * Create a special scene node with empty presentation engine setting.
     *
     * @return a new scene node presentation.
     */
    Presentation createEmpty();

    /**
     * Create a container.
     *
     * @return a new scene node presentation.
     */
    Presentation createContainer();

    /**
     * Create a image display object.
     *
     * @return a new rectangular scene node presentation.
     */
    ImageDisplay createImageDisplay();

    /**
     * Create basic 3D model.
     *
     * @param type model type, such as Model3d.CUBE or Model3d.Sphere
     * @return newly created model
     */
    Model3d createModel3d(int type);

    /**
     * Create object 3D model.
     *
     * @return newly created model
     */
    IObject3d createObject3d();

    /**
     * Create a 2D object for drawing.
     *
     * @return new object that can be used to draw 2D graphics
     */
    Graphics2d createGraphics2d();

    /**
     * Create a 3D object for drawing.
     *
     * @return new object that can be used to draw 3D graphics
     */
    Graphics3d createGraphics3d();

    /**
     * Pause the rendering
     */
    void pauseRendering();

    /**
     * Resume the rendering
     */
    void resumeRendering();

    /**
     * Check the rendering status
     * @return the rendering is pause or not
     */
    boolean isRenderingPaused();

    /**
     * get FPS
     */
    double getFPS();

    /**
     * Set default projection mode
     */
    void setProjectionMode(int mode);
}
