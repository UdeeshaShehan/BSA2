package com.example.usid.mpos.UI;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.usid.mpos.MainActivity;
import com.example.usid.mpos.R;
import com.example.usid.mpos.domain.inventory.Inventory;
import com.example.usid.mpos.domain.inventory.Product;
import com.example.usid.mpos.domain.inventory.ProductCatalog;
import com.example.usid.mpos.domain.sales.Register;
import com.example.usid.mpos.technicalService.DatabaseExecutor;
import com.example.usid.mpos.technicalService.Demo;
import com.example.usid.mpos.technicalService.NoDaoSetException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * UI for Inventory, shows list of Product in the ProductCatalog.
 * Also use for a sale process of adding Product into sale.
 * 
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class InventoryFragment extends UpdatableFragment {

	protected static final int SEARCH_LIMIT = 0;
	private ListView inventoryListView;
	private ProductCatalog productCatalog;
	private List<Map<String, String>> inventoryList;
	private Button addProductButton;
	private EditText searchBox;
	private Button searchButton,synbutton;

	private ViewPager viewPager;
	private Register register;
	private MainActivity main;

	private UpdatableFragment saleFragment;
	private Resources res;

	/**
	 * Construct a new InventoryFragment.
	 * @param saleFragment
	 */
	public InventoryFragment(UpdatableFragment saleFragment) {
		super();
		this.saleFragment = saleFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		try {
			productCatalog = Inventory.getInstance().getProductCatalog();
			register = Register.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		View view = inflater.inflate(R.layout.layout_inventory, container, false);

		res = getResources();
		inventoryListView = (ListView) view.findViewById(R.id.productListView);
		addProductButton = (Button) view.findViewById(R.id.addProductButton);
		searchButton = (Button) view.findViewById(R.id.searchButton);
		searchBox = (EditText) view.findViewById(R.id.searchBox);
		synbutton=(Button) view.findViewById(R.id.syncButton);

		main = (MainActivity) getActivity();
		viewPager = main.getViewPager();

		initUI();
		return view;
	}

	/**
	 * Initiate this UI.
	 */
	private void initUI() {

		addProductButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup(v);
			}
		});

		searchBox.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				if (s.length() >= SEARCH_LIMIT) {
					search();
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		});

		inventoryListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
				int id = Integer.parseInt(inventoryList.get(position).get("id").toString());

				register.addItem(productCatalog.getProductById(id), 1);
				//saleFragment.update();
				viewPager.setCurrentItem(1);
			}     
		});
		synbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Product pd;
				//get


					// URLEncode user defined data

				/*	String loginValue    = URLEncoder.encode(login.getText().toString(), "UTF-8");
					String fnameValue  = URLEncoder.encode(fname.getText().toString(), "UTF-8");
					String emailValue   = URLEncoder.encode(email.getText().toString(), "UTF-8");
					String passValue    = URLEncoder.encode(pass.getText().toString(), "UTF-8");*/
				SyncAsyncTask runner = new SyncAsyncTask();
				runner.execute("");


				}



			/*if (nameBox.getText().toString().equals("")
						|| barcodeBox.getText().toString().equals("")
						|| priceBox.getText().toString().equals("")) {

					Toast.makeText(getActivity().getBaseContext(),
							res.getString(R.string.please_input_all), Toast.LENGTH_SHORT)
							.show();

				} else {
					boolean success = productCatalog.addProduct(nameBox
							.getText().toString(), barcodeBox.getText()
							.toString(), Double.parseDouble(priceBox.getText()
							.toString()));
					pd=new Product(nameBox
							.getText().toString(), barcodeBox.getText()
							.toString(), Double.parseDouble(priceBox.getText()
							.toString()));
					addInJSONArray(pd);
					MainActivity.plist.add(pd);

					if (success) {
						Toast.makeText(getActivity().getBaseContext(),
								res.getString(R.string.success) + ", "
										+ nameBox.getText().toString(),
								Toast.LENGTH_SHORT).show();

						fragment.update();
						clearAllBox();
						AddProductDialogFragment.this.dismiss();

					} else {
						Toast.makeText(getActivity().getBaseContext(),
								res.getString(R.string.fail),
								Toast.LENGTH_SHORT).show();
					}
				}*/
			//}
		});

	/*	searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegratorSupportV4 scanIntegrator = new IntentIntegratorSupportV4(InventoryFragment.this);
				scanIntegrator.initiateScan();
			}
		});*/

	}

	/**
	 * Show list.
	 * @param list
	 */
	private void showList(List<Product> list) {

		inventoryList = new ArrayList<Map<String, String>>();
		for(Product product : list) {
			inventoryList.add(product.toMap());
		}

		ButtonAdapter sAdap = new ButtonAdapter(getActivity().getBaseContext(), inventoryList,
				R.layout.listview_inventory, new String[]{"name"}, new int[] {R.id.name}, R.id.optionView, "id");
		inventoryListView.setAdapter(sAdap);
	}

	/**
	 * Search.
	 */
	private void search() {
		String search = searchBox.getText().toString();

		if (search.equals("/demo")) {
			testAddProduct();
			searchBox.setText("");
		} else if (search.equals("/clear")) {
			DatabaseExecutor.getInstance().dropAllData();
			searchBox.setText("");
		}
		else if (search.equals("")) {
			showList(productCatalog.getAllProduct());
		} else {
			List<Product> result = productCatalog.searchProduct(search);
			showList(result);
			if (result.isEmpty()) {

			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
/*		IntentResult scanningResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);

		if (scanningResult != null) {
			String scanContent = scanningResult.getContents();
			searchBox.setText(scanContent);
		} else {
			Toast.makeText(getActivity().getBaseContext(), res.getString(R.string.fail),
					Toast.LENGTH_SHORT).show();
		}*/
	}

	/**
	 * Test adding product
	 */
	protected void testAddProduct() {
		Demo.testProduct(getActivity());
		Toast.makeText(getActivity().getBaseContext(), res.getString(R.string.success),
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * Show popup.
	 * @param anchorView
	 */
	public void showPopup(View anchorView) {
		AddProductDialogFragment newFragment = new AddProductDialogFragment(InventoryFragment.this);
		newFragment.show(getFragmentManager(), "");
	}

	@Override
	public void update() {
		search();
	}

	@Override
	public void onResume() {
		super.onResume();
		update();
	}
	class SyncAsyncTask extends AsyncTask<String, Void, String> {
		//Background task which serve for the client
		@Override
		protected String doInBackground(String... params) {
			String result = null;
			// Create http cliient object to send request to server

			HttpClient Client = new DefaultHttpClient();

			// Create URL string

			String URL = "http://safsaf.net16.net/get.php";


			//Log.i("httpget", URL);

			try
			{
				String SetServerString = "";
				String[] productDetail=new String[10];

				// Create Request to server and get response

				HttpGet httpget = new HttpGet(URL);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				SetServerString = Client.execute(httpget, responseHandler);

				// Show response on activity

				Log.d("nalin",SetServerString);
				/*Toast.makeText(getActivity().getBaseContext(),
						SetServerString,
						Toast.LENGTH_SHORT).show();*/
				StringTokenizer tokenizer = new StringTokenizer(SetServerString, " \t\n\r\f\",.:;?![]'");//["11111111111, Soap, , Eli Lili and co, 5, 58.50, 15, 2017-02-28, 65"]
				int count=0;
				while (tokenizer.hasMoreElements()) {
					productDetail[count]=tokenizer.nextElement().toString();
					count++;
					if(count>8)
						break;
				}
				if(count>=3){
					boolean success = productCatalog.addProduct(productDetail[1], productDetail[0], 50.0);
					if (success) {
						/*Toast.makeText(getActivity().getBaseContext(),
								res.getString(R.string.success) ,
								Toast.LENGTH_SHORT).show();*/

						saleFragment.update();


					} else {
						/*Toast.makeText(getActivity().getBaseContext(),
								res.getString(R.string.fail),
								Toast.LENGTH_SHORT).show();*/
					}
					count=0;
				}

			}
			catch(Exception ex)
			{   ex.printStackTrace();
				Log.d("nalin","Fail!");
			}
			return result;
		}

		@Override
		protected void onPostExecute(String s) {

		}
	}


}