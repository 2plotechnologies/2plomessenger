package com.twoploapps.a2plomessenger.FragmentsNew;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.twoploapps.a2plomessenger.NewActivitys.CanalesActivity;
import com.twoploapps.a2plomessenger.NewActivitys.CrearCanalActivity;
import com.twoploapps.a2plomessenger.R;

public class CanalesFragment extends Fragment {


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
        ImageButton crearCanal = channelview.findViewById(R.id.btn_new_channel);
        ImageButton buscarCanal = channelview.findViewById(R.id.btn_search);
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