package edu.sdsu.vyshak.personalexpensetracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import edu.sdsu.vyshak.personalexpensetracker.data.DBHelper;
import edu.sdsu.vyshak.personalexpensetracker.R;
import edu.sdsu.vyshak.personalexpensetracker.bean.Budget;

/**
 * Created by Vyshak on 5/9/2017.
 */

public class CustomBudgetAdapter extends ArrayAdapter<Budget> {


    private ArrayList<Budget> dataSet;
    Context mContext;

    public CustomBudgetAdapter(ArrayList<Budget> data, Context context) {
        super(context, R.layout.budget_custom, data);
        this.dataSet = data;
        this.mContext=context;
    }

    public ArrayList<Budget> getData() {
        return dataSet;
    }

    public void setDataSet(ArrayList<Budget> dataSet) {
        this.dataSet.addAll(dataSet);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= LayoutInflater.from(getContext());
        View rowView=inflater.inflate(R.layout.budget_custom, null,true);
        final Budget shopItems = dataSet.get(position);
        ImageButton doneShopping = (ImageButton) rowView.findViewById(R.id.budgetsClose);
        final TextView name = (TextView) rowView.findViewById(R.id.budgetname);
        final TextView price = (TextView) rowView.findViewById(R.id.budgetamount);
        final TextView freq = (TextView) rowView.findViewById(R.id.budgetfreq);
        name.setText(shopItems.getCategory());
        price.setText(String.valueOf(shopItems.getAmount()));
        freq.setText(shopItems.getCycle() );

        doneShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData().remove(shopItems);
                DBHelper dbHelper = new DBHelper(getContext());
                dbHelper.removeBudget(shopItems.getCategory());
                notifyDataSetChanged();
            }
        });
        return rowView;
    };
}
