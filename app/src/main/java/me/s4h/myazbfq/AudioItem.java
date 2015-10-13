package me.s4h.myazbfq;


public class AudioItem {
    public Long id;
    public String title;
    public String album;
    public String artist;
    public AudioItem(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public AudioItem(){}

    public AudioItem(Long id, String title, String album, String artist) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    @Override
    public String toString() {
        return this.title;
    }
}
