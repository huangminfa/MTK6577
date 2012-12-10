/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony.cat;

import java.util.List;

/**
 * Class for representing BER-TLV objects.
 *
 * @see "ETSI TS 102 223 Annex C" for more information.
 *
 * {@hide}
 */
class BerTlv {
	private int mTag = BER_UNKNOWN_TAG;
	private List<ComprehensionTlv> mCompTlvs = null;

	public static final int BER_UNKNOWN_TAG             = 0x00;
	public static final int BER_PROACTIVE_COMMAND_TAG   = 0xd0;
	public static final int BER_MENU_SELECTION_TAG      = 0xd3;
	public static final int BER_EVENT_DOWNLOAD_TAG      = 0xd6;

	private BerTlv(int tag, List<ComprehensionTlv> ctlvs) {
		mTag = tag;
		mCompTlvs = ctlvs;
	}

	/**
	 * Gets a list of ComprehensionTlv objects contained in this BER-TLV object.
	 *
	 * @return A list of COMPREHENSION-TLV object
	 */
	public List<ComprehensionTlv> getComprehensionTlvs() {
		return mCompTlvs;
	}

	/**
	 * Gets a tag id of the BER-TLV object.
	 *
	 * @return A tag integer.
	 */
	public int getTag() {
		return mTag;
	}

	/**
	 * Decodes a BER-TLV object from a byte array.
	 *
	 * @param data A byte array to decode from
	 * @return A BER-TLV object decoded
	 * @throws ResultException
	 */
	public static BerTlv decode(byte[] data) throws ResultException {
		int curIndex = 0;
		int endIndex = data.length;
		int tag, length = 0;
		
		//Add by Huibin Mao MTK80229
		//ICS Migration start
		boolean isSetUpMenu = false;
		boolean hasMenuItems = false;
		//ICS Migration end
		
		try {
			/* tag */
			tag = data[curIndex++] & 0xff;
			if (tag == BER_PROACTIVE_COMMAND_TAG) {
				/* length */
				int temp = data[curIndex++] & 0xff;
				if (temp < 0x80) {
					length = temp;
				} else if (temp == 0x81) {
					temp = data[curIndex++] & 0xff;
					if (temp < 0x80) {
						throw new ResultException(
								ResultCode.CMD_DATA_NOT_UNDERSTOOD);
					}
					length = temp;
				} else {
					throw new ResultException(
							ResultCode.CMD_DATA_NOT_UNDERSTOOD);
				}
			} else {
				if (ComprehensionTlvTag.COMMAND_DETAILS.value() == (tag & ~0x80)) {
					tag = BER_UNKNOWN_TAG;
					curIndex = 0;
				}
			}
		} catch (IndexOutOfBoundsException e) {
			throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
		} catch (ResultException e) {
			throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
		}

		/* COMPREHENSION-TLVs */
		if (endIndex - curIndex < length) {
			throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
		}

		List<ComprehensionTlv> ctlvs = ComprehensionTlv.decodeMany(data,curIndex);
		
		//Add by Huibin Mao mtk80229
		//ICS Migration start
		for(int i = 0; i < ctlvs.size();) 
		{
			ComprehensionTlv tlv = ctlvs.get(i);
			if(tlv != null && tlv.getTag() == ComprehensionTlvTag.COMMAND_DETAILS.value()) 
			{
				CatLog.d("CAT", "BerTlv-decode: find command details");
				byte[] rawValues = tlv.getRawValue();
				int valueIndex = tlv.getValueIndex();
				if((rawValues[valueIndex + 1] & 0xff) == AppInterface.CommandType.SET_UP_MENU.value()) 
				{
					CatLog.d("CAT", "BerTlv-decode: command type is SET_UP_MENU");
					isSetUpMenu = true;
				}

				i += 1;
			} 
			else if(tlv != null && tlv.getTag() == ComprehensionTlvTag.ITEM.value() && hasMenuItems == false) 
			{
				CatLog.d("CAT", "BerTlv-decode: find one menu item");
				hasMenuItems = true;
				i += 1;
			} 
			else if(tlv == null) 
			{
				CatLog.d("CAT", "BerTlv-decode: remove one null object");
				ctlvs.remove(i);
			} 
			else 
			{
				CatLog.d("CAT", "BerTlv-decode: other tlv object");
				i += 1;
			}
		}
		
		if(isSetUpMenu == true && hasMenuItems == true) 
		{
			CatLog.d("CAT", "BerTlv-decode: SET_UP_MENU is valid");
		} 
		else if(isSetUpMenu == true && hasMenuItems == false) 
		{
			CatLog.d("CAT", "BerTlv-decode: command is invalid, no menu item, throw exception");
			throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
		} 
		else if(isSetUpMenu == false) 
		{
			CatLog.d("CAT", "BerTlv-decode: other proactive command");
		}
		//ICS Migration end
		
		return new BerTlv(tag, ctlvs);
	}
}
