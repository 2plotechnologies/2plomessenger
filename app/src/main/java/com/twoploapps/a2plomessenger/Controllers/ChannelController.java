package com.twoploapps.a2plomessenger.Controllers;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twoploapps.a2plomessenger.InicioActivity;
import com.twoploapps.a2plomessenger.Models.Canal;
import com.twoploapps.a2plomessenger.NewActivitys.MensajesCanalActivity;
import com.twoploapps.a2plomessenger.R;

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

}
