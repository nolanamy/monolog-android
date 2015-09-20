package com.example.earmark;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AudioRecorderService extends Service {
    private static final String TAG = "AudioRecorderService";
    private static final int CHUNK_LENGTH_MS = 15 * 1000; // fifteen seconds

    private MediaRecorder mediaRecorder;

    private String fileName;
    private Timer  timer = new Timer();

    public AudioRecorderService() {
        fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName += "/Monolog/Audio/";

        File directory = new File(fileName);
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
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // TODO does this not include corded/headset mics?
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setOutputFile(fileName + new Date().getTime() + ".wav");
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mediaRecorder.start();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopRecording();
                startRecording();
            }
        }, new Date(new Date().getTime() + CHUNK_LENGTH_MS));
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }
}
