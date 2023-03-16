package test.pprivera.com.testapplicationtraccar3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static test.pprivera.com.testapplicationtraccar3.MainActivity.interstitialAd;
import static test.pprivera.com.testapplicationtraccar3.MainActivity.interstitialCount;

public class ManagerActivity extends AppCompatActivity {

    private final Activity activity = this;
    private final Context context = ManagerActivity.this;
    private static final String TAG = ManagerActivity.class.getSimpleName();

    private static final int REQUEST_UPLOAD = 0;
    private static final int REQUEST_UPLOAD_DEVICE = 1;
    private static final int REQUEST_INVITE = 2;

    private static final int PERMISSIONS_REQUEST_LOCATION = 2;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 3;

    // First Handler
    private static final int FIRST_MAP_GEOFENCES_UPDATE = 1;
    private static final int GET_LOGIN_FILE_TASK = 2;

    // Loop Handler
    private static final int GET_DEVICES_TASK = 1;
    private static final int GET_POSITIONS_TASK = 2;
    private static final int GET_LAST_POSITIONS_TASK = 3;
    private static final int MAP_UPDATE = 4;

    // Post Handler
    private static final int MAP_GEOFENCES_UPDATE = 1;
    private static final int MAP_DEVICES_DELETED_UPDATE = 2;

    private static final int TIME_ZONE = -5;
    private static final int MAX_DEVICES = 5;

    private static final boolean CLOSE_ANIMATION = true;
    private static final boolean HIDE_ANIMATION = true;
    private static final boolean MY_LOCATION_BUTTON_ENABLED = false;
    private static final boolean ROTATE_GESTURES_ENABLED = false;
    private static final boolean MAP_TOOLBAR_ENABLED = false;

    // Amazon S3 Parameters
    private static final String BUCKET_NAME = "traccarcsti";
    private static final String REGION = "us-east-1";

    // Application Shared Preferences
    private static final String PREF_NAME = "ManagerActivity";
    private static final String MAP_POSITIONS = "positions";
    private static final int PRIVATE_MODE = 0;

    private SessionManager sessionManager;
    private String credBase64;

    private SharedPreferences mapPreferences;

    private GoogleMap map;
    private MapView mapView;
    private SparseArray<String> devices;
    private SparseBooleanArray devicesVisibility;
    private SparseArray<MarkerOptions> markerOptions;
    private SparseArray<Marker> markers;
    private SparseArray<CircleOptions> circleOptions;
    private SparseArray<Circle> circles;
    private SparseArray<ArrayList<LatLng>> positions;
    private SparseArray<ArrayList<Location>> locations;
    private SparseArray<Polyline> polylines;

    private Handler menuHandler = new Handler();
    private Runnable menuRunnable = new Runnable() {
        @Override
        public void run() {
            Log.w(TAG, "menuRunnable");

            if(spinnerLayout.getVisibility() == View.VISIBLE) {
                spinnerLayout.setAnimation(AnimationUtils.loadAnimation(context,
                        android.R.anim.fade_out));
                spinnerLayout.setVisibility(View.GONE);

                famMenu.showMenu(HIDE_ANIMATION);
                famExtras.showMenu(HIDE_ANIMATION);
            }

            menuHandler.removeCallbacks(this);
        }
    };

    // VERSION 2 private int filesCount = 0;
    private String loginPath;

    private boolean killHandlers = false;
    private Message msgFirst = new Message();
    private Handler taskFirstHandler = new Handler();
    private Runnable taskFirstRunnable = new Runnable() {
        @Override
        public void run() {
            if(killHandlers){
                taskFirstHandler.removeCallbacks(taskFirstRunnable);
                return;
            }

            switch (msgFirst.what) {
                case FIRST_MAP_GEOFENCES_UPDATE:

                    Log.e(TAG, "FIRST_MAP_GEOFENCES_UPDATE " + FIRST_MAP_GEOFENCES_UPDATE);
                    for(int i=0; i<circleOptions.size(); i++) {
                        int key = circleOptions.keyAt(i);

                        if(circles.get(key) == null) {
                            CircleOptions opt = circleOptions.get(key);
                            Circle c = map.addCircle(opt);

                            circles.put(key, c);
                        }
                    }
                    mapView.invalidate();
                    msgFirst.what = GET_LOGIN_FILE_TASK;
                    taskFirstHandler.post(this);
                    break;

                case GET_LOGIN_FILE_TASK:

                    Log.e(TAG, "GET_LOGIN_FILE_TASK " + GET_LOGIN_FILE_TASK);
                    // File name without extension
                    String fileName = "U" + sessionManager.getUserDetails().get(SessionManager.KEY_ID);
                    new GetLoginFileTask().execute(fileName);
                    break;
            }
        }
    };

