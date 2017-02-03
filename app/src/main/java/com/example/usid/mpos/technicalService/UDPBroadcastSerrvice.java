package com.example.usid.mpos.technicalService;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * Created by Udeesha on 1/11/2017.
 */

public class UDPBroadcastSerrvice extends Service {
    // constant
    public static final long NOTIFY_INTERVAL = 10 * 3000; // 10 seconds

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    Handler handler;
    @Override
    public void onCreate() {
         handler= new Handler();
        // cancel if already existed
        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 100, NOTIFY_INTERVAL);
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread


                    /*Toast.makeText(getApplicationContext(), "working",
                           Toast.LENGTH_SHORT).show();*/
            handler.post(new Runnable() {
                public void run() {
                    new Thread(new Task()).start();
                   // new UDPAsyncTask().execute("IP");
                    Log.d("dilushan",getIP());
                }
            });

        }


    }
    /**
     * Get the IP of current Wi-Fi connection
     * @return IP as string
     */
    private String getIP() {
        try {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return null;
        }
    }
    /** Get IP For mobile */
    public static String getMobileIP() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipaddress = inetAddress .getHostAddress().toString();
                        return ipaddress;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, "Exception in Get IP Address: " + ex.toString());
        }
        return null;
    }
    byte[] receiveData = new byte[1024];
    String modifiedSentence;
    private void runUdpClient()  {
        String udpMsg = getIP()+":"+55092;
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName("192.168.8.255");
            DatagramPacket dp;
            dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, 55092);
            Log.d("UDP","sended");
           /* Toast.makeText(getApplicationContext(), "sending",
                    Toast.LENGTH_SHORT).show();*/
            ds.send(dp);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            ds.receive(receivePacket);
            modifiedSentence = new String(receivePacket.getData());
            Toast.makeText(getApplicationContext(), modifiedSentence,
                    Toast.LENGTH_SHORT).show();
        } catch (SocketException e) {
            e.printStackTrace();
        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ds != null) {
                ds.close();
            }
        }
    }
    class UDPAsyncTask extends AsyncTask<String, Void, String> {
        //Background task which serve for the client
        @Override
        protected String doInBackground(String... params) {
            String result = null;
            runUdpClient();
            Log.d("dilushan","timer");
            return result;
        }

        @Override
        protected void onPostExecute(String s) {

        }
    }
    class Task implements Runnable {
        @Override
        public void run() {
            runUdpClient();
            Log.d("dilushan","timer");
        }

    }

}
