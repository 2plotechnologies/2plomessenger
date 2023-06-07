package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

public class DetallePostActivity extends AppCompatActivity {
    DatabaseReference rootref;
    FirebaseAuth mAuth;
    String CurrentUserId;
    ImageView postimg;
    TextView posttext, usernametxt;
    EditText txtcomentar;
    Button btnpublicarcomentario;
    ArrayList<Comentarios> comentariosArrayList;
    ComentariosAdapter adapter;
    LinearLayoutManager lm;
    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_post);
        rootref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        postimg = findViewById(R.id.imageViewPostImage);
        posttext = findViewById(R.id.textViewPostText);
        usernametxt = findViewById(R.id.textViewUserName);
        txtcomentar = findViewById(R.id.editTextComment);
        btnpublicarcomentario = findViewById(R.id.buttonComment);
        RecyclerView comentariosrecukler = findViewById(R.id.recyclerViewComments);
        String postid = getIntent().getStringExtra("post_id");
        comentariosArrayList = new ArrayList<>();
        lm = new LinearLayoutManager(this);
        comentariosrecukler.setLayoutManager(lm);
        adapter = new ComentariosAdapter(comentariosArrayList);
        comentariosrecukler.setAdapter(adapter);
        loadData(postid);
        getUsername();
        rootref.child("Comentarios").child(postid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    comentariosArrayList.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Comentarios comentarios = dataSnapshot.getValue(Comentarios.class);
                        comentariosArrayList.add(comentarios);
                    }
                    Collections.reverse(comentariosArrayList);
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
        btnpublicarcomentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contenido = txtcomentar.getText().toString();
                if(TextUtils.isEmpty(contenido)){
                    Toast.makeText(DetallePostActivity.this, R.string.escribe_algo, Toast.LENGTH_SHORT).show();;
                }else{
                    String comentarioid = rootref.child("Comentarios").push().getKey();
                    Comentarios comentario = new Comentarios();
                    comentario.setNombre_usuario(username);
                    comentario.setComentario(contenido);
                    comentario.setId_usuario(CurrentUserId);
                    comentario.setId_del_post(postid);
                    comentario.setId_comentario(comentarioid);
                    rootref.child("Comentarios").child(postid).child(comentarioid).setValue(comentario)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        txtcomentar.setText("");
                                        Toast.makeText(DetallePostActivity.this, R.string.seagrego, Toast.LENGTH_SHORT).show();
                                    }else{
                                        txtcomentar.setText("");
                                        Toast.makeText(DetallePostActivity.this, "Error: "+task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void getUsername() {
        rootref.child("Usuarios").child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    username = snapshot.child("nombre").getValue(String.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }
    private void loadData(String postid) {
        rootref.child("Posts").child(postid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String username = snapshot.child("nomuser").getValue(String.class);
                    usernametxt.setText(username);
                    if(snapshot.hasChild("imagen")&&snapshot.hasChild("texto")){
                        String imgurl = snapshot.child("imagen").getValue(String.class);
                        Picasso.get().load(imgurl).into(postimg);
                        String postText = snapshot.child("texto").getValue(String.class);
                        posttext.setText(postText);
                    } else if (!snapshot.hasChild("imagen")&&snapshot.hasChild("texto")) {
                        postimg.setVisibility(View.GONE);
                        String postText = snapshot.child("texto").getValue(String.class);
                        posttext.setText(postText);
                    }else if(snapshot.hasChild("imagen")&&!snapshot.hasChild("texto")){
                        String imgurl = snapshot.child("imagen").getValue(String.class);
                        Picasso.get().load(imgurl).into(postimg);
                        posttext.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }
}