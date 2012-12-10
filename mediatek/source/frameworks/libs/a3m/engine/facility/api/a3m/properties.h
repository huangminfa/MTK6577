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
 * Built-in property names.
 * This file contains names for properties which are recognised and used by the
 * various subsystems of A3M.  These names should always be used within A3M,
 * rather than the strings they represent.
 */
#pragma once
#ifndef A3M_PROPERTIES_H
#define A3M_PROPERTIES_H

#include <a3m/base_types.h> /* A3M_CHAR8 */

/******************************************************************************
 * Include Files
 ******************************************************************************/

namespace a3m
{

  namespace properties
  {

      /***********************
       * Material properties *
       ***********************/

      static A3M_CHAR8 const* M_DIFFUSE_COLOUR = "M_DIFFUSE_COLOUR";
      static A3M_CHAR8 const* M_AMBIENT_COLOUR = "M_AMBIENT_COLOUR";
      static A3M_CHAR8 const* M_EMISSIVE_COLOUR = "M_EMISSIVE_COLOUR";
      static A3M_CHAR8 const* M_SPECULAR_COLOUR = "M_SPECULAR_COLOUR";

      static A3M_CHAR8 const* M_SHININESS = "M_SHININESS";
      static A3M_CHAR8 const* M_OPACITY = "M_OPACITY";
      static A3M_CHAR8 const* M_SELF_ILLUMINATION = "M_SELF_ILLUMINATION";
      static A3M_CHAR8 const* M_SPECULAR_LEVEL = "M_SPECULAR_LEVEL";
      static A3M_CHAR8 const* M_UV_OFFSET_SCALE = "M_UV_OFFSET_SCALE";

      static A3M_CHAR8 const* M_DIFFUSE_TEXTURE = "M_DIFFUSE_TEXTURE";
      static A3M_CHAR8 const* M_AMBIENT_TEXTURE = "M_AMBIENT_TEXTURE";
      static A3M_CHAR8 const* M_EMISSIVE_TEXTURE = "M_EMISSIVE_TEXTURE";
      static A3M_CHAR8 const* M_SPECULAR_TEXTURE = "M_SPECULAR_TEXTURE";

      static A3M_CHAR8 const* M_SPECULAR_LEVEL_TEXTURE =
        "M_SPECULAR_LEVEL_TEXTURE";
      static A3M_CHAR8 const* M_GLOSS_TEXTURE = "M_GLOSS_TEXTURE";
      static A3M_CHAR8 const* M_FILTER_COLOUR_TEXTURE =
        "M_FILTER_COLOUR_TEXTURE";
      static A3M_CHAR8 const* M_REFLECTION_TEXTURE = "M_REFLECTION_TEXTURE";
      static A3M_CHAR8 const* M_REFRACTION_TEXTURE = "M_REFRACTION_TEXTURE";
      static A3M_CHAR8 const* M_MIRROR_TEXTURE = "M_MIRROR_TEXTURE";
      static A3M_CHAR8 const* M_BUMP_TEXTURE = "M_BUMP_TEXTURE";
      static A3M_CHAR8 const* M_DISPLACEMENT_TEXTURE = "M_DISPLACEMENT_TEXTURE";

      /********************
       * Light properties *
       ********************/

      static A3M_CHAR8 const* L_COUNT = "L_COUNT";
      static A3M_CHAR8 const* L_DIFFUSE_COLOUR = "L_DIFFUSE_COLOUR";
      static A3M_CHAR8 const* L_AMBIENT_COLOUR = "L_AMBIENT_COLOUR";
      static A3M_CHAR8 const* L_SPECULAR_COLOUR = "L_SPECULAR_COLOUR";
      static A3M_CHAR8 const* L_POSITION = "L_POSITION";
      static A3M_CHAR8 const* L_ATTENUATION_NEAR = "L_ATTENUATION_NEAR";
      static A3M_CHAR8 const* L_ATTENUATION_RECIPROCAL = "L_ATTENUATION_RECIPROCAL";
      static A3M_CHAR8 const* L_SPOT_DIRECTION = "L_SPOT_DIRECTION";
      // Cosine of the half-angle of the light's "full brightness" cone
      static A3M_CHAR8 const* L_SPOT_INNER_COS = "L_SPOT_INNER_COS";
      // Cosine of the half-angle of the light's "fall-off" cone
      static A3M_CHAR8 const* L_SPOT_OUTER_COS = "L_SPOT_OUTER_COS";

      /************************
       * Transform properties *
       ************************/

      static A3M_CHAR8 const* T_MODEL = "T_MODEL";
      static A3M_CHAR8 const* T_VIEW = "T_VIEW";
      static A3M_CHAR8 const* T_MODEL_VIEW = "T_MODEL_VIEW";
      static A3M_CHAR8 const* T_MODEL_VIEW_PROJECTION =
        "T_MODEL_VIEW_PROJECTION";
      static A3M_CHAR8 const* T_NORMAL_MODEL = "T_NORMAL_MODEL";
      static A3M_CHAR8 const* T_CAMERA_POSITION = "T_CAMERA_POSITION";

      /****************************
       * Miscellaneous properties *
       ****************************/

      static A3M_CHAR8 const* TIME = "TIME";

  } // namespace properties

} // namespace a3m

#endif // A3M_PROPERTIES_H
