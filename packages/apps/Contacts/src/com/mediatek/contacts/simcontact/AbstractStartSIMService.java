package com.mediatek.contacts.simcontact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;//for usim
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.provider.Settings;

import android.text.TextUtils;
import android.util.Log;
import android.os.ServiceManager;

import com.android.contacts.ContactsUtils;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.internal.telephony.ITelephony;
import android.provider.Telephony.SIMInfo;

import com.mediatek.contacts.util.ContactsGroupUtils;
import com.mediatek.contacts.util.OperatorUtils;
import com.mediatek.contacts.util.ContactsGroupUtils.USIMGroup;


import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.android.i18n.phonenumbers.AsYouTypeFormatter;
import com.android.i18n.phonenumbers.PhoneNumberUtil;

public abstract class AbstractStartSIMService extends Service {

	private static final String TAG = "AbstractStartSIMService";
	private static final boolean DBG = true;
	
	public static final int SLOT1 = com.android.internal.telephony.Phone.GEMINI_SIM_1;
	public static final int SLOT2 = com.android.internal.telephony.Phone.GEMINI_SIM_2;
	
	public static final String ACTION_PHB_LOAD_FINISHED = "com.android.contacts.ACTION_PHB_LOAD_FINISHED";
	
	public static final String SERVICE_SLOT_KEY = "which_slot";
	public static final String SERVICE_WORK_TYPE = "work_type";
	
	public static final int SERVICE_WORK_NONE = 0;
	public static final int SERVICE_WORK_IMPORT = 1;
	public static final int SERVICE_WORK_REMOVE = 2;
	public static final int SERVICE_WORK_UNKNOWN = 3;

	public static final int MSG_DELAY_PROCESSING = 100;
	public static final int LOAD_SIM_CONTACTS = 200;
	public static final int REMOVE_OLD = 300;
	public static final int IMPORT_NEW = 400;
	public static final int FINISH_IMPORTING = 500;
	
	public static final int MSG_SLOT1_REMOVE_OLD = SLOT1 + REMOVE_OLD;
	public static final int MSG_SLOT2_REMOVE_OLD = SLOT2 + REMOVE_OLD;
	public static final int MSG_SLOT1_LOAD = SLOT1 + LOAD_SIM_CONTACTS;
	public static final int MSG_SLOT2_LOAD = SLOT2 + LOAD_SIM_CONTACTS;
	public static final int MSG_SLOT1_IMPORT = SLOT1 + IMPORT_NEW;
	public static final int MSG_SLOT2_IMPORT = SLOT2 + IMPORT_NEW;
	public static final int MSG_SLOT1_FINISH_IMPORTING = SLOT1 + FINISH_IMPORTING;
	public static final int MSG_SLOT2_FINISH_IMPORTING = SLOT2 + FINISH_IMPORTING;
	public static final int MSG_SLOT1_MSG_DELAY_PROCESSING = SLOT1 + MSG_DELAY_PROCESSING;
	public static final int MSG_SLOT2_MSG_DELAY_PROCESSING = SLOT2 + MSG_DELAY_PROCESSING;

	public static final int SIM_TYPE_SIM = SimCardUtils.SimType.SIM_TYPE_SIM;
	public static final int SIM_TYPE_USIM = SimCardUtils.SimType.SIM_TYPE_USIM;
	//UIM
	public static final int SIM_TYPE_UIM = SimCardUtils.SimType.SIM_TYPE_UIM;
	//UIM
	public static final int SIM_TYPE_UNKNOWN = -1;
	
	//All these status should be control by sub-class.
	//However here does workaround due to some historical reason.
	public static final int SERVICE_IDLE = 0;
	public static final int SERVICE_DELETE_CONTACTS = 1;
	public static final int SERVICE_QUERY_SIM= 2;
	public static final int SERVICE_IMPORT_CONTACTS = 3;
	
	private static int sServiceState = SERVICE_IDLE;
	private static int sServiceState2 = SERVICE_IDLE;

	private ImportAllSimContactsThread mThread1;
	private ImportAllSimContactsThread mThread2;
    private ArrayList<Integer> mSimWorkQueue = new ArrayList<Integer>();
    private ArrayList<Integer> mSimWorkQueue2 = new ArrayList<Integer>();

	private SimHandler mHandler;
	
	// mtk80909 for speed dial
	private int mCurrentSimId;

    private static final String[] COLUMN_NAMES = new String[] {
        "index",
        "name",
        "number",
        "emails",
        "additionalNumber",
        "groupIds",
        /// M: New Feature by Mediatek for AAS 
        "_id",
        "aas" };

    protected static final int INDEX_COLUMN = 0; // index in SIM
    protected static final int NAME_COLUMN = 1;
    protected static final int NUMBER_COLUMN = 2;
    protected static final int EMAIL_COLUMN = 3;
    protected static final int ADDITIONAL_NUMBER_COLUMN = 4;
    protected static final int GROUP_COLUMN = 5;
    /// M: New Feature by Mediatek for AAS 
    protected static final int AAS_COLUMN = 7;


	HashMap<Integer, Integer> grpIdMap = new HashMap<Integer, Integer>();

	// In order to prevent locking DB too long,
	// set the max operation count 90 in a batch.
	private static final int MAX_OP_COUNT_IN_ONE_BATCH = 90;
	
    // Timer to control the refresh rate of the list
    private final RefreshTimer mRefreshTimer = new RefreshTimer(SLOT1);
    private final RefreshTimer mRefreshTimer2 = new RefreshTimer(SLOT2);
    private static final long REFRESH_INTERVAL_MS = 1000;
    private static final long REFRESH_INTERVAL_MS_2 = 1300;
    
