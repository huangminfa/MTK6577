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
 * PropertyLinker class.
 *
 */
#pragma once
#ifndef A3M_PROPERTYLINKER_H
#define A3M_PROPERTYLINKER_H

/******************************************************************************
 * Include Files
 ******************************************************************************/
#include <a3m/shaderprogram.h> /* ShaderProgram and ShaderProgram::Linker */

namespace a3m
{
  /** \defgroup a3mPropertyLinker PropertyLinker
   * \ingroup  a3mShaderprogram
   *
   * Utility class to perform linking of global properties with ShaderPrograms.
   *
   * @{
   */

  /**
   * Property linker class.
   * A convenience class which owns a list of properties, and implements the
   * ShaderProgram::Linker interface so that programs can use the property
   * values set in this object.  Implementing this functionality in its own
   * class avoids having to duplicate it in classes which required linking
   * capabilities (e.g. renderers).  It also allows different parts of a system
   * to share the same list of global property values (e.g. if an application
   * uses several different renderers).
   */
  class PropertyLinker : public ShaderProgram::Linker
  {
  public:
    /** Links a ShaderProgram's uniforms with the linker properties.
     * Called by the shader program if it needs linking.
     */
    void link(ShaderProgram& program /**< Shader program to link */);

    /** Add a property to the linker.
     * If a property is not expected to be set in an Appearance, it is
     * important that it be set in the linker, otherwise objects may not render
     * correctly.
     */
    template<typename T>
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

      m_properties[propertyName] =
        ShaderUniformBase::Ptr(new ShaderUniform<T>(size));
    }

    /** Sets the values of a named property.
     * If the property in question does not yet exist, it will be created.  A
     * warning will be logged if an index greater than zero is given, and the
     * property has not been previously defined as an array.
     */
    template<typename T>
    void setProperty(
        A3M_CHAR8 const* propertyName,
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
      }

      it->second->setValue(value, i);
    }

  private:
    typedef std::map<std::string, ShaderUniformBase::Ptr> PropertyMap;

    PropertyMap m_properties; /**< Properties mapped by name */
  };

  /** @} */

} /* end of namespace */

#endif /* A3M_PROPERTYLINKER_H */
