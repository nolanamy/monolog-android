package com.example.earmark;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AudioRecorderService extends Service {
    private static final String TAG                 = "AudioRecorderService";
    private static final int    CHUNK_LENGTH_MS     = 10 * 1000; // ten seconds
    private static final int    AMPLITUDE_THRESHOLD = 20000;

    private ExtAudioRecorder extAudioRecorder;

    private String path;
    private String fileName;
    private Timer timer = new Timer();

    public AudioRecorderService() {
        path = Environment.getExternalStorageDirectory().getAbsolutePath();
        path += "/Monolog/Audio/";

        File directory = new File(path);
        directory.mkdirs();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        stopRecording();
        timer.cancel();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        handleStartCommand(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return handleStartCommand(intent);
    }

    private int handleStartCommand(Intent intent) {
        startRecording();
        return START_STICKY;
    }

    private void startRecording() {
        fileName = new Date().getTime() + ".wav";
        extAudioRecorder = ExtAudioRecorder.getInstance(false);
        extAudioRecorder.setOutputFile(path + fileName);
        extAudioRecorder.prepare();
        extAudioRecorder.start();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopRecording();
                startRecording();
            }
        }, CHUNK_LENGTH_MS);
    }

    private void stopRecording() {
        int maxAmplitude = extAudioRecorder.getMaxAmplitude();
        extAudioRecorder.stop();
        extAudioRecorder.release();
        extAudioRecorder = null;

        if (maxAmplitude > AMPLITUDE_THRESHOLD) {
            Log.i(TAG, "uploading maxAmplitude=" + maxAmplitude + " fileName=" + fileName);
            UploaderService.startFileUpload(getApplicationContext(), path, fileName);
        } else {
            Log.i(TAG, "deleting maxAmplitude=" + maxAmplitude + " fileName=" + fileName);
            new File(path + fileName).delete();
        }
    }
}
