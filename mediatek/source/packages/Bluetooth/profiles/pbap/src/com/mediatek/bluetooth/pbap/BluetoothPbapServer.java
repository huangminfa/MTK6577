/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.bluetooth.pbap;

import com.mediatek.bluetooth.R;

import android.telephony.TelephonyManager;
import android.bluetooth.BluetoothAdapter;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.RawContacts;
//import android.pim.vcard.VCardConfig;
import android.content.Context;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.android.vcard.VCardConfig;
import com.android.bluetooth.pbap.BluetoothPbapVCardListing;
import com.android.bluetooth.pbap.BluetoothVCardEntryHandler;
import com.android.bluetooth.pbap.BluetoothVCardComposer;
//import com.android.bluetooth.pbap.VCardConfig;
import com.android.bluetooth.pbap.BluetoothPbapSimAdn;

import javax.obex.ResponseCodes;

public class BluetoothPbapServer {

    private static final String TAG = "BluetoothPbapServer";

    /* Flag for debug messages */
    private static final boolean DEBUG = true;

    /* Constant definition */
    /* RFCOMM connection request received */
    public static final int PBAP_AUTHORIZE_IND = 101;
    /* Client chanllenge request received */
    public static final int PBAP_AUTH_CHALL_IND = 102;
    /* Client connection request received, no connected ind will received, so considered as established */
    public static final int PBAP_SESSION_ESTABLISHED = 103;
    /* Disconnect indication received */
    public static final int PBAP_SESSION_DISCONNECTED = 104;

    /* Confirm codes for JNI interface */
    private static final int PBAP_CNF_SUCCESS = 0;
    private static final int PBAP_CNF_FAILED = 1;

    /* Keep the application context */
    private Context mContext;

    /* Handler to send msg to BluetoothPbapService */
    private Handler mServiceHandler;

    /* Server state */
    private int mServerState;

    /* Native data */
    private int mNativeData;

    /* Message listener */
    private PbapSocketListener mListener;

    /* Path object */
    private BluetoothPbapPath mPath;

    /* Vcard listing object */
    private BluetoothPbapVCardListing mVcardListing;

    BluetoothPbapSimAdn mSimAdn = null;


    /* local name and number */
    private String mLocalName;
    private String mLocalNumber;

    /* Store result path */
    private String mResultPath = null;

    /* Initialize class */
    static {
	System.loadLibrary("extpbap_jni");
        classInitNative();
    }

    BluetoothPbapServer(Handler handler, final Context context)
    {
        mContext = context;
        mServiceHandler = handler;
        mListener = null;
        mPath = null;
        mLocalName = null;
        mLocalNumber = null;
        mVcardListing = null;
        mSimAdn = null;
        initializeNativeDataNative();
    }

    protected void finalize() throws Throwable {
        try {
            cleanupNativeDataNative();
        } finally {
            super.finalize();
        }
    }

    private class PbapSocketListener extends Thread {
        public boolean mStopListen;
        @Override
        public void run() {
            printLog("Start listening");
            while (!mStopListen) {
                if (!listenIndicationNative(false)) {
                    errorLog("listen failed");
        	        mStopListen = true;
        	    }
            }
            printLog("SocketListener stopped.");
        }
    }

