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
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    // Constructor
    public SongsManager(){

    }

    /**
     * Function to read all mp3 files from sdcard
     * and store the details in ArrayList
     * */
    public ArrayList<HashMap<String, String>> getPlayList(){
        File home = Environment.getExternalStorageDirectory();

        File [] listedFiles = reclistFile(home);
        if ((listedFiles != null) && listedFiles.length > 0) {
            for (File file : listedFiles) {
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("songTitle", file.getName().substring(0, (file.getName().length() - 4)));
                song.put("songPath", file.getPath());

                // Adding each song to PlayListActivity
                songsList.add(song);
            }
        }
        // return songs list array
        return songsList;
    }

    File [] reclistFile( File path ){
        ArrayList <File> saved = new ArrayList<>();
        for (File file : path.listFiles()){
            if (file.isDirectory()){
                saved.addAll(reclistFile2(file));
            }
            if (file.getName().endsWith(".mp3")) saved.add(file);
        }
        return saved.toArray(new File[saved.size()]);
    }

    ArrayList <File> reclistFile2( File path ){
        ArrayList <File> saved = new ArrayList<>();
        for (File file : path.listFiles()){
            if (file.isDirectory()){
                saved.addAll(reclistFile2(file));
            }
            else if (file.getName().endsWith(".mp3")) saved.add(file);
        }
        return saved;
    }


}
