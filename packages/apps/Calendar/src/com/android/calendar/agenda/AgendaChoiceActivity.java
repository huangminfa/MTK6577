package com.android.calendar.agenda;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.android.calendar.CalendarController;
import com.android.calendar.R;
import com.android.calendar.Utils;

///M:This class is for Choice calendar item 
public class AgendaChoiceActivity extends Activity {
    private static String KEY_OTHER_APP_RESTORE_TIME = "other_app_request_time";
    public int mAgendaReqType = Utils.REQ_TYPE_AGENDA_CHOICE_EVENT;
    
    private CalendarController mController;
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // This needs to be created before setContentView
        mController = CalendarController.getInstance(this);
        setContentView(R.layout.agenda_choice);
        
        long timeMillis = -1;
        if (icicle != null) {
            timeMillis = icicle.getLong(KEY_OTHER_APP_RESTORE_TIME);
        } else {
            timeMillis = System.currentTimeMillis();
        }

        setFragments(timeMillis, mAgendaReqType);
    }

    private void setFragments(long timeMillis, int viewType){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        AgendaFragment frag = new AgendaFragment(timeMillis, false);
        frag.setAgendaRequestType(mAgendaReqType);
        ft.replace(R.id.agenda_choice_frame, frag);
        ft.commit();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_OTHER_APP_RESTORE_TIME, mController.getTime());
    }
   
}
///@}
