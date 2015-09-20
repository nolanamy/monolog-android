package com.example.earmark;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MonologgrApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RealmConfiguration config = new RealmConfiguration.Builder(getApplicationContext()).build();
        Realm.setDefaultConfiguration(config);
    }
}