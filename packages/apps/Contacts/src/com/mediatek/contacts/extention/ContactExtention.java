package com.mediatek.contacts.extention;

import java.util.HashMap;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.android.contacts.list.ContactListItemView;
import com.android.contacts.list.ContactTileAdapter.ContactEntry;
import com.android.contacts.model.DataKind;

public class ContactExtention {
    
    public int getLayoutResID() {
        return -1;
    }

    public DataKind getExtentionKind(DataKind kind, String mimeType, Cursor cursor) {
        // TODO Auto-generated method stub
        return kind;
    }

    public Intent getExtentionIntent(int im, int ft) {
        // TODO Auto-generated method stub
        return null;
    }

    public void measureExtentionIcon(ImageView mExtentionIcon) {
        // TODO Auto-generated method stub
        
    }

    public int layoutExtentionIcon(int leftBound, int topBound, int bottomBound, int rightBound,
            int mGapBetweenImageAndText, ImageView mExtentionIcon) {
        // TODO Auto-generated method stub
        return rightBound;
    }

    public void bindExtentionIcon(ContactListItemView view, int partition, Cursor cursor) {
        // TODO Auto-generated method stub
        
    }

    public Drawable setExtentionIcon(ContactExtention mContactExtention, ContactEntry entry) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getExtentionMimeType() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setExtentionButton(View resultView, CharSequence charSequence, String string, Activity activity) {
        // TODO Auto-generated method stub
        
    }

    public String setExtentionSubTitle(String data, String mimeType, HashMap<String, String> mPhoneAndSubtitle) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getExtentionTitles(String mimeType, String kind) {
        // TODO Auto-generated method stub
        return kind;
    }

    public void setDetailKindView(View view) {
        // TODO Auto-generated method stub
        
    }

    public void setScondBuotton(String mimetype, String data, String displayName, Activity activity) {
        // TODO Auto-generated method stub
        
    }

    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    public void onContactDetialOpen(Uri contactLookupUri) {
        // TODO Auto-generated method stub
        
    }


}
