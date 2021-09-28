package com.aryanwalia.connect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ListView paired_devices;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> adapter_paired_devies;
    private final int LOCATION_PERMISSION_REQUEST = 101;
    private final int SELECT_DEVICE = 102;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paired_devices = findViewById(R.id.paired_devices);

        initBluetooth();
    }

    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            Toast.makeText(this,"No Bluetooth found",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.main_refresh:
                boolean ifBlue = checkBlue();
                if(!ifBlue){
                    Toast.makeText(this,"Turn Bluetooth On",Toast.LENGTH_SHORT).show();
                }else{
                    initList();
                }
                return true;
            case R.id.main_people:
                checkAndFind();
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }

    private void initList() {

        adapter_paired_devies = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        paired_devices.setAdapter(adapter_paired_devies);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> paired_ones = bluetoothAdapter.getBondedDevices();
        if(paired_ones != null && paired_ones.size() > 0){
            for(BluetoothDevice device:paired_ones){
                adapter_paired_devies.add(device.getName()+"\n"+device.getAddress());
            }
        }

        paired_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                Intent intent = new Intent();
                intent.putExtra("deviceAddress", address);
                setResult(RESULT_OK, intent);
                finish();
            }

        });
    }

    private boolean checkBlue() {
        return bluetoothAdapter.isEnabled();
    }

    private void checkAndFind() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST);
        }else{
            Intent intent = new Intent(MainActivity.this,FindPeople.class);
            startActivityForResult(intent,SELECT_DEVICE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==SELECT_DEVICE && resultCode==RESULT_OK){
            String address = data.getStringExtra("deviceAddress");
            Intent intent = new Intent();
            intent.putExtra("deviceAddress",address);
            setResult(RESULT_OK, intent);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_REQUEST){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(this,FindPeople.class);
                startActivityForResult(intent,SELECT_DEVICE);
            }
            else{
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Location Permission is Required\nPlease Grant")
                        .setPositiveButton("Grant", (dialog, which) -> checkAndFind())
                        .setNegativeButton("Deny", (dialog, which) -> MainActivity.this.finish())
                        .show();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}