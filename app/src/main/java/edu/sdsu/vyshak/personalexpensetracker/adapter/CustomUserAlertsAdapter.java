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

import edu.sdsu.vyshak.personalexpensetracker.bean.UserAlerts;
import edu.sdsu.vyshak.personalexpensetracker.data.DBHelper;
import edu.sdsu.vyshak.personalexpensetracker.R;

/**
 * Created by Vyshak on 5/12/2017.
 */

public class CustomUserAlertsAdapter extends ArrayAdapter<UserAlerts> {
    private ArrayList<UserAlerts> dataSet;
    private Context mContext;

    public CustomUserAlertsAdapter(ArrayList<UserAlerts> data, Context context) {
        super(context, R.layout.shoplist_custom, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rowView = inflater.inflate(R.layout.alerts_custom, null, true);
        final UserAlerts shopItems = dataSet.get(position);
        CheckBox donePending = (CheckBox) rowView.findViewById(R.id.done_Alert);
        ImageButton doneAlert = (ImageButton) rowView.findViewById(R.id.imageButtonAlertClose);

        final TextView itemToBePurchased = (TextView) rowView.findViewById(R.id.alertmessage);
        itemToBePurchased.setText(shopItems.getDuebill());

        final TextView itemDate = (TextView) rowView.findViewById(R.id.alertdate);
        itemDate.setText(shopItems.getDuedate());

        donePending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                if (checkBox.isChecked())
                    itemToBePurchased.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                else
                    itemToBePurchased.setPaintFlags(checkBox.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

            }
        });
        doneAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataSet.remove(shopItems);
                DBHelper dbHelper = new DBHelper(getContext());
                dbHelper.removeShoppingList(shopItems.getDuebill());
                notifyDataSetChanged();
            }
        });
        return rowView;
    }

    ;
}
