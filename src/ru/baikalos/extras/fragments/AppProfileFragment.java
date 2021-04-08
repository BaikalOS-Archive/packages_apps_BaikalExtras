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
import com.android.internal.baikalos.BaikalSettings;

import ru.baikalos.extras.BaseSettingsFragment;
import ru.baikalos.extras.R;
import com.aicp.gear.preference.SeekBarPreferenceCham;

public class AppProfileFragment extends BaseSettingsFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "ApplicationProfile";

    private static final String APP_PROFILE_READER = "app_profile_reader";
    private static final String APP_PROFILE_DISABLE_BOOT = "app_profile_disable_boot";
    private static final String APP_PROFILE_PERF = "app_profile_performance";
    private static final String APP_PROFILE_THERM = "app_profile_thermal";
    private static final String APP_PROFILE_BRIGHTNESS = "app_profile_brightness";
    private static final String APP_PROFILE_FPS = "app_profile_fps";
//    private static final String APP_PROFILE_CAMERA_HAL1 = "app_profile_camera_hal1";
    private static final String APP_PROFILE_PINNED = "app_profile_pinned";
    private static final String APP_PROFILE_STAMINA = "app_profile_stamina";
    private static final String APP_PROFILE_REQUIRE_GMS = "app_profile_require_gms";
//    private static final String APP_PROFILE_RESTRICTED = "app_profile_restricted";
    private static final String APP_PROFILE_BACKGROUND = "app_profile_background";
//    private static final String APP_PROFILE_DISABLE_TWL = "app_profile_disable_twl";
    private static final String VOLUME_SCALE = "app_profile_volume_scale";

    private static final String APP_PROFILE_BLOCK_FOCUS_RECV = "app_profile_block_focus_recv";
    private static final String APP_PROFILE_BLOCK_FOCUS_SEND = "app_profile_block_focus_send";


    private String mPackageName;
    private int mUid;
    private Context mContext;

    private SwitchPreference mAppReader;
    private SwitchPreference mAppPinned;
    private SwitchPreference mAppStamina;
    private SwitchPreference mAppRequireGms;
    private SwitchPreference mAppDisableBoot;
    private SwitchPreference mBlockFocusRecv;
    private SwitchPreference mBlockFocusSend;

    //private SwitchPreference mAppRestricted;

    private ListPreference mAppPerfProfile;
    private ListPreference mAppThermProfile;
    private ListPreference mAppBrightnessProfile;
    private ListPreference mAppFpsProfile;
    private ListPreference mAppBackgroundProfile;

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

        boolean readerMode  = SystemProperties.get("sys.baikal.reader", "1").equals("1");
        boolean variableFps  = SystemProperties.get("sys.baikal.var_fps", "1").equals("1");

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
                Log.e(TAG, "getAppBrightness: mPackageName=" + mPackageName + ",brightness=" + brightness);
                mAppBrightnessProfile.setValue(Integer.toString(brightness));
                mAppBrightnessProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mProfile.mBrightness = val;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();

                        //mBaikalService.setAppBrightness(mPackageName, val );
                        Log.e(TAG, "setAppBrightness: mPackageName=" + mPackageName + ",brightness=" + val);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: setAppBrightness Fatal! exception", re );
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

            mAppPinned = (SwitchPreference) findPreference(APP_PROFILE_PINNED);
            if( mAppPinned != null ) {
                mAppPinned.setChecked(mProfile.mPinned);
                //mAppRestricted.setChecked(mBaikalService.isAppRestrictedProfile(mPackageName));
                mAppPinned.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        //int val = Integer.parseInt(newValue.toString());
                        //DiracAudioEnhancerService.du.setHeadsetType(mContext, val);
                        try {
                            mProfile.mPinned = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            //mBaikalService.setAppPriority(mPackageName, ((Boolean)newValue) ? -1 : 0 );
                            Log.e(TAG, "mAppPinned: mPackageName=" + mPackageName + ",mPinned=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppPinned Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mAppStamina = (SwitchPreference) findPreference(APP_PROFILE_STAMINA);
            if( mAppStamina != null ) {
                mAppStamina.setChecked(mProfile.mStamina);
                //mAppRestricted.setChecked(mBaikalService.isAppRestrictedProfile(mPackageName));
                mAppStamina.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        //int val = Integer.parseInt(newValue.toString());
                        //DiracAudioEnhancerService.du.setHeadsetType(mContext, val);
                        try {
                            mProfile.mStamina = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            //mBaikalService.setAppPriority(mPackageName, ((Boolean)newValue) ? -1 : 0 );
                            Log.e(TAG, "mAppStamina: mPackageName=" + mPackageName + ",mStamina=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppStamina Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            /*mAppRestricted = (SwitchPreference) findPreference(APP_PROFILE_RESTRICTED);
            if( mAppRestricted != null ) {
                mAppRestricted.setChecked(mProfile.mRestricted);
                //mAppRestricted.setChecked(mBaikalService.isAppRestrictedProfile(mPackageName));
                mAppRestricted.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        //int val = Integer.parseInt(newValue.toString());
                        //DiracAudioEnhancerService.du.setHeadsetType(mContext, val);
                        try {
                            mProfile.mRestricted = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            //mBaikalService.setAppPriority(mPackageName, ((Boolean)newValue) ? -1 : 0 );
                            Log.e(TAG, "mAppRestricted: mPackageName=" + mPackageName + ",mRestricted=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppRestricted Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }*/
        

            mAppBackgroundProfile = (ListPreference) findPreference(APP_PROFILE_BACKGROUND);
            if( mAppBackgroundProfile != null ) {
                int background = mProfile.mBackground;
                Log.e(TAG, "getAppBackground: mPackageName=" + mPackageName + ",background=" + background);
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
                Log.e(TAG, "mBlockFocusRecv: mPackageName=" + mPackageName + ", mFocus=" + ignoreAudioFocus);
                mBlockFocusSend.setChecked(ignoreAudioFocus);
                mBlockFocusSend.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        BaikalSettings.setBlockFocusSend(mUid,(Boolean)newValue);
                        Log.e(TAG, "mBlockFocusRecv: mPackageName=" + mPackageName + ", mFocus=" + BaikalSettings.getBlockFocusSend(mUid));
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mBlockFocusRecv Fatal! exception", re );
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
    }
}
