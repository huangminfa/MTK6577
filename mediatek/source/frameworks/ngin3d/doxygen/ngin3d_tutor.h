/**************************************************************************
 *
 * Copyright (c) 2012 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 **************************************************************************/
/** \file
 * Getting-started tutorials (Text only, no code).
 */

/** \defgroup nginTutor Getting started with Ngin3D

This is a short guide to getting started with 3D graphics views and Ngin3D.

*/

/** \defgroup nginTutorProjection 3D Projections
    \ingroup nginTutor

An explanation of the 3D projections used in Ngin3D.

<h2>BEFORE you begin</h2>

It is critically important that you think about your application carefully
before starting to design an Ngin3D application because there are two very
different ways to use the graphics system.

- To have the camera in a fixed place, pointing at a fixed region of
space.  This model is like a theatre where the camera is the audience's
viewpoint and the action is happening on the screen or stage.

- To have the camera under full control so you can move it \e anywhere, point
it \e anywhere, and \e zoom in and out.


The first is best for applications where you want to control your 3D objects as
if they are on a screen in front of the user.  This is the most common case for
user interfaces.  This requires that you use the projection UI_PERSPECTIVE.  You
will not be able to move the camera or change its settings (they are calculated
automatically so you won't get them wrong).

The second is necessary if you want to fly the camera around your model like in
a game.  This is the PERSPECTIVE model.

\warning Do \b not expect to be able to switch between these projections.  It is
allowed, but the results will be probably be extremely confusing.

There are other projections in Ngin3D:

- ORTHOGRAPHIC
- UI_PERSPECTIVE_LHC

ORTHOGRAPHIC is \b not currently implemented - there is no Use Case for it that
UI_PROJECTION cannot solve. UI_PERSPECTIVE_LHC is only present for compatibility
with older applications. It must not be used and may stop being supported.


<h2>UI_PERSPECTIVE Mode</h2>

UI_PERSPECTIVE is the default mode in Ngin3D.

In this mode the Z axis is directly towards the viewer, and the 'screen' is on
the XY plane.

\warning The Y axis points \b down.

The camera is positioned some distance from the screen in the +Z direction, and
points directly at the centre of this screen area. The origin of the screen
(0,0) is at the top left.

The zoom (Field of view) of the camera is automatically adjusted so that the
'screen' on the XY plane matches the physical screen pixels on the device.

<BR>
\image html proj_ui_01.png "UI_PERSPECTIVE - Camera fixed, pointing at XY screen."

An object placed at (0,0,0) in the world will \e always be visible at the top
left of the screen.  (The yellow ball in the diagram).

It is allowed to change the Z position of the camera, but this is the \b only
parameter that can be changed.  The default value is Z = 1111.

The value of 1111 is used as the default because it is the default set-up of the
AfterEffects tool.




<h2>Classical PERSPECTIVE mode</h2>

In this mode the resulting display will be what is in front of the camera, and
\e that will depend where you are pointing the 'lens'.

Everything uses a right-hand coordinate system with Y conventionally meaning
'up'.

The zoom (Field of view) of the camera is under the control of the application.
A smaller field-of-view will make objects appear larger (e.g. the red FOV in
the diagram fills the screen with a small area of the world).

<BR>
\image html proj_classic_01.png "PERSPECTIVE - Camera anywhere, pointing anywhere, variable zoom."

An object placed at (0,0,0) in the world will only be visible if the camera is
pointed in that direction.  (The yellow ball in the diagram is not visible on
the screen).

\warning The default camera position is at (0,0,0). If your scene model is based
here, remember to move the camera away from the scene. (Otherwise the camera is
\e inside the yellow ball!)


<h2>Projection Exercises</h2>

For a walk-through of the visual effects of these different projections, go to
\ref nginTutor01

*/
/* END OF FILE */


