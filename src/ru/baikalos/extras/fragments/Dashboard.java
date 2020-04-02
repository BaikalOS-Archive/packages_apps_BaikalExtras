/*
 * Copyright (C) 2017-2018 AICP
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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;

import ru.baikalos.extras.BaseSettingsFragment;
import ru.baikalos.extras.Constants;
import ru.baikalos.extras.PreferenceMultiClickHandler;
import ru.baikalos.extras.R;
import com.aicp.gear.preference.LongClickablePreference;
import ru.baikalos.extras.utils.Util;

import android.os.SystemProperties;

import java.util.Random;

public class Dashboard extends BaseSettingsFragment {

    private static final String PREF_BAIKALOS_LOGO = "baikalos_logo";
    private static final String PREF_BAIKALOS_OTA = "baikalos_ota";
    private static final String PREF_LOG_IT = "log_it";

    private static final String PREF_BAIKALOS_PARTS = "device_part";
    private static final String PREF_BAIKALOS_PARTS_PACKAGE_NAME = "org.lineageos.settings.device";


    private static final Intent INTENT_OTA = new Intent().setComponent(new ComponentName(
            Constants.BAIKALOS_OTA_PACKAGE, Constants.BAIKALOS_OTA_ACTIVITY));

    private LongClickablePreference mBaikalOSLogo;
    private Preference mBaikalOSOTA;
    private Preference mBaikalOSParts;

    private Random mRandom = new Random();
    private int mLogoClickCount = 0;

    @Override
    protected int getPreferenceResource() {
        return R.xml.dashboard;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageManager pm = getActivity().getPackageManager();

        boolean perfProf  = SystemProperties.get("baikal.eng.perf", "0").equals("1") || 
                            SystemProperties.get("spectrum.support", "0").equals("1");
        boolean thermProf  = SystemProperties.get("baikal.eng.therm", "0").equals("1");

        Preference profilesCategory = findPreference("app_setings_cat");

        if( !perfProf && !thermProf ) {
            if( profilesCategory != null ) {
                getPreferenceScreen().removePreference(profilesCategory);
            }
        }

        mBaikalOSLogo = (LongClickablePreference) findPreference(PREF_BAIKALOS_LOGO);

        mBaikalOSOTA = findPreference(PREF_BAIKALOS_OTA);
        if (mBaikalOSOTA != null && !Util.isPackageEnabled(Constants.BAIKALOS_OTA_PACKAGE, pm)) {
            mBaikalOSOTA.getParent().removePreference(mBaikalOSOTA);
        }

        // DeviceParts
        mBaikalOSParts = findPreference(PREF_BAIKALOS_PARTS);
        if (mBaikalOSParts != null && !Util.isPackageEnabled(PREF_BAIKALOS_PARTS_PACKAGE_NAME, pm)) {
            mBaikalOSParts.getParent().removePreference(mBaikalOSParts);
        }

        Preference logIt = findPreference(PREF_LOG_IT);
        //Util.requireRoot(getActivity(), logIt);

        mBaikalOSLogo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return true;
            }
        });
        mBaikalOSLogo.setOnLongClickListener(R.id.logo_view, 1000,
                new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            return true;
                        }
                });
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mBaikalOSOTA || preference == mBaikalOSLogo) {
            startActivity(INTENT_OTA);
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }
}
