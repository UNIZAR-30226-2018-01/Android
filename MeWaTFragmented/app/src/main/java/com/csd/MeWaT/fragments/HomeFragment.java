package com.csd.MeWaT.fragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
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

import butterknife.BindView;
import butterknife.ButterKnife;


public class HomeFragment extends BaseFragment {


    @BindView(R.id.favoritos) ImageButton Favoritos;
    @BindView(R.id.listas) ImageButton Listas;
    @BindView(R.id.listas_top) ImageButton Listas_top;
    @BindView(R.id.recientes) ImageButton Recientes;

    int fragCount;

    private ArrayList<Song> resultList;
    private ArrayList<HashMap<String,String>> listAdapter =new ArrayList<HashMap<String,String>>();
    SimpleAdapter adapter;


    public static HomeFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(ARGS_INSTANCE, instance);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Favoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoriteTask favoriteTask = new FavoriteTask();
                favoriteTask.execute();
            }
        });

        Listas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListTask listTask = new ListTask();
                listTask.execute();
            }
        });


        ( (MainActivity)getActivity()).updateToolbarTitle((fragCount == 0) ? "Home" : "Sub Home "+fragCount);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public class ListTask extends AsyncTask<Void, Void, Boolean> {

        ListTask() {}

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            URL url;
            HttpURLConnection client = null;
            InputStreamReader inputStream;


            resultList = new ArrayList<>();
            try {
                url = new URL("http://mewat1718.ddns.net:8080/ps/VerLista");

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("User-agent", System.getProperty("http.agent"));
                client.setDoOutput(true);
                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("nombreLista", "Favoritos")
                        .appendQueryParameter("creadorLista", MainActivity.user);
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
                client.disconnect();

                BufferedReader reader = new BufferedReader(inputStream);
                StringBuilder builder = new StringBuilder();

                for (String line = null; (line = reader.readLine()) != null ; ) {
                    builder.append(line).append("\n");
                }

                // Parse into JSONObject
                String resultStr = builder.toString();
                JSONTokener tokener = new JSONTokener(resultStr);
                JSONObject result = new JSONObject(tokener);

                if (!result.has("error")){

                    JSONArray resultArray = result.getJSONArray("canciones");
                    for(int i = 0; i<resultArray.length();i++){
                        JSONObject jsObj = resultArray.getJSONObject(i);
                        resultList.add(new Song(jsObj.getString("tituloCancion"),
                                    jsObj.getString("nombreArtista"),
                                    jsObj.getString("nombreAlbum"),
                                    jsObj.getString("genero"),
                                    jsObj.getString("url")
                            )
                        );
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
            HashMap<String,String> temp = new HashMap<String,String>();

            if (success) {
                listAdapter.clear();
                for(Song s: resultList){
                    temp.put("title",s.getTitle());
                    temp.put("album",s.getAlbum());
                    temp.put("artist",s.getArtista());
                    listAdapter.add(temp);
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

    public class FavoriteTask extends AsyncTask<Void, Void, Boolean> {

        FavoriteTask() {}

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            URL url;
            HttpURLConnection client = null;
            InputStreamReader inputStream;


            resultList = new ArrayList<>();
            try {
                url = new URL("http://mewat1718.ddns.net:8080/ps/VerLista");

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("User-agent", System.getProperty("http.agent"));
                client.setDoOutput(true);
                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("nombreLista", "Favoritos")
                        .appendQueryParameter("creadorLista", MainActivity.user);
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
                client.disconnect();

                BufferedReader reader = new BufferedReader(inputStream);
                StringBuilder builder = new StringBuilder();

                for (String line = null; (line = reader.readLine()) != null ; ) {
                    builder.append(line).append("\n");
                }

                // Parse into JSONObject
                String resultStr = builder.toString();
                JSONTokener tokener = new JSONTokener(resultStr);
                JSONObject result = new JSONObject(tokener);

                if (!result.has("error")){

                    JSONArray resultArray = result.getJSONArray("canciones");
                    for(int i = 0; i<resultArray.length();i++){
                        JSONObject jsObj = resultArray.getJSONObject(i);
                        resultList.add(new Song(jsObj.getString("tituloCancion"),
                                        jsObj.getString("nombreArtista"),
                                        jsObj.getString("nombreAlbum"),
                                        jsObj.getString("genero"),
                                        jsObj.getString("url")
                                )
                        );
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
            HashMap<String,String> temp = new HashMap<String,String>();

            if (success) {
                listAdapter.clear();
                for(Song s: resultList){
                    temp.put("title",s.getTitle());
                    temp.put("album",s.getAlbum());
                    temp.put("artist",s.getArtista());
                    listAdapter.add(temp);
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
