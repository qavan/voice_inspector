package com.sac.speechdemo;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.sac.speech.Logger;
import com.sac.speech.Speech;
import com.sac.speechdemo.util.AudioUtil;

import java.util.ArrayList;
import java.util.List;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Speech.init(this, getPackageName());
        Logger.setLogLevel(Logger.LogLevel.DEBUG);
    }
}
