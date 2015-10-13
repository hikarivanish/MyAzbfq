package me.s4h.myazbfq;


public class AudioItem {
    public Long id;
    public String title;

    public AudioItem(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public AudioItem(){}

    @Override
    public String toString() {
        return this.title;
    }
}
