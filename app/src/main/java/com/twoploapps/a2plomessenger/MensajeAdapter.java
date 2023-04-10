package com.twoploapps.a2plomessenger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class MensajeAdapter extends RecyclerView.Adapter<MensajeAdapter.MensajesViewHolder> {

    private List<Mensajes> usuarioMensajes;
    private FirebaseAuth auth;
    private DatabaseReference UserRef;
    private static final String AES = "AES";
    private static final String key = BuildConfig.CLAVE_CIFRADO;

    public MensajeAdapter (List<Mensajes> usuarioMensajes){
        this.usuarioMensajes = usuarioMensajes;
    }

    public class  MensajesViewHolder extends RecyclerView.ViewHolder{
        public EmojiTextView enviarMensajeTexto, recibirMensajeTexto;
        public CircleImageView recibirImagenPerfil;
        public ImageView mensajeImagenEnviar, mensajeImagenRecibir;
        public MensajesViewHolder(@NonNull View itemView) {
            super(itemView);
            enviarMensajeTexto=(EmojiTextView) itemView.findViewById(R.id.enviar_mensaje);
            recibirMensajeTexto=(EmojiTextView) itemView.findViewById(R.id.recibir_mensaje);
            recibirImagenPerfil=(CircleImageView) itemView.findViewById(R.id.mensaje_imagen_perfil);
            mensajeImagenEnviar=(ImageView)itemView.findViewById(R.id.mensaje_enviar_imagen);
            mensajeImagenRecibir=(ImageView)itemView.findViewById(R.id.mensaje_recibir_imagen);
        }
    }

    @NonNull
    @Override
    public MensajesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usuario_mensaje_layout, parent,false);
        auth = FirebaseAuth.getInstance();
        return new MensajesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensajesViewHolder holder, int position) {

        String mensajeEnviadoID = auth.getCurrentUser().getUid();
        Mensajes mensajes = usuarioMensajes.get(holder.getAdapterPosition());
        String DeUsuarioId = mensajes.getDe();
        String TipoMensaje = mensajes.getTipo();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios").child(DeUsuarioId);
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild("imagen")){
                    String privacidadimg = snapshot.child("PI").getValue().toString();
                    if(privacidadimg.equals("Oculto")){
                        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/pngwing.com.png?alt=media&token=1d2dff28-0fd1-4caf-9ca0-b6192b0fc8c2").into(holder.recibirImagenPerfil);
                    }else{
                        String ImagenRecibido = snapshot.child("imagen").getValue().toString();
                        Picasso.get().load(ImagenRecibido).placeholder(R.drawable.welcome).into(holder.recibirImagenPerfil);
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

            if (DeUsuarioId.equals(mensajeEnviadoID)){
                holder.enviarMensajeTexto.setVisibility(View.VISIBLE);
                holder.enviarMensajeTexto.setBackgroundResource(R.drawable.enviar_mensaje_layout);
                holder.enviarMensajeTexto.setTextColor(Color.WHITE);
                holder.enviarMensajeTexto.setText(decrypt(mensajes.getMensaje()) + "\n\n"+mensajes.getFecha()+" - "+mensajes.getHora());
            }else{
                holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                holder.recibirMensajeTexto.setVisibility(View.VISIBLE);
                holder.recibirMensajeTexto.setBackgroundResource(R.drawable.recibir_mensaje_layout);
                holder.recibirMensajeTexto.setTextColor(Color.WHITE);
                holder.recibirMensajeTexto.setText(decrypt(mensajes.getMensaje()) + "\n\n"+mensajes.getFecha()+" - "+mensajes.getHora());
            }
        }else if (TipoMensaje.equals("imagen")){
            if (DeUsuarioId.equals(mensajeEnviadoID)){
                holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                Picasso.get().load(mensajes.getMensaje()).into(holder.mensajeImagenEnviar);
            }else{
                holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                holder.mensajeImagenRecibir.setVisibility(View.VISIBLE);
                Picasso.get().load(mensajes.getMensaje()).into(holder.mensajeImagenRecibir);
            }
        }else if (TipoMensaje.equals("pdf") || TipoMensaje.equals("docx")){
            if (DeUsuarioId.equals(mensajeEnviadoID)){
                holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsappro-d46ee.appspot.com/o/Archivos%2Farchivos.png?alt=media&token=9de607b2-da1d-4b6c-91f2-10bfd3828baf")
                        .into(holder.mensajeImagenEnviar);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(usuarioMensajes.get(holder.getAdapterPosition()).getMensaje()));
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
            if (DeUsuarioId.equals(mensajeEnviadoID)){
                holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/videoicon.png?alt=media&token=c0db70d9-4d53-4955-b4aa-c6089d455791")
                        .into(holder.mensajeImagenEnviar);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(usuarioMensajes.get(holder.getAdapterPosition()).getMensaje()));
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
            if (DeUsuarioId.equals(mensajeEnviadoID)){
                holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/imgbin_computer-icons-sound-icon-volume-png.png?alt=media&token=41edf2db-f52b-4894-958b-93f31ed86766")
                        .into(holder.mensajeImagenEnviar);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(usuarioMensajes.get(holder.getAdapterPosition()).getMensaje()));
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
        if (DeUsuarioId.equals(mensajeEnviadoID)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (usuarioMensajes.get(holder.getAdapterPosition()).getTipo().equals("pdf") || usuarioMensajes.get(holder.getAdapterPosition()).getTipo().equals("docx")){
                        CharSequence opciones[] = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.eliminar_mi),
                                holder.itemView.getContext().getString(R.string.descargar),
                                holder.itemView.getContext().getString(R.string.cancelar),
                                holder.itemView.getContext().getString(R.string.eliminar_todos)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    EliminarMensajesEnviados(holder.getAdapterPosition(),holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }else if (position==1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(usuarioMensajes.get(holder.getAdapterPosition()).getMensaje()));
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==3){
                                    EliminarMensajesTodos(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if (usuarioMensajes.get(holder.getAdapterPosition()).getTipo().equals("texto")){
                        CharSequence opciones[] = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.eliminar_mi),
                                holder.itemView.getContext().getString(R.string.cancelar),
                                holder.itemView.getContext().getString(R.string.eliminar_todos)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    EliminarMensajesEnviados(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==2) {

                                    EliminarMensajesTodos(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }else if (usuarioMensajes.get(holder.getAdapterPosition()).getTipo().equals("imagen")){
                        CharSequence opciones[] = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.eliminar_mi),
                                holder.itemView.getContext().getString(R.string.ver_img),
                                holder.itemView.getContext().getString(R.string.cancelar),
                                holder.itemView.getContext().getString(R.string.eliminar_todos)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    EliminarMensajesEnviados(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==1){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImagenActivity.class);
                                    intent.putExtra("url",usuarioMensajes.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);

                                }else if (position==3){

                                    EliminarMensajesTodos(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if(usuarioMensajes.get(holder.getAdapterPosition()).getTipo().equals("mp4")){
                        CharSequence opciones[] = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.eliminar_mi),
                                holder.itemView.getContext().getString(R.string.ver_vid),
                                holder.itemView.getContext().getString(R.string.cancelar),
                                holder.itemView.getContext().getString(R.string.eliminar_todos)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    EliminarMensajesEnviados(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==1){
                                    Intent intent = new Intent(holder.itemView.getContext(), VideoActivity.class);
                                    intent.putExtra("url",usuarioMensajes.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==3){
                                    EliminarMensajesTodos(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }else if(usuarioMensajes.get(holder.getAdapterPosition()).getTipo().equals("mp3")){
                        CharSequence opciones[] = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.eliminar_mi),
                                holder.itemView.getContext().getString(R.string.reproduciraudio),
                                holder.itemView.getContext().getString(R.string.cancelar),
                                holder.itemView.getContext().getString(R.string.eliminar_todos)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    EliminarMensajesEnviados(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==1){
                                    MediaPlayer mediaPlayer = new MediaPlayer();
                                    try {
                                        mediaPlayer.setDataSource(holder.itemView.getContext(), Uri.parse(usuarioMensajes.get(holder.getAdapterPosition()).getMensaje()));
                                        mediaPlayer.prepare();
                                        mediaPlayer.start();
                                    } catch (IOException e) {
                                        Toast.makeText(holder.itemView.getContext(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }else if (position==3){
                                    EliminarMensajesTodos(holder.getAdapterPosition(), holder);
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
                    if (usuarioMensajes.get(holder.getAdapterPosition()).getTipo().equals("pdf") || usuarioMensajes.get(holder.getAdapterPosition()).getTipo().equals("docx")){
                        CharSequence opciones[] = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.eliminar_mi),
                                holder.itemView.getContext().getString(R.string.descargar),
                                holder.itemView.getContext().getString(R.string.cancelar)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {

                                if (position==0){
                                    EliminarMensajesRecibidos(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }else if (position==1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(usuarioMensajes.get(holder.getAdapterPosition()).getMensaje()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if (usuarioMensajes.get(holder.getAdapterPosition()).getTipo().equals("texto")){
                        CharSequence opciones[] = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.eliminar_mi),
                                holder.itemView.getContext().getString(R.string.cancelar)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {

                                if (position==0){

                                    EliminarMensajesRecibidos(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }else if (usuarioMensajes.get(holder.getAdapterPosition()).getTipo().equals("imagen")){
                        CharSequence opciones[] = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.eliminar_mi),
                                holder.itemView.getContext().getString(R.string.ver_img),
                                holder.itemView.getContext().getString(R.string.cancelar)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {

                                if (position==0){
                                    EliminarMensajesRecibidos(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }else if (position==1){

                                    Intent intent = new Intent(holder.itemView.getContext(), ImagenActivity.class);
                                    intent.putExtra("url",usuarioMensajes.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }else if(usuarioMensajes.get(holder.getAdapterPosition()).getTipo().equals("mp4")){
                        CharSequence opciones[] = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.eliminar_mi),
                                holder.itemView.getContext().getString(R.string.ver_vid),
                                holder.itemView.getContext().getString(R.string.cancelar)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    EliminarMensajesEnviados(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==1){
                                    Intent intent = new Intent(holder.itemView.getContext(), VideoActivity.class);
                                    intent.putExtra("url",usuarioMensajes.get(holder.getAdapterPosition()).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }else if(usuarioMensajes.get(holder.getAdapterPosition()).getTipo().equals("mp3")){
                        CharSequence opciones[] = new CharSequence[]{
                                holder.itemView.getContext().getString(R.string.eliminar_mi),
                                holder.itemView.getContext().getString(R.string.reproduciraudio),
                                holder.itemView.getContext().getString(R.string.cancelar)
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position==0){
                                    EliminarMensajesEnviados(holder.getAdapterPosition(), holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position==1){
                                    MediaPlayer mediaPlayer = new MediaPlayer();
                                    try {
                                        mediaPlayer.setDataSource(holder.itemView.getContext(), Uri.parse(usuarioMensajes.get(holder.getAdapterPosition()).getMensaje()));
                                        mediaPlayer.prepare();
                                        mediaPlayer.start();
                                    } catch (IOException e) {
                                        Toast.makeText(holder.itemView.getContext(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }
    private static String decrypt(String mensaje) {
        byte[] decryptedData = new byte[0];
        try {
            byte[] data = Base64.decode(mensaje, Base64.DEFAULT);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            decryptedData = cipher.doFinal(data);
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    @Override
    public int getItemCount() {
        return usuarioMensajes.size();
    }

    private void EliminarMensajesEnviados(final int position, final MensajesViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Mensajes").child(usuarioMensajes.get(position).getDe())
                .child(usuarioMensajes.get(position).getPara())
                .child(usuarioMensajes.get(position).getMensajeID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), R.string.toasteliminado, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(holder.itemView.getContext(), R.string.toasteliminadoerror, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void EliminarMensajesRecibidos(final int position, final MensajesViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Mensajes").child(usuarioMensajes.get(position).getPara())
                .child(usuarioMensajes.get(position).getDe())
                .child(usuarioMensajes.get(position).getMensajeID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), R.string.toasteliminado, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(holder.itemView.getContext(), R.string.toasteliminadoerror, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void EliminarMensajesTodos(final int position, final MensajesViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Mensajes").child(usuarioMensajes.get(position).getPara())
                .child(usuarioMensajes.get(position).getDe())
                .child(usuarioMensajes.get(position).getMensajeID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootRef.child("Mensajes").child(usuarioMensajes.get(position).getDe())
                            .child(usuarioMensajes.get(position).getPara())
                            .child(usuarioMensajes.get(position).getMensajeID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), R.string.toasteliminado, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(holder.itemView.getContext(), R.string.toasteliminadoerror, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
