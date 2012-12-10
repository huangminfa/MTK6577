/*****************************************************************************
 *
 * Copyright (c) 2011 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 *****************************************************************************/
/** \file
 * ShaderProgram class
 *
 */
#pragma once
#ifndef A3M_SHADERPROGRAM_H
#define A3M_SHADERPROGRAM_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/detail/programresource.h>  /* for ProgramResource */
#include <a3m/detail/shaderresource.h>  /* for ShaderResource */
#include <a3m/assetcache.h> /* for AssetCache */
#include <a3m/pointer.h>          /* for SharedPtr */
#include <a3m/shaderuniform.h>   /* ShaderUniform */
#include <a3m/detail/resourcecache.h>   /* for ResourceCache */

namespace a3m
{
  /** \defgroup a3mShaderprogram Shader Program
   * \ingroup  a3mRefRender
   * \ingroup  a3mRefAssets
   *
   * The shader program class encapsulates a program running on the GPU
   * defined by a vertex shader and fragment shader pair.
   *
   * ShaderPrograms are Assets and are therefore managed by an AssetCache in the
   * AssetPool.
   *
   * \note
   * Generally, the client will want to use AssetCachePool to create and manage
   * asset caches, rather than creating them manually.
   *
   * \code
   * // Create a shader program cache and add a path from which to load programs
   * ShaderProgramCache::Ptr cache( new ShaderProgramCache() );
   * registerSource( *cache, ".\\assets\\shaders");
   *
   * // Register a loader for the cache to use
   * cache->registerLoader( myLoader );
   *
   * // Load ShaderProgram from ".\assets\shaders"
   * ShaderProgram::Ptr shaderProgram = cache->get( "phong.sp" );
   * \endcode
   *
   *  @{
   */

  /* Forward declarations */
  class ShaderProgramCache;
  class VertexBuffer;

  /** Shader Program class.
   * A ShaderProgram object contains an OpenGL shader program object. Upon
   * construction the supplied vertex and fragment shader source is compiled
   * and linked. The vertex attributes in a VertexBuffer can be bound to those
   * used in the shader program using the bind() method. All uniforms used by
   * the shader program can be obtained with the getUniforms() method. This
   * creates a linked list of uniforms which are used to store client-specific
   * values for each uniform. To "bind" a retrieved shader uniform, use
   * uniform->enable().
   */
  class ShaderProgram : public Shared, NonCopyable
  {
  public:
    A3M_NAME_SHARED_CLASS( ShaderProgram )

    /** Callback interface for linking with program uniforms.
     * While the value of many a uniform is local to the program which uses it,
     * some uniform values need to be broadcast globally to all programs.
     * Linker objects return a uniform value when requested by an program, so
     * that it can acquire a referenced to this shared global value.  This
     * provides a convenient way of setting a single uniform value on multiple
     * programs, without having to manually filter and update programs with the
     * said value.
     *
     * Classes can implement this interface if they own shader uniforms that
     * they wish to link using program::link().
     */
    class Linker
    {
    public:
      /** Virtual destructor.
       */
      virtual ~Linker() {}

      /** Only called if the shader program requires linking.
       */
      virtual void link(
          ShaderProgram& program
          /**< Program to link */) = 0;
    };

    /** Smart pointer type */
    typedef a3m::SharedPtr< ShaderProgram > Ptr;

    /** Cache type for this class */
    typedef ShaderProgramCache CacheType;

    /** Destructor. */
    ~ShaderProgram();

    /** Select this program for use. */
    void enable(
        A3M_BOOL forceEnableUniforms = A3M_FALSE
        /**< Whether to force the shader uniforms to update, irrespective of
         * whether they have changed. */);

    /** Validate the shader program.
     * Warning: this may be slow and should be used for debugging purposes
     * only.
     * \return A3M_TRUE if the program was successfuly compiled, linked and
     * validated.
     */
    A3M_BOOL isValid();

    /** Bind the VertexBuffer object.
     * Associates any vertex attributes used in the shader program with
     * matching attributes defined in the VertexBuffer.
     */
    void bind( VertexBuffer &buffer /**< Vertex buffer containing
                                         matching vertex attributes */ );

    /** Sets a uniform at a specific index.
     * Replaces the actual uniform object referenced by the shader program.  If
     * the shader program uniform in question has been linked, the link will be
     * undone.
     */
    void setUniform(
        A3M_INT32 i,
        /**< Uniform index */
        ShaderUniformBase::Ptr const& uniform
        /**< Uniform object to set in program */);

    /** Returns TRUE if a uniform with the given property name exists.
     */
    A3M_BOOL uniformExists(A3M_CHAR8 const* propertyName) const;

    /** Returns a uniform at a specific index.
     * \return Shader uniform object
     */
    ShaderUniformBase::Ptr const& getUniform(A3M_INT32 i /**< Index */) const;

    /** Returns a uniform by property name.
     * The returned object will be null if the uniform exists, but hasn't been
     * set yet (use uniformExists() to test for a uniform's existence).
     * \return Shader uniform object, or null if it doesn't exist
     */
    ShaderUniformBase::Ptr const& getUniform(
        A3M_CHAR8 const* propertyName
        /**< Property name */) const;

