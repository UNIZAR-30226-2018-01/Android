package com.csd.MeWaT.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import com.csd.MeWaT.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import android.content.Context;
import android.util.Log;

/**
 * A login screen that offers login via User/password.
 */
public class LoginActivity extends AppCompatActivity {


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;
    private TextView notRegistered;
    private View mProgressView;
    private View mLoginFormView;
    private String idSesion,Username;
    static final int USER_SIGNUP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: Escribir if version android >api 15
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.INTERNET}
                , 150);



        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        if(sp.getBoolean("userAuthed",false)){
            Intent MainActivity = new Intent( LoginActivity.this, MainActivity.class);
            MainActivity.putExtra("idSesion",sp.getString("idSesion",""));
            MainActivity.putExtra("user",sp.getString("username",""));
            LoginActivity.this.startActivity(MainActivity);
            finish();
        }
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUserView = (EditText) findViewById(R.id.User);


        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUserSignInButton = (Button) findViewById(R.id.User_sign_in_button);
        mUserSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        final Activity i = this;
        notRegistered = (TextView) findViewById(R.id.link_signup);
        notRegistered.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent SignUpActivity = new Intent(i,SignUpActivity.class);
                i.startActivityForResult(SignUpActivity,USER_SIGNUP);
            }
        });

        mLoginFormView = findViewById(R.id.singup_form);
        mProgressView = findViewById(R.id.login_progress);

    }




    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid User, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String User = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the User entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid User address.
        if (TextUtils.isEmpty(User)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        } else if (!isUserValid(User)) {
            mUserView.setError(getString(R.string.error_invalid_User));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the User login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(User, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUserValid(String User) {
        //TODO: Replace this with your own logic
        return User.length() >= 4;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    public static Boolean connectionIsHttps (String urlString){
        if (urlString.regionMatches(0, "https", 0, 5)){
            return true;
        }
        else {
            return false;
        }
    }

    public static String getHostNameFromUrl (String urlString){
        if (connectionIsHttps(urlString)){
            return urlString.substring(8,urlString.indexOf("/", 8));
        }
        else{
            return urlString.substring(7,urlString.indexOf("/", 7));
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the User.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;

        UserLoginTask(String User, String password) {
            mUser = User;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            URL url;
            HttpsURLConnection client = null;
            InputStreamReader inputStream;
            String title,info1;InputStream info;


            try {
                url = new URL("https://mewat1718.ddns.net/ps/IniciarSesion");

                client = (HttpsURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("", System.getProperty("https.agent"));
                client.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
                client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                client.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("nombre", mUser)
                        .appendQueryParameter("contrasenya", mPassword);
                String query = builder.build().getEncodedQuery();

                OutputStream os = client.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                int responseCode = client.getResponseCode();
                System.out.println("\nSending 'Get' request to URL : " +    url+"--"+responseCode);
            } catch (MalformedURLException e) {
                return false;
            } catch (SocketTimeoutException e) {
                return false;
            }catch (IOException e) {
                info = client.getErrorStream();
                return false;

            }
            try {
                inputStream = new InputStreamReader(client.getInputStream());


                BufferedReader reader = new BufferedReader(inputStream);
                StringBuilder builder = new StringBuilder();

                for (String line = null; (line = reader.readLine()) != null ; ) {
                    builder.append(line).append("\n");
                }

                // Parse into JSONObject
                String resultStr = builder.toString();
                JSONTokener tokener = new JSONTokener(resultStr);
                JSONObject result = new JSONObject(tokener);
                client.disconnect();

                if (!result.has("error")){
                    Username = (String) result.get("login");
                    idSesion = (String) result.get("idSesion");
                }else{
                    return false;
                }


            }catch (IOException e){
                System.out.println(e);
                return false;
            } catch (JSONException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                SharedPreferences p = getPreferences(Context.MODE_PRIVATE);

                p.edit().putBoolean("userAuthed",true).apply();
                p.edit().putString("idSesion",idSesion).apply();
                p.edit().putString("username",Username).apply();

                Intent MainActivity = new Intent( LoginActivity.this, MainActivity.class);
                MainActivity.putExtra("idSesion",idSesion);
                MainActivity.putExtra("user",Username);
                LoginActivity.this.startActivity(MainActivity);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == USER_SIGNUP){
            if(resultCode == Activity.RESULT_OK){
                Intent returnIntent = new Intent();
                String pass = returnIntent.getStringExtra("pass");
                String user = returnIntent.getStringExtra("user");
                mAuthTask = new UserLoginTask(user,pass);
                mAuthTask.execute();
            }else{
                Toast.makeText(this.getApplicationContext(), "Error Signing Up", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

