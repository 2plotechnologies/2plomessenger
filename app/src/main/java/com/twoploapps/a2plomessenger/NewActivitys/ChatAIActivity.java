package com.twoploapps.a2plomessenger.NewActivitys;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.twoploapps.a2plomessenger.Models.MensajeAI;
import com.twoploapps.a2plomessenger.NewAdapters.RV_Adapters.ChatAIAdapter;
import com.twoploapps.a2plomessenger.R;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAIActivity extends AppCompatActivity {
    private RecyclerView rV_chats_AI;
    private String CurrentUserId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        Toolbar toolbar=findViewById(R.id.aichat_bar_layout);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        CurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rV_chats_AI =findViewById(R.id.rv_mensajes_ai);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rV_chats_AI.setLayoutManager(linearLayoutManager);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_chat_bar, null);
        actionBar.setCustomView(view);
        TextView nombreusuario= findViewById(R.id.usuario_nombre);
        TextView ultimaconexion=findViewById(R.id.usuario_conexion);
        CircleImageView usuarioimagen=findViewById(R.id.usuario_imagen);
        EmojiEditText mensaje=findViewById(R.id.mensaje_ai);
        ImageView  botonenviar=findViewById(R.id.enviar_mensaje_ai_boton);
        ImageView emojiboton=findViewById(R.id.emojiboton_ai);
        nombreusuario.setText(R.string.chatcon2ploai);
        Picasso.get().load(R.drawable.twoploai).into(usuarioimagen);
        ultimaconexion.setText(R.string.inpulsado);
        EmojiPopup popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.root_view_ai)).build(mensaje);

        List<MensajeAI> listaMensajes = new ArrayList<>();
        ChatAIAdapter adaptador = new ChatAIAdapter(listaMensajes);
        rV_chats_AI.setAdapter(adaptador);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("chat_ia").child(CurrentUserId);

        dbRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaMensajes.clear();
                for (DataSnapshot mensajeSnap : snapshot.getChildren()) {
                    MensajeAI mensaje = mensajeSnap.getValue(MensajeAI.class);
                    listaMensajes.add(mensaje);
                }
                adaptador.notifyDataSetChanged();
                rV_chats_AI.scrollToPosition(listaMensajes.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejo de error
            }
        });

        // Initialize the Gemini Developer API backend service
        // Create a `GenerativeModel` instance with a model that supports your use case
        GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.0-flash");

        // Use the GenerativeModelFutures Java compatibility layer which offers
        // support for ListenableFuture and Publisher APIs
        GenerativeModelFutures model = GenerativeModelFutures.from(ai);


        emojiboton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.toggle();
            }
        });

        botonenviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(mensaje.getText())){
                    guardarMensajeUsuario(mensaje.getText().toString());
                    // Initialize the Gemini Developer API backend service
                    // Create a `GenerativeModel` instance with a model that supports your use case
                    GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                            .generativeModel("gemini-2.0-flash");

                    // Use the GenerativeModelFutures Java compatibility layer which offers
                    // support for ListenableFuture and Publisher APIs
                    GenerativeModelFutures model = GenerativeModelFutures.from(ai);

                    // Provide a prompt that contains text
                    Content prompt = new Content.Builder()
                            .addText(mensaje.getText().toString())
                            .build();

                    // To generate text output, call generateContent with the text input
                    ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);
                    Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                        @Override
                        public void onSuccess(GenerateContentResponse result) {
                            String resultText = result.getText();
                            guardarRespuestaAI(resultText);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                        }
                    }, Runnable::run);
                }
            }
        });
    }

    private void guardarRespuestaAI(String respuestaIA) {
        // Luego en onSuccess, guardar respuesta de la IA
        DatabaseReference aiResponseRef = FirebaseDatabase.getInstance().getReference()
                .child("chat_ia").child(CurrentUserId).push();

        Map<String, Object> aiMessage = new HashMap<>();
        aiMessage.put("de", "IA");
        aiMessage.put("mensaje", respuestaIA);
        aiMessage.put("timestamp", ServerValue.TIMESTAMP);
        aiResponseRef.setValue(aiMessage);
    }

    private void guardarMensajeUsuario(String mensaje) {
        DatabaseReference aiChatRef = FirebaseDatabase.getInstance().getReference()
                .child("chat_ia").child(CurrentUserId).push();

        // Guardar mensaje del usuario
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("de", "usuario");
        userMessage.put("mensaje", mensaje);
        userMessage.put("timestamp", ServerValue.TIMESTAMP);
        aiChatRef.setValue(userMessage);
    }
}
