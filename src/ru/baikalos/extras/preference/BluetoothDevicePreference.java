package ru.baikalos.extras.preference;

import android.bluetooth.*;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import android.os.UserHandle;
import android.util.Log;


import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Iterator;



public class BluetoothDevicePreference extends MultiSelectListPreference implements OnPreferenceChangeListener {

    private static final String TAG = "BluetoothDevicePreference";


    Context mContext;

    public BluetoothDevicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        List<BluetoothDevice> connectedLEDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);

        CharSequence[] entries = new CharSequence[pairedDevices.size()+connectedLEDevices.size()];
        CharSequence[] entryValues = new CharSequence[pairedDevices.size()+connectedLEDevices.size()];
        Set<String> checkedValues = new HashSet<String>();

        int i = 0;
        for (BluetoothDevice dev : pairedDevices) {
            entries[i] = dev.getName();
            if (entries[i] == null) entries[i] = "unknown";
            entryValues[i] = dev.getAddress();
            i++;
        }

        for (BluetoothDevice dev : connectedLEDevices) {
            entries[i] = dev.getName();
            if (entries[i] == null) entries[i] = "unknown";
            entryValues[i] = dev.getAddress();
            i++;
        }

        setEntries(entries);
        setEntryValues(entryValues);

        String btDevices = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.BAIKALOS_TRUST_BT_DEV);

        //if( btDevices == null ) return;

        if (btDevices != null && btDevices.length() != 0) {
            String[] parts = btDevices.split("\\|");
            for (int j = 0; j < parts.length; j++) {
                checkedValues.add(parts[j]);
            }
        }

        setValues(checkedValues);

        setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Set<String> checked = (Set<String>) newValue;
        for(String address : checked) {
            Log.e(TAG, "checked: " + address);
        }

        StringBuffer buffer = new StringBuffer();
        Iterator<String> nextItem = checked.iterator();

        while (nextItem.hasNext()) {
            String sTag = nextItem.next();
            buffer.append(sTag + "|");
        }

        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }

        Log.e(TAG, "checked string: " + buffer.toString());
        Settings.Secure.putStringForUser(mContext.getContentResolver(), Settings.Secure.BAIKALOS_TRUST_BT_DEV,
            buffer.toString(), UserHandle.USER_CURRENT);

        return true;
        
    }

    public BluetoothDevicePreference(Context context) {
        this(context, null);
    }

}