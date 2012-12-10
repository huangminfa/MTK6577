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
 * Technical manual intro and document structure (Text only, no code).
 */


/*************************************************************************
 * TECHNICAL DESIGN AND MAINTENANCE SECTION Top Level
 *
 * Overview of the system as a whole.
 * Explain how A3M is made up, what the important parts and interfaces are.
 *
 * Note - using defgroup here to put structure into modules tab in the
 * document.  page and subpage don't do that.  However, using defgroup also
 * means the subsections are automatically listed, not under our control.
 *************************************************************************/

/** \defgroup a3mTech Technical Manual

A technical reference of design information for \b maintainers and \b developers
that provides an 'under the bonnet' view of A3M. It is intended for engineers
skilled in 3D graphics implementation who may need to expand and/or modify the
engine.

This section includes details of internal-only items.

*/


/** \defgroup a3mArch Architectural Overview
    \ingroup a3mTech

A3M (Advanced 3D Middleware) is a stand alone graphics engine.  From a users
point of view there are two major components, representing two C++ API's at
different levels of abstraction.

The 'upper' level API is one where the user specifies a scene using objects,
camera(s), lights, a structure saying how those objects relate to each other,
the materials the objects are made of, and the type of rendering to be used.
Rendering types include, for example, perspective or orthogonal views, with or
without shadows, etc. The final image is made up of a number of these
renderings, so the user can render a perspective scene, then add an orthogonal
overlay.

The 'lower' level API provides access to finer detail of the graphics system
such as vertex buffers, maths facilities, buffer management, textures, file
loaders, etc.  It provides a wrapper for the underlying <a href =
"http://www.khronos.org/opengles/">OpenGL ES</a>.

It is intended that 'basic' applications, where the writer does not have in-
depth graphics skills, can easily use the fixed functionality of the 'upper'
scene rendering layer.  However, if more complex effects are required than a
skilled graphics engineer can extend the functionality of the scene rendering
layer, using the facilities of the 'lower' facility layer.

These layers are illustrated in the diagram below.  The main A3M components are
in orange.

<BR> \image html a3m_layers_01.png "A3M Architecture"

In addition to these core modules there are the following:

<h2>JNI Adaptor</h2>

A3M is written in C++.  This module provides the Java interface for A3M.  This
interface is a simpler interface than the full A3M API and the adaption between
these interfaces is done within this module.  The module only exists for Android
configurations of A3M.

<h2>Platform Specific Services PSS</h2>

This module contains all code that is specific to the OS or hardware that is
running A3M.  It contains functions such as file system access, I/O streams,
etc. that vary between platforms and OS's.

All other A3M modules are intended to be platform agnostic. However, variations
in GPU architecture may impact on the order in which high level rendering
operations are scheduled, so even high level code cannot be truly independent
of the platform and maximise efficiency.

See \ref a3mPss

*/



/** \defgroup a3mInt A3M Internals
    \ingroup a3mTech

This section gives technical information regarding data and functions that <b>
must only be used \em within A3M code</b>.  Thus this information is intended
for use by A3M maintainers; and not by users.

\warning These facilities may change without notice.

*/


/** \defgroup a3mIntUtil A3M Internal Utilities
    \ingroup a3mInt

Convenience items <b>for use in A3M code only</b>.

*/


/* END OF FILE */


