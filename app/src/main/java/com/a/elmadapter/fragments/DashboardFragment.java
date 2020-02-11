package com.a.elmadapter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.a.elmadapter.R;
import com.a.elmadapter.SettingsActivity;

public class DashboardFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = DashboardFragment.class.getSimpleName();

    private OnFragmentListener listener;

    private TextView textView;
    private Button button;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        textView = rootView.findViewById(R.id.text_dashboard);
        button = rootView.findViewById(R.id.button_dashboard);
        button.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        if (context instanceof OnFragmentListener) {
            listener = (OnFragmentListener) context;
            listener.onChangeToolbarTitle("Dashboard"); // Call this in `onResume()`
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        listener.onChangeToolbarTitle("Dashboard");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_dashboard) {
            SettingsActivity settingsActivity = new SettingsActivity();
            settingsActivity.getAllSupportPids();
        }
    }

    public interface OnFragmentListener {
        void onChangeToolbarTitle(String title);
    }
}
