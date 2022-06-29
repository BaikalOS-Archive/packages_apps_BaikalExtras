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
import java.util.ArrayList;


public class BackupUtil {

    private static final String TAG = "BaikalExtras";


    public void systemBackup(String backup, boolean forceAll) {
        new CreateBackupTask().execute(backup, forceAll);
    }

    public void systemRestore(String restore) {
        new CreateRestoreTask().execute(restore, true);
    }

    public void appBackup(String backup) {
        new CreateBackupTask().execute(backup);
    }

    public void appRestore(String restore) {
        new CreateRestoreTask().execute(restore);
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


    private class CreateBackupTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.log_it_logs_in_progress));
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();*/
        }

        @Override
        protected Boolean doInBackground(Object... params) {

            String fileName = "baikal_backup";

            ArrayList<String> packages = new ArrayList<String>();

            boolean saveApks = true;
            boolean saveObbs = false;
            boolean saveShared = false;
            boolean doEverything = false;
            boolean doWidgets = false;
            boolean allIncludesSystem = true;
            boolean doCompress = true;
            boolean doKeyValue = false;
            boolean forceAll = false;

            if (params.length < 1 || params.length > 2 ) {
                Log.e(TAG, "CreateBackupTask: invalid argument count");
                return false;
            }


            Log.e(TAG, "CreateBackupTask: " + params[0].toString());

            if( params.length == 2 ) {
                String backup = params[0].toString();
                forceAll = (Boolean)params[1];

                Boolean system = false;
                Boolean obb = false;

                saveApks = true;
                doEverything = true;
                allIncludesSystem = false;
                doKeyValue = true;

                if( backup.equals("sys") ) {
                    system = true;
                    saveApks = false;
                }

                //if( backup.equals("sys_apps") || backup.equals("sys_apps_obb") ) system = true;
                if( backup.equals("apps_obb") || backup.equals("sys_apps_obb") ) obb = true;
                fileName = backup + ".bbp";

                //allIncludesSystem = system;
                saveObbs = obb;
                saveShared = false;

                //if( allIncludesSystem ) { 
                //    doKeyValue = true;
                //    doWidgets = true;
                //}
                
            } else {
                packages.add(params[0].toString());
                fileName = params[0].toString() + ".bba";
                doKeyValue = true;
                saveApks = false;
                saveObbs = false;
                forceAll = true;
            }

            ParcelFileDescriptor pfd;
            try {

                File root = new File(Environment.getExternalStorageDirectory(), "baikalos/backup");
                if (!root.exists()) 
                {
                    root.mkdirs();
                }

                File file = new File(root, fileName);

                pfd = ParcelFileDescriptor.open(file,
                    ParcelFileDescriptor.MODE_CREATE |
                    ParcelFileDescriptor.MODE_TRUNCATE |
                    ParcelFileDescriptor.MODE_WRITE_ONLY
                    );

                SystemProperties.set("sys.baikal.backup_inp", "1");
                SystemProperties.set("sys.baikal.force_backup", forceAll ? "1" : "0");
                IBackupManager backupManager = IBackupManager.Stub.asInterface(ServiceManager.getService("backup"));

                String[] packArray = new String[packages.size()];
                backupManager.adbBackup(UserHandle.USER_SYSTEM, pfd, saveApks, saveObbs, saveShared, doWidgets, doEverything,
                    allIncludesSystem, doCompress, doKeyValue, packages.toArray(packArray));

            } catch (Exception e) {
                Log.e(TAG, "baikalBackup: Failed! ", e );
            }        
            SystemProperties.set("sys.baikal.force_backup", "0");
            Log.e(TAG, "baikalBackup: completed" );
            SystemProperties.set("sys.baikal.backup_inp", "0");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean param) {
            super.onPostExecute(param);
            //progressDialog.dismiss();
        }
    }

    private class CreateRestoreTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.log_it_logs_in_progress));
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();*/
        }

        @Override
        protected Boolean doInBackground(Object... params) {

            String fileName = "baikal_backup";
            Boolean forceAll = true;


            if (params.length < 1 || params.length > 2 ) {
                Log.e(TAG, "CreateRestoreTask: invalid argument count");
                return false;
            }


            Log.e(TAG, "CreateRestoreTask: " + params[0].toString());

            if( params.length == 1 ) {
                fileName = params[0].toString() + ".bba";
            } else {
                fileName = params[0].toString() + ".bbp";
                forceAll = (Boolean)params[1];
            }

            ParcelFileDescriptor pfd;
            try {

                File root = new File(Environment.getExternalStorageDirectory(), "baikalos/backup");
                File file = new File(root, fileName);

                pfd = ParcelFileDescriptor.open(file,
                    ParcelFileDescriptor.MODE_READ_ONLY
                    );

                SystemProperties.set("sys.baikal.restore_inp", "1");
                SystemProperties.set("sys.baikal.force_backup", forceAll ? "1" : "0");
                IBackupManager backupManager = IBackupManager.Stub.asInterface(ServiceManager.getService("backup"));

                backupManager.adbRestore(UserHandle.USER_SYSTEM, pfd);


            } catch (Exception e) {
                Log.e(TAG, "baikalBackup: restore Failed! ", e );
            }        
            SystemProperties.set("sys.baikal.force_backup", "0");
            Log.e(TAG, "baikalBackup: completed" );
            SystemProperties.set("sys.baikal.restore_inp", "0");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean param) {
            super.onPostExecute(param);
            //progressDialog.dismiss();
        }
    }

}
