package me.s4h.myazbfq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wang on 10/14/2015.
 */
public class AudioListAdapter extends ArrayAdapter<AudioItem> {
    LayoutInflater inflater;

    public AudioListAdapter(Context context) {
        super(context, 0);
        this.inflater = LayoutInflater.from(context);
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.audio_list_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        AudioItem item = this.getItem(position);
        holder.title.setText(item.title);
        holder.artistAlbum.setText(item.artist + " - " + item.album);
        holder.duration.setText(String.format("%02d:%02d",item.duration/60_000,item.duration/1000%60));

        return view;
    }

     static class ViewHolder {
        @Bind(R.id.audio_item_title)
        TextView title;
        @Bind(R.id.audio_item_artist_album)
        TextView artistAlbum;
        @Bind(R.id.audio_item_duration)
        TextView duration;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
