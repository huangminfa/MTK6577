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
 * Default fragment shader used when rendering glo file contents.
 */

precision mediump float;


/* Material uniforms */
uniform sampler2D u_m_diffuseTexture;    // Diffuse texture including alpha
uniform sampler2D u_m_specularTexture;   // Specular texture (alpha ignored)
uniform sampler2D u_m_ambientTexture;    // Ambient texture (alpha ignored)
uniform sampler2D u_m_emissiveTexture;   // Emissive texture (alpha ignored)
uniform sampler2D u_m_reflectiveTexture; // Sphere-mapped environment texture
                                         // (alpha ignored)

uniform vec4 u_m_emissiveColour;  // Emissive material colour


/* Varyings */
// Apparent colour of vertex taking into account ambient colour of light,
// distance from light and ambient colour of material
varying lowp vec4 v_ambientColour;

// Apparent colour of vertex taking into account diffuse colour of light,
// distance from light, diffuse colour of material and angle of incidence of
// light
varying lowp vec4 v_diffuseColour;

// Apparent colour of vertex taking into account specular colour of light,
// distance from light, specular colour and shininess of material, angle of
// incidence of light and view vector
varying lowp vec4 v_specularColour;

// Texture coordinate copied from vertex attribute
varying lowp vec2 v_texCoord;

// Texture coordinate for sphere-mapped reflections
varying lowp vec3 v_sphereCoord;


void main()
{
  // Look up value from diffuse texture map
  lowp vec4 diffuseTex = texture2D( u_m_diffuseTexture, v_texCoord );

  // Multiply by diffuse colour
  lowp vec4 diffuse = diffuseTex * v_diffuseColour;

  // Look up value from specular texture map
  lowp vec4 specularTex = texture2D( u_m_specularTexture, v_texCoord );

  // Colour calculated from sum of:
  // diffuse colour as calculated above,
  // specular colour (specular texture X specular lighting component),
  // ambient colour (ambient texture X ambient lighting X diffuse texture),
  // emmisive colour (emissive texture X emissive colour),
  // reflection (specular texture X environment map texture look-up)
  gl_FragColor = 0.2 * 
    specularTex * (1.0 - v_sphereCoord.z) *
    texture2D( u_m_reflectiveTexture, v_sphereCoord.xy );

  // Use diffuse component alpha
  gl_FragColor.a = diffuse.a;
}
