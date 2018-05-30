package com.csd.MeWaT.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DownloadSongTask extends AsyncTask<Void, Void, Boolean> {


    File cDir,tempFile;
    public DownloadSongTask(File file) {
        cDir = file;
    }

    protected Boolean doInBackground(Void... urls) {

        for(int i = MainActivity.songnumber;i<MainActivity.songnumber+1;i++) {

            Song s = MainActivity.songsList.get(i);



            /** Getting a reference to temporary file, if created earlier */
            tempFile = new File(cDir.getPath() + "/" + "Cancion0" + i + ".mp3");

            int responseCode;
            byte[] buffer = new byte[1024 * 1024 * 1];
            InputStream in = null;
            OutputStream outStream = null;
            try {
                URL url = new URL(s.getUrl().replace(" ", "%20"));
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.connect();
                responseCode = con.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    //download

                    in = con.getInputStream();
                    outStream = new FileOutputStream(tempFile);

                    int read = 0;

                    while ((read = in.read(buffer)) != -1) {
                        outStream.write(buffer, 0, read);
                    }

                    System.out.println("Done!");

                    in.close();
                    outStream.close();
                }

            } catch (Exception ex) {

                Log.e("Exception", ex.toString());
                return false;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outStream != null) {
                    try {
                        // outputStream.flush();
                        outStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                MainActivity.songsList.get(i).setUrlLocal(cDir.getPath() + "/" + "Cancion0" + i + ".mp3");
            }
        }
        return true;
    }
    @Override
    protected void onPostExecute(Boolean data) {
        MainActivity.playSong(MainActivity.songnumber);
    }

}

