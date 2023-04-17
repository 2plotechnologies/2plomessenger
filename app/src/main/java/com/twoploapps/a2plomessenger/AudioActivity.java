package com.twoploapps.a2plomessenger;
import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import java.io.IOException;
public class AudioActivity extends AppCompatActivity  implements View.OnClickListener{
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        String audioUrl = getIntent().getStringExtra("audio_url");
        ImageButton playButton = findViewById(R.id.play_button);
        ImageButton pauseButton = findViewById(R.id.pause_button);
        ImageButton stopButton = findViewById(R.id.stop_button);
        playButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        seekBar = findViewById(R.id.seekbar);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.setOnPreparedListener(mp -> {
            mediaPlayer.start();
            isPlaying = true;
            new Thread(() -> {
                while (isPlaying) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
            }).start();
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_button:
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    isPlaying = true;
                }
                break;
            case R.id.pause_button:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isPlaying = false;
                }
                break;
            case R.id.stop_button:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    isPlaying = false;
                    try {
                        mediaPlayer.prepare();
                        mediaPlayer.seekTo(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}