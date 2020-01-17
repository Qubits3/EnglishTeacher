package com.example.englishteacher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final String CHANNEL_ID = "100";
    public final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
    NotificationChannel channel;
    PendingIntent pendingIntent;
    NotificationManagerCompat notificationManagerCompat;
//    static SharedPreferences sharedPreferences;

    static boolean isTapped = false;

    private static final String TAG = "MainActivity";

    public static int randomNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt("isDismissed", 0);  //bildirim kaydırılarak mı kapatılmış yoksa üstüne basılarak mı, 0 ise kaydırılarak kapatılmıştır
//        editor.commit();  //kayıt

        randomNumber = new Random().nextInt(2197);    //en son 2197

        Intent intent = new Intent(this, MyService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        createNotification();
        createNotificationChannel();
    }

    private void createNotification() {

        randomNumber = new Random().nextInt(2197);

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(Words.english[randomNumber] + " -> " + Words.turkish[randomNumber])
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(pendingIntent)
                .setColorized(true)
                .setColor(Color.BLUE)
                .setOnlyAlertOnce(true)  //Bildirim sadece ilk geldiğinde kullanıcıyı uyarır güncellendiğinde uyarmaz
                .setAutoCancel(true);  //Üzerine basılınca otomatik kapanır
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            channel = new NotificationChannel(CHANNEL_ID, name, importance);  //Create notification channel
            channel.setDescription(description);
            channel.setSound(null, null);
            channel.enableVibration(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

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
}
