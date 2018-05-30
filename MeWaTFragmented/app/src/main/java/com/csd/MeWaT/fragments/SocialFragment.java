package com.csd.MeWaT.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.utils.CustomAdapterSong;
import com.csd.MeWaT.utils.Lista;
import com.csd.MeWaT.utils.Song;
import com.csd.MeWaT.utils.SongsManager;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static com.csd.MeWaT.utils.Utils.getFileDetailFromUri;


public class SocialFragment extends BaseFragment{

    /**********************************************************************
     * Code for edit profile
     **********************************************************************/

    @BindView(R.id.social_listView)
    ListView social_listView;

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout layout;



    private ArrayList<Song> resultList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> listAdapter = new ArrayList<HashMap<String, String>>();
    CustomAdapterSong adapter;

    public SocialFragment(){
        //Empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View view = inflater.inflate(R.layout.fragment_social, container, false);
        ButterKnife.bind(this, view);


        adapter = new CustomAdapterSong(view.getContext(),listAdapter,R.layout.list_row_song,
                new String[]{"songTitle","songArtist"},
                new int[]{R.id.songTitle,R.id.songArtist});
        social_listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layout.setRefreshing(true);
        SocialTask socialTask = new SocialTask();
        socialTask.execute();

        social_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity.setSongsListAndStart(resultList,(int) l);
            }
        });

        social_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialog(l);
                return true;
            }
        });

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                layout.setRefreshing(true);
                SocialTask socialTask = new SocialTask();
                socialTask.execute();
            }
        });
    }

    String options;
    public void dialog(long l){
        final ArrayAdapter<String> arrayAdapter= new ArrayAdapter<>(getContext(),R.layout.dialog_layout,new String []{"Compartir","Añadir a Lista"});
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

                if(which==0){
                    new MainActivity.getFollowingUsers().execute();
                    List<String> users = new ArrayList<>();
                    for(String s : MainActivity.followedUser) users.add(s);
                    final ArrayAdapter<String> arrayAdapter2= new ArrayAdapter<>(getContext(),R.layout.dialog_layout,users.toArray(new String[0]));
                    builderInner.setAdapter(arrayAdapter2, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new ShareSong(l2).execute(arrayAdapter2.getItem(which));
                        }
                    });
                }else{
                    List<String> listas = new ArrayList<>();
                    for(Lista ls : MainActivity.lists) listas.add(ls.getName());
                    final ArrayAdapter<String> arrayAdapter2= new ArrayAdapter<>(getContext(),R.layout.dialog_layout,listas.toArray(new String[0]));

                    builderInner.setAdapter(arrayAdapter2, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Add2List(l2).execute(arrayAdapter2.getItem(which));
                        }
                    });
                }
                builderInner.show();
            }
        });
        builderSingle.show();

    }

    public class SocialTask extends AsyncTask<Void, Void, Boolean> {

        SocialTask() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;
            resultList.clear();
            try {

                url = new URL("https://mewat1718.ddns.net/ps/VerCompartidas");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);
                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder();             //No Añade parametros,puesto que no se pide
                //String query = builder.build().getEncodedQuery();

                OutputStream os = client.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                //writer.write(query);
                //writer.flush();
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
                        resultList.add(new Song(jsObj.getString("tituloCancion"),
                                        jsObj.getString("nombreAlbum"),
                                        jsObj.getString("nombreArtista"),
                                        jsObj.getString("genero"),
                                        jsObj.getString("ruta").replace("/usr/local/apache-tomcat-9.0.7/webapps", "https://mewat1718.ddns.net"),
                                        jsObj.getString("ruta_imagen").replace("..", "https://mewat1718.ddns.net")
                                )
                        );
                    }
                    adapter.setArrayList(resultList);

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
                listAdapter.clear();
                for(Song s: resultList){
                    HashMap<String, String> temp = new HashMap<String, String>();
                    temp.put("songTitle", s.getTitle());
                    temp.put("songArtist", s.getArtist());
                    listAdapter.add(temp);
                }
                adapter.notifyDataSetChanged();
                Utils.setListViewHeightBasedOnChildren(social_listView);
                layout.setRefreshing(false);
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
    public class ShareSong extends AsyncTask<String, Void, Boolean> {

        private final Integer l;

        ShareSong(Integer index) {
            l = index;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;

            try {
                url = new URL("https://mewat1718.ddns.net/ps/CompartirCancion");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("ruta", resultList.get(l).getUrl().replace("https://mewat1718.ddns.net","/usr/local/apache-tomcat-9.0.7/webapps"))
                        .appendQueryParameter("usuarioDestino",params[0]);             //Añade parametros
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
                Toast.makeText(getActivity().getApplicationContext(), "Compartida Correctamente",
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
                        .appendQueryParameter("ruta", resultList.get(l).getUrl().replace("https://mewat1718.ddns.net","/usr/local/apache-tomcat-9.0.7/webapps"))
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
