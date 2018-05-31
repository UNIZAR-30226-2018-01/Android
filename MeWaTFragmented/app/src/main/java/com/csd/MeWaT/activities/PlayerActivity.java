package com.csd.MeWaT.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.utils.DownloadSongImageTask;
import com.csd.MeWaT.utils.DownloadSongTask;
import com.csd.MeWaT.utils.Library;
import com.csd.MeWaT.utils.Song;
import com.csd.MeWaT.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener  {

    private static ImageButton btnPlay;
    @BindView(R.id.btnNext)
    ImageButton btnNext;
    @BindView(R.id.btnPrevious)
    ImageButton btnPrevious;
    @BindView(R.id.btnPlaylist)
    ImageButton btnPlaylist;
    @BindView(R.id.btnRepeat)
    ImageButton btnRepeat;
    @BindView(R.id.btnShuffle)
    ImageButton btnShuffle;

    private static de.hdodenhof.circleimageview.CircleImageView albumThumbnail;
    @BindView(R.id.minimizePlayer)
    ImageButton minimizePlayer;


    @BindView(R.id.songCurrentDurationLabel)
    TextView songCurrentDurationLabel;
    @BindView(R.id.songTotalDurationLabel)
    TextView songTotalDurationLabel;


    private static SeekBar songProgressBar;
    private static TextView songTitleLabel;
    private static TextView songAlbumLabel;
    private static TextView songArtistLabel;


    // Media Player

    private static MediaPlayer mp;
    private Handler mHandler = new Handler();;
    private Utils utils;
    public static int currentSongIndex = 0;
    private boolean isShuffle = false;
    private int isRepeat = 0;
    public static ArrayList<Song> songsList = new ArrayList<>();
    public static File cacheDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Mediaplayer
        mp = MainActivity.mp;
        cacheDir = MainActivity.cacheDir;
        currentSongIndex = MainActivity.songnumber;
        // Getting all songs list
        songsList = MainActivity.songsList;

        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);

        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songAlbumLabel = (TextView) findViewById(R.id.songAlbum);
       songArtistLabel = (TextView) findViewById(R.id.songArtist);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        albumThumbnail = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.album_thumbnail);

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                songProgressBar.setEnabled(true);
                songProgressBar.setProgress(0);
                songProgressBar.setMax(100);
                mp.start();

                // Updating progress bar
                updateProgressBar();
            }
        });

        btnRepeat.setImageResource(R.drawable.ic_repeat_black_24dp);

        if(!mp.isPlaying()){
            btnPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
            btnShuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);
            btnNext.setImageResource(R.drawable.ic_skip_next_black_24dp);
            btnPrevious.setImageResource(R.drawable.ic_skip_previous_black_24dp);
            btnPlaylist.setImageResource(R.drawable.ic_queue_music_black_24dp);

        }
        else {
            songTitleLabel.setText(songsList.get(currentSongIndex).getTitle());
            songArtistLabel.setText(songsList.get(currentSongIndex).getArtist());
            songAlbumLabel.setText(songsList.get(currentSongIndex).getAlbum());
            btnPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
            btnShuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);
            btnNext.setImageResource(R.drawable.ic_skip_next_black_24dp);
            btnPrevious.setImageResource(R.drawable.ic_skip_previous_black_24dp);
            btnPlaylist.setImageResource(R.drawable.ic_queue_music_black_24dp);
        }




        utils = new Utils();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        if(!mp.isPlaying())songProgressBar.setEnabled(false);
        else{
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        }
        songProgressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN );
        mp.setOnCompletionListener(this); // Important
        // Funcionalidad

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // check for already playing
                if(songsList.size()>0) {
                    if (mp.isPlaying()) {
                        mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                    } else {
                        // Resume song
                        if (mp != null) {
                            if(!MainActivity.resumed)playSong(currentSongIndex);
                            if(!songProgressBar.isEnabled())songProgressBar.setEnabled(true);
                            mp.start();
                            // Changing button image to pause button
                            btnPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                        }
                    }
                }
            }
        });



        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check if next song is there or not
                if(songsList.size()>0) {
                    if (currentSongIndex < (songsList.size() - 1)) {
                        playSongDownload(currentSongIndex + 1);
                        currentSongIndex = currentSongIndex + 1;
                    } else {
                        // play first song
                        playSongDownload(0);
                        currentSongIndex = 0;
                    }
                }
            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(songsList.size()>0) {
                    if (currentSongIndex > 0) {
                        playSongDownload(currentSongIndex - 1);
                        currentSongIndex = currentSongIndex - 1;
                    } else {
                        // play last song
                        playSongDownload(songsList.size() - 1);
                        currentSongIndex = songsList.size() - 1;
                    }
                }
            }
        });

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(isRepeat == 2){
                    isRepeat = 0;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.ic_repeat_black_24dp);
                }else if (isRepeat == 0){
                    // make repeat to true
                    isRepeat = 1;
                    Toast.makeText(getApplicationContext().getApplicationContext(), "Repeat all is ON", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.ic_repeat_all_blue_24dp);
                }else if (isRepeat == 1){
                    // make repeat to true
                    isRepeat = 2;
                    Toast.makeText(getApplicationContext().getApplicationContext(), "Repeat one is ON", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.ic_repeat_one_blue_24dp);
                }
            }
        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(isShuffle){
                    isShuffle = false;
                    Toast.makeText(getApplicationContext().getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);
                }else{
                    // make repeat to true
                    isShuffle= true;
                    Toast.makeText(getApplicationContext().getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.ic_shuffle_blue_24dp);
                }
            }
        });

        /**
         * Button Click event for Play list click event
         * Launches list activity which displays list of songs
         * */
        btnPlaylist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
                startActivityForResult(i, Library.PLAYLIST);
            }
        });

        minimizePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * Receiving song index from playlist view
     * and play the song
     * */
    @Override
    public void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if(requestCode == Library.PLAYLIST){
            if(resultCode == Activity.RESULT_OK) {
                 currentSongIndex= data.getExtras().getInt("songIndex");
                // play selected song
                playSongDownload(currentSongIndex);
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    public static void playSongDownload(int songIndex){
        currentSongIndex = songIndex;
        if(songsList.get(songIndex).getUrlLocal().equals("")) new DownloadSongTask(cacheDir).execute();
        else playSong(songIndex);
    }

    /**
     * Function to play a song
     * @param songIndex - index of song
     * */
    public static void  playSong(int songIndex) {
        // Play song
        try {
            MainActivity.resumed=true;
            songProgressBar.setEnabled(true);
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).getUrlLocal());
            mp.prepareAsync();
            // Displaying Song title
            songTitleLabel.setText(songsList.get(songIndex).getTitle());
            songAlbumLabel.setText(songsList.get(songIndex).getAlbum());
            songArtistLabel.setText(songsList.get(songIndex).getArtist());

            DownloadSongImageTask downloadSongImageTask = new DownloadSongImageTask(albumThumbnail);
            downloadSongImageTask.execute(songsList.get(songIndex).getUrlImg());


            MainActivity.songnumber=songIndex;

            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);

            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

            // Updating progress bar
            //updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

            // Displaying Total Duration time
            songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);

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
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        // check for repeat is ON or OFF
        if(isRepeat==2){
            // repeat is on play same song again
            playSongDownload(currentSongIndex);
        } else if(isShuffle){
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSongDownload(currentSongIndex);
        } else{
            // no repeat or shuffle ON - play next song
            if(currentSongIndex < (songsList.size() - 1)){
                currentSongIndex = currentSongIndex + 1;
                playSongDownload(currentSongIndex);
            }else{
                if(isRepeat == 1){
                    // play first song
                    playSongDownload(0);
                    currentSongIndex = 0;
                }
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

  }
