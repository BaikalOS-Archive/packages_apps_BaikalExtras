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
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.res.Resources;

import com.android.internal.baikalos.Actions;

import com.android.internal.baikalos.AppProfileSettings;

import ru.baikalos.extras.BaseSettingsFragment;
import ru.baikalos.extras.PerfProfileDetailsActivity;
import ru.baikalos.extras.R;

import com.aicp.gear.preference.SeekBarPreferenceCham;
import com.aicp.gear.preference.SecureSettingSeekBarPreference;

public class Performance extends BaseSettingsFragment {

    private static final String TAG = "BaikalExtras";

    private Context mContext;

    private Preference mBackup;
    private Preference mRestore;
    private Preference mReset;

    @Override
    protected int getPreferenceResource() {
        return R.xml.performance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();

        final PreferenceScreen screen = getPreferenceScreen();

        final PreferenceCategory profilesCategory =
                (PreferenceCategory) screen.findPreference("default_profiles");


        boolean perfProf  = SystemProperties.get("baikal.eng.perf", "0").equals("1");
        boolean thermProf  = SystemProperties.get("baikal.eng.therm", "0").equals("1");
        boolean perfEdit  = SystemProperties.get("baikal.eng.perf.edit", "0").equals("1");

        if( !perfProf && !thermProf ) {
            if( profilesCategory != null ) {
                screen.removePreference(profilesCategory);
            }
        }

        try {

            boolean variableFps  = SystemProperties.get("sys.baikal.var_fps", "1").equals("1");

            mBackup = (Preference) findPreference("app_setings_backup");
            mRestore = (Preference) findPreference("app_setings_restore");
            mReset = (Preference) findPreference("app_setings_reset");

        } catch(Exception re) {
            Log.e(TAG, "onCreate: Fatal! exception", re );
        }

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mBackup) {
            backItUp();
            return true;
        } else if (preference == mRestore) {
            restore();
            return true;
        } else if (preference == mReset) {
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


    public void backItUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.app_setings_backup_title);
        builder.setMessage(R.string.app_setings_backup_summary);
        builder.setPositiveButton(R.string.app_setings_backup_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                AppProfileSettings.saveBackup(Performance.this.getActivity().getContentResolver());
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void restore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.app_setings_restore_title);
        builder.setMessage(R.string.app_setings_restore_summary);
        builder.setPositiveButton(R.string.app_setings_restore_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                AppProfileSettings.restoreBackup(Performance.this.getActivity().getContentResolver());
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void reset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.app_setings_reset_title);
        builder.setMessage(R.string.app_setings_reset_summary);
        builder.setPositiveButton(R.string.app_setings_reset_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                AppProfileSettings.resetAll(Performance.this.getActivity().getContentResolver());
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void setSystemPropertyString(String key, String value) {
        Log.i(TAG, "setSystemPropertyString: key=" + key + ", value=" + value);
        try {
            SystemProperties.set(key, value);
        } catch(Exception e) {
            Log.e(TAG, "setSystemPropertyString: ", e);
        }
    }

    private String getSystemPropertyString(String key, String def) {
        return SystemProperties.get(key,def);
    }

    private int getSystemPropertyInt(String key, int def) {
        try {
            String value = getSystemPropertyString(key, Integer.toString(def));
            return Integer.parseInt(value);
        } catch (Exception e) { };
       
        return def;
    }

    private void setSystemPropertyInt(String key, int value) {
        try {
            setSystemPropertyString(key, Integer.toString(value));
        } catch (Exception e) { };
    }


    private boolean getSystemPropertyBoolean(String key) {
        if( SystemProperties.get(key,"0").equals("1") || SystemProperties.get(key,"0").equals("true") ) return true;
	    return false;
    }

    private void setSystemPropertyBoolean(String key, boolean value) {
        String text = value?"1":"0";
        Log.i(TAG, "setSystemPropertyBoolean: key=" + key + ", value=" + value);
        try {
            SystemProperties.set(key, text);
        } catch(Exception e) {
            Log.e(TAG, "setSystemPropertyBoolean: ", e);
        }
    }
}
