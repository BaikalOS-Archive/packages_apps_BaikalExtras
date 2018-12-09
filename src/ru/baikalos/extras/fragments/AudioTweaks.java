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
import android.os.UserHandle;
import android.os.UserManager;
import android.os.SystemProperties;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import ru.baikalos.extras.BaseSettingsFragment;
import ru.baikalos.extras.R;
import ru.baikalos.gear.preference.SeekBarPreferenceCham;

public class AudioTweaks extends BaseSettingsFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "AudioTweaks";

    private static final String AUDIO_TWEAKS_ANC = "audio_tweaks_anc";
    private static final String AUDIO_TWEAKS_FLUENCE = "audio_tweaks_fluence";
    private static final String AUDIO_TWEAKS_WHATSAPP = "audio_tweaks_whatsapp";
    private static final String AUDIO_TWEAKS_HP_DETECT = "audio_tweaks_hp_detect";
    private static final String AUDIO_TWEAKS_SUSPEND_PLAY = "audio_tweaks_suspend_play";

    private static final String SYSTEM_PROPERTY_HP_DETECT = "persist.fx.hp_detect";
    private static final String SYSTEM_PROPERTY_WHATSAPP_HACK = "persist.vendor.audio.whatsapp";
    private static final String SYSTEM_PROPERTY_ENABLE_ANC = "persist.ps.anc.enable";
    private static final String SYSTEM_PROPERTY_ENABLE_FLUENCE = "persist.ps.audio.use_fluence";

    private static final String SYSTEM_PROPERTY_SUSPEND_PLAY = "persist.audio.offload.suspend";



    private Context mContext;

    private SwitchPreference mEnableANC;
    private SwitchPreference mEnableFluence;
    private SwitchPreference mEnableWhatsAppHack;
    private SwitchPreference mEnableHeadphonesDetection;
    private SwitchPreference mEnableSuspendPlay;

    @Override
    protected int getPreferenceResource() {
        return R.xml.audio_tweaks;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();

        mEnableANC = (SwitchPreference) findPreference(AUDIO_TWEAKS_ANC);
        if( mEnableANC != null ) { 
                mEnableANC.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_ENABLE_ANC, false));
                mEnableANC.setOnPreferenceChangeListener(this);
        }

        mEnableFluence = (SwitchPreference) findPreference(AUDIO_TWEAKS_FLUENCE);
        if( mEnableFluence != null ) { 
                mEnableFluence.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_ENABLE_FLUENCE, false));
                mEnableFluence.setOnPreferenceChangeListener(this);
        }
        mEnableWhatsAppHack = (SwitchPreference) findPreference(AUDIO_TWEAKS_WHATSAPP);
        if( mEnableWhatsAppHack != null ) { 
                mEnableWhatsAppHack.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_WHATSAPP_HACK, false));
                mEnableWhatsAppHack.setOnPreferenceChangeListener(this);
        }
        mEnableHeadphonesDetection = (SwitchPreference) findPreference(AUDIO_TWEAKS_HP_DETECT);
        if( mEnableHeadphonesDetection != null ) { 
                mEnableHeadphonesDetection.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_HP_DETECT, false));
                mEnableHeadphonesDetection.setOnPreferenceChangeListener(this);
        }

        mEnableSuspendPlay = (SwitchPreference) findPreference(AUDIO_TWEAKS_SUSPEND_PLAY);
        if( mEnableSuspendPlay != null ) { 
                mEnableSuspendPlay.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_SUSPEND_PLAY, false));
                mEnableSuspendPlay.setOnPreferenceChangeListener(this);
        }

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        

        if (preference == mEnableANC) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_ENABLE_ANC, (Boolean) newValue);
        } else if (preference == mEnableFluence) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_ENABLE_FLUENCE, (Boolean) newValue);
        } else if (preference == mEnableWhatsAppHack) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_WHATSAPP_HACK, (Boolean) newValue);
        } else if (preference == mEnableHeadphonesDetection) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_HP_DETECT, (Boolean) newValue);
        } else if (preference == mEnableSuspendPlay) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_SUSPEND_PLAY, (Boolean) newValue);
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setSystemPropertyBoolean(String key, boolean value) {
        String text = value?"1":"0";
        SystemProperties.set(key, text);
    }
}
