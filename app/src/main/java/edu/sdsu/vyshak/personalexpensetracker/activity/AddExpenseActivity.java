package edu.sdsu.vyshak.personalexpensetracker.activity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.sdsu.vyshak.personalexpensetracker.bean.Budget;
import edu.sdsu.vyshak.personalexpensetracker.data.DBHelper;
import edu.sdsu.vyshak.personalexpensetracker.bean.Expenses;
import edu.sdsu.vyshak.personalexpensetracker.R;
import edu.sdsu.vyshak.personalexpensetracker.sync.VolleyQueue;

/*
* This class acts as a form for the user to log their expense.
* Created by Vyshak on 4/19/2017.
* */

public class AddExpenseActivity extends AppCompatActivity {
    private String transactionType;
    private String spentDate;
    private String categoryChosen;
    private String currencyChosen;
    private String paymentChosen;
    private String expenseInfo;
    private String TAG="AddExpenseActivity";

    private float expense;
    private float toUSD;

    private DBHelper mydb;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private DatabaseReference dbExpRef;

    private ArrayList<String> categories;
    private ArrayList<String> userAccounts;

    Date date = new Date();

    /**
     * onCreate method to
     * authenticate user,
     * initiate buttons & adapters,
     * read asset files,
     * load data into pickers and
     * activate event listeners
     *
     * @param savedInstanceState bundle
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mydb = new DBHelper(getApplicationContext());
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {
                    auth.signOut();
                    finish();
                }
            }
        };

        categories = new ArrayList<>();
        userAccounts = new ArrayList<>();
        userAccounts.add("Cash");
        userAccounts.addAll(mydb.getAllAccounts(user.getUid()));

        final EditText expenseDescription = (EditText) findViewById(R.id.expenseDesc);
        final EditText amountSpent = (EditText) findViewById(R.id.amountSpent);

        final ArrayList<String> incomeOrExpenseList = new ArrayList<>(Arrays.asList("Income","Expense"));
        ArrayAdapter<String> expenseIncomeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,incomeOrExpenseList);
        expenseIncomeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transactionType=incomeOrExpenseList.get(0);

        Spinner expenseIncomeSpinner = (Spinner) findViewById(R.id.incomeExpense);
        expenseIncomeSpinner.setAdapter(expenseIncomeAdapter);
        expenseIncomeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                transactionType=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
        Calendar today = Calendar.getInstance();
        spentDate=String.valueOf(today.get(Calendar.YEAR)+"-"+(today.get(Calendar.MONTH)+1)+"-"+today.get(Calendar.DAY_OF_MONTH));
        datePicker.init(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener(){

                    @Override
                    public void onDateChanged(DatePicker view,
                                              int year, int monthOfYear,int dayOfMonth) {
                        spentDate= String.valueOf(year+"-"+(monthOfYear+1)+"-"+dayOfMonth);
                    }});

        try {
            InputStream categoriesFile = getAssets().open("categories");
            BufferedReader in = new BufferedReader( new InputStreamReader(categoriesFile));
            String line;
            while((line = in.readLine()) != null){
                categories.add(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Categories read error", e);
        }

        Spinner expenseTypeSpinner = (Spinner) findViewById(R.id.expenseType);
        final ArrayAdapter<String> expenseAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,categories);
        expenseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryChosen=categories.get(0);
        expenseTypeSpinner.setAdapter(expenseAdapter);
        expenseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryChosen=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner usedCurrency = (Spinner) findViewById(R.id.usedCurrency);
        List<String> currencies = new ArrayList<String>();
        try {
            InputStream currenciesFile = getAssets().open("currencyTypes");
            BufferedReader in = new BufferedReader( new InputStreamReader(currenciesFile));
            String line;
            while((line = in.readLine()) != null){
                currencies.add(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Currency types read error", e);
        }

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencyChosen=currencies.get(0);

        usedCurrency.setAdapter(currencyAdapter);
        usedCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currencyChosen=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner modeOfPaymentSpinner = (Spinner) findViewById(R.id.usedAccount);
        ArrayAdapter<String> accountUsedAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,userAccounts);
        accountUsedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentChosen=userAccounts.get(0);

        modeOfPaymentSpinner.setAdapter(accountUsedAdapter);
        modeOfPaymentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                paymentChosen=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button dialogButton = (Button) findViewById(R.id.addExpenseButton);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(amountSpent.getText().toString()) || expense==' '){
                    Snackbar.make(v, "Please Enter Amount", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else {
                    expense=Float.valueOf(amountSpent.getText().toString());
                    expenseInfo = expenseDescription.getText().toString();
                    if(!currencyChosen.equals("USD($)")){
                        getCurrencyRate();

                    }else{
                        Expenses expenses = new Expenses();
                        expenses.setUserId(user.getUid());
                        expenses.setAccount(paymentChosen);
                        expenses.setAmount(expense);
                        expenses.setCurrency(currencyChosen);
                        expenses.setCategory(categoryChosen);
                        expenses.setDesc(expenseInfo);
                        expenses.setDate(spentDate);
                        expenses.setTranstype(transactionType);
                        expenses.setTime(String.valueOf(date.getTime()));
                        mydb.insertTransaction(user.getUid(), paymentChosen, expense, currencyChosen, transactionType, categoryChosen, spentDate, expenseInfo); //String account, String transtype, String category, String date
                        dbExpRef = FirebaseDatabase.getInstance().getReference().child("IncomesExpense/").child(user.getUid() + "-" + paymentChosen);
                        dbExpRef.child(expenses.getTime()).setValue(expenses);
                        addNotification();
                        Snackbar.make(getCurrentFocus(), "Expense Added Successfully", Snackbar.LENGTH_LONG)
                                  .setAction("Action", null).show();
                        finish();
                }}


            }
        });

        Button cancelNewAccount = (Button) findViewById(R.id.cancelAddExpenseButton);
        cancelNewAccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /**
     * This method notifies user if the budget consumption is >60% or >90%
     *
     */
    private void addNotification() {
        List<Budget> budgetArray = new ArrayList<>();
        budgetArray.addAll(mydb.getBudgetLimits());
        double checkexpense=0;
        for(Budget budget:budgetArray){
            Date date = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
            c.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
            String weekStart = String.valueOf(c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH));
            c.add(Calendar.DAY_OF_MONTH, 6);
            String weekEnd = String.valueOf(c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH));
            String monthStart = String.valueOf(c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.getActualMinimum(Calendar.DAY_OF_MONTH));
            String monthEnd = String.valueOf(c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.getActualMaximum(Calendar.DAY_OF_MONTH));
            if(budget.getCycle().equals("week")){
                checkexpense=mydb.getExpenseForCategory(budget.getCategory(),weekStart);
            }else
            {
                checkexpense=mydb.getExpenseForCategory(budget.getCategory(),monthStart);
            }
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this);
            if(budget.getCategory().equals(categoryChosen) &&(budget.getAmount() - checkexpense) <= 0.1*budget.getAmount()) {
                                mBuilder.setSmallIcon(R.drawable.ic_budget_icon)
                                .setContentTitle("Budget Update!")
                                .setContentText("You have spent 90% in " + budget.getCategory() + " category");
                Intent notificationIntent = new Intent(this, SetBudgetActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(HomeActivity.class);

                stackBuilder.addNextIntent(notificationIntent);
                PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(contentIntent);

                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(001, mBuilder.build());
                finish();
            }
            if(budget.getCategory().equals(categoryChosen) && (budget.getAmount() - checkexpense) <= 0.4*budget.getAmount()) {
                mBuilder.setSmallIcon(R.drawable.ic_budget_icon)
                                .setContentTitle("Budget Update!")
                                .setContentText("You have spent 60% in " + budget.getCategory() + " category");
                Intent notificationIntent = new Intent(this, SetBudgetActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(HomeActivity.class);

                stackBuilder.addNextIntent(notificationIntent);
                PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(contentIntent);

                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(001, mBuilder.build());
                finish();
            }
        }
    }

    /**
     * This method is to get the currency conversion factors from the server.
     *
     */

    public void getCurrencyRate(){
        Response.Listener<JSONObject> success_state = new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                try {
                    JSONObject dataObj = response.getJSONObject("rates");
                    double convertAmount = (double) dataObj.get(currencyChosen.substring(0,3));
                    updateRate(convertAmount);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
                }
        };
        String url = "http://api.fixer.io/latest?base=USD";
        JsonObjectRequest getRequestState = new JsonObjectRequest(url,null,success_state,failure);//url, success_state, failure
        VolleyQueue.instance(this).add(getRequestState);
    }

    /**
     * This method updates the UI after multiplying with the rate conversion factor obtained from the server
     *@param convertAmount rate of conversion value with double type.
     */

    private void updateRate(double convertAmount) {
        toUSD = (float) convertAmount;
        expense= Math.round((float) (expense/convertAmount));

        Expenses expenses = new Expenses();
        expenses.setUserId(user.getUid());
        expenses.setAccount(paymentChosen);
        expenses.setAmount(expense);
        expenses.setCurrency(currencyChosen);
        expenses.setCategory(categoryChosen);
        expenses.setDesc(expenseInfo);
        expenses.setDate(spentDate);
        expenses.setTranstype(transactionType);
        expenses.setTime(String.valueOf(date.getTime()));

        mydb.insertTransaction(user.getUid(), paymentChosen, expense, currencyChosen, transactionType, categoryChosen, spentDate, expenseInfo); //String account, String transtype, String category, String date

        dbExpRef = FirebaseDatabase.getInstance().getReference().child("IncomesExpense/").child(user.getUid() + "-" + paymentChosen);
        dbExpRef.child(expenses.getTime()).setValue(expenses);

        addNotification();
        finish();
    }

}
