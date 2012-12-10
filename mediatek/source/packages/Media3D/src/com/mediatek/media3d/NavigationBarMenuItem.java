package com.mediatek.media3d;

public class NavigationBarMenuItem {
    private int mItemId;
    private int mIconId;
    
    public NavigationBarMenuItem(int itemId) {
        mItemId = itemId;
    }
    
    public void setItemId(int itemId) {
        mItemId = itemId;
    }

    public int getItemId() {
        return mItemId;
    }

    public void setIcon(int iconId) {
        mIconId = iconId;
    }

    public int getIconId() {
        return mIconId;
    }
}
