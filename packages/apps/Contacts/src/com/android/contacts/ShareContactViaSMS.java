package com.android.contacts;

import com.android.contacts.util.Constants;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.PowerManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import android.database.Cursor;
import android.content.ContentResolver;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import java.util.ArrayList;
import java.util.List;
//import com.android.mms.ui./*ComposeMessageActivity*/*;

public class ShareContactViaSMS extends Activity {
	
	private static final String TAG = "ShareContactViaSMS";
	private String mAction;
	private Uri dataUri;
	private int singleContactId = -1;
	String lookUpUris;
	Intent intent;
	private ProgressDialog mProgressDialog;
	private SearchContactThread mSearchContactThread;
	
	static final String[] CONTACTS_PROJECTION = new String[] { Contacts._ID, // 0
		Contacts.DISPLAY_NAME_PRIMARY, // 1
		Contacts.DISPLAY_NAME_ALTERNATIVE, // 2
		Contacts.SORT_KEY_PRIMARY, // 3
		Contacts.DISPLAY_NAME, // 4
    };
	
	static final int PHONE_ID_COLUMN_INDEX = 0;
//    final String[] sLookupProjection = new String[] {
//            Contacts.LOOKUP_KEY
//    };
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        intent = getIntent();
        mAction = intent.getAction();
        String contactId = intent.getStringExtra("contactId");
        String userProfile = intent.getStringExtra("userProfile");
        if (userProfile != null && "true".equals(userProfile)) {
        	Toast.makeText(this.getApplicationContext(), getString(R.string.user_profile_cannot_sd_card), Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
        
        if (contactId != null && !"".equals(contactId)) {
            singleContactId = Integer.parseInt(contactId);
        }

        lookUpUris = intent.getStringExtra("LOOKUPURIS");
        if ((lookUpUris == null || "".equals(lookUpUris)) && singleContactId == -1) {
        	Toast.makeText(this.getApplicationContext(), getString(R.string.send_file_sms_error), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
		}
        Log.i(TAG,"mAction is " + mAction);
    }
    
	private void showProgressDialog() {
		if (mProgressDialog == null) {
            String title = getString(R.string.please_wait);
            String message = getString(R.string.please_wait);
            mProgressDialog = ProgressDialog.show(this, title, message, true, false);
            mProgressDialog.setOnCancelListener(mSearchContactThread);
            mSearchContactThread.start();
        }
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Intent.ACTION_SEND.equals(mAction) && intent.hasExtra(Intent.EXTRA_STREAM)) {
			mSearchContactThread = new SearchContactThread();
			showProgressDialog();
        }
	}
    
    public void shareViaSMS(String lookUpUris) {    	
		StringBuilder contactsID = new StringBuilder();
		int curIndex = 0;
		Cursor cursor = null;
		String id = null;
        String textVCard = "";
		if (singleContactId == -1) {
		    String[] tempUris = lookUpUris.split(":");
		    StringBuilder selection = new StringBuilder(Contacts.LOOKUP_KEY + " in (");
		    int index = 0;
		    for (int i = 0; i < tempUris.length; i++) {
		        selection.append("'" + tempUris[i] + "'");
		        if (index != tempUris.length-1) {
		            selection.append(",");
                }
		        index++;
            }
		    
		    selection.append(")");
			cursor = getContentResolver().query(/*dataUri*/Contacts.CONTENT_URI, CONTACTS_PROJECTION, selection.toString(), null, Contacts.SORT_KEY_PRIMARY);
			Log.i(TAG,"cursor is " + cursor);
			if (null != cursor) {
				while (cursor.moveToNext()) {				
					if (cursor != null) id = cursor.getString(PHONE_ID_COLUMN_INDEX);
					if (curIndex++ != 0) {
						contactsID.append("," + id);
					} else {
						contactsID.append(id);
					}
				}
				cursor.close();
			}
		} else {			
			id = Integer.toString(singleContactId);
			contactsID.append(id);
		}

        long[] contactsIds = null;
        if (contactsID.toString() != null && !contactsID.toString().equals("")) {
            String[] vCardConIds = contactsID.toString().split(",");
            Log.e(TAG, "ComposeMessage.initActivityState(): vCardConIds.length" + vCardConIds.length);
            contactsIds = new long[vCardConIds.length];
            try {
                for (int i = 0; i < vCardConIds.length; i++) {
                    contactsIds[i] = Long.parseLong(vCardConIds[i]);
                }
            } catch (NumberFormatException e) {
                contactsIds = null;
            }
        }
        if (contactsIds != null && contactsIds.length > 0) {
            Log.i(TAG, "compose.addTextVCard(): contactsIds.length() = " + contactsIds.length);
//          String textVCard = TextUtils.isEmpty(mTextEditor.getText())? "": "\n";
		
          StringBuilder sb = new StringBuilder("");
          for (long contactId : contactsIds) {
              if (contactId == contactsIds[contactsIds.length-1]) {
                  sb.append(contactId);
              } else {
                  sb.append(contactId + ",");
              }
          }
          String selection = Data.CONTACT_ID + " in (" + sb.toString() + ")";
		
          Log.i(TAG, "compose.addTextVCard(): selection = " + selection);
          Uri dataUri = Uri.parse("content://com.android.contacts/data");
          Log.i(TAG,"Before query to build contact name and number string ");
          Cursor c = getContentResolver().query(
              dataUri, // URI
              new String[]{Data.CONTACT_ID, Data.MIMETYPE, Data.DATA1}, // projection
              selection, // selection
              null, // selection args
              Contacts.SORT_KEY_PRIMARY); // sortOrder
          Log.i(TAG,"After query to build contact name and number string ");
          if (c != null) {
        	  Log.i(TAG,"Before getVCardString ");
              textVCard = getVCardString(c, textVCard);
              Log.i(TAG,"After getVCardString ");
              c.close();
          }	
        }
        Log.i(TAG,"textVCard is " + " \n" + textVCard);
		Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", "", null));
		i.putExtra("sms_body", textVCard);
		try {
			ShareContactViaSMS.this.startActivity(i);
		} catch (ActivityNotFoundException e) {
            ShareContactViaSMS.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ShareContactViaSMS.this.getApplicationContext(),
                            getString(R.string.quickcontact_missing_app), Toast.LENGTH_SHORT)
                            .show();
                }
            });
			Log.d(TAG, "ActivityNotFoundException for secondaryIntent");
		}
		
		finish();
    }
    
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.i(TAG,"In onBackPressed");
		finish();
	}
    
    // create the String of vCard via Contacts message
    private String getVCardString(Cursor cursor, String textVCard) {
        final int dataContactId     = 0;
        final int dataMimeType      = 1;
        final int dataString        = 2;
        long contactId = 0l;
        long contactCurrentId = 0l;
        int i = 1;
        String mimeType;
        TextVCardContact tvc = new TextVCardContact();
        int j = 0;
        while (cursor.moveToNext()) {
            contactId = cursor.getLong(dataContactId);
            mimeType = cursor.getString(dataMimeType);
            if (contactCurrentId == 0l) {
                contactCurrentId = contactId;
            }

            // put one contact information into textVCard string
            if (contactId != contactCurrentId) {
                contactCurrentId = contactId;
                textVCard += tvc.toString();
                tvc.reset();
            }

            // get cursor data
            if (CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                tvc.name = cursor.getString(dataString);
            }
            if (CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                tvc.numbers.add(cursor.getString(dataString));
            }
            if (CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                tvc.emails.add(cursor.getString(dataString));
            }
            if (CommonDataKinds.Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                tvc.organizations.add(cursor.getString(dataString));
            }
            // put the last one contact information into textVCard string
            if (cursor.isLast()) {
                textVCard += tvc.toString() + "\n";
            }
            j++;
            if (j % 10 == 0) {
            	if (textVCard.length() > 2000) {
            		break;
            	}
            }
        }
//        Log.i(TAG, "compose.getVCardString():return string = " + textVCard);
        return textVCard;
    }
    

    private class TextVCardContact {
        protected String name = "";
        protected List<String> numbers = new ArrayList<String>();
        protected List<String> emails = new ArrayList<String>();
        protected List<String> organizations = new ArrayList<String>();

        protected void reset() {
            name = "";
            numbers.clear();
            emails.clear();
            organizations.clear();
        }
        @Override
        public String toString() {
            String textVCardString = "";
            int i = 1;
            if (name != null && !name.equals("")) {
                textVCardString += getString(R.string.nameLabelsGroup) + ": " + name + "\n";
            }
            if (!numbers.isEmpty()) {
                if (numbers.size() > 1) {
                    i = 1;
                    for (String number : numbers) {
                        textVCardString += "Tel" + i + ": " + number + "\n";
                        i++;
                    }
                } else {
                    textVCardString += "Tel" + ": " + numbers.get(0) + "\n";
                }
            }
            if (!emails.isEmpty()) {
                if (emails.size() > 1) {
                    i = 1;
                    for (String email : emails) {
                        textVCardString += getString(R.string.email_other) + i + ": " + email + "\n";
                        i++;
                    }
                } else {
                    textVCardString += getString(R.string.email_other) + ": " + emails.get(0) + "\n";
                }
            }
            if (!organizations.isEmpty()) {
                if (organizations.size() > 1) {
                    i = 1;
                    for (String organization : organizations) {
                        textVCardString += getString(R.string.organizationLabelsGroup) + i + ": " + organization + "\n";
                        i++;
                    }
                } else {
                    textVCardString += getString(R.string.organizationLabelsGroup) + ": " + organizations.get(0) + "\n";
                }
            }
            return textVCardString;
        }
    }
    
    private class SearchContactThread extends Thread implements OnCancelListener, OnClickListener {
        // To avoid recursive link.
        private class CanceledException extends Exception {
        	
        }

        public SearchContactThread() {
            
        }

        @Override
        public void run() {
        	String type = intent.getType();
        	dataUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
//        	dataUri.buildUpon().appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY, "1").build();
        	Log.i(TAG,"dataUri is " + dataUri);
        	Log.i(TAG,"type is " + type);
            if (dataUri != null && type != null) {
            	shareViaSMS(lookUpUris);
            }
        }


        public void onCancel(DialogInterface dialog) {
//            mCanceled = true;
            finish();
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                finish();
            }
        }
    }

}