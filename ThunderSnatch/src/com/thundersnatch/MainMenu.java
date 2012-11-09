/*
 * Filename: MainMenu.java
 * 
 * Description: This file represents the mainmenu of the game. From here, the user
 * 				can create a game, join a game, logout, or quit the application.
 * 				Creating and joining a game will take you to their corresponding
 *				screens. Logging out will take you back to the login screen so you
 *              can log into a new account. Lastly, quitting will exit the application.
 */

package com.thundersnatch;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainMenu extends Activity {
	
	public Location location;
	
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        // Sets the activity to fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_main_menu);
        
        //Start looking for a location
        //Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                saveLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
          };
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        
        
        // Sets up the "Create Game" button handler.
        Button createGame = (Button)findViewById(R.id.button1);
        createGame.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				Intent intent = new Intent(MainMenu.this, CreateGame.class);
	            MainMenu.this.startActivity(intent);
				
			}
		});
        
        // Sets up the "Join Game" button handler.
        Button joinGame = (Button)findViewById(R.id.button2);
        joinGame.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				Intent intent = new Intent(MainMenu.this, JoinGame.class);
	            MainMenu.this.startActivity(intent);
				
			}
		});
        
        // Sets up the "Logoff" button handler.
        Button logoff = (Button)findViewById(R.id.button4);
        logoff.setOnClickListener(new View.OnClickListener() {        	
			public void onClick(View v) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
				builder.setCancelable(true);
				builder.setTitle("Are you sure want to logout?");
				builder.setInverseBackgroundForced(true);
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();
						
						// Stores in the settings that the user no longer
						// wants their account to be remembered since they
						// logged out.
						SharedPreferences settings = getApplicationContext().getSharedPreferences("com.thundersnatch", Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean("remember_me", false).commit();
						
						finish();
						
						Intent intent = new Intent(MainMenu.this, LoginScreen.class);
			            MainMenu.this.startActivity(intent);
			            
					}
				});
				
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();

					}
				});
				
				AlertDialog alert = builder.create();
				alert.show();
				
			}
		});
        
        // Sets up the "Quit" button handler.
        Button quit = (Button)findViewById(R.id.button3);
        quit.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				finish();
				
			}
		});
        
     // Sets up the "Map - Developer" button handler.
        Button map = (Button)findViewById(R.id.button5);
        map.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				Intent intent = new Intent(MainMenu.this, PlayGame.class);
				intent.putExtra("GameID", 0);
				intent.putExtra("TeamID", 0);
				intent.putExtra("UserGameID", 34);
				intent.putExtra("TeamColor", "red");
				if(location != null){
					intent.putExtra("Longitude", (float)location.getLongitude());
					intent.putExtra("Latitude", (float)location.getLatitude());
				}else{
					intent.putExtra("Longitude", 0);
					intent.putExtra("Latitude", 0);
				}
	            MainMenu.this.startActivity(intent);
			}
		});
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }
    
    public void saveLocation(Location l){
    	location = l;
    }
}
