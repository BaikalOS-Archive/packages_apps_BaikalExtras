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



public class PowerSave extends BaseSettingsFragment {


    private static final String TAG = "PowerSave";

    private static final String SYSTEM_PROPERTY_POWERSAVE_COREC_CTL = "persist.baikal.core_ctl";
    private static final String POWER_SAVE_CORECTL = "powersave_core_ctl_enable";


    private Context mContext;

    private SwitchPreference mEnableCoreCtl;
    
    private ListPreference mDefaultPerfProfile;
    private ListPreference mDefaultThermProfile;

    //IBaikalServiceController mBaikalService;

    @Override
    protected int getPreferenceResource() {
        return R.xml.power_save;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PreferenceScreen screen = getPreferenceScreen();

        final PreferenceCategory profilesCategory =
                (PreferenceCategory) screen.findPreference("default_profiles");


        boolean perfProf  = SystemProperties.get("baikal.eng.perf", "0").equals("1") || 
                            SystemProperties.get("spectrum.support", "0").equals("1");

        boolean thermProf  = SystemProperties.get("baikal.eng.therm", "0").equals("1");

        boolean core_ctl  = SystemProperties.get("baikal.eng.core_ctl", "0").equals("1");

        if( !perfProf && !thermProf ) {
            if( profilesCategory != null ) {
                screen.removePreference(profilesCategory);
            }
            return;
        }

    

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();

        try {

            mDefaultPerfProfile = (ListPreference) findPreference("default_perf_profile");
            if( mDefaultPerfProfile != null ) { 
                if( !perfProf ) {
                    mDefaultPerfProfile.setVisible(false);
                } else {
                    //String profile = mBaikalService.getDefaultPerfProfile();
                    //Log.e(TAG, "mDefaultPerfProfile: getProfile=" + profile);
                    //mDefaultPerfProfile.setValue(profile);
                    mDefaultPerfProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, "mDefaultPerfProfile: setProfile=" + newValue.toString());
                            //mBaikalService.setDefaultPerfProfile(newValue.toString());
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mDefaultPerfProfile Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }

            mDefaultThermProfile = (ListPreference) findPreference("default_therm_profile");
            if( mDefaultThermProfile != null ) { 
                if( !thermProf ) {
                    mDefaultThermProfile.setVisible(false);
                } else {
                    //String profile = mBaikalService.getDefaultThermProfile();
                    //Log.e(TAG, "mDefaultThermProfile: getProfile=" + profile);
                    //mDefaultThermProfile.setValue(profile);
                    mDefaultThermProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, "mDefaultThermProfile: setProfile=" + newValue.toString());
                            //mBaikalService.setDefaultThermProfile(newValue.toString());
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mDefaultPerfProfile Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }

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

}
