package com.example.recordvoice.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recordvoice.FileHelper;
import com.example.recordvoice.Interface1;
import com.example.recordvoice.MainActivity;
import com.example.recordvoice.Model.Model;
import com.example.recordvoice.R;
import com.example.recordvoice.RecordPlayActivity;
import com.example.recordvoice.constant.Constants;
import com.example.recordvoice.database.DatabaseHandle;
import com.example.recordvoice.database.RecordCall;
import com.example.recordvoice.service.RecordService;
import com.google.api.client.http.FileContent;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

/**
 * Created by vieta on 15/8/2016.
 */
public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.MyViewHolder>{

    List<RecordCall> listRecord;
    Context context;
    String type;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView phone_number, time, length_record,date;
        public ImageView avatar, menu;
        public CardView mCard;
        public MyViewHolder(View view){
            super(view);
            phone_number = (TextView) view.findViewById(R.id.phone_number);
            time = (TextView) view.findViewById(R.id.time);
            length_record = (TextView) view.findViewById(R.id.length_record);
            date = (TextView) view.findViewById(R.id.date);
            avatar = (ImageView) view.findViewById(R.id.avatar);
            menu = (ImageView) view.findViewById(R.id.menu_cardview);
            mCard = (CardView) view.findViewById(R.id.mCard);
        }
    }

    public RecordAdapter(Context context, List<RecordCall> listRecord,String type){
        this.context =  context;
        this.listRecord = listRecord;
        this.type = type;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    ArrayList<String> list = new ArrayList<>();

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        String myDateStr = listRecord.get(position).getDate();
        SimpleDateFormat curFormater = new SimpleDateFormat("yyyyMMddhhmmss", Locale.ENGLISH);
        Date dateObj = new Date();
        try {
            dateObj = curFormater.parse(myDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String timeString = DateFormat.getTimeInstance(SimpleDateFormat.MEDIUM, Locale.UK).format(dateObj);
        String time = timeString.substring(0,5);
        holder.time.setText(time);
        String dateString = DateFormat.getDateInstance().format(dateObj);
        String date = dateString.split(",")[0];
        if(date.equals(getDateCurrent())){
            date = "Today";
        }
        holder.date.setText(date);
        String myPhone = listRecord.get(position).getPhoneNumber();

        int type = listRecord.get(position).getTypeCall();
        //Log.d("LogMain1",type+"");
        if(type==0){
            holder.avatar.setImageResource(R.drawable.ic_call_out);
            //holder.avatar.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_call_out));
            //Glide.with(context).load(R.drawable.call_out).into(new GlideDrawableImageViewTarget(holder.avatar));
        }else{
            holder.avatar.setImageResource(R.drawable.ic_call_in);
           // holder.avatar.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_call_in));
            //Glide.with(context).load(R.drawable.call_in).into(new GlideDrawableImageViewTarget(holder.avatar));
        }
        final String fileName = listRecord.get(position).getFileName();
        holder.phone_number.setText(myPhone);
        holder.length_record.setText(getDuration(fileName));
        //Log.d("LogMain12",fileName+"/m");
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mCard.setCardBackgroundColor(context.getResources().getColor(R.color.card_focus));
                showPopupMenu(view,fileName,position,holder);
            }
        });

        final String phoneNumber = myPhone;
        final String time_date =  time +" " + date;
        holder.mCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startPlayExternal(getCallName);
                startPlayRecord(phoneNumber,time_date,fileName);
            }
        });

//        holder.mCard.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                Log.d("LogMain","logMain");
//                holder.checkBox1.setVisibility(View.VISIBLE);
//                holder.checkBox1.setChecked(true);
//                return true;
//            }
//        });
        //list.add(myPhone);
    }

