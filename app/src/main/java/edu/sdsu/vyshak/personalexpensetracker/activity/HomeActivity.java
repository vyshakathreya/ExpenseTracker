package edu.sdsu.vyshak.personalexpensetracker.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.sdsu.vyshak.personalexpensetracker.R;
import edu.sdsu.vyshak.personalexpensetracker.bean.UserAlerts;
import edu.sdsu.vyshak.personalexpensetracker.fragment.SummaryListFragment;
import edu.sdsu.vyshak.personalexpensetracker.bean.User;
import edu.sdsu.vyshak.personalexpensetracker.sync.VolleyQueue;
import edu.sdsu.vyshak.personalexpensetracker.adapter.CustomAccountsAdapter;
import edu.sdsu.vyshak.personalexpensetracker.adapter.CustomUserAlertsAdapter;
import edu.sdsu.vyshak.personalexpensetracker.bean.Accounts;
import edu.sdsu.vyshak.personalexpensetracker.bean.Display;
import edu.sdsu.vyshak.personalexpensetracker.bean.Expenses;
import edu.sdsu.vyshak.personalexpensetracker.data.DBHelper;

/**
 * Created by Vyshak on 5/9/2017.
 * This class controls the operations on the home screen.
 *
 */

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView userEmail;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference dbAccRef;
    private TextView loggedUser,overallBalance,currencyTextView,currencyValue;
    private EditText accountNickname, expenseDescription, amountSpent;
    private int accountCount;
    private String TAG="home";
    private String accountName, accountType, userCurrency,monthStart;
    private Spinner accountTypeSpinner,expenseType,modeofPayment,usedCurrency,expenseIncome;
    private Context context = this;
    public ArrayList<String> userAccounts = new ArrayList<>();
    private DBHelper mydb;
    private CustomUserAlertsAdapter alertsAdapter;
    private ArrayList<UserAlerts> userAlertsArray;
    private SimpleDateFormat formatter;
    private double conversionValue;
    private float balanceinaccount,convertedAmount;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * OnCreate method initializes the canvas. Gets the currency conversion for the balance available.
     * Sets the layout with buttons and slider.
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mydb = new DBHelper(this);
        Calendar today = Calendar.getInstance();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        formatter = new SimpleDateFormat("dd-MMM-yyyy");
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerLayout = navigationView.getHeaderView(0);

        loggedUser = (TextView) headerLayout.findViewById(R.id.loggedUser);
        userEmail = (TextView) headerLayout.findViewById(R.id.userEmail);
        currencyTextView = (TextView) findViewById(R.id.textView_currency);

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user.getDisplayName() != null) {
                    loggedUser.setVisibility(View.VISIBLE);
                    userEmail.setVisibility(View.VISIBLE);
                    loggedUser.setText("Hi, " + user.getDisplayName());
                    userEmail.setText(user.getEmail());
                } else {
                    loggedUser.setVisibility(View.INVISIBLE);
                    auth.signOut();
                    finish();
                }
            }
        };
        if (user != null) {
            if(user.getDisplayName() != null)
                loggedUser.setVisibility(View.VISIBLE);
            userEmail.setVisibility(View.VISIBLE);
            loggedUser.setText("Hi, " + user.getDisplayName());
            userEmail.setText(user.getEmail());
            userAccounts.add("cash");
            userAccounts.addAll(mydb.getAllAccounts(user.getUid()));
        } else {
            loggedUser.setVisibility(View.INVISIBLE);
            auth.signOut();
            finish();
        }
        updateDB();
        if(userCurrency != null) {
            userCurrency = mydb.getuserinfo("currency", user.getEmail());
            getCurrencyRate();
        }
        userAccounts.add("cash");
        userAccounts.addAll(mydb.getAllAccounts(user.getUid()));
        accountCount = userAccounts.size() ;

        currencyValue = (TextView) findViewById(R.id.balanceinmycurrency);
        userCurrency = mydb.getuserinfo("currency", user.getEmail());

        balanceinaccount=mydb.totalBalance(user.getUid());
        overallBalance = (TextView) findViewById(R.id.textView_balance);
        overallBalance.setText(String.valueOf(mydb.totalBalance(user.getUid())));

        today.set(Calendar.DAY_OF_MONTH,1);
        monthStart = String.valueOf(today.get(Calendar.YEAR) +"-"+ (today.get(Calendar.MONTH)+1) +"-"+ today.get(Calendar.DAY_OF_MONTH));
        ListView paymentslist = (ListView) findViewById(R.id.paymentlist);

        userAlertsArray = new ArrayList<>();
        userAlertsArray.addAll(mydb.getAlerts(monthStart));
        alertsAdapter = new CustomUserAlertsAdapter(userAlertsArray, this);
        paymentslist.setAdapter(alertsAdapter);

        TextView notify = (TextView) findViewById(R.id.notifynothing);
        if(userAlertsArray.size()<0){
            notify.setVisibility(View.VISIBLE);
        }else
            notify.setVisibility(View.INVISIBLE);

        Button setAlerts = (Button) findViewById(R.id.button_alerts);
        setAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alertIntent = new Intent(HomeActivity.this,SetBudgetActivity.class);
                startActivity(alertIntent);
            }
        });

        Button shoppingCartButton = (Button) findViewById(R.id.button_shoppingCart);
        shoppingCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shoppingIntent = new Intent(HomeActivity.this,ShoppingListActivity.class);
                startActivity(shoppingIntent);
            }
        });

        Button incomeExpenseButton = (Button) findViewById(R.id.button_addExpense);
        incomeExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addExpenseIntent = new Intent(HomeActivity.this,AddExpenseActivity.class);
                startActivity(addExpenseIntent);
            }
        });

        Button addAccountButton = (Button) findViewById(R.id.button_addAccount);
        addAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAccount();
            }
        });
    }

    /**
     * Connects to the api fixer.io to get the conversion factors with base currency as USD
     */
    public void getCurrencyRate(){
        Response.Listener<JSONObject> success_state = new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                try {
                    JSONObject dataObj = response.getJSONObject("rates");
                    double convertAmount = (double) dataObj.get(userCurrency.substring(0,3));
                    conversionValue = convertAmount;
                    updateRate(convertAmount);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("statefail", error.toString());

            }
        };
        String url = "http://api.fixer.io/latest?base=USD";
        JsonObjectRequest getRequestState = new JsonObjectRequest(url,null,success_state,failure);
        VolleyQueue.instance(this).add(getRequestState);

    }

    /**
     *Uses the conversion factor to compute and update the UI
     * @param convertAmount double valued conversion factor
     *If home currency is not selected, disables the panel.
     */
    private void updateRate(double convertAmount) {

        convertedAmount = (float) (convertAmount*balanceinaccount);
        conversionValue= convertAmount;
        if (userCurrency != null) {
            currencyTextView.setText(userCurrency);
            currencyValue.setText(String.valueOf(convertedAmount));
        }
        else {
            currencyTextView.setVisibility(View.INVISIBLE);
            currencyValue.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Reload the balances and graph upon coming back from the activity stack
     *
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        userAlertsArray.clear();
        userAlertsArray.addAll(mydb.getAlerts(monthStart));
        alertsAdapter.notifyDataSetChanged();
        overallBalance.setText(String.valueOf(mydb.totalBalance(user.getUid())));
        currencyValue.setText(String.valueOf((float) conversionValue*mydb.totalBalance(user.getUid())));

        if (userCurrency != null) {
            currencyTextView.setText(userCurrency);
            currencyValue.setText(String.valueOf(convertedAmount));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        balanceinaccount=mydb.totalBalance(user.getUid());
        overallBalance.setText(String.valueOf(mydb.totalBalance(user.getUid())));
        userAlertsArray.clear();
        userAlertsArray.addAll(mydb.getAlerts(monthStart));
        alertsAdapter.notifyDataSetChanged();
        userCurrency = mydb.getuserinfo("currency", user.getEmail());
        if(userCurrency != null) {
            getCurrencyRate();
        }
        currencyTextView.setText(userCurrency);
        currencyValue.setText(String.valueOf((float) (conversionValue*mydb.totalBalance(user.getUid()))));
    }

    /**
     * Close the slider window (drawer) upon clicking back button
     *
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Populate the menu options in the drawer
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    /**
     * Get the selected menu option to change the activity
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *Navigate to the chosen menu option
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_addExpense) {
            if(mydb.gettransactions("Select * from transactionsSummary where uid like "+"\'"+ user.getUid()+"\'").isEmpty()) {
                Snackbar.make(getCurrentFocus(), "Please Add Transactions", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }else {
                Intent addExpenseIntent = new Intent(HomeActivity.this, AddExpenseActivity.class);
                startActivity(addExpenseIntent);
            }
        }else if (id == R.id.nav_accountSummary) {
            if(mydb.gettransactions("Select * from transactionsSummary where uid like "+"\'"+ user.getUid()+"\'").isEmpty()) {
                Snackbar.make(getCurrentFocus(), "Please Add Transactions", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }else{
                Intent summaryIntent = new Intent(HomeActivity.this,SummaryListFragment.class);
                startActivity(summaryIntent);
            }
        }else if (id == R.id.shopList){
            Intent shoppingIntent = new Intent(HomeActivity.this,ShoppingListActivity.class);
            startActivity(shoppingIntent);
        }else if (id == R.id.logout){
            auth.signOut();
            finish();
        }else if(id == R.id.nav_addAccount){
           addAccount();
        }else if(id == R.id.nav_manageAccounts){
            manageAccounts();
        }else if(id == R.id.nav_retriveData){
            if(!mydb.gettransactions("Select * from transactionsSummary where uid like "+"\'"+ user.getUid()+"\'").isEmpty()) {
                Snackbar.make(getCurrentFocus(), "Updated !!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }else {
                retrieveData();
            }
        }
        else if(id == R.id.setBudget){
            Intent setBudgetIntent = new Intent(HomeActivity.this,SetBudgetActivity.class);
            startActivity(setBudgetIntent);
        }
        else if(id == R.id.graphical){
            Intent graphIntent = new Intent(HomeActivity.this,PlotsActivity.class);
            startActivity(graphIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Retrieve the data from the firebase
     *
     */
    private void retrieveData() {
        DatabaseReference people = FirebaseDatabase.getInstance().getReference();
        people.child("accounts").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (user.getDisplayName() != null) {

                    if (dataSnapshot.hasChild(user.getDisplayName())) {
                        FirebaseDatabase.getInstance().getReference().child("accounts").child(user.getDisplayName()).getRef().addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                Accounts chatMessage = dataSnapshot.getValue(Accounts.class);
                                mydb.insertAccount(user.getUid(), chatMessage.getAccountName(), chatMessage.getAccountType(), chatMessage.getAccountName() + "-" + chatMessage.getAccountType());
                                getCurrencyRate();
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {
                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        people.child("IncomesExpense").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (String users : userAccounts) {
                    if (dataSnapshot.hasChild(user.getUid() + "-" + users)) {
                        FirebaseDatabase.getInstance().getReference().child("IncomesExpense").child(user.getUid() + "-" + users).getRef()
                                .addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        Expenses chatMessage = dataSnapshot.getValue(Expenses.class);
                                        Date homeDate = new Date(chatMessage.getDate());
                                        mydb.insertTransaction(user.getUid(),chatMessage.getAccount(),chatMessage.getAmount(),chatMessage.getCurrency(),
                                                chatMessage.getTranstype(),chatMessage.getCategory(), String.valueOf(homeDate),chatMessage.getDesc());
                                        overallBalance.setText(String.valueOf(mydb.totalBalance(user.getUid())));
                                        Snackbar.make(getCurrentFocus(), "Data Saved Successfully", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();

                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Remove selected accounts from a list, displayed using a dialog
     *
     */
    private void manageAccounts() {
        final Dialog dialog = new Dialog(context);
        final String[] toremove = new String[1];
        dialog.setContentView(R.layout.content_manage_accounts);
        dialog.setTitle("Modify Accounts");
        final ListView listView = (ListView) dialog.findViewById(R.id.accounts_all_list);
        final ArrayList<Display> userAccountsDisplay= new ArrayList<>();
        final Display[] dispremove = new Display[1];
        userAccountsDisplay.addAll(mydb.getAllUserAccounts(user.getUid()));
        final CustomAccountsAdapter listAdapter = new CustomAccountsAdapter(userAccountsDisplay,this);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dispremove[0] = userAccountsDisplay.get(position);
                toremove[0] = dispremove[0].getDisplayname();
            }
        });
        Button dialogButtonOK = (Button) dialog.findViewById(R.id.account_remove_OK);
        dialogButtonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mydb.removeAccount(toremove[0],user.getUid());
                dbAccRef = FirebaseDatabase.getInstance().getReference();
                dbAccRef.child("accounts/" + user.getDisplayName()).child(toremove[0]).removeValue();
                userAccountsDisplay.remove(dispremove[0]);
                listAdapter.notifyDataSetChanged();
            }
        });
        Button dialogButtonCancel = (Button) dialog.findViewById(R.id.account_remove_cancel);
        dialogButtonCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    /**
     * Create dialog fields to add a new account.
     * For 2 fields creating a fragment is to spacious.
     *
     */
    private void addAccount() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.add_account);
        dialog.setTitle("Add Account");
        List<String> accountTypes = new ArrayList<String>();
        try {
            InputStream accountsFile = this.getAssets().open("accountTypes");
            BufferedReader in = new BufferedReader( new InputStreamReader(accountsFile));
            String line;
            while((line = in.readLine()) != null){
                accountTypes.add(line);
            }
        } catch (IOException e) {
            Log.e("rew", "read Error", e);
        }
        accountNickname = (EditText) dialog.findViewById(R.id.accountName);
        accountTypeSpinner = (Spinner) dialog.findViewById(R.id.accountType);
        ArrayAdapter<String> accountSpinnerAdapter = new ArrayAdapter<>(context,android.R.layout.simple_spinner_item,accountTypes);
        accountSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountTypeSpinner.setAdapter(accountSpinnerAdapter);
        accountType=accountTypes.get(0);
        accountTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                accountType= parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button dialogButton = (Button) dialog.findViewById(R.id.addAccountButton);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountName=accountNickname.getText().toString();
                if(TextUtils.isEmpty(accountName)) {
                    Snackbar.make(v, "Please Enter Account Name", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else{
                    if(accountType != null){
                        userAccounts.add(accountName+"-"+accountType);
                        mydb.insertAccount(user.getUid(),accountName,accountType,accountName+"-"+accountType);
                        dbAccRef = FirebaseDatabase.getInstance().getReference();
                        dbAccRef.child("accounts/" + user.getDisplayName()).child(accountName+"-"+accountType).child("accountName").setValue(accountName);
                        dbAccRef.child("accounts/" + user.getDisplayName()).child(accountName+"-"+accountType).child("accountType").setValue(accountType);
                        dialog.dismiss();
                        Snackbar.make(getCurrentFocus(), "Account Created Successfully", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }

                }

            }
        });
        Button cancelNewAccount = (Button) dialog.findViewById(R.id.cancelAddAccountButton);
        cancelNewAccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                accountNickname.clearComposingText();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * Involves updating the database from the server. Helps sync between same users on multiple devices.
     *
     */
    private void updateDB() {
        DatabaseReference people = FirebaseDatabase.getInstance().getReference();

        people.child("users").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseDatabase.getInstance().getReference().child("users").getRef()
                        .addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                User chatMessage = dataSnapshot.getValue(User.class);
                                mydb.storeUsers(chatMessage.getName(),chatMessage.getEmail(),chatMessage.getCurrency(),chatMessage.getPhone());
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                User chatMessage = dataSnapshot.getValue(User.class);
                                mydb.storeUsers(chatMessage.getName(),chatMessage.getEmail(),chatMessage.getCurrency(),chatMessage.getPhone());
                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {
                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                                User chatMessage = dataSnapshot.getValue(User.class);
                                mydb.storeUsers(chatMessage.getName(),chatMessage.getEmail(),chatMessage.getCurrency(),chatMessage.getPhone());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Close the MySql connection before closing the app.
     *
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
        mydb.close();
    }

}
