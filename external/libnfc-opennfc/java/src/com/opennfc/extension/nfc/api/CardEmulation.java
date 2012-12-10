/*
 * Copyright (c) 2011 Inside Secure, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opennfc.extension.nfc.api;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import android.util.Log;

public final class CardEmulation {

	/** Enable/disable debug */
	private static final boolean DEBUG = true;
	
	/** Policy for IClass */
	public static final int SECURE_ELEMENT_POLICY_ICLASS = ConstantAutogen.W_NFCC_PROTOCOL_CARD_ISO_15693_2;
	/** Policy for ISO 14443 A */
	public static final int SECURE_ELEMENT_POLICY_ISO_14443_A = ConstantAutogen.W_NFCC_PROTOCOL_CARD_ISO_14443_4_A;
	/** Policy for ISO 14443 B */
	public static final int SECURE_ELEMENT_POLICY_ISO_14443_B = ConstantAutogen.W_NFCC_PROTOCOL_CARD_ISO_14443_4_B;
	/** Policy for Mifare */
	public static final int SECURE_ELEMENT_POLICY_MIFARE = ConstantAutogen.W_NFCC_PROTOCOL_CARD_MIFARE_CLASSIC;
	/** No policy */
	public static final int SECURE_ELEMENT_POLICY_NONE = 0;
	/** Tag use in debug */
	private static final String TAG = CardEmulation.class.getSimpleName();

	public static final byte GET_SECURE_ELEMENT_POLICY_CMD = 0;
	public static final byte SET_SECURE_ELEMENT_POLICY_CMD = 1;
	
	private static OpenNFCExtManager manager = OpenNFCExtManager.getManager();
	private CardEmulation() {}
	
	public static SecureElementPolicy getSecureElementPolicy() throws CardEmulationException {
		Log.d(TAG, "getSecureElementPolicy(): starting...");
		OpenNFCExtReplyMessage sePolicyData = null;
		try {
			sePolicyData = manager.getData(OpenNFCExtManager.CARD_EMULATION, 
					GET_SECURE_ELEMENT_POLICY_CMD, null);
		} catch (InterruptedException ex) {
			throw new CardEmulationException("interrupted operation");
		}
		int status = sePolicyData.getStatus();
		Log.d(TAG, "getSecureElementPolicy(): status=" + status);
		if (status != 0) {
			if (DEBUG) {
				Log.e(TAG, "getSecureElementPolicy(): error=" + status);
			}
			throw CardEmulationException.getCardEmulationException(status);
		}

		ByteArrayInputStream bais = null;
		int secureElementPolicy = 0;
		int uiccPolicy = 0;
		try {
			bais = new ByteArrayInputStream(sePolicyData.getData());
			DataInputStream dis = new DataInputStream(bais);
			uiccPolicy = dis.readInt();
			secureElementPolicy = dis.readInt();
		} catch(IOException ex) {
			throw new CardEmulationException(ex.getMessage());			
		} finally {
			if (bais != null) {
				try {
					bais.close();
				} catch (IOException ex) {}
			}
		}
		return new SecureElementPolicy(secureElementPolicy, uiccPolicy);
	}

	public static void setSecureElementPolicy(SecureElementPolicy policy) throws CardEmulationException {
		if (policy == null) {
			throw new NullPointerException("null policy");
		}

		ByteArrayOutputStream baos = null;
		byte[] data = null;
		try {
			baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeInt(policy.getUiccPolicy());
			dos.writeInt(policy.getSecureElementPolicy());
			data = baos.toByteArray();
		} catch(IOException ex) {
			throw new CardEmulationException(ex.getMessage());			
		} finally {
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException ex) {}
			}
		}
		
		int status = 0;
		try {
			status = manager.sendData(OpenNFCExtManager.CARD_EMULATION, 
					SET_SECURE_ELEMENT_POLICY_CMD, data);
		} catch (InterruptedException ex) {
			throw new CardEmulationException("interrupted operation");
		}	

		if (status != 0) {
			if (DEBUG) {
				Log.e(TAG, "setSecureElementPolicy(): error=" + status);
			}
			throw CardEmulationException.getCardEmulationException(status);
		}
	}
	
}