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
 * Ray-casting result class
 */
package com.mediatek.ngin3d.a3m;

import com.mediatek.a3m.Vector3;

/**
 * Stores the result of a raycast intersection test.
 */
public class RaycastResult {
    /**
     * Distance along ray that the intersection occurred.
     * A distance of less than zero indicates that no intersection occurred.
     */
    public float distance = -1.0f;

    /**
     * Normal of the surface that was intersected.
     */
    public Vector3 normal = new Vector3();

    /**
     * Layer in which the node resides.
     * Layers are rendered in increasing numerical order, so a node in higher
     * layer will be picked over one in a lower layer, even if it is further
     * from the start of the raycast.
     */
    public int layer; // Layer 0 by default

    /**
     * Presentation node object that was intersected.
     * A null node indicates that no intersection occurred.
     */
    public SceneNodePresentation node;
}

