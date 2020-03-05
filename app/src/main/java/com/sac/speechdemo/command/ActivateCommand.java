package com.sac.speechdemo.command;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.example.user.speechrecognizationasservice.R;
import com.sac.speech.GoogleVoiceTypingDisabledException;
import com.sac.speech.Speech;
import com.sac.speech.SpeechDelegate;
import com.sac.speech.SpeechRecognitionNotAvailable;

import java.util.Locale;

public abstract class ActivateCommand implements IActivateCommand, SpeechDelegate, Speech.stopDueToDelay {
    private static final String TAG = "ACTIVATE_COMMAND";
    private boolean mIsActivated = false;
    private Context mContext;
    private SpeechDelegate mSpeechDelegate;

    private String COMMAND_ACTIVATE;
    private String COMMAND_DEACTIVATE;

    public ActivateCommand(Context context) {
        mContext = context;

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
        } catch (SpeechRecognitionNotAvailable | GoogleVoiceTypingDisabledException speechRecognitionNotAvailable) {
            speechRecognitionNotAvailable.printStackTrace();
        }
    }

    @Override
    public void applyCommand(String textCommands) throws SpeechRecognitionNotAvailable, GoogleVoiceTypingDisabledException {
        String command = textCommands.toLowerCase();
        Log.i(TAG, command);
        if (command.contains(COMMAND_ACTIVATE)) {
            Log.i(TAG, "Activated");
            activate();
        } else if (command.contains(COMMAND_DEACTIVATE)) {
            Log.i(TAG, "Deactivated");
            deActivate();
        } else {
            // если распознать не удалось, то onErrorCommand
            onErrorCommand(ICommand.COMMAND_ACTIVATE, textCommands);
        }
    }

    private void activate() throws SpeechRecognitionNotAvailable, GoogleVoiceTypingDisabledException {
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
