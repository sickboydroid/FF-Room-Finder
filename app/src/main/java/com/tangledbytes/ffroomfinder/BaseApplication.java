package com.tangledbytes.ffroomfinder;

import android.app.Application;

/**
 * This is invoked before any of the other app components are invoked in app.
 */
public class BaseApplication extends Application {
    private final String TAG = "BaseApplication";

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
