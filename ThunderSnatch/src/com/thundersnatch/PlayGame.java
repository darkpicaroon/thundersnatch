package com.thundersnatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
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


//          SCOTT AND ERIC:
//Still need to work out how to incorporate the bases into this class.
//the locations of the bases are currently being stored in the Team table
//in the database under FlagStartXPosition and FlagStartYPosition.

//The other thing that needs to be done is to incorporate the "rules"
//Mike and I have outlined what needs to be done for this at the bottom of 
//this class in a function called compareLocations(). that function is being called 
//after putting the locations on the map in a thread like structure called
//startUpdateCountDown()

//Its about 4 in the morning so im calling it a night. The very minimum needed to 
//present tomorrow is done: we can have multiple users join a lobby and then proceed 
//to the playgame class together, where there positions will be placed on the map.
//I have tested this many times to ensure functionality.

//good luck to you guys!
// -Andrew






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
	
	Player user;
	Player redFlag = null;
	Player blueFlag = null;

	private String updateURL = "http://www.rkaneda.com/Update.php";
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
	
	private SharedPreferences settings;
    private SharedPreferences.Editor editor;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		settings = getApplicationContext().getSharedPreferences("com.thundersnatch", Context.MODE_PRIVATE);
        editor = settings.edit();
        
		userGameID = settings.getInt("UserGameID", 0);
		gameID = settings.getInt("GameID", 0);
		teamID = settings.getInt("TeamID", 0);
		teamColor = settings.getString("TeamColor", "");
		userID = settings.getInt("userID", 0);

		user = new Player(userID, "USER",
				settings.getFloat("Longitude", 0),
				settings.getFloat("Latitude", 0), false, false, teamID);	

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_map);
		
		clockText = (TextView)findViewById(R.id.countDown);
		startCountdown(900000);
		startUpdateCountDown(900000);
		
		blueScoreView = (TextView)findViewById(R.id.blueScore);
		blueScore = 0;
		redScoreView = (TextView)findViewById(R.id.redScore);
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
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_play_game, menu);
		return true;
	}

	protected boolean isRouteDisplayed() {
		return false;
	}

