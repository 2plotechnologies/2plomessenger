package com.twoploapps.a2plomessenger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ComentariosAdapter extends RecyclerView.Adapter<ComentariosAdapter.viewholdercomentarios> {
    FirebaseAuth auth=FirebaseAuth.getInstance();
    String CurrentUserId=auth.getCurrentUser().getUid();
    List<Comentarios> comentariosList;
    public ComentariosAdapter(List<Comentarios> comentariosList){
        this.comentariosList = comentariosList;
    }
    @NonNull
    @Override
    public viewholdercomentarios onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comentario_layout,parent,false);
        return new viewholdercomentarios(view);
    }
    @Override
    public void onBindViewHolder(@NonNull viewholdercomentarios holder, int position) {
        Comentarios comentarios = comentariosList.get(position);
        holder.txtnombreusuario.setText(comentarios.getNombre_usuario());
        holder.txtcomentario.setText(comentarios.getComentario());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(comentarios.getId_usuario().equals(CurrentUserId)){
                    CharSequence opciones[] = new CharSequence[]{
                            holder.itemView.getContext().getString(R.string.eliminar_todos),
                            holder.itemView.getContext().getString(R.string.cancelar),
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setItems(opciones, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(i==0){
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                ref.child("Comentarios").child(comentarios.getId_del_post())
                                        .child(comentarios.getId_comentario())
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(holder.itemView.getContext(), R.string.comentarioeliminado,
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return comentariosList.size();
    }
    public static class viewholdercomentarios extends  RecyclerView.ViewHolder{
        TextView txtnombreusuario, txtcomentario;
        public viewholdercomentarios(@NonNull View itemView) {
            super(itemView);
            txtnombreusuario = itemView.findViewById(R.id.txtcomentusername);
            txtcomentario = itemView.findViewById(R.id.txtcomentariocontend);
        }
    }
}
