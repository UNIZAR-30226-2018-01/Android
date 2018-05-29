package com.csd.MeWaT.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.utils.DownloadUserImageTask;
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

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ProfileFragment extends BaseFragment{


    @BindView(R.id.username)
    TextView username;

    @BindView(R.id.user_image)
    ImageView user_image;

    @BindView(R.id.favoriteButton)
    Button favButton;

    @BindView(R.id.myListButton)
    Button myListButton;

    @BindView(R.id.followedButton)
    Button followedButton;

    @BindView(R.id.followingButton)
    Button followingButton;



    @BindView(R.id.historyButton)
    Button historyButton;

    private ArrayList<Song> songResultList = new ArrayList<>();

    private ArrayList<Lista> ListResultList = new ArrayList<>();

    private ArrayList<HashMap<String,String>> listAdapterUser = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);




        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        new DownloadUserImageTask(user_image).execute("https://mewat1718.ddns.net/ps/images/"+MainActivity.user+".jpg");
        username.setText(MainActivity.user);


        myListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SearchListByUser().execute();

            }
        });

        followedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SearchTaskByUser().execute("VerSeguidores");
            }
        });

        followingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new SearchTaskByUser().execute("VerSeguidos");

            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new SearchTaskBySong(null).execute("EscuchadasRecientemente");

            }
        });

        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Favoritos");
                ArrayList<Lista> arrayList = new ArrayList<>();
                arrayList.add(new Lista("Favoritos",MainActivity.user));
                mFragmentNavigation.pushFragment(SongListFragment.newInstanceList(arrayList));
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actionbar_settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.settingsbutton:
                if(mFragmentNavigation != null) {
                    mFragmentNavigation.pushFragment(new ModifyFragment());
                }
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    /**
     * Represents an asynchronous album search task
     */
    public class SearchTaskByUser extends AsyncTask<String, Void, Boolean> {

        private ArrayList<String> userResultList;
        private boolean seg;

        SearchTaskByUser() {

        }

        @Override
        protected Boolean doInBackground(String... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;

            seg = params[0].equals("VerSeguidores");
            userResultList = new ArrayList<>();
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
                    JSONArray resultArray = result.getJSONArray( params[0].equals("VerSeguidores")?"listaDeSeguidores":"listaDeSeguidos");
                    String search = params[0].equals("VerSeguidores")?"nombreSeguido":"nombreSeguido";
                    for(int i = 0; i<resultArray.length();i++){
                        JSONObject jsObj = resultArray.getJSONObject(i);
                        userResultList.add( jsObj.getString(search));
                    }
                    if(params[0].equals("VerSeguidos"))MainActivity.followedUser=userResultList;
                }else{
                    if(result.has("error")){
                        if(result.get("error").equals("Usuario no logeado")){
                            SharedPreferences sp = getActivity().getSharedPreferences("USER_LOGIN", Context.MODE_PRIVATE);

                            sp.edit().clear().apply();

                            Intent LoginActivity = new Intent( getActivity(), com.csd.MeWaT.activities.LoginActivity.class);
                            getActivity().startActivity(LoginActivity);
                            getActivity().finish();
                        }
                        return false;
                    }
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
                if (userResultList.size()>0){
                    if(seg){
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Seguidores");
                        if(mFragmentNavigation != null) {
                            mFragmentNavigation.pushFragment(UserListFragment.newInstance(userResultList));
                        }
                    }
                    else{
                        if(mFragmentNavigation != null) {
                            mFragmentNavigation.pushFragment(UserListFragment.newInstanceFollowing(userResultList));
                        }
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Siguiendo");
                    }

                }
            } else {
                if(seg)Toast.makeText(getActivity().getApplicationContext(), "No tiene ningún Seguidor",Toast.LENGTH_SHORT).show();
                else Toast.makeText(getActivity().getApplicationContext(), "No sigue a ningún Usuario",Toast.LENGTH_SHORT).show();

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

        SearchTaskBySong( String list) {
            List = list;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;


            songResultList = new ArrayList<>();
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
                }else{
                    if(result.has("error")){
                        if(result.get("error").equals("Usuario no logeado")){
                            SharedPreferences sp = getActivity().getSharedPreferences("USER_LOGIN", Context.MODE_PRIVATE);

                            sp.edit().clear().apply();

                            Intent LoginActivity = new Intent( getActivity(), com.csd.MeWaT.activities.LoginActivity.class);
                            getActivity().startActivity(LoginActivity);
                            getActivity().finish();
                        }
                        return false;
                    }
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
                if(mFragmentNavigation != null) {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Historial");
                    mFragmentNavigation.pushFragment(SongListFragment.newInstanceListSongs(songResultList));
                }
            }else{
                Toast.makeText(getActivity().getApplicationContext(), "No se ha reproducido ninguna Cancion",
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
    public class SearchListByUser extends AsyncTask<String, Void, Boolean> {


        SearchListByUser(){}

        @Override
        protected Boolean doInBackground(String... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;


            ListResultList.clear();
            try {
                url = new URL("https://mewat1718.ddns.net/ps/MostrarListasReproduccion");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("user", MainActivity.user)
                        .appendQueryParameter("contrasenya", MainActivity.password);             //Añade parametros
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

                    JSONArray resultArray = result.getJSONArray("nombre");
                    for(int i = 0; i<resultArray.length();i++){
                        if(!resultArray.getString(i).equals("Favoritos"))ListResultList.add(new Lista(resultArray.getString(i),MainActivity.user));
                    }
                    MainActivity.lists = ListResultList;
                }else{
                    if(result.has("error")){
                        if(result.get("error").equals("Usuario no logeado")){
                            SharedPreferences sp = getActivity().getSharedPreferences("USER_LOGIN", Context.MODE_PRIVATE);

                            sp.edit().clear().apply();

                            Intent LoginActivity = new Intent( getActivity(), com.csd.MeWaT.activities.LoginActivity.class);
                            getActivity().startActivity(LoginActivity);
                            getActivity().finish();
                        }
                        return false;
                    }
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
                if(mFragmentNavigation != null) {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Mis Listas");
                    mFragmentNavigation.pushFragment(ListListFragment.newInstance(ListResultList));
                }
            } else {

            }
        }

        @Override
        protected void onCancelled() {

        }
    }


}