//    @Override
//    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        super.onAttachedToRecyclerView(recyclerView);
//        interface1.setList(list);
//    }

    public void startPlayRecord(String phoneNumber, String time_date, String videoName){
        Intent intent = new Intent(context, RecordPlayActivity.class);
        intent.putExtra("phone_number",phoneNumber);
        intent.putExtra("time",time_date);
        intent.putExtra("video_name",videoName);
        context.startActivity(intent);
    }


    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view, String filename, int position, final MyViewHolder holder) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_popup, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(filename,position));
        popup.show();
        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                holder.mCard.setCardBackgroundColor(context.getResources().getColor(R.color.card_no_focus));
               // Log.d("LogMain123","Log");
            }
        });
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private String filename;
        private int position;

        public MyMenuItemClickListener(String filename, int position) {
            this.filename = filename;
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.delete:
                    DeleteRecord(filename,position);
                    break;
                case R.id.play:
                    startPlayExternal(filename);
                    break;
                case R.id.upload:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            uploadFileToDrive(filename);
                        }
                    }).start();
                    break;
                default:
                   // Log.d("LogMain123","Log");
            }
            return false;
        }
    }

    private String getDuration(String charSequence){
        String filepath = FileHelper.getFilePath(context) + "/"
                + Constants.FILE_DIRECTORY;
        java.io.File file = new java.io.File(filepath, charSequence);
        Uri intentUri;
        String duration;

        if (file.exists())
            intentUri = Uri.parse("file://" + FileHelper.getFilePath(context) + "/"
                    + Constants.FILE_DIRECTORY + "/" + charSequence);
        else
            intentUri = Uri.parse("file://"
                + context.getFilesDir().getAbsolutePath() + "/"
                + Constants.FILE_DIRECTORY + "/" + charSequence);
        try{
            MediaPlayer  mediaPlayer = MediaPlayer.create(context,intentUri);
            duration= milliSecondsToTimer(mediaPlayer.getDuration());
        }catch (Exception e){
            e.getMessage();
            duration = "0:00";
        }
        return duration;
    }

    void DeleteRecord(final String fileName, final int position) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_text)
                .setPositiveButton(R.string.confirm_delete_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String filepath = FileHelper.getFilePath(context)
                                        + "/" + Constants.FILE_DIRECTORY;
                                java.io.File file = new java.io.File(filepath, fileName);

                                if (file.exists()) {
                                    DatabaseHandle databaseHandle = new DatabaseHandle(context);
                                    databaseHandle.delete(listRecord.get(position));
                                    file.delete();
                                    listRecord.remove(position);
                                    notifyDataSetChanged();
                                }
                            }
                        })
                .setNegativeButton(R.string.confirm_delete_no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        }).show();
    }

    void startPlayExternal(String charSequence) {
        String filepath = FileHelper.getFilePath(context) + "/"
                + Constants.FILE_DIRECTORY;
        java.io.File file = new java.io.File(filepath, charSequence);
        Uri intentUri;

        if (file.exists())
            intentUri = Uri.parse("file://" + FileHelper.getFilePath(context) + "/"
                    + Constants.FILE_DIRECTORY + "/" + charSequence);
        else
            intentUri = Uri.parse("file://"
                    + context.getFilesDir().getAbsolutePath() + "/"
                    + Constants.FILE_DIRECTORY + "/" + charSequence);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(intentUri, "audio/3gpp");
        context.startActivity(intent);
    }

    void uploadFileToDrive(String filename){
        String filepath = FileHelper.getFilePath(context) + "/"
                + Constants.FILE_DIRECTORY;
        String idParentFolder;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.LISTEN_ENABLED,Context.MODE_PRIVATE);
        idParentFolder = sharedPreferences.getString(MainActivity.KEY_ID_PARENT,"");
        if(idParentFolder.length()==0){
            idParentFolder = "0B1qtABEYwAJdelNvd25RTVc2MWs";
        }

        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(filename);
        fileMetadata.setMimeType("video/3gp");
        if(idParentFolder.length()>0){
            fileMetadata.setParents(Arrays.asList(idParentFolder));
        }

        java.io.File filePath = new java.io.File(filepath,filename);
        FileContent mediaContent = new FileContent("video/3gp", filePath);
        try{
            //Log.d("File ID: ", "yes");
            com.google.api.services.drive.model.File file = MainActivity.mService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

            // Toast.makeText(context,context.getString(R.string.upload_success),Toast.LENGTH_SHORT).show();

            Toasty.custom(context, "Record Call Start", context.getResources().getDrawable(R.drawable.record_start_icon),
                    context.getResources().getColor(R.color.Yellow), Toast.LENGTH_LONG, true, true).show();


            // Log.d("File ID: ", file.getId());
        }catch (Exception e){
            e.getMessage();
        }
    }

    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    private String getDateCurrent(){
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd",Locale.ENGLISH);
        Date date = new Date();
        String dateString = DateFormat.getDateInstance().format(date).split(",")[0];
        return dateString;
    }

    @Override
    public int getItemCount() {
        return listRecord.size();
    }
}
