package com.twoploapps.a2plomessenger;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
public class TokenUpdateService extends FirebaseMessagingService {
    private FirebaseMessaging mFirebaseMessaging;

    @Override
    public void onCreate() {
        super.onCreate();
        mFirebaseMessaging = FirebaseMessaging.getInstance();
        mFirebaseMessaging.getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String token) {
                updateToken(token);
            }
        });
    }
    @Override
    public void onNewToken(@NonNull String token) {
        updateToken(token);
    }
    private void updateToken(String token) {
        FirebaseUser curUser = FirebaseAuth.getInstance().getCurrentUser();
        if(curUser!=null){
            String userId = curUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(userId);
            ref.child("token").setValue(token);
        }
    }
}
