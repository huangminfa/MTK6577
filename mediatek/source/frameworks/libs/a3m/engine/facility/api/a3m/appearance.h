/**************************************************************************
 *
 * Copyright (c) 2010 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/** \file
 * Appearance class
 *
 */
#pragma once
#ifndef A3M_APPEARANCE_H
#define A3M_APPEARANCE_H

/******************************************************************************
 * Include Files
 ******************************************************************************/
#include <a3m/assetcachepool.h>  /* AssetCachePool             */
#include <a3m/base_types.h>      /* A3M base type defines      */
#include <a3m/pointer.h>         /* for SharedPtr              */
#include <a3m/noncopyable.h>     /* for NonCopyable            */
#include <a3m/shaderprogram.h>   /* for ShaderProgram::Ptr     */
#include <a3m/shaderuniform.h>   /* for ShaderUniformBase::Ptr */
#include <a3m/stream.h>          /* for Stream::Ptr            */
#include <a3m/texture2d.h>       /* for Texture2DCache         */
#include <a3m/polygonmode.h>     /* for PolygonMode            */
#include <a3m/compositingmode.h> /* for CompositingMode        */
#include <a3m/renderdevice.h>    /* for render()               */

#include <map>                   /* for std::map               */
#include <string>                /* for std::string            */
#include <vector>                /* for std::vector            */

namespace a3m
{
  /** \defgroup a3mAppearance Appearance
   * \ingroup  a3mRefScene
   *
   * The appearance class specifies all aspects of how a primitive will be
   * rendered. It contains a ShaderProgram and a list of properties which
   * correspond to ShaderUniforms in the ShaderProgram. It also specifies the
   * polygon mode (culling operations), alpha blending mode and depth
   * testing/writing that will be used.
   *
   * Uniforms in the ShaderProgram are associated with defined property names.
   * (this association is usually made in the shader program file).  The
   * property values can either be defined on global scale, usually by the
   * renderer, or can be defined on a per-Appearance level.  Be aware that
   * global uniform values take precidence over Appearance-level values; if
   * this is not desired, custom properties can be used for the uniforms which
   * require a per-Appearance value; there is, however, usually little or no
   * overlap between the two groups.
   *
   * The most convenient way of defining properties is in appearance (.ap)
   * files, although properties may be set programmatically.  Setting the
   * property will automatically add it to the Appearance if it doesn't yet
   * exist, but arrays must be explicitly added before being set:
   *
   * \code
   * // Set standard "A3M-defined" diffuse colour
   * appearance.setProperty( properties::M_DIFFUSE_COLOUR, diffuseColour ) );
   *
   * // Add and set custom user-defined property array of size 2
   * appearance.addProperty( "CUSTOM_PARAMS", 2 ) );
   * appearance.setProperty( "CUSTOM_PARAMS", myFirstParam, 0 ) );
   * appearance.setProperty( "CUSTOM_PARAMS", myOtherParam, 1 ) );
   * \endcode
   *
   * While you are free to define your own properties, A3M has a defined set of
   * property names which are recognised by the built-in Glo loader and
   * renderer, and can be found in properties.h.
   *
   *  @{
   */

  /** Interface for property collector objects.
   * Classes should implement this interface to be able to collect property data
   * via the Appearance::collectProperties() function.
   */
  class PropertyCollector
  {
  public:
    virtual ~PropertyCollector() {}

    /** Called by the appearance once per property to pass property information.
     * \return A3M_TRUE to continue collecting the properties, or A3M_FALSE to
     * terminate collection early.
     */
    virtual A3M_BOOL collect(
        ShaderUniformBase::Ptr const& uniform,
        /**< Property uniform object */
        A3M_CHAR8 const* name,
        /**< Name of the property */
        A3M_INT32 index
        /**< Index of the property uniform in its associated shader
         * program */) = 0;
  };

  /** Appearance class.
   * The Appearance class specifies all aspects of how a primitive will be
   * rendered. It contains a ShaderProgram and a list of ShaderUniforms that
   * will be written to the ShaderProgram. It also specifies the polygon mode
   * (culling operations), alpha blending mode and depth testing/writing that
   * will be used.
   */
  class Appearance : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( Appearance )

    /** Smart pointer type for this class */
    typedef SharedPtr< Appearance > Ptr;

    /** Default constructor.
     */
    Appearance() :
      m_linked(A3M_FALSE)
    {
    }

    /**
     * Constructs an appearance from an appearance file.
     */
    Appearance(
      AssetCachePool& pool,
      /**< Pool from which to load the required assets */
      A3M_CHAR8 const* appearanceName
      /**< Name of appearance file to read */ );

    /** Set ShaderProgram.
     * Sets the ShaderProgram object used by this Appearance.
     */
    void setShaderProgram( ShaderProgram::Ptr const &shaderProgram
                           /**< [in] ShaderProgram */ );
    /** Get ShaderProgram.
     * \return the ShaderProgram object used by this Appearance.
     */
    ShaderProgram::Ptr const &getShaderProgram() const;

