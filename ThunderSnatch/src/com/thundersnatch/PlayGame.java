package com.thundersnatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import android.widget.Toast;

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
	
	public final int MIN_FLAG_DISTANCE = 100;
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
	double distanceToOwnBase;
	Time startTime;

	Player user;

	private String getPlayerPositionsURL = "http://www.rkaneda.com/GetPlayerPositions.php";
	private String leaveGameURL = "http://www.rkaneda.com/LeaveGame.php";
	private String scoreURL = "http://www.rkaneda.com/ScoreFlag.php";

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
	
	boolean isAlertUp;

	private SharedPreferences settings;
	private SharedPreferences.Editor editor;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_map);

		settings = getApplicationContext().getSharedPreferences(
				"com.thundersnatch", Context.MODE_PRIVATE);
		editor = settings.edit();

		userGameID = settings.getInt("UserGameID", 0);
		gameID = settings.getInt("GameID", 0);
		teamID = settings.getInt("TeamID", 0);
		teamColor = settings.getString("TeamColor", "");
		userID = settings.getInt("userID", 0);
		
		isAlertUp = false;
		
		clockText = (TextView) findViewById(R.id.countDown);
		
		String startTimeTemp = "";
		startTimeTemp = settings.getString("GameStartDate", "");
		System.out.println("Hello " + startTimeTemp);
		if(startTimeTemp.equals("")){
			timer = startCountdown(900000);
			updateTimer = startUpdateCountDown(900000);
		}
		else{
			String temp = startTimeTemp.split("\\s+")[1];
			int millisecondsRemaining = getTime(temp);
			System.out.println("milliseconds rcvd: " + millisecondsRemaining);
			timer = startCountdown(millisecondsRemaining);
			updateTimer = startUpdateCountDown(millisecondsRemaining);
		}

		user = new Player(userID, "USER", settings.getFloat("Longitude", 0),
				settings.getFloat("Latitude", 0), false, false, teamID);


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
		//input: String syntax HH:MM:SS
		//output: Difference in milliseconds between Start time and phone's current time
		System.out.println("getTime rcvd: " + s);
		String[] temp = s.split("\\:");
		System.out.println("getTime: temp array: " + temp.toString());
		String currentTimeString = DateFormat.getDateTimeInstance().format(new Date());
		String[] temp2 = currentTimeString.split("\\s+");
		currentTimeString = temp2[3];
		String am_pm = temp2[4];
		System.out.println("currentTimeString: " + currentTimeString);
		temp2 = currentTimeString.split("\\:");
		System.out.println("temp2: " + temp2.toString());
		int currentTime = ((Integer.parseInt(temp2[0]))*3600000 + (Integer.parseInt(temp2[1]))*60000 + (Integer.parseInt(temp2[2]))*1000);
		int startTime = (Integer.parseInt(temp[0])*3600000) + (Integer.parseInt(temp[1])*60000) + (Integer.parseInt(temp[2])*1000) + 900000;
		if(am_pm.equals("PM")) {
			currentTime += 43200000;
		}
		
		System.out.println("current: " + currentTime);
		System.out.println("start: " + startTime);
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
					distanceToOwnBase = jsonResponse.getDouble("distanceToBase") * 364320;

					JSONObject team0 = jsonResponse.getJSONObject("Team0");
					JSONObject team1 = jsonResponse.getJSONObject("Team1");
					
					Context context = getApplicationContext();
					int duration = Toast.LENGTH_SHORT;
					CharSequence text;
					Toast toast;
					if(team0.getInt("TeamIndex") == 0){
						int temp = team0.getInt("Points");
						if(temp > blueScore){
							text = "Blue team scores!";
							toast = Toast.makeText(context, text, duration);
							toast.show();
						}
						blueScore = temp;
						blueScoreView.setText("" + blueScore);
						
					}else{
						int temp = team0.getInt("Points");
						if(temp > redScore){
							text = "Red team scores!";
							toast = Toast.makeText(context, text, duration);
							toast.show();
						}
						redScore = temp;
						redScoreView.setText("" + redScore);
					}
					
					if(team1.getInt("TeamIndex") == 0){
						int temp = team1.getInt("Points");
						if(temp > blueScore){
							text = "Blue team scores!";
							toast = Toast.makeText(context, text, duration);
							toast.show();
						}
						blueScore = temp;
						blueScoreView.setText("" + blueScore);
					}else{
						int temp = team1.getInt("Points");
						if(temp > redScore){
							text = "Red team scores!";
							toast = Toast.makeText(context, text, duration);
							toast.show();
						}
						redScore = temp;
						redScoreView.setText("" + redScore);
					}

					
					if(redScore >= 3 || blueScore >= 3){
						endGame();
					}
					
					if (bases[1] == null) {

						double x = team0.getDouble("FlagStartXPos");
						double y = team0.getDouble("FlagStartYPos");
						int teamId = team0.getInt("TeamID");
						if(team0.getInt("TeamIndex") == 0){
//							bases[0] = new Player(
//								(float) (settings.getFloat("Longitude", 0) + 0.0002),
//								(float) (settings.getFloat("Latitude", 0) + 0.0002),
//								teamId, 0);
						 bases[0] = new Player((float)x, (float)y, teamId, 0);
						}else{
//							bases[1] = new Player(
//									(float) (settings.getFloat("Longitude", 0) - 0.0002),
//									(float) (settings.getFloat("Latitude", 0) - 0.0002),
//									teamId, 1);
							 bases[1] = new Player((float)x, (float)y, teamId, 1);
						}

						x = team1.getDouble("FlagStartXPos");
						y = team1.getDouble("FlagStartYPos");
						teamId = team1.getInt("TeamID");
						if(team1.getInt("TeamIndex") == 0){
//							bases[0] = new Player(
//								(float) (settings.getFloat("Longitude", 0) + 0.0002),
//								(float) (settings.getFloat("Latitude", 0) + 0.0002), teamId, 0);
						 bases[0] = new Player((float)x, (float)y, teamId, 0);
						}else{
//							bases[1] = new Player(
//									(float) (settings.getFloat("Longitude", 0) - 0.0002),
//									(float) (settings.getFloat("Latitude", 0) - 0.0002), teamId, 1);
						bases[1] = new Player((float)x, (float)y, teamId, 1);
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
				if(players[i].userGameID == user.userGameID){
					// update user flag information with info from server
					int opp = json.getInt("hasOpponentFlag");

					if (opp == 0)
						user.hasOpponentFlag = false;
					else
						user.hasOpponentFlag = true;

					int own = json.getInt("hasOwnFlag");

					if (own == 0)
						user.hasOwnFlag = false;
					else
						user.hasOwnFlag = true;
				}
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
					if (user.userGameID == players[i].userGameID && !user.hasOpponentFlag) {
						drawable = getResources().getDrawable(R.drawable.black_dot);
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
						if(players[i].hasOwnFlag){
							drawable = getResources().getDrawable(R.drawable.blue_flag);
						}
						else{
							drawable = getResources().getDrawable(R.drawable.blue_castle_transparent);
						}
					}
					else{
						if(players[i].hasOwnFlag){
							drawable = getResources().getDrawable(R.drawable.red_flag);
						}
						else drawable = getResources().getDrawable(R.drawable.red_castle_transparent);
					}
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
				endGame();
			}
		}.start();
		return timer;
	}

	// this will basically create a new thread that will handle updating
	// everything
	public CountDownTimer startUpdateCountDown(int milliseconds) {
		CountDownTimer timer = new CountDownTimer(milliseconds, 500) {
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
		//System.out.println("Distance: " + distanceToOppFlag);
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
			if(bases[i].hasOwnFlag && bases[i].teamID != user.teamID && !user.hasOpponentFlag){
				if(distanceToOppFlag < MIN_FLAG_DISTANCE && !isAlertUp){
					takeFlag();
				}
			}
			if(user.hasOpponentFlag && user.teamID == bases[i].teamID){
				if(!isAlertUp && distanceToOwnBase < MIN_FLAG_DISTANCE){
					score();
				}
			}
		}
		//compare to players
		for(int i = 0; i < numPlayers; i++){
			//take opp flag
			if(players[i].hasOpponentFlag && players[i].teamID != user.teamID && !user.hasOpponentFlag){
				if(distanceToOppFlag < MIN_FLAG_DISTANCE && !isAlertUp){
					takeFlag();
				}
			}
		}
	}
	
	private void score(){
		isAlertUp = true;
		AlertDialog steal = new AlertDialog.Builder(PlayGame.this).create();
		
		steal.requestWindowFeature(Window.FEATURE_NO_TITLE);
		WindowManager.LayoutParams orientSteal = steal.getWindow().getAttributes();
		orientSteal.gravity = Gravity.BOTTOM | Gravity.CENTER;
		orientSteal.x = 0;
		orientSteal.y = 0;
		steal.getWindow().setAttributes(orientSteal);
		    			
		steal.setIcon(0);
		    			
		steal.setTitle("Ready to score!");
		steal.setButton("Score!", new DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, 
				int which){
				scoreServerCall();
				isAlertUp = false;
			}

		});
			
		steal.setButton2("Cancel", new DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, 
				int which){
				isAlertUp = false;
			}

		});
		steal.show();
	}
	
	private void takeFlag(){
		isAlertUp = true;
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
				isAlertUp = false;
			}

		});
			
		steal.setButton2("Cancel", new DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, 
				int which){
				isAlertUp = false;
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
						.add(new BasicNameValuePair("userId", "" + userID));
				nameValuePairs.add(new BasicNameValuePair("gameId", "" + gameID));
				nameValuePairs.add(new BasicNameValuePair("teamId", "" + teamID));
				nameValuePairs.add(new BasicNameValuePair("xPos", "" + user.xPosition));
				nameValuePairs.add(new BasicNameValuePair("yPos", "" + user.yPosition));
				nameValuePairs.add(new BasicNameValuePair("takeOppFlag", "1"));
				nameValuePairs.add(new BasicNameValuePair("takeOwnFlag", "0"));
				

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
						user.hasOpponentFlag = true;
						// Create new input stream with received data assigned
						InputStream instream = entity.getContent();

						// Create new JSON Object. assign converted data as
						// parameter.
						JSONObject jsonResponse = new JSONObject(
								convertStreamToString(instream));
						distanceToOppFlag = jsonResponse.getDouble("distanceToOpponentFlag") * 364320;
						distanceToOwnFlag = jsonResponse.getDouble("distanceToOwnFlag") * 364320;
						distanceToOwnBase = jsonResponse.getDouble("distanceToBase") * 364320;

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
	
	private void scoreServerCall(){

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
			URI uri = new URI(scoreURL);
			HttpPost httppost = new HttpPost(uri);

			// place credentials in the array list
			nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs
					.add(new BasicNameValuePair("userId", "" + userID));
			nameValuePairs.add(new BasicNameValuePair("gameId", "" + gameID));
			nameValuePairs.add(new BasicNameValuePair("teamId", "" + teamID));
			nameValuePairs.add(new BasicNameValuePair("xPos", "" + user.xPosition));
			nameValuePairs.add(new BasicNameValuePair("yPos", "" + user.yPosition));
			

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
					user.hasOpponentFlag = false;
					
					// Create new input stream with received data assigned
					InputStream instream = entity.getContent();

					// Create new JSON Object. assign converted data as
					// parameter.
					JSONObject jsonResponse = new JSONObject(
							convertStreamToString(instream));
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void endGame(){
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;
		CharSequence text;
		Toast toast;
		if(redScore > blueScore){
			text = "Red team wins!!!";
			toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		else if(redScore < blueScore){
			text = "Blue team wins!!!";
			toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		else if(redScore == blueScore){
			text = "Tied game";
			toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		
		updateTimer.cancel();
		timer.cancel();
		finish();
		leaveGame();
	}
}
