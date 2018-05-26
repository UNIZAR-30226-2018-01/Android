package com.csd.MeWaT.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.csd.MeWaT.R;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomAdapterAlbum extends SimpleAdapter {
    LayoutInflater inflater;
    Context context;
    ArrayList<Album> arrayList;

    public CustomAdapterAlbum(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
        inflater.from(context);
    }

    public void setArrayList(ArrayList<Album> arrayList) {
        this.arrayList = arrayList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ImageView thumbnail = (ImageView) view.findViewById(R.id.ImgAlbum);
        new DownloadSongImageTask(thumbnail).execute(arrayList.get(position).getUrlImg());


        return view;
    }


}
