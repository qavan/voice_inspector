package com.qavan.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static android.speech.SpeechRecognizer.ERROR_AUDIO;
import static android.speech.SpeechRecognizer.ERROR_CLIENT;
import static android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS;
import static android.speech.SpeechRecognizer.ERROR_NETWORK;
import static android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT;
import static android.speech.SpeechRecognizer.ERROR_NO_MATCH;
import static android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY;
import static android.speech.SpeechRecognizer.ERROR_SERVER;
import static android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT;

/**
 * Helper class to easily work with Android speech recognition.
 *
 * @author Sachin Varma
 */
public class Speech {

    private static final String LOG_TAG = Speech.class.getSimpleName();

    private static Speech instance = null;

    private SpeechRecognizer mSpeechRecognizer;
    private String mCallingPackage;
    private boolean mPreferOffline = false;
    private boolean mGetPartialResults = true;
    private SpeechDelegate mDelegate;
    private boolean mIsListening = false;

    private final List<String> mPartialData = new ArrayList<>();
    private String mUnstableData;

    private DelayedOperation mDelayedStopListening;
    private Context mContext;

    private TextToSpeech mTextToSpeech;
    private final Map<String, TextToSpeechProgressListener.TextToSpeechCallback> mTtsCallbacks = new HashMap<>();
    private Locale mLocale = Locale.getDefault();
    private float mTtsRate = 1.7f;
    private float mTtsPitch = 1.0f;
    private int mTtsQueueMode = TextToSpeech.QUEUE_FLUSH;
    private long mStopListeningDelayInMs = 10000;
    private long mTransitionMinimumDelay = 1200;
    private long mLastActionTimestamp;
    private List<String> mLastPartialResults = null;

