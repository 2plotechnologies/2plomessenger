package com.twoploapps.a2plomessenger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class SolicitudesFragment extends Fragment {

    private View SolicitudesFragmentView;
    private RecyclerView ReciclerSolicitudesLista;
    private DatabaseReference SolicitudesRef, UserRef, ContactosRef;
    private FirebaseAuth auth;
    private String CurrentUserId;

    public SolicitudesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SolicitudesFragmentView = inflater.inflate(R.layout.fragment_solicitudes, container,false);
        ReciclerSolicitudesLista =(RecyclerView) SolicitudesFragmentView.findViewById(R.id.resiklersolicitudeslista);
        ReciclerSolicitudesLista.setLayoutManager(new LinearLayoutManager(getContext()));
        SolicitudesRef=FirebaseDatabase.getInstance().getReference().child("Solicitudes");
        UserRef=FirebaseDatabase.getInstance().getReference().child("Usuarios");
        ContactosRef=FirebaseDatabase.getInstance().getReference().child("Contactos");
        auth = FirebaseAuth.getInstance();
        CurrentUserId=auth.getCurrentUser().getUid();
        return SolicitudesFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contactos> options = new  FirebaseRecyclerOptions.Builder<Contactos>()
                .setQuery(SolicitudesRef.child(CurrentUserId), Contactos.class)
                .build();

        FirebaseRecyclerAdapter<Contactos, SolicitudesViewHolder> adapter = new FirebaseRecyclerAdapter<Contactos, SolicitudesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SolicitudesViewHolder holder, int position, @NonNull Contactos model) {

                holder.itemView.findViewById(R.id.solicitud_aceptar_boton).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.solicitud_caneclar_boton).setVisibility(View.VISIBLE);

                final String user_id = getRef(position).getKey();
                DatabaseReference getTipo = getRef(position).child("tipo").getRef();
                getTipo.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){
                            String tipo = snapshot.getValue().toString();
                            if (tipo.equals("recibido")){
                                UserRef.child(user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            if (snapshot.hasChild("imagen")) {
                                                String privacidadimg = snapshot.child("PI").getValue().toString();
                                                if(privacidadimg.equals("Oculto")){
                                                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/pngwing.com.png?alt=media&token=1d2dff28-0fd1-4caf-9ca0-b6192b0fc8c2").into(holder.images);
                                                }else{
                                                    String img = snapshot.child("imagen").getValue().toString();
                                                    Picasso.get().load(img).placeholder(R.drawable.defaultprofilephoto).into(holder.images);
                                                }
                                            }
                                            final String nom;
                                            nom = snapshot.child("nombre").getValue().toString();
                                            String ciu;
                                            ciu = snapshot.child("ciudad").getValue().toString();
                                            String est = snapshot.child("estado").getValue().toString();
                                            String privacidadciu = snapshot.child("PC").getValue().toString();
                                            holder.nombres.setText(nom);
                                            if(privacidadciu.equals("-")){
                                                holder.ciudades.setText("-");
                                            }else{
                                                holder.ciudades.setText(ciu);
                                            }
                                            holder.estados.setText(R.string.solictud_recibida);
                                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    CharSequence opciones[] = new CharSequence[]{
                                                            getString(R.string.aceptar),
                                                            getString(R.string.rechazar)
                                                    };
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle(getString(R.string.solicitud_de)+ nom);
                                                    builder.setItems(opciones, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int i) {
                                                            if (i == 0){
                                                                ContactosRef.child(CurrentUserId).child(user_id).child("Contacto").setValue("Aceptado").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            ContactosRef.child(user_id).child(CurrentUserId).child("Contacto").setValue("Aceptado").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        SolicitudesRef.child(CurrentUserId).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    SolicitudesRef.child(user_id).child(CurrentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            Toast.makeText(getContext(), R.string.nuevo_contacto, Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                            if (i == 1){
                                                                SolicitudesRef.child(CurrentUserId).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            SolicitudesRef.child(user_id).child(CurrentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    Toast.makeText(getContext(), R.string.solicitud_eliminada, Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });

                                                            }
                                                        }
                                                    });
                                                    builder.show();
                                                }
                                            });
                                            holder.aceptar.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    ContactosRef.child(CurrentUserId).child(user_id).child("Contacto").setValue("Aceptado").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                ContactosRef.child(user_id).child(CurrentUserId).child("Contacto").setValue("Aceptado").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            SolicitudesRef.child(CurrentUserId).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        SolicitudesRef.child(user_id).child(CurrentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                Toast.makeText(getContext(), R.string.nuevo_contacto, Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                            holder.cancelar.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    SolicitudesRef.child(CurrentUserId).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                SolicitudesRef.child(user_id).child(CurrentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        Toast.makeText(getContext(), R.string.solicitud_eliminada, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }});

                            }else if (tipo.equals("enviado")){
                                Button solicitu_aceptar =holder.itemView.findViewById(R.id.solicitud_aceptar_boton);
                                solicitu_aceptar.setText(R.string.enviada);
                                holder.itemView.findViewById(R.id.solicitud_caneclar_boton).setVisibility(View.GONE);


                                UserRef.child(user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("imagen")) {
                                            String privacidadimg = snapshot.child("PI").getValue().toString();
                                            if(privacidadimg.equals("Oculto")){
                                                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/pngwing.com.png?alt=media&token=1d2dff28-0fd1-4caf-9ca0-b6192b0fc8c2").into(holder.images);
                                            }else{
                                                String img = snapshot.child("imagen").getValue().toString();
                                                Picasso.get().load(img).placeholder(R.drawable.defaultprofilephoto).into(holder.images);
                                            }
                                        }
                                        String privacidadCiu = snapshot.child("PC").getValue().toString();
                                        final String nom = snapshot.child("nombre").getValue().toString();
                                        String ciu = snapshot.child("ciudad").getValue().toString();
                                        //String est = snapshot.child("estado").getValue().toString();
                                        holder.nombres.setText(nom);
                                        if(privacidadCiu.equals("-")){
                                            holder.ciudades.setText("-");
                                        }else{
                                            holder.ciudades.setText(ciu);
                                        }
                                        holder.estados.setText(getString(R.string.enviaste_solicitud)+nom);
                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence opciones[] = new CharSequence[]{
                                                        getString(R.string.cancelar)
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(getString(R.string.confirmacion_cancelar));
                                                builder.setItems(opciones, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {
                                                        if (i == 0){
                                                            SolicitudesRef.child(CurrentUserId).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        SolicitudesRef.child(user_id).child(CurrentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                Toast.makeText(getContext(), R.string.cancelaste_la_solicitud, Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });

                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }});

                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }});

            }
            @NonNull
            @Override
            public SolicitudesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.user_display_layout,parent,false);
                SolicitudesViewHolder viewHolder = new SolicitudesViewHolder(view);
                return viewHolder;
            }
        };
        ReciclerSolicitudesLista.setAdapter(adapter);
        adapter.startListening();
    }

    private static class SolicitudesViewHolder extends RecyclerView.ViewHolder{
        TextView nombres,ciudades, estados;
        CircleImageView images;
        Button aceptar, cancelar;
        public SolicitudesViewHolder(@NonNull View itemView) {
            super(itemView);
            nombres=itemView.findViewById(R.id.user_nombre);
            ciudades=itemView.findViewById(R.id.user_ciudad);
            estados=itemView.findViewById(R.id.user_estado);
            images=itemView.findViewById(R.id.user_image_perfil);
            aceptar=itemView.findViewById(R.id.solicitud_aceptar_boton);
            cancelar=itemView.findViewById(R.id.solicitud_caneclar_boton);
        }
    }
}