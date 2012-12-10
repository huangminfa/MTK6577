#pragma once
#ifndef COM_MEDIATEK_A3M_JNI_NODEUTILITY_H
#define COM_MEDIATEK_A3M_JNI_NODEUTILITY_H

#include <a3m/scenenode.h> /* for SceneNode */

/** Returns the name of the Java class corresponding to the
 * type of the given scene node. */
char const* getNodeClassName(a3m::SceneNode::Ptr const& node);

#endif /* COM_MEDIATEK_A3M_JNI_NODEUTILITY_H */

