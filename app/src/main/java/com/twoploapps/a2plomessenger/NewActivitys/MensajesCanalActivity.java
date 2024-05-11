package com.twoploapps.a2plomessenger.NewActivitys;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;
import com.twoploapps.a2plomessenger.Controllers.ChannelController;
import com.twoploapps.a2plomessenger.Models.Canal;
import com.twoploapps.a2plomessenger.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MensajesCanalActivity extends AppCompatActivity {
    private TextView nombrecanal;
    private CircleImageView canal_imagen;
    private DatabaseReference RootRef,NotificacionesRef;
    private Canal canal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajes_canal);
        String id = getIntent().getStringExtra("channel_id");
        //Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
        Toolbar toolbar = findViewById(R.id.channel_chat_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_chat_bar, null);
        actionBar.setCustomView(view);

        nombrecanal = findViewById(R.id.usuario_nombre);
        canal_imagen = findViewById(R.id.usuario_imagen);
        TextView ultimaconexion = findViewById(R.id.usuario_conexion);

        ChannelController.getData(id, new ChannelController.OnChannelDataReceived() {
            @Override
            public void onDataReceived(Canal canal) {
                if (canal != null) {
                    nombrecanal.setText(canal.getNombre());
                    ultimaconexion.setText(getString(R.string.canalpublico));
                    Picasso.get().load(canal.getImagen()).into(canal_imagen);
                }
            }
        });
    }
}
