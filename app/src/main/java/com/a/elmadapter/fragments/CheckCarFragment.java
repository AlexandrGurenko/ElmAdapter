package com.a.elmadapter.fragments;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.a.elmadapter.MainActivity;
import com.a.elmadapter.R;
import com.a.elmadapter.obd.obd.commands.control.PendingTroubleCodesCommand;
import com.a.elmadapter.obd.obd.commands.protocol.ResetTroubleCodesCommand;
import com.a.elmadapter.obd.obd.exceptions.NoDataException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.logging.Logger;

public class CheckCarFragment extends Fragment implements View.OnClickListener {

    private static final Logger log = Logger.getLogger(CheckCarFragment.class.getSimpleName());

    private static final String TAG = CheckCarFragment.class.getSimpleName();
    private static final String SAVED_TEXT = "saved_text_check_car";

    private OnFragmentListener listener;

    private SharedPreferences sharedPreferences;

    private BluetoothSocket socket;

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> troubleCodes;

    public CheckCarFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        socket = MainActivity.bluetoothSocket;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final View rootView = inflater.inflate(R.layout.fragment_check_car, container, false);

        troubleCodes = new ArrayList<>();
        listView = rootView.findViewById(R.id.lv_check_car);
        adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.custom_list_item, troubleCodes);
        listView.setAdapter(adapter);

        Button buttonRead = rootView.findViewById(R.id.button_check_read_dtc);
        buttonRead.setOnClickListener(this);
        Button buttonClear = rootView.findViewById(R.id.button_check_clear_dtc);
        buttonClear.setOnClickListener(this);

        sharedPreferences = Objects.requireNonNull(this.getActivity()).getPreferences(Context.MODE_PRIVATE);
        loadDtcList();

        if (savedInstanceState != null) {
            setDtcList(savedInstanceState.getStringArrayList(SAVED_TEXT));
        }

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        if (context instanceof DashboardFragment.OnFragmentListener) {
            listener = (OnFragmentListener) context;
            listener.onChangeToolbarTitle("Check Car"); // Call this in `onResume()`
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        listener.onChangeToolbarTitle("Check Car");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        saveDtcList();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        if (!troubleCodes.isEmpty())
            outState.putStringArrayList(SAVED_TEXT, troubleCodes);
        super.onSaveInstanceState(outState);
    }

    private void setDtcList(ArrayList<String> s) {
        troubleCodes.clear();
        troubleCodes.addAll(s);
        adapter.notifyDataSetChanged();
    }

    private void saveDtcList() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(SAVED_TEXT, new HashSet<>(troubleCodes));
        editor.apply();
    }

    private void loadDtcList() {
        ArrayList<String> savedText = new ArrayList<>(sharedPreferences.getStringSet(SAVED_TEXT, new HashSet<String>()));
        if (!savedText.isEmpty()) {
            setDtcList(savedText);
        }
    }

    private void confirmClear() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle("Clear Codes")
                .setMessage("Are you sure you want to clear trouble codes?")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearDtc();
                        Toast.makeText(getContext(), "Cleared", Toast.LENGTH_SHORT).show();
                        readDtc();
                    }
                })
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_check_read_dtc:
                setDtcList(readDtc());
                break;
            case R.id.button_check_clear_dtc:
                confirmClear();
                break;
        }
    }

    public interface OnFragmentListener {
        void onChangeToolbarTitle(String title);
    }

    private ArrayList<String> readDtc() {
        Log.d(TAG, "Read DTC");
        log.info("Read DTC");

        ArrayList<String> response = new ArrayList<>();

        PendingTroubleCodesCommand ptcc = new PendingTroubleCodesCommand();
        try {
            ptcc.run(socket.getInputStream(), socket.getOutputStream());
            response = ptcc.getListResult();
            Log.d(TAG, String.valueOf(response));

        } catch (InterruptedException | IOException | NullPointerException | NoDataException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            response.add(e.getMessage());

        }
        if (!response.isEmpty()) {
            log.info(response.toString());
            return response;
        } else {
            response.add("No Trouble Codes stored");
            log.info(response.toString());
            return response;
        }
    }

    private void clearDtc() {
        log.info("Clear DTC");
        final ResetTroubleCodesCommand resetTroubleCodes = new ResetTroubleCodesCommand();
        try {
            resetTroubleCodes.run(socket.getInputStream(), socket.getOutputStream());
            log.info("Fault Codes Cleared - " + resetTroubleCodes.getFormattedResult());

        } catch (InterruptedException | IOException | NullPointerException e) {
            log.info(e.getMessage());
        }
    }
}