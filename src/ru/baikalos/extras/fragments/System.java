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
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
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

import android.annotation.UserIdInt;
import android.app.backup.IBackupManager;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;


import android.content.res.Resources;

import com.android.internal.baikalos.Actions;

import com.android.internal.baikalos.AppProfileSettings;

import ru.baikalos.extras.BaseSettingsFragment;
import ru.baikalos.extras.PerfProfileDetailsActivity;
import ru.baikalos.extras.R;

import com.aicp.gear.preference.SeekBarPreferenceCham;
import com.aicp.gear.preference.SecureSettingSeekBarPreference;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class System extends BaseSettingsFragment {

    private static final String TAG = "System";

    private static final String SMART_NFC = "baikalos_smart_nfc";


    private Context mContext;

    private ListPreference mDefaultPerfProfile;
    private ListPreference mDefaultThermProfile;

    private ListPreference mScrOffPerfProfile;
    private ListPreference mIdlePerfProfile;
    private ListPreference mIdleThermProfile;
    private ListPreference mEditPerfProfile;
    private ListPreference mAppFpsProfile;
    private SwitchPreference mSmartNFC;

    //private SwitchPreference mBrForceAll;
    //private ListPreference mBrBackup;
    //private ListPreference mBrRestore;
    //private ListPreference mBrRestoreApp;

    //private BackupUtil mBackupUtil;

    @Override
    protected int getPreferenceResource() {
        return R.xml.system;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();

        final PreferenceScreen screen = getPreferenceScreen();

        final PreferenceCategory profilesCategory =
                (PreferenceCategory) screen.findPreference("default_profiles");


        //mBackupUtil = new BackupUtil();

        boolean perfProf  = SystemProperties.get("baikal.eng.perf", "0").equals("1");
        boolean thermProf  = SystemProperties.get("baikal.eng.therm", "0").equals("1");
        boolean perfEdit  = SystemProperties.get("baikal.eng.perf.edit", "0").equals("1");

        if( !perfProf && !thermProf ) {
            if( profilesCategory != null ) {
                screen.removePreference(profilesCategory);
            }
        }

    
        mSmartNFC = (SwitchPreference) findPreference(SMART_NFC);
        if( mSmartNFC != null ) { 
            if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
                mSmartNFC.setVisible(false);
            }
        }

        try {

            boolean variableFps  = SystemProperties.get("sys.baikal.var_fps", "1").equals("1");

            mAppFpsProfile = (ListPreference) findPreference("default_fps");
            if( mAppFpsProfile != null ) {
                if(!variableFps) {
                    mAppFpsProfile.setVisible(false);
                } else {
                    int fps = getSystemPropertyInt("persist.baikal.fps.default",0);
                    Log.i(TAG, "getDefaultFps: fps=" + fps);
                    mAppFpsProfile.setValue(Integer.toString(fps));
                    mAppFpsProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            setSystemPropertyInt("persist.baikal.fps.default",val);
                            //mBaikalService.setAppBrightness(mPackageName, val );
                            Log.e(TAG, "setDefaultFps: fps=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: setDefaultFps Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }


            mDefaultPerfProfile = (ListPreference) findPreference("default_perf_profile");
            if( mDefaultPerfProfile != null ) { 
                if( !perfProf ) {
                    mDefaultPerfProfile.setVisible(false);
                } else {
                    String profile = getSystemPropertyString("persist.baikal.perf.default","balance");
                    Log.e(TAG, "mDefaultPerfProfile: getProfile=" + profile);
                    mDefaultPerfProfile.setValue(profile);
                    mDefaultPerfProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, "mDefaultPerfProfile: setProfile=" + newValue.toString());
			                setSystemPropertyString("persist.baikal.perf.default",newValue.toString());
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
                    String profile = getSystemPropertyString("persist.baikal.therm.default","balance");
                    Log.e(TAG, "mDefaultThermProfile: getProfile=" + profile);
                    mDefaultThermProfile.setValue(profile);
                    mDefaultThermProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, "mDefaultThermProfile: setProfile=" + newValue.toString());
			                setSystemPropertyString("persist.baikal.therm.default",newValue.toString());
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mDefaultPerfProfile Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }


            mScrOffPerfProfile = (ListPreference) findPreference("scr_off_perf_profile");
            if( mScrOffPerfProfile != null ) { 
                if( !perfProf ) {
                    mScrOffPerfProfile.setVisible(false);
                } else {
                    String profile = getSystemPropertyString("persist.baikal.perf.scr_off","limited");
                    Log.e(TAG, "mScrOffPerfProfile: getProfile=" + profile);
                    mScrOffPerfProfile.setValue(profile);
                    mScrOffPerfProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, "mScrOffPerfProfile: setProfile=" + newValue.toString());
			                setSystemPropertyString("persist.baikal.perf.scr_off",newValue.toString());
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mScrOffPerfProfile Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }


            mIdlePerfProfile = (ListPreference) findPreference("idle_perf_profile");
            if( mIdlePerfProfile != null ) { 
                if( !perfProf ) {
                    mIdlePerfProfile.setVisible(false);
                } else {
                    String profile = getSystemPropertyString("persist.baikal.perf.idle","limited");
                    Log.e(TAG, "mIdlePerfProfile: getProfile=" + profile);
                    mIdlePerfProfile.setValue(profile);
                    mIdlePerfProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, "mIdlePerfProfile: setProfile=" + newValue.toString());
			                setSystemPropertyString("persist.baikal.perf.idle",newValue.toString());
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mIdlePerfProfile Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }

            mIdleThermProfile = (ListPreference) findPreference("idle_therm_profile");
            if( mIdleThermProfile != null ) { 
                if( !perfProf ) {
                    mIdleThermProfile.setVisible(false);
                } else {
                    String profile = getSystemPropertyString("persist.baikal.therm.idle","cool");
                    Log.e(TAG, "mIdleThermProfile: getProfile=" + profile);
                    mIdleThermProfile.setValue(profile);
                    mIdleThermProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, "mIdleThermProfile: setProfile=" + newValue.toString());
			                setSystemPropertyString("persist.baikal.therm.idle",newValue.toString());
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mIdleThermProfile Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }

            mEditPerfProfile = (ListPreference) findPreference("edit_perf_profile");
            if( mEditPerfProfile != null ) { 
                if( !perfProf || !perfEdit ) {
                    mEditPerfProfile.setVisible(false);
                } else {
                    //String profile = getSystemPropertyString("persist.baikal.therm.idle","cool");
                    //Log.e(TAG, "mIdleThermProfile: getProfile=" + profile);
                    //mIdleThermProfile.setValue(profile);
                    mEditPerfProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, "mEditPerfProfile: setProfile=" + newValue.toString());
                            Intent intent = new Intent(mContext, PerfProfileDetailsActivity.class);
                            intent.putExtra("profileName",newValue.toString());
                            startActivity(intent);

                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mEditPerfProfile Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }


            //mBrForceAll = (SwitchPreference)  findPreference("backup_restore_force_all");
            //mBrForceAll.setChecked(false);

            /*mBrRestore = (ListPreference) findPreference("backup_restore_restore");
            if( mBrRestore != null ) { 

                    if( SystemProperties.get("sys.baikal.restore_inp", "0").equals("1") ) {
                        mBrRestore.setEnabled(false);
                    } else {
                        mBrRestore.setEnabled(true);
                    }

                    updateRestoreValues();
                    mBrRestore.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, "mBrRestore: restore=" + newValue.toString());
                            //intent.putExtra("profileName",newValue.toString());
                            systemRestore(newValue.toString());
                            mBrRestore.setEnabled(false);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mBrRestore Fatal! exception", re );
                        }
                        return true;
                      }
                    });
            }*/




            /*mBrBackup = (ListPreference) findPreference("backup_restore_backup");
            if( mBrBackup != null ) { 

                    if( SystemProperties.get("sys.baikal.backup_inp", "0").equals("1") ) {
                        mBrBackup.setEnabled(false);
                    } else {
                        mBrBackup.setEnabled(true);
                    }

                    mBrBackup.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, "mBrBackup: backup=" + newValue.toString());
                            //intent.putExtra("profileName",newValue.toString());
                            systemBackup(newValue.toString());
                            updateRestoreValues();
                            mBrBackup.setEnabled(false);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mBrBackup Fatal! exception", re );
                        }
                        return true;
                      }
                    });
            }*/


            /*
            mBrRestoreApp = (ListPreference) findPreference("backup_restore_restore_app");
            if( mBrRestoreApp != null ) { 

                    if( SystemProperties.get("sys.baikal.restore_inp", "0").equals("1") ) {
                        mBrRestoreApp.setEnabled(false);
                    } else {
                        mBrRestoreApp.setEnabled(true);
                    }

                    updateRestoreAppValues();
                    mBrRestoreApp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Log.e(TAG, "mBrRestoreApp: restore=" + newValue.toString());
                            //intent.putExtra("profileName",newValue.toString());
                            appRestore(newValue.toString());
                            mBrRestoreApp.setEnabled(false);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mBrRestoreApp Fatal! exception", re );
                        }
                        return true;
                      }
                    });
            }*/


        } catch(Exception re) {
            Log.e(TAG, "onCreate: Fatal! exception", re );
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        /*if( mBrRestoreApp != null ) { 
            if( SystemProperties.get("sys.baikal.restore_inp", "0").equals("1") ) {
                mBrRestoreApp.setEnabled(false);
            } else {
                updateRestoreAppValues();
            }
        }

        if( mBrRestore != null ) { 
            if( SystemProperties.get("sys.baikal.restore_inp", "0").equals("1") ) {
                mBrRestore.setEnabled(false);
            } else {
                updateRestoreValues();
            }
        }

        if( mBrBackup != null ) { 
            if( SystemProperties.get("sys.baikal.backup_inp", "0").equals("1") ) {
                mBrBackup.setEnabled(false);
            } else {
                mBrBackup.setEnabled(true);
            }
        }*/

    }


    /*
    private void updateRestoreValues() {
        if( mBrRestore == null ) return;
        
        ArrayList<String> values = new ArrayList<String>();
        ArrayList<String> entries = new ArrayList<String>();

    	if( isBackupPackageAvaialable("apps",".bbp") ) { 
            values.add("apps");
            entries.add("Applications only");
        }
    	if( isBackupPackageAvaialable("apps_obb",".bbp") ) { 
            values.add("apps_obb");
            entries.add("Applications with OBB");
        }

        String[] entriesArray = new String[entries.size()];
        String[] valuesArray = new String[values.size()];

        mBrRestore.setEntries(entries.toArray(entriesArray));
        mBrRestore.setEntryValues(values.toArray(valuesArray));

        if( values.size() == 0 ) {
            mBrRestore.setEnabled(false);
        } else {
            mBrRestore.setEnabled(true);
        }
    }

    private void updateRestoreAppValues() {
        if( mBrRestoreApp == null ) return;
        
        ArrayList<String> values = new ArrayList<String>();
        ArrayList<String> entries = new ArrayList<String>();

        String path = "/sdcard/baikalos/backup/";
        
        File mDir = new File(path);
        if( mDir != null ) {
            File[] files = mDir.listFiles(new PatternFilenameFilter(".*\\.bba"));
            if( files != null && files.length != 0 ) {
                for(File file: files) {
                    values.add(getBaseName(file.getName()));
                    entries.add(getBaseName(file.getName()));
                }
            }
        }

        String[] entriesArray = new String[entries.size()];
        String[] valuesArray = new String[values.size()];

        mBrRestoreApp.setEntries(entries.toArray(entriesArray));
        mBrRestoreApp.setEntryValues(values.toArray(valuesArray));


        if( values.size() == 0 ) {
            mBrRestoreApp.setEnabled(false);
        } else {
            mBrRestoreApp.setEnabled(true);
        }
    }

    private boolean isBackupPackageAvaialable(String packageName, String ext) {
        String path = "/sdcard/baikalos/backup/" + packageName + ext;
        return (new File(path).exists());
    }

    private String getBaseName(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }

    private void systemBackup(String backup) {
        mBackupUtil.systemBackup(backup, true );
    }

    private void systemRestore(String restore) {
        mBackupUtil.systemRestore(restore);        
    }

    private void appRestore(String restore) {
        mBackupUtil.appRestore(restore);        
    }
    */

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

    public final class PatternFilenameFilter implements FilenameFilter {

      private final Pattern pattern;
      public PatternFilenameFilter(String patternStr) {
        this(Pattern.compile(patternStr));
      }

      public PatternFilenameFilter(Pattern pattern) {
        this.pattern = pattern; // Preconditions.checkNotNull(pattern);
      }

      @Override
      public boolean accept(File dir, String fileName) {
        return pattern.matcher(fileName).matches();
      }
    }

}
