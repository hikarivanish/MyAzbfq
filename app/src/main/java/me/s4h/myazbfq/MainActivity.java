package me.s4h.myazbfq;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.provider.MediaStore;
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
import butterknife.OnItemLongClick;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
        SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {
    private static final String LIFESYCLE_TAG = "lifesycle_tag--------->";

    @Bind(R.id.audioListView)
    ListView audioListView;

    @Bind(R.id.videoListView)
    ListView videoListView;

    AudioListAdapter audioListAdapter;
    VideoListAdapter videoListAdapter;
    MediaPlayer mMediaPlayer;


    @Bind(R.id.seekBar)
    SeekBar seekBar;

    @Bind(R.id.infoTextView)
    TextView infoTextView;


    @Bind(R.id.playBtn)
    Button playBtn;

    @Bind(R.id.play_progress)
    TextView playProgressTextView;

    boolean activityRunning = false;
    AudioItem nowPlaying = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("fdsf", "onCreate");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        seekBar.setOnSeekBarChangeListener(this);

        audioListAdapter = new AudioListAdapter(this);
        audioListView.setAdapter(audioListAdapter);

        videoListAdapter = new VideoListAdapter(this);
        videoListView.setAdapter(videoListAdapter);

        new AudioScanTask().execute();
        new VideoScanTask().execute();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);


    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LIFESYCLE_TAG, "onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LIFESYCLE_TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LIFESYCLE_TAG, "onResume");
        activityRunning = true;
        new TrackPositionTask().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LIFESYCLE_TAG, "onPause");
        activityRunning = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LIFESYCLE_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LIFESYCLE_TAG, "onDestroy");
        mMediaPlayer.release();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.i(LIFESYCLE_TAG, "onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState)" + outState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(LIFESYCLE_TAG, "onSaveInstanceState(Bundle outState)" + outState);
    }

    @OnItemClick(R.id.audioListView)
    void onAudioItemClick(ListView listView, View view, int position, long idk) {
        AudioItem item = (AudioItem) listView.getItemAtPosition(position);
        nowPlaying = item;
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
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            this.playBtn.setText("►");
        }
        VideoItem item = (VideoItem) listView.getItemAtPosition(position);
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, item.id);

        Intent intent = new Intent(this, VideoPlayActivity.class);
        intent.putExtra(VideoPlayActivity.KEY_URL, contentUri.toString());
        this.startActivity(intent);

    }


    //MediaMetadataRetriever
    @OnItemLongClick(R.id.audioListView)
    boolean onAudioItemLongClick(ListView listView, View view, int position, long idk) {
        AudioItem item = (AudioItem) listView.getItemAtPosition(position);

        return false;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        this.playBtn.setEnabled(true);
        this.playBtn.setText("| |");
    }

    @OnClick(R.id.playBtn)
    void playBtnClick() {
        if (this.mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            playBtn.setText("►");
        } else {
            mMediaPlayer.start();
            playBtn.setText("| |");
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && this.nowPlaying != null) {
            int p = nowPlaying.duration * progress / 100;
            this.playProgressTextView.setText(String.format("%02d:%02d", p / 60_000, p / 1000 % 60));
        }

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
        this.playBtn.setText("►");
    }

    private class TrackPositionTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.i("fdsf", "TrackPositionTask.doInBackground");
            while (MainActivity.this.activityRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("fdsf", "error sleep", e);
                }
                Log.d("fdsf", "TrackPositionTask is tracking");
                if (MainActivity.this.activityRunning && mMediaPlayer.isPlaying()) {
                    publishProgress(mMediaPlayer.getCurrentPosition(), mMediaPlayer.getDuration());
                }

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            seekBar.setProgress(values[0] * 100 / values[1]);
            playProgressTextView.setText(String.format("%02d:%02d", values[0] / 60_000, values[0] / 1000 % 60));
        }
    }


    private class AudioScanTask extends AsyncTask<Void, Void, List<AudioItem>> {

        @Override
        protected List<AudioItem> doInBackground(Void... params) {
            Log.i("fsdf", "AudioScanTask.doInBackground");
            ContentResolver contentResolver = MainActivity.this.getContentResolver();

            List<AudioItem> items = new ArrayList<>();
            //Some audio may be explicitly marked as not being music
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
//                    MediaStore.Audio.Media.DATA,//文件路径
//                    MediaStore.Audio.Media.DISPLAY_NAME,//文件名
                    MediaStore.Audio.Media.DURATION,//长度秒
                    MediaStore.Audio.Media.YEAR,
                    MediaStore.Audio.Media.ALBUM
            };
            Cursor cursor = contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    null);

            while (cursor.moveToNext()) {
                items.add(new AudioItem(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getString(5)
                ));
            }
            // see http://androidsnippets.com/list-all-music-files
            cursor.close();
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
            Log.i("fsdf", "VideoScanTask.doInBackground");
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
                int heightColumn = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT);
                int widthColumn = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH);
                int durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
                do {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    long thisDuration = cursor.getLong(durationColumn);
                    long thisWidth = cursor.getLong(widthColumn);
                    long thisHeight = cursor.getLong(heightColumn);
                    items.add(new VideoItem(thisId, thisTitle,thisHeight,thisWidth,thisDuration));
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