    private Message msgLoop = new Message();
    private Handler taskLoopHandler = new Handler();
    private Runnable taskLoopRunnable = new Runnable() {
        @Override
        public void run() {
            if(killHandlers){
                taskLoopHandler.removeCallbacks(taskLoopRunnable);
                return;
            }

            switch(msgLoop.what) {
                case GET_DEVICES_TASK:

                    Log.e(TAG, "GET_DEVICES_TASK " + GET_DEVICES_TASK);
                    new ManagerActivity.GetDevicesTask().execute(credBase64);
                    break;

                case GET_POSITIONS_TASK:

                    Log.e(TAG, "GET_POSITIONS_TASK "  + GET_POSITIONS_TASK);
                    new ManagerActivity.GetPositionsTask().execute(credBase64);
                    break;

                case GET_LAST_POSITIONS_TASK:

                    if(positions.size() > 0) {
                        msgLoop.what = MAP_UPDATE;
                        taskLoopHandler.post(taskLoopRunnable);
                        return;
                    }

                    Log.e(TAG, "GET_LAST_POSITIONS_TASK " + GET_LAST_POSITIONS_TASK);

                    Calendar calendar = Calendar.getInstance();
                    Log.e(TAG, calendar.getTime().toString());

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

                    try {
                        Date tempDate = calendar.getTime();
                        long offset = tempDate.getTime();
                        offset -= TIME_ZONE*60*60*1000;
                        tempDate.setTime(offset);
                        calendar.setTime(tempDate);

                        String dateTo = dateFormat.format(tempDate);

                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        tempDate = calendar.getTime();
                        offset = tempDate.getTime();
                        offset -= TIME_ZONE*60*60*1000;
                        tempDate.setTime(offset);

                        String dateFrom = dateFormat.format(tempDate);

                        Log.e(TAG, "dateFrom: " + dateFrom);
                        Log.e(TAG, "dateTo: " + dateTo);
                        Log.e(TAG, "Marker Options Size: " + markerOptions.size());

                        for(int i=0; i<markerOptions.size(); i++) {
                            String deviceId = markerOptions.keyAt(i) + "";

                            ManagerActivity.GetLastPositionsTask task = new ManagerActivity.GetLastPositionsTask();
                            task.execute(credBase64, deviceId, dateFrom, dateTo);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        FirebaseCrash.log("Date Formatting throws Exception");
                    }

                    taskFirstHandler.removeCallbacks(this);
                    break;

                case MAP_UPDATE:

                    Log.e(TAG, "MAP_UPDATE " + MAP_UPDATE);

                    for(int i=0; i<markerOptions.size(); i++) {
                        int key = markerOptions.keyAt(i);
                        MarkerOptions opt = markerOptions.get(key);

                        if(polylines.get(key) == null) {
                            Polyline pl = redrawLine(key);
                            polylines.put(key, pl);
                        }

                        Marker m = markers.get(key);

                        if(m == null) {
                            // New markers
                            m = map.addMarker(opt);
                            markers.put(key, m);

                            if(!devicesVisibility.get(key)) {
                                // If device is unchecked in the menu
                                if(markers.get(key) != null)
                                    markers.get(key).setVisible(false);

                                if(polylines.get(key) != null)
                                    polylines.get(key).setVisible(false);
                            }

                            String fileName = "U" + sessionManager.getUserDetails().get(SessionManager.KEY_ID) + "D" + key;
                            new GetFileTask().execute(fileName);

                        } else if(hasChangedPosition(m.getPosition().latitude, m.getPosition().longitude,
                                opt.getPosition().latitude, opt.getPosition().longitude)) {

                            m.setPosition(opt.getPosition());
                            m.setSnippet(opt.getSnippet());

                            positions.get(key).add(m.getPosition());

                            Polyline pl = redrawLine(key);
                            polylines.put(key, pl);
                        }
                    }

                    mapView.invalidate();
                    msgLoop.what = GET_DEVICES_TASK;
                    taskLoopHandler.postDelayed(this, 20000);
                    break;
            }
        }
    };

    private Message msgPost = new Message();
    private Handler taskPostHandler = new Handler();
    private Runnable taskPostRunnable = new Runnable() {
        @Override
        public void run() {
            if(killHandlers){
                taskPostHandler.removeCallbacks(taskPostRunnable);
                return;
            }

            switch (msgPost.what) {
                case MAP_GEOFENCES_UPDATE:

                    Log.e(TAG, "MAP_GEOFENCES_UPDATE " + MAP_GEOFENCES_UPDATE);
                    for(int i=0; i<circleOptions.size(); i++) {
                        int key = circleOptions.keyAt(i);

                        if(circles.get(key) == null) {
                            CircleOptions opt = circleOptions.get(key);
                            Circle c = map.addCircle(opt);

                            circles.put(key, c);
                        }
                    }
                    mapView.invalidate();
                    break;

                case MAP_DEVICES_DELETED_UPDATE:

                    Log.e(TAG, "MAP_DEVICES_DELETED_UPDATE " + MAP_DEVICES_DELETED_UPDATE);
                    int key = (int) msgPost.obj;
                    if(devices.get(key) != null) devices.delete(key);
                    devicesVisibility.delete(key);
                    if(markerOptions.get(key) != null) markerOptions.delete(key);
                    if(markers.get(key) != null) {
                        Bitmap bm = (Bitmap) markers.get(key).getTag();
                        if(bm != null)
                            bm.recycle();
                        markers.get(key).remove();
                        markers.delete(key);
                    }
                    if(positions.get(key) != null) positions.delete(key);
                    if(polylines.get(key) != null) {
                        polylines.get(key).remove();
                        polylines.delete(key);
                    }
                    mapView.invalidate();

                    String fileName = "U" + sessionManager.getUserDetails().get(SessionManager.KEY_ID) + "D" + key + ".png";
                    new DeleteFileTask().execute(fileName);
                    break;
            }
        }
    };

    private FloatingActionMenu famMenu;
    private FloatingActionMenu famExtras;
    // VERSION 2 private FloatingActionButton actionPlaces;
    private FloatingActionButton actionTracking;
    // VERSION 2 private FloatingActionButton actionSettings;
    private FloatingActionButton actionClose;
    private FloatingActionButton actionLogout;
    private FloatingActionButton actionPeople;
    private FloatingActionButton actionAddPerson;
    private FloatingActionButton actionEditPerson;
    // VERSION 2 private FloatingActionButton actionAddGroup;
    // VERSION 2 private FloatingActionButton actionAddPlace;

    // VERSION 2 private RelativeLayout centerLayout;
    // VERSION 2 private RelativeLayout sizeLayout;

    private AmazonS3Client s3;
    private TransferUtility transferUtility;

    private RelativeLayout spinnerLayout;

    private static int tempKeyOnResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                // NOTA - addTestDevice ocasiona que los iconos FAM Menu se rayen al animarse.
                // ZTE PIERO
                // .addTestDevice("C46C7AB9CEB260CA88BB81357F919B59")
                // Innova CSTI
                // .addTestDevice("0DEF3D0D728B4A7213944D43F486C6A1")
                .build();
        adView.loadAd(adRequest);

        if (interstitialAd.isLoaded()) interstitialAd.show();
        else Log.d(TAG, "The interstitial wasn't loaded yet.");

        sessionManager = new SessionManager(context);

        credBase64 = sessionManager.getUserDetails().get(SessionManager.KEY_CRED_BASE64);

        mapPreferences = ManagerActivity.this.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        if (!mapPreferences.contains(MAP_POSITIONS)) {
            mapPreferences.edit().putString(MAP_POSITIONS, null).apply();
        }
        // else getSavedPositions();

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setUpMap(googleMap);
            }
        });

        devices = new SparseArray<>();
        devicesVisibility = new SparseBooleanArray();
        markerOptions = new SparseArray<>();
        markers = new SparseArray<>();
        circleOptions = new SparseArray<>();
        circles = new SparseArray<>();
        positions = new SparseArray<>();
        locations = new SparseArray<>();
        polylines = new SparseArray<>();

        famMenu = (FloatingActionMenu) findViewById(R.id.menu_top);
        famExtras = (FloatingActionMenu) findViewById(R.id.menu_bottom);
        famMenu.hideMenu(false);
        famExtras.hideMenu(false);

        actionPeople = (FloatingActionButton) findViewById(R.id.action_show_people);
        // VERSION 2 actionPlaces = (FloatingActionButton) findViewById(R.id.action_places);
        actionTracking = (FloatingActionButton) findViewById(R.id.action_tracking);
        // VERSION 2 actionSettings = (FloatingActionButton) findViewById(R.id.action_settings);
        actionLogout = (FloatingActionButton) findViewById(R.id.action_logout);
        actionClose = (FloatingActionButton) findViewById(R.id.action_close);
        actionAddPerson = (FloatingActionButton) findViewById(R.id.action_new_person);
        actionEditPerson = (FloatingActionButton) findViewById(R.id.action_edit_person);
        // VERSION 2 actionAddGroup = (FloatingActionButton) findViewById(R.id.action_new_group);
        // VERSION 2 actionAddPlace = (FloatingActionButton) findViewById(R.id.action_new_place);

        // VERSION 2 centerLayout = (RelativeLayout) findViewById(R.id.centerLayout);
        // VERSION 2 sizeLayout = (RelativeLayout) findViewById(R.id.sizeLayout);

        setOnClickListeners();

        FloatingActionButton actionMyLocation = (FloatingActionButton) findViewById(R.id.action_my_location);
        actionMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMyLocation(true);
            }
        });

        spinnerLayout = (RelativeLayout) findViewById(R.id.spinnerLayout);

        // Set Amazon Client
        s3 = getS3Client();
        Log.e(TAG, s3.toString());

        transferUtility = new TransferUtility(getS3Client(), context.getApplicationContext());

        // Start Sequence
        new ManagerActivity.GetGeofencesTask().execute(credBase64);

        // FAM Animations
        famMenu.setIconAnimated(true);
        famExtras.setIconAnimated(true);

        // Upload photo
        findViewById(R.id.action_change_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                famMenu.close(false);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "Not Granted");

                        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_EXTERNAL_STORAGE);

                        return;
                    }
                }

                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT >= 19) {
                    // For Android versions of KitKat or later, we use a
                    // different intent to ensure
                    // we can get the file path from the returned intent URI
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                } else {
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                }

                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_UPLOAD);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.w(TAG, "onDestroy");
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
        killHandlers = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "onResume");
        if (mapView != null) mapView.onResume();

        /*
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in
         **/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sessionManager.checkLoginStatus();
                if (!sessionManager.isLoggedIn()) finish();
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(TAG, "onPause");
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "onLowMemory");
        if (mapView != null) mapView.onLowMemory();
    }

    private void setUpMap(GoogleMap googleMap) {
        if (map == null) {
            map = googleMap;
            enableMyLocation(true, false);
            setMyLocation(false);

            // Set Up Custom Info Window Adapter
            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);

                    TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
                    tvTitle.setText(marker.getTitle());
                    TextView tvSnippet = ((TextView) myContentsView.findViewById(R.id.snippet));
                    tvSnippet.setText(marker.getSnippet());
                    ImageView ivPhoto = ((ImageView) myContentsView.findViewById(R.id.photo));
                    ivPhoto.setImageBitmap((Bitmap) marker.getTag());

                    return myContentsView;
                }
            });

            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    // DEBUG_PURPOSES showToast(marker.getSnippet());
                }
            });

            // Set Up Map Listeners
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (famMenu.isOpened()) famMenu.close(CLOSE_ANIMATION);
                    if (famExtras.isOpened()) famExtras.close(CLOSE_ANIMATION);
                }
            });

            map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                @Override
                public void onCameraMoveStarted(int i) {
                    if (famMenu.isOpened()) famMenu.close(CLOSE_ANIMATION);
                    if (famExtras.isOpened()) famExtras.close(CLOSE_ANIMATION);
                }
            });

            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (famMenu.isOpened()) famMenu.close(CLOSE_ANIMATION);
                    if (famExtras.isOpened()) famExtras.close(CLOSE_ANIMATION);
                    return false;
                }
            });

            map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    /* VERSION 2 if(centerLayout.getVisibility() == View.VISIBLE) {
                        centerLayout.setVisibility(View.INVISIBLE);
                        sizeLayout.setVisibility(View.VISIBLE);

                        SeekBar sizebar = (SeekBar) findViewById(R.id.sizeBar);

                        final Circle c = map.addCircle(new CircleOptions()
                                .center(latLng)
                                .radius(200)
                                .strokeWidth(5)
                                .strokeColor(ContextCompat.getColor(context, R.color.light_blue))
                                .fillColor(ContextCompat.getColor(context, R.color.light_blue_semi_transparent))
                                .clickable(false));

                        sizebar.setMax(1000);
                        sizebar.setProgress(100);
                        sizebar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                c.setRadius(progress + 100);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });

                        findViewById(R.id.sizeOKButton).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                double latitude = c.getCenter().latitude;
                                double longitude = c.getCenter().longitude;
                                double radius = c.getRadius();
                                String area = "CIRCLE (" + latitude + " " + longitude + ", " + radius + ")";

                                // c.setTag(ADD DEVICES RELATEED TO GEOFENCE);
                                c.remove();

                                showGeofenceAlertDialog(area);

                                sizeLayout.setVisibility(View.INVISIBLE);
                                famMenu.showMenu(HIDE_ANIMATION);
                                famExtras.showMenu(HIDE_ANIMATION);
                            }
                        });

                        findViewById(R.id.sizeCancelButton).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                c.remove();

                                sizeLayout.setVisibility(View.INVISIBLE);
                                famMenu.showMenu(HIDE_ANIMATION);
                                famExtras.showMenu(HIDE_ANIMATION);
                            }
                        });
                    } else {
                        if (famMenu.isOpened()) famMenu.close(CLOSE_ANIMATION);
                        if (famExtras.isOpened()) famExtras.close(CLOSE_ANIMATION);
                    }*/

                    if (famMenu.isOpened()) famMenu.close(CLOSE_ANIMATION);
                    if (famExtras.isOpened()) famExtras.close(CLOSE_ANIMATION);
                }
            });
        }
    }

    private boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1
                || checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void enableMyLocation(boolean checkPermission, boolean permission) {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(missingPermissions.toArray(new String[missingPermissions.size()]),
                            PERMISSIONS_REQUEST_LOCATION);
                }
                return;
            }
        }

        if (permission) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(MY_LOCATION_BUTTON_ENABLED);
            map.getUiSettings().setRotateGesturesEnabled(ROTATE_GESTURES_ENABLED);
            map.getUiSettings().setMapToolbarEnabled(MAP_TOOLBAR_ENABLED);
        } else {
            showSnackBar(getString(R.string.permission_denied));
        }
    }

    private void setMyLocation(final Boolean animate) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Do nothing
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Do nothing
            }

            @Override
            public void onProviderEnabled(String provider) {
                // Do nothing
            }

            @Override
            public void onProviderDisabled(String provider) {
                if(provider.equals(LocationManager.GPS_PROVIDER)) {
                    showSnackBar(getString(R.string.gps_not_enabled));
                }
            }
        });

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            String bestProvider = "";
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = l;
                    bestProvider = provider;
                }
            }
            Log.d(TAG, "Best Provider: " + bestProvider);

            Location myLocation = bestLocation;

            if(myLocation != null) {
                double longitude = myLocation.getLongitude();
                double latitude = myLocation.getLatitude();
                Log.e("myLocation", latitude + ":" + longitude);

                if(animate) {
                    float zoom = map.getCameraPosition().zoom;
                    CameraUpdate cameraUpdate;
                    if(zoom >= 16)
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom);
                    else
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16);
                    map.animateCamera(cameraUpdate);
                } else {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
                }
            } else {

                String title = getString(R.string.warning);
                String message = getString(R.string.warning_message);
                String positive = getString(R.string.ok);

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }


        } else {
            showSnackBar(getString(R.string.gps_not_enabled));
        }
    }

    private void setOnClickListeners() {

        famMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if(opened && famExtras.isOpened()) {
                    famExtras.close(CLOSE_ANIMATION);
                }
            }
        });

        famExtras.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if(opened && famMenu.isOpened()) {
                    famMenu.close(CLOSE_ANIMATION);
                }
            }
        });

        actionPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                famMenu.close(CLOSE_ANIMATION);

                showPeopleAlertDialog();
            }
        });

        /* VERSION 2 actionPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionPlaces.getColorNormal() == ContextCompat.getColor(context, R.color.red)) {
                    // Hide Places
                    for(int i=0; i<circles.size(); i++){
                        circles.get(circles.keyAt(i)).setVisible(false);
                    }

                    actionPlaces.setColorNormal(ContextCompat.getColor(context, R.color.white));
                    actionPlaces.setColorPressed(ContextCompat.getColor(context, R.color.white_pressed));
                    actionPlaces.setImageResource(R.drawable.ic_business_red);
                } else {
                    //Show Places
                    for(int i=0; i<circles.size(); i++){
                        circles.get(circles.keyAt(i)).setVisible(true);
                    }

                    actionPlaces.setColorNormal(ContextCompat.getColor(context, R.color.red));
                    actionPlaces.setColorPressed(ContextCompat.getColor(context, R.color.red_pressed));
                    actionPlaces.setImageResource(R.drawable.ic_business);
                }
            }
        });
        */

        actionTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionTracking.getColorNormal() == ContextCompat.getColor(context, R.color.red)) {
                    // Hide Tracking
                    actionTracking.setLabelText(getString(R.string.show_tracking));

                    for(int i=0; i<polylines.size(); i++) {
                        Polyline pl = polylines.get(polylines.keyAt(i));
                        pl.setVisible(false);
                    }

                    actionTracking.setColorNormal(ContextCompat.getColor(context, R.color.white));
                    actionTracking.setColorPressed(ContextCompat.getColor(context, R.color.white_pressed));
                    actionTracking.setImageResource(R.drawable.ic_transfer_within_a_station_red);
                } else {
                    // Show Tracking
                    actionTracking.setLabelText(getString(R.string.hide_tracking));

                    for(int i=0; i<polylines.size(); i++) {
                        Polyline pl = polylines.get(polylines.keyAt(i));
                        Boolean visible = devicesVisibility.get(polylines.keyAt(i));
                        if(visible) pl.setVisible(true);
                    }

                    actionTracking.setColorNormal(ContextCompat.getColor(context, R.color.red));
                    actionTracking.setColorPressed(ContextCompat.getColor(context, R.color.red_pressed));
                    actionTracking.setImageResource(R.drawable.ic_transfer_within_a_station);
                }
            }
        });

        /* VERSION 2 actionSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Settings");
            }
        });*/

        actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        actionLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                famMenu.close(CLOSE_ANIMATION);
                showLogoutDialog();
            }
        });

        actionAddPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                famExtras.close(CLOSE_ANIMATION);

                // showOpenInChooser();
                // onInviteClicked();
                if(devices.size() < MAX_DEVICES) {
                    sendInvitation();
                } else {
                    String title = getString(R.string.invite_error_title);
                    String message = getString(R.string.invite_error_message);
                    String positive = getString(R.string.ok);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
                    builder.setTitle(title);
                    builder.setMessage(message);
                    builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }

            }
        });

        actionEditPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                famExtras.close(CLOSE_ANIMATION);

                showManagePeopleAlertDialog();
            }
        });

        /* VERSION 2 actionAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Add Group");
            }
        });*/

        /* VERSION 2 actionAddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                famMenu.hideMenu(HIDE_ANIMATION);
                famExtras.hideMenu(HIDE_ANIMATION);
                centerLayout.setVisibility(View.VISIBLE);
            }
        });
        */

        /* VERSION 2 findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centerLayout.setVisibility(View.INVISIBLE);
                famMenu.showMenu(HIDE_ANIMATION);
                famExtras.showMenu(HIDE_ANIMATION);
            }
        });
        */
    }

    private Polyline redrawLine(int id) {

        try {
            ArrayList<LatLng> pos = positions.get(id);
            Polyline polyline = polylines.get(id);
            Boolean visible = devicesVisibility.get(id);

            Random rand = new Random();
            int color = Color.HSVToColor(new float[]{rand.nextFloat() * 360, 0.75f, 0.85f});

            if(polyline != null) {
                color = polyline.getColor();
                polyline.remove();
            }

            String draw = (polyline != null) ? "redrawLine" : "drawLine";
            Log.d(TAG, draw + " - " + id + ", "+ color);

            PolylineOptions options = new PolylineOptions().width(5).color(color).geodesic(true);
            for (int i = 0; i < pos.size(); i++) {
                LatLng point = pos.get(i);
                options.add(point);
            }

            polyline = map.addPolyline(options);

            if(actionTracking.getColorNormal() == ContextCompat.getColor(context, R.color.white) || !visible) {
                // Hide Tracking
                polyline.setVisible(false);
            }

            return polyline;

        } catch (Exception e){
            e.printStackTrace();
            FirebaseCrash.report(e);
            FirebaseCrash.log("private method redrawLine throws Exception");
            return null;
        }
    }

    private boolean hasChangedPosition(double aLat, double aLng, double bLat, double bLng) {
        double distance = DistanceCalculator.distance(aLat, aLng, bLat, bLng);

        Log.e("hasChangedPosition", distance + " - " + (distance > 1.0));
        return distance > 1.0;
    }

    protected void showToast(final String msg){
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

    /* DEBUG_PURPOSES private void showOpenInChooser() {
        String credBase64 = sessionManager.getUserDetails().get(SessionManager.KEY_CRED_BASE64);
        String username = sessionManager.getUserDetails().get(SessionManager.KEY_NAME);
        String link = getString(R.string.firebase_deep_link) + "/" + credBase64;

        // Crear Link y Enviar por Whatsapp, Facebook, etc.
        // Abrir la aplicación desde Link

        List<Intent> targetShareIntents= new ArrayList<>();
        Intent shareIntent=new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        List<ResolveInfo> resInfos=getPackageManager().queryIntentActivities(shareIntent, 0);
        if(!resInfos.isEmpty()){
            Log.w(TAG, "Have package");
            for(ResolveInfo resInfo : resInfos){
                String packageName=resInfo.activityInfo.packageName;
                Log.i("Package Name", packageName);
                if(packageName.contains("mms") ||
                        packageName.contains("com.android.email") ||
                        packageName.contains("com.twitter.android") ||
                        packageName.contains("com.facebook.katana") ||
                        packageName.contains("android.gm") ||
                        packageName.contains("whatsapp") ||
                        packageName.contains("docs")){
                    Intent intent=new Intent();
                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "Text");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                    intent.setPackage(packageName);
                    targetShareIntents.add(intent);
                }
            }
            if(!targetShareIntents.isEmpty()){
                Log.w(TAG, "Have Intent");
                Intent chooserIntent=Intent.createChooser(targetShareIntents.remove(0), "Choose app to share");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                startActivity(chooserIntent);
            }else{
                Log.e(TAG, "Do not Have Intent");
            }
        }
    }*/

    private void showLogoutDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);

        String title = getString(R.string.logout);
        String message = getString(R.string.logout_message);
        String negative = getString(R.string.no);
        String positive = getString(R.string.yes);

        alertBuilder.setTitle(title);
        alertBuilder.setMessage(message);
        alertBuilder.setCancelable(false);
        alertBuilder.setNegativeButton(negative, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertBuilder.setPositiveButton(positive, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ManagerActivity.DeleteSessionTask task = new ManagerActivity.DeleteSessionTask();
                task.execute(credBase64);

                dialog.dismiss();
            }
        });

        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    private void showPeopleAlertDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);

        LinearLayout container = new LinearLayout(context);
        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.dialog_padding);
        container.setPadding(padding, padding, padding, padding);

        if(devices.size() == 0) {
            // No devices
            TextView noDevices = new TextView(context);
            String message = getString(R.string.no_devices_message);
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_text_view_margin);
            noDevices.setLayoutParams(tvParams);
            noDevices.setText(message);
            noDevices.setTextSize(16);

            container.addView(noDevices);
        }

        for(int i = 0; i< devices.size(); i++) {
            final int key = devices.keyAt(i);
            LinearLayout layout = new LinearLayout(context);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(params);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            layout.setOrientation(LinearLayout.HORIZONTAL);

            CheckBox checkBox = new CheckBox(context);
            LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cbParams.topMargin = getResources().getDimensionPixelSize(R.dimen.dialog_text_view_margin);
            cbParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dialog_text_view_margin);
            checkBox.setLayoutParams(cbParams);
            checkBox.setChecked(devicesVisibility.get(key));
            checkBox.setText(devices.get(key));
            checkBox.setTextSize(16);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        devicesVisibility.put(key, true);

                        if(markers.get(key) != null) {
                            markers.get(key).setVisible(true);
                            if(actionTracking.getColorNormal() == ContextCompat.getColor(context, R.color.red)) {
                                // DEBUG_PURPOSES showToast("SHOW");
                                if(polylines.get(key) != null)
                                    polylines.get(key).setVisible(true);
                            }
                        }
                    } else {
                        devicesVisibility.put(key, false);

                        // DEBUG_PURPOSES showToast("HIDE");
                        if(markers.get(key) != null)
                            markers.get(key).setVisible(false);

                        if(polylines.get(key) != null)
                            polylines.get(key).setVisible(false);
                    }
                }
            });

            layout.addView(checkBox);

            container.addView(layout);
        }

        String title = getString(R.string.clients);
        String positive = getString(R.string.ok);

        alertBuilder.setTitle(title);
        alertBuilder.setView(container);
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton(positive, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(interstitialCount == 4) {
                    interstitialCount = 0;
                    if (interstitialAd.isLoaded()) interstitialAd.show();
                    else Log.d(TAG, "The interstitial wasn't loaded yet.");

                } else {
                    interstitialCount++;
                }

                dialog.dismiss();
            }
        });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    private void showManagePeopleAlertDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);

        LinearLayout container = new LinearLayout(context);
        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.dialog_padding);
        container.setPadding(padding, padding, padding, padding);

        if(devices.size() == 0) {
            // No devices
            TextView noDevices = new TextView(context);
            String message = getString(R.string.no_devices_message);
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_text_view_margin);
            noDevices.setLayoutParams(tvParams);
            noDevices.setText(message);
            noDevices.setTextSize(16);

            container.addView(noDevices);
        }

        for(int i = 0; i< devices.size(); i++) {
            final int key = devices.keyAt(i);
            final String name = devices.get(key);
            LinearLayout layout = new LinearLayout(context);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(params);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            layout.setOrientation(LinearLayout.HORIZONTAL);

            TextView clientName = new TextView(context);
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f);
            tvParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_text_view_margin);
            clientName.setLayoutParams(tvParams);
            clientName.setText(name);
            clientName.setTextSize(16);

            /* VERSION 2 final ImageButton edit = new ImageButton(context);
            LinearLayout.LayoutParams eParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.1f);
            eParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_text_view_margin);
            eParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_text_view_margin);
            edit.setLayoutParams(eParams);
            edit.setImageResource(R.drawable.ic_edit_black);
            edit.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            */

            ImageButton photo = new ImageButton(context);
            LinearLayout.LayoutParams pParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.1f);
            pParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_text_view_margin);
            pParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_text_view_margin);
            photo.setLayoutParams(pParams);
            photo.setImageResource(R.drawable.ic_photo_black);
            photo.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "Not Granted");

                            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                    PERMISSIONS_REQUEST_EXTERNAL_STORAGE);

                            return;
                        }
                    }

                    Intent intent = new Intent();
                    tempKeyOnResult = key;
                    if (Build.VERSION.SDK_INT >= 19) {
                        // For Android versions of KitKat or later, we use a
                        // different intent to ensure
                        // we can get the file path from the returned intent URI
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    } else {
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                    }

                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_UPLOAD_DEVICE);
                }
            });

            final ImageButton delete = new ImageButton(context);
            LinearLayout.LayoutParams dParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.1f);
            dParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_text_view_margin);
            dParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_text_view_margin);
            delete.setLayoutParams(dParams);
            delete.setImageResource(R.drawable.ic_delete_black);
            delete.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String deviceId = key + "";
                    final String deviceName = devices.get(key);

                    AlertDialog.Builder alertBuilder =
                            new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);

                    String message = getString(R.string.delete_message);
                    String negative = getString(R.string.no);
                    String positive = getString(R.string.yes);

                    alertBuilder.setTitle(name);
                    alertBuilder.setMessage(message);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setNegativeButton(negative, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alertBuilder.setPositiveButton(positive, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            delete.setEnabled(false);
                            delete.setImageResource(R.drawable.ic_delete_transparent);

                            ManagerActivity.DeleteDevicesTask task = new ManagerActivity.DeleteDevicesTask();
                            task.execute(credBase64, deviceId, deviceName);

                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = alertBuilder.create();
                    dialog.show();
                }
            });

            layout.addView(clientName);
            // VERSION 2 layout.addView(edit);
            layout.addView(photo);
            layout.addView(delete);

            container.addView(layout);
        }

        String title = getString(R.string.clients);
        String positive = getString(R.string.ok);

        alertBuilder.setTitle(title);
        alertBuilder.setView(container);
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(interstitialCount == 4) {
                    interstitialCount = 0;
                    if (interstitialAd.isLoaded()) interstitialAd.show();
                    else Log.d(TAG, "The interstitial wasn't loaded yet.");

                } else {
                    interstitialCount++;
                }

                dialog.dismiss();
            }
        });

        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    /* VERSION 2 private void showGeofenceAlertDialog(String a) {
        final String area = a;

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
        final EditText inputText = new EditText(context);

        inputText.setHint("My Geofence Name");
        inputText.setSingleLine();

        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        inputText.setLayoutParams(params);
        container.addView(inputText);

        alertBuilder.setTitle("Geofence Name")
                .setMessage("Select a name for the geofence")
                .setView(container)
                .setCancelable(false)
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = inputText.getText().toString();
                        if(name.equals("")) name = "My Geofence Name";

                        ManagerActivity.PostGeofencesTask task = new ManagerActivity.PostGeofencesTask();
                        task.execute(credBase64, name, area);

                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }
    */

    private void beginLoginIconDownload(String path) {
        // Location to download files from S3 to. You can choose any accessible
        // file.
        final File file = new File(context.getCacheDir().toString() + "/" + path);

        Log.d(TAG, "beginDownload: " + file.getName());
        Log.d("FILE", "File exists: " + file.exists());

        TransferObserver observer = transferUtility.download(BUCKET_NAME, path, file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(TAG, "onStateChanged: " + id + ", " + state);

                if(state == TransferState.COMPLETED) {
                    if(file.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        int size = getResources().getDimensionPixelSize(R.dimen.img_size_user);
                        myBitmap = Bitmap.createScaledBitmap(myBitmap, size, size, true);
                        famMenu.getMenuIconView().setImageBitmap(myBitmap);

                        msgLoop.what = GET_DEVICES_TASK;
                        taskLoopHandler.post(taskLoopRunnable);
                    }
                } else if(state == TransferState.FAILED) {
                    Log.e(TAG, "Transfer Failed");
                    msgLoop.what = GET_DEVICES_TASK;
                    taskLoopHandler.post(taskLoopRunnable);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d", id, bytesTotal, bytesCurrent));
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(TAG, "Error during download: " + id, ex);
            }
        });
    }

    private void beginDownload(final int key, String filePath) {
        // Location to download files from S3 to. You can choose any accessible
        // file.
        final File file = new File(context.getCacheDir().toString() + "/" + filePath);

        Log.d(TAG, "beginDownload: " + file.getName());
        Log.w("FILE", "File exists: " + file.exists());

        TransferObserver observer = transferUtility.download(BUCKET_NAME, filePath, file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d("Amazon Download", "onStateChanged: " + id + ", " + state);

                if(state == TransferState.COMPLETED) {
                    if(file.exists()) {
                        Log.d(TAG, "Set Markers Icons - " + markers.size());

                        for(int i=0; i<markers.size(); i++) {
                            if(markers.keyAt(i) == key) {
                                Log.w(TAG, "Found! - " + key);
                                Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
                                int size = getResources().getDimensionPixelSize(R.dimen.img_size_device);
                                bm = Bitmap.createScaledBitmap(bm, size, size, true);
                                markers.get(markers.keyAt(i)).setTag(bm);

                                // add marker to Map
                                markers.get(markers.keyAt(i)).setIcon(BitmapDescriptorFactory.fromBitmap(setMarkerBitmap(bm)));
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.d("Amazon Download", String.format("onProgressChanged: %d, total: %d, current: %d",
                        id, bytesTotal, bytesCurrent));
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("Amazon Download", "Error during download: " + id, ex);
            }
        });
    }

    private void beginLoginIconUpload(String filePath) {
        if (filePath == null) {
            showSnackBar(getString(R.string.file_path_not_found));
            return;
        }

        File file = new File(filePath);
        String fileName = "U" + sessionManager.getUserDetails().get(SessionManager.KEY_ID) + ".png";
        // String fileName = "U3.png"; // User ID for Piero Rivera
        // String fileName = "U3D180.png" // Device ID for  ZTE Piero in user Piero Rivera
        // String fileName = file.getName();

        final File tempFile = new File(context.getCacheDir().toString() + "/user.png");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(tempFile);
            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if(myBitmap != null) {

                Log.e("My Bitmap", myBitmap.getWidth() + ", " + myBitmap.getHeight());
                Log.e("FAM Menu Icon", getResources().getDimensionPixelSize(R.dimen.img_size_user) + "");

                myBitmap = scaleBitmap(myBitmap, 120, 120);

                ExifInterface exif = new ExifInterface(tempFile.getAbsolutePath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.e(TAG, "orientation: " + orientation);

                myBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);

                fOut.flush();
                fOut.close();
                myBitmap.recycle();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(!tempFile.exists()) {
            Log.e(TAG, "Temporal File doesn't exist");
            showSnackBar(getString(R.string.file_not_found));
            return;
        }

        Log.d(TAG, "beginLoginIconUpload: " + fileName);
        TransferObserver observer = transferUtility.upload(BUCKET_NAME, fileName, tempFile);
        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginLoginIconUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d("Amazon Upload", "onStateChanged: " + id + ", " + state);

                if(state == TransferState.COMPLETED) {
                    Log.d("Amazon Upload", "Upload Complete");
                    if(tempFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                        int size = getResources().getDimensionPixelSize(R.dimen.img_size_user);
                        myBitmap = Bitmap.createScaledBitmap(myBitmap, size, size, true);
                        famMenu.getMenuIconView().setImageBitmap(myBitmap);
                    }
                    spinnerLayout.setAnimation(AnimationUtils.loadAnimation(context,
                            android.R.anim.fade_out));
                    spinnerLayout.setVisibility(View.GONE);

                } else if (state == TransferState.FAILED) {
                    Log.e("Amazon Upload", "Upload Failed");
                    spinnerLayout.setAnimation(AnimationUtils.loadAnimation(context,
                            android.R.anim.fade_out));
                    spinnerLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.d("Amazon Upload", String.format("onProgressChanged: %d, total: %d, current: %d",
                        id, bytesTotal, bytesCurrent));
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("Amazon Upload", "Error during upload: " + id, ex);
                spinnerLayout.setAnimation(AnimationUtils.loadAnimation(context,
                        android.R.anim.fade_out));
                spinnerLayout.setVisibility(View.GONE);
            }
        });
    }

    private void beginUpload(final int key, String filePath) {
        if (filePath == null) {
            showSnackBar(getString(R.string.file_path_not_found));
            return;
        }

        File file = new File(filePath);
        String fileName = "U" + sessionManager.getUserDetails().get(SessionManager.KEY_ID) + "D" + key + ".png";

        final File tempFile = new File(context.getCacheDir().toString() + "/temp.png");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(tempFile);
            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if(myBitmap != null) {

                myBitmap = scaleBitmap(myBitmap, 120, 120);

                ExifInterface exif = new ExifInterface(tempFile.getAbsolutePath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.e(TAG, "orientation: " + orientation);

                myBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);

                fOut.flush();
                fOut.close();
                myBitmap.recycle();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(!tempFile.exists()) {
            Log.e(TAG, "Temporal File doesn't exist");
            showSnackBar(getString(R.string.file_not_found));
            return;
        }

        Log.d(TAG, "beginUpload: " + fileName);
        TransferObserver observer = transferUtility.upload(BUCKET_NAME, fileName, tempFile);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d("Amazon Upload", "onStateChanged: " + id + ", " + state);

                if(state == TransferState.COMPLETED) {
                    Log.d("Amazon Upload", "Upload Complete");
                    if(tempFile.exists()) {
                        for(int i=0; i<markers.size(); i++) {
                            if(markers.keyAt(i) == key) {
                                Log.w(TAG, "Found! - " + key);
                                Bitmap bm = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                                int size = getResources().getDimensionPixelSize(R.dimen.img_size_device);
                                bm = Bitmap.createScaledBitmap(bm, size, size, true);
                                markers.get(markers.keyAt(i)).setTag(bm);

                                // set marker icon to Map
                                markers.get(markers.keyAt(i)).setIcon(BitmapDescriptorFactory.fromBitmap(setMarkerBitmap(bm)));
                                break;
                            }
                        }

                        if(tempFile.delete())
                            Log.d("Amazon Upload", "Delete Successful");
                    }

                    spinnerLayout.setAnimation(AnimationUtils.loadAnimation(context,
                            android.R.anim.fade_out));
                    spinnerLayout.setVisibility(View.GONE);

                } else if (state == TransferState.FAILED) {
                    Log.e("Amazon Upload", "Upload Failed");
                    if(tempFile.exists()) {
                        if(tempFile.delete())
                            Log.d("Amazon Upload", "Delete Successful");
                    }

                    spinnerLayout.setAnimation(AnimationUtils.loadAnimation(context,
                            android.R.anim.fade_out));
                    spinnerLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.d("Amazon Upload", String.format("onProgressChanged: %d, total: %d, current: %d",
                        id, bytesTotal, bytesCurrent));
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("Amazon Upload", "Error during upload: " + id, ex);
                spinnerLayout.setAnimation(AnimationUtils.loadAnimation(context,
                        android.R.anim.fade_out));
                spinnerLayout.setVisibility(View.GONE);

                if(tempFile.delete())
                    Log.d("Amazon Upload", "Delete Successful");
            }
        });
    }

    private Bitmap scaleBitmap(Bitmap bm, int maxWidth, int maxHeight) {
        int w = bm.getWidth();
        int h = bm.getHeight();

        if (w > h)
            bm = Bitmap.createBitmap(bm, w / 2 - h / 2, 0, h, h);
        else if(h > w)
            bm = Bitmap.createBitmap(bm, 0, h / 2 - w / 2, w, w);

        w = bm.getWidth();
        h = bm.getHeight();

        if(w > maxWidth || h > maxHeight)
            bm = Bitmap.createScaledBitmap(bm, maxWidth, maxHeight, true);
        bm = getRoundedRectBitmap(bm, maxWidth, maxHeight);

        return bm;
    }

    private Bitmap getRoundedRectBitmap(Bitmap bitmap, int width, int height) {
        Bitmap result = null;

        try {
            result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);

            int color = 0xff424242;
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, width, height);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(width / 2, height / 2, width / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
            FirebaseCrash.log("private method getRoundedRectBitmap throws Out of Memory Error");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private Bitmap setMarkerBitmap(Bitmap file) {
        Bitmap marker = BitmapFactory.decodeResource(getResources(),
                R.drawable.custom_marker);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(marker.getWidth(), marker.getHeight(), conf);
        Log.e("Marker", marker.getWidth() + ", " + marker.getHeight());
        Canvas canvas = new Canvas(bmp);

        // modify canvas
        canvas.drawBitmap(marker, 0, 0, null);
        canvas.drawBitmap(file, marker.getWidth() / 5.25f, marker.getHeight() / 12, null);

        return bmp;
    }

    public void getSavedPositions() {
        Log.e(TAG, "Saved Positions - " + mapPreferences.getString(MAP_POSITIONS, null));
    }

    public void savePositions(String data) {
        mapPreferences.edit().putString(MAP_POSITIONS, data).apply();
    }

    public void clearPositions() {
        mapPreferences.edit().clear().apply();
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
                    .addHeader("postman-token", "16074f52-5108-388e-7db4-7b82f7a40faf")
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

                if(responseBody != null) json_response = responseBody.string();
                Log.d("Response", json_response);

                JSONArray jsonArray = new JSONArray(json_response);

                for(int i=0; i<jsonArray.length(); i++) {
                    String item = jsonArray.get(i) + "";

                    JSONObject obj = new JSONObject(item);
                    Log.d("Device Params", "Name: " + obj.getString("name") + ", ID: " + obj.getInt("id")
                            + ", Unique ID: " + obj.getString("uniqueId"));

                    if(devices.get(obj.getInt("id")) == null) {
                        Log.e("DevicesUpdate", "TRUE");
                        devices.put(obj.getInt("id"), obj.getString("name"));
                        devicesVisibility.put(obj.getInt("id"), true);
                    }
                }

                Log.d("Number of Clients", devices.size() + "");

                return true;
            } catch (Exception e) {
                Log.e("Response Exception", "Unable to retrieve data. URL may be invalid.", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("onPostExecute", "Result: " + result);

            if(devices.size() == 0) {
                Log.w(TAG, "NO DEVICES FOUND");
                // No devices found - Loop
                msgLoop.what = GET_DEVICES_TASK;
                taskLoopHandler.postDelayed(taskLoopRunnable, 20000);
                menuHandler.post(menuRunnable);
            } else {
                Log.w(TAG, "AT LEAST ONE DEVICE FOUND");
                // Continue
                msgLoop.what = GET_POSITIONS_TASK;
                taskLoopHandler.post(taskLoopRunnable);
            }
        }
    }

    private class GetPositionsTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String credBase64 = params[0];
            String auth = "Basic " + credBase64;

            String json_response = "";
            String url = MainActivity.MY_URL + MainActivity.PATH_POSITIONS;

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("authorization", auth)
                    .addHeader("accept", "application/json")
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
                    return false;
                } else if(status == 406) {
                    Log.e("Response 406 Error", "Not Acceptable");
                    return false;
                } else if(status == 400) {
                    if(responseBody != null) json_response = responseBody.string();
                    Log.e("Response 400 Error", json_response);
                    return false;
                }

                if(responseBody != null) json_response = responseBody.string();
                Log.d("Response", json_response);

                JSONArray jsonArray = new JSONArray(json_response);

                for(int i=0; i<jsonArray.length(); i++) {
                    String item = jsonArray.get(i) + "";

                    JSONObject obj = new JSONObject(item);
                    Log.d("Position Params", "Position ID : " + obj.getInt("id") + "Device ID : " + obj.getInt("deviceId")
                            + ", Latitude: " + obj.getDouble("latitude") + ", Longitude: " + obj.getDouble("longitude")
                            + ", Altitude: " + obj.getDouble("altitude"));

                    String attributes = obj.getString("attributes");
                    JSONObject attr = new JSONObject(attributes);

                    String deviceTime = obj.getString("deviceTime");
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                    Date date = format.parse(deviceTime);
                    SimpleDateFormat newFormat = new SimpleDateFormat("dd/MM/yy h:mm a", Locale.US);
                    // SimpleDateFormat newFormat = new SimpleDateFormat("dd/MM/yy h:mm a, z", Locale.US);
                    String dateString = newFormat.format(date);

                    String title = devices.get(obj.getInt("deviceId"));
                    String snippet = getString(R.string.battery_level)
                            + ": " + attr.getInt("batteryLevel") + "%\n" + dateString;
                    MarkerOptions marker = new MarkerOptions()
                            .position(new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude")))
                            .title(title)
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker));

                    markerOptions.put(obj.getInt("deviceId"), marker);
                }

                return true;
            } catch (Exception e) {
                Log.e("Response Exception", "Unable to retrieve data. URL may be invalid.", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("onPostExecute", "Result: " + result);

            if(markerOptions.size() == 0) {
                Log.w(TAG, "NO MARKERS FOUND");
                // No markers found - Loop
                msgLoop.what = GET_DEVICES_TASK;
                taskLoopHandler.postDelayed(taskLoopRunnable, 20000);
                menuHandler.post(menuRunnable);
            } else {
                Log.w(TAG, "AT LEAST ONE MARKER FOUND");
                // Continue
                msgLoop.what = GET_LAST_POSITIONS_TASK;
                taskLoopHandler.post(taskLoopRunnable);
            }
        }
    }

    private class GetLastPositionsTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean result = false;

            String deviceId = params[1];
            String dateFrom = params[2];
            String dateTo = params [3];

            String PATH_PARAMS = String.format(Locale.US, "?deviceId=%s&from=%s&to=%s", deviceId, dateFrom, dateTo);
            Log.e("PATH_PARAMS", PATH_PARAMS);

            String credBase64 = params[0];
            String auth = "Basic " + credBase64;

            String json_response = "";
            String url = MainActivity.MY_URL + MainActivity.PATH_POSITIONS + PATH_PARAMS;

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("authorization", auth)
                    .addHeader("accept", "application/json")
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
                    return false;
                } else if(status == 406) {
                    Log.e("Response 406 Error", "Not Acceptable");
                    return false;
                } else if(status == 400) {
                    if(responseBody != null) json_response = responseBody.string();
                    Log.e("Response 400 Error", json_response);
                    return false;
                }

                if(responseBody != null) json_response = responseBody.string();
                Log.d("Response", json_response);

                JSONArray jsonArray = new JSONArray(json_response);

                final int device_id = Integer.parseInt(deviceId);
                positions.put(device_id, new ArrayList<LatLng>());
                locations.put(device_id, new ArrayList<Location>());

                for(int i=0; i<jsonArray.length(); i++) {
                    String item = jsonArray.get(i) + "";

                    JSONObject obj = new JSONObject(item);
                    Log.d("Position Params", "Position ID : " + obj.getInt("id") + ", Latitude: "
                            + obj.getDouble("latitude") + ", Longitude: " + obj.getDouble("longitude")
                            + ", Altitude: " + obj.getDouble("altitude") + ", Device Time: " + obj.getString("deviceTime"));

                    LatLng pos = new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude"));
                    positions.get(device_id).add(pos);

                    String deviceTime = obj.getString("deviceTime");
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                    Date date = format.parse(deviceTime);
                    Location l = new Location(LocationManager.PASSIVE_PROVIDER);
                    l.setLatitude(obj.getDouble("latitude"));
                    l.setLongitude(obj.getDouble("longitude"));
                    l.setAltitude(obj.getDouble("altitude"));
                    l.setTime(date.getTime());
                    locations.get(device_id).add(l);

                    result = true;
                }

                Log.d("Locations", locations.toString());
                Log.d("Locations Size", locations.get(device_id).size() + "");
                // savePositions(locations.toString());

                return result;
            } catch (Exception e) {
                Log.e("Response Exception", "Unable to retrieve data. URL may be invalid.", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("onPostExecute", "Result: " + result);
            if(positions.size() == markerOptions.size()) {
                menuHandler.postDelayed(menuRunnable, 0);
                msgLoop.what = MAP_UPDATE;
                taskLoopHandler.post(taskLoopRunnable);
            }
        }
    }

    private class GetGeofencesTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean result = false;

            String credBase64 = params[0];
            String auth = "Basic " + credBase64;

            String json_response = "";
            String url = MainActivity.MY_URL + MainActivity.PATH_GEOFENCES;

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("authorization", auth)
                    .addHeader("accept", "application/json")
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
                    return false;
                } else if(status == 400) {
                    if(responseBody != null) json_response = responseBody.string();
                    Log.e("Response 400 Error", json_response);
                    return false;
                }

                if(responseBody != null) json_response = responseBody.string();
                Log.d("Response", json_response);

                JSONArray jsonArray = new JSONArray(json_response);

                for(int i=0; i<jsonArray.length(); i++) {
                    String item = jsonArray.get(i) + "";
                    Log.d("JSONArray Item", item);

                    JSONObject obj = new JSONObject(item);
                    Log.d("Geofence Params", "Geofence ID : " + obj.getInt("id") + ", Geofence Name: "
                            + obj.getString("name") + ", Area: " + obj.getString("area"));

                    String [] areaParams = obj.getString("area")
                            .replace("(", "")
                            .replace(")", "")
                            .replace(",", "")
                            .split(" +");

                    if(areaParams[0].equals("CIRCLE")) {

                        double latitude = Double.parseDouble(areaParams[1]);
                        double longitude = Double.parseDouble(areaParams[2]);
                        double radius = Double.parseDouble(areaParams[3]);

                        CircleOptions circle = new CircleOptions().center(new LatLng(latitude, longitude))
                                .radius(radius)
                                .strokeWidth(5)
                                .strokeColor(ContextCompat.getColor(context, R.color.light_blue))
                                .fillColor(ContextCompat.getColor(context, R.color.light_blue_transparent))
                                .clickable(false);

                        circleOptions.put(obj.getInt("id"), circle);

                        result = true;
                    }
                }

                return result;
            } catch (Exception e) {
                Log.e("Response Exception", "Unable to retrieve data. URL may be invalid.", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("onPostExecute", "Result: " + result);

            if(result) {
                msgFirst.what = FIRST_MAP_GEOFENCES_UPDATE;
                taskFirstHandler.post(taskFirstRunnable);
            } else {
                msgFirst.what = GET_LOGIN_FILE_TASK;
                taskFirstHandler.post(taskFirstRunnable);
            }
        }
    }

    /* VERSION 2 private class PostGeofencesTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
            spinnerLayout.setAnimation(AnimationUtils.loadAnimation(context,
                    android.R.anim.fade_in));
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String name = params[1];
            String area = params[2];

            String credBase64 = params[0];
            String auth = "Basic " + credBase64;

            String json_request = "{\n" +
                    "\"name\": \"" + name + "\",\n" +
                    "\"area\": \"" + area + "\"\n" +
                    "\t}";
            String json_response = "";
            String url = MainActivity.MY_URL + MainActivity.PATH_GEOFENCES;

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
                    return false;
                } else if(status == 400) {
                    if(responseBody != null) json_response = responseBody.string();
                    Log.e("Response 400 Error", json_response);
                    return false;
                }

                if(responseBody != null) json_response = responseBody.string();
                Log.d("Response", json_response);

                JSONObject obj = new JSONObject(json_response);
                Log.d("Geofence Params", "Geofence ID : " + obj.getInt("id") + ", Geofence Name: "
                        + obj.getString("name") + ", Area: " + obj.getString("area"));

                String [] areaParams = obj.getString("area")
                        .replace("(", "")
                        .replace(")", "")
                        .replace(",", "")
                        .split(" +");

                if(areaParams[0].equals("CIRCLE")) {

                    double latitude = Double.parseDouble(areaParams[1]);
                    double longitude = Double.parseDouble(areaParams[2]);
                    double radius = Double.parseDouble(areaParams[3]);

                    CircleOptions opt = new CircleOptions().center(new LatLng(latitude, longitude))
                            .radius(radius)
                            .strokeWidth(5)
                            .strokeColor(ContextCompat.getColor(context, R.color.light_blue))
                            .fillColor(ContextCompat.getColor(context, R.color.light_blue_semi_transparent))
                            .clickable(false);

                    circleOptions.put(obj.getInt("id"), opt);
                }

                return true;
            } catch (Exception e) {
                Log.e("Response Exception", "Unable to retrieve data. URL may be invalid.", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("onPostExecute", "Result: " + result);
            spinnerLayout.setAnimation(AnimationUtils.loadAnimation(context,
                    android.R.anim.fade_out));
            spinnerLayout.setVisibility(View.GONE);

            if(result) {
                msgPost.what = MAP_GEOFENCES_UPDATE;
                taskPostHandler.post(taskPostRunnable);
            }
        }
    }
    */

    private class DeleteDevicesTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String credBase64 = params[0];
            String auth = "Basic " + credBase64;

            String deviceId = params[1];
            String deviceName = params[2];

            String json_request = "";
            String json_response = "";
            String url_id = "/" + deviceId;
            String url = MainActivity.MY_URL + MainActivity.PATH_DEVICES + url_id;

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, json_request);
            Request request = new Request.Builder()
                    .url(url)
                    .delete(body)
                    .addHeader("authorization", auth)
                    .addHeader("accept", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "8a083b34-a389-2d4c-3358-8bdaff605568")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();

                int status = response.code();
                Log.e("Response", "Status: " + status);

                if(status == 401) {
                    Log.e("Response 401 Error", "Invalid User");
                    return false;
                } else if(status == 400) {
                    if(responseBody != null) json_response = responseBody.string();
                    Log.e("Response 400 Error", json_response);
                    return false;
                }

                if(responseBody != null) json_response = responseBody.string();
                Log.e("Response", json_response);

                msgPost.obj = Integer.parseInt(deviceId);

                Log.d(TAG, "Device deleted: " + deviceName + " (" + deviceId + ")");
                // DEBUG_PURPOSES showToast("Device deleted: " + deviceName);

                return true;
            } catch (Exception e) {
                Log.e("Response Exception", "Unable to retrieve data. URL may be invalid.", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("onPostExecute", "Result: " + result);
            spinnerLayout.setAnimation(AnimationUtils.loadAnimation(context,
                    android.R.anim.fade_out));
            spinnerLayout.setVisibility(View.GONE);

            if(result) {
                msgPost.what = MAP_DEVICES_DELETED_UPDATE;
                taskPostHandler.post(taskPostRunnable);
            }
        }
    }

    private class DeleteSessionTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
            spinnerLayout.setAnimation(AnimationUtils.loadAnimation(context,
                    android.R.anim.fade_in));
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String credBase64 = params[0];
            String auth = "Basic " + credBase64;

            try {
                String emailEncode = URLEncoder.encode(sessionManager.getUserDetails().get(SessionManager.KEY_EMAIL), "UTF-8");
                String passEncode = URLEncoder.encode(sessionManager.getUserDetails().get(SessionManager.KEY_PASS), "UTF-8");

                String json_response = "";
                String urlencoded_request = "email=" + emailEncode + "&password=" + passEncode;
                String url = MainActivity.MY_URL + MainActivity.PATH_SESSION;

                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                RequestBody body = RequestBody.create(mediaType, urlencoded_request);
                Request request = new Request.Builder()
                        .url(url)
                        .delete(body)
                        .addHeader("authorization", auth)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("postman-token", "f31177ee-91fb-b553-2d6f-f1e598ed90d6")
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .build();

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
                } else if(status != 204) return false;

                return true;
            } catch (Exception e) {
                Log.e("Response Exception", "Unable to retrieve data. URL may be invalid.", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("onPostExecute", "Result: " + result);
            spinnerLayout.setAnimation(AnimationUtils.loadAnimation(context,
                    android.R.anim.fade_out));
            spinnerLayout.setVisibility(View.GONE);

            if(result) {
                File loginFile = new File(context.getCacheDir().toString() + "/" + loginPath);
                if(loginFile.exists()) Log.d(TAG, "File deleted - " + loginFile.delete());

                for(int i=0; i<markers.size(); i++) {
                    Bitmap bm = (Bitmap) markers.get(markers.keyAt(i)).getTag();
                    if(bm != null)
                        bm.recycle();
                }

                // Logout from Session Manager class
                sessionManager.logoutUser();
                // clearPositions();
                finish();
            }
        }
    }

    public static AmazonS3Client getS3Client() {

        AmazonS3Client sS3Client = new AmazonS3Client(new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new BasicAWSCredentials("AKIAIBXFZLXAYTZXKEOQ", "NOez2CIEQwlH+ldsLCDTzDkwB2AxY6ZHK7i0nBuN");
            }

            @Override
            public void refresh() {

            }
        });

        sS3Client.setRegion(Region.getRegion(Regions.fromName(REGION)));
        return sS3Client;
    }

    private class GetFileTask extends AsyncTask<String, Void, Boolean> {
        // The list of objects we find in the S3 bucket
        private List<S3ObjectSummary> s3ObjList;
        private ArrayList<HashMap<String, Object>> transferRecordMaps = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
        }

        @Override
        protected Boolean doInBackground(String... inputs) {
            String name = inputs[0];
            String path = "";

            final int key = Integer.parseInt(name.substring(name.indexOf("D") + 1));
            Log.d(getClass().getSimpleName(), "Key: " + key);

            try {
                // Queries files in the bucket from S3.
                Log.e(TAG,  "Bucket Exists?:" + s3.doesBucketExist(BUCKET_NAME));
                s3ObjList = s3.listObjects(BUCKET_NAME).getObjectSummaries();

                Log.e(TAG, s3ObjList.toString() + ", Size: " + s3ObjList.size());

                for (S3ObjectSummary summary : s3ObjList) {
                    final String tempPath = summary.getKey();
                    HashMap<String, Object> map = new HashMap<>();
                    if(tempPath.contains(name)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                beginDownload(key, tempPath);
                            }
                        });
                        return true;
                    }

                    map.put("key", summary.getKey());
                    transferRecordMaps.add(map);

                    if(summary.getKey().equals("kid.png")) {
                        path = summary.getKey();
                    }
                }

                // File not found
                final String tempPath = path;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        beginDownload(key, tempPath);
                    }
                });
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Unable to retrieve Amazon Storage", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(TAG, transferRecordMaps.toString() + ", Size: " + transferRecordMaps.size());
        }
    }

    private class GetLoginFileTask extends AsyncTask<String, Void, Boolean> {
        // The list of objects we find in the S3 bucket
        private List<S3ObjectSummary> s3ObjList;
        private ArrayList<HashMap<String, Object>> transferRecordMaps = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
        }

        @Override
        protected Boolean doInBackground(String... inputs) {
            String name = inputs[0];
            String path = "";

            try {
                // Queries files in the bucket from S3.
                Log.e(TAG,  "Bucket Exists?:" + s3.doesBucketExist(BUCKET_NAME));
                s3ObjList = s3.listObjects(BUCKET_NAME).getObjectSummaries();

                Log.e(TAG, s3ObjList.toString() + ", Size: " + s3ObjList.size());

                for (S3ObjectSummary summary : s3ObjList) {
                    HashMap<String, Object> map = new HashMap<>();
                    if(summary.getKey().contains(name)) {
                        loginPath = summary.getKey();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                beginLoginIconDownload(loginPath);
                            }
                        });
                        return true;
                    }

                    map.put("key", summary.getKey());
                    transferRecordMaps.add(map);

                    if(summary.getKey().equals("user.png")) {
                        path = summary.getKey();
                    }
                }

                // User file not found
                loginPath = path;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        beginLoginIconDownload(loginPath);
                    }
                });
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Unable to retrieve Amazon Storage", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(TAG, transferRecordMaps.toString() + ", Size: " + transferRecordMaps.size());
        }
    }

    private class DeleteFileTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
        }

        @Override
        protected Boolean doInBackground(String... inputs) {
            String key = inputs[0];

            try {
                s3.deleteObject(BUCKET_NAME, key);
                Log.d(TAG, "Amazon Object Successfully Deleted: " + key);
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Unable to delete Amazon Object" , e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "Result: " + result);
        }
    }

    /* DEBUG_PURPOSES private void onInviteClicked() {
        String credBase64 = sessionManager.getUserDetails().get(SessionManager.KEY_CRED_BASE64);
        String username = sessionManager.getUserDetails().get(SessionManager.KEY_NAME);
        String link = getString(R.string.firebase_deep_link) + "/" + credBase64;

        String title = getString(R.string.invite_title);
        String message = getString(R.string.invite_message);
        message = message.replaceAll("%%USERNAME%%", username);
        String subject = getString(R.string.invite_subject);

        String html = "<body><p><a href=\"%%APPINVITE_LINK_PLACEHOLDER%%\">Click Here</a>" +
                " to install or open the app on your Android device.</p></body>";

        Intent intent = new AppInviteInvitation.IntentBuilder(title)
            .setMessage(message)
            .setDeepLink(Uri.parse(link))
            .setEmailSubject(subject)
            .setEmailHtmlContent(html)
            .build();

        startActivity(intent);
    }
    */

    private void sendInvitation() {
        String link = getString(R.string.firebase_deep_link) + "/" + credBase64;
        String dynamicLink = getString(R.string.firebase_dynamic_link);

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDynamicLinkDomain(dynamicLink)
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            String shortLink = task.getResult().getShortLink().toString();
                            // Uri flowchartLink = task.getResult().getPreviewLink();
                            Log.e(TAG, "shortLink: " + shortLink);

                            // Send via SMS, Whatsapp, Facebook, Gmail, etc.

                            String username = sessionManager.getUserDetails().get(SessionManager.KEY_NAME);
                            if(username == null) return;

                            // String html = "<html><body><p><a href=\"" + shortLink
                            //        + "\">Click Here</a> to install or open the app on your Android device.</p></body></html>";

                            String inviteMessage = getString(R.string.invite_message);
                            inviteMessage = inviteMessage.replaceAll("%%USERNAME%%", username);
                            String inviteLink = getString(R.string.invite_link);
                            inviteLink = inviteLink.replaceAll("%%LINK%%", shortLink);
                            String inviteSubject = getString(R.string.invite_subject);

                            List<Intent> targetShareIntents = new ArrayList<>();
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            List<ResolveInfo> resInfos = getPackageManager().queryIntentActivities(shareIntent, 0);

                            if(!resInfos.isEmpty()) {
                                Log.w(TAG, "Have package");
                                for(ResolveInfo resInfo : resInfos) {
                                    String packageName = resInfo.activityInfo.packageName;
                                    Log.i("Package Name", packageName);
                                    if(packageName.contains("mms") ||
                                            packageName.contains("com.android.email") ||
                                            packageName.contains("com.twitter.android") ||
                                            packageName.contains("com.facebook") ||
                                            packageName.contains("android.gm") ||
                                            packageName.contains("com.whatsapp") ||
                                            packageName.contains("apps.docs")) {

                                        Intent intent = new Intent();
                                        intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                                        intent.setAction(Intent.ACTION_SEND);
                                        intent.setType("text/plain");

                                        if(packageName.contains("mms")) {
                                            // Text Messages
                                            intent.putExtra(Intent.EXTRA_TEXT, inviteMessage + "\n" + inviteLink);
                                        } else if(packageName.contains("com.android.email")) {
                                            // Android Default Email
                                            // intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(html));
                                            intent.putExtra(Intent.EXTRA_TEXT, inviteMessage + "\n" + inviteLink);
                                            intent.putExtra(Intent.EXTRA_SUBJECT, inviteSubject);
                                            intent.setType("message/rfc822");
                                            // intent.setType("text/html");
                                        } else if(packageName.contains("com.twitter.android")) {
                                            // Twitter
                                            intent.putExtra(Intent.EXTRA_TEXT, inviteMessage + "\n" + inviteLink);
                                        } else if(packageName.contains("com.facebook")) {
                                            // Facebook and Facebook Messenger
                                            intent.putExtra(Intent.EXTRA_TEXT, inviteMessage + "\n" + inviteLink);
                                        } else if(packageName.contains("android.gm")) {
                                            // GMail
                                            intent.putExtra(Intent.EXTRA_TEXT, inviteMessage + "\n" + inviteLink);
                                            intent.putExtra(Intent.EXTRA_SUBJECT, inviteSubject);
                                            intent.setType("message/rfc822");
                                        } else if(packageName.contains("com.whatsapp")) {
                                            // Whatsapp
                                            intent.putExtra(Intent.EXTRA_TEXT, inviteMessage + "\n" + inviteLink);
                                        } else if(packageName.contains("apps.docs")) {
                                            // Clipboard and Google Drive
                                            intent.putExtra(Intent.EXTRA_TEXT, shortLink);
                                        }

                                        intent.setPackage(packageName);
                                        targetShareIntents.add(intent);
                                    }
                                }

                                if(!targetShareIntents.isEmpty()) {
                                    Log.w(TAG, "Have Intent");
                                    Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0),
                                            getString(R.string.invite_title));
                                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                                    startActivityForResult(chooserIntent, REQUEST_INVITE);
                                } else {
                                    Log.e(TAG, "Do not Have Intent");
                                }
                            }

                        } else {
                            Log.e(TAG, "Short link couldn't be created");
                            showSnackBar(getString(R.string.check_connection));
                        }
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if(requestCode == REQUEST_UPLOAD || requestCode == REQUEST_UPLOAD_DEVICE) {
            if(resultCode == RESULT_OK) {
                final Uri uri = data.getData();
                final int request = requestCode;

                String title = getString(R.string.change_image);
                String message = getString(R.string.change_image_message);
                String negative = getString(R.string.no);
                String positive = getString(R.string.yes);

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setCancelable(false);
                builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        spinnerLayout.setVisibility(View.VISIBLE);

                        try {
                            String path = getPath(uri);
                            if(request == REQUEST_UPLOAD)
                                beginLoginIconUpload(path);
                            else
                                beginUpload(tempKeyOnResult, path);

                        } catch (URISyntaxException e) {
                            showSnackBar(getString(R.string.file_not_found));
                            Log.e(TAG, "Unable to upload file from the given uri", e);
                        }

                        dialog.dismiss();
                    }
                }).show();
            }

        } else if(requestCode == REQUEST_INVITE) {
            if(resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: Invitation send");
            } else {
                Log.e(TAG, "onActivityResult: Invitation error");
            }
        }
    }

    @SuppressLint("NewApi")
    private String getPath(Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[] {
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor;
            try {
                cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (cursor.moveToFirst()) {
                        String path = cursor.getString(column_index);
                        cursor.close();
                        return path;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Can´t get Cursor", e);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void onBackPressed() {
        if(devices.size() != 0)
            moveTaskToBack(true);
        else
            super.onBackPressed();
    }
}
