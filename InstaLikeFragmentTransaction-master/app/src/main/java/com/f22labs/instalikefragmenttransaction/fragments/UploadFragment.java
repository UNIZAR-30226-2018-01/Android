package com.f22labs.instalikefragmenttransaction.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.f22labs.instalikefragmenttransaction.R;
import com.f22labs.instalikefragmenttransaction.activities.MainActivity;

import butterknife.ButterKnife;


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
                Intent intent = new Intent();
                intent.setType("music/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Music"), PICK_MUSIC);

            }
        });
        confirmButton =  (Button) view.findViewById(R.id.btn_confirm_list);

        ButterKnife.bind(this, view);

        ( (MainActivity)getActivity()).updateToolbarTitle("Upload");



        return view;
    }


}
