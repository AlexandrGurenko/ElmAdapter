package com.a.elmadapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.a.elmadapter.fragments.CheckCarFragment;
import com.a.elmadapter.fragments.DashboardFragment;
import com.a.elmadapter.fragments.HomeFragment;
import com.a.elmadapter.obd.obd.commands.ObdCommandGroup;
import com.a.elmadapter.obd.obd.commands.control.CalibrationIdCommand;
import com.a.elmadapter.obd.obd.commands.control.ModuleVoltageCommand;
import com.a.elmadapter.obd.obd.commands.control.PendingTroubleCodesCommand;
import com.a.elmadapter.obd.obd.commands.control.VinCommand;
import com.a.elmadapter.obd.obd.commands.engine.LoadCommand;
import com.a.elmadapter.obd.obd.commands.engine.OilTempCommand;
import com.a.elmadapter.obd.obd.commands.engine.RPMCommand;
import com.a.elmadapter.obd.obd.commands.engine.RelativeThrottlePositionCommand;
import com.a.elmadapter.obd.obd.commands.engine.SpeedCommand;
import com.a.elmadapter.obd.obd.commands.engine.ThrottlePositionCommand;
import com.a.elmadapter.obd.obd.commands.fuel.FuelLevelCommand;
import com.a.elmadapter.obd.obd.commands.protocol.AvailablePidsCommand01to20;
import com.a.elmadapter.obd.obd.commands.protocol.CloseCommand;
import com.a.elmadapter.obd.obd.commands.protocol.ObdResetCommand;
import com.a.elmadapter.obd.obd.commands.protocol.ResetTroubleCodesCommand;
import com.a.elmadapter.obd.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.a.elmadapter.obd.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.a.elmadapter.obd.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.a.elmadapter.obd.obd.exceptions.NoDataException;
import com.a.elmadapter.obd.obd.exceptions.NonNumericResponseException;
import com.a.elmadapter.obd.obd.exceptions.UnableToConnectException;
import com.a.elmadapter.obd.obd.exceptions.UnknownErrorException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnFragmentListener, DashboardFragment.OnFragmentListener,
        CheckCarFragment.OnFragmentListener {

    private static final Logger rootLogger = Logger.getLogger("");
    private static final Logger log = Logger.getLogger(MainActivity.class.getSimpleName());

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 3;

    BluetoothAdapter bluetoothAdapter;
    static BluetoothDevice bluetoothDevice;
    public static BluetoothSocket bluetoothSocket;
    String deviceAddress;

    UUID uuid;

    ArrayAdapter<String> listViewAdapter;

    ArrayList<String> responses;

    boolean isRunning = false;
    static boolean isConnectedToELM = false;
    static boolean isConnectedToOBD = false;

    @SuppressLint("StaticFieldLeak")
    static HomeFragment homeFragment;
    static DashboardFragment dashboardFragment;
    @SuppressLint("StaticFieldLeak")
    static CheckCarFragment checkCarFragment;
    Toolbar toolbar;
    TextView homeText, dashboardText, checkCarText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clearPref();

        homeText = findViewById(R.id.text_home);
        dashboardText = findViewById(R.id.text_dashboard);
        checkCarText = findViewById(R.id.text_check_car);

        //Navigation
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToBtDevice();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.nav_app_bar_open_drawer_description,
                R.string.nav_app_bar_close_description);
        toggle.syncState();

        homeFragment = new HomeFragment();
        dashboardFragment = new DashboardFragment();
        checkCarFragment = new CheckCarFragment();

        uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        responses = new ArrayList<>();

        listViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                responses);
