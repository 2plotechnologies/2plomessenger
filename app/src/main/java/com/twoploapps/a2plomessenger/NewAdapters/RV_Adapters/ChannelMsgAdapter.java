package com.twoploapps.a2plomessenger.NewAdapters.RV_Adapters;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.twoploapps.a2plomessenger.AudioActivity;
import com.twoploapps.a2plomessenger.Controllers.ChannelController;
import com.twoploapps.a2plomessenger.ImagenActivity;
import com.twoploapps.a2plomessenger.InicioActivity;
import com.twoploapps.a2plomessenger.Models.Canal;
import com.twoploapps.a2plomessenger.Models.MensajeCanal;
import com.twoploapps.a2plomessenger.R;
import com.twoploapps.a2plomessenger.VideoActivity;
import com.twoploapps.a2plomessenger.cifrado;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChannelMsgAdapter extends RecyclerView.Adapter<ChannelMsgAdapter.ViewHolderChannelMessages> {

    List<MensajeCanal> my_channel_msg_List;
    String channelId;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    //String CurrentUserId=auth.getCurrentUser().getUid();

    public ChannelMsgAdapter(List<MensajeCanal>msg_list, String id){
        this.my_channel_msg_List = msg_list;
        this.channelId = id;
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
        }else if (TipoMensaje.equals("imagen")){
            if (deUsuarioId.equals(mensajeEnviadoID)){
                holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                Picasso.get().load(mensajeCanal.getMensaje()).into(holder.mensajeImagenEnviar);
            }else{
                holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                holder.mensajeImagenRecibir.setVisibility(View.VISIBLE);
                Picasso.get().load(mensajeCanal.getMensaje()).into(holder.mensajeImagenRecibir);
            }
        }else if (TipoMensaje.equals("pdf") || TipoMensaje.equals("docx")){
            if (deUsuarioId.equals(mensajeEnviadoID)){
                holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsappro-d46ee.appspot.com/o/Archivos%2Farchivos.png?alt=media&token=9de607b2-da1d-4b6c-91f2-10bfd3828baf")
                        .into(holder.mensajeImagenEnviar);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }else{
                holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                holder.mensajeImagenRecibir.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsappro-d46ee.appspot.com/o/Archivos%2Farchivos.png?alt=media&token=9de607b2-da1d-4b6c-91f2-10bfd3828baf")
                        .into(holder.mensajeImagenRecibir);
            }
        }else if(TipoMensaje.equals("mp4")){
            if (deUsuarioId.equals(mensajeEnviadoID)){
                holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/videoicon.png?alt=media&token=c0db70d9-4d53-4955-b4aa-c6089d455791")
                        .into(holder.mensajeImagenEnviar);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }else{
                holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                holder.mensajeImagenRecibir.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/videoicon.png?alt=media&token=c0db70d9-4d53-4955-b4aa-c6089d455791")
                        .into(holder.mensajeImagenRecibir);
            }
        }else if(TipoMensaje.equals("mp3")){
            if (deUsuarioId.equals(mensajeEnviadoID)){
                holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/imgbin_computer-icons-sound-icon-volume-png.png?alt=media&token=41edf2db-f52b-4894-958b-93f31ed86766")
                        .into(holder.mensajeImagenEnviar);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }else{
                holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                holder.mensajeImagenRecibir.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/imgbin_computer-icons-sound-icon-volume-png.png?alt=media&token=41edf2db-f52b-4894-958b-93f31ed86766")
                        .into(holder.mensajeImagenRecibir);
            }
        }

        if (deUsuarioId.equals(mensajeEnviadoID)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (my_channel_msg_List.get(holder.getAdapterPosition()).getTipo().equals("pdf") || my_channel_msg_List.get(holder.getAdapterPosition()).getTipo().equals("docx")){
                        CharSequence[] opciones = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.descargar),
                                holder.itemView.getContext().getString(R.string.cancelar),
                                holder.itemView.getContext().getString(R.string.eliminar_todos)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                 if (position==0){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje()));
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==2){
                                    ChannelController.EliminarMensajeCanal(holder.getAdapterPosition(), holder, my_channel_msg_List, channelId);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if (my_channel_msg_List.get(holder.getAdapterPosition()).getTipo().equals("texto")){
                        CharSequence[] opciones = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.eliminar_todos),
                                holder.itemView.getContext().getString(R.string.copiar),
                                holder.itemView.getContext().getString(R.string.cancelar),
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    ChannelController.EliminarMensajeCanal(holder.getAdapterPosition(), holder, my_channel_msg_List, channelId);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==1) {
                                    String mensaje = my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje();
                                    String mensajeDesc = cifrado.decrypt(mensaje);
                                    ClipData clipData = ClipData.newPlainText("text", mensajeDesc);
                                    ClipboardManager clipboard = (ClipboardManager) ContextCompat.getSystemService(holder.itemView.getContext(), ClipboardManager.class);
                                    if(clipboard!=null){
                                        clipboard.setPrimaryClip(clipData);
                                        Toast.makeText(holder.itemView.getContext(), R.string.mensajecopiado, Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }
                        });
                        builder.show();
                    }else if (my_channel_msg_List.get(holder.getAdapterPosition()).getTipo().equals("imagen")){
                        CharSequence[] opciones = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.ver_img),
                                holder.itemView.getContext().getString(R.string.cancelar),
                                holder.itemView.getContext().getString(R.string.eliminar_todos)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImagenActivity.class);
                                    intent.putExtra("url",my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==2){
                                    ChannelController.EliminarMensajeCanal(holder.getAdapterPosition(), holder, my_channel_msg_List, channelId);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if(my_channel_msg_List.get(holder.getAdapterPosition()).getTipo().equals("mp4")){
                        CharSequence[] opciones = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.ver_vid),
                                holder.itemView.getContext().getString(R.string.cancelar),
                                holder.itemView.getContext().getString(R.string.eliminar_todos)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    Intent intent = new Intent(holder.itemView.getContext(), VideoActivity.class);
                                    intent.putExtra("url",my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==2){
                                    ChannelController.EliminarMensajeCanal(holder.getAdapterPosition(), holder, my_channel_msg_List, channelId);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }else if(my_channel_msg_List.get(holder.getAdapterPosition()).getTipo().equals("mp3")){
                        CharSequence[] opciones = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.reproduciraudio),
                                holder.itemView.getContext().getString(R.string.cancelar),
                                holder.itemView.getContext().getString(R.string.eliminar_todos)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    Intent intent = new Intent(holder.itemView.getContext(), AudioActivity.class);
                                    intent.putExtra("audio_url",my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==2){
                                    ChannelController.EliminarMensajeCanal(holder.getAdapterPosition(), holder, my_channel_msg_List, channelId);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }else{
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (my_channel_msg_List.get(holder.getAdapterPosition()).getTipo().equals("pdf") || my_channel_msg_List.get(holder.getAdapterPosition()).getTipo().equals("docx")){
                        CharSequence[] opciones = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.descargar),
                                holder.itemView.getContext().getString(R.string.cancelar)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if (my_channel_msg_List.get(holder.getAdapterPosition()).getTipo().equals("texto")){
                        CharSequence[] opciones = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.copiar),
                                holder.itemView.getContext().getString(R.string.cancelar),
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if(position==0){
                                    String mensaje = my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje();
                                    String mensajeDesc = cifrado.decrypt(mensaje);
                                    ClipData clipData = ClipData.newPlainText("text", mensajeDesc);
                                    ClipboardManager clipboard = (ClipboardManager) ContextCompat.getSystemService(holder.itemView.getContext(), ClipboardManager.class);
                                    if(clipboard!=null){
                                        clipboard.setPrimaryClip(clipData);
                                        Toast.makeText(holder.itemView.getContext(), R.string.mensajecopiado, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        builder.show();
                    }else if (my_channel_msg_List.get(holder.getAdapterPosition()).getTipo().equals("imagen")){
                        CharSequence[] opciones = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.ver_img),
                                holder.itemView.getContext().getString(R.string.cancelar)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImagenActivity.class);
                                    intent.putExtra("url",my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }else if(my_channel_msg_List.get(holder.getAdapterPosition()).getTipo().equals("mp4")){
                        CharSequence[] opciones = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.ver_vid),
                                holder.itemView.getContext().getString(R.string.cancelar)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                              if (position==0){
                                    Intent intent = new Intent(holder.itemView.getContext(), VideoActivity.class);
                                    intent.putExtra("url",my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }else if(my_channel_msg_List.get(holder.getAdapterPosition()).getTipo().equals("mp3")){
                        CharSequence[] opciones = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.reproduciraudio),
                                holder.itemView.getContext().getString(R.string.cancelar)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    Intent intent = new Intent(holder.itemView.getContext(),AudioActivity.class);
                                    intent.putExtra("audio_url",my_channel_msg_List.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
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
