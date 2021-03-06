package com.monologgr.network;

import android.util.Log;

import com.monologgr.application.MonologgrApplication;
import com.monologgr.models.ChunkWrapper;
import com.monologgr.services.AudioRecorderService;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class UploaderClient {
    private static final String TAG = "UploaderClient";

    private static final MediaType MEDIA_TYPE_WAV = MediaType.parse("audio/wav");
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String WATSON_URL = "https://stream.watsonplatform.net/speech-to-text/api/v1/recognize?continuous=true&timestamps=true&max_alternatives=10&word_confidence=true";
    private static final String WATSON_USERNAME = "MASKED";
    private static final String WATSON_PASSWORD = "MASKED";

    private static final String MONOLOGGR_URL = "http://monologgr.com/upload.ashx";

    private static final String MONOLOGGR_SEARCH_URL = "http://monologgr.com/wordLookup.ashx";

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

    public boolean upload(ChunkWrapper chunkWrapper) {
        // TODO check chunkWrapper for status
        File file = new File(AudioRecorderService.getPath() + chunkWrapper.chunk.getFileName());

        Request request = new Request.Builder()
                .url(WATSON_URL)
                .header("Authorization", Credentials.basic(WATSON_USERNAME, WATSON_PASSWORD))
                .header("x-monologgr-file-name", chunkWrapper.chunk.getFileName())
                .post(RequestBody.create(MEDIA_TYPE_WAV, file))
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d(TAG, "Uploaded audio; fileName=" + chunkWrapper.chunk.getFileName() + " status=" + response.code());
//            Log.d(TAG, response.body().string());

            if (response.code() == 200) {
                String body = response.body().string();
                Realm realm = Realm.getDefaultInstance();
                chunkWrapper.setWatsonResults(body, realm);
                uploadTranscription(chunkWrapper, body);
                realm.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean uploadTranscription(ChunkWrapper chunkWrapper, String body) {
        Request request = new Request.Builder()
                .url(MONOLOGGR_URL + "?timestamp=" + formatDate(new Date(chunkWrapper.chunk.getRecorded())) + "&email=" + MonologgrApplication.deviceId + "&volume=" + chunkWrapper.chunk.getMaxAmplitude() + "&passwordHash=placeholder&filename=" + chunkWrapper.chunk.getFileName())
                .post(RequestBody.create(MEDIA_TYPE_JSON, body))
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d(TAG, "Uploaded transcription; fileName=" + chunkWrapper.chunk.getFileName() + " status=" + response.code());
            Log.d(TAG, response.body().string());

            if (response.code() == 200) {
                Realm realm = Realm.getDefaultInstance();
                chunkWrapper.setResultsUploaded(realm);
                realm.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String search(String search) {
        String searchEscaped = "";
        try {
            searchEscaped = URLEncoder.encode(search, Charset.defaultCharset().name());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .url(MONOLOGGR_SEARCH_URL + "?email=" + MonologgrApplication.deviceId + "&passwordHash=placeholder&q=" + searchEscaped)
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d(TAG, "Searched; search=" + search + " status=" + response.code());

            if (response.code() == 200) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    private static String formatDate(Date date)
    {
        if (date == null) {
            return "null";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }
}
