package com.example.englishteacher;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {

    private static final String TAG = "MyService";

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: onBind");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {  //Her servis başlatıldığında çağrılır

        Bundle extras = intent.getExtras();

        String s = null;
        if (extras != null) {
            s = extras.getString("english");
        }

        Log.d(TAG, "onStartCommand: " + s);
        return START_NOT_STICKY;
    }
}
