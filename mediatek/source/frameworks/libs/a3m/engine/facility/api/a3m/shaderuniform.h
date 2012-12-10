/*****************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 *****************************************************************************/
/** \file
 * ShaderUniform class
 *
 */
#pragma once
#ifndef A3M_SHADERUNIFORM_H
#define A3M_SHADERUNIFORM_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/pointer.h>          /* for SharedPtr */
#include <a3m/base_types.h>       /* for A3M_FLOAT */
#include <a3m/noncopyable.h>      /* for NonCopyable */
#include <a3m/vector2.h>       /* for Vector2<T> */
#include <a3m/vector3.h>       /* for Vector3<T> */
#include <a3m/vector4.h>       /* for Vector4<T> */
#include <a3m/matrix2.h>       /* for Matrix2<T> */
#include <a3m/matrix3.h>       /* for Matrix3<T> */
#include <a3m/matrix4.h>       /* for Matrix4<T> */
#include <a3m/texture2d.h>       /* for Texture2D */
#include <a3m/texturecube.h>     /* for TextureCube */

namespace a3m
{
  /** \defgroup a3mShaderUniform Shader Uniform
   * \ingroup  a3mRefRender
   * Shader 'uniforms' are parameters for the graphics shaders.
   *
   *  @{
   */

  /** Base class for ShaderUniform.
   * Actual uniforms are stored in ShaderUniform< class T >. The base class
   * provides the means to store ShaderUniforms of different types in a list
   * and be treated homogeneously.
   */
  class ShaderUniformBase : public Shared, NonCopyable
  {
    public:
      A3M_NAME_SHARED_CLASS( ShaderUniformBase )

      /** Smart pointer type for this class */
      typedef SharedPtr< ShaderUniformBase > Ptr;

      /** Destructor.
       * This must be virtual to correctly delete ShaderUniform< class T >
       * objects.*/
      virtual ~ShaderUniformBase() {}

