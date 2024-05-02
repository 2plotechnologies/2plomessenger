package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

public class InicioActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager myviewPager;
    private TabLayout mytabLayout;
    private AcesoTabsAdapter myacesoTabsAdapter;
    private String CurrentUserId, username, imageurl;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, RootRef,GrupoRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        toolbar= findViewById(R.id.app_main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("2Plo Messenger");
        myviewPager = findViewById(R.id.main_tabs_pager);
        myacesoTabsAdapter = new AcesoTabsAdapter(getSupportFragmentManager(),this);
        myviewPager.setAdapter(myacesoTabsAdapter);

        mytabLayout = findViewById(R.id.main_tabs);
        mytabLayout.setupWithViewPager(myviewPager);

        mytabLayout.getTabAt(0).setIcon(R.drawable.chat);
        mytabLayout.getTabAt(1).setIcon(R.drawable.posts);
        mytabLayout.getTabAt(2).setIcon(R.drawable.contactos);
        mytabLayout.getTabAt(3).setIcon(R.drawable.solicitudes);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        RootRef = FirebaseDatabase.getInstance().getReference().child("Grupos");
        GrupoRef = FirebaseDatabase.getInstance().getReference().child("CodigosDeGrupo");
        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        getUsername();
        getConnectionStatus();
        Intent intent = new Intent(this, TokenUpdateService.class);
        startService(intent);
    }

    private void getConnectionStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            // No hay conexión a Internet, abre la actividad de mensaje
            Intent intent = new Intent(this, NoInternetActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void getUsername() {
        UserRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()&&snapshot.hasChild("nombre")){
                    if(snapshot.hasChild("imagen")){
                        String privacidadimg = snapshot.child("PI").getValue().toString();
                        if(!privacidadimg.equals("Oculto")){
                            imageurl = snapshot.child("imagen").getValue().toString();
                        }else{
                            imageurl="https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/pngwing.com.png?alt=media&token=1d2dff28-0fd1-4caf-9ca0-b6192b0fc8c2";
                        }
                    }else{
                        imageurl="https://firebasestorage.googleapis.com/v0/b/plo-messenger.appspot.com/o/pngwing.com.png?alt=media&token=1d2dff28-0fd1-4caf-9ca0-b6192b0fc8c2";
                    }
                    username=snapshot.child("nombre").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser curUser = mAuth.getCurrentUser();
        if(curUser == null){
            EnviarAlLogin();
        }else{
            verificarUsuario();
            ActualizarActividad("activo");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser curUser = mAuth.getCurrentUser();
        if(curUser!=null){
            ActualizarActividad("inactivo");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser curUser = mAuth.getCurrentUser();
        if(curUser!=null){
            ActualizarActividad("inactivo");
        }
    }

    private void EnviarAlLogin() {

        Intent intent = new Intent(InicioActivity.this, LoginActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }
    private void verificarUsuario() {
        final String currentUserId = mAuth.getCurrentUser().getUid();
        UserRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild("nombre")){
                    CompletarDatosUsuario();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void CompletarDatosUsuario() {
        Intent intent = new Intent(InicioActivity.this, SetupActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menus_opciones, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.configmenu){
            //Toast.makeText(this, "Buscar amigos", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(InicioActivity.this,ConfiguracionActivity.class);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.new_call_menu){
            // Generar el código aleatorio
            GeneradorCodigoGrupo codgen = new GeneradorCodigoGrupo();
            String codllamada = codgen.generateAlphaNumeric(6);
            // Crear el AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.nuevallamada);
            builder.setMessage(getString(R.string.callcode) + codllamada + "\n\n"+getString(R.string.indicacionesllamada));
            builder.setPositiveButton(R.string.copiarcodigo, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Copiar el código al portapapeles
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Call code", codllamada);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), R.string.secopio, Toast.LENGTH_SHORT).show();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    ref.child("llamadascodes").child(codllamada).child("Creada por").setValue(CurrentUserId);
                }
            });
            builder.setNegativeButton(R.string.descartarcodigo, null); // Agregar un botón "Cancelar" que no hace nada
            // Mostrar el AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        if(item.getItemId()==R.id.join_call_menu){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.codigodelavideollamada);
            builder.setMessage(getString(R.string.intrduceelcodigodelallamada));
            final EditText input = new EditText(this);
            input.setHint(R.string.codigohint);
            input.setTextColor(Color.BLACK);
            builder.setView(input);
            builder.setPositiveButton(R.string.unirse, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String codigo = input.getText().toString().trim();
                    // Si el EditText no está vacío, inicia LlamadaActivity
                    if (!codigo.isEmpty()) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        ref.child("llamadascodes").child(codigo).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(!snapshot.exists()){
                                    Toast.makeText(InicioActivity.this, R.string.codigonovalido, Toast.LENGTH_SHORT).show();
                                }else{
                                    Intent intent = new Intent(InicioActivity.this, LlamadaActivity.class);
                                    intent.putExtra("codigo", codigo);
                                    intent.putExtra("username",username);
                                    intent.putExtra("img",imageurl);
                                    startActivity(intent);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}});
                    }
                }
            });
            builder.show();
        }
        if(item.getItemId()==R.id.compartir_menu){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String subject = getString(R.string.compartir_titulo);
            String body = getString(R.string.compartir_body);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(intent, getString(R.string.compartir_con)));
        }
        if (item.getItemId() == R.id.cerrarsesion_menu){
            ActualizarActividad("inactivo");
            mAuth.signOut();
            EnviarAlLogin();
        }
        return true;
    }
    private void ActualizarActividad(String estado){
        String CurrentTime, CurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        CurrentDate = dateFormat.format(calendar.getTime());
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("hh mm a");
        CurrentTime = dateFormat1.format(calendar.getTime());

        HashMap<String, Object> EstadoOnline = new HashMap<>();
        EstadoOnline.put("hora", CurrentTime);
        EstadoOnline.put("fecha", CurrentDate);
        EstadoOnline.put("estado", estado);

        UserRef.child(CurrentUserId).child("estadoUser").updateChildren(EstadoOnline);
    }
}


