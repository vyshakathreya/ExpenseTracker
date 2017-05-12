package edu.sdsu.vyshak.personalexpensetracker;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ShoppingListActivity extends AppCompatActivity {

    String paymentChosen,currencyChosen,categoryChosen;
    String TAG="Shopping List";
    float shoppedPrice,toUSD;
    DBHelper mydb;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseAuth.AuthStateListener authListener;
    DatabaseReference dbref;
    ArrayList<String> userAccounts = new ArrayList<>();

    public float getToUSD() {
        return toUSD;
    }

    public void setToUSD(float toUSD) {
        this.toUSD = toUSD;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mydb = new DBHelper(this);

        ListView shopItemsList = (ListView) findViewById(R.id.shoptoBuyList);
        final ArrayList<ShopItems> shoplist = new ArrayList<>();
        final CustomShopAdapter customShopAdapter = new CustomShopAdapter(shoplist, this);
        shoplist.addAll(mydb.getShoppingItems());
        shopItemsList.setAdapter(customShopAdapter);
        final EditText enteredItem = (EditText) findViewById(R.id.enterItem);

        ImageButton enterButton = (ImageButton) findViewById(R.id.addToList);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String listedItem = enteredItem.getText().toString();
                ShopItems shopItems = new ShopItems();
                shopItems.setItemName(listedItem);
                shoplist.add(shopItems);
                customShopAdapter.notifyDataSetChanged();
                enteredItem.setText(" ");
                mydb.saveShoppingItems(shopItems.getItemName());
            }
        });


        final Button shopLogExpense = (Button) findViewById(R.id.launch_dialog_shop);
        shopLogExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSaveDialog();
            }

        });
    }

    private void launchSaveDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.save_shop_list);
        dialog.setTitle("Save Expense");

        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
            }
        };

        user = FirebaseAuth.getInstance().getCurrentUser();

        userAccounts.add("cash");
        userAccounts.addAll(mydb.getAllAccounts(user.getUid()));
        final Spinner shopAccountUser = (Spinner) dialog.findViewById(R.id.shoppedUsingAccount);
        ArrayAdapter<String> accountUsedAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,userAccounts);
        accountUsedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shopAccountUser.setAdapter(accountUsedAdapter);
        shopAccountUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                paymentChosen=parent.getItemAtPosition(position).toString();
                Log.d(TAG,"modeofPayment"+paymentChosen);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final EditText shopExpense = (EditText) dialog.findViewById(R.id.shoppedExpense);
        List<String> currencies = new ArrayList<String>();

        try {
            InputStream currenciesFile = getAssets().open("currencyTypes");
            BufferedReader in = new BufferedReader( new InputStreamReader(currenciesFile));
            String line;
            while((line = in.readLine()) != null){
                currencies.add(line);
            }
        } catch (IOException e) {
            Log.e("rew", "read Error", e);
        }
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner shopCurrency = (Spinner) dialog.findViewById(R.id.shoppedCurrency);
        shopCurrency.setAdapter(currencyAdapter);
        currencyChosen=currencies.get(0);
        shopCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currencyChosen=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayList<String> categories = new ArrayList<>();
        try {
            InputStream categoriesFile = getAssets().open("shoppingCategories");
            BufferedReader in = new BufferedReader( new InputStreamReader(categoriesFile));
            String line;
            while((line = in.readLine()) != null){
                categories.add(line);
            }
        } catch (IOException e) {
            Log.e("rew", "read Error", e);
        }
        categoryChosen=categories.get(0);
        final Spinner categorySpinner = (Spinner) dialog.findViewById(R.id.shoppingCategory);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryChosen = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        final Button shopLogExpense= (Button) dialog.findViewById(R.id.Save_Expenses);
        shopLogExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shoppedPrice = Float.valueOf(shopExpense.getText().toString());
                if(TextUtils.isEmpty(shopExpense.getText().toString())){
                    Toast.makeText(getApplicationContext(),"Please enter amount", Toast.LENGTH_LONG);
                }else {
                    if(!currencyChosen.equals("USD($)")){
                        Log.d("not usd",""+ currencyChosen);
                    getCurrencyRate();
                    }else{
                    Calendar today = Calendar.getInstance();
                    String spentDate = String.valueOf(today.get(Calendar.YEAR)+today.get(Calendar.MONTH)+today.get(Calendar.DAY_OF_MONTH));
                    Expenses expenses = new Expenses();
                    expenses.setUserId(user.getUid());
                    expenses.setAccount(paymentChosen);
                    expenses.setAmount(shoppedPrice);
                    expenses.setCurrency(currencyChosen);
                    expenses.setCategory(categoryChosen);
                    expenses.setDesc("shopping List");
                    expenses.setDate(spentDate);
                    expenses.setTranstype("Expense");
                    mydb.insertTransaction(user.getUid(), paymentChosen, shoppedPrice, currencyChosen, "Expense", categoryChosen, spentDate, "shopping-"+categoryChosen); //String account, String transtype, String category, String date
                    DatabaseReference dbExpRef = FirebaseDatabase.getInstance().getReference().child("IncomesExpense/").child(user.getUid() + "-" + paymentChosen);
                    dbExpRef.child(spentDate).setValue(expenses);
                    shopExpense.setText(" ");
                    shopAccountUser.setSelection(0);
                    shopCurrency.setSelection(0);
                    categorySpinner.setSelection(0);
                    dialog.dismiss();
                        addNotification();
                    Snackbar.make(v, "Data Saved Successfully", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    }
                }

            }

            private void getCurrencyRate() {
                Log.d(TAG,"getting currency rate");
                Response.Listener<JSONObject> success_state = new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        Log.d("getUsers","response length"+response);
                        try {
                            JSONObject dataObj = response.getJSONObject("rates");
                            double convertAmount = (double) dataObj.get(currencyChosen.substring(0,3));
                            updateRate(convertAmount);
                            Log.d(TAG,"conversion value"+String.valueOf(convertAmount));
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
                JsonObjectRequest getRequestState = new JsonObjectRequest(url,null,success_state,failure);//url, success_state, failure
                VolleyQueue.instance(getApplicationContext()).add(getRequestState);

            }


            private void updateRate(double convertAmount) {
                setToUSD( (float) convertAmount);
                shoppedPrice= (float) (shoppedPrice/convertAmount);

                Calendar today = Calendar.getInstance();

                String spentDate = String.valueOf(today.get(Calendar.YEAR) +"-"+ today.get(Calendar.MONTH) +"-"+ today.get(Calendar.DAY_OF_MONTH));
                Expenses expenses = new Expenses();
                expenses.setUserId(user.getUid());
                expenses.setAccount(paymentChosen);
                expenses.setAmount(shoppedPrice);
                expenses.setCurrency(currencyChosen);
                expenses.setCategory(categoryChosen);
                expenses.setDesc("shopping List");
                expenses.setDate(spentDate);
                expenses.setTranstype("Expense");
                mydb.insertTransaction(user.getUid(), paymentChosen, shoppedPrice, currencyChosen, "Expense", categoryChosen, spentDate, "shopping-"+categoryChosen); //String account, String transtype, String category, String date
                DatabaseReference dbExpRef = FirebaseDatabase.getInstance().getReference().child("IncomesExpense/").child(user.getUid() + "-" + paymentChosen);
                dbExpRef.child(spentDate).setValue(expenses);
                shopExpense.setText(" ");
                shopAccountUser.setSelection(0);
                shopCurrency.setSelection(0);
                categorySpinner.setSelection(0);
                dialog.dismiss();
                addNotification();
                Snackbar.make(getCurrentFocus(), "Data Saved Successfully", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
            }
        );
        dialog.show();

    }

    private void addNotification() {
        Log.d(TAG,"entering to notify");
        List<Budget> budgetArray = new ArrayList<>();
        budgetArray.addAll(mydb.getBudgetLimits());
        double checkexpense=0;
        for(Budget budget:budgetArray){
            Date date = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
            c.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
            String weekStart = String.valueOf(c.get(c.YEAR)+"-"+(c.get(c.MONTH)+1)+"-"+c.get(c.DAY_OF_MONTH));
            c.add(Calendar.DAY_OF_MONTH, 6);
            String weekEnd = String.valueOf(c.get(c.YEAR)+"-"+(c.get(c.MONTH)+1)+"-"+c.get(c.DAY_OF_MONTH));
            String monthStart = String.valueOf(c.get(c.YEAR)+"-"+(c.get(c.MONTH)+1)+"-"+c.getActualMinimum(c.DAY_OF_MONTH));
            String monthEnd = String.valueOf(c.get(c.YEAR)+"-"+(c.get(c.MONTH)+1)+"-"+c.getActualMaximum(c.DAY_OF_MONTH));
            if(budget.getCycle().equals("week")){
                checkexpense=mydb.getexpense(budget.getCategory().toString(),weekStart);
            }else
            {
                checkexpense=mydb.getexpense(budget.getCategory().toString(),monthStart);
                Log.d("checking","check expense month"+checkexpense);
            }
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this);
            if(budget.getCategory()==categoryChosen && (budget.getAmount() - checkexpense) <= 0.1*budget.getAmount()) {
                mBuilder.setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Exceding Budget!")
                        .setContentText("You have spent 90% in " + budget.getCategory() + " category")
                        .setOngoing(true);
            }
            if(budget.getCategory()==categoryChosen && (budget.getAmount() - checkexpense) <= 0.4*budget.getAmount()) {
                mBuilder.setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Exceding Budget!")
                        .setContentText("You have spent 60% in " + budget.getCategory() + " category")
                        .setOngoing(true);

            }
            Intent notificationIntent = new Intent(this, SetBudgetActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(Home.class);

            stackBuilder.addNextIntent(notificationIntent);
            PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(contentIntent);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(001, mBuilder.build());
            finish();

        }
    }

    }