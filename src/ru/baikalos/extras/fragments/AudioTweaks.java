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
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.view.View;
import android.util.Log;

import android.content.res.Resources;

import ru.baikalos.extras.BaseSettingsFragment;
import ru.baikalos.extras.R;
import com.aicp.gear.preference.SeekBarPreferenceCham;

public class AudioTweaks extends BaseSettingsFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "AudioTweaks";

    private static final String AUDIO_TWEAKS_SUSPEND_PLAY = "audio_tweaks_suspend_play";
    private static final String AUDIO_TWEAKS_AUDIO_HQ = "audio_tweaks_hq_policy";

    private static final String AUDIO_TWEAKS_A2DP_SBC_HD = "audio_tweaks_a2dp_sbc_hd";
    private static final String AUDIO_TWEAKS_A2DP_SBC_HDX = "audio_tweaks_a2dp_sbc_hdx";

    private static final String AUDIO_TWEAKS_A2DP_LAST_CODEC = "audio_tweaks_a2dp_last_codec";
    private static final String AUDIO_TWEAKS_A2DP_LAST_BITRATE = "audio_tweaks_a2dp_last_bitrate";

    private static final String SYSTEM_PROPERTY_AUDIO_HQ = "persist.baikal_audio_hq";
    private static final String SYSTEM_PROPERTY_SUSPEND_PLAY = "persist.audio.offload.suspend";
    private static final String SYSTEM_PROPERTY_A2DP_SBC_HD = "persist.bluetooth.sbc_hd";
    private static final String SYSTEM_PROPERTY_A2DP_SBC_HDX = "persist.bluetooth.sbc_hdx";

    private static final String SYSTEM_PROPERTY_A2DP_LAST_CODEC = "baikal.last.a2dp_codec";
    private static final String SYSTEM_PROPERTY_A2DP_LAST_BITRATE = "baikal.last.a2dp_bitrate";

    private Context mContext;

    private SwitchPreference mEnableAudioHq;
    private SwitchPreference mEnableSuspendPlay;

    private SwitchPreference mEnableA2DPHD;
    private SwitchPreference mEnableA2DPHDX;

    @Override
    protected int getPreferenceResource() {
        return R.xml.audio_tweaks;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();


        Preference  codec = (Preference) findPreference(AUDIO_TWEAKS_A2DP_LAST_CODEC);
        Preference  bitrate = (Preference) findPreference(AUDIO_TWEAKS_A2DP_LAST_BITRATE);

        String sCodec = SystemProperties.get(SYSTEM_PROPERTY_A2DP_LAST_CODEC,"");
        String sBitrate = SystemProperties.get(SYSTEM_PROPERTY_A2DP_LAST_BITRATE,"");

        switch(sCodec) {
            case "":
                    codec.setVisible(false);
                    codec.setSummary("");
                    bitrate.setVisible(false);
                    bitrate.setSummary("");
                break;

            case "SBC HD":
            case "SBC HDX":
                    codec.setVisible(true);
                    codec.setSummary(sCodec);
                    bitrate.setVisible(true);
                    bitrate.setSummary(sBitrate + " kBit/s");
                break;

            default:
                    codec.setVisible(true);
                    codec.setSummary(sCodec);
                    bitrate.setVisible(false);
                    bitrate.setSummary("");
                break;
        }


        mEnableAudioHq = (SwitchPreference) findPreference(AUDIO_TWEAKS_AUDIO_HQ);
        if( mEnableAudioHq != null ) { 
                mEnableAudioHq.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_AUDIO_HQ, false));
                mEnableAudioHq.setOnPreferenceChangeListener(this);
        }

        mEnableSuspendPlay = (SwitchPreference) findPreference(AUDIO_TWEAKS_SUSPEND_PLAY);
        if( mEnableSuspendPlay != null ) { 
                mEnableSuspendPlay.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_SUSPEND_PLAY, false));
                mEnableSuspendPlay.setOnPreferenceChangeListener(this);
        }

        boolean enableSbcHd = false;
        mEnableA2DPHD = (SwitchPreference) findPreference(AUDIO_TWEAKS_A2DP_SBC_HD);
        if( mEnableA2DPHD != null ) { 
                enableSbcHd = SystemProperties.getBoolean(SYSTEM_PROPERTY_A2DP_SBC_HD,false);
                mEnableA2DPHD.setChecked(enableSbcHd);
                mEnableA2DPHD.setOnPreferenceChangeListener(this);
        }

        mEnableA2DPHDX = (SwitchPreference) findPreference(AUDIO_TWEAKS_A2DP_SBC_HDX);
        if( mEnableA2DPHDX != null ) { 
                if( !enableSbcHd ) {
                    setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_HDX, false);
                    mEnableA2DPHDX.setEnabled(false);
                    mEnableA2DPHDX.setChecked(false);
                } else {
                    mEnableA2DPHDX.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_A2DP_SBC_HDX, false));
                }
                mEnableA2DPHDX.setOnPreferenceChangeListener(this);
        }

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        

        if (preference == mEnableAudioHq) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_AUDIO_HQ, (Boolean) newValue);
        } else if (preference == mEnableSuspendPlay) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_SUSPEND_PLAY, (Boolean) newValue);
        } else if (preference == mEnableA2DPHD) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_HD, (Boolean) newValue);

            Log.e(TAG, "onPreferenceChange: mEnableA2DPHD key=" + SYSTEM_PROPERTY_A2DP_SBC_HD + ", value=" + (Boolean)newValue);

            if( !(Boolean)newValue ) {
                mEnableA2DPHDX.setChecked(false);
                mEnableA2DPHDX.setEnabled(false);
                setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_HDX, false);
            } else {
                mEnableA2DPHDX.setEnabled(true);
            }
        } else if (preference == mEnableA2DPHDX) {
            Log.e(TAG, "onPreferenceChange: mEnableA2DPHDX key=" + SYSTEM_PROPERTY_A2DP_SBC_HDX + ", value=" + (Boolean)newValue);
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_HDX, (Boolean) newValue);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setSystemPropertyBoolean(String key, boolean value) {
        String text = value?"1":"0";
        Log.e(TAG, "setSystemPropertyBoolean: key=" + key + ", value=" + value);
        SystemProperties.set(key, text);
    }
}
