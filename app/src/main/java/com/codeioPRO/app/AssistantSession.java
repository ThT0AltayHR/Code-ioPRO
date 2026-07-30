package com.codeioPRO.app;

import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;

public class AssistantSession extends VoiceInteractionSession {

    public AssistantSession(android.content.Context context, android.os.Handler handler) {
        super(context, handler);
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.setAction(Intent.ACTION_ASSIST);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        getContext().startActivity(intent);
        hide();
    }
}
