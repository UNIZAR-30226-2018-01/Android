package com.csd.MeWaT.fragments;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.utils.Album;
import com.csd.MeWaT.utils.CustomAdapterSong;
import com.csd.MeWaT.utils.DownloadSongImageTask;
import com.csd.MeWaT.utils.Song;

import org.json.JSONArray;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongListFragment extends BaseFragment {

    @BindView(R.id.fragment_song_list_listView)
    ListView listView;
    @BindView(R.id.fragment_song_list_AlbumImg)
    ImageView imageView;
    @BindView(R.id.fragment_song_list_AlbumTitle)
    TextView textView;
    @BindView(R.id.fragment_song_list_linear)
    LinearLayout lnly;


    CustomAdapterSong adapter;
    private static boolean fromalbum = false;

    private Album album = null;
    private ArrayList<Song> songsList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> songsListData = new ArrayList<>();


    public static SongListFragment newInstance(ArrayList<Song> instance) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_INSTANCE, instance);
        SongListFragment fragment = new SongListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static SongListFragment newInstance2(ArrayList<Album> instance) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_INSTANCE, instance);
        fromalbum = true;
        SongListFragment fragment = new SongListFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_song_list, container, false);


        ButterKnife.bind(this, view);

        Bundle args = getArguments();
        if (args != null) {
            if(fromalbum) album=((ArrayList<Album>) args.getSerializable(ARGS_INSTANCE)).get(0);
            songsList = (ArrayList<Song>) args.getSerializable(ARGS_INSTANCE);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity.songsList=songsList;
                MainActivity.songnumber=(int) l;
            }
        });

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Adding menuItems to ListView
        adapter = new CustomAdapterSong(view.getContext(), songsListData,
                R.layout.list_row_song, new String[] { "songTitle","songArtist" }, new int[] {
                R.id.songTitle, R.id.songArtist });

        listView.setAdapter(adapter);
        // listening to single listitem click

        if(album!=null){
            SearchTaskByAlbum searchTaskByAlbum = new SearchTaskByAlbum(album.getName());
            new DownloadSongImageTask(imageView).execute(album.getUrlImg());
            textView.setText(album.getName());
            searchTaskByAlbum.execute();
            lnly.setVisibility(View.VISIBLE);
        }else {


            for (int i = 0; i < songsList.size(); i++) {
                // creating new HashMap
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("songTitle", songsList.get(i).getTitle());
                song.put("songArtist", songsList.get(i).getArtist());

                // adding HashList to ArrayList
                songsListData.add(song);
            }
        }
    }

    /**
     * Represents an asynchronous song search
     */
    public class SearchTaskByAlbum extends AsyncTask<Void, Void, Boolean> {

        private final String query;

        SearchTaskByAlbum(String txt) {
            query = txt;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;


            songsList = new ArrayList<>();
            try {
                url = new URL("https://mewat1718.ddns.net/ps/BuscarCancionAlbum");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("album", query);             //Añade parametros
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

                    JSONArray resultArray = result.getJSONArray("canciones");
                    for(int i = 0; i<resultArray.length();i++){
                        JSONObject jsObj = resultArray.getJSONObject(i);
                        songsList.add(new Song(jsObj.getString("tituloCancion"),
                                        jsObj.getString("nombreAlbum"),
                                        jsObj.getString("nombreArtista"),
                                        jsObj.getString("genero"),
                                        jsObj.getString("ruta").replace("/usr/local/apache-tomcat-9.0.7/webapps","https://mewat1718.ddns.net"),
                                        jsObj.getString("ruta_imagen").replace("..","https://mewat1718.ddns.net")
                                )
                        );
                    }
                    adapter.setArrayList(songsList);

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
                songsListData.clear();
                for(int i=0; i<4 && i<songsList.size();i++){
                    HashMap<String,String> temp = new HashMap<String,String>();
                    temp.put("songTitle",songsList.get(i).getTitle());
                    temp.put("songArtist",songsList.get(i).getArtist());
                    songsListData.add(temp);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong",
                        Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onCancelled() {

        }
    }
}
