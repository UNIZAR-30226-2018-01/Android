package com.csd.MeWaT.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.csd.MeWaT.utils.Lista;
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
import java.util.List;

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
    private static boolean fromalbum = false, fromlist = false;

    private Album album = null;
    private Lista lista = null;
    private String options;
    private ArrayList<Song> songsList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> songsListData = new ArrayList<>();


    public static SongListFragment newInstanceListSongs(ArrayList<Song> instance) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_INSTANCE, instance);
        fromlist = false;
        fromalbum = false;
        SongListFragment fragment = new SongListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static SongListFragment newInstanceAlbum(ArrayList<Album> instance) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_INSTANCE, instance);
        fromalbum = true;
        fromlist = false;
        SongListFragment fragment = new SongListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static SongListFragment newInstanceList(ArrayList<Lista> instance) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_INSTANCE, instance);
        fromlist = true;
        fromalbum = false;
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
            if (fromalbum) album = ((ArrayList<Album>) args.getSerializable(ARGS_INSTANCE)).get(0);
            else if (fromlist) lista = ((ArrayList<Lista>) args.getSerializable(ARGS_INSTANCE)).get(0);
            else songsList = (ArrayList<Song>) args.getSerializable(ARGS_INSTANCE);
        }


        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ArrayAdapter<String> arrayAdapter= new ArrayAdapter<>(getContext(),R.layout.dialog_layout,new String []{"Compartir","Añadir a Lista"});


        // Adding menuItems to ListView
        adapter = new CustomAdapterSong(view.getContext(), songsListData,
                R.layout.list_row_song, new String[]{"songTitle", "songArtist"}, new int[]{
                R.id.songTitle, R.id.songArtist});


        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity.songsList = songsList;
                MainActivity.songnumber = (int) l;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Integer l2 = (int)l;
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
                builderSingle.setTitle("Opciones");

                builderSingle.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builderInner = new AlertDialog.Builder(getContext());

                        options = arrayAdapter.getItem(which);

                        builderInner.setTitle(options);
                        builderInner.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        List<String> listas = new ArrayList<>();
                        for(Lista ls : MainActivity.lists) listas.add(ls.getName());
                        final ArrayAdapter<String> arrayAdapter2= new ArrayAdapter<>(getContext(),R.layout.dialog_layout,listas.toArray(new String[0]));

                        builderInner.setAdapter(arrayAdapter2, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Add2List(l2).execute(arrayAdapter2.getItem(which));
                            }
                        });
                        builderInner.show();
                    }
                });
                builderSingle.show();

                return false;
            }
        });


        // listening to single listitem click

        if (fromalbum) {
            SearchTaskByAlbum searchTaskByAlbum = new SearchTaskByAlbum(album.getName());
            new DownloadSongImageTask(imageView).execute(album.getUrlImg());
            textView.setText(album.getName());
            searchTaskByAlbum.execute();
            lnly.setVisibility(View.VISIBLE);
        } else if (fromlist) {

            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(lista.getName());
            new SearchTaskBySong(lista.getName()).execute("VerLista");
        } else {

            for (int i = 0; i < songsList.size(); i++) {
                // creating new HashMap
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("songTitle", songsList.get(i).getTitle());
                song.put("songArtist", songsList.get(i).getArtist());

                // adding HashList to ArrayList
                songsListData.add(song);
            }
            adapter.setArrayList(songsList);

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
                System.out.println("\nSending 'Get' request to URL : " + url + "--" + responseCode);
            } catch (MalformedURLException e) {
                return false;
            } catch (SocketTimeoutException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
            try {
                inputStream = new InputStreamReader(client.getInputStream());


                BufferedReader reader = new BufferedReader(inputStream);
                StringBuilder builder = new StringBuilder();

                for (String line = null; (line = reader.readLine()) != null; ) {
                    builder.append(line).append("\n");
                }

                // Parse into JSONObject
                String resultStr = builder.toString();
                JSONTokener tokener = new JSONTokener(resultStr);
                JSONObject result = new JSONObject(tokener);

                client.disconnect();
                if (!result.has("error")) {

                    JSONArray resultArray = result.getJSONArray("canciones");
                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject jsObj = resultArray.getJSONObject(i);
                        songsList.add(new Song(jsObj.getString("tituloCancion"),
                                        jsObj.getString("nombreAlbum"),
                                        jsObj.getString("nombreArtista"),
                                        jsObj.getString("genero"),
                                        jsObj.getString("ruta").replace("/usr/local/apache-tomcat-9.0.7/webapps", "https://mewat1718.ddns.net"),
                                        jsObj.getString("ruta_imagen").replace("..", "https://mewat1718.ddns.net")
                                )
                        );
                    }
                    adapter.setArrayList(songsList);

                } else {
                    return false;
                }


            } catch (IOException e) {
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
                for (int i = 0; i < 4 && i < songsList.size(); i++) {
                    HashMap<String, String> temp = new HashMap<String, String>();
                    temp.put("songTitle", songsList.get(i).getTitle());
                    temp.put("songArtist", songsList.get(i).getArtist());
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

    /**
     * Represents an asynchronous song search
     */
    public class SearchTaskBySong extends AsyncTask<String, Void, Boolean> {


        String List = null;

        SearchTaskBySong(String list) {
            List = list;
        }

        boolean noresult = false;
        @Override
        protected Boolean doInBackground(String... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;



            songsList = new ArrayList<>();
            try {
                url = new URL("https://mewat1718.ddns.net/ps/" + params[0]);

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);

                if (List != null) {
                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("nombreLista", List)
                            .appendQueryParameter("nombreCreadorLista", MainActivity.user);             //Añade parametros
                    String query = builder.build().getEncodedQuery();

                    OutputStream os = client.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                }
                int responseCode = client.getResponseCode();
                System.out.println("\nSending 'Get' request to URL : " + url + "--" + responseCode);
            } catch (MalformedURLException e) {
                return false;
            } catch (SocketTimeoutException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
            try {
                inputStream = new InputStreamReader(client.getInputStream());


                BufferedReader reader = new BufferedReader(inputStream);
                StringBuilder builder = new StringBuilder();

                for (String line = null; (line = reader.readLine()) != null; ) {
                    builder.append(line).append("\n");
                }

                // Parse into JSONObject
                String resultStr = builder.toString();
                JSONTokener tokener = new JSONTokener(resultStr);
                JSONObject result = new JSONObject(tokener);

                client.disconnect();
                if (!result.has("error") && !result.has("NoHayCanciones")) {

                    JSONArray resultArray = result.getJSONArray("canciones");
                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject jsObj = resultArray.getJSONObject(i);
                        songsList.add(new Song(jsObj.getString("tituloCancion"),
                                        jsObj.getString("nombreAlbum"),
                                        jsObj.getString("nombreArtista"),
                                        jsObj.getString("genero"),
                                        jsObj.getString("ruta").replace("/usr/local/apache-tomcat-9.0.7/webapps", "https://mewat1718.ddns.net"),
                                        jsObj.getString("ruta_imagen").replace("..", "https://mewat1718.ddns.net")
                                )
                        );
                    }
                    adapter.setArrayList(songsList);
                } else {
                    if(!result.has("error")) noresult=true;
                    else return false;
                }


            } catch (IOException e) {
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
                if(!noresult){
                    songsListData.clear();
                    for (int i = 0; i < 4 && i < songsList.size(); i++) {
                        HashMap<String, String> temp = new HashMap<String, String>();
                        temp.put("songTitle", songsList.get(i).getTitle());
                        temp.put("songArtist", songsList.get(i).getArtist());
                        songsListData.add(temp);
                    }

                    adapter.notifyDataSetChanged();
                }else Toast.makeText(getActivity().getApplicationContext(), "Lista Vacia",
                        Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong",
                        Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    /**
     * Represents an asynchronous album search task
     */
    public class Add2List extends AsyncTask<String, Void, Boolean> {

        private final Integer l;

        Add2List(Integer index) {
            l = index;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;

            try {
                url = new URL("https://mewat1718.ddns.net/ps/AnyadirCancionALista");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("ruta", songsList.get(l).getUrl().replace("https://mewat1718.ddns.net","/usr/local/apache-tomcat-9.0.7/webapps"))
                        .appendQueryParameter("nombreLista",params[0]);             //Añade parametros
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
                Toast.makeText(getActivity().getApplicationContext(), "Añadida Correctamente",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Algo ha ido mal",
                        Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onCancelled() {

        }
    }

}