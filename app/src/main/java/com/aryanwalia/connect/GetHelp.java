package com.aryanwalia.connect;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

public class GetHelp extends AppCompatActivity {
    private ListView help_list;
    private ArrayAdapter<String> help_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_help);
        help_list = findViewById(R.id.help_list);
        init();
    }

    private void init(){
        String one = "1. Click on the Bluetooth icon after opening the app";
        String two = "2. Click on the earth icon to become a server and let the sender connect to you";
        String three = "3. Click on the chat icon to connect to a server from the list";
        String four = "4. To connect, either find a device from already paired devices list by clicking on the chat icon";
        String five = "5. If you are unable to find the device in the chat list, then click on the add people button and add a new device";
        String six = "6. When the Status is CONNECTED, start messaging and have fun";
        String s = one+"\n"+two+"\n"+three+"\n"+four+"\n"+five+"\n"+six;

        List<String> list = Arrays.asList(s.split("\n"));
        help_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,list);
        help_list.setAdapter(help_adapter);
    }
}