    private final TextToSpeech.OnInitListener mTtsInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(final int status) {
            switch (status) {
                case TextToSpeech.SUCCESS:
                    Logger.info(LOG_TAG, "TextToSpeech engine successfully started");
                    break;

                case TextToSpeech.ERROR:
                    Logger.error(LOG_TAG, "Error while initializing TextToSpeech engine!");
                    break;

                default:
                    Logger.error(LOG_TAG, "Unknown TextToSpeech status: " + status);
                    break;
            }
        }
    };

    private UtteranceProgressListener mTtsProgressListener;

    private final RecognitionListener mListener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(final Bundle bundle) {
            mPartialData.clear();
            mUnstableData = null;
        }

        @Override
        public void onBeginningOfSpeech() {
            //TODO visual CARD_RECORD_BUTTON detecting(color changing)

            mDelayedStopListening.start(new DelayedOperation.Operation() {
                @Override
                public void onDelayedOperation() {
                    returnPartialResultsAndRecreateSpeechRecognizer();
                    Log.d("ReachedStop", "Stoppong");
                    //  mListenerDelay.onClick("1");
                }

                @Override
                public boolean shouldExecuteDelayedOperation() {
                    return true;
                }
            });
        }

        @Override
        public void onRmsChanged(final float v) {
            try {
                if (mDelegate != null)
                    mDelegate.onSpeechRmsChanged(v);
            } catch (final Throwable exc) {
                Logger.error(Speech.class.getSimpleName(), "Unhandled exception in delegate onSpeechRmsChanged", exc);
            }

            //TODO visual CARD_RECORD_BUTTON detecting
        }

        @Override
        public void onPartialResults(final Bundle bundle) {
            mDelayedStopListening.resetTimer();

            final List<String> partialResults = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            final List<String> unstableData = bundle.getStringArrayList("android.speech.extra.UNSTABLE_TEXT");

            if (partialResults != null && !partialResults.isEmpty()) {
                mPartialData.clear();
                mPartialData.addAll(partialResults);
                mUnstableData = unstableData != null && !unstableData.isEmpty() ? unstableData.get(0) : null;
                try {
                    if (mLastPartialResults == null || !mLastPartialResults.equals(partialResults)) {
                        if (mDelegate != null)
                            mDelegate.onSpeechPartialResults(partialResults);
                        mLastPartialResults = partialResults;
                    }
                } catch (final Throwable exc) {
                    Logger.error(Speech.class.getSimpleName(), "Unhandled exception in delegate onSpeechPartialResults", exc);
                }
            }
        }

        @Override
        public void onResults(final Bundle bundle) {
            mDelayedStopListening.cancel();

            final List<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            final String[] result;

            if (results != null && !results.isEmpty() && results.get(0) != null && !results.get(0).isEmpty()) {
                result = results.toArray(new String[0]);
            } else {
                Logger.info(Speech.class.getSimpleName(), "No speech results, getting partial");
                result = getPartialResultsAsString();
            }

            mIsListening = false;

            try {
                if (mDelegate != null)
                    mDelegate.onSpeechResult(results.toArray(new String[0]));
            } catch (final Throwable exc) {
                Logger.error(Speech.class.getSimpleName(), "Unhandled exception in delegate onSpeechResult", exc);
            }

            //TODO visual CARD_RECORD_BUTTON error

            initSpeechRecognizer(mContext);
        }

        @Override
        public void onError(final int code) {
            switch (code) {
                case ERROR_AUDIO: {
                    Logger.error(LOG_TAG, "Audio recording error.", new SpeechRecognitionException(code));
                    Toast.makeText(mContext, "Audio recording error", Toast.LENGTH_SHORT).show();
                }
                case ERROR_CLIENT: {
                    Logger.error(LOG_TAG, "Other client side errors.", new SpeechRecognitionException(code));
//                    Toast.makeText(mContext, "Other client side errors", Toast.LENGTH_SHORT).show();
                }
                case ERROR_INSUFFICIENT_PERMISSIONS: {
                    Logger.error(LOG_TAG, "Insufficient permissions", new SpeechRecognitionException(code));
//                    Toast.makeText(mContext, "Insufficient permissions", Toast.LENGTH_SHORT).show();
                }
                case ERROR_NETWORK: {
                    Logger.error(LOG_TAG, "Other network related errors.", new SpeechRecognitionException(code));
//                    Toast.makeText(mContext, "Other network related errors", Toast.LENGTH_SHORT).show();
                }
                case ERROR_NETWORK_TIMEOUT: {
                    Logger.error(LOG_TAG, "Network operation timed out.", new SpeechRecognitionException(code));
//                    Toast.makeText(mContext, "Network operation timed out", Toast.LENGTH_SHORT).show();
                }
                case ERROR_NO_MATCH: {
                    Logger.error(LOG_TAG, "No recognition result matched.", new SpeechRecognitionException(code));
//                    Toast.makeText(mContext, "No recognition result matched", Toast.LENGTH_SHORT).show();
                }
                case ERROR_RECOGNIZER_BUSY: {
                    Logger.error(LOG_TAG, "RecognitionService busy.", new SpeechRecognitionException(code));
//                    Toast.makeText(mContext, "RecognitionService busy", Toast.LENGTH_SHORT).show();
                }
                case ERROR_SERVER: {
                    Logger.error(LOG_TAG, "Server sends error status.", new SpeechRecognitionException(code));
//                    Toast.makeText(mContext, "Server sends error status", Toast.LENGTH_SHORT).show();
                }
                case ERROR_SPEECH_TIMEOUT: {
                    Logger.error(LOG_TAG, "No speech input.", new SpeechRecognitionException(code));
//                    Toast.makeText(mContext, "No speech input", Toast.LENGTH_SHORT).show();
                }
            }
            returnPartialResultsAndRecreateSpeechRecognizer();
        }

        @Override
        public void onBufferReceived(final byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            //TODO visual CARD_RECORD_BUTTON recognized
        }

        @Override
        public void onEvent(final int i, final Bundle bundle) {

        }
    };

    private Speech(final Context context) {
        initSpeechRecognizer(context);
        initTts(context);
    }

    private Speech(final Context context, final String callingPackage) {
        initSpeechRecognizer(context);
        initTts(context);
        mCallingPackage = callingPackage;
    }

    private void initSpeechRecognizer(final Context context) {
        if (context == null)
            throw new IllegalArgumentException("context must be defined!");

        mContext = context;

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            if (mSpeechRecognizer != null) {
                try {
                    mSpeechRecognizer.destroy();
                } catch (final Throwable exc) {
                    Logger.debug(Speech.class.getSimpleName(), "Non-Fatal error while destroying speech. " + exc.getMessage());
                } finally {
                    mSpeechRecognizer = null;
                }
            }

            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            mSpeechRecognizer.setRecognitionListener(mListener);
            initDelayedStopListening(context);

        } else {
            mSpeechRecognizer = null;
        }

        mPartialData.clear();
        mUnstableData = null;
    }

    private void initTts(final Context context) {
        if (mTextToSpeech == null) {
            mTtsProgressListener = new TextToSpeechProgressListener(mContext, mTtsCallbacks);
            mTextToSpeech = new TextToSpeech(context.getApplicationContext(), mTtsInitListener);
            mTextToSpeech.setOnUtteranceProgressListener(mTtsProgressListener);
            mTextToSpeech.setLanguage(mLocale);
            mTextToSpeech.setPitch(mTtsPitch);
            mTextToSpeech.setSpeechRate(mTtsRate);
        }
    }

    private void initDelayedStopListening(final Context context) {
        if (mDelayedStopListening != null) {
            mDelayedStopListening.cancel();
            mDelayedStopListening = null;
        }
//        Toast.makeText(context, "destroyed", Toast.LENGTH_SHORT).show();
        if (mListenerDelay != null) {
            mListenerDelay.onSpecifiedCommandPronounced("1");
        }
        mDelayedStopListening = new DelayedOperation(context, "delayStopListening", mStopListeningDelayInMs);
    }

    /**
     * Initializes speech recognition.
     *
     * @param context application context
     * @return speech instance
     */
    public static Speech init(final Context context) {
        if (instance == null) {
            instance = new Speech(context);
        }

        return instance;
    }

    /**
     * Initializes speech recognition.
     *
     * @param context        application context
     * @param callingPackage The extra key used in an intent to the speech recognizer for
     *                       CARD_RECORD_BUTTON search. Not generally to be used by developers.
     *                       The system search dialog uses this, for example, to set a calling
     *                       package for identification by a CARD_RECORD_BUTTON search API.
     *                       If this extra is set by anyone but the system process,
     *                       it should be overridden by the CARD_RECORD_BUTTON search implementation.
     *                       By passing null or empty string (which is the default) you are
     *                       not overriding the calling package
     * @return speech instance
     */
    public static Speech init(final Context context, final String callingPackage) {
        if (instance == null) {
            instance = new Speech(context, callingPackage);
        }

        return instance;
    }

    /**
     * Must be called inside Activity's onDestroy.
     */
    public synchronized void shutdown() {
        if (mSpeechRecognizer != null) {
            try {
                mSpeechRecognizer.stopListening();
            } catch (final Exception exc) {
                Logger.error(getClass().getSimpleName(), "Warning while de-initing speech recognizer", exc);
            }
        }

        if (mTextToSpeech != null) {
            try {
                mTtsCallbacks.clear();
                mTextToSpeech.stop();
                mTextToSpeech.shutdown();
            } catch (final Exception exc) {
                Logger.error(getClass().getSimpleName(), "Warning while de-initing text to speech", exc);
            }
        }

        unregisterDelegate();
        instance = null;
    }

    /**
     * Gets speech recognition instance.
     *
     * @return SpeechRecognition instance
     */
    public static Speech getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Speech recognition has not been initialized! call init method first!");
        }

        return instance;
    }

    /**
     * Starts CARD_RECORD_BUTTON recognition.
     *
     * @param delegate delegate which will receive speech recognition events and status
     * @throws SimpleException.SpeechRecognitionNotAvailable      when speech recognition is not available on the device
     * @throws SimpleException.GoogleVoiceTypingDisabledException when google CARD_RECORD_BUTTON typing is disabled on the device
     */
    public void startListening(final SpeechDelegate delegate) throws SimpleException.SpeechRecognitionNotAvailable, SimpleException.GoogleVoiceTypingDisabledException {
        startListening(null, delegate);
    }

    /**
     * Starts CARD_RECORD_BUTTON recognition.
     *
     * @param progressView view in which to draw speech animation
     * @param delegate     delegate which will receive speech recognition events and status
     * @throws SimpleException.SpeechRecognitionNotAvailable      when speech recognition is not available on the device
     * @throws SimpleException.GoogleVoiceTypingDisabledException when google CARD_RECORD_BUTTON typing is disabled on the device
     */
    public void startListening(final String progressView, final SpeechDelegate delegate)
            throws SimpleException.SpeechRecognitionNotAvailable, SimpleException.GoogleVoiceTypingDisabledException {
        if (mIsListening) return;

        if (mSpeechRecognizer == null)
            throw new SimpleException.SpeechRecognitionNotAvailable();

        if (delegate == null)
            throw new IllegalArgumentException("delegate must be defined!");

        if (throttleAction()) {
            Logger.debug(getClass().getSimpleName(), "Hey man calm down! Throttling start to prevent disaster!");
            return;
        }
//
//        if (progressView != null && !(progressView.getParent() instanceof LinearLayout))
//            throw new IllegalArgumentException("progressView must be put inside a LinearLayout!");
//
//        mProgressView = progressView;
        mDelegate = delegate;

        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, mGetPartialResults)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, mLocale.getLanguage())
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        if (mCallingPackage != null && !mCallingPackage.isEmpty()) {
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mCallingPackage);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, mPreferOffline);
        }

        try {
            mSpeechRecognizer.startListening(intent);
        } catch (final SecurityException exc) {
            throw new SimpleException.GoogleVoiceTypingDisabledException();
        }

        mIsListening = true;
        updateLastActionTimestamp();

        try {
            if (mDelegate != null)
                mDelegate.onStartOfSpeech();
        } catch (final Throwable exc) {
            Logger.error(Speech.class.getSimpleName(), "Unhandled exception in delegate onStartOfSpeech", exc);
        }

    }

    private void unregisterDelegate() {
        mDelegate = null;
    }

    private void updateLastActionTimestamp() {
        mLastActionTimestamp = new Date().getTime();
    }

    private boolean throttleAction() {
        return (new Date().getTime() <= (mLastActionTimestamp + mTransitionMinimumDelay));
    }

    /**
     * Stops CARD_RECORD_BUTTON recognition listening.
     * This method does nothing if CARD_RECORD_BUTTON listening is not active
     */
    public void stopListening() {
        if (!mIsListening) return;

        if (throttleAction()) {
            Logger.debug(getClass().getSimpleName(), "Hey man calm down! Throttling stop to prevent disaster!");
            return;
        }

        mIsListening = false;
        updateLastActionTimestamp();
        returnPartialResultsAndRecreateSpeechRecognizer();
    }

    private String[] getPartialResultsAsString() {
//        final StringBuilder out = new StringBuilder("");
        final String[] out = mPartialData.toArray(new String[0]);
//
//        if (mUnstableData != null && !mUnstableData.isEmpty())
//            out.append(mUnstableData);

        return out;
    }

    private void returnPartialResultsAndRecreateSpeechRecognizer() {
        mIsListening = false;
        try {
            if (mDelegate != null)
                mDelegate.onSpeechResult(getPartialResultsAsString());
        } catch (final Throwable exc) {
            Logger.error(Speech.class.getSimpleName(), "Unhandled exception in delegate onSpeechResult", exc);
        }

//        if (mProgressView != null)
//            mProgressView.onResultOrOnError();

        // recreate the speech recognizer
        initSpeechRecognizer(mContext);
    }

    /**
     * Check if CARD_RECORD_BUTTON recognition is currently active.
     *
     * @return true if the CARD_RECORD_BUTTON recognition is on, false otherwise
     */
    public boolean isListening() {
        return mIsListening;
    }

    /**
     * Uses text to speech to transform a written message into a sound.
     *
     * @param message message to play
     */
    public void say(final String message) {
        say(message, null);
    }

    /**
     * Uses text to speech to transform a written message into a sound.
     *
     * @param message  message to play
     * @param callback callback which will receive progress status of the operation
     */
    public void say(final String message, final TextToSpeechProgressListener.TextToSpeechCallback callback) {

        final String utteranceId = UUID.randomUUID().toString();

        if (callback != null) {
            mTtsCallbacks.put(utteranceId, callback);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextToSpeech.speak(message, mTtsQueueMode, null, utteranceId);
        } else {
            final HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            mTextToSpeech.speak(message, mTtsQueueMode, params);
        }
    }

    /**
     * Stops text to speech.
     */
    public void stopTextToSpeech() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
        }
    }

    /**
     * Set whether to only use an offline speech recognition engine.
     * The default is false, meaning that either network or offline recognition engines may be used.
     *
     * @param preferOffline true to prefer offline engine, false to use either one of the two
     * @return speech instance
     */
    public Speech setPreferOffline(final boolean preferOffline) {
        mPreferOffline = preferOffline;
        return this;
    }

    /**
     * Set whether partial results should be returned by the recognizer as the user speaks
     * (default is true). The server may ignore a request for partial results in some or all cases.
     *
     * @param getPartialResults true to get also partial recognition results, false otherwise
     * @return speech instance
     */
    public Speech setGetPartialResults(final boolean getPartialResults) {
        mGetPartialResults = getPartialResults;
        return this;
    }

    /**
     * Sets text to speech and recognition language.
     * Defaults to device language setting.
     *
     * @param locale new locale
     * @return speech instance
     */
    public Speech setLocale(final Locale locale) {
        mLocale = locale;
        if (mTextToSpeech != null)
            mTextToSpeech.setLanguage(locale);
        return this;
    }

    /**
     * Sets the speech rate. This has no effect on any pre-recorded speech.
     *
     * @param rate Speech rate. 1.0 is the normal speech rate, lower values slow down the speech
     *             (0.5 is half the normal speech rate), greater values accelerate it
     *             (2.0 is twice the normal speech rate).
     * @return speech instance
     */
    public Speech setTextToSpeechRate(final float rate) {
        mTtsRate = rate;
        mTextToSpeech.setSpeechRate(rate);
        return this;
    }

    /**
     * Sets the speech pitch for the TextToSpeech engine.
     * This has no effect on any pre-recorded speech.
     *
     * @param pitch Speech pitch. 1.0 is the normal pitch, lower values lower the tone of the
     *              synthesized CARD_RECORD_BUTTON, greater values increase it.
     * @return speech instance
     */
    public Speech setTextToSpeechPitch(final float pitch) {
        mTtsPitch = pitch;
        mTextToSpeech.setPitch(pitch);
        return this;
    }

    /**
     * Sets the idle timeout after which the listening will be automatically stopped.
     *
     * @param milliseconds timeout in milliseconds
     * @return speech instance
     */
    public Speech setStopListeningAfterInactivity(final long milliseconds) {
        mStopListeningDelayInMs = milliseconds;
        initDelayedStopListening(mContext);
        return this;
    }

    /**
     * Sets the minimum interval between start/stop events. This is useful to prevent
     * monkey input from users.
     *
     * @param milliseconds minimum interval betweeb state change in milliseconds
     * @return speech instance
     */
    public Speech setTransitionMinimumDelay(final long milliseconds) {
        mTransitionMinimumDelay = milliseconds;
        return this;
    }

    /**
     * Sets the text to speech queue mode.
     * By default is TextToSpeech.QUEUE_FLUSH, which is faster, because it clears all the
     * messages before speaking the new one. TextToSpeech.QUEUE_ADD adds the last message
     * to speak in the queue, without clearing the messages that have been added.
     *
     * @param mode It can be either TextToSpeech.QUEUE_ADD or TextToSpeech.QUEUE_FLUSH.
     * @return speech instance
     */
    public Speech setTextToSpeechQueueMode(final int mode) {
        mTtsQueueMode = mode;
        return this;
    }

    private Speech.stopDueToDelay mListenerDelay;

    // define listener
    public interface stopDueToDelay {
        void onSpecifiedCommandPronounced(final String event);
    }

    // set the listener. Must be called from the fragment
    public void setListener(Speech.stopDueToDelay listener) {
        this.mListenerDelay = listener;
    }

}
