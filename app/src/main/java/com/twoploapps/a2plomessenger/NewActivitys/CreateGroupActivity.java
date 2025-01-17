package com.twoploapps.a2plomessenger.NewActivitys;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.twoploapps.a2plomessenger.Controllers.GroupController;
import com.twoploapps.a2plomessenger.Models.Grupo;
import com.twoploapps.a2plomessenger.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity {
    private EditText txt_nombre, txt_desc;
    private CircleImageView img;
    final  static  int Gallery_PICK =1;
    private StorageReference ChannelImage;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    Uri imageUri;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        Toolbar toolbar = findViewById(R.id.toolbar_crear_grupo);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(getString(R.string.crear_grupo));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
        }
        txt_nombre = findViewById(R.id.nombre_grupo);
        txt_desc = findViewById(R.id.descripcion_grupo);
        img = findViewById(R.id.group_img);
        Button createButton = findViewById(R.id.create_group_button);
        ChannelImage= FirebaseStorage.getInstance().getReference().child("ImagesGrupo");
        dialog = new ProgressDialog(this);
        auth=FirebaseAuth.getInstance();

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,Gallery_PICK);
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarGrupo();
            }
        });
    }

    private void guardarGrupo() {
        String CurrentUserId = auth.getCurrentUser().getUid();
        String nombre = txt_nombre.getText().toString();
        String desc = txt_desc.getText().toString();
        if(imageUri==null || TextUtils.isEmpty(nombre) || TextUtils.isEmpty(desc)){
            Toast.makeText(this, R.string.escribe_algo, Toast.LENGTH_SHORT).show();
            return;
        }
        dialog.setTitle(R.string.guardando);
        dialog.setMessage(getString(R.string.espere));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Grupos");
        String groupId = ref.push().getKey();
        StorageReference filePath = ChannelImage.child(groupId+".jpg");

        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    Toast.makeText(CreateGroupActivity.this, R.string.imagen_guardada, Toast.LENGTH_SHORT).show();
                    ChannelImage.child(groupId+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String downloadUri = uri.toString();
                            Grupo grupo = new Grupo(groupId, nombre, desc, downloadUri, CurrentUserId);
                            GroupController.Create(grupo, groupId, CreateGroupActivity.this);
                            dialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_PICK && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData(); // URI de la imagen seleccionada

            // Mostrar la imagen seleccionada en el ImageView usando Picasso
            Picasso.get().load(imageUri).into(img);

            // Mostrar un mensaje o realizar alguna otra acción si es necesario
            Toast.makeText(this, R.string.imagen_guardada, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.imagen_no_soportada, Toast.LENGTH_SHORT).show();
        }
    }
}
