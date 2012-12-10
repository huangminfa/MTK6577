package com.android.nfc.extension.nfc.utility;

/**
 * @hide
 */
public class Operator {

	public static boolean equals(byte[]cmp1, byte[] cmp2){
		
		if(cmp1.length != cmp2.length)
			return false;
		for(int i = 0; i < cmp1.length; i++){
			if(cmp1[i] != cmp2[i])
				return false;
		}
		return true;
	}
}
