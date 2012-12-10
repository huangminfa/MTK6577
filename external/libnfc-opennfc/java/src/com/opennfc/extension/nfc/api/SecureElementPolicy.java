/*
 * Copyright (c) 2011 Inside Secure, All Rights Reserved. Licensed under 
	the Apache License, Version 2.0 (the "License"); you may not use this file 
	except in compliance with the License. You may obtain a copy of the License 
	at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable 
	law or agreed to in writing, software distributed under the License is distributed 
	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
	express or implied. See the License for the specific language governing permissions 
	and limitations under the License.
 */
package com.opennfc.extension.nfc.api;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Describe policy for SE and UICC
 */
public class SecureElementPolicy implements Parcelable {
	/** Creator used for create instances in Parcelable system */
	public static final Creator<SecureElementPolicy> CREATOR = new Creator<SecureElementPolicy>() {
		/**
		 * Create SecureElementPolicy from parcel
		 * 
		 * <br>
		 * <br>
		 * <u><b>Documentation from parent :</b></u><br>
		 * {@inheritDoc}
		 * 
		 * @param source
		 *            Parcel to parse
		 * @return Created SecureElementPolicy
		 * @see android.os.Parcelable.Creator#createFromParcel(android.os.Parcel)
		 */
		@Override
		public SecureElementPolicy createFromParcel(final Parcel source) {
			return new SecureElementPolicy(source);
		}

		/**
		 * Create a new empty array of SecureElementPolicy
		 * 
		 * <br>
		 * <br>
		 * <u><b>Documentation from parent :</b></u><br>
		 * {@inheritDoc}
		 * 
		 * @param size
		 *            Array size
		 * @return Array created
		 * @see android.os.Parcelable.Creator#newArray(int)
		 */
		@Override
		public SecureElementPolicy[] newArray(final int size) {
			return new SecureElementPolicy[size];
		}
	};
	/** SE policy */
	private int secureElementPolicy;
	/** UICC policy */
	private int uiccPolicy;

	/**
	 * Create SecureElementPolicy
	 * 
	 * @param secureElementPolicy
	 *            SE policy
	 * @param uiccPolicy
	 *            UICC policy
	 */
	public SecureElementPolicy(final int secureElementPolicy, final int uiccPolicy) {
		this.secureElementPolicy = secureElementPolicy;
		this.uiccPolicy = uiccPolicy;
	}

	/**
	 * Create SecureElementPolicy from parcel
	 * 
	 * @param source
	 *            Parcel to parse
	 */
	public SecureElementPolicy(final Parcel source) {
		this.secureElementPolicy = source.readInt();
		this.uiccPolicy = source.readInt();
	}

	/**
	 * Content description
	 * 
	 * <br>
	 * <br>
	 * <u><b>Documentation from parent :</b></u><br>
	 * {@inheritDoc}
	 * 
	 * @return 0
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Sring representation
	 * 
	 * <br>
	 * <br>
	 * <u><b>Documentation from parent :</b></u><br>
	 * {@inheritDoc}
	 * 
	 * @return Sring representation
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SecureElementPolicy [secureElementPolicy=");
		builder.append(this.secureElementPolicy);
		builder.append(", uiccPolicy=");
		builder.append(this.uiccPolicy);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Serialize the SecureElementPolicy in parcel
	 * 
	 * <br>
	 * <br>
	 * <u><b>Documentation from parent :</b></u><br>
	 * {@inheritDoc}
	 * 
	 * @param destination
	 *            Parcel where write
	 * @param flags
	 *            Flag used
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(final Parcel destination, final int flags) {
		destination.writeInt(this.secureElementPolicy);
		destination.writeInt(this.uiccPolicy);
	}
	
	public int getSecureElementPolicy() {
		return secureElementPolicy;
		
	}

	public int getUiccPolicy() {
		return uiccPolicy;
		
	}

	
}