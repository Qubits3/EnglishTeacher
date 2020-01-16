package com.example.englishteacher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final String CHANNEL_ID = "100";
    private final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: onCreate");

        Random random = new Random();

        int randomNumber = random.nextInt(3846);  //en son 3846

        Intent snoozeIntent = new Intent(this, MainActivity.class);

        PendingIntent snoozePendingIntent = PendingIntent.getActivity(this, 0, snoozeIntent, 0);

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(Words.english[randomNumber] + " -> " + Words.turkish[randomNumber])
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_launcher_background, "Click", snoozePendingIntent);
    }

    public static void CancelNotification(Context ctx, int notifyId) {
        String  s = Context.NOTIFICATION_SERVICE;
        NotificationManager mNM = (NotificationManager) ctx.getSystemService(s);
        mNM.cancel(notifyId);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);  //Create notification channel
            channel.setDescription(description);
            channel.setSound(null, null);
            channel.enableVibration(false);
            channel.setLockscreenVisibility(importance);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void notification(View view) {
        createNotificationChannel();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }
}
