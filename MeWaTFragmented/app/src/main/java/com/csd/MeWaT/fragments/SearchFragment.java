package com.csd.MeWaT.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.activities.PlayerActivity;
import com.csd.MeWaT.utils.Album;
import com.csd.MeWaT.utils.CustomAdapterAlbum;
import com.csd.MeWaT.utils.CustomAdapterSong;
import com.csd.MeWaT.utils.CustomAdapterUsers;
import com.csd.MeWaT.utils.Lista;
import com.csd.MeWaT.utils.Song;
import com.csd.MeWaT.utils.Utils;

import org.apache.http.conn.ssl.SSLSocketFactory;
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


public class SearchFragment extends BaseFragment{

    @BindView(R.id.search_text)
    EditText search_text;

    @BindView(R.id.search_button)
    ImageButton search_button;

    @BindView(R.id.songsearch_listview)
    ListView song_listView;

    @BindView(R.id.albumsearch_listview)
    GridView album_listView;

    @BindView(R.id.listsearch_listview)
    ListView list_listView;

    @BindView(R.id.usersearch_listview)
    GridView user_listView;


    @BindView(R.id.ScrollSearchView)
    ScrollView ScrollSearchView;

    @BindView(R.id.ScrollSearchViewSongs)
    LinearLayout ScrollSearchViewSongs;
    @BindView(R.id.ScrollSearchViewAlbums)
    LinearLayout ScrollSearchViewAlbums;
    @BindView(R.id.ScrollSearchViewListas)
    LinearLayout ScrollSearchViewListas;
    @BindView(R.id.ScrollSearchViewUsers)
    LinearLayout ScrollSearchViewUsers;

    @BindView(R.id.moreSongs)
    TextView moreSongs;

    private static ArrayList<Song> songResultList = new ArrayList<>();
    private static ArrayList<HashMap<String,String>> listAdapterSongs =new ArrayList<HashMap<String,String>>();
    CustomAdapterSong adapterSong;

    private static ArrayList<Album> albumResultList= new ArrayList<>();
    private static ArrayList<HashMap<String,String>> listAdapterAlbums =new ArrayList<HashMap<String,String>>();
    CustomAdapterAlbum adapterAlbum;

    private static ArrayList<Lista> listaResultList= new ArrayList<>();
    private static ArrayList<HashMap<String,String>> listAdapterLista =new ArrayList<HashMap<String,String>>();
    SimpleAdapter adapterLista;

    private static ArrayList<String> userResultList= new ArrayList<>();
    private static ArrayList<HashMap<String,String>> listAdapterUser =new ArrayList<HashMap<String,String>>();
    CustomAdapterUsers adapterUser;


    private Integer numFailed;

    String options;


