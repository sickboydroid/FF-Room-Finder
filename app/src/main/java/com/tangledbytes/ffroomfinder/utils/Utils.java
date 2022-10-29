package com.tangledbytes.ffroomfinder.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.List;

public class Utils {
    private static final String TAG = "Utils";
    private Context mContext;

    public Utils(Context context) {
        mContext = context;
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void showToast(Context context, int id) {
        Toast.makeText(context, id, Toast.LENGTH_LONG).show();
    }

    public static File getFilesDir(Context context) {
        return context.getExternalFilesDir(null);
    }

    /**
     * Converts given size (of file) into readable sizes like kb, mb, gb etc.
     */
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Converts minutes to milli seconds
     */
    public static long minToMillis(int minutes) {
        return (1000 * 60) * minutes;
    }

    /**
     * Converts hours to milli seconds
     */
    public static long hourToMillis(int hours) {
        return (1000 * 60 * 60) * hours;
    }

    /**
     * Runs linux commands using android's built in shell.
     */
    public static String shell(String cmd) throws IOException {
        final Process proc = Runtime.getRuntime().exec(cmd);
        // Reads output of command
        final BufferedReader brStdOutput =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));
        // Reads error output (if error occured) of command
        final BufferedReader brErrOutput =
                new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        String output = "";
        String line;
        while ((line = brStdOutput.readLine()) != null)
            output += "\n" + line;
        while ((line = brErrOutput.readLine()) != null)
            output += "\n" + line;
        output = output.trim();
        brStdOutput.close();
        brErrOutput.close();
        return output;
    }

    //////////////////////////////////////////////////
    // Utility methods for checking android version //
    //////////////////////////////////////////////////
    public static boolean hasOreo() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.O;
    }

    public static boolean hasNought() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.N;
    }

    public static boolean hasMarshmellow() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.M;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
    }

    public void showToast(String msg) {
        showToast(getContext(), msg);
    }

    public void showToast(int id) {
        showToast(getContext(), id);
    }

    public File getFilesDir() {
        return getFilesDir(getContext());
    }

    public String getVersionName() throws PackageManager.NameNotFoundException {
        return getContext().getPackageManager()
                .getPackageInfo(getContext().getPackageName(), 0).versionName;
    }

    /**
     * Starts provided foreground service.
     */
    public void startForegroundService(Intent serviceIntent) {
        if (hasOreo())
            getContext().startForegroundService(serviceIntent);
        else
            getContext().startService(serviceIntent);
    }

    /**
     * Performs a ping test to check whether the internet is available or not
     */
    public boolean hasActiveInternetConnection() {
        try {
            // Do ping test
            HttpURLConnection urlc = (HttpURLConnection)
                    (new URL("https://clients3.google.com/generate_204").openConnection());
            return (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0);
        } catch (Exception e) {
            if (e instanceof UnknownHostException)
                return false;
            Log.v(TAG, "Exception occurred while checking for internet connection", e);
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public boolean isServiceRunning(String className) {
        ActivityManager activityManager =
                (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices =
                activityManager.getRunningServices(Integer.MAX_VALUE);
        int size = runningServices.size();
        for (int i = 0; i < size; i++) {
            if (runningServices.get(i).service.getPackageName().equals(getContext().getPackageName())) {
                if (runningServices.get(i).service.getClassName().equals(className))
                    return runningServices.get(i).started;
            }
        }
        return false;
    }

    public boolean hasPermission(String permission) {
        if (hasMarshmellow())
            return (getContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    private Context getContext() {
        if (mContext == null)
            Log.e(TAG, "getContext(): Passed Context is null");
        return mContext;
    }
}
