package com.qavan.voice_inspector;


import android.app.Application;

import com.qavan.speech.Logger;
import com.qavan.speech.Speech;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Speech.init(this, getPackageName());
        Logger.setLogLevel(Logger.LogLevel.DEBUG);
    }
}
