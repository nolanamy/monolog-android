package com.example.earmark;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmChunk extends RealmObject {
    @PrimaryKey
    private String uuid;

    private String fileName;
    private int    maxAmplitude;
    private String transcriptionResults;
    private boolean resultsUploaded;
    private long recorded;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getMaxAmplitude() {
        return maxAmplitude;
    }

    public void setMaxAmplitude(int maxAmplitude) {
        this.maxAmplitude = maxAmplitude;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTranscriptionResults() {
        return transcriptionResults;
    }

    public void setTranscriptionResults(String transcriptionResults) {
        this.transcriptionResults = transcriptionResults;
    }

    public boolean getResultsUploaded() {
        return resultsUploaded;
    }

    public void setResultsUploaded(boolean resultsUploaded) {
        this.resultsUploaded = resultsUploaded;
    }

    public long getRecorded() {
        return recorded;
    }

    public void setRecorded(long recorded) {
        this.recorded = recorded;
    }
}
