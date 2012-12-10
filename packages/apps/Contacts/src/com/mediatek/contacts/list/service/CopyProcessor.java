package com.mediatek.contacts.list.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContentProviderOperation.Builder;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import android.os.PowerManager;
import android.os.ServiceManager;

import com.android.contacts.model.AccountType;
import com.android.contacts.vcard.ProcessorBase;
import com.mediatek.contacts.Anr;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.android.internal.telephony.ITelephony;
import com.mediatek.contacts.list.MultiContactsDuplicationFragment;
import android.provider.Telephony.SIMInfo;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.ErrorCause;
import com.mediatek.contacts.util.ContactsGroupUtils.USIMGroup;
import com.mediatek.contacts.util.ContactsGroupUtils.USIMGroupException;
import com.mediatek.contacts.util.ContactsGroupUtils.USIMAas;
import com.mediatek.contacts.util.OperatorUtils;

public class CopyProcessor extends ProcessorBase {

    private static final String LOG_TAG = MultiContactsDuplicationFragment.TAG;
    private static final boolean DEBUG = MultiContactsDuplicationFragment.DEBUG;

    private final MultiChoiceService mService;
    private final ContentResolver mResolver;
    private final List<MultiChoiceRequest> mRequests;
    private final int mJobId;
    private final MultiChoiceHandlerListener mListener;

    private PowerManager.WakeLock mWakeLock;

    private final Account mAccountSrc;
    private final Account mAccountDst;

    private volatile boolean mCanceled = false;
    private volatile boolean mDone = false;
    private volatile boolean mIsRunning = false;
    
    private static final int MAX_OP_COUNT_IN_ONE_BATCH = 400;
    private static int RETRYCOUNT = 20;

