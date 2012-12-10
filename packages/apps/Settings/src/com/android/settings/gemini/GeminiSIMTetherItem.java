package com.android.settings.gemini;

/**
 * Assisted class for transfer contact data
 * @author MTK80906
 */
public class GeminiSIMTetherItem {
	private String name;
	private String phoneNumType;
	private String simName;
	private int simColor;
	private int checkedStatus;
	private String phoneNum;
	private String simId;

    private int contactId;
	
    public int getContactId() {
        return contactId;
    }
    
    public void setContactId(int contactId) {
        this.contactId = contactId;
    }
    
	public GeminiSIMTetherItem(){
		name = "";
		phoneNum = "";
		simColor = 0;
		checkedStatus = -1;
	}
	
	public GeminiSIMTetherItem(String name, String phoneNum, int simColor, int checkedStatus){
		this.name = name;
		this.phoneNum = phoneNum;
		this.simColor = simColor;
		this.checkedStatus = checkedStatus;
	}
	public String getSimId() {
		return simId;
	}

	public void setSimId(String simId) {
		this.simId = simId;
	}
	public String getPhoneNumType() {
		return phoneNumType;
	}
	
	public void setPhoneNumType(String phoneNumType) {
		this.phoneNumType = phoneNumType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public int getSimColor() {
		return simColor;
	}

	public void setSimColor(int simColor) {
		this.simColor = simColor;
	}

	public int getCheckedStatus() {
		return checkedStatus;
	}

	public void setCheckedStatus(int checkedStatus) {
		this.checkedStatus = checkedStatus;
	}
	
	public String getSimName() {
		return simName;
	}

	public void setSimName(String simName) {
		this.simName = simName;
	}

}
