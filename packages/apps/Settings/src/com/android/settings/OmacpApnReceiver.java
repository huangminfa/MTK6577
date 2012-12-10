package com.android.settings;


import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Telephony;
import android.util.Log;
import android.content.ContentUris;
import android.database.Cursor;
import com.android.internal.telephony.TelephonyProperties;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;
import java.util.ArrayList;
import java.util.HashMap;



import static com.android.settings.OmacpApn.*;

/**
 * 
 * @author mtk80968
 *
 */
public class OmacpApnReceiver extends BroadcastReceiver {
    
    /**
     *  Action: com.mediatek.omacp.settings
     *  Extra: appId, String
     *  Extra: context, String
     *  Extra: simId, int
     *  Please refer to OmacpApplicationCapability part.
     *  Notes: Only those value=true application attributes will be included in the intent extra.
     */
    private static final String TAG = "omacp";
    private static final boolean GEMINI_SUPPORT = FeatureOption.MTK_GEMINI_SUPPORT;
    private static boolean mIsCU = false;
    
    private static final String SIM_ID = "simId";
    private static final String APP_ID = "appId";
    private static final String APP_ID_APN = "apn";
//  private static final String APP_ID_MMS = "w4";//MMS
    private static final String OMACP_CONTEXT = "context";
    private static final String ACTION_OMACP = "com.mediatek.omacp.settings";
//  private static final String MIME_TYPE_APN = "application/com.mediatek.omacp-apn";
//  private static final String MIME_TYPE_MMS = "application/com.mediatek.omacp-w4";
    private static final String ACTION_OMACP_RESULT = "com.mediatek.omacp.settings.result";
    
    private static final String APN_NAME = "NAP-NAME";//name
    private static final String APN_APN = "NAP-ADDRESS";//apn
    private static final String APN_PROXY = "PXADDR";//proxy
    private static final String APN_PORT = "PORTNBR";//port
    private static final String APN_USERNAME = "AUTHNAME";//username
    private static final String APN_PASSWORD = "AUTHSECRET";//password
    private static final String APN_SERVER = "SERVER";//server
    private static final String APN_MMSC = "MMSC";//mmsc
    private static final String APN_MMS_PROXY = "MMS-PROXY";//mms proxy
    private static final String APN_MMS_PORT = "MMS-PORT";//mms port
    private static final String APN_AUTH_TYPE = "AUTHTYPE";//auth type
    private static final String APN_TYPE = "APN-TYPE";//type
    private static final String APN_ID = "APN-ID";//type
    private static final String APN_NAP_ID = "NAPID";//type
    private static final String APN_PROXY_ID = "PROXY-ID";//type    
    
    private static final String NAP_AUTH_INFO = "NAPAUTHINFO";
    private static final String PORT = "PORT";
    private static final String APN_SETTING_INTENT = "apn_setting_intent";
    private static final String CU_3GNET_NAME = "3gnet";
    private static final String CU_3GWAP_NAME = "3gwap";    
    private static final String CU_MMS_TYPE = "mms";        

    private static final int SIM_CARD_1 = 0;
    private static final int SIM_CARD_2 = 1;
    private static final int SIM_CARD_SINGLE = 2;
    private static final int SIM_CARD_UNDEFINED = -1;
    
//  public static final String APN_CARRIERS_URI = "content://telephony/carriers";
//  public static final String APN_CARRIERS_URI_GEMINI = "content://telephony/carriers_gemini";

    public static final String PREFERRED_APN_URI = "content://telephony/carriers/preferapn";
    public static final String PREFERRED_APN_URI_GEMINI = "content://telephony/carriers_gemini/preferapn";
    private static final String PREFERRED_APN_URI_GEMINI_SIM1 = "content://telephony/carriers_sim1/preferapn";
    private static final String PREFERRED_APN_URI_GEMINI_SIM2 = "content://telephony/carriers_sim2/preferapn";
    
    
    private int mSimId;
    private long mApnToUseId = -1;
    private boolean mResult;
//  private String mOmacpContext;
    private String mAppId;
    
    private String mName;
    private String mApn;
    private String mProxy;
    private String mPort;
    private String mUserName;
    private String mPassword;
    private String mServer;
    private String mMmsc;
    private String mMmsProxy;
    private String mMmsPort;
    private String mAuthType;
    private String mType;
    private String mApnId;
    private String mMcc;
    private String mMnc;
    private String mNapId;
    private String mProxyId;
    private ArrayList<Intent> mIntentList;
    private ArrayList<Long> mApnIdList;
    
