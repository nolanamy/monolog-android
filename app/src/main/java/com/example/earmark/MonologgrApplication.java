package com.example.earmark;

import android.app.Application;
import android.provider.Settings;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MonologgrApplication extends Application {
    public static String deviceId;

    @Override
    public void onCreate() {
        super.onCreate();
        RealmConfiguration config = new RealmConfiguration.Builder(getApplicationContext()).build();
        Realm.setDefaultConfiguration(config);

        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}