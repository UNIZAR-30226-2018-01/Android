package com.csd.MeWaT.utils;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.csd.MeWaT.R;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomAdapterSong extends SimpleAdapter {
    LayoutInflater inflater;
    Context context;
    ArrayList<Song> arrayList;


    public CustomAdapterSong(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
        inflater.from(context);
    }

    public void setArrayList(ArrayList<Song> arrayList) {
        this.arrayList = arrayList;
    }


    boolean test;
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ImageView thumbnail = (ImageView) view.findViewById(R.id.listrow_photo);
        new DownloadSongImageTask(thumbnail).execute(arrayList.get(position).getUrlImg());

        ImageButton like = (ImageButton) view.findViewById(R.id.likeButton);

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatImageButton like2 = (AppCompatImageButton) view.findViewById(R.id.likeButton);

                if (arrayList.get(position).getLike()){
                    like2.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    arrayList.get(position).setLike(false);
                    test= arrayList.get(position) == new Song(arrayList.get(position).getTitle(), arrayList.get(position).getAlbum(), arrayList.get(position).getTitle(), arrayList.get(position).getArtist(), arrayList.get(position).getGenre(), arrayList.get(position).getUrl(), arrayList.get(position).getUrlImg());
                }
                else{
                    like2.setImageResource(R.drawable.ic_favorite_blue_filled_24dp);
                    arrayList.get(position).setLike(true);
                }
            }
        });
        return view;
    }

}
