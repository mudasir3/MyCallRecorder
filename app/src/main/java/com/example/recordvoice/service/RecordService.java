/*
 *  Copyright 2012 Kobi Krasnoff
 * 
 * This file is part of Call recorder For Android.

    Call recorder For Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Call recorder For Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Call recorder For Android.  If not, see <http://www.gnu.org/licenses/>
 */
package com.example.recordvoice.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.recordvoice.AppPreferences;
import com.example.recordvoice.DialogConfirmActivity;
import com.example.recordvoice.FileHelper;
import com.example.recordvoice.MainActivity;
import com.example.recordvoice.R;
import com.example.recordvoice.constant.Constants;
import com.example.recordvoice.database.DatabaseHandle;
import com.example.recordvoice.database.RecordCall;

import es.dmoral.toasty.Toasty;

public class RecordService extends Service {

    private MediaRecorder recorder = null;
    private String phoneNumber = null;

    private String fileName;
    private boolean onCall = false;
    private boolean recording = false;
    private boolean silentMode = false;
    private boolean onForeground = false;
    private String type = "";
    public static AudioManager audioManager;

    private AppPreferences preferences;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferences = AppPreferences.getInstance(this);


        Log.d(Constants.TAG, "RecordService onStartCommand");
        if (intent != null) {
            int commandType = intent.getIntExtra("commandType", 0);
            Log.d("LogMain", commandType + "");
            if (commandType != 0) {
                if (commandType == Constants.RECORDING_ENABLED) {
                    Log.d(Constants.TAG, "RecordService RECORDING_ENABLED");
                    silentMode = intent.getBooleanExtra("silentMode", true);
                    if (!silentMode && phoneNumber != null && onCall
                            && !recording)
                        commandType = Constants.STATE_START_RECORDING;

                } else if (commandType == Constants.RECORDING_DISABLED) {
                    Log.d(Constants.TAG, "RecordService RECORDING_DISABLED");
                    silentMode = intent.getBooleanExtra("silentMode", true);
                    if (onCall && phoneNumber != null && recording)
                        commandType = Constants.STATE_STOP_RECORDING;
                }

                if (commandType == Constants.STATE_INCOMING_NUMBER) {
                    Log.d(Constants.TAG, "RecordService STATE_INCOMING_NUMBER");
                    if (phoneNumber == null)
                        phoneNumber = intent.getStringExtra("phoneNumber");
                    Log.d("LogMainphone", phoneNumber + "/m");

                    silentMode = intent.getBooleanExtra("silentMode", true);

                    if (!silentMode && !(phoneNumber.charAt(0) == '*')) {
                        startService();
                    }
                    type = intent.getStringExtra("type");

                } else if (commandType == Constants.STATE_CALL_START) {
                    Log.d(Constants.TAG, "RecordService STATE_CALL_START");
                    onCall = true;
                    Log.d("LogMain", silentMode + " " + phoneNumber + " " + recording + " /,m");
                    if (!silentMode && phoneNumber != null && onCall
                            && !recording && !(phoneNumber.charAt(0) == '*')) {
                        Log.d(Constants.TAG, "RecordService STATE_CALL_START1");
                        startService();
                        startRecording(intent);
                    }
                } else if (commandType == Constants.STATE_CALL_END) {
                    Log.d(Constants.TAG, "RecordService STATE_CALL_END");
                    onCall = false;
                    phoneNumber = null;
                    stopAndReleaseRecorder();
                    recording = false;
                    stopService();
                } else if (commandType == Constants.STATE_START_RECORDING) {
                    Log.d(Constants.TAG, "RecordService STATE_START_RECORDING");
                    if (!silentMode && phoneNumber != null && onCall) {
                        startService();
                        startRecording(intent);
                    }
                } else if (commandType == Constants.STATE_STOP_RECORDING) {
                    Log.d(Constants.TAG, "RecordService STATE_STOP_RECORDING");
                    stopAndReleaseRecorder();
                    recording = false;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * in case it is impossible to record
     */
    private void terminateAndEraseFile() {
        Log.d(Constants.TAG, "RecordService terminateAndEraseFile");
        stopAndReleaseRecorder();
        recording = false;
        deleteFile();
    }

    private void stopService() {
        Log.d(Constants.TAG, "RecordService stopService");
        stopForeground(true);
        onForeground = false;
        this.stopSelf();
    }

    private void deleteFile() {
        Log.d(Constants.TAG, "RecordService deleteFile");
        FileHelper.deleteFile(fileName);
        fileName = null;
    }

    private void stopAndReleaseRecorder() {
        if (recorder == null)
            return;
        Log.d(Constants.TAG, "RecordService stopAndReleaseRecorder");
        boolean recorderStopped = false;
        boolean exception = false;

        try {
            recorder.stop();
            recorderStopped = true;
        } catch (IllegalStateException e) {
            Log.e(Constants.TAG, "IllegalStateException");
            e.printStackTrace();
            exception = true;
        } catch (RuntimeException e) {
            Log.e(Constants.TAG, "RuntimeException");
            exception = true;
        } catch (Exception e) {
            Log.e(Constants.TAG, "Exception");
            e.printStackTrace();
            exception = true;
        }
        try {
            recorder.reset();
        } catch (Exception e) {
            Log.e(Constants.TAG, "Exception");
            e.printStackTrace();
            exception = true;
        }
        try {
            recorder.release();
        } catch (Exception e) {
            Log.e(Constants.TAG, "Exception");
            e.printStackTrace();
            exception = true;
        }

        recorder = null;
        if (exception) {
            deleteFile();
        } else {
            DatabaseHandle databaseHandle = new DatabaseHandle(this);
            RecordCall recordCall = FileHelper.getRecordCall();
            if (recordCall != null) {
                databaseHandle.addHistory(recordCall);
            }
        }
        if (recorderStopped) {
         //   Toast toast = Toast.makeText(this,
           //         this.getString(R.string.receiver_end_call),
             //       Toast.LENGTH_SHORT);
           // toast.show();

            preferences = AppPreferences.getInstance(this);

            if (preferences.getToastStatus())
            Toasty.custom(RecordService.this, "Record Call End", getResources().getDrawable(R.drawable.record_end_icon),
                    getResources().getColor(R.color.Yellow), Toast.LENGTH_LONG, true, true).show();

            //TODO: wnable, disable notifications
            showNoti();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(Constants.TAG, "RecordService onDestroy");
        stopAndReleaseRecorder();
        stopService();
        super.onDestroy();
    }

    private boolean startMediaRecorder(int audioSource) {
        recorder = new MediaRecorder();
        try {
            recorder.reset();
            recorder.setAudioSource(audioSource);
            recorder.setAudioSamplingRate(8000);
            recorder.setAudioEncodingBitRate(12200);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            fileName = FileHelper.getFilename(phoneNumber, type, getApplicationContext());
            recorder.setOutputFile(fileName);

            OnErrorListener errorListener = new OnErrorListener() {
                public void onError(MediaRecorder arg0, int arg1, int arg2) {
                    Log.e(Constants.TAG, "OnErrorListener " + arg1 + "," + arg2);
                    terminateAndEraseFile();
                }
            };
            recorder.setOnErrorListener(errorListener);

            OnInfoListener infoListener = new OnInfoListener() {
                public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
                    Log.e(Constants.TAG, "OnInfoListener " + arg1 + "," + arg2);
                    terminateAndEraseFile();
                }
            };
            recorder.setOnInfoListener(infoListener);

            recorder.prepare();
            // Sometimes prepare takes some time to complete
            Thread.sleep(2000);
            recorder.start();
            recording = true;
            return true;
        } catch (Exception e) {
            e.getMessage();
            return false;
        }
    }

    private void startRecording(Intent intent) {
        Log.d(Constants.TAG, "RecordService startRecording");
        boolean exception = false;


//		try {
//
//			Log.d(Constants.TAG, "RecordService recorderStarted");
//		} catch (IllegalStateException e) {
//			Log.e(Constants.TAG, "IllegalStateException");
//			e.printStackTrace();
//			exception = true;
//		} catch (IOException e) {
//			Log.e(Constants.TAG, "IOException");
//			e.printStackTrace();
//			exception = true;
//		} catch (Exception e) {
//			Log.e(Constants.TAG, "Exception");
//			e.printStackTrace();
//			exception = true;
//		}
        if (!startMediaRecorder(MediaRecorder.AudioSource.VOICE_CALL)) {
            if (startMediaRecorder(MediaRecorder.AudioSource.MIC)) {
                audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(3, audioManager.getStreamMaxVolume(3), 0);

                Intent intent1 = new Intent(getBaseContext(), DialogConfirmActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            } else {
                exception = true;
            }
        }

        if (exception) {
            terminateAndEraseFile();
        }

        if (recording) {
       //     Toast toast = Toast.makeText(this,
         //           this.getString(R.string.receiver_start_call),
           //         Toast.LENGTH_SHORT);
           // toast.show();

            if (preferences.getToastStatus())
            Toasty.custom(RecordService.this, "Record Call Start", getResources().getDrawable(R.drawable.record_start_icon),
                    getResources().getColor(R.color.Yellow), Toast.LENGTH_LONG, true, true).show();


        } else {
            if (preferences.getToastStatus())
            {
            Toast toast = Toast.makeText(this,
                    this.getString(R.string.record_impossible),
                    Toast.LENGTH_LONG);
            toast.show();
        }
        }
    }

    private void startService() {
        if (!onForeground) {
            Log.d(Constants.TAG, "RecordService startService");
            Intent intent = new Intent(this, MainActivity.class);
            // intent.setAction(Intent.ACTION_VIEW);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getBaseContext(), 0, intent, 0);

            //TODO: wnable, disable notifications

            if (preferences.getNotificationsStatus()){
            Notification notification = new NotificationCompat.Builder(
                    getBaseContext())
                    .setContentTitle(
                            this.getString(R.string.notification_title))
                    .setTicker(this.getString(R.string.notification_ticker))
                    .setContentText(this.getString(R.string.notification_text))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent).setOngoing(true)
                    .getNotification();

            notification.flags = Notification.FLAG_NO_CLEAR;

            startForeground(1337, notification);
            onForeground = true;
        }
        }
    }

    //show noti when finish record
    public void showNoti() {

        if (preferences.getNotificationsStatus()) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("New Recording")
                            .setContentText("A phone Call has been Recorded");
            Intent resultIntent = new Intent(this, MainActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(getBaseContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
        }
    }
}


