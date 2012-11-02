package com.thundersnatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class InGame extends MapActivity{
	public final int MAX_NUM_PLAYERS = 20;
	
	public String updateURL = "";

	MapView mapV;
	GeoPoint geoP;
	MapController mapC;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    
	    setContentView(R.layout.activity_map);
	    
	    mapV = (MapView) findViewById(R.id.mapView);
	    mapV.displayZoomControls(true);
	    mapV.setBuiltInZoomControls(true);
	}	
	
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private Player[] updatePositions(float xPosition, float yPosition){
		//create array to return
		Player[] players = new Player[MAX_NUM_PLAYERS];
		
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
            
           // TreeMap<String, Integer> coordinates = new TreeMap<String,Integer>();
            //coordinates.add("XPosition", xPosition);
			//place credentials in the array list
			nameValuePairs = new ArrayList<NameValuePair>();
            //nameValuePairs.add(new NameValuePair("XPosition", xPosition));
            //nameValuePairs.add(new NameValuePair("YPosition", yPosition));
			
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
					 
					 
					 //ADD PLAYER ARRAY ASSIGNMENT
					 /*
					  * for(int i = 0; i < numPlayers; i++){
					  * userID = id;
						xPosition = x;
						yPosition = y;
						hasOwnFlag = own;
						hasOpponentFlag = opp;
						stealsThisGame = steals;
						capturesThisGame = captures;
						teamID = team;
						
						players[i] = Player(userID, xPosition, yPosition, hasOwnFlag, hasOpponentFlag, stealsThisGame, capturesThisGame, teamID);
						}
					  */
					    			     
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
		
}
