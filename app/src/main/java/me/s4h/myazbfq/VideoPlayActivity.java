package me.s4h.myazbfq;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.util.Log;
import android.view.Gravity;

public class VideoPlayActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    public static final String KEY_URL = "video_id";

    private static final String TAG = "VideoPlayActivity";
    private static final int PLAY_RETURN = 2 * 1000; // 2 seconds
    private static final String KEY_PLAY_POSITON = "paly_position";
    private static final String TOAST_ERROR_URL = "Paly url is null, please check parameter:" + KEY_URL;
    private static final String TOAST_ERROR_PLAY = "Paly error, please check url exist!";
    private static final String DIALOG_TITILE = "奋力加载中，请稍后...";

    private static String url;

    private ProgressDialog progressDialog;
    private MediaController mc;
    private VideoView videoView;
    private LinearLayout llMain;
    private ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        url = getIntent().getStringExtra(KEY_URL);
        if (url == null && savedInstanceState != null) {
            url = savedInstanceState.getString(KEY_URL);
        }

        if (url == null) {
            Toast.makeText(getApplicationContext(), TOAST_ERROR_URL, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

//        setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);


        videoView = new VideoView(this);
        videoView.setVideoURI(Uri.parse(url));
        videoView.requestFocus();
        videoView.setOnPreparedListener(this);
        videoView.setOnErrorListener(this);

        mc = new MediaController(this);
        mc.setAnchorView(videoView);
        mc.setKeepScreenOn(true);

        videoView.setMediaController(mc);

        llMain = new LinearLayout(this);
        llMain.setGravity(Gravity.CENTER);
        llMain.setOrientation(LinearLayout.VERTICAL);
        llMain.setLayoutParams(params);

        llMain.addView(videoView, params);
        setContentView(llMain);

        initDialog();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int palyPosition = videoView.getCurrentPosition();
        if (palyPosition > PLAY_RETURN) {
            palyPosition -= PLAY_RETURN;
        }
        outState.putInt(KEY_PLAY_POSITON, palyPosition);
        outState.putString(KEY_URL, url);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        videoView.seekTo(savedInstanceState.getInt(KEY_PLAY_POSITON));
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        progressDialog.cancel();

        videoView.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "onError:" + url);

        Toast.makeText(getApplicationContext(), TOAST_ERROR_PLAY + "\n" + url, Toast.LENGTH_LONG).show();
        progressDialog.cancel();
        finish();

        return true;
    }

    private void initDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(DIALOG_TITILE);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
}