      /** Enable this uniform.
       * Called by ShaderProgram object to load the data stored in this uniform
       * at the given location.  If the value is different from the supplied
       * uniform's value or the second parameter is false then the uniform
       * should be enabled and this uniform should be updated with the value of
       * the other uniform.
       */
      virtual void enable(
              ShaderUniformBase::Ptr const&other,
              /**< The new value to set set in the shader program */
              A3M_INT32 location,
              /**< Location of uniform in shader program */
              A3M_INT32 texUnit,
              /**< Texture unit to which this uniform is linked (only required
                   for texture uniforms) */
              A3M_BOOL force = A3M_FALSE
              /**< Set to A3M_TRUE to force the uniform to be enabled, even
               * if its value hasn't changed */ ) = 0;

      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * A3M_BOOL
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const A3M_BOOL &value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
       /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector2<A3M_BOOL>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Vector2<A3M_BOOL>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector3<A3M_BOOL>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Vector3<A3M_BOOL>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector4<A3M_BOOL>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Vector4<A3M_BOOL>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }

      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * A3M_UINT8
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const A3M_UINT8 &value,
                                 /**< new value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector2<A3M_UINT8>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Vector2<A3M_UINT8>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector3<A3M_UINT8>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Vector3<A3M_UINT8>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector4<A3M_UINT8>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Vector4<A3M_UINT8>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }

      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * A3M_INT32
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const A3M_INT32 &value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector2<A3M_INT32>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Vector2<A3M_INT32>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector3<A3M_INT32>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Vector3<A3M_INT32>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector4<A3M_INT32>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Vector4<A3M_INT32>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }

      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * A3M_FLOAT
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const A3M_FLOAT &value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector2<A3M_FLOAT>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Vector2<A3M_FLOAT>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector3<A3M_FLOAT>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Vector3<A3M_FLOAT>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector4<A3M_FLOAT>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Vector4<A3M_FLOAT>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }

      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Matrix2<A3M_FLOAT>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Matrix2<A3M_FLOAT>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Matrix3<A3M_FLOAT>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Matrix3<A3M_FLOAT>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }
      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Matrix4<A3M_FLOAT>
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const a3m::Matrix4<A3M_FLOAT>& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }

      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Texture2D::Ptr
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const Texture2D::Ptr& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }

      /** Set the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::TextureCube::Ptr
       * and therefore overrides this overload of setValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue( const TextureCube::Ptr& value,
                                 /**< New value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        return A3M_FALSE;
      }

      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * A3M_BOOL
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( A3M_BOOL &value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
       /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector2<A3M_BOOL>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Vector2<A3M_BOOL>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector3<A3M_BOOL>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Vector3<A3M_BOOL>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector4<A3M_BOOL>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Vector4<A3M_BOOL>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }

      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * A3M_UINT8
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( A3M_UINT8 &value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector2<A3M_UINT8>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Vector2<A3M_UINT8>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector3<A3M_UINT8>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Vector3<A3M_UINT8>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector4<A3M_UINT8>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Vector4<A3M_UINT8>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }

      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * A3M_INT32
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( A3M_INT32 &value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector2<A3M_INT32>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Vector2<A3M_INT32>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector3<A3M_INT32>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Vector3<A3M_INT32>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector4<A3M_INT32>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Vector4<A3M_INT32>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }

      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * A3M_FLOAT
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( A3M_FLOAT &value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector2<A3M_FLOAT>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Vector2<A3M_FLOAT>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector3<A3M_FLOAT>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Vector3<A3M_FLOAT>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Vector4<A3M_FLOAT>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Vector4<A3M_FLOAT>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }

      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Matrix2<A3M_FLOAT>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Matrix2<A3M_FLOAT>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Matrix3<A3M_FLOAT>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Matrix3<A3M_FLOAT>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }
      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Matrix4<A3M_FLOAT>
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( a3m::Matrix4<A3M_FLOAT>& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }

      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::Texture2D::Ptr
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( Texture2D::Ptr& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }

      /** Get the value contained in this object.
       * This will only work if the ShaderUniform class derived from
       * ShaderUniformBase has been instantiated with the type
       * a3m::TextureCube::Ptr
       * and therefore overrides this overload of getValue(). Otherwise the
       * default implementation here is called, failing to set the value and
       * returning false.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( TextureCube::Ptr& value,
                                 /**< Current value */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        return A3M_FALSE;
      }

      /** Returns the size of the uniform.
       * If the uniform is an array, the size will be greater than 1, otherwise,
       * it will be equal to 1.
       * \return Size of the uniform
       */
      virtual A3M_INT32 getSize() const = 0;
  };

  /** Class template for shader uniforms.
   * ShaderUniforms are created by a ShaderProgram and store uniform
   * data for a variety of types. The types supported are:
   * \code
   * A3M_BOOL
   * a3m::Vector2<A3M_BOOL>
   * a3m::Vector3<A3M_BOOL>
   * a3m::Vector4<A3M_BOOL>
   *
   * A3M_UINT8
   * a3m::Vector2<A3M_UINT8>
   * a3m::Vector3<A3M_UINT8>
   * a3m::Vector4<A3M_UINT8>
   *
   * A3M_INT32
   * a3m::Vector2<A3M_INT32>
   * a3m::Vector3<A3M_INT32>
   * a3m::Vector4<A3M_INT32>
   *
   * A3M_FLOAT
   * a3m::Vector2<A3M_FLOAT>
   * a3m::Vector3<A3M_FLOAT>
   * a3m::Vector4<A3M_FLOAT>
   * a3m::Matrix2<A3M_FLOAT>
   * a3m::Matrix3<A3M_FLOAT>
   * a3m::Matrix4<A3M_FLOAT>
   * \endcode
   */
  template <typename T>
  class ShaderUniform : public ShaderUniformBase
  {
    public:
      /** Constructor.
       */
      ShaderUniform(
          A3M_INT32 size = 1
          /**< Size of uniform.  Should be greater than 1 for arrays. */ ) :
        m_size(size)
      {
        if (m_size < 1)
        {
          A3M_LOG_ERROR("Uniform size must be greater than zero; setting to 1");
          m_size = 1;
        }

        m_value = new T[m_size];
      }

      /** Destructor */
      virtual ~ShaderUniform()
      {
        delete[] m_value;
      }

      // Override
      virtual void enable(
              ShaderUniformBase::Ptr const&other,
              /**< The new value to set set in the shader program */
              A3M_INT32 location,
              /**< Location of uniform in shader program */
              A3M_INT32 texUnit,
              /**< Texture unit to which this uniform is linked (only required
                   for texture uniforms) */
              A3M_BOOL force = A3M_FALSE
              /**< Set to A3M_TRUE to force the uniform to be enabled, even
               * if its value hasn't changed */ );

      /** Set uniform value.
       * Sets the actual data held by this uniform. This member function
       * overrides the overload of setValue() in ShaderUniformBase with the
       * matching parameter type.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was set.
       */
      virtual A3M_BOOL setValue(const T& value,
                                /**< Data to be stored in this uniform */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ )
      {
        if (i >= m_size)
        {
          return A3M_FALSE;
        }

        m_value[i] = value;

        return A3M_TRUE;
      }

      /** Get uniform value.
       * Gets the actual data held by this uniform. This member function
       * overrides the overload of getValue() in ShaderUniformBase with the
       * matching parameter type.
       * \return A3M_TRUE if the uniform has the correct type and the value
       * was fetched.
       */
      virtual A3M_BOOL getValue( T& value,
                                 /**< Current value of this uniform */
                                 A3M_INT32 i = 0
                                 /**< Index into uniform array (should equal 0
                                      for non-arrays) */ ) const
      {
        if (i >= m_size)
        {
          return A3M_FALSE;
        }

        value = m_value[i];

        return A3M_TRUE;
      }

      /** Returns the size of the uniform.
       * If the uniform is an array, the size will be greater than 1, otherwise,
       * it will be equal to 1.
       * \return Size of the uniform
       */
      virtual A3M_INT32 getSize() const
      {
        return m_size;
      }

    private:
      /* Performs the actual enabling of the uniforms.
       * This function exists so that certain types of uniform may be forced
       * to enable all the time.
       */
      void doEnable(
              ShaderUniformBase::Ptr const&other,
              A3M_INT32 location,
              A3M_INT32 texUnit,
              A3M_BOOL force );

      A3M_INT32 m_size; /**< Uniform size */
      T* m_value; /**< uniform value */
  };

  /** \cond
   * These function overloads call the appropriate version of glUniform for
   * each type. They are not intended for use outside of the
   * ShaderUniform<T>::enable member function.
   */
  namespace ShaderUniformPrivate
  {
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const A3M_BOOL* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Vector2<A3M_BOOL>* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Vector3<A3M_BOOL>* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Vector4<A3M_BOOL>* value );

    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const A3M_UINT8* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Vector2<A3M_UINT8>* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Vector3<A3M_UINT8>* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Vector4<A3M_UINT8>* value );

    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const A3M_INT32* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Vector2<A3M_INT32>* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Vector3<A3M_INT32>* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Vector4<A3M_INT32>* value );

    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const A3M_FLOAT* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Vector2<A3M_FLOAT>* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Vector3<A3M_FLOAT>* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Vector4<A3M_FLOAT>* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Matrix2<A3M_FLOAT>* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Matrix3<A3M_FLOAT>* value );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        const a3m::Matrix4<A3M_FLOAT>* value );

    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        Texture2D::Ptr const *texture2D );
    void setUniform( A3M_INT32 location, A3M_INT32 texUnit, A3M_INT32 size,
        TextureCube::Ptr const *textureCube );
  };



