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

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.ChaChaEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * Created by Udeesha on 1/11/2017.
 */

public class UDPBroadcastSerrvice extends Service implements Observer{
    // constant
    public static final long NOTIFY_INTERVAL = 10 * 3000; // 10 seconds

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;
    KeepAlive k;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    Handler handler;
    @Override
    public void onCreate() {
         handler= new Handler();
        k=KeepAlive.getInstance();
        k.addObserver(this);
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

    @Override
    public void update(Observable o, Object arg) {
        if(k.getDevice1()&&k.getDevice2()&&k.getDevice3()&&k.getDevice4()){
            mTimer.cancel();
        }else{
            mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 100, NOTIFY_INTERVAL);
        }

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
        String udpMsg =getIP();
        DatagramSocket ds = null;
        int total=0;
        String t,t1,t2;
        java.util.Date today;
        java.sql.Timestamp ts1;
        today = new java.util.Date();
        ts1 = new java.sql.Timestamp(today.getTime());
        byte[] key = new byte[32]; // 32 for 256 bit key or 16 for 128 bit
        byte[] iv = new byte[8]; // 64 bit IV required by ChaCha20
        int [] ikey={1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216};
        int [] iiv={101,102,103,104,105,106,107,108};
                            /*key=j[1].getBytes();
                            iv=j[0].getBytes();*/
        //= is.toByteArray();
        for(int i=0;i<32;i++)
            key[i]=(byte) ikey[i];
        for(int i=0;i<8;i++)
            iv[i]=(byte) iiv[i];
        try {
            ds = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName("192.168.8.255");
            DatagramPacket dp;
            try (InputStream isEnc = new ByteArrayInputStream(udpMsg.getBytes(StandardCharsets.UTF_8));
                 ByteArrayOutputStream osEnc = new ByteArrayOutputStream())
            {
                encChaCha(isEnc, osEnc, key, iv);
                t2 = new String(osEnc.toByteArray(),"UTF-8");

            }
            dp = new DatagramPacket(t2.getBytes(), t2.length(), serverAddr, 55092);
            Log.d("UDP","sended");
           /* Toast.makeText(getApplicationContext(), "sending",
                    Toast.LENGTH_SHORT).show();*/
            ds.send(dp);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            ds.receive(receivePacket);
            modifiedSentence = new String(receivePacket.getData());
            String actual;
            InputStream stream = new ByteArrayInputStream(modifiedSentence.getBytes(StandardCharsets.UTF_8));
            try (InputStream isDec = stream;
                 ByteArrayOutputStream osDec = new ByteArrayOutputStream())
            {
                decChaCha(isDec, osDec, key, iv);

                byte[] decoded = osDec.toByteArray();

                actual = new String(decoded, StandardCharsets.UTF_8);
                Log.d("Chacha",actual);
                //System.out.println(test+" "+actual);
                //Assert.assertEquals(test, actual);
            }

            if(actual.length()==8){
               total=Integer.parseInt(actual.substring(0,4))+Integer.parseInt(actual.substring(4));
                t=Integer.toString(total)+" "+Long.toString(ts1.getTime());

                try (InputStream isEnc = new ByteArrayInputStream(t.getBytes(StandardCharsets.UTF_8));
                     ByteArrayOutputStream osEnc = new ByteArrayOutputStream())
                {
                    encChaCha(isEnc, osEnc, key, iv);
                    t1 = new String(osEnc.toByteArray(),"UTF-8");

                }
                dp = new DatagramPacket(t1.getBytes(), t1.length(), serverAddr, 55092);
                ds.send(dp);
            }
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
    public void doChaCha(boolean encrypt, InputStream is, OutputStream os,
                         byte[] key, byte[] iv) throws IOException {
        CipherParameters cp = new KeyParameter(key);
        ParametersWithIV params = new ParametersWithIV(cp, iv);
        StreamCipher engine = new ChaChaEngine();
        engine.init(encrypt, params);


        byte in[] = new byte[8192];
        byte out[] = new byte[8192];
        int len = 0;
        while(-1 != (len = is.read(in))) {
            len = engine.processBytes(in, 0 , len, out, 0);
            os.write(out, 0, len);
        }
    }

    public void encChaCha(InputStream is, OutputStream os, byte[] key,
                          byte[] iv) throws IOException {
        doChaCha(true, is, os, key, iv);
    }

    public void decChaCha(InputStream is, OutputStream os, byte[] key,
                          byte[] iv) throws IOException {
        doChaCha(false, is, os, key, iv);
    }
    public void chachaString() throws IOException, NoSuchAlgorithmException
    {
        String test = "Hello, World!";

        try (InputStream isEnc = new ByteArrayInputStream(test.getBytes(StandardCharsets.UTF_8));
             ByteArrayOutputStream osEnc = new ByteArrayOutputStream())
        {
            //  SecureRandom sr = SecureRandom.getInstanceStrong();
            String a[]={"7B4117E8", "C9B97794E1809E07BB271BF07C861003" };
            byte[] key = new byte[32]; // 32 for 256 bit key or 16 for 128 bit
            byte[] iv = new byte[8]; // 64 bit IV required by ChaCha20
            key=a[1].getBytes();
            iv=a[0].getBytes();
            System.out.println(key+" "+iv);
            //   sr.nextBytes(key);
            //   sr.nextBytes(iv);
            System.out.println(key.toString()+" "+iv.toString());

            encChaCha(isEnc, osEnc, key, iv);

            byte[] encoded = osEnc.toByteArray();
            System.out.println(osEnc);

            try (InputStream isDec = new ByteArrayInputStream(encoded);
                 ByteArrayOutputStream osDec = new ByteArrayOutputStream())
            {
                decChaCha(isDec, osDec, key, iv);

                byte[] decoded = osDec.toByteArray();

                String actual = new String(decoded, StandardCharsets.UTF_8);
                System.out.println(test+" "+actual);
                //Assert.assertEquals(test, actual);
            }
        }
    }

}
