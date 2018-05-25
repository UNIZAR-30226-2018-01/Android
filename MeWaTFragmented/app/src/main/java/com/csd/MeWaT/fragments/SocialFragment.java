package com.csd.MeWaT.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
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

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static com.csd.MeWaT.utils.Utils.getFileDetailFromUri;


public class SocialFragment extends BaseFragment{

    /**********************************************************************
     * Code for edit profile
     **********************************************************************/
    @BindView(R.id.alerta)
    TextView notificacion;
    @BindView(R.id.social_listView)
    ListView social_listView;

    private MediaPlayer mp;
    private SongsManager manag;
    private Utils utils;
    private ArrayList<Song> resultList;
    private ArrayList<HashMap<String, String>> listAdapter = new ArrayList<HashMap<String, String>>();
    SimpleAdapter adapter;

    public SocialFragment(){
        //Empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_social, container, false);
        ButterKnife.bind(this, view);

        //search_button.setImageResource(R.drawable.ic_search_black_24dp);
        utils = new Utils();
        adapter = new SimpleAdapter(view.getContext(),listAdapter,R.layout.social_row,
                new String[]{"title","artist","emisor"},
                new int[]{R.id.songName,R.id.artistName,R.id.emisorName});
        social_listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SocialTask socialTask = new SocialTask();
        socialTask.execute();

        social_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //MainActivity.songsList = resultList;
                //MainActivity.songnumber = (int) l;
            }
        });
    }

    public class SocialTask extends AsyncTask<Void, Void, Boolean> {

        SocialTask() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;
            resultList = new ArrayList<>();
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

                client.disconnect();
                if (!result.has("error")){

                    JSONArray resultArray = result.getJSONArray("canciones");

                    for(int i = 0; i<resultArray.length();i++){
                        JSONObject jsObj = resultArray.getJSONObject(i);
                        resultList.add(new Song(jsObj.getString("tituloCancion"),
                                        jsObj.getString("nombreArtista"),
                                        jsObj.getString("uploader"),
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
                    temp.put("artist",s.getArtista());
                    temp.put("emisor",s.getAlbum());
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
