package com.example.usid.mpos.technicalService;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Udeesha on 2/7/2017.
 */

public class Product implements Parcelable {
    private int id;
    private String name;
    private String barcode;
    private double unitPrice;
    private Bitmap image;
    private double amount;

    /**
     * Static value for UNDEFINED ID.
     */
    public static final int UNDEFINED_ID = -1;
    public static int new_ID =0;
    public Product(String name, double unitPrice, double amount) {
        this.name = name;
        this.unitPrice = unitPrice;
        this.amount = amount;
    }
    public Product(int id, String name, double unitPrice, double amount) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.amount = amount;
    }


    /**
     * Constructs a new Product.
     * @param id ID of the product, This value should be assigned from database.
     * @param name name of this product.
     * @param barcode barcode (any standard format) of this product.
     * @param salePrice price for using when doing sale.
     */
    public Product(int id, String name, String barcode, double salePrice, double amount, Bitmap image) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.unitPrice = salePrice;
        this.image=image;
        this.amount=amount;
    }

    /**
     * Constructs a new Product.
     * @param name name of this product.
     * @param barcode barcode (any standard format) of this product.
     * @param salePrice price for using when doing sale.
     */
    public Product(String name, String barcode, double salePrice, double amount, Bitmap image) {
        this(new_ID, name, barcode, salePrice,amount,image);
        new_ID++;
    }

    protected Product(Parcel in) {
        id = in.readInt();
        name = in.readString();
        barcode = in.readString();
        unitPrice = in.readDouble();
//        image = in.readParcelable(Bitmap.class.getClassLoader());
        amount = in.readDouble();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    /**
     * Returns name of this product.
     * @return name of this product.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of this product.
     * @param name name of this product.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets barcode of this product.
     * @param barcode barcode of this product.
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    /**
     * Sets price of this product.
     * @param unitPrice price of this product.
     */
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    /**
     * Returns id of this product.
     * @return id of this product.
     */
    public int getId() {
        return id;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    /**

     * Returns barcode of this product.
     * @return barcode of this product.
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Returns price of this product.
     * @return price of this product.
     */
    public double getUnitPrice() {
        return unitPrice;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**

     * Returns the description of this Product in Map format.
     * @return the description of this Product in Map format.
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", id + "");
        map.put("name", name);
        map.put("barcode", barcode);
        map.put("unitPrice", unitPrice + "");
        return map;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(barcode);
        parcel.writeDouble(unitPrice);
        parcel.writeDouble(amount);


    }
}
