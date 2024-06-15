package com.twoploapps.a2plomessenger;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.twoploapps.a2plomessenger.Controllers.ChannelController;
import com.twoploapps.a2plomessenger.Controllers.GroupController;
import com.twoploapps.a2plomessenger.NewActivitys.InfoCanalActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoGrupoActivity extends AppCompatActivity {
    private TextView ng,txcode,codigogrupo;
    private EditText nombregrupo, desc_grupo;
    private CircleImageView img_grupo;
    private String currentUserid, id, rol;
    private DatabaseReference GrupoRef, UserRef;
    private StorageReference GroupImage;
    private Button btnsalir, btneliminar, btn_guardar;
    private ProgressDialog dialog;
    private FirebaseAuth auth;
    private ListView listamiembros;
    final  static  int Gallery_PICK = 1;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_grupo);
        Toolbar toolbar = findViewById(R.id.toolbar_infogrupo);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.info_grupo));
        ng = findViewById(R.id.ng);
        txcode = findViewById(R.id.txcode);
        codigogrupo = findViewById(R.id.codigogrupo);
        nombregrupo = findViewById(R.id.nombregrupo);
        desc_grupo = findViewById(R.id.descripcion_grupo_edit);
        img_grupo = findViewById(R.id.group_img_edit);
        listamiembros = findViewById(R.id.list_view);
        btn_guardar = findViewById(R.id.group_button_edit);
        btnsalir = findViewById(R.id.btnsalir);
        btneliminar = findViewById(R.id.btneliminar);
        dialog = new ProgressDialog(this);
        GrupoRef = FirebaseDatabase.getInstance().getReference().child("Grupos");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        GroupImage= FirebaseStorage.getInstance().getReference().child("ImagesGrupo");
        auth=FirebaseAuth.getInstance();
        id = getIntent().getStringExtra("group_id");
        obtenerNombresDeUsuarios(id);
        currentUserid = auth.getCurrentUser().getUid();
        codigogrupo.setText(id);
        getRol();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre_canal = nombregrupo.getText().toString();
                String desc = desc_grupo.getText().toString();
                if(TextUtils.isEmpty(nombre_canal) || TextUtils.isEmpty(desc)){
                    Toast.makeText(InfoGrupoActivity.this, R.string.escribe_algo, Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.setTitle(R.string.guardando);
                dialog.setMessage(getString(R.string.espere));
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);

                Map<String, Object> grupo = new HashMap<>();
                grupo.put("nombre", nombre_canal);
                grupo.put("descripcion", desc);
                GroupController.Edit(grupo,id, InfoGrupoActivity.this);
                dialog.dismiss();
            }
        });

        img_grupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,Gallery_PICK);
            }
        });

        btneliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InfoGrupoActivity.this);
                builder.setMessage(getString(R.string.deseas_eliminar_grupo))
                        .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                GroupController.Delete(id, InfoGrupoActivity.this);
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {}});
                builder.create().show();
            }
        });
        GrupoRef.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name = snapshot.child("nombre").getValue(String.class);
                    String desc = snapshot.child("descripcion").getValue(String.class);
                    String img = snapshot.child("imagen").getValue(String.class);
                    nombregrupo.setText(name);
                    desc_grupo.setText(desc);
                    Picasso.get().load(img).placeholder(R.drawable.defaultprofilephoto).into(img_grupo);
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

    private void getRol() {
        GrupoRef.child(id).child("Miembros").child(currentUserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String Rol = snapshot.child("Rol").getValue(String.class);
                    if(Rol!=null && Rol.equals("miembro")){
                        rol = "miembro";
                        nombregrupo.setEnabled(false);
                        desc_grupo.setEnabled(false);
                        img_grupo.setEnabled(false);
                        btn_guardar.setVisibility(View.GONE);
                        btneliminar.setVisibility(View.GONE);
                    }else if(Rol!=null && Rol.equals("creador")){
                        rol = "admin";
                    }else{
                        rol = "admin";
                        btneliminar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
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
                imageUri = result.getUri();
                Picasso.get().load(imageUri).into(img_grupo);
                dialog.setTitle(R.string.enviando_imagen);
                dialog.setMessage(getString(R.string.estamos_enviando_imagen));
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);

                StorageReference filePath = GroupImage.child(id+".jpg");
                filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(InfoGrupoActivity.this, R.string.imagen_guardada, Toast.LENGTH_SHORT).show();
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri = uri.toString();
                                    Map<String, Object> grupo = new HashMap<>();
                                    grupo.put("imagen", downloadUri);
                                    GroupController.Edit(grupo,id, InfoGrupoActivity.this);
                                    dialog.dismiss();
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
}