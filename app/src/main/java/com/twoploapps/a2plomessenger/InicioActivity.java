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
    private String CurrentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, RootRef,GrupoRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        toolbar=(Toolbar)findViewById(R.id.app_main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("2Plo Messenger");

        myviewPager = (ViewPager)findViewById(R.id.main_tabs_pager);
        myacesoTabsAdapter = new AcesoTabsAdapter(getSupportFragmentManager(),this);
        myviewPager.setAdapter(myacesoTabsAdapter);

        mytabLayout = (TabLayout)findViewById(R.id.main_tabs);
        mytabLayout.setupWithViewPager(myviewPager);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        RootRef = FirebaseDatabase.getInstance().getReference().child("Grupos");
        GrupoRef = FirebaseDatabase.getInstance().getReference().child("CodigosDeGrupo");
        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        Intent intent = new Intent(this, TokenUpdateService.class);
        startService(intent);
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

    private void TBuscarAmigos() {
        Intent intent = new Intent(InicioActivity.this, BuscarAmigosActivity.class);
        startActivity(intent);
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
        if (item.getItemId() == R.id.buscar_contactos_menu){
            //Toast.makeText(this, "Buscar amigos", Toast.LENGTH_SHORT).show();
            TBuscarAmigos();

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
        if (item.getItemId() == R.id.miperfil_menu){
            //Toast.makeText(this, "Mi perfil", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(InicioActivity.this, MiperfilActivity.class);
            startActivity(intent);

        }
        if(item.getItemId() == R.id.privacidad_menu){
            Intent intent = new Intent(InicioActivity.this,PrivacidadActivity.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.acerca_de_menu){
            Intent intent = new Intent(InicioActivity.this, AcercaDeActivity.class);
            startActivity(intent);
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
            mAuth.signOut();
            EnviarAlLogin();

        }
        return true;
    }
/*
    private void UnirseAUnGrupo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(InicioActivity.this, R.style.AlertDialog);
        builder.setTitle("Unirse a un grupo");
        final EditText codigogrupo = new EditText(InicioActivity.this);
        codigogrupo.setHint("Introduce el codigo del grupo");
        codigogrupo.setBackgroundColor(Color.parseColor("#D3D3D3"));
        SpannableString codigoHint = new SpannableString("Introduce el codigo del grupo");
        codigoHint.setSpan(new ForegroundColorSpan(Color.GRAY), 0, codigoHint.length(), 0);
        codigogrupo.setHint(codigoHint);
        codigogrupo.setTextColor(Color.BLACK);
        builder.setView(codigogrupo);

        builder.setPositiveButton("Unirse", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String codigo = codigogrupo.getText().toString();
                if(TextUtils.isEmpty(codigo)){
                    Toast.makeText(InicioActivity.this, "Introduce un codigo", Toast.LENGTH_SHORT).show();
                }else{
                    UnirUsuarioAUnGrupo(codigo);
                }

            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
*/
    /*
    private void UnirUsuarioAUnGrupo(String codigo) {
        GrupoRef.child(codigo).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombregrupo = snapshot.child("nombre").getValue().toString();
                    UserRef.child(CurrentUserId).child("Grupos").child(nombregrupo).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Toast.makeText(InicioActivity.this, "Ya estas en este grupo", Toast.LENGTH_SHORT).show();
                            }else{
                                UserRef.child(CurrentUserId).child("Grupos").child(nombregrupo).child("codigo").setValue(codigo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(InicioActivity.this, "Te uniste al grupo: "+nombregrupo, Toast.LENGTH_SHORT).show();
                                        }else{
                                            String exception = task.getException().getMessage().toString();
                                            Toast.makeText(InicioActivity.this, "Error: "+exception, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else{
                    Toast.makeText(InicioActivity.this, "Ha introducido un codigo incorrecto, intentelo de nuevo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
     */
    /*
    private void CrearNuevoGrupo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(InicioActivity.this, R.style.AlertDialog);
        builder.setTitle("Nombre del grupo: ");
        final EditText nombregrupo = new EditText(InicioActivity.this);
        nombregrupo.setHint("Introduce el nombre del grupo");
        nombregrupo.setBackgroundColor(Color.parseColor("#D3D3D3"));
        SpannableString nombreHint = new SpannableString("Introduce el nombre del grupo");
        nombreHint.setSpan(new ForegroundColorSpan(Color.GRAY), 0, nombreHint.length(), 0);
        nombregrupo.setHint(nombreHint);
        nombregrupo.setTextColor(Color.BLACK);
        builder.setView(nombregrupo);
        builder.setPositiveButton("Crear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombreg = nombregrupo.getText().toString();

                if(TextUtils.isEmpty(nombreg)){
                    Toast.makeText(InicioActivity.this, "Ingrese el nombre de el grupo", Toast.LENGTH_SHORT).show();
                }else{
                    
                    CrearGrupoFirebase(nombreg);

                }
            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

     */
    /*
    private void CrearGrupoFirebase(String nombreg) {
        GeneradorCodigoGrupo codigoGrupo = new GeneradorCodigoGrupo();
        String codigo = codigoGrupo.generateAlphaNumeric(6);
        UserRef.child(CurrentUserId).child("Grupos").child(nombreg).child("codigo").setValue(codigo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(InicioActivity.this, "Grupo creado con exito", Toast.LENGTH_SHORT).show();
                    RootRef.child(nombreg).child("codigo").setValue(codigo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                GrupoRef.child(codigo).child("nombre").setValue(nombreg).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(InicioActivity.this, "Comparte el codigo para que se unan mas usuarios", Toast.LENGTH_SHORT).show();
                                        }else{
                                            String error = task.getException().getMessage().toString();
                                            Toast.makeText(InicioActivity.this, "Error"+error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                Toast.makeText(InicioActivity.this, "Codigo: "+codigo, Toast.LENGTH_SHORT).show();
                            }else{
                                String error = task.getException().getMessage().toString();
                                Toast.makeText(InicioActivity.this, "Se produjo un error: "+error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    String error = task.getException().getMessage().toString();
                    Toast.makeText(InicioActivity.this, "Error"+error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
     */
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


