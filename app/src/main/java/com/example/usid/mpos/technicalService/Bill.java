package com.example.usid.mpos.technicalService;

import com.example.usid.mpos.domain.inventory.Product;

import java.util.ArrayList;

/**
 * Created by nifras on 3/3/17.
 */

public class Bill {
    ArrayList<Product> productArrayList;
    double totalPrice;

    public Bill(ArrayList<Product> productArrayList, double totalPrice) {
        this.productArrayList = productArrayList;
        this.totalPrice = totalPrice;
    }

    public ArrayList<Product> getProductArrayList() {
        return productArrayList;
    }

    public void setProductArrayList(ArrayList<Product> productArrayList) {
        this.productArrayList = productArrayList;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
