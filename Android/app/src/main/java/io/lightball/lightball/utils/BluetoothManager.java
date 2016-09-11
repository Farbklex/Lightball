package io.lightball.lightball.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import io.lightball.lightball.UartInterfaceActivity;
import io.lightball.lightball.ble.BleDevicesScanner;
import io.lightball.lightball.ble.BleManager;
import io.lightball.lightball.ble.BleUtils;

/**
 * @author Anton Weber
 */
public class BluetoothManager implements BleManager.BleManagerListener {

    private final static String TAG = "BluetoothManager";

    Context appContext;

    private BleManager mBleManager;
    private boolean mIsScanPaused = true;
    private BleDevicesScanner mScanner;
    private long mScanCount;
    private ArrayList<BluetoothDeviceData> mScannedDevices;

    public interface OnScanCompleteListener {
        public void onScanComplete(ArrayList<BluetoothDeviceData> res);
    }

    // Singleton
    private static BluetoothManager instance = null;
    private BluetoothManager() { }

    public static synchronized BluetoothManager getInstance() {
        if (instance == null) instance = new BluetoothManager();
            return instance;
    }

    // Must be called right after first getInstance!
    public void init(Context context) {
        appContext = context;
        mBleManager = BleManager.getInstance(appContext);
    }

    public void scan(final OnScanCompleteListener listener) {
        BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(appContext);

        if (BleUtils.getBleStatus(appContext) != BleUtils.STATUS_BLE_ENABLED) {
            Log.w("BluetoothManager", "startScan: BluetoothAdapter not initialized or unspecified address.");
        } else {
            mScanCount = 100;
            mScanner = new BleDevicesScanner(bluetoothAdapter, null, new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    final String deviceName = device.getName();
                    //Log.d(TAG, "Discovered device: " + (deviceName != null ? deviceName : "<unknown>"));

                    BluetoothDeviceData previouslyScannedDeviceData = null;
                    if (mScannedDevices == null)
                        mScannedDevices = new ArrayList<>();       // Safeguard

                    // Check that the device was not previously found
                    for (BluetoothDeviceData deviceData : mScannedDevices) {
                        if (deviceData.device.getAddress().equals(device.getAddress())) {
                            previouslyScannedDeviceData = deviceData;
                            break;
                        }
                    }

                    BluetoothDeviceData deviceData;
                    if (previouslyScannedDeviceData == null) {
                        // Add it to the mScannedDevice list
                        deviceData = new BluetoothDeviceData();
                        mScannedDevices.add(deviceData);
                    } else {
                        deviceData = previouslyScannedDeviceData;
                    }

                    deviceData.device = device;
                    deviceData.rssi = rssi;
                    deviceData.scanRecord = scanRecord;
                    decodeScanRecords(deviceData);

                    if (mScanCount-- == 0) {
                        mScanner.stop();
                        listener.onScanComplete(mScannedDevices);
                    }
                }
            });

            // Start scanning
            mScanner.start();
        }
    }

    public void stopScan() {
        // Stop scanning
        if (mScanner != null) {
            mScanner.stop();
            mScanner = null;
        }
    }

    public ArrayList<BluetoothDeviceData> getScannedDevices() {
        return mScannedDevices;
    }

    private void connect(BluetoothDevice device) {
//        boolean isConnecting = mBleManager.connect(this, device.getAddress());
//        if (isConnecting) {
//            showConnectionStatus(true);
//        }
    }

