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

package com.mediatek.backuprestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.util.ArrayList;
import java.util.zip.ZipFile;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import com.android.vcard.VCardConfig;
import com.android.vcard.VCardEntry;
import com.android.vcard.VCardEntryConstructor;
import com.android.vcard.VCardEntryCounter;
import com.android.vcard.VCardEntryHandler;
import com.android.vcard.VCardInterpreter;
import com.android.vcard.VCardParser;
import com.android.vcard.VCardParser_V21;
import com.android.vcard.VCardParser_V30;
import com.android.vcard.VCardSourceDetector;
import com.android.vcard.exception.VCardException;
import com.android.vcard.exception.VCardNestedException;
import com.android.vcard.exception.VCardNotSupportedException;
import com.android.vcard.exception.VCardVersionException;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;

public class ContactRestoreComposer extends Composer {
    private static final String CONTACTTAG = "Contact:";
    public  static final String ACTION_SHOULD_FINISHED = "com.android.contacts.ImportExportBridge.ACTION_SHOULD_FINISHED";
    private ArrayList<String> mFileNameList;
    private int mIdx;
    private ZipFile mZipFile;
    private static final int NUM = 1500;
    private ByteArrayOutputStream totalContent = new ByteArrayOutputStream();


	public ContactRestoreComposer(Context context) {
		super(context);
	}

    @Override
    public int getModuleType() {
		return ModuleType.TYPE_CONTACT;
	}

    @Override
    public int getCount() {
        int count = 0;
        if (mFileNameList != null) {
            count = mFileNameList.size();
        }

        Log.d(LogTag.RESTORE, CONTACTTAG + "getCount():" + count);
        return count;
    }

    public boolean init() {
        boolean result = false;
        mFileNameList = new ArrayList<String>();
        Log.d(LogTag.RESTORE, CONTACTTAG + "begin init:" + System.currentTimeMillis());
        try {
            mFileNameList = (ArrayList<String>)BackupZip.GetFileList(mZipFileName,
                                                                     true,
                                                                     true,
                                                                     "contacts/contact[0-9]+\\.vcf");
            mZipFile = BackupZip.getZipFileFromFileName(mZipFileName);                                                         
            result = true;
        } catch (Exception e) {
        }

        Log.d(LogTag.RESTORE, CONTACTTAG + "end init:" + System.currentTimeMillis());
        Log.d(LogTag.RESTORE, CONTACTTAG + "init():" + result + ",count:" + mFileNameList.size());
        return result;
    }


    @Override
    public boolean isAfterLast() {
        boolean result = true;
        if (mFileNameList != null) {
            result = (mIdx >= mFileNameList.size()) ? true : false;
        }

        Log.d(LogTag.RESTORE, CONTACTTAG + "isAfterLast():" + result);
        return result;
    }

    public boolean composeOneEntity() {
		return implementComposeOneEntity();
	}

    public boolean implementComposeOneEntity() {
        Log.d(LogTag.RESTORE, CONTACTTAG + "begin readFileContent:" + System.currentTimeMillis());

        boolean result = false;
        String contactFileName = mFileNameList.get(mIdx++);
        byte[] content = BackupZip.readFileContent(mZipFile, contactFileName);
        Log.d(LogTag.RESTORE, CONTACTTAG + "end readFileContent:" + System.currentTimeMillis());

        if (content != null) {
            try {
                totalContent.write(content);
            } catch (IOException e) {

            }

            if ((mIdx % NUM != 0) && !isAfterLast()) {
                return true;
            }

            Log.d(LogTag.RESTORE, CONTACTTAG + "begin restore:" + System.currentTimeMillis());

            //Account account = new Account("Phone", AccountType.ACCOUNT_TYPE_LOCAL_PHONE);
            Account account = new Account("Phone", "Local Phone Account");

            InputStream is = new ByteArrayInputStream(content);
            VCardEntryCounter counter = new VCardEntryCounter();
            VCardSourceDetector detector = new VCardSourceDetector();
            VCardParser_V21 vcardParser = new VCardParser_V21(VCardConfig.VCARD_TYPE_V21_GENERIC);
            try {
                vcardParser.addInterpreter(counter);
                vcardParser.addInterpreter(detector);
                vcardParser.parse(is);
            } catch (IOException e) {
                Log.d(LogTag.BACKUP, CONTACTTAG + "IOException");
            } catch (VCardVersionException e) {
                Log.d(LogTag.BACKUP, CONTACTTAG + "VCardVersionException");
            } catch (VCardException e) {
                Log.d(LogTag.BACKUP, CONTACTTAG + "VCardException");
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }

            final String estimatedCharset = detector.getEstimatedCharset();
            final VCardEntryConstructor constructor =
                new VCardEntryConstructor(VCardConfig.VCARD_TYPE_V21_GENERIC, account, estimatedCharset);
            final RestoreVCardEntryCommitter committer = new RestoreVCardEntryCommitter(mContext.getContentResolver());
            constructor.addEntryHandler(committer);

            final int[] possibleVCardVersions = new int[] {
                VCardConfig.VCARD_TYPE_V21_GENERIC,
                VCardConfig.VCARD_TYPE_V30_GENERIC
            };

            is = new ByteArrayInputStream(totalContent.toByteArray());
            result = readOneVCard(is, VCardConfig.VCARD_TYPE_V21_GENERIC, constructor, possibleVCardVersions);

            if (!isAfterLast()) {
                totalContent = new ByteArrayOutputStream();
            } else {
                totalContent = null;
            }

            Log.d(LogTag.RESTORE, CONTACTTAG + "end restore:" + System.currentTimeMillis());
        } else {
            if (super.mReporter != null) {
                super.mReporter.onErr(new IOException());
            }
        }

        Log.d(LogTag.RESTORE, CONTACTTAG +
              "mZipFileName:" + mZipFileName +
              ",contactFileName:" + contactFileName +
              ",result:" + result);

        return result;
    }


