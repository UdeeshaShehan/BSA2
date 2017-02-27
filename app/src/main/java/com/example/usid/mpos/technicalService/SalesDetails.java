package com.example.usid.mpos.technicalService;

import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;

/**
 * Created by Udeesha on 2/21/2017.
 */

public class SalesDetails  extends Observable {
    public static String bill="";
    ArrayList<Map<String,String>> gotList;

    public SalesDetails() {
        this.gotList =new ArrayList<Map<String,String>>();;
    }
    public void addList(Map<String, String> map){
        setChanged();
        notifyObservers();
    }
}
