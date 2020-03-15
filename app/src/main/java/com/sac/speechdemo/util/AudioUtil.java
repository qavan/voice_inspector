package com.sac.speechdemo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;

import java.util.HashMap;

public class AudioUtil {
    private AudioManager mAudioManager;
    private HashMap<Integer, Boolean> audioStreams;
    private SharedPreferences mSharedPreferences;

    public AudioUtil(Context context) {
        mSharedPreferences = context.getSharedPreferences("audio", Context.MODE_PRIVATE);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioStreams = new HashMap<>();

        audioStreams.put(AudioManager.STREAM_NOTIFICATION, getValue(AudioManager.STREAM_NOTIFICATION));
        audioStreams.put(AudioManager.STREAM_ALARM, getValue(AudioManager.STREAM_ALARM));
        audioStreams.put(AudioManager.STREAM_MUSIC, getValue(AudioManager.STREAM_MUSIC));
        audioStreams.put(AudioManager.STREAM_RING, getValue(AudioManager.STREAM_RING));
        audioStreams.put(AudioManager.STREAM_SYSTEM, getValue(AudioManager.STREAM_SYSTEM));
    }

    /**
     * Отключить звук
     */
    public void onMute() {
        mute(mAudioManager, AudioManager.STREAM_NOTIFICATION, audioStreams);
        mute(mAudioManager, AudioManager.STREAM_ALARM, audioStreams);
        mute(mAudioManager, AudioManager.STREAM_MUSIC, audioStreams);
        mute(mAudioManager, AudioManager.STREAM_RING, audioStreams);
        mute(mAudioManager, AudioManager.STREAM_SYSTEM, audioStreams);
    }

    /**
     * Включить звук
     */
    public void onUnMute() {
        unMute(mAudioManager, AudioManager.STREAM_NOTIFICATION, audioStreams);
        unMute(mAudioManager, AudioManager.STREAM_ALARM, audioStreams);
        unMute(mAudioManager, AudioManager.STREAM_MUSIC, audioStreams);
        unMute(mAudioManager, AudioManager.STREAM_RING, audioStreams);
        unMute(mAudioManager, AudioManager.STREAM_SYSTEM, audioStreams);
    }

    /**
     * Включить звук у потока
     *
     * @param audioManager
     * @param stream       Идент. потока
     * @param streams      массив потоков для внесения изменений
     */
    private void unMute(AudioManager audioManager, int stream, HashMap<Integer, Boolean> streams) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (streams.get(stream) != null && streams.get(stream)) {
                audioManager.adjustStreamVolume(stream, AudioManager.ADJUST_UNMUTE, 0);
                streams.put(stream, false);
                mSharedPreferences.edit().putBoolean(String.valueOf(stream), false).apply();
            }
        } else {
            // Note that this must be the same instance of audioManager that mutes
            // http://stackoverflow.com/questions/7908962/setstreammute-never-unmutes?rq=1
            audioManager.setStreamMute(stream, false);
        }
    }

    /**
     * Отключение звука у потока
     *
     * @param audioManager
     * @param stream       Идент. потока
     * @param streams      массив потоков для внесения изменений
     */
    private void mute(AudioManager audioManager, int stream, HashMap<Integer, Boolean> streams) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!audioManager.isStreamMute(stream)) {
                streams.put(stream, true);
                mSharedPreferences.edit().putBoolean(String.valueOf(stream), true).apply();
                audioManager.adjustStreamVolume(stream, AudioManager.ADJUST_MUTE, 0);
            }
        } else {
            audioManager.setStreamMute(stream, true);
        }
    }

    private Boolean getValue(int stream) {
        if (mSharedPreferences.contains(String.valueOf(stream))) {
            return mSharedPreferences.getBoolean(String.valueOf(stream), false);
        }
        return null;
    }
}
