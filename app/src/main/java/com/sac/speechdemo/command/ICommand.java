package com.sac.speechdemo.command;

import com.sac.speech.GoogleVoiceTypingDisabledException;
import com.sac.speech.SpeechRecognitionNotAvailable;

public interface ICommand {
    /**
     * команды активации
     */
    int COMMAND_ACTIVATE = 0;

    /**
     * основные команды
     */
    int COMMAND_MAIN = 1;

    /**
     * Устанка команды
     *
     * @param textCommands команды
     */
    void applyCommand(String textCommands) throws SpeechRecognitionNotAvailable, GoogleVoiceTypingDisabledException;

    /**
     * Обработчик ошибок распознования
     *
     * @param type         тип команд, COMMAND_ACTIVATE, COMMAND_MAIN
     * @param textCommands команды
     */
    void onErrorCommand(int type, String textCommands);
}
