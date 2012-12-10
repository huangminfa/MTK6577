package com.mediatek.widgetdemos.musicplayer3d;


// class models the music player state machine
public class StatePattern {
	private String TAG = "MusicPlayer3d";
	//transition table
	private String[][] context ={
			//Space added in trackText is workaround for Redmine bug #525. 
			// Please do not remove unless the blurry issue is fixed in ngin3d. 
			{"Moneybrother",    "We Die Only Once ",    "monybrother.jpg"},
			{"Coldplay",        "Clocks",               "coldplay.jpg"},
			{"Daft Punk",       " Da Funk ",            "daft_punk.jpg"},
			{"Deportees",       " When They Come ",     "deportees.jpg"},
			{"James Blake",     "Limit To Your Love",   "james_blake.jpg"},
			{"Radiohead",       "Fake Plastic Trees ",  "radio_head.jpg"},
			{"Britney Spears",  "I Wanna Go",           "britney_spears.jpg"},
			{"Robyn",           "    Hang With Me    ", "robyn.jpg"},
			{"Pink Floyd",      "  Eclipse   ",         "pink_floyd.jpg"}
	};	
    
	private int mCurrent;		
	private int mPrevious;
    private boolean mSwipeUp;
    private boolean mSwipeDirChange;
	
	public StatePattern() {
		mCurrent = 1;
		mPrevious =0;
	    mSwipeUp = true;
	    mSwipeDirChange = false;
	}
	
	public void updateState() {
		mPrevious = mCurrent;
		if(mSwipeUp)
			mCurrent = (Integer) ((mCurrent < (context.length)-1)? mCurrent + 1 : 0);
		else
		    mCurrent = (Integer)((mCurrent > 0)? mCurrent-1: (context.length)-1);
	}
	
	public boolean getSwipeState(){
		return mSwipeUp;
	}
	
	public String[] getContext(int index){
		return context[index];
	}
	
	public int getContextLength(){
		return context.length;
	}

	public int getCurrent(){
		if (ismSwipeDirChange()){
			if(mSwipeUp){
				//swipe direction change from down to up. Move mCurrent + 2
				mCurrent = (Integer) ((mCurrent < (context.length)-1)? mCurrent + 1 : 0);
				mCurrent = (Integer) ((mCurrent < (context.length)-1)? mCurrent + 1 : 0);
			}else{
			    //swipe direction change from up to down. Move mCurrent - 2
				mCurrent = (Integer)((mCurrent > 0)? mCurrent-1: (context.length)-1);
				mCurrent = (Integer)((mCurrent > 0)? mCurrent-1: (context.length)-1);
			}
		}
		return mCurrent;
	}
	
	public int getPrevious(){
		return mPrevious;
	}
		
	public void setSwipeState(boolean state){
		if (mSwipeUp == state){
			setmSwipeDirChange(false);
		}else{
			setmSwipeDirChange(true);
		}
		mSwipeUp = state;
	}
	
	public void setmSwipeDirChange(boolean mSwipeDirChange) {
		this.mSwipeDirChange = mSwipeDirChange;
	}

	public boolean ismSwipeDirChange() {
		return mSwipeDirChange;
	}
}


