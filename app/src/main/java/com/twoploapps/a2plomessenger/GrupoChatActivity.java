package com.twoploapps.a2plomessenger;

import android.app.ProgressDialog;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.twoploapps.a2plomessenger.Controllers.GroupController;
import com.twoploapps.a2plomessenger.Models.Grupo;
import com.twoploapps.a2plomessenger.NewActivitys.MensajesCanalActivity;
import com.vanniktech.emoji.EmojiEditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class GrupoChatActivity extends AppCompatActivity {

    private TextView nombregrupo;
    private CircleImageView grupo_imagen;
    private EmojiEditText mensaje;
    private RecyclerView rv_mensajes_grupo;
    private DatabaseReference RootRef;
    private String id, CurrentUserId, CurrentUserName;
    //private List<MensajeGrupo> mensajegrupoList;
    private ProgressDialog dialog;
    private String check="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo_chat);

        getUserName();

        id = getIntent().getStringExtra("groupId");
        //Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
        Toolbar toolbar = findViewById(R.id.grupochat_bar_layout);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.layout_chat_bar, null);
            actionBar.setCustomView(view);
        }

        dialog = new ProgressDialog(this);

        RootRef = FirebaseDatabase.getInstance().getReference();
        CurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        nombregrupo = findViewById(R.id.usuario_nombre);
        grupo_imagen = findViewById(R.id.usuario_imagen);
        TextView ultimaconexion = findViewById(R.id.usuario_conexion);
        rv_mensajes_grupo = findViewById(R.id.rv_mensajes_grupo);
        mensaje = findViewById(R.id.mensaje_grupo);
        ImageView botonenviar = findViewById(R.id.enviar_mensaje_grupo_boton);
        ImageView botonarchivo = findViewById(R.id.group_enviar_archivos_boton);
        ImageView emojiboton = findViewById(R.id.emojiboton_group);

        GroupController.getData(id, new GroupController.OnGroupDataReceived() {
            @Override
            public void onDataReceived(Grupo grupo) {
                if(grupo != null){
                    nombregrupo.setText(grupo.getNombre());
                    Picasso.get().load(grupo.getImagen()).placeholder(R.drawable.defaultprofilephoto).into(grupo_imagen);
                    ultimaconexion.setText(getString(R.string.chat_en_grupo));
                }
            }
        });

        botonenviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mensaje.getText())){
                    Toast.makeText(GrupoChatActivity.this, R.string.ingrese_mensaje, Toast.LENGTH_SHORT).show();
                }
                String Mensaje = mensaje.getText().toString();

                GroupController.EnviarMensajeGrupo(Mensaje, GrupoChatActivity.this, id, mensaje, CurrentUserName);
            }
        });
    }

    private void getUserName() {
        FirebaseDatabase.getInstance().getReference().child("Usuarios").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    CurrentUserName = snapshot.child("nombre").getValue(String.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }
}