	private boolean deleteAllContact() {
		if (mContext != null) {
            Log.d(LogTag.RESTORE, CONTACTTAG + "begin delete:" + System.currentTimeMillis());

            int count = mContext.getContentResolver()
                .delete(Uri.parse(ContactsContract.RawContacts.CONTENT_URI.toString() +"?" + ContactsContract.CALLER_IS_SYNCADAPTER+"=true"),
                        ContactsContract.RawContacts._ID + ">0",
                        null);

            Log.d(LogTag.RESTORE, CONTACTTAG + "end delete:" + System.currentTimeMillis());

            Log.d(LogTag.RESTORE,
                  CONTACTTAG + "deleteAllContact()," + count + " records deleted!");

            return true;
		}

        return false;
	}

    private boolean readOneVCard(InputStream is,
                                 int vcardType,
                                 final VCardInterpreter interpreter,
                                 final int[] possibleVCardVersions) {
        boolean successful = false;
        final int length = possibleVCardVersions.length;
        VCardParser vcardParser;

        for (int i = 0; i < length; i++) {
            final int vcardVersion = possibleVCardVersions[i];
            try {
                if (i > 0 && (interpreter instanceof VCardEntryConstructor)) {
                    // Let the object clean up internal temporary objects,
                    ((VCardEntryConstructor) interpreter).clear();
                }

                // We need synchronized block here,
                // since we need to handle mCanceled and mVCardParser at once.
                // In the worst case, a user may call cancel() just before creating
                // mVCardParser.
                synchronized (this) {
                    vcardParser = (vcardVersion == VCardConfig.VCARD_TYPE_V21_GENERIC) ?
                        new VCardParser_V21(vcardType) :
                        new VCardParser_V30(vcardType);
                    // if (isCancelled()) {
                    //     Log.i(LOG_TAG, "ImportProcessor already recieves cancel request, so " +
                    //           "send cancel request to vCard parser too.");
                    //     mVCardParser.cancel();
                }

                vcardParser.parse(is, interpreter);
                successful = true;
                break;
            } catch (IOException e) {
                //Log.e(LOG_TAG, "IOException was emitted: " + e.getMessage());
            } catch (VCardNestedException e) {
                //Log.e(LOG_TAG, "Nested Exception is found.");
            } catch (VCardNotSupportedException e) {
                //Log.e(LOG_TAG, e.toString());
            } catch (VCardVersionException e) {
                if (i == length - 1) {
                    //Log.e(LOG_TAG, "Appropriate version for this vCard is not found.");
                } else {
                    // We'll try the other (v30) version.
                }
            } catch (VCardException e) {
                //Log.e(LOG_TAG, e.toString());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        Log.d(LogTag.BACKUP, CONTACTTAG + "readOneVCard() " + successful);
        return successful;
    }

    public void onStart() {
        super.onStart();
        //deleteAllContact();

        Log.d(LogTag.RESTORE, CONTACTTAG + " onStart()");
    }

    @Override
    public void onEnd() {
        super.onEnd();
        if (mFileNameList != null) {
            mFileNameList.clear();
        }
        mZipFile = null;
        Log.d(LogTag.RESTORE, CONTACTTAG + " onEnd()");
    }


    private class  RestoreVCardEntryCommitter implements VCardEntryHandler {
        private final ContentResolver mContentResolver;
        //private long mTimeToCommit;
        //private int mCounter;
        private ArrayList<ContentProviderOperation> mOperationList;
        private final ArrayList<Uri> mCreatedUris = new ArrayList<Uri>();

        public RestoreVCardEntryCommitter(ContentResolver resolver) {
            mContentResolver = resolver;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onEnd() {
            if (mOperationList != null) {
                mCreatedUris.add(pushIntoContentResolver(mOperationList));
            }
        }

        @Override
        public void onEntryCreated(final VCardEntry vcardEntry) {
            //final long start = System.currentTimeMillis();
            mOperationList = vcardEntry.constructInsertOperations(mContentResolver, mOperationList);
            //mCounter++;
            if (mOperationList != null && mOperationList.size() >= 480) {
                mCreatedUris.add(pushIntoContentResolver(mOperationList));
                //mCounter = 0;
                mOperationList = null;
            }
            //mTimeToCommit += System.currentTimeMillis() - start;
            increaseComposed();
        }

        private Uri pushIntoContentResolver(ArrayList<ContentProviderOperation> operationList) {
            try {
                final ContentProviderResult[] results = mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);

                // the first result is always the raw_contact. return it's uri so
                // that it can be found later. do null checking for badly behaving
                // ContentResolvers
                return ((results == null || results.length == 0 || results[0] == null)
                        ? null : results[0].uri);
            } catch (RemoteException e) {
                //Log.e(LogTag.RESTORE, CONTACTTAG + String.format("%s: %s", e.toString(), e.getMessage()));
                return null;
            } catch (OperationApplicationException e) {
                //Log.e(LogTag.RESTORE, CONTACTTAG + String.format("%s: %s", e.toString(), e.getMessage()));
                return null;
            }
        }

        /**
         * Returns the list of created Uris. This list should not be modified by the caller as it is
         * not a clone.
         */
        public ArrayList<Uri> getCreatedUris() {
            return mCreatedUris;
        }
    }

}
