package com.twoploapps.a2plomessenger.NewAdapters.RV_Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.twoploapps.a2plomessenger.Models.MensajeAI;
import com.twoploapps.a2plomessenger.R;

import java.util.List;

public class ChatAIAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_USUARIO = 1;
    private static final int TIPO_AI = 2;

    private List<MensajeAI> mensajes;

    public ChatAIAdapter(List<MensajeAI> mensajes) {
        this.mensajes = mensajes;
    }

    @Override
    public int getItemViewType(int position) {
        if (mensajes.get(position).getDe().equals("usuario")) {
            return TIPO_USUARIO;
        } else {
            return TIPO_AI;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TIPO_USUARIO) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mensaje_usuario, parent, false);
            return new UsuarioViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mensaje_ai, parent, false);
            return new AIViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MensajeAI mensaje = mensajes.get(position);
        if (holder instanceof UsuarioViewHolder) {
            ((UsuarioViewHolder) holder).mensaje.setText(mensaje.getMensaje());
        } else {
            ((AIViewHolder) holder).mensaje.setText(mensaje.getMensaje());
        }
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView mensaje;

        UsuarioViewHolder(View itemView) {
            super(itemView);
            mensaje = itemView.findViewById(R.id.mensaje_usuario);
        }
    }

    static class AIViewHolder extends RecyclerView.ViewHolder {
        TextView mensaje;

        AIViewHolder(View itemView) {
            super(itemView);
            mensaje = itemView.findViewById(R.id.mensaje_ai);
        }
    }
}