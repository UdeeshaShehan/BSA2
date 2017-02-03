package com.example.usid.mpos.UI;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.usid.mpos.R;

/**
 * Created by Udeesha on 1/30/2017.
 */

public class DeviceStatus extends Activity {
    Button button,button2, button3, button4;
    ImageView image,image2,image3,image4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_status);

        addListenerOnButton();

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

}
