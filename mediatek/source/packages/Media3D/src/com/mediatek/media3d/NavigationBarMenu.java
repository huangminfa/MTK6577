package com.mediatek.media3d;

import java.util.ArrayList;
import java.util.List;

public class NavigationBarMenu {
    public List<NavigationBarMenuItem> mMenuItems;

    public NavigationBarMenu() {
        // Create an empty list.
        mMenuItems = new ArrayList<NavigationBarMenuItem>();
    }

    public NavigationBarMenuItem add(int itemId) {
        NavigationBarMenuItem item = new NavigationBarMenuItem(itemId);
        mMenuItems.add(item);
        return item;
    }
    
    public void clear() {
        mMenuItems.clear();
    }

    public int getItemCount() {
        return mMenuItems.size();
    }

    public NavigationBarMenuItem getItem(int index) {
        return mMenuItems.get(index);
    }
}
