package com.qavan.voice_inspector;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.Theme;

public class MainActivity extends Activity {

    private Button btStartService;
    private Button btListen;
    private TextView tvText;

    public static final Intent[] AUTO_START_INTENTS = {
            new Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(Uri.parse("mobilemanager://function/entry/AutoStart"))
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btStartService = findViewById(R.id.btStartService);
        btListen = findViewById(R.id.button);
        tvText = findViewById(R.id.textViewTitle);
        enableAutoStart();

        if (checkServiceRunning()) {
            btStartService.setText(getString(R.string.stop_service));
            tvText.setVisibility(View.VISIBLE);
        }

        btStartService.setOnClickListener(v -> {
            if (btStartService.getText().toString().equalsIgnoreCase(getString(R.string.start_service))) {
                startService(new Intent(MainActivity.this, BackgroundRecognizerService.class));
                btStartService.setText(getString(R.string.stop_service));
                tvText.setVisibility(View.VISIBLE);
            } else {
                stopService(new Intent(MainActivity.this, BackgroundRecognizerService.class));
                btStartService.setText(getString(R.string.start_service));
                tvText.setVisibility(View.GONE);
            }
        });

        btListen.setOnClickListener(v -> {
            if (btListen.getText().toString().equalsIgnoreCase(getString(R.string.start_service))) {
                startService(new Intent(MainActivity.this, BackgroundRecognizerService.class));
                btListen.setText(getString(R.string.stop_service));
                tvText.setVisibility(View.VISIBLE);
            } else {
                stopService(new Intent(MainActivity.this, BackgroundRecognizerService.class));
                btListen.setText(getString(R.string.start_service));
                tvText.setVisibility(View.GONE);
            }
        });
    }

    private void enableAutoStart() {
        for (Intent intent : AUTO_START_INTENTS) {
            if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                new Builder(this).title(R.string.enable_autostart)
                        .content(R.string.ask_permission)
                        .theme(Theme.LIGHT)
                        .positiveText(getString(R.string.allow))
                        .onPositive((dialog, which) -> {
                            try {
                                for (Intent intent1 : AUTO_START_INTENTS)
                                    if (getPackageManager().resolveActivity(intent1, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                                        startActivity(intent1);
                                        break;
                                    }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })
                        .show();
                break;
            }
        }
    }

    public boolean checkServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (getString(R.string.my_service_name).equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
}