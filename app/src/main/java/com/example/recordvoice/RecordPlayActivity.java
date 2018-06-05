package com.example.recordvoice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.recordvoice.constant.Constants;
import java.io.IOException;
/**
 * Created by vieta on 17/8/2016.
 */
public class RecordPlayActivity extends Activity implements SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener,View.OnClickListener {
    private TextView phoneNumber, videoName, timeDate, timeCurrent, timeDuration;
    private MediaPlayer mediaPlayer;
    private ImageView btnPlay;
    private SeekBar seekBar;
    private Handler mHandler = new Handler();
    private Utilities utils;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_play);
        initView();

//        WindowManager.LayoutParams params = getWindow().getAttributes();
//        params.x = -20;
//        params.height = 400;
//        params.width = 700;
//        params.y = -20;
//
//        this.getWindow().setAttributes(params);

        Intent intent= getIntent();
        String phone_number = intent.getStringExtra("phone_number");
        String video_name = intent.getStringExtra("video_name");
        String time_date =  intent.getStringExtra("time");

        phoneNumber.setText(phone_number);
        videoName.setText(video_name);
        timeDate.setText(time_date);

        mediaPlayer = new MediaPlayer();
        utils = new Utilities();
        playRecord(video_name);
        seekBar.setOnSeekBarChangeListener(this);
        mediaPlayer.setOnCompletionListener(this);
        btnPlay.setOnClickListener(this);
//        ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor();
//        Future longRunningTaskFuture = threadPoolExecutor.submit(longRunningTask);
    }

    public void initView(){
        phoneNumber = (TextView) findViewById(R.id.phone_number);
        videoName = (TextView) findViewById(R.id.videoName);
        timeDate = (TextView) findViewById(R.id.time_date);
        btnPlay = (ImageView) findViewById(R.id.btn_Play);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        timeCurrent = (TextView) findViewById(R.id.timeCurrent);
        timeDuration = (TextView) findViewById(R.id.timeDuration);
    }

    private void playRecord(String charSequence){
        String filepath = FileHelper.getFilePath(context) + "/"
                + Constants.FILE_DIRECTORY +"/"+ charSequence;
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(filepath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            btnPlay.setImageResource(R.drawable.pause);

            updateProgressBar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mediaPlayer.getDuration();
            long currentDuration = mediaPlayer.getCurrentPosition();

//            // Displaying Total Duration time
            timeDuration.setText(""+utils.milliSecondsToTimer(totalDuration));
//            // Displaying time completed playing
            timeCurrent.setText(""+utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            seekBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mediaPlayer.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mediaPlayer.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        btnPlay.setImageResource(R.drawable.play);
    }

    @Override
    public void onClick(View view) {
        if(mediaPlayer.isPlaying()){
            if(mediaPlayer!=null){
                mediaPlayer.pause();
            }
            btnPlay.setImageResource(R.drawable.play);
        }else{
            if(mediaPlayer!=null){
                mediaPlayer.start();
            }
            btnPlay.setImageResource(R.drawable.pause);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();

    }
}
