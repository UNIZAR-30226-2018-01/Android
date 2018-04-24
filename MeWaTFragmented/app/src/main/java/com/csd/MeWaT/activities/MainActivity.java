package com.csd.MeWaT.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.fragments.BaseFragment;
import com.csd.MeWaT.fragments.HomeFragment;
import com.csd.MeWaT.fragments.NewsFragment;
import com.csd.MeWaT.fragments.PlayerFragment;
import com.csd.MeWaT.fragments.ProfileFragment;
import com.csd.MeWaT.fragments.SearchFragment;
import com.csd.MeWaT.fragments.UploadFragment;
import com.csd.MeWaT.utils.FragmentHistory;
import com.csd.MeWaT.utils.Utils;
import com.csd.MeWaT.views.FragNavController;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.prefs.Preferences;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.webkit.CookieSyncManager.createInstance;


public class MainActivity extends BaseActivity implements BaseFragment.FragmentNavigation, FragNavController.TransactionListener, FragNavController.RootFragmentListener, PlayerFragment.OnFragmentInteractionListener  {



    @BindView(R.id.content_frame)
    FrameLayout contentFrame;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private int[] mTabIconsSelected = {
            R.drawable.tab_home,
            R.drawable.tab_search,
            R.drawable.tab_player,
            R.drawable.tab_social,
            R.drawable.tab_profile};

    private MediaPlayer mp;

    @BindArray(R.array.tab_name)
    String[] TABS;

    @BindView(R.id.bottom_tab_layout)
    TabLayout bottomTabLayout;

    static final int USER_AUTH = 1;
    private FragNavController mNavController;


    private FragmentHistory fragmentHistory;

    private int returnpermission=150;

    private static CookieManager ckmng;
    //private static CookieSyncManager cksmng;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //TODO: Escribir if version android >api 15
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 150);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, returnpermission+1);

        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        boolean userAuthed = sp.getBoolean("userAuthed",false),hasToLog=true;

        Intent LoginActivity = new Intent(this, LoginActivity.class);

        ckmng = new CookieManager();
       // cksmng = new CookieSyncManager();
        if(userAuthed){
            try {

                List<HttpCookie> cookieList = ckmng.getCookieStore().get(new URI("http://mewat1718.ddns.net"));

                for (HttpCookie i : cookieList){
                    if(i.getName().equals("idSesion") && !i.hasExpired()){
                        hasToLog=false;
                    }
                }

            }catch( URISyntaxException e){

            }
            String user = sp.getString("username","");
            String idSesion = sp.getString("idSesion","");
        }

        if(hasToLog)this.startActivityForResult(LoginActivity,USER_AUTH);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        initToolbar();

        initTab();

        fragmentHistory = new FragmentHistory();


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
    }


    @Override
    protected void onPause() {
        super.onPause();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }
    }

    @Override
    public void pushFragment(Fragment fragment) {
        if (mNavController != null) {
            mNavController.pushFragment(fragment);
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
        getSupportActionBar().setDisplayShowHomeEnabled(!mNavController.isRootFragment());
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
                return new PlayerFragment();
            case FragNavController.TAB4:
                return new NewsFragment();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == USER_AUTH){
            SharedPreferences p = getPreferences(Context.MODE_PRIVATE);
            if(resultCode == Activity.RESULT_OK){

                p.edit().putBoolean("userAuthed",true).apply();
                String s = data.getStringExtra("idSesion");
                String us = data.getStringExtra("user");
                try {

                    ckmng.getCookieStore().add(new URI("http://mewat1718.ddns.net"), new HttpCookie("idSesion", s));
                    ckmng.getCookieStore().add(new URI("http://mewat1718.ddns.net"), new HttpCookie("username", us));
                    flush();


                }catch (URISyntaxException e){

                }

                Toast.makeText(this,"idSesion:"+s,Toast.LENGTH_SHORT).show();
                Toast.makeText(this,"user:"+us,Toast.LENGTH_SHORT).show();
            }
            if(resultCode != Activity.RESULT_OK ){
                Intent LoginActivity = new Intent(this,com.csd.MeWaT.activities.LoginActivity.class);
                this.startActivityForResult(LoginActivity,USER_AUTH);
                p.edit().putBoolean("userAuthed",false).apply();
            }
        }
    }
    public void flush() {
        if (ckmng != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //ckmng.flush();
        //} else if (cksmng != null) {
          // cksmng.sync();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        // empty
    }
}
