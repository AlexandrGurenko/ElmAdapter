package com.a.elmadapter.fragments;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.a.elmadapter.MainActivity;
import com.a.elmadapter.R;
import com.a.elmadapter.obd.obd.commands.control.PendingTroubleCodesCommand;
import com.a.elmadapter.obd.obd.exceptions.NoDataException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class CheckCarFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = CheckCarFragment.class.getSimpleName();
    private static final String SAVED_TEXT = "saved_text_check_car";

    private OnFragmentListener listener;

    private SharedPreferences sharedPreferences;

    private BluetoothSocket socket;

    private TextView textView;

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

        textView = rootView.findViewById(R.id.text_check_car);
        Button buttonRead = rootView.findViewById(R.id.button_check_read_dtc);
        buttonRead.setOnClickListener(this);
        Button buttonClear = rootView.findViewById(R.id.button_check_clear_dtc);
        buttonClear.setOnClickListener(this);

        sharedPreferences = Objects.requireNonNull(this.getActivity()).getPreferences(Context.MODE_PRIVATE);
        loadText();

        if (savedInstanceState != null) {
            textView.setText(savedInstanceState.getString(TAG));
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
        saveText();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        if (!textView.getText().toString().isEmpty())
            outState.putString(SAVED_TEXT, textView.getText().toString());
        super.onSaveInstanceState(outState);
    }

    private void setText(String text) {
        Log.d(TAG, "setText");
        textView.setText(text);
    }

    private void appendText(String text) {
        Log.d(TAG, "appendText");
        textView.append("\n" + text);
    }

    private void appendText(ArrayList<String> text) {
        Log.d(TAG, "appendText Array");
        for (String s : text) {
            textView.append("\n" + s);
        }
    }

    private void saveText() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SAVED_TEXT, textView.getText().toString());
        editor.apply();
    }

    private void loadText() {
        String savedText = sharedPreferences.getString(SAVED_TEXT, "");
        if (!savedText.isEmpty())
            textView.setText(savedText);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_check_read_dtc:
                readDtc();
                break;
            case R.id.button_check_clear_dtc:
                Toast.makeText(getContext(), "Clear btn", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public interface OnFragmentListener {
        void onChangeToolbarTitle(String title);
    }

    private void readDtc() {
        Log.d(TAG, "Read DTC");

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
        appendText(response);
    }
}