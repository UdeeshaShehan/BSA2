package com.example.usid.mpos.technicalService;

import java.util.Observable;

/**
 * Created by Udeesha on 2/3/2017.
 */

public class KeepAlive extends Observable{
    private static boolean device1,device2,device3,device4;
    private static KeepAlive instance;
    public static KeepAlive getInstance(){
        if(instance==null)
            instance=new KeepAlive();
        return instance;
    }
    private KeepAlive() {
        device1=false;
        device2=false;
        device3=false;
        device4=false;
    }

    public boolean getDevice1() {
        return device1;
    }

    public void setDevice1(boolean device1) {
        this.device1 = device1;
        setChanged();
        notifyObservers();
    }

    public boolean getDevice2() {
        return device2;
    }

    public void setDevice2(boolean device2) {
        this.device2 = device2;
        setChanged();
        notifyObservers();
    }

    public boolean getDevice3() {
        return device3;
    }

    public void setDevice3(boolean device3) {
        this.device3 = device3;
        setChanged();
        notifyObservers();
    }

    public boolean getDevice4() {
        return device4;
    }

    public void setDevice4(boolean device4) {
        this.device4 = device4;
        setChanged();
        notifyObservers();
    }
}
