package com.example.englishteacher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Arrays;
import java.util.Random;

public class MyService extends Service {

    public MyService(){

    }

    @Override
    public void onCreate() {



        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        MainActivity mainActivity = new MainActivity();

        return super.onStartCommand(intent, flags, startId);
    }
}
