package com.twoploapps.a2plomessenger.NewActivitys;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.twoploapps.a2plomessenger.InicioActivity;
import com.twoploapps.a2plomessenger.R;

import java.util.HashMap;
import java.util.Map;

public class ReportarActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportar);

        String ReportedUserId = getIntent().getStringExtra("user_id");

        Button btn_enviar = findViewById(R.id.button_send_report);
        Button btn_cancelar = findViewById(R.id.button_cancel);
        EditText txt_reason = findViewById(R.id.editText_reason);

        String CurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btn_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportarActivity.this, InicioActivity.class);
                startActivity(intent);
            }
        });

        btn_enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(txt_reason.getText())){
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("ReportesUsuario");

                    String id_report = ref.push().getKey();

                    Map<String, Object> report = new HashMap<>();

                    report.put("UsuarioReportador", CurrentUserId);
                    report.put("UsuarioReportado", ReportedUserId);
                    report.put("Motivo", txt_reason.getText().toString());

                    ref.child(id_report).setValue(report).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ReportarActivity.this, "Reporte Enviado", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ReportarActivity.this, InicioActivity.class);
                                startActivity(intent);
                            }else{
                                Toast.makeText(ReportarActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
