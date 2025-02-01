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
import com.twoploapps.a2plomessenger.Controllers.GroupController;
import com.twoploapps.a2plomessenger.ImagenActivity;
import com.twoploapps.a2plomessenger.InicioActivity;
import com.twoploapps.a2plomessenger.Models.MensajeGrupo;
import com.twoploapps.a2plomessenger.R;
import com.twoploapps.a2plomessenger.VideoActivity;
import com.twoploapps.a2plomessenger.cifrado;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolderGroupMessages>{

    List<MensajeGrupo> my_group_msg_List;
    String groupId;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    public GroupMessageAdapter(List<MensajeGrupo> msg_list, String id){
        this.my_group_msg_List = msg_list;
        this.groupId = id;
    }
    @NonNull
    @Override
    public ViewHolderGroupMessages onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usuario_mensaje_layout, parent,false);
        return new GroupMessageAdapter.ViewHolderGroupMessages(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderGroupMessages holder, int position) {
        String mensajeEnviadoID = auth.getCurrentUser().getUid();
        MensajeGrupo mensajeGrupo = my_group_msg_List.get(position);
        String deUsuarioId = mensajeGrupo.getDe();
        String TipoMensaje = mensajeGrupo.getTipo();

        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios").child(deUsuarioId);
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if (snapshot.hasChild("imagen")){
                        String privacidadimg = snapshot.child("PI").getValue().toString();
                        if(privacidadimg.equals("Oculto")){
                            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/defaultprofilephoto.png?alt=media&token=d2f0f0de-2386-45bc-952a-aedeea866b0c").into(holder.recibirImagenPerfil);
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

        switch (TipoMensaje) {
            case "texto":
                if (deUsuarioId.equals(mensajeEnviadoID)) {
                    holder.enviarMensajeTexto.setVisibility(View.VISIBLE);
                    holder.enviarMensajeTexto.setBackgroundResource(R.drawable.enviar_mensaje_layout);
                    holder.enviarMensajeTexto.setTextColor(Color.WHITE);
                    holder.enviarMensajeTexto.setText(cifrado.decrypt(mensajeGrupo.getMensaje()) + "\n\n" + mensajeGrupo.getFecha() + " - " + mensajeGrupo.getHora());
                } else {
                    holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                    holder.recibirMensajeTexto.setVisibility(View.VISIBLE);
                    holder.recibirMensajeTexto.setBackgroundResource(R.drawable.recibir_mensaje_layout);
                    holder.recibirMensajeTexto.setTextColor(Color.WHITE);
                    holder.recibirMensajeTexto.setText(mensajeGrupo.getUsername() + "\n\n" + cifrado.decrypt(mensajeGrupo.getMensaje()) + "\n\n" + mensajeGrupo.getFecha() + " - " + mensajeGrupo.getHora());
                }
                break;
            case "imagen":
                if (deUsuarioId.equals(mensajeEnviadoID)) {
                    holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                    Picasso.get().load(mensajeGrupo.getMensaje()).into(holder.mensajeImagenEnviar);
                } else {
                    holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                    holder.mensajeImagenRecibir.setVisibility(View.VISIBLE);
                    Picasso.get().load(mensajeGrupo.getMensaje()).into(holder.mensajeImagenRecibir);
                }
                break;
            case "pdf":
            case "docx":
                if (deUsuarioId.equals(mensajeEnviadoID)) {
                    holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/Archivos%2Ffile.png?alt=media&token=fdba3507-0171-4b33-b8ce-7a7295d30686")
                            .into(holder.mensajeImagenEnviar);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(my_group_msg_List.get(holder.getAdapterPosition()).getMensaje()));
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                } else {
                    holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                    holder.mensajeImagenRecibir.setVisibility(View.VISIBLE);

                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/Archivos%2Ffile.png?alt=media&token=fdba3507-0171-4b33-b8ce-7a7295d30686")
                            .into(holder.mensajeImagenRecibir);
                }
                break;
            case "mp4":
                if (deUsuarioId.equals(mensajeEnviadoID)) {
                    holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/videoicon.png?alt=media&token=c0db70d9-4d53-4955-b4aa-c6089d455791")
                            .into(holder.mensajeImagenEnviar);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(my_group_msg_List.get(holder.getAdapterPosition()).getMensaje()));
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                } else {
                    holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                    holder.mensajeImagenRecibir.setVisibility(View.VISIBLE);

                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/videoicon.png?alt=media&token=c0db70d9-4d53-4955-b4aa-c6089d455791")
                            .into(holder.mensajeImagenRecibir);
                }
                break;
            case "mp3":
                if (deUsuarioId.equals(mensajeEnviadoID)) {
                    holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/pngwing.com%20(5).png?alt=media&token=7b9291bf-53da-4909-a994-3964d9676332")
                            .into(holder.mensajeImagenEnviar);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(my_group_msg_List.get(holder.getAdapterPosition()).getMensaje()));
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                } else {
                    holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                    holder.mensajeImagenRecibir.setVisibility(View.VISIBLE);

                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/pngwing.com%20(5).png?alt=media&token=7b9291bf-53da-4909-a994-3964d9676332")
                            .into(holder.mensajeImagenRecibir);
                }
                break;
        }

        if (deUsuarioId.equals(mensajeEnviadoID)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (my_group_msg_List.get(holder.getAdapterPosition()).getTipo().equals("pdf") || my_group_msg_List.get(holder.getAdapterPosition()).getTipo().equals("docx")){
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
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(my_group_msg_List.get(holder.getAdapterPosition()).getMensaje()));
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==2){
                                    GroupController.EliminarMensajeGrupo(holder.getAdapterPosition(), holder, my_group_msg_List, groupId);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if (my_group_msg_List.get(holder.getAdapterPosition()).getTipo().equals("texto")){
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
                                    GroupController.EliminarMensajeGrupo(holder.getAdapterPosition(), holder, my_group_msg_List, groupId);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==1) {
                                    String mensaje = my_group_msg_List.get(holder.getAdapterPosition()).getMensaje();
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
                    }else if (my_group_msg_List.get(holder.getAdapterPosition()).getTipo().equals("imagen")){
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
                                    intent.putExtra("url",my_group_msg_List.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==2){
                                    GroupController.EliminarMensajeGrupo(holder.getAdapterPosition(), holder, my_group_msg_List, groupId);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if(my_group_msg_List.get(holder.getAdapterPosition()).getTipo().equals("mp4")){
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
                                    intent.putExtra("url",my_group_msg_List.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==2){
                                    GroupController.EliminarMensajeGrupo(holder.getAdapterPosition(), holder, my_group_msg_List, groupId);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }else if(my_group_msg_List.get(holder.getAdapterPosition()).getTipo().equals("mp3")){
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
                                    intent.putExtra("audio_url",my_group_msg_List.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==2){
                                    GroupController.EliminarMensajeGrupo(holder.getAdapterPosition(), holder, my_group_msg_List, groupId);
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
                    if (my_group_msg_List.get(holder.getAdapterPosition()).getTipo().equals("pdf") || my_group_msg_List.get(holder.getAdapterPosition()).getTipo().equals("docx")){
                        CharSequence[] opciones = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.descargar),
                                holder.itemView.getContext().getString(R.string.cancelar)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(my_group_msg_List.get(holder.getAdapterPosition()).getMensaje()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if (my_group_msg_List.get(holder.getAdapterPosition()).getTipo().equals("texto")){
                        CharSequence[] opciones = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.copiar),
                                holder.itemView.getContext().getString(R.string.cancelar),
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if(position==0){
                                    String mensaje = my_group_msg_List.get(holder.getAdapterPosition()).getMensaje();
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
                    }else if (my_group_msg_List.get(holder.getAdapterPosition()).getTipo().equals("imagen")){
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
                                    intent.putExtra("url",my_group_msg_List.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }else if(my_group_msg_List.get(holder.getAdapterPosition()).getTipo().equals("mp4")){
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
                                    intent.putExtra("url",my_group_msg_List.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }else if(my_group_msg_List.get(holder.getAdapterPosition()).getTipo().equals("mp3")){
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
                                    intent.putExtra("audio_url",my_group_msg_List.get(holder.getAdapterPosition()).getMensaje());
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
        return my_group_msg_List.size();
    }

    public static class ViewHolderGroupMessages extends RecyclerView.ViewHolder{
        public EmojiTextView enviarMensajeTexto, recibirMensajeTexto;
        public CircleImageView recibirImagenPerfil;
        public ImageView mensajeImagenEnviar, mensajeImagenRecibir;
        public ViewHolderGroupMessages(@NonNull View itemView) {
            super(itemView);
            enviarMensajeTexto = itemView.findViewById(R.id.enviar_mensaje);
            recibirMensajeTexto = itemView.findViewById(R.id.recibir_mensaje);
            recibirImagenPerfil = itemView.findViewById(R.id.mensaje_imagen_perfil);
            mensajeImagenEnviar = itemView.findViewById(R.id.mensaje_enviar_imagen);
            mensajeImagenRecibir = itemView.findViewById(R.id.mensaje_recibir_imagen);
        }
    }
}