    private static int authType = -1;
    private Uri mUri;
    private String mNumeric;
    private Uri mPreferedUri;

    
    private OmacpApn mOmacpApn;
    
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if(ACTION_OMACP.equals(action)){
            mResult = false;
            //get intent list
            mIntentList = intent.getParcelableArrayListExtra (APN_SETTING_INTENT);
            
            if (mIntentList == null) {
                sendFeedback(context);
                Xlog.e(TAG, "mIntentList == null");  
                return;
            }
       
            int sizeIntent = mIntentList.size();
            
            Xlog.d(TAG, "apn list size is "+sizeIntent);  
            
            if (sizeIntent <=0) {
                sendFeedback(context);
                Xlog.e(TAG, "Intent list size is wrong");    
                return;         
            }
                
            if (initState(mIntentList.get(0)) == false) {
                sendFeedback(context);
                Xlog.e(TAG, "Can not get MCC+MNC");  
                return;
            }            
                  
            mIsCU = Utils.isCuLoad();
                
            for (int k=0; k<sizeIntent; k++) {
                
                //extract APN parameters
                extractAPN (mIntentList.get(k), context);
                
                ContentValues values = new ContentValues();

                validateProfile(values);

                if (mIsCU && (Utils.isCuCard(mNumeric))) {
                    if(UpdateOperApn(context, values, false)) {
                        UpdateOperApn(context, values, true);                        
                    } else {
                        insertAPN(context, values);
                        if(GEMINI_SUPPORT){
                            forTheOtherCard(context, mSimId, values);   
                        }
                    }
                    
                } else {
                    insertAPN(context, values);
                    if(GEMINI_SUPPORT)
                        forTheOtherCard(context, mSimId, values);               
                }              
            }
            
            if (mApnToUseId!=-1) {
                
                mResult = setCurrentApn(context, mApnToUseId, mPreferedUri);
                
            }
        
            sendFeedback(context);

        }
    }
    
    private boolean forTheOtherCard(Context context, int simId, ContentValues values){
        Xlog.d(TAG,"for the other card");
        
        int theOtherSimId = 1 - simId;
        Uri theOtherUri = null;
        if(Utils.opType == Utils.OpIndex.OP_CMCC) {
		switch (theOtherSimId) {
		    case SIM_CARD_1:
		        theOtherUri = Telephony.Carriers.SIM1Carriers.CONTENT_URI;
		        break;
	    
		    case SIM_CARD_2:
		        theOtherUri = Telephony.Carriers.SIM2Carriers.CONTENT_URI;
		        break;
		    
		    case SIM_CARD_SINGLE:
		        theOtherUri = Telephony.Carriers.CONTENT_URI;
		        break;
		    default:
		        break;
		}
        } else {
		switch (theOtherSimId) {
		    case SIM_CARD_1:
		        theOtherUri = Telephony.Carriers.CONTENT_URI;
		        break;
	    
		    case SIM_CARD_2:
		        theOtherUri = Telephony.Carriers.GeminiCarriers.CONTENT_URI;
		        break;
		}
        }
        Xlog.d(TAG,"[forTheOtherCard] theOtherSimId:" + theOtherSimId);
        Xlog.d(TAG,"[forTheOtherCard] theOtherUri:" + theOtherUri);

        if(theOtherUri == null) {
            return false;
        }
        OmacpApn theOtherOmacpApn = new OmacpApn(context, theOtherSimId, theOtherUri, mNumeric);
        ArrayList<HashMap<String, String>> omcpIdList = theOtherOmacpApn.getExistOmacpId();
        int size = omcpIdList.size();
        for(int i = 0; i < size; i++){
            HashMap<String, String> map = omcpIdList.get(i);
            if(map.containsKey(mApnId)){
                Xlog.d(TAG,"The other card: this apn already exists!");
                return true;
            }//apnid matches
        }// end of loop:for
        
        long theOtherId = -1;
        theOtherId = theOtherOmacpApn.insert(context, values);
        Xlog.d(TAG,"The other id = " + theOtherId);
        
        return theOtherId == -1 ? false : true;
    }
    
    /**
     *   send installation result to OMA CP service
     * @param context
     * @param result
     */
    
    private void sendFeedback(Context context){
        Intent it = new Intent();
        it.setAction(ACTION_OMACP_RESULT);
        it.putExtra(APP_ID, APP_ID_APN);
        it.putExtra("result", mResult);

//      log("action = " + ACTION_OMACP_RESULT + " result = " + result);
        context.sendBroadcast(it);
    }
    
    /**
     * Check the key fields' validity and save if valid.
     * @param force save even if the fields are not valid
     * @return true if the data is valid
     */
    private boolean validateProfile(ContentValues values) {

    
        values.put(sProjection[NAME_INDEX], mName);
        values.put(sProjection[APN_INDEX], checkNotSet(mApn));
        values.put(sProjection[PROXY_INDEX], checkNotSet(mProxy));
        values.put(sProjection[PORT_INDEX], checkNotSet(mPort));
        values.put(sProjection[USER_INDEX], checkNotSet(mUserName));
        values.put(sProjection[SERVER_INDEX], checkNotSet(mServer));
        values.put(sProjection[PASSWORD_INDEX], checkNotSet(mPassword));
        values.put(sProjection[MMSC_INDEX], checkNotSet(mMmsc));
        values.put(sProjection[MCC_INDEX], mMcc);
        values.put(sProjection[MNC_INDEX], mMnc);
        values.put(sProjection[MMSPROXY_INDEX], checkNotSet(mMmsProxy));
        values.put(sProjection[MMSPORT_INDEX], checkNotSet(mMmsPort));
        values.put(sProjection[AUTH_TYPE_INDEX], authType);
        values.put(sProjection[TYPE_INDEX], checkNotSet(mType));
        values.put(sProjection[SOURCE_TYPE_INDEX], 2);
        values.put(sProjection[APN_ID_INDEX], checkNotSet(mApnId));
        values.put(sProjection[NAP_ID_INDEX], checkNotSet(mNapId));
        values.put(sProjection[PROXY_ID_INDEX], checkNotSet(mProxyId));
        
//        if(current)
//          values.put(Telephony.Carriers.CURRENT, 1);
        values.put(sProjection[NUMERIC_INDEX], mNumeric);


        return true;
    }
    
    private boolean getMccMnc(){
        // MCC is first 3 chars and then in 2 - 3 chars of MNC

        if (mNumeric != null && mNumeric.length() > 4) {
            // Country code
            String mcc = mNumeric.substring(0, 3);
            // Network code
            String mnc = mNumeric.substring(3);
            // Auto populate MNC and MCC for new entries, based on what SIM reports
            mMcc = mcc;
            mMnc = mnc;
            return true;

        } else {
            return false;
        }
    }
    
    /**
     * Get basic param
     * Init Content URI & numeric
     */
    private boolean initState(Intent intent) {
        //get simId
        if (GEMINI_SUPPORT) {
            mSimId = intent.getIntExtra("simId", SIM_CARD_UNDEFINED);
            Xlog.d(TAG,"GEMINI_SIM_ID_KEY = " + mSimId);
        } else {
            mSimId = SIM_CARD_SINGLE;
            Xlog.d(TAG,"Not support GEMINI");
        }
        
        //get appId
        mAppId = intent.getStringExtra(APP_ID);
        if(Utils.opType == Utils.OpIndex.OP_CMCC) {
		switch (mSimId) {
		    case SIM_CARD_1:
		        mUri = Telephony.Carriers.SIM1Carriers.CONTENT_URI;
		        mNumeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, "-1");
		        mPreferedUri = Uri.parse(PREFERRED_APN_URI_GEMINI_SIM1);
		        break;

		    case SIM_CARD_SINGLE:
		        mUri = Telephony.Carriers.CONTENT_URI;
		        mNumeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, "-1");
		        mPreferedUri = Uri.parse(PREFERRED_APN_URI);
		        break;

		    case SIM_CARD_2:
		        mUri = Telephony.Carriers.SIM2Carriers.CONTENT_URI;
		        mNumeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC_2, "-1");
		        mPreferedUri = Uri.parse(PREFERRED_APN_URI_GEMINI_SIM2);
		        break;       
		    default:
		        break;
		}
        } else {
                switch (mSimId) {
		    case SIM_CARD_1:
		    case SIM_CARD_SINGLE:
		        mUri = Telephony.Carriers.CONTENT_URI;
		        mNumeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, "-1");
		        mPreferedUri = Uri.parse(PREFERRED_APN_URI);
		        break;

		    case SIM_CARD_2:
		        mUri = Telephony.Carriers.GeminiCarriers.CONTENT_URI;
		        mNumeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC_2, "-1");
		        mPreferedUri = Uri.parse(PREFERRED_APN_URI_GEMINI);
		        break;       

		}
       
        }
       
        Xlog.d(TAG,"[initState] mSimId:" + mSimId);
        Xlog.d(TAG,"[initState] mUri:" + mUri);
        Xlog.d(TAG,"[initState] mNumeric:" + mNumeric);
        Xlog.d(TAG,"[initState] mPreferedUri:" + mPreferedUri);
        return getMccMnc();
    }
    
    /**
     *  get port
     */
    private void getPort(Intent intent){
        mPort = null;
        ArrayList<HashMap<String, String>> portList = (ArrayList<HashMap<String, String>>) intent.getExtra(PORT);
        if(portList != null){
            if(portList.size() > 0){
                HashMap<String, String> portMap = portList.get(0);//using the first one, ignore others 
                mPort = portMap.get(APN_PORT);//port
            }
        }
    }
    
    /**
     *  get username,password,auth_type
     */
    private void getNapAuthInfo(Intent intent){
        
        mUserName = null;
        mPassword = null;
        mAuthType = null;
        authType = -1;
        
        ArrayList<HashMap<String, String>> napAuthInfo = (ArrayList<HashMap<String, String>>) intent.getExtra(NAP_AUTH_INFO);
        if(napAuthInfo != null){
            if(napAuthInfo.size() > 0){
                HashMap<String, String> napAuthInfoMap = napAuthInfo.get(0);//using the first one, ignore others 
                mUserName = napAuthInfoMap.get(APN_USERNAME);//username
                mPassword = napAuthInfoMap.get(APN_PASSWORD);//password
                mAuthType = napAuthInfoMap.get(APN_AUTH_TYPE);//auth type
                
                if(mAuthType != null){
                    if("PAP".equalsIgnoreCase(mAuthType)){
                        authType = 1;//PAP
                    } else if("CHAP".equalsIgnoreCase(mAuthType)){
                        authType = 2;//CHAP
                    } 
                    //not support now
//                  else if("MS-CHAP".equalsIgnoreCase(mAuthType)){
//                      authType = 4;//MS-CHAP
//                  }
                    else {
                        authType = 3;//PAP or CHAP
                    }
                }
            }
        }
    }
    
    private String checkNotSet(String value) {
        if (value == null || value.length() == 0) {
            return "";
        } else {
            return value;
        }
    }
    
    
    /**
     * Extract APN parameters from the intent
     */
    private void extractAPN(Intent intent, Context context) {
        
        //apn parameters
        mName = intent.getStringExtra(APN_NAME);//name
        

        if ((mName == null) || (mName.length() < 1)) {
            mName = context.getResources().getString(R.string.untitled_apn);
        }
        mApn = intent.getStringExtra(APN_APN);//apn
        mProxy = intent.getStringExtra(APN_PROXY);//proxy
        
        //get port
        getPort(intent);
        //get username,password,auth_type
        getNapAuthInfo(intent);
        
        mServer = intent.getStringExtra(APN_SERVER);//server
        mMmsc = intent.getStringExtra(APN_MMSC);//MMSC
        mMmsProxy = intent.getStringExtra(APN_MMS_PROXY);//MMSC proxy
        mMmsPort = intent.getStringExtra(APN_MMS_PORT);//MMSC port
        mType = intent.getStringExtra(APN_TYPE);//type
        mApnId = intent.getStringExtra(APN_ID);//apnId:should be unique
        mNapId = intent.getStringExtra(APN_NAP_ID);
        mProxyId = intent.getStringExtra(APN_PROXY_ID);
//      mApnId =  String.valueOf(intent.getLongExtra(APN_ID, -1l));
        
/*      log("mName = " + mName);
        log("mApn = " + mApn);
        log("mProxy = " + mProxy);
        log("mPort = " + mPort);
        log("mUserName = " + mUserName);
        log("mPassword = " + mPassword);
        log("mServer = " + mServer);
        log("mMmsc = " + mMmsc);
        log("mMmsProxy = " + mMmsProxy);
        log("mMmsPort = " + mMmsPort);
        log("mType = " + mType);
        log("mApnId = " + mApnId);
        log("mAuthType = " + mAuthType);
        log("mNapId = " + mNapId);
        log("mProxyId = " + mProxyId);*/
    }
    
    /**
     * Add APN record to database
     */
    private void insertAPN(Context context, ContentValues values) {
        

        Xlog.d(TAG,"insertAPN");
        boolean isApnExisted = false;
        boolean isMmsApn = "mms".equalsIgnoreCase(mType);
        mOmacpApn = new OmacpApn(context, mSimId, mUri, mNumeric);
        ArrayList<HashMap<String, String>> omcpIdList = mOmacpApn.getExistOmacpId();
        int sizeApn = omcpIdList.size();
        for(int i = 0; i < sizeApn; i++){
            HashMap<String, String> map = omcpIdList.get(i);
            if(map.containsKey(mApnId)){
                
                isApnExisted = true;
                mResult = true;
                if (!isMmsApn) {
                    mApnToUseId = Long.parseLong(map.get(mApnId));
                }
                break;
            }//apnid matches
        }// end of loop:for
        
        if (isApnExisted != true) {
            
            long id = mOmacpApn.insert(context, values);
            if (id != -1) {
                mResult = true;
                if (!isMmsApn) {
                    mApnToUseId = id;
                    
                }
            }

            
        }

    }
    
    private boolean setCurrentApn(final Context context, final long mApnToUseId, final Uri preferedUri) {
        int row = 0;
        ContentValues values = new ContentValues();
        values.put("apn_id", mApnToUseId);
        ContentResolver mContentResolver = context.getContentResolver();
        try {
            row = mContentResolver.update(preferedUri, values, null, null);
        } catch (SQLException e) {
            //
        }
        
        return (row>0)?true:false;

    } 
    
    private boolean UpdateOperApn(Context context, final ContentValues values, boolean IsTheOther) {
        
            boolean bSuc = false;
            if(!IsOperDefaultApn()) {                
                Xlog.d(TAG,"not oper APN");
                return false;
            }
            ContentResolver mContentResolver = context.getContentResolver();
            
            Uri uri = mUri;
            if (IsTheOther) {
                int theOtherSimId = 1 - mSimId;    
                if(Utils.opType == Utils.OpIndex.OP_CMCC) {
		        switch (theOtherSimId) {
		            case SIM_CARD_1:
		                uri = Telephony.Carriers.SIM1Carriers.CONTENT_URI;
		                break;
	    
		            case SIM_CARD_2:
		                uri = Telephony.Carriers.SIM2Carriers.CONTENT_URI;
		                break;
		    
		            case SIM_CARD_SINGLE:
		                uri = Telephony.Carriers.CONTENT_URI;
		                break;
		            default:
		                break;
		        }
                } else {
		        switch (theOtherSimId) {
		            case SIM_CARD_1:
		                uri = Telephony.Carriers.CONTENT_URI;
		                break;
		    
		            case SIM_CARD_2:
		                uri = Telephony.Carriers.GeminiCarriers.CONTENT_URI;
		                break;
                	}
                }
                Xlog.d(TAG,"[UpdateOperApn] theOtherSimId: " + theOtherSimId);
                Xlog.d(TAG,"[UpdateOperApn] uri: " + uri);
            }
            String[] whereArgs = new String[]{mNumeric,mApn};
            
            String where = Telephony.Carriers.NUMERIC+ "=?" + " AND "
            + Telephony.Carriers.APN+ "=?" + " AND "
            + Telephony.Carriers.SOURCE_TYPE + "=0";

            Xlog.d(TAG,where);

            Cursor cursor = mContentResolver.query(
                    uri, 
                    new String[] { Telephony.Carriers._ID, Telephony.Carriers.TYPE }, 
                    where, 
                    whereArgs, 
                    null);
            if(cursor == null) {
                Xlog.i(TAG, "query in updated cursor == null");
            }

            if(cursor != null){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    String typeTemp = cursor.getString(1);
                    if(((mType==null||!mType.equals(CU_MMS_TYPE))
                        &&(typeTemp==null||!typeTemp.equals(CU_MMS_TYPE)))
                        ||((mType!=null&&mType.equals(CU_MMS_TYPE))
                                &&(typeTemp!=null&&typeTemp.equals(CU_MMS_TYPE)))) {

                        Uri mOperUri = ContentUris.withAppendedId(uri, 
                                Integer.parseInt( cursor.getString(0)));
                        try {
                            values.put(sProjection[SOURCE_TYPE_INDEX], 0);
                            if (mContentResolver.update(mOperUri, values, null, null)<0) {
                                
                                values.put(sProjection[SOURCE_TYPE_INDEX], 2);
                                cursor.close();
                                return false;
                            }
                            if (!IsTheOther) {
                                mApnToUseId = Long.parseLong(cursor.getString(0));                              
                            }
                            bSuc = true;
                            mResult = true;

                           } catch (SQLException e) {
                               cursor.close();
                               return false;
                           }
                        }

                    cursor.moveToNext();
                }// end of while
                cursor.close();
                Xlog.e(TAG, "cursor end");               
            } else {
                return false;
        }
            Xlog.e(TAG, "return bSuc = " +bSuc); 
            return bSuc;
    }
    
    private boolean IsOperDefaultApn() {

        if (((mApn!=null)&&(mApn.equals(CU_3GNET_NAME)))
            &&((mType==null)||(!mType.equals(CU_MMS_TYPE)))) {

            return true;
        } else if ((mApn!=null)&&(mApn.equals(CU_3GWAP_NAME))) {

            return true;
        }               
        return false;
            
    }
        

}
