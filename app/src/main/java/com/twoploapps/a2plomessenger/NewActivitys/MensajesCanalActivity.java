package com.twoploapps.a2plomessenger.NewActivitys;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.vanniktech.emoji.EmojiPopup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MensajesCanalActivity extends AppCompatActivity {
    private TextView nombrecanal;
    private CircleImageView canal_imagen;
    private EmojiEditText mensaje;
    private RecyclerView rv_mensajes_canal;
    private LinearLayout EnviarMensajes;
    private DatabaseReference RootRef;
    private String id, CurrentUserId;
    private List<MensajeCanal> mensajeCanalList;
    private ProgressDialog dialog;
    private String check="";

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

        dialog = new ProgressDialog(this);

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
        ImageView botonarchivo = findViewById(R.id.enviar_archivos_boton_channel);
        ImageView emojiboton = findViewById(R.id.emojiboton_channel);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv_mensajes_canal.setLayoutManager(lm);
        ChannelMsgAdapter adapter = new ChannelMsgAdapter(mensajeCanalList, id);
        rv_mensajes_canal.setAdapter(adapter);

        EmojiPopup popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.root_view_channel)).build(mensaje);

        emojiboton.setOnClickListener(v -> popup.toggle());

        nombrecanal.setOnClickListener(v -> {
            Intent intent = new Intent(MensajesCanalActivity.this, InfoCanalActivity.class);
            intent.putExtra("channel_id", id);
            startActivity(intent);
        });

        ultimaconexion.setOnClickListener(v -> {
            Intent intent = new Intent(MensajesCanalActivity.this, InfoCanalActivity.class);
            intent.putExtra("channel_id", id);
            startActivity(intent);
        });

        botonarchivo.setOnClickListener(v -> {
            CharSequence[] opciones = new CharSequence[] {
                    getString(R.string.imagenes),
                    "PDF",
                    "Word",
                    "Video MP4",
                    getString(R.string.audio),
                    getString(R.string.grabar_nota)
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(MensajesCanalActivity.this);
            builder.setTitle(getString(R.string.seleccioinatipo));
            builder.setItems(opciones, (dialog, which) -> {
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
                if (which == 5) {
                    // Nueva opción para grabar nota de voz
                    check = "note";
                    showVoiceRecordingDialog(); // Mostrar diálogo para grabar la nota de voz
                }
            });
            builder.show();
        });

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

    private void showVoiceRecordingDialog() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Si no se ha concedido, solicitar el permiso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MensajesCanalActivity.this);
        builder.setTitle(R.string.grabar_nota);

        // Layout para el diálogo
        View view = LayoutInflater.from(MensajesCanalActivity.this).inflate(R.layout.dialog_voice_recording, null);
        builder.setView(view);

        Button startRecordingButton = view.findViewById(R.id.startRecordingButton);
        Button stopRecordingButton = view.findViewById(R.id.stopRecordingButton);
        stopRecordingButton.setEnabled(false);

        // Inicializar el grabador
        final MediaRecorder[] mediaRecorder = {new MediaRecorder()};
        String filePath = getExternalFilesDir(null).getAbsolutePath() + "/nota_de_voz.mp3";

        AlertDialog dialog = builder.create();

        startRecordingButton.setOnClickListener(v -> {
            try {
                mediaRecorder[0].setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder[0].setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder[0].setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder[0].setOutputFile(filePath);
                mediaRecorder[0].prepare();
                mediaRecorder[0].start();
                Toast.makeText(MensajesCanalActivity.this, getString(R.string.grabacion_iniciada), Toast.LENGTH_SHORT).show();
                startRecordingButton.setEnabled(false);
                stopRecordingButton.setEnabled(true);
            } catch (Exception e) {
                Toast.makeText(MensajesCanalActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        stopRecordingButton.setOnClickListener(v -> {
            try {
                mediaRecorder[0].stop();
                mediaRecorder[0].release();
                mediaRecorder[0] = null;
                Toast.makeText(MensajesCanalActivity.this, getString(R.string.grabacion_finalizada), Toast.LENGTH_SHORT).show();

                // Subir el archivo grabado a Firebase
                uploadVoiceNoteToFirebase(filePath, dialog);
                startRecordingButton.setEnabled(true);
                stopRecordingButton.setEnabled(false);
            } catch (Exception e) {
                Toast.makeText(MensajesCanalActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancelar), (d, which) -> dialog.dismiss());
        dialog.show();
    }

    private void uploadVoiceNoteToFirebase(String filePath, AlertDialog dialog2) {

        dialog.setTitle(R.string.enviando_archivo);
        dialog.setMessage(getString(R.string.estamos_enviando_audio));
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Uri fileUri = Uri.fromFile(new File(filePath));

        ChannelController.EnviarArchivoCanal(fileUri, "mp3", MensajesCanalActivity.this, id, dialog);

        dialog2.dismiss();
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
                }else{
                    EnviarMensajes.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            dialog.setTitle(R.string.enviando_imagen);
            dialog.setMessage(getString(R.string.estamos_enviando_imagen));
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            Uri fileUri = data.getData();
            if(!check.equals("imagen")){
                ChannelController.EnviarArchivoCanal(fileUri, check, MensajesCanalActivity.this, id, dialog);
            }else if(check.equals("imagen")){
                ChannelController.EnviarImagenCanal(fileUri, check, MensajesCanalActivity.this, id, dialog);
            }
        }
    }
}
