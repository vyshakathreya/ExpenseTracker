package edu.sdsu.vyshak.personalexpensetracker;

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

/**
 * Created by vysha on 5/8/2017.
 */

public class CustomAccountsAdapter extends ArrayAdapter<Disp> {


    private ArrayList<Disp> dataSet;
    Context mContext;



    // View lookup cache
    private static class ViewHolder {
        TextView txtSender;
        TextView txtMessageSender;
        TextView txtReciever;
        TextView txtMessageReciever;
    }

    public CustomAccountsAdapter(ArrayList<Disp> data, Context context) {
        super(context, R.layout.accounts_custom, data);
        this.dataSet = data;
        this.mContext=context;

    }

    public ArrayList<Disp> getData() {
        return dataSet;
    }

    public void setDataSet(ArrayList<Disp> dataSet) {
        this.dataSet.addAll(dataSet);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= LayoutInflater.from(getContext());
        View rowView=inflater.inflate(R.layout.accounts_custom, null,true);
        final Disp disp = dataSet.get(position);
        final TextView itemToBePurchased = (TextView) rowView.findViewById(R.id.account_name);
        itemToBePurchased.setText(disp.getDisplayname());
        return rowView;
    };
}