package me.s4h.myazbfq;


import android.content.Intent;

public class AudioItem {
    public Integer id;
    public String title;
    public String album;
    public String artist;
    public int duration;
    public int year;
    public AudioItem(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    public AudioItem(){}

    public AudioItem(Integer id, String title, String album, String artist) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public AudioItem(Integer id,String title,String artist,int duration,int year,String album){
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.year = year;
        this.album = album;

    }
    @Override
    public String toString() {
        return this.title;
    }
}
