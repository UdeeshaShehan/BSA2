package com.example.usid.mpos.UI;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.usid.mpos.domain.sales.Sale;
import com.example.usid.mpos.domain.sales.SaleLedger;
import com.example.usid.mpos.technicalService.Connection;
import com.example.usid.mpos.technicalService.NoDaoSetException;
import com.example.usid.mpos.technicalService.PriceCommunicator;
import com.example.usid.mpos.technicalService.SocketService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	BroadcastReceiver receiverr;
	Intent serviceIntentr;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		try {
			saleLedger = SaleLedger.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
		
		View view = inflater.inflate(R.layout.layout_report, container, false);
		processPayment = (Button) view.findViewById(R.id.processPayment);
		cardHolder = (EditText) view.findViewById(R.id.card_holder_name);
		CVV = (EditText) view.findViewById(R.id.CVV_num);
		expiryDate = (EditText) view.findViewById(R.id.expiry_date);
		cardNo = (EditText) view.findViewById(R.id.Card_number);
		Amount = (EditText) view.findViewById(R.id.amount_id);

		processPayment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String cvv = CVV.getText().toString();
				final String amount = Amount.getText().toString();

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
						String results;
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



							HashMap<String, String> data = new HashMap<>();

							data.put("name_on_card", "Mohamed Nifras");
							data.put("amount", amount);
							data.put("card_type", "visa");
							data.put("card_number", "4032 0391 0542 2911");
							data.put("expiry_month", "12");
							data.put("expiry_year", "2021");
							data.put("cvv", "123");
							data.put("orderID", "12324");
							try {
								Connection connection = Connection.getInstance();
								results = connection.post("payment_process.php", data);

								Log.e("Results", results);
							} catch (URISyntaxException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}

							return null;
						}


						@Override
						protected void onPostExecute(Object o) {
							super.onPostExecute(o);
							progress.dismiss();
							JSONObject res = null;
							try {
								res = new JSONObject(results);
								if(res.getString("status").equals("1")){
									Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
								}
								else {
									Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();

								}
							} catch (JSONException e) {
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

				String result = intent.getStringExtra("result");
				if(result.length()>=3) {
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


						cardHolder.setText(details[1]);

						expiryDate.setText(eMonth + "/" + eYear);
						cardNo.setText(cardNu);
					}
				}
			}
		};
		return view;
	}

	/**
	 * Initiate this UI.
	 */
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


}
