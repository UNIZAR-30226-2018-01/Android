package com.csd.MeWaT.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.utils.Library;
import com.csd.MeWaT.utils.Utils;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;


public class UploadFragment extends BaseFragment{



    ArrayList<String> LS2U = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    ArrayAdapter<String> adapter;


    @BindView(R.id.upload_list)
    ListView listView;

    @BindView(R.id.btn_confirm_list)
    Button confirmButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_upload, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        adapter = new ArrayAdapter<>(view.getContext(),R.layout.list_row_upload,names);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity().getApplicationContext(),String.valueOf(position), Toast.LENGTH_SHORT).show();

            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i = 0; i< names.size();i++) {
                    MusicList2Upload m2u = new MusicList2Upload(names.get(i),LS2U.get(i));
                    m2u.execute((Void) null);
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
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("audio/*");


                Intent chooserIntent = Intent.createChooser(getIntent, "Select Song" );
                startActivityForResult(chooserIntent, Library.PICK_MUSIC);
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Library.PICK_MUSIC) {
            if (resultCode == RESULT_OK){

                Utils.FileDetail file;
                file = Utils.getFileDetailFromUri(getActivity(),data.getData());
                String uri = Utils.getPath(getActivity(),data.getData());
                names.add(file.fileName);
                LS2U.add(uri);
                adapter.notifyDataSetChanged();
            }

        }
        else super.onActivityResult(requestCode,resultCode,data);
    }


    public class MusicList2Upload extends AsyncTask<Void, Void, Boolean> {

        private final String name,uri;

        MusicList2Upload(String name, String uri){
            this.name = name;
            this.uri = uri;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;
            DataOutputStream dos;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 8 * 1024 * 1024;

            try {
                url = new URL("https://mewat1718.ddns.net/ps/SubirCanciones");

                File file = new File(uri);

                FileInputStream fileInputStream = new FileInputStream(file);

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setDoInput(true); // Allow Inputs
                client.setDoOutput(true); // Allow Outputs
                client.setUseCaches(false); // Don't use a Cached Copy
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setRequestProperty("Connection", "Keep-Alive");
                client.setRequestProperty("ENCTYPE", "multipart/form-data");
                client.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);

                dos = new DataOutputStream(client.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + name + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                // close streams
                Log.e("Debug", "File is written");
                fileInputStream.close();
                dos.flush();
                dos.close();
                int responseCode = client.getResponseCode();
                System.out.println("\nSending 'Get' request to URL : " +    url+"--"+responseCode);
            } catch (MalformedURLException e) {
                return false;
            } catch (SocketTimeoutException e) {
                return false;
            }catch (IOException e) {
                System.out.println(e);
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
            HashMap<String,String> temp = new HashMap<String,String>();

            if (success) {
                LS2U.clear();
                names.clear();
                adapter.notifyDataSetChanged();
            }
            else{
                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }
}
