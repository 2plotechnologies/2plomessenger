package com.twoploapps.a2plomessenger.Controllers;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.twoploapps.a2plomessenger.InicioActivity;
import com.twoploapps.a2plomessenger.Models.Canal;
import com.twoploapps.a2plomessenger.R;

public class ChannelController {

    public static void Create(Canal canal, String channelId, Context context){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

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
}
