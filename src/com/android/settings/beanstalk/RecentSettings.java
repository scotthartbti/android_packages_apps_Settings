package com.android.settings.beanstalk;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.content.pm.PackageManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
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
    private static final String KEY_OMNISWITCH = "omniswitch";
    public static final String OMNISWITCH_PACKAGE_NAME = "org.omnirom.omniswitch";

    private Preference mOmniSwitch;

    private ListPreference mImmersiveRecents;
    private SwitchPreference mRecentsClearAll;
    private ListPreference mRecentsClearAllLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.recent_settings);

	PreferenceScreen prefSet = getPreferenceScreen();
	PackageManager pm = getPackageManager();
        ContentResolver resolver = getActivity().getContentResolver();

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

	mOmniSwitch = (Preference)
                prefSet.findPreference(KEY_OMNISWITCH);
        if (!Helpers.isPackageInstalled(OMNISWITCH_PACKAGE_NAME, pm)) {
            prefSet.removePreference(mOmniSwitch);
	}

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
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }
}

