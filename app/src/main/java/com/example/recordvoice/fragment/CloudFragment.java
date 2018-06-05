package com.example.recordvoice.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.recordvoice.MainActivity;
import com.example.recordvoice.Model.CloudRecord;
import com.example.recordvoice.R;
import com.example.recordvoice.adapter.CloudFragmentAdapter;
import com.example.recordvoice.constant.Constants;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.ArrayList;
import java.util.List;


public class CloudFragment extends Fragment{
    private RecyclerView recyclerView;
    private CloudFragmentAdapter cloudFragmentAdapter;
    private ArrayList<CloudRecord> cloudRecordArrayList;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cloud_fragment,container,false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cloudRecordArrayList = new ArrayList<>();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("LogMain","CloudResume");
        new LoadAllCloudFile().execute();
    }

    public class LoadAllCloudFile extends AsyncTask<Void,Void,Void>{
        List<String> fileInfo;

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                fileInfo = new ArrayList<String>();
                SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.LISTEN_ENABLED, Context.MODE_PRIVATE);
                String idparent = sharedPreferences.getString(MainActivity.KEY_ID_PARENT,"");
                FileList result = MainActivity.mService.files().list()
                        .setFields("nextPageToken, files(id, name, parents, size, createdTime)")
                        .execute();

                List<File> files = result.getFiles();
                if (files != null) {
                    for (File file : files) {
                        if(file.getParents().toString().replace("[","").replace("]","").equals(idparent)){
//                            fileInfo.add(String.format("%s (%s)\n",
//                                    file.getName(), file.getId()));
                            CloudRecord cloudRecord = new CloudRecord(file.getName(),file.getSize());
                            cloudRecordArrayList.add(cloudRecord);
                        }
                    }
                }

            }catch (Exception e){
                e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try{
                //Log.d("LogMain123",fileInfo.get(0));
                CloudFragmentAdapter cloudFragmentAdapter = new CloudFragmentAdapter(getContext(),cloudRecordArrayList);
                recyclerView.setAdapter(cloudFragmentAdapter);
            }catch (Exception e){
                e.getMessage();
            }
            super.onPostExecute(aVoid);
        }
    }

}