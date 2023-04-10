package com.twoploapps.a2plomessenger;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactosFragment extends Fragment {

    private View ContactosView;
    private RecyclerView ContactosLista;
    private DatabaseReference ContactosRef, UserRef;
    private FirebaseAuth auth;
    private String CurrentUserId;

    public ContactosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ContactosView = inflater.inflate(R.layout.fragment_contactos, container, false);
        auth=FirebaseAuth.getInstance();
        CurrentUserId=auth.getCurrentUser().getUid();
        ContactosRef= FirebaseDatabase.getInstance().getReference().child("Contactos").child(CurrentUserId);
        UserRef=FirebaseDatabase.getInstance().getReference().child("Usuarios");
        ContactosLista=(RecyclerView)ContactosView.findViewById(R.id.contactoslista);
        ContactosLista.setLayoutManager(new LinearLayoutManager(getContext()));
        return ContactosView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contactos>()
                .setQuery(ContactosRef, Contactos.class).build();
        FirebaseRecyclerAdapter<Contactos, ContactosViewHolder>adapter = new FirebaseRecyclerAdapter<Contactos, ContactosViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactosViewHolder holder, int position, @NonNull Contactos model) {
                String UserIds = getRef(position).getKey();
                UserRef.child(UserIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String privacidadconexion = snapshot.child("PUC").getValue().toString();
                            if (privacidadconexion.equals("Oculto")){
                                holder.usuarioactivo.setVisibility(View.GONE);
                            }else{
                                if(snapshot.child("estadoUser").hasChild("estado")){
                                    String estado = snapshot.child("estadoUser").child("estado").getValue().toString();
                                    String fecha = snapshot.child("estadoUser").child("fecha").getValue().toString();
                                    String hora = snapshot.child("estadoUser").child("hora").getValue().toString();
                                    if(estado.equals("activo")){
                                        holder.usuarioactivo.setVisibility(View.VISIBLE);
                                    }else if(estado.equals("inactivo")){
                                        holder.usuarioactivo.setVisibility(View.GONE);
                                    }
                                }else{
                                    holder.usuarioactivo.setVisibility(View.GONE);
                                }
                            }
                            if(snapshot.hasChild("imagen")){
                                String privacidadimg = snapshot.child("PI").getValue().toString();
                                String privacidadciu = snapshot.child("PC").getValue().toString();
                                String nombreu = snapshot.child("nombre").getValue().toString();
                                if(privacidadciu.equals("-")){
                                    holder.ciudad.setText("-");
                                }else{
                                    String ciudadu = snapshot.child("ciudad").getValue().toString();
                                    holder.ciudad.setText(ciudadu);
                                }
                                String estadou = snapshot.child("estado").getValue().toString();
                                if(privacidadimg.equals("Oculto")){
                                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/pngwing.com.png?alt=media&token=1d2dff28-0fd1-4caf-9ca0-b6192b0fc8c2").into(holder.imagen);
                                }else{
                                    String imagenu = snapshot.child("imagen").getValue().toString();
                                    Picasso.get().load(imagenu).placeholder(R.drawable.defaultprofilephoto).into(holder.imagen);
                                }

                                holder.nombre.setText(nombreu);
                                holder.estado.setText(estadou);
                            }else{
                                String privacidadciu = snapshot.child("PC").getValue().toString();
                                String nombreu = snapshot.child("nombre").getValue().toString();
                                if(privacidadciu.equals("-")){
                                    holder.ciudad.setText("-");
                                }else{
                                    String ciudadu = snapshot.child("ciudad").getValue().toString();
                                    holder.ciudad.setText(ciudadu);
                                }
                                String estadou = snapshot.child("estado").getValue().toString();
                                holder.nombre.setText(nombreu);
                                holder.estado.setText(estadou);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent, false);
                ContactosViewHolder viewHolder = new ContactosViewHolder(view);
                return viewHolder;
            }
        };
        ContactosLista.setAdapter(adapter);
        adapter.startListening();
    }
    public static class ContactosViewHolder extends RecyclerView.ViewHolder{
             TextView nombre, ciudad, estado;
             CircleImageView imagen;
             ImageView usuarioactivo;
        public ContactosViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.user_nombre);
            ciudad = itemView.findViewById(R.id.user_ciudad);
            estado = itemView.findViewById(R.id.user_estado);
            imagen = itemView.findViewById(R.id.user_image_perfil);
            usuarioactivo = itemView.findViewById(R.id.user_activo);
        }
    }
}