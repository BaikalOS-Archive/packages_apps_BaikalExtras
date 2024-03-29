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
import android.provider.Settings;
import android.content.ContentResolver;

import ru.baikalos.extras.BaseSettingsFragment;
import ru.baikalos.extras.R;
import ru.baikalos.extras.utils.Util;

import android.content.Context;

import android.os.SystemProperties;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;


import android.util.Log;

import android.os.ServiceManager;
                        
import android.content.res.Resources;

import java.io.File;

public class PowerSave extends BaseSettingsFragment {


    private static final String TAG = "BaikalExtras";

    private static final String SYSTEM_PROPERTY_POWERSAVE_COREC_CTL = "persist.baikal.core_ctl";
    private static final String POWER_SAVE_CORECTL = "powersave_core_ctl_enable";
    private static final String POWER_SAVE_AGGRESSIVE = "baikalos_aggressive_idle";
    private static final String POWER_SAVE_EXTREME = "baikalos_extreme_idle";


    private Context mContext;

    private SwitchPreference mEnableCoreCtl;
    private SwitchPreference mAggressive;
    private SwitchPreference mExtreme;
    
    @Override
    protected int getPreferenceResource() {
        return R.xml.power_save;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PreferenceScreen screen = getPreferenceScreen();


        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();

        try {

            if( !((new File("/sys/fs/cgroup/freezer/cgroup.freeze")).exists()) ) {
                SwitchPreference pref = (SwitchPreference) findPreference("baikalos_app_freezer_enabled");
                if( pref != null ) pref.setVisible(false);
                pref = (SwitchPreference) findPreference("baikalos_extreme_freezer_enabled");
                if( pref != null ) pref.setVisible(false);
                pref = (SwitchPreference) findPreference("baikalos_pinned_freezer_enabled");
                if( pref != null ) pref.setVisible(false);
                pref = (SwitchPreference) findPreference("baikalos_gms_freezer_enabled");
                if( pref != null ) pref.setVisible(false);
            }

	        mAggressive = (SwitchPreference) findPreference(POWER_SAVE_AGGRESSIVE);
            mExtreme = (SwitchPreference) findPreference(POWER_SAVE_EXTREME);
            boolean aggressive = Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.BAIKALOS_AGGRESSIVE_IDLE, 0) == 1;
            boolean extreme = Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.BAIKALOS_EXTREME_IDLE, 0) == 1;

            mAggressive.setEnabled(!extreme);
            mExtreme.setEnabled(aggressive);



            mAggressive.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mExtreme.setEnabled((Boolean)newValue); 
                    return true;
                }
            });

            mExtreme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mAggressive.setEnabled(! (Boolean)newValue); 
                    return true;
                }
            });


            boolean core_ctl  = SystemProperties.get("baikal.eng.core_ctl", "0").equals("1");

	        mEnableCoreCtl = (SwitchPreference) findPreference(POWER_SAVE_CORECTL);
      	    if( mEnableCoreCtl != null ) { 
                if( !core_ctl ) {
                    mEnableCoreCtl.setVisible(false);
                } else {
                    mEnableCoreCtl.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_POWERSAVE_COREC_CTL, false));
                    mEnableCoreCtl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, "mEnableCoreCtl: set=" + (Boolean) newValue);
                            setSystemPropertyBoolean(SYSTEM_PROPERTY_POWERSAVE_COREC_CTL, (Boolean) newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mEnableCoreCtl Fatal! exception", re );
                        }
                        return true;
                      }
                    });

                }
            }

        } catch(Exception re) {
            Log.e(TAG, "onCreate: Fatal! exception", re );
        }


    }

    private void setSystemPropertyBoolean(String key, boolean value) {
        String text = value?"1":"0";
        Log.e(TAG, "setSystemPropertyBoolean: key=" + key + ", value=" + value);
        SystemProperties.set(key, text);
    }

    private void setSystemPropertyString(String key, String value) {
        Log.e(TAG, "setSystemPropertyBoolean: key=" + key + ", value=" + value);
        SystemProperties.set(key, value);
    }

    private String getSystemPropertyString(String key, String def) {
        return SystemProperties.get(key,def);
    }

    private boolean getSystemPropertyBoolean(String key) {
        if( SystemProperties.get(key,"0").equals("1") || SystemProperties.get(key,"0").equals("true") ) return true;
	    return false;
    }

}
