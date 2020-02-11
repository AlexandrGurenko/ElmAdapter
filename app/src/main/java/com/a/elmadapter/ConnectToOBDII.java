package com.a.elmadapter;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import com.a.elmadapter.obd.obd.commands.ObdCommandGroup;
import com.a.elmadapter.obd.obd.commands.protocol.DescribeProtocolNumberCommand;
import com.a.elmadapter.obd.obd.commands.protocol.EchoOffCommand;
import com.a.elmadapter.obd.obd.commands.protocol.LineFeedOffCommand;
import com.a.elmadapter.obd.obd.commands.protocol.SelectProtocolCommand;
import com.a.elmadapter.obd.obd.commands.protocol.TimeoutCommand;
import com.a.elmadapter.obd.obd.enums.ObdProtocols;
import com.a.elmadapter.obd.obd.exceptions.StoppedException;
import com.a.elmadapter.obd.obd.exceptions.UnknownErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

import static com.a.elmadapter.MainActivity.checkConnection;
import static com.a.elmadapter.MainActivity.checkProtocol;
import static com.a.elmadapter.MainActivity.readVin;
import static com.a.elmadapter.MainActivity.resetAdapter;

public class ConnectToOBDII extends AsyncTask<Void, Void, String> {

    private static final Logger log = Logger.getLogger(MainActivity.class.getSimpleName());

    private static final String TAG = MainActivity.class.getSimpleName();

    private BluetoothSocket socket;

    public ConnectToOBDII(BluetoothSocket socket) {
        this.socket = socket;
    }

    @Override
    protected String doInBackground(Void... voids) {

        StringBuilder response = new StringBuilder();

        try {
            resetAdapter();
        } catch (IOException | InterruptedException | StoppedException e) {
            log.info(e.getMessage());
        }

        MainActivity.isConnectedToELM = true;

        DescribeProtocolNumberCommand describeProtocolCommand = new DescribeProtocolNumberCommand();
        EchoOffCommand echoOffCommand = new EchoOffCommand();
        LineFeedOffCommand lineFeedOffCommand = new LineFeedOffCommand();
        TimeoutCommand timeoutCommand = new TimeoutCommand(125);
        SelectProtocolCommand selectProtocolCommand;
        ArrayList<ObdProtocols> protocols = new ArrayList<>();

        protocols.add(ObdProtocols.ISO_15765_4_CAN);
        protocols.add(ObdProtocols.ISO_15765_4_CAN_B);
        protocols.add(ObdProtocols.ISO_15765_4_CAN_C);
        protocols.add(ObdProtocols.ISO_15765_4_CAN_D);

        protocols.add(ObdProtocols.ISO_9141_2);
        protocols.add(ObdProtocols.ISO_14230_4_KWP);
        protocols.add(ObdProtocols.ISO_14230_4_KWP_FAST);

        protocols.add(ObdProtocols.SAE_J1850_PWM);
        protocols.add(ObdProtocols.SAE_J1850_VPW);
        protocols.add(ObdProtocols.SAE_J1939_CAN);

        boolean protocolSelected = false;

        try {

            ObdCommandGroup obdCommandGroup = new ObdCommandGroup();
            obdCommandGroup.add(echoOffCommand);
            obdCommandGroup.add(lineFeedOffCommand);
            obdCommandGroup.add(timeoutCommand);

            obdCommandGroup.run(socket.getInputStream(), socket.getOutputStream());

            int tryCounter = 0;
            for (int i = 0; i < protocols.size(); i++) {
                selectProtocolCommand = new SelectProtocolCommand(protocols.get(i));
                selectProtocolCommand.run(socket.getInputStream(), socket.getOutputStream());
                describeProtocolCommand.run(socket.getInputStream(), socket.getOutputStream());
                String protocolName = describeProtocolCommand.getCalculatedResult();
                Log.d(TAG, "Protocol: " + protocolName + " - " + selectProtocolCommand.getFormattedResult());

                try {
                    if (checkProtocol()) {
                        protocolSelected = true;
                        break;
                    }
                } catch (UnknownErrorException e) {
                    Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                    if (tryCounter > 2) {
                        response.append("Connection failed. Try turning the ignition off and on and restarting the application");
                        break;
                    }
                    if (i == protocols.size() - 1) {
                        tryCounter++;
                        i = 0;
                    }
                }
            }

            describeProtocolCommand.run(socket.getInputStream(), socket.getOutputStream());

        } catch (IOException | InterruptedException | StoppedException e) {

            log.info("Alert Dialog -> onClick: " + e.getMessage());
        }

        String protocolName;

        if (protocolSelected) {

            log.info("Protocol " + describeProtocolCommand.getCalculatedResult());

            protocolName = describeProtocolCommand.getCalculatedResult();

            MainActivity.isConnectedToOBD = true;

            checkConnection();

            response.append("Protocol selected: ").append(protocolName);
            response.append("\n").append(readVin());

        } else {
            log.info("Connecting Device: fail. Please unplug and reconnect device");
        }

        return response.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        MainActivity.homeFragment.setText(s);
    }
}
