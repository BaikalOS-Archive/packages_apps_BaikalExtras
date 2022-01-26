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
import android.os.Process;
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
import com.android.internal.baikalos.BaikalSettings;
import com.android.internal.baikalos.PowerWhitelistBackend;
import com.android.internal.baikalos.BaikalSpoofer;

import ru.baikalos.extras.BaseSettingsFragment;
import ru.baikalos.extras.R;
import com.aicp.gear.preference.SeekBarPreferenceCham;

import java.io.File;

public class AppProfileFragment extends BaseSettingsFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "ApplicationProfile";

    private static final String APP_PROFILE_READER = "app_profile_reader";
    private static final String APP_PROFILE_DISABLE_BOOT = "app_profile_disable_boot";
    private static final String APP_PROFILE_DISABLE_WAKEUP = "app_profile_disable_wakeup";
    private static final String APP_PROFILE_ALLOW_IDLE_NETWORK = "app_profile_idle_network";
    private static final String APP_PROFILE_PERF = "app_profile_performance";
    private static final String APP_PROFILE_THERM = "app_profile_thermal";
    private static final String APP_PROFILE_BRIGHTNESS = "app_profile_brightness";
    private static final String APP_PROFILE_ROTATION = "app_profile_rotation";
    private static final String APP_PROFILE_FPS = "app_profile_fps";
    private static final String APP_PROFILE_KEEP_ON = "app_profile_keep_on";
//    private static final String APP_PROFILE_CAMERA_HAL1 = "app_profile_camera_hal1";
    private static final String APP_PROFILE_PINNED = "app_profile_pinned";
    private static final String APP_PROFILE_STAMINA = "app_profile_stamina";
    private static final String APP_PROFILE_REQUIRE_GMS = "app_profile_require_gms";
//    private static final String APP_PROFILE_RESTRICTED = "app_profile_restricted";
    private static final String APP_PROFILE_BACKGROUND = "app_profile_background";
    private static final String APP_PROFILE_FREEZER = "app_profile_freezer";
