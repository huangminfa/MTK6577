/*
 * Copyright (c) 2010 Inside Secure, All Rights Reserved.
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

/**
 * This CardEmulationException is thrown when an error is detected in the CardEmulation mode.
 *
 */
public final class CardEmulationException extends Exception {

	private static String[] exceptionMsgs = 
			new String[] 
		{
			// OPEN_NFC_EXTERNAL_API_SUCCES = 0x00
			"Unknown error code",
			// OPEN_NFC_EXTERNAL_API_FAILED = 0x01,
			"External API failed",
			// OPEN_NFC_EXTERNAL_API_CARD_EMULATION_NOT_AVAILABLE = 0x02,
			"External API card emulation is not available",
		};
		
	
    /**
     * Creates a new CardEmulationException.
     *
     * @param  message  the exception message.
     **/
    public CardEmulationException(final String message) {
        super(message);
    }

    /**
     * Creates a new CardEmulationException.
     *
     * @param  message  the exception message.
     **/
    public static CardEmulationException getCardEmulationException(int error) {
    	int index = (error >= exceptionMsgs.length) ? 0 : error;
		return new CardEmulationException(exceptionMsgs[index]);
    }

    /**
     * Returns the string value of the exception.
     *
     * @return the string representing the exception.
     **/
    @Override
    public String toString() {
      return "CardEmulationException : " + getMessage();
    }
}