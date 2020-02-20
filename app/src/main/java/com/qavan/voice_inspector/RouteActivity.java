package com.qavan.voice_inspector;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.Theme;

public class RouteActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    private ToggleButton mToggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        mToggleButton = findViewById(R.id.idCheckBoxService);
        mToggleButton.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Utils.checkServiceRunning(this, BackgroundRecognizerService.SERVICE_NAME)) {
            mToggleButton.setText(getString(R.string.stop_service_after));
        }
    }

    /**
     * Обработчик нажатия на кнопку
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Intent intent = new Intent(RouteActivity.this, BackgroundRecognizerService.class);

        if (buttonView.isChecked()) {
            startService(intent);
        } else {
            stopService(intent);
        }

        buttonView.setText(getString(buttonView.isChecked() ?
                R.string.stop_service_after :
                R.string.start_service_before));
    }
}