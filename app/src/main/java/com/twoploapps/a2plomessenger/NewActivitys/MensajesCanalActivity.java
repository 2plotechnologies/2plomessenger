package com.twoploapps.a2plomessenger.NewActivitys;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.twoploapps.a2plomessenger.Controllers.ChannelController;
import com.twoploapps.a2plomessenger.Models.Canal;
import com.twoploapps.a2plomessenger.Models.MensajeCanal;
import com.twoploapps.a2plomessenger.NewAdapters.RV_Adapters.ChannelMsgAdapter;
import com.twoploapps.a2plomessenger.R;
import com.vanniktech.emoji.EmojiEditText;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MensajesCanalActivity extends AppCompatActivity {
    private TextView nombrecanal;
    private CircleImageView canal_imagen;
    private EmojiEditText mensaje;
    private ImageView botonarchivo;
    private ImageView emojiboton;
    private RecyclerView rv_mensajes_canal;
    private LinearLayout EnviarMensajes;
    private DatabaseReference RootRef,NotificacionesRef;
    private Canal canal;
    private String id, CurrentUserId;
    private List<MensajeCanal> mensajeCanalList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajes_canal);
        id = getIntent().getStringExtra("channel_id");
        //Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
        Toolbar toolbar = findViewById(R.id.channel_chat_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.layout_chat_bar, null);
            actionBar.setCustomView(view);
        }

        RootRef = FirebaseDatabase.getInstance().getReference();
        CurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mensajeCanalList = new ArrayList<>();

        nombrecanal = findViewById(R.id.usuario_nombre);
        canal_imagen = findViewById(R.id.usuario_imagen);
        TextView ultimaconexion = findViewById(R.id.usuario_conexion);
        rv_mensajes_canal = findViewById(R.id.rv_channel_msg);
        EnviarMensajes = findViewById(R.id.channel_chat_linear_layout);
        mensaje = findViewById(R.id.mensaje_channel);
        ImageView botonenviar = findViewById(R.id.enviar_mensaje_boton_channel);
        botonarchivo = findViewById(R.id.enviar_archivos_boton_channel);
        emojiboton = findViewById(R.id.emojiboton_channel);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv_mensajes_canal.setLayoutManager(lm);
        ChannelMsgAdapter adapter = new ChannelMsgAdapter(mensajeCanalList);
        rv_mensajes_canal.setAdapter(adapter);

        getRol();

        RootRef.child("Canales").child(id).child("Mensajes").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MensajeCanal mensajeCanal = snapshot.getValue(MensajeCanal.class);
                mensajeCanalList.add(mensajeCanal);
                adapter.notifyDataSetChanged();
                int itemCount = rv_mensajes_canal.getAdapter().getItemCount();

                // Hacer un desplazamiento inmediato al último elemento
                rv_mensajes_canal.scrollToPosition(itemCount - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        botonenviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mensaje.getText())){
                    Toast.makeText(MensajesCanalActivity.this, R.string.ingrese_mensaje, Toast.LENGTH_SHORT).show();
                }
                String Mensaje = mensaje.getText().toString();
                ChannelController.EnviarMensajeCanal(Mensaje, MensajesCanalActivity.this, id, mensaje);
            }
        });

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

    private void getRol() {
        RootRef.child("Canales").child(id).child("Miembros").child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String Rol = snapshot.child("Rol").getValue(String.class);
                    if(Rol!=null && Rol.equals("miembro")){
                        EnviarMensajes.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }
}
