package com.twoploapps.a2plomessenger;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity2 extends AppCompatActivity {
    private EditText txtcorreo, txtcontraseña;
    private TextView txtitulo, txpregunta;
    private Button btnlogin;
    private ImageView logo;
    private CheckBox mostarpassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        txtcorreo=(EditText) findViewById(R.id.txtemail);
        txtcontraseña=(EditText) findViewById(R.id.txtcontraseña);
        txtitulo=(TextView) findViewById(R.id.titulo);
        logo=(ImageView) findViewById(R.id.iconoapp);
        txpregunta=(TextView) findViewById(R.id.registro);
        btnlogin=(Button) findViewById(R.id.btnlogin);
        mostarpassword=(CheckBox) findViewById(R.id.mostrarpasswordlogin);
        mAuth=FirebaseAuth.getInstance();
        mostarpassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    txtcontraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    //txtconfirmpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else{
                    txtcontraseña.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //txtconfirmpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        txpregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirRegistroActivity();
            }
        });
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });
    }

    public void abrirRegistroActivity() {
        Intent intent = new Intent(LoginActivity2.this, RegistroActivity.class);
        startActivity(intent);
    }
    public void userLogin(){
        String Mail = txtcorreo.getText().toString().trim().toLowerCase();
        String Password = txtcontraseña.getText().toString().trim();

        if(TextUtils.isEmpty(Mail)){
            txtcorreo.setError(getString(R.string.hintmail));
            txtcorreo.requestFocus();
        }else if(TextUtils.isEmpty(Password)){
            Toast.makeText(LoginActivity2.this, R.string.hintpass, Toast.LENGTH_SHORT).show();
            txtcontraseña.requestFocus();
        }else{
            mAuth.signInWithEmailAndPassword(Mail, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        updateToken();
                        Toast.makeText(LoginActivity2.this, R.string.welcome, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity2.this, InicioActivity.class);
                        startActivity(intent);
                    }else{
                        String error = task.getException().getMessage().toString();
                        Toast.makeText(LoginActivity2.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void updateToken() {
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Obtener el nuevo token
                    String token = task.getResult();
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                    databaseRef.child("Usuarios").child(userId).child("token").setValue(token);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            EnviarAlInicio();
        }
    }

    private void EnviarAlInicio() {
        Intent intent = new Intent(LoginActivity2.this, InicioActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}