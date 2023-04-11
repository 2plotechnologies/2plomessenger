package com.twoploapps.a2plomessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

public class LlamadaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamada);
        String idllamada = getIntent().getStringExtra("codigo");
        String meetingUrl = "https://meet.jit.si/" + idllamada;
        JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                .setRoom(meetingUrl)
                .setFeatureFlag("welcomepage.enabled", false)
                .build();
        JitsiMeetActivity.launch(this,options);
    }
}