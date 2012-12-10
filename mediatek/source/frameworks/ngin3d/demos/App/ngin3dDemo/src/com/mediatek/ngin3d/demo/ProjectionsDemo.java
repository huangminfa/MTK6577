/**
 * \file
 * Example to demonstrate the two main projection modes.
 */

package com.mediatek.ngin3d.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.mediatek.ngin3d.Ngin3d;
import com.mediatek.ngin3d.Glo3D;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.android.StageView;
import com.mediatek.ngin3d.demo.R;

import javax.microedition.khronos.opengles.GL10;

/** \ingroup ngin3dDemos

Illustration of the Ngin3D projection modes.

This demo shows a single Glo3D model viewed by a camera demonstrating various
camera parameters. A single model is used as the focus of this demo, but the
same procedures work with a scene of many models.

<h2>Code Walkthrough</h2>

Note that the class extends StageActivity rather than Activity.
\code public class ProjectionsDemo extends StageActivity { \endcode

This conveniently creates a Stage (mStage) for us to place the Actors (the scene
objects) on.

The first operation is to obtain a suitable object and place it on the Stage.
The object model (a GLO format file created in an external tool such as 3DS Max)
is loaded from the assets folder. A Container object is created to hold the set
of objects (the scenario) and the object is added to the Container, and then the
scene is positioned where required.

\code
Glo3D.createFromAsset( <filename> );
scenario.add(landscape);
scenario.setPosition( x,y,z );
\endcode

The object is initially positioned at the origin (0,0,0) and the camera
is initially positioned a distance away along the Z axis and at the same time
it is pointed at the origin. This is achieved by:
\code
mStage.setCamera(
  new Point(CAM_X, CAM_Y, CAM_Z), // Camera position in world
  new Point(OBJ_X, OBJ_Y, OBJ_Z)  // Aim the camera at the object
  );
\endcode

<h2>Exercises</h2>
See \ref nginTutor01

 */
public class ProjectionsDemo extends StageActivity {
    /** Tag to identify log messages */
    private static final String TAG = "ProjectionsDemo";

    /* Camera position in world */
    private static final float CAM_X = 0f;
    private static final float CAM_Y = 0f;
    private static final float CAM_Z = 1000f;

    /* Field-of-view of the camera in degrees */
    private static final float CAM_FOV = 20.0f;
    private float camFov = CAM_FOV;

    /* Half-width & half-height of portrait 800x480 screen (in pixels) */
    private static final float HWIDTH = 240f;
    private static final float HHEIGHT = 400f;

    /* Clipping distances for camera */
    private static final float Z_NEAR = 2.f;
    private static final float Z_FAR = 2000.f;

    /* Object's position in world */
    private static final float OBJ_X = 0.f;
    private static final float OBJ_Y = 0.f;
    private static final float OBJ_Z = 0.f;

    /* Scaling factor to apply to model */
    private static final float OBJ_SIZE = 10.0f;
    private float objSize = OBJ_SIZE;
    private float objYScale = 1.0f;

    private Point camPosn = new Point(CAM_X, CAM_Y, CAM_Z);
    private Point objPosn = new Point(OBJ_X, OBJ_Y, OBJ_Z);

    private Container scenario = new Container();

    /**
     * This method creates the scene on start-up.
     * @param savedInstanceState Standard parameter for android activities
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Glo3D landscape = Glo3D.createFromAsset("landscape.glo");
        scenario.add(landscape);

        resetView();

        /* Add the assembled 'scene' to the stage */
        mStage.add(scenario);
    }


    /**
     * Reset all the scene and camera parameters.
     */
    private void resetView() {
        camFov = CAM_FOV;
        camPosn = new Point(CAM_X, CAM_Y, CAM_Z);
        objPosn = new Point(OBJ_X, OBJ_Y, OBJ_Z);
        objSize = OBJ_SIZE;
        objYScale = 1.0f;

        scenario.setPosition(objPosn);
        scenario.setScale(new Scale(objSize, objSize, objSize));

        mStage.setProjection( Stage.PERSPECTIVE, Z_NEAR, Z_FAR, CAM_Z );
        mStage.setCamera( camPosn, objPosn );
        mStage.setCameraFov(camFov);
    }

    /* Excluding the menu handling classes from the Doxygen description. */
    /*! \cond */

    /** Class to support use of the Android menu function */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.proj_demo_menu, menu);
        return true;
    }

    /** Class to handle the return from the Android menu function */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // Menu is coded as 'reset' then a few parameter changers.
        switch (itemId){
        case R.id.proj_00: // Reset everything
            resetView();
            break;

        case R.id.proj_01: // Halve the field of view = zoom in
            camFov /= 2.0f;
            mStage.setCameraFov(camFov);
            break;

        case R.id.proj_02: // Switch to UI_PERSPECTIVE
            // This is inadvisable normally but this demo is to illustrate
            // the different projections.  Set the camera to the normal
            // UI_ position before losing control of the camera.

            mStage.setCamera( new Point(HWIDTH, HHEIGHT, 1111.0f),
                              new Point(HWIDTH, HHEIGHT, 0.0f));
            mStage.setCameraFov(45f);

            mStage.setProjection( Stage.UI_PERSPECTIVE, Z_NEAR, Z_FAR, 1111.0f );
            break;

        case R.id.proj_03: // Move object to UI centre
            objPosn = new Point(HWIDTH, HHEIGHT, 0.0f);
            scenario.setPosition(objPosn);
            break;

        case R.id.proj_04: // double size of object
            objSize *= 2.0f;
            scenario.setScale(new Scale(objSize, objSize * objYScale, objSize));
            break;

        case R.id.proj_05: // invert Y scale
            objYScale = -objYScale;
            scenario.setScale(new Scale(objSize, objSize * objYScale, objSize));
            break;

        default:
            return false;
        }
        return true;
    }

    /*! \endcond */

}


