package com.android.phone;

//Add by lei.wang@archmerind.com
public interface CallBarringInterface {
    public void doCancelAllState();
    public void doCallBarringRefresh(String state);
    public int getErrorState();
    public void setErrorState(int state);
}
