package ru.baikalos.extras.preference;

import android.bluetooth.*;
import android.content.Context;
import androidx.preference.ListPreference;
import android.util.AttributeSet;

import java.util.Set;

public class BluetoothDevicePreference extends ListPreference {

    public BluetoothDevicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();
        CharSequence[] entries = new CharSequence[pairedDevices.size()];
        CharSequence[] entryValues = new CharSequence[pairedDevices.size()];
        int i = 0;
        for (BluetoothDevice dev : pairedDevices) {
            entries[i] = dev.getName();
            if (entries[i] == null) entries[i] = "unknown";
            entryValues[i] = dev.getAddress();
            i++;
        }
        setEntries(entries);
        setEntryValues(entryValues);
    }

    public BluetoothDevicePreference(Context context) {
        this(context, null);
    }

}