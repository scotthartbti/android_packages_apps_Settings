/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.android.settings.beanstalk;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;
import android.hardware.fingerprint.FingerprintManager;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.beanstalk.Utils;
import com.android.settings.utils.Helpers;
import com.android.settings.beanstalk.CustomSeekBarPreference;

public class NotificationDrawerQsSettings extends SettingsPreferenceFragment  implements Preference.OnPreferenceChangeListener{

    private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
    private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
    private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";
    private static final String PREF_COLUMNS = "qs_layout_columns";
    private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
    private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";

    private static final String CUSTOM_HEADER_IMAGE = "status_bar_custom_header";
    private static final String PREF_QS_EASY_TOGGLE = "qs_easy_toggle";
    private static final String DAYLIGHT_HEADER_PACK = "daylight_header_pack";
    private static final String DEFAULT_HEADER_PACKAGE = "com.android.systemui";
    private static final String CUSTOM_HEADER_IMAGE_SHADOW = "status_bar_custom_header_shadow";
    private static final String CUSTOM_HEADER_PROVIDER = "custom_header_provider";
    private static final String CUSTOM_HEADER_BROWSE = "custom_header_browse";
    private static final String NOTIFICATION_GUTS_KILL_APP_BUTTON = "notification_guts_kill_app_button";
    private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
    private static final String CATEGORY_WEATHER = "weather_category";
    private static final String PREF_BRIGHTNESS_ICON_POSITION = "brightness_icon_position";
    private static final String FP_SWIPE_QUICK_PULLDOWN = "quick_pulldown_fp";

    private ListPreference mDaylightHeaderPack;
    private CustomSeekBarPreference mHeaderShadow;
    private ListPreference mTileAnimationStyle;
    private ListPreference mTileAnimationDuration;
    private ListPreference mTileAnimationInterpolator;
    private CustomSeekBarPreference mQsColumns;
    private CustomSeekBarPreference mRowsPortrait;
    private CustomSeekBarPreference mRowsLandscape;
    private ListPreference mHeaderProvider;
    private String mDaylightHeaderProvider;
    private PreferenceScreen mHeaderBrowse;
    private SwitchPreference mEasyToggle;
    private Preference mNotificationKill;
    private PreferenceCategory mWeatherCategory;
    private SwitchPreference mBrightnessIconPosition;
    private SwitchPreference mFingerprintSwipe;
    private FingerprintManager mFingerprintManager;

