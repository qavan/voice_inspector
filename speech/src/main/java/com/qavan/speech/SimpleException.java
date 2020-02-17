package com.qavan.speech;

public class SimpleException extends Exception {
    public static class GoogleVoiceTypingDisabledException extends Exception {
        public GoogleVoiceTypingDisabledException() {
            super("Google CARD_RECORD_BUTTON typing must be enabled");
        }
    }

    public static class SpeechRecognitionNotAvailable extends Exception {
        public SpeechRecognitionNotAvailable() {
            super("Speech recognition not available");
        }
    }
}
