/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.SystemProperties;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.view.View;
import android.util.Log;

import android.os.ServiceManager;
import android.os.RemoteException;


import android.content.res.Resources;

import com.android.internal.baikalos.AppProfileSettings;
import com.android.internal.baikalos.AppProfile;
import com.android.internal.baikalos.Actions;

import ru.baikalos.extras.BaseSettingsFragment;
import ru.baikalos.extras.R;
import com.aicp.gear.preference.SeekBarPreferenceCham;

public class PerfProfileFragment extends BaseSettingsFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "PerformanceProfile";

    private String mProfileName;
    private Context mContext;

    private ListPreference mCpuSilverMin;
    private ListPreference mCpuSilverMax;
    private ListPreference mCpuGoldMin;
    private ListPreference mCpuGoldMax;
    private ListPreference mGpuMin;
    private ListPreference mGpuMax;
    private SwitchPreference mCoreControl;



     public PerfProfileFragment(String profileName) {
        mProfileName = profileName; 
    }

    @Override
    protected int getPreferenceResource() {
        return R.xml.perf_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();

        boolean perfProf  = SystemProperties.get("baikal.eng.perf", "0").equals("1");

        if( !perfProf ) return;

        if( mProfileName == null || mProfileName.equals("") ) return;

        try {
            mCpuSilverMin = InitListPreference("edit_profile_silver_min","persist.bkp." + mProfileName + "." + "csmin", "baikal.def." + mProfileName + "." + "csmin" );
            mCpuSilverMax = InitListPreference("edit_profile_silver_max","persist.bkp." + mProfileName + "." + "csmax", "baikal.def." + mProfileName + "." + "csmax");
            mCpuGoldMin = InitListPreference("edit_profile_gold_min","persist.bkp." + mProfileName + "." + "cgmin", "baikal.def." + mProfileName + "." + "cgmin");
            mCpuGoldMax = InitListPreference("edit_profile_gold_max","persist.bkp." + mProfileName + "." + "cgmax", "baikal.def." + mProfileName + "." + "cgmax");
            mGpuMin = InitListPreference("edit_profile_gpu_min","persist.bkp." + mProfileName + "." + "gmin", "baikal.def." + mProfileName + "." + "gmin");
            mGpuMax = InitListPreference("edit_profile_gpu_max","persist.bkp." + mProfileName + "." + "gmax", "baikal.def." + mProfileName + "." + "gmax");
            mCoreControl = InitSwitchPreference("edit_profile_corecontrol","persist.bkp." + mProfileName + "." + "cc", "baikal.def." + mProfileName + "." + "cc","baikal.def.cc_on","baikal.def.cc_off");
        } catch( Exception e ) {
            Log.e(TAG, "onCreate: " + mProfileName + " Fatal! exception", e );
        }

    }

    private ListPreference InitListPreference(String prefName, String systemProperty, String defProperty)
    {

            ListPreference pref = (ListPreference) findPreference(prefName);
            if( pref != null ) { 
                String freq = getPerfPropertyString(systemProperty,defProperty);
                Log.e(TAG, prefName + ": freq=" + freq);
                if( !freq.equals("0") ) {
                    pref.setValue(freq);
                }
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, prefName + ": set freq=" + newValue.toString());
    	                    setSystemPerfProfile(systemProperty,newValue.toString());
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: " + prefName + " Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }
            return pref;
       
    }

    private SwitchPreference InitSwitchPreference(String prefName, String systemProperty, String defProperty, String propertyOn, String propertyOff)
    {

            SwitchPreference pref = (SwitchPreference) findPreference(prefName);
            if( pref != null ) { 
                String valueOff =  getSystemPropertyString(propertyOff,"-1");
                String valueOn =  getSystemPropertyString(propertyOn,"-1");
                if( valueOff == null || valueOn == null || propertyOn.equals("-1") || propertyOn.equals("-1") ) {
                    pref.setVisible(false);
                    return null;
                }

                boolean enabled = getPerfPropertyString(systemProperty,defProperty).equals(valueOn);
                Log.e(TAG, prefName + ": enabled=" + enabled);
                pref.setChecked(enabled);
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, prefName + ": set enabled=" + (boolean)newValue);
                            if( (boolean)newValue) {
                                setSystemPropertyString(systemProperty,valueOn);
                            } else {
                                setSystemPropertyString(systemProperty,valueOff);
                            }
                            sendUpdateProfile();
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: " + prefName + " Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }
            return pref;
       
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private String getPerfPropertyString(String key, String def) {
        String prop = getSystemPropertyString(key,"0");
        if( prop.equals("0") ) {
            prop = getSystemPropertyString(def,"0");
        }
        return prop;
    }

    private void setSystemPerfProfile(String key, String value) {
        setSystemPropertyString(key, value);
        sendUpdateProfile();
    }


    private void setSystemPropertyString(String key, String value) {
        Log.e(TAG, "setSystemPropertyBoolean: key=" + key + ", value=" + value);
        SystemProperties.set(key, value);
    }

    private String getSystemPropertyString(String key, String def) {
        return SystemProperties.get(key,def);
    }

    private void sendUpdateProfile() {
        Log.e(TAG, "PerfProfile: sending profile update");
        Intent intent = new Intent(Actions.ACTION_SET_PROFILE);
        //intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
    	intent.putExtra("profile","");
        mContext.sendBroadcastAsUser(intent,UserHandle.ALL);
    }


}
