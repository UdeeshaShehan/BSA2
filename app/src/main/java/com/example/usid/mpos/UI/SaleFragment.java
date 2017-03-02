package com.example.usid.mpos.UI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usid.mpos.MainActivity;
import com.example.usid.mpos.R;
import com.example.usid.mpos.domain.inventory.Inventory;
import com.example.usid.mpos.domain.inventory.LineItem;
import com.example.usid.mpos.domain.inventory.Product;
import com.example.usid.mpos.domain.inventory.ProductCatalog;
import com.example.usid.mpos.domain.sales.Register;
import com.example.usid.mpos.technicalService.BluetoothChatService;
import com.example.usid.mpos.technicalService.Communicator;
import com.example.usid.mpos.technicalService.FragmentCommunicator;
import com.example.usid.mpos.technicalService.NoDaoSetException;
import com.example.usid.mpos.technicalService.SalesDetails;
import com.example.usid.mpos.technicalService.SocketService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.StringTokenizer;

import static android.content.ContentValues.TAG;
import static android.content.Context.WIFI_SERVICE;
import static com.example.usid.mpos.MainActivity.plist;

/**
 * UI for Sale operation.
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class SaleFragment extends UpdatableFragment implements FragmentCommunicator,Observer {
    
	private Register register;
	private ArrayList<Map<String, String>> saleList;
	private ListView saleListView;
	private Button clearButton,confirmButton,addButton,connecting;
	private TextView totalPrice;
	private Button endButton;
	private UpdatableFragment reportFragment;
	private Resources res;
	private ArrayList<Map<String, String>> list;
    private ProductCatalog productCatalog;
	ListView listView;
	TextView totaltex,cardNo,quant;
	private Thread thread;
	BroadcastReceiver receiver;
	Intent serviceIntent;
	HashMap<String,Product> hmap;
	Product p1,p2;
	private static int itemNo=0;
	static int count=0;
	Communicator com;
	JSONObject obj;
	/**
	 * Construct a new SaleFragment.
	 * @param
	 */
	public SaleFragment(UpdatableFragment reportFragment) {
		super();
		this.reportFragment = reportFragment;
		 hmap= new HashMap<String,Product>();

	}
	private Button enabling,discovering,send;
	String barcode1[]=new String [2];

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		p1=new Product("RS232","6940570323939",500.00);
		p2=new Product("Book","4792210100156",50.00);
		hmap.put("6940570323939",p1);
		hmap.put("4792210100156",p2);
		barcode1[0]="6940570323939";
		barcode1[1]="4792210100156";


		//try {

		/*}catch(Exception e){
			e.printStackTrace();
		}*/
		obj = new JSONObject();
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
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				String result = intent.getStringExtra("result");
			if(result.length()>=3) {
				if (!result.substring(0, 2).equals("ba")) {
					if(result.length()>=40)
					cardNo.setText(result.substring(22, 38));//.substring(5,21));
				} else {
					Product pd = null;
					setListViewSale(result.substring(3));
					Log.e("barcode",result.substring(3));

			/*try {

                     pd=new Product("item "+itemNo,result.substring(3),50);
				itemNo++;
						if (pd != null) {
							total += pd.getUnitPrice();
							list.add(pd.toMap());
						} else {
							Toast.makeText(getActivity().getBaseContext(), "No Barcode : " + result, Toast.LENGTH_LONG).show();
						}

					}catch (Exception e){
						e.printStackTrace();
					}*/

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
					//setListViewSale(result.substring(3));
					//cardNo.setText(result);
				}
			}
			}
		};
		return view;
	}
	private final int SERVER_PORT = 8080;
	String saleBarcode;
	Button bt;
	Thread t;
	int en = 0;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listView=(ListView)getActivity().findViewById(R.id.sale_List);
		totaltex=(TextView)getActivity().findViewById(R.id.totalPrice);
		quant=(TextView)getActivity().findViewById(R.id.quantity);
		cardNo=(TextView)getActivity().findViewById(R.id.cardNo);
		clearButton=(Button)getActivity().findViewById(R.id.clearButton);
		confirmButton=(Button)getActivity().findViewById(R.id.endButton);
		addButton=(Button)getActivity().findViewById(R.id.addButton);
		enabling=(Button)getActivity().findViewById(R.id.enabling);
		discovering=(Button)getActivity().findViewById(R.id.discoverable);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		olist=new ArrayList<Map<String, String>>();
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(getActivity().getBaseContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
			//finish();

		}

		//com=(Communicator) getActivity();
		list=new ArrayList<Map<String,String>>();
		//db=com.getDatabase();
