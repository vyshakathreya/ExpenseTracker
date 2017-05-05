package edu.sdsu.vyshak.personalexpensetracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener  {
    private static final String TAG = "Login Activity";
    private TextView statusTextView;
    private EditText emailField;
    private EditText passwordField;
    private CheckBox rememberCheck;
    private CheckBox showpasswordCheck;
    private View progressView;
    private View loginFormView;
    public static String PREFS_NAME="mypre";
    public static String PREF_USERNAME="username";
    public static String PREF_PASSWORD="password";

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    FormFragment fragmentForm = new FormFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        statusTextView = (TextView) findViewById(R.id.status);
        emailField = (EditText) findViewById(R.id.field_email);
        passwordField = (EditText) findViewById(R.id.field_password);
        rememberCheck = (CheckBox) findViewById(R.id.rememberMe);
        showpasswordCheck = (CheckBox) findViewById(R.id.showPassword);
        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);

        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        if(rememberCheck.isChecked()){
            SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
            String username = pref.getString(PREF_USERNAME, null);
            String password = pref.getString(PREF_PASSWORD, null);
            if( username!= null && password != null) {
                emailField.setText(username);
                passwordField.setText(password);
            }
        }
        if (showpasswordCheck.isChecked()){
            passwordField.setTransformationMethod(null);
            passwordField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            Log.d(TAG,"password checked");
        }
        else{
            passwordField.setTransformationMethod(new PasswordTransformationMethod());
            passwordField.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                    Intent intent = new Intent(LoginActivity.this,SummaryList.class);
                    startActivity(intent);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    updateUI(user);
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        if(rememberCheck.isChecked()){
            getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                    .edit()
                    .putString(PREF_USERNAME,emailField.getText().toString())
                    .putString(PREF_PASSWORD,passwordField.getText().toString())
                    .commit();
            Log.d(TAG,"saving data");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    private void createAccount() {
        Log.d(TAG, "createAccount: putting fragment" );
        FragmentManager fragments = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragments.beginTransaction();
        fragmentTransaction.replace(R.id.content, fragmentForm);
        fragmentTransaction.commit();
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgress(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        if(task.isSuccessful()){
                            Intent intent = new Intent(LoginActivity.this,SummaryList.class);
                            startActivity(intent);
                            showProgress(false);
                        }
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            if(!haveNetworkAccess())
                                Toast.makeText(LoginActivity.this, R.string.enableNetwork,
                                        Toast.LENGTH_SHORT).show();
                            else
                            Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            showProgress(false);
                        }
                        if (!task.isSuccessful()) {
                            if(!haveNetworkAccess())
                                Toast.makeText(LoginActivity.this, R.string.enableNetwork,
                                        Toast.LENGTH_SHORT).show();
                            else
                            statusTextView.setText(R.string.auth_failed);
                            showProgress(false);
                        }
                    }
                });
    }

    /*private void getBack() {
        Intent intent = this.getIntent();
        this.setResult(RESULT_OK,intent);
        finish();
    }*/

    public void signOut() {
        auth = FirebaseAuth.getInstance();
        auth.signOut();
        updateUI(null);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = emailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Required.");
            valid = false;
        } else {
            emailField.setError(null);
        }

        String password = passwordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Required.");
            valid = false;
        } else {
            passwordField.setError(null);
        }
        return valid;
    }

    private void updateUI(FirebaseUser user) {

        statusTextView.setText(R.string.signed_out);
        findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
        findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);

    }

    public boolean haveNetworkAccess() {
        Log.d(TAG,"checking network");
        ConnectivityManager connMgr = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (!(networkInfo != null && networkInfo.isConnected())) {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            createAccount();
        } else if (i == R.id.email_sign_in_button) {
            if(rememberCheck.isChecked()){
                getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                        .edit()
                        .putString(PREF_USERNAME,emailField.getText().toString())
                        .putString(PREF_PASSWORD,passwordField.getText().toString())
                        .commit();
                Log.d(TAG,"saving data");
            }
            signIn(emailField.getText().toString(), passwordField.getText().toString());
        }
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

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
   /* public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
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
    }*/
}

