package com.example.usid.mpos.UI;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.usid.mpos.R;
import com.example.usid.mpos.domain.inventory.Product;

import java.util.ArrayList;


/**
 * Created by usid on 8/13/16.
 */
public class ListViewAdapter3 extends BaseAdapter {

    public ArrayList<Product> list;
    FragmentActivity activity;
    TextView txtFirst;
    TextView txtSecond;
    TextView txtThird;
    TextView txtFourth;
    public ListViewAdapter3(FragmentActivity activity, ArrayList<Product> list){
        super();
        this.activity=activity;
        this.list=list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub



        LayoutInflater inflater=activity.getLayoutInflater();

        if(convertView == null){

            convertView=inflater.inflate(R.layout.colum_row3, null);


            txtSecond=(TextView) convertView.findViewById(R.id.name1);
            txtThird=(TextView) convertView.findViewById(R.id.quantity1);
            txtFourth=(TextView) convertView.findViewById(R.id.unitPrice1);

        }
        txtFourth.setTextColor(Color.BLACK);
        txtSecond.setTextColor(Color.BLACK);
        txtThird.setTextColor(Color.BLACK);
        Product map=list.get(position);
        txtSecond.setText(map.getName());
        txtThird.setText(Double.toString(map.getAmount()));
        txtFourth.setText(Double.toString(map.getUnitPrice()*map.getAmount()));

        return convertView;
    }
}
