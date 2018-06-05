package com.example.recordvoice.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.recordvoice.Model.CloudRecord;
import com.example.recordvoice.R;

import java.util.ArrayList;

/**
 * Created by vieta on 18/11/2016.
 */
public class CloudFragmentAdapter extends  RecyclerView.Adapter<CloudFragmentAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<CloudRecord> cloudRecordArrayList;

    public CloudFragmentAdapter(Context context, ArrayList<CloudRecord> cloudRecordArrayList){
        this.context = context;
        this.cloudRecordArrayList = cloudRecordArrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cloud_fragment_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        CloudRecord cloudRecord = cloudRecordArrayList.get(position);
        String size = "Size: " + cloudRecord.getSize()/1000 +" KB";
        holder.tvName.setText(cloudRecord.getName());
        holder.tvSize.setText(size);
    }

    @Override
    public int getItemCount() {
        return cloudRecordArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView tvName, tvSize;

        public MyViewHolder(View v){
            super(v);
            tvName = (TextView) v.findViewById(R.id.tvName);
            tvSize = (TextView) v.findViewById(R.id.tvSize);
        }
    }
}
