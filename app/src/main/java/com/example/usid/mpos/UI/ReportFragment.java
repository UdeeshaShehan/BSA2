package com.example.usid.mpos.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usid.mpos.MainActivity;
import com.example.usid.mpos.R;
import com.example.usid.mpos.domain.DateTimeStrategy;
import com.example.usid.mpos.domain.inventory.Product;
import com.example.usid.mpos.domain.sales.Sale;
import com.example.usid.mpos.domain.sales.SaleLedger;
import com.example.usid.mpos.technicalService.BluetoothChatService;
import com.example.usid.mpos.technicalService.MQTTConnection;
import com.example.usid.mpos.technicalService.NoDaoSetException;
import com.example.usid.mpos.technicalService.PriceCommunicator;
import com.example.usid.mpos.technicalService.SalesDetails;
import com.example.usid.mpos.technicalService.SocketService;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.ChaChaEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * UI for showing sale's record.
 * @author Refresh Team
 *
 */
public class ReportFragment extends UpdatableFragment implements PriceCommunicator{
	
	private SaleLedger saleLedger;
	List<Map<String, String>> saleList;
	private ListView saleLedgerListView;
	private TextView totalBox;
	private Spinner spinner;
	private Button previousButton;
	private Button nextButton;
	private TextView currentBox;
	private Calendar currentTime;
	private DatePickerDialog datePicker;
	private final int SERVER_PORT = 8080;
	private Thread thread;
	private Button processPayment;
	private EditText cardNo;
	private EditText expiryDate;
	private EditText cardHolder;
	private EditText CVV;
	private EditText Amount;
	public static final int DAILY = 0;
	public static final int WEEKLY = 1;
	public static final int MONTHLY = 2;
	public static final int YEARLY = 3;
	public static String results ="";
	BroadcastReceiver receiverr;
	Intent serviceIntentr;
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static String PAN="";

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


