/*****************************************************************************
 *
 * Copyright (c) 2012 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 *****************************************************************************/
/** \file
 * ShaderResource
 *
 */
#ifndef A3M_SHADERRESOURCE_H
#define A3M_SHADERRESOURCE_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/pointer.h>                /* for SharedPtr   */
#include <a3m/detail/glresource.h>      /* for GlResource */

namespace a3m
{
  namespace detail
  {
    /**
     * \ingroup  a3mResource
     * An OpenGL shader resource.
     */
    class ShaderResource : public GlResource
    {
    public:
      /** Smart pointer type for this class */
      typedef SharedPtr<ShaderResource> Ptr;

      /** Type of shader - simply Vertex or Fragment */
      enum ShaderType
      {
        VERTEX, /**< Vertex shader */
        FRAGMENT /**< Fragment (pixel) shader */
      };

      /** ShaderResource constructor */
      ShaderResource(ShaderType shaderType /**< Type of shader, e.g. VERTEX */) :
        GlResource(),
        m_shaderType(shaderType)
      {
      }

      /** Get the \b OpenGL type of the shader.
       * \return GL type - e.g. for VERTEX shaders this returns GL_VERTEX_SHADER
       */
      A3M_UINT32 getGlShaderType() const;

    private:
      // Override
      A3M_UINT32 doAllocate();

      // Override
      void doDeallocate(A3M_UINT32 id);

      ShaderType m_shaderType;
    };

  } /* namespace detail */

} /* namespace a3m */

#endif /* A3M_SHADERRESOURCE_H */

