/**************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
**************************************************************************/
/** \file
 * Maths library documentation
 */

/*************** PUT NO CODE IN HERE!  TEXT ONLY!! ************************/

/* The maths functions  and types are generally reference material and so are
collected in the manual under the 'reference' section and the dox group a3mRef.
There is no 'maths library' section in the manual. */

/** \defgroup a3mRefMaths Maths (vectors, matrices, angles, etc)
    \ingroup a3mRef

Classes and functions to support mathematical calculations and processes.
*/


/** \defgroup a3mVector Vectors (Introduction and usage)
    \ingroup a3mRefMaths

Classes and functions to support working with 2-, 3-, and 4-D vectors.

Vector arithmetic is supported with functions named as follows:
\code
 Vector<N>f *vector<N>f<Op>( Vector<N>f *result,
                             Vector<N>f const *v1,
                             Vector<N>f const *v2 );
where:
 N = <2|3|4> (number of dimensions)
 Op = <Add|Sub|Mul>
\endcode

Addition. result = v1 + v2:
\code
 Vector2f *vector2fAdd( Vector2f *result, Vector2f *v1, Vector2f *v2 )
\endcode

Vectors may be multiplied by a single (scale) number: Here scaled = scale * v
\code
 Vector<N>f *vector<N>fScale( Vector<N>f *scaled,
                              <A3M_FLOAT> scale,
                              Vector<N>f const *v );
\endcode

Dot products: This returns v1 \b . v2
\code
 <A3M_FLOAT> vector<N>fDot( Vector<N>f const *v1,
                            Vector<N>f const *v2 );
\endcode

Cross products (of 3d vectors): This returns v1 \b x v2
\code
  Vector3f *vector3fCross( Vector3f *result,
                           Vector3f const *v1,
                           Vector3f const *v2 );
\endcode

The length (magnitude) of a vector is calulated using:
\code
 <A3M_FLOAT> vector<N>fLength( Vector<N>f const *v )
\endcode

A vector may be normalized using:
\code
  Vector<N>f *vector<N>fNormalize( Vector<N>f *normal,
                                   Vector<N>f const *v );
\endcode

*/

/* END OF FILE */


