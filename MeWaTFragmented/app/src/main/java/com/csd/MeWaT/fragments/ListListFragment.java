package com.csd.MeWaT.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.utils.Album;
import com.csd.MeWaT.utils.Lista;

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

public class ListListFragment extends BaseFragment {

    @BindView(R.id.ownLists_listView)
    ListView list_listView;

    private String m_Text;


    SimpleAdapter adapter;

    private static ArrayList <HashMap<String,String>> ListListAdapter = new ArrayList<>();
    private static ArrayList<Lista> ListResultList = new ArrayList<>();

    public static ListListFragment newInstance(ArrayList <Lista> instance) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_INSTANCE, instance);
        ListResultList = instance;

        ListListAdapter.clear();
        for(Lista s: ListResultList){
            HashMap<String,String> temp =  new HashMap<>();
            temp.put("nombre",s.getName());
            ListListAdapter.add(temp);
        }

        ListListFragment fragment = new ListListFragment();
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

        View view = inflater.inflate(R.layout.fragment_list_list, container, false);


        ButterKnife.bind(this, view);


        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        setHasOptionsMenu(true);



        adapter = new SimpleAdapter(view.getContext(), ListListAdapter,R.layout.list_row_lista,
                new String[]{"nombre"},
                new int[]{R.id.NameListRow});


        list_listView.setAdapter(adapter);


        list_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mFragmentNavigation != null) {
                    ArrayList<Lista> alb =  new ArrayList<>();
                    alb.add(ListResultList.get((int)l));
                    mFragmentNavigation.pushFragment1(SongListFragment.newInstanceList(alb),"Mis Listas");
                }
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actionbar_plus, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.add:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("New PlayList");

                // Set up the input
                final EditText input = new EditText(getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        new AddListTask().execute(m_Text);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            default:
                return super.onOptionsItemSelected(item);
        }


    }


    public class AddListTask extends AsyncTask<String, Void, Boolean> {


        AddListTask(){

        }

        @Override
        protected Boolean doInBackground(String... parms) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;

            try {
                url = new URL("https://mewat1718.ddns.net/ps/CrearListaDeReproduccion");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);


                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("nombreLista", parms[0]);
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
                if (result.has("error")){
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
        protected void onPostExecute(Boolean success) {
            if (success){
                Toast.makeText(getActivity().getApplicationContext(), " Success ", Toast.LENGTH_SHORT).show();
                new SearchListByUser().execute();
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(), "Sorry, something was wrong!", Toast.LENGTH_SHORT).show();
            }
        }
    }

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
                        if(!resultArray.getString(i).equals("Favoritos")){
                            ListResultList.add(new Lista(resultArray.getString(i),MainActivity.user));
                        }
                    }
                }else{
                    if(result.get("error").equals("Usuario no logeado")){
                        SharedPreferences sp = getActivity().getSharedPreferences("USER_LOGIN", Context.MODE_PRIVATE);

                        sp.edit().clear().apply();

                        Intent LoginActivity = new Intent( getActivity(), com.csd.MeWaT.activities.LoginActivity.class);
                        getActivity().startActivity(LoginActivity);
                        getActivity().finish();
                    }
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
                ListListAdapter.clear();
                for(Lista s: ListResultList){
                    HashMap<String,String> temp =  new HashMap<>();
                    temp.put("nombre",s.getName());
                    ListListAdapter.add(temp);
                }
                adapter.notifyDataSetChanged();
            } else {

            }
        }

        @Override
        protected void onCancelled() {

        }
    }



}
