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
 * Animation classes
 */

#pragma once
#ifndef ANIMATION_H
#define ANIMATION_H

#include <a3m/base_types.h>   /* for A3M_INT32 etc.         */
#include <a3m/pointer.h>      /* for SharedPtr              */
#include <vector>             /* std::vector class template */
#include <algorithm>          /* for lower_bound function   */

namespace a3m
{
  /** \todo Integrate into documentation (this is not currently in any groups) */

  /** \todo Explain AnimationKey
   */
  template< class CoeffType, A3M_INT32 coeffCount >
  struct AnimationKey
  {
    /** Definition for actual coefftype for coeffCount > 1 (array) */
    template< class T, A3M_INT32 count >
    struct Coeff { /** Coeff type */ typedef T Type[count]; };

    /** Definition actual coefftype for coeffCount == 1 (scalar) */
    template< class T >
    struct Coeff< T, 1 > { /** Coeff type */ typedef T Type; };

    /** Time this 'key point' exists at */
    A3M_FLOAT time;

    /** \todo explain use of this CoeffType / coeffCount */
    typename Coeff< CoeffType, coeffCount >::Type coeff;

    /** Override comparison operator to return comparison of time values only */
    A3M_BOOL operator<( AnimationKey< CoeffType, coeffCount > const &other ) const
    {
      return time < other.time;
    }
  };

  class Animation;

  /** Animation Listener - methods linking events and consequences. */
  class AnimationListener
  {
  public:
    /** Destructor */
    virtual ~AnimationListener() {}
    /** \todo explain onFinished */
    virtual void onFinished( Animation * ) = 0;
    /** \todo explain onReleaseListener */
    virtual void onReleaseListener( Animation * ) = 0;
  };

  /** Animation class, which simply provides a couple of base methods.
   * Animation uses a floating point 'time' axis to manage the state of the
   * animated value. Note that 'animation' is not limited to physical movement
   * but could include animation of colours, light values etc.
   */
  class Animation : public Shared
  {
  public:
    A3M_NAME_SHARED_CLASS( Animation )

    /** Smart pointer type for this class */
    typedef SharedPtr< Animation > Ptr;

    /** Destructor */
    virtual ~Animation() {}

    /** Set the animation to the specified point in 'time' along it's length */
    virtual void update( A3M_FLOAT time /**< position to set to */ ) = 0;

    /** Returns the length of the animation */
    virtual A3M_FLOAT length() = 0;
  };

  /** Animation Group wraps one or more animations, and animation management. */
  class AnimationGroup : public Animation
  {
  public:
    /** Smart pointer type for this class */
    typedef SharedPtr< AnimationGroup > Ptr;

    /** Constructor */
    AnimationGroup();

    /** Destructor */
    ~AnimationGroup();

    /** Set the link between this animation and the event handler */
    void setListener( AnimationListener *listener /**< Ptr to handler */ );

    /** Add an animation to the Group */
    void addAnimation( Animation *animation /**< Ptr to the animation to add in */ );

    virtual void update( A3M_FLOAT time );

    virtual A3M_FLOAT length() {return m_length;}

    /** Set the start and end points of a loop.  Will have no effect
     * unless and until looping is enabled with setLooping().
     */
    void setLoopPoints( A3M_FLOAT start,/**< point to which the loop returns to */
                        A3M_FLOAT end   /**< point where the animation loops
                                             back to the loop start */
                      );

    /** enable /disable looping */
    void setLooping( A3M_BOOL loop /**< set TRUE is looping is required */ );

    /** Start the animation from a specifed point along the
     *  length of the animation. */
    void start( A3M_FLOAT position /**< point at which to start */ );

    /** Stop animation */
    void stop();

  private:
    std::vector< Animation * > m_animations;
    A3M_BOOL m_looping;
    enum State
    {
      STOPPED,
      STARTING,
      PLAYING
    };
    State m_state;
    A3M_FLOAT m_loop_start;
    A3M_FLOAT m_loop_end;
    A3M_FLOAT m_length;
    A3M_FLOAT m_timeOrigin;
    AnimationListener *m_listener;
  };

  /** \todo explain AnimationChannel, and Animator */
  template< class CoeffType, A3M_INT32 coeffCount, class Animator >
  class AnimationChannel : public Animation
  {
  public:
    /** \todo explain use of Key */
    typedef AnimationKey< CoeffType, coeffCount > Key;

    /** Constructor */
    AnimationChannel( Key *keys, A3M_UINT32 keyCount, Animator animator )
      : m_animator( animator )
    {
      m_keys.assign( keys, keys + keyCount );
    }

    virtual void update( A3M_FLOAT time )
    {
      Key key;
      key.time = time;
      typename std::vector< Key >::iterator i;
      i = std::lower_bound( m_keys.begin(), m_keys.end(), key );
      if( i == m_keys.end() )
      {
        --i;
      }
      m_animator.set( i->coeff );
    }
    virtual A3M_FLOAT length() {return m_keys.back().time;}

  private:
    Animator m_animator;
    std::vector< Key > m_keys;
  };
}

#endif // ANIMATION_H
