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
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;


public class GameLobby extends Activity {
	Bundle extras;
	String serverURL = "http://www.rkaneda.com/GetPlayerListForLobby.php";
	
	boolean readyToStart = false;
	
	LobbyLists redLobbyList;
	LobbyLists blueLobbyList;
	
	private SharedPreferences settings;
    private SharedPreferences.Editor editor;
	
	int userGameID;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Sets the activity to fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_game_lobby);
        
        settings = getApplicationContext().getSharedPreferences("com.thundersnatch", Context.MODE_PRIVATE);
        editor = settings.edit();
        
        redLobbyList = new LobbyLists((ListView)findViewById(R.id.redList));
    	blueLobbyList = new LobbyLists((ListView)findViewById(R.id.blueList));

        
        extras = this.getIntent().getExtras();
        //userGameID = extras.getInt("UserGameID");
        
        
        // Sets up the "Start" button handler.
        Button start = (Button)findViewById(R.id.startButton);
        start.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				finish();
				
				JSONObject response = serverShit(1);

		        moveToGame(extras);
				
			}
		});
        
        try{
	        while(!readyToStart){
	        	JSONObject response = serverShit(0);
	        	System.out.println("is valid:" + response.getBoolean("isValid"));
	        	if(response.getInt("gameStatus") == 1){
	        		readyToStart = true;
	        		break;
	        	}
	        	int numPlayers = response.getInt("numPlayers");
	        	System.out.println("numPlayers " + numPlayers);
	        	for(int i = 0; i < numPlayers; i++){
	        		String username = response.getJSONArray("PlayerArray").getJSONObject(i).getString("UserName");
	        		System.out.println(username);
	        		int teamID = response.getJSONArray("PlayerArray").getJSONObject(i).getInt("TeamID");
	        		if(teamID == extras.getInt("TeamID")){
	        			//addItems does nothing if string already exists in list
	        			//put players on users side
	        			if(extras.getString("teamColor" ).equals("blue"))//put player on blue side
	        				blueLobbyList.addItems(username);
	        			else //put player on red side
	        				redLobbyList.addItems(username);
	        		}
	        		else{
	        			//put player on opp side
	        			if(extras.getString("teamColor" ).equals("blue"))//put player on red side
	        				redLobbyList.addItems(username);
	        			else //put player on blue side
	        				blueLobbyList.addItems(username);
	        		}
	        	}
	        }
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        //moveToGame(extras);
    }
    
    private void moveToGame(Bundle extras){
    	Intent intent = new Intent(GameLobby.this, PlayGame.class);
        intent.putExtra("UserGameID", "" + userGameID);
        GameLobby.this.startActivity(intent);
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_game_lobby, menu);
        return true;
    }
    
    
    private JSONObject serverShit(int status) {
		
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
			URI uri = new URI(serverURL);
			HttpPost httppost = new HttpPost(uri);// this is where the address
													// to the php file goes

			// place credentials in the array list
			nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("userId", "" + settings.getInt("UserID", -1)));
			nameValuePairs.add(new BasicNameValuePair("gameId", "" + settings.getInt("GameID", -1)));
			nameValuePairs.add(new BasicNameValuePair("status", "" + status));

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
					return jsonResponse;

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
