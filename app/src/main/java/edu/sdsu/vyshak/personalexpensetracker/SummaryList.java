package edu.sdsu.vyshak.personalexpensetracker;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SummaryList extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter sectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager viewPager;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference dbAccRef;
    private boolean isFabOpen=false;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private FloatingActionButton fab,fabAddAccount,fabAddExpense;
    private TextView loggedUser;
    private EditText accountNickname, expenseDescription, amountSpent;
    private int accountCount;
    private String TAG="Summary List";
    private String accountName, accountType, transactionType;
    private Spinner accountTypeSpinner,expenseType,modeofPayment,usedCurrency,expenseIncome;
    private Context context = this;
    public ArrayList<String> userAccounts = new ArrayList<>();
    private Date date = new Date();
    private DatabaseReference dbExpRef;
    private String categoryChosen,currencyChosen, paymentChosen, spentDate, expense, expenseInfo;
    private DatePicker datePicker;
    DBHelper mydb;

    public int getAccountCount() {
        return accountCount;
    }

    public void setAccountCount(int accountCount) {
        this.accountCount = accountCount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_list);
        mydb = new DBHelper(this);
        userAccounts.add("Cash");
        userAccounts.addAll(mydb.getAllAccounts());
        setAccountCount(userAccounts.size());
        loggedUser = (TextView) findViewById(R.id.loggedUser);
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    loggedUser.setVisibility(View.VISIBLE);
                    loggedUser.setText("Hi," + user.getDisplayName());
                } else {
                    loggedUser.setVisibility(View.INVISIBLE);
                    auth.signOut();
                    finish();
                }
            }
        };
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loggedUser.setVisibility(View.VISIBLE);
            loggedUser.setText("Hi," + user.getDisplayName());
        } else {
            loggedUser.setVisibility(View.INVISIBLE);
            auth.signOut();
            finish();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        fabAddAccount = (FloatingActionButton) findViewById(R.id.addAccount);
        fabAddExpense = (FloatingActionButton) findViewById(R.id.addExpense);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                animateFab();
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

            }
        });
        fabAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
              final Dialog dialog = new Dialog(context,android.R.style.Theme_Light);
                    dialog.setContentView(R.layout.add_account);
                    dialog.setTitle("Add Account");
                    List<String> accountTypes = new ArrayList<String>();
                    accountTypes.add("Checking");
                    accountTypes.add("Savings");
                    accountTypes.add("Credit");

                    accountNickname = (EditText) dialog.findViewById(R.id.accountName);
                    accountTypeSpinner = (Spinner) dialog.findViewById(R.id.accountType);
                    ArrayAdapter<String> accountSpinnerAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,accountTypes);
                    accountSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    accountTypeSpinner.setAdapter(accountSpinnerAdapter);
                    accountTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            accountType= parent.getItemAtPosition(position).toString();
                            Log.d(TAG,"account Type" + accountType);
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
                            userAccounts.add(accountName+"-"+accountType);
                            setAccountCount(userAccounts.size());
                            sectionsPagerAdapter.notifyDataSetChanged();
                            mydb.insertAccount(accountName,accountType,accountName+"-"+accountType);
                            dbAccRef = FirebaseDatabase.getInstance().getReference();
                            dbAccRef.child("accounts/" + user.getDisplayName()).child(accountName+"-"+accountType).child("accountName").setValue(accountName);
                            dbAccRef.child("accounts/" + user.getDisplayName()).child(accountName+"-"+accountType).child("accountType").setValue(accountType);
                            dialog.dismiss();
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

            });

        fabAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialogExpense = new Dialog(context,android.R.style.Theme_Light);
                dialogExpense.setContentView(R.layout.add_expense);
                dialogExpense.setTitle("Add Income/Log Expense");
                expenseDescription = (EditText) dialogExpense.findViewById(R.id.expenseDesc);
                amountSpent = (EditText) dialogExpense.findViewById(R.id.amountSpent);
                ArrayList<String> incomeorExpense = new ArrayList<>(Arrays.asList("Income","Expense"));
                expenseIncome = (Spinner) dialogExpense.findViewById(R.id.incomeExpense);
                ArrayAdapter<String> expenseIncomeAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,incomeorExpense);
                expenseIncomeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                expenseIncome.setAdapter(expenseIncomeAdapter);
                expenseIncome.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        transactionType=parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                datePicker = (DatePicker) dialogExpense.findViewById(R.id.datePicker);
                Calendar today = Calendar.getInstance();
                datePicker.init(
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH),
                        today.get(Calendar.DAY_OF_MONTH),
                        new DatePicker.OnDateChangedListener(){

                            @Override
                            public void onDateChanged(DatePicker view,
                                                      int year, int monthOfYear,int dayOfMonth) {
                                Toast.makeText(getApplicationContext(),
                                        "onDateChanged", Toast.LENGTH_SHORT).show();

                                spentDate= String.valueOf(dayOfMonth+"-"+monthOfYear+"-"+year);
                            }});

                List<String> categories = new ArrayList<String>();
                categories.add("F&B");
                categories.add("Health");
                categories.add("Entertainment");
                categories.add("Housing");
                expenseType = (Spinner) dialogExpense.findViewById(R.id.expenseType);
                ArrayAdapter<String> expenseAdapter = new ArrayAdapter<>(context,android.R.layout.simple_list_item_1,categories);
                expenseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                expenseType.setAdapter(expenseAdapter);
                expenseType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        categoryChosen=parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                usedCurrency = (Spinner) dialogExpense.findViewById(R.id.usedCurrency);
                List<String> currencies = new ArrayList<String>();
                currencies.add("GBP");
                currencies.add("INR");
                currencies.add("USD");
                ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,currencies);
                currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

                modeofPayment = (Spinner) dialogExpense.findViewById(R.id.usedAccount);
                ArrayAdapter<String> accountUsedAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,userAccounts);
                accountUsedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                modeofPayment.setAdapter(accountUsedAdapter);
                modeofPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        paymentChosen=parent.getItemAtPosition(position).toString();
                        Log.d(TAG,"modeofPayment"+paymentChosen);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

              Button dialogButton = (Button) dialogExpense.findViewById(R.id.addExpenseButton);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        expense=amountSpent.getText().toString();
                        expenseInfo = expenseDescription.getText().toString();


