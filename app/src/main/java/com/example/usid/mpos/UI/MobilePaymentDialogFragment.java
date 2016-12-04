package com.example.usid.mpos.UI;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.usid.mpos.R;

/**
 * Created by usid on 12/1/16.
 */

public class MobilePaymentDialogFragment extends DialogFragment {
    private UpdatableFragment fragment;
    private EditText paymentID;
    private EditText pinNum;
    private Button confirmButton;
    private Button clearButton;
    private Resources res;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.layout_mobile_payment, container,
                false);
        res = getResources();
        paymentID = (EditText) v.findViewById(R.id.mobileNumBox);
        pinNum = (EditText) v.findViewById(R.id.IdBox);
        confirmButton = (Button) v.findViewById(R.id.confirmButton);
        clearButton = (Button) v.findViewById(R.id.clearButton);
        getDialog().setTitle("Mobile Payment");
        initUI();
        return v;
    }


    @SuppressLint("ValidFragment")
    public MobilePaymentDialogFragment() {
        super();

    }

    /**
     * Clear all box
     */
    private void clearAllBox() {
        paymentID.setText("");
        pinNum.setText("");

    }
    /**
     * Construct a new
     */
    private void initUI() {
		/*scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegratorSupportV4 scanIntegrator = new IntentIntegratorSupportV4(AddProductDialogFragment.this);
				scanIntegrator.initiateScan();
			}
		});*/

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (pinNum.getText().toString().equals("")
                        || paymentID.getText().toString().equals("")
                        ) {

                    Toast.makeText(getActivity().getBaseContext(),
                            res.getString(R.string.please_input_all), Toast.LENGTH_SHORT)
                            .show();

                } else {

                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( paymentID.getText().toString().equals("") && pinNum.getText().toString().equals("")){
                    MobilePaymentDialogFragment.this.dismiss();
                } else {
                    clearAllBox();
                }
            }
        });
    }
}
