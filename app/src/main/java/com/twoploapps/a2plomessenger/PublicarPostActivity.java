package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class PublicarPostActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private String CurrentUserId;
    private StorageReference reference;
    private DatabaseReference PostRef, nomUserRef;
    private String NombreUser;
    ImageView imageView;
    Button btncargar, btnpublicar;
    EditText textPost;
    ProgressBar myprogressbar;
    Uri imageUri;
    final  static  int Gallery_PICK =1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicar_post);
        auth=FirebaseAuth.getInstance();
        CurrentUserId= auth.getCurrentUser().getUid();
        PostRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        nomUserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        reference = FirebaseStorage.getInstance().getReference().child("Posts");
        imageView = (ImageView) findViewById(R.id.verimg);
        btncargar = findViewById(R.id.cargarimg);
        btnpublicar = findViewById(R.id.publicar);
        textPost = findViewById(R.id.postedit);
        myprogressbar = findViewById(R.id.progresspost);
        obtenerNombreUser();
        verificarStrikes();
        btncargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,Gallery_PICK);
            }
        });
        btnpublicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publicarPost();
            }
        });
    }

    private void verificarStrikes() {
        DatabaseReference rf = FirebaseDatabase.getInstance().getReference();
        rf.child("Usuarios").child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("strikes")){
                        int strikes = Integer.parseInt(snapshot.child("strikes").getValue().toString());
                        if(strikes==5){
                            AlertDialog.Builder builder = new AlertDialog.Builder(PublicarPostActivity.this);
                            builder.setMessage(getString(R.string.recibiostrikes))
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.entenido, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // Este código se ejecutará al hacer clic en el botón "Entendido"
                                            Intent intent = new Intent(PublicarPostActivity.this, InicioActivity.class);
                                            startActivity(intent);
                                            // Terminamos la actividad actual para que el usuario no pueda volver atrás con el botón de "atrás"
                                            finish();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void publicarPost() {
        String postId = PostRef.push().getKey();
        //No hay ni texto ni imagen
        if(imageUri==null&& TextUtils.isEmpty(textPost.getText())){
            Toast.makeText(this, R.string.mensaje_post_vacio, Toast.LENGTH_SHORT).show();
        //Imagen sin texto
        } else if (imageUri!=null && TextUtils.isEmpty(textPost.getText())) {
            myprogressbar.setVisibility(View.VISIBLE);
            btncargar.setEnabled(false);
            textPost.setEnabled(false);
            btnpublicar.setVisibility(View.GONE);
            String postid = PostRef.push().getKey();
            StorageReference imagenPath = reference.child("posts/" + postid + ".jpg");
            imagenPath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imagenPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Date date = new Date();
                            long timestamp = date.getTime();
                            String imagenurl = uri.toString();
                            Posts post = new Posts();
                            post.setImagen(imagenurl);
                            post.setNomuser(NombreUser);
                            post.setIduser(CurrentUserId);
                            post.setPostId(postId);
                            post.setFecha(timestamp);
                            PostRef.child(postId).setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(PublicarPostActivity.this, R.string.confirma_post, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(PublicarPostActivity.this, InicioActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        Toast.makeText(PublicarPostActivity.this, "Error: "+task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PublicarPostActivity.this, "Error: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        //Texto sin imagen
        }else if(imageUri==null && !TextUtils.isEmpty(textPost.getText())){
            myprogressbar.setVisibility(View.VISIBLE);
            btncargar.setEnabled(false);
            textPost.setEnabled(false);
            btnpublicar.setVisibility(View.GONE);
            Date date = new Date();
            long timestamp = date.getTime();
            Posts post = new Posts();
            post.setTexto(textPost.getText().toString());
            post.setIduser(CurrentUserId);
            post.setNomuser(NombreUser);
            post.setPostId(postId);
            post.setFecha(timestamp);
            PostRef.child(postId).setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(PublicarPostActivity.this, R.string.confirma_post, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PublicarPostActivity.this, InicioActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(PublicarPostActivity.this, "Error:"+task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        //Imagen con texto
        }else if(!TextUtils.isEmpty(textPost.getText())&&imageUri!=null){
            myprogressbar.setVisibility(View.VISIBLE);
            btncargar.setEnabled(false);
            textPost.setEnabled(false);
            btnpublicar.setVisibility(View.GONE);
            StorageReference imagenPath = reference.child("portadas/" + postId + ".jpg");
            imagenPath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imagenPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Date date = new Date();
                            long timestamp = date.getTime();
                            String imagenurl = uri.toString();
                            Posts post = new Posts();
                            post.setImagen(imagenurl);
                            post.setNomuser(NombreUser);
                            post.setIduser(CurrentUserId);
                            post.setTexto(textPost.getText().toString());
                            post.setPostId(postId);
                            post.setFecha(timestamp);
                            PostRef.child(postId).setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(PublicarPostActivity.this, R.string.confirma_post, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(PublicarPostActivity.this, InicioActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        Toast.makeText(PublicarPostActivity.this, "Error: "+task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PublicarPostActivity.this, "Error: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_PICK && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(imageView);
        }
    }private void obtenerNombreUser() {
        nomUserRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    NombreUser = snapshot.child("nombre").getValue().toString();
                }else{
                    NombreUser = "Unknown User";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                NombreUser = "Unknown User";
            }
        });
    }
}