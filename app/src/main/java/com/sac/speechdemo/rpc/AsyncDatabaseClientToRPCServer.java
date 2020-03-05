package com.sac.speechdemo.rpc;

import android.os.AsyncTask;
import android.util.Log;

import com.sac.speechdemo.Task;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AsyncDatabaseClientToRPCServer extends AsyncTask<List<Task>, Void, List<Task>> {
    private final static String TAG = "ASYNC_CLIENT_TO_RPC";
    private static final String RPC_SERVER_ADDRESS = "http://kes.it-serv.ru/voice/rpc";
    private static final String RPC_SERVER_TOKEN = "0LvQvtCz0LjQvTox";
    private static final String RPC_SERVER_DATABASE_NAME = "cd_points";

    private OkHttpClient mHttpClient = new OkHttpClient();

    @Override
    protected List<Task> doInBackground(List<Task>... tasks) {
        //TODO ADD CHECK {SUCCESS: TRUE OR FALSE}
        MediaType mediaType = MediaType.parse("application/json");
        List<String> tasksJsonStringList = new ArrayList<>();
        String formattableString = "{ \"action\":\"%s\", \"method\": \"Update\",\"data\": [ { \"id\": \"%s\", " +
                "\"c_address\": \"%s\", \"c_subscr\": \"%s\", \"d_prev_date\": \"%s\", \"n_prev_value\": \"%s\", " +
                "\"d_current_date\": \"%s\", \"n_current_value\": \"%s\", \"b_done\": %s }], \"type\": \"rpc\", \"tid\": 1 }";
        for (Task task : tasks[0]) {
            tasksJsonStringList.add(String.format(formattableString,
                    RPC_SERVER_DATABASE_NAME,
                    task.getPostgreId(),
                    task.getC_address(),
                    task.getC_client(),
                    task.getD_prev_date(),
                    task.getN_prev_value(),
                    task.getD_current_date(),
                    task.getN_current_value(),
                    String.valueOf(!task.getB_done())));
        }
        Log.d(TAG, String.format("Prepared %s tasks for sync queue", tasksJsonStringList.size()));
        RequestBody body = RequestBody.create(mediaType, tasksJsonStringList.toString());
        Request request = new Request.Builder()
                .url(RPC_SERVER_ADDRESS)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("RPC-Authorization", String.format("Token %s", RPC_SERVER_TOKEN))
                .build();
        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);

                String jsonData = Objects.requireNonNull(response
                        .body())
                        .string()
                        .replace("[\n  {\n    \"meta\": {", "  {\n    \"meta\": {")
                        .replace("\"\n  }\n]", "\"\n  }\n");
                JSONObject Jobject;

                try {
                    Jobject = new JSONObject(jsonData);
                } catch (JSONException e) {
                    Log.i(TAG, "JSONException error while parsing from response");
                    e.printStackTrace();
                    return;
                }
                if (Jobject != null) {
                    try {
                        Log.i(TAG, String.format("Gotcha code %s! All tasks successfully unloaded to RPC server", Jobject.get("code")));
                    } catch (JSONException e) {
                        Log.i(TAG, "JSONException error at task uploading");
                        e.printStackTrace();
                    }
                }
            }
        });
        return null;
    }

    @Override
    protected void onPostExecute(List<Task> tasks) {
        super.onPostExecute(tasks);
        this.cancel(true);
    }
}
