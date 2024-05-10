package com.twoploapps.a2plomessenger.NewActivitys;

import android.os.Bundle;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twoploapps.a2plomessenger.Models.Canal;
import com.twoploapps.a2plomessenger.NewAdapters.RV_Adapters.ChannelsAdapter;
import com.twoploapps.a2plomessenger.R;

import java.util.ArrayList;
import java.util.Collections;

public class CanalesActivity extends AppCompatActivity {
    private RecyclerView rv_channels;
    private SearchView searchView;
    private LinearLayoutManager lm;
    private ChannelsAdapter adapter;
    private ArrayList<Canal> canalArrayList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canales);

        Toolbar toolbar= findViewById(R.id.toolbar_buscar_canales);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.buscarCanal));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        rv_channels = findViewById(R.id.channel_rv);
        searchView = findViewById(R.id.buscador_channel);
        lm = new LinearLayoutManager(this);
        canalArrayList = new ArrayList<>();
        adapter = new ChannelsAdapter(canalArrayList);
        rv_channels.setLayoutManager(lm);
        rv_channels.setAdapter(adapter);

        DatabaseReference channelsRef = FirebaseDatabase.getInstance().getReference().child("Canales");

        channelsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    canalArrayList.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Canal canal = dataSnapshot.getValue(Canal.class);
                        canalArrayList.add(canal);
                    }
                    Collections.reverse(canalArrayList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                buscar(newText);
                return true;
            }
        });
    }

    private void buscar(String s) {
        ArrayList<Canal> mcanales = new ArrayList<>();
        for(Canal obj : canalArrayList){
            if(obj.getNombre().toLowerCase().contains(s.toLowerCase())){
                mcanales.add(obj);
            }
        }
        ChannelsAdapter canalesAdapter = new ChannelsAdapter(mcanales);
        rv_channels.setAdapter(canalesAdapter);
    }
}
