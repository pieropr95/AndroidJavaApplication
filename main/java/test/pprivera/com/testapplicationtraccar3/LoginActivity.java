package test.pprivera.com.testapplicationtraccar3;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginActivity extends AppCompatActivity {

    private final Context context = LoginActivity.this;
    private static final String TAG = LoginActivity.class.getSimpleName();

    private SessionManager sessionManager;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        sessionManager = new SessionManager(getApplicationContext());

        final EditText emailText = (EditText) findViewById(R.id.email);
        final EditText passwordText = (EditText) findViewById(R.id.password);

        progress = new ProgressDialog(context);
        progress.setMessage(getString(R.string.loading_message));
        progress.setCancelable(false);
        progress.setIndeterminate(true);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Login
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();

                if(!email.contains("@")) {
                    showSnackBar(getString(R.string.invalid_email));
                    return;
                }

                if(password.length() < 4) {
                    showSnackBar(getString(R.string.invalid_password));
                    return;
                }

                PostSessionTask task = new PostSessionTask();
                task.execute(email, password);
                progress.show();
            }
        });

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Register
                Intent intent = new Intent(context, RegisterActivity.class);
                startActivity(intent);
            }
        });
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

    private class PostSessionTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, getClass().getSimpleName());
        }

        @Override
        protected Boolean doInBackground(String... cred) {
            String email = cred[0];
            String pass = cred[1];

            try {
                String emailEncode = URLEncoder.encode(email, "UTF-8");
                String passEncode = URLEncoder.encode(pass, "UTF-8");

                String urlencoded_request = "email=" + emailEncode + "&password=" + passEncode;
                String json_response = "";
                String url = MainActivity.MY_URL + MainActivity.PATH_SESSION;

                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                RequestBody body = RequestBody.create(mediaType, urlencoded_request);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .addHeader("cache-control", "no-cache")
                        .addHeader("postman-token", "8843970e-4be9-3d6d-1418-0d05b26e3d34")
                        .build();

                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();

                int status = response.code();
                Log.e("Response", "Status: " + status);
                if(status == 401 || status == 400) {
                    showToast(getString(R.string.invalid_email_or_password));
                    return false;
                }

                if(responseBody != null) json_response = responseBody.string();
                Log.e("Response", json_response);

                String credentials = email + ":" + pass;
                String credBase64 = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT).replace("\n", "");

                JSONObject obj = new JSONObject(json_response);

                Log.d("Session Params", "ID: " + obj.getInt("id"));
                Log.d("Session Params", "Name: " + obj.getString("name"));
                Log.d("Session Params", "Email: " + obj.getString("email"));
                Log.d("Session Params", "Admin: " + obj.getBoolean("admin"));
                Log.d("Session Params", "Expiration Time: " + obj.getString("expirationTime"));
                Log.d("Session Params", "Device Limit: " + obj.getInt("deviceLimit"));

                sessionManager.createLoginSession(obj.getString("name"), email, pass, credBase64, obj.getInt("id") + "");

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Unable to retrieve data. URL may be invalid.", e);
                showSnackBar(getString(R.string.check_connection));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.e("Response", "Result: " + result);
            progress.dismiss();

            if(result) {
                Intent intent = new Intent(context, ManagerActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
