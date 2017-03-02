package com.example.usid.mpos.technicalService;

/**
 * Created by Udeesha on 12/25/2016.
 */

public interface Communicator {
    public void respond(String name,String barcode,String price);
    public void sendPrice(String price);
}
