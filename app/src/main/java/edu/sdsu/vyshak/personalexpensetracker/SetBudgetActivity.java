package edu.sdsu.vyshak.personalexpensetracker;


import android.os.Bundle;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;

public class SetBudgetActivity extends AppCompatActivity {

    String chosenBudgetCategory,limitCycle;
    String TAG="Alert Activity";
    DBHelper mydb;
    float amountLimit=0;
    final ArrayList<Budget> budgetArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final CustomBudgetAdapter customBudgetAdapter;
        mydb= new DBHelper(this);
        List<String> categories = new ArrayList<String>();
        try {
            InputStream budgetcategories = this.getAssets().open("budgetcategories");
            BufferedReader in = new BufferedReader( new InputStreamReader(budgetcategories));
            String line;
            while((line = in.readLine()) != null){
                categories.add(line);
            }
        } catch (IOException e) {
            Log.e("rew", "read Error", e);
        }

        budgetArray.addAll(mydb.getBudgetLimits());

        ListView budgetlist= (ListView) findViewById(R.id.budgetlist);
        customBudgetAdapter = new CustomBudgetAdapter(budgetArray, this);
        budgetlist.setAdapter(customBudgetAdapter);

        Spinner billSpinner = (Spinner) findViewById(R.id.budget_category_spinner);
        ArrayAdapter<String> billAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,categories);
        billAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chosenBudgetCategory=categories.get(0);
        billSpinner.setAdapter(billAdapter);
        billAdapter.notifyDataSetChanged();
        billSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chosenBudgetCategory=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayList<String> frequencyList = new ArrayList<>(Arrays.asList("month","week"));
        Spinner spinnerRepeat = (Spinner) findViewById(R.id.notifybefore);
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,frequencyList);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeat.setAdapter(repeatAdapter);
        repeatAdapter.notifyDataSetChanged();

        limitCycle="month";
        spinnerRepeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                limitCycle=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final EditText budgetAmount = (EditText) findViewById(R.id.budget_amount);

        Button dialogButtonOK = (Button) findViewById(R.id.button_setBudget_OK);
        dialogButtonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String anount = budgetAmount.getText().toString();
                if(TextUtils.isEmpty(anount))
                    Snackbar.make(v, "Please Enter Amount", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                else {
                    if(budgetArray.contains(anount)){
                        Snackbar.make(v, "Please remove earlier budget", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    amountLimit = Float.parseFloat(anount);
                    mydb.savelimits(chosenBudgetCategory, amountLimit, limitCycle);
                    budgetArray.clear();
                    budgetArray.addAll(mydb.getBudgetLimits());
                    customBudgetAdapter.notifyDataSetChanged();
                    Log.d("Clicked", anount);
                }
            }
        });

        Button dialogButtonCancel = (Button) findViewById(R.id.button_setBudget_Cancel);
        dialogButtonCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /*private void addNotification() {
        Log.d(TAG,"entering to notify");

        for(Budget budget:budgetArray){
            double checkexpense=mydb.getexpense(budget,date,date);
            if((budget.getAmount()-checkexpense) <= 0.1*budget.getAmount()) {
                addNotification();

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle("Exceding Budget!")
                                .setContentText("You have spent 90% in " + budget.getCategory() + " category")
                                .setOngoing(true);


                Intent notificationIntent = new Intent(this, SetBudgetActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(Home.class);

                stackBuilder.addNextIntent(notificationIntent);
                PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
               // PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),STATUS_ICON_REQUEST_CODE,intent,0);
                mBuilder.setContentIntent(contentIntent);

                // Add as notification
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(001, mBuilder.build());
                finish();
            }
        }
    }*/

}


