package com.twoploapps.a2plomessenger.NewAdapters.RV_Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
import com.twoploapps.a2plomessenger.NewActivitys.MensajesCanalActivity;
import com.twoploapps.a2plomessenger.PostsAdapter;
import com.twoploapps.a2plomessenger.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.ViewHolderChannels> {
    List<Canal> my_channelList;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    String CurrentUserId=auth.getCurrentUser().getUid();

    public ChannelsAdapter(List<Canal> channelList){
        this.my_channelList = channelList;
    }
    @NonNull
    @Override
    public ChannelsAdapter.ViewHolderChannels onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_display_layout,parent,false);
        return new ViewHolderChannels(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelsAdapter.ViewHolderChannels holder, int position) {
        Canal canal = my_channelList.get(position);
        if(canal.getCreador_Id().equals(CurrentUserId)){
            holder.btn_follow.setVisibility(View.GONE);
        }
        holder.ChannelName.setText(canal.getNombre());
        Picasso.get().load(canal.getImagen()).into(holder.channelImg);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Canales").child(canal.getId()).child("Miembros").child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String rol = snapshot.child("Rol").getValue(String.class);
                    if(rol!=null && rol.equals("miembro")){
                        holder.btn_follow.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});

        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.child("Canales").child(canal.getId()).child("Miembros").child(CurrentUserId).child("Rol").setValue("miembro");
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), MensajesCanalActivity.class);
                intent.putExtra("channel_id", canal.getId());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return my_channelList.size();
    }

    public static class ViewHolderChannels extends RecyclerView.ViewHolder {

        CircleImageView channelImg;
        TextView ChannelName;
        Button btn_follow;

        public ViewHolderChannels(@NonNull View itemView) {
            super(itemView);
            channelImg = itemView.findViewById(R.id.channel_image);
            ChannelName = itemView.findViewById(R.id.channel_nombre);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }
    }
}
