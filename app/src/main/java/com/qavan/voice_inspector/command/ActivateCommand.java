package com.qavan.voice_inspector.command;

public abstract class ActivateCommand implements IActivateCommand {

    private boolean mIsActivated = false;

    private final String COMMAND_ACTIVATE = "activate";
    private final String COMMAND_DEACTIVATE = "deactivate";

    public ActivateCommand() {
        // Нужно инициализировать службу speech
    }

    public boolean isActivated() {
        return mIsActivated;
    }

    @Override
    public void applyCommand(String[] textCommands) {
        // тут нужно обработать textCommands и вызвать либо onActivate, либо onDeactivate
        String command = textCommands[0].toLowerCase();

        if (command.equals(COMMAND_ACTIVATE)) {
            activate();
        } else if (command.equals(COMMAND_DEACTIVATE)) {
            deActivate();
        } else {
            // если распознать не удалось, то onErrorCommand
            onErrorCommand(ICommand.COMMAND_ACTIVATE, textCommands);
        }
    }

    private void activate() {
        onActivated();
        mIsActivated = true;
    }

    private void deActivate() {
        onDeactivated();
        mIsActivated = false;
    }

    public void destroy() {
        if (mIsActivated) {
            deActivate();
        }
        // тут выполняем еще что-то
    }
}
