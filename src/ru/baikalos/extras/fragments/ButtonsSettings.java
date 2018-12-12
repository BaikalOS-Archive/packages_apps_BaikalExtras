/*
 * Copyright (C) 2018 BaikalOS
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

package ru.baikalos.extras.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import ru.baikalos.extras.BaseSettingsFragment;
import ru.baikalos.extras.R;

public class ButtonsSettings extends BaseSettingsFragment {

    private static final String TAG = "ButtonSettings";

    // Switches
    private static final String KEY_BUTTON_BRIGHTNESS      = "button_brightness";
    private static final String KEY_NAVIGATION_BAR         = "navigation_bar";

    // Long Press/Double Tap Actions
    private static final String KEY_HOME_LONG_PRESS        = "home_key_long_press";
    private static final String KEY_HOME_DOUBLE_TAP        = "home_key_double_tap";
    private static final String KEY_BACK_LONG_PRESS        = "back_key_long_press";
    private static final String KEY_BACK_DOUBLE_TAP        = "back_key_double_tap";
    private static final String KEY_MENU_LONG_PRESS        = "menu_key_long_press";
    private static final String KEY_MENU_DOUBLE_TAP        = "menu_key_double_tap";
    private static final String KEY_ASSIST_LONG_PRESS      = "assist_key_long_press";
    private static final String KEY_ASSIST_DOUBLE_TAP      = "assist_key_double_tap";
    private static final String KEY_APP_SWITCH_LONG_PRESS  = "app_switch_key_long_press";
    private static final String KEY_APP_SWITCH_DOUBLE_TAP  = "app_switch_key_double_tap";
    private static final String KEY_CAMERA_LONG_PRESS      = "camera_key_long_press";
    private static final String KEY_CAMERA_DOUBLE_TAP      = "camera_key_double_tap";

    // Categories
    private static final String KEY_CATEGORY_HOME          = "home_key";
    private static final String KEY_CATEGORY_BACK          = "back_key";
    private static final String KEY_CATEGORY_MENU          = "menu_key";
    private static final String KEY_CATEGORY_ASSIST        = "assist_key";
    private static final String KEY_CATEGORY_APP_SWITCH    = "app_switch_key";
    private static final String KEY_CATEGORY_CAMERA        = "camera_key";

    // Masked keys
    private static final int KEY_MASK_HOME = 0x01;
    private static final int KEY_MASK_BACK = 0x02;
    private static final int KEY_MASK_MENU = 0x04;
    private static final int KEY_MASK_ASSIST = 0x08;
    private static final int KEY_MASK_APP_SWITCH = 0x10;
    private static final int KEY_MASK_CAMERA = 0x20;

    private int mDeviceHardwareKeys;

    @Override
    protected int getPreferenceResource() {
        return R.xml.buttons_settings;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Resources res = getActivity().getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen screen = getPreferenceScreen();

        final boolean navigationBarEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.NAVIGATION_BAR_ENABLED, 0, UserHandle.USER_CURRENT) != 0;

        mDeviceHardwareKeys = res.getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);

        final boolean hasHome = (mDeviceHardwareKeys & KEY_MASK_HOME) != 0 || navigationBarEnabled;
        final boolean hasMenu = (mDeviceHardwareKeys & KEY_MASK_MENU) != 0;
        final boolean hasBack = (mDeviceHardwareKeys & KEY_MASK_BACK) != 0 || navigationBarEnabled;
        final boolean hasAssist = (mDeviceHardwareKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitch = (mDeviceHardwareKeys & KEY_MASK_APP_SWITCH) != 0 || navigationBarEnabled;
        final boolean hasCamera = (mDeviceHardwareKeys & KEY_MASK_CAMERA) != 0;

        final PreferenceCategory homeCategory =
                (PreferenceCategory) screen.findPreference(KEY_CATEGORY_HOME);

        final PreferenceCategory backCategory =
                (PreferenceCategory) screen.findPreference(KEY_CATEGORY_BACK);

        final PreferenceCategory menuCategory =
                (PreferenceCategory) screen.findPreference(KEY_CATEGORY_MENU);

        final PreferenceCategory assistCategory =
                (PreferenceCategory) screen.findPreference(KEY_CATEGORY_ASSIST);

        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) screen.findPreference(KEY_CATEGORY_APP_SWITCH);

        final PreferenceCategory cameraCategory =
                (PreferenceCategory) screen.findPreference(KEY_CATEGORY_CAMERA);

        if (!hasMenu && menuCategory != null) {
            screen.removePreference(menuCategory);
        }

        if (!hasAssist && assistCategory != null) {
            screen.removePreference(assistCategory);
        }

        if (!hasCamera && cameraCategory != null) {
            screen.removePreference(cameraCategory);
        }

    }
}