    /** Add a property to the appearance.
     * If a property has not been set globally, it is important that it be set
     * in the appearance, otherwise objects using the appearance may not render
     * correctly.
     */
    template< typename T >
    void addProperty(
        A3M_CHAR8 const *propertyName,
        /**< Name of the property */
        A3M_INT32 size = 1
        /**< Size of the property (should be greater than 1 for arrays) */)
    {
      if (size < 1)
      {
        // Logical error in client's code.
        A3M_LOG_ERROR("Size of property cannot be less than 1.");

        return;
      }

      ShaderUniformBase::Ptr uniform(new ShaderUniform<T>(size));
      m_properties[propertyName] = Property(uniform);
      m_linked = A3M_FALSE;
    }

    /** Sets the values of a named property.
     * If the property in question does not yet exist, it will be created.  A
     * warning will be logged if an index greater than zero is given, and the
     * property has not been previously defined as an array.
     */
    template< typename T >
    void setProperty(
        A3M_CHAR8 const *propertyName,
        /**< Name of the property */
        T const &value,
        /**< Value to set */
        A3M_INT32 i = 0
        /**< Index into property array (should equal 0 for non-arrays) */ )
    {
      if (i < 0)
      {
        // Logical error in client's code.
        A3M_LOG_ERROR("Property index cannot be less than 0.");
        return;
      }

      PropertyMap::iterator it = m_properties.find( propertyName );

      if ( it == m_properties.end() )
      {
        if (i > 0)
        {
          A3M_LOG_WARN("Property array \"%s[%d]\" value is being set without "
              "prior knowledge of the size of the array; please define size of "
              "array explicitly using addProperty() before setting values.",
              propertyName, i);
        }

        addProperty< T >( propertyName, i + 1 );
        it = m_properties.find( propertyName );
      }

      it->second.uniform->setValue( value, i );
    }

    /** Gets the uniform associated with a property by name.
     * \return Property uniform, or null if the property doesn't exist
     */
    ShaderUniformBase::Ptr const& getPropertyUniform(
        A3M_CHAR8 const *propertyName
        /**< Name of the property */) const;

    /** Passes data for each property in turn to a collector object.
     * This function can be used to acquire information about each of the
     * properties in the appearance via callbacks.
     */
    void collectProperties(
        PropertyCollector* collector
        /**< Collector to which the appearance passes the data */ ) const;

    /** Get PolygonMode.
     \return reference to the PolygonMode object used by this Appearance.
     */
    PolygonMode const &getPolygonMode() const;

    /** Set PolygonMode.
     * Set the PolygonMode object used by this Appearance.
     */
    void setPolygonMode( PolygonMode const &newPolygonMode
                         /**<[in] PolygonMode to be used */ );

    /** Get CompositingMode.
     \return reference to the CompositingMode object used by this Appearance.
     */
    CompositingMode const &getCompositingMode() const;

    /** Set CompositingMode.
     * Set the CompositingMode object used by this Appearance.
     */
    void setCompositingMode( CompositingMode const &newCompositingMode
                         /**<[in] CompositingMode to be used */ );

    /** Returns the Appearance's name.
     * \return name for this Appearance
     */
    A3M_CHAR8 const *getName() const { return m_name.c_str(); }

    /** Sets the Appearance's name.
     */
    void setName( A3M_CHAR8 const * name /**< New name for this Appearance */ )
    {
      m_name = name;
    }

  private:
    /*
     * Properties associate a uniform value with an index into the shader
     * program, and are mapped by name.
     */
    struct Property
    {
      // Required to store in a std::map
      Property() :
        index(-1)
      {
      }

      explicit Property(
          ShaderUniformBase::Ptr uniform_, A3M_INT32 index_ = -1) :
        uniform(uniform_),
        index(index_)
      {
      }

      ShaderUniformBase::Ptr uniform;
      A3M_INT32 index;
    };

    /*
     * Declaring a3m::RenderDevice::render() as friend of a3m::Appearance
     * class. This is to enable RenderDevice::render() to call the
     * Appearance::enable() methods.
     *
     */
    friend void RenderDevice::render( VertexBuffer &vb,
                                      IndexBuffer &ib,
                                      Appearance &app );
    /* Enable this Appearance.
     * Makes this Appearance current for future drawing operations.
     */
    void enable();

    /* Enable this Appearance.
     * Makes this Appearance current for future drawing operations. This
     * version minimises state changes by comparing appearance settings
     * with the supplied Appearance which reflects the current state.
     */
    void enable( Appearance &other /* [inout] Appearance object reflecting
                                      current render state */ );

    /* Enables the shader program and the appearance properties.
     */
    void enableShaderProgram();

    /* Set the uniforms in the shader program which correspond to properties in
     * this appearance.  Properties are only set if they are null in the
     * shader program.
     */
    void applyProperties();

    /* Reset the properties defined in this appearance, in the shader program.
     */
    void resetProperties();

    /* Links properties defined in the appearance with uniforms in the shader
     * program.
     */
    void linkShaderProgram();

    typedef std::map<std::string, Property> PropertyMap;

    std::string            m_name;            /* Name of the appearance      */
    PolygonMode            m_polygonMode;     /* Culling mode                */
    CompositingMode        m_compositingMode; /* Compositing mode            */
    ShaderProgram::Ptr     m_shaderProgram;   /* Shared ShaderProgram object */
    PropertyMap            m_properties;      /* Properties mapped by name   */
    A3M_BOOL               m_linked;          /* Is program linked?          */
  };
  /** @} */

} /* end of namespace */

#endif /* A3M_APPEARANCE_H */
