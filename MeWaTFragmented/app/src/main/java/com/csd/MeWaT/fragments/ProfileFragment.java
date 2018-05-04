package com.csd.MeWaT.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ProfileFragment extends BaseFragment{


    @BindView(R.id.username)
    TextView username;

    @BindView(R.id.user_image)
    ImageView user_image;

    @BindView(R.id.settingsButton)
    ImageButton settingsButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);


        username.setText(MainActivity.user);

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


}
