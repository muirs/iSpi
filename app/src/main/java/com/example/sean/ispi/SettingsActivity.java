package com.example.sean.ispi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {

    private float dist = 1000;
    private boolean reset = false;

    private Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Button mResetButton;
        final Button mhalfKButton;
        final Button mOneKButton;
        final Button mTwoKButton;

        i = new Intent(SettingsActivity.this, MainActivity.class);

        Intent settingsIntent = getIntent();

        mResetButton = (Button) findViewById(R.id.reset_button);
        mhalfKButton = (Button) findViewById(R.id.half_k_button);
        mOneKButton = (Button) findViewById(R.id.k_button);
        mTwoKButton = (Button) findViewById(R.id.two_k_button);

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset = true;
            }
        });

        mhalfKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dist = 500;
            }
        });

        mOneKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dist = 1000;
            }
        });

        mTwoKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dist = 2000;
            }
        });
    }

    @Override
    public void finish() {

        i.putExtra("RESET",reset);
        i.putExtra("DIST",dist);

        setResult(RESULT_OK,i);
        super.finish();
    }


}
