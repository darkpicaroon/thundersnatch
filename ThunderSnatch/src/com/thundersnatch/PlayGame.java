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
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

//TEAM ID's ARE NOT BEING STORED FOR SOME REASON. NEED TO DEBUG

/* To enable the maps functionality, you must set the keystore location in your own IDE
 * to the file "debug.keystore" in this package.
 * 
 * To do this, open up the "Preferences..." menu; expand "Android"; click on the "Build" tab; 
 * insert the filepath into the "custom debug keystore:" text box; click "Apply"
 */

public class PlayGame extends MapActivity {

	public final int MAX_NUM_PLAYERS = 20;
	public Player[] players = new Player[MAX_NUM_PLAYERS];
	private int numPlayers = 0;

	private int userGameID;
	private int gameID;
	private int teamID;
	private String teamColor;

	Player user;

	private String updateURL = "http://www.rkaneda.com/Update.php";

	public MapView map;
	MapController mapC;
	MyLocationOverlay compass;
	MyLocationOverlay location;
	GeoPoint touchPoint;
	List<Overlay> overlayList;
	Drawable d;
	
	int x, y;
	
	long startPoint;
	long stopPoint;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		userGameID = extras.getInt("UserGameID");
		gameID = extras.getInt("GameID");
		teamID = extras.getInt("TeamID");
		teamColor = extras.getString("TeamColor");

		user = new Player(userGameID, "USER",
				extras.getFloat("Longitude"),
				extras.getFloat("Latitude"), false, false, false, teamID);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_play_game);

		map = (MapView) findViewById(R.id.MapView);
		map.displayZoomControls(false);
		map.setBuiltInZoomControls(false);
		map.setSatellite(false);
		map.setTraffic(false);

		MapController mapControl = map.getController();
		mapControl.setZoom(3);
		

		if (!(user.xPosition == 0) || !(user.yPosition == 0)) {
			updatePositions(user.xPosition, user.yPosition,
					 user.hasOwnFlag, user.hasOpponentFlag, players);
			putLocationsOnMap(players, map);
			mapControl.animateTo(new GeoPoint((int) (user.yPosition * 1e6),
					(int) (user.xPosition * 1e6)));
		}

		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				user.xPosition = (float) location.getLatitude();
				user.yPosition = (float) location.getLongitude();

				// now being done in thread
				 updatePositions(user.xPosition, user.yPosition,
						 user.hasOwnFlag, user.hasOpponentFlag, players);
				 putLocationsOnMap(players, map);
				
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
    
	public void compareLocation(Player[] players){
		
		int i;
		for(i=0; i<players.length; i++){
			
		}
		
		players[i] = jointFlagPlayer(players[i]); 
	}
	
	public Player jointFlagPlayer(Player player){
		
		
		return player;
	}
	
    /*
     * Override method for MapActivity.
     * Doesn't need to be changed.
     */
	protected boolean isRouteDisplayed() {
		return false;
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
			URI uri = new URI(updateURL);
			HttpPost httppost = new HttpPost(uri);

			// place credentials in the array list
			nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("xPosition", ""
					+ xPosition));
			nameValuePairs.add(new BasicNameValuePair("yPosition", ""
					+ yPosition));
			nameValuePairs.add(new BasicNameValuePair("userGameID", ""
					+ userGameID));
			nameValuePairs.add(new BasicNameValuePair("gameID", "" + gameID));
			int iOwn = hasOwnFlag ? 1 : 0;
			nameValuePairs.add(new BasicNameValuePair("hasOwnFlag", "" + iOwn));
			int iOpp = hasOppFlag ? 1 : 0;
			nameValuePairs.add(new BasicNameValuePair("hasOppFlag", "" + iOpp));

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

					numPlayers = jsonResponse.getInt("NumUsers");
					for (int i = 0; i < numPlayers; i++) {
						convertJsonResponseToPlayer(jsonResponse, i, players);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// end update method

	/*
	 * 
	 */
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

	public void convertJsonResponseToPlayer(JSONObject json, int i,
			Player[] players) {
		try {
			String stringToParse = json.getString("User" + i);
			String[] playerInfo = stringToParse.split(", ");
			String[] temp;
			temp = playerInfo[0].split("UserGameID: ");
			int userID = Integer.parseInt(temp[1]);
			if (userID < 0){
				
			}
			int index = -1;
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
				temp = playerInfo[3].split("xPosition: ");
				players[index].xPosition = Float.parseFloat(temp[1]);
				temp = playerInfo[4].split("yPosition: ");
				players[index].yPosition = Float.parseFloat(temp[1]);
				temp = playerInfo[5].split("hasOppFlag: ");
				int opp = Integer.parseInt(temp[1]);
				if (opp == 0)
					players[index].hasOpponentFlag = false;
				else
					players[index].hasOpponentFlag = true;
				temp = playerInfo[6].split("hasOwnFlag: ");
				int own = Integer.parseInt(temp[1]);
				if (own == 0)
					players[index].hasOwnFlag = false;
				else
					players[index].hasOwnFlag = true;
			} else {
				// player does not exist in array; need to create new player
				index = index * -1;

				temp = playerInfo[1].split("userName: ");
				String userName = temp[1];
				temp = playerInfo[2].split("teamID: ");
				int teamID = Integer.parseInt(temp[1]);
				temp = playerInfo[3].split("xPosition: ");
				float xPosition = Float.parseFloat(temp[1]);
				temp = playerInfo[4].split("yPosition: ");
				float yPosition = Float.parseFloat(temp[1]);
				temp = playerInfo[5].split("hasOppFlag: ");
				int opp = Integer.parseInt(temp[1]);
				boolean hasOpponentFlag;
				if (opp == 0)
					hasOpponentFlag = false;
				else
					hasOpponentFlag = true;
				temp = playerInfo[6].split("hasOwnFlag: ");
				boolean hasOwnFlag;
				int own = Integer.parseInt(temp[1]);
				if (own == 0)
					hasOwnFlag = false;
				else
					hasOwnFlag = true;
				temp = playerInfo[7].split("isBase: ");
				int base = Integer.parseInt(temp[1]);
				boolean isBase;
				if (base == 0)
					isBase = false;
				else
					isBase = true;
				players[index] = new Player(userID, userName, xPosition,
						yPosition, hasOpponentFlag, hasOwnFlag, isBase, teamID);

			}
		} catch (Exception e) {
			System.out.println("Error: " + e);
		}
	}// end json response conversion

	public void putLocationsOnMap(Player[] players, MapView map) {

		map.getOverlays().clear();
		List<Overlay> mapOverlays = map.getOverlays();
		Drawable drawable = null;
		GeoPoint point;
		MapItemizedOverlay itemizedoverlay;
		OverlayItem overlayitem;

		if (players != null) {
			for (int i = 0; i < numPlayers; i++) {
				if (user.userGameID == players[i].userGameID) {
					System.out.println("do i get here?");
					
					drawable = getResources().getDrawable(R.drawable.black_dot);
				} 
				else if ((teamColor.equals("red") && teamID == players[i].teamID)
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
	}// end putLocations on map

}
