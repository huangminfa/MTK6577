/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.music;

import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.AlphabetIndexer;

/**
 * Handles comparisons in a different way because the Album, Song and Artist name
 * are stripped of some prefixes such as "a", "an", "the" and some symbols.
 *
 */
class MusicAlphabetIndexer extends AlphabetIndexer {
    
    private static final String TAG = "MusicAlphabetIndexer";
    
    public MusicAlphabetIndexer(Cursor cursor, int sortedColumnIndex, CharSequence alphabet) {
        super(cursor, sortedColumnIndex, alphabet);
    }
    
    @Override
    protected int compare(String word, String letter) {
        String wordKey = MediaStore.Audio.keyFor(word);
        String letterKey = MediaStore.Audio.keyFor(letter);
        //MusicLogUtils.i(TAG, "compare:word: " + word);
        //MusicLogUtils.i(TAG, "letter: " + letter);
        if (wordKey.startsWith(letter)) {
            MusicLogUtils.i(TAG, "startsWith return 0 ");
            return 0;
        } else {
            //MusicLogUtils.i(TAG, "Return: " + wordKey.compareTo(letterKey));
            return wordKey.compareTo(letterKey);
        }
    }
    @Override
    public int getPositionForSection(int sectionIndex) {
        int alphabetLength = mAlphabet.length();
        if (sectionIndex >= alphabetLength) {
            return mDataCursor.getCount();
        } else {
            return super.getPositionForSection(sectionIndex);
        }

    }
    
    @Override
    public int getSectionForPosition(int position) {
        int savedCursorPos = mDataCursor.getPosition();
        mDataCursor.moveToPosition(position);
        String curName = mDataCursor.getString(mColumnIndex);
        int alphabetLength = mAlphabet.length();
        int lastCompareResult = 0;
        int compareResult;
        char letter;
        String targetLetter;
        
        mDataCursor.moveToPosition(savedCursorPos);
        // Linear search, as there are only a few items in the section index
        // Could speed this up later if it actually gets used.
        //MusicLogUtils.e(TAG, "getSectionForPosition2 : position = " + position);
        for (int i = 0; i < alphabetLength; i++) {
            letter = mAlphabet.charAt(i);
            targetLetter = Character.toString(letter);
            compareResult = compare(curName, targetLetter);
            
            if (compareResult == 0 || ((compareResult > 0) && (lastCompareResult < 0))) {
                //MusicLogUtils.e(TAG, "getSectionForPosition2 : return = " + i);
                return i;
            }
            
            if ((compareResult < 0) && (lastCompareResult > 0)) {
                return i - 1;
            }
            
            lastCompareResult = compareResult;
            
        }
        //MusicLogUtils.e(TAG, "getSectionForPosition2 : return last one = alphabetLength:" + alphabetLength);
        return alphabetLength - 1; // Don't recognize the letter - falls under zero'th section
    }
    
}
