package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserPostsActivity extends AppCompatActivity {
    private DatabaseReference PostRef, UserRef;
    private FirebaseAuth auth;
    private String CurrentUserId;
    private StorageReference reference;
    private String NombreUser;
    ImageView imageView;
    Button btncargar, btnactualizar;
    EditText textPost;
    ProgressBar myprogressbar;
    Uri imageUri;
    final  static  int Gallery_PICK =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);
        auth=FirebaseAuth.getInstance();
        CurrentUserId=auth.getCurrentUser().getUid();
        PostRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        reference = FirebaseStorage.getInstance().getReference().child("Posts");
        imageView = (ImageView) findViewById(R.id.imgpost);
        btncargar = findViewById(R.id.updateimg);
        btnactualizar = findViewById(R.id.updatepost);
        textPost = findViewById(R.id.posttextedit);
        myprogressbar = findViewById(R.id.progresspostupdate);
        obtenerNombreUser();
        getPostData();
        btncargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,Gallery_PICK);
            }
        });
        btnactualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePost();
            }
        });
    }
    private void getPostData() {
        String postId = getIntent().getExtras().get("post_id").toString();
        PostRef.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("imagen")&&snapshot.hasChild("texto")){
                        imageUri = Uri.parse(snapshot.child("imagen").getValue().toString());
                        Picasso.get().load(imageUri).into(imageView);
                        textPost.setText(snapshot.child("texto").getValue().toString());
                    }else if(!snapshot.hasChild("imagen")&&snapshot.hasChild("texto")){
                        textPost.setText(snapshot.child("texto").getValue().toString());
                    }else if(snapshot.hasChild("imagen")&&!snapshot.hasChild("texto")){
                        imageUri = Uri.parse(snapshot.child("imagen").getValue().toString());
                        Picasso.get().load(imageUri).into(imageView);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void updatePost() {
        String postId = getIntent().getExtras().get("post_id").toString();
        if(TextUtils.isEmpty(textPost.getText())){
            Toast.makeText(this, R.string.mensaje_post_vacio, Toast.LENGTH_SHORT).show();
        }else{
            myprogressbar.setVisibility(View.VISIBLE);
            btncargar.setEnabled(false);
            textPost.setEnabled(false);
            btnactualizar.setVisibility(View.GONE);
            Date date = new Date();
            long timestamp = date.getTime();
            Map<String, Object> postUpdates = new HashMap<>();
            postUpdates.put("texto", textPost.getText().toString());
            postUpdates.put("fecha", timestamp);
            PostRef.child(postId).updateChildren(postUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(UserPostsActivity.this, R.string.guardado_exitosamente, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserPostsActivity.this, InicioActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(UserPostsActivity.this, "Error:"+task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_PICK && resultCode == RESULT_OK && data != null){
            myprogressbar.setVisibility(View.VISIBLE);
            btncargar.setEnabled(false);
            textPost.setEnabled(false);
            btnactualizar.setVisibility(View.GONE);
            String postId = getIntent().getExtras().get("post_id").toString();
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(imageView);
            StorageReference imagenPath = reference.child("posts/" + postId + ".jpg");
            imagenPath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imagenPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Date date = new Date();
                            long timestamp = date.getTime();
                            Map<String, Object> postUpdates = new HashMap<>();
                            postUpdates.put("imagen", uri.toString());
                            postUpdates.put("fecha", timestamp);
                            PostRef.child(postId).updateChildren(postUpdates);
                            Toast.makeText(UserPostsActivity.this, R.string.imagen_guardada, Toast.LENGTH_SHORT).show();
                            myprogressbar.setVisibility(View.GONE);
                            btncargar.setEnabled(true);
                            textPost.setEnabled(true);
                            btnactualizar.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Toast.makeText(UserPostsActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void obtenerNombreUser() {
        UserRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
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