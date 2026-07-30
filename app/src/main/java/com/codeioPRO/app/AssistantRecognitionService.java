package com.codeioPRO.app;

import android.speech.RecognitionService;
import android.os.Bundle;

/**
 * Stub recognition service — Code-ioPRO delegates to system STT.
 */
public class AssistantRecognitionService extends RecognitionService {

    @Override
    protected void onStartListening(android.content.Intent recognizerIntent, Callback listener) {
        // Delegate to system recognizer — this stub satisfies the manifest entry
    }

    @Override
    protected void onCancel(Callback listener) {}

    @Override
    protected void onStopListening(Callback listener) {}
}
