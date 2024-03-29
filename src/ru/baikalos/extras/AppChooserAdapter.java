package ru.baikalos.extras;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import ru.baikalos.extras.R;

import com.android.internal.baikalos.AppProfileSettings;
import com.android.internal.baikalos.AppProfile;

import android.content.pm.ApplicationInfo;

public abstract class AppChooserAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = "AppChooserAdapter";

    final Context mContext;
    final Handler mHandler;
    final PackageManager mPackageManager;
    final LayoutInflater mLayoutInflater;

    final AppProfileSettings mAppSettings;

    protected List<PackageInfo> mInstalledAppInfo;
    protected List<AppItem> mInstalledApps = new LinkedList<AppItem>();
    protected List<PackageInfo> mTemporarylist;

    boolean isUpdating;
    boolean hasLauncherFilter = false;
    boolean onlyChanged = false;
    boolean includeSystem = false;
    boolean includeWL = false;

    public AppChooserAdapter(Context context) {
        mContext = context;
        mHandler = new Handler();
        mAppSettings = AppProfileSettings.getInstance(mHandler,mContext, mContext.getContentResolver(),null);

        mPackageManager = mContext.getPackageManager();
        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInstalledAppInfo = mPackageManager.getInstalledPackages(/*PackageManager.GET_PERMISSIONS*/0);
        mTemporarylist = mInstalledAppInfo;
    }

    public synchronized void update() {
        onStartUpdate();

        new Thread(new Runnable() {
            @Override
            public void run() {
                isUpdating = true;

                if( mInstalledAppInfo == null ) { 
                    Log.i(TAG, "add start: loading packages");
                    mInstalledAppInfo = mPackageManager.getInstalledPackages(/*PackageManager.GET_PERMISSIONS*/0);
                    mTemporarylist = mInstalledAppInfo;
                    Log.i(TAG, "add end: loading packages");
                }

                final List<AppItem> temp = new LinkedList<AppItem>();
                for (PackageInfo info : mTemporarylist) {

                    Log.i(TAG, "add start: mPackageName=" + info.packageName);

                    final AppItem item = new AppItem();
                    item.appInfo = info.applicationInfo;
                    //item.title = info.applicationInfo.loadLabel(mPackageManager);
                    //item.icon = info.applicationInfo.loadIcon(mPackageManager);
                    item.packageName = info.packageName;
                    item.uid = info.applicationInfo.uid;
                    

                    if( !includeSystem &&
                        (info.applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0 ) continue;

                    AppProfile mProfile = mAppSettings.getProfile(info.packageName);

                    Log.i(TAG, "add profile: mPackageName=" + info.packageName);

                    if( onlyChanged ) {
                        if( mProfile == null ) continue;
                    }

                    if( includeWL ) {
                        if( mProfile == null ) continue;
                        if( mProfile.mBackground >= 0 ) continue;
                    }

                    final int index = Collections.binarySearch(temp, item);
                    //final boolean isLauncherApp =
                    //        mPackageManager.getLaunchIntentForPackage(info.packageName) != null;

                    Log.i(TAG, "add index: mPackageName=" + info.packageName);

                    //if (!hasLauncherFilter || isLauncherApp) {
                        if (index < 0) {
                            temp.add((-index - 1), item);
                        } else {
                            temp.add((index + 1), item);
                        }
                    //}

                    Log.i(TAG, "add end: mPackageName=" + info.packageName);

                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mInstalledApps = temp;
                        notifyDataSetChanged();
                        isUpdating = false;
                        onFinishUpdate();
                    }
                });
            }
        }).start();
    }

    public abstract void onStartUpdate();

    public abstract void onFinishUpdate();

    @Override
    public int getCount() {
        return mInstalledApps.size();
    }

    @Override
    public AppItem getItem(int position) {
        if( mInstalledApps.size() == 0 ) return null;
        if (mInstalledApps.size() > 0 && position >= mInstalledApps.size()) {
            return mInstalledApps.get(mInstalledApps.size()-1);
        } else if (position < 0) {
            return mInstalledApps.get(0);
        }

        return mInstalledApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mInstalledApps.size()) {
            return -1;
        }

        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mLayoutInflater.inflate(R.layout.view_app_list, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(android.R.id.title);
            holder.icon = (ImageView) convertView.findViewById(android.R.id.icon);
            holder.pkg = (TextView) convertView.findViewById(android.R.id.message);
            convertView.setTag(holder);
        }
        AppItem appInfo = getItem(position);
        appInfo.loadIfNeeded();

        holder.pkg.setText(appInfo.packageName);
        if( appInfo.title != null ) holder.name.setText(appInfo.title);
        if( appInfo.icon != null ) holder.icon.setImageDrawable(appInfo.icon);
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (TextUtils.isEmpty(constraint)) {
                    // No filter implemented we return all the list
                    mTemporarylist = mInstalledAppInfo;
                    return new FilterResults();
                }

                ArrayList<PackageInfo> FilteredList = new ArrayList<PackageInfo>();
                for (PackageInfo data : mInstalledAppInfo) {
                    final String filterText = constraint.toString().toLowerCase(Locale.ENGLISH);
                    try {
                        if (data.applicationInfo.loadLabel(mPackageManager).toString()
                                .toLowerCase(Locale.ENGLISH).contains(filterText)) {
                            FilteredList.add(data);
                        } else if (data.packageName.contains(filterText)) {
                            FilteredList.add(data);
                        }
                    } catch (Exception e) {
                    }
                }
                mTemporarylist = FilteredList;
                return new FilterResults();
            }
        };
    }

    public class AppItem implements Comparable<AppItem> {
        public CharSequence title;
        public String packageName;
        public Drawable icon;
        public int uid;
        public ApplicationInfo appInfo;


        public void loadIfNeeded() {
            if( appInfo == null ) return;
            if( title == null ) { 
                Log.i(TAG, "loadIfNeeded title start: mPackageName=" + packageName);
                title = appInfo.loadLabel(mPackageManager);
                Log.i(TAG, "loadIfNeeded title end: mPackageName=" + packageName);
            }
            if( icon == null ) {
                Log.i(TAG, "loadIfNeeded icon start: mPackageName=" + packageName);
                icon = appInfo.loadIcon(mPackageManager);
                Log.i(TAG, "loadIfNeeded icon end: mPackageName=" + packageName);
            }
        }

        @Override
        public int compareTo(AppItem another) {
            if( this.title == null ) this.title = this.appInfo.loadLabel(mPackageManager);
            if( another.title == null ) another.title = another.appInfo.loadLabel(mPackageManager);
            return this.title.toString().compareTo(another.title.toString());
        }
    }

    static class ViewHolder {
        TextView name;
        ImageView icon;
        TextView pkg;
    }

    protected void setLauncherFilter(boolean enabled) {
        hasLauncherFilter = enabled;
    }

    protected void filterOnlyChanged(boolean isChecked) {
        onlyChanged = isChecked;
    }                  

    protected void filterIncludeSystem(boolean isChecked) {
        includeSystem = isChecked;
    }                  

    protected void filterIncludeWL(boolean isChecked) {
        includeWL = isChecked;
    }                  

}
