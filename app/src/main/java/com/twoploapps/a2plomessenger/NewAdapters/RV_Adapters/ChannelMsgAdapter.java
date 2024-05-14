package com.twoploapps.a2plomessenger.NewAdapters.RV_Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.twoploapps.a2plomessenger.Models.Canal;
import com.twoploapps.a2plomessenger.Models.MensajeCanal;
import com.twoploapps.a2plomessenger.R;
import com.twoploapps.a2plomessenger.cifrado;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChannelMsgAdapter extends RecyclerView.Adapter<ChannelMsgAdapter.ViewHolderChannelMessages> {

    List<MensajeCanal> my_channel_msg_List;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    //String CurrentUserId=auth.getCurrentUser().getUid();

    public ChannelMsgAdapter(List<MensajeCanal>msg_list){
        this.my_channel_msg_List = msg_list;
    }

    @NonNull
    @Override
    public ChannelMsgAdapter.ViewHolderChannelMessages onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usuario_mensaje_layout, parent,false);
        return new ViewHolderChannelMessages(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelMsgAdapter.ViewHolderChannelMessages holder, int position) {
        String mensajeEnviadoID = auth.getCurrentUser().getUid();
        MensajeCanal mensajeCanal = my_channel_msg_List.get(position);
        String deUsuarioId = mensajeCanal.getDe();
        String TipoMensaje = mensajeCanal.getTipo();

        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios").child(deUsuarioId);
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if (snapshot.hasChild("imagen")){
                        String privacidadimg = snapshot.child("PI").getValue().toString();
                        if(privacidadimg.equals("Oculto")){
                            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/pngwing.com.png?alt=media&token=1d2dff28-0fd1-4caf-9ca0-b6192b0fc8c2").into(holder.recibirImagenPerfil);
                        }else{
                            String ImagenRecibido = snapshot.child("imagen").getValue().toString();
                            Picasso.get().load(ImagenRecibido).placeholder(R.drawable.defaultprofilephoto).into(holder.recibirImagenPerfil);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});

        holder.recibirMensajeTexto.setVisibility(View.GONE);
        holder.recibirImagenPerfil.setVisibility(View.GONE);
        holder.enviarMensajeTexto.setVisibility(View.GONE);
        holder.mensajeImagenRecibir.setVisibility(View.GONE);
        holder.mensajeImagenEnviar.setVisibility(View.GONE);

        if (TipoMensaje.equals("texto")){
            if (deUsuarioId.equals(mensajeEnviadoID)){
                holder.enviarMensajeTexto.setVisibility(View.VISIBLE);
                holder.enviarMensajeTexto.setBackgroundResource(R.drawable.enviar_mensaje_layout);
                holder.enviarMensajeTexto.setTextColor(Color.WHITE);
                holder.enviarMensajeTexto.setText(cifrado.decrypt(mensajeCanal.getMensaje()) + "\n\n"+mensajeCanal.getFecha()+" - "+mensajeCanal.getHora());
            }else{
                holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                holder.recibirMensajeTexto.setVisibility(View.VISIBLE);
                holder.recibirMensajeTexto.setBackgroundResource(R.drawable.recibir_mensaje_layout);
                holder.recibirMensajeTexto.setTextColor(Color.WHITE);
                holder.recibirMensajeTexto.setText(cifrado.decrypt(mensajeCanal.getMensaje()) + "\n\n"+mensajeCanal.getFecha()+" - "+mensajeCanal.getHora());
            }
        }
    }

    @Override
    public int getItemCount() {
        return my_channel_msg_List.size();
    }

    public static class ViewHolderChannelMessages extends RecyclerView.ViewHolder{
        public EmojiTextView enviarMensajeTexto, recibirMensajeTexto;
        public CircleImageView recibirImagenPerfil;
        public ImageView mensajeImagenEnviar, mensajeImagenRecibir;
        public ViewHolderChannelMessages(@NonNull View itemView) {
            super(itemView);
            enviarMensajeTexto = itemView.findViewById(R.id.enviar_mensaje);
            recibirMensajeTexto = itemView.findViewById(R.id.recibir_mensaje);
            recibirImagenPerfil = itemView.findViewById(R.id.mensaje_imagen_perfil);
            mensajeImagenEnviar = itemView.findViewById(R.id.mensaje_enviar_imagen);
            mensajeImagenRecibir = itemView.findViewById(R.id.mensaje_recibir_imagen);
        }
    }
}
