package com.example.earmark;

import android.util.Log;

import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class UploaderClient {
    private static final String TAG = "UploaderClient";
    private static final MediaType MEDIA_TYPE_WAV = MediaType.parse("audio/wav");

    private static UploaderClient instance;
    private OkHttpClient client;

    public static UploaderClient getInstance() {
        if (instance == null) {
            instance = new UploaderClient();
        }
        return instance;
    }

    private UploaderClient() {
        client = new OkHttpClient();
        client.setReadTimeout(30000, TimeUnit.MILLISECONDS);
    }

    public boolean upload(String path, String fileName) {
        File file = new File(path + fileName);

        Request request = new Request.Builder()
                .url("MASKED/speech-to-text/api/v1/recognize?continuous=true&timestamps=true&max_alternatives=10&word_confidence=true")
                .header("Authorization", Credentials.basic("MASKED", "MASKED"))
                .header("x-monologgr-file-name", fileName)
                .post(RequestBody.create(MEDIA_TYPE_WAV, file))
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d(TAG, "fileName=" + fileName + " status=" + response.code());
            Log.d(TAG, response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
