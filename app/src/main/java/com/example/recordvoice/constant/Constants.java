/*
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
package com.example.recordvoice.constant;

public class Constants {

	public static final String TAG = "Call recorder1: ";

	public static final String FILE_DIRECTORY = "CallRecords";
	public static final String LISTEN_ENABLED = "ListenEnabled";
	public static final String DELETE_PREFERENCE = "deletePreferences";
	public static final String FILE_NAME_PATTERN = "^d[\\d]{14}p[_\\d](.*)*\\.3gp$";

	public static final int MEDIA_MOUNTED = 0;
	public static final int MEDIA_MOUNTED_READ_ONLY = 1;
	public static final int NO_MEDIA = 2;

	public static final int STATE_INCOMING_NUMBER = 1;
	public static final int STATE_CALL_START = 2;
	public static final int STATE_CALL_END = 3;
	public static final int STATE_START_RECORDING = 4;
	public static final int STATE_STOP_RECORDING = 5;
	public static final int RECORDING_ENABLED = 6;
	public static final int RECORDING_DISABLED = 7;

	public static final int RECORDING_INCOMING_ENABLED = 8;
	public static final int RECORDING_INCOMING_DISABLED = 9;

	public static final int RECORDING_OUTGOING_ENABLED = 10;
	public static final int RECORDING_OUTGOING_DISABLED = 10;

	public static final String IN_CALL = "in";
	public static final String OUT_CALL = "out";

	public static final String ALL_RECORD = "all_record";
	public static final String CALL_IN_RECORD = "call_in_record";
	public static final String CALL_OUT_RECORD = "call_out_record";

	public static final int MULTI_PERMISSION = 1001;

	public static final String GoogleDriveFolder = "RecordCall";

}
