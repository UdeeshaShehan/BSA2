package com.example.usid.mpos.UI;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.usid.mpos.R;

import java.util.ArrayList;
import java.util.Map;

import static com.example.usid.mpos.UI.Constants.FOURTH_COLUMN;
import static com.example.usid.mpos.UI.Constants.SECOND_COLUMN;
import static com.example.usid.mpos.UI.Constants.THIRD_COLUMN;

/**
 * Created by usid on 8/13/16.
 */
public class ListViewAdapter2 extends BaseAdapter {

    public ArrayList<Map<String, String>> list;
    Activity activity;
    TextView txtFirst;
    TextView txtSecond;
    TextView txtThird;
    TextView txtFourth;
    public ListViewAdapter2(Activity activity, ArrayList<Map<String, String>> list){
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

            convertView=inflater.inflate(R.layout.colum_row2, null);


            txtSecond=(TextView) convertView.findViewById(R.id.name);
            txtThird=(TextView) convertView.findViewById(R.id.unitPrice);
            txtFourth=(TextView) convertView.findViewById(R.id.barcode);

        }
        txtSecond.setTextColor(Color.WHITE);
        txtThird.setTextColor(Color.WHITE);
        txtFourth.setTextColor(Color.WHITE);
        Map<String, String> map=list.get(position);
        txtSecond.setText(map.get(SECOND_COLUMN));
        txtThird.setText(map.get(THIRD_COLUMN));
        txtFourth.setText(map.get(FOURTH_COLUMN));

        return convertView;
    }
}
