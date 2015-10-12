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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener {

    @Bind(R.id.listView)
    ListView listView;

    ArrayAdapter<MediaItem> adapter;
    MediaPlayer mMediaPlayer;


    @Bind(R.id.seekBar)
    SeekBar seekBar;

    @Bind(R.id.infoTextView)
    TextView infoTextView;


    @Bind(R.id.playBtn)
    Button playBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        seekBar.setOnSeekBarChangeListener(this);

        adapter = new ArrayAdapter<MediaItem>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        new ScanTask().execute();
        mMediaPlayer = new MediaPlayer();
        new TrackPositionTask().execute();
    }


    @OnItemClick(R.id.listView)
    void onItemClick(ListView listView, View view, int position, long idk) {
        MediaItem item = (MediaItem) listView.getItemAtPosition(position);

        infoTextView.setText(item.title);

        mMediaPlayer.reset();
        long id = item.id;
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
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

        this.seekBar.setProgress(0);
        this.seekBar.setEnabled(true);
        this.playBtn.setEnabled(true);
        this.playBtn.setText("∥");

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        mMediaPlayer.seekTo(mMediaPlayer.getDuration() * progress / 100);
    }

    private class TrackPositionTask extends AsyncTask<Void, Double, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("fdsf", "error sleep", e);
                }
                if (mMediaPlayer.isPlaying()) {
                    publishProgress(mMediaPlayer.getCurrentPosition() * 1.0 / mMediaPlayer.getDuration());
                }

            }
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            seekBar.setProgress((int) (values[0] * 100));
        }
    }


    private class ScanTask extends AsyncTask<Void, Void, List<MediaItem>> {

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
                    items.add(new MediaItem(thisId, thisTitle));
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
