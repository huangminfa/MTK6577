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

package com.mediatek.apst.util.command.bookmark;

import java.util.ArrayList;

import com.mediatek.apst.util.command.RawBlockResponse;
import com.mediatek.apst.util.entity.bookmark.BookmarkData;
import com.mediatek.apst.util.entity.bookmark.BookmarkFolder;

/**
 * a response transfer list of bookmark
 * 
 * @author mtk54043 Yu Chen
 * @version 1.0.0
 * @since JDK1.6
 */
public class AsyncGetAllBookmarkInfoRsp extends RawBlockResponse {
	// ==============================================================
	// Constants
	// ==============================================================
	private static final long serialVersionUID = 2L;

	private ArrayList<BookmarkData> mBookmarkDataList;

	private ArrayList<BookmarkFolder> mBookmarkFolderList;

	// ==============================================================
	// Fields
	// ==============================================================

	// ==============================================================
	// Constructors
	// ==============================================================
	public AsyncGetAllBookmarkInfoRsp(int token) {
		super(FEATURE_BOOKMARK, token);
		mBookmarkDataList = new ArrayList<BookmarkData>();
	}

	// ==============================================================
	// Getters
	// ==============================================================
	public ArrayList<BookmarkFolder> getmBookmarkFolderList() {
		return mBookmarkFolderList;
	}

	public ArrayList<BookmarkData> getmBookmarkDataList() {
		return mBookmarkDataList;
	}

	// ==============================================================
	// Setters
	// ==============================================================

	public void setmBookmarkFolderList(
			ArrayList<BookmarkFolder> mBookmarkFolderList) {
		this.mBookmarkFolderList = mBookmarkFolderList;
	}

	public void setmBookmarkDataList(ArrayList<BookmarkData> mBookmarkDataList) {
		this.mBookmarkDataList = mBookmarkDataList;
	}

	// ==============================================================
	// Methods
	// ==============================================================
	/**
	 * Gets the BookmarkData info list.
	 * 
	 * @return The BookmarkData info list.
	 */
	// public ArrayList<BookmarkData> getResults() {
	// ByteBuffer buffer = ByteBuffer.wrap(this.getRaw());
	// int count = buffer.getInt();
	// ArrayList<BookmarkData> results = new ArrayList<BookmarkData>(count);
	// for (int i = 0; i < count; i++) {
	// BookmarkData item = new BookmarkData();
	// item.readRaw(buffer);
	// results.add(item);
	// }
	// return results;
	// }
	// ==============================================================
	// Inner & Nested classes
	// ==============================================================

}
