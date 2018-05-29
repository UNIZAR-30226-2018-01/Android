package com.csd.MeWaT.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.csd.MeWaT.R;
import com.csd.MeWaT.utils.CustomAdapterUsers;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserListFragment extends BaseFragment {

    @BindView(R.id.user_listview)
    GridView user_listView;

    private static ArrayList<HashMap<String,String>> listAdapterUser =new ArrayList<HashMap<String,String>>();
    CustomAdapterUsers adapterUser;

    public static UserListFragment newInstance(ArrayList<HashMap<String,String>> instance) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_INSTANCE, instance);
        listAdapterUser = instance;
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

    }
}
