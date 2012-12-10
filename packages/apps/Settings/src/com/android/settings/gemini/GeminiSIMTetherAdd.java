package com.android.settings.gemini;

import com.android.settings.R;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Data;
import android.provider.Telephony.SIMInfo;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;

public class GeminiSIMTetherAdd extends Activity implements Button.OnClickListener, OnItemClickListener{
	private static final String TAG = "GeminiSIMTetherAdd";
	private static final int DIALOG_WAITING = 1001;
	private static final int DIALOG_LOADING = DIALOG_WAITING+1;
	private static final int MESSAGE_SAVE_FINISHED = 1002;
	private static final int MENU_ID_CANCEL = Menu.FIRST;
    private static final int MENU_ID_SAVE= Menu.FIRST + 1;
	private static boolean isNeedSave = false;
	private static boolean isSaving = false;
	private static GeminiSIMTetherAdapter mAdapter;
	private ArrayList<GeminiSIMTetherItem> mDataList = new ArrayList<GeminiSIMTetherItem>();
	
	private LinearLayout mNoContactNoticeView;
	private LinearLayout mContactSelectView;
	private LinearLayout mWhole;
	
	private GeminiSIMTetherMamager mMamager;
	private ListView listView;
	private CheckBox mSelectAllBtn;
	//private Button mSelectNoneBtn;
	private Button mNoContactConfirmBtn;
	private Dialog needSaveAlertDialog;
	private MyAsyncTask mAsyncTask;
	private volatile boolean isRefresh=false;
	private volatile boolean mNeedRefresh=false;
	private final Context mContext=this;
	private ContentObserver MyContentObserver=new ContentObserver(new Handler()){

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (isSaving){
				Xlog.d(TAG,"isSaving="+isSaving);
				return;
			}
			if (isRefresh) {
				Xlog.d(TAG,"isRefresh="+isRefresh);
				mNeedRefresh=true;
			}else{
				Xlog.d(TAG,"isRefresh="+isRefresh);
				if(mAsyncTask!=null){
					mAsyncTask.cancel(true);
				}
				MyAsyncTask mySync=new MyAsyncTask(mContext);
				mAsyncTask=(MyAsyncTask) mySync.execute();
			}
			Xlog.d(TAG,"onChange selfChange="+selfChange);
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gemini_sim_tether_info_add);
		mMamager = GeminiSIMTetherMamager.getInstance(this);
		
		String simDisplayName = "";
		String mCurrSIMId = mMamager.getCurrSIMID();
		long simId = Integer.parseInt(mCurrSIMId);
        SIMInfo simInfo = SIMInfo.getSIMInfoById(this, simId);
        int simCount = SIMInfo.getInsertedSIMCount(this);
        if(simCount > 1 && simInfo != null){
            simDisplayName = simInfo.mDisplayName;
        }
        if(simDisplayName != null && !simDisplayName.equals("")){
            this.setTitle(simDisplayName);
        }
        
        mWhole = (LinearLayout)findViewById(R.id.whole_view);
		if (FeatureOption.MTK_THEMEMANAGER_APP) {
//			mWhole.setThemeContentBgColor(0xff000000);
        }
		mWhole.setVisibility(View.INVISIBLE);
        mNoContactNoticeView = (LinearLayout)findViewById(R.id.gemini_sim_tether_no_contact_view);
        mContactSelectView = (LinearLayout)findViewById(R.id.gemini_sim_tether_contact_view);
        
        mNoContactConfirmBtn = (Button)findViewById(R.id.gemini_sim_tether_nocontact_confirm_btn);
        if(mNoContactConfirmBtn!=null){
            mNoContactConfirmBtn.setOnClickListener(this);
        }
		listView = (ListView)findViewById(android.R.id.list);
		mSelectAllBtn = (CheckBox)findViewById(R.id.gemini_sim_tether_select_all);
		//mSelectNoneBtn = (Button)findViewById(R.id.gemini_sim_tether_select_none);
		if(mSelectAllBtn!=null){
		    mSelectAllBtn.setOnClickListener(this);
		}
		//mSelectNoneBtn.setOnClickListener(this);
		
