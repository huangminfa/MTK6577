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

package com.mediatek.omacp.message;

import com.mediatek.omacp.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import com.mediatek.xlog.Xlog;

import com.mediatek.omacp.provider.OmacpProviderDatabase;

public class OmacpMessageList extends ListActivity {
	
	private static final String XLOG = "Omacp/OmacpMessageList";
	
	private static final boolean DEBUG = true;
	
	//IDs of the context menu items for the list of message
	private static final int CONTEXT_MENU_VIEW                = 1000;
	private static final int CONTEXT_MENU_DELETE              = 1001;
	private static final int CONTEXT_MENU_VIEW_MESSAGE_DETAIL = 1002;
	//IDs of the option menu item
	private static final int OPTION_MENU_DELETE_ALL           = 100;
	
	//IDs of the query token items
	private static final int MESSAGE_LIST_QUERY_TOKEN       = 1600;
    public static final int DELETE_MESSAGE_TOKEN            = 1601;
	
	private MessageListQueryHandler mQueryHandler;
    private OmacpMessageListAdapter mListAdapter;
    
    private CharSequence mTitle;
//    private boolean mNeedToMarkAsSeen;
    private long mMessageId = -1;
    private AlertDialog mDetailDialog;
    
    private static final Uri sAllMessagesUri = OmacpProviderDatabase.CONTENT_URI.buildUpon().
    													appendQueryParameter("simple", "true").build();
    
    private static final String[] ALL_MESSAGES_PROJECTION = {
    	OmacpProviderDatabase._ID, OmacpProviderDatabase.SIM_ID, OmacpProviderDatabase.SENDER, 
    	OmacpProviderDatabase.SERVICE_CENTER, OmacpProviderDatabase.SEEN, OmacpProviderDatabase.READ, 
    	OmacpProviderDatabase.DATE, OmacpProviderDatabase.INSTALLED, OmacpProviderDatabase.PIN_UNLOCK, 
    	OmacpProviderDatabase.SEC, OmacpProviderDatabase.MAC, OmacpProviderDatabase.TITLE, 
    	OmacpProviderDatabase.SUMMARY, OmacpProviderDatabase.BODY, OmacpProviderDatabase.CONTEXT,
    	OmacpProviderDatabase.MIME_TYPE
    	};
    
    static final int ID               = 0;
    static final int SIM_ID           = 1;
    static final int SENDER           = 2;
    static final int SERVICE_CENTER   = 3;
    static final int SEEN             = 4;
    static final int READ             = 5;
    static final int DATE             = 6;
    static final int INSTALLED        = 7;
    static final int PIN_LOCK         = 8;
    static final int SEC              = 9;
    static final int MAC              = 10;
    static final int TITLE            = 11;
    static final int SUMMARY          = 12;
    static final int BODY             = 13;
    static final int CONTEXT          = 14;
    static final int MIME_TYPE        = 15;
    
    private static final String[] SEEN_PROJECTION = new String[] { "seen" };
    
    private static final String APP_ID_KEY = "appId";
    private static final String APP_CAPABILITY_ACTION = "com.mediatek.omacp.capability";
    private static final String APP_CAPABILITY_RESULT_ACTION = "com.mediatek.omacp.capability.result";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        mTitle = getString(R.string.configuration_message);
        
        setContentView(R.layout.message_list_screen);
        
        ListView listView = getListView();
        listView.setOnCreateContextMenuListener(mContextMenuListener);
        listView.setOnKeyListener(mKeyListener);
        
        initListAdapter();
        
        mQueryHandler = new MessageListQueryHandler(getContentResolver());
        
        //register capability result receiver
        registerReceiver(mResultReceiver, new IntentFilter(APP_CAPABILITY_RESULT_ACTION));
        
