package com.sac.speechdemo;

import android.app.ActivityManager;
import android.content.Context;

public class Utils {

    /**
     * Проверка на запуск фоновой службы
     *
     * @param context     контекст
     * @param serviceName имя службы
     * @return результат, true - служба найдена
     */
    public static boolean checkServiceRunning(Context context, String serviceName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceName.equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
