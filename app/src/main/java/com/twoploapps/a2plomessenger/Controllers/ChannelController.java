package com.twoploapps.a2plomessenger.Controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.twoploapps.a2plomessenger.ChatActivity;
import com.twoploapps.a2plomessenger.InicioActivity;
import com.twoploapps.a2plomessenger.MensajeAdapter;
import com.twoploapps.a2plomessenger.Models.Canal;
import com.twoploapps.a2plomessenger.Models.MensajeCanal;
import com.twoploapps.a2plomessenger.NewActivitys.MensajesCanalActivity;
import com.twoploapps.a2plomessenger.NewAdapters.RV_Adapters.ChannelMsgAdapter;
import com.twoploapps.a2plomessenger.R;
import com.twoploapps.a2plomessenger.cifrado;
import com.vanniktech.emoji.EmojiEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ChannelController {

    public interface OnChannelDataReceived {
        void onDataReceived(Canal canal);
    }

    static FirebaseAuth auth = FirebaseAuth.getInstance();
    static DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    public static void Create(Canal canal, String channelId, Context context){

        ref.child("Canales").child(channelId).setValue(canal).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    String CurrentUserId = auth.getCurrentUser().getUid();
                    ref.child("Canales").child(channelId).child("Miembros").child(CurrentUserId).child("Rol").setValue("creador");
                    Toast.makeText(context, context.getString(R.string.canalcreado), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, InicioActivity.class);
                    context.startActivity(intent);
                }else{
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void Edit(Map<String, Object> canal, String channelId, Context context){
        ref.child("Canales").child(channelId).updateChildren(canal).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context, R.string.guardado_exitosamente, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void Delete(String channelId, Context context){
        ref.child("Canales").child(channelId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context, R.string.canal_eliminado, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, InicioActivity.class);
                    context.startActivity(intent);
                }else{
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void getData(String id, OnChannelDataReceived callback){
        ref.child("Canales").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Canal canal = new Canal();
                if(snapshot.exists()){
                    canal.setId(id);
                    canal.setNombre(snapshot.child("nombre").getValue(String.class));
                    canal.setImagen(snapshot.child("imagen").getValue(String.class));
                    canal.setDescripcion(snapshot.child("descripcion").getValue(String.class));
                    canal.setCreador_Id(snapshot.child("creador_id").getValue(String.class));
                }
                callback.onDataReceived(canal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onDataReceived(null);
            }
        });
    }

    public static void EnviarMensajeCanal(String mensaje, Context context, String channelId, EmojiEditText Et_mensaje) {
        String CurrentUserId = auth.getCurrentUser().getUid();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String CurrentDate = dateFormat.format(calendar.getTime());
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("hh mm a");
        String CurrentTime = dateFormat1.format(calendar.getTime());
        String MessageChannelId = ref.child("Canales").child(channelId).child("Mensajes").push().getKey();
        String mensajeEnc = cifrado.encrypt(mensaje);

        MensajeCanal msg = new MensajeCanal(CurrentUserId, mensajeEnc, "texto", MessageChannelId, CurrentDate, CurrentTime);
        ref.child("Canales").child(channelId).child("Mensajes").child(MessageChannelId).setValue(msg)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context, R.string.mensaje_enviado, Toast.LENGTH_SHORT).show();
                    Et_mensaje.setText("");
                }else{
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Et_mensaje.setText("");
                }
            }
        });
    }

    public static void EnviarArchivoCanal(Uri fileUri, String check, Context context, String channelId, ProgressDialog dialog){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Documentos");
        DatabaseReference channelMensajeRef = ref.child("Canales").child(channelId).child("Mensajes").push();
        String MensajePushID = channelMensajeRef.getKey();
        final StorageReference filePath = storageReference.child(MensajePushID+"."+check);
        filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String CurrentUserId = auth.getCurrentUser().getUid();
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                        String CurrentDate = dateFormat.format(calendar.getTime());
                        SimpleDateFormat dateFormat1 = new SimpleDateFormat("hh mm a");
                        String CurrentTime = dateFormat1.format(calendar.getTime());

                        MensajeCanal msg = new MensajeCanal(CurrentUserId, uri.toString(), check, MensajePushID, CurrentDate, CurrentTime);

                        ref.child("Canales").child(channelId).child("Mensajes").child(MensajePushID).setValue(msg)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            dialog.dismiss();
                                        }
                                    }
                                });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double p = (100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                dialog.setTitle(R.string.enviando_archivo);
                dialog.setMessage((int) p + "%");
            }
        });
    }

    public static void EnviarImagenCanal(Uri fileUri, String check, Context context, String channelId, ProgressDialog dialog){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Archivos");
        DatabaseReference channelMensajeRef = ref.child("Canales").child(channelId).child("Mensajes").push();
        String MensajePushID = channelMensajeRef.getKey();
        final StorageReference filePath = storageReference.child(MensajePushID+"."+"jpg");

        filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String CurrentUserId = auth.getCurrentUser().getUid();
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                        String CurrentDate = dateFormat.format(calendar.getTime());
                        SimpleDateFormat dateFormat1 = new SimpleDateFormat("hh mm a");
                        String CurrentTime = dateFormat1.format(calendar.getTime());

                        MensajeCanal msg = new MensajeCanal(CurrentUserId, uri.toString(), check, MensajePushID, CurrentDate, CurrentTime);

                        ref.child("Canales").child(channelId).child("Mensajes").child(MensajePushID).setValue(msg)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            dialog.dismiss();
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }

    public static void EliminarMensajeCanal(final int position, final ChannelMsgAdapter.ViewHolderChannelMessages holder, List<MensajeCanal> list, String channelId){
        ref.child("Canales").child(channelId).child("Mensajes").child(list.get(position).getMensajeID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), R.string.toasteliminado, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
