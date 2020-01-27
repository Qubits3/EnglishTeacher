package com.example.englishteacher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;

    MenuItem optionSignIn, optionSignOut, optionProfile;
    Button notificationButton;

    private final String CHANNEL_ID = "100";
    public final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
    NotificationChannel channel;
    PendingIntent pendingIntent;
    NotificationManagerCompat notificationManagerCompat;

    long[] vibrationPattern = {100, 60, 100, 60};  //Vibration pattern for vibrating  /* bekle -> titre mantığına göre çalışır 100 ms bekle 60 ms titre 100 ms bekle 60ms bekle */

    String gettedEnglish, gettedTurkish, currentDocumentID;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);

        optionSignIn = menu.findItem(R.id.option_sign_in);
        optionSignOut = menu.findItem(R.id.option_sign_out);
        optionProfile = menu.findItem(R.id.option_profile);

        if (user != null){
            optionSignIn.setVisible(false);
            optionSignOut.setVisible(true);
            optionProfile.setVisible(true);
        }else {
            optionSignOut.setVisible(false);
            optionProfile.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.option_profile){
            Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
            startActivity(intent);
        }else if (item.getItemId() == R.id.option_sign_in){
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
        }else if (item.getItemId() == R.id.option_sign_out){
            Toast.makeText(getApplicationContext(), "You are signed out", Toast.LENGTH_SHORT).show();
            mAuth.signOut();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationButton = findViewById(R.id.button);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        Log.d(TAG, "onCreate: ");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        getRandomData();
    }

    private void createNotification() {

        getRandomData();

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(gettedEnglish + " -> " + gettedTurkish)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(pendingIntent)
                .setColorized(true)
                .setColor(Color.BLUE)  //Set color for app name in the notification
                .setOnlyAlertOnce(true);  //Bildirim sadece ilk geldiğinde kullanıcıyı uyarır güncellendiğinde uyarmaz
                //.setAutoCancel(true);  //Üzerine basılınca otomatik kapanır
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;  //Popup için en yüksek yapılmalı
            channel = new NotificationChannel(CHANNEL_ID, name, importance);  //Create notification channel
            channel.setDescription(description);
            channel.setSound(null, null);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setVibrationPattern(vibrationPattern);  //You must set that for vibrating operations

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void notification(View view) {

        //getRandomData();

        createNotification();
        createNotificationChannel();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    public void upload(View view) {

        String[] firstArray = new String[Words.english.length], secondArray = new String[Words.turkish.length];  //this must be local variable

        RandomWordGenerator.generate(Words.english, Words.turkish, firstArray, secondArray);

        for (int i = 0; i < Words.english.length;i++) {
            Map<String, Object> uploadMap = new HashMap<>();
            uploadMap.put("english", firstArray[i]);
            uploadMap.put("turkish", secondArray[i]);
            uploadMap.put("timestamp", Timestamp.now().toDate());

            if (!user.getEmail().isEmpty()) {
                db.collection(user.getEmail())
                        .add(uploadMap)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }
    }

    public void delete(View view){

        if (!user.getEmail().isEmpty() && gettedEnglish != null && currentDocumentID != null) {
            db.collection(user.getEmail())
                    .document(currentDocumentID).delete();  //şu an aktif olan documenti sil
        }
    }

    private void getRandomData() {

        if (!user.getEmail().isEmpty()){
            db.collection(user.getEmail())
                    .orderBy("timestamp")
                    .limit(1)  //bir seferde sadece 1 document seç
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                                currentDocumentID = snapshot.getId();

                                Map<String, Object> gettedData = snapshot.getData();
                                if (gettedData != null) {
                                    gettedEnglish = gettedData.get("english").toString();
                                }
                                if (gettedData != null) {
                                    gettedTurkish = gettedData.get("turkish").toString();
                                }

                                Toast.makeText(getApplicationContext(), gettedEnglish + gettedTurkish, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {}
}
