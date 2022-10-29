package com.tangledbytes.ffroomfinder.roomfinder;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Path;
import android.net.Uri;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import android.util.Log;

import java.util.List;

public abstract class BaseRoomFinder {
    private static final String TAG = "BaseRoomFinder";
    private AccessibilityService mService;
    private Context mContext;

    public abstract String getPackageName();
    public abstract void onStart();
    public abstract void onEvent(AccessibilityEvent event);

    public BaseRoomFinder(Context context) {
        mContext = context;
    }

    public BaseRoomFinder(AccessibilityService service) {
        mService = service;
    }

    protected Context getContext() {
        if (mContext == null)
            return getService();
        return mContext;
    }

    protected AccessibilityService getService() {
        return mService;
    }

    protected AccessibilityNodeInfo getRootInActiveWindow() {
        int noOfTries = 0;
        AccessibilityNodeInfo rootNode;
        while ((rootNode = getService().getRootInActiveWindow()) == null && noOfTries <= 10) {
            sleep(200);
            noOfTries++;
        }
        return rootNode;
    }

    /**
     * Handy method for waiting.
     * @param millis Waiting time
     */
    protected void sleep(long millis) {
        try {
            synchronized (this) {
                wait(millis);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "sleep(Object, long): Failed to wait", e);
        }
    }

	protected void performGesture(int fromX, int fromY, int toX, int toY) {
		performGesture(fromX, fromY, toX, toY, 500);
	}
	
	protected void performGesture(int fromX, int fromY, int toX, int toY, int duration) {
		if(duration < 150)
			duration = 150;
		Path path = new Path();
        path.moveTo(fromX, fromY);
		path.lineTo(toX, toY);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription gestureDescription = builder
            .addStroke(new GestureDescription.StrokeDescription(path, 10, duration - 100))
            .build();
        getService().dispatchGesture(gestureDescription, null, null);
		sleep(duration);
	}

    protected void clickView(String viewIdResourceName) {
        AccessibilityNodeInfo rootNodeInfo = mService.getRootInActiveWindow();
        if (rootNodeInfo == null) {
            Log.w(TAG, "click(String): Could not retrive widnow content!");
            return;
        }
        List<AccessibilityNodeInfo> nodesToClick = rootNodeInfo
            .findAccessibilityNodeInfosByViewId(viewIdResourceName);
        clickAllNodes(nodesToClick);
    }

	protected void clickAt(int x, int y) {
		clickAt(x, y, 100);
	}
	
    protected void clickAt(int x, int y, int duration) {
		if(duration < 100)
			duration = 100;
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription gestureDescription = builder
            .addStroke(new GestureDescription.StrokeDescription(path, 10, 100))
            .build();
        getService().dispatchGesture(gestureDescription, null, null);
		sleep(duration);
    }
	
    protected boolean clickAllNodes(List<AccessibilityNodeInfo> nodesToClick) {
        boolean clickedAnyNode = false;
        for (AccessibilityNodeInfo node : nodesToClick) {
            if (node.isClickable() && node.isEnabled()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                clickedAnyNode = true;
            }
        }
        return clickedAnyNode;
    }

    protected void launchApp(String packageName) {
        Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(packageName);
        getContext().startActivity(intent);
    }

    protected void closeApp(String packageName) {
        // Start settings
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        mService.startActivity(intent);
        sleep(3500);

        // Try to find out what it says on the Force Stop button (different in different languages)
        String forceStopButtonName = null;
        try {
            String resourcesPackageName = "com.android.settings";
            Resources resources =
                getContext().getPackageManager().getResourcesForApplication(resourcesPackageName);
            int resourceId = resources.getIdentifier("force_stop", "string", resourcesPackageName);
            if (resourceId > 0)
                forceStopButtonName = resources.getString(resourceId);
            else
                Log.e(TAG, "Label for the force stop button in settings could not be found");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Settings activity's resources not found");
        }

        // Click on force stop button
        List<AccessibilityNodeInfo> nodesForceStop =
            getRootInActiveWindow().findAccessibilityNodeInfosByText("FORCE STOP");
        if (nodesForceStop.isEmpty())
            nodesForceStop =
                getRootInActiveWindow()
                .findAccessibilityNodeInfosByViewId("com.android.settings:id/force_stop_button");

        if (nodesForceStop.isEmpty())
            nodesForceStop =
                getRootInActiveWindow()
                .findAccessibilityNodeInfosByText(forceStopButtonName);

        if (nodesForceStop.isEmpty())
            nodesForceStop = 
                getRootInActiveWindow()
                .findAccessibilityNodeInfosByViewId("com.android.settings:id/right_button");

        if (!clickAllNodes(nodesForceStop))
            return;

        sleep(1500); // Wait for rendering of diakog

        // Click on okay button from dialog
        List<AccessibilityNodeInfo> nodesOkayButton =
            getRootInActiveWindow()
            .findAccessibilityNodeInfosByText(getContext().getString(android.R.string.ok));
        if (nodesOkayButton.isEmpty())
		// In some phones 'Ok' button is sometimes labled as 'Force Stop'.
            nodesOkayButton =
                getRootInActiveWindow()
                .findAccessibilityNodeInfosByText(forceStopButtonName);
        if (nodesOkayButton.isEmpty())
            nodesOkayButton =
                getRootInActiveWindow()
                .findAccessibilityNodeInfosByViewId("android:id/button1");
        clickAllNodes(nodesOkayButton);   
    }

    protected void restartApp(String packageName) {
        closeApp(packageName);
        sleep(1500);

        // Launch again the stopped packge
        launchApp(packageName);
    }
}