    protected static boolean sNeedToDelay = false;
    private static final int DELAY_TIME = 2500;
    protected BroadcastReceiver mReceiver = null;
    class ServiceInterruptReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            log("[onReceive]intent: " + intent);
            String action = intent.getAction();
            if (action.equals("com.android.action.LAUNCH_CONTACTS_LIST")) {
                sNeedToDelay = true;
            }
            log("[onReceive]sNeedToDelay: " + sNeedToDelay);
        }
    };
	
    
    public static class ServiceWorkData {
        public int    mWorkType = SERVICE_WORK_NONE;
        public int    mSlotId = -1;
        public int    mSimId = -1;
        public int    mSimType = SIM_TYPE_UNKNOWN;
        public Cursor mSimCursor = null;
        public int    mInServiceState = SERVICE_IDLE;
        
        ServiceWorkData(){}

        ServiceWorkData(int workType, int slotId, int simId, int simType,
                Cursor simCursor, int serviceState) {
            mWorkType = workType;
            mSlotId = slotId;
            mSimId = simId;
            mSimType  = simType;
            mSimCursor = simCursor;
            mInServiceState = serviceState;
        }

        ServiceWorkData(ServiceWorkData workData) {
            mWorkType = workData.mWorkType;
            mSlotId = workData.mSlotId;
            mSimId = workData.mSimId;
            mSimType  = workData.mSimType;
            mSimCursor = workData.mSimCursor;
            mInServiceState = workData.mInServiceState;
        }
    }
    
	public void onCreate(Bundle icicle) {
		log("onCreate()");
	}

	public void onStart(Intent intent, int startId) {

		log("[onStart]" + intent + ", startId " + startId);
		if (intent == null) {
			return;
		}

		if (mReceiver == null) {
    		mReceiver = new ServiceInterruptReceiver();
    		IntentFilter intentFilter = new IntentFilter();
    		intentFilter.addAction("com.android.action.LAUNCH_CONTACTS_LIST");
    		registerReceiver(mReceiver, intentFilter);
		}
		
		mHandler = new SimHandler();
		int slotId = intent.getIntExtra(SERVICE_SLOT_KEY, 0);
		int workType = intent.getIntExtra(SERVICE_WORK_TYPE, -1);
		if (slotId == 0) {
		    mSimWorkQueue.add(workType);
		} else {
		    mSimWorkQueue2.add(workType);
		}
		Log.i(TAG, "[onStart]slotId: " + slotId + "|workType:" + workType);
		// mtk80909 for Speed Dial
		mCurrentSimId = slotId;
		ContactsUtils.isServiceRunning[mCurrentSimId] = true;

        ServiceWorkData workData = new ServiceWorkData(workType, slotId, -1,
                SIM_TYPE_UNKNOWN, null, SERVICE_IDLE); 
        sendTaskPoolMsg(workData);
	}

	public void onDestroy() {
		super.onDestroy();
		log("onDestroy()");
		
		if (mReceiver != null) {
		    unregisterReceiver(mReceiver);
		    mReceiver = null;
		}
		
		// mtk80909 for Speed Dial
		ContactsUtils.isServiceRunning[mCurrentSimId] = false;
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Delete all sim contacts.
	 * 
	 * @param slotId
	 */
	public void deleteSimContact(final ServiceWorkData workData) {
	    final int slotId = workData.mSlotId;
	    
		new Thread(new Runnable() {
			public void run() {
				log("[deleteSimContact] begin. slotId: " + slotId);
				//Check SIM state here to make sure whether it needs to remove all SIM or not
                int j = 10;
                boolean simInfoReady = SimCardUtils.isSimInfoReady();
                while (j > 0) {
                    log("[deleteSimContact] simInfoReady: " + simInfoReady);
                    if (!simInfoReady) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.w(TAG, "catched excepiotn.");
                        }
                        simInfoReady = SimCardUtils.isSimInfoReady();
                    } else {
                        break;
                    }
                    j--;
                }
                
				int currSimId = 0;
				List<SIMInfo> simList = SIMInfo.getAllSIMList(AbstractStartSIMService.this);
				String selection = null;
				StringBuilder delSelection = new StringBuilder();
				for(SIMInfo simInfo:simList) {
					if (simInfo.mSlot == slotId) {
						currSimId = (int)simInfo.mSimId;
					}
					if (simInfo.mSlot == -1) {
						delSelection.append(simInfo.mSimId).append(",");
					}
				} 
				
                workData.mSimId = currSimId;
                
                if (currSimId > 0 && workData.mSimType == SIM_TYPE_UNKNOWN) {
                    workData.mSimType = SimCardUtils.getSimTypeBySlot(slotId);
                }
                
				if (delSelection.length() > 0)
                        delSelection.deleteCharAt(delSelection.length() - 1);
				if (currSimId == 0 && slotId >= 0) {
				    if (delSelection.length() > 0) {
				        selection = delSelection.toString();
				    }
				} else {
				    selection = currSimId + ((delSelection.length() > 0)? ("," + delSelection.toString()): "");
				}
				
				log("[deleteSimContact]slotId:" + slotId + "|selection:" + selection);
				
                if (!TextUtils.isEmpty(selection)) {
                    int count = getContentResolver().delete(
                            RawContacts.CONTENT_URI.buildUpon()
                                    .appendQueryParameter("sim", "true").build(),
                            RawContacts.INDICATE_PHONE_SIM + " IN ("+ selection + ")", null);
                    log("[deleteSimContact]slotId:" +slotId + "|count:" + count);
                }
                
                int anotherSlot = slotId == SLOT1?SLOT2:(slotId == SLOT2?SLOT1:-1);
                if (workData.mSimCursor == null || currSimId == 0) {
                    getContentResolver().delete(Groups.CONTENT_URI,
                            Groups.ACCOUNT_NAME + "='" + "USIM" + slotId + "' AND " + 
                            Groups.ACCOUNT_TYPE + "='USIM Account'", null); 
                }
                if (!SimCardUtils.isSimInserted(anotherSlot)) {
                    getContentResolver().delete(Groups.CONTENT_URI,
                            Groups.ACCOUNT_NAME + "='" + "USIM" + anotherSlot + "' AND " + 
                            Groups.ACCOUNT_TYPE + "='USIM Account'", null);
                }

//				sendImportSimContactsMsg(workData);
                boolean simStateReady = checkSimState(slotId) && checkPhoneBookState(slotId);
                int workType = workData.mWorkType;
                int i = 10;
                while (i > 0) {
                    if (!simStateReady && workType == SERVICE_WORK_IMPORT) {
                    	try {
                    		Thread.sleep(1000);
                    	} catch (Exception e) {
                    		Log.w(TAG, "catched excepiotn.");
                    	}
                    	simStateReady = checkSimState(slotId) && checkPhoneBookState(slotId);
                    } else {
                        break;
                    }
                    i--;
                }

                workData.mSimId = -1;
                if (simStateReady) {
                    SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(AbstractStartSIMService.this, slotId);
                    if (simInfo != null)
                        workData.mSimId = (int) simInfo.mSimId;
                }
                
                sendLoadSimMsg(workData);
            }
        }).start();
	}

	/**
	 * Check the state of the sim card in the given slot. If sim state is ready
	 * for importint, then return true.
	 * 
	 * @param slotId
	 * @return
	 */
    boolean checkSimState(final int slotId) {
        boolean simPUKReq = SimCardUtils.isSimPukRequest(slotId);
        boolean simPINReq = SimCardUtils.isSimPinRequest(slotId);
        boolean simInserted = SimCardUtils.isSimInserted(slotId);
        boolean isRadioOn = SimCardUtils.isSetRadioOn(getContentResolver(), slotId);
        boolean isFdnEnabled = SimCardUtils.isFdnEnabed(slotId);
        boolean isSimInfoReady = SimCardUtils.isSimInfoReady();
        Log.d(TAG, "[checkSimState]slotId:" + slotId + "||simPUKReq: "
                + simPUKReq + "||simPINReq: " + simPINReq + "||isRadioOn: "
                + isRadioOn + "||isFdnEnabled: " + isFdnEnabled
                + "||simInserted: " + simInserted + "||isSimInfoReady:" + isSimInfoReady);
        if (simPUKReq || !isRadioOn || isFdnEnabled || simPINReq
                || !simInserted || !isSimInfoReady) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * check PhoneBook State is ready if ready, then return true.
     * 
     * @param slotId
     * @return
     */
    boolean checkPhoneBookState(final int slotId) {
        return SimCardUtils.isPhoneBookReady(slotId);
    }

	private void query(final ServiceWorkData workData) {
	    final int slotId = workData.mSlotId;
		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
		if (workData.mSimType == SIM_TYPE_UNKNOWN) {
		    workData.mSimType = SimCardUtils.getSimTypeBySlot(slotId);
		}
		final int simType = workData.mSimType;
		log("[query]slotId: " + slotId + " ||isUSIM:" + (simType==SIM_TYPE_USIM));
		final Uri iccUri = SimCardUtils.SimUri.getSimUri(slotId);
		log("[query]uri: " + iccUri);
		new Thread() {
			public void run() {

                Cursor cursor = null;
                try {
				    cursor = getContentResolver().query(iccUri, COLUMN_NAMES, null, null, null);
                } catch (java.lang.NullPointerException e) {
                	Log.d(TAG, "catched exception. cursor is null.");
                }
                
				Log.d(TAG, "[query]slotId:" + slotId + "|cursor:" + cursor);
                if (cursor != null) {
                    int count = cursor.getCount();
                    Log.d(TAG, "[query]slotId:" + slotId + "|cursor count:" + count);
                }
                if (simType == SIM_TYPE_USIM) {
                    USIMGroup.syncUSIMGroupContactsGroup(
                            AbstractStartSIMService.this, workData, grpIdMap);
                } else {
                    USIMGroup.deleteUSIMGroupOnPhone(AbstractStartSIMService.this, slotId);
                }
                workData.mSimCursor = cursor;
//                sendRemoveSimContactsMsg(workData);
                sendImportSimContactsMsg(workData);
			}
		}.start();

	}

	private class ImportAllSimContactsThread extends Thread {
		int mSlot = 0;
		Cursor mSimCursor = null;
		int mSimType = -1;
		int mSimId = 0;

		public ImportAllSimContactsThread(final ServiceWorkData workData) {
			super("ImportAllSimContactsThread");
			mSlot = workData.mSlotId;
			mSimCursor = workData.mSimCursor;
			mSimType = workData.mSimType;
			mSimId = workData.mSimId;
		}

		@Override
		public void run() {
			final ContentResolver resolver = getContentResolver();
            
			log("importing thread for SIM " + mSlot);
			if (mSimCursor != null) {
				log("1begin insert slot:" + mSlot + " contact|mSimId:" + mSimId);
				if (mSimId < 1) {
	                SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(AbstractStartSIMService.this, mSlot);
	                if (simInfo != null)
	                    mSimId = (int) simInfo.mSimId;
				}
				log("2begin insert slot:" + mSlot + " contact|mSimId:" + mSimId);
                if (mSimId > 0) {
                    synchronized (this) {
                        actuallyImportOneSimContact(mSimCursor, resolver,
                                mSlot, mSimId, mSimType, null, false);
                    }
                }
				log("end insert slot" + mSlot + " contact");
				mSimCursor.close();
				
				if (checkSimState(mSlot) && mSimId > 0) {
					Cursor sdnCursor = null;
					final Uri iccSdnUri = SimCardUtils.SimUri.getSimSdnUri(mSlot);
					log("iccSdnUri" + iccSdnUri);
					sdnCursor = getContentResolver().query(iccSdnUri, COLUMN_NAMES, null, null, null);
					if (sdnCursor != null) {
						log("sdnCursor.getCount() = " + sdnCursor.getCount());
						try {
							if (sdnCursor.getCount() > 0) {
								actuallyImportOneSimContact(sdnCursor, resolver, mSlot, mSimId, mSimType, null, true);
							}
						} catch (Exception e) {
							log("actuallyImportOneSimContact exception" + e.toString());
						} finally {
							sdnCursor.close();
						}
					}

				}
                
			}
			
			log("[ImportAllSimContactsThread]send finish msg");
			Message msg = mHandler.obtainMessage(mSlot + FINISH_IMPORTING);
			msg.sendToTarget();
			log("[ImportAllSimContactsThread]send finish end");
		}
	}

	private void actuallyImportOneSimContact(final Cursor cursor,
			final ContentResolver resolver, int slot, long simId, int simType, HashSet<Long> insertSimIdSet, boolean importSdnContacts) {

	    AccountTypeManager atm = AccountTypeManager.getInstance(AbstractStartSIMService.this);
	    List<AccountWithDataSet> lac = atm.getAccounts(true);
	    boolean isUsim = (simType == SIM_TYPE_USIM);
	    
	    int accountSlot = -1;
	    AccountWithDataSetEx account = null;
	    for (AccountWithDataSet accountData: lac) {
            if (accountData instanceof AccountWithDataSetEx) {
                AccountWithDataSetEx accountEx = (AccountWithDataSetEx) accountData;
	            accountSlot = accountEx.getSlotId();
	            if (accountSlot == slot) {
	                int accountSimType = (accountEx.type.equals(AccountType.ACCOUNT_TYPE_USIM))?
	                        SIM_TYPE_USIM:SIM_TYPE_SIM;
	                //UIM
	                if (accountEx.type.equals(AccountType.ACCOUNT_TYPE_UIM)){
	                    accountSimType = SIM_TYPE_UIM;
	                }
	                //UIM
	                if (accountSimType == simType) {
	                    account = accountEx;
	                    break;
	                }
	                break;
	            }
	        }
	    }
	    
	    if (account == null) {
//            String accountName = isUsim ? "USIM" + slot : "SIM" + slot;
//            String accountType = isUsim ? AccountType.ACCOUNT_TYPE_USIM
//                    : AccountType.ACCOUNT_TYPE_SIM;
//	        TBD: use default sim name and sim type.
	    }
	    
	    
		final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		int i = 0;
		String additionalNumber = null;
		if (cursor != null) {
			cursor.moveToPosition(-1);
	         /*
             * Bug Fix by Mediatek Begin.
             *   Original Android's code:
             *     xxx
             *   CR ID: ALPS00289127
             *   Descriptions: 
             */
            String countryCode = ContactsUtils.getCurrentCountryIso(AbstractStartSIMService.this);
            Log.i(TAG,"[actuallyImportOneSimContact] countryCode : "+countryCode);
            
            /*
             * Bug Fix by Mediatek End.
             */
			while (cursor.moveToNext()) {
				long indexInSim = cursor.getLong(INDEX_COLUMN); // index in SIM
				int intIndexInSim = (int)indexInSim;
				log("SLOT" + slot + "||indexInSim:" + indexInSim 
						+ "||isInserted:" + (insertSimIdSet==null?false:insertSimIdSet.contains(indexInSim)));
				//Do nothing if sim contacts is already inserted into contacts DB.
				if (insertSimIdSet != null && insertSimIdSet.contains(indexInSim)) {
					continue;
				}

                final NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(
                        cursor.getString(NAME_COLUMN));
				final String name = namePhoneTypePair.name;
				final int phoneType = namePhoneTypePair.phoneType;
				log("phoneType is " + phoneType);
				final String phoneTypeSuffix = namePhoneTypePair.phoneTypeSuffix;
				String phoneNumber = cursor.getString(NUMBER_COLUMN);

				log("indexInSim = " + indexInSim + ", intIndexInSim = " + intIndexInSim 
						+ ", name = " + name + ", number = " + phoneNumber);

				int j = 0;
				
				ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
				ContentValues values = new ContentValues();
				
                /*
                 * New Feature by Mediatek Begin.
                 * M:AAS
                 */
                String accountType = null;
                if (account != null) {
                    accountType = account.type;
                    /*
                     * New Feature by Mediatek End.
                     */
                    values.put(RawContacts.ACCOUNT_NAME, account.name);
                    values.put(RawContacts.ACCOUNT_TYPE, account.type);
                }
				values.put(RawContacts.INDICATE_PHONE_SIM, simId);
				values.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
				values.put(RawContacts.INDEX_IN_SIM, indexInSim);//index in SIM
				
				if (importSdnContacts) {
					values.put(RawContacts.IS_SDN_CONTACT, 1);
				}
				
				builder.withValues(values);
				operationList.add(builder.build());
				j++;
				
				if (!TextUtils.isEmpty(phoneNumber)) {
				    /*
                     * Bug Fix by Mediatek Begin.
                     *   Original Android's code:
                     *     xxx
                     *   CR ID: ALPS00289127
                     *   Descriptions: 
                     */
                    Log.i(TAG,"[actuallyImportOneSimContact] phoneNumber before : "+phoneNumber);
                    AsYouTypeFormatter mFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(countryCode);
                    char [] cha = phoneNumber.toCharArray();
                    int ii = cha.length;
                    for (int num = 0; num <ii; num++){
                        phoneNumber = mFormatter.inputDigit(cha[num]);
                    }
                    Log.i(TAG,"[actuallyImportOneSimContact] phoneNumber after : "+phoneNumber);
                    /*
                     * Bug Fix by Mediatek End.
                     */
					builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
					builder.withValueBackReference(Phone.RAW_CONTACT_ID, i);
					builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
					// builder.withValue(Phone.TYPE, phoneType);
				    /*
				     * New Feature by Mediatek Begin.
				     * M:AAS, op03's primary number doesn't have type.
				     */
                    if (!OperatorUtils.isAasEnabled(accountType)) {
                        builder.withValue(Data.DATA2, 2);
                    }
				    /*
				     * New Feature by Mediatek End.
				     */
					
					// added by mediatek 3.26
					if("OP01".equals(OperatorUtils.getOptrProperties())){
					phoneNumber = phoneNumber.replace(PhoneNumberUtils.PAUSE, 'p')
                    .replace(PhoneNumberUtils.WAIT, 'w');
					} else {
						phoneNumber = phoneNumber.replace('p', PhoneNumberUtils.PAUSE)
	                    .replace('w', PhoneNumberUtils.WAIT);
					}
					builder.withValue(Phone.NUMBER, phoneNumber);
					if (!TextUtils.isEmpty(phoneTypeSuffix)) {
						builder.withValue(Data.DATA15, phoneTypeSuffix);
					}
					operationList.add(builder.build());
					j++;
				}

				if (!TextUtils.isEmpty(name)) {
					builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
					builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, i);
					builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
					builder.withValue(StructuredName.DISPLAY_NAME, name);
					operationList.add(builder.build());
					j++;
				}

				// if USIM
				if (isUsim) {
					log("[actuallyImportOneSimContact]import a USIM contact.");
					//insert USIM email
					final String emailAddresses = cursor.getString(EMAIL_COLUMN);
					log("[actuallyImportOneSimContact]emailAddresses:" + emailAddresses);
					if (!TextUtils.isEmpty(emailAddresses)) {
						final String[] emailAddressArray;
						emailAddressArray = emailAddresses.split(",");
						for (String emailAddress : emailAddressArray) {
							log("emailAddress IS " + emailAddress);
							if (!TextUtils.isEmpty(emailAddress) && !emailAddress.equals("null")) {
								builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
								builder.withValueBackReference(Email.RAW_CONTACT_ID, i);
								builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
								builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
								builder.withValue(Email.DATA, emailAddress);
								operationList.add(builder.build());
								j++;
							}
						}
					}

					//insert USIM additional number
					additionalNumber = cursor.getString(ADDITIONAL_NUMBER_COLUMN);
					log("[actuallyImportOneSimContact]additionalNumber:" + additionalNumber);
					if (!TextUtils.isEmpty(additionalNumber)) {
					    /*
                         * Bug Fix by Mediatek Begin.
                         *   Original Android's code:
                         *     xxx
                         *   CR ID: ALPS00289127
                         *   Descriptions: 
                         */
                        Log.i(TAG,"[actuallyImportOneSimContact] additionalNumber before : "+additionalNumber);
                        AsYouTypeFormatter mFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(countryCode);
                        char [] cha = additionalNumber.toCharArray();
                        int ii = cha.length;
                        for (int num = 0; num <ii; num++){
                            additionalNumber = mFormatter.inputDigit(cha[num]);
                        }
                        Log.i(TAG,"[actuallyImportOneSimContact] additionalNumber after : "+additionalNumber);
                        /*
                         * Bug Fix by Mediatek End.
                         */
						log("additionalNumber is " + additionalNumber);
						builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
						builder.withValueBackReference(Phone.RAW_CONTACT_ID, i);
						builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
						// builder.withValue(Phone.TYPE, phoneType);

                        /*
                         * New Feature by Mediatek Begin. 
                         * M:AAS
                         */
                        if (OperatorUtils.isAasEnabled(accountType)) {
                            int aasColumn = cursor.getColumnIndex("aas");
                            String aas = cursor.getString(aasColumn);
                            builder.withValue(Data.DATA2, aas);
                            builder.withValue(Data.DATA5, aas);
                            Log.i(TAG,"additionalNumber is " + additionalNumber + "; aas is " + aas);
                        } else {
                            /*
                             * New Feature by Mediatek End.
                             */
                            builder.withValue(Data.DATA2, 7);
                        }
						if("OP01".equals(OperatorUtils.getOptrProperties())){
						additionalNumber = additionalNumber.replace(PhoneNumberUtils.PAUSE, 'p')
                        .replace(PhoneNumberUtils.WAIT, 'w');
						} else {
							additionalNumber = additionalNumber.replace('p', PhoneNumberUtils.PAUSE)
		                    .replace('w', PhoneNumberUtils.WAIT);
						}
						
						builder.withValue(Phone.NUMBER, additionalNumber);
						builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
						operationList.add(builder.build());
						j++;
					}
					
					//insert USIM group
					final String ugrpStr = cursor.getString(GROUP_COLUMN);
					log("[actuallyImportOneSimContact]sim group id string: " + ugrpStr);
					if (!TextUtils.isEmpty(ugrpStr)) {
						String[] ugrpIdArray = null;
						if (!TextUtils.isEmpty(ugrpStr)) {
							ugrpIdArray = ugrpStr.split(",");
						}
						for (String ugrpIdStr : ugrpIdArray) {
							int ugrpId = -1;
							try {
								if (!TextUtils.isEmpty(ugrpIdStr))
									ugrpId = Integer.parseInt(ugrpIdStr);
							} catch (Exception e) {
								log("[USIM Group] catched exception");
								e.printStackTrace();
								continue;
							}
							log("[USIM Group] sim group id ugrpId: " + ugrpId);
							if (ugrpId > 0) {
								Integer grpId = grpIdMap.get(ugrpId);
								log("[USIM Group]simgroup mapping group grpId: " + grpId);
								if (grpId == null) {
									Log.e(TAG, "[USIM Group] Error. Catch unhandled "
													+ "SIM group error. ugrp: " + ugrpId);
									continue;
								}
								builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
								builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
								builder.withValue(GroupMembership.GROUP_ROW_ID, grpId);
								builder.withValueBackReference(Phone.RAW_CONTACT_ID, i);
								operationList.add(builder.build());
								j++;
							}
						}
					}
				}
				i = i + j;
				if (i > MAX_OP_COUNT_IN_ONE_BATCH) {
					try {
						//TBD: The deleting and inserting of SIM contacts will be controled 
						//     in the same operation queue in the future. 
						if (!checkSimState(slot)) {
							log("check sim State: false");
							break;
						}
						log("Before applyBatch. sNeedToDelay:" + sNeedToDelay);
						if (sNeedToDelay) {
						    try {
						        Thread.sleep(DELAY_TIME);
						    } catch (java.lang.InterruptedException e) {
						        sNeedToDelay = false;
						    }
						    sNeedToDelay = false;
						}
						log("Before applyBatch. ");
						resolver.applyBatch(ContactsContract.AUTHORITY, operationList);
						log("After applyBatch ");
					} catch (RemoteException e) {
						Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
					} catch (OperationApplicationException e) {
						Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
					}
					i = 0;
					operationList.clear();
				}
			}
			try {
				log("Before applyBatch. sNeedToDelay:" + sNeedToDelay);
				if (sNeedToDelay) {
				    try {
				        Thread.sleep(DELAY_TIME);
				    } catch (java.lang.InterruptedException e) {
				        sNeedToDelay = false;
				    }
				    sNeedToDelay = false;
				}
				log("Before applyBatch ");
				if (checkSimState(slot)) {
                    log("check sim State: true");
                    if (!operationList.isEmpty())
					    resolver.applyBatch(ContactsContract.AUTHORITY, operationList);
				}
				log("After applyBatch ");
			} catch (RemoteException e) {
				Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
			} catch (OperationApplicationException e) {
				Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));

			}
		}
	}
	
	
    private void processDelayMessage(ServiceWorkData workData) {
        int slotId = workData.mSlotId;
        if (slotId == SLOT1) {
            mRefreshTimer.schedule(REFRESH_INTERVAL_MS, workData);
        } else if (slotId == SLOT2) {
            mRefreshTimer2.schedule(REFRESH_INTERVAL_MS_2, workData);
        }
    }
	   
    class RefreshTimer extends Timer {
        private RefreshTimerTask timerTask = null;
        private int mSlotId = -1;
        
        RefreshTimer(int slot) {
            mSlotId = slot;
        }

        protected void clear() {
            timerTask = null;
        }

        protected synchronized void schedule(long delay, ServiceWorkData workData) {
            log("[schedule]slotId:" + mSlotId);
            log("[schedule]timerTask:" + timerTask);
            log("[schedule]delay:" + delay);
            if (timerTask != null) {
                timerTask.updateTaskWork(workData);
            }
            if (delay < 0) {
                sendRemoveSimContactsMsg(workData);
            } else {
                timerTask = new RefreshTimerTask(mSlotId, workData);
                schedule(timerTask, delay);
            }
        }
    }

    class RefreshTimerTask extends TimerTask {
        private int mSlotId = -1;
        private ServiceWorkData mWorkData = null;
        
        RefreshTimerTask(int slot, ServiceWorkData workData) {
            mSlotId = slot;
            mWorkData = workData;
        }
        
        void updateTaskWork (ServiceWorkData workData) {
            mWorkData = workData;
        }
        
        @Override
        public void run() {
            log("[RefreshTimerTask|rum]mSlotId:" + mSlotId);            
            sendRemoveSimContactsMsg(mWorkData);
        }
    }
    
    private void sendLoadSimMsg(ServiceWorkData workData) {
        int slotId = workData.mSlotId;
        log("[sendLoadSimMsg]slotId:" + slotId);
        Message msg = mHandler.obtainMessage(slotId + LOAD_SIM_CONTACTS);
        msg.arg1 = workData.mWorkType;
        msg.obj = workData;
        msg.sendToTarget();
//        if (slotId == SLOT1) {
//            mRefreshTimer.clear();
//        } else if (slotId == SLOT2) {
//            mRefreshTimer2.clear();
//        }
    }
    
    private void sendRemoveSimContactsMsg(ServiceWorkData workData) {
        int slotId = workData.mSlotId;
        log("[sendRemoveSimContactsMsg]slotId:" + slotId);
        Message msg = mHandler.obtainMessage(slotId + REMOVE_OLD);
        msg.obj = workData;
        msg.sendToTarget();
        
        if (slotId == SLOT1) {
            mRefreshTimer.clear();
        } else if (slotId == SLOT2) {
            mRefreshTimer2.clear();
        }
    }
    
    public void sendImportSimContactsMsg(final ServiceWorkData workData) {
        int slotId = workData.mSlotId;
        log("[sendImportSimContactsMsg]slotId:" + slotId);
        Message msg = mHandler.obtainMessage(slotId + IMPORT_NEW);
        msg.obj = workData;
        msg.sendToTarget();
    }
    
    public void sendTaskPoolMsg(final ServiceWorkData workData) {
        int slotId = workData.mSlotId;
        Message msg = mHandler.obtainMessage(slotId + MSG_DELAY_PROCESSING);
        msg.arg1 = workData.mWorkType;
        msg.obj = workData;
        msg.sendToTarget();
    }
    
	private class SimHandler extends Handler {

		public SimHandler() {
			super();
		}
		@Override
		public void handleMessage(Message msg) {
			log("[handleMessage] msg.what = " + msg.what);
			switch (msg.what) {
			case MSG_SLOT1_MSG_DELAY_PROCESSING: {
			    log("[handleMessage]process MSG SLOT1|| sServiceState:" + sServiceState);
			    ServiceWorkData workData = (ServiceWorkData)msg.obj;
			    processDelayMessage(workData);
                break;
            }
			
			case MSG_SLOT2_MSG_DELAY_PROCESSING: {
			    log("[handleMessage]process MSG SLOT2|| sServiceState2:" + sServiceState2);
			    ServiceWorkData workData = (ServiceWorkData)msg.obj;
			    processDelayMessage(workData);
			    break;
			}
			
			case MSG_SLOT1_LOAD: {
			    log("[handleMessage]LOAD SLOT1|| sServiceState:" + sServiceState);
			    if (sServiceState < SERVICE_QUERY_SIM) {
			        sServiceState = SERVICE_QUERY_SIM;
			        ServiceWorkData workData = (ServiceWorkData)msg.obj;
			        int workType = workData.mWorkType;
			        boolean simStateReady = checkSimState(SLOT1) && checkPhoneBookState(SLOT1);
			        log("[handleMessage]LOAD SLOT1|| simStateReady:" + simStateReady);
			        if (simStateReady && workType == SERVICE_WORK_IMPORT) {
			            query((ServiceWorkData)msg.obj);
			        } else {
			            Message message = mHandler.obtainMessage(SLOT1 + FINISH_IMPORTING);
			            message.sendToTarget();
			        }
//			        else {
//			            sendRemoveSimContactsMsg((ServiceWorkData)msg.obj);
//			        }
			    }
			    break;
			}
			
			case MSG_SLOT2_LOAD: {
			    log("[handleMessage]LOAD SLOT2|| sServiceState2:" + sServiceState2);
			    if (sServiceState2 < SERVICE_QUERY_SIM) {
			        sServiceState2 = SERVICE_QUERY_SIM;
			        ServiceWorkData workData = (ServiceWorkData)msg.obj;
                    int workType = workData.mWorkType;
			        boolean simStateReady = checkSimState(SLOT2) && checkPhoneBookState(SLOT2);
			        log("[handleMessage]LOAD SLOT2|| simStateReady:" + simStateReady);
			        if (simStateReady && workType == SERVICE_WORK_IMPORT) { 
			            query((ServiceWorkData)msg.obj);
			        } else {
			            Message message = mHandler.obtainMessage(SLOT2 + FINISH_IMPORTING);
                        message.sendToTarget();
			        }
//			        else {
//			            sendRemoveSimContactsMsg((ServiceWorkData)msg.obj);
//			        }
			    }
			    break;
			}
			
			case MSG_SLOT1_REMOVE_OLD: {
				log("[handleMessage]REMOVE SLOT1|| sServiceState:" + sServiceState);
				if (sServiceState < SERVICE_DELETE_CONTACTS) {
					sServiceState = SERVICE_DELETE_CONTACTS;
					ServiceWorkData workData= (ServiceWorkData)msg.obj;
					deleteSimContact(workData);
				}
				break;
			}
			case MSG_SLOT2_REMOVE_OLD: {
				log("[handleMessage]REMOVE SLOT2|| sServiceState2:" + sServiceState2);
				if (sServiceState2 < SERVICE_DELETE_CONTACTS) {
					sServiceState2 = SERVICE_DELETE_CONTACTS;
					ServiceWorkData workData= (ServiceWorkData)msg.obj;
                    deleteSimContact(workData);
				}
				break;
			}
			case MSG_SLOT1_IMPORT: {
				log("[handleMessage]import SLOT1|| sServiceState:" + sServiceState);
				if (sServiceState < SERVICE_IMPORT_CONTACTS) {
					sServiceState = SERVICE_IMPORT_CONTACTS;
					ServiceWorkData workData= (ServiceWorkData)msg.obj;
					mThread1 = new ImportAllSimContactsThread(workData);
					mThread1.start();
				}
				break;
			}
			
			case MSG_SLOT2_IMPORT: {
				log("[handleMessage]import SLOT2|| sServiceState2:" + sServiceState2);
				if (sServiceState2 < SERVICE_IMPORT_CONTACTS) {
					sServiceState2 = SERVICE_IMPORT_CONTACTS;
                    ServiceWorkData workData= (ServiceWorkData)msg.obj;
					mThread2 = new ImportAllSimContactsThread(workData);
					mThread2.start();
				}
				break;
			}
			
			case MSG_SLOT1_FINISH_IMPORTING: {
                    log("[handleMessage]finish SLOT1|| sServiceState:" + sServiceState);
                    sServiceState = SERVICE_IDLE;
                    boolean reallyFinished1 = true;
                    if (!mSimWorkQueue.isEmpty()) {
                        Log.d(TAG, "SLOT1 mSimWorkQueue: " + mSimWorkQueue.toString());
                        int doneWorkType = mSimWorkQueue.isEmpty() ? -1 : mSimWorkQueue.remove(0);
                        Log.d(TAG, "SLOT1 DoneWorkType: " + doneWorkType);
                        if(doneWorkType == SERVICE_WORK_IMPORT)
                            ContactsUtils.isServiceRunning[mCurrentSimId] = false;
                        if (!mSimWorkQueue.isEmpty()) {
                            int lastWorkType = mSimWorkQueue.get(mSimWorkQueue.size() - 1);
                            mSimWorkQueue.clear();
                            Log.d(TAG, "SLOT1 LastWorkType: " + lastWorkType);
                            if (doneWorkType != lastWorkType) {
                                mSimWorkQueue.add(lastWorkType);

                                ServiceWorkData workData = new ServiceWorkData(lastWorkType, SLOT1,
                                        -1, SIM_TYPE_UNKNOWN, null, SERVICE_IDLE);
                                Message reworkMsg = mHandler.obtainMessage(SLOT1
                                        + MSG_DELAY_PROCESSING);
                                reworkMsg.arg1 = lastWorkType;
                                reworkMsg.obj = workData;
                                reworkMsg.sendToTarget();
                                reallyFinished1 = false;
                            }
                        }
                    }
                    Log.d(TAG, "SLOT1 reallyFinished1: " + reallyFinished1);

                    if (reallyFinished1) {
                        Intent intent = new Intent(ACTION_PHB_LOAD_FINISHED);
                        intent.putExtra("simId", SLOT1);
                        intent.putExtra("slotId", SLOT1);
                        AbstractStartSIMService.this.sendBroadcast(intent);
                        //stopSelf();
                    }
                    log("After stopSelf SLOT1");
                    break;
                }
                case MSG_SLOT2_FINISH_IMPORTING: {
                    log("[handleMessage]finish SLOT2|| sServiceState2:" + sServiceState2);
                    sServiceState2 = SERVICE_IDLE;
                    boolean reallyFinished2 = true;
                    if (!mSimWorkQueue2.isEmpty()) {
                        Log.d(TAG, "SLOT2 mSimWorkQueue2: " + mSimWorkQueue2.toString());
                        int doneWorkType = mSimWorkQueue2.isEmpty() ? -1 : mSimWorkQueue2.remove(0);
                        Log.d(TAG, "SLOT2 DoneWorkType: " + doneWorkType);
                        if(doneWorkType == SERVICE_WORK_IMPORT)
                            ContactsUtils.isServiceRunning[mCurrentSimId] = false;						
                        if (!mSimWorkQueue2.isEmpty()) {
                            int lastWorkType = mSimWorkQueue2.get(mSimWorkQueue2.size() - 1);
                            mSimWorkQueue2.clear();
                            Log.d(TAG, "SLOT2 LastWorkType: " + lastWorkType);
                            if (doneWorkType != lastWorkType) {
                                mSimWorkQueue2.add(lastWorkType);

                                ServiceWorkData workData = new ServiceWorkData(lastWorkType, SLOT2,
                                        -1, SIM_TYPE_UNKNOWN, null, SERVICE_IDLE);
                                Message reworkMsg = mHandler.obtainMessage(SLOT2
                                        + MSG_DELAY_PROCESSING);
                                reworkMsg.arg1 = lastWorkType;
                                reworkMsg.obj = workData;
                                reworkMsg.sendToTarget();
                                reallyFinished2 = false;
                            }
                        }

                    }
                    Log.d(TAG, "SLOT2 reallyFinished2: " + reallyFinished2);
                    if (reallyFinished2) {
                        Intent intent = new Intent(ACTION_PHB_LOAD_FINISHED);
                        intent.putExtra("simId", SLOT2);
                        intent.putExtra("slotId", SLOT2);
                        AbstractStartSIMService.this.sendBroadcast(intent);
                        //stopSelf();
                    }
                    log("After stopSelf SLOT2");
                    break;
                }
            }

        }
	}
	
    private static class NamePhoneTypePair {
        public String name;
        public int phoneType;
        public String phoneTypeSuffix;
        public NamePhoneTypePair(String nameWithPhoneType) {
            // Look for /W /H /M or /O at the end of the name signifying the type
            int nameLen = nameWithPhoneType.length();
            if (nameLen - 2 >= 0 && nameWithPhoneType.charAt(nameLen - 2) == '/') {
                char c = Character.toUpperCase(nameWithPhoneType.charAt(nameLen - 1));
                phoneTypeSuffix = String.valueOf(nameWithPhoneType.charAt(nameLen - 1));
                if (c == 'W') {
                    phoneType = Phone.TYPE_WORK;
                } else if (c == 'M' || c == 'O') {
                    phoneType = Phone.TYPE_MOBILE;
                } else if (c == 'H') {
                    phoneType = Phone.TYPE_HOME;
                } else {
                    phoneType = Phone.TYPE_OTHER;
                }
                name = nameWithPhoneType.substring(0, nameLen - 2);
            } else {
                phoneTypeSuffix = "";
                phoneType = Phone.TYPE_OTHER;
                name = nameWithPhoneType;
            }
        }
    }
    
    public static int getServiceState(int slotId) {
        if (slotId == 0) {
            return sServiceState;
        } else {
            return sServiceState2;
        }
    }
    
    public static boolean isServiceRunning(int slotId) {
        if (slotId == 0) {
            return sServiceState != SERVICE_IDLE;
        } else {
            return sServiceState2 != SERVICE_IDLE;
        }
    }
    
	private void log(String msg) {
	    if (DBG) {
	        Log.d(TAG, msg);
	    }
	}
}

