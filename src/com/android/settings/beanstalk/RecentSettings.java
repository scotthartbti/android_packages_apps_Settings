package com.android.settings.beanstalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.content.pm.PackageManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.View;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.util.beanstalk.Helpers;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class RecentSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AdvancedLockscreen";

    private static final String IMMERSIVE_RECENTS = "immersive_recents";
    private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";
    private static final String RECENTS_USE_OMNISWITCH = "recents_use_omniswitch";
    private static final String RECENTS_USE_SLIM= "use_slim_recents";
    private static final String OMNISWITCH_START_SETTINGS = "omniswitch_start_settings";
    public static final String OMNISWITCH_PACKAGE_NAME = "org.omnirom.omniswitch";
    public static Intent INTENT_OMNISWITCH_SETTINGS = new Intent(Intent.ACTION_MAIN).setClassName(OMNISWITCH_PACKAGE_NAME,
                                OMNISWITCH_PACKAGE_NAME + ".SettingsActivity");
    private static final String SLIM_RECENTS_SETTINGS = "slim_recent_panel";
    private static final String CATEGORY_STOCK_RECENTS = "stock_recents";
    private static final String CATEGORY_OMNI_RECENTS = "omni_recents";
    private static final String CATEGORY_SLIM_RECENTS = "slim_recents";
    private static final String PREF_HIDE_APP_FROM_RECENTS = "hide_app_from_recents";

    private Preference mOmniSwitchSettings;
    private PreferenceCategory mOmniRecents;
    private PreferenceCategory mSlimRecents;
    private PreferenceCategory mStockRecents;
    private SwitchPreference mRecentsUseOmniSwitch;
    private SwitchPreference mRecentsUseSlim;
    private ListPreference mImmersiveRecents;
    private SwitchPreference mRecentsClearAll;
    private ListPreference mRecentsClearAllLocation;
    private boolean mOmniSwitchInitCalled;
    private Preference mHideAppsFromRecents;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.recent_settings);

	PreferenceScreen prefSet = getPreferenceScreen();
	PackageManager pm = getPackageManager();
        ContentResolver resolver = getActivity().getContentResolver();

	mStockRecents = (PreferenceCategory) findPreference(CATEGORY_STOCK_RECENTS);
        mOmniRecents = (PreferenceCategory) findPreference(CATEGORY_OMNI_RECENTS);
	mSlimRecents = (PreferenceCategory) findPreference(CATEGORY_SLIM_RECENTS);

        // Immersive recents
        mImmersiveRecents = (ListPreference) prefSet.findPreference(IMMERSIVE_RECENTS);
        mImmersiveRecents.setValue(String.valueOf(Settings.System.getInt(
                resolver, Settings.System.IMMERSIVE_RECENTS, 0)));
        mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
        mImmersiveRecents.setOnPreferenceChangeListener(this);

        // Clear all location
        mRecentsClearAllLocation = (ListPreference) prefSet.findPreference(RECENTS_CLEAR_ALL_LOCATION);
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
        mRecentsClearAllLocation.setValue(String.valueOf(location));
        mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
        mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

	// OmniRecents
        mRecentsUseOmniSwitch = (SwitchPreference) prefSet.findPreference(RECENTS_USE_OMNISWITCH);
        try {
            mRecentsUseOmniSwitch.setChecked(Settings.System.getInt(resolver,
                    Settings.System.RECENTS_USE_OMNISWITCH) == 1);
            mOmniSwitchInitCalled = true;
        } catch(SettingNotFoundException e){
            // if the settings value is unset
        }
        mRecentsUseOmniSwitch.setOnPreferenceChangeListener(this);

        mOmniSwitchSettings = (Preference) prefSet.findPreference(OMNISWITCH_START_SETTINGS);
        mOmniSwitchSettings.setEnabled(mRecentsUseOmniSwitch.isChecked());

        mRecentsUseSlim = (SwitchPreference) prefSet.findPreference(RECENTS_USE_SLIM);
        mRecentsUseSlim.setOnPreferenceChangeListener(this);
	updateRecents();

	mHideAppsFromRecents = prefSet.findPreference(PREF_HIDE_APP_FROM_RECENTS);

    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.BEANSTALK;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mImmersiveRecents) {
            Settings.System.putInt(resolver, Settings.System.IMMERSIVE_RECENTS,
                    Integer.parseInt((String) newValue));
            mImmersiveRecents.setValue(String.valueOf(newValue));
            mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
            return true;
        } else if (preference == mRecentsClearAllLocation) {
            int location = Integer.parseInt((String) newValue);
            int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
            return true;
	} else if (preference == mRecentsUseOmniSwitch) {
            boolean value = (Boolean) newValue;
            if (value && !mOmniSwitchInitCalled){
                openOmniSwitchFirstTimeWarning();
                mOmniSwitchInitCalled = true;
            }
            Settings.System.putInt(
                    resolver, Settings.System.RECENTS_USE_OMNISWITCH, value ? 1 : 0);
            mOmniSwitchSettings.setEnabled(value);
            updateRecents();
            return true;
        } else if (preference == mRecentsUseSlim) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(
                    resolver, Settings.System.USE_SLIM_RECENTS, value ? 1 : 0);
            updateRecents();
	    return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
	if (preference == mOmniSwitchSettings) {
            startActivity(INTENT_OMNISWITCH_SETTINGS);
            return true;
	} else if (preference == mHideAppsFromRecents) {
                Intent intent = new Intent(getActivity(), HAFRAppListActivity.class);
                getActivity().startActivity(intent);
	}
        return super.onPreferenceTreeClick(preference);
    }

    private void openOmniSwitchFirstTimeWarning() {
            new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.omniswitch_first_time_title))
                .setMessage(getResources().getString(R.string.omniswitch_first_time_message))
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).show();
        }

    private void updateRecents() {
        boolean omniRecents = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.RECENTS_USE_OMNISWITCH, 0) == 1;
        boolean slimRecents = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.USE_SLIM_RECENTS, 0) == 1;

        mStockRecents.setEnabled(!omniRecents && !slimRecents);
        // Slim recents overwrites omni recents
        mOmniRecents.setEnabled(omniRecents || !slimRecents);
        // Don't allow OmniSwitch if we're already using slim recents
        mSlimRecents.setEnabled(slimRecents || !omniRecents);
    }
}

