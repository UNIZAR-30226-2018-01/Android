package com.csd.MeWaT.fragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.utils.CustomAdapterSong;
import com.csd.MeWaT.utils.Song;
import com.csd.MeWaT.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HomeFragment extends BaseFragment {


    @BindView(R.id.LayoutRecientes)
    LinearLayout Recents;

    @BindView(R.id.RecListView)
    GridView RecListView;

    @BindView(R.id.moreRecents)
    TextView moreRecents;

    //---------------------------------------
    @BindView(R.id.LayoutGeneros)
    LinearLayout Genre;

    @BindView(R.id.GenreListView)
    GridView GenreListView;

    @BindView(R.id.moreGenre)
    TextView moreGenre;

    //------------------------------------------
    @BindView(R.id.LayoutTop10)
    LinearLayout Top10;

    @BindView(R.id.Top10ListView)
    GridView Top10ListView;

    @BindView(R.id.moreTop10)
    TextView moreTop10;
    //------------------------------------------



    private ArrayList<Song> resultRecentsList;
    private ArrayList<HashMap<String,String>> ResultListAdapter =new ArrayList<HashMap<String,String>>();
    CustomAdapterSong RecentsAdapter;

    private ArrayList<String> resultGenreList;
    private ArrayList<HashMap<String,String>> GenreListAdapter =new ArrayList<HashMap<String,String>>();
    SimpleAdapter GenreAdapter;

    private ArrayList<Song> resultTop10List;
    private ArrayList<HashMap<String,String>> resultTop10Adapter =new ArrayList<HashMap<String,String>>();
    CustomAdapterSong Top10Adapter;





    public static HomeFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(ARGS_INSTANCE, instance);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public HomeFragment() {  }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
