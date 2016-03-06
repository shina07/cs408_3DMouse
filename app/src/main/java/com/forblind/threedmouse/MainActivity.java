package com.forblind.threedmouse;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by SangHaWoo on 2015-11-15.
 * Select device to connect via bluetooth
 */
public class MainActivity extends AppCompatActivity{


    BluetoothAdapter mBluetoothAdapter;

    ListView listView;
    ArrayAdapter<String> mArrayAdapter;

    ArrayList<BluetoothDevice> devices;

    //temp change for git ignore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listView);
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listView.setAdapter(mArrayAdapter);
        devices = new ArrayList<>();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "this device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "bluetooth is not turned on", Toast.LENGTH_SHORT).show();
            } else {
                getPairedDevices();
            }
        }

        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(mmReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.this.finish();
                BluetoothDevice selectedDevice = devices.get(position);
                Intent intent = new Intent(MainActivity.this, CalibrateActivity.class);
                intent.putExtra("device", selectedDevice);
                mBluetoothAdapter.cancelDiscovery();
                startActivity(intent);
//                Intent intent = new Intent(MainActivity.this, MouseActivity.class);
//                intent.putExtra("device", selectedDevice);
//                mBluetoothAdapter.cancelDiscovery();
//                startActivity(intent);
            }
        });

    }

    private void getPairedDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                devices.add(device);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mmReceiver);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF){
                    // Bluetooth is disconnected, do handling here
                    Toast.makeText(MainActivity.this, "bluetooth is turned off", Toast.LENGTH_SHORT).show();
                } else if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){
                    Toast.makeText(MainActivity.this, "bluetooth is turned on", Toast.LENGTH_SHORT).show();
                    getPairedDevices();
                }
            }
        }
    };

    private final BroadcastReceiver mmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };
}
