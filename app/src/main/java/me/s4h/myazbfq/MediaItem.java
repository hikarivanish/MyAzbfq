package me.s4h.myazbfq;


public class MediaItem {
    public Long id;
    public String title;

    public MediaItem(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public  MediaItem(){}

    @Override
    public String toString() {
        return this.title;
    }
}
