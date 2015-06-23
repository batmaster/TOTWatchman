package com.tot.totwatchman;

import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class HistoryActivity extends Activity {
	
	private String guardName;
	
	private Spinner spinnerNames;
	private EditText editTextFromDate;
	private EditText editTextToDate;
	private Button buttonSearch;
	private ListView listView;
	
	private static final String DATE_FORMAT = "yyyy/MM/dd";
	private static final SimpleDateFormat SDF;
	static {
		SDF = new SimpleDateFormat(DATE_FORMAT);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
		guardName = getIntent().getStringExtra("guardName");
		
		spinnerNames = (Spinner) findViewById(R.id.spinnerNames);
		GetNamesTask task = new GetNamesTask();
		task.execute();
		
		spinnerNames.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String name = ((TextView) view.findViewById(R.id.textViewName)).getText().toString();
				GetListTask task = new GetListTask(name, editTextFromDate.getText().toString(), editTextToDate.getText().toString());
				task.execute();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		
		editTextFromDate = (EditText) findViewById(R.id.editTextFromDate);
		editTextFromDate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DatePickerDialog.OnDateSetListener dialogListener = new DatePickerDialog.OnDateSetListener() {

				    @Override
				    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						Calendar calendar = Calendar.getInstance();
				        calendar.set(Calendar.YEAR, year);
				        calendar.set(Calendar.MONTH, monthOfYear);
				        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				        
				        editTextFromDate.setText(SDF.format(calendar.getTime()));
				    }
				};
				
				try {
					Date d = SDF.parse(editTextFromDate.getText().toString());
					int year = d.getYear() + 1900;
					int month = d.getMonth();
					int date = d.getDate();
					DatePickerDialog dialog = new DatePickerDialog(HistoryActivity.this, dialogListener, year, month, date);
					dialog.show();
				} catch (ParseException e) {
					e.printStackTrace();
					DatePickerDialog dialog = new DatePickerDialog(HistoryActivity.this, dialogListener, 2000, 0, 1);
					dialog.show();
				}
			}
		});
		editTextFromDate.setText(SDF.format(Calendar.getInstance().getTime()));
		
		editTextToDate = (EditText) findViewById(R.id.editTextToDate);
		editTextToDate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DatePickerDialog.OnDateSetListener dialogListener = new DatePickerDialog.OnDateSetListener() {

				    @Override
				    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						Calendar calendar = Calendar.getInstance();
				        calendar.set(Calendar.YEAR, year);
				        calendar.set(Calendar.MONTH, monthOfYear);
				        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				        
				        editTextToDate.setText(SDF.format(calendar.getTime()));
				    }
				};
				
				try {
					Date d = SDF.parse(editTextFromDate.getText().toString());
					int year = d.getYear() + 1900;
					int month = d.getMonth();
					int date = d.getDate();
					DatePickerDialog dialog = new DatePickerDialog(HistoryActivity.this, dialogListener, year, month, date);
					try {
						dialog.getDatePicker().setMinDate(SDF.parse(editTextFromDate.getText().toString()).getTime());
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
					dialog.show();
				} catch (ParseException e) {
					e.printStackTrace();
					DatePickerDialog dialog = new DatePickerDialog(HistoryActivity.this, dialogListener, 2000, 0, 1);
					try {
						dialog.getDatePicker().setMinDate(SDF.parse(editTextFromDate.getText().toString()).getTime());
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
					dialog.show();
				}
			}
		});
		editTextToDate.setText(SDF.format(Calendar.getInstance().getTime()));
		
		buttonSearch = (Button) findViewById(R.id.buttonSearch);
		buttonSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String name = ((TextView) spinnerNames.getSelectedView().findViewById(R.id.textViewName)).getText().toString();
				GetListTask task = new GetListTask(name, editTextFromDate.getText().toString(), editTextToDate.getText().toString());
				task.execute();
			}
		});
		
		listView = (ListView) findViewById(R.id.listView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class GetListTask extends AsyncTask<String, Integer, String> {

		private String name;
		private String fromDate;
		private String toDate;
		
		private List<ListViewRowItem> list;
		
		private ProgressDialog loading;
		
		public GetListTask(String name) {
			this(name, "", "");
		}
		
		public GetListTask(String name, String fromDate, String toDate) {
			this.name = name;
			this.fromDate = fromDate;
			this.toDate = toDate;
			
			loading = new ProgressDialog(HistoryActivity.this);
			loading.setTitle("รายการเช็คอิน");
			loading.setMessage("กำลังโหลด...");
//			loading.setCancelable(false);
			loading.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		}
		
		@Override
		protected String doInBackground(String[] params) {
			list = new ArrayList<ListViewRowItem>();
			
			try {
				String parsed = Parser.parse(Request.getList(name, fromDate, toDate));
				JSONArray js = new JSONArray(parsed);
				for (int i = 0; i < js.length(); i++) {
					JSONObject jo = js.getJSONObject(i);
					ListViewRowItem item = new ListViewRowItem(jo.getString("id_th"), jo.getString("idguard"), jo.getString("area"), jo.getString("dates"), jo.getString("times"), jo.getString("area_name"));
					list.add(item);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ConnectTimeoutException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟนานเกินไป");
			} catch (SocketTimeoutException e) {
				loading.setMessage("รอผลตอบกลับนานเกินไป");
			} catch (HttpHostConnectException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้");
				e.printStackTrace();
			}
			
			if (list.size() == 0)
				list.add(new ListViewRowItem());
			
			return "some message";
		}

		@Override
		protected void onPreExecute() {
			loading.show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String message) {
			loading.dismiss();
			listView.setAdapter(null);
			ListViewRowAdapter adapter = new ListViewRowAdapter(HistoryActivity.this.getApplicationContext(), list);
			listView.setAdapter(adapter);
		}
		
		public AsyncTask<String, Integer, String> execute() {
			return execute("test");
		}
	}

	private class GetNamesTask extends AsyncTask<String, Integer, String> {

		private List<SpinnerRowItem> list;
		
		private ProgressDialog loading;
		
		public GetNamesTask() {
			loading = new ProgressDialog(HistoryActivity.this);
			loading.setTitle("รายชื่อพนักงาน");
			loading.setMessage("กำลังโหลด...");
//			loading.setCancelable(false);
			loading.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		}
		
		@Override
		protected String doInBackground(String[] params) {
			list = new ArrayList<SpinnerRowItem>();
			
			try {
				String parsed = Parser.parse(Request.request(Request.REQ_GET_NAMES));
				JSONArray js = new JSONArray(parsed);
				for (int i = 0; i < js.length(); i++) {
					JSONObject jo = js.getJSONObject(i);
					SpinnerRowItem item = new SpinnerRowItem(jo.getString("name"));
					list.add(item);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ConnectTimeoutException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟนานเกินไป");
			} catch (SocketTimeoutException e) {
				loading.setMessage("รอผลตอบกลับนานเกินไป");
			} catch (HttpHostConnectException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้");
				e.printStackTrace();
			}
			return "some message";
		}

		@Override
		protected void onPreExecute() {
			loading.show();
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(String message) {
			loading.dismiss();
			SpinnerRowAdapter adapter = new SpinnerRowAdapter(HistoryActivity.this.getApplicationContext(), list);
			spinnerNames.setAdapter(adapter);

			if (guardName != null) {
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).getName().equals(guardName))
						spinnerNames.setSelection(i);
				}
			}
		}
		
		public AsyncTask<String, Integer, String> execute() {
			return execute("test");
		}
	}
}
