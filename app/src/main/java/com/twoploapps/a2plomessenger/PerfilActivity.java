package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.twoploapps.a2plomessenger.NewActivitys.ReportarActivity;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilActivity extends AppCompatActivity {

    private String usuario_revid, usuario_enviarid, CurrenEstado;
    private TextView usuarionom, usuariociu, usuarioest;
    private CircleImageView usuarioima;
    private Button enviarmensaje, cancelarmensaje;
    private DatabaseReference UserRef, SolicitudRef, ContactosRef, NotificacionesRef;
    boolean contactoAgregado = false;

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        toolbar=(Toolbar) findViewById(R.id.usuario_perfil_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        usuario_revid = getIntent().getExtras().get("usuario_id").toString();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Usuarios");
        ContactosRef= FirebaseDatabase.getInstance().getReference().child("Contactos");
        SolicitudRef=FirebaseDatabase.getInstance().getReference().child("Solicitudes");
        NotificacionesRef=FirebaseDatabase.getInstance().getReference().child("Notificaciones");
        usuario_enviarid = auth.getCurrentUser().getUid();
        usuarionom = findViewById(R.id.usuario_vic_nombre);
        usuariociu = findViewById(R.id.usuario_vic_ciudad);
        usuarioest = findViewById(R.id.usuario_vic_estado);
        usuarioima = findViewById(R.id.usuario_vic_imagen);
        enviarmensaje = findViewById(R.id.enviar_mensaje_usuario_vic);
        cancelarmensaje = findViewById(R.id.cancelar_mensaje_usuario_vic);
        FloatingActionButton reportUser = findViewById(R.id.fab_report_user);
        reportUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PerfilActivity.this, ReportarActivity.class);
                intent.putExtra("user_id", usuario_revid);
                startActivity(intent);
            }
        });
        CurrenEstado = "nueva";
        ObtenerInfromacionDB();
        verificarContactoAgregado();
        usuarioExiste();
    }

    private void usuarioExiste() {
        UserRef.child(usuario_revid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("estadodecuenta")){
                        String estado = snapshot.child("estadodecuenta").getValue().toString();
                        if(estado.equals("eliminada")){
                            AlertDialog.Builder builder = new AlertDialog.Builder(PerfilActivity.this);
                            builder.setMessage(getString(R.string.esteusuarionoestadisponible))
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent intent = new Intent(PerfilActivity.this, BuscarAmigosActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();

                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }

    private void verificarContactoAgregado() {
        ContactosRef.child(usuario_enviarid).child(usuario_revid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    contactoAgregado = true;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }

    private void ObtenerInfromacionDB() {
        UserRef.child(usuario_revid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("imagen"))){
                    String privacidadimg = snapshot.child("PI").getValue().toString();
                    String nombreUser = snapshot.child("nombre").getValue().toString();
                    String ciudadUser = snapshot.child("ciudad").getValue().toString();
                    String estadoUser = snapshot.child("estado").getValue().toString();
                    if(privacidadimg.equals("Oculto")){
                        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/defaultprofilephoto.png?alt=media&token=d2f0f0de-2386-45bc-952a-aedeea866b0c").into(usuarioima);
                    }else if(privacidadimg.equals("Contactos") && !contactoAgregado){
                        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/defaultprofilephoto.png?alt=media&token=d2f0f0de-2386-45bc-952a-aedeea866b0c").into(usuarioima);
                    }else{
                        String imagenUser = snapshot.child("imagen").getValue().toString();
                        Picasso.get().load(imagenUser).placeholder(R.drawable.defaultprofilephoto).into(usuarioima);
                    }
                    usuarionom.setText(nombreUser);
                    String privacidadciu = snapshot.child("PC").getValue().toString();
                    if(privacidadciu.equals("-")||privacidadciu.equals("Contacto")){
                        usuariociu.setText("-");
                    }else{
                        usuariociu.setText(ciudadUser);
                    }
                    usuarioest.setText(estadoUser);
                    MotorEnviarReuqeriemiento();
                }else{
                    String nombreUser = snapshot.child("nombre").getValue().toString();
                    String ciudadUser = snapshot.child("ciudad").getValue().toString();
                    String estadoUser = snapshot.child("estado").getValue().toString();
                    usuarionom.setText(nombreUser);
                    String privacidadciu = snapshot.child("PC").getValue().toString();
                    if(privacidadciu.equals("-")){
                        usuariociu.setText("-");
                    }else if(privacidadciu.equals("Contacto")&&!contactoAgregado){
                        usuariociu.setText("-");
                    }else{
                        usuariociu.setText(ciudadUser);
                    }
                    usuarioest.setText(estadoUser);
                    MotorEnviarReuqeriemiento();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }});
    }

    private void MotorEnviarReuqeriemiento() {
        SolicitudRef.child(usuario_enviarid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(usuario_revid)){
                    String requerimiento = snapshot.child(usuario_revid).child("tipo").getValue().toString();
                    if (requerimiento.equals("enviado")){
                        CurrenEstado = "enviada";
                        enviarmensaje.setText(R.string.cancelar_solicitud);
                    } else  if (requerimiento.equals("recibido")){
                        CurrenEstado = "recibida";
                        enviarmensaje.setText(R.string.aceptar_solicitud);
                        cancelarmensaje.setVisibility(View.VISIBLE);
                        cancelarmensaje.setEnabled(true);
                        cancelarmensaje.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelarSolicitudMensaje();
                            }
                        });
                    }
                }else {
                    ContactosRef.child(usuario_enviarid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(usuario_revid)){
                                CurrenEstado="amigos";
                                enviarmensaje.setText("ELIMINAR CONTACTO");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!usuario_enviarid.equals(usuario_revid)){
            enviarmensaje.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enviarmensaje.setEnabled(false);
                    if (CurrenEstado.equals("nueva")){
                        EnviarSolicitudMensaje();
                    }
                    if (CurrenEstado.equals("enviada")){
                        CancelarSolicitudMensaje();
                    }
                    if (CurrenEstado.equals("recibida")){
                        AceptadaSolicitudMensaje();
                    }
                    if (CurrenEstado.equals("amigos")){
                        EliminarContacto();
                    }
                }
            });
        }else {
            enviarmensaje.setVisibility(View.GONE);
        }

    }

    private void EliminarContacto() {
        ContactosRef.child(usuario_enviarid).child(usuario_revid)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    ContactosRef.child(usuario_revid).child(usuario_enviarid)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                enviarmensaje.setEnabled(true);
                                CurrenEstado="nueva";
                                enviarmensaje.setText(R.string.enviar_mensaje);
                                cancelarmensaje.setVisibility(View.GONE);
                                cancelarmensaje.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }


    private void AceptadaSolicitudMensaje() {
        ContactosRef.child(usuario_enviarid).child(usuario_revid).child("Contacto").setValue("Aceptado").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    ContactosRef.child(usuario_revid).child(usuario_enviarid).child("Contacto").setValue("Aceptado").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                SolicitudRef.child(usuario_enviarid).child(usuario_revid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){
                                            SolicitudRef.child(usuario_revid).child(usuario_enviarid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    enviarmensaje.setEnabled(true);
                                                    CurrenEstado="amigos";
                                                    enviarmensaje.setText(R.string.eliminar_contacto);

                                                    cancelarmensaje.setVisibility(View.GONE);
                                                    cancelarmensaje.setEnabled(false);

                                                }
                                            });
                                        }

                                    }
                                });
                            }

                        }
                    });
                }
            }
        });

    }

    private void CancelarSolicitudMensaje() {

        SolicitudRef.child(usuario_enviarid).child(usuario_revid)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    SolicitudRef.child(usuario_revid).child(usuario_enviarid)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                enviarmensaje.setEnabled(true);
                                CurrenEstado="nueva";
                                enviarmensaje.setText(R.string.enviar_mensaje);

                                cancelarmensaje.setVisibility(View.GONE);
                                cancelarmensaje.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void EnviarSolicitudMensaje() {
        SolicitudRef.child(usuario_enviarid).child(usuario_revid)
                .child("tipo").setValue("enviado")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            SolicitudRef.child(usuario_revid).child(usuario_enviarid)
                                    .child("tipo").setValue("recibido")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                HashMap<String, String> chatNoficicacion = new HashMap<>();
                                                chatNoficicacion.put("de", usuario_enviarid);
                                                chatNoficicacion.put("tipo","requerimiento");

                                                NotificacionesRef.child(usuario_revid).push().setValue(chatNoficicacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){

                                                            enviarmensaje.setEnabled(true);
                                                            CurrenEstado="vieja";
                                                            enviarmensaje.setText(R.string.cancelar_solicitud);

                                                        }
                                                    }
                                                });



                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}