package com.csd.MeWaT.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.csd.MeWaT.R;
import com.csd.MeWaT.activities.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SocialFragment extends BaseFragment{



    @BindView(R.id.btn_click_me)
    Button btnClickMe;

    int fragCount;


    public static SocialFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(ARGS_INSTANCE, instance);
        SocialFragment fragment = new SocialFragment();
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

        View view = inflater.inflate(R.layout.fragment_social, container, false);

        ButterKnife.bind(this, view);

        Bundle args = getArguments();
        if (args != null) {
            fragCount = args.getInt(ARGS_INSTANCE);
        }



        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        btnClickMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragmentNavigation != null) {
                    mFragmentNavigation.pushFragment(SocialFragment.newInstance(fragCount + 1));


                }
            }
        });


        ( (MainActivity)getActivity()).updateToolbarTitle((fragCount == 0) ? "News" : "Sub News "+fragCount);


    }
}
