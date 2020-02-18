package com.qavan.voice_inspector.command;

public interface IActivateCommand extends ICommand {
    /**
     * Активация прослушивания
     */
    void onActivated();

    /**
     * Деактивация прослушивания
     */
    void onDeactivated();
}
