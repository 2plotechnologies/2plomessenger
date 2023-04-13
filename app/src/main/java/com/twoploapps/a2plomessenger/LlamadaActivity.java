package com.twoploapps.a2plomessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.MalformedURLException;
import java.net.URL;

public class LlamadaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamada);
        Button btnvolver = findViewById(R.id.btnvolverainicio);
        String idllamada = getIntent().getStringExtra("codigo");
        String username = getIntent().getStringExtra("username");
        String imgurl = getIntent().getStringExtra("img");
        try{
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setFeatureFlag("welcomepage.enabled", false)
                    .build();
            JitsiMeet.setDefaultConferenceOptions(options);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
        userInfo.setDisplayName(username);
        try {
            userInfo.setAvatar(new URL(imgurl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        JitsiMeetConferenceOptions options
                = new JitsiMeetConferenceOptions.Builder()
                .setRoom(idllamada)
                .setAudioMuted(true)
                .setVideoMuted(true)
                .setUserInfo(userInfo)
                .build();
        JitsiMeetActivity.launch(this, options);
        btnvolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LlamadaActivity.this,InicioActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}