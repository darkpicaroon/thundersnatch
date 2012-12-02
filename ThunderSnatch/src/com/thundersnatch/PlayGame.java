package com.thundersnatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;



/* To enable the maps functionality, you must set the keystore location in your own IDE
 * to the file "debug.keystore" in this package.
 * 
 * To do this, open up the "Preferences..." menu; expand "Android"; click on the "Build" tab; 
 * insert the filepath into the "custom debug keystore:" text box; click "Apply"
 */

public class PlayGame extends MapActivity {

	public final int MAX_NUM_PLAYERS = 20;
	public Player[] players = new Player[MAX_NUM_PLAYERS];
	public Player[] bases = new Player[2];
	private int numPlayers = 0;

	private int userID;
	private int userGameID;
	private int gameID;
	private int teamID;
	private String teamColor;
	double distanceToOwnFlag;
	double distanceToOppFlag;
	Time startTime;

	Player user;

	private String getPlayerPositionsURL = "http://www.rkaneda.com/GetPlayerPositions.php";
	private String leaveGameURL = "http://www.rkaneda.com/LeaveGame.php";

	public MapView map;
	GeoPoint geoP;
	MapController mapC;

	TextView clockText;

	TextView redScoreView;
	int redScore;
	TextView blueScoreView;
	int blueScore;
	
	CountDownTimer timer;
	CountDownTimer updateTimer;

	private SharedPreferences settings;
	private SharedPreferences.Editor editor;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		settings = getApplicationContext().getSharedPreferences(
				"com.thundersnatch", Context.MODE_PRIVATE);
		editor = settings.edit();

		userGameID = settings.getInt("UserGameID", 0);
		gameID = settings.getInt("GameID", 0);
		teamID = settings.getInt("TeamID", 0);
		teamColor = settings.getString("TeamColor", "");
		userID = settings.getInt("userID", 0);
		
		clockText = (TextView) findViewById(R.id.countDown);
		
		String startTimeTemp = settings.getString("GameStartTime", "");
		if(startTimeTemp.equals("")){
			timer = startCountdown(900000);
			updateTimer = startUpdateCountDown(900000);
		}
		else{
			//input format: YYYY-MM-DD HH:MM:SS
			String temp = startTimeTemp.split("\\s+")[1];
			
			
			//add 15 minutes to start time and subtract from now()
			//set that milisecondsRemaining to that value
			int millisecondsRemaining = getTime(temp);
			timer = startCountdown(millisecondsRemaining);
			updateTimer = startUpdateCountDown(millisecondsRemaining);
		}

