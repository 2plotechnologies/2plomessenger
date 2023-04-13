package com.twoploapps.a2plomessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;

public class LlamadaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamada);
        String idllamada = getIntent().getStringExtra("codigo");
        try{
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setFeatureFlag("welcomepage.enabled", false)
                    .build();
            JitsiMeet.setDefaultConferenceOptions(options);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        JitsiMeetConferenceOptions options
                = new JitsiMeetConferenceOptions.Builder()
                .setRoom(idllamada)
                .setAudioMuted(true)
                .setVideoMuted(true)
                .build();
        JitsiMeetActivity.launch(this, options);
    }
}