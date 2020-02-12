package com.qavan.speech;

public interface TextToSpeechCallback {
    void onStart();

    void onCompleted();

    void onError();
}
