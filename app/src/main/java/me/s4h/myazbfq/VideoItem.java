package me.s4h.myazbfq;


public class VideoItem {
    public Long id;
    public String title;
    public long height,width;
    public long duration;
    public VideoItem(Long id, String title,long height,long width,long duration) {
        this.id = id;
        this.title = title;
        this.height = height;
        this.width = width;
        this.duration = duration;
    }

    public VideoItem(){}

    @Override
    public String toString() {
        return this.title;
    }
}
