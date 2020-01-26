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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;

    MenuItem optionSignIn, optionSignOut, optionProfile;

    private final String CHANNEL_ID = "100";
    public final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
    NotificationChannel channel;
    PendingIntent pendingIntent;
    NotificationManagerCompat notificationManagerCompat;

    long[] vibrationPattern = {100, 60, 100, 60};  //Vibration pattern for vibrating  /* bekle -> titre mantığına göre çalışır 100 ms bekle 60 ms titre 100 ms bekle 60ms bekle */

    public static int randomNumber = 0;
    int count = 1;

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

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        Log.d(TAG, "onCreate: ");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
    }

    private void createNotification() {

        //ToDo: serverdan random document çek -> serverdaki count sayısına bağlı olarak bir random sayı gelecek altta, o sayıyı kullanarak document çek

        int count = getCount();
        Toast.makeText(getApplicationContext(), String.valueOf(count), Toast.LENGTH_SHORT).show();
        randomNumber = new Random().nextInt(count);

        //Toast.makeText(getApplicationContext(), String.valueOf(getCount()), Toast.LENGTH_LONG).show();

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(Words.english[randomNumber] + " -> " + Words.turkish[randomNumber])
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

        createNotification();
        createNotificationChannel();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    public void upload(View view) {

        for (int i = 0; i < 10;i++) {
            Map<String, Object> uploadMap = new HashMap<>();
            uploadMap.put("english_" + i, Words.english[i]);
            uploadMap.put("turkish_" + i, Words.turkish[i]);

            if (!user.getEmail().isEmpty()) {
                db.collection(user.getEmail())
                        .document("document_" + i)
                        .set(uploadMap)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                Map<String, Object> countMap = new HashMap<>();
                countMap.put("count", 10);

                db.collection(user.getEmail())
                        .document("count")
                        .set(countMap)
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

        if (!user.getEmail().isEmpty()) {
            db.collection(user.getEmail())
                    .document("document_" + randomNumber).delete();  //şu an aktif olan sayıya göre direkt documenti sil

            db.collection(user.getEmail()).document("count")
                    .get()  //count isminde documente referans göster
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();

                                int count = Integer.valueOf(document.get("count").toString());  //count ismindeki documentin değerini(field) al
                                count--;

                                Map<String, Object> tempMap = new HashMap<>();
                                tempMap.put("count", count);

                                db.collection(user.getEmail())  //bir azalttığımız count değerini servera gönder
                                        .document("count")
                                        .set(tempMap);
                            }
                        }
                    });
        }


    }

    private int getCount(){

        if (!user.getEmail().isEmpty()) {
            db.collection(user.getEmail()).document("count")
                    .get()  //count isminde documente referans göster
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();

                                count = Integer.valueOf(document.get("count").toString());  //count ismindeki documentin değerini(field) al
                            }
                        }
                    });
        }
        return count;
    }

    @Override
    public void onBackPressed() {}
}
