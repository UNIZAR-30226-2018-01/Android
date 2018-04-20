package com.csd.MeWaT.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;


import java.io.File;

import java.util.ArrayList;

import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;


public class UploadFragment extends BaseFragment{


    Button selmusicbutton, confirmButton;
    ListView listView;
    public static final int PICK_MUSIC = 1;
    ArrayList<String> LS2U = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    ArrayAdapter<String> adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        selmusicbutton = (Button) view.findViewById(R.id.btn_select_music);
        listView = (ListView) view.findViewById(R.id.upload_list);
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

                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
                pickIntent.setType("audio/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Song");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, PICK_MUSIC);
            }
        });
        confirmButton =  (Button) view.findViewById(R.id.btn_confirm_list);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicList2Upload m2u= new MusicList2Upload(LS2U);
                m2u.execute((Void) null);

            }
        });

        ButterKnife.bind(this, view);

        ( (MainActivity)getActivity()).updateToolbarTitle("Upload");



        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_MUSIC) {
            if (resultCode == RESULT_OK){

                FileDetail file;
                file = getFileDetailFromUri(getContext(),data.getData());
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

        private final ArrayList<String> musiclist;

        MusicList2Upload(ArrayList<String> parameter) {
            musiclist = parameter;
        }

        @Override // @SuppressWarnings("Falto de implementar necesito comunicar el upload")
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                Thread.sleep(2000);
                return false;
                // Simulate network access.

            } catch (InterruptedException e) {
                return false;
            }

//            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {

            }else{
                confirmButton.setError("Something went wrong please try again");
            }

        }

        @Override
        protected void onCancelled() {
        }
    }


}
