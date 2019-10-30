package com.wrappy.android.common;


public interface Backable {
    /**
     * This method is triggered via {@link android.app.Activity#onBackPressed()}
     * @return true if back button was consumed by this method.
     */
    boolean onBackPressed();
}
