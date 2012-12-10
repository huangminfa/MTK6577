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
 * Emissive shader for clock widget.
 */

precision mediump float;

/* Material uniforms */
uniform vec4 u_m_diffuseColour;   // Diffuse colour with alpha.
uniform sampler2D u_m_diffuseTexture;    // Diffuse texture including alpha

// Texture coordinate copied from vertex attribute
varying lowp vec2 v_texCoord;

void main()
{
  lowp vec4 diffuseTex = texture2D( u_m_diffuseTexture, v_texCoord );
  gl_FragColor = u_m_diffuseColour * diffuseTex;
}
