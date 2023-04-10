package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class RegistroActivity extends AppCompatActivity {
    private TextView textobienvenida, textoregistro, textousername, textocorr, textopass,txtpreguntaya;
    private EditText textouser, textocorreo, textocontra;
    private Button btnregister, btniniciasesion;
    private CheckBox mostrarpassword;
    private ImageView logo;

    private String userID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private static final String AES = "AES";
    private static final String key = BuildConfig.CIFRADOPASSWORD;
    private static final int IV_SIZE = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        textobienvenida=(TextView) findViewById(R.id.textobienvenida);
        txtpreguntaya=(TextView)findViewById(R.id.txtpregutaya);
        logo=(ImageView) findViewById(R.id.logoapp);
        textouser=(EditText) findViewById(R.id.textouser);
        textocorreo=(EditText) findViewById(R.id.textocorreo);
        textocontra=(EditText) findViewById(R.id.textocontra);
        btnregister=(Button)findViewById(R.id.btnregistrer);
        mostrarpassword=(CheckBox) findViewById(R.id.mostrarpasswordregistro);
        mAuth=FirebaseAuth.getInstance();
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
        String email = textocorreo.getText().toString().trim();
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
        }else{
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        userID = mAuth.getCurrentUser().getUid();
                        DocumentReference documentReference = database.collection("users").document(userID);
                        String encryptedPassword = encrypt(password);
                        Map<String, Object> user = new HashMap<>();
                        user.put("username",username);
                        user.put("email",email);
                        user.put("password",encryptedPassword);

                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("TAG","onSuccess: Datos registrados"+userID);
                            }
                        });
                        Toast.makeText(RegistroActivity.this, R.string.usuario_registrado, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegistroActivity.this, SetupActivity2.class);
                        intent.putExtra("username",username);
                        startActivity(intent);
                    }else{
                        String error = task.getException().getMessage().toString();
                        Toast.makeText(RegistroActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private String encrypt(String data) {
        byte[] encryptedData = new byte[0];
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            encryptedData = cipher.doFinal(data.getBytes());
        } catch (Exception ex) {
            Log.e("Error","No se puede registrar, error: "+ex.getMessage());
        }
        return Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }
}