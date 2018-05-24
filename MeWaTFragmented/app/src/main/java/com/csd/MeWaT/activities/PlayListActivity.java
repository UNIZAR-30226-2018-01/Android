package com.csd.MeWaT.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.csd.MeWaT.R;
import com.csd.MeWaT.utils.Song;
import com.csd.MeWaT.utils.SongsManager;
import com.csd.MeWaT.activities.PlayListActivity;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayListActivity extends AppCompatActivity {
    // Songs list
    ImageButton b2p;
    ListView lv;

    public ArrayList<Song> songsList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> songsListData = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        songsList = MainActivity.songsList;
        b2p =  (ImageButton) findViewById(R.id.back2Player);

        lv =  (ListView) findViewById(R.id.list);

        // looping through playlist
        for (int i = 0; i < songsList.size(); i++) {
            // creating new HashMap
            HashMap<String, String> song = new HashMap<String, String>();
            song.put("songTitle", songsList.get(i).getTitle());
            song.put("songPath", songsList.get(i).getUrl());

            // adding HashList to ArrayList
            songsListData.add(song);
        }

        // Adding menuItems to ListView
        ListAdapter adapter = new SimpleAdapter(this, songsListData,
                R.layout.list_row, new String[] { "songTitle" }, new int[] {
                R.id.songTitle });

        lv.setAdapter(adapter);
        // listening to single listitem click

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting listitem index
                int songIndex = position;

                // Starting new intent
                Intent in = new Intent();
                // Sending songIndex to PlayerActivity
                in.putExtra("songIndex", songIndex);
                setResult(Activity.RESULT_OK,in);
                // Closing PlayListView
                finish();
            }
        });

        b2p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

    }
}
