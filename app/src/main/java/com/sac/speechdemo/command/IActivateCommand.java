package com.sac.speechdemo.command;

import com.sac.speech.GoogleVoiceTypingDisabledException;
import com.sac.speech.SpeechRecognitionNotAvailable;

public interface IActivateCommand extends ICommand {
    /**
     * Активация прослушивания
     */
    void onActivated() throws SpeechRecognitionNotAvailable, GoogleVoiceTypingDisabledException;

    /**
     * Деактивация прослушивания
     */
    void onDeactivated();
}