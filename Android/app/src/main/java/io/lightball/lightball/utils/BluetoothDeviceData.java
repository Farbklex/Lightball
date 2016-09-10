package io.lightball.lightball.utils;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Alexander Hoffmann on 10.09.16.
 */
// region Helpers
public class BluetoothDeviceData {
    public BluetoothDevice device;
    public int rssi;
    public byte[] scanRecord;
    public String advertisedName;           // Advertised name

    // Decoded scan record (update R.array.scan_devicetypes if this list is modified)
    public static final int kType_Unknown = 0;
    public static final int kType_Uart = 1;
    public static final int kType_Beacon = 2;
    public static final int kType_UriBeacon = 3;

    public int type;
    public int txPower;
    public ArrayList<UUID> uuids;

    public String getNiceName() {
        String name = device.getName();
        if (name == null) {
            name = advertisedName;      // Try to get a name (but it seems that if device.getName() is null, this is also null)
        }
        if (name == null) {
            name = device.getAddress();
        }

        return name;
    }
}
//endregion
