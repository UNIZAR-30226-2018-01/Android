package com.f22labs.instalikefragmenttransaction.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.f22labs.instalikefragmenttransaction.R;
import com.f22labs.instalikefragmenttransaction.activities.MainActivity;

import java.io.File;

import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;


public class UploadFragment extends BaseFragment{


    Button selmusicbutton, confirmButton;
    public static final int PICK_MUSIC = 1;
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

        selmusicbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("audio/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("audio/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Song");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, PICK_MUSIC);
            }
        });
        confirmButton =  (Button) view.findViewById(R.id.btn_confirm_list);

        ButterKnife.bind(this, view);

        ( (MainActivity)getActivity()).updateToolbarTitle("Upload");



        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_MUSIC) {
            if (resultCode == RESULT_OK){
                Uri file = data.getData();
            }



        }
    }


}
