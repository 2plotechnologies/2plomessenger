package com.twoploapps.a2plomessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText numero, codigo;
    private CountryCodePicker ccp;
    private Button boton_registrar, enviar_codigo;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerification;
    private PhoneAuthProvider.ForceResendingToken mResendingToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private String phoneNumber,countryCode, mNumero;
    private TextView emailLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        numero =(EditText)findViewById(R.id.numero);
        codigo =(EditText)findViewById(R.id.codigo);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);

        boton_registrar =(Button)findViewById(R.id.botonregistrar);
        enviar_codigo =(Button)findViewById(R.id.enviar_codigo);
        emailLogin = (TextView) findViewById(R.id.emailLogin);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        emailLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,LoginActivity2.class);
                startActivity(intent);
            }
        });
        boton_registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumber = numero.getText().toString();
                countryCode = ccp.getSelectedCountryCodeWithPlus();
                mNumero = countryCode+phoneNumber;
                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(LoginActivity.this, "Ingrese su número", Toast.LENGTH_SHORT).show();
                }else {
                    loadingBar.setTitle("Enviando el codigo");
                    loadingBar.setMessage("Por Favor espere");
                    loadingBar.show();
                    loadingBar.setCancelable(true);
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(mNumero)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(LoginActivity.this)
                            .setCallbacks(callbacks)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        enviar_codigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numero.setVisibility(View.GONE);
                boton_registrar.setVisibility(View.GONE);
                ccp.setVisibility(View.GONE);
                String verificacionCode = codigo.getText().toString();
                if (TextUtils.isEmpty(verificacionCode)){
                    Toast.makeText(LoginActivity.this, "Ingrese el codigo recibido", Toast.LENGTH_SHORT).show();
                }else{
                    loadingBar.setTitle("Ingresando");
                    loadingBar.setMessage("Por favor espere");
                    loadingBar.show();
                    loadingBar.setCancelable(true);
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerification, verificacionCode);
                    signInPhoneAuthCredential(credential);
                }
            }
        });


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, e.getLocalizedMessage()+" Número no valido, intentalo de nuevo", Toast.LENGTH_SHORT).show();
                numero.setVisibility(View.VISIBLE);
                boton_registrar.setVisibility(View.VISIBLE);
                codigo.setVisibility(View.GONE);
                enviar_codigo.setVisibility(View.GONE);

            }
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token){
                mVerification = verificationId;
                mResendingToken = token;
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, "Codigo Enviado, revise su mensajeria", Toast.LENGTH_SHORT).show();
                numero.setVisibility(View.GONE);
                boton_registrar.setVisibility(View.GONE);
                emailLogin.setVisibility(View.GONE);
                ccp.setVisibility(View.GONE);
                codigo.setVisibility(View.VISIBLE);
                enviar_codigo.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    loadingBar.dismiss();
                    Toast.makeText(LoginActivity.this, "Ingresado con exito", Toast.LENGTH_SHORT).show();
                    EnviarAlInicio();
                }else{
                    String mensaje = task.getException().toString();
                    Toast.makeText(LoginActivity.this, "error"+mensaje, Toast.LENGTH_SHORT).show();
                }

            }
        });
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
        Intent intent = new Intent(LoginActivity.this, SetupActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}