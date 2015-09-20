package com.example.earmark;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.Date;

public class AudioRecorderService extends Service {
    private static final String TAG = "AudioRecorderService";

    private MediaRecorder mediaRecorder;

    private String fileName;

    public AudioRecorderService() {
        fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName += "/";
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
        Date now = new Date();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // TODO does this not include corded/headset mics?
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setOutputFile(fileName + now.getTime() + ".wav");
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mediaRecorder.start();
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }
}
