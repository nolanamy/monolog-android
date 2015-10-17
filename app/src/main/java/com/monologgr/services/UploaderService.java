package com.monologgr.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.monologgr.network.UploaderClient;
import com.monologgr.models.ChunkWrapper;

import io.realm.Realm;

public class UploaderService extends IntentService {
    private static final String TAG  = "UploaderService";
    private static final String UUID = "uuid";

    public static void startFileUpload(Context context, String uuid) {
        Intent intent = new Intent(context, UploaderService.class);
        intent.putExtra(UUID, uuid);
        context.startService(intent);
    }

    public UploaderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Realm realm = Realm.getDefaultInstance();
            final String uuid = intent.getStringExtra(UUID);
            ChunkWrapper chunkWrapper = ChunkWrapper.findByUuid(uuid, realm);
            if (chunkWrapper != null) {
                uploadFile(chunkWrapper);
            }
            realm.close();
        }
    }

    private void uploadFile(ChunkWrapper chunkWrapper) {
        UploaderClient.getInstance().upload(chunkWrapper);
    }
}
