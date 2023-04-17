package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String RecibirUserID, nombre, imagens;
    private TextView nombreusuario, ultimaconexion;
    private CircleImageView usuarioimagen;
    private Toolbar toolbar;
    private EmojiEditText mensaje;
    private ImageView botonenviar, botonarchivo, emojiboton;
    private DatabaseReference RootRef,NotificacionesRef;
    private FirebaseAuth auth;
    private String EnviarUserID;
    private ProgressDialog dialog;

    private final List<Mensajes> mensajesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MensajeAdapter mensajeAdapter;
    private RecyclerView UsuariosrecyclerView;
    private String CurrentTime, CurrentDate;
    private String check="",myUrl="";
    private StorageTask uploadTaks;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        auth=FirebaseAuth.getInstance();
        EnviarUserID = auth.getCurrentUser().getUid();
        RootRef=FirebaseDatabase.getInstance().getReference();
        NotificacionesRef=FirebaseDatabase.getInstance().getReference().child("Notificaciones");
        RecibirUserID=getIntent().getExtras().get("user_id").toString();
        nombre=getIntent().getExtras().get("user_nombre").toString();
        imagens=getIntent().getExtras().get("user_imagen").toString();
        //Toast.makeText(this, "Mensaje a: "+nombre, Toast.LENGTH_SHORT).show();
        IniciarelLayout();
        nombreusuario.setText(nombre);
        Picasso.get().load(imagens).placeholder(R.drawable.defaultprofilephoto).into(usuarioimagen);
        dialog = new ProgressDialog(this);
        mensajeAdapter = new MensajeAdapter(mensajesList);
        UsuariosrecyclerView = (RecyclerView) findViewById(R.id.listamensajesresikler);
        linearLayoutManager = new LinearLayoutManager(this);
        UsuariosrecyclerView.setLayoutManager(linearLayoutManager);
        UsuariosrecyclerView.setAdapter(mensajeAdapter);
        MetodoConexion();
        RootRef.child("Mensajes").child(EnviarUserID).child(RecibirUserID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensajes mensajes = snapshot.getValue(Mensajes.class);
                mensajesList.add(mensajes);
                mensajeAdapter.notifyDataSetChanged();
                UsuariosrecyclerView.smoothScrollToPosition(UsuariosrecyclerView.getAdapter().getItemCount());
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

    }
    public void MetodoConexion(){
        RootRef.child("Usuarios").child(RecibirUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String privacidadultimaconexion = snapshot.child("PUC").getValue().toString();
                if(privacidadultimaconexion.equals("Oculto")){
                    ultimaconexion.setText("-");
                }else{
                    if(snapshot.child("estadoUser").hasChild("estado")){
                        String estado = snapshot.child("estadoUser").child("estado").getValue().toString();
                        String fecha = snapshot.child("estadoUser").child("fecha").getValue().toString();
                        String hora = snapshot.child("estadoUser").child("hora").getValue().toString();
                        if(estado.equals("activo")){
                            ultimaconexion.setText(R.string.activo);
                        }else if(estado.equals("inactivo")){
                            ultimaconexion.setText(getString(R.string.ultima_conexion_chat) + fecha + "\n" + hora);
                        }
                    }else{
                        ultimaconexion.setText(R.string.inactivo);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void IniciarelLayout() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        CurrentDate = dateFormat.format(calendar.getTime());
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("hh mm a");
        CurrentTime = dateFormat1.format(calendar.getTime());
        toolbar=(Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_chat_bar, null);
        actionBar.setCustomView(view);
        nombreusuario=(TextView) findViewById(R.id.usuario_nombre);
        ultimaconexion=(TextView) findViewById(R.id.usuario_conexion);
        usuarioimagen=(CircleImageView) findViewById(R.id.usuario_imagen);
        mensaje=(EmojiEditText) findViewById(R.id.mensaje);
        botonenviar=(ImageView) findViewById(R.id.enviar_mensaje_boton);
        botonarchivo=(ImageView) findViewById(R.id.enviar_archivos_boton);
        emojiboton=(ImageView) findViewById(R.id.emojiboton);
        EmojiPopup popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.root_view)).build(mensaje);
        emojiboton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.toggle();
            }
        });
        nombreusuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, PerfilActivity.class);
                intent.putExtra("usuario_id",RecibirUserID);
                startActivity(intent);
            }
        });
        ultimaconexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, PerfilActivity.class);
                intent.putExtra("usuario_id",RecibirUserID);
                startActivity(intent);
            }
        });
        botonenviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnviarMensaje();
            }
        });
        botonarchivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence opciones[] = new CharSequence[] {
                        getString(R.string.imagenes),
                        "PDF",
                        "Word",
                        "Video MP4",
                        getString(R.string.audio)
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle(getString(R.string.seleccioinatipo));
                builder.setItems(opciones, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            check="imagen";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, getString(R.string.selecciionar_imagenes)),438);
                        }
                        if(which==1){
                            check="pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, getString(R.string.seleccionar_pdf)),438);
                        }
                        if(which==2){
                            check="docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, getString(R.string.seleccionar_word)),438);
                        }
                        if(which==3){
                            check="mp4";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("video/mp4");
                            startActivityForResult(intent.createChooser(intent, getString(R.string.seleccionar_video)),438);
                        }
                        if(which==4){
                            check="mp3";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("audio/mpeg");
                            startActivityForResult(intent.createChooser(intent, getString(R.string.audio)),438);
                        }
                    }
                });
                builder.show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            dialog.setTitle(R.string.enviando_imagen);
            dialog.setMessage(getString(R.string.estamos_enviando_imagen));
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            fileUri = data.getData();
            if(!check.equals("imagen")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Documentos");
                String mensajeEnviadoRef = "Mensajes/"+EnviarUserID + "/" + RecibirUserID;
                String mensajeRecibidoRef = "Mensajes/"+RecibirUserID + "/" + EnviarUserID;

                DatabaseReference usuarioMensajeRef = RootRef.child("mensajes").child(EnviarUserID).child(RecibirUserID).push();
                String MensajePushID = usuarioMensajeRef.getKey();
                final StorageReference filePath = storageReference.child(MensajePushID+"."+check);

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            uploadTaks = filePath.putFile(fileUri);
                            uploadTaks.continueWithTask(new Continuation() {
                                @Override
                                public Object then(@NonNull Task task) throws Exception {
                                    if(!task.isSuccessful()){
                                        throw task.getException();
                                    }
                                    return filePath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener <Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        Uri downloadUrid = task.getResult();
                                        myUrl = downloadUrid.toString();
                                        Map mensajeTxt = new HashMap();
                                        mensajeTxt.put("mensaje",myUrl);
                                        mensajeTxt.put("tipo",check);
                                        mensajeTxt.put("de",EnviarUserID);
                                        mensajeTxt.put("para",RecibirUserID);
                                        mensajeTxt.put("mensajeID",MensajePushID);
                                        mensajeTxt.put("fecha",CurrentDate);
                                        mensajeTxt.put("hora",CurrentTime);

                                        Map mensajeTxtFull = new HashMap<>();
                                        mensajeTxtFull.put(mensajeEnviadoRef+"/"+ MensajePushID,mensajeTxt);
                                        mensajeTxtFull.put(mensajeRecibidoRef+"/"+ MensajePushID,mensajeTxt);

                                        RootRef.updateChildren(mensajeTxtFull);
                                        dialog.dismiss();
                                        HashMap<String, String> chatNoficicacion = new HashMap<>();
                                        chatNoficicacion.put("de", EnviarUserID);
                                        chatNoficicacion.put("tipo","mensaje");
                                        NotificacionesRef.child(RecibirUserID).push().setValue(chatNoficicacion);
                                    }
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double p = (100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        dialog.setTitle(R.string.enviando_archivo);
                        dialog.setMessage((int) p + "%");
                    }
                });
            }else if(check.equals("imagen")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Archivos");
                String mensajeEnviadoRef = "Mensajes/"+EnviarUserID + "/" + RecibirUserID;
                String mensajeRecibidoRef = "Mensajes/"+RecibirUserID + "/" + EnviarUserID;

                DatabaseReference usuarioMensajeRef = RootRef.child("mensajes").child(EnviarUserID).child(RecibirUserID).push();
                String MensajePushID = usuarioMensajeRef.getKey();
               final StorageReference filePath = storageReference.child(MensajePushID+"."+"jpg");

                uploadTaks = filePath.putFile(fileUri);
                uploadTaks.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener <Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();
                            Map mensajeTxt = new HashMap();
                            mensajeTxt.put("mensaje",myUrl);
                            mensajeTxt.put("tipo",check);
                            mensajeTxt.put("de",EnviarUserID);
                            mensajeTxt.put("para",RecibirUserID);
                            mensajeTxt.put("mensajeID",MensajePushID);
                            mensajeTxt.put("fecha",CurrentDate);
                            mensajeTxt.put("hora",CurrentTime);
                            Map mensajeTxtFull = new HashMap<>();
                            mensajeTxtFull.put(mensajeEnviadoRef+"/"+ MensajePushID,mensajeTxt);
                            mensajeTxtFull.put(mensajeRecibidoRef+"/"+ MensajePushID,mensajeTxt);

                            RootRef.updateChildren(mensajeTxtFull).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        dialog.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mensaje enviado", Toast.LENGTH_SHORT).show();
                                        HashMap<String, String> chatNoficicacion = new HashMap<>();
                                        chatNoficicacion.put("de", EnviarUserID);
                                        chatNoficicacion.put("tipo","mensaje");
                                        NotificacionesRef.child(RecibirUserID).push().setValue(chatNoficicacion);
                                    }else{
                                        String error = task.getException().getMessage().toString();
                                        dialog.dismiss();
                                        Toast.makeText(ChatActivity.this, R.string.error_al_enviar + error, Toast.LENGTH_SHORT).show();
                                    }
                                    mensaje.setText("");
                                }
                            });
                        }
                    }
                });
            }else{
                dialog.dismiss();
                Toast.makeText(this, R.string.seleccione_un_archivo, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void EnviarMensaje() {
        String mensajeTexto = mensaje.getText().toString().trim();
        if(TextUtils.isEmpty(mensajeTexto)){
            Toast.makeText(this, R.string.ingrese_mensaje, Toast.LENGTH_SHORT).show();
        }else{
            String mensajeEnviadoRef = "Mensajes/"+EnviarUserID + "/" + RecibirUserID;
            String mensajeRecibidoRef = "Mensajes/"+RecibirUserID + "/" + EnviarUserID;
            DatabaseReference usuarioMensajeRef = RootRef.child("mensajes").child(EnviarUserID).child(RecibirUserID).push();
            String MensajePushID = usuarioMensajeRef.getKey();
            String mensajeEnc = cifrado.encrypt(mensajeTexto);
            Map mensajeTxt = new HashMap();
            mensajeTxt.put("mensaje",mensajeEnc);
            mensajeTxt.put("tipo","texto");
            mensajeTxt.put("de",EnviarUserID);
            mensajeTxt.put("para",RecibirUserID);
            mensajeTxt.put("mensajeID",MensajePushID);
            mensajeTxt.put("fecha",CurrentDate);
            mensajeTxt.put("hora",CurrentTime);

            Map mensajeTxtFull = new HashMap<>();
            mensajeTxtFull.put(mensajeEnviadoRef+"/"+ MensajePushID,mensajeTxt);
            mensajeTxtFull.put(mensajeRecibidoRef+"/"+ MensajePushID,mensajeTxt);
            RootRef.updateChildren(mensajeTxtFull).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, R.string.mensaje_enviado, Toast.LENGTH_SHORT).show();
                        HashMap<String, String> chatNoficicacion = new HashMap<>();
                        chatNoficicacion.put("de", EnviarUserID);
                        chatNoficicacion.put("tipo","mensaje");
                        NotificacionesRef.child(RecibirUserID).push().setValue(chatNoficicacion);
                    }else{
                        Toast.makeText(ChatActivity.this, R.string.error_al_enviar, Toast.LENGTH_SHORT).show();
                    }
                    mensaje.setText("");
                }
            });
        }
    }
}