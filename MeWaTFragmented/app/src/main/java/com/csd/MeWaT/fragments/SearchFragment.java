package com.csd.MeWaT.fragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.utils.SongsManager;
import com.csd.MeWaT.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SearchFragment extends BaseFragment implements MediaPlayer.OnCompletionListener{

    @BindView(R.id.textBusqueda)
    EditText busqueda;
    @BindView(R.id.buscar)
    ImageButton botonBuscar;

    private MediaPlayer mp;
    private SongsManager mang;
    private Utils utils;
    private String query;

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

        botonBuscar.setImageResource(R.drawable.tab_search);
        busqueda.setSelection(R.id.textBusqueda);
        utils = new Utils();
        query = new String();
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        botonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the query from the textView
                query = busqueda.getText().toString();
                // if the query isnt empry, then search fro anything in the DB
                if (!query.isEmpty()){

                }
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    public class SearchTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            URL url;
            HttpURLConnection client = null;
            InputStreamReader input;
            String title, info;
            InputStream inpStream;

            try{
                url = new URL("http://mewat1718.ddns.net:8080/ps/Busqueda");

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("User-agent", System.getProperty("http.agent"));
                client.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("textBusqueda",query );


            }catch (MalformedURLException e){

            }catch (SocketTimeoutException e){

            }catch (IOException e){

            }
            return null;
        }
    }
}
