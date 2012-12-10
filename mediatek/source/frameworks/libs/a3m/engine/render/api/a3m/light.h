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
 * Light class
 *
 */
#pragma once
#ifndef A3M_LIGHT_H
#define A3M_LIGHT_H

/*****************************************************************************
 * Include Files
 *****************************************************************************/
#include <a3m/angle.h>           /* for Anglef                               */
#include <a3m/scenenode.h>       /* for SceneNode (base class)               */
#include <a3m/colour.h>          /* for Colour4f                             */

namespace a3m
{
  /** \todo Integrate into documentation (this is not currently in any groups) */

  /** Light class.
   * A Light object represents a point, spot or directional light as part of a
   * scene graph.
   */
  class Light : public SceneNode
  {
  public:
    A3M_NAME_SHARED_CLASS( Light )

    /** Smart pointer type for this class */
    typedef SharedPtr< Light > Ptr;

    /** Type ID for this class */
    static A3M_UINT32 const NODE_TYPE;

    /**
     * Enum LightType
     *
     * Describes the type of light as enum.
     */
    enum LightType
    {
      LIGHTTYPE_OMNI,        /**< Omni light        */
      LIGHTTYPE_DIRECTIONAL, /**< Directional light */
      LIGHTTYPE_SPOT         /**< Spot light        */
    };

    /** Constructor.
     * Constructs an empty solid node with an identity local transformation.
     */
    Light();

    /**
     * Returns light name
     * \return Light name
     */
    A3M_CHAR8 const *getLightName() const { return m_lightName.c_str(); }

    /**
     * Sets the Light's name.
     */
    void setLightName( A3M_CHAR8 const * name /**< New name for this Light */ );

    // Override
    virtual A3M_UINT32 getType() const { return NODE_TYPE; }

    /**
     * It activates or deactivates light.
     *
     * \return None.
     */
    void setActive( A3M_BOOL active
                    /**< Active = A3M_TRUE -> light ON else OFF */ );

    /**
     * Sets light type parameter as one of the supported in enum LightType.
     *
     * Default light type is "Directional" (LIGHTTYPE_DIRECTIONAL)
     * \return None.
     */
    void setLightType( LightType lightType /**< Type of light */ );

    /**
     * Sets diffuse colour for a light.
     * Here, colour values (r,g,b,a) are passed in Colour4f struct format.
     *
     * Default is white colour (r,g,b,a) = (1.0,1.0,1.0,1.0)
     * \return None.
     */
    void setDiffuseColour( const Colour4f& diffuseColour
                           /**< Diffuse colour of a light passed in as const
                                reference to a Colour4f struct variable. */ );

    /**
     * Sets ambient colour for a light.<BR>
     * Here, colour values (r,g,b,a) are passed in colour4f struct format.
     *
     * Default is black colour (r,g,b,a) = (0,0,0,1.0)
     * \return None.
     */
    void setAmbientColour( const Colour4f& ambientColour
                           /**< Ambient colour of a light passed in as const
                                reference to a Colour4f struct variable. */ );


    /**
     * Sets specular colour for a light.
     * Here, colour values (r,g,b,a) are passed in colour4f struct format.
     *
     * Default is white colour (r,g,b,a) = (1.0,1.0,1.0,1.0)
     * \return None.
     */
    void setSpecularColour( const Colour4f& specularColour
                            /**< Specular colour of a light passed in as const
                                 reference to a Colour4f struct variable. */ );

    /**
     * Sets intensity of a light.
     *
     * Intensity is the multiplication factor to light colour and can be used
     * to dim the light.
     *
     * Default intensity is 1.0.
     * \return None.
     */
    void setIntensity( A3M_FLOAT intensity
                       /**< Intesity of light. Multiplication factor applied to
                            the light colour. Normal range is [0, 1.0] but,
                            current implementation can support -ve and above
                            1.0 value as well. */ );

    /**
     * Set distance at which light intensity begins to attenuate.
     *
     * Light intensity attenuates between the set "Near" and "Far" distances.
     * Points nearer the "Near" distance are lit with full intensity. Points
     * farther than the "Far" distance are not lit.
     *
     * see: \ref setIsAttenuated
     *
     * Default value is 5.0 units.
     * \return None.
     */
    void setAttenuationNear( A3M_FLOAT attenuationNear
                         /**< new attenuation near value */ );

    /**
     * Set distance at which light intensity falls to zero.
     *
     * Light intensity attenuates between the set "Near" and "Far" distances.
     * Points nearer the "Near" distance are lit with full intensity. Points
     * farther than the "Far" distance are not lit.
     *
     * see: \ref setIsAttenuated
     *
     * Default value is 10.0 units.
     * \return None.
     */
    void setAttenuationFar( A3M_FLOAT attenuationFar
                         /**< new attenuation far value  */ );

    /**
     * Sets inner angle for inner region of a spot light.
     *
     * A spot light having two regions (inner region and outer region) require
     * angles to be specified. Inner region of a spot light is specified with
     * inner radius and inner angle and; it will have full intensity of spot
     * light. This parameter value should be ignored for light other than spot
     * light.
     *
     * Default inner angle is 30 degrees.
     * \return None.
     */
    void setSpotInnerAngle( Anglef const& spotInnerAngle
                            /**< Absolute value of inner angle for spot
                                 light. */ );

