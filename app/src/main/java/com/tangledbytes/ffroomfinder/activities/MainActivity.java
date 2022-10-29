package com.tangledbytes.ffroomfinder.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.tangledbytes.ffroomfinder.R;
import com.tangledbytes.ffroomfinder.services.AutomatorService;
import com.tangledbytes.ffroomfinder.utils.AppConstants;
import com.tangledbytes.ffroomfinder.utils.Utils;
import android.util.Log;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private final Context mContext = this;
    private Utils mUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUtils = new Utils(mContext);
        Button btnStartAutomator = findViewById(R.id.start);
        btnStartAutomator.setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
        if (!isAccessibilityServiceEnabled()) {
            Log.v(TAG, "Asking for granting accessibility permission.");
            mUtils.showToast("Please grant accessibility permission to continue.");
            final Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            return;
        } else if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            mUtils.showToast("Please grant 'WRITE_EXTERNAL_STORAGE' permission in settings");
            return;
                }
        if (view.getId() == R.id.start) {
            Log.v(TAG, "Starting automator service");
            final Intent intent = new Intent();
            intent.setAction(AppConstants.AUTOMATOR_START);
            sendBroadcast(intent);
        }
    }

    /**
     * Checks whether the given accessibility service has been enabled or not.
     * @return True if enabled otherwise false.
     */
    private boolean isAccessibilityServiceEnabled() {
        ComponentName expectedComponentName = new ComponentName(mContext, AutomatorService.class);
        String enabledServicesSetting = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null)
            return false;

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);
            if (enabledService != null && enabledService.equals(expectedComponentName))
                return true;
        }
        return false;
    }
}
