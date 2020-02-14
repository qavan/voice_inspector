package com.qavan.speech;

import java.util.List;


public interface SpeechDelegate {
    void onStartOfSpeech();

    void onSpeechRmsChanged(float value);

    void onSpeechPartialResults(List<String> results);

    void onSpeechResult(String result);
}
