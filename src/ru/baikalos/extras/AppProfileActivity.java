/*
* Copyright (C) 2017 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package ru.baikalos.extras;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import ru.baikalos.extras.fragments.AppProfileFragment;
import android.util.Log;


public class AppProfileActivity extends BaseActivity {

    private static final String TAG = "ApplicationProfile";

    private AppProfileFragment mAppProfileFragment;
    private String mPackageName, mAppName;
    private int mAppUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            mPackageName = getIntent().getStringExtra("packageName");
            mAppName = getIntent().getStringExtra("appName");
            mAppUid = getIntent().getIntExtra("appUid",0);

        Log.e(TAG, "AppProfileActivity: mPackageName=" + mPackageName + ", mAppName=" + mAppName + ", mAppUid=" + mAppUid);


        getActionBar().setDisplayHomeAsUpEnabled(true);
        if( mAppName != null ) {
            setTitle(mAppName);
        }

        Fragment fragment = getFragmentManager().findFragmentById(android.R.id.content);
        if (fragment == null) {
            Log.e(TAG, "AppProfileActivity: new fragment mPackageName=" + mPackageName + ", mAppName=" + mAppName + ", mAppUid=" + mAppUid);
            mAppProfileFragment = new AppProfileFragment(mPackageName,mAppUid);
            getFragmentManager().beginTransaction()
                .add(android.R.id.content, mAppProfileFragment)
                .commit();
        } else {
        Log.e(TAG, "AppProfileActivity: reusing fragment mPackageName=" + mPackageName + ", mAppName=" + mAppName + ", mAppUid=" + mAppUid);
            mAppProfileFragment = (AppProfileFragment) fragment;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
 
}
