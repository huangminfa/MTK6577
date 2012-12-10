package com.mediatek.engineermode.nfc;

public class NfcUtils {

	public static String printArray(Object array) {
		String res = "";
		if (array instanceof byte[]) {
			for (int i = 0; i < ((byte[]) array).length; i++) {
				if (i != 0 && i % 4 == 0) {
					res += "\n";
				}
				res += String.format("0x%02X ", ((byte[]) array)[i]);
			}
		} else if (array instanceof short[]) {
			for (int i = 0; i < ((short[]) array).length; i++) {
				if (i != 0 && i % 4 == 0) {
					res += "\n";
				}
				res += String.format("0x%04X ", ((short[]) array)[i]);
			}
		} else {
			res = "UNSUPPORTED TYPE.";
		}
		return res;
	}
}
