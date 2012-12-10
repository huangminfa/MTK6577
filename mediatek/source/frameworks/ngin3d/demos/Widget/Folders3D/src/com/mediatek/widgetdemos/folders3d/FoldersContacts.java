package com.mediatek.widgetdemos.folders3d;

import java.util.Comparator;

/**
 * This class defines linked list element for Folders3d contacts.
 * 
 */
public class FoldersContacts implements Comparator<FoldersContacts> {

    public String contactName;
    public String contactPhotoFileName;
    public String contactNameTexture;

    /**
     * Default Constructor. Does not initialize parameters
     */
    public FoldersContacts() {
    }

    /**
     * Constructor which sets parameter values
     * 
     * @param sContactName
     * @param sContactPhotoFileName
     * @param sContactNameTexture
     */
    public FoldersContacts(String sContactName, String sContactPhotoFileName,
            String sContactNameTexture) {
        contactName = sContactName;
        contactPhotoFileName = sContactPhotoFileName;
        contactNameTexture = sContactNameTexture;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(FoldersContacts object1, FoldersContacts object2) {
        return object1.contactName.compareTo(object2.contactName);
    }

}
