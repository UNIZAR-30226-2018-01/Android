package com.example.carlos.mewat.helper;

import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Carlos on 19/03/2018.
 */

public class SongsManager {
    // SDCard Path
    final String MEDIA_PATH = Environment.getExternalStorageDirectory().getPath();
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    // Constructor
    public SongsManager(){

    }

    /**
     * Function to read all mp3 files from sdcard
     * and store the details in ArrayList
     * */
    public ArrayList<HashMap<String, String>> getPlayList(){
        File home = new File(MEDIA_PATH);
        File [] listedFiles = home.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return (s.endsWith(".mp3") || s.endsWith(".MP3"));
            }
        });

        if ((listedFiles != null) && listedFiles.length > 0) {
            for (File file : listedFiles) {
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("songTitle", file.getName().substring(0, (file.getName().length() - 4)));
                song.put("songPath", file.getPath());

                // Adding each song to SongList
                songsList.add(song);
            }
        }
        // return songs list array
        return songsList;
    }

    /**
     * Class to filter files which are having .mp3 extension
     * */
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3") || name.endsWith(".MP3"));
        }
    }
}
