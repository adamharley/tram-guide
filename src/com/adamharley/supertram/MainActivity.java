package com.adamharley.supertram;

import java.util.Calendar;
import java.util.HashMap;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TimePicker;

public class MainActivity extends Activity {
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		Spinner ReturnIsEndTimeSpinner = (Spinner) findViewById(R.id.ReturnIsEndTimeSpinner);
		ReturnIsEndTimeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	TimePicker ReturnTimePicker = (TimePicker) findViewById(R.id.ReturnTimePicker);
		    	
		    	// If return journey is not required, hide associated TimePicker
		        if (Integer.toString(position).equals(0)) {
		        	ReturnTimePicker.setVisibility(TimePicker.GONE);
		        } else {
		        	ReturnTimePicker.setVisibility(TimePicker.VISIBLE);
		        }
		    }

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {}
		});
		
		Calendar calendar = Calendar.getInstance();
		Integer weekday = calendar.get(Calendar.DAY_OF_WEEK);
		Spinner DayOfWeekSpinner = (Spinner) findViewById(R.id.DayOfWeekSpinner);
		
		if (weekday.equals(1)) { // Sunday
			DayOfWeekSpinner.setSelection(2);
		} else if (weekday.equals(7)) { // Saturday
			DayOfWeekSpinner.setSelection(1);
		} else { // Monday-Friday
			DayOfWeekSpinner.setSelection(0);
		}
		
		// TODO Add "Not required" to ReturnIsEndTime
		// TODO Add "Travelling from/to..." to StartSpinner/EndSpinner
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_map:
                Intent intent = new Intent(this, MapActivity.class);
                startActivity(intent);
	            return true;
	        case R.id.action_settings:
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void getResults(View v) {
        Intent intent = new Intent(this, ResultsActivity.class);
        HashMap<String, String> query = new HashMap<String, String>(9);
        int[] station_ids = getResources().getIntArray(R.array.stations_ids);

        // Stations must be converted to their respective ID values
        Spinner StartSpinner = (Spinner) findViewById(R.id.StartSpinner);
        Integer start = StartSpinner.getSelectedItemPosition();
        if (start.equals(0)) {
        	return; // Bail
        }
        query.put("cboStart", ((Integer) station_ids[start]).toString());

        Spinner EndSpinner = (Spinner) findViewById(R.id.EndSpinner);
        Integer end = EndSpinner.getSelectedItemPosition();
        if (end.equals(0)) {
        	return; // Bail
        }
        query.put("cboEnd", ((Integer) station_ids[end]).toString());

        Spinner DayOfWeekSpinner = (Spinner) findViewById(R.id.DayOfWeekSpinner);
        // This app doesn't include the placeholder
        Integer dayOfWeek =  DayOfWeekSpinner.getSelectedItemPosition() + 1;
        if (dayOfWeek.equals(0)) {
        	return; // Bail
        }
        query.put("cboDayOfWeek", dayOfWeek.toString());

        Spinner IsEndTimeSpinner = (Spinner) findViewById(R.id.IsEndTimeSpinner);
        Integer isEndTime = IsEndTimeSpinner.getSelectedItemPosition();
        query.put("cboIsEndTime", isEndTime.toString());
        
        TimePicker TimePicker = (TimePicker) findViewById(R.id.TimePicker);
        query.put("cboHour", TimePicker.getCurrentHour().toString());
        query.put("cboMinute", TimePicker.getCurrentMinute().toString());

        Spinner ReturnIsEndTimeSpinner = (Spinner) findViewById(R.id.ReturnIsEndTimeSpinner);
        // Values range from -1 to 1
        Integer ReturnIsEndTime = ReturnIsEndTimeSpinner.getSelectedItemPosition() - 1;
        query.put("cboReturnIsEndTime", ReturnIsEndTime.toString());
        
        TimePicker ReturnTimePicker = (TimePicker) findViewById(R.id.ReturnTimePicker);
        query.put("cboReturnHour", ReturnTimePicker.getCurrentHour().toString());
        query.put("cboReturnMinute", ReturnTimePicker.getCurrentMinute().toString());
        
        intent.putExtra("query", query);
        
        startActivity(intent);
	}

}