/*!
 * \defgroup nginTutor01 Exercise #1 Projections with ProjectionsDemo app
 * \ingroup nginTutor

A walk-through demonstration of the effects of camera settings.

The Ngin3D Demo "ProjectionsDemo" shows a single Glo3D model viewed by a camera
demonstrating various camera parameters.  Read the \ref nginTutorProjection
"Explanation of 3D Projections" first!

<h2>Exercises</h2>

Open the ProjectionsDemo app on a handset and follow these instructions.
Please do \b NOT use the menu options until instructed to do so.

If you get lost, use the menu option \b Reset to get back to this starting point.

When you first start up ProjectionsDemo there is a small model in the distance
in the centre of the screen. The \e centre of this model is positioned at the
origin (0,0,0).  The camera is on the Z axis looking directly at the origin.

This initial scene is using classic \b PERSPECTIVE mode. At this moment \b Y is
up, \b X is to your right and \b Z is towards you out of the phone.

<h2>Scale the model</h2>

Using the menu, select \b x2.  This doubles the scaling of the model each time
you press it. Press it a few times. The camera zoom is \e not changing, the
model is getting bigger.

Note that the detail is preserved when the scaling is high.

<h2>Narrow the Field of View</h2>
Reset the scene.

Using the menu select \b FOV/2. Notice that this \e seems to have the same effect
as scaling the model by two.

If you keep pressing this option you will see that the edges of the model
get slightly corrupted.  This is because of rounding errors.  This only happens
at very small FOV angles - less than 2 degrees!!  You would normally view objects
with a FOV of around 45 degrees which is an approximation to the human FOV when
looking at a screen.

<div class="learn">Do not use very small FOV angles.</div>


<h2>Switch to UI_PERSPECTIVE</h2>
Reset the scene.

Using the menu, select <b>UI Mode</b>.

The model is now in the top left corner, slightly smaller and up-side-down!
This \e is expected!

In \b UI_PERSPECTIVE the UI screen 'origin' is in the top left corner of the
screen.  The model is positioned at 0,0,0 so it is now also at the top left of
the screen.

The model is slightly smaller because the default camera Z position in
UI_PERSPECTIVE is \b 1111 units.  In the Classic PERSPECTIVE mode it was set to
\b 1000 units, so it is now a little further away.

The model is up-side-down because the \b Y axis in UI_PERSPECTIVE mode points \b
downwards.  The model's Y axis points upwards, so when you render this model in
this mode it appears up-side-down.

\note The model is \e reflected in Y, it is not 'turned' up-side-down.  The left
and right sides have not been reversed, so what was on the right of the model is
still on the right of the model.

<div class="learn">In <i>UI_</i>PERSPECTIVE, 0,0 is top left and Y is down </div>


<h2>Position the model in XY</h2>

In \b UI_PERSPECTIVE the XY coordinate UI screen at Z=0, and screen pixels, are
the same thing.

Using the menu, select <b>240,480</b>. Assuming you are using a 480x800 phone the
model is moved to the centre of the screen - i.e. 240 units ( = pixels ) to the
right in X and 400 \e down in Y.  Remember the Y axis points downwards in
UI_PERSPECTIVE.

\note In Ngin3D you can specify a screen position with normalized coordinates,
e.g. (0.5, 0.5), to mean the centre of the screen.

<div class="learn">In <i>UI_</i>PERSPECTIVE, XY position is in pixels (if Z=0) </div>

<h2>Try Changing the FOV</h2>

Using the menu select \b FOV/2.

<b>Nothing happens</b>.  This is because the FOV is fixed in UI_PERSPECTIVE.  It
is fixed at whatever FOV is required to show all of the stage area (i.e. 800x480
of the XY plane) on the screen.

If you move the camera in Z the FOV changes automatically.  There is no need for
the app to calculate FOV in \b UI_PERSPECTIVE.


 *
 */
