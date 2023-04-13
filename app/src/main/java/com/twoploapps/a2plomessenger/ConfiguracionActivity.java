package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracionActivity extends AppCompatActivity {
    TextView txtnombre, txtciudad, txtestado;
    CircleImageView userimg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        txtnombre = findViewById(R.id.nombreconfig);
        txtciudad = findViewById(R.id.ciudadconfig);
        txtestado = findViewById(R.id.estadoconfig);
        userimg = findViewById(R.id.imgconfig);
        TextView txtcambiar = findViewById(R.id.opcion_cambiar_contrasena);
        TextView txtprivacidad = findViewById(R.id.opcion_privacidad);
        TextView txtacercade = findViewById(R.id.opcion_acerca_de);
        TextView txtcerrarsesion = findViewById(R.id.opcion_cerrar_sesion);
        ImageView btneditarperfil = findViewById(R.id.btneditarperfilconfig);
        Button btnvolver = findViewById(R.id.volverainicioconfig);
        getUserData();
        btneditarperfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConfiguracionActivity.this, MiperfilActivity.class);
                startActivity(intent);
            }
        });
        txtcambiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = auth.getCurrentUser();
                if(currentUser!=null){
                    String emailAddress = currentUser.getEmail();
                    if(emailAddress!=null){
                        auth.sendPasswordResetEmail(emailAddress)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ConfiguracionActivity.this);
                                            builder.setMessage(getString(R.string.correoenviado)+": "+emailAddress);
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    // Cerrar el dialogo
                                                    dialog.dismiss();
                                                }
                                            });
                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        } else {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(ConfiguracionActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            }
        });
        txtprivacidad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConfiguracionActivity.this, PrivacidadActivity.class);
                startActivity(intent);
            }
        });
        btnvolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConfiguracionActivity.this, InicioActivity.class);
                startActivity(intent);
            }
        });
        txtacercade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConfiguracionActivity.this, AcercaDeActivity.class);
                startActivity(intent);
            }
        });
        txtcerrarsesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signOut();
                Intent intent = new Intent(ConfiguracionActivity.this, LoginActivity2.class);
                startActivity(intent);
            }
        });
    }

    private void getUserData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String CurrentUserId = mAuth.getCurrentUser().getUid();
        DatabaseReference uRef = FirebaseDatabase.getInstance().getReference();
        uRef.child("Usuarios").child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("imagen")){
                        String urlimg = snapshot.child("imagen").getValue().toString();
                        Picasso.get().load(urlimg).into(userimg);
                    }
                    String nombre = snapshot.child("nombre").getValue().toString();
                    String ciudad = snapshot.child("ciudad").getValue().toString();
                    String estado = snapshot.child("estado").getValue().toString();
                    txtnombre.setText(nombre);
                    txtciudad.setText(ciudad);
                    txtestado.setText(estado);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }
}