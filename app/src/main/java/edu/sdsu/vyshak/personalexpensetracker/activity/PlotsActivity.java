package edu.sdsu.vyshak.personalexpensetracker.activity;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.sdsu.vyshak.personalexpensetracker.R;
import edu.sdsu.vyshak.personalexpensetracker.bean.Expenses;
import edu.sdsu.vyshak.personalexpensetracker.data.DBHelper;

public class PlotsActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private GraphView graph;
    private static List<Expenses> allExpenses = new ArrayList<>();
    private DBHelper mydb;
    private String TAG="Plots Activity";
    private List<Float> amountsExpense = new ArrayList<>();
    private FirebaseUser user;
    private FirebaseAuth auth;
    private String spentDate;
    private LineGraphSeries<DataPoint> mSeries1;

    /**
     *The onCreate method, creates the adapter that will return a fragment for each of the three
     * primary sections of the activity.
     * Set up the ViewPager with the sections adapter.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plots);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        user=FirebaseAuth.getInstance().getCurrentUser();
        List<String> datesExpense = new ArrayList<>();
        List<Integer> amountsExpense = new ArrayList<>();
        mydb = new DBHelper(this);
        String query = "Select * from transactionsSummary where uid like " + "\'" + user.getUid() + "\'"+" order by date";
        allExpenses.addAll((Collection<? extends Expenses>) mydb.gettransactions(query));
        String spentDate;
        String TAG="plots";

        for (Expenses expenses : allExpenses) {
            datesExpense.add(expenses.getDate());
            amountsExpense.add((int)expenses.getAmount());
        }

        List<Expenses> allExpenses = new ArrayList<>();
        datesExpense = new ArrayList<>();
        amountsExpense = new ArrayList<>();
        Calendar calendar= Calendar.getInstance();
        mydb = new DBHelper(this);
        for (Expenses expenses : allExpenses) {
            amountsExpense.add((int) expenses.getAmount());
        }
        DataPoint[] values = new DataPoint[amountsExpense.size()];

        for (int i=0; i<allExpenses.size(); i++) {
            Date d = calendar.getTime();
            calendar.add(Calendar.DATE, i);
            double x = i;
            double y = amountsExpense.get(i);
            DataPoint v = new DataPoint(d, y);
            values[i] = v;
        }
        Date d1,d3;
        calendar.set(calendar.MONTH,1);
        d1 = calendar.getTime();
        spentDate = String.valueOf(calendar.get(Calendar.YEAR) +"-"+ calendar.get(Calendar.MONTH) +"-"+ calendar.get(Calendar.DAY_OF_MONTH));
        Log.d("",spentDate);
        int max = calendar.getActualMaximum(calendar.DAY_OF_MONTH);
        calendar.set(calendar.DAY_OF_MONTH,max);
        d3 = calendar.getTime();
        GraphView graph = (GraphView) findViewById(R.id.graph);
        mSeries1 = new LineGraphSeries<>(generateData());
        graph.addSeries(mSeries1);
        graph.getViewport().setMinX(d1.getTime());
        graph.getViewport().setMaxX(d3.getTime());
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setHumanRounding(true);
    }

    public DataPoint[] generateData() {
        String query = "Select * from transactionsSummary where uid like " + "\'" + user.getUid() + "\'"+" and date > "+spentDate+" order by date";
        allExpenses.addAll((Collection<? extends Expenses>) mydb.gettransactions(query));

        if(allExpenses.size() > 0) {
            for (Expenses expenses : allExpenses) {
                amountsExpense.add(expenses.getAmount());
            }
        }
            DataPoint[] values = new DataPoint[amountsExpense.size()];
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            for (int i = 0; i < allExpenses.size(); i++) {
                Date d = calendar.getTime();
                calendar.add(Calendar.DATE, i);
                double x = i;
                double y = amountsExpense.get(i);
                DataPoint v = new DataPoint(d, y);
                values[i] = v;
            }

        return values;
    }

    /**
     *
     * This method is used to inflate the menu; this adds items to the action bar if it is present.
     * getMenuInflater().inflate(R.menu.menu_plots, menu);
     *
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     *
     * This method handles action bar item clicks here. The action bar will
     * automatically handle clicks on the HomeActivity/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     *
     * */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
        private DBHelper mydb;
        private String TAG="Plots Activity";
        private List<String> datesExpense;
        private List<Float> amountsExpense;
        private FirebaseUser user;
        private FirebaseAuth auth;
        private String spentDate;

        public PlaceholderFragment() {
        }

        /**
         *
         * Returns a new instance of this fragment for the given section
         * number.
         *
         */

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        /**
         *
         * This method creates a graph view. Plots the data fetched from the database.
         *
         */

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_plots, container, false);
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
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
