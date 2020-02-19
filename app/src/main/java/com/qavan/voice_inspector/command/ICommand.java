package com.qavan.voice_inspector.command;

import com.qavan.speech.SimpleException;

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
    void applyCommand(String[] textCommands) throws SimpleException.SpeechRecognitionNotAvailable, SimpleException.GoogleVoiceTypingDisabledException;

    /**
     * Обработчик ошибок распознования
     *
     * @param type         тип команд, COMMAND_ACTIVATE, COMMAND_MAIN
     * @param textCommands команды
     */
    void onErrorCommand(int type, String[] textCommands);
}