//    private boolean manageBluetoothAvailability() {
//        boolean isEnabled = true;
//
//        // Check Bluetooth HW status
//        int errorMessageId = 0;
//        final int bleStatus = BleUtils.getBleStatus(getBaseContext());
//        switch (bleStatus) {
//            case BleUtils.STATUS_BLE_NOT_AVAILABLE:
//                errorMessageId = R.string.dialog_error_no_ble;
//                isEnabled = false;
//                break;
//            case BleUtils.STATUS_BLUETOOTH_NOT_AVAILABLE: {
//                errorMessageId = R.string.dialog_error_no_bluetooth;
//                isEnabled = false;      // it was already off
//                break;
//            }
//            case BleUtils.STATUS_BLUETOOTH_DISABLED: {
//                isEnabled = false;      // it was already off
//                // if no enabled, launch settings dialog to enable it (user should always be prompted before automatically enabling bluetooth)
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, kActivityRequestCode_EnableBluetooth);
//                // execution will continue at onActivityResult()
//                break;
//            }
//        }
//        if (errorMessageId > 0) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            AlertDialog dialog = builder.setMessage(errorMessageId)
//                    .setPositiveButton(android.R.string.ok, null)
//                    .show();
//            DialogUtils.keepDialogOnOrientationChanges(dialog);
//        }
//
//        return isEnabled;
//    }


    // Utility / helper methods
    private void decodeScanRecords(BluetoothDeviceData deviceData) {
        // based on http://stackoverflow.com/questions/24003777/read-advertisement-packet-in-android
        final byte[] scanRecord = deviceData.scanRecord;

        ArrayList<UUID> uuids = new ArrayList<>();
        byte[] advertisedData = Arrays.copyOf(scanRecord, scanRecord.length);
        int offset = 0;
        deviceData.type = BluetoothDeviceData.kType_Unknown;

        // Check if is an iBeacon ( 0x02, 0x0x1, a flag byte, 0x1A, 0xFF, manufacturer (2bytes), 0x02, 0x15)
        final boolean isBeacon = advertisedData[0] == 0x02 && advertisedData[1] == 0x01 && advertisedData[3] == 0x1A && advertisedData[4] == (byte) 0xFF && advertisedData[7] == 0x02 && advertisedData[8] == 0x15;

        // Check if is an URIBeacon
        final byte[] kUriBeaconPrefix = {0x03, 0x03, (byte) 0xD8, (byte) 0xFE};
        final boolean isUriBeacon = Arrays.equals(Arrays.copyOf(scanRecord, kUriBeaconPrefix.length), kUriBeaconPrefix) && advertisedData[5] == 0x16 && advertisedData[6] == kUriBeaconPrefix[2] && advertisedData[7] == kUriBeaconPrefix[3];

        if (isBeacon) {
            deviceData.type = BluetoothDeviceData.kType_Beacon;

            // Read uuid
            offset = 9;
            UUID uuid = BleUtils.getUuidFromByteArrayBigEndian(Arrays.copyOfRange(scanRecord, offset, offset + 16));
            uuids.add(uuid);
            offset += 16;

            // Skip major minor
            offset += 2 * 2;   // major, minor

            // Read txpower
            final int txPower = advertisedData[offset++];
            deviceData.txPower = txPower;
        } else if (isUriBeacon) {
            deviceData.type = BluetoothDeviceData.kType_UriBeacon;

            // Read txpower
            final int txPower = advertisedData[9];
            deviceData.txPower = txPower;
        } else {
            // Read standard advertising packet
            while (offset < advertisedData.length - 2) {
                // Length
                int len = advertisedData[offset++];
                if (len == 0) break;

                // Type
                int type = advertisedData[offset++];
                if (type == 0) break;

                // Data
//            Log.d(TAG, "record -> lenght: " + length + " type:" + type + " data" + data);

                switch (type) {
                    case 0x02:          // Partial list of 16-bit UUIDs
                    case 0x03: {        // Complete list of 16-bit UUIDs
                        while (len > 1) {
                            int uuid16 = advertisedData[offset++] & 0xFF;
                            uuid16 |= (advertisedData[offset++] << 8);
                            len -= 2;
                            uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                        }
                        break;
                    }

                    case 0x06:          // Partial list of 128-bit UUIDs
                    case 0x07: {        // Complete list of 128-bit UUIDs
                        while (len >= 16) {
                            try {
                                // Wrap the advertised bits and order them.
                                UUID uuid = BleUtils.getUuidFromByteArraLittleEndian(Arrays.copyOfRange(advertisedData, offset, offset + 16));
                                uuids.add(uuid);

                            } catch (IndexOutOfBoundsException e) {
                                Log.e(TAG, "BlueToothDeviceFilter.parseUUID: " + e.toString());
                            } finally {
                                // Move the offset to read the next uuid.
                                offset += 16;
                                len -= 16;
                            }
                        }
                        break;
                    }

                    case 0x09: {
                        byte[] nameBytes = new byte[len - 1];
                        for (int i=0; i<len-1; i++) {
                            nameBytes[i] = advertisedData[offset++];
                        }

                        String name = null;
                        try {
                            name = new String(nameBytes, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        deviceData.advertisedName = name;
                        break;
                    }

                    case 0x0A: {        // TX Power
                        final int txPower = advertisedData[offset++];
                        deviceData.txPower = txPower;
                        break;
                    }

                    default: {
                        offset += (len - 1);
                        break;
                    }
                }
            }

            // Check if Uart is contained in the uuids
            boolean isUart = false;
            for (UUID uuid : uuids) {
                if (uuid.toString().equalsIgnoreCase(UartInterfaceActivity.UUID_SERVICE)) {
                    isUart = true;
                    break;
                }
            }
            if (isUart) {
                deviceData.type = BluetoothDeviceData.kType_Uart;
            }
        }

        deviceData.uuids = uuids;
    }

    // Callbacks
    @Override
    public void onConnected() {

    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onServicesDiscovered() {

    }

    @Override
    public void onDataAvailable(BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void onReadRemoteRssi(int rssi) {

    }
}
