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
import com.twoploapps.a2plomessenger.InicioActivity;
import com.twoploapps.a2plomessenger.Models.Grupo;
import com.twoploapps.a2plomessenger.Models.MensajeCanal;
import com.twoploapps.a2plomessenger.Models.MensajeGrupo;
import com.twoploapps.a2plomessenger.NewAdapters.RV_Adapters.ChannelMsgAdapter;
import com.twoploapps.a2plomessenger.NewAdapters.RV_Adapters.GroupMessageAdapter;
import com.twoploapps.a2plomessenger.R;
import com.twoploapps.a2plomessenger.cifrado;
import com.vanniktech.emoji.EmojiEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class GroupController {

    public interface OnGroupDataReceived {
        void onDataReceived(Grupo grupo);
    }
    static FirebaseAuth auth = FirebaseAuth.getInstance();
    static DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    static DatabaseReference NotificacionesRef = FirebaseDatabase.getInstance().getReference().child("Notificaciones");
    static String groupName;
    public static void Create(Grupo grupo, String groupId, Context context){

        ref.child("Grupos").child(groupId).setValue(grupo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    String CurrentUserId = auth.getCurrentUser().getUid();
                    ref.child("Grupos").child(groupId).child("Miembros").child(CurrentUserId).child("Rol").setValue("creador");
                    Toast.makeText(context, context.getString(R.string.grupocreado), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, InicioActivity.class);
                    context.startActivity(intent);
                }else{
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void Join (String groupId, Context context){
        String CurrentUserId = auth.getCurrentUser().getUid();

        ref.child("Grupos").child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("Miembros").hasChild(CurrentUserId)){
                        Toast.makeText(context, context.getString(R.string.ya_estas_en_el_grupo), Toast.LENGTH_SHORT).show();
                    }else{
                        ref.child("Grupos").child(groupId).child("Miembros").child(CurrentUserId).child("Rol").setValue("miembro")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(context, context.getString(R.string.te_uniste_al_grupo), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }else{
                    Toast.makeText(context, context.getString(R.string.codigonovalido), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }

    public static void getData(String id, GroupController.OnGroupDataReceived callback){
        ref.child("Grupos").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Grupo grupo = new Grupo();
                if(snapshot.exists()){
                    grupo.setId(id);
                    grupo.setNombre(snapshot.child("nombre").getValue(String.class));
                    grupo.setImagen(snapshot.child("imagen").getValue(String.class));
                    grupo.setDescripcion(snapshot.child("descripcion").getValue(String.class));
                    grupo.setCreadorId(snapshot.child("creadorId").getValue(String.class));
                }
                callback.onDataReceived(grupo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onDataReceived(null);
            }
        });
    }

    public static void EnviarMensajeGrupo(String mensaje, Context context, String groupId, EmojiEditText Et_mensaje, String username) {
        String CurrentUserId = auth.getCurrentUser().getUid();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String CurrentDate = dateFormat.format(calendar.getTime());
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("hh mm a");
        String CurrentTime = dateFormat1.format(calendar.getTime());
        String MessageGroupId = ref.child("Grupos").child(groupId).child("Mensajes").push().getKey();
        String mensajeEnc = cifrado.encrypt(mensaje);

        MensajeGrupo mensajeGrupo = new MensajeGrupo(CurrentUserId, mensajeEnc, "texto", MessageGroupId, CurrentDate, CurrentTime, username);

        ref.child("Grupos").child(groupId).child("Mensajes").child(MessageGroupId).setValue(mensajeGrupo)
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

    public static void EnviarArchivoGrupo(Uri fileUri, String check, Context context, String groupId, ProgressDialog dialog, String username){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Documentos");
        DatabaseReference groupMensajeRef = ref.child("Grupos").child(groupId).child("Mensajes").push();
        String MensajePushID = groupMensajeRef.getKey();
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

                        MensajeGrupo msg = new MensajeGrupo(CurrentUserId, uri.toString(), check, MensajePushID, CurrentDate, CurrentTime, username);

                        ref.child("Grupos").child(groupId).child("Mensajes").child(MensajePushID).setValue(msg)
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

    public static void EnviarImagenGrupo(Uri fileUri, String check, Context context, String groupId, ProgressDialog dialog, String username){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Archivos");
        DatabaseReference groupMensajeRef = ref.child("Grupos").child(groupId).child("Mensajes").push();
        String MensajePushID = groupMensajeRef.getKey();
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

                        MensajeGrupo msg = new MensajeGrupo(CurrentUserId, uri.toString(), check, MensajePushID, CurrentDate, CurrentTime, username);

                        ref.child("Grupos").child(groupId).child("Mensajes").child(MensajePushID).setValue(msg)
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

    public static void EliminarMensajeGrupo(final int position, final GroupMessageAdapter.ViewHolderGroupMessages holder, List<MensajeGrupo> list, String groupId){
        ref.child("Grupos").child(groupId).child("Mensajes").child(list.get(position).getMensajeID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), R.string.toasteliminado, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
