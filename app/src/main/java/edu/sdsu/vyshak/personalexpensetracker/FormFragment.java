package edu.sdsu.vyshak.personalexpensetracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class FormFragment extends Fragment implements AdapterView.OnItemSelectedListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    ArrayList<String> countries = new ArrayList<>();
    ArrayList<String> states = new ArrayList<>();
    ArrayList<String> years = new ArrayList<>();

    private String userName;
    private String email;
    private String passwordChosen;
    private String currency;
    private String phone;
    private String TAG="Form Fragment";
    private Spinner spinnerCurrency;
    private EditText userNameField;
    private EditText emailField;
    private EditText passwordField;
    private EditText phoneField;
    private static final int REQ_FORM=123;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private ArrayList<String> currencies;
    private View progressView;
    private View formView;
    public FormFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Form.
     */
    public static FormFragment newInstance(String param1, String param2) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                   // getActivity().finish();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        formView = inflater.inflate(R.layout.fragment_form, container, false);
        //this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if(!haveNetworkAccess()){
            Toast.makeText(getActivity(),"Please enable network access",Toast.LENGTH_LONG);
        }
        progressView = formView.findViewById(R.id.formprogress);
        Calendar cal = Calendar.getInstance();
        emailField = (EditText) formView.findViewById(R.id.email);
        phoneField = (EditText) formView.findViewById(R.id.phone);
        passwordField = (EditText) formView.findViewById(R.id.password);
        userNameField=(EditText) formView.findViewById(R.id.textusername);
        spinnerCurrency = (Spinner) formView.findViewById(R.id.spinner_currency);
        spinnerCurrency.setOnItemSelectedListener(this);
        currencies = new ArrayList<>();
        try {
            InputStream currenciesFile = getActivity().getAssets().open("currencyTypes");
            BufferedReader in = new BufferedReader( new InputStreamReader(currenciesFile));
            String line;
            while((line = in.readLine()) != null){
                currencies.add(line);
            }
        } catch (IOException e) {
            Log.e("rew", "read Error", e);
        }
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_list_item_1,currencies);
        spinnerCurrency.setAdapter(currencyAdapter);
        final Button submit = (Button) formView.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                userName = userNameField.getText().toString();
                email=emailField.getText().toString();
                passwordChosen = passwordField.getText().toString();
                phone = phoneField.getText().toString();
                Log.d(TAG,""+userName+passwordChosen+phone+email);
                if(TextUtils.isEmpty(email)) {
                    emailField.setError("Required.");
                }
                else{
                    emailField.setError(null);
                }
                if(TextUtils.isEmpty(phone))
                    phoneField.setError("Required");
                else if(phone.length() != 10)
                    phoneField.setError("Invalid");
                else{
                    phoneField.setError(null);
                }
                if(TextUtils.isEmpty(passwordChosen)){
                    passwordField.setError("Required");
                }
                else if(passwordChosen.length() < 3)
                    passwordField.setError("Min 3 characters");
                else{
                    passwordField.setError(null);
                }
                if(TextUtils.isEmpty(userName)){
                    userNameField.setError("Required");
                }
                else{
                    userNameField.setError(null);
                }
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(passwordChosen) && !TextUtils.isEmpty(userName) && passwordChosen.length()>3
                        && phone.length() == 10) {
                    postData();
                }
                else Toast.makeText(getActivity(), "Please correct errors and try again",
                        Toast.LENGTH_LONG).show();

            }
        });
        return formView;
    }

    @Override
    public void onStop() {
        super.onStop();
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    public void onButtonPressed(Uri uri) {
       /* if (mListener != null) {
           // mListener.onFragmentInteraction(uri);
        }*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Log.d("Listener", "onItemSelected: ");
        if(parent.getId()== R.id.spinner_currency) {
            currency = (String) parent.getItemAtPosition(position);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void postData() {
        showProgress(true);
        auth.createUserWithEmailAndPassword(email, passwordChosen)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), R.string.account_fail,
                                    Toast.LENGTH_SHORT).show();
                            showProgress(false);

                        } else {
                            dbRef = FirebaseDatabase.getInstance().getReference();
                        dbRef.child("users/" + user.getUid()).child("email").setValue(email);
                        dbRef.child("users/" + user.getUid()).child("name").setValue(userName);
                        dbRef.child("users/" + user.getUid()).child("phone").setValue(phone);
                        dbRef.child("users/" + user.getUid()).child("currency").setValue(currency);
                        dbRef.child("users/" + user.getUid()).child("uid").setValue(user.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {

                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(userName)
                                            .build();
                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Snackbar.make(getView(), "User profile Created", Snackbar.LENGTH_LONG)
                                                                .setAction("Action", null).show();
                                                        showProgress(false);
                                                        auth.signOut();
                                                        getActivity().finish();
                                                    }
                                                }
                                            });
                                }

                            }
                        });
                    }
                    }
                });

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            formView.setVisibility(show ? View.GONE : View.VISIBLE);
            formView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    formView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            formView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    public boolean haveNetworkAccess() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (!(networkInfo != null && networkInfo.isConnected())) {
            return false;
        }
        return true;
    }



}