//        tvClientMsg=(TextView) getActivity().findViewById(R.id.textViewClientMessage);
//        // tvClientMsg = (TextView) findViewById(R.id.textViewClientMessage);
//        tvServerIP = (TextView) getActivity().findViewById(R.id.textViewServerIP);
//        tvServerPort = (TextView) getActivity().findViewById(R.id.textViewServerPort);
//        tvServerPort.setText(Integer.toString(SERVER_PORT));
		connecting=(Button)getActivity().findViewById(R.id.device);
		enabling.setOnClickListener(new View.OnClickListener() {


			@Override
			public void onClick(View view) {
				if (en == 0) {
					enable();
					enabling.setText("Disable");
					en = 1;
				} else {
					disable();
					enabling.setText("Enable");
					en = 0;
				}
			}
		});
		connecting.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				connect();
			}
		});
		discovering.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				discoverable();
			}
		});
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
		clearButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View view) {
			//     	new UDPAsyncTask().execute("IP");
				if(list!=null)
					list.clear();
				if(olist!=null)
					olist.clear();
				totaltex.setText("");
				if(adapter!=null)
					adapter.notifyDataSetChanged();
				cardNo.setText("");
				totaltex.setText("");
				itemNo=0;
				total=0;
				SalesDetails.bill="";

			}
		});
		confirmButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				/*if(count==2)
					count=0;
                String f=barcode1[count];
				count++;
				setListViewSale(f);
				Float tf=new Float(total);
				*/
				com.sendPrice(Double.toString(total));
				Map<String,String> product;
				Iterator it=list.iterator();
				List<String> list1 = new ArrayList<String>();
				//if(SalesDetails.bill.length()>2&&SalesDetails.bill.charAt(0)!='9')
				SalesDetails.bill="9-";
				while (it.hasNext()){
					product=(Map<String,String>) it.next();
					list1.add(product.get("name")+":"+product.get("unitPrice"));
							//product.get("unitprice"));
					//SalesDetails.bill+=(product.get("name")+","+product.get("unitprice")+" ");
					//Log.e("Products",product.get("name"));
				}
				Set<String> uniqueSet = new HashSet<String>(list1);
				for (String temp : uniqueSet) {
					SalesDetails.bill+=temp + ": " + Collections.frequency(list1, temp)+"-";
				}


			}
		});
		addButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				AddItem newFragment = new AddItem(SaleFragment.this);
				newFragment.show(getActivity().getSupportFragmentManager(), "");
			}
		});

	}
	ArrayList<Map<String, String>> olist;
	ListViewAdapter2 adapter;
	float total = 0;
	double to;
	public void updateListOrder(Product pd){
		quant.setText("Quantity");
		olist.add(pd.toMap());
		to+=(pd.getUnitPrice()*pd.getAmount());
		totaltex.setText(Double.toString(to));
		Constants.THIRD_COLUMN="amount";
		adapter = new ListViewAdapter2(getActivity(), olist);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				int pos = position + 1;
				Toast.makeText(getActivity().getBaseContext(), Integer.toString(pos) + " Clicked", Toast.LENGTH_SHORT).show();
			}

		});
	}
	public void setListViewSale(String s) {
		quant.setText("Barcode");
		Constants.THIRD_COLUMN="barcode";
		Log.e("List","list");
		Log.e("list",s);
		saleBarcode = "1,2,3,4";
		int in=0;
		if (s != null){
			saleBarcode = s;
			//StringTokenizer st2 = new StringTokenizer(saleBarcode, ",");
			Product pd = null;

		/*	try {
				//pd = productCatalog.searchProduct(saleBarcode).get(0);
				if(hmap.containsKey(saleBarcode)){
					pd=hmap.get(saleBarcode);
				}
				if (pd != null) {
					total += pd.getUnitPrice();
					list.add(pd.toMap());
				} else {
					Toast.makeText(getActivity().getBaseContext(), "No Barcode : " + s, Toast.LENGTH_LONG).show();
				}*/

				try {
					/*for(int i=0;i<2;i++)
						Log.e("SaleFragment",productCatalog.getProductById(i).getName());
*/
					//pd = productCatalog.searchProduct(s).get(0);
					pd = productCatalog.getProductByBarcode(s);
					if (pd != null) {
						total += pd.getUnitPrice();
						list.add(pd.toMap());
					} else {
						Toast.makeText(getActivity().getBaseContext(), "No Barcode : " + s, Toast.LENGTH_LONG).show();
					}

				}catch (Exception e){
					e.printStackTrace();
				}
			/*}catch(Exception e){
				e.printStackTrace();
			}*/
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
	public void setListViewSaleNew(String s) {
		quant.setText("Barcode");
		Constants.THIRD_COLUMN="barcode";
		saleBarcode = "1,2,3,4";
		int in=0;
		if (s != null){
			saleBarcode = s;
			Product pd = null;

			try {
				for(int i=0;i<plist.size();i++){
					Toast.makeText(getActivity().getBaseContext(), "No2 Barcode : " + plist.get(i).getBarcode(), Toast.LENGTH_LONG).show();
					if(plist.get(i).getBarcode().equals(s)){
						pd=plist.get(i);
						break;
					}
				}


				if (pd != null) {
					total += pd.getUnitPrice();
					list.add(pd.toMap());
				} else {
					Toast.makeText(getActivity().getBaseContext(), "No1 Barcode : " + s, Toast.LENGTH_LONG).show();
				}

			}catch (Exception e){
				e.printStackTrace();
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
		Constants.THIRD_COLUMN="barcode";
		quant.setText("Barcode");
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
		serviceIntent = new Intent(getActivity().getApplicationContext(),
				SocketService.class);
		getActivity().startService(serviceIntent);

		getActivity().registerReceiver(receiver, new IntentFilter(
				SocketService.BROADCAST_ACTION));
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
		getActivity().stopService(serviceIntent);
		getActivity().unregisterReceiver(receiver);
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

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		context = getActivity();
		com =(Communicator) context;
		((MainActivity)context).fragCom = this;

	}

	@Override
	public void passDataToActivity(String name, String barcode, String price) {
		Product pd;
		try {
		    pd=new Product(name,barcode,Double.parseDouble(price));
			if (pd != null) {
				total += pd.getUnitPrice();
				list.add(pd.toMap());
			} else {
				Toast.makeText(getActivity().getBaseContext(), "No Barcode : " +barcode, Toast.LENGTH_LONG).show();
			}
			/*while (st2.hasMoreElements()) {
				tk=(String) st2.nextElement();
				pd = productCatalog.searchProduct(tk).get(0);
				if (pd != null) {
					total += pd.getUnitPrice();
					list.add(pd.toMap());
				} else {
					Toast.makeText(getActivity().getBaseContext(), "No Barcode : " + tk, Toast.LENGTH_LONG).show();
				}

			}*/
		}catch(Exception e){
			e.printStackTrace();
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

	@Override
	public void update(Observable o, Object arg) {

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
	class UDPAsyncTask extends AsyncTask<String, Void, String> {
		//Background task which serve for the client
		@Override
		protected String doInBackground(String... params) {
			String result = null;
		//	runUdpClient();
			return result;
		}

		@Override
		protected void onPostExecute(String s) {

		}
	}
	/**
	 * Get the IP of current Wi-Fi connection
	 * @return IP as string
	 */
	private String getIP() {
		try {
			WifiManager wifiManager = (WifiManager)getActivity().getBaseContext().getSystemService(WIFI_SERVICE);
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
/*	private void runUdpClient()  {
		String udpMsg = getIP()+":"+55058;
		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket();
			InetAddress serverAddr = InetAddress.getByName("192.168.8.100");
			DatagramPacket dp;
			dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, 55056);
			Log.d("UDP","sended");
			Toast.makeText(getActivity().getBaseContext(), "sending",
					Toast.LENGTH_SHORT).show();
			ds.send(dp);
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			ds.receive(receivePacket);
			modifiedSentence = new String(receivePacket.getData());
			Toast.makeText(getActivity().getBaseContext(), modifiedSentence,
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
	}*/
	public static final int DAILY = 0;
	public static final int WEEKLY = 1;
	public static final int MONTHLY = 2;
	public static final int YEARLY = 3;
	BroadcastReceiver receiverr;
	Intent serviceIntentr;
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;


	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private Button enable,disable,discover,connect;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;

	// Member object for the chat services
	private BluetoothChatService mChatService = null;

    /*private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MessageAdapter mAdapter;*/

	public int counter = 0;
	public void enable(){
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			if (mChatService == null) setupChat();
		}
	}
	public void disable(){
		mBluetoothAdapter.disable();
		if (mChatService != null) mChatService.stop();
		Toast.makeText(getActivity().getApplicationContext(),"Bluetooth turned off",
				Toast.LENGTH_LONG).show();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mChatService != null) mChatService.stop();
	}
	Button mSendButton;
	private void setupChat() {
		/*mOutEditText = (EditText) findViewById(R.id.amount_id);
		pin= (EditText) findViewById(R.id.CVV_num);
		mOutEditText.setOnEditorActionListener(mWriteListener);
		*/
		mSendButton = (Button) getActivity().findViewById(R.id.send);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String message = SalesDetails.bill;
				sendMessage(message);
			}
		});

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(getActivity(), mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}
	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() !=
				BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}


	private void sendMessage(String message) {

		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(getActivity().getBaseContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}
		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);
			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			//mOutEditText.setText(mOutStringBuffer);
		}
	}
	public void connect() {
		if(mBluetoothAdapter.isEnabled()) {
			Intent serverIntent = new Intent(getActivity().getApplicationContext(), DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			Toast.makeText(getActivity().getBaseContext(), "devices",
					Toast.LENGTH_SHORT).show();
		}else {
			Toast.makeText(getActivity().getBaseContext(), "please bluetooth enable",
					Toast.LENGTH_SHORT).show();
		}

	}

	public void discoverable() {
		if(mBluetoothAdapter.isEnabled()) {
			ensureDiscoverable();
			Toast.makeText(getActivity().getBaseContext(), "discovering",
					Toast.LENGTH_SHORT).show();
		}else {
			Toast.makeText(getActivity().getBaseContext(), "please bluetooth enable",
					Toast.LENGTH_SHORT).show();
		}
	}
	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_WRITE:
					byte[] writeBuf = (byte[]) msg.obj;
					// construct a string from the buffer
					String writeMessage = new String(writeBuf);
                    /*mAdapter.notifyDataSetChanged();
                    messageList.add(new androidRecyclerView.Message(counter++, writeMessage, "Me"));*/
					break;
				case MESSAGE_READ:
					try {
						byte[] readBuf = (byte[]) msg.obj;
						// construct a string from the valid bytes in the buffer
						String readMessage = new String(readBuf, 0, msg.arg1);
						Toast.makeText(getActivity().getBaseContext(), readMessage, Toast.LENGTH_LONG).show();
						Log.d("BLue",readMessage);
						if(readMessage.length()>2&&readMessage.charAt(0)!='9') {
							try {
								StringTokenizer st = new StringTokenizer(readMessage, " ");
								String res[] = new String[5];
								int i = 0;
								while (st.hasMoreTokens()) {
									res[i] = st.nextToken();
									i++;
								}
								st = new StringTokenizer(res[1], "^");
								String res2[] = new String[5];
								i = 0;
								while (st.hasMoreTokens()) {
									res2[i] = st.nextToken();
									i++;
								}
								// "no, %B4216890200522445^KARUNASINGHE/NALIN D^1710221190460000000000394000000?";
                           /* cardHolder.setText(res2[1].substring(0, 15));
                            Log.d("expire", res2[2]);

                            expiryDate.setText(res2[2].substring(0, 2) + "/" + res2[2].substring(2, 4));
                            cardNo.setText(res2[0].substring(2, 4) + "********");*/
								Log.d("expire", res2[0]);
							}catch (Exception e){
								e.printStackTrace();
							}
                   /* mAdapter.notifyDataSetChanged();
                    messageList.add(new androidRecyclerView.Message(counter++, readMessage, mConnectedDeviceName));*/
						}else{
							if(list!=null)
								list.clear();
							if(olist!=null)
								olist.clear();
							if(adapter!=null)
								adapter.notifyDataSetChanged();

							totaltex.setText("");
							to=0;
							total= (float) 0.0;
							StringTokenizer st = new StringTokenizer(readMessage, "-");
							ArrayList <String> res = new ArrayList<String>();
							to=0;
							int i = 0;
							while (st.hasMoreTokens()) {
								res.add(st.nextToken());

							}
							for(int j=1;j<res.size();j++) {
								st = new StringTokenizer(res.get(j), ":");
								String res2[] = new String[5];
								int k = 0;
								while (st.hasMoreTokens()) {
									res2[k] = st.nextToken();
									k++;
								}
								Product pd=new Product(res2[0],Double.parseDouble(res2[1]),Double.parseDouble(res2[2]));
								updateListOrder(pd);
                                /*Map<String, String> map = new HashMap<String, String>();
                                map.put("name", res2[0]);
                                map.put("barcode", res2[1]);
                                map.put("unitPrice", res2[3]);*/
                               /* SalesDetails salesDetails=new SalesDetails();
                                salesDetails.addList(map);*/

							}
						}
					}catch (Exception e){
						e.printStackTrace();
					}
					break;
				case MESSAGE_DEVICE_NAME:
					// save the connected device's name
					mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
					Toast.makeText(getActivity().getBaseContext(), "Connected to "
							+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
					break;
				case MESSAGE_TOAST:
					Toast.makeText(getActivity().getBaseContext(), msg.getData().getString(TOAST),
							Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CONNECT_DEVICE:
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK) {
					// Get the device MAC address
					String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
					// Get the BLuetoothDevice object
					BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

					// Attempt to connect to the device
					if(mChatService!=null)
						mChatService.connect(device);
				}
				break;
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a chat session
					setupChat();
				} else {
					// User did not enable Bluetooth or an error occured
					Toast.makeText(getActivity().getBaseContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();

				}
		}
	}


}
