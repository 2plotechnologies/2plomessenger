package com.twoploapps.a2plomessenger;
import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import java.io.IOException;
public class AudioActivity extends AppCompatActivity  implements View.OnClickListener{
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        String audioUrl = getIntent().getStringExtra("audio_url");
        ImageButton playButton = findViewById(R.id.play_button);
        ImageButton pauseButton = findViewById(R.id.pause_button);
        ImageButton stopButton = findViewById(R.id.stop_button);
        ImageButton prev = findViewById(R.id.prev_button);
        ImageButton nex = findViewById(R.id.next_button);
        playButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        prev.setOnClickListener(this);
        nex.setOnClickListener(this);
        seekBar = findViewById(R.id.seekbar);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(mp -> {
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration());
            updateSeekBar();
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Pausar la reproducción del audio mientras se arrastra el seekbar
                mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Establecer la posición del audio en función de la posición actual del seekbar
                mediaPlayer.seekTo(seekBar.getProgress());
                mediaPlayer.start();
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_button:
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                break;
            case R.id.pause_button:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                break;
            case R.id.stop_button:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    try {
                        mediaPlayer.prepare();
                        mediaPlayer.seekTo(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.prev_button:
                if (mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    if (currentPosition - 5000 > 0) {
                        mediaPlayer.seekTo(currentPosition - 5000);
                    } else {
                        mediaPlayer.seekTo(0);
                    }
                }
                break;
            case R.id.next_button:
                if (mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();
                    if (currentPosition + 5000 < duration) {
                        mediaPlayer.seekTo(currentPosition + 5000);
                    } else {
                        mediaPlayer.seekTo(duration);
                    }
                }
                break;
        }
    }
    private void updateSeekBar() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                }
            };
            handler.postDelayed(runnable, 1000);
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