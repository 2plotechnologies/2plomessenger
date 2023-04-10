package com.twoploapps.a2plomessenger;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsFragment extends Fragment {
    private View PostsView;
    private RecyclerView PostLista;
    private DatabaseReference PostRef;
    private FirebaseAuth auth;
    private String CurrentUserId;
    ArrayList<Posts> postsArrayList;
    PostsAdapter adapter;
    LinearLayoutManager lm;
    public PostsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImageButton publicar;
        PostsView = inflater.inflate(R.layout.fragment_posts, container, false);
        publicar = PostsView.findViewById(R.id.btn_register);
        auth=FirebaseAuth.getInstance();
        CurrentUserId=auth.getCurrentUser().getUid();
        PostRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        PostLista=(RecyclerView)PostsView.findViewById(R.id.postslista);
        lm = new LinearLayoutManager(getActivity());
        PostLista.setLayoutManager(lm);
        postsArrayList = new ArrayList<>();
        adapter = new PostsAdapter(postsArrayList);
        publicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),PublicarPostActivity.class);
                startActivity(intent);
            }
        });
        // Inflate the layout for this fragment
        return PostsView;
    }
    @Override
    public void onStart() {
        super.onStart();
        PostLista.setAdapter(adapter);
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference("Contactos").child(CurrentUserId);
        ArrayList<String> contactIds = new ArrayList<>();
        // Query para buscar los posts que no tienen fecha
        Query query = postsRef.orderByChild("fecha").equalTo(null);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Recorremos los posts sin fecha y les asignamos una por defecto
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Posts post = postSnapshot.getValue(Posts.class);
                    Date date = new Date();
                    long timestamp = date.getTime();
                    post.setFecha(timestamp);
                    postsRef.child(post.getPostId()).child("fecha").setValue(post.getFecha());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error al buscar posts sin fecha", databaseError.toException());
            }
        });
        contactsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    contactIds.clear(); // Limpiar la lista antes de agregar nuevos elementos
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String contactId = dataSnapshot.getKey(); // Obtener el ID del contacto
                        contactIds.add(contactId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        PostRef.orderByChild("fecha").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    postsArrayList.clear(); // limpia la lista antes de agregar nuevos elementos
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String userId = dataSnapshot.child("iduser").getValue(String.class);
                        if (contactIds.contains(userId) || Objects.equals(userId, CurrentUserId)) { // Comprobar si el usuario es un contacto del usuario actual
                            Posts post = dataSnapshot.getValue(Posts.class);
                            postsArrayList.add(post);
                        }
                    }
                    Collections.reverse(postsArrayList);
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}