    MQTTConnection mqttConnectionCredit;
	MQTTConnection mqttConnectionBill;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		try {
			saleLedger = SaleLedger.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
		
		View view = inflater.inflate(R.layout.layout_report, container, false);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(getActivity().getBaseContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
			//finish();

		}
		processPayment = (Button) view.findViewById(R.id.processPayment);
		enable= (Button) view.findViewById(R.id.enable);
	//	send=(Button) view.findViewById(R.id.send);
		discover= (Button) view.findViewById(R.id.discover);
		connect= (Button) view.findViewById(R.id.connect);
		cardHolder = (EditText) view.findViewById(R.id.card_holder_name);
		CVV = (EditText) view.findViewById(R.id.CVV_num);
		expiryDate = (EditText) view.findViewById(R.id.expiry_date);
		cardNo = (EditText) view.findViewById(R.id.Card_number);
		Amount = (EditText) view.findViewById(R.id.amount_id);
		mqttConnectionCredit = new MQTTConnection("credit", this);
		mqttConnectionBill = new MQTTConnection("bill", this);
		enable.setOnClickListener(new View.OnClickListener() {
			int e=0;
			@Override
			public void onClick(View view) {
				if(e==0) {
					enable();
					enable.setText("Disable");
					e=1;
				}else{
					disable();
					e=0;
				}
			}
		});
		/*send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				disable();
			}
		});*/
		discover.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                discoverable();
			}
		});
		connect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                connect();
			}
		});

		processPayment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final String cvv = CVV.getText().toString();
				final String amount = Amount.getText().toString();
				final String name = cardHolder.getText().toString();
				//final String PAN = cardNo.getText().toString().equals("")? "4032039105422911": cardNo.getText().toString();


				if(CVV.length()<=0 || amount.length() <=0){
					AlertDialog.Builder quitDialog = new AlertDialog.Builder(getActivity());
					quitDialog.setTitle("Error..!\nEnter all the details!!!!");
					quitDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {


						}
					});

					quitDialog.show();

				}

				else {


					AsyncTask at = new AsyncTask() {
						ProgressDialog progress;

						@Override
						protected void onPreExecute() {
							super.onPreExecute();

							progress = new ProgressDialog(getActivity());
							progress.setTitle("Credit Card");
							progress.setMessage("Processing...");
							progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
							progress.show();
// To dismiss the dialog

						}

						@Override
						protected Object doInBackground(Object[] params) {



							//HashMap<String, String> data = new HashMap<>();
							JSONObject data = new JSONObject();
							try {
								data.put("name_on_card", name);
								data.put("amount", amount);
								data.put("card_type", "visa");
								data.put("card_number", PAN);
								data.put("expiry_month", "12");
								data.put("expiry_year", "2021");
								data.put("cvv", cvv);
								data.put("orderID", "12324");
								data.put("corre-id", mqttConnectionCredit.corre_id);
								try{
									mqttConnectionCredit.pub(data.toString());
								}
								catch (Exception e){
									e.printStackTrace();
								}
							/*	try {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											cardHolder.setText("");



								//while (MQTTConnection.response==null);
											expiryDate.setText("");
											cardNo.setText("");
										}});
								}catch(Exception e){
									e.printStackTrace();
								}*/
								//while (MQTTConnection.response==null);

								/*final Timer timer = new Timer();


								timer.scheduleAtFixedRate(new TimerTask() {
									@Override
									public void run() {
										while (MQTTConnection.response==null);
										timer.cancel();

									}
								},10000, 10000);

								timer.purge();*/

								long now = System.currentTimeMillis();
								int runtime=10000;//in milliseconds
								do
								{
									if(mqttConnectionCredit.response !=null)
										break;
									//enter your code here
								}while (now+runtime<System.currentTimeMillis());


							} catch (JSONException e) {
								e.printStackTrace();
							}



							Log.e("Results", results);


                            //Connection connection = Connection.getInstance();
                            //results = connection.post("payment_process.php", data);



                            return null;
						}


						@Override
						protected void onPostExecute(Object o) {
							super.onPostExecute(o);
							try {
								progress.dismiss();
								JSONObject res = null;
								try {


									res = new JSONObject(mqttConnectionCredit.response);
									mqttConnectionCredit.response =null;
									if (res.getString("status").equals("1")) {
										Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
										cardHolder.setText("");
										CVV.setText("");
										expiryDate.setText("");
										cardNo.setText("");
										Amount.setText("");
									} else {
										Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();

									}
								} catch (JSONException e) {
									Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
									e.printStackTrace();
								} catch (Exception e){
									Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
									e.printStackTrace();
								}
							}catch (Exception e){

								e.printStackTrace();
							}

						}
					};
					//finally execute the task
					at.execute();
				}
			}
		});

		/*previousButton = (Button) view.findViewById(R.id.previousButton);
		nextButton = (Button) view.findViewById(R.id.nextButton);
		currentBox = (TextView) view.findViewById(R.id.currentBox);
		saleLedgerListView = (ListView) view.findViewById(R.id.saleListView);
		totalBox = (TextView) view.findViewById(R.id.totalBox);
		spinner = (Spinner) view.findViewById(R.id.spinner1);*/
		
		initUI();
		receiverr = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
              try {
				  String result = intent.getStringExtra("result");
				  if (result.length() >= 20) {
					  if (!result.substring(0, 2).equals("ba")) {
						  String track = result;// "no, %B4216890200522445^KARUNASINGHE/NALIN D^1710221190460000000000394000000?";
						  track = track.replaceAll("no", "");
						  track = track.replaceAll("\\,", "");

						  String[] details = track.split("\\^");
						  details[0] = details[0].replace("%B", "");
						  details[1] = details[1].replace("/", " ");
						  details[2] = details[2].substring(0, 4);
						  String cardNu = details[0].substring(0, 4) + " **** **** ****";
						  String eYear = details[2].substring(0, 2);
						  String eMonth = details[2].substring(2);
						  /*StringTokenizer st = new StringTokenizer(track, "^");
						  String res[] = new String[10];
						  int i = 0;
						  while (st.hasMoreTokens()) {
							  res[i] = st.nextToken();
							  i++;
						  }*/
						  PAN = details[0];

						  cardHolder.setText(details[1]);

						  expiryDate.setText(eMonth + "/" + eYear);
						  cardNo.setText(cardNu);
						  playDefaultNotificationSound();
					  }
				  }
			  }catch (Exception e){
				  e.printStackTrace();
			  }
			}
		};

		connectMQTT(mqttConnectionCredit);
		connectMQTT(mqttConnectionBill);
		return view;
	}
	private void playDefaultNotificationSound() {
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
		r.play();
	}
	private void connectMQTT(final MQTTConnection mqttConnection){

		AsyncTask asyncTask = new AsyncTask() {
			@Override
			protected Object doInBackground(Object[] objects) {
				try {
					boolean isConnected = mqttConnection.connect();
					if(!isConnected){

						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								AlertDialog.Builder tryDialog = new AlertDialog.Builder(getActivity());
								tryDialog.setTitle("JPOSClient is not Connected!!!!");
								final EditText input = new EditText(getActivity());
								// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
								input.setInputType(InputType.TYPE_CLASS_TEXT);
								input.setText(mqttConnection.url);
								tryDialog.setView(input);

								tryDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String server = input.getText().toString();
										if(!server.trim().equals("")){
											mqttConnection.url = server;

										}
										try {
											connectMQTT(mqttConnection);


										}
										catch (Exception e){

										}

									}
								});
								tryDialog.show();
							}
						});

					}
				}
				catch (Exception e){


					e.printStackTrace();
				}
				return null;
			}
		};
		asyncTask.execute();

	}
	/**
	 * Initiate this UI.
	 */

	public void showBilltoMerchandiser(final ArrayList<Product> arrayList, double total){
		Log.d("ShowBIll","Test Pass");

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					final Dialog dialog1 = new Dialog(getActivity());
					dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog1.setContentView(R.layout.notification);
					TextView referenceNo = (TextView) dialog1.findViewById(R.id.refenrenceNo);
					ListView listView = (ListView)dialog1.findViewById(R.id.list_bill_item);
					final ListViewAdapter3 adapter = new ListViewAdapter3(getActivity(), arrayList);
					listView.setAdapter(adapter);

					int reference = (int)Math.abs(Math.random()%100000);
					referenceNo.setText(reference+"");
					Button ok = (Button) dialog1.findViewById(R.id.ok);


					ok.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							dialog1.hide();
							arrayList.clear();
						}
					});

					dialog1.show();
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
		});

	}
	private void initUI() {
	/*	currentTime = Calendar.getInstance();
		datePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int y, int m, int d) {
				currentTime.set(Calendar.YEAR, y);
				currentTime.set(Calendar.MONTH, m);
				currentTime.set(Calendar.DAY_OF_MONTH, d);
				update();
			}
		}, currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH));
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
		        R.array.period, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinner.setAdapter(adapter);
		spinner.setSelection(0);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				update();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
			
		});
		
		currentBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				datePicker.show();
			}
		});
		
		
		
		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addDate(-1);
			}
		});
		
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addDate(1);
			}
		});
		
		saleLedgerListView.setOnItemClickListener(new OnItemClickListener() {
		      public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
		    	  String id = saleList.get(position).get("id").toString();
		    	 *//* Intent newActivity = new Intent(getActivity().getBaseContext(), SaleDetailActivity.class);
		          newActivity.putExtra("id", id);
		          startActivity(newActivity);  
		      }     
		});*//*
		
*/	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		context = getActivity();

		((MainActivity)context).fragPrice = this;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		/*thread =new Thread(new Runnable() {

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
						ReportFragment.ServerAsyncTask serverAsyncTask = new ReportFragment.ServerAsyncTask();
						//Start the AsyncTask execution
						//Accepted client socket object will pass as the parameter
						serverAsyncTask.execute(new Socket[] {socClient});
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});*/
		//thread.start();
	}

	/**
	 * Show list.
	 * @param list
	 */
	private void showList(List<Sale> list) {

		saleList = new ArrayList<Map<String, String>>();
		for (Sale sale : list) {
			saleList.add(sale.toMap());
		}
		
		SimpleAdapter sAdap = new SimpleAdapter(getActivity().getBaseContext() , saleList,
				R.layout.listview_report, new String[] { "id", "startTime", "total"},
				new int[] { R.id.sid, R.id.startTime , R.id.total});
		saleLedgerListView.setAdapter(sAdap);
	}

	@Override
	public void update() {
		int period = spinner.getSelectedItemPosition();
		List<Sale> list = null;
		Calendar cTime = (Calendar) currentTime.clone();
		Calendar eTime = (Calendar) currentTime.clone();
		
		if(period == DAILY){
			currentBox.setText(" [" + DateTimeStrategy.getSQLDateFormat(currentTime) +  "] ");
			currentBox.setTextSize(16);
		} else if (period == WEEKLY){
			while(cTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
				cTime.add(Calendar.DATE, -1);
			}
			
			String toShow = " [" + DateTimeStrategy.getSQLDateFormat(cTime) +  "] ~ [";
			eTime = (Calendar) cTime.clone();
			eTime.add(Calendar.DATE, 7);
			toShow += DateTimeStrategy.getSQLDateFormat(eTime) +  "] ";
			currentBox.setTextSize(16);
			currentBox.setText(toShow);
		} else if (period == MONTHLY){
			cTime.set(Calendar.DATE, 1);
			eTime = (Calendar) cTime.clone();
			eTime.add(Calendar.MONTH, 1);
			eTime.add(Calendar.DATE, -1);
			currentBox.setTextSize(18);
			currentBox.setText(" [" + currentTime.get(Calendar.YEAR) + "-" + (currentTime.get(Calendar.MONTH)+1) + "] ");
		} else if (period == YEARLY){
			cTime.set(Calendar.DATE, 1);
			cTime.set(Calendar.MONTH, 0);
			eTime = (Calendar) cTime.clone();
			eTime.add(Calendar.YEAR, 1);
			eTime.add(Calendar.DATE, -1);
			currentBox.setTextSize(20);
			currentBox.setText(" [" + currentTime.get(Calendar.YEAR) +  "] ");
		}
		currentTime = cTime;
		list = saleLedger.getAllSaleDuring(cTime, eTime);
		double total = 0;
		for (Sale sale : list)
			total += sale.getTotal();
		
		totalBox.setText(total + "");
		showList(list);
	}

	@Override
	public void onResume() {
		super.onResume();
		serviceIntentr = new Intent(getActivity().getApplicationContext(),
				SocketService.class);
		getActivity().startService(serviceIntentr);

		getActivity().registerReceiver(receiverr, new IntentFilter(
				SocketService.BROADCAST_ACTION));
		/*if(!thread.isAlive()) {
			thread.start();
		}*/
		//	update();
	}

	@Override
	public void onStop() {
		super.onStop();

		/*if(thread!= null) {
			thread.stop();

		}*/
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().stopService(serviceIntentr);
		getActivity().unregisterReceiver(receiverr);
/*		if(thread!= null) {
			thread.stop();

		}*/
	}
	
	/**
	 * Add date.
	 * @param increment
	 */
	private void addDate(int increment) {
		int period = spinner.getSelectedItemPosition();
		if (period == DAILY){
			currentTime.add(Calendar.DATE, 1 * increment);
		} else if (period == WEEKLY){
			currentTime.add(Calendar.DATE, 7 * increment);
		} else if (period == MONTHLY){
			currentTime.add(Calendar.MONTH, 1 * increment);
		} else if (period == YEARLY){
			currentTime.add(Calendar.YEAR, 1 * increment);
		}
		update();
	}

	@Override
	public void getPrice(String price) {
		Amount.setText(price);
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
				Log.e("Result", result);
				//Close the client connection
				mySocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {

			try {
				String track = result;// "no, %B4216890200522445^KARUNASINGHE/NALIN D^1710221190460000000000394000000?";
				track = track.replaceAll("no", "");
				track = track.replaceAll("\\,","");

				String [] details = track.split("\\^");
				details[0] = details[0].replace("%B","");
				details[1] = details[1].replace("/"," ");
				details[2] = details[2].substring(0,4);
				String cardNu = details[0].substring(0,4)+ " **** **** ****";
				String eYear = details[2].substring(0,2);
				String eMonth = details[2].substring(2);
				PAN = details[0];

				cardHolder.setText(details[1]);

				expiryDate.setText(eMonth+"/"+eYear);
				cardNo.setText(cardNu);
			}
			catch (Exception e){

			}



			//After finishing the execution of background task data will be write the text view
			//tvClientMsg.setText(s);
			//Toast.makeText(getActivity().getBaseContext(), s, Toast.LENGTH_LONG).show();
			int i=0;
			/*if(s.substring(0,2).equals("no"))
				cardNo.setText(s.substring(5,21));*/
			//else
				//setListViewSale(s);
		}
	}
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
				Toast.makeText(getActivity().getBaseContext(), message,
						Toast.LENGTH_SHORT).show();

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
						Log.d("Blue",readMessage);
						if(readMessage.charAt(0)!='9') {
						/*	byte[] key = new byte[32]; // 32 for 256 bit key or 16 for 128 bit
							byte[] iv = new byte[8]; // 64 bit IV required by ChaCha20
							int [] ikey={1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216};
							int [] iiv={101,102,103,104,105,106,107,108};
                            *//*key=j[1].getBytes();
                            iv=j[0].getBytes();*//*
							//= is.toByteArray();
							for(int i=0;i<32;i++)
								key[i]=(byte) ikey[i];
							for(int i=0;i<8;i++)
								iv[i]=(byte) iiv[i];
							try (InputStream isDec =new ByteArrayInputStream(readBuf) ;
								 ByteArrayOutputStream osDec = new ByteArrayOutputStream())
							{
								decChaCha(isDec, osDec, key, iv);

								byte[] decoded = osDec.toByteArray();

								readMessage = new String(decoded, StandardCharsets.UTF_8);
								Log.d("Chacha",readMessage);
								//System.out.println(test+" "+actual);
								//Assert.assertEquals(test, actual);
							}*/
							StringTokenizer st = new StringTokenizer(readMessage, "^");
							String res[] = new String[10];
							int i = 0;
							while (st.hasMoreTokens()) {
								res[i] = st.nextToken();
								i++;
							}
							/*st = new StringTokenizer(res[1], "^");
							String res2[] = new String[10];
							i = 0;
							while (st.hasMoreTokens()) {
								res2[i] = st.nextToken();
								i++;
							}*/
							// "no, %B4216890200522445^KARUNASINGHE/NALIN D^1710221190460000000000394000000?";
							cardHolder.setText(res[1].substring(0));
							Log.d("expire", res[2]);

							expiryDate.setText(res[2].substring(0, 2) + "/" + res[2].substring(2, 4));
							cardNo.setText(res[0].substring(6, 8) + "********");
							PAN = res[0].substring(2);
							CVV.setText(res[3]);
							Amount.setText(res[4]);
							Log.d("expire", res[0]);
                   /* mAdapter.notifyDataSetChanged();
                    messageList.add(new androidRecyclerView.Message(counter++, readMessage, mConnectedDeviceName));*/
						}else{
							StringTokenizer st = new StringTokenizer(readMessage, " ");
							String res[] = new String[100];
							int i = 0;
							while (st.hasMoreTokens()) {
								res[i] = st.nextToken();
								i++;
							}
							for(int j=1;j<i;j++) {
								st = new StringTokenizer(res[j], ",");
								String res2[] = new String[5];
								int k = 0;
								while (st.hasMoreTokens()) {
									res2[k] = st.nextToken();
									k++;
								}
								Map<String, String> map = new HashMap<String, String>();
								map.put("name", res2[0]);
								map.put("barcode", res2[1]);
								map.put("unitPrice", res2[3]);
								SalesDetails salesDetails=new SalesDetails();
								salesDetails.addList(map);

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
					try {
						Toast.makeText(getActivity().getBaseContext(), msg.getData().getString(TOAST),
								Toast.LENGTH_SHORT).show();
					}catch(Exception  e){
						e.printStackTrace();
					}
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
	public void doChaCha(boolean encrypt, InputStream is, OutputStream os,
						 byte[] key, byte[] iv) throws IOException {
		CipherParameters cp = new KeyParameter(key);
		ParametersWithIV params = new ParametersWithIV(cp, iv);
		StreamCipher engine = new ChaChaEngine();
		engine.init(encrypt, params);


		byte in[] = new byte[8192];
		byte out[] = new byte[8192];
		int len = 0;
		while(-1 != (len = is.read(in))) {
			len = engine.processBytes(in, 0 , len, out, 0);
			os.write(out, 0, len);
		}
	}

	public void encChaCha(InputStream is, OutputStream os, byte[] key,
						  byte[] iv) throws IOException {
		doChaCha(true, is, os, key, iv);
	}

	public void decChaCha(InputStream is, OutputStream os, byte[] key,
						  byte[] iv) throws IOException {
		doChaCha(false, is, os, key, iv);
	}
	public void chachaString() throws IOException, NoSuchAlgorithmException
	{
		String test = "Hello, World!";

		try (InputStream isEnc = new ByteArrayInputStream(test.getBytes(StandardCharsets.UTF_8));
			 ByteArrayOutputStream osEnc = new ByteArrayOutputStream())
		{
			//  SecureRandom sr = SecureRandom.getInstanceStrong();
			String a[]={"7B4117E8", "C9B97794E1809E07BB271BF07C861003" };
			byte[] key = new byte[32]; // 32 for 256 bit key or 16 for 128 bit
			byte[] iv = new byte[8]; // 64 bit IV required by ChaCha20
			key=a[1].getBytes();
			iv=a[0].getBytes();
			System.out.println(key+" "+iv);
			//   sr.nextBytes(key);
			//   sr.nextBytes(iv);
			System.out.println(key.toString()+" "+iv.toString());

			encChaCha(isEnc, osEnc, key, iv);

			byte[] encoded = osEnc.toByteArray();
			System.out.println(osEnc);

			try (InputStream isDec = new ByteArrayInputStream(encoded);
				 ByteArrayOutputStream osDec = new ByteArrayOutputStream())
			{
				decChaCha(isDec, osDec, key, iv);

				byte[] decoded = osDec.toByteArray();

				String actual = new String(decoded, StandardCharsets.UTF_8);
				System.out.println(test+" "+actual);
				//Assert.assertEquals(test, actual);
			}
		}
	}
}
