package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InfoGrupoActivity extends AppCompatActivity {
    private TextView ng,txcode,codigogrupo;
    private EditText nombregrupo;
    private String currentUserid, id;
    private Toolbar toolbar;
    private DatabaseReference GrupoRef, UserRef;
    private Button btnsalir;
    private FirebaseAuth auth;
    private ListView listamiembros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_grupo);
        toolbar = findViewById(R.id.toolbar_infogrupo);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.info_grupo));
        ng = findViewById(R.id.ng);
        txcode = findViewById(R.id.txcode);
        codigogrupo = findViewById(R.id.codigogrupo);
        nombregrupo = findViewById(R.id.nombregrupo);
        listamiembros = findViewById(R.id.list_view);
        btnsalir = findViewById(R.id.btnsalir);
        GrupoRef = FirebaseDatabase.getInstance().getReference().child("Grupos");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        auth=FirebaseAuth.getInstance();
        id = getIntent().getStringExtra("group_id");
        obtenerNombresDeUsuarios(id);
        currentUserid = auth.getCurrentUser().getUid();
        codigogrupo.setText(id);
        GrupoRef.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name = snapshot.child("nombre").getValue(String.class);
                    nombregrupo.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        btnsalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InfoGrupoActivity.this);
                builder.setMessage(getString(R.string.deseas_salir))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.si), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                GrupoRef.child(id).child("Miembros").child(currentUserid).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(InfoGrupoActivity.this, getString(R.string.saliste_del_grupo), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
    private void obtenerNombresDeUsuarios(String groupId) {
        DatabaseReference grupoRef = FirebaseDatabase.getInstance().getReference().child("Grupos").child(groupId).child("Miembros");
        grupoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List<String> userIds = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        userIds.add(dataSnapshot.getKey());
                    }
                    obtenerNombresDeUsuariosPorId(userIds);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    private void obtenerNombresDeUsuariosPorId(List<String> userIds) {
        List<String> nombresUsuarios = new ArrayList<>();
        for (String userId : userIds) {
            UserRef.child(userId).child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombre = snapshot.getValue(String.class);
                        if (nombre != null) {
                            nombresUsuarios.add(nombre);
                        }
                        // Una vez obtenidos todos los nombres, actualiza el ListView
                        if (nombresUsuarios.size() == userIds.size()) {
                            actualizarListView(nombresUsuarios);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle possible errors.
                }
            });
        }
    }
    private void actualizarListView(List<String> nombresUsuarios) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombresUsuarios);
        listamiembros.setAdapter(adapter);
    }


}