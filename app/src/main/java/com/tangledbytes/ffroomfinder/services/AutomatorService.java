package com.tangledbytes.ffroomfinder.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityEvent;

import com.tangledbytes.ffroomfinder.roomfinder.RoomFInder;
import com.tangledbytes.ffroomfinder.utils.AppConstants;
import com.tangledbytes.ffroomfinder.utils.Utils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AutomatorService extends AccessibilityService {
    private static final String TAG = "AutomatorService";
    private RoomFInder mAutomator;

    class AutomatorListener extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
	    try {
		if(intent.getAction().equals(AppConstants.AUTOMATOR_START)) {
		    mAutomator.onStart();
                }
	    } catch(Throwable tr) {
                Log.e(TAG, "Exception occurred in broadcast receiver!", tr);
	    }
	}
    }

    @Override
    protected void onServiceConnected() {
	Log.i(TAG, "Service Connected");
	updateServiceInfo();
        IntentFilter filter = new IntentFilter(AppConstants.AUTOMATOR_START);
	registerReceiver(new AutomatorListener(), filter);
	super.onServiceConnected();
    }

    @Override
    public void onDestroy() {
	Log.i(TAG, "Service destroyed!");
	super.onDestroy();
    }

    @Override
    public void onInterrupt() {
	Log.i(TAG, "Service interrupted!");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
	try {
           if(mAutomator != null)
                mAutomator.onEvent(event);
	} catch(Throwable tr) {
            Utils.showToast(getApplicationContext(), "Access: " + tr);
	}
    }

    public void updateServiceInfo() {
        mAutomator = new RoomFInder(this);
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.packageNames = getPackagesToHandle();
        info.flags = AccessibilityServiceInfo.DEFAULT |
            AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
            AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
    }

    private String[] getPackagesToHandle() {
        final List<String> packageNames = new ArrayList<>();
        packageNames.add("com.android.settings");
        packageNames.add(mAutomator.getPackageName());
        final String[] strArrayPackageNames = new String[packageNames.size()];
        for (int i = 0; i < packageNames.size(); i  ++)
            strArrayPackageNames[i] = packageNames.get(i);
        return strArrayPackageNames;
    }
}
