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
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.beanstalk.Utils;
import com.android.settings.beanstalk.CustomSeekBarPreference;

public class NotificationDrawerQsSettings extends SettingsPreferenceFragment  implements Preference.OnPreferenceChangeListener{

    private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
    private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
    private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";
    private static final String PREF_COLUMNS = "qs_layout_columns";
    private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
    private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";
    private static final String PREF_SYSUI_QQS_COUNT = "sysui_qqs_count_key";
    private static final String PREF_QS_DATA_ADVANCED = "qs_data_advanced";

    private ListPreference mTileAnimationStyle;
    private ListPreference mTileAnimationDuration;
    private ListPreference mTileAnimationInterpolator;
    private CustomSeekBarPreference mQsColumns;
    private CustomSeekBarPreference mRowsPortrait;
    private CustomSeekBarPreference mRowsLandscape;
    private CustomSeekBarPreference mSysuiQqsCount;
    private SwitchPreference mQsDataAdvanced;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.notification_drawer_qs_settings);
        
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

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
                Settings.System.QS_LAYOUT_COLUMNS, 3);
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

        mSysuiQqsCount = (CustomSeekBarPreference) findPreference(PREF_SYSUI_QQS_COUNT);
        int SysuiQqsCount = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.QQS_COUNT, 6);
        mSysuiQqsCount.setValue(SysuiQqsCount / 1);
        mSysuiQqsCount.setOnPreferenceChangeListener(this);

        mQsDataAdvanced = (SwitchPreference) findPreference(PREF_QS_DATA_ADVANCED);
        mQsDataAdvanced.setOnPreferenceChangeListener(this);
        if (Utils.isWifiOnly(getActivity())) {
            prefSet.removePreference(mQsDataAdvanced);
        } else {
        mQsDataAdvanced.setChecked((Settings.Secure.getInt(resolver,
                Settings.Secure.QS_DATA_ADVANCED, 0) == 1));
        }
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
        } else if (preference == mSysuiQqsCount) {
            int SysuiQqsCount = (Integer) objValue;
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.QQS_COUNT, SysuiQqsCount * 1);
            return true;
        } else if  (preference == mQsDataAdvanced) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.QS_DATA_ADVANCED, checked ? 1:0);
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
}
