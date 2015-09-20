package com.example.earmark;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

public class UploaderService extends IntentService {
    private static final String TAG = "UploaderService";
    private static final String PATH = "path";
    private static final String FILE_NAME = "fileName";

    public static void startFileUpload(Context context, String path, String fileName) {
        Intent intent = new Intent(context, UploaderService.class);
        intent.putExtra(PATH, path);
        intent.putExtra(FILE_NAME, fileName);
        context.startService(intent);
    }

    public UploaderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String path = intent.getStringExtra(PATH);
            final String fileName = intent.getStringExtra(FILE_NAME);
            uploadFile(path, fileName);
        }
    }

    private void uploadFile(String path, String fileName) {
        // TODO upload file
    }
}
