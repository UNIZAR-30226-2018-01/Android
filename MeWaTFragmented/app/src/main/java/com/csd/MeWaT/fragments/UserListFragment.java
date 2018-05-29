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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.utils.CustomAdapterUsers;

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

public class UserListFragment extends BaseFragment {

    @BindView(R.id.user_listview)
    GridView user_listView;

    private static Boolean following = false;
    private static ArrayList<HashMap<String,String>> listAdapterUser =new ArrayList<HashMap<String,String>>();
    private static ArrayList<String> userResultList =new ArrayList<>();
    CustomAdapterUsers adapterUser;

    public static UserListFragment newInstance(ArrayList<String> instance) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_INSTANCE, instance);
        userResultList = instance;
        following = false;
        UserListFragment fragment = new UserListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static UserListFragment newInstanceFollowing(ArrayList<String> instance) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_INSTANCE, instance);
        userResultList = instance;
        following = true;
        UserListFragment fragment = new UserListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapterUser= new CustomAdapterUsers(view.getContext(), listAdapterUser,R.layout.list_row_user,
                new String[]{"user"},
                new int[]{R.id.UserName});
        user_listView.setAdapter(adapterUser);

        adapterUser.setArrayList(userResultList);
        listAdapterUser.clear();
        for(int i = 0; i<4 && i<userResultList.size();i++){
            HashMap<String,String> temp = new HashMap<>();
            temp.put("user",userResultList.get(i));
            listAdapterUser.add(temp);
        }
        adapterUser.notifyDataSetChanged();


        user_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                final int l2 = (int)l;

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
                builderSingle.setTitle("Dejar de Seguir");

                String option="Seguir";

                builderSingle.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                for(String s : MainActivity.followedUser) {
                    if (s.equals(userResultList.get((int) l))) option = "Dejar de Seguir";
                }
                if(option.equals("Seguir")){
                    builderSingle.setPositiveButton(option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new FollowUser("SeguirUsuario",userResultList.get(l2)).execute();
                        }
                    });
                }else{
                    builderSingle.setPositiveButton(option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new FollowUser("DejarDeSeguirUsuario",userResultList.get(l2)).execute();
                        }
                    });
                }


                builderSingle.show();

                return false;
            }
        });

    }



    /**
     * Represents an asynchronous album search task
     */
    public class FollowUser extends AsyncTask<Void, Void, Boolean> {


        String servlet,user;
        FollowUser(String ask, String usuario){
            servlet = ask;

            user = usuario;
        }

        @Override
        protected Boolean doInBackground(Void ... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;

            try {
                url = new URL("https://mewat1718.ddns.net/ps/"+servlet);

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter(servlet.equals("SeguirUsuario")?"seguido":"nombreSeguido", user);             //Añade parametros
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
                if (servlet.equals("DejarDeSeguirUsuario")){
                    Toast.makeText(getActivity().getApplicationContext(), "Usuario dejado de Seguir",
                            Toast.LENGTH_SHORT).show();
                    MainActivity.followedUser.remove(user);
                    if(following) {
                        adapterUser.setArrayList(userResultList);
                        listAdapterUser.clear();
                        for (int i = 0; i < 4 && i < userResultList.size(); i++) {
                            HashMap<String, String> temp = new HashMap<>();
                            temp.put("user", userResultList.get(i));
                            listAdapterUser.add(temp);
                        }
                        adapterUser.notifyDataSetChanged();
                        userResultList.remove(user);
                    }
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Usuario Seguido",
                            Toast.LENGTH_SHORT).show();
                    MainActivity.followedUser.add(user);
                }

            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Sin exito",
                        Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onCancelled() {

        }
    }
}
