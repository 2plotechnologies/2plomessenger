package com.twoploapps.a2plomessenger.NewAdapters.RV_Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.twoploapps.a2plomessenger.Models.Canal;
import com.twoploapps.a2plomessenger.Models.Grupo;
import com.twoploapps.a2plomessenger.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolderGroups> {

    List<Grupo> my_groupsList;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    String CurrentUserId=auth.getCurrentUser().getUid();

    public GroupsAdapter(List<Grupo> gruposList){
        this.my_groupsList = gruposList;
    }

    @NonNull
    @Override
    public ViewHolderGroups onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_display_layout,parent,false);
        return new ViewHolderGroups(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderGroups holder, int position) {
        Grupo grupo = my_groupsList.get(position);

        holder.groupName.setText(grupo.getNombre());

        Picasso.get().load(grupo.getImagen()).placeholder(R.drawable.defaultprofilephoto).into(holder.groupImg);
    }

    @Override
    public int getItemCount() {
        return my_groupsList.size();
    }

    public static class ViewHolderGroups extends RecyclerView.ViewHolder {

        CircleImageView groupImg;
        TextView groupName;
        public ViewHolderGroups(@NonNull View itemView) {
            super(itemView);
            groupImg = itemView.findViewById(R.id.group_image);
            groupName = itemView.findViewById(R.id.group_nombre);
        }
    }
}
