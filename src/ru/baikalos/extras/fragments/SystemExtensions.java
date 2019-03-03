/*
 * Copyright (C) 2017 AICP
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

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.content.res.Resources;

import ru.baikalos.extras.BaseSettingsFragment;
import ru.baikalos.extras.R;
import ru.baikalos.extras.utils.Util;
import android.util.Log;

import android.support.v14.preference.SwitchPreference;

import android.content.Context;
import android.os.SystemProperties;

public class SystemExtensions extends BaseSettingsFragment 
            implements Preference.OnPreferenceChangeListener{

    private static final String PREF_SYSTEM_APP_REMOVER = "system_app_remover";

    private static final String BAIKAL_USE_SANS_KEY = "persist.baikal.use_google_sans";

    private SwitchPreference mBaikalUseGoogleSans;
    private Context mContext;


    @Override
    protected int getPreferenceResource() {
        return R.xml.system_extensions;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();

        mBaikalUseGoogleSans = (SwitchPreference) findPreference("use_google_sans");
        if( mBaikalUseGoogleSans != null ) { 
            mBaikalUseGoogleSans.setChecked(SystemProperties.getBoolean(BAIKAL_USE_SANS_KEY, false));
            mBaikalUseGoogleSans.setOnPreferenceChangeListener(this);
        }


        final Resources res = getActivity().getResources();

        boolean mHallSensor = res.getBoolean(
                com.android.internal.R.bool.config_deviceHasHallSensor);

        boolean mLidSensor = res.getBoolean(
                com.android.internal.R.bool.config_deviceHasLidSensor);

        if( !mHallSensor ) {
            Preference mHallSensorPref = findPreference("baikal_hall_sensor_enabled");
            mHallSensorPref.setVisible(false);
        }

        if( !mLidSensor ) {
            Preference mLidSensorPref = findPreference("baikal_lid_sensor_enabled");
            Preference mLidReversePref = findPreference("baikal_lid_sensor_reverse");
            Preference mLidIgnorePref = findPreference("baikal_lid_ignore_wake");
            mLidSensorPref.setVisible(false);
            mLidReversePref.setVisible(false);
            mLidIgnorePref.setVisible(false);
        }

        Preference systemAppRemover = findPreference(PREF_SYSTEM_APP_REMOVER);
        Util.requireRoot(getActivity(), systemAppRemover);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBaikalUseGoogleSans) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(BAIKAL_USE_SANS_KEY, (Boolean) newValue);
            return true;
        } else {
            return true;
        }
    }

    private void setSystemPropertyBoolean(String key, boolean value) {
        String text = value?"1":"0";
        Log.e("BaikalSystemTweaks", "setSystemPropertyBoolean: key=" + key + ", value=" + value);
        SystemProperties.set(key, text);
    }
}
