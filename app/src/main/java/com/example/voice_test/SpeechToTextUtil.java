package com.example.voice_test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.List;

public class SpeechToTextUtil implements RecognitionListener {
    private static final String TAG = "SPEECH_TO_TEXT";
    public static final int SPEECH_TO_TEXT = 1;

    private SpeechRecognizer recognizer;
    private List<String> mResults;
    private SpeechToTextListener mSpeechToTextListener;

    public SpeechToTextUtil(Context context) {
        if (context instanceof SpeechToTextListener) {
            mSpeechToTextListener = (SpeechToTextListener) context;
        }

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        }
    }

    public void onResume(Intent intent) {
        if (recognizer != null) {
            recognizer.startListening(intent);
            recognizer.setRecognitionListener(this);
        } else {
            onError("Ошибка SpeechRecognizer");
        }
    }

    public void onStart(Intent intent) {
        recognizer.startListening(intent);
    }

    public void onPause() {
        recognizer.stopListening();
    }

    private void onResult(String[] results) {
        if (mSpeechToTextListener != null) {
            mSpeechToTextListener.onResult(results);
        }
    }

    private void onError(String message) {
        if (mSpeechToTextListener != null) {
            mSpeechToTextListener.onError(message, SPEECH_TO_TEXT);
        }
    }

    public void onReadyForSpeech(Bundle params) {
        Log.i(TAG, "onReadyForSpeech");
    }

    public void onBeginningOfSpeech() {
        Log.i(TAG, "onBeginningOfSpeech");
    }

    public void onRmsChanged(float rmsdB) {
        Log.i(TAG, "onRmsChanged " + rmsdB);
    }

    public void onBufferReceived(byte[] buffer) {
        Log.i(TAG, "onBufferReceived");
    }

    public void onEndOfSpeech() {
        Log.i(TAG, "onEndOfSpeech");
    }

    public void onError(int error) {
        mSpeechToTextListener.onError("ERROR " + error, 1);
    }

    public void onResults(Bundle results) {
        mResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.i(TAG, String.format("Result: %s", mResults));
        mSpeechToTextListener.onResult(mResults.toArray(new String[0]));
    }

    public void onPartialResults(Bundle partialResults) {
        Log.i(TAG, "onPartialResults");
    }

    public void onEvent(int eventType, Bundle params) {
        Log.i(TAG, String.format("onEvent %d %s", eventType, params));
    }

    public interface SpeechToTextListener {
        void onResult(String[] results);

        void onError(String message, int code);
    }
}