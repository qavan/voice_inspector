package com.qavan.voice_inspector;


import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.qavan.speech.SimpleException;
import com.qavan.speech.Speech;
import com.qavan.speech.SpeechDelegate;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class BackgroundRecognizerService extends Service implements SpeechDelegate, Speech.stopDueToDelay {

    public static SpeechDelegate delegate;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ((AudioManager) Objects.requireNonNull(getSystemService(Context.AUDIO_SERVICE))).setStreamMute(AudioManager.STREAM_SYSTEM, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Speech.init(this);
        delegate = this;
        Speech.getInstance().setListener(this);

        if (Speech.getInstance().isListening()) {
            Speech.getInstance().stopListening();
            muteBeepSoundOfRecorder();
        } else {
            System.setProperty("rx.unsafe-disable", "True");
            RxPermissions.getInstance(this).request(Manifest.permission.RECORD_AUDIO).subscribe(this::checkPicrophonePremissionVoidUtil);
            muteBeepSoundOfRecorder();
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //NOTTODO for communication return IBinder implementation
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
            Log.d("PartialResult", partial + "");
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
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ((AudioManager) Objects.requireNonNull(getSystemService(Context.AUDIO_SERVICE))).setStreamMute(AudioManager.STREAM_SYSTEM, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Speech.getInstance().isListening()) {
            muteBeepSoundOfRecorder();
            Speech.getInstance().stopListening();
        } else {
            RxPermissions.getInstance(this).request(Manifest.permission.RECORD_AUDIO).subscribe(this::checkPicrophonePremissionVoidUtil);
            muteBeepSoundOfRecorder();
        }
    }

    /**
     * Function to remove the beep sound of CARD_RECORD_BUTTON recognizer.
     */
    private void muteBeepSoundOfRecorder() {
        AudioManager amAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (amAudioManager != null) {
            amAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            amAudioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
            amAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            amAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
            amAudioManager.setStreamMute(AudioManager.STREAM_RING, true);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //Restarting the service if it is removed.
        PendingIntent service = PendingIntent.getService(getApplicationContext(), new Random().nextInt(),
                new Intent(getApplicationContext(), BackgroundRecognizerService.class), PendingIntent.FLAG_ONE_SHOT);

        AlarmManager amAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert amAlarmManager != null;
        amAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 3000, service);
        super.onTaskRemoved(rootIntent);
    }

    void checkPicrophonePremissionVoidUtil(boolean granted) {
        if (granted) {
            try {
                Speech.getInstance().stopTextToSpeech();
                Speech.getInstance().startListening(null, this);
            } catch (SimpleException.SpeechRecognitionNotAvailable exc) {
//                        showSpeechNotSupportedDialog();

            } catch (SimpleException.GoogleVoiceTypingDisabledException exc) {
//                        showEnableGoogleVoiceTyping();
            }
        } else {
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
        }
    }
}