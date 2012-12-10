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

/*
 * Very simple shader just renders flat diffuse lighting
 */

precision mediump float;

/* Transformation uniforms */
uniform mat4 u_t_modelViewProjection; // Model to projection space transform

/* Vertex attributes */
attribute vec4 a_position;    // Position in model space
attribute vec2 a_uv0;         // Texture coordinate

// Texture coordinate copied from vertex attribute
varying lowp vec2 v_texCoord;

void main()
{
  // Final position is model position multiplied by model view projection matrix
  gl_Position = u_t_modelViewProjection * a_position;

  // Pass through texture coordinate
  v_texCoord = a_uv0;
}
