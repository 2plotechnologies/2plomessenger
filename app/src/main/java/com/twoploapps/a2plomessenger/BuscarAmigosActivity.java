package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class BuscarAmigosActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView BuscaramigosRecyclerView;
    private SearchView searchView;
    private DatabaseReference UserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_amigos);

        searchView=(SearchView) findViewById(R.id.buscador);
        BuscaramigosRecyclerView = (RecyclerView)findViewById(R.id.buscar_amigos_recyclerview);
        BuscaramigosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        toolbar=(Toolbar)findViewById(R.id.buscar_amigos_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.buscar_amigos));
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contactos> options =
                new FirebaseRecyclerOptions.Builder<Contactos>().setQuery(UserRef, Contactos.class).build();
        FirebaseRecyclerAdapter<Contactos, BuscarAmigosViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contactos, BuscarAmigosViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull BuscarAmigosViewHolder holder, int position, @NonNull Contactos model) {
                        holder.nombreu.setText(model.getNombre());
                        if(Objects.equals(model.getPC(), "-")||Objects.equals(model.getPC(),"Contactos")){
                            holder.ciudadu.setText("-");
                        }else{
                            holder.ciudadu.setText(model.getCiudad());
                        }
                        holder.estadou.setText(model.getEstado());
                        if(model.getPI()!=null&& Objects.equals(model.getPI(), "Oculto")||Objects.equals(model.getPI(),"Contactos")){
                            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/defaultprofilephoto.png?alt=media&token=d2f0f0de-2386-45bc-952a-aedeea866b0c").placeholder(R.drawable.defaultprofilephoto).into(holder.imagenu);
                        }else{
                            Picasso.get().load(model.getImagen()).placeholder(R.drawable.defaultprofilephoto).into(holder.imagenu);
                        }
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String usuario_id = getRef(holder.getAdapterPosition()).getKey();
                                Intent intent = new Intent(BuscarAmigosActivity.this, PerfilActivity.class);
                                intent.putExtra("usuario_id", usuario_id);
                                startActivity(intent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public BuscarAmigosViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

                        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout, viewGroup,false);
                        BuscarAmigosViewHolder viewHolder = new BuscarAmigosViewHolder(view);
                        return viewHolder;

                    }
                };
        BuscaramigosRecyclerView.setAdapter(adapter);
        adapter.startListening();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                textSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                textSearch(s);
                return false;
            }
        });
    }

    public void textSearch(String s) {
        Query query = UserRef.orderByChild("nombre").startAt(s).endAt(s + "~");
        FirebaseRecyclerOptions<Contactos> firebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<Contactos>()
                        .setQuery(query,Contactos.class).build();
        FirebaseRecyclerAdapter<Contactos, BuscarAmigosViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contactos, BuscarAmigosViewHolder>(firebaseRecyclerOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull BuscarAmigosViewHolder holder, int position, @NonNull Contactos model) {
                        holder.nombreu.setText(model.getNombre());
                        if(Objects.equals(model.getPC(), "-")||Objects.equals(model.getPC(), "Contacto")){
                            holder.ciudadu.setText("-");
                        }else{
                            holder.ciudadu.setText(model.getCiudad());
                        }
                        holder.estadou.setText(model.getEstado());
                        if(model.getPI()!=null&& Objects.equals(model.getPI(), "Oculto")||Objects.equals(model.getPI(),"Contactos")){
                            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/defaultprofilephoto.png?alt=media&token=d2f0f0de-2386-45bc-952a-aedeea866b0c").placeholder(R.drawable.defaultprofilephoto).into(holder.imagenu);
                        }else{
                            Picasso.get().load(model.getImagen()).placeholder(R.drawable.defaultprofilephoto).into(holder.imagenu);
                        }
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String usuario_id = getRef(holder.getAdapterPosition()).getKey();
                                Intent intent = new Intent(BuscarAmigosActivity.this, PerfilActivity.class);
                                intent.putExtra("usuario_id", usuario_id);
                                startActivity(intent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public BuscarAmigosViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

                        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout, viewGroup,false);
                        BuscarAmigosViewHolder viewHolder = new BuscarAmigosViewHolder(view);
                        return viewHolder;

                    }
                };
        BuscaramigosRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class BuscarAmigosViewHolder extends RecyclerView.ViewHolder{

        TextView nombreu, ciudadu, estadou;
        CircleImageView imagenu;
        public BuscarAmigosViewHolder(@NonNull View itemView) {
            super(itemView);

            nombreu = itemView.findViewById(R.id.user_nombre);
            ciudadu = itemView.findViewById(R.id.user_ciudad);
            estadou = itemView.findViewById(R.id.user_estado);
            imagenu = itemView.findViewById(R.id.user_image_perfil);
        }
    }
}