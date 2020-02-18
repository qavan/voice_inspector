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
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.qavan.speech.SimpleException;
import com.qavan.speech.Speech;
import com.qavan.speech.SpeechDelegate;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class BackgroundRecognizerService extends Service implements SpeechDelegate, Speech.stopDueToDelay {
    public static SpeechDelegate delegate;
    private boolean triggeredKeyWord = false;
    private String currentFocus = "rout";
    private int ticks = 0;

    private final int MAX_TICK_COUNT = 5;
    private String KEY_WORD_ACTIVATION;
    private ArrayList<String> KEY_WORDS_SEARCH_BY_NUMBER;
    private ArrayList<String> KEY_WORDS_SEARCH_BY_ID;
    private ArrayList<String> KEY_WORDS_SEARCH_BY_ROOM;
    private String KEY_WORD_UPDATE;

    private TextToSpeechUtil tts;
    private AudioManager amAudioManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ((AudioManager) Objects.requireNonNull(getSystemService(Context.AUDIO_SERVICE))).setStreamMute(AudioManager.STREAM_SYSTEM, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tts = new TextToSpeechUtil(this, (float) 1.7d);
        amAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Speech.init(this);
        delegate = this;
        Speech.getInstance().setListener(this);

        if (Speech.getInstance().isListening()) {
            Speech.getInstance().stopListening();
            muteBeepSoundOfRecorder();
        } else {
            System.setProperty("rx.unsafe-disable", "True");
            RxPermissions.getInstance(this).request(Manifest.permission.RECORD_AUDIO).subscribe(this::checkMicrophonePermissionVoidUtil);
            muteBeepSoundOfRecorder();
        }
        KEY_WORD_ACTIVATION = getResources().getString(R.string.KEY_WORD_ACTIVATION);
        KEY_WORDS_SEARCH_BY_NUMBER = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.KEY_WORD_SEARCH_BY_NUMBER)));
        KEY_WORDS_SEARCH_BY_ID = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.KEY_WORDS_SEARCH_BY_ID)));
        KEY_WORDS_SEARCH_BY_ROOM = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.KEY_WORDS_SEARCH_BY_ROOM)));
        KEY_WORD_UPDATE = getResources().getString(R.string.KEY_WORD_UPDATE);
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
        result = result.toLowerCase();
        Log.d("ON_SPEECH_RESULT", result + "");
//        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        if (!TextUtils.isEmpty(result)) {
            if (currentFocus.equals("rout")) {
                if (result.contains(KEY_WORD_ACTIVATION)) {
                    ticks = 0;
                    triggeredKeyWord = true;
                    Log.d("ON_SPEECH_RESULT", "ACTIVATED");
                } else if (KEY_WORDS_SEARCH_BY_NUMBER.contains(result)) {
                    Speech.getInstance().stopListening();
                    unMuteBeepSoundOfRecorder();
                    tts.speak("скажите номер");
//                    muteBeepSoundOfRecorder();
                    //TODO ADD SEARCH BY NUMBER OF COUNTER WITH PROCESSING OF SIMILAR RESULT
                    ticks = 0;
                    Log.d("ON_SPEECH_RESULT", "KEY_WORD_SEARCH_BY_NUMBER");
                } else if (KEY_WORDS_SEARCH_BY_ID.contains(result)) {
                    //TODO ADD SEARCH BY ID WITH PROCESSING OF SIMILAR RESULT
                    ticks = 0;
                    Log.d("ON_SPEECH_RESULT", "KEY_WORD_SEARCH_BY_ID");
                } else if (KEY_WORDS_SEARCH_BY_ROOM.contains(result)) {
                    //TODO ADD SEARCH BY ROOM WITH PROCESSING OF SIMILAR RESULT
                    ticks = 0;
                    Log.d("ON_SPEECH_RESULT", "KEY_WORD_SEARCH_BY_ROOM");
                } else if (result.contains(KEY_WORD_UPDATE)) {
                    //TODO ADD UPDATE
                    ticks = 0;
                    Log.d("ON_SPEECH_RESULT", "KEY_WORD_UPDATE");
                }
            }
            //TODO ADD FOCUS OF CARD
        } else {
            if (ticks < MAX_TICK_COUNT && triggeredKeyWord) {
                ticks += 1;
                Log.d("ON_SPEECH_RESULT", String.format("TICK +1 %s", ticks));
            } else if (ticks >= MAX_TICK_COUNT && triggeredKeyWord) {
                ticks = 0;
                triggeredKeyWord = false;
                Log.d("ON_SPEECH_RESULT", "DEACTIVATED");
            } else {
                Log.d("ON_SPEECH_RESULT", "PASS");
            }
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
//            muteBeepSoundOfRecorder();
            Speech.getInstance().stopListening();
        } else {
            RxPermissions.getInstance(this).request(Manifest.permission.RECORD_AUDIO).subscribe(this::checkMicrophonePermissionVoidUtil);
//            muteBeepSoundOfRecorder();
        }
    }

    /**
     * Function to remove the beep sound of CARD_RECORD_BUTTON recognizer.
     */
    private void muteBeepSoundOfRecorder() {
        if (amAudioManager != null) {
            amAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }
    }

    private void unMuteBeepSoundOfRecorder() {
        if (amAudioManager != null) {
            amAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //Restarting the service if it is removed.
        PendingIntent service = PendingIntent.getService(getApplicationContext(), new Random().nextInt(),
                new Intent(getApplicationContext(), BackgroundRecognizerService.class), PendingIntent.FLAG_ONE_SHOT);

        AlarmManager amAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert amAlarmManager != null;
//        amAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 3000, service);
        super.onTaskRemoved(rootIntent);
    }

    void checkMicrophonePermissionVoidUtil(boolean granted) {
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