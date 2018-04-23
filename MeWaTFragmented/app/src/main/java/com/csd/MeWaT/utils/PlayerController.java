package com.csd.MeWaT.utils;

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PlayerController {

    private MediaPlayer mp;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    /**
     * Function to play a song
     * @param songIndex - index of song
     * */
    public String[]  playSong(int songIndex){
        // Play song
        String songTitle = new String(),songAlbum = new String();
        try {
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).get("songPath"));
            mp.prepare();
            mp.start();
            // Displaying Song title
            songTitle= songsList.get(songIndex).get("songTitle");
            songAlbum= songsList.get(songIndex).get("songAlbum");

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] retString = {songTitle,songAlbum};
        return retString;
    }



}
