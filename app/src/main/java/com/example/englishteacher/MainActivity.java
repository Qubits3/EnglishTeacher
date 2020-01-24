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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    FirebaseUser user;

    MenuItem optionSignIn, optionSignOut, optionProfile;

    private final String CHANNEL_ID = "100";
    public final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
    NotificationChannel channel;
    PendingIntent pendingIntent;
    NotificationManagerCompat notificationManagerCompat;

    long[] vibrationPattern = {100, 60, 100, 60};  //Vibration pattern for vibrating  /* bekle -> titre mantığına göre çalışır 100 ms bekle 60 ms titre 100 ms bekle 60ms bekle */

    public static int randomNumber = 0;

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

        Log.d(TAG, "onCreate: ");

        randomNumber = new Random().nextInt(2197);    //en son 2197

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
    }

    private void createNotification() {

        randomNumber = new Random().nextInt(2197);

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

    @Override
    public void onBackPressed() {}
}
