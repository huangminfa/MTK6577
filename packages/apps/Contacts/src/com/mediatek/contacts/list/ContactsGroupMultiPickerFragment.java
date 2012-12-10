package com.mediatek.contacts.list;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.mediatek.contacts.util.ContactsGroupUtils;
import com.mediatek.contacts.util.ContactsGroupUtils.ContactsGroupArrayData;
import com.android.contacts.R;
import com.android.contacts.util.WeakAsyncTask;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.gemini.GeminiPhone;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class ContactsGroupMultiPickerFragment extends MultiContactsPickerBaseFragment {

    private static final String TAG = ContactsGroupMultiPickerFragment.class.getSimpleName();
    public static final boolean DEBUG = true;
    private static HashMap<Long, ContactsGroupUtils.ContactsGroupArrayData> selectedContactsMap = 
         new HashMap<Long, ContactsGroupUtils.ContactsGroupArrayData>();

    private String fromUgroupName = null;
    private int mSlotId = 0;
    private long fromPgroupId = 0;
    private String mAccountName;
    static ProgressDialog mPogressDialog;
    private MoveGroupTask mMoveGroupTask;
    private boolean mCancel = false;
    private Account mAccount = null;

    private static final int MAX_OP_COUNT_IN_ONE_BATCH = 150;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Intent intent = this.getArguments().getParcelable(FRAGMENT_ARGS);
        fromUgroupName = intent.getStringExtra("mGroupName");
        mAccountName = intent.getStringExtra("mAccountName"); 
        mSlotId = intent.getIntExtra("mSlotId", -1);
        fromPgroupId = intent.getLongExtra("mGroupId", -1);
        mAccount = intent.getParcelableExtra("account");
        if (DEBUG) {
            Log.i(TAG, "[onCreate]fromUgroupName:" + fromUgroupName
                    + "|fromPgroupId:" + fromPgroupId
                    + "|mSlotId:" + mSlotId + "|mAccountName:" + mAccountName); 
        }

        showFilterHeader(false);
        if (mReceiver == null) {
            mReceiver = new SimReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            filter.addAction(TelephonyIntents.ACTION_PHB_STATE_CHANGED);
            filter.addAction(GeminiPhone.EVENT_PRE_3G_SWITCH);
            getActivity().registerReceiver(mReceiver,filter);
            Log.i(TAG, "registerReceiver mReceiver");
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
            Log.i(TAG,"unregisterReceiver mReceiver");
        }
    }
    

    @Override
    public void onOptionAction() {
        if (getListView().getCheckedItemCount() == 0) {
            Toast.makeText(this.getContext(), R.string.multichoice_no_select_alert,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        selectedContactsMap.clear();
        ContactsGroupMultiPickerAdapter adapter = (ContactsGroupMultiPickerAdapter) this.getAdapter();
        int count = getListView().getCount();
        int simIndex = 0;
        int indexSimOrPhone = 0;
        for (int position = 0; position < count; ++position) {
            if (getListView().isItemChecked(position)) {
                long contactId = adapter.getContactID(position);
                if(DEBUG)Log.d(TAG, "contactId = " + contactId);
                simIndex = adapter.getSimIndex(position);
                // ToDo to Check the method called
                indexSimOrPhone = adapter.getContactIndicator(position);
                adapter.getContactID(position);
                selectedContactsMap.put(contactId,
                        new ContactsGroupUtils.ContactsGroupArrayData()
                                .initData(simIndex, indexSimOrPhone));
            }
        }
        if (DEBUG) {
            Log.i(TAG, "[onOptionAction]selectedContactsMap size"
                    + selectedContactsMap.size());
        }
        
        startTargetGroupQuery();
    }

    @Override
    protected ContactListAdapter createListAdapter() {
        ContactsGroupMultiPickerAdapter adapter = new ContactsGroupMultiPickerAdapter(
                getActivity(), getListView());
        adapter.setFilter(ContactListFilter
                .createFilterWithType(ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS));
        adapter.setSectionHeaderDisplayEnabled(true);
        adapter.setDisplayPhotos(true);
        adapter.setQuickContactEnabled(false);
        adapter.setEmptyListEnabled(true);

        adapter.setGroupTitle(fromUgroupName);
        
        adapter.setGroupAccount(mAccount);

        return adapter;
    }

    
	public class MoveGroupTask extends
			WeakAsyncTask<String, Void, Integer, Activity> {
		private WeakReference<ProgressDialog> mProgress;
		
		public MoveGroupTask(Activity target) {
			super(target);
		}
		
		@Override
		protected void onPreExecute(final Activity target) {
			mPogressDialog = ProgressDialog.show(target, null, target
					.getText(R.string.moving_group_members), false, true);
			mPogressDialog
					.setOnDismissListener(new DialogInterface.OnDismissListener() {
						public void onDismiss(DialogInterface dialog) {
							mMoveGroupTask.cancel(true);
							target.finish();
						}
					});
			mPogressDialog
					.setOnCancelListener(new DialogInterface.OnCancelListener() {

						public void onCancel(DialogInterface dialog) {
							mCancel = true;
							boolean cancel = mMoveGroupTask.cancel(true);
							Log.i(TAG, cancel + "------------cancel");
							target.finish();

						}
					});
			mProgress = new WeakReference<ProgressDialog>(mPogressDialog);
			super.onPreExecute(target);
		}

		@Override
		protected Integer doInBackground(Activity target,
				String... params) {   

			String fromGroupName = params[0];
			String toGroupName = params[1];
			long fromGroupId = Long.parseLong(params[2]);
			long toGroupId = Long.parseLong(params[3]);
			int slot = Integer.parseInt(params[4]);

			if (DEBUG) {
				Log.i(TAG, "[doInBackground]fromGroupName:" + fromGroupName + "|toGroupName:"
						+ toGroupName + "|fromGroupId:" + fromGroupId
						+ "|toGroupId:" + toGroupId + "|slot:" + slot
						+ "|selectedContactsMap size:"
						+ selectedContactsMap.size());
			}
			int ret = doMove(target.getContentResolver(), fromGroupName, slot,
					toGroupName, selectedContactsMap, fromGroupId, toGroupId);
			if (DEBUG) {
                Log.i(TAG, "[doInBackground]result:" + ret);
            }
			return ret;
		}

		@Override
		protected void onCancelled() {
			mCancel = true;
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(final Activity target, Integer error) {
			final ProgressDialog progress = mProgress.get();
			int toast;
			if (!target.isFinishing() && progress != null
					&& progress.isShowing()) {
				progress.dismiss();
			}
			super.onPostExecute(target, error);
			if (error == 0) {
				toast = R.string.moving_group_members_sucess;
			} else if (error == -1) {
				toast = R.string.moving_group_members_fail;
			} else {
			    toast = R.string.moving_group_members_sucess;
			}
			if (error == 0)
				Toast.makeText(target, toast, Toast.LENGTH_LONG).show();
			selectedContactsMap.clear();
			target.finish();
		}
	}

    public class MoveDialog extends DialogFragment {

        private Activity mContext = null;
        private String accountName = null;
        private int slotId = -1;
        private long originalGroupId = -1;
        private String originalGroupName = null;
        private long targetGroupId = -1;
        private String targetGroupName = null;
        private long [] mIdArray = null;
        private String [] mTitleArray = null;
        
        public MoveDialog(){
        	
        }
        
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mContext = activity;
            mMoveGroupTask = new MoveGroupTask(mContext);
        }

        public void onCreate(Bundle savedState) {
            super.onCreate(savedState);
            Intent intent = this.getArguments().getParcelable(FRAGMENT_ARGS);

            originalGroupName = intent.getStringExtra("mGroupName");
            accountName = intent.getStringExtra("mAccountName");
            slotId = intent.getIntExtra("mSlotId", -1);
            originalGroupId = intent.getLongExtra("mGroupId", -1);
            
            mIdArray = intent.getLongArrayExtra("IdArray");
            mTitleArray = intent.getStringArrayExtra("TitleArray");

            Log.d(TAG, "[MoveDialog#onCreate]originalGroupName:"
                    + originalGroupName + "|originalGroupId:" + originalGroupId
                    + "|accountName:" + accountName + "|slotId:" + slotId
                    + "|mIdArray" + (mIdArray == null ? "null" : mIdArray.toString())
                    + "|mTitleArray:" + (mTitleArray == null ? "null" : mTitleArray.toString()));

            if (savedState != null) {
                targetGroupName = savedState.getString("target_group_name");
                targetGroupId = savedState.getLong("target_group_id", -1);
                Log.d(TAG, "[MoveDialog#onCreate]targetGroupName:"
                        + targetGroupName + "|targetGroupId:"
                        + String.valueOf(targetGroupId));
            }

        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (!TextUtils.isEmpty(targetGroupName))
                outState.putString("target_group_name", targetGroupName);
            outState.putLong("target_group_id", targetGroupId);
            outState.putString("mGroupName", originalGroupName);
            outState.putString("mAccountName", accountName);
            outState.putInt("mSlotId", slotId);
            outState.putLong("mGroupId", originalGroupId);
            super.onSaveInstanceState(outState);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setTitle(R.string.move_contacts_to);
            builder.setIcon(com.android.internal.R.drawable.ic_dialog_alert_holo_light);
            
            builder.setSingleChoiceItems(mTitleArray, -1,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                long id = mIdArray[which];
                                String title = mTitleArray[which];
                                Log.d(TAG, "[onClick]Move to title:" + title + " ||id: " + id);
                                targetGroupId = id;
                                targetGroupName = title;
                            } catch (Exception e) {
                                return;
                            }
                        }
                    });
            
            builder.setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    if (targetGroupId > 0) { 
                    	mMoveGroupTask.execute(
                                originalGroupName, 
                                targetGroupName, 
                                String.valueOf(originalGroupId), 
                                String.valueOf(targetGroupId), 
                                String.valueOf(slotId));
                    } else {
                        if (mContext != null) {
                            Toast.makeText(mContext, R.string.multichoice_no_select_alert, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
            return builder.create();
        }
    }
    private int doMove(ContentResolver resolver, String fromUgroupName, int slotId,
			String toUgroupName, HashMap<Long, ContactsGroupArrayData> selectedContactsMap,
			long fromPgroupId, long toPgroupId) {
        
        int failCount = 0;

		int fromUgroupId = -1;
		int toUgroupId = -1;
		if (slotId >= 0) {
	      try {
	          fromUgroupId = ContactsGroupUtils.USIMGroup.hasExistGroup(slotId, fromUgroupName);
	          toUgroupId = ContactsGroupUtils.USIMGroup.hasExistGroup(slotId, toUgroupName);
	         } catch (RemoteException e) {
	             //log
	             fromUgroupId = -1;
	             toUgroupId = -1;
	         }
		}

		Cursor c = resolver.query(Data.CONTENT_URI,
                new String[]{Data.CONTACT_ID}, 
                Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID + "=?", 
                new String[]{GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(toPgroupId)}, null);
		
		HashSet<Long> set = new HashSet<Long>();
		while (c != null && c.moveToNext()) {
			long contactId = c.getLong(0);
			Log.i(TAG, contactId + "--------contactId");
			set.add(contactId);
		}
		if (c != null)
			c.close();

		ContentValues values = new ContentValues();
		StringBuilder idBuilder = new StringBuilder();
		StringBuilder idBuilderDel = new StringBuilder();
		// USIM group begin
		boolean isInTargetGroup = false;
		// USIM group end
		Iterator<Entry<Long, ContactsGroupArrayData>> iter = selectedContactsMap.entrySet().iterator();
		int moveCount = 0;

		while (iter.hasNext() && !mCancel) {
			Log.i(TAG, mCancel+"----------mCancel---------");
			Entry<Long, ContactsGroupArrayData> entry = iter.next();
			long id = entry.getKey();
			Log.i(TAG, id+"--------entry.getKey()");
			// USIM Group begin
			isInTargetGroup = set.contains(id);
			
			
			int tsimId = entry.getValue().getmSimIndexPhoneOrSim();
			
			if(DEBUG)Log.i(TAG, "contactsId--------------"+id);
			if(DEBUG)Log.i(TAG, "mSimIndexPhoneOrSim--------------"+tsimId);
			if(DEBUG)Log.i(TAG, "mSimIndex--------------"+entry.getValue().getmSimIndex());
			if (tsimId > 0
					&& !ContactsGroupUtils.moveUSIMGroupMember(entry.getValue(), slotId,
							 isInTargetGroup, fromUgroupId, toUgroupId)) {
				// failed to move USIM contacts from one group to another
				Log.d(TAG,"Failed to move USIM contacts from one group to another");
                failCount++;
				continue;
			}
			
			if (isInTargetGroup) { // mark as need to be delete later
				if (idBuilderDel.length() > 0) {
					idBuilderDel.append(",");
				}
				idBuilderDel.append(id);
			} else { // mark as need to be update later
				if (idBuilder.length() > 0) {
					idBuilder.append(",");
				}
				idBuilder.append(id);
			}
			// USIM Group end
			moveCount++;
			if (moveCount > MAX_OP_COUNT_IN_ONE_BATCH) {
				int count = 0;
				if (idBuilder.length() > 0) {
					String where = ContactsGroupUtils.SELECTION_MOVE_GROUP_DATA.replace("%1",
							idBuilder.toString()).replace("%2", String.valueOf(fromPgroupId));
					Log.i(TAG, "[doMove]where: " + where);
					values.put(CommonDataKinds.GroupMembership.GROUP_ROW_ID, toPgroupId);
					count = resolver.update(ContactsContract.Data.CONTENT_URI, values, where, null);
					idBuilder.setLength(0);
				}
				Log.i(TAG, "[doMove]move data count:" + count);
				count = 0;
				if (idBuilderDel.length() > 0) {
					String whereDel = ContactsGroupUtils.SELECTION_MOVE_GROUP_DATA.replace("%1",
							idBuilderDel.toString());
					whereDel = whereDel.replace("%2", String.valueOf(fromPgroupId));
					Log.i(TAG, "[doMove]whereDel: " + whereDel);
					count = resolver.delete(ContactsContract.Data.CONTENT_URI, whereDel, null);
					Log.i(TAG, "[doMove]delete repeat contact:" + whereDel.toString());
					idBuilderDel.setLength(0);
				}
				Log.i(TAG, "[doMove]delete repeat data count:" + count);
				moveCount = 0;
			}
		}
		int count = 0;
		if (idBuilder.length() > 0) {
			String where = ContactsGroupUtils.SELECTION_MOVE_GROUP_DATA.replace("%1",
					idBuilder.toString()).replace("%2", String.valueOf(fromPgroupId));
			Log.i(TAG, "[doMove]End where: " + where);
			values.put(CommonDataKinds.GroupMembership.GROUP_ROW_ID, toPgroupId);
			count = resolver.update(ContactsContract.Data.CONTENT_URI, values, where, null);
			idBuilder.setLength(0);
		}
		Log.i(TAG, "[doMove]End move data count:" + count);
		count = 0;
		if (idBuilderDel.length() > 0) {
			String whereDel = ContactsGroupUtils.SELECTION_MOVE_GROUP_DATA.replace("%1",
					idBuilderDel.toString());
			whereDel = whereDel.replace("%2", String.valueOf(fromPgroupId));
			Log.i(TAG, "[doMove]End whereDel: " + whereDel);
			count = resolver.delete(ContactsContract.Data.CONTENT_URI, whereDel, null);
			idBuilderDel.setLength(0);
		}
		Log.i(TAG, "[doMove]End delete repeat data count:" + count);
		int totalCount = selectedContactsMap.entrySet().size();
        return failCount == 0 ? 0 : (failCount == totalCount ? -1 : failCount);
	}
    
    
    private BroadcastReceiver mReceiver;
    class SimReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "In onReceive ");
            final String action = intent.getAction();
            Log.i(TAG, "action is " + action);
            if(Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
                Log.i(TAG, "[processAirplaneModeChanged]isAirplaneModeOn:" + isAirplaneModeOn);
                if (isAirplaneModeOn && mSlotId >= 0) {
                    getActivity().finish();
                }
            } else if (TelephonyIntents.ACTION_PHB_STATE_CHANGED.equals(action)) {
                boolean phbReady = intent.getBooleanExtra("ready", false);
                int slotId = intent.getIntExtra("simId", -10);
                Log.i(TAG, "[processPhbStateChange]phbReady:" + phbReady + "|slotId:" + slotId);
                if (mSlotId >= 0) {
                    getActivity().finish();
                }
            } else if (GeminiPhone.EVENT_PRE_3G_SWITCH.equals(action)) {
                Log.i(TAG, "Modem switch .....");
                if (mSlotId >= 0) {
                    getActivity().finish();
                }
            }
        }
        
    };
    
    
    private MoveDialog mMoveDialog = null;
    private GroupQueryHandler mQueryHandler = null;
    private int mGroupQueryToken = 0;
    private class GroupQueryHandler extends AsyncQueryHandler {

        public GroupQueryHandler(ContentResolver cr) {
            super(cr);
        }
        
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (token != mGroupQueryToken)
                return;
            if (mMoveDialog != null && mMoveDialog.getDialog() != null
                    && mMoveDialog.getDialog().isShowing())
                return;
            Intent intent = getArguments().getParcelable(FRAGMENT_ARGS);
            if (cursor != null) {
                int count = cursor.getCount();
                long[] idArray = new long[count];
                String [] titleArray = new String[count];
                int i = 0;
                while (cursor.moveToNext()) {
                    String title = cursor.getString(1);
                    if (null != title) {
                        idArray[i] = cursor.getLong(0);
                        titleArray[i] = title;
                        i++;
                    } else {
                        Log.i(TAG, "Error: group title is NULL!!");
                    }
                }
                intent.putExtra("TitleArray", titleArray);
                intent.putExtra("IdArray", idArray);
                
                cursor.close();
            }
            
            if (getActivity() != null && !getActivity().isFinishing()) {
                mMoveDialog = new MoveDialog();
                mMoveDialog.setArguments(getArguments());
                mMoveDialog.show(getFragmentManager(), "moveGroup");
            }
        }
    }
    
    
    public void startTargetGroupQuery() {
        if (mQueryHandler == null) {
            mQueryHandler = new GroupQueryHandler(this.getActivity().getContentResolver());
        }
        Intent intent = getArguments().getParcelable(FRAGMENT_ARGS);
        String accountName = intent.getStringExtra("mAccountName");
        long originalGroupId = intent.getLongExtra("mGroupId", -1);
        mQueryHandler.startQuery(++mGroupQueryToken, null, Groups.CONTENT_URI,
                new String[] { Groups._ID, Groups.TITLE },
                Groups.ACCOUNT_NAME + "= '" + accountName + "' AND "
                        + Groups._ID + " !=" + originalGroupId + " AND "
                        + Groups.AUTO_ADD + "=0 AND " + Groups.FAVORITES
                        + "=0 AND " + Groups.DELETED + "=0 ",
                null, Groups.TITLE);
    }
    
}
