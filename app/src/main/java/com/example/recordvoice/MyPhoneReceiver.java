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
package com.example.recordvoice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.example.recordvoice.constant.Constants;
import com.example.recordvoice.database.CallLog;
import com.example.recordvoice.database.DatabaseHandle;
import com.example.recordvoice.service.RecordService;

import java.util.concurrent.atomic.AtomicBoolean;

public class MyPhoneReceiver extends BroadcastReceiver {

    private String phoneNumber;
    private CallLog phoneCall;

    AtomicBoolean isWhitelisted = new AtomicBoolean();

    @Override
    public void onReceive(Context context, Intent intent) {
        phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        String extraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        phoneCall = new CallLog();
//		Log.d(Constants.TAG, "MyPhoneReciever phoneNumber "+phoneNumber);
//		Log.d("LogReceiver",intent.getAction()+"/m");

        String type = Constants.IN_CALL;

        isWhitelisted.set(DatabaseHandle.isWhitelisted(context, phoneNumber));

        if (isWhitelisted.get()) {

        } else {
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                type = Constants.OUT_CALL;
                phoneCall.setPhoneNumber(phoneNumber);
                phoneCall.setOutgoing();
            }
            //if (MainActivity.updateExternalStorageState() == Constants.MEDIA_MOUNTED) {
            try {
                SharedPreferences settings = context.getSharedPreferences(
                        Constants.LISTEN_ENABLED, 0);
                boolean silent = settings.getBoolean("silentMode", false); //truue
                //Log.d("LogReceiver",silent+"/m");
                if (extraState != null) {
                    if (extraState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {

                        if (isWhitelisted.get()) {
                            isWhitelisted.set(false);
                            return;
                        }

                        Intent myIntent = new Intent(context,
                                RecordService.class);
                        myIntent.putExtra("commandType",
                                Constants.STATE_CALL_START);
                        context.startService(myIntent);
                    } else if (extraState
                            .equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        Intent myIntent = new Intent(context,
                                RecordService.class);
                        myIntent.putExtra("commandType",
                                Constants.STATE_CALL_END);
                        context.startService(myIntent);
                    } else if (extraState
                            .equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        if (phoneNumber == null)
                            phoneNumber = intent
                                    .getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        isWhitelisted.set(DatabaseHandle.isWhitelisted(context, phoneNumber));

                        Intent myIntent = new Intent(context,
                                RecordService.class);
                        myIntent.putExtra("type", type);
                        //Log.d("LogServiceRe",type);
                        myIntent.putExtra("commandType",
                                Constants.STATE_INCOMING_NUMBER);
                        myIntent.putExtra("phoneNumber", phoneNumber);
                        myIntent.putExtra("silentMode", silent);
                        context.startService(myIntent);
                    }
                } else if (phoneNumber != null) {
                    Intent myIntent = new Intent(context, RecordService.class);
                    myIntent.putExtra("commandType",
                            Constants.STATE_INCOMING_NUMBER);
                    myIntent.putExtra("phoneNumber", phoneNumber);
                    myIntent.putExtra("silentMode", silent);
                    myIntent.putExtra("type", type);
                    //Log.d("LogServiceRe",type);
                    context.startService(myIntent);
                }
            } catch (Exception e) {
                //Log.e(Constants.TAG, "Exception");
                e.printStackTrace();
            }
            //}
        }
    }
}
