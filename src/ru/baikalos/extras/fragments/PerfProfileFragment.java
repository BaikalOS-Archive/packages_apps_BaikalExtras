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

import android.app.AlertDialog;
import android.content.DialogInterface;

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

    private static final String TAG = "BaikalExtras";

    private static final String APP_PROFILE_PERFORMANCE_SCALE = "app_profile_cpu_performance_limit";

    private String mProfileName;
    private Context mContext;

    private ListPreference mPerformance;
    private ListPreference mCpuSilverMin;
    private ListPreference mCpuSilverMax;
    private ListPreference mCpuGoldMin;
    private ListPreference mCpuGoldMax;
    private ListPreference mGpuMin;
    private ListPreference mGpuMax;
    private SwitchPreference mCoreControl;
    private Preference mReset;
    private SeekBarPreferenceCham mEditProfileSchedBoost;
    private SeekBarPreferenceCham mEditProfileBackBoost;
    private SeekBarPreferenceCham mEditProfileGpuBoost;

    private SwitchPreference mPreferIdle;
    private SwitchPreference mPreferHighCap;
    private SeekBarPreferenceCham mDownmigrateBoosted;
    private SeekBarPreferenceCham mUpmigrateBoosted;

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
            mPerformance = initListPreference("edit_profile_cpu_performance_limit","persist.bkp." + mProfileName + "." + "cpulimit", "baikal.def." + mProfileName + "." + "cpulimit");
            mCpuSilverMin = initListPreferenceFreq("edit_profile_silver_min","persist.bkp." + mProfileName + "." + "csmin", "baikal.def." + mProfileName + "." + "csmin" );
            mCpuSilverMax = initListPreference("edit_profile_silver_max","persist.bkp." + mProfileName + "." + "csmax", "baikal.def." + mProfileName + "." + "csmax");
            mCpuGoldMin = initListPreferenceFreq("edit_profile_gold_min","persist.bkp." + mProfileName + "." + "cgmin", "baikal.def." + mProfileName + "." + "cgmin");
            mCpuGoldMax = initListPreferenceFreq("edit_profile_gold_max","persist.bkp." + mProfileName + "." + "cgmax", "baikal.def." + mProfileName + "." + "cgmax");
            mGpuMin = initListPreferenceFreq("edit_profile_gpu_min","persist.bkp." + mProfileName + "." + "gmin", "baikal.def." + mProfileName + "." + "gmin");
            mGpuMax = initListPreferenceFreq("edit_profile_gpu_max","persist.bkp." + mProfileName + "." + "gmax", "baikal.def." + mProfileName + "." + "gmax");
            mCoreControl = initSwitchPreference("edit_profile_corecontrol","persist.bkp." + mProfileName + "." + "cc", "baikal.def." + mProfileName + "." + "cc","baikal.def.cc_on","baikal.def.cc_off");

            mPreferIdle = initSwitchPreference("edit_profile_prefer_idle","persist.bkp." + mProfileName + "." + "p_idle", "baikal.def." + mProfileName + "." + "p_idle", null, null);
            mPreferHighCap = initSwitchPreference("edit_profile_prefer_highcap","persist.bkp." + mProfileName + "." + "p_hc", "baikal.def." + mProfileName + "." + "p_hc", null, null);
            mDownmigrateBoosted = initSeekBarPreference("edit_profile_downmigrate","persist.bkp." + mProfileName + "." + "downmigrate", "baikal.def." + mProfileName + "." + "downmigrate");
            mUpmigrateBoosted = initSeekBarPreference("edit_profile_upmigrate","persist.bkp." + mProfileName + "." + "upmigrate", "baikal.def." + mProfileName + "." + "upmigrate");

            mEditProfileSchedBoost = initSeekBarPreference("edit_profile_sched_boost","persist.bkp." + mProfileName + "." + "schboost", "baikal.def." + mProfileName + "." + "schboost");
            mEditProfileBackBoost = initSeekBarPreference("edit_profile_back_boost","persist.bkp." + mProfileName + "." + "bkboost", "baikal.def." + mProfileName + "." + "bkboost");
            mEditProfileGpuBoost = initSeekBarPreference("edit_profile_gpu_boost","persist.bkp." + mProfileName + "." + "gpuboost", "baikal.def." + mProfileName + "." + "gpuboost");


            mReset = (Preference) findPreference("edit_profile_reset");

        } catch( Exception e ) {
            Log.e(TAG, "onCreate: " + mProfileName + " Fatal! exception", e );
        }

    }

    private ListPreference initListPreference(String prefName, String systemProperty, String defProperty)
    {

            ListPreference pref = (ListPreference) findPreference(prefName);
            if( pref != null ) { 
                if( !isPropertyAvailable(defProperty) ) {
                    pref.setEnabled(false);
                    pref.setVisible(false);
                    return pref;
                }

                String freq = getPerfPropertyString(systemProperty,defProperty);
                Log.e(TAG, prefName + ": value=" + freq);
                pref.setValue(freq);
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, prefName + ": set value=" + newValue.toString());
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

    private ListPreference initListPreferenceFreq(String prefName, String systemProperty, String defProperty)
    {

            ListPreference pref = (ListPreference) findPreference(prefName);
            if( pref != null ) { 

                if( !isPropertyAvailable(defProperty) ) {
                    pref.setEnabled(false);
                    pref.setVisible(false);
                    return pref;
                }

                String freq = getPerfPropertyString(systemProperty,defProperty);
                Log.e(TAG, prefName + ": value=" + freq);
                if( !freq.equals("0") ) {
                    pref.setValue(freq);
                }
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, prefName + ": set value=" + newValue.toString());
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


    private SwitchPreference initSwitchPreference(String prefName, String systemProperty, String defProperty, String propertyOn, String propertyOff)
    {

            SwitchPreference pref = (SwitchPreference) findPreference(prefName);
            if( pref != null ) { 

                if( !isPropertyAvailable(defProperty) ) {
                    pref.setEnabled(false);
                    pref.setVisible(false);
                    return pref;
                }

                String valueOff =  getSystemPropertyString(propertyOff,"0");
                String valueOn =  getSystemPropertyString(propertyOn,"1");
                if( valueOff == null || valueOn == null || propertyOn.equals("-1") || propertyOn.equals("-1") ) {
                    pref.setVisible(false);
                    return pref;
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

    private SeekBarPreferenceCham initSeekBarPreference(String prefName, String systemProperty, String defProperty)
    {

            SeekBarPreferenceCham pref = (SeekBarPreferenceCham) findPreference(prefName);
            if( pref != null ) { 

                if( !isPropertyAvailable(defProperty) ) {
                    pref.setEnabled(false);
                    pref.setVisible(false);
                    return pref;
                }

                String defValue =  getSystemPropertyString(defProperty,"-99999");

                if( defValue == null || defValue.equals("-99999") ) {
                    pref.setVisible(false);
                    return pref;
                }

                String propValue = getPerfPropertyString(systemProperty,defProperty);
                int val = Integer.parseInt(propValue);
                Log.e(TAG, prefName + ": value=" + val);
                pref.setValue(val);
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, prefName + ": set value=" + Integer.parseInt(newValue.toString()));
                            setSystemPropertyString(systemProperty,newValue.toString());
                            sendUpdateProfile();
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: " + prefName + " Fatal! exception", re );
                        }
                        return true;
                    }
                });
            } else {
                Log.e(TAG, "onCreate: failed " + prefName);
            }
            return pref;
       
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mReset) {
            reset();
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void reset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.edit_profile_reset_title);
        builder.setMessage(R.string.edit_profile_reset_summary);
        builder.setPositiveButton(R.string.edit_profile_reset_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                resetDefaults();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void resetDefaults() {

        resetListValue(mPerformance,"persist.bkp." + mProfileName + "." + "cpulimit", "baikal.def." + mProfileName + "." + "cpulimit" );
        resetListValue(mCpuSilverMin,"persist.bkp." + mProfileName + "." + "csmin", "baikal.def." + mProfileName + "." + "csmin" );
        resetListValue(mCpuSilverMax,"persist.bkp." + mProfileName + "." + "csmax", "baikal.def." + mProfileName + "." + "csmax");
        resetListValue(mCpuGoldMin,"persist.bkp." + mProfileName + "." + "cgmin", "baikal.def." + mProfileName + "." + "cgmin");
        resetListValue(mCpuGoldMax,"persist.bkp." + mProfileName + "." + "cgmax", "baikal.def." + mProfileName + "." + "cgmax");
        resetListValue(mGpuMin,"persist.bkp." + mProfileName + "." + "gmin", "baikal.def." + mProfileName + "." + "gmin");
        resetListValue(mGpuMax,"persist.bkp." + mProfileName + "." + "gmax", "baikal.def." + mProfileName + "." + "gmax");
        resetSwitchValue(mCoreControl,"persist.bkp." + mProfileName + "." + "cc", "baikal.def." + mProfileName + "." + "cc","baikal.def.cc_on","baikal.def.cc_off");

        resetSwitchValue(mPreferIdle,"persist.bkp." + mProfileName + "." + "p_idle", "baikal.def." + mProfileName + "." + "p_idle", null, null);
        resetSwitchValue(mPreferHighCap,"persist.bkp." + mProfileName + "." + "p_hc", "baikal.def." + mProfileName + "." + "p_hc", null, null);

        resetSeekBarValue(mDownmigrateBoosted,"persist.bkp." + mProfileName + "." + "downmigrate", "baikal.def." + mProfileName + "." + "downmigrate");
        resetSeekBarValue(mUpmigrateBoosted,"persist.bkp." + mProfileName + "." + "upmigrate", "baikal.def." + mProfileName + "." + "upmigrate");

        resetSeekBarValue(mEditProfileSchedBoost,"persist.bkp." + mProfileName + "." + "schboost", "baikal.def." + mProfileName + "." + "schboost");
        resetSeekBarValue(mEditProfileBackBoost,"persist.bkp." + mProfileName + "." + "bkboost", "baikal.def." + mProfileName + "." + "bkboost");
        resetSeekBarValue(mEditProfileGpuBoost,"persist.bkp." + mProfileName + "." + "gpuboost", "baikal.def." + mProfileName + "." + "gpuboost");

        sendUpdateProfile();
    }

    private void resetListValue(ListPreference pref, String systemProperty, String defProperty)
    {
        if( !isPropertyAvailable(defProperty)  ) return;
        String prop = getSystemPropertyString(defProperty,"-1");
        if( prop.equals("-1") ) return;
        setSystemPropertyString(systemProperty,prop);
        pref.setValue(prop);
    }

    private void resetSwitchValue(SwitchPreference pref, String systemProperty, String defProperty, String propertyOn, String propertyOff)
    {
        if( !isPropertyAvailable(defProperty)  ) return;
        String valueOff =  getSystemPropertyString(propertyOff,"0");
        String valueOn =  getSystemPropertyString(propertyOn,"1");
        String prop = getSystemPropertyString(defProperty,"-99999");
        if( prop.equals("-99999") || valueOn.equals("-99999") || valueOff.equals("-99999") ) return;
        setSystemPropertyString(systemProperty,prop);
        boolean enabled = prop.equals(valueOn);
        pref.setChecked(enabled);
    }

    private void resetSeekBarValue(SeekBarPreferenceCham pref, String systemProperty, String defProperty)
    {
        if( !isPropertyAvailable(defProperty)  ) return;
        String prop = getSystemPropertyString(defProperty,"-99999");
        if( prop.equals("-99999") ) return;
        setSystemPropertyString(systemProperty,prop);
        pref.setValue(Integer.parseInt(prop));
    }
    

    private boolean isPropertyAvailable(String def) {
        String prop = getSystemPropertyString(def,"-99999");
        return ! "-99999".equals(prop);
    }

    private String getPerfPropertyString(String key, String def) {
        if( key == null ) return def;
        String prop = getSystemPropertyString(key,"-1");
        if( prop.equals("-1") ) {
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
