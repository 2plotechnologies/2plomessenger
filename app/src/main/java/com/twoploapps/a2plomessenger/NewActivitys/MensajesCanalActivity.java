package com.twoploapps.a2plomessenger.NewActivitys;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.twoploapps.a2plomessenger.R;

public class MensajesCanalActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajes_canal);
        String id = getIntent().getStringExtra("channel_id");
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }
}
