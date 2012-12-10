/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mediatek.apst.util.entity.bookmark;

import java.io.Serializable;

/**
 * Class Name: BookmarkFolder
 * <p>Package: com.mediatek.apst.util.entity.bookmark
 * <p>
 * <p>Description: 
 * <p>BookmarkFolder entity,include the bookmarkFolder's basic information.
 * <p>
 * @author mtk54043 Yu.Chen
 * @version V1.0
 */
public class BookmarkFolder implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private int parentid;
	private int folderlevel;
	private String name;
	private long date;
	private int visits;

	// constructor
	public BookmarkFolder() {
	}

	public BookmarkFolder(int id, int parentid, int folderlevel, String name,
			long date, int visits) {
		this.id = id;
		this.parentid = parentid;
		this.folderlevel = folderlevel;
		this.name = name;
		this.date = date;
		this.visits = visits;
	}

	// setters
	public void setId(int id) {
		this.id = id;
	}

	public void setParentId(int parentid) {
		this.parentid = parentid;
	}

	public void setFolderLevel(int folderlevel) {
		this.folderlevel = folderlevel;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public void setVisits(int visits) {
		this.visits = visits;
	}

	// getters
	public int getId() {
		return this.id;
	}

	public int getParentId() {
		return this.parentid;
	}

	public int getFolderLevel() {
		return this.folderlevel;
	}

	public String getName() {
		return this.name;
	}

	public long getDate() {
		return this.date;
	}

	public int getVisits() {
		return this.visits;
	}

	// public void writeRaw(ByteBuffer buffer) throws NullPointerException,
	// BufferUnderflowException {
	//
	// buffer.putInt(this.id);
	// buffer.putInt(this.parentid);
	// buffer.putInt(this.folderlevel);
	// RawTransUtil.putString(buffer, name);
	// buffer.putLong(date);
	// buffer.putInt(visits);
	// }
	//
	// public void readRaw(ByteBuffer buffer) throws NullPointerException,
	// BufferOverflowException {
	// this.id = buffer.getInt();
	// this.parentid = buffer.getInt();
	// this.folderlevel = buffer.getInt();
	// this.name = RawTransUtil.getString(buffer);
	// this.date = buffer.getLong();
	// this.visits = buffer.getInt();
	// }
}
