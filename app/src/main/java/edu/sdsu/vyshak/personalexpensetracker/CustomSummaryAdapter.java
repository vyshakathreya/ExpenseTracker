package edu.sdsu.vyshak.personalexpensetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by vysha on 4/17/2017.
 */

public class CustomSummaryAdapter extends ArrayAdapter<Expenses> {


    private ArrayList<Expenses> dataSet;
        Context mContext;



// View lookup cache
private static class ViewHolder {
    TextView txtSender;
    TextView txtMessageSender;
    TextView txtReciever;
    TextView txtMessageReciever;
}

    public CustomSummaryAdapter(ArrayList<Expenses> data, Context context) {
        super(context, R.layout.expense_summary_list, data);
        this.dataSet = data;
        this.mContext=context;

    }

    public ArrayList<Expenses> getData() {
        return dataSet;
    }

    public void setDataSet(ArrayList<Expenses> dataSet) {
        this.dataSet.addAll(dataSet);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= LayoutInflater.from(getContext());
            View rowView=inflater.inflate(R.layout.expense_summary_list, parent, false);
            Expenses expenses = getItem(position);//dataSet.get(position);
            TextView description = (TextView) rowView.findViewById(R.id.description);
            TextView dateExpense = (TextView) rowView.findViewById(R.id.transactionDate);
            TextView creditDebit = (TextView) rowView.findViewById(R.id.transactionType);
            TextView amountSpent=(TextView) rowView.findViewById(R.id.transactionAmount);
            description.setText(expenses.getDesc());
            dateExpense.setText(expenses.getDate());
            creditDebit.setText(expenses.getTranstype());
            //amountSpent.setText(expenses.getAmount());
            return rowView;
        };
    }
