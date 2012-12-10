/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.inputmethod.pinyin;

import android.view.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used to map the symbols on Dream's hardware keyboard to corresponding
 * Chinese full-width symbols.
 */
public class KeyMapDream {
    // Number of shift bits to store full-width symbols
    private static final Map<Integer, Integer> mHalfToFullSymTable = new HashMap<Integer, Integer>();
    	static {
		mHalfToFullSymTable.put(0x30, 0xff10); // 0
		mHalfToFullSymTable.put(0x31, 0xff11); // 1
		mHalfToFullSymTable.put(0x32, 0xff12); // 2
		mHalfToFullSymTable.put(0x33, 0xff13); // 3
		mHalfToFullSymTable.put(0x34, 0xff14); // 4
		mHalfToFullSymTable.put(0x35, 0xff15); // 5
		mHalfToFullSymTable.put(0x36, 0xff16); // 6
		mHalfToFullSymTable.put(0x37, 0xff17); // 7
		mHalfToFullSymTable.put(0x38, 0xff18); // 8
		mHalfToFullSymTable.put(0x39, 0xff19); // 9

		mHalfToFullSymTable.put(0x21, 0xff01); // !
		mHalfToFullSymTable.put(0x22, 0xff02); // "
		mHalfToFullSymTable.put(0x23, 0xff03); // #
		mHalfToFullSymTable.put(0x24, 0xffe5); // $
		mHalfToFullSymTable.put(0x25, 0xff05); // %
		mHalfToFullSymTable.put(0x26, 0xff06); // &
		mHalfToFullSymTable.put(0x27, 0xff07); // '
		mHalfToFullSymTable.put(0x28, 0xff08); // (
		mHalfToFullSymTable.put(0x29, 0xff09); // )
		mHalfToFullSymTable.put(0x2a, 0xff0a); // *
		mHalfToFullSymTable.put(0x2b, 0xff0b); // +
		mHalfToFullSymTable.put(0x2c, 0xff0c); // ,
		mHalfToFullSymTable.put(0x2d, 0xff0d); // -
		mHalfToFullSymTable.put(0x2e, 0xff0e); // .
		mHalfToFullSymTable.put(0x2f, 0xff0f); // /

		mHalfToFullSymTable.put(0x3a, 0xff1a); // :
		mHalfToFullSymTable.put(0x3b, 0xff1b); // ;
		mHalfToFullSymTable.put(0x3c, 0xff1c); // <
		mHalfToFullSymTable.put(0x3d, 0xff1d); // =
		mHalfToFullSymTable.put(0x3e, 0xff1e); // >
		mHalfToFullSymTable.put(0x3f, 0xff1f); // ?
		mHalfToFullSymTable.put(0x40, 0xff20); // @

		mHalfToFullSymTable.put(0x5b, 0xff3b); // [
		mHalfToFullSymTable.put(0x5c, 0xff3c); // \
		mHalfToFullSymTable.put(0x5d, 0xff3d); // ]
		mHalfToFullSymTable.put(0x5e, 0xff3e); // ^
		mHalfToFullSymTable.put(0x5f, 0xff3f); // _
		
		mHalfToFullSymTable.put(0x7b, 0xff5b); // {
		mHalfToFullSymTable.put(0x7d, 0xff5d); // }
		mHalfToFullSymTable.put(0x7e, 0xff5e); // ~
	}

    static public char getChineseLabel(int keyCode) {
        if (keyCode <= 0) return 0;
        int result = keyCode;
        if (mHalfToFullSymTable.containsKey(keyCode)) {
					result = mHalfToFullSymTable.get(keyCode);
			}
			return (char)result;
    }
}
