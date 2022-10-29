package com.tangledbytes.ffroomfinder.utils;

import android.os.Environment;
import java.io.File;

public abstract class AppConstants {
    public static final String AUTOMATOR_START = "com.tamgledbytes.automator.action.AUTOMATOR_START";
    
    /////////////////////////
    // Package name of app //
    /////////////////////////
    public static final String PACKAGE_NAME = "com.tangledbytes.automator";

    ///////////////////////////////////////
    // Constants for controlling logging //
    ///////////////////////////////////////
    public static final boolean DEBUG = true;
    public static final boolean EXTREME_LOGGING = false;

    //////////////////////////
    // File names and paths //
    //////////////////////////
    public static final File EXTERNAL_STORAGE_DIR = Environment.getExternalStorageDirectory();
    
    /////////////////////
    // Preference keys //
    /////////////////////
    public static final class preference {
	private preference() {}
    }
}
