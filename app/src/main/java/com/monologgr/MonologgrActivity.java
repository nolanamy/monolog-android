package com.monologgr;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;

public class MonologgrActivity extends AppCompatActivity {
    private static final String TAG = "MonologgrActivity";

    private SearchView searchView;

    private ListView listView;

    private Realm realm;

    private AsyncTask<String, Void, Void> asyncTask;

    private MediaPlayer mediaPlayer;
    private AudioTrack  audioTrack;
    private int         minBufferSize;

    private SearchResultsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, AudioRecorderService.class));
        setContentView(R.layout.activity_monologgr);

        listView = (ListView) findViewById(R.id.search_results_list_view);

        realm = Realm.getDefaultInstance();
        adapter = new SearchResultsAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String playingFileName = adapter.getPlayingFileName();
                String fileName = adapter.getItem(position).fileName;
                if (!playingFileName.isEmpty()) {
                    stopPlaying();
                }
                if (!playingFileName.equals(fileName)) {
                    startPlaying(fileName);
                }
            }
        });

        minBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_monologgr, menu);

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String searchText) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                if (searchText.length() > 2) {
                    ((SearchResultsAdapter) listView.getAdapter()).search(searchText);
                }
                return false;
            }
        });
        return true;
    }

    private void startPlaying(String fileName) {
        Log.e(TAG, "startPlaying; fileName=" + fileName);

        adapter.setPlayingFileName(fileName);
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        asyncTask = new AsyncTask<String, Void, Void>() {

            @Override
            protected Void doInBackground(String... params) {
                String fileName = params[0];

                int i = 0;
                byte[] music = null;

                try {
                    File initialFile = new File(AudioRecorderService.getPath() + fileName);
                    InputStream is = new FileInputStream(initialFile);

                    audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                            minBufferSize, AudioTrack.MODE_STREAM);

                    music = new byte[512];
                    audioTrack.play();

                    while((i = is.read(music)) != -1 && !isCancelled())
                        audioTrack.write(music, 0, i);

                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void params) {
                stop();
            }
        };
        asyncTask.execute(fileName);
    }

    private void stopPlaying() {
        Log.e(TAG, "stopPlaying");
        asyncTask.cancel(true);
    }

    public boolean stop() {
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
        adapter.setPlayingFileName("");
        return true;
    }
}