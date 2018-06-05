package com.example.recordvoice;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recordvoice.constant.Constants;
import com.example.recordvoice.fragment.CloudFragment;
import com.example.recordvoice.fragment.RecordFragment;
import com.example.recordvoice.service.RecordService;
import com.example.recordvoice.vending.util.IabHelper;
import com.example.recordvoice.vending.util.IabResult;
import com.example.recordvoice.vending.util.Inventory;
import com.example.recordvoice.vending.util.Purchase;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener, EasyPermissions.PermissionCallbacks {

    private SwitchCompat switchCompat;
    private SharedPreferences sharedPreferences;
    private SharedPreferences deleteSharePreferences;
    private Toolbar toolbar;
    private TextView title;
    private static final String KEY_IS_FOLDER = "is_create_folder";

   private AppPreferences preferences;

    //Admob
    private InterstitialAd interstitialAd;
    private AdRequest adRequestFull;

    static final int REQUEST_ACCOUNT_PICKER = 901;
    static final int REQUEST_AUTHORIZATION = 902;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 903;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 904;

    private static final String BUTTON_TEXT = "Call Drive API";
    private static final String PREF_ACCOUNT_NAME = "accountName12";
    private static final String[] SCOPES = {DriveScopes.DRIVE};
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    public static com.google.api.services.drive.Drive mService = null;
    public static String KEY_ID_PARENT = "idParentFolder";
    public static String KEY_IS_BUY = "buy";

    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        checkPermissionForSDK();

        preferences = AppPreferences.getInstance(this);

        sharedPreferences = getSharedPreferences(Constants.LISTEN_ENABLED, MODE_PRIVATE);

        deleteSharePreferences = getSharedPreferences(Constants.DELETE_PREFERENCE, MODE_PRIVATE);
        deleteSharePreferences.registerOnSharedPreferenceChangeListener(this);

        switchCompat = (SwitchCompat) findViewById(R.id.onOff);
        switchCompat.setOnClickListener(this);
        switchCompat.setChecked(!sharedPreferences.getBoolean("silentMode", false));

//        boolean silentMode = sharedPreferences.getBoolean("silentMode",false);
//        Intent myIntent = new Intent(MainActivity.this, RecordService.class);
//        Log.d("LogMain",silentMode+"");
//        myIntent.putExtra("commandType",
//                silentMode ? Constants.RECORDING_DISABLED
//                        : Constants.RECORDING_ENABLED);
//        Log.d("LogMain",silentMode+"/m");
//        myIntent.putExtra("silentMode", silentMode);
//        MainActivity.this.startService(myIntent);
        //switchCompat.callOnClick();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (TextView) findViewById(R.id.title);
        setSupportActionBar(toolbar);
//        defaultToolbar();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        setContentMain(R.id.nav_allrecord);

        //admob
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.app_id));
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.ad_unit_full));
        requestNewInterstitial();

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                requestNewInterstitial();
            }
        });
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Drive API ...");

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        if (sharedPreferences.getBoolean(KEY_IS_BUY, false)) {
            getResultsFromApi();
        }

        String base64EncodedPublicKey = getResources().getString(R.string.key_billing);

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d("Log", "In-app Billing setup failed: " +
                            result);
                } else {
                    Log.d("Log", "In-app Billing is set up OK");
                }
            }
        });


    }


    @Override
    public void onBackPressed() {
        showFullAds();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        finish();
    }

    IabHelper mHelper;
    String ITEM_SKU = "remove.ads.yo.music";
    //String ITEM_SKU = "android.test.purchased";

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
            }

        }
    };

    public void consumeItem() {
        try {
            mHelper.queryInventoryAsync(mReceivedInventoryListener);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
            if (result.isFailure()) {
                // Handle failure
            } else {
                try {
                    mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                            mConsumeFinishedListener);
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        //hideMenu();
                     //   Toast.makeText(MainActivity.this, "Buy success", Toast.LENGTH_SHORT).show();

                        if (preferences.getToastStatus()) {
                            Toasty.custom(MainActivity.this, "Buy Success", getResources().getDrawable(R.drawable.ic_check_circle_black_24dp),
                                    Color.GREEN, Toast.LENGTH_SHORT, true, true).show();
                        }
                        getResultsFromApi();
                        sharedPreferences.edit().putBoolean(KEY_IS_BUY, true).apply();
                    } else {
                        // handle error
                    }
                }
            };


    private void buyCloudStorage() {


        String payload = "123";
        try {
            mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,
                    mPurchaseFinishedListener, "mypurchasetoken");
        } catch (IabHelper.IabAsyncInProgressException e) {
//            complain("Error launching purchase flow. Another async operation in progress.");

        }

    }

    Menu optionsMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchCompat.setChecked(!sharedPreferences.getBoolean("silentMode", false));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    boolean permissionReadContacts;
    boolean permissionInternet;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (R.id.action_about == id) {
            AboutDialog.show(this);
            return true;
        }

        if (R.id.action_exclude_list == id) {
                Intent intent = new Intent(this, WhitelistActivity.class);
                startActivity(intent);
            return true;
        }


