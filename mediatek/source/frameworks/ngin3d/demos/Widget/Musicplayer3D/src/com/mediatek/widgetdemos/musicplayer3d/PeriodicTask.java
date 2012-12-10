package com.mediatek.widgetdemos.musicplayer3d;

import java.util.Calendar;

import android.os.Handler;

// A user-defined task that runs periodically using Android Handlers (not threaded)
abstract public class PeriodicTask {
    private Handler mHandler = new Handler();
    private int mPeriod;
    private int mRepetitions;
    private boolean mSynchronize;
    private int mOffset;
    private long mStartTime;
    private int mRunCount;

    private Runnable mTask = new Runnable() {
        public void run() {
            // Reschedule the task if we have repetitions remaining
            if (mRepetitions <= 0 || mRunCount < mRepetitions - 1) {
                schedule();
            } else {
                stop();
            }

            // Call the actual task run function
            int elapsedTime = (int) (System.currentTimeMillis() - mStartTime);
            PeriodicTask.this.run(mRunCount, elapsedTime);
            ++mRunCount;
        }
    };

    // Creates an infinitely repeating task that runs with a period of milliseconds
    public PeriodicTask(int period) {
        initialize(period, 0, false, 0);
    }

    // Creates a periodic task that repeats only a specified number of times
    public PeriodicTask(int period, int repetitions) {
        initialize(period, repetitions, false, 0);
    }

    // Creates an infinitely repeating task which is synchronised to the second
    // with the system clock
    public PeriodicTask(int period, boolean synchronize) {
        initialize(period, 0, synchronize, 0);
    }

    // Creates a synchronised, infinitely repeating task which is offset with
    // respect to the system clock
    public PeriodicTask(int period, boolean synchronize, int offset) {
        initialize(period, 0, synchronize, offset);
    }

    // Creates a finitely repeating, synchronised task
    public PeriodicTask(int period, int repetitions, boolean synchronize) {
        initialize(period, repetitions, synchronize, 0);
    }

    // Create a finitely repeating, synchronised, offset task
    public PeriodicTask(int period, int repetitions, boolean synchronize, int offset) {
        initialize(period, repetitions, synchronize, offset);
    }

    // Called by the constructors to set the task configuration
    private void initialize(int period, int repetitions, boolean synchronize, int offset) {
        mPeriod = period;
        mRepetitions = repetitions;
        mSynchronize = synchronize;
        mOffset = offset;
    }

    // Start the task immediately
    public void start() {
        stop();
        mStartTime = System.currentTimeMillis();
        mRunCount = 0;
        mHandler.post(mTask);
    }

    // Schedule the task to run after one period has elapsed
    public void startDelayed() {
        stop();
        mStartTime = System.currentTimeMillis();
        mRunCount = 0;
        schedule();
    }

    // Stop the task (works on all tasks)
    public void stop() {
        mHandler.removeCallbacks(mTask);
    }

    private void schedule() {
        stop();

        int delay = mPeriod;

        // Do we want to synchronize per second with the system clock?
        if (mSynchronize) {
            Calendar calendar = Calendar.getInstance();
            int milliseconds = calendar.get(Calendar.MILLISECOND);

            if (milliseconds >= mOffset) {
                milliseconds -= mOffset;
            } else {
                milliseconds += mOffset;
            }

            delay -= milliseconds;
        }

        mHandler.postDelayed(mTask, delay);
    }

    // The actual function which is called periodically
    // Users should implement this to make the task do what they want
    // runCount starts from zero, and elapsedTime measures the time since the
    // task was started
    abstract public void run(int runCount, int elapsedTime);
}

