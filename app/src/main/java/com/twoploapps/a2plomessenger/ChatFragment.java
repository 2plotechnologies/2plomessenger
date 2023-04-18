package com.twoploapps.a2plomessenger;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Constructor;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatFragment extends Fragment {
    private View ChatViewUnica;
    private RecyclerView ChatLista;
    private DatabaseReference ContactosRef, UserRef;
    private FirebaseAuth auth;
    private String CurrentUserId;
    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        auth=FirebaseAuth.getInstance();
        CurrentUserId=auth.getCurrentUser().getUid();
        ContactosRef= FirebaseDatabase.getInstance().getReference().child("Contactos").child(CurrentUserId);
        UserRef= FirebaseDatabase.getInstance().getReference().child("Usuarios");
        ChatViewUnica = inflater.inflate(R.layout.fragment_chat, container, false);
        ChatLista=(RecyclerView)ChatViewUnica.findViewById(R.id.chat_lista);
        ChatLista.setLayoutManager(new LinearLayoutManager(getContext()));
        ImageButton btnbuscar = ChatViewUnica.findViewById(R.id.btnbuscarcontactos);
        btnbuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),BuscarAmigosActivity.class);
                startActivity(intent);
            }
        });
        return ChatViewUnica;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contactos> options = new FirebaseRecyclerOptions.Builder<Contactos>()
                .setQuery(ContactosRef, Contactos.class).build();
        FirebaseRecyclerAdapter<Contactos, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contactos, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contactos model) {
                final String userIds =getRef(position).getKey();
                final String[] imagens = {"default"};
                UserRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            if(snapshot.hasChild("imagen")){
                                String privacidadimg = snapshot.child("PI").getValue().toString();
                                if(privacidadimg.equals("Oculto")){
                                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/pngwing.com.png?alt=media&token=1d2dff28-0fd1-4caf-9ca0-b6192b0fc8c2").into(holder.imagenc);
                                }else{
                                    imagens[0] = snapshot.child("imagen").getValue().toString();
                                    Picasso.get().load(imagens[0]).placeholder(R.drawable.defaultprofilephoto).into(holder.imagenc);
                                }

                            }
                            final String nombres = snapshot.child("nombre").getValue().toString();
                            String privacidadciu = snapshot.child("PC").getValue().toString();
                            if(privacidadciu.equals("-")){
                                holder.ciudadc.setText("-");
                            }else{
                                String ciudads = snapshot.child("ciudad").getValue().toString();
                                holder.ciudadc.setText(ciudads);
                            }
                            String estadoc = snapshot.child("estado").getValue().toString();
                            holder.nombrec.setText(nombres);
                            String privacidadest = snapshot.child("PUC").getValue().toString();
                            if(privacidadest.equals("Oculto")){
                                holder.estadoc.setText(estadoc);
                            }else{
                                holder.estadoc.setText(estadoc+"\n"+R.string.ultima_conexion_fragment+"\nhora");
                                if(snapshot.child("estadoUser").hasChild("estado")){
                                    String estado = snapshot.child("estadoUser").child("estado").getValue().toString();
                                    String fecha = snapshot.child("estadoUser").child("fecha").getValue().toString();
                                    String hora = snapshot.child("estadoUser").child("hora").getValue().toString();
                                    if(estado.equals("activo")){
                                        holder.estadoc.setText(estadoc + "\n"+holder.itemView.getContext().getString(R.string.activo));
                                    }else if(estado.equals("inactivo")){
                                        holder.estadoc.setText(estadoc+"\n"+holder.itemView.getContext().getString(R.string.ultima_conexion_fragment) + fecha + "\n" + hora);
                                    }
                                }else{
                                    holder.estadoc.setText(estadoc + "\n"+holder.itemView.getContext().getString(R.string.inactivo));
                                }
                            }

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    UserRef.child(CurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                if(snapshot.hasChild("ProtegeChats")&&snapshot.hasChild("ClaveChats")){
                                                    String pass = snapshot.child("ClaveChats").getValue().toString();
                                                    String decryptedPass = cifrado.decrypt(pass);
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                                                    builder.setTitle(R.string.ingresa_password);
                                                    final EditText input = new EditText(holder.itemView.getContext());
                                                    input.setHint(R.string.introduce_tu_contrase_a);
                                                    input.setTextColor(Color.BLACK);
                                                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                                    builder.setView(input);
                                                    builder.setCancelable(false);
                                                    builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            String clave = input.getText().toString().trim();
                                                            if(!clave.isEmpty()) {
                                                                if(!clave.equals(decryptedPass)){
                                                                    Toast.makeText(holder.itemView.getContext(), R.string.claveincorrecta, Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent(holder.itemView.getContext(),InicioActivity.class);
                                                                    startActivity(intent);
                                                                }else{
                                                                    Intent intent = new Intent(getContext(), ChatActivity.class);
                                                                    intent.putExtra("user_id", userIds);
                                                                    intent.putExtra("user_nombre", nombres);
                                                                    intent.putExtra("user_imagen", imagens[0]);
                                                                    startActivity(intent);
                                                                }
                                                            }
                                                        }
                                                    }).setNegativeButton(R.string.cancelar,null);
                                                    builder.show();
                                                }else{
                                                    Intent intent = new Intent(getContext(), ChatActivity.class);
                                                    intent.putExtra("user_id", userIds);
                                                    intent.putExtra("user_nombre", nombres);
                                                    intent.putExtra("user_imagen", imagens[0]);
                                                    startActivity(intent);
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}});
                                }
                            });
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }});
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent, false);
                return new ChatsViewHolder(view);
            }
        };
        ChatLista.setAdapter(adapter);
        adapter.startListening();
    }
    public static class ChatsViewHolder extends RecyclerView.ViewHolder{
        CircleImageView imagenc;
        TextView nombrec, ciudadc, estadoc;
        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenc = itemView.findViewById(R.id.user_image_perfil);
            nombrec = itemView.findViewById(R.id.user_nombre);
            ciudadc = itemView.findViewById(R.id.user_ciudad);
            estadoc = itemView.findViewById(R.id.user_estado);
        }
    }
}