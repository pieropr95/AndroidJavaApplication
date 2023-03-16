package test.pprivera.com.testapplicationtraccar3;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private final Context context = MainActivity.this;
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String DEFAULT_INTERVAL = "60";    // INTERVALO: 60 SEGUNDOS
    private static final String DEFAULT_DISTANCE = "0";
    private static final String DEFAULT_ANGLE = "0";
    private static final String DEFAULT_PROVIDER = "mixed";

    private static final int PERMISSIONS_REQUEST_LOCATION = 2;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 3;

    // Traccar API URL
    public static final String MY_URL = "http://track.csticloud.biz:8083";
    public static final String MY_URL_OSMAND = "http://track.csticloud.biz:5055";
    public static final String PATH_DEVICES = "/api/devices";
    public static final String PATH_GEOFENCES = "/api/geofences";
    public static final String PATH_POSITIONS = "/api/positions";
    public static final String PATH_SESSION = "/api/session";
    public static final String PATH_USERS = "/api/users";

    // Traccar Shared Preferences
    public static final String KEY_INT_DEVICE = "track_id";
    public static final String KEY_CRED = "cred";
    public static final String KEY_DEVICE = "id";
    public static final String KEY_URL = "url";
    public static final String KEY_INTERVAL = "interval";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_ANGLE = "angle";
    public static final String KEY_PROVIDER = "provider";
    public static final String KEY_STATUS = "status";

    public static SharedPreferences sharedPreferences;
    public static AlarmManager alarmManager;
    public static PendingIntent alarmIntent;

    public static String device_uniqueId = "";
    public static int device_id = 0;

    private SessionManager sessionManager;

    private ImageButton startButton;
    private ProgressBar startPB;
    private Boolean done = false;
    private Handler progressHandler = new Handler();
    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            int progress = startPB.getProgress();
            if(progress == 100) {
                done = true;
                progressHandler.removeCallbacks(this);
            } else {
                progressHandler.postDelayed(this, 0);
            }
        }
    };

    private Handler foundHandler = new Handler();
    private Runnable foundRunnable = new Runnable() {
        @Override
        public void run() {

            if(welcomeLayout.getVisibility() == View.VISIBLE) {
                welcomeLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.scale_down));
                welcomeLayout.setVisibility(View.INVISIBLE);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String loginMessage = sessionManager.isLoggedIn() ?
                        sessionManager.getUserDetails().get(SessionManager.KEY_NAME) : getString(R.string.welcome);
                loginText.setText(loginMessage.toUpperCase());
                loginText.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
                loginText.setVisibility(View.VISIBLE);

                startButton.startAnimation(AnimationUtils.loadAnimation(context, R.anim.scale_up));
                startButton.setVisibility(View.VISIBLE);
                startButton.setImageResource(R.drawable.ic_play_arrow);
                startButton.setBackground(ContextCompat.getDrawable(context, R.drawable.oval_green));
            }

            foundHandler.removeCallbacks(this);
        }
    };

    private Handler notFoundHandler = new Handler();
    private Runnable notFoundRunnable = new Runnable() {
        @Override
        public void run() {

            if(startButton.getVisibility() == View.VISIBLE) {
                Log.e(TAG, "Device Not Found");
                sharedPreferences.edit().putString(KEY_CRED, null).apply();
                sharedPreferences.edit().putBoolean(KEY_STATUS, false).apply();
                stopTrackingService();

                startPB.setVisibility(View.INVISIBLE);
                startButton.startAnimation(AnimationUtils.loadAnimation(context, R.anim.scale_down));
                startButton.setVisibility(View.INVISIBLE);
                startButton.setImageResource(R.drawable.ic_play_arrow);
                startButton.setBackground(ContextCompat.getDrawable(context, R.drawable.oval_green));

                loginText.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_out));
                loginText.setVisibility(View.INVISIBLE);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String welcomeMessage = sessionManager.isLoggedIn() ?
                        getString(R.string.welcome) + ", " +
                                sessionManager.getUserDetails().get(SessionManager.KEY_NAME) :
                        getString(R.string.welcome);
                welcomeTitle.setText(welcomeMessage);

                welcomeLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.scale_up));
                welcomeLayout.setVisibility(View.VISIBLE);
            } else {

                showClientAlertDialog(sharedPreferences.getString(KEY_CRED, null));
            }

            notFoundHandler.removeCallbacks(this);
        }
    };

    private RelativeLayout spinnerLayout;
    private RelativeLayout welcomeLayout;
    private TextView welcomeTitle;
    private TextView loginText;

    // INTERSTITAL ADS WILL SHOW EVERY 5 DISMISSES OF A DIALOG IN MANAGER ACTIVITY
    public static int interstitialCount = 0;
    public static InterstitialAd interstitialAd;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        MobileAds.initialize(context, getString(R.string.google_app_ad_id));
        AdView adView = (AdView) findViewById(R.id.adView);
        final AdRequest adRequest = new AdRequest.Builder()
                // NOTA - addTestDevice ocasiona que los iconos FAM Menu se rayen al animarse.
                // ZTE PIERO
                // .addTestDevice("C46C7AB9CEB260CA88BB81357F919B59")
                // Innova CSTI
                // .addTestDevice("0DEF3D0D728B4A7213944D43F486C6A1")
                .build();
        adView.loadAd(adRequest);
        Log.e(TAG, adView.getAdSize().toString());

        interstitialAd = new InterstitialAd(context);
        // REAL AD
        // interstitialAd.setAdUnitId(getString(R.string.google_ad_inter_id));
        // TEST AD
        interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        interstitialAd.loadAd(adRequest);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                interstitialAd.loadAd(adRequest);
            }
        });

        sessionManager = new SessionManager(context);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Init Shared Preferences
        if (!sharedPreferences.contains(KEY_DEVICE)) {
            device_uniqueId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            initPreferences();
            migrateLegacyPreferences(sharedPreferences);
        } else {
            device_id = sharedPreferences.getInt(KEY_INT_DEVICE, 0);
            device_uniqueId = sharedPreferences.getString(KEY_DEVICE, null);
        }
        Log.e("Device Unique ID", device_uniqueId);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AutostartReceiver.class), 0);

        if (sharedPreferences.getBoolean(MainActivity.KEY_STATUS, false)) {
            startTrackingService(true, false);
        }

        Button managerGPSButton = (Button) findViewById(R.id.managerButton);
        managerGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;

                if (!sessionManager.isLoggedIn())
                    intent = new Intent(context, LoginActivity.class);
                else intent = new Intent(context, ManagerActivity.class);

                startActivity(intent);
            }
        });

        /* DEBUG_PURPOSES
        TextView deviceTextView = (TextView) findViewById(R.id.deviceTextView);
        String deviceText = "Device ID: " + device_uniqueId;
        deviceTextView.setText(deviceText);
        */

        startButton = (ImageButton) findViewById(R.id.startButton);
        startPB = (ProgressBar) findViewById(R.id.startProgressBar);

        final ObjectAnimator animation = ObjectAnimator.ofInt(startPB, "progress", 100);
        animation.setDuration(2000);
        animation.setInterpolator(new LinearInterpolator());

        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        anim.setInterpolator(interpolator);

        spinnerLayout = (RelativeLayout) findViewById(R.id.spinnerLayout);
        welcomeLayout = (RelativeLayout) findViewById(R.id.welcomeLayout);
        welcomeTitle = (TextView) findViewById(R.id.welcomeTitle);
        loginText = (TextView) findViewById(R.id.loginText);

        if (!sharedPreferences.getBoolean(MainActivity.KEY_STATUS, false)) {
            startButton.setImageResource(R.drawable.ic_play_arrow);
            startButton.setBackground(ContextCompat.getDrawable(context, R.drawable.oval_green));
        } else {
            startButton.setImageResource(R.drawable.ic_stop);
            startButton.setBackground(ContextCompat.getDrawable(context, R.drawable.oval_red));
        }

        startButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    if (sharedPreferences.getBoolean(MainActivity.KEY_STATUS, false)) {
                        // STOP
                        startPB.setVisibility(View.VISIBLE);
                        animation.start();
                        progressHandler.postDelayed(progressRunnable, 0);
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {

                    if (!sharedPreferences.getBoolean(MainActivity.KEY_STATUS, false)) {
                        // START
                        // DEBUG_PURPOSES showToast("START");

                        startButton.setImageResource(R.drawable.ic_stop);
                        startButton.setBackground(ContextCompat.getDrawable(context, R.drawable.oval_red));
                        startPB.setVisibility(View.VISIBLE);
                        startTrackingService(true, false);
                        sharedPreferences.edit().putBoolean(MainActivity.KEY_STATUS, true).apply();
                        startButton.startAnimation(anim);

                    } else {
                        // STOP
                        animation.cancel();
                        startPB.setProgress(0);
                        startPB.setVisibility(View.INVISIBLE);
                        if (done) {
                            done = false;
                            // DEBUG_PURPOSES showToast("STOP");

                            startButton.setImageResource(R.drawable.ic_play_arrow);
                            startButton.setBackground(ContextCompat.getDrawable(context, R.drawable.oval_green));
                            stopTrackingService();
                            sharedPreferences.edit().putBoolean(MainActivity.KEY_STATUS, false).apply();
                            // startButton.startAnimation(anim);

                            if (interstitialAd.isLoaded()) interstitialAd.show();
                            else Log.d(TAG, "The interstitial wasn't loaded yet.");
                        }
                    }
                }
                return false;
            }
        });

        FirebaseCrash.log("MainActivity created");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "onResume");

        // Check if the intent contains an AppInvite and then process the referral information.
        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData data) {
                        if (data == null) {
                            Log.d(TAG, "getInvitation: no data");
                            return;
                        }

                        // Get the deep link
                        Log.d(TAG, "data: " + data);
                        Uri deepLink = data.getLink();

                        // Extract invite
                        FirebaseAppInvite invite = FirebaseAppInvite.getInvitation(data);
                        if (invite != null) {
                            String invitationId = invite.getInvitationId();
                            Log.d(TAG, "Invite: " + invite + ", Invitation ID: " + invitationId);
                        }

                        // Handle the deep link
                        Log.d(TAG, "deepLink: " + deepLink);
                        if(deepLink != null) {
                            // Get Path of Deep Link
                            Log.d(TAG, "deepLink Path: " + deepLink.getPath());
                            String receivedCred = deepLink.getPath().substring(1);

                            spinnerLayout.setVisibility(View.VISIBLE);
                            new GetDevicesTask().execute(receivedCred);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });

        if(sharedPreferences.getString(KEY_CRED, null) != null) {
            welcomeLayout.setVisibility(View.INVISIBLE);
            startButton.setVisibility(View.VISIBLE);

            MainActivity.GetDevicesTask task = new MainActivity.GetDevicesTask();
            String cred = sharedPreferences.getString(KEY_CRED, null);
            task.execute(cred);
        }

        String loginMessage = sessionManager.isLoggedIn() ?
                sessionManager.getUserDetails().get(SessionManager.KEY_NAME) : getString(R.string.welcome);
        String welcomeMessage = sessionManager.isLoggedIn() ?
                getString(R.string.welcome) + ", " + sessionManager.getUserDetails().get(SessionManager.KEY_NAME)
                : getString(R.string.welcome);

        // managerGPSButton.setText(loginMessage);
        welcomeTitle.setText(welcomeMessage);
        loginText.setText(loginMessage.toUpperCase());

        if(welcomeLayout.getVisibility() == View.VISIBLE) loginText.setVisibility(View.INVISIBLE);
        else loginText.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.traccarClient) {
            Intent intent = new Intent(context, StatusActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showToast(final String msg) {
        //gets the main thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // run this code in the main thread
                Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    protected void showSnackBar(final String msg) {
        //gets the main thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // run this code in the main thread
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void showClientAlertDialog(final String cred) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
        final EditText inputText = new EditText(context);

        String title = getString(R.string.device_name_title);
        String message = getString(R.string.device_name_message);
        String negative = getString(R.string.cancel);
        String positive = getString(R.string.ok);

        inputText.setHint(getString(R.string.device_name_hint));
        inputText.setSingleLine();

        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        inputText.setLayoutParams(params);
        container.addView(inputText);

        alertBuilder.setTitle(title)
                .setMessage(message)
                .setView(container)
                .setCancelable(false)
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sharedPreferences.edit().putString(KEY_CRED, null).apply();
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = inputText.getText().toString();
                        if(name.equals("")) name = getString(R.string.device_name_hint);

                        MainActivity.PostDevicesTask task = new MainActivity.PostDevicesTask();
                        task.execute(cred, name, MainActivity.device_uniqueId);

                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    private void initPreferences() {
        Log.e(TAG, "initPreferences " + MainActivity.device_uniqueId);

        sharedPreferences.edit().putBoolean(KEY_STATUS, false)
                .putInt(KEY_INT_DEVICE, MainActivity.device_id)
                .putString(KEY_DEVICE, MainActivity.device_uniqueId)
                .putString(KEY_CRED, null)
                .putString(KEY_URL, MY_URL_OSMAND)
                .putString(KEY_INTERVAL, DEFAULT_INTERVAL)
                .putString(KEY_DISTANCE, DEFAULT_DISTANCE)
                .putString(KEY_ANGLE, DEFAULT_ANGLE)
                .putString(KEY_PROVIDER, DEFAULT_PROVIDER)
                .apply();
    }

    private void migrateLegacyPreferences(SharedPreferences preferences) {
        String port = preferences.getString("port", null);
        if (port != null) {
            Log.d(TAG, "migrateLegacyPreferences: migrating to URL preference");

            String host = preferences.getString("address", MY_URL_OSMAND);
            String scheme = preferences.getBoolean("secure", false) ? "https" : "http";

            Uri.Builder builder = new Uri.Builder();
            builder.scheme(scheme).encodedAuthority(host + ":" + port).build();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_URL, builder.toString());

            editor.remove("port");
            editor.remove("address");
            editor.remove("secure");
            editor.apply();
        }
    }

    private boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1
                || checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void startTrackingService(boolean checkPermission, boolean permission) {
        if (checkPermission) {
            Set<String> missingPermissions = new HashSet<>();
            if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                missingPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                missingPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (missingPermissions.isEmpty()) {
                permission = true;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    requestPermissions(missingPermissions.toArray(new String[missingPermissions.size()]),
                            PERMISSIONS_REQUEST_LOCATION);
                return;
            }
        }

        if (permission) {
            startService(new Intent(this, TrackingService.class));
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    15000, 15000, alarmIntent);
        } else {
            sharedPreferences.edit().putBoolean(MainActivity.KEY_STATUS, false).apply();
        }
    }

    private void stopTrackingService() {
        alarmManager.cancel(alarmIntent);
        stopService(new Intent(this, TrackingService.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            startTrackingService(false, granted);

        } else if(requestCode == PERMISSIONS_REQUEST_EXTERNAL_STORAGE) {

            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            Log.d(TAG, "Permission External Storage Granted: " + granted);
        }
    }

    private class GetDevicesTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String credBase64 = params[0];
            String auth = "Basic " + credBase64;

            String json_response = "";
            String url = MainActivity.MY_URL + MainActivity.PATH_DEVICES;

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("authorization", auth)
                    .addHeader("accept", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "0cf9a913-c700-6b34-72ee-ad9085558120")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();

                int status = response.code();
                Log.d("Response", "Status: " + status);

                if(status == 401) {
                    Log.e("Response 401 Error", "Invalid User");
                    return false;
                } else if(status == 400) {
                    if(responseBody != null) json_response = responseBody.string();
                    Log.e("Response 400 Error", json_response);
                    return false;
                }

                sharedPreferences.edit().putString(KEY_CRED, credBase64).apply();

                if(responseBody != null) json_response = responseBody.string();
                Log.d("Response", json_response);

                JSONArray jsonArray = new JSONArray(json_response);

                for(int i=0; i<jsonArray.length(); i++) {
                    String item = jsonArray.get(i) + "";

                    JSONObject obj = new JSONObject(item);
                    Log.d("Device Params", "Name: " + obj.getString("name") + ", ID: " + obj.getInt("id")
                            + ", Unique ID: " + obj.getString("uniqueId"));

                    if(obj.getString("uniqueId").equals(MainActivity.device_uniqueId)) {
                        Log.d(TAG, "Device Already Added");
                        // Device Found
                        foundHandler.post(foundRunnable);
                        return true;
                    }
                }

                // Device Not Found
                notFoundHandler.post(notFoundRunnable);
                return false;
            } catch (Exception e) {
                Log.e("Response Exception", "Unable to retrieve data. URL may be invalid.", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("onPostExecute", "Result: " + result);
            if(spinnerLayout.getVisibility() == View.VISIBLE)
                spinnerLayout.setVisibility(View.GONE);
        }
    }

    private class PostDevicesTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String credBase64 = params[0];
            String auth = "Basic " + credBase64;

            String name = params[1];
            String deviceUniqueId = params[2];

            String json_request = "{\n" +
                    "\"name\": \"" + name + "\",\n" +
                    "\"uniqueId\": \"" + deviceUniqueId + "\"\n" +
                    "\t}";
            String json_response = "";
            String url = MainActivity.MY_URL + MainActivity.PATH_DEVICES;

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, json_request);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("content-type", "application/json")
                    .addHeader("authorization", auth)
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "16074f52-5108-388e-7db4-7b82f7a40faf")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();

                int status = response.code();
                Log.d("Response", "Status: " + status);

                if(status == 401) {
                    Log.e("Response 401 Error", "Invalid User");
                    showToast(getString(R.string.invalid_code));
                    return false;
                }
                else if(status == 400) {
                    if(responseBody != null) json_response = responseBody.string();
                    Log.e("Response 400 Error", json_response);

                    if(json_response.contains("uk_user_email")) {
                        Log.e(TAG, "Device is probably in another manager account");
                        sharedPreferences.edit().putString(KEY_CRED, null).apply();
                        sharedPreferences.edit().putBoolean(KEY_STATUS, false).apply();

                        showSnackBar(getString(R.string.device_not_added));
                        return false;
                    } else {
                        showToast(getString(R.string.invalid_code));
                    }

                    return false;
                }

                if(responseBody != null) json_response = responseBody.string();
                Log.d("Response", json_response);

                JSONObject obj = new JSONObject(json_response);

                MainActivity.device_id = obj.getInt("id");
                MainActivity.sharedPreferences.edit().putInt(MainActivity.KEY_INT_DEVICE, obj.getInt("id")).apply();
                Log.d("Device Params", "ID: " + obj.getInt("id"));
                Log.d("Device Param", "Device Name: " + obj.getString("name"));
                Log.d("Device Param", "Unique ID: " + obj.getString("uniqueId"));
                Log.d("Device Param", "Status: " + obj.getString("status"));
                Log.d("Device Param", "Position ID: " + obj.getString("positionId"));
                Log.d("Device Param", "Phone: " + obj.getString("phone"));
                Log.d("Device Param", "Model: " + obj.getString("model"));

                // showToast(getString(R.string.device_added));
                return true;
            } catch (Exception e) {
                Log.e("Response Exception", "Unable to retrieve data. URL may be invalid.", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            spinnerLayout.setVisibility(View.GONE);
            Log.d("Response", "Result: " + result);
            if(result) foundHandler.post(foundRunnable);
        }
    }

    private class MyBounceInterpolator implements android.view.animation.Interpolator {
        private double mAmplitude = 1;
        private double mFrequency = 10;

        MyBounceInterpolator(double amplitude, double frequency) {
            mAmplitude = amplitude;
            mFrequency = frequency;
        }

        public float getInterpolation(float time) {
            return (float) (-1 * Math.pow(Math.E, -time/ mAmplitude) *
                    Math.cos(mFrequency * time) + 1);
        }
    }
}
