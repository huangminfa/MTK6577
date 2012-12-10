package com.android.settings.gemini;

import com.android.settings.R;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony.SIMInfo;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.mediatek.featureoption.FeatureOption;

import com.mediatek.xlog.Xlog;

public class GeminiSIMTetherInfo extends Activity{
	private static final String TAG = "GeminiSIMTetherInfo";
	static final String[] valueDs={"AAA", "BBB", "CCC"};
	private static final int DIALOG_WAITING=1001;
	private static GeminiSIMTetherAdapter mAdapter;
	private ArrayList<GeminiSIMTetherItem> mAdpaterData = new ArrayList<GeminiSIMTetherItem>();
	
	private LinearLayout mWhole;
	private ListView listView;
	private TextView textView;
	private ScrollView mScrollview;
	private Button addBtn;
	private GeminiSIMTetherMamager mMamager;
	private String currSIMID;
	private MyAsyncTask mAsyncTask;
	private boolean mhasRecord;
	private volatile boolean isRefresh=false;//Since two thread will access this variable add volatile type
	private volatile boolean mNeedRefresh=false;
	private final Context mContext=this;
	private ContentObserver MyContentObserver=new ContentObserver(new Handler()){
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (isRefresh) {
				Xlog.d(TAG,"isRefresh="+isRefresh);
				mNeedRefresh=true;
			}else{
				Xlog.d(TAG,"isRefresh="+isRefresh);
				if (mAsyncTask!=null){
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
		setContentView(R.layout.gemini_sim_tether_info);
		
		mMamager = GeminiSIMTetherMamager.getInstance(this);

		mWhole = (LinearLayout)findViewById(R.id.whole_view);
		if (FeatureOption.MTK_THEMEMANAGER_APP) {
//			mWhole.setThemeContentBgColor(0xff000000);
		}
		
		listView = (ListView)findViewById(android.R.id.list);
		textView = (TextView)findViewById(R.id.no_record_notice);
		mScrollview = (ScrollView)findViewById(R.id.scrollview_record);
		
		addBtn = (Button)this.findViewById(R.id.add_btn);
		if(addBtn!=null){
		    addBtn.setOnClickListener(new Button.OnClickListener(){
		        @Override
		        public void onClick(View arg0) {
		            addContacts();
		        }
		    });
		}
		
		long simId = getIntent().getLongExtra("simid", -1);
		Xlog.i(TAG, "onCreate(), simid="+simId);
		SIMInfo simInfo = SIMInfo.getSIMInfoById(this, simId);
		int simCount = SIMInfo.getInsertedSIMCount(this);
		String simDisplayName = "";
		if(simCount > 1 && simInfo != null){
		    simDisplayName = simInfo.mDisplayName;
		}
		if(simDisplayName != null && !simDisplayName.equals("")){
		    this.setTitle(simDisplayName);
		}
		currSIMID = String.valueOf(simId);
		mMamager.setCurrSIMID(currSIMID);
		
		//listView.setAdapter(new ArrayAdapter<String>(this, R.layout.gemini_sim_tether_info_item, values));
		if(listView != null){
		    listView.setOnItemClickListener(new OnItemClickListener() {
		        @Override
		        public void onItemClick(AdapterView<?> parent, View view, int position,
		                long id) {
		            Xlog.i(TAG, "Tether record at " + position + " is clicked" );
		        }
		    });
		}
		hideInformation();
		MyAsyncTask mySync=new MyAsyncTask(this);
		mAsyncTask=(MyAsyncTask) mySync.execute();
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Xlog.d(TAG,"onPause");
		this.getApplicationContext().getContentResolver().unregisterContentObserver(MyContentObserver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Xlog.d(TAG,"onResume");
		this.getApplicationContext().getContentResolver()
		.registerContentObserver(GeminiSIMTetherMamager.GEMINI_TETHER_URI, true, MyContentObserver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		boolean isCanceled=false;
		if(mAsyncTask != null){
			isCanceled = mAsyncTask.cancel(true);
		}
		Xlog.d(TAG,"onDestroy---isCanceled="+isCanceled);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case DIALOG_WAITING:
			ProgressDialog progressDialog = new ProgressDialog(this);                
            progressDialog.setMessage(getResources().getString(R.string.settings_license_activity_loading));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            return progressDialog;
		default:
			return null;
		}
	}
	private void hideInformation() {
		textView.setVisibility(View.GONE);
		mScrollview.setVisibility(View.GONE);
		listView.setVisibility(View.GONE);
	}

	private void updateView (boolean isRecord) {
		Xlog.d(TAG, "isRecord="+isRecord);
		if(isRecord){	//record do exist
			textView.setVisibility(View.GONE);
			mScrollview.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}else{	//no record found,  just give a notice
			textView.setVisibility(View.VISIBLE);
			mScrollview.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		}
	}
	public void addContacts(){
		Xlog.i(TAG, "Begin to add contacts now");
		boolean isCanceled;
		isCanceled = mAsyncTask.cancel(true);
		Xlog.d(TAG,"addContacts()---isCanceled="+isCanceled);
		Intent intent = new Intent();
		intent.setClass(this, GeminiSIMTetherAdd.class);
		this.startActivityForResult(intent, RESULT_CANCELED);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			Xlog.d(TAG,"onActivityResult() requestCode="+requestCode+" resultCode="+resultCode);
			hideInformation();
			if (resultCode==RESULT_OK){
				MyAsyncTask mySync=new MyAsyncTask(this);
				mAsyncTask=(MyAsyncTask) mySync.execute();
			} else {
				if (resultCode == RESULT_CANCELED) {
					updateView(mhasRecord);
				} else {
					if (resultCode == RESULT_FIRST_USER) {
						updateView(false);
					}
				}
			}
	}
	public void initDataList(){
		mAdpaterData = mMamager.getCurrSimData();
		Xlog.d(TAG, "Database query size = " + mAdpaterData.size());
	}
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
			showDialog(DIALOG_WAITING);
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Xlog.i(TAG, "onPostExecute");
			removeDialog(DIALOG_WAITING);
			mAdapter = new GeminiSIMTetherAdapter(context, mAdpaterData);
			listView.setAdapter(mAdapter);
			mhasRecord = mAdpaterData.size()>0;
			updateView(mhasRecord);
			isRefresh=false;
			Xlog.d(TAG,"onPostExecute()+ mhasRecord="+mhasRecord);
		}
		@Override
		protected Void doInBackground(Void... params) {
			Xlog.d(TAG, "doInBackground()");
			isRefresh = true;
			do {
				mNeedRefresh = false;
				Xlog.d(TAG,"before---mNeedRefresh="+mNeedRefresh);
				initDataList();
				Xlog.d(TAG,"after---mNeedRefresh="+mNeedRefresh);
			}while( mNeedRefresh );
			
			return null;
		}
	}
}