        //send intent to ask the capabilities of the applications
        Intent intent = new Intent();
        intent.setAction(APP_CAPABILITY_ACTION);
        this.sendBroadcast(intent);
	}
	
	@Override
    protected void onDestroy(){
	    super.onDestroy();
	    unregisterReceiver(mResultReceiver);
	}
	
	@Override
    protected void onStart() {
        super.onStart();

//        mNeedToMarkAsSeen = true;

        startAsyncQuery();
    }
	
	@Override
    protected void onResume() {
        startAsyncQuery();
        super.onResume();   
    }
	
	@Override
    protected void onStop() {
        super.onStop();
        mListAdapter.changeCursor(null);
    }
	
	@Override
    protected void onNewIntent(Intent intent) {
        // Handle intents that occur after the activity has already been created.
		startAsyncQuery();
    }
	
	private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {        
        @Override
        public void onReceive(Context context, Intent intent) {
        	if(intent.getAction().equals(APP_CAPABILITY_RESULT_ACTION)){        		
        		String appId = intent.getStringExtra(APP_ID_KEY); 
        		
        		if(DEBUG){
        			Xlog.d(XLOG, "OmacpMessageList mResultReceiver received capability, appid is : " + appId);
        		}
        		
        		if(appId == null){
        			Xlog.e(XLOG, "OmacpMessageList mResultReceiver appid is null.");
        			return;
        		}
        		
        		//change the capability values
        		if(appId.equalsIgnoreCase(OmacpMessageUtils.BROWSER_APPID)){//browser
        			handleBrowserCapability(intent);
        		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.MMS_APPID) || appId.equalsIgnoreCase(OmacpMessageUtils.MMS_2_APPID)){//mms
        			handleMmsCapability(intent);
        		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.DM_APPID)){//dm
        			handleDmCapability(intent);
        		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.SMTP_APPID) || appId.equalsIgnoreCase(OmacpMessageUtils.POP3_APPID) || appId.equalsIgnoreCase(OmacpMessageUtils.IMAP4_APPID)){//email
        			handleEmailCapability(intent);        			
        		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.RTSP_APPID)){//rtsp
        			handleRtspCapability(intent);
        		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.SUPL_APPID)){//supl
        			handleSuplCapability(intent);
        		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.DS_APID)){//ds
        			handleDsCapability(intent);
        		}else if(appId.equalsIgnoreCase(OmacpMessageUtils.IMPS_APPID)){//imps
        			handleImpsCapability(intent);
        		}else{
        			Xlog.e(XLOG, "OmacpMessageList mResultReceiver appid unknown.");
        		}
        	}
        }
	};
	
	private void handleImpsCapability(Intent it){
		OmacpApplicationCapability.imps = it.getBooleanExtra("imps", false);
		OmacpApplicationCapability.imps_provider_id = it.getBooleanExtra("imps_provider_id", false);
		OmacpApplicationCapability.imps_server_name = it.getBooleanExtra("imps_server_name", false);
		OmacpApplicationCapability.imps_content_type = it.getBooleanExtra("imps_content_type", false);
		OmacpApplicationCapability.imps_server_address = it.getBooleanExtra("imps_server_address", false);
		OmacpApplicationCapability.imps_address_type = it.getBooleanExtra("imps_address_type", false);
		OmacpApplicationCapability.imps_to_proxy = it.getBooleanExtra("imps_to_proxy", false);
		OmacpApplicationCapability.imps_to_napid = it.getBooleanExtra("imps_to_napid", false);
		OmacpApplicationCapability.imps_auth_level = it.getBooleanExtra("imps_auth_level", false);
		OmacpApplicationCapability.imps_auth_name = it.getBooleanExtra("imps_auth_name", false);
		OmacpApplicationCapability.imps_auth_secret = it.getBooleanExtra("imps_auth_secret", false);
		OmacpApplicationCapability.imps_services = it.getBooleanExtra("imps_services", false);
		OmacpApplicationCapability.imps_client_id_prefix = it.getBooleanExtra("imps_client_id_prefix", false);
	}
	
	private void handleDsCapability(Intent it){
		OmacpApplicationCapability.ds = it.getBooleanExtra("ds", false);
		OmacpApplicationCapability.ds_server_name = it.getBooleanExtra("ds_server_name", false);
		OmacpApplicationCapability.ds_to_proxy = it.getBooleanExtra("ds_to_proxy", false);
		OmacpApplicationCapability.ds_to_napid = it.getBooleanExtra("ds_to_napid", false);
		OmacpApplicationCapability.ds_provider_id = it.getBooleanExtra("ds_provider_id", false);
		OmacpApplicationCapability.ds_server_address = it.getBooleanExtra("ds_server_address", false);
		OmacpApplicationCapability.ds_address_type = it.getBooleanExtra("ds_address_type", false);
		OmacpApplicationCapability.ds_port_number = it.getBooleanExtra("ds_port_number", false);
		OmacpApplicationCapability.ds_auth_level = it.getBooleanExtra("ds_auth_level", false);
		OmacpApplicationCapability.ds_auth_type = it.getBooleanExtra("ds_auth_type", false);
		OmacpApplicationCapability.ds_auth_name = it.getBooleanExtra("ds_auth_name", false);
		OmacpApplicationCapability.ds_auth_secret = it.getBooleanExtra("ds_auth_secret", false);
		OmacpApplicationCapability.ds_auth_data = it.getBooleanExtra("ds_auth_data", false);
		OmacpApplicationCapability.ds_database_content_type = it.getBooleanExtra("ds_database_content_type", false);
		OmacpApplicationCapability.ds_database_url = it.getBooleanExtra("ds_database_url", false);
		OmacpApplicationCapability.ds_database_name = it.getBooleanExtra("ds_database_name", false);
		OmacpApplicationCapability.ds_database_auth_type = it.getBooleanExtra("ds_database_auth_type", false);
		OmacpApplicationCapability.ds_database_auth_name = it.getBooleanExtra("ds_database_auth_name", false);
		OmacpApplicationCapability.ds_database_auth_secret = it.getBooleanExtra("ds_database_auth_secret", false);
		OmacpApplicationCapability.ds_client_database_url = it.getBooleanExtra("ds_client_database_url", false);
		OmacpApplicationCapability.ds_sync_type = it.getBooleanExtra("ds_sync_type", false);
	}
	
	private void handleSuplCapability(Intent it){
		OmacpApplicationCapability.supl = it.getBooleanExtra("supl", false);
		OmacpApplicationCapability.supl_provider_id = it.getBooleanExtra("supl_provider_id", false);
		OmacpApplicationCapability.supl_server_name = it.getBooleanExtra("supl_server_name", false);
		OmacpApplicationCapability.supl_to_napid = it.getBooleanExtra("supl_to_napid", false);
		OmacpApplicationCapability.supl_server_addr = it.getBooleanExtra("supl_server_addr", false);
		OmacpApplicationCapability.supl_addr_type = it.getBooleanExtra("supl_addr_type", false);
	}
	
	private void handleRtspCapability(Intent it){
		OmacpApplicationCapability.rtsp = it.getBooleanExtra("rtsp", false);
		OmacpApplicationCapability.rtsp_provider_id = it.getBooleanExtra("rtsp_provider_id", false);
		OmacpApplicationCapability.rtsp_name = it.getBooleanExtra("rtsp_server_name", false);
		OmacpApplicationCapability.rtsp_to_proxy = it.getBooleanExtra("rtsp_to_proxy", false);
		OmacpApplicationCapability.rtsp_to_napid = it.getBooleanExtra("rtsp_to_napid", false);
		OmacpApplicationCapability.rtsp_max_bandwidth = it.getBooleanExtra("rtsp_max_bandwidth", false);
		OmacpApplicationCapability.rtsp_net_info = it.getBooleanExtra("rtsp_net_info", false);
		OmacpApplicationCapability.rtsp_min_udp_port = it.getBooleanExtra("rtsp_min_udp_port", false);
		OmacpApplicationCapability.rtsp_max_udp_port = it.getBooleanExtra("rtsp_max_udp_port", false);
	}
	
	private void handleEmailCapability(Intent it){
		OmacpApplicationCapability.email = it.getBooleanExtra("email", false);
		OmacpApplicationCapability.email_provider_id = it.getBooleanExtra("email_provider_id", false);
		OmacpApplicationCapability.email_setting_name = it.getBooleanExtra("email_setting_name", false);
		OmacpApplicationCapability.email_to_napid = it.getBooleanExtra("email_to_napid", false);
		OmacpApplicationCapability.email_outbound_addr = it.getBooleanExtra("email_outbound_addr", false);
		OmacpApplicationCapability.email_outbound_addr_type = it.getBooleanExtra("email_outbound_addr_type", false);
		OmacpApplicationCapability.email_outbound_port_number = it.getBooleanExtra("email_outbound_port_number", false);
		OmacpApplicationCapability.email_outbound_secure = it.getBooleanExtra("email_outbound_secure", false);
		OmacpApplicationCapability.email_outbound_auth_type = it.getBooleanExtra("email_outbound_auth_type", false);
		OmacpApplicationCapability.email_outbound_user_name = it.getBooleanExtra("email_outbound_user_name", false);
		OmacpApplicationCapability.email_outbound_password = it.getBooleanExtra("email_outbound_password", false);
		OmacpApplicationCapability.email_from = it.getBooleanExtra("email_from", false);
		OmacpApplicationCapability.email_rt_addr = it.getBooleanExtra("email_rt_addr", false);
		OmacpApplicationCapability.email_inbound_addr = it.getBooleanExtra("email_inbound_addr", false);
		OmacpApplicationCapability.email_inbound_addr_type = it.getBooleanExtra("email_inbound_addr_type", false);
		OmacpApplicationCapability.email_inbound_port_number = it.getBooleanExtra("email_inbound_port_number", false);
		OmacpApplicationCapability.email_inbound_secure = it.getBooleanExtra("email_inbound_secure", false);
		OmacpApplicationCapability.email_inbound_auth_type = it.getBooleanExtra("email_inbound_auth_type", false);
		OmacpApplicationCapability.email_inbound_user_name = it.getBooleanExtra("email_inbound_user_name", false);
		OmacpApplicationCapability.email_inbound_password = it.getBooleanExtra("email_inbound_password", false);
	}
	
	private void handleDmCapability(Intent it){
		OmacpApplicationCapability.dm = it.getBooleanExtra("dm", false);
		OmacpApplicationCapability.dm_provider_id = it.getBooleanExtra("dm_provider_id", false);
		OmacpApplicationCapability.dm_server_name = it.getBooleanExtra("dm_server_name", false);
		OmacpApplicationCapability.dm_to_proxy = it.getBooleanExtra("dm_to_proxy", false);
		OmacpApplicationCapability.dm_to_napid = it.getBooleanExtra("dm_to_napid", false);
		OmacpApplicationCapability.dm_server_address = it.getBooleanExtra("dm_server_address", false);
		OmacpApplicationCapability.dm_addr_type = it.getBooleanExtra("dm_addr_type", false);
		OmacpApplicationCapability.dm_port_number = it.getBooleanExtra("dm_port_number", false);
		OmacpApplicationCapability.dm_auth_level = it.getBooleanExtra("dm_auth_level", false);
		OmacpApplicationCapability.dm_auth_type = it.getBooleanExtra("dm_auth_type", false);
		OmacpApplicationCapability.dm_auth_name = it.getBooleanExtra("dm_auth_name", false);
		OmacpApplicationCapability.dm_auth_secret = it.getBooleanExtra("dm_auth_secret", false);
		OmacpApplicationCapability.dm_auth_data = it.getBooleanExtra("dm_auth_data", false);
		OmacpApplicationCapability.dm_init = it.getBooleanExtra("dm_init", false);
	}
	
	private void handleMmsCapability(Intent it){
		OmacpApplicationCapability.mms = it.getBooleanExtra("mms", false);
		OmacpApplicationCapability.mms_mmsc_name = it.getBooleanExtra("mms_mmsc_name", false);
		OmacpApplicationCapability.mms_to_proxy = it.getBooleanExtra("mms_to_proxy", false);
		OmacpApplicationCapability.mms_to_napid = it.getBooleanExtra("mms_to_napid", false);
		OmacpApplicationCapability.mms_mmsc = it.getBooleanExtra("mms_mmsc", false);
		OmacpApplicationCapability.mms_cm = it.getBooleanExtra("mms_cm", false);
		OmacpApplicationCapability.mms_rm = it.getBooleanExtra("mms_rm", false);
		OmacpApplicationCapability.mms_ms = it.getBooleanExtra("mms_ms", false);
		OmacpApplicationCapability.mms_pc_addr = it.getBooleanExtra("mms_pc_addr", false);
		OmacpApplicationCapability.mms_ma = it.getBooleanExtra("mms_ma", false);
	}
	
	private void handleBrowserCapability(Intent it){
		OmacpApplicationCapability.browser = it.getBooleanExtra("browser", false);
		OmacpApplicationCapability.browser_bookmark_folder = it.getBooleanExtra("browser_bookmark_folder", false);
		OmacpApplicationCapability.browser_to_proxy = it.getBooleanExtra("browser_to_proxy", false);
		OmacpApplicationCapability.browser_to_napid = it.getBooleanExtra("browser_to_napid", false);
		OmacpApplicationCapability.browser_bookmark_name = it.getBooleanExtra("browser_bookmark_name", false);
		OmacpApplicationCapability.browser_bookmark = it.getBooleanExtra("browser_bookmark", false);
		OmacpApplicationCapability.browser_username = it.getBooleanExtra("browser_username", false);
		OmacpApplicationCapability.browser_password = it.getBooleanExtra("browser_password", false);
		OmacpApplicationCapability.browser_homepage = it.getBooleanExtra("browser_homepage", false);		
	}
	
	private final OnCreateContextMenuListener mContextMenuListener =
        new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            Cursor cursor = mListAdapter.getCursor();
            if (cursor == null || cursor.getPosition() < 0) {
                return;
            }
            
            menu.setHeaderTitle(R.string.message_options);
            
            mMessageId = cursor.getLong(ID);

            menu.add(0, CONTEXT_MENU_VIEW, 0, R.string.view);
            menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.delete);
            menu.add(0, CONTEXT_MENU_VIEW_MESSAGE_DETAIL, 0, R.string.view_message_detail);
        }
    };
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	Cursor cursor = mListAdapter.getCursor();
        if (mMessageId >= 0 ) {
            switch (item.getItemId()) {
            	case CONTEXT_MENU_DELETE: 
            		confirmDeleteMessageDialog(new DeleteMessageListener(mMessageId, mQueryHandler, OmacpMessageList.this), 
                    		false, OmacpMessageList.this);
            		break;
            	case CONTEXT_MENU_VIEW:
            		openMessage(mMessageId);
            		break;            
            	case CONTEXT_MENU_VIEW_MESSAGE_DETAIL:
            		String messageDetails = getMessageDetails(OmacpMessageList.this, cursor);
            		mDetailDialog = new AlertDialog.Builder(OmacpMessageList.this)
                            .setTitle(R.string.message_details_title)
                            .setMessage(messageDetails)
                            .setPositiveButton(android.R.string.ok, null)
                            .setCancelable(true)
                            .show();
                    break;
            default:
                break;
            }
        }
        return super.onContextItemSelected(item);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
    	
    	if (mListAdapter.getCount() > 0) {
            menu.add(0, OPTION_MENU_DELETE_ALL, 0, R.string.delete_all).setIcon(
                    android.R.drawable.ic_menu_delete);
        }

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) { 			  			
            case OPTION_MENU_DELETE_ALL:
            	confirmDeleteMessageDialog(new DeleteMessageListener(-1, mQueryHandler, OmacpMessageList.this), 
                		true, OmacpMessageList.this);
                break;
        }
        return true;
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	if (v instanceof OmacpMessageListItem) {
    		OmacpMessageListItem itemView = (OmacpMessageListItem) v;
    		OmacpMessageListItemData ch = itemView.getConversationHeader();
	        openMessage(ch.getMessageId());
        }
    }
    
    private final OnKeyListener mKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DEL: {
                        long id = getListView().getSelectedItemId();
                        if (id > 0) {
                            confirmDeleteMessageDialog(new DeleteMessageListener(id, mQueryHandler, OmacpMessageList.this), 
                            		false, OmacpMessageList.this);
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    };
    
    private void initListAdapter() {
        mListAdapter = new OmacpMessageListAdapter(this, null);
        mListAdapter.setOnContentChangedListener(mContentChangedListener);
        setListAdapter(mListAdapter);
    }
    
    private final OmacpMessageListAdapter.OnContentChangedListener mContentChangedListener =
        new OmacpMessageListAdapter.OnContentChangedListener() {
        public void onContentChanged(OmacpMessageListAdapter adapter) {
            startAsyncQuery();
        }
    };
    
    private final class MessageListQueryHandler extends AsyncQueryHandler {
        public MessageListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
	            case MESSAGE_LIST_QUERY_TOKEN:
	            	if(cursor.getCount() == 0){
	            		//Modify the shared preference value to indicate the mms that configuration message does not exist
	            		SharedPreferences sh = OmacpMessageList.this.getSharedPreferences("omacp", 
	            				OmacpMessageList.this.MODE_WORLD_READABLE);
	                	Editor editor = sh.edit();
	                	editor.putBoolean("configuration_msg_exist", false);
	                	editor.commit();
	            		OmacpMessageList.this.finish();
	            		break;
	            	}
	            	
	                mListAdapter.changeCursor(cursor);
	                setTitle(mTitle);
	                setProgressBarIndeterminateVisibility(false);
	
//	                if (mNeedToMarkAsSeen) {
//	                    mNeedToMarkAsSeen = false;
	                    markAllMessagesAsSeen(OmacpMessageList.this);
//	                }
	                break;
	
	            default:
	                Xlog.e(XLOG, "OmacpMessageList onQueryComplete called with unknown token " + token);
	                break;
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            switch (token) {
	            case DELETE_MESSAGE_TOKEN:
	                // Update the notification for new messages since they may be deleted.
	                OmacpMessageNotification.nonBlockingUpdateNewMessageIndicator(OmacpMessageList.this, false);
	
	                // Make sure the list reflects the delete
	                startAsyncQuery();
	                onContentChanged();
	                break;
	                
	            default:
	                Xlog.e(XLOG, "OmacpMessageList onDeleteComplete called with unknown token " + token);
	                break;
            }
        }
    }
    
    public static void markAllMessagesAsSeen(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                blockingMarkAllMessagesAsSeen(context);
                OmacpMessageNotification.blockingUpdateNewMessageIndicator(context, false);
            }
        }).start();
    }
    
    private static void blockingMarkAllMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(OmacpProviderDatabase.CONTENT_URI,
                SEEN_PROJECTION,
                "seen=0",
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        if(DEBUG){
        	Xlog.i(XLOG, "OmacpMessageList mark " + count + " messages as seen.");
        }        

        ContentValues values = new ContentValues(1);
        values.put("seen", 1);

        resolver.update(OmacpProviderDatabase.CONTENT_URI,
                values,
                "seen=0",
                null);
    }  
    
    private void startAsyncQuery() {
        try {
            setTitle(getString(R.string.refreshing));
            setProgressBarIndeterminateVisibility(true);
            mQueryHandler.cancelOperation(MESSAGE_LIST_QUERY_TOKEN);
            mQueryHandler.startQuery(MESSAGE_LIST_QUERY_TOKEN, null, sAllMessagesUri, ALL_MESSAGES_PROJECTION, null, null, OmacpProviderDatabase.DEFAULT_SORT_ORDER);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }
    
    /**
     * Build and show the proper delete thread dialog. The UI is slightly different
     * depending on whether we're deleting a single thread or all threads.
     * @param listener gets called when the delete button is pressed
     * @param deleteAll whether to show a single thread or all threads UI
     * @param hasLockedMessages whether the thread(s) contain locked messages
     * @param context used to load the various UI elements
     */
    public static void confirmDeleteMessageDialog(final DeleteMessageListener listener, boolean deleteAll, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setMessage(deleteAll ? R.string.confirm_delete_all_messages : R.string.confirm_delete_message)
	        .setCancelable(true)
	        .setPositiveButton(R.string.delete, listener)
	        .setNegativeButton(R.string.no, null)
	        .show();
    }
    
    public static class DeleteMessageListener implements OnClickListener {
        private final long mMessageId;
        private final AsyncQueryHandler mHandler;
        private final Context mContext;

        public DeleteMessageListener(long messageId, AsyncQueryHandler handler, Context context) {
        	mMessageId = messageId;
        	mHandler = handler;
            mContext = context;
        }

        private void markAsRead(final long threadId){
        	Uri messageUri = ContentUris.withAppendedId(OmacpProviderDatabase.CONTENT_URI, mMessageId);

        	ContentValues readContentValues = new ContentValues(1);
        	readContentValues.put("read", 1);
        	mContext.getContentResolver().update(messageUri, readContentValues, "read=0", null);
        }
        
        public void onClick(DialogInterface dialog, final int whichButton) {
        	if(DEBUG){
        		Xlog.i(XLOG, "OmacpMessageList DeleteMessageListener onClick mMessageId is : " + mMessageId);
        	}
        	
        	new Runnable() {
                public void run() {
                    int token = DELETE_MESSAGE_TOKEN;
                    if (mMessageId == -1) {
                        mHandler.startDelete(token, null, OmacpProviderDatabase.CONTENT_URI, null, null);
                    } else {
                    	markAsRead(mMessageId);
                    	Uri uri = ContentUris.withAppendedId(OmacpProviderDatabase.CONTENT_URI, mMessageId);
                        mHandler.startDelete(token, null, uri, null, null);
                    }
                }
            }.run();
        }
    }
    
    private void openMessage(long messageId){
    	startActivity(OmacpMessageSettingsDetail.createIntent(this, messageId));
    }
    
    private String getMessageDetails(Context context, Cursor cursor){
    	if(cursor == null){
    		Xlog.e(XLOG, "OmacpMessageList getMessageDetails cursor is null.");
    		return null;
    	}
    	
    	StringBuilder details = new StringBuilder();
    	Resources res = context.getResources();

    	//Message Type: Configuration message.
    	details.append(res.getString(R.string.message_type_label));
    	details.append(res.getString(R.string.configuration_message));
       
    	//Sender: ***
    	details.append('\n');
    	details.append(res.getString(R.string.from_label));       
    	details.append(cursor.getString(SENDER));

    	// Date: ***
    	details.append('\n');
    	details.append(res.getString(R.string.received_label));
    	long date = cursor.getLong(DATE) * 1000;
    	details.append(OmacpMessageListItemData.formatTimeStampString(context, date, true));

    	return details.toString();
    }

}
