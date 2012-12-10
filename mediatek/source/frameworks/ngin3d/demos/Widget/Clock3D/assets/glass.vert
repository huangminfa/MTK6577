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
 * Default vertex shader used when rendering glo file contents.
 */

/*
 * Calulates ambient, diffuse, specular lighting and texture coordinates for
 * sphere-mapped environment shading
 */

precision mediump float;

/* This should match the maximumum number of lights in the renderer */
#define MAX_LIGHTS 8


/* Transformation uniforms */
uniform mat4 u_t_model;               // Model to world space transform
uniform mat4 u_t_view;                // World to view space transform
uniform mat4 u_t_modelView;           // Model to view space transform
uniform mat4 u_t_modelViewProjection; // Model to projection space transform
uniform mat3 u_t_normal;              // Model to world space normal transform


/* Lighting uniforms */
uniform vec4 u_l_cameraPosition;        // Camera position in world space
uniform int u_l_count;                  // Number of lights
uniform vec4 u_l_position[MAX_LIGHTS];  // Light position in world space
uniform vec4 u_l_ambient[MAX_LIGHTS];   // Ambient colour of light
uniform vec4 u_l_diffuse[MAX_LIGHTS];   // Diffuse colour of light
uniform vec4 u_l_specular[MAX_LIGHTS];  // Specular colour of light

// 1/(radius of lit sphere) measured in world space units
uniform float u_l_radius_reciprocal[MAX_LIGHTS];


/* Material uniforms */
uniform vec4 u_m_ambientColour;   // Ambient colour (alpha ignored)
uniform vec4 u_m_diffuseColour;   // Diffuse colour with alpha.
uniform vec4 u_m_specularColour;  // Specular colour (alpha ignored)
uniform vec4 u_m_emissiveColour;  // Emissive colour (alpha ignored)
uniform float u_m_shininess;      // Exponent used in specular calculation
uniform float u_m_specularLevel;  // Specular colour scale factor
uniform float u_m_selfIllumination; // Emissive colour scale factor
uniform float u_m_opacity;        // Opacity value


/* Vertex attributes */
attribute vec4 a_position;    // Position in model space
attribute vec3 a_normal;      // Normal in model space
attribute vec2 a_uv0;         // Texture coordinate


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
  // Final position is model position multiplied by model view projection matrix
  gl_Position = u_t_modelViewProjection * a_position;

  // Pass through texture coordinate
  v_texCoord = a_uv0;

  // Convert normal to world space and normalize
  vec3 normal = normalize( u_t_normal * a_normal );

  // Calculate vertex position in world space for lighting calulations
  vec3 vertexPosition = (u_t_model * a_position).xyz;

  // Calculate vector from vertex to camera in world space
  vec3 vertexToCameraNorm = normalize( u_l_cameraPosition.xyz - vertexPosition );

  // Initialize colour components before accumulation for each light
  v_ambientColour = vec4( 0., 0., 0., 1. );
  v_diffuseColour = vec4( 0., 0., 0., 1. );
  v_specularColour = vec4( 0., 0., 0., 1. );

  // Ensure number of lights does not exceed array lengths
  int lightCount = (u_l_count <= MAX_LIGHTS) ? u_l_count : MAX_LIGHTS;

  // Calculate colour components for each light
  for (int i = 0; i < lightCount; ++i)
  {
    // Calculate vector from vertex to light in world space
    vec3 vertexToLight = u_l_position[i].xyz - vertexPosition;

    // Calculate attenuation due to distance from light
    // This approximates to that used 3ds Max
    float lightLevel =
      sqrt( 1.0 - length(vertexToLight) * u_l_radius_reciprocal[i] );

    // If lightLevel is positive then this light contributes
    if( lightLevel > 0.0 )
    {
      // Calculate diffuse lighting if the angle between the surface normal and
      // light ray is less than 90 degrees
      vec3 vertexToLightNorm = normalize( vertexToLight );
      float normalDotLight = dot(normal, vertexToLightNorm );
      if( normalDotLight > 0.0 )
      {
        v_diffuseColour += ( normalDotLight * lightLevel )
                           * u_l_diffuse[i];
      }

      // Calculate specular lighting using Blinn model.
      // Specular intensity peaks where the normal lies exactly halfway between
      // the light source and the eye
      vec3 halfVector = normalize( vertexToCameraNorm + vertexToLightNorm );
      float normalDotHalf = dot( normal, halfVector );
      if( normalDotHalf > 0.0 )
      {
        v_specularColour += pow( normalDotHalf, u_m_shininess ) * lightLevel
                            * u_l_specular[i];
      }

      // Simply add ambient component
      v_ambientColour += lightLevel * u_l_ambient[i];
    }
  }

  // Multiply colour components already calculated by their corresponding
  // material colours.  Diffuse lighting flatness is determined by the self-
  // illumination value.
  v_diffuseColour = u_m_diffuseColour * ( u_m_selfIllumination +
      ( 1.0 - u_m_selfIllumination ) * v_diffuseColour );
  v_specularColour *= u_m_specularColour * u_m_specularLevel;
  v_ambientColour *= u_m_ambientColour;

  // Use the diffuse colour alpha and opacity as the 'overall' alpha.
  v_diffuseColour.a = u_m_diffuseColour.a;


  /* Calculate sphere map environment reflection. */

  // Calculate reflection vector (view vector reflected off the surface) in
  // world space
  vec3 reflectWorld = -reflect( vertexToCameraNorm, normal );

  // Calculate sphere map texture coordinates. This is done here to gain
  // performance at the cost of quality.

  // Calculate   m = 2 * sqrt( r.x^2 + r.y^2 + (r.z + 1)^2)
  // See http://www.reindelsoftware.com/Documents/Mapping/Mapping.html

  vec3 reflectCamera = (u_t_view * vec4( reflectWorld, 0.0 )).xyz;
  v_sphereCoord.z = reflectCamera.z;
  reflectCamera.z += 1.0;
  float m = 2.0 * length( reflectCamera );

  v_sphereCoord.x = reflectCamera.x / m + 0.5;
  v_sphereCoord.y = 1.0 - ( reflectCamera.y / m + 0.5 );
}
