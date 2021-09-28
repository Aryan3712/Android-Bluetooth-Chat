package com.aryanwalia.connect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FindPeople extends AppCompatActivity {
    private ListView available_devices;
    private ProgressBar progressBar;
    private ArrayAdapter<String> adapter_available_devices;
    private  BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.find_people_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.find_people:
                scanDevices();
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }

    private void scanDevices() {
        progressBar.setVisibility(View.GONE);
        adapter_available_devices.clear();
        Toast.makeText(this,"Scanning",Toast.LENGTH_SHORT).show();
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private void init() {
        available_devices = findViewById(R.id.available_devices);
        progressBar = findViewById(R.id.progress_scan_devices);
        adapter_available_devices = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        available_devices.setAdapter(adapter_available_devices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDeviceListener,intentFilter);
        IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothDeviceListener,intentFilter1);

        available_devices.setOnItemClickListener((parent, view, position, id) -> {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent();
            intent.putExtra("deviceAddress", address);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private BroadcastReceiver bluetoothDeviceListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                    adapter_available_devices.add(device.getName()+"\n"+device.getAddress());
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                progressBar.setVisibility(View.GONE);
                if(adapter_available_devices.getCount() == 0){
                    Toast.makeText(context,"No new devices found",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context,"Click on the device to start the chat",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

}










