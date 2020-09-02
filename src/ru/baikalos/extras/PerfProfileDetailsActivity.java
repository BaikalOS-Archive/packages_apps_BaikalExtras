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
import ru.baikalos.extras.fragments.PerfProfileFragment;
import android.util.Log;


public class PerfProfileDetailsActivity extends BaseActivity {

    private static final String TAG = "PerfProfile";

    private PerfProfileFragment mPerfProfileFragment;
    private String mProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProfile = getIntent().getStringExtra("profileName");

        Log.e(TAG, "PerfProfile: mProfile=" + mProfile);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        if( mProfile != null ) {
            setTitle(mProfile);
        }

        Fragment fragment = getFragmentManager().findFragmentById(android.R.id.content);
        if (fragment == null) {
            Log.e(TAG, "PerfProfile: new fragment mProfile=" + mProfile);
            mPerfProfileFragment = new PerfProfileFragment(mProfile);
            getFragmentManager().beginTransaction()
                .add(android.R.id.content, mPerfProfileFragment)
                .commit();
        } else {
            Log.e(TAG, "PerfProfile: reusing fragment mProfile=" + mProfile);
            mPerfProfileFragment = (PerfProfileFragment) fragment;
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
