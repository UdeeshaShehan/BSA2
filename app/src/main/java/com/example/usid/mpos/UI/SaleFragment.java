package com.example.usid.mpos.UI;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usid.mpos.R;
import com.example.usid.mpos.domain.inventory.Inventory;
import com.example.usid.mpos.domain.inventory.LineItem;
import com.example.usid.mpos.domain.inventory.Product;
import com.example.usid.mpos.domain.inventory.ProductCatalog;
import com.example.usid.mpos.domain.sales.Register;
import com.example.usid.mpos.technicalService.NoDaoSetException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * UI for Sale operation.
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class SaleFragment extends UpdatableFragment {
    
	private Register register;
	private ArrayList<Map<String, String>> saleList;
	private ListView saleListView;
	private Button clearButton;
	private TextView totalPrice;
	private Button endButton;
	private UpdatableFragment reportFragment;
	private Resources res;
	private ArrayList<Map<String, String>> list;
    private ProductCatalog productCatalog;
	ListView listView;
	TextView totaltex,cardNo;
	private Thread thread;
	/**
	 * Construct a new SaleFragment.
	 * @param
	 */
	public SaleFragment(UpdatableFragment reportFragment) {
		super();
		this.reportFragment = reportFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		try {
			register = Register.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		View view = inflater.inflate(R.layout.layout_sale, container, false);

		try {
			productCatalog = Inventory.getInstance().getProductCatalog();
			register = Register.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
		res = getResources();
		/*saleListView = (ListView) view.findViewById(R.id.sale_List);
		totalPrice = (TextView) view.findViewById(R.id.totalPrice);
		clearButton = (Button) view.findViewById(R.id.clearButton);
		endButton = (Button) view.findViewById(R.id.endButton);*/

	//initUI();
		return view;
	}
	private final int SERVER_PORT = 8080;
	String saleBarcode;
	Button bt;
	Thread t;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listView=(ListView)getActivity().findViewById(R.id.sale_List);
		totaltex=(TextView)getActivity().findViewById(R.id.totalPrice);
		cardNo=(TextView)getActivity().findViewById(R.id.cardNo);
		bt=(Button)getActivity().findViewById(R.id.clearButton);
		//com=(Communicator) getActivity();
		list=new ArrayList<Map<String,String>>();
		//db=com.getDatabase();
//        tvClientMsg=(TextView) getActivity().findViewById(R.id.textViewClientMessage);
//        // tvClientMsg = (TextView) findViewById(R.id.textViewClientMessage);
//        tvServerIP = (TextView) getActivity().findViewById(R.id.textViewServerIP);
//        tvServerPort = (TextView) getActivity().findViewById(R.id.textViewServerPort);
//        tvServerPort.setText(Integer.toString(SERVER_PORT));
		//Call method
		//getDeviceIpAddress();
		//New thread to listen to incoming connections
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					//Create a server socket object and bind it to a port
					ServerSocket socServer = new ServerSocket(SERVER_PORT);
					//Create server side client socket reference
					Socket socClient = null;
					//Infinite loop will listen for client requests to connect
					while (true) {
						//Accept the client connection and hand over communication to server side client socket
						socClient = socServer.accept();
						//For each client new instance of AsyncTask will be created
						ServerAsyncTask serverAsyncTask = new ServerAsyncTask();
						//Start the AsyncTask execution
						//Accepted client socket object will pass as the parameter
						serverAsyncTask.execute(new Socket[] {socClient});
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		//thread.start();

		//list=db.getAllProducts();
		bt.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View view) {
				if(list!=null)
					list.clear();
				if(adapter!=null)
					adapter.notifyDataSetChanged();
				cardNo.setText("");
				totaltex.setText("");
			}
		});

	}
	ListViewAdapter2 adapter;
	public void setListViewSale(String s) {
		saleBarcode = "1,2,3,4";
		int in=0;
		if (s != null){
			saleBarcode = s;
			StringTokenizer st2 = new StringTokenizer(saleBarcode, ",");
			Product pd = null;
			float total = 0;
			String tk;
			while (st2.hasMoreElements()) {
				tk=(String) st2.nextElement();
				pd = productCatalog.searchProduct(tk).get(0);
				if (pd != null) {
					total += pd.getUnitPrice();
					list.add(pd.toMap());
				} else {
					Toast.makeText(getActivity().getBaseContext(), "No Barcode : " + tk, Toast.LENGTH_LONG).show();
				}

			}
			totaltex.setText(Float.toString(total));
			adapter = new ListViewAdapter2(getActivity(), list);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
					int pos = position + 1;
					Toast.makeText(getActivity().getBaseContext(), Integer.toString(pos) + " Clicked", Toast.LENGTH_SHORT).show();
				}

			});

		}
	}

	/**
	 * Initiate this UI.
	 */
	/*private void initUI() {
		
		saleListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				showEditPopup(arg1,arg2);
			}
		});

		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewPager viewPager = ((MainActivity) getActivity()).getViewPager();
				viewPager.setCurrentItem(1);
			}
		});

		endButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(register.hasSale()){
					showPopup(v);
				} else {
					Toast.makeText(getActivity().getBaseContext() , res.getString(R.string.hint_empty_sale), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!register.hasSale() || register.getCurrentSale().getAllLineItem().isEmpty()) {
					Toast.makeText(getActivity().getBaseContext() , res.getString(R.string.hint_empty_sale), Toast.LENGTH_SHORT).show();
				} else {
					showConfirmClearDialog();
				}
			} 
		});
	}*/
	
	/**
	 * Show list
	 * @param list
	 */
	private void showList(List<LineItem> list) {
		
		saleList = new ArrayList<Map<String, String>>();
		for(LineItem line : list) {
			saleList.add(line.toMap());
		}
		
		SimpleAdapter sAdap;
		sAdap = new SimpleAdapter(getActivity().getBaseContext(), saleList,
				R.layout.listview_lineitem, new String[]{"name","quantity","price"}, new int[] {R.id.name,R.id.quantity,R.id.price});
		saleListView.setAdapter(sAdap);
	}

	/**
	 * Try parsing String to double.
	 * @param value
	 * @return true if can parse to double.
	 */
	public boolean tryParseDouble(String value)
	{  
		try  {  
			Double.parseDouble(value);
			return true;  
		} catch(NumberFormatException e) {
			return false;  
		}  
	}
	
	/**
	 * Show edit popup.
	 * @param anchorView
	 * @param position
	 */
	public void showEditPopup(View anchorView, int position){
		Bundle bundle = new Bundle();
		bundle.putString("position",position+"");
		bundle.putString("sale_id",register.getCurrentSale().getId()+"");
		bundle.putString("product_id",register.getCurrentSale().getLineItemAt(position).getProduct().getId()+"");
	/*
		EditFragmentDialog newFragment = new EditFragmentDialog(SaleFragment.this, reportFragment);
		newFragment.setArguments(bundle);
		newFragment.show(getFragmentManager(), "");*/
		
	}

	/**
	 * Show popup
	 * @param anchorView
	 */
	public void showPopup(View anchorView) {
		Bundle bundle = new Bundle();
		bundle.putString("edttext", totalPrice.getText().toString());
	/*	PaymentFragmentDialog newFragment = new PaymentFragmentDialog(SaleFragment.this, reportFragment);
		newFragment.setArguments(bundle);
		newFragment.show(getFragmentManager(), "");*/
	}

	@Override
	public void update() {
		if(register.hasSale()){
			showList(register.getCurrentSale().getAllLineItem());
			totalPrice.setText(register.getTotal() + "");
		}
		else{
			showList(new ArrayList<LineItem>());
			totalPrice.setText("0.00");
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
/*		if(!thread.isAlive()) {
			thread.start();
		}*/
	//	update();
	}

	@Override
	public void onStop() {
		super.onStop();
/*		if(thread!= null) {
			thread.stop();

		}*/
	}

	@Override
	public void onPause() {
		super.onPause();
		/*if(thread!= null) {
			thread.stop();

		}*/
	}

	/**
	 * Show confirm or clear dialog.
	 */
	private void showConfirmClearDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setTitle(res.getString(R.string.dialog_clear_sale));
		dialog.setPositiveButton(res.getString(R.string.no), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		dialog.setNegativeButton(res.getString(R.string.clear), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				register.cancleSale();
				update();
			}
		});

		dialog.show();
	}
	class ServerAsyncTask extends AsyncTask<Socket, Void, String> {
		//Background task which serve for the client
		@Override
		protected String doInBackground(Socket... params) {
			String result = null;
			//Get the accepted socket object
			Socket mySocket = params[0];
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
				//Close the client connection
				mySocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String s) {
			//After finishing the execution of background task data will be write the text view
			//tvClientMsg.setText(s);
			//Toast.makeText(getActivity().getBaseContext(), s, Toast.LENGTH_LONG).show();
			int i=0;
			if(s.substring(0,2).equals("no"))
				cardNo.setText(s.substring(5,21));
			else
				setListViewSale(s);
		}
	}

}
