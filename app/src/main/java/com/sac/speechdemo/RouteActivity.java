package com.sac.speechdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.speechrecognizationasservice.R;
import com.sac.speechdemo.rpc.AsyncDatabaseClientToRPCServer;
import com.sac.speechdemo.rpc.AsyncDatabaseRPCServerToClient;
import com.sac.speechdemo.util.Utils;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.List;

public class RouteActivity extends Activity {
    private static final String TAG = "ROUTE_ACTIVITY";

    private static List<Task> mTasks;
    private static TaskAdapter mTaskAdapter;
    private static TaskDao mTaskDao;
    private static Query<Task> mTasksQuery;

    private final int REQUEST_PERMISSIONS = 1;

    private Button btStartService;
    private static RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);


        btStartService = findViewById(R.id.idStartServiceButton);
        btStartService.setOnClickListener(serviceOnClick);

        mTasks = new ArrayList<>();
        Log.i(RouteActivity.TAG, String.valueOf(mTasks.size()));
        mTaskAdapter = new TaskAdapter(mTasks, getLayoutInflater(), this);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mTaskAdapter);
        final Button mSyncWithServer = findViewById(R.id.idSyncRPCServerToClientButton);
        mSyncWithServer.setOnClickListener(asyncLoadFromServer);

        final Button mSyncWithClient = findViewById(R.id.idSyncClientToRPCServerButton);
        mSyncWithClient.setOnClickListener(asyncUploadToServer);

        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        mTaskDao = daoSession.getTaskDao();
        mTasksQuery = mTaskDao.queryBuilder().orderAsc(TaskDao.Properties.TaskId).build();

        Utils.updateTasks(mTasks, mTasksQuery, mTaskAdapter);

        if (Utils.checkServiceRunning(this, getString(R.string.my_service_name))) {
            btStartService.setText(getString(R.string.stop_service));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.getPermission(this, getApplicationContext(), Manifest.permission.INTERNET);
        Utils.updateTasks(mTasks, mTasksQuery, mTaskAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO};
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }

        Utils.getPermission(this, getApplicationContext(), Manifest.permission.INTERNET);
        Utils.updateTasks(mTasks, mTasksQuery, mTaskAdapter);
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


    Button.OnClickListener serviceOnClick = v -> {
        if (btStartService.getText().toString().equalsIgnoreCase(getString(R.string.start_service))) {
            startService(new Intent(RouteActivity.this, MyService.class));
            btStartService.setText(getString(R.string.stop_service));
        } else {
            stopService(new Intent(RouteActivity.this, MyService.class));
            btStartService.setText(getString(R.string.start_service));
        }
    };

    Button.OnClickListener asyncLoadFromServer = v -> {
        AsyncDatabaseRPCServerToClient aTask = new AsyncDatabaseRPCServerToClient();
        aTask.execute("");
        Utils.updateTasks(mTasks, mTasksQuery, mTaskAdapter);
    };

    Button.OnClickListener asyncUploadToServer = v -> {
        AsyncDatabaseClientToRPCServer aTask = new AsyncDatabaseClientToRPCServer();
        aTask.execute(RouteActivity.getTasks());
    };


    public Context getContext() {
        return getApplicationContext();
    }

    public static void setTasks(List<Task> tasks) {
        if (mTasks.size() != tasks.size()) {
            mTasks = tasks;
            for (Task task : tasks) {
                mTaskDao.insert(task);
            }
            String onUpdateString = String.format("Got and synced %s tasks", mTasks.size());
            Log.i(RouteActivity.TAG, onUpdateString);
        } else
            Log.i(RouteActivity.TAG, "Tasks already synced");
        RouteActivity.mRecyclerView.hasPendingAdapterUpdates();
    }

    public static List<Task> getTasks() {
        return mTasksQuery.list();
    }
}
