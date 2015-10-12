package me.s4h.myazbfq;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {

    @Bind(R.id.listView)
    ListView listView;

    ArrayAdapter<MediaItem> adapter;
    MediaPlayer mMediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        adapter = new ArrayAdapter<MediaItem>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        new Mytask().execute();
    }



    @OnItemClick(R.id.listView)
    void onItemClick(ListView listView, View view, int position, long idk) {
        MediaItem item = (MediaItem) listView.getItemAtPosition(position);
        Toast.makeText(this,item.toString(),Toast.LENGTH_SHORT).show();
        long id = item.id;
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), contentUri);
        } catch (IOException e) {
            Log.e("fdf", "setdateasource error", e);
        }
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }


    private class Mytask extends AsyncTask<Void, Void, List<MediaItem> > {

        @Override
        protected List<MediaItem> doInBackground(Void... params) {
            List<MediaItem> items = new ArrayList<>();
            ContentResolver contentResolver = MainActivity.this.getContentResolver();
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor == null) {
                // query failed, handle error.
                Log.e("fdsf", "error occurred");
            } else if (!cursor.moveToFirst()) {
                // no media on the device
                Log.i("fdwef", "no media found");
            } else {
                int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
                int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
                do {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    items.add(new MediaItem(thisId,thisTitle));
                    Log.i("fdsf", thisId + thisTitle);
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<MediaItem> longStringMap) {
            MainActivity.this.adapter.addAll(longStringMap);
        }
    }


}
