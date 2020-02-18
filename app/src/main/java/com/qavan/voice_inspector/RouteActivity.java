package com.qavan.voice_inspector;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.cardview.widget.CardView;

import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.Theme;

import static android.widget.LinearLayout.*;

public class RouteActivity extends Activity {

    private ToggleButton cbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        cbService = findViewById(R.id.idCheckBoxService);
        enableAutoStart();
        if (checkServiceRunning()) {
            cbService.setText(getString(R.string.stop_service_after));
        }

        cbService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked()) {
                startService(new Intent(RouteActivity.this, BackgroundRecognizerService.class));
                cbService.setText(getString(R.string.stop_service_after));
            } else {
                stopService(new Intent(RouteActivity.this, BackgroundRecognizerService.class));
                cbService.setText(getString(R.string.start_service_before));
            }

        });
        //TODO CARDS AUTO RESIZE TO SMALL SIZE
    }

    private void enableAutoStart() {

        final Intent[] autoStartIntents = {
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

        //TODO ADD CHECK OF AUTO START IS ENABLE

        for (Intent intent : autoStartIntents) {
            if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                new Builder(this).title(R.string.enable_autostart)
                        .content(R.string.ask_permission)
                        .theme(Theme.LIGHT)
                        .positiveText(getString(R.string.allow))
                        .onPositive((dialog, which) -> {
                            try {
                                for (Intent intent1 : autoStartIntents)
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

    public void addCard(View view) {
        CardView exampleCard = findViewById(R.id.cardViewProcess);

        CardView newCardView = new CardView(view.getContext());
        newCardView.setLayoutParams(exampleCard.getLayoutParams());
        newCardView.setRadius(exampleCard.getRadius());

        TextView newCardStatus = new TextView(view.getContext());
        newCardStatus.setEms(10);
        newCardStatus.setAllCaps(true);
        TextView CS = findViewById(R.id.idCardStatus);
        newCardStatus.setLayoutParams(CS.getLayoutParams());
        newCardStatus.setFontFeatureSettings(CS.getFontFeatureSettings());
        newCardStatus.setText(CS.getText());
        newCardStatus.setTypeface(CS.getTypeface());
        newCardStatus.setTextColor(CS.getCurrentTextColor());
        newCardStatus.setTextSize(12);
        newCardStatus.setText(R.string.ROUT_CARD_STATUS_PROCESS);

        TextView newCardTitle = new TextView(view.getContext());
        TextView CT = findViewById(R.id.idCardTitle);
        newCardTitle.setLayoutParams(CT.getLayoutParams());
        newCardTitle.setFontFeatureSettings(CT.getFontFeatureSettings());
        newCardTitle.setText(CT.getText());
        newCardTitle.setTypeface(CT.getTypeface());
        newCardTitle.setTextColor(CT.getCurrentTextColor());
        newCardTitle.setTextSize(18);
        newCardTitle.setText(R.string.ROUT_CARD_TITLE_PLACEHOLDER);

        TextView newCardAddress = new TextView(view.getContext());
        newCardAddress.setLayoutParams(findViewById(R.id.idCardAddress).getLayoutParams());
        newCardAddress.setMaxLines(2);
        newCardAddress.setMinLines(2);
        newCardAddress.setText(getResources().getString(R.string.ROUT_CARD_ADDRESS_PLACEHOLDER_FULL));
        newCardAddress.setTextColor(getResources().getColor(R.color.colorBlackPrimary));
        newCardAddress.setTextSize(14);
        newCardAddress.setText(R.string.ROUT_CARD_ADDRESS_PLACEHOLDER_FULL);

        TextView newCardDate = new TextView(view.getContext());
        newCardDate.setLayoutParams(findViewById(R.id.idCardDate).getLayoutParams());
        newCardDate.setText(getResources().getString(R.string.ROUT_CARD_DATE_PLACEHOLDER));
        newCardDate.setTextColor(getResources().getColor(R.color.colorBlackPrimaryLightOP45));
        newCardDate.setTextSize(14);
        newCardDate.setText(R.string.ROUT_CARD_DATE_PLACEHOLDER);

        TextView newCardWtf = new TextView(view.getContext());
        newCardWtf.setLayoutParams(findViewById(R.id.idCardWtf).getLayoutParams());
        newCardWtf.setText(getResources().getString(R.string.ROUT_CARD_PERSON_PLACEHOLDER));
        newCardWtf.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        newCardWtf.setTextColor(getResources().getColor(R.color.colorBlackPrimaryLightOP45));
        newCardWtf.setTextSize(14);
        newCardWtf.setText(R.string.ROUT_CARD_PERSON_PLACEHOLDER);

        LinearLayout newCardBottom = new LinearLayout(view.getContext());
        newCardBottom.setOrientation(HORIZONTAL);
        newCardBottom.setLayoutParams(findViewById(R.id.idCardBottom).getLayoutParams());
        newCardBottom.addView(newCardDate);
        newCardBottom.addView(newCardWtf);

        LinearLayout newCardLL = new LinearLayout(view.getContext());
        newCardLL.setLayoutParams(findViewById(R.id.idCardLL).getLayoutParams());
        newCardLL.setOrientation(VERTICAL);
        newCardLL.setGravity(5);

        newCardLL.addView(newCardStatus);
        newCardLL.addView(newCardTitle);
        newCardLL.addView(newCardAddress);
        newCardLL.addView(newCardBottom);

        newCardView.addView(newCardLL);
        LinearLayout linLay = findViewById(R.id.cardsLinearLayout);
        linLay.addView(newCardView);
    }

}