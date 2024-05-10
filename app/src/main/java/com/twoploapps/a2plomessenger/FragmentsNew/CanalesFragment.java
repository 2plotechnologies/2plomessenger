package com.twoploapps.a2plomessenger.FragmentsNew;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twoploapps.a2plomessenger.Models.Canal;
import com.twoploapps.a2plomessenger.NewActivitys.CanalesActivity;
import com.twoploapps.a2plomessenger.NewActivitys.CrearCanalActivity;
import com.twoploapps.a2plomessenger.NewAdapters.RV_Adapters.ChannelsAdapter;
import com.twoploapps.a2plomessenger.R;

import java.util.ArrayList;
import java.util.Collections;

public class CanalesFragment extends Fragment {

    private LinearLayoutManager lm;
    private ChannelsAdapter adapter;
    private ArrayList<Canal> canalArrayList;
    private String CurrentUserId;

    public CanalesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View channelview =  inflater.inflate(R.layout.fragment_canales, container, false);
        RecyclerView rv_my_channels = channelview.findViewById(R.id.canaleslista);
        ImageButton crearCanal = channelview.findViewById(R.id.btn_new_channel);
        ImageButton buscarCanal = channelview.findViewById(R.id.btn_search);

        lm = new LinearLayoutManager(getContext());
        canalArrayList = new ArrayList<>();
        adapter = new ChannelsAdapter(canalArrayList);
        rv_my_channels.setLayoutManager(lm);
        rv_my_channels.setAdapter(adapter);

        CurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference channelsRef = FirebaseDatabase.getInstance().getReference().child("Canales");

        channelsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    canalArrayList.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        if(dataSnapshot.child("Miembros").hasChild(CurrentUserId)){
                            Canal canal = dataSnapshot.getValue(Canal.class);
                            canalArrayList.add(canal);
                        }
                    }
                    Collections.reverse(canalArrayList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
        crearCanal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CrearCanalActivity.class);
                startActivity(intent);
            }
        });

        buscarCanal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CanalesActivity.class);
                startActivity(intent);
            }
        });

        return channelview;
    }
}