/*        CREATE TABLE IF NOT EXISTS transactions " +
        "(transaction_id integer AUTOINCREMENT, accountNickName text primary key, accountType text, " +
                "transactionType text, category text, date integer)*/
                        mydb.insertTransaction(paymentChosen, Integer.parseInt(expense),currencyChosen,transactionType,categoryChosen,spentDate,expenseInfo); //String account, String transtype, String category, String date
                        dbExpRef = FirebaseDatabase.getInstance().getReference().child("IncomesExpense/").child(user.getUid()+"-"+paymentChosen);
                        dbExpRef.child(String.valueOf(date.getTime())).child("category").setValue(categoryChosen);
                        dbExpRef.child(String.valueOf(date.getTime())).child("currency").setValue(currencyChosen);
                        dbExpRef.child(String.valueOf(date.getTime())).child("transaction").setValue(transactionType);
                        dbExpRef.child(String.valueOf(date.getTime())).child("expense").setValue(expense);
                        dbExpRef.child(String.valueOf(date.getTime())).child("date").setValue(spentDate);
                        dbExpRef.child(String.valueOf(date.getTime())).child("description").setValue(expenseInfo);
                        String squery="Select * from userTransactions where account like "+"\'"+paymentChosen+"\'";
                        PlaceholderFragment.newInstance(userAccounts.indexOf(paymentChosen) + 1,squery);
                        dialogExpense.dismiss();
                    }
                });
                Button cancelNewAccount = (Button) dialogExpense.findViewById(R.id.cancelAddExpenseButton);
                cancelNewAccount.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {

                        dialogExpense.dismiss();
                    }
                });

                dialogExpense.show();
            }
        });

    }

    private void animateFab() {

        if(isFabOpen){
            fab.startAnimation(rotate_backward);
            fabAddAccount.startAnimation(fab_close);
            fabAddExpense.startAnimation(fab_close);
            fabAddAccount.setVisibility(View.INVISIBLE);
            fabAddExpense.setVisibility(View.INVISIBLE);
            isFabOpen = false;
        } else {
            fab.startAnimation(rotate_forward);
            fabAddExpense.startAnimation(fab_open);
            fabAddAccount.startAnimation(fab_open);
            fabAddExpense.setVisibility(View.VISIBLE);
            fabAddAccount.setVisibility(View.VISIBLE);
            isFabOpen = true;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_summary_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.shopList) {
            String s1="check";
            String s2="check";
            ShopListFragment shopListFragment = ShopListFragment.newInstance(s1,s2);
            //shopListFragment.onCreate();
            return true;
        }

        if(id == R.id.alertMe){
            return true;
        }

        if (id == R.id.logout){
            auth.signOut();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_SUMMARY_LIST = "summary_list";

        public static void setAccountName(String accountName) {
            PlaceholderFragment.accountName = accountName;
        }

        public static String accountName;
        public static ArrayList<Expenses> expenseArray;
        public static DBHelper dbHelper;
        public static String query;
        public static ArrayAdapter accountSummaryAdapter;
        public static CustomSummaryAdapter customSummaryAdapter;
        private static int columnCount;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber,String accName) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(ARG_SUMMARY_LIST, accName);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_summary_list, container, false);
            String query="Select * from userTransactions where account like "+"\'"+accountName+"\'";
            dbHelper = new DBHelper(getContext());
            expenseArray = new ArrayList<>();
            expenseArray=dbHelper.gettransactions(getArguments().getString(ARG_SUMMARY_LIST));
            customSummaryAdapter = new CustomSummaryAdapter(expenseArray, getContext());
            ListView accountSummary = (ListView) rootView.findViewById(R.id.accountSummary);
            Log.d("recieved Array",""+expenseArray);
            accountSummary.setAdapter(customSummaryAdapter);
            customSummaryAdapter.notifyDataSetChanged();
            return rootView;
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //PlaceholderFragment.setAccountName(userAccounts.get(position).toString());
            //PlaceholderFragment.accountName=userAccounts.get(position);
            Log.d(TAG,"getItem");
            String squery="Select * from userTransactions where account like "+"\'"+userAccounts.get(position).toString()+"\'";
            ArrayList<Expenses> arrExpenses = new ArrayList<>();
            arrExpenses.addAll(mydb.gettransactions(squery));
            return PlaceholderFragment.newInstance(position + 1,squery);

            /*switch (position) {
                case 0:
                    //PlaceholderFragment.updateInfo(userAccounts.get(position));

                case 1:
                    PlaceholderFragment.accountName=userAccounts.get(position);
                    return PlaceholderFragment.newInstance(position + 1);
                case 2:
                    PlaceholderFragment.accountName=userAccounts.get(position);
                    return PlaceholderFragment.newInstance(position + 1);
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }*/
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            int count = getAccountCount();
            Log.d(TAG,"total Acc"+count);
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {

          if(userAccounts.get(position).toString() != null)
            return userAccounts.get(position).toString();
            /*switch (position) {
                case 0:
                    return userAccounts.get(0);
                case 1:
                    return "Account 2";
                case 2:
                    return "Account 3";
            }*/
          Log.d(TAG,"userAccounts"+userAccounts.get(0));
          return null;
        }


    }
}
