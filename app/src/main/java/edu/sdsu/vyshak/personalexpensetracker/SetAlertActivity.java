package edu.sdsu.vyshak.personalexpensetracker;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SetAlertActivity extends AppCompatActivity {

    String duedate,duebill,repeatCycle,notifyBeforeThisTime;
    String TAG="Alert Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alert);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DatePicker datePickerbill = (DatePicker) findViewById(R.id.billDue);
        datePickerbill.findViewById(Resources.getSystem().getIdentifier("year", "id", "android")).setVisibility(View.GONE);
        Calendar today = Calendar.getInstance();
        duedate = String.valueOf(today.get(Calendar.YEAR) + "-" + (today.get(Calendar.MONTH) +1 ) + "-" + today.get(Calendar.DAY_OF_MONTH));
        datePickerbill.init(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener(){
                    @Override
                    public void onDateChanged(DatePicker view,
                                              int year, int monthOfYear,int dayOfMonth) {
                        duedate= String.valueOf(dayOfMonth+"-"+(monthOfYear +1)+"-"+year);
                    }});

        List<String> bills = new ArrayList<String>();
        try {
            InputStream billsFile = this.getAssets().open("bills");
            BufferedReader in = new BufferedReader( new InputStreamReader(billsFile));
            String line;
            while((line = in.readLine()) != null){
                bills.add(line);
            }
        } catch (IOException e) {
            Log.e("rew", "read Error", e);
        }

        Spinner billSpinner = (Spinner) findViewById(R.id.bills_spinner);
        ArrayAdapter<String> billAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,bills);
        billAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duebill=bills.get(0);
        billSpinner.setAdapter(billAdapter);
        billAdapter.notifyDataSetChanged();
        billSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                duebill=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayList<String> frequencyList = new ArrayList<>(Arrays.asList("month","year"));
        Spinner spinnerRepeat = (Spinner) findViewById(R.id.spinner_repeat);
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,frequencyList);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeat.setAdapter(repeatAdapter);
        repeatAdapter.notifyDataSetChanged();
        spinnerRepeat.setSelection(0);
        spinnerRepeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                repeatCycle=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button dialogButtonOK = (Button) findViewById(R.id.button_setAlert_OK);
        dialogButtonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNotification();
            }
        });
        Button dialogButtonCancel = (Button) findViewById(R.id.button_setAlert_Cancel);
        dialogButtonCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void addNotification() {
        Log.d(TAG,"settign not");
        DBHelper mydb = new DBHelper(this);
        mydb.setAlerts(duedate,duebill,repeatCycle);
        finish();
    }

}


