package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.SimpleFormatter;

public class GrupoChatActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageView EnviarMensajeBoton, emojuboton, archivosboton;
    private EmojiEditText MensajeUsuario;
    private ScrollView scrollView;
    private EmojiTextView verMensajes;
    private String CurrentGrupoNombre, CurrentUserId, CurrentUserName, Fecha, Hora,CodigoGrupo;
    EmojiPopup popup;
    private FirebaseAuth auth;
    private DatabaseReference UserRef, GrupoRef, GrupoMensajeKeyRef,RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo_chat);

        CurrentGrupoNombre = getIntent().getExtras().get("nombregrupo").toString();
        Toast.makeText(this, CurrentGrupoNombre, Toast.LENGTH_SHORT).show();

        auth=FirebaseAuth.getInstance();
        CurrentUserId=auth.getCurrentUser().getUid();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Usuarios");
        GrupoRef=FirebaseDatabase.getInstance().getReference().child("Usuarios");
        RootRef = FirebaseDatabase.getInstance().getReference().child("Grupos");

        IniciarObjetos();

        InformacionUusario();


        EnviarMensajeBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GuardarMensajeDb();
                MensajeUsuario.setText("");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        archivosboton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GrupoChatActivity.this, "Enviar archivos en grupo aun no es soportado, disculpe las molestias", Toast.LENGTH_SHORT).show();
            }
        });
        emojuboton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.toggle();
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        RootRef.child(CurrentGrupoNombre).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    MostrarMensajes(snapshot);
                }else{
                    Toast.makeText(GrupoChatActivity.this, "No existe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    MostrarMensajes(snapshot);
                }else{
                    Toast.makeText(GrupoChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
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
    }

    private void IniciarObjetos() {
        toolbar=(Toolbar) findViewById(R.id.grupochat_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(CurrentGrupoNombre);

        EnviarMensajeBoton=(ImageView)findViewById(R.id.enviar_mensaje_grupo);
        emojuboton=(ImageView) findViewById(R.id.emojiboton_grupos);
        archivosboton=(ImageView) findViewById(R.id.enviar_archivos_boton_grupos);
        MensajeUsuario=(EmojiEditText) findViewById(R.id.texto_grupo_chat);
        scrollView=(ScrollView)findViewById(R.id.mi_scroll_view);
        verMensajes=(EmojiTextView) findViewById(R.id.grupo_chat_texto);
        popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.root_view_group)).build(MensajeUsuario);
    }
    private void InformacionUusario() {

        UserRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    CurrentUserName = snapshot.child("nombre").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});


    }
    private void CodigoDelGrupo() {
        UserRef.child(CurrentUserId).child("Grupos").child(CurrentGrupoNombre).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    CodigoGrupo = snapshot.child("codigo").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GuardarMensajeDb() {
        String mensaje = MensajeUsuario.getText().toString().trim();
        String mensajekey = RootRef.child(CurrentGrupoNombre).push().getKey();
        if(TextUtils.isEmpty(mensaje)){
            Toast.makeText(this, "Por favor ingrese su mensaje", Toast.LENGTH_SHORT).show();
        }else{
            Calendar fechacalendar = Calendar.getInstance();
            SimpleDateFormat currentFecha = new SimpleDateFormat("MM dd, yyyy");
            Fecha = currentFecha.format(fechacalendar.getTime());

            Calendar horacalendar = Calendar.getInstance();
            SimpleDateFormat currentHora = new SimpleDateFormat("HH:mm a");
            Hora = currentHora.format(horacalendar.getTime());

            HashMap<String, Object> mensajegrupo = new HashMap<>();
            RootRef.child(CurrentGrupoNombre).updateChildren(mensajegrupo);

            GrupoMensajeKeyRef = RootRef.child(CurrentGrupoNombre).child(mensajekey);

            HashMap<String, Object> mensajeinformacion = new HashMap<>();
            mensajeinformacion.put("nombre",CurrentUserName);
            mensajeinformacion.put("mensaje",mensaje);
            mensajeinformacion.put("fecha",Fecha);
            mensajeinformacion.put("hora",Hora);
            GrupoMensajeKeyRef.updateChildren(mensajeinformacion);
        }
    }

    private void MostrarMensajes(DataSnapshot snapshot) {
        Iterator iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String fecha = (String) ((DataSnapshot)iterator.next()).getValue();
            String hora = (String) ((DataSnapshot)iterator.next()).getValue();
            String mensaje = (String) ((DataSnapshot)iterator.next()).getValue();
            String nombre = (String) ((DataSnapshot)iterator.next()).getValue();
            verMensajes.append(nombre+"\n"+mensaje+"\n"+fecha+"  "+hora+"\n\n\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_grupo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.editar_grupo){
            Intent intent = new Intent(GrupoChatActivity.this,InfoGrupoActivity.class);
            intent.putExtra("nombre_grupo",CurrentGrupoNombre);
            startActivity(intent);
        }
        return true;
    }
}