		user = new Player(userID, "USER", settings.getFloat("Longitude", 0),
				settings.getFloat("Latitude", 0), false, false, teamID);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_map);


		blueScoreView = (TextView) findViewById(R.id.blueScore);
		blueScore = 0;
		redScoreView = (TextView) findViewById(R.id.redScore);
		redScore = 0;

		map = (MapView) findViewById(R.id.mapView);
		map.displayZoomControls(false);
		map.setBuiltInZoomControls(false);
		map.setSatellite(false);
		map.setTraffic(false);

		MapController mapControl = map.getController();
		mapControl.setZoom(18);

		if (!(user.xPosition == 0) || !(user.yPosition == 0)) {
			putLocationsOnMap(players, map);
			mapControl.animateTo(new GeoPoint((int) (user.xPosition * 1e6),
					(int) (user.yPosition * 1e6)));
		}

		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				user.xPosition = (float) location.getLatitude();
				user.yPosition = (float) location.getLongitude();
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);

	}
	
	public int getTime(String s) {
		//input: String syntax YYYY-MM-DD HH:MM:SS
		//output: Difference in milliseconds between Start time and phone's current time
		s = s.split("\\s+")[1];
		String[] temp = s.split("\\:");
		Calendar c  = Calendar.getInstance();
		int currentTime = c.get(Calendar.MILLISECOND);
		int startTime = Integer.parseInt(temp[0]) + Integer.parseInt(temp[1]) + Integer.parseInt(temp[2]);
		
		return startTime - currentTime;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_play_game, menu);
		return true;
	}

	protected boolean isRouteDisplayed() {
		return false;
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		updateTimer.cancel();
		timer.cancel();
	}

	@Override
	protected void onPause() {
		super.onPause();
		updateTimer.cancel();
		timer.cancel();
		
	}

	protected void updatePositions(float xPosition, float yPosition,
			boolean hasOwnFlag, boolean hasOppFlag, Player[] players) {

		// Create a HTTPClient as the form container
		HttpClient httpclient = new DefaultHttpClient();

		// Create an array list for the input data to be sent
		ArrayList<NameValuePair> nameValuePairs;

		// Create a HTTP Response and HTTP Entity
		HttpResponse response;
		HttpEntity entity;

		// run http methods
		try {
			// Use HTTP POST method
			URI uri = new URI(getPlayerPositionsURL);
			HttpPost httppost = new HttpPost(uri);

			// place credentials in the array list
			nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs
					.add(new BasicNameValuePair("userId", "" + userGameID));
			nameValuePairs.add(new BasicNameValuePair("gameId", "" + gameID));
			nameValuePairs.add(new BasicNameValuePair("teamId", "" + teamID));
			nameValuePairs.add(new BasicNameValuePair("xPos", "" + xPosition));
			nameValuePairs.add(new BasicNameValuePair("yPos", "" + yPosition));
			nameValuePairs.add(new BasicNameValuePair("takeOppFlag", "" + 0));
			nameValuePairs.add(new BasicNameValuePair("takeOwnFlag", "" + 0));
			

			// Add array list to http post
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// assign executed form container to response
			response = httpclient.execute(httppost);

			// check status code, need to check status code 200
			if (response.getStatusLine().getStatusCode() == 200) {

				// assign response entity to http entity
				entity = response.getEntity();

				// check if entity is not null
				if (entity != null) {
					// Create new input stream with received data assigned
					InputStream instream = entity.getContent();

					// Create new JSON Object. assign converted data as
					// parameter.
					JSONObject jsonResponse = new JSONObject(
							convertStreamToString(instream));
					
					distanceToOppFlag = jsonResponse.getDouble("distanceToOpponentFlag") * 364320;
					distanceToOwnFlag = jsonResponse.getDouble("distanceToOwnFlag") * 364320;

					JSONObject team0 = jsonResponse.getJSONObject("Team0");
					JSONObject team1 = jsonResponse.getJSONObject("Team1");
					
					if(team0.getInt("TeamIndex") == 0){
						blueScore = team0.getInt("Points");
						blueScoreView.setText("" + blueScore);
					}else{
						redScore = team0.getInt("Points");
						redScoreView.setText("" + redScore);
					}
					
					if(team1.getInt("TeamIndex") == 0){
						blueScore = team1.getInt("Points");
						blueScoreView.setText("" + blueScore);
					}else{
						redScore = team1.getInt("Points");
						redScoreView.setText("" + redScore);
					}

					if (bases[1] == null) {

						double x = team0.getDouble("FlagStartXPos");
						double y = team0.getDouble("FlagStartYPos");
						int teamId = team0.getInt("TeamID");
						if(team0.getInt("TeamIndex") == 0){
							bases[0] = new Player(
								(float) (settings.getFloat("Longitude", 0) + 0.0005),
								(float) (settings.getFloat("Latitude", 0) + 0.0005),
								true, true, teamId, 0);
//						 blueFlag = new Player((float)x, (float)y, true,
//						 true, teamId, 0);
						}else{
							bases[1] = new Player(
									(float) (settings.getFloat("Longitude", 0) + 0.0005),
									(float) (settings.getFloat("Latitude", 0) + 0.0005),
									true, true, teamId, 1);
//							 blueFlag = new Player((float)x, (float)y, true,
//							 true, teamId, 0);
						}

						x = team1.getDouble("FlagStartXPos");
						y = team1.getDouble("FlagStartYPos");
						teamId = team1.getInt("TeamID");
						if(team1.getInt("TeamIndex") == 0){
							bases[0] = new Player(
								(float) (settings.getFloat("Longitude", 0) + 0.0005),
								(float) (settings.getFloat("Latitude", 0) + 0.0005),
								true, true, teamId, 0);
//						 blueFlag = new Player((float)x, (float)y, true,
//						 true, teamId, 0);
						}else{
							bases[1] = new Player(
									(float) (settings.getFloat("Longitude", 0) + 0.0005),
									(float) (settings.getFloat("Latitude", 0) + 0.0005),
									true, true, teamId, 1);
//							 redFlag = new Player((float)x, (float)y, true,
//							 true, teamId, 0);
						}
					}

					numPlayers = jsonResponse.getInt("numPlayers");
					// System.out.println(numPlayers);
					JSONObject playerList = jsonResponse
							.getJSONObject("PlayerArray");

					for (int i = 0; i < playerList.length(); i++) {
						JSONObject player = playerList.getJSONObject("player"
								+ i);
						convertJsonResponseToPlayer(player, i);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// end update method

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}// end stream to string


	public void convertJsonResponseToPlayer(JSONObject json, int i) {
		try {
			int index = -1;
			int userID = json.getInt("UserID");
			for (int j = 0; j < numPlayers; j++) {
				if (players[j] != null) {
					if (players[j].userGameID == userID) {
						index = j;
						break;
					}
				} else {
					index = index * j;
					break;
				}
			}
			if (index > 0) {

				// player already exists in array
				players[index].xPosition = (float) json.getDouble("XPos");
				players[index].yPosition = (float) json.getDouble("YPos");
				int opp = json.getInt("hasOpponentFlag");

				if (opp == 0)
					players[index].hasOpponentFlag = false;
				else
					players[index].hasOpponentFlag = true;

				int own = json.getInt("hasOwnFlag");

				if (own == 0)
					players[index].hasOwnFlag = false;
				else
					players[index].hasOwnFlag = true;

			} else {

				// player does not exist in array; need to create new player
				index = index * -1;

				int opp = json.getInt("hasOpponentFlag");
				int own = json.getInt("hasOwnFlag");

				boolean hasOpponentFlag;
				if (opp == 0)
					hasOpponentFlag = false;
				else
					hasOpponentFlag = true;

				boolean hasOwnFlag;
				if (own == 0)
					hasOwnFlag = false;
				else
					hasOwnFlag = true;

				players[index] = new Player(json.getInt("UserID"),
						json.getString("UserName"),
						(float) json.getDouble("XPos"),
						(float) json.getDouble("YPos"), hasOpponentFlag,
						hasOwnFlag, json.getInt("TeamID"));

			}
		} catch (Exception e) {
			System.out.println("Error: " + e);
		}
	}// end json response conversion

	public void putLocationsOnMap(Player[] players, MapView map) {

		List<Overlay> mapOverlays = null;
		Drawable drawable = null;
		GeoPoint point;
		MapItemizedOverlay itemizedoverlay;
		OverlayItem overlayitem;

		int count = 0;
		if (players.length == 20) {
			count = numPlayers;
			map.getOverlays().clear();
			mapOverlays = map.getOverlays();

		} else if (players.length == 2) {
			count = 2;
			mapOverlays = map.getOverlays();
		} else
			System.out.println("Error");


		if (players != null) {
			for (int i = 0; i < count; i++) {

				if(!players[i].isBase){
					if (user.userGameID == players[i].userGameID) {
					} else if ((teamColor.equals("red") && teamID == players[i].teamID)
							|| (teamColor.equals("blue") && teamID != players[i].teamID)) {
						if (players[i].hasOwnFlag == true)
							drawable = getResources().getDrawable(
									R.drawable.red_flag);
						else if (players[i].hasOpponentFlag == true)
							drawable = getResources().getDrawable(
									R.drawable.blue_flag);
						else
							drawable = getResources().getDrawable(
									R.drawable.red_dot);
					} else if ((teamColor.equals("blue") && teamID == players[i].teamID)
							|| (teamColor.equals("red") && teamID != players[i].teamID)) {
						if (players[i].hasOwnFlag == true)
							drawable = getResources().getDrawable(
									R.drawable.blue_flag);
						else if (players[i].hasOpponentFlag == true)
							drawable = getResources().getDrawable(
									R.drawable.red_flag);
						else
							drawable = getResources().getDrawable(
									R.drawable.blue_dot);
					}
				}
				else{
					if(players[i].teamIndex == 0){
						drawable = getResources().getDrawable(R.drawable.blue_castle);
					}
					drawable = getResources().getDrawable(R.drawable.castle);
				}
				if(drawable != null){
					itemizedoverlay = new MapItemizedOverlay(drawable, this);
	
					point = new GeoPoint((int) (players[i].xPosition * 1E6),
							(int) (players[i].yPosition * 1E6));
					overlayitem = new OverlayItem(point, players[i].userName, ""
							+ players[i].teamID);
					itemizedoverlay.addOverlay(overlayitem);
					mapOverlays.add(itemizedoverlay);
				}
			}
		}
		point = new GeoPoint((int) (user.xPosition * 1E6),
				(int) (user.yPosition * 1E6));
		overlayitem = new OverlayItem(point, user.userName, ""
				+ user.teamID);
		itemizedoverlay = new MapItemizedOverlay(getResources().getDrawable(R.drawable.black_dot), this);
		itemizedoverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedoverlay);
		map.invalidate();
	}

	public CountDownTimer startCountdown(int milliseconds) {
		CountDownTimer timer = new CountDownTimer(milliseconds, 1000) {
			public void onTick(long millisecondsUntilFinished) {
				int seconds = (int) ((millisecondsUntilFinished / 1000) % 60);
				int minutes = (int) ((millisecondsUntilFinished - (seconds * 1000)) / 60000);
				if (seconds >= 10)
					clockText.setText(minutes + ":" + seconds);
				else
					clockText.setText(minutes + ":0" + seconds);
			}

			public void onFinish() {

			}
		}.start();
		return timer;
	}

	// this will basically create a new thread that will handle updating
	// everything
	public CountDownTimer startUpdateCountDown(int milliseconds) {
		CountDownTimer timer = new CountDownTimer(milliseconds, 1000) {
			public void onTick(long millisecondsUntilFinished) {
				updatePositions(user.xPosition, user.yPosition,
						user.hasOwnFlag, user.hasOpponentFlag, players);
				putLocationsOnMap(players, map);
				putLocationsOnMap(bases, map);
				compareLocations();
			}

			public void onFinish() {

			}
		}.start();
		return timer;
	}

	@Override
	protected void onStop() {
		super.onStop();
		updateTimer.cancel();
		timer.cancel();
		finish();
		leaveGame();
	}

	private boolean leaveGame() {

		// Create a HTTPClient as the form container
		HttpClient httpclient = new DefaultHttpClient();

		// Create an array list for the input data to be sent
		ArrayList<NameValuePair> nameValuePairs;

		// Create a HTTP Response and HTTP Entity
		HttpResponse response;
		HttpEntity entity;

		// run http methods
		try {

			// Use HTTP POST method
			URI uri = new URI(leaveGameURL);
			HttpPost httppost = new HttpPost(uri);// this is where the address
													// to the php file goes

			// place credentials in the array list
			nameValuePairs = new ArrayList<NameValuePair>();
			userID = settings.getInt("userID", 0);
			nameValuePairs.add(new BasicNameValuePair("userId", "" + userID));
			gameID = settings.getInt("GameID", 0);
			nameValuePairs.add(new BasicNameValuePair("gameId", "" + gameID));

			// Add array list to http post
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// assign executed form container to response
			response = httpclient.execute(httppost);

			// check status code, need to check status code 200
			if (response.getStatusLine().getStatusCode() == 200) {

				// assign response entity to http entity
				entity = response.getEntity();

				// check if entity is not null
				if (entity != null) {
					// Create new input stream with received data assigned
					InputStream instream = entity.getContent();

					// Create new JSON Object. assign converted data as
					// parameter.
					JSONObject jsonResponse = new JSONObject(
							convertStreamToString(instream));

					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void compareLocations() {
		System.out.println("Distance: " + distanceToOppFlag);
		//refresh bases flag
		bases[0].hasOwnFlag = true;
		bases[1].hasOwnFlag = true;
		for(int i = 0; i < numPlayers; i++){
			if(players[i].hasOpponentFlag){
				if(players[i].teamID != bases[0].teamID) bases[0].hasOwnFlag = false;
				else bases[1].hasOwnFlag = false;
			}
			if(players[i].hasOwnFlag){
				if(players[i].teamID == bases[0].teamID) bases[0].hasOwnFlag = false;
				else bases[1].hasOwnFlag = false;
			}
		}
		//compare to bases
		for(int i = 0; i < 2; i++){
			//from other base
			if(bases[i].hasOwnFlag && bases[i].teamID != user.teamID){
				if(distanceToOppFlag < 100) takeFlag();
			}
			if(user.hasOpponentFlag && user.teamID == bases[i].teamID){
				//score
			}
		}
		//compare to players
		for(int i = 0; i < numPlayers; i++){
			//take opp flag
			if(players[i].hasOpponentFlag && players[i].teamID != user.teamID){
				if(distanceToOppFlag < 100) takeFlag();
			}
		}
	}
	
	private void takeFlag(){
		AlertDialog steal = new AlertDialog.Builder(PlayGame.this).create();
		
		steal.requestWindowFeature(Window.FEATURE_NO_TITLE);
		WindowManager.LayoutParams orientSteal = steal.getWindow().getAttributes();
		orientSteal.gravity = Gravity.BOTTOM | Gravity.CENTER;
		orientSteal.x = 0;
		orientSteal.y = 0;
		steal.getWindow().setAttributes(orientSteal);
		    			
		steal.setIcon(0);
		    			
		steal.setTitle("Flag in proximity!");
		steal.setButton("Steal Flag", new DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, 
				int which){
				takeFlagServerCall();
			}

		});
			
		steal.setButton2("Cancel", new DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, 
				int which){
			}

		});
		steal.show();
	}
	
	private void takeFlagServerCall(){

			// Create a HTTPClient as the form container
			HttpClient httpclient = new DefaultHttpClient();

			// Create an array list for the input data to be sent
			ArrayList<NameValuePair> nameValuePairs;

			// Create a HTTP Response and HTTP Entity
			HttpResponse response;
			HttpEntity entity;

			// run http methods
			try {
				// Use HTTP POST method
				URI uri = new URI(getPlayerPositionsURL);
				HttpPost httppost = new HttpPost(uri);

				// place credentials in the array list
				nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs
						.add(new BasicNameValuePair("userId", "" + userGameID));
				nameValuePairs.add(new BasicNameValuePair("gameId", "" + gameID));
				nameValuePairs.add(new BasicNameValuePair("teamId", "" + teamID));
				nameValuePairs.add(new BasicNameValuePair("xPos", "" + user.xPosition));
				nameValuePairs.add(new BasicNameValuePair("yPos", "" + user.yPosition));
				nameValuePairs.add(new BasicNameValuePair("takeOppFlag", "" + 1));
				nameValuePairs.add(new BasicNameValuePair("takeOwnFlag", "" + 0));
				

				// Add array list to http post
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// assign executed form container to response
				response = httpclient.execute(httppost);

				// check status code, need to check status code 200
				if (response.getStatusLine().getStatusCode() == 200) {

					// assign response entity to http entity
					entity = response.getEntity();

					// check if entity is not null
					if (entity != null) {
						// Create new input stream with received data assigned
						InputStream instream = entity.getContent();

						// Create new JSON Object. assign converted data as
						// parameter.
						JSONObject jsonResponse = new JSONObject(
								convertStreamToString(instream));
						
						distanceToOppFlag = jsonResponse.getDouble("distanceToOpponentFlag") * 364320;
						distanceToOwnFlag = jsonResponse.getDouble("distanceToOwnFlag") * 364320;

						numPlayers = jsonResponse.getInt("numPlayers");
						JSONObject playerList = jsonResponse
								.getJSONObject("PlayerArray");

						for (int i = 0; i < playerList.length(); i++) {
							JSONObject player = playerList.getJSONObject("player"
									+ i);
							convertJsonResponseToPlayer(player, i);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}
