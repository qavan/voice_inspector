package com.sac.speechdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.user.speechrecognizationasservice.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int REQUEST_PERMISSIONS = 1;

    private Button btStartService;
    private TextView tvText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btStartService = findViewById(R.id.btStartService);
        btStartService.setOnClickListener(this);
        tvText = findViewById(R.id.tvText);

        if (Utils.checkServiceRunning(this, getString(R.string.my_service_name))) {
            btStartService.setText(getString(R.string.stop_service));
            tvText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO};

            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onClick(View v) {
        if (btStartService.getText().toString().equalsIgnoreCase(getString(R.string.start_service))) {
            startService(new Intent(MainActivity.this, MyService.class));
            btStartService.setText(getString(R.string.stop_service));
            tvText.setVisibility(View.VISIBLE);
        } else {
            stopService(new Intent(MainActivity.this, MyService.class));
            btStartService.setText(getString(R.string.start_service));
            tvText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length != 1) {
                    Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}
