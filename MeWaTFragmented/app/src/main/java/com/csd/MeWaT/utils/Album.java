package com.csd.MeWaT.utils;

import java.io.Serializable;

/**
 * Created by Carlos on 19/03/2018.
 */
public class Album implements Serializable{
    private String name;
    private String artist;
    private String urlImg;


    /**
     * Function to play a song
     * @param pars - title,album,artista,genero,url
     * */
    public Album(String ...pars) {
        for(int i=0;i<pars.length;i++){
            if (i==0) this.name=pars[0];
            if (i==1) this.artist=pars[1];
            if (i==2) this.urlImg=pars[2];
        }
    }

    public String getUrlImg(){
        return urlImg;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

}