		isNeedSave = false;
		//listView.setAdapter(new ArrayAdapter<String>(this, R.layout.gemini_sim_tether_info_item, values));
		MyAsyncTask mySync=new MyAsyncTask(this);
		mAsyncTask=(MyAsyncTask) mySync.execute();
	}
	

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
	    if(mDataList.size()>0) {
	        menu.add(Menu.NONE, MENU_ID_CANCEL, Menu.NONE, R.string.gemini_sim_tether_select_cancel)
                .setEnabled(true)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(Menu.NONE, MENU_ID_SAVE,  Menu.NONE, R.string.gemini_sim_tether_select_ok)
                 .setEnabled(isNeedSave)
                 .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	    }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ID_CANCEL:
        	setResult(RESULT_CANCELED);
            revertAllTetherChange();
            break;
        case MENU_ID_SAVE:
            saveTetherConfigs();
            break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

	@Override
	public void onResume(){
		super.onResume();
		Xlog.d(TAG,"onResume");
		listView.invalidateViews();
		this.getApplicationContext().getContentResolver()
		.registerContentObserver(GeminiSIMTetherMamager.GEMINI_TETHER_URI, true, MyContentObserver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		boolean isCanceled=false;
		if (mAsyncTask != null){
			isCanceled = mAsyncTask.cancel(true);
		}
		Xlog.d(TAG,"onDestroy---isCanceled="+isCanceled);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
	    super.onConfigurationChanged(newConfig);
	}
 
	private Handler mCheckHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(GeminiSIMTetherAdapter.FLAG_CHECKBOX_MESSAGE == msg.what){
                Xlog.i(TAG, "check box at position [" + msg.arg1+ "] new state is " + 
                        (msg.arg2==GeminiSIMTetherAdapter.FLAG_CHECKBOX_STSTUS_CHECKED?"checked":"unchecked"));
                isNeedSave = true;
                GeminiSIMTetherAdd.this.invalidateOptionsMenu();
                mSelectAllBtn.setChecked(false);
            }
            super.handleMessage(msg);
        }
    };
    
    private Handler mSaveProgressHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == MESSAGE_SAVE_FINISHED) {
                Xlog.i(TAG, "tether info save finished");
                removeDialog(DIALOG_WAITING);
                isSaving = false;
                Xlog.i(TAG, "saveTetherConfigs(), end");
                setResult(RESULT_OK);
                finish();
            }
        }
    };	
    class MyAsyncTask extends AsyncTask<Void,Void,Void>{
		private Context context;
		public MyAsyncTask(Context ct) {
			Xlog.i(TAG, "MyAsyncTask constructor");
			context = ct;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Xlog.d(TAG,"onPreExecute");
			showDialog(DIALOG_LOADING);
		}
		@Override
		protected void onPostExecute(Void result) {
			Xlog.i(TAG, "onPostExecute");
			removeDialog(DIALOG_LOADING);
			super.onPostExecute(result);
			mWhole.setVisibility(View.VISIBLE);
			boolean isHaveContact = false;
			mSelectAllBtn.setChecked(false);
			if(mDataList != null){
			    int contactSize = mDataList.size();
			    if(contactSize > 0){
			        isHaveContact = true;
			    }
			    boolean isSomeUnselected = false;
			    for(int i=0;i<contactSize;i++){
			        GeminiSIMTetherItem item = mDataList.get(i);
			        if(item.getCheckedStatus() != GeminiSIMTetherAdapter.FLAG_CHECKBOX_STSTUS_CHECKED){
			            isSomeUnselected = true;
			            break;
			        }
			    }
			    if(!isSomeUnselected){
			        mSelectAllBtn.setChecked(true);
			    }
			}
			
			if(isHaveContact){
			    mNoContactNoticeView.setVisibility(View.GONE);
			    mContactSelectView.setVisibility(View.VISIBLE);
			}else{
			    mNoContactNoticeView.setVisibility(View.VISIBLE);
	            mContactSelectView.setVisibility(View.GONE);
			}
			mAdapter = new GeminiSIMTetherAdapter(context, mDataList, mCheckHandler);
			if(listView != null){
			    listView.setAdapter(mAdapter);
			    listView.setOnItemClickListener((OnItemClickListener) context);
			}
			isRefresh = false;
		}
		@Override
		protected Void doInBackground(Void... params) {
			isRefresh = true;
			do {
				mNeedRefresh = false;
				Xlog.d(TAG,"before---mNeedRefresh="+mNeedRefresh);
				mDataList = mMamager.getAllContactData();
				Xlog.d(TAG,"after---mNeedRefresh="+mNeedRefresh);
			}while( mNeedRefresh );
			
			return null;
		}
	}
    @Override
	protected void onPause() {
		super.onPause();
		Xlog.d(TAG,"onPause");
		this.getApplicationContext().getContentResolver().unregisterContentObserver(MyContentObserver);
	}

	public void initDataList(){
	    Xlog.i(TAG, "initDataList(), begin");
	    //if no contact, hide contact select view
	    boolean isHaveContact = false;
		mDataList = mMamager.getAllContactData();
		mSelectAllBtn.setChecked(false);
		if(mDataList != null){
		    int contactSize = mDataList.size();
		    if(contactSize > 0){
		        isHaveContact = true;
		    }
		    boolean isSomeUnselected = false;
		    for(int i=0;i<contactSize;i++){
		        GeminiSIMTetherItem item = mDataList.get(i);
		        if(item.getCheckedStatus() != GeminiSIMTetherAdapter.FLAG_CHECKBOX_STSTUS_CHECKED){
		            isSomeUnselected = true;
		            break;
		        }
		    }
		    if(!isSomeUnselected){
		        mSelectAllBtn.setChecked(true);
		    }
		}
		
		if(isHaveContact){
		    mNoContactNoticeView.setVisibility(View.GONE);
		    mContactSelectView.setVisibility(View.VISIBLE);
		}else{
		    mNoContactNoticeView.setVisibility(View.VISIBLE);
            mContactSelectView.setVisibility(View.GONE);
		}
		Xlog.i(TAG, "initDataList(), end");
	}
	
	@Override
	public void onClick(View v) {
		if(v == mSelectAllBtn){
		    if(mSelectAllBtn.isChecked()){
		        Xlog.i(TAG, "Select All Contact");
		        setAllContactSelected(true);
		    }else{
		        Xlog.i(TAG, "Select None Contact");
		        setAllContactSelected(false);
		    }
		}
		else if(v == mNoContactConfirmBtn){
			setResult(RESULT_FIRST_USER);
		    finish();
		}
	}
	
	/**
	 * select all contact list in the adapter
	 */
	public void setAllContactSelected(boolean checked){
		isNeedSave = true;
		this.invalidateOptionsMenu();
		if(listView != null){
			int count = mDataList.size();
			for(int i=0;i<count;i++){
			    mDataList.get(i).setCheckedStatus(checked?GeminiSIMTetherAdapter.FLAG_CHECKBOX_STSTUS_CHECKED:GeminiSIMTetherAdapter.FLAG_CHECKBOX_STSTUS_UNCHECKED);
			}
			listView.invalidateViews();
		}
	}
	
	public void saveTetherConfigs(){
	    Xlog.i(TAG, "saveTetherConfigs(), begin");
		if(isNeedSave == true && listView != null){
		    showDialog(DIALOG_WAITING);
		    isSaving = true;
		    new Thread(){
		        @Override
		        public void run(){
		            ArrayList<Integer> tetheredContactList = new ArrayList<Integer>();
		            int count = mDataList.size();
		            for(int i=0;i<count;i++){
		                //View itemView = listView.getChildAt(i);
		                GeminiSIMTetherItem item = (GeminiSIMTetherItem)mDataList.get(i);
		                int checkedStatus = item.getCheckedStatus();
		                if(checkedStatus == GeminiSIMTetherAdapter.FLAG_CHECKBOX_STSTUS_CHECKED){
		                    int contactId = item.getContactId();
		                    tetheredContactList.add(new Integer(contactId));
		                }
		            }
		            mMamager.setCurrTetheredNum(tetheredContactList);
		            mSaveProgressHandler.sendEmptyMessage(MESSAGE_SAVE_FINISHED);
		        }
		    }.start();
		}
	}
	
	public void revertAllTetherChange(){
		if(isNeedSave){
		    if(needSaveAlertDialog == null){
		        needSaveAlertDialog = new AlertDialog.Builder(this)
		        .setTitle(R.string.gemini_sim_tether_revert_title)
		        .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
		        .setMessage(R.string.gemini_sim_tether_revert_message)
		        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		                finish();
		            }
		        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        needSaveAlertDialog.dismiss();
                        needSaveAlertDialog=null;
                    }
                }).create();
		        needSaveAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        Xlog.d(TAG, "needSaveAlertDialog is dismissed");
                        if(needSaveAlertDialog!=null){
                            needSaveAlertDialog.dismiss();
                            needSaveAlertDialog = null;
                        }
                    }
                });
		        needSaveAlertDialog.show();
		    }else{
		        Xlog.i(TAG, "Too frequently operation, just show one dialog");
		    }
		}else{
			finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if(keyCode == KeyEvent.KEYCODE_BACK){
		    if(!isSaving){
		        revertAllTetherChange();
		    }
			return true;
		}
		return false;
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
          switch (id) {
            case DIALOG_WAITING:
                ProgressDialog progressDialog = new ProgressDialog(this);                
                progressDialog.setMessage(getResources().getString(R.string.gemini_tether_saving_progress_message));
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                return progressDialog;
            case DIALOG_LOADING:
                ProgressDialog progressDialog1 = new ProgressDialog(this);                
                progressDialog1.setMessage(getResources().getString(R.string.settings_license_activity_loading));
                progressDialog1.setIndeterminate(true);
                progressDialog1.setCancelable(false);
                return progressDialog1;
            default:
               return null;
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        // mDataList.get(position).setCheckedStatus(checkBoxNewState);
        CheckBox checkBox = (CheckBox)view.findViewById(R.id.gemini_contact_check_btn);
        if(checkBox!=null){
            boolean isChecked = checkBox.isChecked();
            checkBox.setChecked(!isChecked);
            int checkBoxNewState = !isChecked ? GeminiSIMTetherAdapter.FLAG_CHECKBOX_STSTUS_CHECKED:
                        GeminiSIMTetherAdapter.FLAG_CHECKBOX_STSTUS_UNCHECKED;
            mDataList.get(position).setCheckedStatus(checkBoxNewState);
            
            int count = mDataList.size();
            boolean allIsSelected = true;
            for(int i=0;i<count;i++){
                if(mDataList.get(i).getCheckedStatus()==GeminiSIMTetherAdapter.FLAG_CHECKBOX_STSTUS_UNCHECKED){
                    allIsSelected = false;
                    break;
                }
            }
            isNeedSave = true;
            this.invalidateOptionsMenu();
            mSelectAllBtn.setChecked(allIsSelected);
        }else{
            Xlog.e(TAG, "onItemClick(), fail to get checkbox object");
        }
    }
}
