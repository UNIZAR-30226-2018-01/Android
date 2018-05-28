package com.csd.MeWaT.utils;

import android.support.annotation.Nullable;

import com.csd.MeWaT.activities.MainActivity;

import java.io.Serializable;

/**
 * Created by Carlos on 19/03/2018.
 */
public class Song {
    private String url;
    private String urlImg;
    private String title;
    private String album;
    private String artista;
    private String genero;
    private Boolean like=false;


    /**
     * Function to play a song
     * @param pars - title,album,artista,genero,url
     * */
    public Song(String ...pars) {
        for(int i=0;i<pars.length;i++){
            if (i==0) this.title=pars[0];
            if (i==1) this.album=pars[1];
            if (i==2) this.artista=pars[2];
            if (i==3) this.genero=pars[3];
            if (i==4) this.url=pars[4];
            if (i==5) this.urlImg= pars[5];
        }
        for(Song s : MainActivity.favList){
            if (s.equals(this)) this.like=true;
        }
    }

    public String getUrl(){
        return url;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artista;
    }

    public String getGenre() {
        return genero;
    }

    public String getTitle() {
        return title;
    }

    public String getUrlImg() {
        return urlImg;
    }

    public Boolean getLike() {
        return like;
    }

    public void setLike(Boolean like) {
        this.like = like;
    }

    public Boolean equals( Song compare){

        return genero.equals(compare.getGenre()) && title.equals(compare.getTitle())
                && album.equals(compare.getAlbum()) && artista.equals(compare.getArtist())
                && url.equals(compare.getUrl()) && urlImg.equals(compare.getUrlImg());
    }
}