/*
        if (id == R.id.action_save) {
            if (null != selectedItems && selectedItems.length > 0) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        for (PhoneCallRecord record : selectedItems) {
                            record.getPhoneCall().setKept(true);
                            record.getPhoneCall().save(MainActivity.this);
                        }
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(LocalBroadcastActions.NEW_RECORDING_BROADCAST)); // Causes refresh

                    }
                };
                handler.post(runnable);
            }
            return true;
        }

        if (id == R.id.action_share) {
            if (null != selectedItems && selectedItems.length > 0) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Uri> fileUris = new ArrayList<Uri>();
                        for (PhoneCallRecord record : selectedItems) {
                            fileUris.add(Uri.fromFile(new java.io.File(record.getPhoneCall().getPathToRecording())));
                        }
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_title));
                        shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, Html.fromHtml(getString(R.string.email_body_html)));
                        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body));
                        shareIntent.setType("audio/*");
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
                    }
                };
                handler.post(runnable);
            }
            return true;
        }

        if (id == R.id.action_delete) {
            if (null != selectedItems && selectedItems.length > 0) {
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        this);
                alert.setTitle(R.string.delete_recording_title);
                alert.setMessage(R.string.delete_recording_subject);
                alert.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Database callLog = Database.getInstance(MainActivity.this);
                                for (PhoneCallRecord record : selectedItems) {
                                    int id = record.getPhoneCall().getId();
                                    callLog.removeCall(id);
                                }

                                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(LocalBroadcastActions.RECORDING_DELETED_BROADCAST));
                            }
                        };
                        handler.post(runnable);

                        dialog.dismiss();

                    }
                }
                );
                alert.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();
            }
            return true;
        }

        if (id == R.id.action_delete_all) {

            AlertDialog.Builder alert = new AlertDialog.Builder(
                    this);
            alert.setTitle(R.string.delete_recording_title);
            alert.setMessage(R.string.delete_all_recording_subject);
            alert.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Database.getInstance(MainActivity.this).removeAllCalls(false);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(LocalBroadcastActions.RECORDING_DELETED_BROADCAST));
                        }
                    };
                    handler.post(runnable);

                    dialog.dismiss();

                }
            });
            alert.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });

            alert.show();

            return true;
        }


        if (R.id.action_about == id) {
            AboutDialog.show(this);
            return true;
        }

*/
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        setContentMain(id);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setContentMain(int id) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();

        if (id == R.id.nav_allrecord) {
            bundle.putString("type", Constants.ALL_RECORD);
            fragment = new RecordFragment();
            fragment.setArguments(bundle);
        } else if (id == R.id.nav_call_in) {
            bundle.putString("type", Constants.CALL_IN_RECORD);
            fragment = new RecordFragment();
            fragment.setArguments(bundle);
        } else if (id == R.id.nav_call_out) {
            bundle.putString("type", Constants.CALL_OUT_RECORD);
            fragment = new RecordFragment();
            fragment.setArguments(bundle);
        } else if (id == R.id.nav_share) {
            share();
        } else if (id == R.id.nav_rate) {
            rate();
        } else if (id == R.id.cloud) {
            buyCloudStorage();
            //getResultsFromApi();
            fragment = new CloudFragment();
        } else if (id == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
    }

    private void connectToGoogle() {
        // Initialize credentials and service object.
        new Thread(new Runnable() {
            @Override
            public void run() {
                //getResultsFromApi();
            }
        }).start();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.onOff:
                Log.d("LogMainClick", "click");
                boolean silentMode = sharedPreferences.getBoolean("silentMode", false);

              //  SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
                boolean switchState = preferences.getToastStatus();

                if (!silentMode) {
                  //  Toast.makeText(this, "Disable Record Call", Toast.LENGTH_LONG).show();

                    if (switchState){
                    Toasty.custom(MainActivity.this, "Call Record  Disabled", getResources().getDrawable(R.drawable.disable),
                            getResources().getColor(R.color.Yellow), Toast.LENGTH_LONG, true, true).show();

                }else {

                    }
                }
                else {
                    if (switchState) {
                        Toasty.custom(MainActivity.this, "Call Record  Enabled", getResources().getDrawable(R.drawable.enable),
                                getResources().getColor(R.color.Green), Toast.LENGTH_SHORT, true, true).show();

                    }
                }

                sharedPreferences.edit().putBoolean("silentMode", !silentMode).apply();
                preferences.setToastStatus(switchState);

                Intent myIntent = new Intent(MainActivity.this, RecordService.class);

                Log.d("LogMainClick", silentMode + "");

                myIntent.putExtra("commandType",
                        !silentMode ? Constants.RECORDING_DISABLED : Constants.RECORDING_ENABLED);

                Log.d("LogMainClick", silentMode + "/m");

                myIntent.putExtra("silentMode", silentMode);
                MainActivity.this.startService(myIntent);
        }
    }

    private void share() {
        if (hasConnection()) {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String shareBody = "https://play.google.com/store/apps/details?id=" + getPackageName();
                intent.putExtra(Intent.EXTRA_SUBJECT, "Share app");
                intent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(intent, "Chia sẻ"));
            } catch (Exception e) {
                e.getMessage();
            }
        } else {
                Toasty.custom(MainActivity.this, "No Network Connection", getResources().getDrawable(R.drawable.no_network),
                    Color.RED, Toast.LENGTH_SHORT, true, true).show();
        }
    }

    public boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        }

        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }
        return false;
    }

    private void rate() {
        if (hasConnection()) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        } else {
            Toasty.custom(MainActivity.this, "No Network Connection", getResources().getDrawable(R.drawable.no_network),
                    Color.RED, Toast.LENGTH_SHORT, true, true).show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d("LogMain", "1");
        Map<String, ?> allEntries = deleteSharePreferences.getAll();
        boolean isCheck = false;
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (Boolean.parseBoolean(entry.getValue().toString())) {
                isCheck = true;
                break;
            }
        }
        if (isCheck) {
            changeToolBar();
        } else {
            defaultToolbar();
        }

    }

    public void changeToolBar() {
        title.setText("Selected");
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_keyboard_backspace_white_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void defaultToolbar() {
        title.setText("Record Call");
//        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.home));
        setSupportActionBar(toolbar);
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            //mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
//                    /mOutputText.setText(
//                            "This app requires Google Play Services. Please install " +
//                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Drive API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Drive API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         *
         * @return List of Strings describing files, or an empty list if no files
         * found.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> fileInfo = new ArrayList<String>();
            FileList result = mService.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null) {
                for (File file : files) {
                    fileInfo.add(String.format("%s (%s)\n",
                            file.getName(), file.getId()));
                }
            }
            if (sharedPreferences.getString(KEY_ID_PARENT, "").equals("")) {
                createFolder();
            }

            return fileInfo;
        }

        private void createFolder() {
            try {
                File fileMetadata = new File();
                fileMetadata.setName(Constants.GoogleDriveFolder);
                fileMetadata.setMimeType("application/vnd.google-apps.folder");

                File file = mService.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
                //sharedPreferences.edit().putBoolean(KEY_IS_FOLDER,true).apply();
                sharedPreferences.edit().putString(KEY_ID_PARENT, file.getId()).apply();
            } catch (Exception e) {
                e.getMessage();
            }
        }


        @Override
        protected void onPreExecute() {
            //mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                //mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the Drive API:");
                //mOutputText.setText(TextUtils.join("\n", output));
                Log.d("LogMain", output.get(1));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    //mOutputText.setText("The following error occurred:\n"
                    // + mLastError.getMessage());
                }
            } else {
                // mOutputText.setText("Request cancelled.");
            }
        }
    }

    private void requestNewInterstitial() {
        adRequestFull = new AdRequest.Builder()
                .addTestDevice("5DE009358208E67E37FD2A7F7661044A")
                .build();

        interstitialAd.loadAd(adRequestFull);
    }

    private void showFullAds() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    private void checkPermissionForSDK() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionWrite = ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Log.e("LogPermission", permissionWrite + "/m");
            int permissionInternet = ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.INTERNET);
            Log.e("LogPermission", permissionWrite + "/m");
            int permerssionRecordAudio = ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO);
            Log.e("LogPermission", permerssionRecordAudio + "/m");
            int permerssionProcessOut = ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.PROCESS_OUTGOING_CALLS);
            Log.e("LogPermission", permerssionProcessOut + "/m");
            int permissionReadContact = ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_CONTACTS);
            Log.e("LogPermission", permissionReadContact + "/m");
            int permissionReadCallLog = ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_CALL_LOG);
            Log.e("LogPermission", permissionReadContact + "/m");
            int permissionReadPhoneState = ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_PHONE_STATE);
            Log.e("LogPermission", permissionReadPhoneState + "/m");
            int permissionModifyAudio = ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS);
            Log.e("LogPermission", permissionModifyAudio + "/m");
            ArrayList<String> list = new ArrayList<>();

            if (permissionWrite != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else{
                //TODO: permission alert
            }
            if (permerssionRecordAudio != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.RECORD_AUDIO);
            }
            if (permerssionProcessOut != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
            }
            if (permissionReadContact != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.READ_CONTACTS);
            }
            if (permissionReadCallLog != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.READ_CALL_LOG);
            }
            if (permissionReadPhoneState != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (permissionModifyAudio != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
            }
            if (permissionModifyAudio != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.INTERNET);
            }
            if (list.size() > 0) {
                ActivityCompat.requestPermissions(MainActivity.this, list.toArray(new String[list.size()]),
                        Constants.MULTI_PERMISSION);
            }
        }
    }

}
