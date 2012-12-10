/*
 * Copyright (C) 2006-2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;


public class BearerDesc implements Parcelable  {
	public int bearerType = 0;

	public int precedence = 0;
	public int delay = 0;
	public int reliability = 0;
	public int peak = 0;
	public int mean = 0;
	public int pdpType = 0;

	public int dataCompression = 0;
	public int headerCompression = 0;

	public int dataRate = 0;
	public int bearerService = 0;
	public int connectionElement = 0;

	BearerDesc() {}  

	private BearerDesc(Parcel in) {
		bearerType         = in.readInt();
		precedence         = in.readInt();
		delay              = in.readInt();
		reliability        = in.readInt();
		peak               = in.readInt();
		mean               = in.readInt();
		pdpType            = in.readInt();
		dataCompression    = in.readInt();
		headerCompression  = in.readInt();
		dataRate           = in.readInt();
		bearerService      = in.readInt();
		connectionElement  = in.readInt();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(bearerType);
		dest.writeInt(precedence);
		dest.writeInt(delay);
		dest.writeInt(reliability);
		dest.writeInt(peak);
		dest.writeInt(mean);
		dest.writeInt(pdpType);
		dest.writeInt(dataCompression);
		dest.writeInt(headerCompression);
		dest.writeInt(dataRate);
		dest.writeInt(bearerService);
		dest.writeInt(connectionElement);
	}

	public static final Parcelable.Creator<BearerDesc> CREATOR = new Parcelable.Creator<BearerDesc>() {
		public BearerDesc createFromParcel(Parcel in) {
			return new BearerDesc(in);
		}

		public BearerDesc[] newArray(int size) {
			return new BearerDesc[size];
		}
	};
}