    private static final int MY_USER_ID = UserHandle.myUserId();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.notification_drawer_qs_settings);
        
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();
	final PackageManager pm = getActivity().getPackageManager();

        int defaultValue;

        mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
        int tileAnimationStyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_STYLE, 0,
                UserHandle.USER_CURRENT);
        mTileAnimationStyle.setValue(String.valueOf(tileAnimationStyle));
        updateTileAnimationStyleSummary(tileAnimationStyle);
        updateAnimTileStyle(tileAnimationStyle);
        mTileAnimationStyle.setOnPreferenceChangeListener(this);

        mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
        int tileAnimationDuration = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_DURATION, 2000,
                UserHandle.USER_CURRENT);
        mTileAnimationDuration.setValue(String.valueOf(tileAnimationDuration));
        updateTileAnimationDurationSummary(tileAnimationDuration);
        mTileAnimationDuration.setOnPreferenceChangeListener(this);

        mTileAnimationInterpolator = (ListPreference) findPreference(PREF_TILE_ANIM_INTERPOLATOR);
        int tileAnimationInterpolator = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_INTERPOLATOR, 0,
                UserHandle.USER_CURRENT);
        mTileAnimationInterpolator.setValue(String.valueOf(tileAnimationInterpolator));
        updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
        mTileAnimationInterpolator.setOnPreferenceChangeListener(this);

        mQsColumns = (CustomSeekBarPreference) findPreference(PREF_COLUMNS);
        int columnsQs = Settings.System.getInt(resolver,
                Settings.System.QS_LAYOUT_COLUMNS, 5);
        mQsColumns.setValue(columnsQs / 1);
        mQsColumns.setOnPreferenceChangeListener(this);

        mRowsPortrait = (CustomSeekBarPreference) findPreference(PREF_ROWS_PORTRAIT);
        int rowsPortrait = Settings.System.getInt(resolver,
                Settings.System.QS_ROWS_PORTRAIT, 3);
        mRowsPortrait.setValue(rowsPortrait / 1);
        mRowsPortrait.setOnPreferenceChangeListener(this);

        defaultValue = getResources().getInteger(com.android.internal.R.integer.config_qs_num_rows_landscape_default);
        mRowsLandscape = (CustomSeekBarPreference) findPreference(PREF_ROWS_LANDSCAPE);
        int rowsLandscape = Settings.System.getInt(resolver,
                Settings.System.QS_ROWS_LANDSCAPE, defaultValue);
        mRowsLandscape.setValue(rowsLandscape / 1);
        mRowsLandscape.setOnPreferenceChangeListener(this);

        mEasyToggle = (SwitchPreference) findPreference(PREF_QS_EASY_TOGGLE);
        mEasyToggle.setOnPreferenceChangeListener(this);
        mEasyToggle.setChecked((Settings.Secure.getInt(resolver,
                Settings.Secure.QS_EASY_TOGGLE, 0) == 1));

 	String settingHeaderPackage = Settings.System.getString(resolver,
                Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK);
        if (settingHeaderPackage == null) {
            settingHeaderPackage = DEFAULT_HEADER_PACKAGE;
        }
        mDaylightHeaderPack = (ListPreference) findPreference(DAYLIGHT_HEADER_PACK);

        List<String> entries = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        getAvailableHeaderPacks(entries, values);
        mDaylightHeaderPack.setEntries(entries.toArray(new String[entries.size()]));
        mDaylightHeaderPack.setEntryValues(values.toArray(new String[values.size()]));

        int valueIndex = mDaylightHeaderPack.findIndexOfValue(settingHeaderPackage);
        if (valueIndex == -1) {
            // no longer found
            settingHeaderPackage = DEFAULT_HEADER_PACKAGE;
            Settings.System.putString(resolver,
                    Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK, settingHeaderPackage);
            valueIndex = mDaylightHeaderPack.findIndexOfValue(settingHeaderPackage);
        }
        mDaylightHeaderPack.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntry());
        mDaylightHeaderPack.setOnPreferenceChangeListener(this);

        mHeaderShadow = (CustomSeekBarPreference) findPreference(CUSTOM_HEADER_IMAGE_SHADOW);
        final int headerShadow = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, 80);
        mHeaderShadow.setValue((int)(((double) headerShadow / 255) * 100));
        mHeaderShadow.setOnPreferenceChangeListener(this);

	mDaylightHeaderProvider = getResources().getString(R.string.daylight_header_provider);
        String providerName = Settings.System.getString(getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER);
        if (providerName == null) {
            providerName = mDaylightHeaderProvider;
        }
        mHeaderProvider = (ListPreference) findPreference(CUSTOM_HEADER_PROVIDER);
        valueIndex = mHeaderProvider.findIndexOfValue(providerName);
        mHeaderProvider.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mHeaderProvider.setSummary(mHeaderProvider.getEntry());
        mHeaderProvider.setOnPreferenceChangeListener(this);
        mDaylightHeaderPack.setEnabled(providerName.equals(mDaylightHeaderProvider));

        mHeaderBrowse = (PreferenceScreen) findPreference(CUSTOM_HEADER_BROWSE);
        mHeaderBrowse.setEnabled(isBrowseHeaderAvailable());

	mNotificationKill = findPreference(NOTIFICATION_GUTS_KILL_APP_BUTTON);
        mNotificationKill.setOnPreferenceChangeListener(this);

	mWeatherCategory = (PreferenceCategory) prefSet.findPreference(CATEGORY_WEATHER);
        if (mWeatherCategory != null && (!Helpers.isPackageInstalled(WEATHER_SERVICE_PACKAGE, pm))) {
            prefSet.removePreference(mWeatherCategory);
	}

	// Fingerprint swipe for quick pulldown
        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintSwipe = (SwitchPreference) findPreference(FP_SWIPE_QUICK_PULLDOWN);
        if (!mFingerprintManager.isHardwareDetected()) {
            prefSet.removePreference(mFingerprintSwipe);
        }

	mBrightnessIconPosition = (SwitchPreference) findPreference(PREF_BRIGHTNESS_ICON_POSITION);
        mBrightnessIconPosition.setOnPreferenceChangeListener(this);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.BEANSTALK;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {

        int intValue;
        int index;

        if (preference == mTileAnimationStyle) {
            int tileAnimationStyle = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_STYLE,
                    tileAnimationStyle, UserHandle.USER_CURRENT);
            updateTileAnimationStyleSummary(tileAnimationStyle);
            updateAnimTileStyle(tileAnimationStyle);
            return true;
        } else if (preference == mTileAnimationDuration) {
            int tileAnimationDuration = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_DURATION,
                    tileAnimationDuration, UserHandle.USER_CURRENT);
            updateTileAnimationDurationSummary(tileAnimationDuration);
            return true;
        } else if (preference == mTileAnimationInterpolator) {
            int tileAnimationInterpolator = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_INTERPOLATOR,
                    tileAnimationInterpolator, UserHandle.USER_CURRENT);
            updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
            return true;
	} else if (preference == mBrightnessIconPosition) {
            Helpers.showSystemUIrestartDialog(getActivity());
            return true;
        } else if (preference == mQsColumns) {
            int qsColumns = (Integer) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.QS_LAYOUT_COLUMNS, qsColumns * 1);
            return true;
        } else if (preference == mRowsPortrait) {
            int rowsPortrait = (Integer) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.QS_ROWS_PORTRAIT, rowsPortrait * 1);
            return true;
        } else if (preference == mRowsLandscape) {
            int rowsLandscape = (Integer) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.QS_ROWS_LANDSCAPE, rowsLandscape * 1);
           return true;
        } else if  (preference == mEasyToggle) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.QS_EASY_TOGGLE, checked ? 1:0);
            return true;
	} else if (preference == mNotificationKill) {
            // Setting will only apply to new created notifications.
            // By restarting SystemUI, we can re-create all notifications
            Helpers.showSystemUIrestartDialog(getActivity());
            return true;
	} else if (preference == mDaylightHeaderPack) {
            String value = (String) objValue;
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK, value);
            int valueIndex = mDaylightHeaderPack.findIndexOfValue(value);
            mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntries()[valueIndex]);
            return true;
        } else if (preference == mHeaderShadow) {
            Integer headerShadow = (Integer) objValue;
            int realHeaderValue = (int) (((double) headerShadow / 100) * 255);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, realHeaderValue);
            return true;
	} else if (preference == mHeaderProvider) {
            String value = (String) objValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER, value);
            int valueIndex = mHeaderProvider.findIndexOfValue(value);
            mHeaderProvider.setSummary(mHeaderProvider.getEntries()[valueIndex]);
            mDaylightHeaderPack.setEnabled(value.equals(mDaylightHeaderProvider));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    private void updateTileAnimationStyleSummary(int tileAnimationStyle) {
        String prefix = (String) mTileAnimationStyle.getEntries()[mTileAnimationStyle.findIndexOfValue(String
                .valueOf(tileAnimationStyle))];
        mTileAnimationStyle.setSummary(getResources().getString(R.string.qs_set_animation_style, prefix));
    }

    private void updateTileAnimationDurationSummary(int tileAnimationDuration) {
        String prefix = (String) mTileAnimationDuration.getEntries()[mTileAnimationDuration.findIndexOfValue(String
                .valueOf(tileAnimationDuration))];
        mTileAnimationDuration.setSummary(getResources().getString(R.string.qs_set_animation_duration, prefix));
    }

    private void updateTileAnimationInterpolatorSummary(int tileAnimationInterpolator) {
        String prefix = (String) mTileAnimationInterpolator.getEntries()[mTileAnimationInterpolator.findIndexOfValue(String
                .valueOf(tileAnimationInterpolator))];
        mTileAnimationInterpolator.setSummary(getResources().getString(R.string.qs_set_animation_interpolator, prefix));
    }

    private void updateAnimTileStyle(int tileAnimationStyle) {
        if (mTileAnimationDuration != null) {
            if (tileAnimationStyle == 0) {
                mTileAnimationDuration.setSelectable(false);
                mTileAnimationInterpolator.setSelectable(false);
            } else {
                mTileAnimationDuration.setSelectable(true);
                mTileAnimationInterpolator.setSelectable(true);
            }
        }
    }

    private void getAvailableHeaderPacks(List<String> entries, List<String> values) {
	String defaultLabel = null;
        Map<String, String> headerMap = new HashMap<String, String>();
        Intent i = new Intent();
        PackageManager packageManager = getActivity().getPackageManager();
        i.setAction("org.omnirom.DaylightHeaderPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            if (packageName.equals(DEFAULT_HEADER_PACKAGE)) {
                defaultLabel = label;
            } else {
                headerMap.put(label, packageName);
            }
        }
            i.setAction("org.omnirom.DaylightHeaderPack1");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
            if (label == null) {
                label = packageName;
            }
	    headerMap.put(label, packageName  + "/" + r.activityInfo.name);
        }
        List<String> labelList = new ArrayList<String>();
        labelList.addAll(headerMap.keySet());
        Collections.sort(labelList);
        for (String label : labelList) {
            entries.add(label);
	    values.add(headerMap.get(label));
        }
	entries.add(0, defaultLabel);
        values.add(0, DEFAULT_HEADER_PACKAGE);
    }

    private boolean isBrowseHeaderAvailable() {
        PackageManager pm = getPackageManager();
        Intent browse = new Intent();
        browse.setClassName("org.omnirom.omnistyle", "org.omnirom.omnistyle.BrowseHeaderActivity");
        return pm.resolveActivity(browse, 0) != null;
    }
}
