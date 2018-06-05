package com.example.recordvoice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StatFs;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recordvoice.constant.Constants;
import com.example.recordvoice.database.CallLog;
import com.example.recordvoice.database.DatabaseHandle;
import com.example.recordvoice.service.CleanupService;
import com.example.recordvoice.service.RecordService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class SettingsActivity extends AppCompatActivity {

    private AppPreferences preferences;
    private Boolean silentMode;

    private Switch tswitch, notificationSwitch;
    private SharedPreferences sharedPreferences;
    private SwitchCompat switchCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = AppPreferences.getInstance(this);
        sharedPreferences = getSharedPreferences(Constants.LISTEN_ENABLED, MODE_PRIVATE);

        tswitch = findViewById(R.id.toastSwitch);
        notificationSwitch = findViewById(R.id.notificationSwitch);

        tswitch.setChecked(preferences.getToastStatus());
        notificationSwitch.setChecked(preferences.getNotificationsStatus());


        switchCompat = (SwitchCompat) findViewById(R.id.onOff);
       // switchCompat.setChecked(preferences.getToastStatus());
        switchCompat.setChecked(!sharedPreferences.getBoolean("silentMode", false));
        switchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (view.getId()) {
                    case R.id.onOff:
                        Log.d("LogMainClick", "click");
                       // SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
                      boolean silentMode = sharedPreferences.getBoolean("silentMode", false);
                        boolean switchState = preferences.getToastStatus();

                        if (!silentMode) {
                            //  Toast.makeText(this, "Disable Record Call", Toast.LENGTH_LONG).show();

                            if (switchState){
                                Toasty.custom(SettingsActivity.this, "Call Record  Disabled", getResources().getDrawable(R.drawable.disable),
                                        getResources().getColor(R.color.Yellow), Toast.LENGTH_LONG, true, true).show();

                            }
                       }
                        else {
                            if (switchState) {
                                Toasty.custom(SettingsActivity.this, "Call Record  Enabled", getResources().getDrawable(R.drawable.enable),
                                        getResources().getColor(R.color.Green), Toast.LENGTH_SHORT, true, true).show();

                            }
                        }

                        sharedPreferences.edit().putBoolean("silentMode", !silentMode).apply();



                        Intent myIntent = new Intent(SettingsActivity.this, RecordService.class);

                        Log.d("LogMainClick", silentMode + "");

                        myIntent.putExtra("commandType",
                                !silentMode ? Constants.RECORDING_DISABLED : Constants.RECORDING_ENABLED);

                        Log.d("LogMainClick", silentMode + "/m");

                        myIntent.putExtra("silentMode", silentMode);
                        SettingsActivity.this.startService(myIntent);
                }
            }
        });





        tswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (tswitch.isChecked()) {
                    preferences.setToastStatus(true);
                    tswitch.setChecked(preferences.getToastStatus());
                } else {
                    preferences.setToastStatus(false);
                    tswitch.setChecked(preferences.getToastStatus());
                }
            }
        });

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.setNotificationsStatus(b);
                notificationSwitch.setChecked(preferences.getNotificationsStatus());
            }
        });


  /*      CheckBox checkBox = findViewById(R.id.checkBox);
        checkBox.setChecked(preferences.isRecordingIncomingEnabled());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.setRecordingIncomingEnabled(isChecked);
            }
        });
        checkBox = findViewById(R.id.checkBox2);
        checkBox.setChecked(preferences.isRecordingOutgoingEnabled());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.setRecordingOutgoingEnabled(isChecked);


            }
        });
*/


        File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(this, null);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();
        ArrayList<Integer> icons = new ArrayList<>();

        File filesDir = getFilesDir();
        list.add(filesDir.getAbsolutePath());
        icons.add(R.drawable.ic_folder_black_24dp);

        for (File file : externalFilesDirs) {
            list.add(file.getAbsolutePath());
            icons.add(R.drawable.ic_cards_black_24);
        }
        final MyArrayAdapter<String> dataAdapter = new MyArrayAdapter<String>(this, list, icons);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               //TODO: Coming Soon
                Toasty.custom(SettingsActivity.this, "Coming Soon !", getResources().getDrawable(R.drawable.coming_soon),
                        Color.BLUE, Toast.LENGTH_LONG, true, true).show();

               /* String path = dataAdapter.getItem(position);
                calcFreeSpace(path);
                AppPreferences.getInstance(getApplicationContext()).setFilesDirectory(path);*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        String path = AppPreferences.getInstance(getApplicationContext()).getFilesDirectory().getAbsolutePath();
        spinner.setSelection(dataAdapter.getPosition(path.replace("/calls/", "")));
        calcFreeSpace(path);

        // Now, count the recordings
        ArrayList<CallLog> allCalls = DatabaseHandle.getInstance(getApplicationContext()).getAllCalls();
        TextView textView = (TextView) findViewById(R.id.textView4);
        String str = textView.getText().toString();
        str = String.format(str, allCalls.size());
        textView.setText(Html.fromHtml(str));

        // Get the length of each file...
        long length = 0;
        for (CallLog call : allCalls) {
            File file = new File(call.getPathToRecording());
            length += file.length();
        }
        textView = (TextView) findViewById(R.id.textView5);
        str = textView.getText().toString();
        str = String.format(str, length / 1024);
        textView.setText(Html.fromHtml(str));

        spinner = (Spinner) findViewById(R.id.spinner2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
             //TODO: Coming Soon!
                Toasty.custom(SettingsActivity.this, "Coming Soon !", getResources().getDrawable(R.drawable.coming_soon),
                        Color.BLUE, Toast.LENGTH_LONG, true, true).show();
                // Obviously <string-array name="pref_frequencies"> MUST be in the same order as AppPreferences.OlderThan enum
               // final AppPreferences.OlderThan olderThan = AppPreferences.OlderThan.values()[position];
              //  AppPreferences.getInstance(getApplicationContext()).setOlderThan(olderThan);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(AppPreferences.getInstance(getApplicationContext()).getOlderThan().ordinal());

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //TODO: Coming Soon!
                Toasty.custom(SettingsActivity.this, "Coming Soon !", getResources().getDrawable(R.drawable.coming_soon),
                        Color.BLUE, Toast.LENGTH_LONG, true, true).show();
                //  CleanupService.sartCleaning(SettingsActivity.this);
            }
        });
    }

    private void calcFreeSpace(String path) {
        // http://stackoverflow.com/questions/3394765/how-to-check-available-space-on-android-device-on-mini-sd-card
        StatFs stat = new StatFs(path);
        long bytesTotal = 0;
        long bytesAvailable = 0;
        float megAvailable = 0;
        long megTotalAvailable = 0;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesTotal = (long) stat.getBlockSizeLong() * (long) stat.getBlockCountLong();
            bytesAvailable = (long) stat.getBlockSizeLong() * (long) stat.getAvailableBlocksLong();
        } else {
            bytesTotal = (long) stat.getBlockSize() * (long) stat.getBlockCount();
            bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        }
        megAvailable = bytesAvailable / 1048576;
        megTotalAvailable = bytesTotal / 1048576;

        // Free Space
        TextView textView = (TextView) findViewById(R.id.textView6);
        String str = getString(R.string.pref_folder_total_folder_size);
        str = String.format(str, megAvailable);
        textView.setText(Html.fromHtml(str));
    }

    class MyArrayAdapter<T> extends ArrayAdapter<T> {

        ArrayList<Integer> icons;

        public MyArrayAdapter(Context context, List objects, ArrayList<Integer> icons) {
            super(context, android.R.layout.simple_spinner_item, objects);
            this.icons = icons;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(icons.get(position), 0, 0, 0);
            return view;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchCompat.setChecked(!sharedPreferences.getBoolean("silentMode", false));
    }

    @Override
    public void onStop() {
        final AppPreferences.OlderThan olderThan = AppPreferences.getInstance(this).getOlderThan();
        if (olderThan != AppPreferences.OlderThan.NEVER) {
            MyAlarmReceiver.setAlarm(SettingsActivity.this);
        } else {
            MyAlarmReceiver.cancleAlarm(SettingsActivity.this);
        }
        super.onStop();
    }
}
