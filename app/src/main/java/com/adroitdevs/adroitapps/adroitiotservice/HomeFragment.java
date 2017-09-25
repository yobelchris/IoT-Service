package com.adroitdevs.adroitapps.adroitiotservice;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.adroitdevs.adroitapps.adroitiotservice.adapter.DeviceAdapter;
import com.adroitdevs.adroitapps.adroitiotservice.adapter.RiwayatAdapter;
import com.adroitdevs.adroitapps.adroitiotservice.model.Device;
import com.adroitdevs.adroitapps.adroitiotservice.model.RiwayatJemur;
import com.adroitdevs.adroitapps.adroitiotservice.model.TokenPrefrences;
import com.adroitdevs.adroitapps.adroitiotservice.service.MyJobService;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements DeviceAdapter.IDeviceAdapter {
    private static final String URL = "http://192.168.88.59:3000/";
    IListener mListener;
    DeviceAdapter mAdapter;
    RiwayatAdapter riwayatAdapter;
    ArrayList<Device> mList = new ArrayList<>();
    ArrayList<RiwayatJemur> listJemur = new ArrayList<>();
    // TODO: Rename and change types of parameters
    ProgressDialog progressDialog;
    Context context;
    TextView listTitle, jemuranList, deviceList, hour, minute;
    RecyclerView rv;
    Gson gson = new Gson();
    String idJemuran = "";
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null && intent.hasExtra("id")) {
                idJemuran = intent.getStringExtra("id");
                Log.d("HomeFragment", idJemuran);
            }
            if (intent.getExtras() != null && intent.hasExtra("updateList")) {
                updateListRiwayat();
                Log.d("HomeFragment", "updateList");
            }
            updateTimer(intent);
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getContext();
        mListener = (IListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        listTitle = (TextView) view.findViewById(R.id.TextSub);
        jemuranList = (TextView) view.findViewById(R.id.listRiwayat);
        deviceList = (TextView) view.findViewById(R.id.listDevices);
        hour = (TextView) view.findViewById(R.id.hourTime);
        minute = (TextView) view.findViewById(R.id.minuteTime);
        LinearLayoutManager lm = new LinearLayoutManager(this.getContext());
        rv = (RecyclerView) view.findViewById(R.id.frame);
        rv.setLayoutManager(lm);
        fillData();
        mAdapter = new DeviceAdapter(this, mList);
        riwayatAdapter = new RiwayatAdapter(this, listJemur);
        rv.setAdapter(mAdapter);
        listTitle.setText("Devices");
        jemuranList.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBackroundWhite));
        jemuranList.setBackgroundResource(R.drawable.roun_rect_gray);
        deviceList.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGray));
        deviceList.setBackgroundResource(R.drawable.roun_rect_white);
        jemuranList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rv.setAdapter(null);
                rv.setAdapter(riwayatAdapter);
                jemuranList.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGray));
                jemuranList.setBackgroundResource(R.drawable.roun_rect_white);
                deviceList.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBackroundWhite));
                deviceList.setBackgroundResource(R.drawable.roun_rect_gray);
                listTitle.setText("Riwayat Jemur");
            }
        });

        deviceList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rv.setAdapter(null);
                rv.setAdapter(mAdapter);
                jemuranList.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBackroundWhite));
                jemuranList.setBackgroundResource(R.drawable.roun_rect_gray);
                deviceList.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGray));
                deviceList.setBackgroundResource(R.drawable.roun_rect_white);
                listTitle.setText("Devices");
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        Log.d("HomeFragment", "Resume");
        super.onResume();
        this.getActivity().registerReceiver(br, new IntentFilter(MyJobService.COUNTDOWN_BR));
    }

    @Override
    public void onPause() {
        this.getActivity().unregisterReceiver(br);
        Log.d("HomeFragment", "Pause");
        super.onPause();
    }

    @Override
    public void onStop() {
        try {
            this.getActivity().unregisterReceiver(br);
            Log.d("HomeFragment", "Stop");
        } catch (Exception ex) {

        }
        super.onStop();
    }

    public void updateTimer(Intent intent) {
        if (intent.getExtras() != null && intent.hasExtra("hours") && intent.hasExtra("minutes")) {
            int hours = intent.getIntExtra("hours", 0);
            int minutes = intent.getIntExtra("minutes", 0);
            hour.setText(String.valueOf(hours));
            minute.setText(String.valueOf(minutes));
        }
    }

    private void fillData() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Mengambil data");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        String url = URL + "profile";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("status")) {
                        JSONObject result = response.getJSONObject("result");
                        JSONObject profile = result.getJSONObject("profile");
                        mListener.setTextProfile(profile.getString("nama"), profile.getString("email"));
                        if (!result.isNull("devices")) {
                            JSONArray devices = result.getJSONArray("devices");
                            for (int i = 0; i < devices.length(); i++) {
                                JSONObject device = devices.getJSONObject(i);
                                mList.add(gson.fromJson(device.toString(), Device.class));
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                        if (!result.isNull("riwayat")) {
                            JSONArray riwayats = result.getJSONArray("riwayat");
                            for (int j = 0; j < riwayats.length(); j++) {
                                JSONObject riwayat = riwayats.getJSONObject(j);
                                listJemur.add(gson.fromJson(riwayat.toString(), RiwayatJemur.class));
                            }
                            riwayatAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getContext(), "Get data error", Toast.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error : " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Status", error.toString());
                if (!error.toString().equals("com.android.volley.TimeoutError")) {
                    if (error.networkResponse.statusCode == 401) {
                        TokenPrefrences.clearToken(getContext());
                        Intent intent = new Intent(getActivity(), SigninActivity.class);
                        progressDialog.dismiss();
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Connection Error : " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Connection Error : " + error.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> head = new HashMap<>();
                head.put("Content-Type", "application/json");
                head.put("Authorization", "Bearer " + TokenPrefrences.getToken(context));
                return head;
            }
        };
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(request);
    }

    private void updateStatus(final VolleyCallback callback) {
        String url = URL + "history";
        StringRequest request = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject res = new JSONObject(response);
                    Log.d("update", String.valueOf(res.getBoolean("status")));
                    callback.onSuccess(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("update", error.toString());
                callback.onSuccess(false);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> body = new HashMap<>();
                body.put("id", idJemuran);
                return body;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> head = new HashMap<>();
                head.put("Authorization", "Bearer " + TokenPrefrences.getToken(getContext()));
                return head;
            }
        };
        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    @Override
    public void status(final String stat, final int id, final VolleyCallback callback) {
        String url = URL + "update";
        final Device device = mList.get(id);
        progressDialog = new ProgressDialog(this.getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("ReqStat", stat);
                if (stat.equals("angkat") || stat.equals("Manual") || stat.equals("Off")) {
                    mListener.stopJob(new VolleyCallback() {
                        @Override
                        public void onSuccess(boolean result) {
                            if (result) {
                                updateStatus(new VolleyCallback() {
                                    @Override
                                    public void onSuccess(boolean result) {
                                        if (result) {
                                            updateListRiwayat();
                                        }
                                    }

                                    @Override
                                    public void onSuccessJsonObject(JSONObject result) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onSuccessJsonObject(JSONObject result) {

                        }
                    });
                    try {
                        getActivity().unregisterReceiver(br);
                    } catch (IllegalArgumentException ex) {
                        Log.e("HomeFragment", ex.toString());
                    }
                    hour.setText("0");
                    minute.setText("0");
                } else if (!stat.equals("On")) {
                    getActivity().registerReceiver(br, new IntentFilter(MyJobService.COUNTDOWN_BR));
                    mListener.startJob(device.device_id, new VolleyCallback() {
                        @Override
                        public void onSuccess(boolean result) {
                            if (result) {
                                updateListRiwayat();
                            }
                        }

                        @Override
                        public void onSuccessJsonObject(JSONObject result) {

                        }
                    });
                }
                progressDialog.dismiss();
                callback.onSuccess(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onSuccess(false);
                progressDialog.dismiss();
                Log.d("ReqStat", error.toString() + " " + stat);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> body = new HashMap<>();
                body.put("id", device.device_id);
                body.put("job", stat);
                return body;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> head = new HashMap<>();
                head.put("Conten-Type", "application/json");
                head.put("Authorization", "Bearer " + TokenPrefrences.getToken(context));
                return head;
            }
        };
        VolleySingleton.getInstance(this.getContext()).addToRequestQueue(request);
    }

    private void updateListRiwayat() {
        String url = URL + "history";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("status")) {
                        JSONArray result = response.getJSONArray("result");
                        listJemur = new ArrayList<>();
                        for (int j = 0; j < result.length(); j++) {
                            JSONObject riwayat = result.getJSONObject(j);
                            listJemur.add(gson.fromJson(riwayat.toString(), RiwayatJemur.class));
                        }
                        riwayatAdapter.refreshList(listJemur);
                    }
                } catch (JSONException e) {
                    Log.e("HomeFragment", e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> head = new HashMap<>();
                head.put("Conten-Type", "application/json");
                head.put("Authorization", "Bearer " + TokenPrefrences.getToken(context));
                return head;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    interface IListener {
        void setTextProfile(String nama, String email);

        void stopJob(VolleyCallback callback);

        void startJob(String id, VolleyCallback callback);
    }
}