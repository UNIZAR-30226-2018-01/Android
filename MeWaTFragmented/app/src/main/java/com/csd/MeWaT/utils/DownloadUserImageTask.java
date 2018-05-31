package com.csd.MeWaT.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.csd.MeWaT.R;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DownloadUserImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;


    public DownloadUserImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    HttpsURLConnection client = null;

    protected Bitmap doInBackground(String... urls) {
        URL urldisplay;
        Bitmap mIcon11 = null;
        int responseCode;
        InputStream in;
        try {
            URL url = new URL(urls[0].replace(" ", "%20"));
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.connect();
            responseCode = con.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                //download
                in = con.getInputStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                in.close();
            }

        } catch (Exception ex) {
            Log.e("Exception", ex.toString());
        }
        return mIcon11;
    }
    @Override
    protected void onPostExecute(Bitmap data) {
        if(data == null) bmImage.setImageResource(R.drawable.ic_launcher);
        else bmImage.setImageBitmap(data);
    }

}

