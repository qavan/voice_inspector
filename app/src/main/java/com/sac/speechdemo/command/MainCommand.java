package com.sac.speechdemo.command;

import android.util.Log;


public abstract class MainCommand implements IMainCommand {
    private static final String TAG = "MAIN_COMMAND";

    @Override
    public void applyCommand(String textCommands) {
        // тут надо распознавать команды
        Log.i(TAG, textCommands);
    }

}