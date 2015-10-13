package me.s4h.myazbfq;


public class VideoItem {
    public Long id;
    public String title;

    public VideoItem(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public VideoItem(){}

    @Override
    public String toString() {
        return this.title;
    }
}
