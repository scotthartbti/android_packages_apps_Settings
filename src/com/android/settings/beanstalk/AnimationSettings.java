/*
 * Copyright (C) 2016 BeanStalk
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
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.TrafficStats;
import android.os.Bundle;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;


public class AnimationSettings extends SettingsPreferenceFragment
    implements OnPreferenceChangeListener {

    private static final String TAG = "AnimationSettings";

    private static final String POWER_MENU_ANIMATIONS = "power_menu_animations";
    private static final String KEY_TOAST_ANIMATION = "toast_animation";
    private static final String TOAST_ICON_COLOR = "toast_icon_color";
    private static final String TOAST_TEXT_COLOR = "toast_text_color";

    private ListPreference mPowerMenuAnimations;
    private ListPreference mToastAnimation;
    private ColorPickerPreference mIconColor;
    private ColorPickerPreference mTextColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.animation_settings);

	PreferenceScreen prefSet = getPreferenceScreen();
	ContentResolver resolver = getActivity().getContentResolver();

        // Power Menu Animations
        mPowerMenuAnimations = (ListPreference) prefSet.findPreference(POWER_MENU_ANIMATIONS);
        mPowerMenuAnimations.setValue(String.valueOf(Settings.System.getInt(
                resolver, Settings.System.POWER_MENU_ANIMATIONS, 0)));
        mPowerMenuAnimations.setSummary(mPowerMenuAnimations.getEntry());
        mPowerMenuAnimations.setOnPreferenceChangeListener(this);

	// Toast Animations
        mToastAnimation = (ListPreference) prefSet.findPreference(KEY_TOAST_ANIMATION);
        mToastAnimation.setSummary(mToastAnimation.getEntry());
        int CurrentToastAnimation = Settings.System.getInt(
                resolver, Settings.System.TOAST_ANIMATION, 1);
        mToastAnimation.setValueIndex(CurrentToastAnimation); //set to index of default value
        mToastAnimation.setSummary(mToastAnimation.getEntries()[CurrentToastAnimation]);
        mToastAnimation.setOnPreferenceChangeListener(this);

	int intColor = 0xffffffff;
        String hexColor = String.format("#%08x", (0xffffffff & 0xffffffff));

        // Toast icon color
        mIconColor = (ColorPickerPreference) findPreference(TOAST_ICON_COLOR);
        intColor = Settings.System.getInt(resolver,
                Settings.System.TOAST_ICON_COLOR, 0xffffffff);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mIconColor.setNewPreviewColor(intColor);
        mIconColor.setSummary(hexColor);
        mIconColor.setOnPreferenceChangeListener(this);

        // Toast text color
        mTextColor = (ColorPickerPreference) findPreference(TOAST_TEXT_COLOR);
        intColor = Settings.System.getInt(resolver,
                Settings.System.TOAST_TEXT_COLOR, 0xffffffff);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mTextColor.setNewPreviewColor(intColor);
        mTextColor.setSummary(hexColor);
        mTextColor.setOnPreferenceChangeListener(this);
    }

    @Override
    protected int getMetricsCategory() {
       return MetricsEvent.BEANSTALK;
   }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mPowerMenuAnimations) {
            Settings.System.putInt(getContentResolver(), Settings.System.POWER_MENU_ANIMATIONS,
                    Integer.valueOf((String) newValue));
            mPowerMenuAnimations.setValue(String.valueOf(newValue));
            mPowerMenuAnimations.setSummary(mPowerMenuAnimations.getEntry());
            return true;
	} else if (preference == mToastAnimation) {
            int index = mToastAnimation.findIndexOfValue((String) newValue);
            Settings.System.putString(resolver,
                    Settings.System.TOAST_ANIMATION, (String) newValue);
            mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
            Toast.makeText(getActivity(), "Toast test!!!", Toast.LENGTH_SHORT).show();
            return true;
	}  else if (preference == mIconColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                   .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                   Settings.System.TOAST_ICON_COLOR, intHex);
            Toast.makeText(getActivity(), mToastAnimation.getEntry(),
                   Toast.LENGTH_SHORT).show();
        } else if (preference == mTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                  .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                  Settings.System.TOAST_TEXT_COLOR, intHex);
            Toast.makeText(getActivity(), mToastAnimation.getEntry(),
                  Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
