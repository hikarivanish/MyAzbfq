package me.s4h.myazbfq;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
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
import butterknife.OnClick;
import butterknife.OnItemClick;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {

    @Bind(R.id.audioListView)
    ListView audioListView;

    @Bind(R.id.videoListView)
    ListView videoListView;

    ArrayAdapter<AudioItem> audioListAdapter;
    ArrayAdapter<VideoItem> videoListAdapter;
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

        audioListAdapter = new ArrayAdapter<AudioItem>(this, android.R.layout.simple_list_item_1);
        audioListView.setAdapter(audioListAdapter);

        videoListAdapter = new ArrayAdapter<VideoItem>(this, android.R.layout.simple_list_item_1);
        videoListView.setAdapter(videoListAdapter);

        new AudioScanTask().execute();
        new VideoScanTask().execute();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);

        new TrackPositionTask().execute();
    }


    @OnItemClick(R.id.audioListView)
    void onAudioItemClick(ListView listView, View view, int position, long idk) {
        AudioItem item = (AudioItem) listView.getItemAtPosition(position);

        infoTextView.setText(item.title);

        mMediaPlayer.reset();
        long id = item.id;
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), contentUri);
        } catch (IOException e) {
            Log.e("fdf", "setdateasource error", e);
        }
        mMediaPlayer.prepareAsync();
    }

    @OnItemClick(R.id.videoListView)
    void onVideoItemCLick(ListView listView, View view, int position, long idk) {
        VideoItem item = (VideoItem) listView.getItemAtPosition(position);
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, item.id);

        Intent intent = new Intent(this, VideoPlayActivity.class);
        intent.putExtra(VideoPlayActivity.KEY_URL, contentUri.toString());
        this.startActivity(intent);

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        this.playBtn.setEnabled(true);
        this.playBtn.setText("pause");
    }

    @OnClick(R.id.playBtn)
    void playBtnClick() {
        if (this.mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            playBtn.setText("play");
        } else {
            mMediaPlayer.start();
            playBtn.setText("pause");
        }
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        this.playBtn.setText("play");
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


    private class AudioScanTask extends AsyncTask<Void, Void, List<AudioItem>> {

        @Override
        protected List<AudioItem> doInBackground(Void... params) {
            List<AudioItem> items = new ArrayList<>();
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
                    items.add(new AudioItem(thisId, thisTitle));
                    Log.i("fdsf", thisId + thisTitle);
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<AudioItem> longStringMap) {
            MainActivity.this.audioListAdapter.addAll(longStringMap);
        }
    }

    private class VideoScanTask extends AsyncTask<Void, Void, List<VideoItem>> {

        @Override
        protected List<VideoItem> doInBackground(Void... params) {
            List<VideoItem> items = new ArrayList<>();
            ContentResolver contentResolver = MainActivity.this.getContentResolver();
            Uri uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor == null) {
                // query failed, handle error.
                Log.e("fdsf", "error occurred");
            } else if (!cursor.moveToFirst()) {
                // no media on the device
                Log.i("fdwef", "no media found");
            } else {
                int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.TITLE);
                int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Video.Media._ID);
                do {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    items.add(new VideoItem(thisId, thisTitle));
                    Log.i("fdsf", thisId + thisTitle);
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<VideoItem> longStringMap) {
            MainActivity.this.videoListAdapter.addAll(longStringMap);
        }
    }
}