/*****************************************************************************
 * Implementation
 *****************************************************************************/

  template <typename T>
  inline void ShaderUniform<T>::enable(
      ShaderUniformBase::Ptr const&other, A3M_INT32 location, A3M_INT32 texUnit,
      A3M_BOOL force )
  {
    doEnable(other, location, texUnit, force);
  }

  template <>
  inline void ShaderUniform<Texture2D::Ptr>::enable(
      ShaderUniformBase::Ptr const&other, A3M_INT32 location, A3M_INT32 texUnit,
      A3M_BOOL force )
  {
    doEnable(other, location, texUnit, A3M_TRUE);
  }

  template <>
  inline void ShaderUniform<TextureCube::Ptr>::enable(
      ShaderUniformBase::Ptr const&other, A3M_INT32 location, A3M_INT32 texUnit,
      A3M_BOOL force )
  {
    doEnable(other, location, texUnit, A3M_TRUE);
  }

  template <typename T>
  void ShaderUniform<T>::doEnable(
      ShaderUniformBase::Ptr const&other, A3M_INT32 location, A3M_INT32 texUnit,
      A3M_BOOL force )
  {
    A3M_BOOL isDifferent = force;
    A3M_INT32 size = getSize();

    if (other)
    {
      A3M_ASSERT(other->getSize() >= size);

      for (A3M_INT32 i = 0; i < size; ++i)
      {
        T newValue;
        if( other->getValue( newValue, i ) && ( newValue != m_value[i] ) )
        {
          m_value[i] = newValue;
          isDifferent = A3M_TRUE;
        }
      }
    }

    if (isDifferent)
    {
      ShaderUniformPrivate::setUniform(location, texUnit, size, m_value);
    }
  }

  /** \endcond */

  /** @} */

} /* end of namespace */

#endif /* A3M_SHADERUNIFORM_H */
