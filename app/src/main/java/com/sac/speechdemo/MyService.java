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

import com.sac.speech.Speech;
import com.sac.speech.SpeechDelegate;
import com.sac.speech.TextToSpeechCallback;
import com.sac.speechdemo.util.AudioUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MyService extends Service implements SpeechDelegate, Speech.stopDueToDelay {
    public static SpeechDelegate delegate;
    private AudioUtil audioStreams;
    private long time = new Date().getTime();
    private boolean triggeredKeyWord = false;
    private String currentFocus = "route";
    private int ticks = 0;
    private Boolean isSpeaking = false;
    private TextToSpeechCallback textToSpeechCallback;

    private final int MAX_TICK_COUNT = 5;
    private String KEY_WORD_ACTIVATION;
    private String KEY_WORD_DEACTIVATION;
    private String KEY_WORD_EXIT;
    private ArrayList<String> KEY_WORDS_SEARCH_BY_NUMBER;
    private ArrayList<String> KEY_WORDS_SEARCH_BY_ID;
    private ArrayList<String> KEY_WORDS_SEARCH_BY_ROOM;
    private String KEY_WORD_UPDATE;
    private String KEY_WORD_UPLOAD;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        audioStreams = new AudioUtil(this);


        Speech.init(this);
        delegate = this;
        Speech.getInstance().stopListening();
//        Speech.getInstance().setListener(this);
        Speech.getInstance().setListener(event -> {
            try {
                Thread.sleep(1700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            restartListener();
        });
        Speech.getInstance().setTransitionMinimumDelay(100);
        Speech.getInstance().setLocale(Locale.getDefault());
        Speech.getInstance().setTextToSpeechRate(1.7f);
        Speech.getInstance().setPreferOffline(true);
        Speech.getInstance().setGetPartialResults(false);

        TextToSpeechCallback textToSpeechCallback = new TextToSpeechCallback() {
            @Override
            public void onStart() {
                isSpeaking = true;
            }

            @Override
            public void onCompleted() {
                isSpeaking = false;
                restartListener();
            }

            @Override
            public void onError() {
                restartListener();
            }
        };
        restartListener();
        KEY_WORD_ACTIVATION = getResources().getString(R.string.KEY_WORD_ACTIVATION);
        KEY_WORD_DEACTIVATION = getResources().getString(R.string.KEY_WORD_DEACTIVATION);
        KEY_WORD_EXIT = getResources().getString(R.string.KEY_WORD_EXIT);
        KEY_WORDS_SEARCH_BY_NUMBER = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.KEY_WORD_SEARCH_BY_NUMBER)));
        KEY_WORDS_SEARCH_BY_ID = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.KEY_WORDS_SEARCH_BY_ID)));
        KEY_WORDS_SEARCH_BY_ROOM = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.KEY_WORDS_SEARCH_BY_ROOM)));
        KEY_WORD_UPDATE = getResources().getString(R.string.KEY_WORD_UPDATE);
        KEY_WORD_UPLOAD = getResources().getString(R.string.KEY_WORD_UPLOAD);
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
        String TAG_LOCAL = "ON_SPEECH_RESULT";
        Log.d("Result", result + "");
        result = result.toLowerCase();
        Log.d("ON_SPEECH_RESULT", result + "");
        if (!TextUtils.isEmpty(result)) {
            if (currentFocus.equals("route")) {
                if (result.contains(KEY_WORD_EXIT)) {
                    voiceCommandProcessing(MAX_TICK_COUNT, TAG_LOCAL, "EXIT", "Выход", textToSpeechCallback);
                    Speech.getInstance().shutdown();
                } else if (result.contains(KEY_WORD_ACTIVATION)) {
                    triggeredKeyWord = true;
                    voiceCommandProcessing(0, TAG_LOCAL, "ACTIVATED", "Активирован", textToSpeechCallback);
                } else if (result.contains(KEY_WORD_DEACTIVATION)) {
                    triggeredKeyWord = false;
                    voiceCommandProcessing(MAX_TICK_COUNT, TAG_LOCAL, "DEACTIVATED", "Дективирован", textToSpeechCallback);
                } else if (triggeredKeyWord & KEY_WORDS_SEARCH_BY_NUMBER.contains(result)) {                                                                           //TODO ADD SEARCH BY NUMBER OF COUNTER WITH PROCESSING OF SIMILAR RESULT
                    voiceCommandProcessing(0, TAG_LOCAL, "KEY_WORD_SEARCH_BY_NUMBER", "Слушаю номера счёта", textToSpeechCallback);
                    currentFocus = "route number";
                } else if (triggeredKeyWord & KEY_WORDS_SEARCH_BY_ID.contains(result)) {                                                                               //TODO ADD SEARCH BY ID WITH PROCESSING OF SIMILAR RESULT
                    voiceCommandProcessing(0, TAG_LOCAL, "KEY_WORD_SEARCH_BY_ID", "Слушаю номера счётчика", textToSpeechCallback);
                    currentFocus = "route id";
                } else if (triggeredKeyWord & KEY_WORDS_SEARCH_BY_ROOM.contains(result)) {                                                                             //TODO ADD SEARCH BY ROOM WITH PROCESSING OF SIMILAR RESULT
                    voiceCommandProcessing(0, TAG_LOCAL, "KEY_WORD_SEARCH_BY_ROOM", "Слушаю номера квартиры", textToSpeechCallback);
                    currentFocus = "route room";
                } else if (triggeredKeyWord & result.contains(KEY_WORD_UPDATE)) {                                                                                      //TODO ADD UPDATE
                    voiceCommandProcessing(0, TAG_LOCAL, "KEY_WORD_UPDATE", "Загружаю", textToSpeechCallback);
                } else if (triggeredKeyWord & result.contains(KEY_WORD_UPLOAD)) {                                                                                      //TODO ADD UPLOAD
                    voiceCommandProcessing(0, TAG_LOCAL, "KEY_WORD_UPDATE", "Выгружаю", textToSpeechCallback);
                }
            } else if (currentFocus.equals("route number")) {
                Integer number = Integer.parseInt(result);
                if (number == null) {
                    Speech.getInstance().say("Скажите число", textToSpeechCallback);
                } else {
                    Speech.getInstance().say(number.toString(), textToSpeechCallback);
                    currentFocus = "route";
                }
            } else if (currentFocus.equals("route id")) {
                Integer number = Integer.parseInt(result);
                if (number == null) {
                    Speech.getInstance().say("Скажите число", textToSpeechCallback);
                } else {
                    Speech.getInstance().say(number.toString(), textToSpeechCallback);
                    currentFocus = "route";
                }
            } else if (currentFocus.equals("route room")) {
                Integer number = Integer.parseInt(result);
                if (number == null) {
                    Speech.getInstance().say("Скажите число", textToSpeechCallback);
                } else {
                    Speech.getInstance().say(number.toString(), textToSpeechCallback);
                    currentFocus = "route";
                }
            }
            //TODO ADD FOCUS OF CARD
        } else {
            if (ticks < MAX_TICK_COUNT && triggeredKeyWord) {
                Log.d(TAG_LOCAL, String.format("TICK +1 %s", ticks));
                voiceCommandProcessing(ticks + 1, TAG_LOCAL, String.format("TICK +1 %s", ticks + 1), "", textToSpeechCallback);
            } else if (ticks >= MAX_TICK_COUNT && triggeredKeyWord) {
                triggeredKeyWord = false;
                voiceCommandProcessing(0, TAG_LOCAL, "DEACTIVATED", "Деактивирован", textToSpeechCallback);
            } else {
                Log.d(TAG_LOCAL, "PASS");
            }
        }
    }

    public void voiceCommandProcessing(int tickCount, String TAG, String TAG_mess, String ttsMessage, TextToSpeechCallback textToSpeechCallback) {
        ticks = tickCount;
        Log.d(TAG, TAG_mess);
        if (!ttsMessage.equals("")) Speech.getInstance().say(ttsMessage, textToSpeechCallback);
    }

    @Override
    public void onSpecifiedCommandPronounced(String event) {
        restartListener();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
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
        if (isSpeechDestroyed() | isSpeaking)
            return;

        if (Speech.getInstance().isListening()) {
//            audioStreams.onMute();
            Speech.getInstance().stopListening();
        } else {
            if (isRecordAudioPermission()) {
                Speech.getInstance().stopTextToSpeech();
                Speech.getInstance().startListening(this);
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
            }
//            audioStreams.onMute();
        }
    }

    @Override
    public void onDestroy() {
        Log.d("TAG_MY", String.valueOf(time));

        if (!isSpeechDestroyed() && Speech.getInstance().isListening()) {
            Speech.getInstance().shutdown();
        }

//        audioStreams.onUnMute();
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