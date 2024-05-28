package com.twoploapps.a2plomessenger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.github.marlonlom.utilities.timeago.TimeAgoMessages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.viewholderposts> {
    FirebaseAuth auth=FirebaseAuth.getInstance();
    String CurrentUserId=auth.getCurrentUser().getUid();
    List<Posts> postsList;
    //private String postId;

    public PostsAdapter(List<Posts> postsList) {
        this.postsList = postsList;
    }

    @NonNull
    @Override
    public viewholderposts onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout,parent,false);
        viewholderposts holder = new viewholderposts(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull viewholderposts holder, int position) {
        Posts posts = postsList.get(position);
        if(posts.getIduser().equals(CurrentUserId)){
            holder.eliminar.setVisibility(View.VISIBLE);
            holder.editar.setVisibility(View.VISIBLE);
            holder.reportar.setVisibility(View.GONE);
        }else{
            holder.eliminar.setVisibility(View.GONE);
            holder.editar.setVisibility(View.GONE);
            holder.reportar.setVisibility(View.VISIBLE);
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Posts").child(posts.getPostId()).child("ReportadoPor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild(CurrentUserId)) {
                        holder.reportar.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        long timeInMillis = posts.getFecha();

        Locale LocaleBylanguageTag = Locale.getDefault();
        TimeAgoMessages messages = new TimeAgoMessages.Builder().withLocale(LocaleBylanguageTag).build();

        String text = TimeAgo.using(timeInMillis, messages);
        holder.username.setText(posts.getNomuser());
        holder.time.setText(text);
        if(posts.hasImage() && posts.hasText()){
            Picasso.get().load(posts.getImagen()).into(holder.imagen);
            holder.texto.setText(posts.getTexto());
        }
        else if(!posts.hasText()&& posts.hasImage()){
            Picasso.get().load(posts.getImagen()).into(holder.imagen);
            holder.texto.setVisibility(View.GONE);
        }
        else if(!posts.hasImage()&& posts.hasText()){
            holder.texto.setText(posts.getTexto());
            holder.imagen.setVisibility(View.GONE);
        }
        holder.eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                builder.setMessage(holder.itemView.getContext().getString(R.string.etasseguropost))
                        .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                rootRef.child("Posts").child(posts.getPostId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(holder.itemView.getContext(), R.string.postborrado, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {}});
                builder.create().show();
            }
        });
        holder.editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),UserPostsActivity.class);
                intent.putExtra("post_id", posts.getPostId());
                holder.itemView.getContext().startActivity(intent);
            }
        });
        holder.reportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                rootRef.child("Reportes").child(posts.getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            if(snapshot.hasChild("Cantidad")){
                                int cantidad = Integer.parseInt(snapshot.child("Cantidad").getValue().toString());
                                int nueva = cantidad + 1;
                                rootRef.child("Reportes").child(posts.getPostId()).child("Cantidad").setValue(nueva).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Calendar calendar = Calendar.getInstance();
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                            String currentDate = dateFormat.format(calendar.getTime());
                                            Toast.makeText(holder.itemView.getContext(), R.string.sereporto, Toast.LENGTH_SHORT).show();
                                            rootRef.child("Posts").child(posts.getPostId()).child("ReportadoPor").child(CurrentUserId).setValue(currentDate);
                                        }
                                    }
                                });
                            }
                        }else{
                            rootRef.child("Reportes").child(posts.getPostId()).child("Cantidad").setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Calendar calendar = Calendar.getInstance();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                        String currentDate = dateFormat.format(calendar.getTime());
                                        Toast.makeText(holder.itemView.getContext(), R.string.sereporto, Toast.LENGTH_SHORT).show();
                                        rootRef.child("Posts").child(posts.getPostId()).child("ReportadoPor").child(CurrentUserId).setValue(currentDate);
                                    }
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),PerfilActivity.class);
                intent.putExtra("usuario_id", posts.getIduser());
                holder.itemView.getContext().startActivity(intent);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), DetallePostActivity.class);
                intent.putExtra("post_id", posts.getPostId());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public class viewholderposts extends RecyclerView.ViewHolder {
        TextView texto, username, time;
        ImageView imagen;
        ImageButton eliminar,editar;
        ImageButton reportar;
        public viewholderposts(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            time = itemView.findViewById(R.id.txt_time);
            texto = itemView.findViewById(R.id.posttext);
            imagen = itemView.findViewById(R.id.postimage);
            eliminar = itemView.findViewById(R.id.btn_eliminar_post);
            editar = itemView.findViewById(R.id.btn_editar_post);
            reportar = itemView.findViewById(R.id.btnreportar);
        }
    }
}
