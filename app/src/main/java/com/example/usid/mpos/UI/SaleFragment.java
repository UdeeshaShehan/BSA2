package com.example.usid.mpos.UI;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import com.example.usid.mpos.technicalService.Communicator;
import com.example.usid.mpos.technicalService.FragmentCommunicator;
import com.example.usid.mpos.technicalService.NoDaoSetException;
import com.example.usid.mpos.technicalService.SocketService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.content.Context.WIFI_SERVICE;
import static com.example.usid.mpos.MainActivity.plist;

/**
 * UI for Sale operation.
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class SaleFragment extends UpdatableFragment implements FragmentCommunicator{
    
	private Register register;
	private ArrayList<Map<String, String>> saleList;
	private ListView saleListView;
	private Button clearButton,confirmButton,addButton;
	private TextView totalPrice;
	private Button endButton;
	private UpdatableFragment reportFragment;
	private Resources res;
	private ArrayList<Map<String, String>> list;
    private ProductCatalog productCatalog;
	ListView listView;
	TextView totaltex,cardNo;
	private Thread thread;
	BroadcastReceiver receiver;
	Intent serviceIntent;
	HashMap<String,Product> hmap;
	Product p1,p2;
	private static int itemNo=0;
	static int count=0;
	Communicator com;
	/**
	 * Construct a new SaleFragment.
	 * @param
	 */
	public SaleFragment(UpdatableFragment reportFragment) {
		super();
		this.reportFragment = reportFragment;
		 hmap= new HashMap<String,Product>();

	}
	String barcode1[]=new String [2];
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		p1=new Product("RS232","6940570323939",500.00);
		p2=new Product("Book","4792210100156",50.00);
		hmap.put("6940570323939",p1);
		hmap.put("4792210100156",p2);
		barcode1[0]="6940570323939";
		barcode1[1]="4792210100156";
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
				if(!result.substring(0,2).equals("ba")) {
					cardNo.setText(result.substring(22,38));//.substring(5,21));
				}else {
					Product pd = null;
					setListViewSale(result.substring(3));

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
					setListViewSale(result.substring(3));
					//cardNo.setText(result);
				}

			}
		};
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
		clearButton=(Button)getActivity().findViewById(R.id.clearButton);
		confirmButton=(Button)getActivity().findViewById(R.id.endButton);
		addButton=(Button)getActivity().findViewById(R.id.addButton);

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
		clearButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View view) {
				new UDPAsyncTask().execute("IP");
				if(list!=null)
					list.clear();
				if(adapter!=null)
					adapter.notifyDataSetChanged();
				cardNo.setText("");
				totaltex.setText("");
				itemNo=0;
				total=0;

			}
		});
		confirmButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(count==2)
					count=0;
                String f=barcode1[count];
				count++;
				setListViewSale(f);
				Float tf=new Float(total);
				com.sendPrice(tf.toString());
				/*Product product;
				List<Product> list=productCatalog.getAllProduct();
				Iterator it=list.iterator();
				while (it.hasNext()){
					product=(Product) it.next();
					Log.e("Products",product.getName());
				}*/


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
	ListViewAdapter2 adapter;
	float total = 0;

	public void setListViewSale(String s) {
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
			runUdpClient();
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
	private void runUdpClient()  {
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
	}

}
