package com.twoploapps.a2plomessenger.NewActivitys;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import androidx.appcompat.content.res.AppCompatResources;
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
import com.squareup.picasso.Picasso;
import com.twoploapps.a2plomessenger.Controllers.ChannelController;
import com.twoploapps.a2plomessenger.Models.Canal;
import com.twoploapps.a2plomessenger.R;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoCanalActivity extends AppCompatActivity {
    private EditText nombre, descripcion;
    private CircleImageView img;
    final  static  int Gallery_PICK = 1;
    private StorageReference ChannelImage;
    private ProgressDialog dialog;
    private Button editar, unfollow, eliminar;
    private DatabaseReference RootRef;
    private String CurrentUserId, id;
    private boolean isFollowing = false;
    Uri imageUri;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_canal);

        Toolbar toolbar = findViewById(R.id.toolbar_info_canal);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(getString(R.string.infocanal));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
        }

        RootRef = FirebaseDatabase.getInstance().getReference();

        nombre = findViewById(R.id.nombre_canal_edit);
        descripcion = findViewById(R.id.descripcion_canal_edit);
        img = findViewById(R.id.channel_img_edit);
        editar = findViewById(R.id.channel_button_edit);
        unfollow = findViewById(R.id.dejar_de_seguir);
        eliminar = findViewById(R.id.eliminar_canal);

        ChannelImage= FirebaseStorage.getInstance().getReference().child("ImagesCanal");
        dialog = new ProgressDialog(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        CurrentUserId = auth.getCurrentUser().getUid();
        id = getIntent().getStringExtra("channel_id");
        getRol();

        ChannelController.getData(id, new ChannelController.OnChannelDataReceived() {
            @Override
            public void onDataReceived(Canal canal) {
                if (canal != null) {
                    nombre.setText(canal.getNombre());
                    descripcion.setText(canal.getDescripcion());
                    Picasso.get().load(canal.getImagen()).into(img);
                }
            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,Gallery_PICK);
            }
        });

        unfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFollowing){
                    RootRef.child("Canales").child(id).child("Miembros").child(CurrentUserId).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            isFollowing = false;
                            Toast.makeText(InfoCanalActivity.this, R.string.dejastedeseguirestecanal, Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    RootRef.child("Canales").child(id).child("Miembros").child(CurrentUserId)
                            .child("Rol").setValue("miembro").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        isFollowing = true;
                                        unfollow.setText(R.string.dejar_de_seguir);
                                        unfollow.setBackgroundColor(getResources().getColor(R.color.red));
                                        unfollow.setBackground(AppCompatResources.getDrawable(InfoCanalActivity.this,R.drawable.boton_redondeado));
                                    }
                                }
                            });
                }
            }
        });

        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editarCanal();
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InfoCanalActivity.this);
                builder.setMessage(getString(R.string.deseas_eliminar_canal))
                        .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ChannelController.Delete(id, InfoCanalActivity.this);
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {}});
                builder.create().show();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void editarCanal() {
        String nombre_canal = nombre.getText().toString();
        String desc = descripcion.getText().toString();
        if(TextUtils.isEmpty(nombre_canal) || TextUtils.isEmpty(desc)){
            Toast.makeText(this, R.string.escribe_algo, Toast.LENGTH_SHORT).show();
            return;
        }

        dialog.setTitle(R.string.guardando);
        dialog.setMessage(getString(R.string.espere));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);

        Map<String, Object> canal = new HashMap<>();
        canal.put("nombre", nombre_canal);
        canal.put("descripcion", desc);
        ChannelController.Edit(canal,id, InfoCanalActivity.this);
        dialog.dismiss();
    }

    private void getRol() {
        RootRef.child("Canales").child(id).child("Miembros").child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String Rol = snapshot.child("Rol").getValue(String.class);
                    if(Rol!=null && Rol.equals("miembro")){
                        isFollowing = true;
                        nombre.setEnabled(false);
                        descripcion.setEnabled(false);
                        img.setEnabled(false);
                        editar.setVisibility(View.GONE);
                        eliminar.setVisibility(View.GONE);
                    }else{
                        unfollow.setVisibility(View.GONE);
                    }
                }else{
                    isFollowing = false;
                    nombre.setEnabled(false);
                    descripcion.setEnabled(false);
                    img.setEnabled(false);
                    editar.setVisibility(View.GONE);
                    eliminar.setVisibility(View.GONE);
                    unfollow.setText(R.string.seguir);
                    unfollow.setBackgroundColor(getResources().getColor(R.color.fb_color));
                    unfollow.setBackground(AppCompatResources.getDrawable(InfoCanalActivity.this,R.drawable.boton_redondeado));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_PICK && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(img);
            dialog.setTitle(R.string.enviando_imagen);
            dialog.setMessage(getString(R.string.estamos_enviando_imagen));
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);

            StorageReference filePath = ChannelImage.child(id+".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(InfoCanalActivity.this, R.string.imagen_guardada, Toast.LENGTH_SHORT).show();
                    filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                        final String downloadUri = uri.toString();
                        Map<String, Object> canal = new HashMap<>();
                        canal.put("imagen", downloadUri);
                        ChannelController.Edit(canal,id, InfoCanalActivity.this);
                        dialog.dismiss();
                    });
                }
            });
        }else{
            Toast.makeText(this, R.string.imagen_no_soportada, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }
}
