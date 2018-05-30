package com.csd.MeWaT.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.csd.MeWaT.R;
import com.csd.MeWaT.utils.Album;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;


public class GenreListFragment extends BaseFragment {


    @BindView(R.id.GenreListView)
    GridView GenreListView;

    private ArrayList<String> resultGenreList= new ArrayList<>();
    private ArrayList<HashMap<String,String>> resultGenreAdapter =new ArrayList<HashMap<String,String>>();
    SimpleAdapter GenreAdapter;


    public GenreListFragment(){

    }


    public static GenreListFragment newInstance(ArrayList<String> instance){
        Bundle args = new Bundle();
        args.putSerializable(ARGS_INSTANCE, instance);
        GenreListFragment fragment = new GenreListFragment();
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
        View view = inflater.inflate(R.layout.fragment_genre_list, container, false);
        ButterKnife.bind(this, view);

        Bundle args = getArguments();
        if (args != null) {
            resultGenreList=((ArrayList<String>) args.getSerializable(ARGS_INSTANCE));
        }
        resultGenreAdapter.clear();
        for(int i = 0; i<4 && i<resultGenreList.size();i++){
            HashMap<String,String> temp = new HashMap<String,String>();
            temp.put("name",resultGenreList.get(i));
            resultGenreAdapter.add(temp);
        }

        GenreAdapter = new SimpleAdapter(view.getContext(), resultGenreAdapter,R.layout.list_row_genre,
                new String[]{"name"},
                new int[]{R.id.GenreName});
        GenreListView.setAdapter(GenreAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GenreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayList<String> res = new ArrayList<>();
                res.add(resultGenreList.get((int)l));
                if(mFragmentNavigation != null) {
                    mFragmentNavigation.pushFragment(SongListFragment.newInstanceGenre(res));
                }
            }
        });

    }


}
