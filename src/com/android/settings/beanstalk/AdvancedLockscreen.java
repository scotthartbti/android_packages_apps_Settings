/*
 * Copyright (C) 2016 Beanstalk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.beanstalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.internal.widget.LockPatternUtils;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.beanstalk.SystemSettingSwitchPreference;
import com.android.settings.beanstalk.Utils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import com.android.internal.widget.LockPatternUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class AdvancedLockscreen extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AdvancedLockscreen";

    private static final String LOCKSCREEN_OWNER_INFO_COLOR = "lockscreen_owner_info_color";
    private static final String LOCKSCREEN_ALARM_COLOR = "lockscreen_alarm_color";
    private static final String LOCKSCREEN_CLOCK_COLOR = "lockscreen_clock_color";
    private static final String LOCKSCREEN_CLOCK_DATE_COLOR = "lockscreen_clock_date_color";
    private static final String LOCKSCREEN_COLORS_RESET = "lockscreen_colors_reset";
    private static final String FP_UNLOCK_KEYSTORE = "fp_unlock_keystore";
    private static final String PREF_HIDE_BOTTOM_SHORTCUTS = "hide_lockscreen_shortcuts";
    private static final String PREF_SHOW_CAMERA_INTENT = "show_camera_intent";

    private static final int MY_USER_ID = UserHandle.myUserId();

    private ColorPickerPreference mLockscreenOwnerInfoColorPicker;
    private ColorPickerPreference mLockscreenAlarmColorPicker;
    private ColorPickerPreference mLockscreenClockColorPicker;
    private ColorPickerPreference mLockscreenClockDateColorPicker;
    private Preference mLockscreenColorsReset;
    private SwitchPreference mBottomShortcuts;
    private SwitchPreference mShowCameraIntent;
    private PreferenceCategory mMiscCategory;
    private SwitchPreference mFpKeystore;
    private FingerprintManager mFingerprintManager;

    static final int DEFAULT = 0xffffffff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.advanced_lockscreen);

	PreferenceScreen prefSet = getPreferenceScreen();
	ContentResolver resolver = getActivity().getContentResolver();

	mMiscCategory = (PreferenceCategory) prefSet.findPreference("lockscreen_misc_category");

	int intColor;
        String hexColor;

	// Fingerprint unlock keystore
        mFpKeystore = (SwitchPreference) findPreference(FP_UNLOCK_KEYSTORE);
        if (!mFingerprintManager.isHardwareDetected()){
            mMiscCategory.removePreference(mFpKeystore);
        } else {
            mFpKeystore.setChecked((Settings.System.getInt(resolver,
                Settings.System.FP_UNLOCK_KEYSTORE, 0) == 1));
	}

        mLockscreenOwnerInfoColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_OWNER_INFO_COLOR);
        mLockscreenOwnerInfoColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_OWNER_INFO_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mLockscreenOwnerInfoColorPicker.setSummary(hexColor);
        mLockscreenOwnerInfoColorPicker.setNewPreviewColor(intColor);

        mLockscreenAlarmColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_ALARM_COLOR);
        mLockscreenAlarmColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_ALARM_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mLockscreenAlarmColorPicker.setSummary(hexColor);
        mLockscreenAlarmColorPicker.setNewPreviewColor(intColor);

        mLockscreenClockColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_CLOCK_COLOR);
        mLockscreenClockColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_CLOCK_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mLockscreenClockColorPicker.setSummary(hexColor);
        mLockscreenClockColorPicker.setNewPreviewColor(intColor);

        mLockscreenClockDateColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_CLOCK_DATE_COLOR);
        mLockscreenClockDateColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_CLOCK_DATE_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mLockscreenClockDateColorPicker.setSummary(hexColor);
        mLockscreenClockDateColorPicker.setNewPreviewColor(intColor);

	// Hide bottom shortcuts preference on secure lockscreens
        final LockPatternUtils lockPatternUtils = new LockPatternUtils(getActivity());

        mBottomShortcuts = (SwitchPreference) findPreference(PREF_HIDE_BOTTOM_SHORTCUTS);
	mShowCameraIntent = (SwitchPreference) findPreference(PREF_SHOW_CAMERA_INTENT);
        if (!lockPatternUtils.isSecure(MY_USER_ID)) {
            mBottomShortcuts.setChecked((Settings.Secure.getInt(resolver,
                Settings.Secure.HIDE_LOCKSCREEN_SHORTCUTS, 0) == 1));
            mMiscCategory.removePreference(mShowCameraIntent);
        } else {
             mMiscCategory.removePreference(mBottomShortcuts);
             mBottomShortcuts.setChecked((Settings.Secure.getInt(resolver,
                    Settings.Secure.SHOW_CAMERA_INTENT, 0) == 1));
        }

	mLockscreenColorsReset = (Preference) findPreference(LOCKSCREEN_COLORS_RESET);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.BEANSTALK;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
	if (preference == mLockscreenColorsReset) {
            resetToDefault();
            return true;
	} else {
            return super.onPreferenceTreeClick(preference);
	}
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLockscreenOwnerInfoColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_OWNER_INFO_COLOR, intHex);
            return true;
        } else if (preference == mLockscreenAlarmColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_ALARM_COLOR, intHex);
            return true;
        } else if (preference == mLockscreenClockColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_CLOCK_COLOR, intHex);
            return true;
        } else if (preference == mLockscreenClockDateColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_CLOCK_DATE_COLOR, intHex);
            return true;
        }
    return false;
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.lockscreen_colors_reset_title);
        alertDialog.setMessage(R.string.lockscreen_colors_reset_message);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        ContentResolver resolver = getActivity().getContentResolver();
        Settings.System.putInt(resolver,
                Settings.System.LOCKSCREEN_OWNER_INFO_COLOR, DEFAULT);
        mLockscreenOwnerInfoColorPicker.setNewPreviewColor(DEFAULT);
        mLockscreenOwnerInfoColorPicker.setSummary(R.string.default_string);
        Settings.System.putInt(resolver,
                Settings.System.LOCKSCREEN_ALARM_COLOR, DEFAULT);
        mLockscreenAlarmColorPicker.setNewPreviewColor(DEFAULT);
        mLockscreenAlarmColorPicker.setSummary(R.string.default_string);
        Settings.System.putInt(resolver,
                Settings.System.LOCKSCREEN_CLOCK_COLOR, DEFAULT);
        mLockscreenClockColorPicker.setNewPreviewColor(DEFAULT);
        mLockscreenClockColorPicker.setSummary(R.string.default_string);
        Settings.System.putInt(resolver,
                Settings.System.LOCKSCREEN_CLOCK_DATE_COLOR, DEFAULT);
        mLockscreenClockDateColorPicker.setNewPreviewColor(DEFAULT);
        mLockscreenClockDateColorPicker.setSummary(R.string.default_string);
    }
}
