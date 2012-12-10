package com.android.phone;

public class CellBroadcastChannel {
    private int mKeyId;
    private int mChannelId;
    private String mChannelName;
    private boolean mChannelState;

    public CellBroadcastChannel() {
    }

    public CellBroadcastChannel(int keyId, int numberId, String name,
            boolean state) {
        mKeyId = keyId;
        mChannelId = numberId;
        mChannelName = name;
        mChannelState = state;
    }

    public CellBroadcastChannel(int numberId, String name, boolean state) {
        mChannelId = numberId;
        mChannelName = name;
        mChannelState = state;
    }

    public int getKeyId() {
        return mKeyId;
    }

    public void setKeyId(int id) {
        mKeyId = id;
    }

    public int getChannelId() {
        return mChannelId;
    }

    public void setChannelId(int id) {
        mChannelId = id;
    }

    public String getChannelName() {
        return mChannelName;
    }

    public void setChannelName(String name) {
        mChannelName = name;
    }

    public boolean getChannelState() {
        return mChannelState;
    }

    public void setChannelState(boolean state) {
        mChannelState = state;
    }
}
