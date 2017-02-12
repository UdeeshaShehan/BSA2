package com.example.usid.mpos.technicalService;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.ChaChaEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by usid on 12/1/16.
 */

public class SocketService extends Service {
    private static String LOG_TAG = "BroadcastService";
    public static final String BROADCAST_ACTION = "com.websmithing.broadcasttest.displayevent";
    Intent intent1;
    long b[];
    KeepAlive keepAlive;

    @Override
    public void onCreate() {
        super.onCreate();
        LOG_TAG = this.getClass().getSimpleName();
        Log.i(LOG_TAG, "In onCreate");
        b=new long[4];
        keepAlive=KeepAlive.getInstance();
        MyTimerTask myTask = new MyTimerTask();
        Timer myTimer = new Timer();
        myTimer.schedule(myTask, 40000, 40000);
        intent1 = new Intent(BROADCAST_ACTION);
    }
    private final int SERVER_PORT = 8080;
    private static ServerSocket socServerU;
    private static ServerSocket socServer;
    private ServerSocket getInstance(){
        if(socServerU==null){
            try {
                socServerU = new ServerSocket(); // <-- create an unbound socket first
                socServerU.setReuseAddress(true);
                socServerU.bind(new InetSocketAddress(SERVER_PORT));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return socServerU;
    }
    Thread thread;
    long timestamp;
    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "In onStartCommand");

        thread=new Thread(new Runnable() {
            public void run() {
                try {
                    //Create a server socket object and bind it to a port
                   /* ServerSocket socServer = new ServerSocket(SERVER_PORT);*/
                    socServer = getInstance();
                   /* try {
                        socServer = new ServerSocket(); // <-- create an unbound socket first
                        socServer.setReuseAddress(true);
                        socServer.bind(new InetSocketAddress(SERVER_PORT)); // <-- now bind it
                    }catch(BindException e){
                        socServer.close();
                        socServer = getInstance();
                        e.printStackTrace();
                    }*/

                    //Create server side client socket reference
                  //  socServer=getInstance();
                    Socket socClient = null;
                    String result;
                    char a;
                    Date today;
                    Timestamp ts1;
                    //Infinite loop will listen for client requests to connect
                    while (true) {
                        //Accept the client connection and hand over communication to server side client socket
                        socClient = socServer.accept();
                        //For each client new instance of AsyncTask will be created
                        //   ReportFragment.ServerAsyncTask serverAsyncTask = new ReportFragment.ServerAsyncTask();
                        //Start the AsyncTask execution
                        //Accepted client socket object will pass as the parameter
                        //serverAsyncTask.execute(new Socket[] {socClient});
                        Socket mySocket = socClient;
                        DatagramSocket ds = null;
                        try {
                            //Get the data input stream comming from the client
                            // String j[]={"7B4117E8", "C9B97794E1809E07BB271BF07C861003" };
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
                            InputStream is = mySocket.getInputStream();
                            //Get the output stream to the client
                            PrintWriter out = new PrintWriter(
                                    mySocket.getOutputStream(), true);
                            InetAddress serverAddr = socClient.getInetAddress();
                            Log.d("IP clien",serverAddr.toString());
                            String test="OK";
                            try (InputStream isEnc = new ByteArrayInputStream(test.getBytes(StandardCharsets.UTF_8));
                                 ByteArrayOutputStream osEnc = new ByteArrayOutputStream())
                            {
                                encChaCha(isEnc, osEnc, key, iv);
                                Log.d("chacha",getHex(osEnc.toByteArray()));
                               /* out.write(osEnc.toString());*/
                               // out.println(5);
                                 /*char s[]=new char[64];
                                   s=getHex(osEnc.toByteArray()).toCharArray();
                                for(int i=0;i<s.length;i++){
                                    out.println(s[i]);
                                }*/
                              out.println(getHex(osEnc.toByteArray()));
                                Log.d("sended",getHex(osEnc.toByteArray()));
                               // out.println("Udeesha Dilshan Murshith Safwan");
                            }
                            //Write data to the data output stream

                            //Buffer the data input stream
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(is));
                            //Read the contents of the data buffer

                            String actual;
                            try (InputStream isDec = is;
                                 ByteArrayOutputStream osDec = new ByteArrayOutputStream())
                            {
                                decChaCha(isDec, osDec, key, iv);

                                byte[] decoded = osDec.toByteArray();

                                actual = new String(decoded, StandardCharsets.UTF_8);
                                Log.d("Chacha",actual);
                                //System.out.println(test+" "+actual);
                                //Assert.assertEquals(test, actual);
                            }
                            //result="";
                      //      result = br.readLine();
//                            Log.d("Chacha",result);
                            today = new java.util.Date();
                            ts1 = new java.sql.Timestamp(today.getTime());
                            String modifiedSentence;
                            Long tsLong = System.currentTimeMillis()/1000;
                            String ts = tsLong.toString();
                            if(actual!=null&&!actual.equals("")&&actual.length()>=9&&actual.charAt(0)=='5'){
                                modifiedSentence = actual.substring(2);
                                Log.d("receive", modifiedSentence);
                                int total;
                                String t, t1;
                                       /* String actual2;
                                        InputStream stream = new ByteArrayInputStream(modifiedSentence.getBytes(StandardCharsets.UTF_8));
                                        try (InputStream isDec = stream;
                                             ByteArrayOutputStream osDec = new ByteArrayOutputStream())
                                        {
                                            decChaCha(isDec, osDec, key, iv);

                                            byte[] decoded = osDec.toByteArray();

                                            actual2 = new String(decoded, StandardCharsets.UTF_8);
                                            Log.d("ChachaUDP",actual2);
                                            //System.out.println(test+" "+actual);
                                            //Assert.assertEquals(test, actual);
                                        }
*/
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (modifiedSentence.length() == 8) {
                                    try {
                                        total = Integer.parseInt(modifiedSentence.substring(0, 4)) + Integer.parseInt(modifiedSentence.substring(4));
                                        if (Integer.toString(total).length() == 4)
                                            t = "0" + Integer.toString(total) + " " + Long.toString(ts1.getTime());
                                        else
                                            t = Integer.toString(total) + " " + Long.toString(ts1.getTime());

                                        try (InputStream isEnc = new ByteArrayInputStream(t.getBytes(StandardCharsets.UTF_8));
                                             ByteArrayOutputStream osEnc = new ByteArrayOutputStream()) {
                                            encChaCha(isEnc, osEnc, key, iv);
                                            t1 = new String(osEnc.toByteArray(), "UTF-8");
                                            out.println(t1);
                                            ds = new DatagramSocket();
                                            DatagramPacket dp;
                                            dp = new DatagramPacket(osEnc.toByteArray(), osEnc.size(), serverAddr, 55092);
                                            ds.send(dp);
                                        }
                                        Log.d("res", t1);
                                               /* if(!mySocket.isClosed()) {
                                                    Log.d("Is closed", "not closed");
                                                    PrintWriter out1 = new PrintWriter(
                                                            mySocket.getOutputStream(), true);
                                                    out.println(t1);
                                                    out.flush();
                                                    out.close();
                                                }*/
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            if(actual!=null&&!actual.equals("")&&(actual.length()>10)) {
                                try{
                                if ((Long.parseLong(ts) - Long.parseLong(actual.substring(0, 10))) < 5) {
                                    a = actual.charAt(11);

                                    switch (a) {
                                        case '1':
                                            b[0] = ts1.getTime();
                                            Log.d("light1", Long.toString(b[0]));
                                            break;
                                        case '2':
                                            b[1] = ts1.getTime();
                                            Log.d("light2", Long.toString(b[1]));
                                            break;
                                        case '3':
                                            b[2] = ts1.getTime();
                                            Log.d("light3", Long.toString(b[2]));
                                            break;
                                        case '4':
                                            b[3] = ts1.getTime();
                                            Log.d("light4", Long.toString(b[3]));
                                            break;
                                        case '5':
                                            modifiedSentence = actual.substring(2);
                                            Log.d("receive", modifiedSentence);
                                            int total;
                                            String t, t1;
                                       /* String actual2;
                                        InputStream stream = new ByteArrayInputStream(modifiedSentence.getBytes(StandardCharsets.UTF_8));
                                        try (InputStream isDec = stream;
                                             ByteArrayOutputStream osDec = new ByteArrayOutputStream())
                                        {
                                            decChaCha(isDec, osDec, key, iv);

                                            byte[] decoded = osDec.toByteArray();

                                            actual2 = new String(decoded, StandardCharsets.UTF_8);
                                            Log.d("ChachaUDP",actual2);
                                            //System.out.println(test+" "+actual);
                                            //Assert.assertEquals(test, actual);
                                        }
*/
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            if (modifiedSentence.length() == 8) {
                                                try {
                                                    total = Integer.parseInt(modifiedSentence.substring(0, 4)) + Integer.parseInt(modifiedSentence.substring(4));
                                                    if (Integer.toString(total).length() == 4)
                                                        t = "0" + Integer.toString(total) + " " + Long.toString(ts1.getTime());
                                                    else
                                                        t = Integer.toString(total) + " " + Long.toString(ts1.getTime());

                                                    try (InputStream isEnc = new ByteArrayInputStream(t.getBytes(StandardCharsets.UTF_8));
                                                         ByteArrayOutputStream osEnc = new ByteArrayOutputStream()) {
                                                        encChaCha(isEnc, osEnc, key, iv);
                                                        t1 = new String(osEnc.toByteArray(), "UTF-8");
                                                        out.println(t1);
                                                        ds = new DatagramSocket();
                                                        DatagramPacket dp;
                                                        dp = new DatagramPacket(osEnc.toByteArray(), osEnc.size(), serverAddr, 55092);
                                                        ds.send(dp);
                                                    }
                                                    Log.d("res", t1);
                                               /* if(!mySocket.isClosed()) {
                                                    Log.d("Is closed", "not closed");
                                                    PrintWriter out1 = new PrintWriter(
                                                            mySocket.getOutputStream(), true);
                                                    out.println(t1);
                                                    out.flush();
                                                    out.close();
                                                }*/
                                                } catch (NumberFormatException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            break;
                                    }
                                    intent1.putExtra("result", actual.substring(11));//get rid of timestamp
                                    sendBroadcast(intent1);
                                    //   Log.e("Chacha", "1 one"+result);
                                    //Close the client connection
                                }
                            }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                            mySocket.close();
                        } catch (IOException e1) {
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return START_REDELIVER_INTENT;
    }
    static final String HEXES = "0123456789ABCDEF";

    public static String getHex( byte [] raw ) {
        if ( raw == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * raw.length );
        for ( final byte b : raw ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
           // hex.append(' ');
        }
        return hex.toString();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // Wont be called as service is not bound
        Log.i(LOG_TAG, "In onBind");
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(LOG_TAG, "In onTaskRemoved");
        /*try {
            socServer.close();
            thread.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException en){
            en.printStackTrace();
        }catch (Exception ee){
            ee.printStackTrace();
        }*/
    }

    @Override
    public boolean onUnbind(Intent intent) {
       /* try {
            socServer.close();
            thread.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException en){
            en.printStackTrace();
        }catch (Exception ee){
            ee.printStackTrace();
        }*/
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
        /*try {
            socServer.close();
            thread.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException en){
            en.printStackTrace();
        }catch (Exception ee){
            ee.printStackTrace();
        }*/
    }
    int light=0;
    class MyTimerTask extends TimerTask {
        public void run() {

            /*if(light==0){
                keepAlive.setDevice1(true);
                light=1;
            }else{
                keepAlive.setDevice1(false);
                light=0;
            }*/
            java.util.Date today;
            java.sql.Timestamp ts1;
            today = new java.util.Date();
            ts1 = new java.sql.Timestamp(today.getTime());
            if((ts1.getTime()-b[0])>60000)
                keepAlive.setDevice1(false);
            else if(ts1.getTime()>b[0])
                keepAlive.setDevice1(true);

            if((ts1.getTime()-b[1])>60000)
                keepAlive.setDevice2(false);
            else if(ts1.getTime()>b[1])
                keepAlive.setDevice2(true);

            if((ts1.getTime()-b[2])>60000)
                keepAlive.setDevice3(false);
            else if(ts1.getTime()>b[2])
                keepAlive.setDevice3(true);

            if((ts1.getTime()-b[3])>60000)
                keepAlive.setDevice4(false);
            else if(ts1.getTime()>b[3])
                keepAlive.setDevice4(true);

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
