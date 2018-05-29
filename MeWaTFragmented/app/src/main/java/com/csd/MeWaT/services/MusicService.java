package com.csd.MeWaT.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.utils.Song;
import com.csd.MeWaT.utils.Utils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by mengd on 29/05/2018.
 */

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,View.OnClickListener{

    private Handler mHandler = new Handler();
    private final IBinder musicBind = new MusicBinder();

    private ArrayList<Song> songsList = new ArrayList<>();
    private static Integer songnumber=0,isRepeat=0;
    private static Boolean isShuffle,resumed;

    private WeakReference<ImageButton> btnPlay;
    private WeakReference<TextView> SongTitle;
    public static WeakReference<ProgressBar> songProgressBar;
    static Handler progressBarHandler = new Handler();

    public static MediaPlayer mp;
    private boolean isPause = false;

    @Override
    public void onCreate() {
        mp = new MediaPlayer();
        mp.reset();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mp.start();
                songProgressBar.get().setProgress(0);
                songProgressBar.get().setMax(100);
                // Updating progress bar
                updateProgressBar();
            }
        });
        super.onCreate();
    }

    public void setSongsList(ArrayList<Song> list){
        songsList=list;
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }



    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initUI();
        super.onStart(intent, startId);
        return START_STICKY;
    }

    public void setSong(int songIndex){
        songnumber=songIndex;
    }

    @Override
    public boolean onUnbind(Intent intent){
        mp.stop();
        mp.release();
        return false;
    }
    private void initUI() {
        btnPlay = new WeakReference<>(MainActivity.playTabPlayer);
        SongTitle = new WeakReference<>(MainActivity.songTitleTabPlayer);
        songProgressBar = new WeakReference<>(MainActivity.SongProgressBarTabPlayer);
        btnPlay.get().setOnClickListener(this);
        mp.setOnCompletionListener(this);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPlay:
                if (mp.isPlaying()) {
                    mp.pause();
                    isPause = true;
                    progressBarHandler.removeCallbacks(mUpdateTimeTask);
                    btnPlay.get().setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                    return;
                }
                if (isPause) {
                    mp.start();
                    isPause = false;
                    updateProgressBar();
                    btnPlay.get().setBackgroundResource(R.drawable.ic_pause_black_24dp);
                    return;
                }

                if (!mp.isPlaying()) {
                    playSong(songnumber);
                }
                break;
        }
    }


    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {

        mHandler.postDelayed(mUpdateTimeTask, 100);
    }


    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            Utils utils = new Utils();
            // Updating progress bar
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.get().setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };



    @Override
    public void onDestroy() {

    }


    public void  playSong(int songIndex) {
        // Play song
        try {
            resumed=true;
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).getUrl());
            mp.prepareAsync();
            // Displaying Song title
            String songTitle = songsList.get(songIndex).getTitle();
            SongTitle.get().setText(songTitle);
            songnumber=songIndex;

            // Changing Button Image to pause image
            btnPlay.get().setImageResource(R.drawable.ic_pause_black_24dp);

            // set Progress bar values
            songProgressBar.get().setProgress(0);
            songProgressBar.get().setMax(100);


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        // check for repeat is ON or OFF
        if(isRepeat==2){
            // repeat is on play same song again
            playSong(songnumber);
        } else if(isShuffle){
            // shuffle is on - play a random song
            Random rand = new Random();
            songnumber = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSong(songnumber);
        } else{
            // no repeat or shuffle ON - play next song
            if(songnumber < (songsList.size() - 1)){
                songnumber = songnumber + 1;
                playSong(songnumber);
            }else{
                if(isRepeat == 1){
                    // play first song
                    playSong(0);
                    songnumber = 0;
                }
            }
        }
    }

}
