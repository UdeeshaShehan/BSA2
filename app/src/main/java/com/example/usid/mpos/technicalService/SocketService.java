package com.example.usid.mpos.technicalService;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by usid on 12/1/16.
 */

public class SocketService extends Service{
    private String LOG_TAG = null;


    @Override
    public void onCreate() {
        super.onCreate();
        LOG_TAG = this.getClass().getSimpleName();
        Log.i(LOG_TAG, "In onCreate");

    }
    private final int SERVER_PORT = 8080;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "In onStartCommand");

        new Thread(new Runnable() {
            public void run() {
                try {
                    //Create a server socket object and bind it to a port
                    ServerSocket socServer = new ServerSocket(SERVER_PORT);
                    //Create server side client socket reference
                    Socket socClient = null;
                    String result;
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
                        try {
                            //Get the data input stream comming from the client
                            InputStream is = mySocket.getInputStream();
                            //Get the output stream to the client
                            PrintWriter out = new PrintWriter(
                                    mySocket.getOutputStream(), true);
                            //Write data to the data output stream
                            out.println("Hello from server \r");
                            //Buffer the data input stream
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(is));
                            //Read the contents of the data buffer
                            result = br.readLine();
                            Log.e("Result", result);
                            //Close the client connection
                            mySocket.close();
                        } catch (IOException e1) {
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return START_REDELIVER_INTENT;
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
    }}
