package com.android.phone;

public class CellBroadcastLanguage {

    private int mLanguageId;
    private String mLanguageName;
    private boolean mLanguageState;

    public CellBroadcastLanguage() {
    }

    public CellBroadcastLanguage(int id, String name, boolean state) {
        mLanguageId = id;
        mLanguageName = name;
        mLanguageState = state;
    }

    public int getLanguageId() {
        return mLanguageId;
    }

    public void setLanguageId(int id) {
        mLanguageId = id;
    }

    public String getLanguageName() {
        return mLanguageName;
    }

    public void setLanguageName(String name) {
        mLanguageName = name;
    }

    public boolean getLanguageState() {
        return mLanguageState;
    }

    public void setLanguageState(boolean state) {
        mLanguageState = state;
    }
}
