package com.csd.MeWaT.fragments;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.activities.PlayListActivity;
import com.csd.MeWaT.utils.SongsManager;
import com.csd.MeWaT.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends Fragment implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener  {


    private OnFragmentInteractionListener mListener;

    @BindView(R.id.btnPlay)
    ImageButton btnPlay;
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

    @BindView(R.id.songProgressBar)
    SeekBar songProgressBar;
    @BindView(R.id.songCurrentDurationLabel)
    TextView songCurrentDurationLabel;
    @BindView(R.id.songTotalDurationLabel)
    TextView songTotalDurationLabel;


    @BindView(R.id.songTitle)
    TextView songTitleLabel;
    @BindView(R.id.albumTitle)
    TextView songAlbumLabel;

    // Media Player
    private MediaPlayer mp;
    private Handler mHandler = new Handler();;
    private SongsManager songManager;
    private Utils utils;
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private int isRepeat = 0;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    public PlayerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlayerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayerFragment newInstance() {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mediaplayer
        mp = MainActivity.mp;
        songManager = new SongsManager();
        // Getting all songs list
        songsList = MainActivity.songsList;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, view);


        btnRepeat.setImageResource(R.drawable.ic_repeat_black_24dp);
        btnPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        btnShuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);
        btnNext.setImageResource(R.drawable.ic_skip_next_black_24dp);
        btnPrevious.setImageResource(R.drawable.ic_skip_previous_black_24dp);
        btnPlaylist.setImageResource(R.drawable.ic_queue_music_black_24dp);

        utils = new Utils();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important




        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle bundle){

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // check for already playing
                if(songsList.size()>0) {
                    if (mp.isPlaying()) {
                        if (mp != null) {
                            mp.pause();
                            // Changing button image to play button
                            btnPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                        }
                    } else {
                        // Resume song
                        if (mp != null) {
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
                        playSong(currentSongIndex + 1);
                        currentSongIndex = currentSongIndex + 1;
                    } else {
                        // play first song
                        playSong(0);
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
                        playSong(currentSongIndex - 1);
                        currentSongIndex = currentSongIndex - 1;
                    } else {
                        // play last song
                        playSong(songsList.size() - 1);
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
                    Toast.makeText(getActivity().getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.ic_repeat_black_24dp);
                }else if (isRepeat == 0){
                    // make repeat to true
                    isRepeat = 1;
                    Toast.makeText(getActivity().getApplicationContext(), "Repeat all is ON", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.ic_repeat_all_white_24dp);
                }else if (isRepeat == 1){
                    // make repeat to true
                    isRepeat = 2;
                    Toast.makeText(getActivity().getApplicationContext(), "Repeat one is ON", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.ic_repeat_one_white_24dp);
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
                    Toast.makeText(getActivity().getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);
                }else{
                    // make repeat to true
                    isShuffle= true;
                    Toast.makeText(getActivity().getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.ic_shuffle_white_24dp);
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
                Intent i = new Intent(view.getContext(), PlayListActivity.class);
                startActivityForResult(i, 100);
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
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
            currentSongIndex = data.getExtras().getInt("songIndex");
            // play selected song
            playSong(currentSongIndex);
        }

    }

    /**
     * Function to play a song
     * @param songIndex - index of song
     * */
    public void  playSong(int songIndex) {
        // Play song
        try {
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).get("songPath"));
            mp.prepare();
            mp.start();
            // Displaying Song title
            String songTitle = songsList.get(songIndex).get("songTitle");
            songTitleLabel.setText(songTitle);

            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);

            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

            // Updating progress bar
            updateProgressBar();
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

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     * */
    @Override
    public void onCompletion(MediaPlayer arg0) {

        // check for repeat is ON or OFF
        if(isRepeat==2){
            // repeat is on play same song again
            playSong(currentSongIndex);
        } else if(isShuffle){
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSong(currentSongIndex);
        } else{
            // no repeat or shuffle ON - play next song
            if(currentSongIndex < (songsList.size() - 1)){
                playSong(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            }else{
                if(isRepeat == 1){
                    // play first song
                    playSong(0);
                    currentSongIndex = 0;
                }
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mp.release();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
