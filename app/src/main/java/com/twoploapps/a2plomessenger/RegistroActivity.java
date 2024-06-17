package com.twoploapps.a2plomessenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class RegistroActivity extends AppCompatActivity {
    private EditText textouser, textocorreo, textocontra;
    private Button btnregister;
    private String userID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        TextView textobienvenida = findViewById(R.id.textobienvenida);
        TextView txtpreguntaya = findViewById(R.id.txtpregutaya);
        ImageView logo = findViewById(R.id.logoapp);
        textouser = findViewById(R.id.textouser);
        textocorreo = findViewById(R.id.textocorreo);
        textocontra = findViewById(R.id.textocontra);
        btnregister = findViewById(R.id.btnregistrer);
        CheckBox mostrarpassword = findViewById(R.id.mostrarpasswordregistro);
        progressBar = findViewById(R.id.progressregistro);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        mostrarpassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    textocontra.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    //txtconfirmpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else{
                    textocontra.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //txtconfirmpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewUser();
            }
        });

        txtpreguntaya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginActivity();
            }
        });
    }

    public void openLoginActivity() {
        Intent intent = new Intent(RegistroActivity.this, LoginActivity2.class);
        startActivity(intent);
    }
    public void CreateNewUser() {
        String username = textouser.getText().toString().trim();
        String email = textocorreo.getText().toString().trim().toLowerCase();
        String password = textocontra.getText().toString().trim();

        if(TextUtils.isEmpty(username)){
            textouser.setError(getString(R.string.imgresa_nombre));
            textouser.requestFocus();
        }else if(TextUtils.isEmpty(email)){
            textocorreo.setError(getString(R.string.ingresa_correo));
            textocorreo.requestFocus();
        }else if(TextUtils.isEmpty(password)){
            textocontra.setError(getString(R.string.ingresa_password));
            textocontra.requestFocus();
        }else if(!isStrongPassword(password)){
            textocontra.setError(getString(R.string.ingresa_password_fuerte));
            textocontra.requestFocus();
        }else{
            progressBar.setVisibility(View.VISIBLE);
            btnregister.setVisibility(View.GONE);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        userID = mAuth.getCurrentUser().getUid();
                        DocumentReference documentReference = database.collection("users").document(userID);
                        String encryptedPassword = cifrado.encrypt(password);
                        Map<String, Object> user = new HashMap<>();
                        user.put("username",username);
                        user.put("email",email);
                        user.put("password",encryptedPassword);

                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Timber.tag("TAG").d("onSuccess: Datos registrados%s", userID);
                            }
                        });
                        Toast.makeText(RegistroActivity.this, R.string.usuario_registrado, Toast.LENGTH_SHORT).show();
                        mAuth.getCurrentUser().sendEmailVerification();
                        Intent intent = new Intent(RegistroActivity.this, SetupActivity2.class);
                        intent.putExtra("username",username);
                        startActivity(intent);
                    }else{
                        progressBar.setVisibility(View.GONE);
                        btnregister.setVisibility(View.VISIBLE);
                        String error = task.getException().getMessage().toString();
                        Toast.makeText(RegistroActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    public static boolean isStrongPassword(String password) {
        // Criterios para una contraseña fuerte
        int longitudMinima = 6;
        boolean tieneMayuscula = false;
        boolean tieneMinuscula = false;
        boolean tieneDigito = false;
        boolean tieneCaracterEspecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                tieneMayuscula = true;
            } else if (Character.isLowerCase(c)) {
                tieneMinuscula = true;
            } else if (Character.isDigit(c)) {
                tieneDigito = true;
            } else if (!Character.isLetterOrDigit(c)) {
                tieneCaracterEspecial = true;
            }
        }

        // Verificar si todos los criterios se cumplen
        return password.length() >= longitudMinima &&
                tieneMayuscula &&
                tieneMinuscula &&
                tieneDigito &&
                tieneCaracterEspecial;
    }
}