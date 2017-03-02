package com.example.usid.mpos.technicalService;

import android.app.Activity;
import android.app.FragmentManager;
import android.util.Log;

import com.example.usid.mpos.SecurityController;
import com.example.usid.mpos.UI.ReportFragment;
import com.example.usid.mpos.domain.inventory.Product;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.internal.ExceptionHelper;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.util.Strings;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * Created by nifras on 2/19/17.
 */

public class MQTTConnection {


    public static String url = "192.168.8.101";
    static String sAddress = "tcp://" + url +":1883";
    ArrayList<Product> list;
/*    static String sUserName = "admin";
    static String sPassword = "admin";
    static String sDestination = "credit";




    final String serverUri = "tcp://192.168.8.101:1883";


    final String subscriptionTopic = "/topic/credit";*/
    public String corre_id = "sdfsdfdgdh3t34sw23";
    public   String publishTopic = "credit";
    public String clientId = "PosLankaMerchant";
    public static String response=null;

    private static MqttClient client;
    ReportFragment activity;

    public MQTTConnection(String topic, ReportFragment activity) {
        publishTopic = topic;
        clientId = clientId+topic;
        list = new ArrayList<>();
        this.activity = activity;

    }

    public static MqttClient getClient() {
        return client;
    }

    public  boolean connect() {
        try {
            sAddress = "tcp://" + url+ ":1883";

            MemoryPersistence persistance = new MemoryPersistence();
            client = new MqttClient(sAddress, clientId, persistance);
            if(!client.isConnected()) {
                client.connect();
                sub();
                return true;
            }

        } catch (MqttException e) {
//            e.printStackTrace();
        } catch (Exception e) {
//            e.printStackTrace();

        }


        return false;
    }

    public  boolean pub(String payload) {

        try {
            if(!client.isConnected()){
                connect();
            }
            String encPayload = SecurityController.encrypt(payload);
            MqttMessage message = new MqttMessage(encPayload.getBytes());

            client.publish(publishTopic, message);

            return true;
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }
    public  void sub(){
        try {

            client.subscribe(publishTopic, new IMqttMessageListener(){
                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    byte [] payload = mqttMessage.getPayload();
                    String msg = new String(payload);
                    msg = SecurityController.decrypt(msg);

                    Log.d("MQTT", msg);
                    try {
                        JSONObject jsonObject = new JSONObject(msg);
                        //!jsonObject.get("corre-id").equals("123226651942") ||
                        if(jsonObject.has("status") && jsonObject.get("corre-id").equals(corre_id)){
                            response = msg;
                        }


                        else if (jsonObject.has("total") && jsonObject.has("items")){
                            double total = Double.parseDouble(jsonObject.get("total").toString());
                            JSONArray jsonarray = jsonObject.getJSONArray("items");
                            JSONObject bill = new JSONObject();

//                            bill.put("items", jsonarray);
//                            bill.put("total", total);
                            for(int i=0;i<jsonarray.length();i++)
                            {


                                try {
                                    JSONObject currLot = jsonarray.getJSONObject(i);
                                    double  unitPrice = Double.parseDouble(currLot.get("unitPrice").toString());
                                    String name =  currLot.get("name").toString();
                                    int id = Integer.parseInt(currLot.get("id").toString());
                                    String barcode = currLot.get("barcode").toString();
                                    int amount = Integer.parseInt(currLot.getString("amount").toString());


                                    Product product = new Product(id,name, unitPrice, barcode);
                                    product.setAmount(amount);
                                    list.add(product);
                                }
                                catch (Exception e){

                                }


                            }
                            activity.showBilltoMerchandiser(list, total);

                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }



                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }


}