//	protected void updatePositions(float xPosition, float yPosition,
//			boolean hasOwnFlag, boolean hasOppFlag, Player[] players) {
//
//		// Create a HTTPClient as the form container
//		HttpClient httpclient = new DefaultHttpClient();
//
//		// Create an array list for the input data to be sent
//		ArrayList<NameValuePair> nameValuePairs;
//
//		// Create a HTTP Response and HTTP Entity
//		HttpResponse response;
//		HttpEntity entity;
//
//		// run http methods
//		try {
//			// Use HTTP POST method
//			URI uri = new URI(updateURL);
//			HttpPost httppost = new HttpPost(uri);
//
//			// place credentials in the array list
//			nameValuePairs = new ArrayList<NameValuePair>();
//			nameValuePairs.add(new BasicNameValuePair("xPosition", ""
//					+ xPosition));
//			nameValuePairs.add(new BasicNameValuePair("yPosition", ""
//					+ yPosition));
//			nameValuePairs.add(new BasicNameValuePair("userGameID", ""
//					+ userGameID));
//			nameValuePairs.add(new BasicNameValuePair("gameID", "" + gameID));
//			int iOwn = hasOwnFlag ? 1 : 0;
//			nameValuePairs.add(new BasicNameValuePair("hasOwnFlag", "" + iOwn));
//			int iOpp = hasOppFlag ? 1 : 0;
//			nameValuePairs.add(new BasicNameValuePair("hasOppFlag", "" + iOpp));
//
//			// Add array list to http post
//			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//
//			// assign executed form container to response
//			response = httpclient.execute(httppost);
//
//			// check status code, need to check status code 200
//			if (response.getStatusLine().getStatusCode() == 200) {
//
//				// assign response entity to http entity
//				entity = response.getEntity();
//
//				// check if entity is not null
//				if (entity != null) {
//					// Create new input stream with received data assigned
//					InputStream instream = entity.getContent();
//
//					// Create new JSON Object. assign converted data as
//					// parameter.
//					JSONObject jsonResponse = new JSONObject(
//							convertStreamToString(instream));
//
//					numPlayers = jsonResponse.getInt("NumUsers");
//					for (int i = 0; i < numPlayers; i++) {
//						convertJsonResponseToPlayer(jsonResponse, i, players);
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}// end update method
	
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
			nameValuePairs.add(new BasicNameValuePair("userId", ""
					+ userGameID));
			nameValuePairs.add(new BasicNameValuePair("gameId", "" + gameID));
			nameValuePairs.add(new BasicNameValuePair("teamId", "" + teamID));
			nameValuePairs.add(new BasicNameValuePair("xPos", ""
					+ xPosition));
			nameValuePairs.add(new BasicNameValuePair("yPos", ""
					+ yPosition));

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

					JSONObject team0 = jsonResponse.getJSONObject("Team0");
					JSONObject team1 = jsonResponse.getJSONObject("Team1");
					
					if (bases[1] == null)
					{

						double x = team0.getDouble("FlagStartXPos");
						double y = team0.getDouble("FlagStartYPos");
						int teamId = team0.getInt("TeamID");
						blueFlag = new Player((float) x, (float) (y), true,
								true, teamId, 0);
						bases[0] = blueFlag;

						x = team1.getDouble("FlagStartXPos");
						y = team1.getDouble("FlagStartYPos");
						teamId = team1.getInt("TeamID");
						redFlag = new Player((float) x, (float) (y), true,
								true, teamId, 1);
						bases[1] = redFlag;
					}
					
					numPlayers = jsonResponse.getInt("numPlayers");
					//System.out.println(numPlayers);
					JSONObject playerList = jsonResponse.getJSONObject("PlayerArray");
					 
					for (int i = 0; i < playerList.length(); i++) {
						JSONObject player = playerList.getJSONObject("player" + i);
						System.out.println(player);
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

//	public void convertJsonResponseToPlayer(JSONObject json, int i,
//			Player[] players) {
//		try {
//			String stringToParse = json.getString("User" + i);
//			String[] playerInfo = stringToParse.split(", ");
//			String[] temp;
//			temp = playerInfo[0].split("UserGameID: ");
//			int userID = Integer.parseInt(temp[1]);
//			int index = -1;
//			for (int j = 0; j < numPlayers; j++) {
//				if (players[j] != null) {
//					if (players[j].userGameID == userID) {
//						index = j;
//						break;
//					}
//				} else {
//					index = index * j;
//					break;
//				}
//			}
//			if (index > 0) {
//				// player already exists in array
//				temp = playerInfo[3].split("XPos: ");
//				players[index].xPosition = Float.parseFloat(temp[1]);
//				temp = playerInfo[4].split("YPos: ");
//				players[index].yPosition = Float.parseFloat(temp[1]);
//				temp = playerInfo[5].split("hasOpponentFlag: ");
//				int opp = Integer.parseInt(temp[1]);
//				if (opp == 0)
//					players[index].hasOpponentFlag = false;
//				else
//					players[index].hasOpponentFlag = true;
//				temp = playerInfo[6].split("hasOwnFlag: ");
//				int own = Integer.parseInt(temp[1]);
//				if (own == 0)
//					players[index].hasOwnFlag = false;
//				else
//					players[index].hasOwnFlag = true;
//			} else {
//				// player does not exist in array; need to create new player
//				index = index * -1;
//
//				temp = playerInfo[1].split("UserName: ");
//				String userName = temp[1];
//				temp = playerInfo[2].split("TeamID: ");
//				int teamID = Integer.parseInt(temp[1]);
//				temp = playerInfo[3].split("XPos: ");
//				float xPosition = Float.parseFloat(temp[1]);
//				temp = playerInfo[4].split("YPos: ");
//				float yPosition = Float.parseFloat(temp[1]);
//				temp = playerInfo[5].split("hasOpponentFlag: ");
//				int opp = Integer.parseInt(temp[1]);
//				boolean hasOpponentFlag;
//				if (opp == 0)
//					hasOpponentFlag = false;
//				else
//					hasOpponentFlag = true;
//				temp = playerInfo[6].split("hasOwnFlag: ");
//				boolean hasOwnFlag;
//				int own = Integer.parseInt(temp[1]);
//				if (own == 0)
//					hasOwnFlag = false;
//				else
//					hasOwnFlag = true;
//				players[index] = new Player(userID, userName, xPosition,
//						yPosition, hasOpponentFlag, hasOwnFlag, teamID);
//
//			}
//		} catch (Exception e) {
//			System.out.println("Error: " + e);
//		}
//	}// end json response conversion
	
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
				players[index].xPosition = (float)json.getDouble("XPos");
				players[index].yPosition = (float)json.getDouble("YPos");
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
				
				players[index] = new Player(json.getInt("UserID"), json.getString("UserName"),
						(float)json.getDouble("XPos"), (float)json.getDouble("YPos"), 
						hasOpponentFlag, hasOwnFlag, json.getInt("TeamID"));
					
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
		if (players.length == 20){
			count = numPlayers;
			map.getOverlays().clear();
			mapOverlays = map.getOverlays();
			
		}
		else if (players.length == 2){
			count = 2;
			mapOverlays = map.getOverlays();
		}
		else
			System.out.println("Error");
		
		System.out.println("count:" + count);
		
		if (players != null) {
			for (int i = 0; i < count; i++) {

				System.out.println(i);
				System.out.println(players[i]);
				if (user.userGameID == players[i].userGameID) {
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

				itemizedoverlay = new MapItemizedOverlay(drawable, this);

				point = new GeoPoint((int) (players[i].xPosition * 1E6),
						(int) (players[i].yPosition * 1E6));
				overlayitem = new OverlayItem(point, players[i].userName, ""
						+ players[i].teamID);
				itemizedoverlay.addOverlay(overlayitem);
				mapOverlays.add(itemizedoverlay);
			}
		}
		map.invalidate();
	}
	
	public void startCountdown(int milliseconds){
		new CountDownTimer(milliseconds, 1000){
			public void onTick(long millisecondsUntilFinished){
				int seconds = (int)((millisecondsUntilFinished/1000)%60);
				int minutes = (int)((millisecondsUntilFinished - (seconds*1000))/60000);
				if(seconds >= 10)
					clockText.setText(minutes + ":" + seconds);
				else
					clockText.setText(minutes + ":0" + seconds);
			}
			public void onFinish(){
				
			}
		}.start();
	}
	
	//this will basically create a new thread that will handle updating everything
	public void startUpdateCountDown(int milliseconds){
		new CountDownTimer(milliseconds, 1000){
			public void onTick(long millisecondsUntilFinished){
				updatePositions(user.xPosition, user.yPosition,
						 user.hasOwnFlag, user.hasOpponentFlag, players);
				putLocationsOnMap(players, map);
				putLocationsOnMap(bases, map);
				compareLocations();
			}
			public void onFinish(){
				
			}
		}.start();
	}
	
	@Override
    protected void onPause()
    {
    	super.onPause();
    	
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
	
	/*
	 * This is all you guys, have fun lol
	 * 
	 * update score with: redScoreView.setText(redScore++);
	 * 
	 */
	public void compareLocations(){
		//loop through the player array, on players that have a flag and are on opposing team
		//check distance, and if distance small enough, use AlertDialog to steal/capture flag
		
		//steal from opp player
		//on steal, make a new servercall with the opponents usergameID as a parameter
		//to indicate that they no longer have the flag
		//you will need to work with david/raiden to create that php file.
		
		//snatch flag from base
		//so there is that case for shit getting done in each time interval
		//then check to see if player is within specified distance to enemy "base" player to capture flag
		//show them steal button and handle capture
		
		//capture flag and score point
		//finally, check if player has the enemies flag and is within specified distance to friendly "base player"
		//to capture flag
		//show them capture button (or just do it automatically) and handle server shit
		//need way to increment the points on the server and in local text field
	}
}
