package com.csd.MeWaT.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.csd.MeWaT.R;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomAdapterUsers extends SimpleAdapter {
    LayoutInflater inflater;
    Context context;
    ArrayList<String> arrayList = new ArrayList<>();

    public CustomAdapterUsers(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
        inflater.from(context);
    }

    public void setArrayList(ArrayList<String> arrayList) {
        this.arrayList = arrayList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ImageView picProfile = (ImageView) view.findViewById(R.id.ImgUser);
        new DownloadUserImageTask(picProfile).execute("https://mewat1718.ddns.net/ps/images/"+arrayList.get(position)+".jpg");

        return view;
    }
    @Override
    public int getCount()
    {
        return arrayList.size();
    }

}
