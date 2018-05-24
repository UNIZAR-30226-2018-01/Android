package com.csd.MeWaT.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.csd.MeWaT.R;
import com.csd.MeWaT.fragments.BaseFragment;
import com.csd.MeWaT.fragments.HomeFragment;
import com.csd.MeWaT.fragments.ProfileFragment;
import com.csd.MeWaT.fragments.SearchFragment;
import com.csd.MeWaT.fragments.SocialFragment;
import com.csd.MeWaT.fragments.UploadFragment;
import com.csd.MeWaT.utils.FragmentHistory;
import com.csd.MeWaT.utils.Song;
import com.csd.MeWaT.utils.Utils;
import com.csd.MeWaT.views.FragNavController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentNavigation,FragNavController.TransactionListener, FragNavController.RootFragmentListener, MediaPlayer.OnCompletionListener   {



    @BindView(R.id.content_frame)
    FrameLayout contentFrame;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindArray(R.array.tab_name)
    String[] TABS;

    @BindView(R.id.bottom_tab_layout)
    TabLayout bottomTabLayout;

    @BindView(R.id.tab_player_layout)
    LinearLayout tabPlayerLayout;

    @BindView(R.id.songProgressBarTabPlayer)
    ProgressBar SongProgressBarTabPlayer;

    @BindView(R.id.songTitleTabPlayer)
    TextView songTitleTabPlayer;

    @BindView(R.id.playTabPlayer)
    ImageButton playTabPlayer;

    @BindView(R.id.expandPlayer)
    ImageButton extendPlayer;


    private Handler mHandler = new Handler();;

    private int[] mTabIconsSelected = {
            R.drawable.tab_home,
            R.drawable.tab_search,
            R.drawable.tab_share,
            R.drawable.tab_social,
            R.drawable.tab_profile};

    public static MediaPlayer mp;
    public static ArrayList<Song> songsList = new ArrayList<>();
    public static String user, idSesion;
    public static Integer songnumber=0;
    public static Boolean isShuffle = false;
    public static Integer isRepeat = 0;
    public static Boolean resumed = false;

    private FragNavController mNavController;
    private FragmentHistory fragmentHistory;



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        resumed = false;
        mp=new MediaPlayer();

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mp.start();
                // Updating progress bar
                updateProgressBar();
            }
        });

        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);



        idSesion=getIntent().getExtras().getString("idSesion");
        user = getIntent().getExtras().getString("user");

        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);
        tabPlayerLayout = (LinearLayout) this.findViewById(R.id.tab_player_layout);


        ButterKnife.bind(this);


        SongProgressBarTabPlayer.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN );

        initToolbar();

        initTab();
        fragmentHistory = new FragmentHistory();

        SongProgressBarTabPlayer.setProgress(0);
        SongProgressBarTabPlayer.setMax(100);

        mNavController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.content_frame)
                .transactionListener(this)
                .rootFragmentListener(this, TABS.length)
                .build();


        switchTab(0);

        bottomTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                fragmentHistory.push(tab.getPosition());

                switchTab(tab.getPosition());


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

                mNavController.clearStack();

                switchTab(tab.getPosition());


            }
        });
        playTabPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check for already playing
                if(songsList.size()>0) {
                    if (mp.isPlaying()) {
                        mp.pause();
                        // Changing button image to play button
                        playTabPlayer.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    } else {
                        // Resume song
                        if (mp != null) {
                            if(!SongProgressBarTabPlayer.isEnabled())SongProgressBarTabPlayer.setEnabled(true);
                            playSong(songnumber);
                            // Changing button image to pause button
                            playTabPlayer.setImageResource(R.drawable.ic_pause_black_24dp);
                        }
                    }
                }
            }
        });


        final Activity i = this;
        extendPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent player = new Intent(i,PlayerActivity.class);
                i.startActivity(player);
            }
        });

        tabPlayerLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b && mp.isPlaying()){
                    songTitleTabPlayer.setText(songsList.get(songnumber).getTitle());
                    // Updating progress bar
                    updateProgressBar();
                }

            }
        });


    }


    public void  playSong(int songIndex) {
        // Play song
        try {
            resumed=true;
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).getUrl());
            mp.prepare();
            // Displaying Song title
            String songTitle = songsList.get(songIndex).getTitle();
            songTitleTabPlayer.setText(songTitle);
            songnumber=songIndex;

            // Changing Button Image to pause image
            playTabPlayer.setImageResource(R.drawable.ic_pause_black_24dp);

            // set Progress bar values
            SongProgressBarTabPlayer.setProgress(0);
            SongProgressBarTabPlayer.setMax(100);


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

            Utils utils = new Utils();
            // Updating progress bar
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            SongProgressBarTabPlayer.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    private void initToolbar() {
        setSupportActionBar(toolbar);


    }

    private void initTab() {
        if (bottomTabLayout != null) {
            for (int i = 0; i < TABS.length; i++) {
                bottomTabLayout.addTab(bottomTabLayout.newTab());
                TabLayout.Tab tab = bottomTabLayout.getTabAt(i);
                if (tab != null)
                    tab.setCustomView(getTabView(i));
            }
        }
    }


    private View getTabView(int position) {
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.tab_item_bottom, null,false);
        ImageView icon = (ImageView) view.findViewById(R.id.tab_icon);
        icon.setImageDrawable(Utils.setDrawableSelector(MainActivity.this, mTabIconsSelected[position], mTabIconsSelected[position]));
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {

        super.onStop();
    }


    private void switchTab(int position) {
        mNavController.switchTab(position);
        updateToolbarTitle(position);
    }


    @Override
    protected void onResume() {

        super.onResume();
       if(mp.isPlaying()){
           songTitleTabPlayer.setText(songsList.get(songnumber).getTitle());
            updateProgressBar();
       }


    }


    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }
    }


    @Override
    public void onTabTransaction(Fragment fragment, int index) {
        // If we have a backstack, show the back button
        if (getSupportActionBar() != null && mNavController != null) {


            updateToolbar();

        }
    }

    private void updateToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }


    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        //do fragmentty stuff. Maybe change title, I'm not going to tell you how to live your life
        // If we have a backstack, show the back button
        if (getSupportActionBar() != null && mNavController != null) {

            updateToolbar();

        }
    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {

            case FragNavController.TAB1:
                return new HomeFragment();
            case FragNavController.TAB2:
                return new SearchFragment();
            case FragNavController.TAB3:
                return new UploadFragment();
            case FragNavController.TAB4:
                return new SocialFragment();
            case FragNavController.TAB5:
                return new ProfileFragment();


        }
        throw new IllegalStateException("Need to send an index that we know");
    }


    private void updateToolbarTitle(int position){


        getSupportActionBar().setTitle(TABS[position]);

    }


    public void updateToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);

    }


    @Override
    public void pushFragment(Fragment fragment) {
        if (mNavController != null) {
            mNavController.pushFragment(fragment);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (!mNavController.isRootFragment()) {
            mNavController.popFragment();
        } else {

            if (fragmentHistory.isEmpty()) {
                super.onBackPressed();
            } else {


                if (fragmentHistory.getStackSize() > 1) {

                    int position = fragmentHistory.popPrevious();

                    switchTab(position);

                    updateTabSelection(position);

                } else {

                    switchTab(4);

                    updateTabSelection(4);

                    fragmentHistory.emptyStack();
                }
            }

        }
    }


    private void updateTabSelection(int currentTab){

        for (int i = 0; i <  TABS.length; i++) {
            TabLayout.Tab selectedTab = bottomTabLayout.getTabAt(i);
            if(currentTab != i) {
                selectedTab.getCustomView().setSelected(false);
            }else{
                selectedTab.getCustomView().setSelected(true);
            }
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
