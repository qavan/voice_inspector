package com.sac.speechdemo.rpc;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.sac.speechdemo.RouteActivity;
import com.sac.speechdemo.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AsyncDatabaseRPCServerToClient extends AsyncTask<String, Void, Boolean> {
    private final static String TAG = "ASYNC_RPC_TO_CLIENT";
    private static final String RPC_SERVER_ADDRESS = "http://kes.it-serv.ru/voice/rpc";
    private static final String RPC_SERVER_TOKEN = "0LvQvtCz0LjQvTox";
    private static final String RPC_SERVER_DATABASE_NAME = "cd_points";

    private List<Task> mRemoteTasks = new ArrayList<>();
    private OkHttpClient mHttpClient = new OkHttpClient();
    private int gotTasksCount = 0;
    private String errorText = "";
    private Boolean status = false;

    @Override
    protected Boolean doInBackground(String... rpcServerRemoteAddressCanBeMaxOneSizeStringList) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, String.format("{\n    \"action\":\"%s\",\n    \"method\":\"Query\",\n    \"data\":[\n       {\n          \"query\":\"\",\n          \"page\":1,\n          \"start\":0,\n          \"limit\":25\n       }\n    ],\n    \"type\":\"rpc\",\n    \"tid\":9\n }", RPC_SERVER_DATABASE_NAME));
        Request request = new Request.Builder()
                .url(RPC_SERVER_ADDRESS)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("RPC-Authorization", String.format("Token %s", RPC_SERVER_TOKEN))
                .build();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@org.jetbrains.annotations.NotNull Call call, @org.jetbrains.annotations.NotNull IOException e) {
                errorText = e.getMessage();
                Log.e(TAG, Objects.requireNonNull(errorText));
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@org.jetbrains.annotations.NotNull Call call, @org.jetbrains.annotations.NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    errorText = response.message();
                    Log.e(TAG, String.format("Server returned %s", errorText));
                } else {
                    String jsonData = Objects.requireNonNull(response.body()).string().replace("[\n  {\n    \"meta\": {", "  {\n    \"meta\": {").replace("\"\n  }\n]", "\"\n  }\n");
                    JSONObject Jobject;
                    JSONObject Jsonobj;
                    JSONArray Jsonarr;
                    try {
                        Jobject = new JSONObject(jsonData);
                        Jsonobj = Jobject.getJSONObject("result");
                        Jsonarr = Jsonobj.getJSONArray("records");
                    } catch (JSONException e) {
                        Log.i(TAG, "JSONException error while parsing from response");
                        e.printStackTrace();
                        countDownLatch.countDown();
                        return;
                    }
                    for (int i = 0; i < Jsonarr.length(); i++) {
                        try {
                            JSONObject currObj = Jsonarr.getJSONObject(i);
                            Task tmpTask = new Task(currObj.get("id").toString(), currObj.get("c_subscr").toString(), currObj.get("c_address").toString(), currObj.get("c_subscr").toString(), currObj.get("d_prev_date").toString(), currObj.get("n_prev_value").toString(), currObj.get("d_current_date").toString(), currObj.get("n_current_value").toString(), Boolean.valueOf(currObj.get("b_done").toString()));
                            mRemoteTasks.add(tmpTask);
                            gotTasksCount += 1;
                        } catch (JSONException e) {
                            Log.e(TAG, String.format("JSONException error at task [i]=%s", i));
                            e.printStackTrace();
                        }
                    }
                    status = true;
                    Log.i(TAG, String.format("Loaded %s tasks", mRemoteTasks.size()));
                    RouteActivity.setTasks(mRemoteTasks);
                }
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return status;
    }

    @Override
    protected void onPostExecute(Boolean status) {
        if (status) {
            Toast.makeText(RouteActivity.getContext(), String.format("С сервера успешно загружено %s задач!", gotTasksCount), Toast.LENGTH_LONG).show();
            Log.i(TAG, String.format("Loaded %s tasks from RPC server", gotTasksCount));
        } else {
            Toast.makeText(RouteActivity.getContext(), String.format("Ошибка синхронизации с сервером %s\nСообщение об ошибке уже отправлено", errorText), Toast.LENGTH_LONG).show();
            Log.e(TAG, String.format("Gotcha %s error while connecting to RPC server", errorText));
        }
    }
}
