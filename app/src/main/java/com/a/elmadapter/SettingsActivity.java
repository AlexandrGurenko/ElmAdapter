package com.a.elmadapter;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.a.elmadapter.obd.obd.commands.ObdCommand;
import com.a.elmadapter.obd.obd.commands.protocol.AvailablePidsCommand01to20;
import com.a.elmadapter.obd.obd.commands.protocol.AvailablePidsCommand21to40;
import com.a.elmadapter.obd.obd.commands.protocol.AvailablePidsCommand41to60;
import com.a.elmadapter.obd.obd.exceptions.NoDataException;
import com.a.elmadapter.obd.obd.exceptions.NonNumericResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final Logger log = Logger.getLogger(TAG);

    BluetoothSocket socket;

    TextView textView;
    ListView listView;
    Button button;

    ArrayList<String> commandNames;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        log.info("Settings Activity started");

        socket = MainActivity.bluetoothSocket;

        textView = findViewById(R.id.tv_settings);
        listView = findViewById(R.id.lv_settings);
        button = findViewById(R.id.button_settings);
        button.setOnClickListener(this);

        commandNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, commandNames);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
    }

    public void getAllSupportPids() {
        ArrayList<Class<? extends ObdCommand>> allClasses = new ArrayList<>();
        AvailablePidsCommand01to20 command01to20 = new AvailablePidsCommand01to20();
        AvailablePidsCommand21to40 command21to40 = new AvailablePidsCommand21to40();
        AvailablePidsCommand41to60 command41to60 = new AvailablePidsCommand41to60();

        try {
            command01to20.run(socket.getInputStream(), socket.getOutputStream());
            Log.d(TAG, command01to20.getFormattedResult());
            allClasses.addAll(command01to20.getListClassResult());
        } catch (IOException | NoDataException | InterruptedException | NullPointerException | NonNumericResponseException e) {
            e.printStackTrace();
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            log.info(e.getMessage());
        }

        try {
            command21to40.run(socket.getInputStream(), socket.getOutputStream());
            Log.d(TAG, command21to40.getFormattedResult());
            allClasses.addAll(command21to40.getListClassResult());
        } catch (IOException | NoDataException | InterruptedException | NullPointerException | NonNumericResponseException e) {
            e.printStackTrace();
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            log.info(e.getMessage());
        }

        try {
            command41to60.run(socket.getInputStream(), socket.getOutputStream());
            Log.d(TAG, command41to60.getFormattedResult());
            allClasses.addAll(command41to60.getListClassResult());
        } catch (IOException | NoDataException | InterruptedException | NullPointerException | NonNumericResponseException e) {
            e.printStackTrace();
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            log.info(e.getMessage());
        }

        for (Class<? extends ObdCommand> c : allClasses) {
            try {
                commandNames.add(c.newInstance().getNameCommand());
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_settings) {
            Log.d(TAG,"click");
            getAllSupportPids();
        }
    }
}
