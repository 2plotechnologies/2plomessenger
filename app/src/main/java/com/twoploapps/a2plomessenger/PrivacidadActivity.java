package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PrivacidadActivity extends AppCompatActivity {
    private DatabaseReference UserRef,ultRef,ImagenRef;
    private FirebaseDatabase database;
    private String CurrentUserId;
    private FirebaseAuth mAuth;
    private RadioButton hideseen, showseen, hideciu, showciu, hideimage, showimage, showciucontacts, showimgcontacts;
    private CheckBox protectchats, screenshotblock;
    private Button botonguardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacidad);
        hideseen = (RadioButton) findViewById(R.id.cb_hideseen);
        showseen = (RadioButton) findViewById(R.id.cb_showseen);
        hideciu = (RadioButton) findViewById(R.id.cb_hideciu);
        showciu = (RadioButton) findViewById(R.id.cb_showciu);
        hideimage = (RadioButton) findViewById(R.id.cb_hideimage);
        showimage = (RadioButton) findViewById(R.id.cb_showimage);
        showciucontacts = findViewById(R.id.cb_showcontactsciu);
        showimgcontacts = findViewById(R.id.cb_contactoimage);
        screenshotblock = findViewById(R.id.chk_bloquearscreenshots);
        protectchats = findViewById(R.id.chk_proteccionchats);
        botonguardar = (Button) findViewById(R.id.saveprivacity);
        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        ultRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        ultRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        protectchats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (protectchats.isChecked()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PrivacidadActivity.this);
                    builder.setTitle(R.string.proteger_chats);
                    builder.setMessage(getString(R.string.escribetuclavedechats));
                    final EditText input = new EditText(PrivacidadActivity.this);
                    input.setHint(R.string.ingresa_password);
                    input.setTextColor(Color.BLACK);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);
                    builder.setPositiveButton(R.string.guardar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String clave = input.getText().toString().trim();
                            if(!clave.isEmpty()){
                                UserRef.child(CurrentUserId).child("ClaveChats").setValue(clave).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(PrivacidadActivity.this, "Password created successfuly", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                    builder.show();
                }
            }
        });
        botonguardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hideseen.isChecked()){
                    ultRef.child(CurrentUserId).child("PUC").setValue("Oculto");
                }else if(showseen.isChecked()){
                    ultRef.child(CurrentUserId).child("PUC").setValue("Publico");
                }
                if(hideciu.isChecked()){
                    UserRef.child(CurrentUserId).child("PC").setValue("-");
                }else if(showciu.isChecked()){
                    UserRef.child(CurrentUserId).child("PC").setValue("Publico");
                }else if(showciucontacts.isChecked()){
                    UserRef.child(CurrentUserId).child("PC").setValue("Contactos");
                }
                if(hideimage.isChecked()){
                    UserRef.child(CurrentUserId).child("PI").setValue("Oculto");
                }else if(showimage.isChecked()){
                    UserRef.child(CurrentUserId).child("PI").setValue("Publico");
                }else if(showimgcontacts.isChecked()){
                    UserRef.child(CurrentUserId).child("PI").setValue("Contactos");
                }
                if(protectchats.isChecked()){
                    UserRef.child(CurrentUserId).child("ProtegeChats").setValue("verdadero");
                }else{
                    try{
                        UserRef.child(CurrentUserId).child("ProtegeChats").removeValue();
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                if(screenshotblock.isChecked()){
                    UserRef.child(CurrentUserId).child("screenshotsbloqueados").setValue("verdadero");
                }else{
                    try{
                        UserRef.child(CurrentUserId).child("screenshotsbloqueados").removeValue();
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                Toast.makeText(PrivacidadActivity.this, R.string.guardado_exitosamente, Toast.LENGTH_SHORT).show();
            }
        });
        obtenerConfiguracion();
    }

    private void obtenerConfiguracion() {
        UserRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String privacidaduc = snapshot.child("PUC").getValue().toString();
                    String privacidadciu = snapshot.child("PC").getValue().toString();
                    String privacidadimg = snapshot.child("PI").getValue().toString();
                    if(privacidaduc.equals("Oculto")){
                        hideseen.setChecked(true);
                    }else if(privacidaduc.equals("Publico")){
                        showseen.setChecked(true);
                    }
                    if(privacidadciu.equals("-")){
                        hideciu.setChecked(true);
                    }else if(privacidadciu.equals("Publico")){
                        showciu.setChecked(true);
                    }
                    else if(privacidadciu.equals("Contactos")){
                       showciucontacts.setChecked(true);
                    }
                    if(privacidadimg.equals("Oculto")){
                        hideimage.setChecked(true);
                    }else if(privacidadimg.equals("Publico")){
                        showimage.setChecked(true);
                    }else if(privacidadimg.equals("Contactos")){
                        showimgcontacts.setChecked(true);
                    }
                    protectchats.setChecked(snapshot.hasChild("ProtegeChats"));
                    screenshotblock.setChecked(snapshot.hasChild("screenshotsbloqueados"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}