    /**
     * Sets full angle including inner and outer region of a spot light.
     *
     * A spot light having two regions (inner region and outer region) require
     * angles to be specified. Outer angle specifies angle spanning one side of
     * centre line to other side for spot light. In outer region light
     * intensity falls gradually. This parameter value should be ignored for
     * light other than spot light.
     *
     * Default outer angle is 45 degrees.
     * \return None.
     */
    void setSpotOuterAngle( Anglef const& spotOuterAngle
                            /**< Absolute value of the outer angle for spot
                                 light. */ );

    /** Set shadow casting behaviour of light.
     *
     */
    void setCastsShadows( A3M_BOOL castsShadows /**< A3M_TRUE if this light
                                                     should cast shadows */ )
    {
      m_castsShadows = castsShadows;
    }

    /** Set attenuation behaviour of light.
     *
     * By default the intensity of a light drops starting at the distance
     * specified by the inner radius, reaching zero at the distance specified
     * by the outer radius. If isAttenuated is set to false, then the light
     * intensity will stay at full regardless of distance from the light to the
     * lit object.
     */
    void setIsAttenuated( A3M_BOOL isAttenuated /**< A3M_TRUE if this light
                          should use inner and outer radius to attenuate the
                          light intensity according to distance from the light
                          */ )
    {
      m_isAttenuated = isAttenuated;
    }

    /**
     * Returns whether light is currently active or not
     *
     * \return Active (A3M_TRUE) or inactive (A3M_FALSE)
     */
    A3M_BOOL getActive() const { return m_active; }

    /**
     * Returns light type
     *
     * \return Light type as enum LightType.
     */
    LightType getLightType() const { return m_lightType; }

    /**
     * Returns diffuse colour of light
     *
     * \return Diffuse colour as a const reference to Colour4f struct
     */
    const Colour4f& getDiffuseColour() const { return m_diffuseColour; }

    /**
     * Returns ambient colour of light
     *
     * \return Ambient colour as a const reference to Colour4f struct
     */
    const Colour4f& getAmbientColour() const { return m_ambientColour; }

    /**
     * Returns specular colour of light
     *
     * \return Specular colour as a const reference to Colour4f struct
     */
    const Colour4f& getSpecularColour() const { return m_specularColour; }

    /**
     * Returns intensity of light
     *
     * \return Intensity of light
     */
    A3M_FLOAT getIntensity() const { return m_intensity; }

    /**
     * Returns distance at which intensity begins to attenuate.
     *
     * see: \ref setAttenuationNear
     *
     * \return near attenuation of light.
     */
    A3M_FLOAT getAttenuationNear() const { return m_attenuation_near; }

    /**
     * Returns distance at which intensity falls to zero.
     *
     * see: \ref setAttenuationFar
     *
     * \return far attenuation of light.
     */
    A3M_FLOAT getAttenuationFar() const { return m_attenuation_far; }

    /**
     * Returns inner angle of spot light.
     *
     * \return Inner angle of spot light
     */
    Anglef const& getSpotInnerAngle() const { return m_spotInnerAngle; }

    /**
     * Returns outer angle of spot light.
     *
     * \return Outer angle of spot light
     */
    Anglef const& getSpotOuterAngle() const { return m_spotOuterAngle; }

    /** Get shadow casting behaviour of light.
     *
     * \return A3M_TRUE if this light should cause objects to cast shadows
     */
    A3M_BOOL getCastsShadows() const { return m_castsShadows; }

    /** Get attenuation behaviour of light.
     *
     * By default the intensity of a light drops starting at the distance
     * specified by the inner radius, reaching zero at the distance specified
     * by the outer radius. If isAttenuated is set to false, then the light
     * intensity will stay at full regardless of distance from the light to the
     * lit object.
     * \return A3M_TRUE if intensity of this light should fall with distance
     */
    A3M_BOOL getIsAttenuated()
    {
      return m_isAttenuated;
    }

    /** Accept a scene node visitor.
     */
    virtual void accept( SceneNodeVisitor &visitor /**< [in] visitor */ );

  private:
    // Flag indicating whether light is active or not
    A3M_BOOL m_active;

    // True if this light should cause objects to cast shadows
    A3M_BOOL m_castsShadows;

    // Light type - spot/directional/omni-directional
    LightType m_lightType;

    // "Brightness" of the light
    A3M_FLOAT m_intensity;

    // Attentation near distance
    A3M_FLOAT m_attenuation_near;

    // Attentation far distance
    A3M_FLOAT m_attenuation_far;

    // Spot-light cone inner angle
    Anglef m_spotInnerAngle;

    // Spot-light cone outer angle
    Anglef m_spotOuterAngle;

    // Diffuse colour component
    Colour4f m_diffuseColour;

    // Ambient colour component
    Colour4f m_ambientColour;

    // Specular colour component
    Colour4f m_specularColour;

    // True if light intensity should be attenuated with distance
    A3M_BOOL m_isAttenuated;

    // Light name
    std::string m_lightName;
  };
} /* namespace a3m */

#endif /* A3M_LIGHT_H */
