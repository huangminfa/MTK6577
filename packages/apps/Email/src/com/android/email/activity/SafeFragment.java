package com.android.email.activity;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Handler;

public class SafeFragment extends Fragment {

    private ArrayList<Runnable> mList = new ArrayList<Runnable>();

    private Handler mHandler = new Handler();

    protected void addResumeRunnable(Runnable run) {
        mList.add(run);
    }

    @Override
    public void onResume() {
        super.onResume();
        for (Runnable run : mList) {
            mHandler.post(run);
        }
        mList.clear();
    }
}
