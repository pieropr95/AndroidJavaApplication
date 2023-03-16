package test.pprivera.com.testapplicationtraccar3;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RegisterActivity extends AppCompatActivity {

    private final Context context = RegisterActivity.this;
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private static final int SEND_EMAIL = 0;
    private static final int SHOW_ALERT_DIALOG = 1;

    private ProgressDialog progress;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_EMAIL:
                    String [] params = (String []) msg.obj;

                    final String name = params[0];
                    final String email = params[1];
                    String alink = "http://track.csticloud.biz:7051/activate/" + params[2];

                    String dynamicLink = getString(R.string.firebase_dynamic_link);

                    FirebaseDynamicLinks.getInstance().createDynamicLink()
                            .setLink(Uri.parse(alink))
                            .setDynamicLinkDomain(dynamicLink)
                            .buildShortDynamicLink()
                            .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                                @Override
                                public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                    if (task.isSuccessful()) {
                                        // Short link created
                                        String shortLink = task.getResult().getShortLink().toString();
                                        // Uri flowchartLink = task.getResult().getPreviewLink();
                                        Log.e(TAG, "shortLink: " + shortLink);
                                        new ActivationEmailTask().execute(name, email, shortLink);

                                    } else {
                                        Log.e(TAG, "Short link couldn't be created");
                                        showSnackBar(getString(R.string.check_connection));
                                        progress.dismiss();
                                    }
                                }
                            });
                    break;

                case SHOW_ALERT_DIALOG:
                    String key = (String) msg.obj;

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context,
                            R.style.Theme_AppCompat_Light_Dialog_Alert);

                    String title = "Welcome, " + key;
                    String message = "Thank you for joining us. We've sent you a confirmation link to your email.";
                    String positive = getString(R.string.ok);

                    alertBuilder.setTitle(title);
                    alertBuilder.setMessage(message);
                    alertBuilder.setCancelable(false);
                    alertBuilder.setPositiveButton(positive, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = alertBuilder.create();
                    dialog.show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        final EditText nameText = (EditText) findViewById(R.id.name);
        final EditText usernameText = (EditText) findViewById(R.id.email);
        final EditText passwordText = (EditText) findViewById(R.id.password);

        progress = new ProgressDialog(context);
        progress.setMessage(getString(R.string.loading_message));
        progress.setCancelable(false);
        progress.setIndeterminate(true);

        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Register
                String name = nameText.getText().toString();
                String email = usernameText.getText().toString();
                String password = passwordText.getText().toString();

                if(name.length() == 0) {
                    showSnackBar(getString(R.string.invalid_name));
                    return;
                }

                if(!email.contains("@")) {
                    showSnackBar(getString(R.string.invalid_email));
                    return;
                }

                if(password.length() < 4) {
                    showSnackBar(getString(R.string.invalid_password));
                    return;
                }

                RegisterActivity.PostUsersTask task = new RegisterActivity.PostUsersTask();
                task.execute(name, email, password);
                progress.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
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

    private class PostUsersTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
        }

        @Override
        protected Boolean doInBackground(String... cred) {
            String name = cred[0];
            String email = cred[1];
            String pass = cred[2];
            String auth = "Basic aW5ub3ZhYXBwczR5b3VAZ21haWwuY29tOlBhJCR3MHJk";

            String json_request = "{\n" +
                    "\"name\": \"" + name + "\",\n" +
                    "\"email\": \"" + email + "\",\n" +
                    "\"password\": \"" + pass + "\",\n" +
                    "\"disabled\": true\n" +
                    "}";
            String json_response = "";
            String url = MainActivity.MY_URL + MainActivity.PATH_USERS;

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, json_request);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("authorization", auth)
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "899201db-59e3-b418-cc24-622043664370")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();

                int status = response.code();
                Log.e("Response", "Status: " + status);
                if(status == 401) return false;
                else if(status == 400) {
                    if(responseBody != null) json_response = responseBody.string();
                    Log.e("Response 400 Error", json_response);

                    if(json_response.contains("uk_user_email")) {
                        Log.e(TAG, "This account has already been created");

                        showSnackBar(getString(R.string.repeated_email));
                        return false;
                    } else {
                        showToast(getString(R.string.invalid_code));
                    }

                    return false;
                }

                if(responseBody != null) json_response = responseBody.string();
                Log.e("Response", json_response);

                JSONObject obj = new JSONObject(json_response);
                String id = obj.getString("id");
                Log.e("Response", "ID: " + id);

                String [] params = {name, email, id};
                handler.obtainMessage(SEND_EMAIL, params).sendToTarget();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Unable to retrieve data. URL may be invalid.");
                showSnackBar(getString(R.string.check_connection));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("Response", "Result: " + result);
            if(!result) {
                progress.dismiss();
            }
        }
    }

    private class ActivationEmailTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String name = params[0];
            String email = params[1];
            String alink = params[2];
            String auth = "Basic aW5ub3ZhYXBwczR5b3VAZ21haWwuY29tOlBhJCR3MHJk";

            String json_request = "{\n" +
                    "\"name\": \"" + name + "\",\n" +
                    "\"email\": \"" + email + "\",\n" +
                    "\"alink\": \"" + alink + "\"\n" +
                    "}";

            String xml_request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                    "xmlns:ws=\"http://ws.connectors.connect.mirth.com/\">\n" +
                    "   <soapenv:Header/>\n" +
                    "   <soapenv:Body>\n" +
                    "      <ws:acceptMessage>\n" +
                    "         <arg0>" + json_request + "</arg0>\n" +
                    "      </ws:acceptMessage>\n" +
                    "   </soapenv:Body>\n" +
                    "</soapenv:Envelope>";

            String xml_response = "";
            String url = "http://track.csticloud.biz:7050/services/sendConfirmationMail?wsdl";

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("text/xml");
            RequestBody body = RequestBody.create(mediaType, xml_request);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("authorization", auth)
                    .addHeader("content-type", "text/xml")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "6068a689-9cda-223a-65c7-0650c38fe399")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();

                int status = response.code();
                Log.e("Response", "Status: " + status);
                if(status != 200) return false;

                if(responseBody != null) xml_response = responseBody.string();
                Log.d("Response", xml_response);

                handler.obtainMessage(SHOW_ALERT_DIALOG, name).sendToTarget();

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Unable to retrieve data. URL may be invalid.", e);
                showSnackBar(getString(R.string.check_connection));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("Response", "Result: " + result);
            progress.dismiss();
        }
    }
}