//    private static final String APP_PROFILE_DISABLE_TWL = "app_profile_disable_twl";
    private static final String VOLUME_SCALE = "app_profile_volume_scale";

    private static final String APP_PROFILE_PERFORMANCE_SCALE = "app_profile_cpu_performance_limit";

    private static final String APP_PROFILE_BLOCK_FOCUS_RECV = "app_profile_block_focus_recv";
    private static final String APP_PROFILE_BLOCK_FOCUS_SEND = "app_profile_block_focus_send";
    private static final String APP_PROFILE_FORCE_SONIFICATION = "app_profile_force_sonification";

    private static final String APP_PROFILE_SPOOF = "app_profile_spoof";
    private static final String APP_PROFILE_PHKA = "app_profile_phka";
    private static final String APP_PROFILE_DEVMODE = "app_profile_devmode";

    private static final String APP_PROFILE_LOCATION = "app_profile_location";
    private static final String APP_PROFILE_CAMERA = "app_profile_camera";
    private static final String APP_PROFILE_MICROPHONE = "app_profile_microphone";

    private static final String APP_PROFILE_LOWRES = "app_profile_lowres";

    private String mPackageName;
    private int mUid;
    private Context mContext;

    private SwitchPreference mAppReader;
    private SwitchPreference mAppPinned;
    private SwitchPreference mAppFreezer;
    private SwitchPreference mAppStamina;
    private SwitchPreference mAppRequireGms;
    private SwitchPreference mAppDisableBoot;
    private SwitchPreference mAppDisableWakeup;
    private SwitchPreference mBlockFocusRecv;
    private SwitchPreference mBlockFocusSend;
    private SwitchPreference mForceSonification;
    private SwitchPreference mAppKeepOn;
    private SwitchPreference mLowRes;
    private SwitchPreference mAppPhkaProfile;
    private SwitchPreference mAppDevModeProfile;
    private SwitchPreference mAppAllowIdleNetwork;

    //private SwitchPreference mAppRestricted;

    private ListPreference mAppPerfProfile;
    private ListPreference mAppThermProfile;
    private ListPreference mAppBrightnessProfile;
    private ListPreference mAppRotationProfile;
    private ListPreference mAppFpsProfile;
    private ListPreference mAppBackgroundProfile;
    private ListPreference mAppSpoofProfile;
    private ListPreference mAppLocation;
    private ListPreference mAppCamera;
    private ListPreference mAppMicrophone;
    private ListPreference mPerformanceScale;

    //private Preference mAppRestore;

    private SeekBarPreferenceCham mVolumeScale;

    private AppProfileSettings mAppSettings;
    private com.android.internal.baikalos.AppProfile mProfile;

    //IBaikalServiceController mBaikalService;

    public AppProfileFragment(String packageName, int uid) {
        mPackageName = packageName; 
        mUid = uid;
    }

    @Override
    protected int getPreferenceResource() {
        return R.xml.app_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();

        boolean perfProf  = SystemProperties.get("baikal.eng.perf", "0").equals("1");
        boolean thermProf  = SystemProperties.get("baikal.eng.therm", "0").equals("1");

        boolean readerMode  = SystemProperties.get("sys.baikal.reader", "0").equals("1");
        boolean variableFps  = SystemProperties.get("sys.baikal.var_fps", "0").equals("1");
        boolean lowResSupported = SystemProperties.get("sys.baikal.lowres", "0").equals("1");

        //mBackupUtil = new BackupUtil();


        //mAppRestore = findPreference("app_backup_restore_restore");

        /*if( mPackageName == null || !isBackupAvaialable(mPackageName) ) {
            mAppRestore.setEnabled(false);
        } else {
            mAppRestore.setEnabled(true);
        }

        mAppRestore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mBackupUtil.appRestore(mPackageName);
                return true;
            }
        });
        */

        /*Preference appBackup = findPreference("app_backup_restore_backup");

        if( mPackageName == null ) {
            appBackup.setEnabled(false);
        }

        appBackup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mBackupUtil.appBackup(mPackageName);
                if( !isBackupAvaialable(mPackageName) ) {
                    mAppRestore.setEnabled(false);
                } else {
                    mAppRestore.setEnabled(true);
                }
                return true;
            }
        });*/


        mAppSettings = AppProfileSettings.getInstance(new Handler(),mContext, mContext.getContentResolver(),null);
        mProfile = mAppSettings.getProfile(mPackageName);
        if( mProfile == null ) { 
            mProfile = new com.android.internal.baikalos.AppProfile();
            mProfile.mPackageName = mPackageName;
        }
    

        try {

            mAppDisableBoot = (SwitchPreference) findPreference(APP_PROFILE_DISABLE_BOOT);
            if( mAppDisableBoot != null ) {
                mAppDisableBoot.setChecked(mProfile.mBootDisabled);
                //mAppRestricted.setChecked(mBaikalService.isAppRestrictedProfile(mPackageName));
                Log.e(TAG, "mAppDisableBoot: mPackageName=" + mPackageName + ",mBootDisabled=" + mProfile.mBootDisabled);
                mAppDisableBoot.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        //int val = Integer.parseInt(newValue.toString());
                        //DiracAudioEnhancerService.du.setHeadsetType(mContext, val);
                        try {
                            mProfile.mBootDisabled = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            //mBaikalService.setAppPriority(mPackageName, ((Boolean)newValue) ? -1 : 0 );
                            Log.e(TAG, "mAppDisableBoot: mPackageName=" + mPackageName + ",disableBoot=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppDisableBoot Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mAppDisableWakeup = (SwitchPreference) findPreference(APP_PROFILE_DISABLE_WAKEUP);
            if( mAppDisableWakeup != null ) {
                mAppDisableWakeup.setChecked(mProfile.mDisableWakeup);
                Log.e(TAG, "mAppDisableWakeup: mPackageName=" + mPackageName + ",mDisableWakeup=" + mProfile.mDisableWakeup);
                //mAppRestricted.setChecked(mBaikalService.isAppRestrictedProfile(mPackageName));
                mAppDisableWakeup.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        //int val = Integer.parseInt(newValue.toString());
                        //DiracAudioEnhancerService.du.setHeadsetType(mContext, val);
                        try {
                            mProfile.mDisableWakeup = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            //mBaikalService.setAppPriority(mPackageName, ((Boolean)newValue) ? -1 : 0 );
                            Log.e(TAG, "mAppDisableWakeup: mPackageName=" + mPackageName + ", mDisableWakeup=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppDisableWakeup Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mAppAllowIdleNetwork = (SwitchPreference) findPreference(APP_PROFILE_ALLOW_IDLE_NETWORK);
            if( mAppAllowIdleNetwork != null ) {
                mAppAllowIdleNetwork.setChecked(mProfile.mAllowIdleNetwork);
                Log.e(TAG, "mAppAllowIdleNetwork: mPackageName=" + mPackageName + ",mAppAllowIdleNetwork=" + mProfile.mAllowIdleNetwork);
                //mAppRestricted.setChecked(mBaikalService.isAppRestrictedProfile(mPackageName));
                mAppAllowIdleNetwork.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        //int val = Integer.parseInt(newValue.toString());
                        //DiracAudioEnhancerService.du.setHeadsetType(mContext, val);
                        try {
                            mProfile.mAllowIdleNetwork = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            //mBaikalService.setAppPriority(mPackageName, ((Boolean)newValue) ? -1 : 0 );
                            Log.e(TAG, "mAppAllowIdleNetwork: mPackageName=" + mPackageName + ", mAppAllowIdleNetwork=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppAllowIdleNetwork Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mAppReader = (SwitchPreference) findPreference(APP_PROFILE_READER);
            if( mAppReader != null ) {
                if( !readerMode ) {
                    mAppReader.setVisible(false);
                } else {
                    mAppReader.setChecked(mProfile.mReader);
                    //mAppRestricted.setChecked(mBaikalService.isAppRestrictedProfile(mPackageName));
                    mAppReader.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            //int val = Integer.parseInt(newValue.toString());
                            //DiracAudioEnhancerService.du.setHeadsetType(mContext, val);
                            try {
                                mProfile.mReader = ((Boolean)newValue);
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                //mBaikalService.setAppPriority(mPackageName, ((Boolean)newValue) ? -1 : 0 );
                                Log.e(TAG, "mAppReader: mPackageName=" + mPackageName + ",setReader=" + (Boolean)newValue);
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppReader Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }


            //initBaikalAppOp(APP_PROFILE_PINNED,BaikalServiceManager.OP_PINNED);
            //initBaikalAppOp(APP_PROFILE_DISABLE_TWL,BaikalServiceManager.OP_DISABLE_TWL);

            mAppPerfProfile = (ListPreference) findPreference(APP_PROFILE_PERF);
            if( mAppPerfProfile != null ) { 
                if(!perfProf) {
                    mAppPerfProfile.setVisible(false);
                } else {
                    String profile = mProfile.mPerfProfile;
                    Log.e(TAG, "getAppPerfProfile: mPackageName=" + mPackageName + ",getProfile=" + profile);
                    if( profile == null ) profile = "default";
                    mAppPerfProfile.setValue(profile);
                    mAppPerfProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mPerfProfile = newValue.toString();
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();

                                //mBaikalService.setAppPerfProfile(mPackageName, newValue.toString() );
                                Log.e(TAG, "mAppPerfProfile: mPackageName=" + mPackageName + ",setProfile=" + newValue.toString());
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppPerfProfile Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }

            mAppThermProfile = (ListPreference) findPreference(APP_PROFILE_THERM);
            if( mAppThermProfile != null ) {
                if(!thermProf) {
                    mAppThermProfile.setVisible(false);
                } else {
                    String profile = mProfile.mThermalProfile;
                    Log.e(TAG, "getAppThermProfile: mPackageName=" + mPackageName + ",getProfile=" + profile);
                    if( profile == null ) profile = "default";
                    mAppThermProfile.setValue(profile);
                    mAppThermProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mThermalProfile = newValue.toString();
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();

                                //mBaikalService.setAppThermProfile(mPackageName, newValue.toString() );
                                Log.e(TAG, "mAppThermProfile: mPackageName=" + mPackageName + ",setProfile=" + newValue.toString());
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppThermProfile Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }

        
            mAppBrightnessProfile = (ListPreference) findPreference(APP_PROFILE_BRIGHTNESS);
            if( mAppBrightnessProfile != null ) {
                int brightness = mProfile.mBrightness;
                Log.e(TAG, "getAppBrightness: mPackageName=" + mPackageName + ", brightness=" + brightness);
                mAppBrightnessProfile.setValue(Integer.toString(brightness));
                mAppBrightnessProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mProfile.mBrightness = val;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();

                        //mBaikalService.setAppBrightness(mPackageName, val );
                        Log.e(TAG, "setAppBrightness: mPackageName=" + mPackageName + ", brightness=" + val);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: setAppBrightness Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppRotationProfile = (ListPreference) findPreference(APP_PROFILE_ROTATION);
            if( mAppRotationProfile != null ) {
                int rotation = mProfile.mRotation;
                Log.e(TAG, "getAppRotation: mPackageName=" + mPackageName + ", rotation=" + rotation);
                mAppRotationProfile.setValue(Integer.toString(rotation));
                mAppRotationProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mProfile.mRotation = val;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();

                        //mBaikalService.setAppBrightness(mPackageName, val );
                        Log.e(TAG, "setAppRotation: mPackageName=" + mPackageName + ", rotation=" + val);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: setAppRotation Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppFpsProfile = (ListPreference) findPreference(APP_PROFILE_FPS);
            if( mAppFpsProfile != null ) {
                if(!variableFps) {
                    mAppFpsProfile.setVisible(false);
                } else {
                    int fps = mProfile.mFrameRate;
                    Log.e(TAG, "setAppFps: mPackageName=" + mPackageName + ",fps=" + fps);
                    mAppFpsProfile.setValue(Integer.toString(fps));
                    mAppFpsProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mFrameRate = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();

                            //mBaikalService.setAppBrightness(mPackageName, val );
                            Log.e(TAG, "setAppFps: mPackageName=" + mPackageName + ",fps=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: setAppFps Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }


            mAppKeepOn = (SwitchPreference) findPreference(APP_PROFILE_KEEP_ON);
            if( mAppKeepOn != null ) {
                mAppKeepOn.setChecked(mProfile.mKeepOn);
                //mAppRestricted.setChecked(mBaikalService.isAppRestrictedProfile(mPackageName));
                Log.e(TAG, "mAppKeepOn: mPackageName=" + mPackageName + ",mKeepOn=" + mProfile.mKeepOn);
                mAppKeepOn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        //int val = Integer.parseInt(newValue.toString());
                        //DiracAudioEnhancerService.du.setHeadsetType(mContext, val);
                        try {
                            mProfile.mKeepOn = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            //mBaikalService.setAppPriority(mPackageName, ((Boolean)newValue) ? -1 : 0 );
                            Log.e(TAG, "mAppKeepOn: mPackageName=" + mPackageName + ",mKeepOn=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppKeepOn Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }


            mAppPinned = (SwitchPreference) findPreference(APP_PROFILE_PINNED);
            if( mAppPinned != null ) {
                mAppPinned.setChecked(mProfile.mPinned);
                Log.e(TAG, "mAppPinned: mPackageName=" + mPackageName + ",mKeepOn=" + mProfile.mPinned);
                mAppPinned.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mPinned = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppPinned: mPackageName=" + mPackageName + ",mPinned=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppPinned Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }


            mAppFreezer = (SwitchPreference) findPreference(APP_PROFILE_FREEZER);

            if( !((new File("/sys/fs/cgroup/freezer/cgroup.freeze")).exists()) ) {
                mAppFreezer.setVisible(false);
                mAppFreezer = null;
            }

            if( mAppFreezer != null ) {

                boolean forceUnfreeze = false;
                if( "com.android.deskclock".equals(mPackageName) ||
                    "com.google.android.deskclock".equals(mPackageName) ) {
                    forceUnfreeze = true;
                    Log.e(TAG, "mAppFreezer: mPackageName=" + mPackageName + ", forceUnfreeze=true");
                }

                if( forceUnfreeze && mProfile.mFreezerMode == 0 ) {
                    mProfile.mFreezerMode = 1;
                    mAppSettings.updateProfile(mProfile);
                    mAppSettings.save();
                }

                mAppFreezer.setChecked(mProfile.mFreezerMode != 0);
                Log.e(TAG, "mFreezerMode: mPackageName=" + mPackageName + ",mFreezerMode=" + mProfile.mFreezerMode);
                mAppFreezer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mFreezerMode = ((Boolean)newValue) ? 1 : 0;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppFreezer: mPackageName=" + mPackageName + ",mAppFreezer=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppFreezer Fatal! exception", re );
                        }
                        return true;
                    }
                });
                if( forceUnfreeze ) {
                    mAppFreezer.setEnabled(false);
                }
            }

            PowerWhitelistBackend mBackend = PowerWhitelistBackend.getInstance(getContext());
            mBackend.refreshList();


            mAppStamina = (SwitchPreference) findPreference(APP_PROFILE_STAMINA);
            if( mAppStamina != null ) {
                if( isStaminaWl() || mBackend.isSysWhitelisted(mPackageName) ) {
                    mAppStamina.setChecked(true);
                    mAppStamina.setEnabled(false);

                    if( !mProfile.mStamina ) {
                        mProfile.mStamina = true;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                    }
                } else {
                    mAppStamina.setChecked(mProfile.mStamina);
                    Log.e(TAG, "mAppStamina: mPackageName=" + mPackageName + ",mStamina=" + mProfile.mStamina);
                    mAppStamina.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mStamina = ((Boolean)newValue);
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                Log.e(TAG, "mAppStamina: mPackageName=" + mPackageName + ",mStamina=" + (Boolean)newValue);
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppStamina Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }
       
            mAppBackgroundProfile = (ListPreference) findPreference(APP_PROFILE_BACKGROUND);
            if( mAppBackgroundProfile != null ) {

                if( mBackend.isSysWhitelisted(mPackageName) ) {
                    Log.e(TAG, "getAppBackground: mPackageName=" + mPackageName + ", forced background=-1");
                    mAppBackgroundProfile.setValue("-1");
                    mAppBackgroundProfile.setEnabled(false);
                    if( mProfile.mBackground != -1 ) {
                        mProfile.mBackground = -1;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                    }
                } else {
                    int background = mProfile.mBackground;
                    Log.e(TAG, "getAppBackground: mPackageName=" + mPackageName + ", background=" + background);
                    mAppBackgroundProfile.setValue(Integer.toString(background));
                    mAppBackgroundProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mBackground = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();

                            //mBaikalService.setAppBrightness(mPackageName, val );
                            Log.e(TAG, "setAppBackground: mPackageName=" + mPackageName + ",background=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: setAppBackground Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }    
            }


            mAppRequireGms = (SwitchPreference) findPreference(APP_PROFILE_REQUIRE_GMS);
            if( mAppRequireGms != null ) {
                boolean requireGms = mProfile.mRequireGms;
                Log.e(TAG, "mAppRequireGms: mPackageName=" + mPackageName + ",requireGms=" + requireGms);
                mAppRequireGms.setChecked(mProfile.mRequireGms);
                mAppRequireGms.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mRequireGms = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();

                        //mBaikalService.setAppBrightness(mPackageName, val );
                        Log.e(TAG, "mAppRequireGms: mPackageName=" + mPackageName + ",requireGms=" + mProfile.mRequireGms);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppRequireGms Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mVolumeScale = (SeekBarPreferenceCham) findPreference(VOLUME_SCALE);
            if( mVolumeScale != null ) {
                int scale = BaikalSettings.getVolumeScaleInt(mUid);
                if( scale > 100 ) scale = 100;
                mVolumeScale.setValue(scale);
                Log.e(TAG, "mVolumeScale: mPackageName=" + mPackageName + ", mVolumeScale=" + BaikalSettings.getVolumeScale(mUid));
                mVolumeScale.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            BaikalSettings.setVolumeScaleInt(mUid,Integer.parseInt(newValue.toString()));
                            Log.e(TAG, "mVolumeScale: mPackageName=" + mPackageName + ", mVolumeScale=" + BaikalSettings.getVolumeScale(mUid));
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mVolumeScale Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }


            mPerformanceScale = (ListPreference) findPreference(APP_PROFILE_PERFORMANCE_SCALE);
            if( mPerformanceScale != null ) {
                int level = mProfile.mPerformanceLevel;
                if( level > 4 ) level = 4;
                if( level < 0 ) level = 0;
                mPerformanceScale.setValue(Integer.toString(level));
                Log.e(TAG, "mPerformanceScale: mPackageName=" + mPackageName + ", mPerformanceScale=" + level);
                mPerformanceScale.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mPerformanceLevel = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mPerformanceScale: mPackageName=" + mPackageName + ", mPerformanceScale=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mPerformanceScale Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }


            mBlockFocusRecv = (SwitchPreference) findPreference(APP_PROFILE_BLOCK_FOCUS_RECV);
            if( mBlockFocusRecv != null ) {
                boolean ignoreAudioFocus = BaikalSettings.getBlockFocusRecv(mUid);
                Log.e(TAG, "mBlockFocusRecv: mPackageName=" + mPackageName + ", mFocus=" + ignoreAudioFocus);
                mBlockFocusRecv.setChecked(ignoreAudioFocus);
                mBlockFocusRecv.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        BaikalSettings.setBlockFocusRecv(mUid,(Boolean)newValue);
                        Log.e(TAG, "mBlockFocusRecv: mPackageName=" + mPackageName + ", mFocus=" + BaikalSettings.getBlockFocusRecv(mUid));
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mBlockFocusRecv Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mBlockFocusSend = (SwitchPreference) findPreference(APP_PROFILE_BLOCK_FOCUS_SEND);
            if( mBlockFocusSend != null ) {
                boolean ignoreAudioFocus = BaikalSettings.getBlockFocusSend(mUid);
                Log.e(TAG, "mBlockFocusSend: mPackageName=" + mPackageName + ", mFocus=" + ignoreAudioFocus);
                mBlockFocusSend.setChecked(ignoreAudioFocus);
                mBlockFocusSend.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        BaikalSettings.setBlockFocusSend(mUid,(Boolean)newValue);
                        Log.e(TAG, "mBlockFocusSend: mPackageName=" + mPackageName + ", mFocus=" + BaikalSettings.getBlockFocusSend(mUid));
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mBlockFocusSend Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mForceSonification = (SwitchPreference) findPreference(APP_PROFILE_FORCE_SONIFICATION);
            if( mForceSonification != null ) {
                boolean forceSonification = BaikalSettings.getForceSonification(mUid);
                Log.e(TAG, "mForceSonification: mPackageName=" + mPackageName + ", mForce=" + forceSonification);
                mForceSonification.setChecked(forceSonification);
                mForceSonification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        BaikalSettings.setForceSonification(mUid,(Boolean)newValue);
                        Log.e(TAG, "mForceSonification: mPackageName=" + mPackageName + ", mForce=" + BaikalSettings.getForceSonification(mUid));
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mForceSonification Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppLocation = (ListPreference) findPreference(APP_PROFILE_LOCATION);
            if( mAppLocation != null ) {
                    int level = mProfile.mLocationLevel;
                    Log.e(TAG, "mAppLocation: mPackageName=" + mPackageName + ",level=" + level);
                    mAppLocation.setValue(Integer.toString(level));
                    mAppLocation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mLocationLevel = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();

                            //mBaikalService.setAppBrightness(mPackageName, val );
                            Log.e(TAG, "mAppLocation: mPackageName=" + mPackageName + ",level=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppLocation Fatal! exception", re );
                        }
                        return true;
                      }
                    });
            }

            mAppCamera = (ListPreference) findPreference(APP_PROFILE_CAMERA);
            if( mAppCamera != null ) {
                    int level = mProfile.mCamera;
                    Log.e(TAG, "mAppCamera: mPackageName=" + mPackageName + ",level=" + level);
                    mAppCamera.setValue(Integer.toString(level));
                    mAppCamera.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mCamera = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();

                            //mBaikalService.setAppBrightness(mPackageName, val );
                            Log.e(TAG, "mAppCamera: mPackageName=" + mPackageName + ",level=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppCamera Fatal! exception", re );
                        }
                        return true;
                      }
                    });
            }

            mAppMicrophone = (ListPreference) findPreference(APP_PROFILE_MICROPHONE);
            if( mAppMicrophone != null ) {
                    int level = mProfile.mMicrophone;
                    Log.e(TAG, "mAppMicrophone: mPackageName=" + mPackageName + ",level=" + level);
                    mAppMicrophone.setValue(Integer.toString(level));
                    mAppMicrophone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mMicrophone = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();

                            //mBaikalService.setAppBrightness(mPackageName, val );
                            Log.e(TAG, "mAppMicrophone: mPackageName=" + mPackageName + ",level=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppMicrophone Fatal! exception", re );
                        }
                        return true;
                      }
                    });
            }




            mAppSpoofProfile = (ListPreference) findPreference(APP_PROFILE_SPOOF);
            if( mAppSpoofProfile != null ) {
                    int spoof = mProfile.mSpoofDevice;
                    Log.e(TAG, "setAppSpoof: mPackageName=" + mPackageName + ",spoof=" + spoof);
                    mAppSpoofProfile.setValue(Integer.toString(spoof));
                    mAppSpoofProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mSpoofDevice = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();

                            //mBaikalService.setAppBrightness(mPackageName, val );
                            Log.e(TAG, "setAppSpoof: mPackageName=" + mPackageName + ",spoof=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: setAppSpoof Fatal! exception", re );
                        }
                        return true;
                      }
                    });
            }


            mAppPhkaProfile = (SwitchPreference) findPreference(APP_PROFILE_PHKA);
            if( mAppPhkaProfile != null ) {
                boolean appPhkaProfile = mProfile.mPreventHwKeyAttestation;
                Log.e(TAG, "mAppPhkaProfile: mPackageName=" + mPackageName + ", appPhkaProfile=" + appPhkaProfile);
                mAppPhkaProfile.setChecked(mProfile.mPreventHwKeyAttestation);
                mAppPhkaProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mPreventHwKeyAttestation = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();

                        //mBaikalService.setAppBrightness(mPackageName, val );
                        Log.e(TAG, "mAppPhkaProfile: mPackageName=" + mPackageName + ", appPhkaProfile=" + mProfile.mPreventHwKeyAttestation);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppPhkaProfile Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppDevModeProfile = (SwitchPreference) findPreference(APP_PROFILE_DEVMODE);
            if( mAppDevModeProfile != null ) {
                boolean appDevModeProfile = mProfile.mHideDevMode;
                Log.e(TAG, "mAppDevModeProfile: mPackageName=" + mPackageName + ", appDevModeProfile=" + appDevModeProfile);
                mAppDevModeProfile.setChecked(mProfile.mHideDevMode);
                mAppDevModeProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mHideDevMode = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();

                        //mBaikalService.setAppBrightness(mPackageName, val );
                        Log.e(TAG, "mAppDevModeProfile: mPackageName=" + mPackageName + ", appDevModeProfile=" + mProfile.mHideDevMode);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppDevModeProfile Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }




       } catch(Exception re) {
           Log.e(TAG, "onCreate: Fatal! exception", re );
       }
       
    }


    private void initBaikalAppOp(String XML_KEY, int baikalOption) {
        try {
            SwitchPreference pref = (SwitchPreference) findPreference(XML_KEY);
            if( pref != null ) { 
                //pref.setChecked(mBaikalService.getAppOption(mPackageName,baikalOption) == 1);
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        //mBaikalService.setAppOption(mPackageName, baikalOption, ((Boolean)newValue) ? 1 : 0 );
                        Log.e(TAG, "setAppOption: mPackageName=" + mPackageName + ",option="+ baikalOption + ", value=" + (Boolean)newValue);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: setAppOption Fatal! exception", re );
                    }
                    return true;
                }
                });
            }
        } catch(Exception re) {
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        /*if( mAppRestore != null ) {
            if( mPackageName == null || !isBackupAvaialable(mPackageName) ) {
                mAppRestore.setEnabled(false);
            } else {
                mAppRestore.setEnabled(true);
            }
        }*/

    }

    /*private boolean isBackupAvaialable(String packageName) {
        String path = "/sdcard/baikalos/backup/" + packageName + ".bba";
        return (new File(path).exists());
    }*/

    private boolean isStaminaWl() {
        if( mUid < Process.FIRST_APPLICATION_UID )  return true;
        if( mPackageName == null ) return true;
        if( mPackageName.startsWith("com.android.service.ims") ) return true;
        if( mPackageName.startsWith("com.android.launcher3") ) return true;
        if( mPackageName.startsWith("com.android.systemui") ) return true;
        if( mPackageName.startsWith("com.android.nfc") ) return true;
        if( mPackageName.startsWith("com.android.providers") ) return true;
        if( mPackageName.startsWith("com.android.inputmethod") ) return true;
        if( mPackageName.startsWith("com.qualcomm.qti.telephonyservice") ) return true;
        if( mPackageName.startsWith("com.android.phone") ) return true;
        if( mPackageName.startsWith("com.android.server.telecom") ) return true;
        if( mPackageName.startsWith("com.android.dialer") ) return true;
        if( mPackageName.startsWith("com.google.android.dialer") ) return true;
        if( mPackageName.startsWith("com.google.android.gsf") ) return true;
        if( mPackageName.startsWith("com.google.android.gms") ) return true;
        if( mPackageName.startsWith("com.google.android.contacts") ) return true;
        if( mPackageName.startsWith("com.google.android.calendar") ) return true;
        if( mPackageName.startsWith("com.google.android.ims") ) return true;
        if( mPackageName.startsWith("com.google.android.ext.shared") ) return true;
        try {
            if( SystemProperties.get("baikal.dialer","").equals(mPackageName) ) return true; 
            if( SystemProperties.get("baikal.sms","").equals(mPackageName) ) return true; 
            if( SystemProperties.get("baikal.call_screening","").equals(mPackageName) ) return true; 
        } catch( Exception e ) {
        }
        return false;
    }
}
