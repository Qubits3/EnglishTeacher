package com.example.englishteacher;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> words, UUIDs;
    PostClass adapter;
    ListView listView;

    private FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;

    MenuItem optionSignOut, optionProfile;

    private final String CHANNEL_ID = "100";
    public final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
    NotificationChannel channel;
    PendingIntent pendingIntent;
    NotificationManagerCompat notificationManagerCompat;

    long[] vibrationPattern = {100, 60, 100, 60};  //Vibration pattern for vibrating  /* bekle -> titre mantığına göre çalışır 100 ms bekle 60 ms titre 100 ms bekle 60ms titre */

    String englishWordFromFirebase, turkishWordFromFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        words = new ArrayList<>();
        UUIDs = new ArrayList<>();

        adapter = new PostClass(words, this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                createNotification(words.get(position));
                createNotificationChannel();
                notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);
                notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
            }
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        getRandomData();
    }

    private void createNotification(String words) {

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(words)
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

    private void getRandomData() {

        if (!user.getEmail().isEmpty()) {
            db.collection(user.getEmail())
                    .orderBy("timestamp")
                    .limit(5)  //get only 5 documents each time
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {

                                Map<String, Object> getData = snapshot.getData();
                                if (getData != null) {
                                    UUIDs.add(snapshot.getId());

                                    englishWordFromFirebase = getData.get("english").toString();
                                    turkishWordFromFirebase = getData.get("turkish").toString();

                                    words.add(englishWordFromFirebase + " -> " + turkishWordFromFirebase);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);

        optionSignOut = menu.findItem(R.id.option_sign_out);
        optionProfile = menu.findItem(R.id.option_email);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.option_deleteNotes) {
            for (String singleNote : UUIDs) {
                db.collection(user.getEmail())
                        .document(singleNote).delete();
            }
            words.clear();
            getRandomData();
        } else if (item.getItemId() == R.id.option_uploadAllNotes) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.upload_all_notes_question_mark);
            builder.setMessage(R.string.do_you_really_want_to_upload_all_notes_question_mark);
            builder.setNegativeButton(R.string.no, null);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String[] firstArray = new String[Words.english.length], secondArray = new String[Words.turkish.length];  //this must be local variable

                    RandomWordGenerator.generate(Words.english, Words.turkish, firstArray, secondArray);

                    for (int i = 0; i < Words.english.length; i++) {
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
            });
            builder.show();
        } else if (item.getItemId() == R.id.option_email) {
            Toast.makeText(getApplicationContext(), user.getEmail(), Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.option_sign_out) {
            Toast.makeText(getApplicationContext(), "You are signed out", Toast.LENGTH_SHORT).show();
            mAuth.signOut();

            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
