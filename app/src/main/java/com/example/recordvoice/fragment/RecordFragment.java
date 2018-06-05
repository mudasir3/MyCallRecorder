package com.example.recordvoice.fragment;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.recordvoice.R;
import com.example.recordvoice.adapter.RecordAdapter;
import com.example.recordvoice.constant.Constants;
import com.example.recordvoice.database.DatabaseHandle;
import com.example.recordvoice.database.RecordCall;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;

/**
 * Created by vieta on 15/8/2016.
 */
public class RecordFragment extends Fragment {

    private List<RecordCall> list = null;
    private RecordAdapter recordAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String type;
    RecyclerView recyclerView;


    //admob
    private AdView adView;
    private AdRequest adRequest;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record,container,false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setNestedScrollingEnabled(false);
        type = getArguments().getString("type");
        adView = (AdView) view.findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().addTestDevice("5DE009358208E67E37FD2A7F7661044A").build();
        adView.loadAd(adRequest);

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        if(type.equals(Constants.ALL_RECORD)){
            MyAssyncTask myAssyncTask = new MyAssyncTask(Constants.ALL_RECORD);
            myAssyncTask.execute();
        }else if(type.equals(Constants.CALL_IN_RECORD)){
            MyAssyncTask myAssyncTask = new MyAssyncTask(Constants.CALL_IN_RECORD);
            myAssyncTask.execute();
        }else if(type.equals(Constants.CALL_OUT_RECORD)){
            MyAssyncTask myAssyncTask = new MyAssyncTask(Constants.CALL_OUT_RECORD);
            myAssyncTask.execute();
        }
    }

    public class MyAssyncTask extends AsyncTask<Void, Void, Void>{
        private ProgressDialog progressDialog;
        public String type;

        public MyAssyncTask(String type){
            this.type = type;
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Waiting for loading ...");
            progressDialog.setTitle("Finding Record");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DatabaseHandle databaseHandle = new DatabaseHandle(getContext());
            if(type==Constants.ALL_RECORD){
                list = databaseHandle.getAllRecord();
            }else if(type == Constants.CALL_IN_RECORD){
                list = databaseHandle.getAllRecordCondition(1);
            }else{
                list = databaseHandle.getAllRecordCondition(0);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            recordAdapter = new RecordAdapter(getContext(), list, type);
            recyclerView.setAdapter(recordAdapter);
        }
    }
}
