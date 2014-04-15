package com.adamharley.supertram;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class ResultsActivity extends Activity {
	
	public static String padLeft(String s, int n, String p) {
	    return String.format("%1$" + n + "s", s).replace(' ', p.charAt(0));
	}
	
	private Integer parseStationName(String station_name) {
		final String[] station_names = getResources().getStringArray(R.array.stations_names);
		final int[] station_ids = getResources().getIntArray(R.array.stations_ids);
		
		Integer i = Arrays.asList(station_names).indexOf(station_name);
		
		return i.equals(-1) ? 0 : station_ids[i];
	}
	
	private HashMap<String, Boolean> getRoutes(HashMap<String, Boolean> startStationRoutes, HashMap<String, Boolean> endStationRoutes) {
		HashMap<String, Boolean> routes = new HashMap<String, Boolean>(3);
		
		final String[] routeNames = {"yellow", "blue", "purple"};
		
		for (String routeName : routeNames) {
			routes.put(routeName, startStationRoutes.get(routeName) && endStationRoutes.get(routeName));
		}
		
		return routes;
	}
	
	private HashMap<String, Boolean> getStationRoutes(Integer station_id) {
		HashMap<String, Boolean> routes = new HashMap<String, Boolean>(3);
/*
		1-24 = Yellow route
		4-15, 26, 27-45, 48 = Blue route
		13-25, 27-34, 46-48 = Purple route
*/
		routes.put("yellow",
					station_id >= 1 && station_id <= 24
				);
		
		routes.put("blue",
					(station_id >= 4 && station_id <= 15) ||
					(station_id >= 26 && station_id <= 45) ||
					station_id.equals(48)
				);
		
		routes.put("purple",
					(station_id >= 13 && station_id <= 25) ||
					(station_id >= 27 && station_id <= 34) ||
					(station_id >= 46 && station_id <= 48)
				);
		
		return routes;
	}
	
	private HashMap<String, String> getJourney(HashMap<String, String> query)
			throws NullPointerException, IOException {
		
		String url = getResources().getString(R.string.page_url);
		Document doc = Jsoup.connect(url)
				  .data(query)
				  .userAgent("Mozilla")
				  .timeout(3000)
				  .post();
		
		@SuppressWarnings("unused")
		String html = doc.html();
		
		HashMap<String, String> journey = new HashMap<String, String>(6);
		
		Element start = doc.getElementById("lblStart");
		journey.put("start", start.text());
		
		Element startTime = doc.getElementById("lblStartTime");
		journey.put("startTime", startTime.text());
		
		Element end = doc.getElementById("lblEnd");
		journey.put("end", end.text());
		
		Element endTime = doc.getElementById("lblEndTime");
		journey.put("endTime", endTime.text());
		
		Element change = doc.getElementById("lblChange");
		journey.put("change", change.text());
		
		Element fare = doc.getElementById("lblFare");
		journey.put("fare", fare.text());
		
		Element viewstate = doc.getElementById("__VIEWSTATE");
		journey.put("viewstate", viewstate.val());

		Element eventvalidation = doc.getElementById("__EVENTVALIDATION");
		journey.put("eventvalidation", eventvalidation.val());
		
		return journey;
	}

	private void displayJourney(HashMap<String, String> journey) {
		TextView startTV = (TextView) findViewById(R.id.startTV);
		TextView startTimeTV = (TextView) findViewById(R.id.startTimeTV);
		TextView endTV = (TextView) findViewById(R.id.endTV);
		TextView endTimeTV = (TextView) findViewById(R.id.endTimeTV);
		TextView changeTV = (TextView) findViewById(R.id.changeTV);
		TextView fareTV = (TextView) findViewById(R.id.fareTV);
		
		boolean change = ! journey.get("change").equals("-");
		
		startTV.setText(journey.get("start"));
		startTimeTV.setText(journey.get("startTime"));
		endTV.setText(journey.get("end"));
		endTimeTV.setText(journey.get("endTime"));
		fareTV.setText(journey.get("fare"));
		
		HashMap<String, Boolean> startStationRoutes = getStationRoutes(parseStationName(journey.get("start")));
		HashMap<String, Boolean> endStationRoutes = getStationRoutes(parseStationName(journey.get("end")));
		
		if (change) {
			HashMap<String, Boolean> changeStationRoutes = getStationRoutes(parseStationName(journey.get("change")));
			
			HashMap<String, Boolean> startRoute = getRoutes(startStationRoutes, changeStationRoutes);
			HashMap<String, Boolean> changeRoute = getRoutes(changeStationRoutes, endStationRoutes);
		} else {
			HashMap<String, Boolean> startRoute = getRoutes(startStationRoutes, endStationRoutes);
		}
		
		/*
		 * Yellow: #FABB00
		 * Blue: #005187
		 * Purple: #88146A
		 */
		
		if (change) {
			changeTV.setText(journey.get("change"));
			changeTV.setVisibility(TextView.VISIBLE);
		} else {
			changeTV.setVisibility(TextView.GONE);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
		
		@SuppressWarnings("unchecked")
		HashMap<String, String> query = (HashMap<String, String>) getIntent().getSerializableExtra("query");
		
		// Initial search
		query.put("lnkGetResults.x", "0");
		query.put("lnkGetResults.y", "0");
/*		
		// Earlier times
		query.put("lnkEarlier.x", "0");
		query.put("lnkEarlier.y", "0");
		
		// Later times
		query.put("lnkLater.x", "0");
		query.put("lnkLater.y", "0");
*/		
		query.put("__VIEWSTATE", getString(R.string.viewstate));
		query.put("__EVENTVALIDATION", getString(R.string.eventvalidation));
		
		Integer cboHour = Integer.parseInt(query.get("cboHour"));
		Integer cboMinute = Integer.parseInt(query.get("cboMinute"));
		Integer cboReturnHour = Integer.parseInt(query.get("cboReturnHour"));
		Integer cboReturnMinute = Integer.parseInt(query.get("cboReturnMinute"));
		
		// Hours must be between 5am and 1am; 12am and 1am are 24 and 25
		if (cboHour < 5) {
			if (cboHour.equals(0)) {
				cboHour = 24;
			} else if (cboHour.equals(1)) {
				cboHour = 25;
			} else {
				// This should never happen
			}
		}
		if (cboReturnHour < 5) {
			if (cboReturnHour.equals(0)) {
				cboReturnHour = 24;
			} else if (cboReturnHour.equals(1)) {
				cboReturnHour = 25;
			} else {
				// This should never happen
			}
		}
		
		// Minutes must be in multiples of five
		Integer cboMinuteRemainder = cboMinute % 5;
		if (! cboMinuteRemainder.equals(0)) {
			cboMinute -= cboMinuteRemainder;
		}
		Integer cboReturnMinuteRemainder = cboReturnMinute % 5;
		if (! cboReturnMinuteRemainder.equals(0)) {
			cboReturnMinute -= cboReturnMinuteRemainder;
		}
		
		// Hours and minutes must be padded with zeros
		query.put("cboHour", padLeft(cboHour.toString(), 2, "0"));
		query.put("cboMinute", padLeft(cboMinute.toString(), 2, "0"));
		query.put("cboReturnHour", padLeft(cboReturnHour.toString(), 2, "0"));
		query.put("cboReturnMinute", padLeft(cboReturnMinute.toString(), 2, "0"));
		
		try {
			HashMap<String, String> journey = getJourney(query);
			displayJourney(journey);
		} catch (IOException e) { // See http://jsoup.org/apidocs/org/jsoup/Connection.html#post()
			// TODO Handle HttpStatusException status 500
			e.printStackTrace();
		} catch (NullPointerException e) { // Element not found
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.results, menu);
		return true;
	}

}
