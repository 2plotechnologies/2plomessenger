package com.twoploapps.a2plomessenger;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.Edits;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twoploapps.a2plomessenger.Controllers.GroupController;
import com.twoploapps.a2plomessenger.NewActivitys.CreateGroupActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GruposFragment extends Fragment {


    public GruposFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View grupoFragmentoView = inflater.inflate(R.layout.fragment_grupos, container, false);
        ImageButton crearGrupo= grupoFragmentoView.findViewById(R.id.btn_new_group);
        ImageButton unirGrupo = grupoFragmentoView.findViewById(R.id.btn_join);

        crearGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateGroupActivity.class);
                startActivity(intent);
            }
        });

        unirGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.unirse_grupo);
                builder.setMessage(getString(R.string.introduce_el_code_del_grupo));
                final EditText input = new EditText(getActivity());
                input.setHint(R.string.codigogrupohint);
                builder.setView(input);
                builder.setCancelable(false);
                builder.setPositiveButton(getString(R.string.unirse), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!TextUtils.isEmpty(input.getText().toString())){
                            GroupController.Join(input.getText().toString(), getActivity());
                        }
                    }
                });
                builder.create().show();
            }
        });

        return grupoFragmentoView;
    }
}