package com.csd.MeWaT.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import android.widget.SeekBar;
import android.widget.TextView;

import com.csd.MeWaT.R;
import com.csd.MeWaT.fragments.BaseFragment;
import com.csd.MeWaT.fragments.HomeFragment;
import com.csd.MeWaT.fragments.ProfileFragment;
import com.csd.MeWaT.fragments.SearchFragment;
import com.csd.MeWaT.fragments.SocialFragment;
import com.csd.MeWaT.fragments.UploadFragment;
import com.csd.MeWaT.services.MusicService;
import com.csd.MeWaT.utils.DownloadSongTask;
import com.csd.MeWaT.utils.DownloadUserImageTask;
import com.csd.MeWaT.utils.FragmentHistory;
import com.csd.MeWaT.utils.Lista;
import com.csd.MeWaT.utils.Song;
import com.csd.MeWaT.utils.Utils;
import com.csd.MeWaT.views.FragNavController;
import com.csd.MeWaT.services.MusicService.MusicBinder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.DebouncingOnClickListener;


public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentNavigation,FragNavController.TransactionListener, FragNavController.RootFragmentListener, MediaPlayer.OnCompletionListener {



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

    public static ProgressBar SongProgressBarTabPlayer;

    public static TextView songTitleTabPlayer;

    public static ImageButton playTabPlayer;

    @BindView(R.id.expandPlayer)
    ImageButton extendPlayer;


    private Handler mHandler = new Handler();;

    private int pos=0;
    private int[] mTabIconsSelected = {
            R.drawable.tab_home,
            R.drawable.tab_search,
            R.drawable.tab_share,
            R.drawable.tab_social,
            R.drawable.tab_profile};

    public static MediaPlayer mp;
    public static ArrayList<Song> songsList = new ArrayList<>();
    public static ArrayList<Song> favList = new ArrayList<>();
    public static String user, idSesion,password;
    public static Integer songnumber=0;
    public static Boolean isShuffle = false;
    public static Integer isRepeat = 0;
    public static Boolean resumed = false;
    public static Boolean player = false;

    private FragNavController mNavController;
    private FragmentHistory fragmentHistory;
    public static File cacheDir;


    public static ArrayList<Lista> lists = new ArrayList<>();
    public static ArrayList<String> followedUser = new ArrayList<>();



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        resumed = false;
        mp=new MediaPlayer();
        cacheDir = getCacheDir();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                SongProgressBarTabPlayer.setProgress(0);
                SongProgressBarTabPlayer.setMax(100);
                mp.start();
                updateProgressBar();
            }
        });

        idSesion=getIntent().getExtras().getString("idSesion");
        user = getIntent().getExtras().getString("user");
        password = getIntent().getExtras().getString("password");


        new SearchFavSongs().execute();
        new SearchListByUser().execute();
        new getFollowingUsers().execute();


        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);
        tabPlayerLayout = (LinearLayout) this.findViewById(R.id.tab_player_layout);


        ButterKnife.bind(this);

        SongProgressBarTabPlayer = (ProgressBar) findViewById(R.id.songProgressBarTabPlayer);
        songTitleTabPlayer = (TextView) findViewById(R.id.songTitleTabPlayer);
        playTabPlayer = (ImageButton) findViewById(R.id.playTabPlayer);

        SongProgressBarTabPlayer.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN );

        initToolbar();

        initTab();
        fragmentHistory = new FragmentHistory();

        mNavController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.content_frame)
                .transactionListener(this)
                .rootFragmentListener(this, TABS.length)
                .build();


        switchTab(0);

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
                            if(!resumed)playSongDownload(songnumber);
                            if(!SongProgressBarTabPlayer.isEnabled())SongProgressBarTabPlayer.setEnabled(true);
                            mp.start();
                            // Changing button image to pause button
                            playTabPlayer.setImageResource(R.drawable.ic_pause_black_24dp);
                        }
                    }
                }
            }
        });


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

        final Activity i = this;
        extendPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player=true;
                Intent player = new Intent(i,PlayerActivity.class);
                i.startActivity(player);
            }
        });

    }


    public static void setSongsListAndStart(ArrayList<Song> put, int number){
        boolean iguales = true;
        for(int i = 0; i<songsList.size();i++)if(!songsList.get(i).equals(put.get(i)))iguales=false;
        if(!iguales || songsList.size()==0) {
            for (Song s : songsList) {
                if (!s.getUrlLocal().equals("")) new File(s.getUrlLocal()).delete();
            }
            songsList = put;
            songnumber=number;
            new DownloadSongTask(cacheDir).execute();
        }else{
            playSongDownload(number);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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


    public static void playSongDownload(int songIndex){
        songnumber = songIndex;
        if(songsList.get(songIndex).getUrlLocal().equals(""))new DownloadSongTask(cacheDir).execute();
        else playSong(songIndex);
    }


    public static void  playSong(int songIndex) {
        // Play song
        try {
            resumed=true;
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).getUrlLocal());
            mp.prepareAsync();
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


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(player) {
            // check for repeat is ON or OFF
            if (isRepeat == 2) {
                // repeat is on play same song again
                playSongDownload(songnumber);
            } else if (isShuffle) {
                // shuffle is on - play a random song
                Random rand = new Random();
                songnumber = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
                playSongDownload(songnumber);
            } else {
                // no repeat or shuffle ON - play next song
                if (songnumber < (songsList.size() - 1)) {
                    songnumber = songnumber + 1;
                    playSongDownload(songnumber);
                } else {
                    if (isRepeat == 1) {
                        // play first song
                        playSongDownload(0);
                        songnumber = 0;
                    }
                }
            }
        }else{
            PlayerActivity.onCompletion(mediaPlayer);
        }
    }





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
    public void onStop() {

        super.onStop();
    }


    private void switchTab(int position) {
        pos=position;
        mNavController.clearStack();
        Hola.clear();
        mNavController.switchTab(position);
        updateToolbarTitle(position);
    }


    @Override
    protected void onResume() {
        super.onResume();
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

    public static ArrayList<String> Hola = new ArrayList<>();

    @Override
    public void pushFragment(Fragment fragment) {
        if (mNavController != null) {
            Hola.add(TABS[pos]);
            mNavController.pushFragment(fragment);
        }
    }

    @Override
    public void pushFragment1(Fragment fragment,String Title){
        if (mNavController != null) {
            Hola.add(Title);
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
        getSupportActionBar().setTitle(Hola.get(Hola.size()-1));
        Hola.remove(Hola.get(Hola.size()-1));
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

                    switchTab(0);

                    updateTabSelection(0);

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


    public class SearchFavSongs extends AsyncTask<Void, Void, Boolean> {


        SearchFavSongs() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;


            favList = new ArrayList<>();
            try {
                url = new URL("https://mewat1718.ddns.net/ps/VerLista");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("nombreLista", "favoritos")
                        .appendQueryParameter("nombreCreadorLista", user);             //Añade parametros
                String query = builder.build().getEncodedQuery();

                OutputStream os = client.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                int responseCode = client.getResponseCode();
                System.out.println("\nSending 'Get' request to URL : " +    url+"--"+responseCode);
            } catch (MalformedURLException e) {
                return false;
            } catch (SocketTimeoutException e) {
                return false;
            }catch (IOException e) {
                return false;
            }
            try {
                inputStream = new InputStreamReader(client.getInputStream());


                BufferedReader reader = new BufferedReader(inputStream);
                StringBuilder builder = new StringBuilder();

                for (String line = null; (line = reader.readLine()) != null ; ) {
                    builder.append(line).append("\n");
                }

                // Parse into JSONObject
                String resultStr = builder.toString();
                JSONTokener tokener = new JSONTokener(resultStr);
                JSONObject result = new JSONObject(tokener);

                client.disconnect();
                if (!result.has("error")){

                    JSONArray resultArray = result.getJSONArray("canciones");
                    for(int i = 0; i<resultArray.length();i++){
                        JSONObject jsObj = resultArray.getJSONObject(i);
                        favList.add(new Song(jsObj.getString("tituloCancion"),
                                        jsObj.getString("nombreAlbum"),
                                        jsObj.getString("nombreArtista"),
                                        jsObj.getString("genero"),
                                        jsObj.getString("ruta").replace("/usr/local/apache-tomcat-9.0.7/webapps","https://mewat1718.ddns.net"),
                                        jsObj.getString("ruta_imagen").replace("..","https://mewat1718.ddns.net")
                                )
                        );
                    }
                }else{
                    return false;
                }


            }catch (IOException e){
                Throwable s = e.getCause();
                return false;
            } catch (JSONException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

        }

        @Override
        protected void onCancelled() {

        }
    }

    /**
     * Represents an asynchronous song search
     */
    public class SearchListByUser extends AsyncTask<String, Void, Boolean> {


        SearchListByUser(){}

        ArrayList<Lista> rubbish = new ArrayList<>();

        @Override
        protected Boolean doInBackground(String... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;

            rubbish.clear();
            try {
                url = new URL("https://mewat1718.ddns.net/ps/MostrarListasReproduccion");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("user", MainActivity.user)
                        .appendQueryParameter("contrasenya", MainActivity.password);             //Añade parametros
                String query = builder.build().getEncodedQuery();

                OutputStream os = client.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();

                int responseCode = client.getResponseCode();
                System.out.println("\nSending 'Get' request to URL : " +    url+"--"+responseCode);
            } catch (MalformedURLException e) {
                return false;
            } catch (SocketTimeoutException e) {
                return false;
            }catch (IOException e) {
                return false;
            }
            try {
                inputStream = new InputStreamReader(client.getInputStream());


                BufferedReader reader = new BufferedReader(inputStream);
                StringBuilder builder = new StringBuilder();

                for (String line = null; (line = reader.readLine()) != null ; ) {
                    builder.append(line).append("\n");
                }

                // Parse into JSONObject
                String resultStr = builder.toString();
                JSONTokener tokener = new JSONTokener(resultStr);
                JSONObject result = new JSONObject(tokener);

                client.disconnect();
                if (!result.has("error")){

                    JSONArray resultArray = result.getJSONArray("nombre");
                    for(int i = 0; i<resultArray.length();i++){
                        if(!resultArray.getString(i).equals("Favoritos"))rubbish.add(new Lista(resultArray.getString(i),MainActivity.user));
                    }
                    resultArray.get(0);
                }else{
                    return false;
                }


            }catch (IOException e){
                Throwable s = e.getCause();
                return false;
            } catch (JSONException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                lists = rubbish;
            } else {

            }
        }

        @Override
        protected void onCancelled() {

        }
    }


    /**
     * Represents an asynchronous album search task
     */
    public static class getFollowingUsers extends AsyncTask<String, Void, Boolean> {


        public getFollowingUsers() {

        }

        @Override
        protected Boolean doInBackground(String... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;

            followedUser.clear();
            try {
                url = new URL("https://mewat1718.ddns.net/ps/VerSeguidos");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);

                int responseCode = client.getResponseCode();
                System.out.println("\nSending 'Get' request to URL : " +    url+"--"+responseCode);
            } catch (MalformedURLException e) {
                return false;
            } catch (SocketTimeoutException e) {
                return false;
            }catch (IOException e) {
                return false;
            }
            try {
                inputStream = new InputStreamReader(client.getInputStream());


                BufferedReader reader = new BufferedReader(inputStream);
                StringBuilder builder = new StringBuilder();

                for (String line = null; (line = reader.readLine()) != null ; ) {
                    builder.append(line).append("\n");
                }

                // Parse into JSONObject
                String resultStr = builder.toString();
                JSONTokener tokener = new JSONTokener(resultStr);
                JSONObject result = new JSONObject(tokener);

                client.disconnect();
                if (!result.has("error")){
                    JSONArray resultArray = result.getJSONArray("listaDeSeguidos");
                    for(int i = 0; i<resultArray.length();i++){
                        JSONObject jsObj = resultArray.getJSONObject(i);
                        followedUser.add( jsObj.getString("nombreSeguido"));
                    }
                    resultArray.get(0);
                }else{
                    return false;
                }
            }catch (IOException e){
                Throwable s = e.getCause();
                return false;
            } catch (JSONException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {

            } else {

            }

        }

        @Override
        protected void onCancelled() {

        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mp.stop();
        mp.release();
    }

}
