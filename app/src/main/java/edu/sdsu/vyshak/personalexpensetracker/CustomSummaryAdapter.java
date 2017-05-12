package edu.sdsu.vyshak.personalexpensetracker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by vysha on 4/17/2017.
 */

public class CustomSummaryAdapter extends ArrayAdapter<Expenses> {


    private ArrayList<Expenses> dataSet;
        Context mContext;
    private String newDateString;


// View lookup cache
private static class ViewHolder {
    TextView txtDesc;
    TextView txtDate;
    TextView txtAmount;
    TextView txtCrDb;
}


    public CustomSummaryAdapter(ArrayList<Expenses> data, Context context) {
        super(context, R.layout.expense_summary_list, data);
        this.dataSet = data;
        this.mContext=context;

    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.expense_summary_list, parent, false);
            Expenses expenses = getItem(position);
            viewHolder.txtDesc = (TextView) convertView.findViewById(R.id.description);
            viewHolder.txtDate = (TextView) convertView.findViewById(R.id.transactionDate);
            viewHolder.txtCrDb = (TextView) convertView.findViewById(R.id.transactionType);
            viewHolder.txtAmount = (TextView) convertView.findViewById(R.id.transactionAmount);
        convertView.setTag(viewHolder);
        viewHolder.txtDesc.setText(expenses.getDesc());
        String startDateString = expenses.getDate();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Date startDate;


        //SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yyyy");
            //String dateText = df2.format(expenses.getDate());
        viewHolder.txtDate.setText(expenses.getDate());
        if(expenses.getTranstype().equals("Expense")) {
            viewHolder.txtCrDb.setTextColor(Color.RED);
            viewHolder.txtCrDb.setText(expenses.getTranstype());
        }else{
            viewHolder.txtCrDb.setTextColor(Color.GREEN);
            viewHolder.txtCrDb.setText(expenses.getTranstype());
        }

        viewHolder.txtAmount.setText((Double.toString(expenses.getAmount())));
            return convertView;
        };
    }
