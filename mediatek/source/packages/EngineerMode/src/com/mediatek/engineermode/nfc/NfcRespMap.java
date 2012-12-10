package com.mediatek.engineermode.nfc;

import java.util.HashMap;

public class NfcRespMap {

	public static final String KEY_SETTINGS = "nfc.settings";
	public static final String KEY_SS_REGISTER_NOTIF =
		"nfc.software_stack.reg_notif";
	public static final String KEY_SS_SCAN_COMPLETE =
		"nfc.software_stack.scan_complete";
	public static final String KEY_SS_TAG_DECT =
		"nfc.software_stack.normaltag_dect";
	public static final String KEY_SS_P2P_TARGET_DECT =
		"nfc.software_stack.p2p_dect";

	private final HashMap<String, Object> container = new HashMap<String, Object>();
	private static volatile NfcRespMap mRespMap = new NfcRespMap();

	public static NfcRespMap getInst() {
		if (mRespMap == null) {
			synchronized (NfcRespMap.class) {
				mRespMap = new NfcRespMap();
			}
		}
		return mRespMap;
	}

	public void clear() {
		container.clear();
	}

	public void put(String key, Object resp) {
		container.put(key, resp);
	}

	public Object take(String key) {
		return container.get(key);
	}

}
