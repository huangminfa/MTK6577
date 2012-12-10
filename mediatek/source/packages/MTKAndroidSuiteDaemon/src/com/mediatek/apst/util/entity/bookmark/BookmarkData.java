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
 * Class Name: BookmarkData
 * <p>Package: com.mediatek.apst.util.entity.bookmark
 * <p>
 * <p>Description: 
 * <p>Bookmark entity,include the bookmark's basic information.
 * <p>
 * @author mtk54043 Yu.Chen
 * @version V1.0
 */
public class BookmarkData implements Serializable{

	private static final long serialVersionUID = 1L;
	// data
	private int id; // ID
	private String title; // Name,Title
	private String url; // URL
	private int visits; // Visit times
	private long date; // unknown
	private long created; // Created time
	private long modified; // Modified time
	private long access; // Access time
	private String description; // Description
	private int bookmark; // true if bookmark, false if history
	private byte[] favicon; // Icon file
	private int folderid; // Folder id

	// constructor
	public BookmarkData() {
	}

	public BookmarkData(int id, String title, String url, int visits,
			long date, long created, long modefied, long access,
			String description, int bookmark, byte[] favicon, int folderid) {
		this.id = id;
		this.title = title;
		this.url = url;
		this.visits = visits;
		this.date = date;
		this.created = created;
		this.modified = modefied;
		this.access = access;
		this.description = description;
		this.bookmark = bookmark;
		this.favicon = favicon;
		this.folderid = folderid;
	}

	// setters

	public void setId(int id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setVisits(int visits) {
		this.visits = visits;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public void setModified(long modified) {
		this.modified = modified;
	}

	public void setAccess(long access) {
		this.access = access;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setBookmark(int bookmark) {
		this.bookmark = bookmark;
	}

	public void setFavicon(byte[] favicon) {
		this.favicon = favicon;
	}

	public void setFolderId(int folderid) {
		this.folderid = folderid;
	}

	// getters
	public int getId() {
		return this.id;
	}

	public String getTitle() {
		return this.title;
	}

	public String getUrl() {
		return this.url;
	}

	public int getVisits() {
		return this.visits;
	}

	public long getDate() {
		return this.date;
	}

	public long getCreated() {
		return this.created;
	}

	public long getModified() {
		return this.modified;
	}

	public long getAccess() {
		return this.access;
	}

	public String getDescription() {
		return this.description;
	}

	public int getBookmark() {
		return this.bookmark;
	}

	public byte[] getFavIcon() {
		return this.favicon;
	}

	public int getFolderId() {
		return this.folderid;
	}
//
//	public void writeRaw(ByteBuffer buffer) throws NullPointerException,
//			BufferUnderflowException {
//
//		buffer.putInt(this.id);
//		RawTransUtil.putString(buffer, this.title);
//		RawTransUtil.putString(buffer, this.url);
//		buffer.putInt(this.visits);
//		buffer.putLong(this.date);
//		buffer.putLong(this.created);
//		RawTransUtil.putString(buffer, this.description);
//		buffer.putInt(this.bookmark);
//		RawTransUtil.putBytes(buffer, this.favicon);
//		buffer.putInt(folderid);
//	}
//
//	public void readRaw(ByteBuffer buffer) throws NullPointerException,
//			BufferOverflowException {
//
//		this.id = buffer.getInt();
//		this.title = RawTransUtil.getString(buffer);
//		this.url = RawTransUtil.getString(buffer);
//		this.visits = buffer.getInt();
//		this.date = buffer.getLong();
//		this.created = buffer.getLong();
//		this.description = RawTransUtil.getString(buffer);
//		this.bookmark = buffer.getInt();
//		this.favicon = RawTransUtil.getBytes(buffer);
//		this.folderid = buffer.getInt();
//	}
}
