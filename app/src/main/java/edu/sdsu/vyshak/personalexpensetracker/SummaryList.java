package edu.sdsu.vyshak.personalexpensetracker;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;

import static edu.sdsu.vyshak.personalexpensetracker.R.string.date;

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
    private static FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private TextView loggedUser;
    private int accountCount;
    private String TAG="Summary List";
    public ArrayList<String> userAccounts = new ArrayList<>();
    DBHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_list);
        mydb = new DBHelper(this);
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
        } else {
            finish();
        }
        userAccounts.add("Cash");
        userAccounts.addAll(mydb.getAllAccounts(user.getUid()));
        setAccountCount(userAccounts.size());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
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
        public ArrayList<Expenses> expenseArray;
        public static DBHelper dbHelper;
        public static CustomSummaryAdapter customSummaryAdapter;

        public PlaceholderFragment() {
        }

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
            dbHelper = new DBHelper(getContext());
            expenseArray = new ArrayList<>();
            expenseArray.addAll(dbHelper.gettransactions(getArguments().getString(ARG_SUMMARY_LIST)));
            customSummaryAdapter = new CustomSummaryAdapter(expenseArray, getContext());
            ListView accountSummary = (ListView) rootView.findViewById(R.id.accountSummary);
            accountSummary.setAdapter(customSummaryAdapter);

            accountSummary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Expenses deleteTransaction =(Expenses) parent.getItemAtPosition(position);
                    String deleteAccount =deleteTransaction.getAccount();
                    String deleteTransact  = deleteTransaction.getTranstype();
                    String deleteUser = deleteTransaction.getUserId();
                    String deleteDate = deleteTransaction.getDate();
                    String deleteDesc = deleteTransaction.getDesc();
                    String deleteCurr = deleteTransaction.getCurrency();
                    String query="Delete from transactionsSummary where account like"+"\'"+deleteAccount+"\'" + "and transactionType like "+"\'"+deleteTransact+"\'"
                            + "and uid like "+"\'"+deleteUser+"\'" + "and date = "+"\'"+deleteDate+"\'" + "and description like "+"\'"+deleteDesc+"\'"
                            + "and currency like "+"\'"+deleteCurr+"\'";
                    customSummaryAdapter.notifyDataSetChanged();
                    if(deleteTransaction.getTime() != null) {
                        DatabaseReference dbExpRef = FirebaseDatabase.getInstance().getReference().child("IncomesExpense/").child(user.getUid() + "-" + deleteAccount).child(deleteTransaction.getTime());
                        dbExpRef.removeValue();
                    }

                    editDeleteTransaction(query,deleteTransaction);
                }
            });
            return rootView;
        }

        private void editDeleteTransaction(final String query, final Expenses expenses) {
            final Dialog dialog = new Dialog(getContext(),android.R.style.Theme_Material_Dialog);
            dialog.setContentView(R.layout.edit_delete_transaction);
            Button dialogButtonEdit = (Button) dialog.findViewById(R.id.edit_Transaction);
            dialogButtonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //editTransaction();
                    dialog.dismiss();
                }
            });
            Button dialogButtonDelete = (Button) dialog.findViewById(R.id.delete_Transaction);
            dialogButtonDelete.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    expenseArray.remove(expenses);
                    customSummaryAdapter.notifyDataSetChanged();
                    DBHelper dbHelper = new DBHelper(getContext());
                    dbHelper.removeTransaction(query);
                    dialog.dismiss();

                }
            });
            dialog.show();
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
            Log.d(TAG,"getItem");
            String squery="Select * from transactionsSummary where uid like "+"\'"+ user.getUid()+"\'"+ "and account like "+"\'"+userAccounts.get(position).toString()+"\'";
            return PlaceholderFragment.newInstance(position + 1,squery);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            int count = getAccountCount();
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {

          if(userAccounts.get(position).toString() != null)
            return userAccounts.get(position).toString();
          return null;
        }
   }

    public int getAccountCount() {
        return accountCount;
    }

    public void setAccountCount(int accountCount) {
        this.accountCount = accountCount;
    }

}
