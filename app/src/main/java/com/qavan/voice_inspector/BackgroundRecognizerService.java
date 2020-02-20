package com.qavan.voice_inspector;


import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.qavan.speech.SimpleException;
import com.qavan.speech.Speech;
import com.qavan.voice_inspector.command.ActivateCommand;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.List;
import java.util.Random;

public class BackgroundRecognizerService extends Service implements Speech.stopDueToDelay {
    /**
     * Имя сервиса
     */
    public static final String SERVICE_NAME = "com.qavan.voice_inspector.BackgroundRecognizerService";

    private ActivateCommand acActivateCommand;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        acActivateCommand = new ActivateCommand(this) {
            @Override
            public void onStartOfSpeech() {

            }

            @Override
            public void onSpeechRmsChanged(float value) {

            }

            @Override
            public void onSpeechPartialResults(List<String> results) {

            }

            @Override
            public void onSpeechResult(String[] result) throws SimpleException.SpeechRecognitionNotAvailable, SimpleException.GoogleVoiceTypingDisabledException {
                System.out.println(result[0]);
                applyCommand(result);
            }

            @Override
            public void onSpecifiedCommandPronounced(String event) {
                if (Speech.getInstance().isListening()) {
                    // TODO muteBeepSoundOfRecorder();
                    Speech.getInstance().stopListening();
                } else {
                    RxPermissions.getInstance(this.getContext()).request(Manifest.permission.RECORD_AUDIO).subscribe(this::checkMicrophonePermissionVoidUtil);
                    // TODO muteBeepSoundOfRecorder();
                }
            }

            void checkMicrophonePermissionVoidUtil(boolean granted) {
                if (granted) {
                    try {
                        Speech.getInstance().startListening(null, this.getDelegate());
                    } catch (SimpleException.SpeechRecognitionNotAvailable exc) {
                        // TODO showSpeechNotSupportedDialog();

                    } catch (SimpleException.GoogleVoiceTypingDisabledException exc) {
                        // TODO showEnableGoogleVoiceTyping();
                    }
                } else {
                    Toast.makeText(this.getContext(), R.string.permission_required, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onActivated() {
                Speech.getInstance().say("активировано");
            }

            @Override
            public void onDeactivated() {
                Speech.getInstance().say("деактивировано");
            }

            @Override
            public void onErrorCommand(int type, String[] textCommands) {
            }
        };

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSpecifiedCommandPronounced(String event) {
        if (Speech.getInstance().isListening()) {
            // TODO muteBeepSoundOfRecorder();
            Speech.getInstance().stopListening();
        } else {
            RxPermissions.getInstance(this).request(Manifest.permission.RECORD_AUDIO).subscribe(this::checkMicrophonePermissionVoidUtil);
            // TODO muteBeepSoundOfRecorder();
        }
    }

    /**
     * Автоматический перезапуск службы в случае завершения
     *
     * @param rootIntent
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        PendingIntent service = PendingIntent.getService(getApplicationContext(), new Random().nextInt(),
                new Intent(getApplicationContext(), BackgroundRecognizerService.class), PendingIntent.FLAG_ONE_SHOT);

        AlarmManager amAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert amAlarmManager != null;
        amAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 300, service);
        super.onTaskRemoved(rootIntent);
    }


    //    @Override
//    public void on
    public void checkMicrophonePermissionVoidUtil(boolean granted) {
        if (granted) {
            try {
                Speech.getInstance().stopTextToSpeech();
                Speech.getInstance().startListening(null, acActivateCommand.getDelegate());
            } catch (SimpleException.SpeechRecognitionNotAvailable exc) {
                // TODO showSpeechNotSupportedDialog();

            } catch (SimpleException.GoogleVoiceTypingDisabledException exc) {
                // TODO showEnableGoogleVoiceTyping();
            }
        } else {
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
        }
    }
}