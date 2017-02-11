package com.example.usid.mpos.UI;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.usid.mpos.R;
import com.example.usid.mpos.technicalService.KeepAlive;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Udeesha on 1/30/2017.
 */

public class DeviceStatus extends Activity implements Observer {
    Button button,button2, button3, button4;
    ImageView image,image2,image3,image4;
    KeepAlive k;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_status);
        image = (ImageView) findViewById(R.id.imageView1);
        image2 = (ImageView) findViewById(R.id.imageView2);
        image3 = (ImageView) findViewById(R.id.imageView3);
        image4 = (ImageView) findViewById(R.id.imageView4);
        k=KeepAlive.getInstance();
        k.addObserver(this);
      //  addListenerOnButton();

    }
    public void device1On(){
        image.setImageResource(R.drawable.on);
    }
    public void device1Off(){
        image.setImageResource(R.drawable.off);
    }
    public void device2On(){
        image2.setImageResource(R.drawable.on);
    }
    public void device2Off(){
        image2.setImageResource(R.drawable.off);
    }
    public void device3On(){
        image3.setImageResource(R.drawable.on);
    }
    public void device3Off(){
        image3.setImageResource(R.drawable.off);
    }
    public void device4On(){
        image4.setImageResource(R.drawable.on);
    }
    public void device4Off(){
        image4.setImageResource(R.drawable.off);
    }

    public void addListenerOnButton() {

        image = (ImageView) findViewById(R.id.imageView1);

        button = (Button) findViewById(R.id.btnChangeImage);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                image.setImageResource(R.drawable.on);
            }

        });
        image2 = (ImageView) findViewById(R.id.imageView2);

        button2 = (Button) findViewById(R.id.btnChangeImage2);
        button2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                image2.setImageResource(R.drawable.on);
            }

        });
        image3 = (ImageView) findViewById(R.id.imageView3);

        button3 = (Button) findViewById(R.id.btnChangeImage3);
        button3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                image3.setImageResource(R.drawable.on);
            }

        });
        image4 = (ImageView) findViewById(R.id.imageView4);

        button4 = (Button) findViewById(R.id.btnChangeImage4);
        button4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                image4.setImageResource(R.drawable.on);
            }

        });

    }

    @Override
    public void update(Observable o, Object arg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(k.getDevice1()){
                    device1On();
                }else{
                    device1Off();
                }
                if(k.getDevice2()){
                    device2On();
                }else{
                    device2Off();
                }
                if(k.getDevice3()){
                    device3On();
                }else{
                    device3Off();
                }
                if(k.getDevice4()){
                    device4On();
                }else{
                    device4Off();
                }
            }
        });


    }
}
