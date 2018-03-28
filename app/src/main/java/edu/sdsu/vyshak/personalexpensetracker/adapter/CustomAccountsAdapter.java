package edu.sdsu.vyshak.personalexpensetracker.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.sdsu.vyshak.personalexpensetracker.bean.Display;
import edu.sdsu.vyshak.personalexpensetracker.R;

/**
 * Created by Vyshak on 5/8/2017.
 */

public class CustomAccountsAdapter extends ArrayAdapter<Display> {

    private ArrayList<Display> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtSender;
        TextView txtMessageSender;
        TextView txtReciever;
        TextView txtMessageReciever;
    }

    public CustomAccountsAdapter(ArrayList<Display> data, Context context) {
        super(context, R.layout.accounts_custom, data);
        this.dataSet = data;
        this.mContext=context;
    }

    public ArrayList<Display> getData() {
        return dataSet;
    }

    public void setDataSet(ArrayList<Display> dataSet) {
        this.dataSet.addAll(dataSet);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= LayoutInflater.from(getContext());
        View rowView=inflater.inflate(R.layout.accounts_custom, null,true);
        final Display disp = dataSet.get(position);
        final TextView itemToBePurchased = (TextView) rowView.findViewById(R.id.account_name);
        itemToBePurchased.setText(disp.getDisplayname());
        return rowView;
    };
}