    private final boolean getLocalNameAndNum() {
        TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            mLocalNumber = tm.getLine1Number();
            mLocalName = tm.getLine1AlphaTag();
        }else {
            mLocalNumber = new String();
            mLocalName = new String();
        }
        printLog("getLocalNameAndNum : name="+mLocalName+" num="+mLocalNumber);
        return (TextUtils.isEmpty(mLocalNumber) == false || TextUtils.isEmpty(mLocalName) == false);
    }

    private final int getPbSize(int type) {
        int size = 0;
        ContentResolver resolver = null;
        Cursor c = null;
        Uri pbUri = null;
        String sel = null;
        printLog("[API] getPbSize("+type+")");

        resolver = mContext.getContentResolver();
        if(resolver == null){
            return 0;
        }else {
            switch(type) {
            case BluetoothPbapPath.FOLDER_TYPE_PB:
                pbUri = Contacts.CONTENT_URI;
                sel = Contacts.INDICATE_PHONE_SIM + "=" + (-1);
                break;
            case BluetoothPbapPath.FOLDER_TYPE_ICH:
                pbUri = CallLog.Calls.CONTENT_URI;
                sel = Calls.TYPE + "=" + CallLog.Calls.INCOMING_TYPE;
                break;
            case BluetoothPbapPath.FOLDER_TYPE_OCH:
                pbUri = CallLog.Calls.CONTENT_URI;
                sel = Calls.TYPE + "=" + CallLog.Calls.OUTGOING_TYPE;
                break;
            case BluetoothPbapPath.FOLDER_TYPE_MCH:
                pbUri = CallLog.Calls.CONTENT_URI;
                sel = Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE;
                break;
            case BluetoothPbapPath.FOLDER_TYPE_CCH:
                pbUri = CallLog.Calls.CONTENT_URI;
                break;
            case BluetoothPbapPath.FOLDER_TYPE_SIM1_PB:
                pbUri = Contacts.CONTENT_URI;
                sel = Contacts.INDICATE_PHONE_SIM + ">" + (-1);
                break;
                /*
                if( mSimAdn == null ){
                    mSimAdn = new BluetoothPbapSimAdn(mContext);
                }
                if(mSimAdn.getAdnList() == null){
                    if( mSimAdn.updateAdn() == false )
                        return 0;
                }
                return mSimAdn.getCount()+1;
                */
            default:
                return 0;
            }
            try {
                c = resolver.query(pbUri, null, sel, null, null);
                if(c != null) {
                    size = c.getCount();
                    if((type == BluetoothPbapPath.FOLDER_TYPE_PB) ||
                        (type == BluetoothPbapPath.FOLDER_TYPE_SIM1_PB)) {
                        // including owner record
                        size++;
                    }
                }
            }finally {
                c.close();
            }
        }
        return size;
    }
    
    private final int getNewMissedCallSize() {
        final Uri myUri = CallLog.Calls.CONTENT_URI;
        String selection = new String(Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE+" AND "+CallLog.Calls.NEW+"=1");
        int size = 0;

        printLog("getNewMissedCallSize : selection="+selection);
        Cursor callCursor = null;
        try {
            callCursor = mContext.getContentResolver().query(myUri, null, selection, null,
                    CallLog.Calls.DEFAULT_SORT_ORDER);
            if (callCursor != null) {
                size = callCursor.getCount();
            }
        } finally {
            if (callCursor != null) {
                callCursor.close();
            }
        }
        printLog("newMissed calls="+size);
        return size;
    }

    
    /********************************/
    /* Interface for BluetoothPbapService  */
    /********************************/
    public boolean enable() {
        boolean ret = false;
        
        printLog("[API] enable");
        // TODO: Enable BT task pbap server and start listener
        mListener = new PbapSocketListener();
        if( enableNative() ) {
            mListener.mStopListen = false;
            mListener.start();
            ret = true;
        }
        else {
            errorLog("enableNative failed");
            mListener = null;
        }
        return ret;
    }

    public void disable() {
        printLog("[API] disable");
        // TODO: Stop listener and disable BT task pbap server
        if(mListener != null) {
            mListener.mStopListen = true;
            wakeupListenerNative();
            try {
                mListener.join();
            } catch (InterruptedException ex) {
                errorLog("mListener close error" + ex);
            }
            disableNative();
        }
        else {
            errorLog("Pbap server is not enabled yet");
        }
    }
    
    /* Accept or reject the connection rquest */
    boolean accept(boolean isAccept) {
        printLog("[API] accept("+String.valueOf(isAccept)+")");
        return authorizeRspNative(isAccept);
    }

    boolean authChallRsp(boolean cancel, String password, String userID) {
        printLog("[API] authChallRsp("+String.valueOf(cancel)+","+password+","+userID+")");
        return authChallengeRspNative(cancel, password, userID);
    }
    /*********************/
    /* JNI Callback functions */
    /*********************/
    private int onAuthorizeInd(String addr) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        printLog("[CBK] onAuthorizeInd("+addr+")");
        if(adapter != null) {
            sendServiceMsg(BluetoothPbapServer.PBAP_AUTHORIZE_IND, adapter.getRemoteDevice(addr));
        }else {
            // TODO: Reject authorize directly
            errorLog("Failed to get default adapter");
            authorizeRspNative(false);
        }
        return 1;
    }
    /* Client connection request. Directly accept the request */
    private int onConnectInd(String addr, String name, int connection_id) {
        printLog("[CBK] onConnectInd("+connection_id+", "+addr+", "+name+")");
        // SH : response of connection request
        if( true == connectRspNative(connection_id, true) )
        {
            mPath = new BluetoothPbapPath();
            getLocalNameAndNum();
            mVcardListing = new BluetoothPbapVCardListing(mContext, mLocalName, mLocalNumber);
            sendServiceMsg(BluetoothPbapServer.PBAP_SESSION_ESTABLISHED, null);
        }
        return ResponseCodes.OBEX_HTTP_OK;
    }

    private void onDisconnectInd() {
        printLog("[CBK] onDisconnectInd");

        // TODO: if composer is still working, stop it
        mPath = null;
        mVcardListing = null;
        mSimAdn = null;
        sendServiceMsg(BluetoothPbapServer.PBAP_SESSION_DISCONNECTED, null);
    }

    private int onSetPathInd(final String name, final int op) {
        int ret = ResponseCodes.OBEX_HTTP_OK;

        printLog("[CBK] onSetPathInd("+name+","+op+")");
        // TODO: change parameters. Do not use ambigious parameters (backup and create)
        if(mPath.setPath(op, name) != true) {
            setPathRspNative(PBAP_CNF_FAILED);
            ret = ResponseCodes.OBEX_HTTP_BAD_REQUEST;
        }else {
            setPathRspNative(PBAP_CNF_SUCCESS);
        }
        return ret;
    }



    private int onPullVcardListingInd(int con_id, 
                                                                    String name, 
                                                                    int order, 
                                                                    String searchVal, 
                                                                    int searchAttr, 
                                                                    int maxListCount, 
                                                                    int listStartOffset) {
        int type;
        int pbSize = 0xFFFF;
        int missedCalls = 0xFFFF;
        String resultPath= null;
        int ret = ResponseCodes.OBEX_HTTP_OK;

        printLog("[CBK] onPullVcardListingInd("+con_id+", "+name+", "+order+", "+searchVal+", "+searchAttr+", "+maxListCount+", "+listStartOffset+")");
        /* Check path */
        if(mPath == null) {
            errorLog("mPath is null");
            ret = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }else {
            /* Get type */
            type = mPath.getPathType(name, true);
            if(type == BluetoothPbapPath.FOLDER_TYPE_UNKNOWN) {
                errorLog("unknown folder type type");
                ret = ResponseCodes.OBEX_HTTP_BAD_REQUEST;
            }else {
                /* Get missed call size */
                if(type == BluetoothPbapPath.FOLDER_TYPE_MCH)
                    missedCalls = getNewMissedCallSize();
                
                if(maxListCount == 0) {
                    pbSize = getPbSize(type);
                }else {
                    /* Check parameters */
                    if(order == BluetoothPbapVCardListing.VCARD_ORDER_PHONETICAL || 
                        searchAttr == BluetoothPbapVCardListing.VCARD_SEARCH_SOUND) {
                        printLog("order or search attrib is not supported");
                        ret = ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
                    }else {
                        /* Parameters are supported */
                        switch(type) {
                        case BluetoothPbapPath.FOLDER_TYPE_PB:
                        case BluetoothPbapPath.FOLDER_TYPE_SIM1_PB:
                            getLocalNameAndNum();
                        case BluetoothPbapPath.FOLDER_TYPE_ICH:
                        case BluetoothPbapPath.FOLDER_TYPE_OCH:
                        case BluetoothPbapPath.FOLDER_TYPE_MCH:
                        case BluetoothPbapPath.FOLDER_TYPE_CCH:
                            ret = mVcardListing.list(type, order, searchAttr, searchVal, listStartOffset, maxListCount);
                            if(ret == ResponseCodes.OBEX_HTTP_OK)
                                resultPath = mVcardListing.getPath();
                            break;
                        default:
                            errorLog("Unknown folder type : "+type);
                            ret = ResponseCodes.OBEX_HTTP_BAD_REQUEST;
                            break;
                        }
                    }
                }
            }
        }
        pullVcardListingRspNative((ret == ResponseCodes.OBEX_HTTP_OK)?PBAP_CNF_SUCCESS:PBAP_CNF_FAILED, 
                                                    pbSize,
                                                    missedCalls,
                                                    resultPath);
        return ret;
    }

    private int composeVCards(int type, 
                                                          boolean vcard21,
                                                          int listOffset,
                                                          int maxCount,
                                                          long filter,
                                                          long contactID) {
        BluetoothVCardEntryHandler handler= null;
        BluetoothVCardComposer composer = null;
        String ownerCard = null;
        String selection = null;
        String order = null;
        boolean ownerIncl = false;

        printLog("[API] composeVCards("+type+","+String.valueOf(vcard21)+","+listOffset+","+maxCount+","+filter+","+contactID+")");
        mResultPath = null;
        /* Check type */
        if(type == BluetoothPbapPath.FOLDER_TYPE_UNKNOWN) {
            errorLog("[ERR] type is unknown");
            return ResponseCodes.OBEX_HTTP_BAD_REQUEST;
        }
        /* check offset and count */
        if(listOffset < 0 || maxCount < 0) {
            errorLog("[ERR] listOffset or maxCount is negtive");
            return ResponseCodes.OBEX_HTTP_BAD_REQUEST;
        }
        /* Set order */
        if((type == BluetoothPbapPath.FOLDER_TYPE_PB) ||
            (type == BluetoothPbapPath.FOLDER_TYPE_SIM1_PB)) {
            order = "upper("+Contacts.DISPLAY_NAME+")";
        }else {
            order = CallLog.Calls.DATE+" DESC";
        }
        /* set selection */
        switch(type) {
        case BluetoothPbapPath.FOLDER_TYPE_ICH:
            selection = Calls.TYPE + "=" + CallLog.Calls.INCOMING_TYPE;
            break;
        case BluetoothPbapPath.FOLDER_TYPE_OCH:
            selection = Calls.TYPE + "=" + CallLog.Calls.OUTGOING_TYPE;
            break;
        case BluetoothPbapPath.FOLDER_TYPE_MCH:
            selection = Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE;
            break;
        case BluetoothPbapPath.FOLDER_TYPE_PB:
            selection = RawContacts.INDICATE_PHONE_SIM + "=" + RawContacts.INDICATE_PHONE;
            if(contactID >= 0) {
                selection += " AND " + Contacts._ID + "= " + contactID;
                listOffset = 0;
                maxCount = 1;
            }else if(listOffset == 0) {
                ownerIncl = true;
                maxCount--;
            }else {
                listOffset--;
            }
            break;
         case BluetoothPbapPath.FOLDER_TYPE_SIM1_PB:
            selection = RawContacts.INDICATE_PHONE_SIM + ">" + RawContacts.INDICATE_PHONE;
            if(contactID >= 0) {
                selection += " AND " + Contacts._ID + "= " + contactID;
                listOffset = 0;
                maxCount = 1;
            }else if(listOffset == 0) {
                ownerIncl = true;
                maxCount--;
            }else {
                listOffset--;
            }
            break;           
        case BluetoothPbapPath.FOLDER_TYPE_CCH:
            break;
        default:
            errorLog("Unsupported folder type");
            return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
        }
        
        /* check if the owner card is included */
        getLocalNameAndNum();
        composer = new BluetoothVCardComposer(mContext, 
                                                            vcard21?VCardConfig.VCARD_TYPE_V21_GENERIC:VCardConfig.VCARD_TYPE_V30_GENERIC,
                                                            true, 
                                                            ((type != BluetoothPbapPath.FOLDER_TYPE_PB) && (type != BluetoothPbapPath.FOLDER_TYPE_SIM1_PB)),
                                                            true);
        composer.setVCardAttribFilter(filter);
        if(ownerIncl) {
            /* If owner card is included */
            ownerCard = composer.composeVCardForPhoneOwnNumber(Phone.TYPE_MOBILE, 
                                                                                                        mLocalName, 
                                                                                                        mLocalNumber, 
                                                                                                        vcard21);
        }
        handler = new BluetoothVCardEntryHandler(ownerCard);
        composer.addHandler(handler);
        if (!composer.init(selection, null, order)) {
            errorLog("composer init failed");
            if(!composer.getErrorReason().equals(BluetoothVCardComposer.FAILURE_REASON_NO_ENTRY)) {
                // if no entry, response with empty or owner record only else return internal error.
                return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
            }
        }else {
            /* move to listOffset */
            if(composer.movePosition(listOffset) == true) {
                int total = composer.getCount() - listOffset;
                if(total < maxCount)
                    maxCount = total;
                /* Create vcard one by one */
                for(int i = 0;i < maxCount;i++) {
                    if (!composer.createOneEntry()) {
                        errorLog("CreateEntry "+i+" failed ");
                        return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                    }
                }
            }else {
                /* It shall be caused the offset is out of range */
                return ResponseCodes.OBEX_HTTP_NOT_FOUND;
            }
        }
        mResultPath = handler.getPath();        
        composer.terminate();
        return ResponseCodes.OBEX_HTTP_OK;
    }

    private int onPullPhonebookInd(int con_id, 
                                                                String name, 
                                                                long filter, 
                                                                boolean vcard21, 
                                                                int maxListCount, 
                                                                int listStartOffset) {
        printLog("[CBK] onPullPhonebookInd("+con_id+", "+name+", "+filter+", "+String.valueOf(vcard21)+", "+maxListCount+", "+listStartOffset+")");
        BluetoothVCardEntryHandler handler= null;
        //BluetoothVCardComposer composer = null;
        String ownerCard;
        String selection = null;
        int missedCalls = 0xFFFF;
        int pbSize = 0xFFFF;
        int type;
        int ret = ResponseCodes.OBEX_HTTP_OK;

        if(name.endsWith(".vcf") == true){
            name = name.substring(0, name.length()-4);
        }
        type = mPath.getPathType(name, false);

        if(type == BluetoothPbapPath.FOLDER_TYPE_UNKNOWN) {
            ret = ResponseCodes.OBEX_HTTP_BAD_REQUEST;
        }else {
            mResultPath = null;        
            /* Get count of missed calls */
            if(type == BluetoothPbapPath.FOLDER_TYPE_MCH) {
                missedCalls = getNewMissedCallSize();
            }
            
            if(maxListCount == 0) {
                pbSize = getPbSize(type);
            }else {

                ret = composeVCards(type,vcard21, listStartOffset, maxListCount, filter, -1);
                /*
                if(type == BluetoothPbapPath.FOLDER_TYPE_SIM1_PB){
                    if(mSimAdn == null){
                       mSimAdn = new  BluetoothPbapSimAdn(mContext);
                    }
                    ret = ResponseCodes.OBEX_HTTP_OK;
                    if(mSimAdn.getAdnList() == null){
                        if( mSimAdn.updateAdn() == false ){
                            ret = mSimAdn.getLastError();
                        }
                    }
                    if(ret == ResponseCodes.OBEX_HTTP_OK){
                        ret = mSimAdn.composeVCard(vcard21,
                                                                    listStartOffset,
                                                                    maxListCount,
                                                                    (filter & BluetoothVCardComposer.VCARD_ATTRIB_EMAIL) != 0,
                                                                    true);
                        mResultPath = mSimAdn.getVCardFilePath();
                    }                        
                }else{
                    ret = composeVCards(type,vcard21, listStartOffset, maxListCount, filter, -1);
                }
                */
            }
        }

        pullPhonebookRspNative((ret==ResponseCodes.OBEX_HTTP_OK)?PBAP_CNF_SUCCESS:PBAP_CNF_FAILED,
                                                pbSize,
                                                missedCalls,
                                                mResultPath);
        return ret;
    }

    private int onPullVcardEntryInd(int con_id, String name, long filter, boolean vcard21) {
        printLog("[CBK] onPullVcardEntryInd("+con_id+", "+name+", "+filter+", "+String.valueOf(vcard21)+")");        
        int ret = ResponseCodes.OBEX_HTTP_OK;
        int type = mPath.getPathType(null, true);
        long idx;
        long contactID = -1;
        //BluetoothVCardComposer composer = null;
        String ownerCard = null;
        String path = null;

        if(type != BluetoothPbapPath.FOLDER_TYPE_UNKNOWN) {
            if(name.endsWith(".vcf")) {
                try {
                    idx = Long.parseLong(name.substring(0,name.length()-4));
                    if((type == BluetoothPbapPath.FOLDER_TYPE_SIM1_PB) ||
                        (type == BluetoothPbapPath.FOLDER_TYPE_PB)) {
                        if (idx > 0) {
                            if(mVcardListing == null) {
                                getLocalNameAndNum();
                                mVcardListing = new BluetoothPbapVCardListing(mContext, mLocalName, mLocalNumber);
                            }
                            
                            if (type == BluetoothPbapPath.FOLDER_TYPE_PB) {
                                contactID = mVcardListing.queryPbID((int)idx-1);
                            }else {
                                contactID = mVcardListing.querySimPbID((int)idx-1);
                            }

                            if(contactID < 0)
                                ret = ResponseCodes.OBEX_HTTP_NOT_FOUND;                            
                        }
                    }
                    else {
                        /* Call log */
                        if(idx <= 0) {
                            ret = ResponseCodes.OBEX_HTTP_NOT_FOUND;
                        }else {
                        /* idx will be passed as offset */
                            idx--;
                        }
                    }

                    if(ret == ResponseCodes.OBEX_HTTP_OK) {
                        ret = composeVCards(type, vcard21, (int)idx, 1, filter, contactID);
                    }
                    /*
                    if(type == BluetoothPbapPath.FOLDER_TYPE_SIM1_PB){
                        if(mSimAdn == null){
                           mSimAdn = new  BluetoothPbapSimAdn(mContext);
                        }
                        ret = ResponseCodes.OBEX_HTTP_OK;
                        if(mSimAdn.getAdnList() == null){
                            if(mSimAdn.updateAdn() == false){
                                ret = mSimAdn.getLastError();
                            }
                        }
                        if(ret == ResponseCodes.OBEX_HTTP_OK){
                            ret = mSimAdn.composeVCard(vcard21,
                                                                        (int)idx,
                                                                        1,
                                                                        (filter & BluetoothVCardComposer.VCARD_ATTRIB_NOTE) != 0,
                                                                        true);
                            mResultPath = mSimAdn.getVCardFilePath();
                        }
                    }else{
                        if(type == BluetoothPbapPath.FOLDER_TYPE_PB) {
                            if(idx > 0) {
                                if(mVcardListing == null) {
                                    getLocalNameAndNum();
                                    mVcardListing = new BluetoothPbapVCardListing(mContext, mLocalName, mLocalNumber);
                                }
                                contactID = mVcardListing.queryPbID((int)idx-1);
                                if(contactID < 0)
                                    ret = ResponseCodes.OBEX_HTTP_NOT_FOUND;
                            }
                        }else {
                            /* Call log */
                    /*
                            if(idx <= 0) {
                                ret = ResponseCodes.OBEX_HTTP_NOT_FOUND;
                            }else {
                            */
                                /* idx will be passed as offset */
                    /*
                                idx--;
                            }
                        }
                        if(ret == ResponseCodes.OBEX_HTTP_OK) {
                            ret = composeVCards(type, vcard21, (int)idx, 1, filter, contactID);
                        }
                    }*/
                }catch(NumberFormatException e) {
                    ret = ResponseCodes.OBEX_HTTP_BAD_REQUEST;
                }
            }else {
                ret = ResponseCodes.OBEX_HTTP_BAD_REQUEST;
            }
        }else {
            ret = ResponseCodes.OBEX_HTTP_BAD_REQUEST;
        }

        if(ret == ResponseCodes.OBEX_HTTP_OK) {
            pullVcardEntryRspNative(PBAP_CNF_SUCCESS, mResultPath);
        }else {
            pullVcardEntryRspNative(PBAP_CNF_FAILED, null);
        }
        return ret;
    }

    private void onAbortInd() {
        // TODO: interrupt the compose
    }
    
    private void onAuthChallInd(String name, boolean isUserIdRequired, boolean isFullAccess)
    {
        printLog("onAuthChallInd: name="+name +
                         ", isUserIdRequired="+String.valueOf(isUserIdRequired)+
                         ", isFullAccess="+String.valueOf(isFullAccess)+")");
        sendServiceMsg(BluetoothPbapServer.PBAP_AUTH_CHALL_IND, null);
    }
    /****************/
    /* Native functions */
    /****************/
    private native static void classInitNative();
    private native void initializeNativeDataNative();
    private native void cleanupNativeDataNative();
    private native boolean enableNative();
    private native void wakeupListenerNative();
    private native void disableNative();
    private native void disconnectNative();
    private native boolean connectRspNative(int con_id, boolean accept);
    private native boolean listenIndicationNative(boolean noWait);
    private native boolean authorizeRspNative(boolean accept);
    private native boolean authChallengeRspNative(boolean cancel, 
                                                                             String password,
										   String userID);
    private native boolean setPathRspNative(int rsp);
    private native boolean pullPhonebookRspNative(int rsp, int pbSize, int newMissed, String vcard);
    private native boolean pullVcardListingRspNative(int rsp, int pbSize, int newMissed, String vcardList);
    private native boolean pullVcardEntryRspNative(int rsp, String vcard);

    /* Utility function */
    private void printLog(String msg) {
	if (DEBUG) Log.d(TAG, msg);
    }
    
    private void errorLog(String msg) {
	Log.e(TAG, msg);
    }
    
    private void sendServiceMsg(int what, Object arg) {
        Message msg = null;
        printLog("[API] sendServiceMsg(" + what + ")");
        if(mServiceHandler != null) {
            msg = mServiceHandler.obtainMessage(what);
            msg.what = what;
            msg.obj = arg;
            mServiceHandler.sendMessage(msg);
        }
        else {
            printLog("mServiceHandler is null");
        }
    }
    
}
