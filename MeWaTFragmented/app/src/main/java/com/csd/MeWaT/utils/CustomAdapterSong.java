package com.csd.MeWaT.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class CustomAdapterSong extends SimpleAdapter {
    LayoutInflater inflater;
    Context context;
    ArrayList<Song> arrayList = new ArrayList<>();


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
        if(arrayList.get(position).getLike()) like.setImageResource(R.drawable.ic_favorite_blue_filled_24dp);
        else like.setImageResource(R.drawable.ic_favorite_border_black_24dp);

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatImageButton like2 = (AppCompatImageButton) view.findViewById(R.id.likeButton);

                if (arrayList.get(position).getLike()){
                    like2.setImageResource(R.drawable.ic_favorite_border_black_24dp);

                    new Add2List(position).execute("QuitarCancionDeLista");
                    test = true;
                }
                else{
                    like2.setImageResource(R.drawable.ic_favorite_blue_filled_24dp);

                    test = false;
                    new Add2List(position).execute("AnyadirCancionALista");

                }
            }
        });
        return view;
    }

    @Override
    public int getCount()
    {
        return arrayList.size()-1;
    }

    /**
     * Represents an asynchronous album search task
     */
    public class Add2List extends AsyncTask<String, Void, Boolean> {

        private final Integer position;

        Add2List(Integer index) {
            position = index;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;

            try {
                url = new URL("https://mewat1718.ddns.net/ps/"+params[0]);

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("ruta", arrayList.get(position).getUrl().replace("https://mewat1718.ddns.net","/usr/local/apache-tomcat-9.0.7/webapps"))
                        .appendQueryParameter("nombreLista","Favoritos");             //Añade parametros
                String query = builder.build().getEncodedQuery();

                OutputStream os = client.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                int responseCode = client.getResponseCode();
                System.out.println("\nSending 'Get' request to URL : " +    url+"--"+responseCode);
            } catch (MalformedURLException e) {
                return false;
            } catch (SocketTimeoutException e) {
                return false;
            }catch (IOException e) {
                return false;
            }
            try {
                inputStream = new InputStreamReader(client.getInputStream());


                BufferedReader reader = new BufferedReader(inputStream);
                StringBuilder builder = new StringBuilder();

                for (String line = null; (line = reader.readLine()) != null ; ) {
                    builder.append(line).append("\n");
                }

                // Parse into JSONObject
                String resultStr = builder.toString();
                JSONTokener tokener = new JSONTokener(resultStr);
                JSONObject result = new JSONObject(tokener);

                client.disconnect();
                if (!result.has("error")){

                }else{
                    return false;
                }


            }catch (IOException e){
                Throwable s = e.getCause();
                return false;
            } catch (JSONException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                if(test) {
                    arrayList.get(position).setLike(false);
                    int i =0;
                    for(Song s : MainActivity.favList) {
                        if (s.equals(arrayList.get(position)))MainActivity.favList.remove(i);
                        i++;
                    }
                    Toast.makeText(context.getApplicationContext(), "Eliminada Correctamente de Favoritos",
                            Toast.LENGTH_SHORT).show();
                } else{
                    arrayList.get(position).setLike(true);
                    MainActivity.favList.add(arrayList.get(position));
                    Toast.makeText(context.getApplicationContext(), "Añadida Correctamente a Favoritos",
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                if(test) {
                    MainActivity.favList.remove(arrayList.get(position));
                    Toast.makeText(context.getApplicationContext(), "Error",
                            Toast.LENGTH_SHORT).show();
                } else{
                    MainActivity.favList.add(arrayList.get(position));
                    Toast.makeText(context.getApplicationContext(), "Error",
                            Toast.LENGTH_SHORT).show();
                }

            }
        }

        @Override
        protected void onCancelled() {

        }
    }

}
