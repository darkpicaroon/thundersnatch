package com.thundersnatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class GameLobby extends Activity {
	
	private int userID;
	private double xPos;
	private double yPos;
	private int teamSize;
	private int mapRadius;
	
	private boolean host;

	private static final int ADD_ITEM_ID = 1;
	
	private ArrayList<HashMap<String,String>> redTeam = new ArrayList<HashMap<String,String>>();
	private SimpleAdapter redTeamAdapter;
	
	private ArrayList<HashMap<String,String>> blueTeam = new ArrayList<HashMap<String,String>>();
	private SimpleAdapter blueTeamAdapter;
	
	private ListView redListView;
	private ListView blueListView;
	
	String serverURL = "http://www.rkaneda.com/GetPlayerListForLobby.php";
	
	boolean readyToStart = false;
	
	
	private SharedPreferences settings;
    private SharedPreferences.Editor editor;
	
	int userGameID;
	
	CountDownTimer updater;
	
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
       
        
        redListView = (ListView)findViewById(R.id.listView1);
        blueListView = (ListView)findViewById(R.id.listView2);
        		
        redTeamAdapter = new SimpleAdapter( 
				this, 
				redTeam,
				R.layout.red_team_item,
				new String[] { "line1" },
				new int[] { R.id.text1 }  );
        
        blueTeamAdapter = new SimpleAdapter( 
				this, 
				blueTeam,
				R.layout.blue_team_item,
				new String[] { "line1" },
				new int[] { R.id.text1 }  );
        
        redListView.setAdapter(redTeamAdapter);
        blueListView.setAdapter(blueTeamAdapter);
        
        
     // Sets up the "Start" button handler.
        Button start = (Button)findViewById(R.id.startButton);
        start.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				updater.cancel();
				finish();
				
				JSONObject response = lobbyServerInterface(1);

		        moveToGame();
				
			}
		});
        updater = startCountdown();
        
    }
    
    @Override
    public void onStop() {
        super.onStop();
        updater.cancel();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_game_lobby, menu);
//        return true;
//    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      boolean result = super.onCreateOptionsMenu(menu);
      menu.add(0, ADD_ITEM_ID, Menu.NONE, R.string.add_item );
      return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
          case ADD_ITEM_ID:
				addRedPlayer("fuck");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void moveToGame(){
    	Intent intent = new Intent(GameLobby.this, PlayGame.class);
        GameLobby.this.startActivity(intent);
    }
    
    private void addRedPlayer(String player) {
	    HashMap<String,String> item = new HashMap<String,String>();
	    
	    item.put("line1", player);
	    if(!redTeam.contains(item)){
	    	redTeam.add(item);
	        redTeamAdapter.notifyDataSetChanged();
	    }
	  }
       
       private void addBluePlayer(String player) {
        HashMap<String,String> item = new HashMap<String,String>();
        item.put("line1", player);
        if(!blueTeam.contains(item)){
        	blueTeam.add(item);
        	blueTeamAdapter.notifyDataSetChanged();
        }
      }
    
    
    private JSONObject lobbyServerInterface(int status) {
		
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
			userID = settings.getInt("userID", 0);
			nameValuePairs.add(new BasicNameValuePair("userId", "" + userID));
			int gameID = settings.getInt("GameID", 0);
			nameValuePairs.add(new BasicNameValuePair("gameId", "" + gameID));
			nameValuePairs.add(new BasicNameValuePair("status", "" + status));
			
			System.out.println("UserID: " + userID + " GameID: " + gameID);

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
    
    public void updateLobby(){
    	try{
        	JSONObject response = lobbyServerInterface(0);
        	System.out.println("response: " + response.toString());
        	if(response.getInt("GameStatus") == 1){
        		readyToStart = true;
        	}
        	int numPlayers = response.getInt("numPlayers");
        	for(int i = 0; i < numPlayers; i++){
        		String username = response.getJSONObject("PlayerArray").getJSONObject("player" + i).getString("UserName");
        		int teamID = response.getJSONObject("PlayerArray").getJSONObject("player" + i).getInt("TeamID");
        		if(teamID == settings.getInt("TeamID", 0)){
        			//addItems does nothing if string already exists in list
        			//put players on users side
        			if(settings.getString("TeamColor", "").equals("blue"))//put player on blue side
        				addBluePlayer(username);
        			else //put player on red side
        				addRedPlayer(username);
        		}
        		else{
        			//put player on opp side
        			if(settings.getString("TeamColor", "").equals("blue"))//put player on red side
        				addRedPlayer(username);
        			else //put player on blue side
        				addBluePlayer(username);
        		}
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }
    }
    
    public CountDownTimer startCountdown(){
		CountDownTimer timer = new CountDownTimer(300000, 1000){
			public void onTick(long millisecondsUntilFinished){
				updateLobby();
				if(readyToStart) moveToGame();
			}
			public void onFinish(){
			}
		}.start();
		return timer;
	}
}

