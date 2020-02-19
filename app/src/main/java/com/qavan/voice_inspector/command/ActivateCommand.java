package com.qavan.voice_inspector.command;


import android.content.Context;
import android.media.AudioManager;

import com.qavan.speech.SimpleException;
import com.qavan.speech.Speech;
import com.qavan.speech.SpeechDelegate;
import com.qavan.voice_inspector.R;

import java.util.Locale;


public abstract class ActivateCommand implements IActivateCommand, SpeechDelegate, Speech.stopDueToDelay {
    private boolean mIsActivated = false;
    private Context mContext;
    private SpeechDelegate mSpeechDelegate;
    private AudioManager amAudioManager;

    private String COMMAND_ACTIVATE;
    private String COMMAND_DEACTIVATE;

    public ActivateCommand(Context context) {
        mContext = context;

        amAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        COMMAND_ACTIVATE = mContext.getResources().getString(R.string.KEY_WORD_ACTIVATION);
        COMMAND_DEACTIVATE = mContext.getResources().getString(R.string.KEY_WORD_DEACTIVATION);

        Speech.init(mContext);

        Speech.getInstance().setTextToSpeechRate(1.7f);
        Speech.getInstance().setLocale(Locale.getDefault());
        Speech.getInstance().setGetPartialResults(false);

        mSpeechDelegate = this;

        Speech.getInstance().setListener(this);

        if (Speech.getInstance().isListening()) {
            Speech.getInstance().stopListening();
//            muteBeepSoundOfRecorder();
        } else {
            System.setProperty("rx.unsafe-disable", "True");
//            RxPermissions.getInstance(mContext).request(Manifest.permission.RECORD_AUDIO).subscribe(BackgroundRecognizerService.checkMicrophonePermissionVoidUtil);
//            muteBeepSoundOfRecorder();
        }
        try {
            Speech.getInstance().startListening(mSpeechDelegate);
        } catch (SimpleException.SpeechRecognitionNotAvailable | SimpleException.GoogleVoiceTypingDisabledException speechRecognitionNotAvailable) {
            speechRecognitionNotAvailable.printStackTrace();
        }
    }

    @Override
    public void applyCommand(String[] textCommands) throws SimpleException.SpeechRecognitionNotAvailable, SimpleException.GoogleVoiceTypingDisabledException {
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

    private void activate() throws SimpleException.SpeechRecognitionNotAvailable, SimpleException.GoogleVoiceTypingDisabledException {
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

    @Override
    public abstract void onSpecifiedCommandPronounced(String event);

    public boolean isActivated() {
        return mIsActivated;
    }

    public Context getContext() {
        return mContext;
    }

    public SpeechDelegate getDelegate() {
        return mSpeechDelegate;
    }
}
