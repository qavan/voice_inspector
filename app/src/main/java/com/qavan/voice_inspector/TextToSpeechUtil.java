package com.qavan.voice_inspector;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

public class TextToSpeechUtil {
    private TextToSpeech mTextToSpeech;
    private TextToSpeechListener mTextToSpeechListener;
    public static final int TEXT_TO_SPEECH = 0;

    public TextToSpeechUtil(Context context, Float speechRate) {
        if (context instanceof TextToSpeechListener) {
            mTextToSpeechListener = (TextToSpeechListener) context;
        }
        mTextToSpeech = new TextToSpeech(context, null);
        mTextToSpeech.setSpeechRate(speechRate);
        mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                am.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }

            @Override
            public void onError(String utteranceId) {

            }

            @Override
            public void onStart(String utteranceId) {
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                am.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
        });
    }

    public void onResume() {
        if (this.mTextToSpeech.getEngines().size() == 0) {
            if (Locale.getDefault() == Locale.ENGLISH) {
                onError("No TextToSpeechUtil on device!");
            } else {
                onError("На устройстве отсутвует приложение с TextToSpeechUtil!");
            }
        } else {
            this.mTextToSpeech.setLanguage(Locale.getDefault());
        }
    }

    public void onPause() {
        this.mTextToSpeech.shutdown();
    }

    public void speak(String inputText) {
        this.mTextToSpeech.speak(inputText, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void onError(String message) {
        if (mTextToSpeechListener != null) {
            mTextToSpeechListener.onError(message, TEXT_TO_SPEECH);
        }
    }

    public void setOnUtteranceProgressListener(UtteranceProgressListener utteranceProgressListener) {
        if (utteranceProgressListener != null) {
            mTextToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
        }
    }

    public interface TextToSpeechListener {
        void onError(String message, int code);
    }
}
