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
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by vysha on 5/6/2017.
 */

public class CustomShopAdapter extends ArrayAdapter<ShopItems> {


    private ArrayList<ShopItems> dataSet;
    Context mContext;



    // View lookup cache
    private static class ViewHolder {
        TextView txtSender;
        TextView txtMessageSender;
        TextView txtReciever;
        TextView txtMessageReciever;
    }

    public CustomShopAdapter(ArrayList<ShopItems> data, Context context) {
        super(context, R.layout.shoplist_custom, data);
        this.dataSet = data;
        this.mContext=context;

    }

    public ArrayList<ShopItems> getData() {
        return dataSet;
    }

    public void setDataSet(ArrayList<ShopItems> dataSet) {
        this.dataSet.addAll(dataSet);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= LayoutInflater.from(getContext());
        View rowView=inflater.inflate(R.layout.shoplist_custom, null,true);
        final ShopItems shopItems = dataSet.get(position);
        CheckBox donePending = (CheckBox) rowView.findViewById(R.id.done_Pending);
        ImageButton doneShopping = (ImageButton) rowView.findViewById(R.id.imageButtonClose);
        final TextView itemToBePurchased = (TextView) rowView.findViewById(R.id.itemToPurchase);
        itemToBePurchased.setText(shopItems.getItemName());
        donePending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                if(checkBox.isChecked())
                    itemToBePurchased.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                else
                    itemToBePurchased.setPaintFlags(checkBox.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

            }
        });
        doneShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData().remove(shopItems);
                DBHelper dbHelper = new DBHelper(getContext());
                dbHelper.removeShoppingList(shopItems.getItemName());
                notifyDataSetChanged();
            }
        });
        return rowView;
    };
}