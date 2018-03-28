package edu.sdsu.vyshak.personalexpensetracker.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.sdsu.vyshak.personalexpensetracker.fragment.FormFragment;
import edu.sdsu.vyshak.personalexpensetracker.R;

/**
 *
 * A login screen that offers login via email/password.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener  {
    private static final String TAG = "Login Activity";
    private TextView statusTextView;
    private EditText emailField;
    private EditText passwordField;
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
        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);

        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
            SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
            String username = pref.getString(PREF_USERNAME, null);
            String password = pref.getString(PREF_PASSWORD, null);
            if( username!= null && password != null) {
                emailField.setText(username);
                passwordField.setText(password);
            }
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                    Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                    startActivity(intent);
                } else {
                    updateUI(user);
                }
            }
        };

            getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                    .edit()
                    .putString(PREF_USERNAME,emailField.getText().toString())
                    .putString(PREF_PASSWORD,passwordField.getText().toString())
                    .apply();
    }

    /*
    * Ensure user is logged in when the application comes from background
    * */
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

    /**
     * Transitions to a account creation form.
     *
     */
    private void createAccount() {
        FragmentManager fragments = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragments.beginTransaction();
        fragmentTransaction.replace(R.id.content, fragmentForm);
        fragmentTransaction.commit();
    }

    /**
     * Sign in is based on Email-password authentication in the firebase.
     *Form is validated before submission. Internet availabiltiy is checked before proceeding.
     */
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
                        if(task.isSuccessful()){
                            Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
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

    /**
     * Ensuring form is not empty saves from null pointers.
     *
     */
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

    /**
     * Initialize the status text view.
     *
     */
    private void updateUI(FirebaseUser user) {

        statusTextView.setText(R.string.signed_out);
        findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);

    }


    /**
     *Check for network access
     *
     * @return boolean true if the network is available, false otherwise.
     *
     */
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

    /**
     * Store only the user name
     *Modified to use apply instead of commit for the shared preferences
     * Apply runs in the background instead of running immediately.
     */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            createAccount();
        } else if (i == R.id.email_sign_in_button) {
                getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                        .edit()
                        .putString(PREF_USERNAME,emailField.getText().toString())
                        .putString(PREF_PASSWORD,passwordField.getText().toString())
                        .apply();
            signIn(emailField.getText().toString(), passwordField.getText().toString());
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     * Only for Api higher than honeycomb.
     *
     */

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
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

}