    /** Get the name of the uniform at a specific index.
     * This is the name of the uniform as it appears in the shader code.
     * \return Shader uniform name
     */
    A3M_CHAR8 const* getUniformName(A3M_INT32 i /**< Index */) const;

    /** Set the name of the property represented by a uniform at a given index.
     * The property of a uniform is used to identify its function independently
     * of its name in the shader code.
     */
    void setUniformPropertyName(
        A3M_INT32 i, /**< Index */
        A3M_CHAR8 const* propertyName /**< Property name */ );

    /** Get the name of the property represented by a uniform at a given index.
     * \return Property name
     */
    A3M_CHAR8 const* getUniformPropertyName(A3M_INT32 i /**< Index */) const;

    /** Gives the number of shader uniforms in this program.
     * \return The number of shader uniforms
     */
    A3M_INT32 getUniformCount() const;

    /** Links the uniforms in this shader program to a linker.
     * Any uniforms in the linker that reference the same property as a uniform
     * in the program will be "linked" (i.e. the linker and program will
     * literally share the same uniform object).
     */
    void link(Linker& linker /**< Linker to use */);

    /** Get the compiled binary code for the shader.
     * Use this function to save the compiled shader program. The buffer
     * pointed to by the "binary" parameter should be freed using
     * delete [] when you are finished with it.
     * \return true if successful.
     */
    A3M_BOOL getBinary( A3M_UINT32 &size,
                        /**< Length of binary data */
                        A3M_UINT32 &format,
                        /**< Format of binary data */
                        A3M_CHAR8 *&binary
                        /**< Buffer containing precompiled shader */);

  private:
    friend class ShaderProgramCache; /* Is ShaderProgram's factory class */

    /*
     * Structure containing a uniform and its associated data.
     */
    struct UniformInfo
    {
      UniformInfo(
          A3M_CHAR8 const* name,
          A3M_INT32 location,
          A3M_INT32 texUnit,
          ShaderUniformBase::Ptr uniform) :
        name(name),
        location(location),
        texUnit(texUnit),
        currentValue(uniform)
      {
        // UniformInfo::uniform stays initialised to null, until it gets
        // set via setUniform()
      }

      std::string name;
      std::string propertyName;
      A3M_INT32 location;
      A3M_INT32 texUnit;
      ShaderUniformBase::Ptr uniform;
      ShaderUniformBase::Ptr currentValue;
    };

    /*
     * Predicate objects for searching for uniforms in a vector.
     */
    struct UniformPropertyEquals
    {
      std::string propertyName;

      UniformPropertyEquals(A3M_CHAR8 const* value) :
        propertyName(value)
      {
      }

      A3M_BOOL operator()(UniformInfo const& uniform)
      {
        return uniform.propertyName == propertyName;
      }
    };

    /*
     * Private constructor.
     * This constructor is called by ShaderProgramCache.
     */
    ShaderProgram(
        detail::ProgramResource::Ptr const& resource /* Program resource */);

    /* Utility function to create a shader uniform. */
    template< class T >
    void createUniform( A3M_CHAR8 const *propertyName,
                        A3M_INT32 location,
                        A3M_INT32 texUnit,
                        A3M_INT32 size );

    /* Builds an internal list of all the uniforms defined by this program.
     */
    void getUniforms();

    /* Enables the uniforms whose values have changed.
     */
    void enableUniforms(A3M_BOOL force = A3M_FALSE);

    typedef std::vector<UniformInfo> UniformVector;

    A3M_BOOL m_linked; /* Indicates whether program has been linked */
    detail::ProgramResource::Ptr m_resource; /* OpenGL program resource */
    UniformVector m_uniforms; /* List of program uniforms */
  };

  /**
   * AssetCache specialised for storing and creating ShaderProgram assets.
   */
  class ShaderProgramCache : public AssetCache<ShaderProgram>
  {
  public:
    /** Smart pointer type for this class */
    typedef SharedPtr< ShaderProgramCache > Ptr;

    /** Creates a shader program from source.
     * Compiles and links the given vertex and fragment shader source code into
     * an OpenGL shader program object.
     * \return The shader program, or null if compilation or linking failed
     */
    ShaderProgram::Ptr create(
        A3M_CHAR8 const* vsSource, /**< Source code for vertex shader */
        A3M_CHAR8 const* fsSource, /**< Source code for fragment shader */
        A3M_CHAR8 const* name = 0 /**< Optional name to give the asset. If
                                    omitted, the asset will not be reachable
                                    via the AssetCache::get() function. */);

    /** Creates a shader program from precompiled binary.
     * Loads a precompiled shader program from memory. This function may not be
     * supported on certain platforms, and will return null if so.
     * \return The shader program, or null if loading failed
     */
    ShaderProgram::Ptr create(
        A3M_UINT32 size, /**< Length of binary data */
        A3M_UINT32 format, /**< Format of binary data */
        A3M_CHAR8 const* binary, /**< Buffer containing precompiled shader */
        A3M_CHAR8 const* name = 0 /**< Optional name to give the asset. If
                                    omitted, the asset will not be reachable
                                    via the AssetCache::get() function. */);
  };

  /** @} */

} /* end of namespace */

#endif /* A3M_SHADERPROGRAM_H */
