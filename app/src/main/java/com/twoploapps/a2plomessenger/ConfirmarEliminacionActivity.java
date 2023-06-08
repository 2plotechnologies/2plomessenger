package com.twoploapps.a2plomessenger;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ConfirmarEliminacionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar_eliminacion);
        Button btnconfirmar = findViewById(R.id.btn_confirmar_eliminar_cuenta);
        Button btncancelar = findViewById(R.id.btn_cancelar_eliminar_cuenta);
        btncancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnconfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmarEliminacionActivity.this);
                    builder.setTitle(R.string.introduce_tu_contrase_a);
                    final EditText input = new EditText(ConfirmarEliminacionActivity.this);
                    input.setHint(R.string.ingresalaclavedetucuenta);
                    input.setTextColor(Color.BLACK);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(!TextUtils.isEmpty(input.getText().toString().trim())){
                                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), input.getText().toString().trim());
                                currentUser.reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    String id = currentUser.getUid();
                                                    eliminardatos(id, currentUser);
                                                } else {
                                                    Toast.makeText(ConfirmarEliminacionActivity.this,
                                                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    private void eliminardatos(String userid, FirebaseUser curruser) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(userid);
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", "unknown user");
        updates.put("ciudad", "-");
        updates.put("estado", "-");
        updates.put("edad", "-");
        updates.put("genero", "-");
        updates.put("PC", "-");
        updates.put("PI", "Oculto");
        updates.put("PUC", "Oculto");
        updates.put("estadodecuenta", "eliminada");
        updates.put("imagen", "https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/pngwing.com.png?alt=media&token=1d2dff28-0fd1-4caf-9ca0-b6192b0fc8c2");
        userRef.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    eliminarPosts(userid, curruser);
                }
            }
        });
    }

    private void eliminarPosts(String userid, FirebaseUser curruser) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = postsRef.orderByChild("iduser").equalTo(userid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        postSnapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    eliminarCuenta(curruser);
                                }
                            }
                        });
                    }
                }else{
                    eliminarCuenta(curruser);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }

    private void eliminarCuenta(FirebaseUser curruser) {
        curruser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(ConfirmarEliminacionActivity.this, R.string.seeliminosucuenta, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ConfirmarEliminacionActivity.this, RegistroActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(ConfirmarEliminacionActivity.this,
                                    "Error: "+task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}