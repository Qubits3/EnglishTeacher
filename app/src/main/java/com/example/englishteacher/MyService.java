package com.example.englishteacher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;

public class MyService extends Service {

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {  //Her servis başlatıldığında çağrılır

        System.out.println("onStartCommand");

        Words.denemeEnglish[MainActivity.randomNumber] = null;

        for (int i = 0; i < Words.denemeEnglish.length;i++){
            System.out.print(Words.denemeEnglish[i] + " ");
        }

        return super.onStartCommand(intent, flags, startId);
    }
}
