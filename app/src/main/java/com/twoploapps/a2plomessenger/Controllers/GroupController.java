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
import com.twoploapps.a2plomessenger.Models.Grupo;
import com.twoploapps.a2plomessenger.R;

public class GroupController {
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
}
