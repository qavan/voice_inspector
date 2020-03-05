package com.sac.speechdemo;

import android.Manifest.permission;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.user.speechrecognizationasservice.R;
import com.sac.speech.GoogleVoiceTypingDisabledException;
import com.sac.speech.Speech;
import com.sac.speech.SpeechDelegate;
import com.sac.speech.SpeechRecognitionNotAvailable;
import com.sac.speechdemo.util.AudioUtil;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class MyService extends Service implements SpeechDelegate, Speech.stopDueToDelay {
    private AudioUtil audioStreams;
    private long time = new Date().getTime();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        audioStreams = new AudioUtil(this);

        Speech.init(this);
        Speech.getInstance().stopListening();
        Speech.getInstance().setListener(this);

        restartListener();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    @Override
    public void onStartOfSpeech() {
    }

    @Override
    public void onSpeechRmsChanged(float value) {

    }

    @Override
    public void onSpeechPartialResults(List<String> results) {
        for (String partial : results) {
            Log.d("Result", partial + "");
        }
    }

    @Override
    public void onSpeechResult(String result) {
        Log.d("Result", result + "");
        if (!TextUtils.isEmpty(result)) {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSpecifiedCommandPronounced(String event) {
        restartListener();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //Restarting the service if it is removed.
        PendingIntent service =
                PendingIntent.getService(getApplicationContext(), new Random().nextInt(),
                        new Intent(getApplicationContext(), MyService.class), PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, service);
        super.onTaskRemoved(rootIntent);
    }

    /**
     * Проверка наличие прав на запись через микрофон
     *
     * @return true - прова есть
     */
    private boolean isRecordAudioPermission() {
        return ActivityCompat.checkSelfPermission(this, permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void restartListener() {
        if (isSpeechDestroyed())
            return;

        if (Speech.getInstance().isListening()) {
            audioStreams.onMute();
            Speech.getInstance().stopListening();
        } else {
            if (isRecordAudioPermission()) {
                try {
                    Speech.getInstance().stopTextToSpeech();
                    Speech.getInstance().startListening(this);
                } catch (SpeechRecognitionNotAvailable ignored) {


                } catch (GoogleVoiceTypingDisabledException ignored) {

                }
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
            }
            audioStreams.onMute();
        }
    }

    @Override
    public void onDestroy() {
        Log.d("TAG_MY", String.valueOf(time));

        if (!isSpeechDestroyed() && Speech.getInstance().isListening()) {
            Speech.getInstance().shutdown();
        }

        audioStreams.onUnMute();
        super.onDestroy();
    }

    /**
     * Инициализирован ли Speech
     *
     * @return true - да
     */
    private boolean isSpeechDestroyed() {
        try {
            return Speech.getInstance() == null;
        } catch (IllegalStateException ignored) {
            return true;
        }
    }
}