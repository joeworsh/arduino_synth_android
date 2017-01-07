/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.worsham.arduinosynth;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity
{
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    /**
     * Called when the activity is first opened. Setup the bluetooth
     * scanning activity.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        mHandler = new Handler();
    }

    /**
     * When the requested permissions are granted, establish the bluetooth connection.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && permissions.length == grantResults.length)
        {
            initBluetooth();
            scanBluetooth();
        }
    }

    /**
     * Initialize the bluetooth connections.
     */
    private void initBluetooth()
    {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    /**
     * Scan for bluetooth devices.
     */
    private void scanBluetooth()
    {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    /**
     * Called to create the menu when selected.
     * @param menu the menu to created
     * @return true if created successfully
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    /**
     * Called when a menu item is selected from the menu. The
     * menu is used to support scanning of bluetooth devices.
     * @param item the menu item that was selected.
     * @return true if selected successfully
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    /**
     * Called when the application is returned to after being paused or started for the first time?.
     */
    @Override
    protected void onResume()
    {
        super.onResume();
    }

    /**
     * Called when requested activities have finished and returned a result.
     * This activity is interested in results from REQUEST_ENABLE_BT activity.
     * @param requestCode of the requested activity
     * @param resultCode of the requested activity
     * @param data returned from the requested activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Stop scanning bluetooth devices when the user leaves this app.
     * Note this is called when requested permissions!
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        scanLeDevice(false);
        if (mLeDeviceListAdapter != null)
            mLeDeviceListAdapter.clear();
    }

    /**
     * When the user clicks on a bluetooth device in the list, open up the next activity
     * to interact with this device.
     * @param l the list view
     * @param v the view
     * @param position the position of the item which has been clicked
     * @param id the id of the clicked item
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        // extract the selected bluetooth device
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position).device;
        if (device == null)
            return;

        // request a new activity with this bluetooth device
        final Intent intent = new Intent(this, SynthActivity.class);
        intent.putExtra(SynthActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(SynthActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (mScanning)
        {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(leScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }

    /**
     * Used to start and stop bluetooth BLE device scanning from within the app.
     * @param enable
     */
    private void scanLeDevice(final boolean enable)
    {
        if (mBluetoothAdapter == null)
            return;

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(leScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.getBluetoothLeScanner().startScan(leScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(leScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter
    {
        private ArrayList<BluetoothDeviceHolder> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter()
        {
            super();
            mLeDevices = new ArrayList<BluetoothDeviceHolder>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDeviceHolder device)
        {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDeviceHolder getDevice(int position) {
            return mLeDevices.get(position);
        }
        public void clear() {
            mLeDevices.clear();
        }
        @Override
        public int getCount() {
            return mLeDevices.size();
        }
        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.activity_device_scan, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice device = mLeDevices.get(i).device;
            ScanRecord scanRecord = mLeDevices.get(i).scanRecord;
            final String deviceName = scanRecord.getDeviceName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());
            return view;
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            registerFoundDevice(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for(ScanResult result : results)
            {
                registerFoundDevice(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        private void registerFoundDevice(final ScanResult result)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BluetoothDeviceHolder holder = new BluetoothDeviceHolder();
                    holder.device = result.getDevice();
                    holder.scanRecord = result.getScanRecord();
                    mLeDeviceListAdapter.addDevice(holder);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
    static class BluetoothDeviceHolder {
        BluetoothDevice device;
        ScanRecord scanRecord;
    }
}