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
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class PlayGame extends MapActivity {

	public final int MAX_NUM_PLAYERS = 20;
	
	public String updateURL = "http://www.rkaneda.com/update.php";
	
	MapView map;
	GeoPoint geoP;
	MapController mapC;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_map);
        map = (MapView)findViewById(R.id.mapView); 
        map.displayZoomControls(false);
        map.setBuiltInZoomControls(false);
        
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            	location.setLatitude(28.555708);
            	location.setLongitude(-81.276141);
            	//Called when a new location is found by the network location provider.
            	List<Overlay> mapOverlays = map.getOverlays();
            	Drawable drawable = getResources().getDrawable(R.drawable.black_dot);
                MapItemizedOverlay itemizedoverlay = new MapItemizedOverlay(drawable);
                
                GeoPoint point = new GeoPoint((int)(location.getLatitude() * 1E6),  (int)(location.getLongitude() * 1E6));
            	OverlayItem overlayitem = new OverlayItem(point, "username goes here", "");
            	itemizedoverlay.addOverlay(overlayitem);
                mapOverlays.add(itemizedoverlay);
                
                Player[] players = updatePositions((float)location.getLatitude(), (float)location.getLongitude());
                if(players != null){
	                for(int i = 0; i < players.length; i++){
	    				//if ((myTeam == redTeam && myTeamID == iTeamID) || (myTeam == blueTeam && myTeamID != iTeamID))
	                		//if iHasOwnFlag == 1
	                			//drawable = getResources().getDrawable(R.drawable.red_flag);
	                		//else if iHasOppFlag == 1
	            				//drawable = getResources().getDrawable(R.drawable.blue_flag);
	                		//else drawable = getResources().getDrawable(R.drawable.red_dot);
	                	//if ((myTeam == blueTeam && myTeamID == iTeamID) || (myTeam == redTeam && myTeamID != iTeamID))
	                		//if iHasOwnFlag == 1
	            				//drawable = getResources().getDrawable(R.drawable.blue_flag);
	            			//else if iHasOppFlag == 1
	        					//drawable = getResources().getDrawable(R.drawable.red_flag);
	            			//else drawable = getResources().getDrawable(R.drawable.blue_dot);
	    				
	                    itemizedoverlay = new MapItemizedOverlay(drawable);
	                    
	                	point = new GeoPoint((int)(players[i].xPosition * 1E6),  (int)(players[i].yPosition * 1E6));
	                	overlayitem = new OverlayItem(point, players[i].userName, "");
	                	itemizedoverlay.addOverlay(overlayitem);
	                    mapOverlays.add(itemizedoverlay);
	                }
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
          };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_play_game, menu);
        return true;
    }

	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private Player[] updatePositions(float xPosition, float yPosition){
		//Create array to return
		Player[] players = null;
		
		//Create a HTTPClient as the form container
        HttpClient httpclient = new DefaultHttpClient();
        
        //Create an array list for the input data to be sent
        ArrayList<NameValuePair> nameValuePairs;
        
        //Create a HTTP Response and HTTP Entity
        HttpResponse response;
        HttpEntity entity;
       
		
        //run http methods
		try {
			//Use HTTP POST method
            URI uri = new URI(updateURL);
            HttpPost httppost = new HttpPost(uri);
            
			//place credentials in the array list
			nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("XPosition", "" + xPosition));
            nameValuePairs.add(new BasicNameValuePair("YPosition", "" + yPosition));
            //nameValuePairs.add(new BasicNameValuePair("userGameID", "" + userGameID));
            //nameValuePairs.add(new BasicNameValuePair("gameID", "" + gameID));
			
			//Add array list to http post
		    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		   
		    //assign executed form container to response
			response = httpclient.execute(httppost);
			
			//check status code, need to check status code 200
			if(response.getStatusLine().getStatusCode()== 200){
			    
			   //assign response entity to http entity
			   entity = response.getEntity();
			    
			   //check if entity is not null
			   if(entity != null){
				     //Create new input stream with received data assigned
					 InputStream instream = entity.getContent();
					 
					 //Create new JSON Object. assign converted data as parameter.
					 JSONObject jsonResponse = new JSONObject(convertStreamToString(instream));
					 
					 

					 players = new Player[jsonResponse.getInt("NumUsers")];
					 for(int i = 0; i < jsonResponse.getInt("NumUsers"); i++){
						 players[i] = convertJsonResponseToPlayer(jsonResponse, i);
					 }
					    			     
			   }
		   }
		} 
		catch(Exception e){
		   e.printStackTrace();
		}
		return players;
		
		
	}
	
	private static String convertStreamToString(InputStream is) {
	    /*
	 * To convert the InputStream to String we use the BufferedReader.readLine()
	 * method. We iterate until the BufferedReader return null which means
	 * there's no more data to read. Each line will appended to a StringBuilder
	 * and returned as String.
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
	}
	
	public Player convertJsonResponseToPlayer(JSONObject json, int i){
		try{
			String stringToParse = json.getString("User" + i);
			String[] playerInfo = stringToParse.split(", ");
			String[] temp;
			temp = playerInfo[0].split("UserGameID: ");
			int userID = Integer.parseInt(temp[1]);
			temp = playerInfo[0].split("userName: ");
			String userName = temp[1];	
			temp = playerInfo[0].split("TeamID: ");
			int teamID = Integer.parseInt(temp[1]);
			temp = playerInfo[0].split("xPosition: ");
			float xPosition = Float.parseFloat(temp[1]);
			temp = playerInfo[0].split("yPosition: ");
			float yPosition = Float.parseFloat(temp[1]);
			temp = playerInfo[0].split("hasOppFlag: ");
			boolean hasOpponentFlag = Boolean.parseBoolean(temp[1]);
			temp = playerInfo[0].split("hasOwnFlag: ");
			boolean hasOwnFlag = Boolean.parseBoolean(temp[1]);
			return new Player(userID, userName, xPosition, yPosition, hasOwnFlag, hasOpponentFlag,teamID);
		}catch(Exception e){
			System.out.println(e);
		}
		return null;
	}
	
}
