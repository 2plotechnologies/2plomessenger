package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
public class SetupActivity2 extends AppCompatActivity {
    private EditText nombre, ciudad, genero, edad,estado;
    private Button guardarinfo;
    private CircleImageView imagen_setup;
    private FirebaseAuth auth;
    private DatabaseReference UserRef;
    private ProgressDialog dialog;
    private String CurrenUserID;
    final  static  int Gallery_PICK =1;
    private StorageReference UserProfileImagen;
    private Toolbar toolbar;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        nombre=(EditText) findViewById(R.id.nombre_setup);
        ciudad=(EditText) findViewById(R.id.ciudad_setup);
        genero=(EditText) findViewById(R.id.genero_setup);
        edad=(EditText) findViewById(R.id.edad_setup);
        estado=(EditText) findViewById(R.id.Estado_setup);
        guardarinfo=(Button)findViewById(R.id.boton_setup);
        imagen_setup=(CircleImageView)findViewById(R.id.imagen_setup);
        toolbar = (Toolbar)findViewById(R.id.toolbar_setup);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.completa_perfil);
        dialog = new ProgressDialog(this);
        auth=FirebaseAuth.getInstance();
        CurrenUserID= auth.getCurrentUser().getUid();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String username = extras.get("username").toString();
            nombre.setText(username);
            // Ahora puedes usar username
        } else {
            // extras es nulo, no hay nada que hacer aquí
            Log.e("Mensaje", "Usuario cerro app antes de completar perfil");
        }
        estado.setText(R.string.estado_default);
        UserRef= FirebaseDatabase.getInstance().getReference().child("Usuarios");
        UserProfileImagen= FirebaseStorage.getInstance().getReference().child("ImagesPerfil");
        guardarinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GuardarInfromacionDB();
            }
        });
        imagen_setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent  intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,Gallery_PICK);

            }
        });

        UserRef.child(CurrenUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    if (snapshot.hasChild("imagen")){
                        String imagen = snapshot.child("imagen").getValue().toString();
                        Picasso.get()
                                .load(imagen)
                                .placeholder(R.drawable.defaultprofilephoto)
                                .error(R.drawable.defaultprofilephoto)
                                .into(imagen_setup);

                    }else{
                        //Toast.makeText(SetupActivity2.this, "Puede cargar una foto....", Toast.LENGTH_SHORT).show();
                        Log.i("mensaje","puede cargar una foto");
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_PICK && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode ==RESULT_OK){
                dialog.setTitle(R.string.imagen_perfil);
                dialog.setMessage(getString(R.string.estamos_guardando));
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                final Uri resultUri = result.getUri();
                StorageReference filePath = UserProfileImagen.child(CurrenUserID+".jpg");
                final File url = new File(resultUri.getPath());
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SetupActivity2.this, R.string.imagen_guardada, Toast.LENGTH_SHORT).show();
                            UserProfileImagen.child(CurrenUserID+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri = uri.toString();
                                    UserRef.child(CurrenUserID).child("imagen").setValue(downloadUri)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Picasso.get()
                                                                .load(downloadUri)
                                                                .error(R.drawable.defaultprofilephoto)
                                                                .into(imagen_setup);
                                                        Toast.makeText(SetupActivity2.this, R.string.imagen_se_guardo, Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }else{
                                                        String error = task.getException().getMessage();
                                                        Toast.makeText(SetupActivity2.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            }else{
                Toast.makeText(this, R.string.imagen_no_soportada, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }
    }
    private void GuardarInfromacionDB() {
        String nom = nombre.getText().toString().trim();
        String ciu = ciudad.getText().toString().trim();
        String gen = genero.getText().toString().trim();
        String eda = edad.getText().toString().trim();
        String est = estado.getText().toString().trim();
        int edad = 0;
        try{
            edad = Integer.parseInt(eda);
        }catch(NumberFormatException ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
        if (TextUtils.isEmpty(nom)||nom.length()>30){
            Toast.makeText(this, R.string.ingrese_su_nombre, Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(ciu)||ciu.length()>50){
            Toast.makeText(this, R.string.ingrese_su_ciudad, Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(gen)||gen.length()>10){
            Toast.makeText(this, R.string.ingrese_su_genero, Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(eda)){
            Toast.makeText(this, R.string.ingrese_su_edad, Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(est)||est.length()>140){
            Toast.makeText(this, R.string.ingrese_su_estado, Toast.LENGTH_SHORT).show();
        }else if(edad<15){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.edad_insuficiente));
            builder.setTitle("Error");
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).setNegativeButton(R.string.volver, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }else{
            dialog.setTitle(R.string.guardando);
            dialog.setMessage(getString(R.string.espere));
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (task.isSuccessful()){
                        token = task.getResult();
                        HashMap map = new HashMap();
                        map.put("nombre",nom);
                        map.put("ciudad",ciu);
                        map.put("genero", gen);
                        map.put("edad",eda);
                        map.put("estado",est);
                        map.put("token",token);
                        map.put("PC","Publico");
                        map.put("PI","Publico");
                        map.put("PUC","Publico");
                        UserRef.child(CurrenUserID).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(SetupActivity2.this, R.string.se_guardo, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    EnviarAlInicio();
                                }else{
                                    String err = task.getException().getMessage();
                                    Toast.makeText(SetupActivity2.this, "Error: "+err, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }
    private void EnviarAlInicio() {
        Intent intent = new Intent(SetupActivity2.this, InicioActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}