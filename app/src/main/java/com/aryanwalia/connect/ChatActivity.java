package com.aryanwalia.connect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ChatActivity extends AppCompatActivity {

    private static final int SELECT_DEVICE = 101 ;
    private EditText message_box;
    private Button send_btn;
    private ListView conversation;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> adapterMainChat;
    private String connectedDevice;
    private String connectedAddress;
    public SendReceive sendReceive;
    public static final String DEVICE_NAME = "deviceName";

    public static final int STATE_LISTENING = 1;
    public static final int STATE_CONNECTING= 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTION_FAILED = 4;
    public static final int STATE_MESSAGE_RECEIVED = 5;
    public static final int STATE_MESSAGE_SENT = 6;
    public static final int STATE_MESSAGE_DEVICE_NAME = 7;
    public static final int STATE_MESSAGE_DEVICE_NAME_FOR_SERVER = 8;

    private static final String APP_NAME = "Connect";
    private static final UUID MY_UUID = UUID.fromString("17a48150-3238-457f-9f70-4b437fe18a12");

    Handler handler = new Handler(msg -> {
        switch(msg.what){
            case STATE_LISTENING:
                setState("Listening");
                break;
            case STATE_CONNECTING:
                setState("Connecting...");
                break;
            case STATE_CONNECTED:
                setState("Connected: "+connectedDevice);
                break;
            case STATE_CONNECTION_FAILED:
                setState("Connection Failed");
                break;
            case STATE_MESSAGE_RECEIVED:
                byte[] buffer = (byte[]) msg.obj;
                String inputBuffer = new String(buffer, 0, msg.arg1);
                String decrypted = AES.decrypt(inputBuffer);
                adapterMainChat.add(connectedDevice+": "+"\n"+decrypted);
                break;
            case STATE_MESSAGE_SENT:
                byte[] buffer1 = (byte[]) msg.obj;
                String outputBuffer = new String(buffer1);
                String decrypted_ = AES.decrypt(outputBuffer);
                adapterMainChat.add("You: " +"\n"+decrypted_);
                break;
            case STATE_MESSAGE_DEVICE_NAME:
                connectedDevice = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(this, connectedDevice, Toast.LENGTH_SHORT).show();
                break;
            case STATE_MESSAGE_DEVICE_NAME_FOR_SERVER:
                connectedDevice = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(this, connectedDevice, Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    });

    private void connection() {
        BluetoothDevice device;
        device = bluetoothAdapter.getRemoteDevice(connectedAddress);
        Message message = handler.obtainMessage(STATE_MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        message.setData(bundle);
        handler.sendMessage(message);
    }


    private void setState(CharSequence subTitle) {
        getSupportActionBar().setSubtitle(subTitle);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        message_box = findViewById(R.id.message_box);
        send_btn = findViewById(R.id.send_btn);
        conversation = findViewById(R.id.convers_list);
        adapterMainChat = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        conversation.setAdapter(adapterMainChat);

        send_btn.setOnClickListener(v -> {
            String string = String.valueOf(message_box.getText());
            String encrypted_string = AES.encrypt(string);
            if(!string.isEmpty() && connectedDevice!=null) {
                message_box.setText("");
                sendReceive.write(encrypted_string.getBytes());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_options,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.bluetooth_on_off:
                enableBluetooth();
                return true;
            case R.id.server:
                if(bluetoothAdapter!=null){
                    becomeServer();
                    Toast.makeText(this,"Turning Server On",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Click on the bluetooth icon",Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.list_devices:
                if(bluetoothAdapter!=null)
                    showPairedDevices();
                else
                    Toast.makeText(this,"Click on the bluetooth icon",Toast.LENGTH_SHORT).show();
                return true;

            case R.id.help_section:
                showHelp();
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }

    private void becomeServer() {
        ServerClass serverClass = new ServerClass();
        serverClass.start();
    }

    private void showHelp() {
        Intent intent = new Intent(this,GetHelp.class);
        startActivity(intent);
        Toast.makeText(this,"Showing Help",Toast.LENGTH_SHORT).show();
    }

    private void showPairedDevices() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivityForResult(intent,SELECT_DEVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==SELECT_DEVICE && resultCode==RESULT_OK){
            String address = data.getStringExtra("deviceAddress");
            connectedAddress = address;
            ClientClass clientClass = new ClientClass(bluetoothAdapter.getRemoteDevice(address));
            clientClass.start();
            setState("Connecting...");
            Toast.makeText(this,"Address: "+address,Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void enableBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
            Toast.makeText(this,"Bluetooth already on",Toast.LENGTH_SHORT).show();
        }

        if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent discoveryIntent =new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(discoveryIntent);
        }
    }
    private void connectionServer(BluetoothSocket socket){
        BluetoothDevice device ;
        device = socket.getRemoteDevice();
        Message message = handler.obtainMessage(STATE_MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private class ServerClass extends Thread{
        private BluetoothServerSocket serverSocket;
        public ServerClass(){
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = tmp;
        }

        public void run(){
            BluetoothSocket socket = null;
            while(socket==null){
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if(socket!=null){
                    connectionServer(socket);
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread{
        private BluetoothDevice device;
        private BluetoothSocket socket;
        public ClientClass(BluetoothDevice device1){
            device = device1;
            BluetoothSocket tmp = null;
            try {
                tmp=device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
        }

        public void run(){
            bluetoothAdapter.cancelDiscovery();
            try {
                socket.connect();
                connection();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket){
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {

            }
            inputStream = tempIn;
            outputStream = tempOut;

        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(true){
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                handler.obtainMessage(STATE_MESSAGE_SENT,-1,-1,bytes).sendToTarget();
            } catch (IOException e) {

            }
        }
    }
}