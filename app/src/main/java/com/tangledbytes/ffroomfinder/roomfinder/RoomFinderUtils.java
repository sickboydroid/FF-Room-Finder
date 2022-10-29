package com.tangledbytes.ffroomfinder.roomfinder;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import android.util.Log;

public class RoomFinderUtils {
    private static final String TAG = "AutomatorUtils";
    private final BaseRoomFinder mAutomator;

    public RoomFinderUtils(BaseRoomFinder automator) {
        mAutomator = automator;
    }

    public static void debugClick(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_VIEW_CLICKED)
            return;
        AccessibilityNodeInfo nodeInfo = event.getSource();
        if (nodeInfo == null) {
            return;
        }
        final String SPACE = "\n\t\t\t";
        String strNodeInfo = SPACE +
                "-----------------" +
                SPACE +
                "View Debug Info: " +
                SPACE +
                "From package: " + nodeInfo.getPackageName() +
                SPACE +
                "Classname: " + nodeInfo.getClassName() +
                SPACE +
                "ViewID: " + nodeInfo.getViewIdResourceName() +
                SPACE +
                "Text: " + nodeInfo.getText() +
                SPACE +
                "IsClickable: " + nodeInfo.isClickable() +
                SPACE +
                "-----------------";
        Log.v(TAG, strNodeInfo);
    }

    /**
     * @see RoomFinderUtils#logViewHierarchy(AccessibilityNodeInfo, int)
     */
    public static void logViewHierarchy(AccessibilityNodeInfo rootNode) {
        logViewHierarchy(rootNode, 0);
    }

    /**
     * Logs view hierarchy in a very understandable manner
     *
     * @see RoomFinderUtils#logViewHierarchy(AccessibilityNodeInfo)
     */
    private static void logViewHierarchy(AccessibilityNodeInfo rootNode, final int depth) {
        if (rootNode == null) return;

        StringBuilder spacerString = new StringBuilder();
        for (int i = 0; i < depth; ++i) {
            spacerString.append('-');
        }
        // Log the view with its important info here
        Log.v(TAG, spacerString + rootNode.getClassName().toString() + ", id=" + rootNode.getViewIdResourceName() + ", text=" + rootNode.getText() + ", " + "Clickable=" + rootNode.isClickable());

        for (int i = 0; i < rootNode.getChildCount(); ++i) {
            logViewHierarchy(rootNode.getChild(i), depth + 1);
        }
    }

    public String getCurrentActivity(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString());
                ActivityInfo activityInfo = null;
                try {
                    activityInfo = mAutomator.getService().getPackageManager()
                            .getActivityInfo(componentName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (activityInfo != null)
                    return componentName.flattenToShortString();
            }
        }
        return null;
    }

    /**
     * @see RoomFinderUtils#getViewHierarchy(AccessibilityNodeInfo)
     * @see RoomFinderUtils#getViewHierarchy(AccessibilityNodeInfo, String)
     */
    public String getViewHierarchy() {
        return getViewHierarchy(mAutomator.getRootInActiveWindow());
    }

    /**
     * @see RoomFinderUtils#getViewHierarchy()
     * @see RoomFinderUtils#getViewHierarchy(AccessibilityNodeInfo, String)
     */
    public String getViewHierarchy(AccessibilityNodeInfo rootNode) {
        return getViewHierarchy(rootNode, null);
    }

    /**
     * Returns view hierarchy of the given node.
     * <b>NOTE: It gets class name of each view and returns them as single string.</b>
     *
     * @param rootNode Node from which you want to get view hierarchy.
     * @return view hierarchy as a string
     * @see RoomFinderUtils#getViewHierarchy()
     * @see RoomFinderUtils#getViewHierarchy(AccessibilityNodeInfo)
     */
    private String getViewHierarchy(AccessibilityNodeInfo rootNode, String viewHierachy) {
        if (rootNode == null) return viewHierachy;
        if (viewHierachy == null) viewHierachy = "";

        // Add view's class name to string
        viewHierachy += " " + rootNode.getClassName().toString();

        for (int i = 0; i < rootNode.getChildCount(); ++i) {
            viewHierachy = getViewHierarchy(rootNode.getChild(i), viewHierachy);
        }
        return viewHierachy;
    }
}
