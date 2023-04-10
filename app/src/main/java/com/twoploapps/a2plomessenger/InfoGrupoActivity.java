package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class InfoGrupoActivity extends AppCompatActivity {
    private TextView ng,txcode,codigogrupo;
    private EditText nombregrupo;
    private String CurrentGruponombre, currentUserid;
    private Toolbar toolbar;
    private DatabaseReference GrupoRef, UserRef;
    private Button btnsalir;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_grupo);
        toolbar=(Toolbar) findViewById(R.id.toolbar_infogrupo);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Informacion del grupo");
        ng = (TextView) findViewById(R.id.ng);
        txcode = (TextView) findViewById(R.id.txcode);
        codigogrupo = (TextView) findViewById(R.id.codigogrupo);
        nombregrupo=(EditText) findViewById(R.id.nombregrupo);
        btnsalir=(Button)findViewById(R.id.btnsalir);
        GrupoRef = FirebaseDatabase.getInstance().getReference().child("Grupos");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        CurrentGruponombre = getIntent().getExtras().get("nombre_grupo").toString();
        auth=FirebaseAuth.getInstance();
        currentUserid = auth.getCurrentUser().getUid();
        nombregrupo.setText(CurrentGruponombre);
        nombregrupo.setEnabled(false);
        GrupoRef.child(CurrentGruponombre).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String CodigoGrupo = snapshot.child("codigo").getValue().toString();
                    codigogrupo.setText(CodigoGrupo);
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
                builder.setMessage("¿Deseas salir del grupo?")
                        .setCancelable(false)
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                UserRef.child(currentUserid).child("Grupos").child(CurrentGruponombre).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(!task.isSuccessful()){
                                            Toast.makeText(InfoGrupoActivity.this, "Error al salir", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Intent intent = new Intent(InfoGrupoActivity.this,InicioActivity.class);
                                            startActivity(intent);
                                            Toast.makeText(InfoGrupoActivity.this, "Saliste del grupo", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
}