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
/**
 * Viewer applet
 *
 */

#pragma once
#ifndef A3M_VIEWER_H
#define A3M_VIEWER_H

/******************************************************************************
 * Include Files
 ******************************************************************************/
#include "applet.h"             /* Applet base class    */
#include "realtime.h"           /* Timer                */
#include "animdialog.h"         /* Animation dialog     */
#include <a3m/angle.h>          /* Angle class          */
#include <a3m/assetcachepool.h> /* AssetCachePool class */
#include <a3m/glofile.h>        /* GLO file loading     */
#include <a3m/colour.h>         /* Colour4f             */
#include <a3m/camera.h>         /* Camera class         */
#include <a3m/simplerenderer.h> /* SimpleRenderer class */

#include <string>               /* std::string          */
#include <vector>               /* std::vector          */
#include <fstream>              /* std::ifstream        */
#include <sstream>              /* std::istringstream   */

/** An Applet-derived class for viewing GLO files.
 */

class SceneDialog;

class Viewer : public Applet, a3m::AnimationListener, AnimationDialog::Listener
{
public:
  // Pass in name of configuration file
  Viewer( A3M_CHAR8 const *filename, SceneDialog *sceneDialog );

  ~Viewer();

  // Overrides from Applet
  virtual A3M_BOOL init();
  virtual void draw();
  virtual void update();
  virtual void touchMove( A3M_INT32 x, A3M_INT32 y );
  virtual void touchUp( A3M_INT32 x, A3M_INT32 y );
  virtual void touchDown( A3M_INT32 x, A3M_INT32 y );
  virtual void keyDown( Keycode key );
  virtual void keyUp( Keycode key );
  virtual void mouseWheel( A3M_INT32 delta );

  // Overrides from AnimationListener
  virtual void onFinished( a3m::Animation * animation );
  virtual void onReleaseListener( a3m::Animation * );

  // Overrides from AnimationDialog::Listener
  virtual void onScrub( A3M_FLOAT );

private:

  void doAnimKeys( Keycode key );
  void doCameraKeys( Keycode key );

  void initConfig();    // Read config file and load glo files etc.
  void initCameras();   // Find cameras in scene
  void initLights();    // Add light if there are none in the scene

  // Filename is remembered in constructor, and loaded in init()
  std::string m_filename;

  // Collection of loaded GLO files.
  typedef std::vector< a3m::Glo > GloList;
  typedef std::vector< GloList > GloSeqList;
  GloList m_glos;
  GloSeqList m_gloSeqs;

  // Root node
  a3m::SceneNode::Ptr m_scene;
  a3m::AssetCachePool::Ptr m_pool;
  a3m::SimpleRenderer m_renderer;

  // Two camera nodes so that horizontal and vertical rotation can be dealt
  // With seperately. The m_cameraVRotate node's connected to the
  // m_cameraHRotate node. The m_cameraVRotate node's connected to the m_scene
  // node (them nodes, them nodes, them dry nodes...)
  a3m::SceneNode::Ptr m_cameraHRotate;
  a3m::SceneNode::Ptr m_cameraVRotate;

  a3m::Camera::Ptr m_camera;    // Default controllable camera
  A3M_UINT32 m_currentCamera;    // Index of current camera in m_cameras

  // Rotation angles
  a3m::Anglef m_hRotation;
  a3m::Anglef m_vRotation;

  // 'Look-at' position for camera and distance from camera
  a3m::Vector3f m_focus;
  A3M_FLOAT m_distance;

  // 'field of view' angle from camera
  a3m::Anglef m_fov;

  // the distance from the camera to the near clipping plane
  A3M_FLOAT m_near;

  // the distance from the camera to the far clipping plane
  A3M_FLOAT m_far;

  // Used to record relative mouse movement between calls to touchMove()
  A3M_INT32 m_lastX;
  A3M_INT32 m_lastY;

  A3M_BOOL m_ctrlDown; // Keep track of ctrl-key status
  A3M_BOOL m_shiftDown; // Keep track of shift-key status

  // For animation control.
  RealTime m_timer;
  A3M_BOOL m_animationLooping;
  A3M_BOOL m_animationPaused;
  A3M_FLOAT m_animationLength;

  a3m::Colour4f m_background;

  std::vector< a3m::Camera * > m_cameras;

  SceneDialog *m_sceneDialog;  // Pointer to scene dialog
};

#endif // A3M_VIEWER_H


