package com.twoploapps.a2plomessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class NoInternetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
        Button btnreintentar = findViewById(R.id.btnreintentar);
        btnreintentar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected()) {
                    Toast.makeText(NoInternetActivity.this, R.string.no_tienes_conexi_n, Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(NoInternetActivity.this, InicioActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}