//        lvElm.setAdapter(listViewAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int REQUEST_EXTERNAL_STORAGE = 1;
            final String[] PERMISSION_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            requestPermissions(PERMISSION_STORAGE, REQUEST_EXTERNAL_STORAGE);

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        changeFragment(homeFragment);

        setupLoggers();
        log.info(getResources().getString(R.string.app_name) + " started. V 1.1.0");
    }

    private void connectToBtDevice() {
        log.info("choiceBtDevice");
        ArrayList<String> deviceStrs = new ArrayList<>();
        final ArrayList<String> devices = new ArrayList<>();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.select_dialog_singlechoice,
                    deviceStrs.toArray(new String[0]));

            alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    deviceAddress = devices.get(position);

                    bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
                    String deviceName = bluetoothDevice.getName();
                    log.info("Device selected: " + deviceName);
                    homeFragment.setText("Device selected: " + deviceName);

                    try {
                        bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
                        bluetoothSocket.connect();
                    } catch (IOException e) {
                        Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                        log.info("Read failed, socket might closed or timeout. Check ELM adapter and try again");
                    }

                    if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                        new ConnectToOBDII(bluetoothSocket).execute();
                    } else {
                        homeFragment.setText("Bluetooth is in use by another application or is not found");
                        disconnect();
                    }
                }
            });

            alertDialog.setTitle("Choose Bluetooth Device");
            alertDialog.show();

        } else {
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        }
    }

    static void resetAdapter() throws IOException, InterruptedException {
        new ObdResetCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
        log.info("ELM reset");
    }

    private void setupLoggers() {
        String logFileName = Environment.getExternalStorageDirectory()
                + File.separator
                + this.getApplicationContext().getPackageName().concat(File.separator).concat("log");
        try {
            new File(logFileName).mkdirs();
            FileHandler logFileHandler = new FileHandler(logFileName.concat("/Test.log.%d.txt"),
                    250 * 1024 * 1024,
                    25,
                    false);

            logFileHandler.setFormatter(new SimpleFormatter() {
                final String format = "%1$tF\t%1$tT.%1$tL\t%4$s\t%3$s\t%5$s%n";

                @SuppressLint("DefaultLocale")
                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(format,
                            new Date(lr.getMillis()),
                            lr.getSourceClassName(),
                            lr.getLoggerName(),
                            lr.getLevel().getName(),
                            lr.getMessage());
                }
            });

            rootLogger.addHandler(logFileHandler);
        } catch (IOException e) {

            log.info(e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_home:
                changeFragment(homeFragment);
                break;
            case R.id.nav_dashboard:
                changeFragment(dashboardFragment);
                break;
            case R.id.nav_checkCar:
                changeFragment(checkCarFragment);
                break;
        }

        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);

        return true;
    }

    private void changeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onChangeToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    static boolean checkProtocol() {
        AvailablePidsCommand01to20 command01to20 = new AvailablePidsCommand01to20();
        try {
            command01to20.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

        } catch (IOException | NoDataException | InterruptedException | NullPointerException | NonNumericResponseException e) {
            e.printStackTrace();
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            log.info(e.getMessage());
            return e.getMessage().contains("4100");
        }

        log.info("111 + + + " + command01to20.getCalculatedResult());
        return !command01to20.getCalculatedResult().isEmpty();
    }

    static void checkConnection() {
        new CheckConnectionState().execute();
    }

    public static String readVin() {
        log.info("Read VIN");

        String response = null;

        VinCommand vinCommand = new VinCommand();

//        if (isConnectedToOBD && protocolName.contains("9141")) {
//            readCalibrationID();
//        }

        if (isConnectedToELM) {
            try {
                vinCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                Log.d(TAG, vinCommand.getFormattedResult());
                if (isConnectedToOBD) {
                    log.info("VIN: " + vinCommand.getFormattedResult());
                    response = "VIN: " + vinCommand.getFormattedResult();
                }

            } catch (IOException | NoDataException | InterruptedException | NullPointerException | UnknownErrorException e) {
                Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                log.info("VIN: " + e.getMessage());
            }
            if (response == null)
                response = readCalibrationID();
        } else {
            Log.d(TAG, "Error connection");
            log.info("Error connection");
        }
        return response == null ? "Failed reed VIN" : response;
    }

    private void readRPM() {
        log.info("Read RPM");
        RPMCommand rpmCommand = new RPMCommand();
        if (isConnectedToELM) {
            try {
                rpmCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                Log.d(TAG, rpmCommand.getFormattedResult());
                if (isConnectedToOBD) {
                    responses.add(0, "\nRPM: " + rpmCommand.getFormattedResult());
                    listViewAdapter.notifyDataSetChanged();
                }
                log.info("RPM: " + rpmCommand.getFormattedResult());
            } catch (IOException | NoDataException | InterruptedException | NullPointerException e) {

                Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                if (isConnectedToOBD) {
                    responses.add(0, "\n" + e.getMessage());
                }
                listViewAdapter.notifyDataSetChanged();
                log.info(e.getMessage());
            }
        }
    }

    private static String readCalibrationID() {
        log.info("Read Calibration ID");
        String response = null;

        CalibrationIdCommand calibrationIdCommand = new CalibrationIdCommand();
        if (isConnectedToELM) {
            try {
                calibrationIdCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

                Log.d(TAG, calibrationIdCommand.getFormattedResult());
                if (isConnectedToOBD) {
                    response = "ID: " + calibrationIdCommand.getFormattedResult();
                }
                log.info("ID: " + calibrationIdCommand.getFormattedResult());
            } catch (IOException | NoDataException | InterruptedException | NullPointerException e) {
                response = e.getMessage();
                Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                log.info(e.getMessage());
            }
        }
        return response != null ? response : "Read Calibration ID failed";
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.readVIN:
                readVin();
                return true;

            case R.id.readDTC:
                log.info("Read DTC");
                PendingTroubleCodesCommand ptcc = new PendingTroubleCodesCommand();
                try {
                    ptcc.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

                    Log.d(TAG, ptcc.getFormattedResult());
                    log.info(ptcc.getFormattedResult());

                } catch (InterruptedException | IOException | NullPointerException | NoDataException e) {

                    Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                    log.info(e.getMessage());

                }
                return true;

            case R.id.clearDTC:
                log.info("Clear DTC");
                final ResetTroubleCodesCommand resetTroubleCodes = new ResetTroubleCodesCommand();
                try {
                    resetTroubleCodes.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    log.info("Fault Codes Cleared - " + resetTroubleCodes.getFormattedResult());

                } catch (InterruptedException | IOException | NullPointerException e) {
                    log.info(e.getMessage());
                }
                return true;

            case R.id.scan:
                log.info("Scan");
                log.info("Starting background thread for scanning");
                new ScanTask().execute();
                return true;

            case R.id.readOilTemp:
                log.info("Oil temp");
                OilTempCommand oilTempCommand = new OilTempCommand();
                if (isConnectedToELM) {
                    try {
                        oilTempCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

                        Log.d(TAG, oilTempCommand.getFormattedResult());
                        log.info(oilTempCommand.getFormattedResult() + " - " + oilTempCommand.getCalculatedResult());
                        responses.add(0, "\n" + oilTempCommand.getFormattedResult());
                        listViewAdapter.notifyDataSetChanged();
                    } catch (InterruptedException | IOException | NoDataException | NullPointerException e) {

                        Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                        responses.add(0, "\n" + e.getMessage());
                        listViewAdapter.notifyDataSetChanged();
                        log.info(e.getMessage());
                    }
                } else {
                    Log.d(TAG, "Error connection");
                    log.info("Error connection");
                }
                return true;

            case R.id.disconnect:
                disconnect();

                return true;

            case R.id.exit:
                disconnect();
                btOff();
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static void disconnect() {
        if (isConnectedToELM) {
            CloseCommand close = new CloseCommand();
            try {
                close.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                bluetoothSocket.close();
                bluetoothSocket = null;
            } catch (IOException | InterruptedException e) {
                log.info(e.getMessage());
            }
            isConnectedToELM = false;
            isConnectedToOBD = false;
            log.info("Disconnected");
        }
    }

    private void btOff() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled())
            bluetoothAdapter.disable();
    }

    private void clearPref() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    protected void onStop() {
        clearPref();
        super.onStop();
    }

    @SuppressLint("StaticFieldLeak")
    private class ScanTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground");

            RPMCommand rpmCommand = new RPMCommand();
            SpeedCommand speedCommand = new SpeedCommand();
            ThrottlePositionCommand throttlePositionCommand = new ThrottlePositionCommand();
            RelativeThrottlePositionCommand relativeThrottlePositionCommand = new RelativeThrottlePositionCommand();
            LoadCommand loadCommand = new LoadCommand();
            EngineCoolantTemperatureCommand coolantCommand = new EngineCoolantTemperatureCommand();
            AirIntakeTemperatureCommand airIntakeTemperatureCommand = new AirIntakeTemperatureCommand();
            AmbientAirTemperatureCommand ambientAirTemperatureCommand = new AmbientAirTemperatureCommand();
            FuelLevelCommand fuelLevelCommand = new FuelLevelCommand();
            ModuleVoltageCommand moduleVoltageCommand = new ModuleVoltageCommand();

            isRunning = true;
            while (isRunning && isConnectedToELM) {
                try {
                    ObdCommandGroup obdCommandGroup = new ObdCommandGroup();
                    obdCommandGroup.add(rpmCommand);
                    obdCommandGroup.add(speedCommand);
                    obdCommandGroup.add(throttlePositionCommand);
                    obdCommandGroup.add(relativeThrottlePositionCommand);
                    obdCommandGroup.add(loadCommand);
                    obdCommandGroup.add(coolantCommand);
                    obdCommandGroup.add(airIntakeTemperatureCommand);
                    obdCommandGroup.add(ambientAirTemperatureCommand);
                    obdCommandGroup.add(fuelLevelCommand);
                    obdCommandGroup.add(moduleVoltageCommand);

                    obdCommandGroup.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

                    String[] answers = new String[10];
                    answers[0] = rpmCommand.getFormattedResult();
                    answers[1] = speedCommand.getFormattedResult();
                    answers[2] = throttlePositionCommand.getFormattedResult() + " - Throttle Position";
                    answers[3] = relativeThrottlePositionCommand.getFormattedResult() + " - Relative Throttle Position";
                    answers[4] = loadCommand.getFormattedResult() + " - Engine Load";
                    answers[5] = coolantCommand.getFormattedResult() + " - Coolant Temp";
                    answers[6] = airIntakeTemperatureCommand.getFormattedResult() + " - Air Intake";
                    answers[7] = ambientAirTemperatureCommand.getFormattedResult() + " - Ambient Air";
                    answers[8] = fuelLevelCommand.getFormattedResult() + " - Fuel Level";
                    answers[9] = moduleVoltageCommand.getFormattedResult();

                    Log.d(TAG, Arrays.toString(answers));
                    log.info(Arrays.toString(answers));

                    publishProgress(answers);

                } catch (InterruptedException | IOException | UnableToConnectException e) {

                    Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                    log.info(e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d(TAG, "onProgressUpdate");
            super.onProgressUpdate(values);

            responses.clear();

            responses.addAll(Arrays.asList(values));

            listViewAdapter.notifyDataSetChanged();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            responses.clear();
            listViewAdapter.notifyDataSetChanged();

        }
    }

    @SuppressLint("StaticFieldLeak")
    private static class CheckConnectionState extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

//            while (checkProtocol()) {
//                SystemClock.sleep(2000);
//                log.info("Keep Alive - OK");
//            }
            while (isConnected(bluetoothDevice)) {
                SystemClock.sleep(5000);
                log.info("Connected");
            }

            return null;
        }

        boolean isConnected(BluetoothDevice device) {
            try {
                Method m = device.getClass().getMethod("isConnected", (Class[]) null);
                boolean conn = (boolean) m.invoke(device, (Object[]) null);
                return conn;
            } catch (Exception e) {
                log.info("+++" + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            disconnect();
        }
    }
}
