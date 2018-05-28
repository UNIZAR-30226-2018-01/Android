package com.csd.MeWaT.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.utils.Utils;

import org.apache.http.conn.ssl.SSLSocketFactory;
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

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static com.csd.MeWaT.utils.Utils.getFileDetailFromUri;

/**
 * Created by mengd on 21/05/2018.
 */

public class ModifyFragment extends BaseFragment {
    /**********************************************************************
     * Code for edit profile
     **********************************************************************/

    @BindView(R.id.userChange)
    EditText userText;
    @BindView(R.id.oldPswd) EditText oldPasword;
    @BindView(R.id.pswdChange) EditText pasText;
    @BindView(R.id.pswdChangeRep) EditText pasRepText;
    @BindView(R.id.btton_change_icon)
    Button chgIconBton;
    @BindView(R.id.btton_change_name) Button chgNameBton;
    @BindView(R.id.btton_change_paswd) Button chgPswdBton;


    @BindView(R.id.btton_CloseSesion)
    Button closeSesion;

    @BindView(R.id.passDelete)
    EditText pasDelete;

    @BindView(R.id.btton_deleteUser)
    Button deteleUser;

    public static final int PICK_IMAGE = 8;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_edit_profile, container, false);
        ButterKnife.bind(this, view);
        userText.setText(MainActivity.user);
        chgNameBton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = userText.getText().toString();
                if (!name.isEmpty()) {
                    ModifyFragment.ChangeNameTask changTask = new ModifyFragment.ChangeNameTask(name);
                    changTask.execute();
                }
                else {
                    userText.setError("This field is requiered");
                }
            }
        });

        chgPswdBton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String old = oldPasword.getText().toString();
                String pasword = pasText.getText().toString();
                String paswordRep = pasRepText.getText().toString();
                if (!pasword.isEmpty()){
                    if (!old.isEmpty()) {
                        if (pasword.equals(paswordRep)) {
                            ModifyFragment.ChangePaswdTask changTask = new ModifyFragment.ChangePaswdTask(old, pasword, paswordRep);
                            changTask.execute();
                        } else {
                            pasRepText.setError("The password isnt the same");
                        }
                    }else {
                        oldPasword.setError("This filed cannot be empty");
                    }
                }
                else {
                    pasText.setError("This field cannot be empty");
                }
            }
        });

        chgIconBton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });
        return view;
    }

    public class ChangeNameTask extends AsyncTask<Void, Void, Boolean> {

        private final String UserName;


        ChangeNameTask(String name){
            UserName = name;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;

            try {
                url = new URL("https://mewat1718.ddns.net/ps/CambiarNombre");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);
                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("nuevoNombre", UserName);
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
                }else{
                    MainActivity.user=UserName;
                    SharedPreferences sp = getActivity().getPreferences(Context.MODE_PRIVATE);
                    sp.edit().putString("username",UserName).apply();
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
                Toast.makeText(getActivity().getApplicationContext(), "Username has been changed! ", Toast.LENGTH_SHORT).show();
                userText.setText(MainActivity.user);
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(), "Sorry, something was wrong!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class ChangePaswdTask extends AsyncTask<Void, Void, Boolean>{
        private final String paswd;
        private final String paswdRep;
        private final String oldPwsd;

        ChangePaswdTask(String oldpaswd, String pswd, String pswdRep){
            paswd = pswd;
            paswdRep = pswdRep;
            oldPwsd = oldpaswd;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;

            try {
                url = new URL("https://mewat1718.ddns.net/ps/CambiarContrasenya");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("viejaPass", oldPwsd)
                        .appendQueryParameter("nuevaContrasenya", paswd)
                        .appendQueryParameter("nuevaContrasenyaR", paswdRep);
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
                Toast.makeText(getActivity().getApplicationContext(), "Password has been changed ", Toast.LENGTH_SHORT).show();
                oldPasword.getText().clear();
                pasText.getText().clear();
                pasRepText.getText().clear();
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(), "Sorry, something was wrong!", Toast.LENGTH_SHORT).show();
                oldPasword.getText().clear();
                pasText.getText().clear();
                pasRepText.getText().clear();

            }
        }
    }

    public class DeleteTask extends AsyncTask<Void, Void, Boolean>{
        private final String paswd;
        private final String paswdRep;
        private final String oldPwsd;

        DeleteTask(String oldpaswd, String pswd, String pswdRep){
            paswd = pswd;
            paswdRep = pswdRep;
            oldPwsd = oldpaswd;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;

            try {
                url = new URL("https://mewat1718.ddns.net/ps/EliminarCuenta");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("pass", oldPwsd)
                        .appendQueryParameter("nuevaContrasenya", paswd)
                        .appendQueryParameter("nuevaContrasenyaR", paswdRep);
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
                Toast.makeText(getActivity().getApplicationContext(), "Password has been changed ", Toast.LENGTH_SHORT).show();
                oldPasword.getText().clear();
                pasText.getText().clear();
                pasRepText.getText().clear();
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(), "Sorry, something was wrong!", Toast.LENGTH_SHORT).show();
                oldPasword.getText().clear();
                pasText.getText().clear();
                pasRepText.getText().clear();

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK){
                Utils.FileDetail file;
                file = getFileDetailFromUri(getContext(),data.getData());
            }
        }
    }
}
