package com.example.usid.mpos.UI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import com.example.usid.mpos.domain.inventory.ProductCatalog;
import com.example.usid.mpos.technicalService.Communicator;

/**
 * Created by Udeesha on 12/24/2016.
 */

@SuppressLint("ValidFragment")
public class AddItem extends DialogFragment {

    private EditText barcodeBox;
    private ProductCatalog productCatalog;
    private EditText priceBox;
    private EditText nameBox;
    private Button confirmButton;
    private Button clearButton;
    private UpdatableFragment fragment;
    private Resources res;
    public Context context;
    private Communicator com;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = getActivity();
        com =(Communicator) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.layout_additem, container,
                false);
        res = getResources();

        barcodeBox= (EditText) v.findViewById(R.id.barcodeItemBox);
        nameBox = (EditText) v.findViewById(R.id.nameItemBox);
        priceBox=(EditText) v.findViewById(R.id.priceItemBox);
        confirmButton = (Button) v.findViewById(R.id.confirmItemButton);
        clearButton = (Button) v.findViewById(R.id.clearItemButton);
        getDialog().setTitle("Add Item");
        initUI();
        return v;
    }


    @SuppressLint("ValidFragment")
    public AddItem(SaleFragment fragment) {
        super();
        this.fragment = fragment;

    }

    /**
     * Clear all box
     */
    private void clearAllBox() {
        barcodeBox.setText("");
        nameBox.setText("");
        priceBox.setText("");

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
                if ( nameBox.getText().toString().equals("")||priceBox.getText().toString().equals("")
                        ) {

                    Toast.makeText(getActivity().getBaseContext(),
                            res.getString(R.string.please_input_all), Toast.LENGTH_SHORT)
                            .show();

                } else {
                    if(barcodeBox.getText().toString().equals("")){ //communicate without barcode

                        com.respond(nameBox.getText().toString(),"*************",priceBox.getText().toString());
                    }else{
                        com.respond(nameBox.getText().toString(),barcodeBox.getText().toString(),priceBox.getText().toString());
                    }
                    AddItem.this.dismiss();
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( barcodeBox.getText().toString().equals("") && nameBox.getText().toString().equals("")&&priceBox.getText().toString().equals("")){
                    AddItem.this.dismiss();
                } else {
                    clearAllBox();
                }
            }
        });
    }


}