    public CopyProcessor(final MultiChoiceService service,
            final MultiChoiceHandlerListener listener, final List<MultiChoiceRequest> requests,
            final int jobId, final Account sourceAccount, final Account destinationAccount) {
        mService = service;
        mResolver = mService.getContentResolver();
        mListener = listener;

        mRequests = requests;
        mJobId = jobId;
        mAccountSrc = sourceAccount;
        mAccountDst = destinationAccount;

        final PowerManager powerManager = (PowerManager) mService.getApplicationContext()
                .getSystemService("power");
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE, LOG_TAG);
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (DEBUG)
            Log.d(LOG_TAG, "CopyProcessor received cancel request");
        if (mDone || mCanceled) {
            return false;
        }
        mCanceled = true;
        if (!mIsRunning) {
            mService.handleFinishNotification(mJobId, false);
            mListener.onCanceled(MultiChoiceService.TYPE_COPY, mJobId, -1, -1, -1);
        }
        return true;
    }

    @Override
    public int getType() {
        return MultiChoiceService.TYPE_COPY;
    }

    @Override
    public synchronized boolean isCancelled() {
        return mCanceled;
    }

    @Override
    public synchronized boolean isDone() {
        return mDone;
    }

    @Override
    public void run() {
        try {
            mIsRunning = true;
            mWakeLock.acquire();
            //UIM
//            if (AccountType.ACCOUNT_TYPE_SIM.equals(mAccountDst.type)
//                    || AccountType.ACCOUNT_TYPE_USIM.equals(mAccountDst.type)) {
            if (AccountType.ACCOUNT_TYPE_SIM.equals(mAccountDst.type)
                    || AccountType.ACCOUNT_TYPE_USIM.equals(mAccountDst.type) || AccountType.ACCOUNT_TYPE_UIM.equals(mAccountDst.type)) {
                
            //UIM
                //copyContactsToSim();
                copyContactsToSimWithRadioStateCheck();
            } else {
                copyContactsToAccount();
            }
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "RuntimeException thrown during copy", e);
            throw e;
        } finally {
            synchronized (this) {
                mDone = true;
            }
            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    private void copyContactsToSim() {
        int errorCause = ErrorCause.NO_ERROR;

        // Process sim data, sim id or slot
        AccountWithDataSetEx account = (AccountWithDataSetEx) mAccountDst;
        Log.d(LOG_TAG, "[copyContactsToSim]AccountName:" + account.name
                + "|accountType:" + account.type);
        int dstSlotId = account.getSlotId();
        SIMInfo dstSimInfo = SIMInfoWrapper.getDefault().getSimInfoBySlot(dstSlotId);
        long dstSimId = dstSimInfo.mSimId;
        Log.d(LOG_TAG, "[copyContactsToSim]dstSlotId:" + dstSlotId + "|dstSimId:" + dstSimId);
        boolean isTargetUsim = SimCardUtils.isSimUsimType(dstSlotId);
        String dstSimType = isTargetUsim ? "USIM" : "SIM";
        Log.d(LOG_TAG, "[copyContactsToSim]dstSimType:" + dstSimType);

        if (!isSimReady(dstSlotId) && !isPhoneBookReady(dstSlotId)) {
            errorCause = ErrorCause.SIM_NOT_READY;
            mService.handleFinishNotification(mJobId, false);
            mListener.onFailed(MultiChoiceService.TYPE_COPY, mJobId, mRequests.size(),
                    0, mRequests.size(), errorCause);
            return;
        }

        ArrayList<String> numberArray = new ArrayList<String>();
        ArrayList<String> additionalNumberArray = new ArrayList<String>();
        ArrayList<String> emailArray = new ArrayList<String>();
        /*
         * New Feature by Mediatek Begin.
         * M:AAS,
         *  phoneTypeArray used to record number's type.
         *  anrTypeArray used to record additional number's type.
         */
        ArrayList<Integer> phoneTypeArray = new ArrayList<Integer>();
        ArrayList<Integer> anrTypeArray = new ArrayList<Integer>();
        /*
         * New Feature by Mediatek End.
         */
        String targetName = null;

         ContentResolver resolver = this.mResolver;

// The following lines are provided and maintained by Mediatek inc.
// Keep previous code here.
// Description: 
//    The following code is used to do copy group data to usim. However, it also needs
//        to implement function that can copy group data in different account before 
//        using the following code.        
//
// Previous Code:        
//    HashMap<Integer, String> grpIdNameCache = new HashMap<Integer, String>();
//    HashMap<String, Integer> ugrpNameIdCache = new HashMap<String, Integer>();
//    HashSet<Long> grpIdSet = new HashSet<Long>();
//    ArrayList<Integer> ugrpIdArray = new ArrayList<Integer>();
//    Cursor groupCursor = resolver.query(Groups.CONTENT_SUMMARY_URI, 
//            new String[] {Groups._ID, Groups.TITLE}, 
//            Groups.DELETED + "=0 AND " 
//                + Groups.ACCOUNT_NAME + "='" + mAccountSrc.name + "' AND "
//                + Groups.ACCOUNT_TYPE + "='" + mAccountSrc.type + "'", 
//            null, null);
//    try {
//        while (groupCursor.moveToNext()) {
//            int gId = groupCursor.getInt(0);
//            String gTitle = groupCursor.getString(1);
//            grpIdNameCache.put(gId, gTitle);
//            Log.d(LOG_TAG, "[USIM Group]cache phone group. gId:" + gId + "|gTitle:" + gTitle);
//        }
//    } finally {
//        if (groupCursor != null)
//            groupCursor.close();
//    }
// The previous lines are provided and maintained by Mediatek inc.        

        // Process request one by one
        int totalItems = mRequests.size();
        int successfulItems = 0;
        int currentCount = 0;

        boolean isSimStorageFull = false;
        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        for (MultiChoiceRequest request : this.mRequests) {
            if (mCanceled) {
                break;
            }
            if (!isSimReady(dstSlotId) || !isPhoneBookReady(dstSlotId)) {
                Log.d(LOG_TAG, "copyContactsToSim run: sim not ready");
                errorCause = ErrorCause.ERROR_UNKNOWN;
                break;
            }
            currentCount++;
            // Notify the copy process on notification bar
            mListener.onProcessed(MultiChoiceService.TYPE_COPY, mJobId, currentCount, totalItems,
                    request.mContactName);

            // reset data
            numberArray.clear();
            additionalNumberArray.clear();
            
            /*
             * New Feature by Mediatek Begin.
             * M:AAS
             */
            phoneTypeArray.clear();
            anrTypeArray.clear();
            /*
             * New Feature by Mediatek End.
             */
            emailArray.clear();
            targetName = null;

            int contactId = request.mContactId;

            // Query to get all src data resource.
            Uri dataUri = Uri.withAppendedPath(
                    ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
                    Contacts.Data.CONTENT_DIRECTORY);
            final String[] projection = new String[] {
                    Contacts._ID, 
                    Contacts.Data.MIMETYPE, 
                    Contacts.Data.DATA1,
                    Contacts.Data.IS_ADDITIONAL_NUMBER,
                    ///M:AAS,4. DATA2&DATA5 is aas index(OP03) but DATA2 is phone type(common).
                    Contacts.Data.DATA2
            };
            Cursor c = resolver.query(dataUri, projection, null, null, null);
            
            if (c != null && c.moveToFirst()) {
                do {
                    String mimeType = c.getString(1);
                    if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        // For phone number
                        String number = c.getString(2);
                        String isAdditionalNumber = c.getString(3);
                        /*
                         * New Feature by Mediatek Begin.
                         * Original Android's code: xxx
                         * M:AAS
                         */
                        int aas = c.getInt(4);
                        if (isAdditionalNumber != null && isAdditionalNumber.equals("1")) {
                            additionalNumberArray.add(number);
                            if(OperatorUtils.isOp03Enabled()) {
                                anrTypeArray.add(aas);
                            }
                        } else {
                            numberArray.add(number);
                            if (OperatorUtils.isOp03Enabled()) {
                                phoneTypeArray.add(aas);
                            }
                        }
                        /*
                         * New Feature by Mediatek End.
                         */
                    } else if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        // For name
                        targetName = c.getString(2);
                    }
                    if (isTargetUsim) {
                        if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            // For email
                            String email = c.getString(2);
                            emailArray.add(email);
                        } else if (GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            
// The following lines are provided and maintained by Mediatek inc.
// Keep previous code here.
// Description: 
//        The following code is used to do copy group data to usim.
//
// Previous Code:                              
//    // For group
//    int grpId = c.getInt(1);
//    // Check group id. Here Do not process unknown group.
//    if (grpIdNameCache.containsKey(grpId)) {
//        String grpName = grpIdNameCache.get(grpId);
//        int ugrpId = -1;
//        if (ugrpNameIdCache.containsKey(grpName))
//            ugrpId = ugrpNameIdCache.get(grpName);
//        else {
//            try {
//                USIMGroup.syncUSIMGroupDelIfNoMember(mService, dstSlotId,
//                        (int) dstSimId);
//                ugrpId = USIMGroup.syncUSIMGroupNewIfMissing(dstSlotId,
//                        grpName);
//            } catch (android.os.RemoteException e) {
//                ugrpId = -1;
//                errorCause = ErrorCause.ERROR_UNKNOWN;
//                e.printStackTrace();
//            } catch (USIMGroupException e) {
//                ugrpId = -1;
//                errorCause += e.getErrorType();
//                if (e.getErrorType() == USIMGroupException.GROUP_NAME_OUT_OF_BOUND) {
//                    errorCause = ErrorCause.USIM_GROUP_NAME_OUT_OF_BOUND;
//                } else if (e.getErrorType() == USIMGroupException.GROUP_NUMBER_OUT_OF_BOUND) {
//                    errorCause = ErrorCause.USIM_GROUP_NUMBER_OUT_OF_BOUND;
//                } else {
//                    errorCause = ErrorCause.ERROR_UNKNOWN;
//                }
//                e.printStackTrace();
//            }
//            ugrpNameIdCache.put(grpName, ugrpId);
//        }
//        if (ugrpId > 0) {
//            grpIdSet.add((long) grpId);
//            ugrpIdArray.add(ugrpId);
//        }
//    }
// The previous lines are provided and maintained by Mediatek inc.                              
                        }
                    }
                } while (c.moveToNext());
            }
            if (c != null)
                c.close();

            // copy new resournce to target sim or usim,
            // and insert into database if sucessful
            Uri dstSimUri = SimCardUtils.SimUri.getSimUri(dstSlotId);
            int maxCount = TextUtils.isEmpty(targetName) ? 0 : 1;
            /*
             * New Feature by Mediatek Begin.
             * M:AAS, so far its value is 1;
             */
            int maxAnrCount = USIMAas.getAnrCount(dstSlotId);
            /*
             * New Feature by Mediatek End.
             */
            
            if (isTargetUsim) {
                int numberCount = numberArray.size();
                int additionalCount = additionalNumberArray.size();
                int emailCount = emailArray.size();
                
                maxCount = (maxCount > additionalCount) ? maxCount : additionalCount;
                maxCount = (maxCount > emailCount) ? maxCount : emailCount;
                /*
                 * New Feature by Mediatek Begin.
                 * Original Android's code: xxx
                 * M:AAS
                 */
                int numberQuota;
                if (OperatorUtils.isOp03Enabled()) {
                    numberQuota = (int) ((numberCount + additionalCount) / (1.0 + maxAnrCount) + (float) maxAnrCount
                            / (1.0 + maxAnrCount));
                    Log.i(LOG_TAG, "maxAnr=" + maxAnrCount + "; numberQuota=" + numberQuota);
                } else {
                    numberQuota = (int) ((numberCount + additionalCount) / 2.0 + 0.5);
                }
                
                /*
                 * New Feature by Mediatek End.
                 */
                
                maxCount = maxCount > numberQuota ? maxCount : numberQuota;
            } else {
                numberArray.addAll(additionalNumberArray);
                additionalNumberArray.clear();
                int numberCount = numberArray.size();
                maxCount = maxCount > numberCount ? maxCount : numberCount;
            }
            int sameNameCount = 0;
            ContentValues values = new ContentValues();
            String simTag = null;
            String simNum = null;
            String simAnrNum = null;
            String simEmail = null;

            simTag = sameNameCount > 0 ? (targetName + sameNameCount) : targetName;
            simTag = TextUtils.isEmpty(simTag) ? "" : simTag;
            if ((simTag == null || simTag.isEmpty() || simTag.length() == 0)
                    && numberArray.isEmpty()) {
                Log.e(LOG_TAG, " name and number are empty");
                errorCause = ErrorCause.ERROR_UNKNOWN;
                continue;
            }

            int subContact = 0;
            /*
             * New Feature by Mediatek Begin.
             * M:AAS
             */
            ArrayList<Anr> anrsList = new ArrayList<Anr>();
            /*
             * New Feature by Mediatek End.
             */
            for (int i = 0; i < maxCount; i++) {
                values.put("tag", simTag);
                Log.d(LOG_TAG, "copyContactsToSim tag is " + simTag);
                simNum = null;
                simAnrNum = null;
                simEmail = null;
                if (!numberArray.isEmpty()) {
                    simNum = numberArray.remove(0);
                    simNum = TextUtils.isEmpty(simNum) ? "" : simNum.replace("-", "");
                    values.put("number", PhoneNumberUtils.stripSeparators(simNum));
                    Log.d(LOG_TAG, "copyContactsToSim number is " + simNum);
                    /*
                     * New Feature by Mediatek Begin.
                     * M:AAS, correspond to numberArray.remove(0).
                     */
                    if (OperatorUtils.isOp03Enabled() && !phoneTypeArray.isEmpty()) {
                        phoneTypeArray.remove(0);
                    }
                    /*
                     * New Feature by Mediatek End.
                     */
                    
                }
                
                /*
                 * New Feature by Mediatek Begin.
                 * M:AAS
                 */
                anrsList.clear();
                Context context = mService.getApplicationContext();
                /*
                 * New Feature by Mediatek End.
                 */
                if (isTargetUsim) {
                    Log.d(LOG_TAG, "copyContactsToSim copy to USIM");
                    if (!additionalNumberArray.isEmpty()) {
                        /*
                         * New Feature by Mediatek Begin.
                         * M:AAS,save aas info to Usim card(Op03). So far, it only supports 1 anr.
                         * TODO:support multi-anrs in the feature.
                         */
                        if (OperatorUtils.isAasEnabled(mAccountDst.type)) {
                            buildAnrValuesForSim(context, additionalNumberArray, anrTypeArray,
                                    maxAnrCount, dstSlotId, values, anrsList);
                        } else {
                            Log.d(LOG_TAG, "additional number array is not empty");
                            simAnrNum = additionalNumberArray.remove(0);
                            simAnrNum = TextUtils.isEmpty(simAnrNum) ? "" : simAnrNum.replace("-",
                                    "");
                            values.put("anr", PhoneNumberUtils.stripSeparators(simAnrNum));
                            Log.d(LOG_TAG, "copyContactsToSim anr is " + simAnrNum);
                        }
                        /*
                         * New Feature by Mediatek End.
                         */
                    } else if (!numberArray.isEmpty()) {
                        /*
                         * New Feature by Mediatek Begin.
                         * M:AAS,save aas info to Usim card(Op03). So far, it only supports 1 anr.
                         * TODO:support multi-anrs in the feature.
                         */
                        if (OperatorUtils.isAasEnabled(mAccountDst.type)) {
                            buildAnrValuesForSim(context, numberArray, phoneTypeArray,
                                    maxAnrCount, dstSlotId, values, anrsList);
                        } else {
                            Log.d(LOG_TAG, "additional number array is empty and fill it with ADN number");
                            simAnrNum = numberArray.remove(0);
                            simAnrNum = TextUtils.isEmpty(simAnrNum) ? "" : simAnrNum.replace("-", "");
                            values.put("anr", PhoneNumberUtils.stripSeparators(simAnrNum));
                            Log.d(LOG_TAG, "copyContactsToSim anr is " + simAnrNum);
                        }
                        /*
                         * New Feature by Mediatek End.
                         */
                    }

                    if (!emailArray.isEmpty()) {
                        simEmail = emailArray.remove(0);
                        simEmail = TextUtils.isEmpty(simEmail) ? "" : simEmail;
                        values.put("emails", simEmail);
                        Log.d(LOG_TAG, "copyContactsToSim emails is " + simEmail);
                    }
                }
                Log.i(LOG_TAG, "Before insert Sim card.");
                Uri retUri = resolver.insert(dstSimUri, values);
                Log.i(LOG_TAG, "After insert Sim card.");

                Log.i(LOG_TAG, "retUri is " + retUri);
                if (retUri != null) {
                    List<String> checkUriPathSegs = retUri.getPathSegments();
                    if ("error".equals(checkUriPathSegs.get(0))) {
                        String errorCode = checkUriPathSegs.get(1);
                        Log.i(LOG_TAG, "error code = " + errorCode);
                        if (DEBUG) {
                            printSimErrorDetails(errorCode);
                        }
                        if (errorCause != ErrorCause.ERROR_USIM_EMAIL_LOST) {
                            errorCause = ErrorCause.ERROR_UNKNOWN;
                        }
                        if ("-3".equals(checkUriPathSegs.get(1))) {
                            errorCause = ErrorCause.SIM_STORAGE_FULL;
                            isSimStorageFull = true;
                            Log.e(LOG_TAG, "Fail to insert sim contacts fail"
                                    + " because sim storage is full.");
                            break;
                        } else if ("-12".equals(checkUriPathSegs.get(1))) {
                            errorCause = ErrorCause.ERROR_USIM_EMAIL_LOST;
                            Log.e(LOG_TAG, "Fail to save USIM email "
                                    + " because emial slot is full in USIM.");
                            Log.d(LOG_TAG, "Ignore this error and "
                                    + "remove the email address to save this item again");
                            values.remove("emails");
                            retUri = resolver.insert(dstSimUri, values);
                            Log.d(LOG_TAG, "[Save Again]The retUri is " + retUri);
                            if (retUri != null && ("error".equals(retUri.getPathSegments().get(0)))) {
                                if ("-3".equals(retUri.getPathSegments().get(1))) {
                                    errorCause = ErrorCause.SIM_STORAGE_FULL;
                                    isSimStorageFull = true;
                                    Log.e(LOG_TAG, "Fail to insert sim contacts fail"
                                            + " because sim storage is full.");
                                    break;
                                }
                            }
                            if (retUri != null && !("error".equals(retUri.getPathSegments().get(0)))) {
                                long indexInSim = ContentUris.parseId(retUri);
                                /*
                                 * New Feature by Mediatek Begin.
                                 * M:AAS
                                 * TODO:support multi-anrs in the feature.
                                 */
                                if (OperatorUtils.isAasEnabled(mAccountDst.type)) {
                                    SubContactsUtils.buildInsertOperation(operationList,
                                            mAccountDst, simTag, simNum, null, null/* simAnrNum */,
                                            resolver, dstSimId, dstSimType, indexInSim, null,
                                            anrsList);
                                /*
                                 * New Feature by Mediatek End.
                                 */
                                } else {
                                    SubContactsUtils.buildInsertOperation(operationList,
                                            mAccountDst, simTag, simNum, null, simAnrNum, resolver,
                                            dstSimId, dstSimType, indexInSim, null);
                                }
                                subContact++;
                            }
                        }
                    } else {
                        Log.d(LOG_TAG, "insertUsimFlag = true");
                        long indexInSim = ContentUris.parseId(retUri);

                        /*
                         * New Feature by Mediatek Begin.
                         * M:AAS
                         * TODO:support multi-anrs in the feature.
                         */
                        if (OperatorUtils.isAasEnabled(mAccountDst.type)) {
                            SubContactsUtils.buildInsertOperation(operationList, mAccountDst,
                                    simTag, simNum, simEmail, null/* simAnrNum */, resolver, dstSimId,
                                    dstSimType, indexInSim, null, anrsList);
                        /*
                         * New Feature by Mediatek End.
                         */
                        } else {
                            SubContactsUtils.buildInsertOperation(operationList, mAccountDst,
                                    simTag, simNum, simEmail, simAnrNum, resolver, dstSimId,
                                    dstSimType, indexInSim, null);
                        }
                        subContact++;
                        //successfulItems++;
                    }
                } else {
                    errorCause = ErrorCause.ERROR_UNKNOWN;
                }
                if (operationList.size() > MAX_OP_COUNT_IN_ONE_BATCH) {
                    try {
                        Log.i(LOG_TAG, "Before applyBatch. ");
                        resolver.applyBatch(ContactsContract.AUTHORITY, operationList);
                        Log.i(LOG_TAG, "After applyBatch ");
                    } catch (android.os.RemoteException e) {
                        Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    } catch (android.content.OperationApplicationException e) {
                        Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    }
                    operationList.clear();
                }
            }// inner looper
            if (subContact > 0) {
                successfulItems ++;
            }
            if (isSimStorageFull)
                break;
        }
        
        if (operationList.size() > 0) {
            try {
                Log.i(LOG_TAG, "Before end applyBatch. ");
                resolver.applyBatch(ContactsContract.AUTHORITY, operationList);
                Log.i(LOG_TAG, "After end applyBatch ");
            } catch (android.os.RemoteException e) {
                Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            } catch (android.content.OperationApplicationException e) {
                Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            }
            operationList.clear();
        }
        
        if (mCanceled) {
            Log.d(LOG_TAG, "copyContactsToSim run: mCanceled = true");
            errorCause = ErrorCause.USER_CANCEL;
            mService.handleFinishNotification(mJobId, false);
            mListener.onCanceled(MultiChoiceService.TYPE_COPY, mJobId, totalItems,
                    successfulItems, totalItems - successfulItems);
            return;
        }
        
        mService.handleFinishNotification(mJobId, errorCause == ErrorCause.NO_ERROR);
        if (errorCause == ErrorCause.NO_ERROR) {
            mListener.onFinished(MultiChoiceService.TYPE_COPY, mJobId, totalItems);
        } else {
            mListener.onFailed(MultiChoiceService.TYPE_COPY, mJobId, totalItems,
                    successfulItems, totalItems - successfulItems, errorCause);
        }
    }

    private boolean isSimReady(int slot) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        if (null == iTel)
            return false;

        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (DEBUG) {
                    Log.i(LOG_TAG, "iTel.hasIccCardGemini(simId) is "
                            + iTel.hasIccCardGemini(slot));
                    Log.i(LOG_TAG, "iTel.isRadioOnGemini(simId) is " + iTel.isRadioOnGemini(slot));
                    Log.i(LOG_TAG, "iTel.isFDNEnabledGemini(simId) is "
                            + iTel.isFDNEnabledGemini(slot));
                    Log.i(LOG_TAG, "getSimStateGemini(simId) "
                            + (TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault()
                                    .getSimStateGemini(slot)));
                }
                return iTel.hasIccCardGemini(slot)
                        && iTel.isRadioOnGemini(slot)
                        && !iTel.isFDNEnabledGemini(slot)
                        && TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault()
                                .getSimStateGemini(slot);
            } else { // Single SIM
                return iTel.hasIccCard()
                        && iTel.isRadioOn()
                        && !iTel.isFDNEnabled()
                        && TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault()
                                .getSimState();
            }
        } catch (android.os.RemoteException e) {
            Log.w(LOG_TAG, "RemoteException!");
            return false;
        }
    }

    private boolean isPhoneBookReady(int slot) {
        Log.i(LOG_TAG, "isPhoneBookReady " + SimCardUtils.isPhoneBookReady(slot));
        return SimCardUtils.isPhoneBookReady(slot);
    }

    final static String[] DATA_ALLCOLUMNS = new String[] {
        Data._ID,
        Data.MIMETYPE,
        Data.IS_PRIMARY,
        Data.IS_SUPER_PRIMARY,
        Data.DATA1,
        Data.DATA2,
        Data.DATA3,
        Data.DATA4,
        Data.DATA5,
        Data.DATA6,
        Data.DATA7,
        Data.DATA8,
        Data.DATA9,
        Data.DATA10,
        Data.DATA11,
        Data.DATA12,
        Data.DATA13,
        Data.DATA14,
        Data.DATA15,
        Data.SYNC1,
        Data.SYNC2,
        Data.SYNC3,
        Data.SYNC4,
        Data.IS_ADDITIONAL_NUMBER
    };

    private void copyContactsToAccount() {
        Log.d(LOG_TAG, "copyContactsToAccount");
        if (mCanceled) {
            return;
        }
        /*
         * New Feature by Mediatek Begin.
         * Original Android's code: xxx
         * M:AAS
         */
        AccountWithDataSetEx account = (AccountWithDataSetEx) mAccountDst;
        int slotId = account.getSlotId();
        Log.i(LOG_TAG, "copyContactsToAccount, slotId=" + slotId);
        Log.i(LOG_TAG, "copyContactsToAccount, mAccountSrc.type=" + mAccountSrc.type);
        boolean isSrcUSim = false;
        if (AccountType.ACCOUNT_TYPE_USIM.equals(mAccountSrc.type)) {
            isSrcUSim = true;
        }
        /*
         * New Feature by Mediatek End.
         */
        int successfulItems = 0;
        int currentCount = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (MultiChoiceRequest request : this.mRequests) {
            sb.append(String.valueOf(request.mContactId));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        Log.d(LOG_TAG, "copyContactsToAccount contactIds " + sb.toString() + " ");
        Cursor rawContactsCursor = mResolver.query(
                RawContacts.CONTENT_URI, 
                new String[] {RawContacts._ID, RawContacts.DISPLAY_NAME_PRIMARY}, 
                RawContacts.CONTACT_ID + " IN " + sb.toString(), 
                null, null);
        
        int totalItems = rawContactsCursor == null? 0 : rawContactsCursor.getCount();

        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        // Process request one by one
        if (rawContactsCursor != null) {
            Log.d(LOG_TAG, "copyContactsToAccount: rawContactsCursor.size = " + rawContactsCursor.getCount());

            long nOldRawContactId;
            while (rawContactsCursor.moveToNext()) {
                if (mCanceled) {
                    Log.d(LOG_TAG, "runInternal run: mCanceled = true");
                    break;
                }
                currentCount++;
                String displayName = rawContactsCursor.getString(1);

                mListener.onProcessed(MultiChoiceService.TYPE_COPY, mJobId,
                        currentCount, totalItems, displayName);

                nOldRawContactId = rawContactsCursor.getLong(0);

                Cursor dataCursor = mResolver.query(Data.CONTENT_URI, 
                        DATA_ALLCOLUMNS, Data.RAW_CONTACT_ID + "=? ", 
                        new String[] { String.valueOf(nOldRawContactId) }, null);
                if (dataCursor == null || dataCursor.getCount() <= 0) {
                    continue;
                }
                
                int backRef = operationList.size();
                ContentProviderOperation.Builder builder = ContentProviderOperation
                        .newInsert(RawContacts.CONTENT_URI);
                if (!TextUtils.isEmpty(mAccountDst.name)
                        && !TextUtils.isEmpty(mAccountDst.type)) {
                    builder.withValue(RawContacts.ACCOUNT_NAME,mAccountDst.name);
                    builder.withValue(RawContacts.ACCOUNT_TYPE,mAccountDst.type);
                } else {
                    builder.withValues(new ContentValues());
                }
                builder.withValue(RawContacts.AGGREGATION_MODE,RawContacts.AGGREGATION_MODE_DISABLED);
                operationList.add(builder.build());
                
                dataCursor.moveToPosition(-1);
                String[] columnNames = dataCursor.getColumnNames();
                while (dataCursor.moveToNext()) {
                    //do not copy group data between different account.
                    String mimeType = dataCursor.getString(dataCursor.getColumnIndex(Data.MIMETYPE));
                    Log.i(LOG_TAG, "mimeType:" + mimeType);
                    if (GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType))
                        continue;
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    /*
                     * New Feature by Mediatek Begin.
                     * Original Android's code: xxx
                     * M:AAS
                     */
                    generateDataBuilder(dataCursor, builder, columnNames, mimeType, slotId, mAccountSrc.type);
                    /*
                     * New Feature by Mediatek End.
                     */
                    builder.withValueBackReference(Data.RAW_CONTACT_ID, backRef);
                    operationList.add(builder.build());
                }
                dataCursor.close();
                successfulItems++;
                if (operationList.size() > MAX_OP_COUNT_IN_ONE_BATCH) {
                    try {
                        Log.i(LOG_TAG, "Before applyBatch. ");
                        mResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
                        Log.i(LOG_TAG, "After applyBatch ");
                    } catch (android.os.RemoteException e) {
                        Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    } catch (android.content.OperationApplicationException e) {
                        Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    }
                    operationList.clear();
                }
            }
            rawContactsCursor.close();
            if (operationList.size() > 0) {
                try {
                    Log.i(LOG_TAG, "Before end applyBatch. ");
                    mResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
                    Log.i(LOG_TAG, "After end applyBatch ");
                } catch (android.os.RemoteException e) {
                    Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                } catch (android.content.OperationApplicationException e) {
                    Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                }
                operationList.clear();
            }
            if (mCanceled) {
                Log.d(LOG_TAG, "runInternal run: mCanceled = true");
                mService.handleFinishNotification(mJobId, false);
                mListener.onCanceled(MultiChoiceService.TYPE_COPY, mJobId, totalItems,
                        successfulItems, totalItems - successfulItems);
                if (rawContactsCursor != null && !rawContactsCursor.isClosed()) {
                    rawContactsCursor.close();
                }
                return;
            }
        }

        mService.handleFinishNotification(mJobId, successfulItems == totalItems);
        if (successfulItems == totalItems) {
            mListener.onFinished(MultiChoiceService.TYPE_COPY, mJobId, totalItems);
        } else {
            mListener.onFailed(MultiChoiceService.TYPE_COPY, mJobId, totalItems,
                    successfulItems, totalItems - successfulItems);
        }

        Log.d(LOG_TAG, "copyContactsToAccount: end");
    }

    private void cursorColumnToBuilder(Cursor cursor, String [] columnNames, int index, ContentProviderOperation.Builder builder) {
        switch (cursor.getType(index)) {
            case Cursor.FIELD_TYPE_NULL:
                // don't put anything in the content values
                break;
            case Cursor.FIELD_TYPE_INTEGER:
                builder.withValue(columnNames[index], cursor.getLong(index));
                break;
            case Cursor.FIELD_TYPE_STRING:
                builder.withValue(columnNames[index], cursor.getString(index));
                break;
            case Cursor.FIELD_TYPE_BLOB:    
                builder.withValue(columnNames[index], cursor.getBlob(index));
                break;
            default:
                throw new IllegalStateException("Invalid or unhandled data type");
        }
    }
    
    private void printSimErrorDetails(String errorCode) {
        int iccError = Integer.valueOf(errorCode);
        switch (iccError) {
            case ErrorCause.SIM_NUMBER_TOO_LONG:
                Log.d(LOG_TAG, "ERROR PHONE NUMBER TOO LONG");
                break;
            case ErrorCause.SIM_NAME_TOO_LONG:
                Log.d(LOG_TAG, "ERROR NAME TOO LONG");
                break;
            case ErrorCause.SIM_STORAGE_FULL:
                Log.d(LOG_TAG, "ERROR STORAGE FULL");
                break;
            case ErrorCause.SIM_ICC_NOT_READY:
                Log.d(LOG_TAG, "ERROR ICC NOT READY");
                break;
            case ErrorCause.SIM_PASSWORD_ERROR:
                Log.d(LOG_TAG, "ERROR ICC PASSWORD ERROR");
                break;
            case ErrorCause.SIM_ANR_TOO_LONG:
                Log.d(LOG_TAG, "ERROR ICC ANR TOO LONG");
                break;
            case ErrorCause.SIM_GENERIC_FAILURE:
                Log.d(LOG_TAG, "ERROR ICC GENERIC FAILURE");
                break;
            case ErrorCause.SIM_ADN_LIST_NOT_EXIT:
                Log.d(LOG_TAG, "ERROR ICC ADN LIST NOT EXIST");
                break;
            case ErrorCause.ERROR_USIM_EMAIL_LOST:
                Log.d(LOG_TAG, "ERROR ICC USIM EMAIL LOST");
                break;
            default:
                Log.d(LOG_TAG, "ERROR ICC UNKNOW");
                break;
        }
    }

    private void copyContactsToSimWithRadioStateCheck() {
        if (mCanceled) {
            return;
        }

        int errorCause = ErrorCause.NO_ERROR;

        AccountWithDataSetEx account = (AccountWithDataSetEx) mAccountDst;
        Log.d(LOG_TAG, "[copyContactsToSimWithRadioCheck]AccountName: " + account.name
                + " | accountType: " + account.type);
        int dstSlotId = account.getSlotId();
        if (!isSimReady(dstSlotId)) {
            errorCause = ErrorCause.SIM_NOT_READY;
            mService.handleFinishNotification(mJobId, false);
            mListener.onFailed(MultiChoiceService.TYPE_COPY, mJobId, mRequests.size(),
                    0, mRequests.size(), errorCause);
            return;
        }

        if (!isPhoneBookReady(dstSlotId)) {
            int i = 0;
            while (i++ < RETRYCOUNT) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (isPhoneBookReady(dstSlotId)) {
                    break;
                }
            }
        }
        if (!isPhoneBookReady(dstSlotId)) {
            errorCause = ErrorCause.SIM_NOT_READY;
            mService.handleFinishNotification(mJobId, false);
            mListener.onFailed(MultiChoiceService.TYPE_COPY, mJobId, mRequests.size(),
                    0, mRequests.size(), errorCause);
            return;
        }
        copyContactsToSim();
    }
    
    // The following lines are provided and maintained by Mediatek Inc.
    ///M:AAS
    private void generateDataBuilder(Cursor dataCursor, Builder builder,
            String[] columnNames, String mimeType, int slotId, String srcAccountType) {
        String isAnr = dataCursor.getString(dataCursor.getColumnIndex(Data.IS_ADDITIONAL_NUMBER));
        int aasIndex;
        String aasTag;
        int phoneType;
        for (int i = 1; i < columnNames.length; i++) {
            if (OperatorUtils.isAasEnabled(srcAccountType) && Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // if the data is phone number data(primary or additional),deal
                // with its number type specially.
                if ("1".equals(isAnr) && Data.DATA5.equals(columnNames[i])) {
                    aasIndex = dataCursor.getInt(i);
                    aasTag = USIMAas.getUSIMAASById(slotId, aasIndex);
                    if ((phoneType = getPhoneTypeByName(aasTag)) != -1) {
                        builder.withValue(Data.DATA2, phoneType);
                    } else {
                        builder.withValue(Data.DATA2, Phone.TYPE_MOBILE);
                    }
                    builder.withValue(Data.DATA5, "");
                } else if (!("1".equals(isAnr)) && Data.DATA2.equals(columnNames[i])) {
                    // primary number's type should be set to Mobile.
                    builder.withValue(columnNames[i], Phone.TYPE_MOBILE);
                } else {
                    cursorColumnToBuilder(dataCursor, columnNames, i, builder);
                }
            } else {
                cursorColumnToBuilder(dataCursor, columnNames, i, builder);
            }
        }
    }
    
    private int getPhoneTypeByName(String phoneName) {
        if (phoneName == null) {
            return -1;
        }
        int typeCount = 20;

        Context context = mService.getApplicationContext();
        String existType;
        for (int i = 1; i <= 20; i++) {
            existType = context.getString(Phone.getTypeLabelResource(i));
            Log.w(LOG_TAG, "getPhoneTypeByName, existType=" + existType);
            if (phoneName.equals(existType)) {
                Log.i(LOG_TAG, "getPhoneTypeByName, type=" + i);
                return i;
            }
        }

        return -1;
    }
    
    private void buildAnrValuesForSim(Context context, ArrayList<String> additionalNumberArray,
            ArrayList<Integer> phoneTypeArray, int maxAnrCount, int dstSlotId,
            ContentValues values, ArrayList<Anr> anrsList) {
        String simNum = null;
        String simAnrNum = null;
        String phoneTypeName;
        int aasIndex;
        int type;
        for (int j = 0; j < maxAnrCount; j++) {
            simAnrNum = additionalNumberArray.remove(0);
            type = phoneTypeArray.remove(0);
            
            simAnrNum = TextUtils.isEmpty(simAnrNum) ? "" : simAnrNum.replace("-", "");
            values.put("anr" + SubContactsUtils.getSuffix(j), PhoneNumberUtils.stripSeparators(simAnrNum));
            Log.d(LOG_TAG, "buildAnrValuesForSim(op03) anr is " + simAnrNum);
            
            phoneTypeName = context.getString(Phone.getTypeLabelResource(type));
            if ((aasIndex = USIMAas.getAasIndexByName(phoneTypeName, dstSlotId)) 
                    != USIMAas.AAS_INDEX_NOT_EXIST) {
                values.put("aas" + SubContactsUtils.getSuffix(j), aasIndex);
                Log.d(LOG_TAG, "after getAasIndexByName, aas index is " + aasIndex);
            }
            Anr anr = new Anr();
            anr.mAdditionNumber = simAnrNum;
            anr.mType = String.valueOf(aasIndex);
            Log.w(LOG_TAG, "buildAnrValuesForSim, anr= " + anr);
            anrsList.add(anr);
        }
    }
    // The previous lines are provided and maintained by Mediatek Inc.

}
