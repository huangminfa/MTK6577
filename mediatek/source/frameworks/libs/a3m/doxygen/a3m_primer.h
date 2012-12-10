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
 * Graphics primer common to A3M's Java and C++ APIs (Text only, no code).
 */

/*************************************************************************
 * USER MANUAL Top Level - Graphics primer for new developers
 *************************************************************************/

/** \defgroup a3mPrimer A3M Graphics Primer

Graphics programming basics using A3M.

<h2>Introduction</h2>
A3M is a sophisticated wrapper layer for OpenGL ES (which is simply the embedded
version of OpenGL).  If you have no knowledge of OpenGL it might be a good idea
to read about it, but it is not necessary.  This section should explain enough
to start with.

Graphics is mostly about the following things:

- A world coordinate system
- Objects in the world, and how they relate to each other
- What their appearance is (colour, pattern, shine, etc.)
- The camera and its settings

<h2>A3M Coordinate System</h2>

Computer Graphics uses a 3-axis coordinate system.  Unfortunately there are
manyways a simple coordinate system can be interpreted, for example is the 'up'
axis \b Y or \b Z? There are two basic systems, called left-handed and right-
handed as illustrated below.

<BR> \image html left_right_hand.gif "Left and Right Hand Coordinates"

<div class="learn">A3M uses a \b RIGHT-handed coordinate system, with \b Y as
the vertical axis. All modelling (e.g. in 3dsMax) should be done in this
coordinate system. </div>

<h2>Scenegraph</h2>

Objects are arranged in a hierarchy.  A 'car' object will be made up of 'body',
'wheels', 'engine' etc. A 'wheel' will be made up of 'hub' and 'tyre', and so
on.

Each object has a position \b relative to the object it is part of, its
'parent'.  Therefore when you move the car, all the parts (the 'child' nodes)
move with it.  The hierarchy is described as a 'scene graph' and each point on
this graph, a scene 'node' generally represents an object.

<BR> \image html scenegraph_01.png "Scene Graph / Scene Hierarchy"

The position of any object, relative to its parent, is described by a set of
translations, rotations and scaling operations, held as a single transformation
matrix.


<h2>Geometry and lighting</h2>

\b All computer graphics models are created from triangles.  There may be a lot
of them to create smoothly curved shapes, but everything is made of triangles at
the base level.  The size and number of triangles is important for performance
and image quality.

If you have infinite power then the more triangles you use the better the image
will be.  However, in the real world it is necessary to make the number of
triangles as \b few as possible.  Every triangle takes processing time and thus
slows down the frame rate of the graphics.

It is important to know the difference between \b per-vertex lighting and \b
per-pixel lighting. Vertex lighting is generally faster because the lighting
calculations are done only for the three vertices of each triangle.  In pixel
lighting, the lighting calculation is done for each point on the triangle
surface.  This can be much slower (there are many times more calculations to do)
but the resulting visual effect is much better.

<h2>Appearances</h2>

The appearance of an object is the result of many factors, and, of course, the
final appearance will depend on how the object is lit.

The most basic object appearance is its colour - but even this is split into
'diffuse' colour which is the colour of the object in general lighting;
'specular' colour when a light reflects off the object (this is typically
white), and 'ambient' colour which the object shows even when it is in complete
darkness.

\todo Check use of ambient vs emmissive.

<BR> \image html lighting_01.png "Basic lighting models"

It is possible to wrap an image around an object.  In graphics, these images are
called 'textures'.  Points in the image are mapped to corresponding points on
the object (the triangle vertices).  When the graphics engine renders the object
it picks the correct pixel from the 'texture'.

Using textures can have a great benefit in reducing the number of triangles
required, because many a 'detailed' object can be created from a simple geometry
wrapped around by detailed textures.

Textures (also known as 'maps') have many uses other than being an image wrap,
but these uses are outside the scope of this primer.

<h2>Camera and Projections</h2>

The camera is itself a scene node.  It is simply another object within the
graphics scene world. There can be multiple cameras but only \b one active at
any one time.

\todo Revise this section after multi-layer rendering.

With a real-world photographic camera the resulting image depends on where you
are (position), what you are pointing the camera at (target), how you are
holding it (rotation) and what zoom you are using (field-of-view).  It is
exactly the same here.

The camera scene node defines it's position and orientation. The Field of View
(FOV) and Z-clipping distances (see below) can be set separately.

For a perspective view the volume of world space to be rendered is a "view
frustum".

\image html view_frustum_01.png "View Frustum for a Perspective View"

The distance clipping ranges (Z-Near and Z-Far, in the <i>camera's</i> Z
direction) exclude objects that are too near or too far from the camera.  This
improves performance.

It is important that the \b ratio between Z-Far and Z-Near is kept as small as
possible to avoid <b>Z-Fighting</b>.  Z-Fighting occurs when there is
insufficient resolution in the renderer to distinguish the Z distance of pixels
belonging to different objects, so an erratic and often flashing mix of the two
objects is produced.

<div class="learn">Minimise the ratio ZFar/ZNear. (Tip: Increase ZNear).</div>


<h3>Making it simpler for UI graphics</h3>

It can be very confusing configuring an 'unlimited' camera, so MAGE provides a
mode where the camera is fixed facing a 'stage' that you can arrange objects on.

This is described more in the \ref ngin3d documentation

*/

/* END OF FILE */


