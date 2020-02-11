package com.a.elmadapter.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.a.elmadapter.R;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();
    public static final String SAVED_TEXT = "saved_text_home";

    private OnFragmentListener listener;

    private TextView textView;
    SharedPreferences sharedPreferences;

    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferences = Objects.requireNonNull(this.getActivity()).getPreferences(Context.MODE_PRIVATE);

        textView = root.findViewById(R.id.text_home);

        if (savedInstanceState != null) {
            textView.setText(savedInstanceState.getString(TAG));
        }

        loadText();

        // Inflate the layout for this fragment
        return root;
    }

    public void setFragmentListener(OnFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        if (context instanceof OnFragmentListener) {
            listener = (OnFragmentListener) context;
            listener.onChangeToolbarTitle("Home"); // Call this in `onResume()`
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        listener.onChangeToolbarTitle("Home");
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        super.onDetach();
        listener = null;
        saveText();
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        if (!textView.getText().toString().isEmpty())
            outState.putString(SAVED_TEXT, textView.getText().toString());
        super.onSaveInstanceState(outState);
    }

    public void setText(String text) {
        textView.append("\n" + text);
    }

    public interface OnFragmentListener {
        void onChangeToolbarTitle(String title);
    }
}