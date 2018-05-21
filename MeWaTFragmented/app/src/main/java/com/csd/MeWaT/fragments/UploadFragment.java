package com.csd.MeWaT.fragments;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;
import com.csd.MeWaT.utils.Song;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;

import java.io.FileInputStream;
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

import static android.app.Activity.RESULT_OK;


public class UploadFragment extends BaseFragment{


    public static final int PICK_MUSIC = 2;
    ArrayList<String> LS2U = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @BindView(R.id.btn_select_music)
    Button selmusicbutton;

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

        adapter = new ArrayAdapter<>(view.getContext(),R.layout.listupload_row,LS2U);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity().getApplicationContext(),String.valueOf(position), Toast.LENGTH_SHORT).show();

            }
        });

        selmusicbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("audio/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("audio/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Song");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                getActivity().startActivityForResult(chooserIntent, PICK_MUSIC);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(String s : LS2U) {
                    MusicList2Upload m2u = new MusicList2Upload(s);
                    m2u.execute((Void) null);
                }
            }
        });




        return view;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == PICK_MUSIC) {
            if (resultCode == RESULT_OK){

                FileDetail file;
                file = getFileDetailFromUri(getActivity(),data.getData());
                LS2U.add(file.fileName);
                adapter.notifyDataSetChanged();
            }

        }
    }
    /**
     * Used to get file detail from uri.
     * <p>
     * 1. Used to get file detail (name & size) from uri.
     * 2. Getting file details from uri is different for different uri scheme,
     * 2.a. For "File Uri Scheme" - We will get file from uri & then get its details.
     * 2.b. For "Content Uri Scheme" - We will get the file details by querying content resolver.
     *
     * @param uri Uri.
     * @return file detail.
     */
    public static FileDetail getFileDetailFromUri(final Context context, final Uri uri) {
        FileDetail fileDetail = null;
        if (uri != null) {
            fileDetail = new FileDetail();
            // File Scheme.
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                File file = new File(uri.getPath());
                fileDetail.fileName = file.getName();
                fileDetail.fileSize = file.length();
            }
            // Content Scheme.
            else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                Cursor returnCursor =
                        context.getContentResolver().query(uri, null, null, null, null);
                if (returnCursor != null && returnCursor.moveToFirst()) {
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    fileDetail.fileName = returnCursor.getString(nameIndex);
                    fileDetail.fileSize = returnCursor.getLong(sizeIndex);
                    returnCursor.close();
                }
            }
        }
        return fileDetail;
    }
    /**
     * File Detail.
     * <p>
     * 1. Model used to hold file details.
     */
    public static class FileDetail {

        // fileSize.
        public String fileName;

        // fileSize in bytes.
        public long fileSize;

        /**
         * Constructor.
         */
        public FileDetail() {

        }
    }

    public class MusicList2Upload extends AsyncTask<Void, Void, Boolean> {

        private final String query;

        MusicList2Upload(String query){this.query = query;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            URL url;
            HttpURLConnection client = null;
            InputStreamReader inputStream;
            DataOutputStream dos;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            try {
                url = new URL("https://mewat1718.ddns.net:80/ps/BuscarCancionTitulo");

                FileInputStream fileInputStream = new FileInputStream(new File(query));
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("User-agent", System.getProperty("http.agent"));
                client.setDoOutput(true);
                client.setRequestProperty("Connection", "Keep-Alive");
                client.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                client.setRequestProperty("Cookie", "login=" + MainActivity.user +
                        "; idSesion=" + MainActivity.idSesion);

                dos = new DataOutputStream(client.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + query + "\"" + lineEnd);
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
                adapter.notifyDataSetChanged();
            }
        }

    }
}
