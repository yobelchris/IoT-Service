package com.adroitdevs.adroitapps.adroitiotservice;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adroitdevs.adroitapps.adroitiotservice.adapter.DeviceAdapter;
import com.adroitdevs.adroitapps.adroitiotservice.model.Device;

import java.util.ArrayList;

public class StatusFragment extends Fragment {
    IListener mListener;
    DeviceAdapter mAdapter;
    ArrayList<Device> mList = new ArrayList<>();

    // TODO: Rename and change types of parameters

    public StatusFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_status, container, false);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.listDevice);
        LinearLayoutManager lm = new LinearLayoutManager(this.getContext());
        rv.setLayoutManager(lm);
        mAdapter = new DeviceAdapter(this.getContext(), mList);
        rv.setAdapter(mAdapter);
        fillData();
        return view;

    }

    private void fillData() {
        Resources rs = getResources();
        String[] arDevice = rs.getStringArray(R.array.places);
        String[] arDeskripsi = rs.getStringArray(R.array.place_desc);
        for (int i = 0; i < arDevice.length; i++) {
            mList.add(new Device(arDevice[i], arDeskripsi[i]));
        }
        mAdapter.notifyDataSetChanged();
    }

    interface IListener {
        void changeFragments();
    }


}
