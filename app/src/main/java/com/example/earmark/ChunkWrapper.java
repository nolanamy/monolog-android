package com.example.earmark;

import java.util.UUID;

import io.realm.Realm;

public class ChunkWrapper {

    public RealmChunk chunk;

    public ChunkWrapper(Realm realm) {
        chunk = new RealmChunk();
        chunk.setUuid(UUID.randomUUID().toString());
        chunk = realm.copyToRealm(chunk);
    }

    private ChunkWrapper(RealmChunk chunk) {
        this.chunk = chunk;
        if (chunk == null) {
            throw new RuntimeException("chunk must not be null");
        }
    }

    public static ChunkWrapper findByUuid(String uuid, Realm realm) {
        RealmChunk chunk = realm.where(RealmChunk.class).equalTo("uuid", uuid).findFirst();
        if (chunk != null) {
            return new ChunkWrapper(chunk);
        }
        return null;
    }

    public static ChunkWrapper create(String fileName, int maxAmplitude, Realm realm) {
        realm.beginTransaction();
        ChunkWrapper chunkWrapper = new ChunkWrapper(realm);
        chunkWrapper.chunk.setFileName(fileName);
        chunkWrapper.chunk.setMaxAmplitude(maxAmplitude);
        realm.commitTransaction();
        return chunkWrapper;
    }
}
