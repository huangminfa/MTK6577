/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.apst.util.command.sysinfo;

import java.util.List;

import com.mediatek.apst.util.command.ResponseCommand;

/**
 * Class Name: GetSysInfoRsp
 * <p>
 * Package: com.mediatek.apst.util.command.sysinfo
 * <p>
 * Created on: 2010-12-18
 * <p>
 * <p>
 * Description:
 * <p>
 * Response for getting system information.
 * <p>
 * 
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class GetSysInfoRsp extends ResponseCommand {
	// ==============================================================
	// Constants
	// ==============================================================
	private static final long serialVersionUID = 2L;

	// ==============================================================
	// Fields
	// ==============================================================
	// Device & firmware -------------------------------------------------------
	private String device;
	private String manufacturer;
	private String model;
	private String firmwareVersion;
	// Battery -----------------------------------------------------------------
	private int batteryLevel;
	private int batteryScale;
	// Storage -----------------------------------------------------------------
	private boolean sdMounted;
	private boolean sdWriteable;
	private String sdCardPath;
	private long sdCardTotalSpace;
	private long sdCardAvailableSpace;
	private long internalTotalSpace;
	private long internalAvailableSpace;
	// Applications & Data -----------------------------------------------------
	private int contactsCount;
	private int messagesCount;
	private int applicationsCount;
	private int simContactsCount;
	// SIM ---------------------------------------------------------------------
	private boolean gemini;
	private boolean simAccessible;
	private boolean sim1Accessible;
	private boolean sim2Accessible;
	// Added by Shaoying Han 2011-03-28
	private SimDetailInfo simInfo;
	private SimDetailInfo sim1Info;
	private SimDetailInfo sim2Info;
	private List<SimDetailInfo> simInfoList;
	
	private boolean[] SDCardAndEmmcState;
	private int[] mFeatureOptionList;

    // ==============================================================
	// Constructors
	// ==============================================================
	public GetSysInfoRsp(int requestToken) {
		super(FEATURE_MAIN, requestToken);
	}

	// ==============================================================
	// Getters
	// ==============================================================
	public int getSimContactsCount() {
        return simContactsCount;
    }
	
	public String getDevice() {
		return device;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public String getModel() {
		return model;
	}

	public int getBatteryLevel() {
		return batteryLevel;
	}

	public int getBatteryScale() {
		return batteryScale;
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public boolean isSdMounted() {
		return sdMounted;
	}

	public boolean isSdWriteable() {
		return sdWriteable;
	}

	public String getSdCardPath() {
		return sdCardPath;
	}

	public long getSdCardTotalSpace() {
		return sdCardTotalSpace;
	}

	public long getSdCardAvailableSpace() {
		return sdCardAvailableSpace;
	}

	public long getInternalTotalSpace() {
		return internalTotalSpace;
	}

	public long getInternalAvailableSpace() {
		return internalAvailableSpace;
	}

	public int getContactsCount() {
		return contactsCount;
	}

	public int getMessagesCount() {
		return messagesCount;
	}

	public int getApplicationsCount() {
		return applicationsCount;
	}

	public boolean isGemini() {
		return gemini;
	}

	public boolean isSimAccessible() {
		return simAccessible;
	}

	public boolean isSim1Accessible() {
		return sim1Accessible;
	}

	public boolean isSim2Accessible() {
		return sim2Accessible;
	}
	// Added by Shaoying Han
	public SimDetailInfo getSimInfo() {
		return simInfo;
	}

	public SimDetailInfo getSim1Info() {
		return sim1Info;
	}
	
	public SimDetailInfo getSim2Info() {
		return sim2Info;
	}
	
    public List<SimDetailInfo> getSimInfoList() {
        return simInfoList;
    }

    public boolean[] getSDCardAndEmmcState() {
        return SDCardAndEmmcState;
    }
    
    public int[] getFeatureOptionList(){
        return mFeatureOptionList;
    }
	
	// ==============================================================
	// Setters
	// ==============================================================
	public void setSimContactsCount(int simContactsCount) {
        this.simContactsCount = simContactsCount;
    }
	
	public void setDevice(String device) {
		this.device = device;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	public void setBatteryLevel(int batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	public void setBatteryScale(int batteryScale) {
		this.batteryScale = batteryScale;
	}

	public void setSdMounted(boolean sdMounted) {
		this.sdMounted = sdMounted;
	}

	public void setSdWriteable(boolean sdWriteable) {
		this.sdWriteable = sdWriteable;
	}

	public void setSdCardPath(String sdCardPath) {
		this.sdCardPath = sdCardPath;
	}

	public void setSdCardTotalSpace(long sdCardTotalSpace) {
		this.sdCardTotalSpace = sdCardTotalSpace;
	}

	public void setSdCardAvailableSpace(long sdCardAvailableSpace) {
		this.sdCardAvailableSpace = sdCardAvailableSpace;
	}

	public void setInternalTotalSpace(long internalTotalSpace) {
		this.internalTotalSpace = internalTotalSpace;
	}

	public void setInternalAvailableSpace(long internalAvailableSpace) {
		this.internalAvailableSpace = internalAvailableSpace;
	}

	public void setContactsCount(int contactsCount) {
		this.contactsCount = contactsCount;
	}

	public void setMessagesCount(int messagesCount) {
		this.messagesCount = messagesCount;
	}

	public void setApplicationsCount(int applicationsCount) {
		this.applicationsCount = applicationsCount;
	}

	public void setGemini(boolean gemini) {
		this.gemini = gemini;
	}

	public void setSimAccessible(boolean simAccessible) {
		this.simAccessible = simAccessible;
	}

	public void setSim1Accessible(boolean sim1Accessible) {
		this.sim1Accessible = sim1Accessible;
	}

	public void setSim2Accessible(boolean sim2Accessible) {
		this.sim2Accessible = sim2Accessible;
	}
	// Added by Shaoying Han
	public void setSimInfo(SimDetailInfo simInfo) {
		this.simInfo = simInfo;
	}
	
	public void setSim1Info(SimDetailInfo sim1Info) {
		this.sim1Info = sim1Info;
	}
	
	public void setSim2Info(SimDetailInfo sim2Info) {
		this.sim2Info = sim2Info;
	}

	public void setSimInfoList(List<SimDetailInfo> simInfoList) {
		this.simInfoList = simInfoList;
    }

    public void setSDCardAndEmmcState(boolean[] sDCardAndEmmcState) {
        this.SDCardAndEmmcState = sDCardAndEmmcState;
    }
    
    public void setFeatureOptionList(int[] mFeatureOptionList){
        this.mFeatureOptionList = mFeatureOptionList;
    }
	
	// ==============================================================
	// Methods
	// ==============================================================

	// ==============================================================
	// Inner & Nested classes
	// ==============================================================

}
