package com.sac.speechdemo.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sac.speechdemo.Task;
import com.sac.speechdemo.TaskAdapter;

import org.greenrobot.greendao.query.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

    public static void updateTasks(List<Task> tasks, Query<Task> tasksQuery, TaskAdapter mTaskAdapter) {
        tasks = tasksQuery.list();
        mTaskAdapter.setTasks(tasks);
    }

    public static Date getJavaMainFormattedDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        try {
            return sdf.parse(date.replace("Z", "").replace("T", "-"));
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getFormattedDateDDMMYY(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM");
        return formatter.format(date);
    }

    public static void getPermission(Activity activity, Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Нужны права доступа к интернету!", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(activity, new String[]{permission}, 1);
        }
    }
}
