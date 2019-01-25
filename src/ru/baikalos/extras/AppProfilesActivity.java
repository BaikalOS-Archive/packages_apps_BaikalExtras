package ru.baikalos.extras;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;

import ru.baikalos.extras.AppChooserAdapter.AppItem;

public class AppProfilesActivity extends AppListActivityBase {

    private static final String TAG="baikalappsetting";
    @Override
    public void onListViewItemClick(AppItem info, int id) {
        //addApp(info.packageName);
        Log.e(TAG,"Click on " + info.packageName);
        Intent intent = new Intent(this, AppProfileActivity.class);
        intent.putExtra("packageName",info.packageName);
        intent.putExtra("appName",info.title);
        intent.putExtra("appUid",info.uid);
        startActivity(intent);
    }
}

