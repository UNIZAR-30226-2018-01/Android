package com.csd.MeWaT.fragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.activities.PlayerActivity;
import com.csd.MeWaT.utils.CustomAdapterSong;
import com.csd.MeWaT.utils.Lista;
import com.csd.MeWaT.utils.Song;
import com.csd.MeWaT.utils.Utils;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HomeFragment extends BaseFragment {


    @BindView(R.id.LayoutRecientes)
    LinearLayout Recents;

    @BindView(R.id.RecListView)
    GridView RecListView;

    @BindView(R.id.moreRecents)
    TextView moreRecents;

    //---------------------------------------
    @BindView(R.id.LayoutGeneros)
    LinearLayout Genre;

    @BindView(R.id.GenreListView)
    GridView GenreListView;

    @BindView(R.id.moreGenre)
    TextView moreGenre;

    //------------------------------------------
    @BindView(R.id.LayoutTopSem)
    LinearLayout TopSem;

    @BindView(R.id.TopSemListView)
    GridView TopSemListView;

    @BindView(R.id.moreTopSem)
    TextView moreTopSem;
    //------------------------------------------



    private ArrayList<Song> resultRecentsList = new ArrayList<>();
    private ArrayList<HashMap<String,String>> resultRecentsAdapter =new ArrayList<HashMap<String,String>>();
    CustomAdapterSong RecentsAdapter;

    private ArrayList<String> resultGenreList= new ArrayList<>();
    private ArrayList<HashMap<String,String>> resultGenreAdapter =new ArrayList<HashMap<String,String>>();
    SimpleAdapter GenreAdapter;

    private ArrayList<Song> resultTopSemList= new ArrayList<>();
    private ArrayList<HashMap<String,String>> resultTopSemAdapter =new ArrayList<HashMap<String,String>>();
    CustomAdapterSong TopSemAdapter;





    public static HomeFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(ARGS_INSTANCE, instance);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public HomeFragment() {  }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        RecentsAdapter = new CustomAdapterSong(view.getContext(), resultRecentsAdapter,R.layout.list_row_song,
                new String[]{"title","artist"},
                new int[]{R.id.songTitle,R.id.songArtist});
        RecListView.setAdapter(RecentsAdapter);


        TopSemAdapter = new CustomAdapterSong(view.getContext(), resultTopSemAdapter,R.layout.list_row_song,
                new String[]{"title","artist"},
                new int[]{R.id.songTitle,R.id.songArtist});
        TopSemListView.setAdapter(TopSemAdapter);

        GenreAdapter = new SimpleAdapter(view.getContext(), resultGenreAdapter,R.layout.list_row_genre,
                new String[]{"name"},
                new int[]{R.id.GenreName});
        GenreListView.setAdapter(GenreAdapter);


        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(!resultTopSemAdapter.isEmpty()){
            TopSemAdapter.setArrayList(resultTopSemList);
            TopSemListView.setVisibility(View.VISIBLE);
        }
        if(!resultRecentsAdapter.isEmpty()){
            RecentsAdapter.setArrayList(resultRecentsList);
            RecListView.setVisibility(View.VISIBLE);
        }
        if(!resultGenreAdapter.isEmpty()){
            GenreListView.setVisibility(View.VISIBLE);
        }

        new SearchSongsBy().execute("EscuchadasRecientemente");
        new SearchSongsBy().execute("TopSemanal");
        new SearchGenre().execute();

        RecListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity.songsList = resultRecentsList;
                MainActivity.songnumber = (int) l;
                Intent player = new Intent(getActivity(),PlayerActivity.class);
                getActivity().startActivity(player);
            }
        });

        GenreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }



    public class SearchSongsBy extends AsyncTask<String, Void, Boolean> {


        SearchSongsBy(){}

        Boolean tipo = false;
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO: attempt authentication against a network service.
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
                    if(params[0].equals("TopSemanal")){
                        tipo = true;
                        resultTopSemList.clear();
                        JSONArray resultArray = result.getJSONArray("canciones");
                        for(int i = 0; i<resultArray.length();i++){
                            JSONObject jsObj = resultArray.getJSONObject(i);
                            resultTopSemList.add(new Song(jsObj.getString("tituloCancion"),
                                            jsObj.getString("nombreAlbum"),
                                            jsObj.getString("nombreArtista"),
                                            jsObj.getString("genero"),
                                            jsObj.getString("ruta").replace("/usr/local/apache-tomcat-9.0.7/webapps","https://mewat1718.ddns.net"),
                                            jsObj.getString("ruta_imagen").replace("..","https://mewat1718.ddns.net")
                                    )
                            );
                        }
                        resultArray.get(0);
                        TopSemAdapter.setArrayList(resultTopSemList);

                    }else{
                        resultRecentsList.clear();
                        JSONArray resultArray = result.getJSONArray("canciones");
                        for(int i = 0; i<resultArray.length();i++){
                            JSONObject jsObj = resultArray.getJSONObject(i);
                            resultRecentsList.add(new Song(jsObj.getString("tituloCancion"),
                                            jsObj.getString("nombreAlbum"),
                                            jsObj.getString("nombreArtista"),
                                            jsObj.getString("genero"),
                                            jsObj.getString("ruta").replace("/usr/local/apache-tomcat-9.0.7/webapps","https://mewat1718.ddns.net"),
                                            jsObj.getString("ruta_imagen").replace("..","https://mewat1718.ddns.net")
                                    )
                            );
                        }
                        resultArray.get(0);
                        RecentsAdapter.setArrayList(resultRecentsList);
                    }
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
                if(tipo) {
                    resultTopSemAdapter.clear();
                    for (int i = 0; i < 4 && i < resultTopSemList.size(); i++) {
                        HashMap<String, String> temp = new HashMap<String, String>();
                        temp.put("title", resultTopSemList.get(i).getTitle());
                        temp.put("artist", resultTopSemList.get(i).getArtist());
                        resultTopSemAdapter.add(temp);
                    }
                    if (resultTopSemAdapter.size() > 0) TopSem.setVisibility(View.VISIBLE);
                    else TopSem.setVisibility(View.GONE);
                    Utils.setListViewHeightBasedOnChildren(TopSemListView);
                    TopSemAdapter.notifyDataSetChanged();
                }else{
                    resultRecentsAdapter.clear();
                    for (int i = 0; i < 4 && i < resultRecentsList.size(); i++) {
                        HashMap<String, String> temp = new HashMap<String, String>();
                        temp.put("title", resultRecentsList.get(i).getTitle());
                        temp.put("artist", resultRecentsList.get(i).getArtist());
                        resultRecentsAdapter.add(temp);
                    }
                    if (resultRecentsAdapter.size() > 0) Recents.setVisibility(View.VISIBLE);
                    else Recents.setVisibility(View.GONE);
                    Utils.setListViewHeightBasedOnChildren(RecListView);
                    RecentsAdapter.notifyDataSetChanged();
                }

            } else {
                if (resultTopSemAdapter.size()==0)TopSem.setVisibility(View.GONE);
                if (resultRecentsAdapter.size()==0) Recents.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    /**
     * Represents an asynchronous song search
     */
    public class SearchGenre extends AsyncTask<String, Void, Boolean> {


        SearchGenre(){}

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO: attempt authentication against a network service.
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;


            resultGenreList.clear();
            try {
                url = new URL("https://mewat1718.ddns.net/ps/ObtenerGeneros");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);

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

                    JSONArray resultArray = result.getJSONArray("generos");
                    for(int i = 0; i<resultArray.length();i++){
                       resultGenreList.add(resultArray.getString(i));
                    }
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
                resultGenreAdapter.clear();
                for(int i = 0; i<4 && i<resultGenreList.size();i++){
                    HashMap<String,String> temp = new HashMap<String,String>();
                    temp.put("name",resultGenreList.get(i));
                    resultGenreAdapter.add(temp);
                }
                GenreAdapter.notifyDataSetChanged();
                Utils.setListViewHeightBasedOnChildren(GenreListView);
                if (resultGenreAdapter.size()>0)Genre.setVisibility(View.VISIBLE);
                else Genre.setVisibility(View.GONE);

            } else {
                if (resultGenreAdapter.size()==0)Genre.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

}
