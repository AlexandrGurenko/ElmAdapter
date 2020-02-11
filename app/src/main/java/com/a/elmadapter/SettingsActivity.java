package com.a.elmadapter;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.logging.Logger;

public class SettingsActivity extends AppCompatActivity {

    private static final Logger log = Logger.getLogger(SettingsActivity.class.getSimpleName());

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        log.info("Settings Activity started");

        textView = findViewById(R.id.tv_settings);

    }
}