    public SearchFragment(){
        //Empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);

        search_button.setImageResource(R.drawable.ic_search_black_24dp);

        adapterSong = new CustomAdapterSong(view.getContext(), listAdapterSongs,R.layout.list_row_song,
                new String[]{"title","artist"},
                new int[]{R.id.songTitle,R.id.songArtist});
        song_listView.setAdapter(adapterSong);

        adapterAlbum = new CustomAdapterAlbum(view.getContext(), listAdapterAlbums,R.layout.list_row_album,
                new String[]{"nombre","artist"},
                new int[]{R.id.AlbumTitlerow,R.id.AlbumArtistrow});
        album_listView.setAdapter(adapterAlbum);

        adapterLista= new SimpleAdapter(view.getContext(), listAdapterLista,R.layout.list_row_lista,
                new String[]{"nombre","userOwn"},
                new int[]{R.id.NameListRow,R.id.OwnerListRow});
        list_listView.setAdapter(adapterLista);

        adapterUser= new CustomAdapterUsers(view.getContext(), listAdapterUser,R.layout.list_row_user,
                new String[]{"user"},
                new int[]{R.id.UserName});
        user_listView.setAdapter(adapterUser);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ArrayAdapter<String> arrayAdapter= new ArrayAdapter<>(getContext(),R.layout.dialog_layout,new String []{"Compartir","Añadir a Lista"});


        if(!adapterUser.isEmpty()){
            ScrollSearchViewUsers.setVisibility(View.VISIBLE);
        }
        if(!adapterAlbum.isEmpty()){
            adapterAlbum.setArrayList(albumResultList);
            ScrollSearchViewAlbums.setVisibility(View.VISIBLE);
        }
        if(!adapterLista.isEmpty()){
            ScrollSearchViewListas.setVisibility(View.VISIBLE);
        }
        if(!adapterSong.isEmpty()){
            adapterSong.setArrayList(songResultList);
            ScrollSearchViewSongs.setVisibility(View.VISIBLE);
        }

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the query from the textView
                String query = search_text.getText().toString();
                // if the query isnt empty, then search fro anything in the DB
                if (!query.trim().isEmpty()){
                    numFailed =0;
                    SearchTaskBySong searchTaskBySong = new SearchTaskBySong(query.trim());
                    searchTaskBySong.execute();
                    SearchTaskByAlbum searchTaskByAlbum = new SearchTaskByAlbum(query.trim());
                    searchTaskByAlbum.execute();
                    SearchTaskByList searchTaskByList = new SearchTaskByList(query.trim());
                    searchTaskByList.execute();
                    SearchTaskByUser searchTaskByUser = new SearchTaskByUser(query.trim());
                    searchTaskByUser.execute();

                }
            }
        });

        search_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    String query = search_text.getText().toString();
                    // if the query isnt empty, then search fro anything in the DB
                    if (!query.trim().isEmpty()){

                        numFailed =0;
                        SearchTaskBySong searchTaskBySong = new SearchTaskBySong(query.trim());
                        searchTaskBySong.execute();
                        SearchTaskByAlbum searchTaskByAlbum = new SearchTaskByAlbum(query.trim());
                        searchTaskByAlbum.execute();
                        SearchTaskByList searchTaskByList = new SearchTaskByList(query.trim());
                        searchTaskByList.execute();
                        SearchTaskByUser searchTaskByUser = new SearchTaskByUser(query.trim());
                        searchTaskByUser.execute();
                    }
                    handled = true;
                }
                return handled;


            }
        });


        song_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity.songsList = songResultList;
                MainActivity.songnumber = (int) l;
                Intent player = new Intent(getActivity(),PlayerActivity.class);
                getActivity().startActivity(player);

            }
        });


        song_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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


        album_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mFragmentNavigation != null) {
                    ArrayList<Album> alb =  new ArrayList<>();
                    alb.add(albumResultList.get((int)l));
                    mFragmentNavigation.pushFragment(SongListFragment.newInstanceAlbum(alb));
                }
            }
        });

        list_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        moreSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mFragmentNavigation != null) {
                    mFragmentNavigation.pushFragment(SongListFragment.newInstanceListSongs(songResultList));
                }
            }
        });
    }

    /**
     * Represents an asynchronous song search
     */
    public class SearchTaskBySong extends AsyncTask<Void, Void, Boolean> {

        private final String query;

        SearchTaskBySong(String txt) {
            query = txt;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;


            songResultList.clear();
            try {

                url = new URL("https://mewat1718.ddns.net/ps/BuscarCancionTitulo");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("titulo", query);             //Añade parametros
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

                client.disconnect();
                if (!result.has("error")){

                    JSONArray resultArray = result.getJSONArray("canciones");
                    for(int i = 0; i<resultArray.length();i++){
                        JSONObject jsObj = resultArray.getJSONObject(i);
                        songResultList.add(new Song(jsObj.getString("tituloCancion"),
                                jsObj.getString("nombreAlbum"),
                                jsObj.getString("nombreArtista"),
                                jsObj.getString("genero"),
                                jsObj.getString("ruta").replace("/usr/local/apache-tomcat-9.0.7/webapps","https://mewat1718.ddns.net"),
                                jsObj.getString("ruta_imagen").replace("..","https://mewat1718.ddns.net")
                                )
                        );
                    }
                    resultArray.get(0);
                    adapterSong.setArrayList(songResultList);
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
                listAdapterSongs.clear();
                for(int i=0; i<4 && i<songResultList.size();i++){
                    HashMap<String,String> temp = new HashMap<String,String>();
                    temp.put("title",songResultList.get(i).getTitle());
                    temp.put("artist",songResultList.get(i).getArtist());
                    listAdapterSongs.add(temp);
                }
                if (listAdapterSongs.size()>0)ScrollSearchViewSongs.setVisibility(View.VISIBLE);
                else ScrollSearchViewSongs.setVisibility(View.GONE);
                Utils.setListViewHeightBasedOnChildren(song_listView);
                adapterSong.notifyDataSetChanged();


            } else {
                ScrollSearchViewSongs.setVisibility(View.GONE);
                if(numFailed++==4)
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


            albumResultList.clear();
            try {
                url = new URL("https://mewat1718.ddns.net/ps/BuscarAlbum");

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

                    JSONArray resultArray = result.getJSONArray("albums");
                    for(int i = 0; i<resultArray.length();i++){
                        JSONObject jsObj = resultArray.getJSONObject(i);
                        albumResultList.add(new Album(jsObj.getString("nombre"),
                                        jsObj.getString("artista"),
                                        jsObj.getString("ruta_imagen")
                                                .replace("..",
                                                        "https://mewat1718.ddns.net")
                                )
                        );
                    }
                    resultArray.get(0);
                    adapterAlbum.setArrayList(albumResultList);
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
                listAdapterAlbums.clear();
                for(int i=0; i<4 && i<albumResultList.size();i++){
                    HashMap<String,String> temp = new HashMap<String,String>();
                    temp.put("nombre",albumResultList.get(i).getName());
                    temp.put("artist",albumResultList.get(i).getArtist());
                    temp.put("url",albumResultList.get(i).getUrlImg());
                    listAdapterAlbums.add(temp);
                }
                Utils.setListViewHeightBasedOnChildren(album_listView);
                if (listAdapterAlbums.size()>0)ScrollSearchViewAlbums.setVisibility(View.VISIBLE);
                else ScrollSearchViewAlbums.setVisibility(View.GONE);
                adapterAlbum.notifyDataSetChanged();
            } else {
                ScrollSearchViewAlbums.setVisibility(View.GONE);
                if(numFailed++==4)
                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong",
                        Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    /**
     * Represents an asynchronous list search task
     */
    public class SearchTaskByList extends AsyncTask<Void, Void, Boolean> {

        private final String query;

        SearchTaskByList(String txt) {
            query = txt;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;


            listaResultList.clear();
            try {
                url = new URL("https://mewat1718.ddns.net/ps/BuscarLista");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("nuevoNombre", query);             //Añade parametros
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

                    JSONArray resultArray = result.getJSONArray("busquedaListas");
                    for(int i = 0; i<resultArray.length();i++){
                        JSONObject jsObj = resultArray.getJSONObject(i);
                        listaResultList.add(new Lista(jsObj.getString("nombre"),
                                        jsObj.getString("nombreUsuario")
                                )
                        );

                        resultArray.get(0);                    }

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
                listAdapterLista.clear();
                for(int i = 0; i<4 && i<listaResultList.size();i++){
                    HashMap<String,String> temp = new HashMap<String,String>();
                    temp.put("nombre",listaResultList.get(i).getName());
                    temp.put("userOwn",listaResultList.get(i).getUserOwner());
                    listAdapterLista.add(temp);
                }
                adapterLista.notifyDataSetChanged();
                Utils.setListViewHeightBasedOnChildren(list_listView);
                if (listAdapterLista.size()>0)ScrollSearchViewListas.setVisibility(View.VISIBLE);
                else ScrollSearchViewListas.setVisibility(View.GONE);

            } else {
                ScrollSearchViewListas.setVisibility(View.GONE);
                if(numFailed++==4)
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
    public class SearchTaskByUser extends AsyncTask<Void, Void, Boolean> {

        private final String query;

        SearchTaskByUser(String txt) {
            query = txt;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;


            userResultList.clear();
            try {
                url = new URL("https://mewat1718.ddns.net/ps/BuscarUsuarios");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("usuario", query);             //Añade parametros
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

                    JSONArray resultArray = result.getJSONArray("usuarios");
                    for(int i = 0; i<resultArray.length();i++){
                        userResultList.add(resultArray.getString(i)
                        );
                    }
                    resultArray.get(0);

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
                listAdapterUser.clear();
                for(int i = 0; i<4 && i<userResultList.size();i++){
                    HashMap<String,String> temp = new HashMap<String,String>();
                    temp.put("user",userResultList.get(i));
                    listAdapterUser.add(temp);
                }
                adapterUser.notifyDataSetChanged();
                Utils.setListViewHeightBasedOnChildren(user_listView);
                if (listAdapterUser.size()>0)ScrollSearchViewUsers.setVisibility(View.VISIBLE);
                else ScrollSearchViewUsers.setVisibility(View.GONE);

            } else {
                ScrollSearchViewUsers.setVisibility(View.GONE);
                if(numFailed++==4)
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
            // TODO: attempt authentication against a network service.
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
                        .appendQueryParameter("ruta", songResultList.get(l).getUrl().replace("https://mewat1718.ddns.net","/usr/local/apache-tomcat-9.0.7/webapps"))
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
