package com.qavan.voice_inspector.command;

import com.qavan.speech.SimpleException;

public interface IActivateCommand extends ICommand {
    /**
     * Активация прослушивания
     */
    void onActivated() throws SimpleException.SpeechRecognitionNotAvailable, SimpleException.GoogleVoiceTypingDisabledException;

    /**
     * Деактивация прослушивания
     */
    void onDeactivated();
}
