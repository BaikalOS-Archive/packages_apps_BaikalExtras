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

import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import android.os.Environment;
import android.net.Uri;

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

import java.io.File;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

import android.content.res.Resources;

import ru.baikalos.extras.BaseSettingsFragment;
import ru.baikalos.extras.R;
import com.aicp.gear.preference.SeekBarPreferenceCham;

public class AudioTweaks extends BaseSettingsFragment
            implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String TAG = "AudioTweaks";

    private static final String AUDIO_TWEAKS_SUSPEND_PLAY = "audio_tweaks_suspend_play";
    private static final String AUDIO_TWEAKS_AUDIO_HQ = "audio_tweaks_hq_policy";

    private static final String AUDIO_TWEAKS_SCAN_MEDIA = "audio_tweaks_scan_media";

    private static final String AUDIO_TWEAKS_A2DP_SBC_HD = "audio_tweaks_a2dp_sbc_hd";
    private static final String AUDIO_TWEAKS_A2DP_SBC_HDX = "audio_tweaks_a2dp_sbc_hdx";
    private static final String AUDIO_TWEAKS_A2DP_SBC_HDU = "audio_tweaks_a2dp_sbc_hdu";
    private static final String AUDIO_TWEAKS_A2DP_SBC_48 = "audio_tweaks_a2dp_sbc_48";
    private static final String AUDIO_TWEAKS_NEW_AVRCP = "audio_tweaks_new_avrcp";

    private static final String AUDIO_TWEAKS_A2DP_LAST_CODEC = "audio_tweaks_a2dp_last_codec";
    private static final String AUDIO_TWEAKS_A2DP_LAST_BITRATE = "audio_tweaks_a2dp_last_bitrate";

    private static final String AUDIO_TWEAKS_SONIF_A2DP = "audio_tweaks_sonif_a2dp";
    private static final String AUDIO_TWEAKS_ASSIST_A2DP = "audio_tweaks_assist_a2dp";

    private static final String AUDIO_TWEAKS_MEDIA_A2DP = "audio_tweaks_media_a2dp";
    private static final String AUDIO_TWEAKS_NONE_A2DP = "audio_tweaks_none_a2dp";

    private static final String AUDIO_EFFECTS_SYSTEM = "audio_effects_system";
    private static final String AUDIO_EFFECTS_QC = "audio_effects_qc";
    private static final String AUDIO_EFFECTS_DOLBY = "audio_effects_dolby";
    private static final String SYSTEM_PROPERTY_EFFECTS_SYSTEM = "persist.baikal.ae.disable";
    private static final String SYSTEM_PROPERTY_EFFECTS_QC = "persist.baikal.qcae.disable";
    private static final String SYSTEM_PROPERTY_EFFECTS_DOLBY = "persist.baikal.dolby.enable";
    private static final String SYSTEM_PROPERTY_DOLBY_AVAILABLE = "sys.baikal.dolby.avail";
    private static final String SYSTEM_PROPERTY_QCAE_AVAILABLE = "sys.baikal.qcae.avail";


    private static final String SYSTEM_PROPERTY_AUDIO_HQ = "persist.baikal_audio_hq";
    private static final String SYSTEM_PROPERTY_SUSPEND_PLAY = "persist.audio.offload.suspend";
    private static final String SYSTEM_PROPERTY_A2DP_SBC_HD = "persist.bluetooth.sbc_hd";
    private static final String SYSTEM_PROPERTY_A2DP_SBC_HDX = "persist.bluetooth.sbc_hdx";
    private static final String SYSTEM_PROPERTY_A2DP_SBC_HDU = "persist.bluetooth.sbc_hdu";
    private static final String SYSTEM_PROPERTY_A2DP_SBC_48 = "persist.bluetooth.sbc_48";

    private static final String SYSTEM_PROPERTY_NEW_AVRCP = "persist.bluetooth.enablenewavrcp";

    private static final String SYSTEM_PROPERTY_A2DP_LAST_CODEC = "baikal.last.a2dp_codec";
    private static final String SYSTEM_PROPERTY_A2DP_LAST_BITRATE = "baikal.last.a2dp_bitrate";

    private static final String SYSTEM_PROPERTY_SONIF_A2DP = "persist.baikal.sonif_a2dp";
    private static final String SYSTEM_PROPERTY_ASSIST_A2DP = "persist.baikal.assist_a2dp";
    private static final String SYSTEM_PROPERTY_MEDIA_A2DP = "persist.baikal.media_a2dp";
    private static final String SYSTEM_PROPERTY_NONE_A2DP = "persist.baikal.none_a2dp";

    private static final String HQ_AUDIO_SYSTEM_PATH = "/vendor/etc/audio_policy_configuration_hq.xml";


    private Context mContext;

    private SwitchPreference mEnableAudioHq;
    private SwitchPreference mEnableSuspendPlay;

    private SwitchPreference mEnableA2DPHD;
    private SwitchPreference mEnableA2DPHDX;
    private SwitchPreference mEnableA2DPHDU;
    private SwitchPreference mEnableA2DP48;
    private SwitchPreference mEnableNewAvrcp;

    private SwitchPreference mEnableSonifA2DP;
    private SwitchPreference mEnableAssistA2DP;

    private SwitchPreference mEnableMediaA2DP;
    private SwitchPreference mEnableNoneA2DP;

    private SwitchPreference mEffectsSystem;
    private SwitchPreference mEffectsDolby;
    private SwitchPreference mEffectsQc;




    private Preference mScanMedia;

    @Override
    protected int getPreferenceResource() {
        return R.xml.audio_tweaks;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();

        Boolean isDolbyAvail = SystemProperties.getBoolean(SYSTEM_PROPERTY_DOLBY_AVAILABLE,false);

        mEffectsDolby = (SwitchPreference) findPreference(AUDIO_EFFECTS_DOLBY);

        if( mEffectsDolby != null ) { 
            if( !isDolbyAvail ) {
                mEffectsDolby.setVisible(false);
            } else {
                mEffectsDolby.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_EFFECTS_DOLBY, false));
                mEffectsDolby.setOnPreferenceChangeListener(this);
            }
        }

        Boolean isQcaeAvail = SystemProperties.getBoolean(SYSTEM_PROPERTY_QCAE_AVAILABLE,false);

        mEffectsQc = (SwitchPreference) findPreference(AUDIO_EFFECTS_QC);

        if( mEffectsQc != null ) { 
            if( !isQcaeAvail ) {
                mEffectsQc.setVisible(false);
            } else {
                mEffectsQc.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_EFFECTS_QC, false));
                mEffectsQc.setOnPreferenceChangeListener(this);
            }
        }

        mEffectsSystem = (SwitchPreference) findPreference(AUDIO_EFFECTS_SYSTEM);

        if( mEffectsSystem != null ) { 
            mEffectsSystem.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_EFFECTS_SYSTEM, false));
            mEffectsSystem.setOnPreferenceChangeListener(this);
        }



        Preference  codec = (Preference) findPreference(AUDIO_TWEAKS_A2DP_LAST_CODEC);
        Preference  bitrate = (Preference) findPreference(AUDIO_TWEAKS_A2DP_LAST_BITRATE);

        String sCodec = SystemProperties.get(SYSTEM_PROPERTY_A2DP_LAST_CODEC,"");
        String sBitrate = SystemProperties.get(SYSTEM_PROPERTY_A2DP_LAST_BITRATE,"");


        if( sCodec != null && sCodec.startsWith("SBC HD") ) {
            codec.setVisible(true);
            codec.setSummary(sCodec);
            bitrate.setVisible(true);
            bitrate.setSummary(sBitrate + " kBit/s");
        } 
        else if( sCodec == null || sCodec.equals("") || sCodec.equals("NONE") ) {
            codec.setVisible(false);
            codec.setSummary("");
            bitrate.setVisible(false);
            bitrate.setSummary("");
        } else {
            codec.setVisible(true);
            codec.setSummary(sCodec);
            bitrate.setVisible(false);
            bitrate.setSummary("");
        }

        mScanMedia = (Preference) findPreference(AUDIO_TWEAKS_SCAN_MEDIA);
        if( mScanMedia != null ) { 
                mScanMedia.setOnPreferenceClickListener(this);
        }

        mEnableAudioHq = (SwitchPreference) findPreference(AUDIO_TWEAKS_AUDIO_HQ);
        if( mEnableAudioHq != null ) { 
                if( isHqModeAvaialable() ) {
                    mEnableAudioHq.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_AUDIO_HQ, false));
                    mEnableAudioHq.setOnPreferenceChangeListener(this);
                } else {
                    if( SystemProperties.getBoolean(SYSTEM_PROPERTY_AUDIO_HQ, false) ) {
                        setSystemPropertyBoolean(SYSTEM_PROPERTY_AUDIO_HQ, false);
                    }
                    mEnableAudioHq.setVisible(false);
                }
        } else {
            if( SystemProperties.getBoolean(SYSTEM_PROPERTY_AUDIO_HQ, false) ) {
                setSystemPropertyBoolean(SYSTEM_PROPERTY_AUDIO_HQ, false);
            }
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

        mEnableA2DPHDU = (SwitchPreference) findPreference(AUDIO_TWEAKS_A2DP_SBC_HDU);
        if( mEnableA2DPHDU != null ) { 
                if( !enableSbcHd ) {
                    setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_HDU, false);
                    mEnableA2DPHDU.setEnabled(false);
                    mEnableA2DPHDU.setChecked(false);
                } else {
                    mEnableA2DPHDU.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_A2DP_SBC_HDU, false));
                }
                mEnableA2DPHDU.setOnPreferenceChangeListener(this);
        }

        mEnableA2DP48 = (SwitchPreference) findPreference(AUDIO_TWEAKS_A2DP_SBC_48);
        if( mEnableA2DP48 != null ) { 
                if( !enableSbcHd ) {
                    setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_48, false);
                    mEnableA2DP48.setEnabled(false);
                    mEnableA2DP48.setChecked(false);
                } else {
                    mEnableA2DP48.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_A2DP_SBC_48, false));
                }
                mEnableA2DP48.setOnPreferenceChangeListener(this);
        }

        mEnableNewAvrcp = (SwitchPreference) findPreference(AUDIO_TWEAKS_NEW_AVRCP);
        if( mEnableNewAvrcp != null ) { 
                mEnableNewAvrcp.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_NEW_AVRCP, false));
                mEnableNewAvrcp.setOnPreferenceChangeListener(this);
        }

        mEnableSonifA2DP = (SwitchPreference) findPreference(AUDIO_TWEAKS_SONIF_A2DP);
        if( mEnableSonifA2DP != null ) { 
                mEnableSonifA2DP.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_SONIF_A2DP, false));
                mEnableSonifA2DP.setOnPreferenceChangeListener(this);
        }

        mEnableAssistA2DP = (SwitchPreference) findPreference(AUDIO_TWEAKS_ASSIST_A2DP);
        if( mEnableAssistA2DP != null ) { 
                mEnableAssistA2DP.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_ASSIST_A2DP, false));
                mEnableAssistA2DP.setOnPreferenceChangeListener(this);
        }

        mEnableMediaA2DP = (SwitchPreference) findPreference(AUDIO_TWEAKS_MEDIA_A2DP);
        if( mEnableMediaA2DP != null ) { 
                mEnableMediaA2DP.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_MEDIA_A2DP, false));
                mEnableMediaA2DP.setOnPreferenceChangeListener(this);
        }

        mEnableNoneA2DP = (SwitchPreference) findPreference(AUDIO_TWEAKS_NONE_A2DP);
        if( mEnableNoneA2DP != null ) { 
                mEnableNoneA2DP.setChecked(SystemProperties.getBoolean(SYSTEM_PROPERTY_NONE_A2DP, false));
                mEnableNoneA2DP.setOnPreferenceChangeListener(this);
        }

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mScanMedia) {
            final Intent intent = new Intent("android.intent.action.MEDIA_MOUNTED",Uri.parse("file://" + Environment.getExternalStorageDirectory()));
            final ComponentName serviceComponent = ComponentName.unflattenFromString(
                    "com.android.providers.media.module/com.android.providers.media.MediaService");
            intent.setComponent(serviceComponent);
            Log.e(TAG, "sendBroadcastAsUser: intent=" + intent);
            //mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
            mContext.startService(intent);
            return true;
        }
        return false;
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mEnableAudioHq) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_AUDIO_HQ, (Boolean) newValue);
        } else if (preference == mEnableSuspendPlay) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_SUSPEND_PLAY, (Boolean) newValue);
        } else if (preference == mEnableNewAvrcp) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_NEW_AVRCP, (Boolean) newValue);
        } else if (preference == mEnableA2DPHD) {
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_HD, (Boolean) newValue);

            Log.e(TAG, "onPreferenceChange: mEnableA2DPHD key=" + SYSTEM_PROPERTY_A2DP_SBC_HD + ", value=" + (Boolean)newValue);

            if( !(Boolean)newValue ) {
                mEnableA2DPHDX.setChecked(false);
                mEnableA2DPHDX.setEnabled(false);
                setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_HDX, false);

                mEnableA2DPHDU.setChecked(false);
                mEnableA2DPHDU.setEnabled(false);
                setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_HDU, false);

                mEnableA2DP48.setChecked(false);
                mEnableA2DP48.setEnabled(false);
                setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_48, false);

            } else {
                mEnableA2DPHDX.setEnabled(true);
                mEnableA2DPHDU.setEnabled(true);
                mEnableA2DP48.setEnabled(true);
            }
        } else if (preference == mEnableA2DPHDX) {
            Log.e(TAG, "onPreferenceChange: mEnableA2DPHDX key=" + SYSTEM_PROPERTY_A2DP_SBC_HDX + ", value=" + (Boolean)newValue);
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_HDX, (Boolean) newValue);
        } else if (preference == mEnableA2DPHDU) {
            Log.e(TAG, "onPreferenceChange: mEnableA2DPHDU key=" + SYSTEM_PROPERTY_A2DP_SBC_HDU + ", value=" + (Boolean)newValue);
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_HDU, (Boolean) newValue);
        } else if (preference == mEnableA2DP48) {
            Log.e(TAG, "onPreferenceChange: mEnableA2DP48 key=" + SYSTEM_PROPERTY_A2DP_SBC_48 + ", value=" + (Boolean)newValue);
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_A2DP_SBC_48, (Boolean) newValue);
        } else if (preference == mEnableSonifA2DP) {
            Log.e(TAG, "onPreferenceChange: mEnableSonifA2DP key=" + SYSTEM_PROPERTY_SONIF_A2DP + ", value=" + (Boolean)newValue);
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_SONIF_A2DP, (Boolean) newValue);
        } else if (preference == mEnableAssistA2DP) {
            Log.e(TAG, "onPreferenceChange: mEnableAssistA2DP key=" + SYSTEM_PROPERTY_ASSIST_A2DP + ", value=" + (Boolean)newValue);
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_ASSIST_A2DP, (Boolean) newValue);
        } else if (preference == mEnableMediaA2DP) {
            Log.e(TAG, "onPreferenceChange: mEnableMediaA2DP key=" + SYSTEM_PROPERTY_MEDIA_A2DP + ", value=" + (Boolean)newValue);
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_MEDIA_A2DP, (Boolean) newValue);
        } else if (preference == mEnableNoneA2DP) {
            Log.e(TAG, "onPreferenceChange: mEnableNoneA2DP key=" + SYSTEM_PROPERTY_NONE_A2DP + ", value=" + (Boolean)newValue);
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_NONE_A2DP, (Boolean) newValue);
        } else if (preference == mEffectsSystem) {
            Log.e(TAG, "onPreferenceChange: mEffectsSystem key=" + SYSTEM_PROPERTY_EFFECTS_SYSTEM + ", value=" + (Boolean)newValue);
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_EFFECTS_SYSTEM, (Boolean) newValue);
        } else if (preference == mEffectsQc) {
            Log.e(TAG, "onPreferenceChange: mEffectsQc key=" + SYSTEM_PROPERTY_EFFECTS_QC + ", value=" + (Boolean)newValue);
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_EFFECTS_QC, (Boolean) newValue);
        } else if (preference == mEffectsDolby) {
            Log.e(TAG, "onPreferenceChange: mEffectsDolby key=" + SYSTEM_PROPERTY_EFFECTS_DOLBY + ", value=" + (Boolean)newValue);
            ((SwitchPreference)preference).setChecked((Boolean) newValue);
            setSystemPropertyBoolean(SYSTEM_PROPERTY_EFFECTS_DOLBY, (Boolean) newValue);
            updateDolbyConfiguration((Boolean)newValue);
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

    private void updateDolbyConfiguration(boolean enabled) {
        if( !enabled ) {
            setPackageEnabled("com.motorola.dolby.dolbyui",false);
            setPackageEnabled("com.dolby.daxservice",false);
        } else {
            //setPackageEnabled("com.motorola.dolby.dolbyui",true);
            //setPackageEnabled("com.dolby.daxservice",true);
        }
    }

    private void setPackageEnabled(String packageName, boolean enabled) {
        int state = enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED;
        try {
            getActivity().getPackageManager().setApplicationEnabledSetting(packageName,state,0);
        } catch(Exception e1) {
            Log.e(TAG, "setPackageEnabled: exception=", e1);
        }
    }

    private boolean isHqModeAvaialable() {
        return (new File(HQ_AUDIO_SYSTEM_PATH).exists());
    }

}
