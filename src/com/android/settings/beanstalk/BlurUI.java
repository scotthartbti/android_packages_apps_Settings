/*
 * Copyright (C) 2016 The Xperia Open Source Project
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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.TwoStatePreference;
import android.provider.Settings;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.utils.Helpers;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.beanstalk.CustomSeekBarPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class BlurUI extends SettingsPreferenceFragment
      implements OnPreferenceChangeListener {

        private static final String TAG = "BlurUI";

        private SharedPreferences mBlurUISettings;
        private Editor toEditBlurUISettings;
        private String sQuickSett = "false";

        //Switch Preferences
        private SwitchPreference mExpand;
        private SwitchPreference mQuickSett;
        private TwoStatePreference mRecentsSett;

        //Transluency,Radius and Scale
        private CustomSeekBarPreference mScale;
        private CustomSeekBarPreference mRadius;
        private CustomSeekBarPreference mQuickSettPerc;

        //Recents Radius and Scale
        private CustomSeekBarPreference mRecentsScale;
        private CustomSeekBarPreference mRecentsRadius;

        //Colors
        private ColorPickerPreference mDarkBlurColor;
        private ColorPickerPreference mLightBlurColor;
        private ColorPickerPreference mMixedBlurColor;

        public static int BLUR_LIGHT_COLOR_PREFERENCE_DEFAULT = Color.DKGRAY;
        public static int BLUR_MIXED_COLOR_PREFERENCE_DEFAULT = Color.GRAY;
        public static int BLUR_DARK_COLOR_PREFERENCE_DEFAULT = Color.LTGRAY;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.blur_ui);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            //Some help here
            int intLightColor;
            int intDarkColor;
            int intMixedColor;
            String hexLightColor;
            String hexDarkColor;
            String hexMixedColor;

            mBlurUISettings = getActivity().getSharedPreferences("BlurUI", Context.MODE_PRIVATE);

            mExpand = (SwitchPreference) prefSet.findPreference("blurred_status_bar_expanded_enabled_pref");
            mExpand.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_EXPANDED_ENABLED_PREFERENCE_KEY, 0) == 1));

            mScale = (CustomSeekBarPreference) findPreference("statusbar_blur_scale");
            mScale.setValue(Settings.System.getInt(resolver, Settings.System.BLUR_SCALE_PREFERENCE_KEY, 10));
            mScale.setOnPreferenceChangeListener(this);

            mRadius = (CustomSeekBarPreference) findPreference("statusbar_blur_radius");
            mRadius.setValue(Settings.System.getInt(resolver, Settings.System.BLUR_RADIUS_PREFERENCE_KEY, 5));
            mRadius.setOnPreferenceChangeListener(this);

            mQuickSett = (SwitchPreference) prefSet.findPreference("translucent_quick_settings_pref");
            mQuickSett.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.TRANSLUCENT_QUICK_SETTINGS_PREFERENCE_KEY, 0) == 1));

            mQuickSettPerc = (CustomSeekBarPreference) findPreference("quick_settings_transluency");
            mQuickSettPerc.setValue(Settings.System.getInt(resolver, Settings.System.TRANSLUCENT_QUICK_SETTINGS_PRECENTAGE_PREFERENCE_KEY, 60));
            mQuickSettPerc.setOnPreferenceChangeListener(this);

            mRecentsScale = (CustomSeekBarPreference) findPreference("recents_blur_scale");
            mRecentsScale.setValue(Settings.System.getInt(resolver, Settings.System.RECENT_APPS_SCALE_PREFERENCE_KEY, 6));
            mRecentsScale.setOnPreferenceChangeListener(this);

            mRecentsRadius = (CustomSeekBarPreference) findPreference("recents_blur_radius");
            mRecentsRadius.setValue(Settings.System.getInt(resolver, Settings.System.RECENT_APPS_RADIUS_PREFERENCE_KEY, 3));
            mRecentsRadius.setOnPreferenceChangeListener(this);

            mLightBlurColor = (ColorPickerPreference) findPreference("blur_light_color");
            mLightBlurColor.setOnPreferenceChangeListener(this);
            intLightColor = Settings.System.getInt(resolver, Settings.System.BLUR_LIGHT_COLOR_PREFERENCE_KEY, BLUR_LIGHT_COLOR_PREFERENCE_DEFAULT);
            hexLightColor = String.format("#%08x", (0xffffffff & intLightColor));
            mLightBlurColor.setSummary(hexLightColor);
            mLightBlurColor.setNewPreviewColor(intLightColor);

            mDarkBlurColor = (ColorPickerPreference) findPreference("blur_dark_color");
            mDarkBlurColor.setOnPreferenceChangeListener(this);
            intDarkColor = Settings.System.getInt(resolver, Settings.System.BLUR_DARK_COLOR_PREFERENCE_KEY, BLUR_DARK_COLOR_PREFERENCE_DEFAULT);
            hexDarkColor = String.format("#%08x", (0xffffffff & intDarkColor));
            mDarkBlurColor.setSummary(hexDarkColor);
            mDarkBlurColor.setNewPreviewColor(intDarkColor);

            mMixedBlurColor = (ColorPickerPreference) findPreference("blur_mixed_color");
            mMixedBlurColor.setOnPreferenceChangeListener(this);
            intMixedColor = Settings.System.getInt(resolver, Settings.System.BLUR_MIXED_COLOR_PREFERENCE_KEY, BLUR_MIXED_COLOR_PREFERENCE_DEFAULT);
            hexMixedColor = String.format("#%08x", (0xffffffff & intMixedColor));
            mMixedBlurColor.setSummary(hexMixedColor);
            mMixedBlurColor.setNewPreviewColor(intMixedColor);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mScale) {
                int value = ((Integer)newValue).intValue();
                Settings.System.putInt(
                        resolver, Settings.System.BLUR_SCALE_PREFERENCE_KEY, value);
                return true;
            } else if (preference == mRadius) {
                int value = ((Integer)newValue).intValue();
                Settings.System.putInt(
                        resolver, Settings.System.BLUR_RADIUS_PREFERENCE_KEY, value);
                return true;
            } else if (preference == mQuickSettPerc) {
                int value = ((Integer)newValue).intValue();
                Settings.System.putInt(
                        resolver, Settings.System.TRANSLUCENT_QUICK_SETTINGS_PRECENTAGE_PREFERENCE_KEY, value);
                return true;
            } else if (preference == mRecentsScale) {
                int value = ((Integer)newValue).intValue();
                Settings.System.putInt(
                    resolver, Settings.System.RECENT_APPS_SCALE_PREFERENCE_KEY, value);
                return true;
            } else if(preference == mRecentsRadius) {
                int value = ((Integer)newValue).intValue();
                Settings.System.putInt(
                    resolver, Settings.System.RECENT_APPS_RADIUS_PREFERENCE_KEY, value);
                return true;
            } else if (preference == mLightBlurColor) {
                String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.BLUR_LIGHT_COLOR_PREFERENCE_KEY, intHex);
                return true;
            } else if (preference == mDarkBlurColor) {
                String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.BLUR_DARK_COLOR_PREFERENCE_KEY, intHex);
                return true;
            } else if (preference == mMixedBlurColor) {
                String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.BLUR_MIXED_COLOR_PREFERENCE_KEY, intHex);
                return true;
            }
            return false;
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
        public boolean onPreferenceTreeClick(Preference preference) {
            final ContentResolver resolver = getActivity().getContentResolver();
            if  (preference == mExpand) {
                boolean enabled = ((SwitchPreference)preference).isChecked();
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_EXPANDED_ENABLED_PREFERENCE_KEY, enabled ? 1:0);
                updatePrefs();
            } else if (preference == mQuickSett) {
                boolean enabled = ((SwitchPreference)preference).isChecked();
                Settings.System.putInt(resolver,
                        Settings.System.TRANSLUCENT_QUICK_SETTINGS_PREFERENCE_KEY, enabled ? 1:0);
                if (enabled) {
                    sQuickSett = "true";
                } else {
                    sQuickSett = "false";
                }
                toEditBlurUISettings = mBlurUISettings.edit();
                toEditBlurUISettings.putString("quick_settings_transluency", sQuickSett);
                toEditBlurUISettings.commit();
            }
            return super.onPreferenceTreeClick(preference);
        }

        public void sharedPreferences() {
            toEditBlurUISettings = mBlurUISettings.edit();
            toEditBlurUISettings.putString("quick_settings_transluency", sQuickSett);
            toEditBlurUISettings.commit();
        }

        private void updatePrefs() {
            final ContentResolver resolver = getActivity().getContentResolver();

            boolean tempQuickSett = mBlurUISettings.getString("quick_settings_transluency", "").equals("true");

            if (Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_EXPANDED_ENABLED_PREFERENCE_KEY, 0) == 1) {
                Settings.System.putInt(resolver,
                        Settings.System.TRANSLUCENT_QUICK_SETTINGS_PREFERENCE_KEY, tempQuickSett ? 1:0);
            } else {
                Settings.System.putInt(resolver,
                        Settings.System.TRANSLUCENT_QUICK_SETTINGS_PREFERENCE_KEY, 0);
            }
        }
}
